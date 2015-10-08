package com.mcnc.parecis.bizmob.view;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.speech.RecognizerIntent;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.mcnc.hsmart.controller.Smart2ProcessController;
import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.exception.RequestException;
import com.mcnc.hsmart.core.exception.ServerException;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.util.StringUtil;
import com.mcnc.hsmart.core.view.AbstractActivity;
import com.mcnc.hsmart.core.view.BaseWebView;
import com.mcnc.hsmart.core.view.param.LayoutParam;
import com.mcnc.hsmart.def.AppConfig;
import com.mcnc.hsmart.interfaces.MemoryInterface;
import com.mcnc.hsmart.interfaces.SqliteInterface;
import com.mcnc.hsmart.plugin.BizmobPluginManager;
import com.mcnc.hsmart.util.ExceptionUtil;
import com.mcnc.hsmart.view.ContextMenuButton;
import com.mcnc.hsmart.view.MainActivity;
import com.mcnc.hsmart.wrapper.ImageWrappter;
import com.mcnc.parecis.bizmob.def.Def;
import com.mcnc.parecis.bizmob.def.TaskID;
import com.mcnc.parecis.bizmob.task.SpeechRecognitionTask;
import com.mcnc.parecis.bizmob.util.ProjectUtils;
import com.mcnc.parecis.toyota.R;
import com.phonegap.WebViewReflect;

