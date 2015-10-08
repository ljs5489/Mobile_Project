package com.mcnc.parecis.bizmob.view;

import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.mcnc.hsmart.controller.Smart2ProcessController;
import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.exception.RequestException;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.util.StringUtil;
import com.mcnc.hsmart.core.view.AbstractActivity;
import com.mcnc.hsmart.core.view.BaseWebView;
import com.mcnc.hsmart.def.AppConfig;
import com.mcnc.hsmart.def.Def;
import com.mcnc.hsmart.def.TaskID;
import com.mcnc.hsmart.interfaces.MemoryInterface;
import com.mcnc.hsmart.interfaces.SqliteInterface;
import com.mcnc.hsmart.plugin.BizmobPluginManager;
import com.mcnc.hsmart.wrapper.ImageWrappter;
import com.mcnc.parecis.bizmob.util.ProjectUtils;
import com.mcnc.parecis.toyota.R;
import com.phonegap.WebViewReflect;

public class PopupViewActivity extends ImpMainActivity {
	
	final String SOURCE_PAGE = "sourcePage";
	final String TARGET_PAGE = "target_page";
	final String TAG 		= this.toString();

	final int ORIENTATION_NONE = 0;
	final int ORIENTATION_VERTICAL  = 1;
	final int ORIENTATION_HORIZONTAL  = 2;
	final int ORIENTATION_AUTO  = 3;
	
	private static final int LOW_DPI_STATUS_BAR_HEIGHT = 19; 
	private static final int MEDIUM_DPI_STATUS_BAR_HEIGHT = 25; 
	private static final int HIGH_DPI_STATUS_BAR_HEIGHT = 38; 
	private static final int XHIGH_DPI_STATUS_BAR_HEIGHT = 50; 

	
	String tagetPage = "";
	String callback = "";
	String back_action = "";
	
	int width = 0;
	int height = 0;
	int orientation = ORIENTATION_NONE;
	
	JSONObject data = null;
	
	LinearLayout bodyLayout;
	
	int titleBarHeight = 0;
	
   	
    /**
     * Callback for Theme Style setting
     */
    protected void onApplyThemeResource(Resources.Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, resid, first);
    }
    
	

    protected LinearLayout setBody () {
    	bodyLayout = new LinearLayout(this);

    	LinearLayout.LayoutParams viewparams = new LinearLayout.LayoutParams( width, height);		
		String url = ImageWrappter.getUri( tagetPage );
		init();
		//loadUrl (url);
	   	Intent intent = getIntent();
		loadUrl(webView, url, intent);

		bodyLayout.addView(webView, viewparams);  

		return bodyLayout;
    }    
    
  

	@Override
	protected void setHeader(LinearLayout baseLayout) {
	   	Intent intent = getIntent();
    	tagetPage = intent.getStringExtra("target_page");
    	callback = intent.getStringExtra("callback");
		
    	width = intent.getIntExtra("width", 200);
    	height = intent.getIntExtra("height", 100);
    	orientation = intent.getIntExtra("orientation", ORIENTATION_NONE);
    	
         	
    	String jsonStr  = intent.getStringExtra("data");
    	try {
    		data = new JSONObject(jsonStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
  
		if (intent.hasExtra("modal")) {
			modal = intent.getExtras().getBoolean("modal");
		}		
		if (intent.hasExtra("android_hardware_backbutton")) {
			back_action = intent.getExtras().getString("android_hardware_backbutton");
		}		
	
		if (Def.screenDensity == 160 ) {
			titleBarHeight = MEDIUM_DPI_STATUS_BAR_HEIGHT;
		} else if (Def.screenDensity == 320 ){
			titleBarHeight = XHIGH_DPI_STATUS_BAR_HEIGHT;
		} else {
			titleBarHeight = HIGH_DPI_STATUS_BAR_HEIGHT;
		}
	}

	@Override
	protected void setBody(LinearLayout baseLayout) {
    	// daialog 테두리 없앰
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Body 설정
		bodyLayout = setBody ();
    	//ScrollView scroll = new ScrollView(this);
    	//scroll.setLayoutParams(LayoutParam.FF);
		//scroll.addView(bodyLayout);
		//baseLayout.addView(scroll);
		baseLayout.addView(bodyLayout);
	}

	@Override
	protected void setBottom(LinearLayout baseLayout) {
		// Configuration 적용
		int newH = 0;
		int newW = 0;
		
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);  
		Display display = wm.getDefaultDisplay();
		int	dispW = display.getWidth();
		int	dispH = display.getHeight();
		dispH = dispH - titleBarHeight;
		
		if ( orientation == ORIENTATION_VERTICAL ) {
			newW = dispW < width ? dispW : width ;
			newH = dispH < height ? dispH : height;
		} else if ( orientation == ORIENTATION_HORIZONTAL) {
			newW = dispW < width ? dispW : width ;
			newH = dispH < height ? dispH : height;
		} else if ( orientation == ORIENTATION_AUTO) {
			Configuration newConfig = getResources().getConfiguration();
			if ( newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ) {
				newW = dispW < width ? dispW : width ;
				newH = dispH < height ? dispH : height;
			} else {
				newW = dispW < height ? dispW : height;
				newH = dispH < width ? dispH : width;
			}
		}
		
		if ( newW != 0 && newH != 0 ){
			LinearLayout.LayoutParams webViewLayoutParam = new LinearLayout.LayoutParams( newW, newH );
			webView.setLayoutParams(webViewLayoutParam);
		}
		Logger.d(TAG, "width" + width);
		Logger.d(TAG, "height" + height);
	}
	

	@Override
	protected void onAction(View view) {
		try {
			Logger.d(TAG, "actionType::" + view.getTag(R.id.actionType));
			Logger.d(TAG, "action::" + view.getTag(R.id.action));
			if (view.getTag(R.id.actionType) != null
					&& ((String) view.getTag(R.id.actionType)).equals("jscall")) {
				if (webView != null) {
					webView.loadUrl("javascript:" + view.getTag(R.id.action) + "()");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
			
			//long start = System.currentTimeMillis();
			String decodedURL = URLDecoder.decode(requestURL, "utf-8");
			//long stop = System.currentTimeMillis();
			//Logger.d(TAG, "URLDecodingElapsedTime::" + (stop - start));
			//start = System.currentTimeMillis();
			JSONObject decodedData = new JSONObject(decodedURL.substring(7));
			//stop = System.currentTimeMillis();
			//Logger.d(TAG, "generateJSONElapsedTime::" + (stop - start));
			Logger.f(TAG, "decodedData::" + decodedData);
			JSONObject param = decodedData.getJSONObject("param");
			final String id = decodedData.getString("id");
			if (id.equals("SET_APP")) {
				setContainer ( param );
			} else {
				// 모든 액션 체크
				if ( ! checkUrlAction(param) ) {
					return;
				}
				
				String targetPage = StringUtil.getString(param, TARGET_PAGE);
				Logger.d(TAG, "targetPage::" + targetPage);
				// create request
				request = new Request();
				request.setSrcActivity(this);
				request.setTargetActivity(PopupViewActivity.class);
				request.setCallType(StringUtil.getString(decodedData,
						"call_type"));
				request.setTargetPage(targetPage);
				request.setData(decodedData);
				request.setTrCode(StringUtil.getString(param, "trcode"));
				
				Smart2ProcessController controller = (Smart2ProcessController)getController();
				Class activity = null;
				activity = controller.getActivityClass(Smart2ProcessController.MAIN_ACTIVITY);
				if (id.equals("GOTO_WEB_MODAL") || id.equals("GOTO_WEB")
						|| id.equals("GOTO_FILE_UPLOAD")) {
					execute(id,
							request,
							true,
							AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
				} else if (id.equals("RELOAD_WEB") || id.equals("REPLACE_WEB")) {
					execute(id,
							request,
							true,
							AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
				} else {
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
	
	@Override
	public void onError(Response response) {
		super.onError(response);
	}
	
	@Override
	public void onSuccess(Response response) {
		super.onSuccess(response);
	}
	
	@Override
	public void onBackPressed() {
		if ( back_action.length() > 0 ){
			Logger.d (TAG, "javascript:back_action()");
			webView.loadUrl("javascript:"+back_action+"()");
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
	
	public void init() {

		// Create web container
		this.webView = new BaseWebView(this);
		this.webView.setId(100);

		WebViewReflect.checkCompatibility();

		this.webView.setInitialScale(100);
		this.webView.setVerticalScrollBarEnabled(true);
		this.webView.requestFocusFromTouch();

		// Enable JavaScript
		WebSettings settings = this.webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);

		// Enable database
		Package pack = this.getClass().getPackage();
		String appPackage = pack.getName();
		WebViewReflect.setStorage(settings, true, "/data/data/" + appPackage
				+ "/app_database/");

		// Enable DOM storage
		WebViewReflect.setDomStorage(settings);

		// Enable built-in geolocation
		WebViewReflect.setGeolocationEnabled(settings, true);

		// Bind PhoneGap objects to JavaScript
		bindBrowser(this.webView);

		// Clear cancel flag
		this.cancelLoadUrl = false;
		this.webView.addJavascriptInterface( SqliteInterface.getInstance(), "AndroidSqlite");
		this.webView.addJavascriptInterface( MemoryInterface.getInstance(), "MemoryMap");
		this.webView.addJavascriptInterface(new BizmobPluginManager(this, webView), "BizmobPluginMgr");
		//this.webView.setBackgroundColor(Color.TRANSPARENT);
		this.webView.setBackgroundColor(AppConfig.WEBVIEW_BACKGROUNDCOLOR_DIALOG);
	}

	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		int newH = 0;
		int newW = 0;
		
		Log.d("onconfigurationChanged", "size Change");
		
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);  
		Display display = wm.getDefaultDisplay();
		int	dispW = display.getWidth();
		int	dispH = display.getHeight();
		dispH = dispH - titleBarHeight;
		
		if ( orientation == ORIENTATION_VERTICAL ) {
			newW = dispW < width ? dispW : width ;
			newH = dispH < height ? dispH : height;
			
			int hheight = bodyLayout.getHeight();
			int wwidth = bodyLayout.getWidth();
			
			LinearLayout.LayoutParams webViewLayoutParam = new LinearLayout.LayoutParams( wwidth, hheight );
			webView.setLayoutParams(webViewLayoutParam);
			
		} else if ( orientation == ORIENTATION_HORIZONTAL) {
			newW = dispW < width ? dispW : width ;
			newH = dispH < height ? dispH : height;
		} else if ( orientation == ORIENTATION_AUTO) {
			if ( newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ) {
				newW = dispW < width ? dispW : width ;
				newH = dispH < height ? dispH : height;
			} else {
				newW = dispW < height ? dispW : height;
				newH = dispH < width ? dispH : width;
			}
		}
		
		if ( newW != 0 && newH != 0 ){
			LinearLayout.LayoutParams webViewLayoutParam = new LinearLayout.LayoutParams( newW, newH );
			webView.setLayoutParams(webViewLayoutParam);
		}
		
		int hheight = bodyLayout.getHeight();
		int wwidth = bodyLayout.getWidth();
		
		LinearLayout.LayoutParams webViewLayoutParam = new LinearLayout.LayoutParams( wwidth, hheight );
		webView.setLayoutParams(webViewLayoutParam);
		
		super.onConfigurationChanged(newConfig);
	}
	
	protected void setContainer(JSONObject param) throws JSONException {
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
	}
}