public class TabMainActivity extends MainActivity implements OnClickListener,
		OnTouchListener {

	private String back_action = "";

	private static final String TARGET_PAGE = "target_page";
	private final String TAG = this.toString();
	private long lastTouchTime = 0;

	// context menu 가 두개 이상일때만 사용 1개일 경우 기존처럼 사용한다.
	private HashMap<String, JSONObject> mapContext = new HashMap<String, JSONObject>();

	protected LinearLayout webLayout;
	protected LinearLayout rightHookLayout, leftHookLayout;

	protected ImageView rightHookImg, leftHookImg;
	protected LinearLayout rightBottomBaseLayout, rightToolbarLayout, rightBottomLayout;
	protected LinearLayout leftBottomBaseLayout, leftToolbarLayout, leftBottomLayout;

	protected Animation slideRightOut, slideRightIn, slideLeftOut, slideLeftIn, alpha_up, alpha_down ;
	
	private boolean blefttoolbarVisible = false;
	private boolean brighttoolbarVisible = false;
	protected boolean brightToolbar = false;
	protected boolean bleftToolbar = false;
	protected int toolbarCount = 0;
	
	private final int TOOLBAR_HEIGHT = 694;
	protected long lastClickTime = 0;
	protected String lastAction = "";
	protected long lastPostTime = 0;
	protected String lastTrCode = "";
	
	// Button Layout
	{
		paramLeftbtn = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		//paramLeftbtn.setMargins(10, 10, 2, 10);
		paramLeftbtn.gravity = Gravity.CENTER_VERTICAL;


		// 실제 사이즈대로 그림
		paramRightbtn = new LinearLayout.LayoutParams(LayoutParam.WW);
		//paramRightbtn.setMargins(2, 5, 5, 5);
		paramRightbtn.gravity = Gravity.CENTER_VERTICAL;

		paramRightbtns = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, 1);
		//paramRightbtns.setMargins(1, 5, 5, 5);
		paramRightbtns.gravity = Gravity.CENTER_VERTICAL;
	}

	/**
	 * Create and initialize web container.
	 */
	public void init() { // Create web container
		this.webView = new BaseWebView(this);
		this.webView.setId(100);

		WebViewReflect.checkCompatibility();

		this.webView.setInitialScale(100);
		this.webView.setVerticalScrollBarEnabled(true);
		this.webView.requestFocusFromTouch();
		this.webView.setFocusable(true);
		// Enable JavaScript
		WebSettings settings = this.webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		settings.setBuiltInZoomControls(true);
		//settings.setDisplayZoomControls(false);
		settings.setSupportZoom(true);
		settings.setPluginsEnabled(true);

		this.webView.addJavascriptInterface(SqliteInterface.getInstance(), "AndroidSqlite");
		this.webView.addJavascriptInterface(MemoryInterface.getInstance(), "MemoryMap");
		// plugin 추가
		this.webView.addJavascriptInterface(new BizmobPluginManager(this, webView), "BizmobPluginMgr");
		this.webView.setBackgroundColor(AppConfig.WEBVIEW_BACKGROUNDCOLOR_NORMAL);
		// Clear cancel flag
		this.cancelLoadUrl = false;
	}

	@Override
	public void onDestroy() {
		if (webView != null && webView.getUrl() != null) {
			Logger.d(TAG, "onDestory!!!!!!!!!!!!!!!!!!!");
			//webView.getSettings().setBuiltInZoomControls(false);
			webView.clearCache(false);
			webView.clearAnimation();
			webView.clearHistory();
			webView.freeMemory();
			
			webView.clearView();
			webView.destroyDrawingCache();
			//webView.getSettings().setBuiltInZoomControls(true);
			webView.destroy();
			webView = null;
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void loadUrl(final String url) {
		if ( webView != null  && webView.getUrl() != null ) {
			webView.clearCache(false);
			webView.clearAnimation();
			webView.clearHistory();
			webView.freeMemory();
			webView.clearView();
			webView.destroyDrawingCache();
		}
		this.webView.loadUrl(url);
	}

	@Override
	/**
	 * Called when the system is about to start resuming a previous activity. 
	 */
	protected void onPause() {
		super.onPause();

		if (contextAnimation) {
			Animation sOut = AnimationUtils.loadAnimation(this,
					R.anim.slide_out);
			contextMenuLayout.startAnimation(sOut);
			contextMenuLayout.setVisibility(View.INVISIBLE);
			contextAnimation = false;
		}
	}

	@Override
	/**
	 * Called when an activity you launched exits, giving you the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it. 
	 * 
	 * @param requestCode		The request code originally supplied to startActivityForResult(), 
	 * 							allowing you to identify who this result came from.
	 * @param resultCode		The integer result code returned by the child activity through its setResult().
	 * @param data				An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if ( blefttoolbarVisible ){
			leftHookImg.setVisibility(View.VISIBLE);
			try {
				Drawable d = ImageWrappter.getDrawableAsset("native/bizmob_handle_left_on.png", this);
				leftHookImg.setImageDrawable(d);
			} catch (Exception e) {
				e.printStackTrace();
				leftHookImg.setImageDrawable(getResources().getDrawable( R.drawable.bizmob_handle));
			}
		}
		if ( brighttoolbarVisible ){
			rightHookImg.setVisibility(View.VISIBLE);
			try {
				Drawable d = ImageWrappter.getDrawableAsset("native/bizmob_handle_right_on.png", this);
				rightHookImg.setImageDrawable(d);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (requestCode == SpeechRecognitionTask.SPEECH_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard 
            ArrayList<String> matches = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            
            JSONObject jsonObject = new JSONObject();
            JSONArray array = new JSONArray();
            
            for (int i = 0; i < matches.size(); i++) {
            	Logger.d("VOICE TEST: ", matches.get(i));
				array.put(matches.get(i));
			}
            
            try {
				jsonObject.put("result", true);
				jsonObject.put("resultCode", "");
				jsonObject.put("resultData", array);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            webView.loadUrl("javascript:" + getCallback_name() + "(" + jsonObject + ")");
        }
		if ( webView != null ) {
			webView.loadUrl("javascript:appcallOnFocus()");
		}
	}

	// end

	protected LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT);

	protected LinearLayout.LayoutParams a = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

	protected LinearLayout.LayoutParams b = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

	protected LinearLayout.LayoutParams c = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
	{
		a.leftMargin = 15;
		a.rightMargin = 0;

		b.leftMargin = 0;
		b.rightMargin = 0;

		c.leftMargin = 0;
		c.rightMargin = 15;

	}

	protected void checkOrientation() {
		super.checkOrientation();
	}

	@Override
	protected void setHeader(LinearLayout baseLayout) {
		baseLayout.setOrientation(LinearLayout.HORIZONTAL);
		if (webLayout == null) {
			webLayout = new LinearLayout(this);
		}
		webLayout.setOrientation(LinearLayout.VERTICAL);
		// headerLayout
		headerLayout = new LinearLayout(this);
		// headerLayout.setBackgroundColor(Color.rgb(255, 255, 255));
		LinearLayout.LayoutParams headerLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, Def.TITLE_H, 0
		/*
		 * 50 LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
		 */);
		LinearLayout.LayoutParams headerInParam = new LinearLayout.LayoutParams(
				Def.TOP_BUTTON_W, Def.TITLE_H, 0);
		LinearLayout.LayoutParams headerCenterParam = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, Def.TITLE_H, 1);

		Logger.d(TAG, "headerLayout : " + headerLayout);
		/*
		 * try { headerLayout.setBackgroundDrawable(ImageWrappter.getDrawable(
		 * "app/images/common/bg_title.png", this)); } catch (IOException e) {
		 * e.printStackTrace(); }
		 */

		headerLeftLayout = new LinearLayout(this);
		headerTitleLayout = new LinearLayout(this);
		headerRightLayout = new LinearLayout(this);

		headerTitleLayout.setOnTouchListener(this);

		headerLayout.addView(headerLeftLayout, headerInParam);
		headerLayout.addView(headerTitleLayout, headerCenterParam);
		headerLayout.addView(headerRightLayout, headerInParam);

		webLayout.addView(headerLayout, headerLayoutParam);
		baseLayout.addView(webLayout, new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
		
		// default 설정 적용
		if ( AppConfig.DEFAULT_TITLEBAR_VISIBLE ){
			headerLayout.setVisibility(View.VISIBLE);
			if (AppConfig.DEFAULT_TITLEBAR_IMAGE.length() > 0 ) {
				try {
					headerLayout.setBackgroundDrawable(ImageWrappter.getDrawable( AppConfig.DEFAULT_TITLEBAR_IMAGE, this));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			headerLayout.setVisibility(View.GONE);
		}
	}

	@Override
	protected void setBody(LinearLayout baseLayout) {
		// bodyLayout
		// LinearLayout bodyLayout = new LinearLayout(this);
		bodyFramLayout = new FrameLayout(this);
		// testLayout.setBackgroundColor(Color.rgb(255, 255, 255));
		LinearLayout.LayoutParams bodyLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		init();

		Intent intent = getIntent();
		if (intent.hasExtra("targetPage")) {
			String targetPage = intent.getExtras().getString("targetPage");
			Logger.d(TAG, "targetPage::" + targetPage);
			//this.deleteDatabase("webview.db");
			//this.deleteDatabase("webviewCache.db");
			String url = ImageWrappter.getUri(targetPage);
			//loadUrl(url);
			loadUrl(webView, url, intent);

			if (targetPage.equals("main/html/main.html")) {
				isHomeActivity = true;
			}

			if (Def.IS_RESTART_BY_LOW_MEMORY) {
				Logger.d(TAG, "메모리부족으로 프로그램이 강제종료되었습니다. 작업관리자에서 메모리 정리를 해주세요.");
				Toast.makeText(this,
						"메모리부족으로 프로그램이 강제종료되었습니다. 작업관리자에서 메모리를 정리하면 도움이됩니다.",
						Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}

		// set modalActivity
		if (intent.hasExtra("modal")) {
			modal = intent.getExtras().getBoolean("modal");
		}
		if (intent.hasExtra("orientation")) {
			String strOr = intent.getExtras().getString("orientation");
			if (strOr.startsWith("land")) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} else if (strOr.startsWith("portrait")) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else if (strOr.startsWith("none")) {
				// 고정일경우는 값이 0 고정되지않았을경우는 1을 리턴한다.
				int orient = android.provider.Settings.System
						.getInt(getContentResolver(),
								android.provider.Settings.System.ACCELEROMETER_ROTATION,
								0);
				if (orient == 0) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				}
			} else {
				Logger.d(TAG, "request orientation :" + strOr + "set to all");
			}
		}// Logger.d(TAG, "bodyLayout : " + bodyLayout);
		bodyFramLayout.addView(webView, 0);
		// bodyLayout.addView(webView);
		webLayout.addView(bodyFramLayout, bodyLayoutParam);
	}

	@Override
	protected void setBottom(LinearLayout baseLayout) {
		// left
		if (leftHookLayout == null) {
			leftHookLayout = new LinearLayout(this);
			leftHookImg = new ImageView(this);
			
			Drawable d = null;
			try {
				d = ImageWrappter.getDrawableAsset("native/bizmob_handle_left_on.png", this);
				leftHookImg.setImageDrawable(d);
			} catch (Exception e) {
				e.printStackTrace();
				leftHookImg.setImageDrawable(getResources().getDrawable( R.drawable.bizmob_handle));
			}
			leftHookImg.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			leftHookImg.setOnClickListener(this);
		}

		leftHookLayout.setOrientation(LinearLayout.HORIZONTAL);

		leftBottomBaseLayout = new LinearLayout(this);
		leftBottomBaseLayout.setOrientation(LinearLayout.VERTICAL);
		leftBottomBaseLayout.setGravity(Gravity.RIGHT);
		if (leftBottomLayout == null) {
			leftBottomLayout = new LinearLayout(this);
		}
		leftBottomLayout.setLayoutParams(new LinearLayout.LayoutParams(
				Def.BOTTOM_H, TOOLBAR_HEIGHT));

		// flipper = new ViewFlipper(this);
		// 툴바를 그려주는 부분
		leftToolbarLayout = new LinearLayout(this);
		leftToolbarLayout.setLayoutParams(new LayoutParams(Def.BOTTOM_H,
				TOOLBAR_HEIGHT ));
		leftToolbarLayout.setGravity(Gravity.CENTER);

		leftBottomLayout.addView(leftToolbarLayout);

		
		/** 툴바가 컨텐츠 위로 뜨도록 해야기때문에 영역을 잡지 않도록한다 */
		leftBottomBaseLayout.addView(leftBottomLayout);
		leftBottomBaseLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, TOOLBAR_HEIGHT ));

		leftHookLayout.addView(leftBottomBaseLayout);
		leftHookLayout.addView(leftHookImg);
		leftHookLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		leftHookLayout.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
		frameLayout.addView(leftHookLayout);

		// page를 다 그린후 보여준다.
		//leftHookLayout.setVisibility(View.GONE);
		leftHookImg.setVisibility(View.GONE);
		
		
		// right
		if (rightHookLayout == null) {
			rightHookLayout = new LinearLayout(this);
			rightHookImg = new ImageView(this);
			//rightHookImg.setImageDrawable(getResources().getDrawable(R.drawable.bizmob_handle));
			Drawable d = null;
			try {
				d = ImageWrappter.getDrawableAsset("native/bizmob_handle_right_on.png", this);
				rightHookImg.setImageDrawable(d);
			} catch (Exception e) {
				e.printStackTrace();
				rightHookImg.setImageDrawable(getResources().getDrawable(R.drawable.bizmob_handle));
			}
			rightHookImg.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			//hookImg.setAlpha(70);
			rightHookImg.setOnClickListener(this);
		}

		rightHookLayout.setOrientation(LinearLayout.HORIZONTAL);

		rightBottomBaseLayout = new LinearLayout(this);
		rightBottomBaseLayout.setOrientation(LinearLayout.VERTICAL);
		rightBottomBaseLayout.setGravity(Gravity.RIGHT);
		if (rightBottomLayout == null) {
			rightBottomLayout = new LinearLayout(this);
		}
		rightBottomLayout.setLayoutParams(new LinearLayout.LayoutParams(
				Def.BOTTOM_H, TOOLBAR_HEIGHT));

		rightBottomLayout = new LinearLayout(this);
		rightBottomLayout.setLayoutParams(new LinearLayout.LayoutParams(
				Def.BOTTOM_H, LayoutParams.FILL_PARENT));

		// flipper = new ViewFlipper(this);
		// 툴바를 그려주는 부분
		rightToolbarLayout = new LinearLayout(this);
		rightToolbarLayout.setLayoutParams(new LayoutParams(Def.BOTTOM_H,
				TOOLBAR_HEIGHT));
		rightToolbarLayout.setGravity(Gravity.CENTER);

		rightBottomLayout.addView(rightToolbarLayout);

		/** 툴바가 컨텐츠 위로 뜨도록 해야기때문에 영역을 잡지 않도록한다 */
		rightBottomBaseLayout.addView(rightBottomLayout);
		rightBottomBaseLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, TOOLBAR_HEIGHT ));

		rightHookLayout.addView(rightHookImg);
		rightHookLayout.addView(rightBottomBaseLayout);
		rightHookLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT ));
		rightHookLayout.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		frameLayout.addView(rightHookLayout);

		// page를 다 그린후 보여준다.
		rightHookImg.setVisibility(View.GONE);
	}
	
	

	@Override
	public void onClick(View v) {
		/*
		 * ContextMenu가 클릭이 된 경우 사라지도록 함
		 */
		if (v == rightHookImg) {
			Logger.d(TAG, "onClick rightHookImg ");
			if ( contextAnimation ) {
				if (  rightHookLayout.getChildAt(0) == contextMenuLayout ){
					rightHookLayout.removeViewAt(0);
					contextMenuLayout.startAnimation(alpha_up);
					contextAnimation =  ! contextAnimation;
				}
			}
			rightToggleToolbar();
			return;
		} else if (v == leftHookImg ) {
			Logger.d(TAG, "onClick leftHookImg ");
			if ( contextAnimation ) {
				if ( leftHookLayout.getChildAt(1) == contextMenuLayout ) {
					leftHookLayout.removeViewAt(1);
					contextMenuLayout.startAnimation(alpha_up);
					contextAnimation =  ! contextAnimation;
				}
			}
			leftToggleToolbar();
			return;
		}

		if (v instanceof ContextMenuButton) {
			if (contextAnimation) {
				if ( leftHookLayout.getChildAt(1) == contextMenuLayout ){
					leftHookLayout.removeViewAt(1);
				} else if (  rightHookLayout.getChildAt(0) == contextMenuLayout ){
					rightHookLayout.removeViewAt(0);
				}
				contextMenuLayout.startAnimation(alpha_up);
				rightHookImg.setVisibility(View.VISIBLE);
				leftHookImg.setVisibility(View.VISIBLE);
				//contextMenuLayout.setVisibility(View.INVISIBLE);
			} 
		}
		onAction(v);
	}

	@Override
	public void onError(final Response response) {
		hideProgress();
		if (response == null)
			return;

		String msg = null;
		// 에러시에도 Web으로 콜백을 줄경우 result data가 필요해지므로 Exception으로 만드는것은 최후에 한다.
		if (response.getData() instanceof JSONObject) {
			ServerException e = ExceptionUtil
					.toServerException((JSONObject) response.getData());
			msg = ExceptionUtil.process(e);
		} else if (response.getData() instanceof String) {
			msg = (String) response.getData();
		} else {
			msg = ExceptionUtil.process((Exception) response.getData());
		}

		/*
		 * 백버튼으로 화면이 이미 전환 된 상황에서 웹에서 다시 명령이 내려오면서 Activity가 finish 된 상황에서도 호출이
		 * 발생한 경우
		 * 
		 * finish 유무를 확인 하고 처리.
		 */
		if (this.isFinishing()) {
			return;
		}
		
		// 통신관련 에러는 웹으로 보내주기만하고 메시지 박스를 띄워주지 않음
		Request req = response.getRequest();
		if ( req != null ) {
			String id = req.getID();
			if (  id.equals(TaskID.TASK_ID_RELOAD_WEB) 
					|| id.equals( TaskID.TASK_ID_AUTH )
					|| id.equals( TaskID.TASK_ID_DOWNLOAD_IMAGE)) {
				return;
			}
		}
		

		new AlertDialog.Builder(this)
		.setMessage(msg)
		.setPositiveButton(/* R.string.app_common_ok */"확인",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create().show();	
	
	}

	@Override
	public void onSuccess(Response response) {
		if (response.getRequest().getID()
				.equals(TaskID.TASK_ID_SPEECH_RECOGNITION)) {
			setCallback_name((String) response.getData());
		} else if (response.getRequest().getID()
				.equals(TaskID.TASK_ID_QR_AND_BAR_CODE)) {
			setCallback_name(null);
		} else {
			setCallback_name(null);
		}
		super.onSuccess(response);
	}

	@Override
	protected void onAction(View view) {
		Request request = new Request();
		request.setSrcActivity(this);
		try {
			Logger.f(TAG, "actionType::" + view.getTag(R.id.actionType));
			Logger.f(TAG, "action::" + view.getTag(R.id.action));
			Logger.f(TAG, "tag::" + view.getTag());
			
			// 짧은 시간 안에 두번 클릭 못하도록
			if ( ! checkButtonAction ( view ) ) {
				return;
			}

			if (view.getTag(R.id.control_id) != null
					&& ((String) view.getTag(R.id.control_id))
							.startsWith("MHUpDown")) {
				if (((String) view.getTag(R.id.control_id))
						.equals("MHUpDown_DOWN")) {
					Logger.d(TAG,
							"jscall::javascript:" + view.getTag(R.id.action)
									+ "(\"DOWN\")");
					webView.loadUrl("javascript:" + view.getTag(R.id.action)
							+ "(\"DOWN\")");
				}
				if (((String) view.getTag(R.id.control_id))
						.equals("MHUpDown_UP")) {
					Logger.d(TAG,
							"jscall::javascript:" + view.getTag(R.id.action)
									+ "(\"UP\")");
					webView.loadUrl("javascript:" + view.getTag(R.id.action)
							+ "(\"UP\")");
				}
				return;
			}

			if (view.getTag(R.id.actionType) != null
					&& ((String) view.getTag(R.id.actionType)).equals("jscall")) {
				Logger.d(TAG, "jscall::javascript:" + view.getTag(R.id.action)
						+ "()");
				if (webView != null) {
					webView.loadUrl("javascript:" + view.getTag(R.id.action)
							+ "()");
				}
				if ( contextMenuLayout != null) {
					if ( leftHookLayout.getChildAt(1) == contextMenuLayout ){
						leftHookLayout.removeViewAt(1);
					} else if (  rightHookLayout.getChildAt(0) == contextMenuLayout ){
						rightHookLayout.removeViewAt(0);
					}
					contextMenuLayout.startAnimation(alpha_up);
					rightHookImg.setVisibility(View.VISIBLE);
					leftHookImg.setVisibility(View.VISIBLE);
					contextAnimation = false;
				}
				
			} else if (view.getTag(R.id.actionType) != null
					&& ((String) view.getTag(R.id.actionType)).equals("popup")) {
				
				if (!contextAnimation ) {
					Logger.d(TAG, "createContextMenu !!!!!!!! ");
					String seq = (String) view.getTag(R.id.control_seq);
					if (contextMenuLayout != null) {
						contextMenuLayout.removeAllViews();
					}
					createContextMenu2(mapContext.get(seq), seq);
				}
				if (!view.getTag(R.id.action).toString().equals("")) {
					execute((String) view.getTag(R.id.action), request,
							false, 0);
				}
				
				/*if (mapContext.size() > 1) {
					if (!contextAnimation) {
						Logger.d(TAG, "createContextMenu !!!!!!!! ");
						String seq = (String) view.getTag(R.id.control_seq);
						if (contextMenuLayout != null) {
							contextMenuLayout.removeAllViews();
						}
						createContextMenu2(mapContext.get(seq));
					}
					if (!view.getTag(R.id.action).toString().equals("")) {
						execute((String) view.getTag(R.id.action), request,
								false, 0);
					}
				} else {
					if (!view.getTag(R.id.action).toString().equals("")) {
						execute((String) view.getTag(R.id.action), request,
								false, 0);
					}
				}*/
			} else {
				Logger.d(TAG, "taskID::" + view.getTag(R.id.action));
				if (!view.getTag(R.id.action).toString().equals("")) {
					execute((String) view.getTag(R.id.action), request, false,
							0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Response response = new Response();
			response.setData(e.toString());
			onError(response);
		}
	}

	@Override
	protected void onAction(WebView wv, String requestURL) {
		Logger.d(TAG, "requestURLength::" + requestURL.length());
		Logger.f(TAG, "requestURL::" + requestURL);
		try {
			if (requestURL.startsWith("http://") || requestURL.startsWith("https://") ) {
				if ( ProjectUtils.isAllowUrl(requestURL)) {
					loadUrl(requestURL);
				}
				return;
			}else if (requestURL.startsWith("file://")) {
				loadUrl(requestURL);
				return;
			} else if (!requestURL.startsWith("mcnc://")) {
				throw new RequestException("invalidRequestSchema");
			}
			// requestURL = requestURL.replaceAll("%C2%A0", "%20");
			// Logger.d(TAG, "requestURL::replaced%C2%A0" + requestURL);
			long start = System.currentTimeMillis();
			String decodedURL = URLDecoder.decode(requestURL, "utf-8");
			long stop = System.currentTimeMillis();
			Logger.i(TAG, "URLDecodingElapsedTime::" + (stop - start));

			start = System.currentTimeMillis();
			JSONObject decodedData = new JSONObject(decodedURL.substring(7));
			stop = System.currentTimeMillis();
			Logger.i(TAG, "generateJSONElapsedTime::" + (stop - start));
			Logger.f(TAG, "decodedData::" + decodedData);
			JSONObject param = decodedData.getJSONObject("param");
			final String id = decodedData.getString("id");
			if (id.equals("SET_APP") || id.equals("SET_APP_TABLET")) {
				setContainer(param);
			} else {
				if ( ! checkUrlAction(param) ) {
					return;
				}
				
				String targetPage = StringUtil.getString(param, TARGET_PAGE);
				Logger.f(TAG, "targetPage::" + targetPage);

				// create request
				request = new Request();
				request.setSrcActivity(this);
				Smart2ProcessController controller = (Smart2ProcessController) getController();
				Class activity = null;
				activity = controller
						.getActivityClass(Smart2ProcessController.MAIN_ACTIVITY);
				request.setTargetActivity(activity);

				request.setCallType(StringUtil.getString(decodedData,
						"call_type"));
				request.setTargetPage(targetPage);
				request.setData(decodedData);
				request.setTrCode(StringUtil.getString(param, "trcode"));

				if (id.equals("GOTO_WEB_MODAL") || id.equals("GOTO_WEB")
						|| id.equals("GOTO_FILE_UPLOAD")) {
					// call controller
					execute(id,
							request,
							true,
							AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
				} else if (id.equals("RELOAD_WEB") || id.equals("REPLACE_WEB")) {
					// call controller
					execute(id,
							request,
							true,
							AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
				} else {
					// call controller
					execute(id, request, false, 0);
				}
			}
		} catch (RequestException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			Response response = new Response();
			response.setData(e.toString());
			onError(response);
		}
	}

	protected void setContainer(JSONObject param) throws JSONException,
			IOException {
		
		LinearLayout.LayoutParams buttonMargineParam = new LinearLayout.LayoutParams( 10, Def.TITLE_H);
		// titlebar
		if (param.has("titlebar")) {
			JSONObject titlebar = param.getJSONObject("titlebar");

			if (titlebar.has("image_name")) {
				String imageName = titlebar.getString("image_name");
				if (!imageName.equals("")) {
					try {
						headerLayout.setBackgroundDrawable(ImageWrappter
								.getDrawable(imageName, this));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (titlebar.has("title")) {
				String titleText = titlebar.getString("title").trim();
				titleTv = new TextView(this);
				titleTv.setGravity(Gravity.CENTER);
				headerTitleLayout.removeAllViews();
				if (!titleText.equals("")) {
					titleTv.setText(titleText);
					titleTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, Def.top_DipFontSize);
					titleTv.setTextColor(Def.topTitleColor_02);
					headerTitleLayout.addView(titleTv, LayoutParam.FF);
				}
			}

			if (titlebar.has("visible")) {
				if (titlebar.getBoolean("visible")) {
					headerLayout.setVisibility(View.VISIBLE);
				} else {
					headerLayout.setVisibility(View.GONE);
				}
			}

			if (titlebar.has("left") && titlebar.getJSONArray("left").length() > 0 ) {
				headerLeftLayout.removeAllViews();
				JSONArray left = titlebar.getJSONArray("left");
				String buttonText, imageName, actionType, action, control_id;
				for ( int i = 0; i < left.length(); i++ ) {
					if ( ! isButtonObject (left.get(i)) ){
						Logger.d(TAG, "not Button Object left : " + i + " " + left.get(i));
						continue;
					}
					LinearLayout margine = new LinearLayout (this);
					margine.setLayoutParams(buttonMargineParam);

					JSONObject jsonToolbar = left.getJSONObject(i);
					buttonText = jsonToolbar.getString("button_text");
					imageName = jsonToolbar.getString("image_name");
					actionType = jsonToolbar.getString("action_type");
					action = jsonToolbar.getString(ACTION);
					control_id = jsonToolbar.getString("control_id");
					if (control_id.equals("MHBarButton")) {
						View backBtn = null;
						Drawable d = null;
						if (!imageName.equals("")) {
							backBtn = new ImageView(this);
							try {
								d = ImageWrappter.getDrawable(imageName, this);
								backBtn.setBackgroundDrawable(d);
							} catch (IOException e) {
								e.printStackTrace();
								if ( buttonText.length() == 0 ) {
									buttonText = "?";
								}
								backBtn = new TextView(this);
								((TextView) backBtn).setText(buttonText);
								((TextView) backBtn).setTextColor( Color.RED );
								((TextView) backBtn).setTextSize( Def.top_DipFontSize);
								((TextView) backBtn).setGravity(Gravity.CENTER_VERTICAL);
							}
						} else {
							// 이미지를 찾지 못할경우  텍스트 버튼으로 
							backBtn = new TextView(this);
							/*if ( buttonText.length() == 0 ) {
								buttonText = "?";
							}*/
							((TextView) backBtn).setText(buttonText);
							((TextView) backBtn).setTextColor( Color.RED );
							((TextView) backBtn).setTextSize( Def.top_DipFontSize);
							((TextView) backBtn).setGravity(Gravity.CENTER_VERTICAL);
						}
						backBtn.setTag(R.id.action, action);
						backBtn.setTag(R.id.actionType, actionType);
						backBtn.setOnClickListener(this);
						headerLeftLayout.addView(margine, buttonMargineParam);
						headerLeftLayout.addView(backBtn, paramLeftbtn);
					}
				}
			} else {
				if (!isHomeActivity) {
					if (!modal) {
						LinearLayout margine = new LinearLayout (this);
						margine.setLayoutParams(buttonMargineParam);
						if ( headerLeftLayout != null ) {
							headerLeftLayout.removeAllViews();
						}
						
						ImageButton backBtn = new ImageButton(this);
						Drawable d = null;
						try {
							if (AppConfig.DEFAULT_TITLEBAR_BACKBUTTON_IMAGE.length() > 0 ) {
								try {
									d = ImageWrappter.getDrawable( AppConfig.DEFAULT_TITLEBAR_BACKBUTTON_IMAGE, this);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}else{
								d = ImageWrappter.getDrawableAsset ( "native/back_btn.png" , this);
							}
							
							backBtn.setBackgroundDrawable(d);
						} catch (IOException e) {
							e.printStackTrace();
						}
						backBtn.setTag(R.id.action,
								TaskID.TASK_ID_ACTIVITYFINISH);
						backBtn.setOnClickListener(this);
						View topLeftView = headerLeftLayout.getChildAt(0);
						Logger.d(TAG, "topLeftView::" + topLeftView);
						
						headerLeftLayout.addView(margine, buttonMargineParam);
						headerLeftLayout.addView(backBtn, 0, paramLeftbtn);
					}
				}
			}

			if (titlebar.has("right") && titlebar.getJSONArray("right").length() > 0) {
				JSONArray right = titlebar.getJSONArray("right");
				String buttonText, imageName, actionType, action, control_id;
				if (headerRightLayout != null) {
					headerRightLayout.removeAllViews();
				}
				for ( int i = 0; i < right.length(); i ++ ) {
					if ( ! isButtonObject (right.get(i)) ){
						Logger.d(TAG, "not Button Object left : " + i + " " + right.get(i));
						continue;
					}
					JSONObject jsonToolbar = right.getJSONObject(i);
					buttonText = jsonToolbar.getString("button_text");
					imageName = jsonToolbar.getString("image_name");
					actionType = jsonToolbar.getString("action_type");
					control_id = jsonToolbar.getString("control_id");
					action = jsonToolbar.getString(ACTION);

					if (control_id.equals("MHUpDown")) {
						LinearLayout margine = new LinearLayout (this);
						margine.setLayoutParams(buttonMargineParam);

						updownBtn[0] = new ImageButton(this);
						updownBtn[0].setTag(R.id.action, action);
						updownBtn[0].setTag(R.id.actionType, actionType);
						updownBtn[0].setOnClickListener(this);
						// App 에서 이미지 관리
						try {
							Drawable d = ImageWrappter.getDrawableAsset(
									"native/btn_up.png", this);
							updownBtn[0].setBackgroundDrawable(d);
							updownBtn[0].setTag(R.id.control_id, "MHUpDown_UP");
						} catch (Exception e) {
							e.printStackTrace();
						}
						headerRightLayout.addView(margine, buttonMargineParam);
						headerRightLayout.addView(updownBtn[0], paramRightbtn);
						headerRightLayout.setGravity(Gravity.RIGHT | Gravity.CENTER);

						LinearLayout margine2 = new LinearLayout (this);
						margine2.setLayoutParams(buttonMargineParam);

						updownBtn[1] = new ImageButton(this);
						updownBtn[1].setTag(R.id.action, action);
						updownBtn[1].setTag(R.id.actionType, actionType);
						updownBtn[1].setOnClickListener(this);
						try {
							Drawable d = ImageWrappter.getDrawableAsset(
									"native/btn_down.png", this);
							updownBtn[1].setBackgroundDrawable(d);
							updownBtn[1].setTag(R.id.control_id, "MHUpDown_DOWN");
						} catch (Exception e) {
							e.printStackTrace();
						}
						headerRightLayout.setGravity(Gravity.RIGHT | Gravity.CENTER);
						headerRightLayout.addView(margine2, buttonMargineParam);
						headerRightLayout.addView(updownBtn[1], paramRightbtn);
					} else if (control_id.equals("MHBarButton") ){ // MHBarButton
						LinearLayout margine = new LinearLayout (this);
						margine.setLayoutParams(buttonMargineParam);

						View imgV = new ImageView(this);
						if (!imageName.equals("")) {
							try {
								imgV.setBackgroundDrawable(ImageWrappter.getDrawable(imageName, this));
							} catch (IOException e) {
								e.printStackTrace();
								if ( buttonText.length() == 0 ) {
									buttonText = "?";
								}
								imgV = new TextView(this);
								((TextView) imgV).setText(buttonText);
								((TextView) imgV).setTextColor( Color.RED );
								((TextView) imgV).setTextSize( Def.top_DipFontSize);
								((TextView) imgV).setGravity(Gravity.CENTER_VERTICAL);
							}
						} else {
							/*if ( buttonText.length() == 0 ) {
								buttonText = "?";
							}*/
							imgV = new TextView(this);
							((TextView) imgV).setText(buttonText);
							((TextView) imgV).setTextColor( Color.RED );
							((TextView) imgV).setTextSize( Def.top_DipFontSize);
							((TextView) imgV).setGravity(Gravity.CENTER_VERTICAL);
						}
						imgV.setOnClickListener(this);
						imgV.setTag(R.id.action, action);
						imgV.setTag(R.id.actionType, actionType);
						headerRightLayout.addView( imgV, paramRightbtn);
						headerRightLayout.addView(margine, buttonMargineParam);
						headerRightLayout.setGravity(Gravity.RIGHT | Gravity.CENTER);
					}
				}
			} else {
				if (headerRightLayout != null) {
					headerRightLayout.removeAllViews();
				}
			}
		}
		
		// left_toolbar
		if (param.has("left_toolbar")) {
			JSONObject toolbar = param.getJSONObject("left_toolbar");
			if (toolbar.has("image_name")) {
				String image_name = toolbar.getString("image_name");
				if (image_name.length() > 0) {
					try {
						leftToolbarLayout.setBackgroundDrawable(ImageWrappter
								.getDrawable(image_name, this));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (toolbar.has("visible")) {
				if (toolbar.getBoolean("visible")) {
					blefttoolbarVisible = true;
					leftBottomLayout.setVisibility(View.VISIBLE);
					leftHookImg.setVisibility(View.VISIBLE);
				} else {
					blefttoolbarVisible = false;
					leftBottomLayout.setVisibility(View.GONE);
					leftHookImg.setVisibility(View.GONE);
				}
			}
			
			if (toolbar.has("buttons") && toolbar.getJSONArray("buttons").length() > 0 ) {
				String buttonText, imageName, actionType, action;
				JSONArray arrayButtons = toolbar.getJSONArray("buttons");
				// SET_APP 인 경우 무조건 전체가 내려오며 REPLACE 처리 하도록 한다.
				leftToolbarLayout.removeAllViews();
				leftToolbarLayout.setOrientation(LinearLayout.VERTICAL);
				// context menu가 몇개 있는지 확인
				for (int i = 0; i < arrayButtons.length(); i++) {
					if ( ! isButtonObject (arrayButtons.get(i)) ){
						Logger.d(TAG, "not Button Object arrayButtons : " + i + " " + arrayButtons.get(i));
						continue;
					}
					JSONObject jsonToolbar = (JSONObject) arrayButtons.get(i);
					actionType = jsonToolbar.getString("action_type");
					if (actionType.equals("popup")) {
						mapContext.put(String.valueOf( 10 + i), jsonToolbar);
					}
				}

				for (int i = 0; i < arrayButtons.length(); i++) {
					if ( ! isButtonObject (arrayButtons.get(i)) ){
						Logger.d(TAG, "not Button Object arrayButtons : " + i + " " + arrayButtons.get(i));
						continue;
					}
					JSONObject jsonToolbar = (JSONObject) arrayButtons.get(i);
					buttonText = jsonToolbar.getString("button_text");
					imageName = jsonToolbar.getString("image_name");
					actionType = jsonToolbar.getString("action_type");
					action = jsonToolbar.getString(ACTION);

					Logger.d(TAG, "buttonText::" + buttonText);
					Logger.d(TAG, "imageName::" + imageName);
					Logger.d(TAG, "actionType::" + actionType);
					Logger.d(TAG, "action::" + action);

					LinearLayout toolBtnLayout = new LinearLayout(this);
					toolBtnLayout.setLayoutParams(LayoutParam.FF1);
					

					View img = null;
					// Image를 못찾을 경우 textbutton으로 
					Drawable d = null;
					if (!imageName.equals("")) {
						try {
							d = ImageWrappter.getDrawable(imageName, this);
							img = new ImageView(this);
							((ImageView) img).setImageDrawable (d);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						img = new TextView (this);
						((TextView)img).setText(buttonText);
						((TextView)img).setTextColor( Color.RED );
						((TextView)img).setTextSize( Def.top_DipFontSize);
						((TextView)img).setGravity(Gravity.CENTER_VERTICAL);
						((TextView)img).setTag(R.id.action, action);
						((TextView)img).setTag(R.id.actionType, actionType);
					}
					img.setLayoutParams(LayoutParam.FF);
					img.setOnClickListener(this);

					// 팝업메뉴처리
					if (actionType.equals("popup")) {
						img.setTag(R.id.action, TaskID.TASK_ID_SHOWMOREDATA);
						img.setTag(R.id.control_seq, String.valueOf(10 + i));
					} else {
						img.setTag(R.id.action, action);
						img.setTag(R.id.control_seq, String.valueOf(i));
					}
					img.setTag(R.id.actionType, actionType);
					img.setBackgroundColor(00000000);
					toolBtnLayout.addView(img);
					leftToolbarLayout.addView(toolBtnLayout);
				}
			}
		} else {
			blefttoolbarVisible = false;
			leftBottomLayout.setVisibility(View.GONE);
			leftHookImg.setVisibility(View.GONE);
		}

		
		// toolbar
		if (param.has("right_toolbar")) {
			JSONObject toolbar = param.getJSONObject("right_toolbar");
			if (toolbar.has("image_name")) {
				String image_name = toolbar.getString("image_name");
				if (image_name.length() > 0) {
					try {
						rightToolbarLayout.setBackgroundDrawable(ImageWrappter
								.getDrawable(image_name, this));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			if (toolbar.has("visible")) {
				if (toolbar.getBoolean("visible")) {
					brighttoolbarVisible = true;
					rightBottomLayout.setVisibility(View.VISIBLE);
					rightHookImg.setVisibility(View.VISIBLE);
				} else {
					brighttoolbarVisible = false;
					rightBottomLayout.setVisibility(View.GONE);
					rightHookImg.setVisibility(View.GONE);
				}
			}
			
			if (toolbar.has("buttons") && toolbar.getJSONArray("buttons").length() > 0) {
				String buttonText, imageName, actionType, action;
				JSONArray arrayButtons = toolbar.getJSONArray("buttons");
				// SET_APP 인 경우 무조건 전체가 내려오며 REPLACE 처리 하도록 한다.
				rightToolbarLayout.removeAllViews();
				rightToolbarLayout.setOrientation(LinearLayout.VERTICAL);
				
				// context menu가 몇개 있는지 확인
				// Left context  1x  Right context  2x
				for (int i = 0; i < arrayButtons.length(); i++) {
					if ( ! isButtonObject (arrayButtons.get(i)) ){
						Logger.d(TAG, "not Button Object arrayButtons : " + i + " " + arrayButtons.get(i));
						continue;
					}
					JSONObject jsonToolbar = (JSONObject) arrayButtons.get(i);
					actionType = jsonToolbar.getString("action_type");
					if (actionType.equals("popup")) {
						mapContext.put(String.valueOf(20+i), jsonToolbar);
					}
				}

				for (int i = 0; i < arrayButtons.length(); i++) {
					if ( ! isButtonObject (arrayButtons.get(i)) ){
						Logger.d(TAG, "not Button Object arrayButtons : " + i + " " + arrayButtons.get(i));
						continue;
					}
					JSONObject jsonToolbar = (JSONObject) arrayButtons.get(i);
					buttonText = jsonToolbar.getString("button_text");
					imageName = jsonToolbar.getString("image_name");
					actionType = jsonToolbar.getString("action_type");
					action = jsonToolbar.getString(ACTION);

					
					LinearLayout toolBtnLayout = new LinearLayout(this);
					toolBtnLayout.setLayoutParams(LayoutParam.FF1);

					ImageView img = new ImageView(this);
					img.setLayoutParams(LayoutParam.FF);
					img.setOnClickListener(this);

					// 팝업메뉴처리
					if (actionType.equals("popup")) {
						img.setTag(R.id.action, TaskID.TASK_ID_SHOWMOREDATA);
						img.setTag(R.id.control_seq, String.valueOf(20+i));
					} else {
						img.setTag(R.id.action, action);
						img.setTag(R.id.control_seq, String.valueOf(i));
					}
					img.setTag(R.id.actionType, actionType);
					img.setBackgroundColor(00000000);
					if (!imageName.equals("")) {
						try {
							img.setImageDrawable(ImageWrappter.getDrawable(
									imageName, this));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					toolBtnLayout.addView(img);
					rightToolbarLayout.addView(toolBtnLayout);
				}
			}
		} else {
			brighttoolbarVisible = false;
			rightBottomLayout.setVisibility(View.GONE);
			rightHookImg.setVisibility(View.GONE);
		}
		if (param.has("callback")) {
			String callback = param.getString("callback");
			Logger.d(TAG, "javascript:" + callback + "()");
			if (webView != null) {
				webView.loadUrl("javascript:" + callback + "()");
			}
		}

		if (param.has("android_hardware_backbutton")) {
			back_action = param.getString("android_hardware_backbutton");
		} else {
			back_action = "";
		}

		brightToolbar = false;
		rightBottomLayout.setVisibility(View.GONE);
		bleftToolbar = false;
		leftBottomLayout.setVisibility(View.GONE);

	}
	
	// toolbar context menu button 안나오는 현상. mainActivity와 동일하게 변경
	private boolean isButtonObject ( Object obj ) {
		boolean bResult = false;
		if (( obj instanceof JSONObject) ) {
			if (((JSONObject )obj).has("control_id") || ((JSONObject )obj).has("image_name")) {
				bResult = true;
			}
		}
		return bResult;
	}
	
	private void createContextMenu(JSONObject jsonToolbar)
			throws JSONException, IOException {
		
		LinearLayout.LayoutParams gapLayout = new LinearLayout.LayoutParams( 
				 LinearLayout.LayoutParams.FILL_PARENT,10);
		
		JSONArray contextMenuArray = jsonToolbar.getJSONArray("context");
		contextMenuLayout = new LinearLayout(this);
		contextMenuLayout.setOrientation(LinearLayout.VERTICAL);
		contextMenuLayout.setGravity(Gravity.RIGHT | Gravity.CENTER);

		 LinearLayout.LayoutParams layoutParam2 = new
		 LinearLayout.LayoutParams( 
				 LinearLayout.LayoutParams.WRAP_CONTENT,
				 LinearLayout.LayoutParams.WRAP_CONTENT);
		 layoutParam2.gravity = Gravity.RIGHT | Gravity.CENTER ;
		 layoutParam2.leftMargin = 10;
		 layoutParam2.rightMargin = 10;
		 contextMenuLayout.setLayoutParams(layoutParam2);

		String buttonText, imageName, actionType, action;
		double val = (double) contextMenuArray.length() / (double) 3;
		double loopCnt = Math.ceil(val);
		Logger.d(TAG, "loopCnt::" + loopCnt);

		if (contextMenuArray.length() > 0
				&& contextMenuArray.get(0) instanceof JSONArray) {
			// Context 메뉴가 각 행에 맞게 지정되어 내려오는 경우 처리
			for (int i = 0; i < contextMenuArray.length(); i++) {
				
				LinearLayout contextMenu = new LinearLayout(this);
				contextMenu.setLayoutParams(layoutParam);
				JSONArray jsonContextMenus = contextMenuArray.getJSONArray(i);
				for (int j = 0; j < jsonContextMenus.length(); j++) {
					JSONObject jsonContextMenu = (JSONObject) jsonContextMenus
							.get(j);
					buttonText = jsonContextMenu.getString("button_text");
					imageName = jsonContextMenu.getString("image_name");
					actionType = jsonContextMenu.getString("action_type");
					action = jsonContextMenu.getString(ACTION);
					ContextMenuButton cmb = new ContextMenuButton(this);
					try {
						cmb.setImageDrawable(ImageWrappter.getDrawable(
								imageName, this));
					} catch (IOException e) {
						e.printStackTrace();
					}
					cmb.setText(buttonText);
					if (buttonText.equals("")) {
						cmb.setVisibleText(false);
					} else {
						cmb.setVisibleText(true);
					}
					cmb.setTag(R.id.action, action);
					cmb.setTag(R.id.actionType, actionType);
					cmb.setOnClickListener(this);
					contextMenu.addView(cmb, b);
				}
				// 한 행에 대한 부분 레이아웃에 추가
				LinearLayout temp = new LinearLayout(this);
				temp.setBackgroundColor(0xD9534334);
				contextMenuLayout.addView(temp, gapLayout);
				contextMenuLayout.addView(contextMenu);
			}
		}
		View contextMenuView = bodyFramLayout.getChildAt(1);
		Logger.d(TAG, "contextMenuView::" + contextMenuView);
		if (contextMenuView != null) {
			bodyFramLayout.removeViewAt(1);
		}

		
		rightHookLayout.addView(contextMenuLayout, 0);
		//bodyFramLayout.addView(contextMenuLayout, 1, layoutParam2);
		slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
		slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);
		slideIn.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
				// isDrawingMenu = true;
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				// ll.setVisibility(View.VISIBLE);
				contextMenuLayout.startAnimation(slideOut);
				contextMenuLayout.setVisibility(View.INVISIBLE);
			}
		});
		contextMenuLayout.setVisibility(View.INVISIBLE);
	}

	
	private void createContextMenu2 (JSONObject jsonToolbar, String seq)
		throws JSONException, IOException {
		if ( contextMenuLayout != null ) {
			contextMenuLayout = null;
			Logger.d(TAG, "contextMenuLayout is not null!!");
		}
		JSONArray contextMenuArray = jsonToolbar.getJSONArray("context");
		contextMenuLayout = new LinearLayout(this);
		contextMenuLayout.setOrientation(LinearLayout.HORIZONTAL);
		contextMenuLayout.setGravity(Gravity.RIGHT | Gravity.CENTER);
		
		 LinearLayout.LayoutParams layoutParam2 = new
		 LinearLayout.LayoutParams( 
				 LinearLayout.LayoutParams.WRAP_CONTENT,
				 LinearLayout.LayoutParams.WRAP_CONTENT);
		 layoutParam2.gravity = Gravity.RIGHT | Gravity.CENTER ;
		 layoutParam2.leftMargin = 10;
		 layoutParam2.rightMargin = 10;
		 contextMenuLayout.setLayoutParams(layoutParam2);
		
		String buttonText, imageName, actionType, action;
		double val = (double) contextMenuArray.length() / (double) 3;
		double loopCnt = Math.ceil(val);
		Logger.d(TAG, "loopCnt::" + loopCnt);
		
		if (contextMenuArray.length() > 0
				&& contextMenuArray.get(0) instanceof JSONArray) {
			// Context 메뉴가 각 행에 맞게 지정되어 내려오는 경우 처리
			for (int i = 0; i < contextMenuArray.length(); i++) {
				if ( ! (contextMenuArray.get(i) instanceof JSONArray) ) {
					Logger.d(TAG, "not Array Button Object contextMenuArray : " + i + " " + contextMenuArray.get(i));
					continue;
				}
				LinearLayout contextMenu = new LinearLayout(this);
				contextMenu.setLayoutParams(layoutParam);
				contextMenu.setOrientation(LinearLayout.VERTICAL);
				JSONArray jsonContextMenus = contextMenuArray.getJSONArray(i);
				for (int j = 0; j < jsonContextMenus.length(); j++) {
					if ( ! isButtonObject (jsonContextMenus.get(i)) ){
						Logger.d(TAG, "not Button Object jsonContextMenus : " + i + " " + jsonContextMenus.get(i));
						continue;
					}
					JSONObject jsonContextMenu = (JSONObject) jsonContextMenus.get(j);
					buttonText = jsonContextMenu.getString("button_text");
					imageName = jsonContextMenu.getString("image_name");
					actionType = jsonContextMenu.getString("action_type");
					action = jsonContextMenu.getString(ACTION);
					ContextMenuButton cmb = new ContextMenuButton(this);
					try {
						cmb.setImageDrawable(ImageWrappter.getDrawable(
								imageName, this));
					} catch (IOException e) {
						e.printStackTrace();
					}
					cmb.setText(buttonText);
					if (buttonText.equals("")) {
						cmb.setVisibleText(false);
					} else {
						cmb.setVisibleText(true);
					}
					cmb.setTag(R.id.action, action);
					cmb.setTag(R.id.actionType, actionType);
					cmb.setOnClickListener(this);
					contextMenu.addView(cmb, b);
				}
				LinearLayout padding = new LinearLayout(this);
				LinearLayout.LayoutParams paddingLayout = new LinearLayout.LayoutParams( 
						LinearLayout.LayoutParams.FILL_PARENT , 20);
				contextMenu.addView(padding, paddingLayout);
				
				// 한 행에 대한 부분 레이아웃에 추가
				contextMenuLayout.addView(contextMenu);
				contextMenuLayout.setBackgroundColor (ContextMenuButton.getContextLayoutColor());
			}
		}
		
		// left context 1x   right context 2x
		if ( Integer.valueOf(seq) <  20 ) {
			leftHookLayout.addView(contextMenuLayout, 1);
			leftHookImg.setVisibility(View.GONE);
		} else {
			rightHookLayout.addView(contextMenuLayout, 0);
			rightHookImg.setVisibility(View.GONE);
		}
		contextMenuLayout.setVisibility(View.INVISIBLE);
	}	
	
	
	@Override
	protected void onPageFinished(WebView arg0, String arg1) {
		super.onPageFinished(arg0, arg1);
		if ( webView != null ) {
			webView.loadUrl("javascript:appcallOnFocus()");
		}
	}

	public void contextAnimation() {
		contextMenuLayout.startAnimation(slideIn);
	}

	public void toggleContext() {
		if ( alpha_up == null ){
			alpha_up = AnimationUtils.loadAnimation(this, R.anim.alpha_up);
			alpha_up.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}
				public void onAnimationRepeat(Animation animation) {
				}
				public void onAnimationEnd(Animation animation) {
				}
			});
			
		}
		if ( alpha_down == null ){
			alpha_down = AnimationUtils.loadAnimation(this, R.anim.alpha_down);
			alpha_down.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}
				public void onAnimationRepeat(Animation animation) {
				}
				public void onAnimationEnd(Animation animation) {
				}
			});
		}
		
		if (contextAnimation) {
			if ( leftHookLayout.getChildAt(1) == contextMenuLayout ){
				leftHookLayout.removeViewAt(1);
			} else if (  rightHookLayout.getChildAt(0) == contextMenuLayout ){
				rightHookLayout.removeViewAt(0);
			}
			contextMenuLayout.startAnimation(alpha_up);
			rightHookImg.setVisibility(View.VISIBLE);
			leftHookImg.setVisibility(View.VISIBLE);
			//contextMenuLayout.setVisibility(View.INVISIBLE);
		} else {
			//rightHookImg.setVisibility(View.GONE);
			//leftHookImg.setVisibility(View.GONE);
			contextMenuLayout.startAnimation(alpha_down);
			contextMenuLayout.setVisibility(View.VISIBLE);
		}
		contextAnimation = !contextAnimation;
	}

	@Override
	public void onBackPressed() {
		Logger.d(TAG, "onBackPressed");
		if ( contextAnimation ) {
			toggleContext();
			return;
		}
		if (fullScreen) {
			doRealScreen();
		} else {

			if (back_action.length() > 0) {
				Logger.d(TAG, "javascript:back_action()");
				webView.loadUrl("javascript:" + back_action + "()");
			} else {
				// if (modal) { webView.loadUrl("javascript:appcallCancel()"); }
				Request request = new Request();
				request.setSrcActivity(this);
				try {
					execute(TaskID.TASK_ID_ACTIVITYFINISH, request, false, 0);
				} catch (Exception e) {
					Logger.e(TAG, e.toString());
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			long thisTime = System.currentTimeMillis();
			if (thisTime - lastTouchTime < 250) {
				// testcode 임시
				if (!fullScreen /* && !isHomeActivity */) {
					doFullScreen();
				}
			}
			lastTouchTime = thisTime;
		}
		return false;
	}

	private void doFullScreen() {
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (contextAnimation) {
			contextMenuLayout.setVisibility(View.INVISIBLE);
			contextMenuLayout.startAnimation(alpha_down);
			contextAnimation = false;
		}
		
		if ( blefttoolbarVisible ){
			leftHookImg.setVisibility(View.GONE);
		}
		if ( brighttoolbarVisible ){
			rightHookImg.setVisibility(View.GONE);
		}
		
		if ( brighttoolbarVisible && brightToolbar) {
			if (slideRightOut == null) {
				Logger.d(TAG, "slideDownOut::" + slideDownOut);
				slideRightOut = AnimationUtils.loadAnimation(this,
						R.anim.push_right_out);
			}
			slideRightOut.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					rightBottomLayout.setVisibility(View.GONE);

					animation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
							0.0f);
					animation.setDuration(1);
					rightHookLayout.startAnimation(animation);
				}
			});
			rightBottomLayout.startAnimation(slideRightOut);
			brightToolbar = false;
		} 
		
		if ( blefttoolbarVisible && bleftToolbar) {
			if (slideLeftOut == null) {
				Logger.d(TAG, "slideDownOut::" + slideDownOut);
				slideLeftOut = AnimationUtils.loadAnimation(this,
						R.anim.push_left_out);
			}
			slideLeftOut.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					leftBottomLayout.setVisibility(View.GONE);

					animation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
							0.0f);
					animation.setDuration(1);
					leftHookLayout.startAnimation(animation);
				}
			});
			leftBottomLayout.startAnimation(slideLeftOut);
		} 

		if (slideUpOut == null) {
			Logger.d(TAG, "slideUpOut::" + slideUpOut);
			slideUpOut = AnimationUtils.loadAnimation(this,
					R.anim.slide_up_out);
		}
		slideUpOut.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
				// isDrawingMenu = true;
				// rightBottomLayout.setVisibility(View.GONE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {
				headerLayout.setVisibility(View.GONE);
				animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 0.0f);
				animation.setDuration(1);
				baseLayout.startAnimation(animation);
			}
		});
		baseLayout.startAnimation(slideUpOut);
		fullScreen = true;
		// end
	}

	private void doRealScreen() {
		/*
		 * Orient의 값을 알아내어 상황에 맞게 처리
		 */
		if ( blefttoolbarVisible ){
			leftHookImg.setVisibility(View.VISIBLE);
		}
		if ( brighttoolbarVisible ){
			rightHookImg.setVisibility(View.VISIBLE);
		}
		
		if (slideUpIn == null) {
			Logger.d(TAG, "slideUpIn::" + slideUpIn);
			slideUpIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
		}

		slideUpIn.setAnimationListener(new AnimationListener() {
			public void onAnimationStart(Animation animation) {
				getWindow().setFlags(
						WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
				headerLayout.setVisibility(View.VISIBLE);
				animation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 0.0f);
				animation.setDuration(1);
				headerLayout.startAnimation(animation);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationEnd(Animation animation) {

			}
		});
		baseLayout.startAnimation(slideUpIn);
		fullScreen = false;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Logger.d("CHANGE ORIENT: ", "onConfigurationChanged");
		if (rightBottomLayout != null) {
			if (fullScreen) {
				rightBottomLayout.setVisibility(View.GONE);
			} else if (!brightToolbar) {
				rightBottomLayout.setVisibility(View.GONE);
			} else {
				rightBottomLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	private void rightToggleToolbar() {
		if ( brighttoolbarVisible ) {
			Resources r = Resources.getSystem();
			final Configuration config = r.getConfiguration();

			// 툴바 안보이도록
			if (brightToolbar) {
				if (slideRightOut == null) {
					Logger.d(TAG, "slideDownOut::" + slideDownOut);
					slideRightOut = AnimationUtils.loadAnimation(this,
							R.anim.push_right_out);
				}
				slideRightOut.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationEnd(Animation animation) {
						rightBottomLayout.setVisibility(View.GONE);
						/*animation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
								0.0f);
						animation.setDuration(1);
						00(animation);*/
					}
				});
				rightHookLayout.startAnimation(slideRightOut);
				try {
					Drawable d = ImageWrappter.getDrawableAsset("native/bizmob_handle_right_on.png", this);
					rightHookImg.setImageDrawable(d);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				rightBottomLayout.setVisibility(View.VISIBLE);

				if (slideRightIn == null) {
					Logger.d(TAG, "slideDownIn::" + slideDownIn);
					slideRightIn = AnimationUtils.loadAnimation(this,
							R.anim.push_left_in);
				}
				slideRightIn.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationEnd(Animation animation) {
						rightBottomLayout.setVisibility(View.VISIBLE);
						/*animation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
								0.0f);
						animation.setDuration(1);
						rightBottomLayout.startAnimation(animation);*/
					}
				});
				rightHookLayout.startAnimation(slideRightIn);
				try {
					Drawable d = ImageWrappter.getDrawableAsset("native/bizmob_handle_right_off.png", this);
					rightHookImg.setImageDrawable(d);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			brightToolbar = !brightToolbar;
		}
	}
	
	private void leftToggleToolbar() {
		if (blefttoolbarVisible) {
			// 툴바 안보이도록
			if (bleftToolbar) {
				if (slideLeftOut == null) {
					Logger.d(TAG, "slideDownOut::" + slideDownOut);
					slideLeftOut = AnimationUtils.loadAnimation(this,
							R.anim.push_left_out);
				}
				slideLeftOut.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationEnd(Animation animation) {
						leftBottomLayout.setVisibility(View.GONE);
						/*animation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
								0.0f);
						animation.setDuration(1);
						leftHookLayout.startAnimation(animation);*/
					}
				});
				leftHookLayout.startAnimation(slideLeftOut);
				
				try {
					Drawable d = ImageWrappter.getDrawableAsset("native/bizmob_handle_left_on.png", this);
					leftHookImg.setImageDrawable(d);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				leftBottomLayout.setVisibility(View.VISIBLE);

				if (slideLeftIn == null) {
					Logger.d(TAG, "slideDownIn::" + slideRightIn);
					slideLeftIn = AnimationUtils.loadAnimation(this,
							R.anim.push_right_in);
				}
				slideLeftIn.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
					}

					public void onAnimationRepeat(Animation animation) {
					}

					public void onAnimationEnd(Animation animation) {
						leftBottomLayout.setVisibility(View.VISIBLE);
						/*animation = new TranslateAnimation(0.0f, 0.0f, 0.0f,
								0.0f);
						animation.setDuration(1);
						leftBottomLayout.startAnimation(animation);*/
					}
				});
				leftHookLayout.startAnimation(slideLeftIn);
				try {
					Drawable d = ImageWrappter.getDrawableAsset("native/bizmob_handle_left_off.png", this);
					leftHookImg.setImageDrawable(d);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			bleftToolbar = !bleftToolbar;
		}
	}	
	
	protected boolean checkButtonAction ( View view ){
		boolean bResult = true;
		String action = (String) view.getTag(R.id.action);
		long nCur = System.currentTimeMillis();
		long interval = nCur - lastClickTime;

		if ( action != null && action.equals(lastAction) ) {
			if( interval  < 1000 ) {
				Logger.d(TAG, "action : " + action + " " + (nCur - lastClickTime) + "ms" ); 
				return false;
			} else {
				Logger.d(TAG, "checkButtonAction OK " );
			}
		}
		lastClickTime = nCur;
		lastAction = action;
		return bResult;
	}
	
	protected boolean checkUrlAction ( JSONObject data ){
		boolean bResult = true;
		long nCur = System.currentTimeMillis();
		long interval = nCur - lastPostTime;
		try {
			String trcode = data.toString();
			if ( trcode != null && trcode.equals(lastTrCode) ) {
				if( interval < 1000 ) {
					Logger.d(TAG, "action : " + trcode + " " + (nCur - lastPostTime) + "ms" ); 
					// 1 : double click,  2: double post 
					return false;
				} else {
					Logger.d(TAG, "checkButtonAction OK " );
				}
			}
			lastPostTime = nCur;
			lastTrCode = trcode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bResult;
	}
}
