package com.mcnc.parecis.bizmob.nativeView.browser;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.core.view.BaseWebView;
import com.mcnc.hsmart.core.view.param.LayoutParam;
import com.mcnc.hsmart.util.DialogUtility;
import com.mcnc.hsmart.wrapper.ImageWrappter;
import com.mcnc.parecis.bizmob.def.Def;
import com.mcnc.parecis.bizmob.def.TaskID;
import com.mcnc.parecis.bizmob.view.ImpMainActivity;
import com.mcnc.parecis.toyota.R;
import com.phonegap.WebViewReflect;

public class InternalBrowserActivity extends ImpMainActivity {
	private String TAG = this.toString();
	private String callback = "";
	private JSONObject message = null;
	
	private ImageView prevButton = null;
	private ImageView nextButton = null;
	private ImageView exitButton = null;
	private ImageView smsButton = null;
	private boolean prevEnable = false;
	private boolean nextEnable = false;
	private ValueCallback<Uri> mUploadMessage;
	private int FILECHOOSER_RESULTCODE = 1112233; 
	private String FILECHOOSER_MESSAGE = "업로드할 파일을 선택하세요";
	  
	private BaseActivity context;
	
	WebView.WebViewTransport transport;
	
	@Override
	protected void setHeader(LinearLayout baseLayout) {
		LinearLayout.LayoutParams buttonMargineParam = new LinearLayout.LayoutParams( 10, Def.TITLE_H);
		LinearLayout margineL = new LinearLayout (this);
		LinearLayout margineM = new LinearLayout (this);
		LinearLayout margineS = new LinearLayout (this);
		LinearLayout margineR = new LinearLayout (this);
		
		headerLayout = new LinearLayout(this);
		LinearLayout.LayoutParams headerLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, Def.TITLE_H, 0 );
		LinearLayout.LayoutParams headerInParam = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, Def.TITLE_H, 1);
		LinearLayout.LayoutParams headerCenterParam = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, Def.TITLE_H, 1);

		Logger.d(TAG, "headerLayout : " + headerLayout);

		headerLeftLayout = new LinearLayout(this);
		headerTitleLayout = new LinearLayout(this);
		headerRightLayout = new LinearLayout(this);

		headerTitleLayout.setOnTouchListener(this);

		headerLayout.addView(headerLeftLayout, headerInParam);
		headerLayout.addView(headerTitleLayout, headerCenterParam);
		headerLayout.addView(headerRightLayout, headerInParam);
		
		baseLayout.addView(headerLayout, headerLayoutParam);
		
		try {
			headerLayout.setBackgroundDrawable(ImageWrappter.getDrawableAsset("native/bizmob_browser_title.png", this));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// 이전 버튼
		prevButton = new ImageView(this);
		try {
			prevButton.setImageDrawable(ImageWrappter.getDrawableAsset(
					"native/bizmob_browser_refresh_off.png", this));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		headerLeftLayout.addView(margineL, buttonMargineParam);
		headerLeftLayout.addView(prevButton, LayoutParam.WW);
		headerLeftLayout.setGravity(Gravity.LEFT | Gravity.CENTER);
		
		// 다음
		nextButton = new ImageView(this);
		try {
			nextButton.setImageDrawable(ImageWrappter.getDrawableAsset(
					"native/bizmob_browser_refresh_off.png", this));
		} catch (IOException e) {
			e.printStackTrace();
		}
		headerLeftLayout.addView(margineM, buttonMargineParam);
		headerLeftLayout.addView(nextButton, LayoutParam.WW);
		headerLeftLayout.setGravity(Gravity.LEFT | Gravity.CENTER);
		
		// SMS 
		smsButton = new ImageView(this);
		try {
			smsButton.setImageDrawable(ImageWrappter.getDrawableAsset(
					"native/bizmob_browser_refresh_off.png", this));
		} catch (IOException e) {
			e.printStackTrace();
		}
		headerLeftLayout.addView(margineS, buttonMargineParam);
		headerLeftLayout.addView(smsButton, LayoutParam.WW);
		headerLeftLayout.setGravity(Gravity.LEFT | Gravity.CENTER);
		
		// 종료
		exitButton = new ImageView(this);
		try {
			exitButton.setImageDrawable(ImageWrappter.getDrawableAsset(
					"native/bizmob_browser_close_off.png", this));
		} catch (IOException e) {
			e.printStackTrace();
		}
		headerRightLayout.addView(exitButton, LayoutParam.WW);
		headerRightLayout.addView(margineR, buttonMargineParam);
		headerRightLayout.setGravity(Gravity.RIGHT | Gravity.CENTER);
	
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					
				webView.loadUrl("javascript:clickNativeReloadButton()");
			}
		});
		prevButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( prevEnable ) {
					webView.goBack();
					prevEnable = false;
					nextEnable = false;
					try {
						prevButton.setImageDrawable(ImageWrappter.getDrawableAsset(
								"native/bizmob_browser_back_off.png", v.getContext()));
						nextButton.setImageDrawable(ImageWrappter.getDrawableAsset(
								"native/bizmob_browser_foword_off.png", v.getContext()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		smsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ( webView != null && webView.getUrl() != null ) {
					String url = webView.getUrl();
					TASK_ID_SMS ("", url);
				} else {
					DialogUtility.alert( context, "전달 할 수 있는 URL이 없습니다.");
				}
			}
		});
		// 옵션에 따라 sms 버튼을 보이거나 안보이게 한다.
		Intent intent = getIntent();
		if (intent.hasExtra("sms_button")){
			boolean b = intent.getBooleanExtra("sms_button", false );
			if ( ! b ) {
				smsButton.setVisibility(View.GONE);
			}
		}
		
		
		exitButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				closeBrowser();
			}
		});		
		
		prevButton.setVisibility(View.GONE);
		//nextButton.setVisibility(View.GONE);
	}
	
	private void TASK_ID_SMS( String number, String message ) {
		JSONObject root = new JSONObject();
		JSONObject param = new JSONObject();
		try {
			root.put("id", TaskID.TASK_ID_SMS);
			root.put("call_type", "js2app");
			root.put("param", param);
			param.put("callback", "appcall_TASK_ID_SMS");
			param.put("number", number);		// 최대 
			param.put("message", message );
		} catch (Exception e) {
		}
		request = new Request();
		request.setData(root);
		request.setSrcActivity(this);
		execute(TaskID.TASK_ID_SMS, request, false, 0);
		Logger.d(TAG, "decode:" + root.toString() );
	}
	
	@Override
	protected void setBody(LinearLayout baseLayout) {
		bodyFramLayout = new FrameLayout(this);
		LinearLayout.LayoutParams bodyLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1);


		// Create web container
		webView = new BaseWebView(this);
		webView.setId(100);

		WebViewReflect.checkCompatibility();

		webView.setInitialScale(100);
		webView.setVerticalScrollBarEnabled(true);
		webView.requestFocusFromTouch();
		webView.setFocusable(true);
		webView.addJavascriptInterface(new javaScriptInterface(this), "bizMOB_Android");
		
		// Enable JavaScript
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		
		settings.setSupportMultipleWindows(true);
		
		settings.setRenderPriority(RenderPriority.HIGH);
		
		// Clear cancel flag
		this.cancelLoadUrl = false;
		
		// openFileChooser 가 가능한 CromeClient로 변경
		context = this;
		webView.setWebChromeClient( new BaseWebChromeClient ());
		
		Intent intent = getIntent();
		if (intent.hasExtra("targetPage")) {
			String targetPage = intent.getExtras().getString("targetPage");
			Logger.d(TAG, "targetPage::" + targetPage);
			String url = ImageWrappter.getUri(targetPage);
			loadUrl(url);
			if (  Def.IS_RESTART_BY_LOW_MEMORY ) {
				Logger.d(TAG, "메모리부족으로 프로그램이 강제종료되었습니다. 작업관리자에서 메모리 정리를 해주세요.");
				Toast.makeText( this, "메모리부족으로 프로그램이 강제종료되었습니다. 작업관리자에서 메모리를 정리하면 도움이됩니다.", Toast.LENGTH_LONG).show();
				finish();
				return;
			}
		}
		if (intent.hasExtra("modal")) {
			modal = intent.getExtras().getBoolean("modal");
		}
		if (intent.hasExtra("orientation")) {
			String strOr = intent.getExtras().getString("orientation");
			if ( strOr.startsWith("land") ){
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
			} else if ( strOr.startsWith("portrait") ) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); 
			} else if ( strOr.startsWith("none") ) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			} else {
				Logger.d( TAG, "request orientation :" + strOr + "set to all");
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			}
		}
		bodyFramLayout.addView(webView, 0);
		baseLayout.addView(bodyFramLayout, bodyLayoutParam);
		
	}
	
	@Override
	protected void setBottom(LinearLayout baseLayout) {

		bottomBaseLayout = new LinearLayout(this);
		bottomBaseLayout.setOrientation(LinearLayout.VERTICAL);
		bottomBaseLayout.setGravity(Gravity.BOTTOM);

		emptyLayout = new LinearLayout(this);
		emptyLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, Def.BOTTOM_H, 0));

		bottomLayout = new LinearLayout(this);
		bottomLayout.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, Def.BOTTOM_H, 0));

		// 툴바를 그려주는 부분
		toolbarLayout = new LinearLayout(this);
		toolbarLayout.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, Def.BOTTOM_H));
		toolbarLayout.setGravity(Gravity.CENTER);

		bottomLayout.addView(toolbarLayout);
		baseLayout.addView(emptyLayout);
		bottomBaseLayout.addView(bottomLayout);
		frameLayout.addView(bottomBaseLayout);
	
		bottomLayout.setVisibility(View.GONE);
		emptyLayout.setVisibility(View.GONE);

	}
	
	private void closeBrowser(){
		
		Intent intent = new Intent();
		intent.putExtra("callback", callback);
		setResult(RESULT_OK, intent);
		
		finish();
	}

	@Override
	protected void onPageFinished(WebView wv, String url) {
		setButton();
		try {
			if (url.endsWith(".mp3")|| url.endsWith(".mp4") || url.endsWith(".3gp")) {
	            Intent intent = new Intent(Intent.ACTION_VIEW);
	            intent.setDataAndType(Uri.parse(url), "audio/*");
	            wv.getContext().startActivity(intent);   
	        }	
		} catch (ActivityNotFoundException e) {
		}
	}
	
	@Override
	protected void onAction(WebView wv, String requestURL) {
		try {
			if (requestURL.startsWith("http://") || requestURL.startsWith("https://") ) {
				loadUrl(requestURL);
				return;
			} else if (requestURL.startsWith("bizmob://toyota")){
				Uri uri = Uri.parse(requestURL);
				String paramStr = uri.getQueryParameter("param");
				JSONObject param;
				try {
					param = new JSONObject(paramStr);
					String callback = null;
					String data = null;
					String target_page = null;
					
					if (param.has("callback")) {
						callback = param.getString("callback");
					}
					if (param.has("data")) {
						data = param.getString("data");
					}
					if (param.has("target_page")) {
						target_page = param.getString("target_page");
					}
					
					Intent intent = new Intent();
					intent.putExtra("callback", callback);
					intent.putExtra("data", data);
					intent.putExtra("target_page", target_page);
					setResult(RESULT_OK, intent);
					
					finish();
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			} else {
				super.onAction(wv, requestURL);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
		
		if ( webView.canGoBack() ) {
			webView.goBack();
		}else{
			
			closeBrowser();
			overridePendingTransition(R.anim.hold, R.anim.push_right_out);
		}
		
	}
	
	private void setButton () {
		try {
			if ( webView.canGoBack() ) {
				prevEnable = true;
				prevButton.setImageDrawable(ImageWrappter.getDrawableAsset(
						"native/bizmob_browser_back.png", this ));
			} else {
				prevEnable = false;
				prevButton.setImageDrawable(ImageWrappter.getDrawableAsset(
						"native/bizmob_browser_back.png", this ));
				Logger.d(TAG, "Can't go back!!");
			}
			if ( webView.canGoForward()) {
				nextEnable = true;
				nextButton.setImageDrawable(ImageWrappter.getDrawableAsset(
						"native/bizmob_browser_foword.png", this ));
			} else {
				nextEnable = false;
				nextButton.setImageDrawable(ImageWrappter.getDrawableAsset(
						"native/bizmob_browser_foword.png", this ));
				Logger.d(TAG, "Can't go Forward!!");
			}
		} catch (Exception e) {
		}
	}
	@Override
	public void loadUrl(final String url) {
		webView.loadUrl(url);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		overridePendingTransition(R.anim.push_left_in, R.anim.hold);
		super.onResume();
	}
	
	
	@Override
	public void onDestroy() {
		context = null;
		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode==FILECHOOSER_RESULTCODE ){  
            if (null == mUploadMessage){ 
                return;  
            }
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();  
            mUploadMessage.onReceiveValue(result);
            Logger.d("text", mUploadMessage.toString() + "   " + result );
            mUploadMessage = null;
            return;                  
        }
		super.onActivityResult(requestCode, resultCode, intent);
	}
	
	class BaseWebChromeClient extends WebChromeClient {

		// testCode for phoneGap
		private String TAG = "BaseWebChromeClient";
		private long MAX_QUOTA = 100 * 1024 * 1024;

		/**
		 * Handle database quota exceeded notification.
		 * 
		 * @param url
		 * @param databaseIdentifier
		 * @param currentQuota
		 * @param estimatedSize
		 * @param totalUsedQuota
		 * @param quotaUpdater
		 */
		@Override
		public void onExceededDatabaseQuota(String url,
				String databaseIdentifier, long currentQuota,
				long estimatedSize, long totalUsedQuota,
				WebStorage.QuotaUpdater quotaUpdater) {
			Logger.d(TAG,
					"event raised onExceededDatabaseQuota estimatedSize: "
							+ Long.toString(estimatedSize) + " currentQuota: "
							+ Long.toString(currentQuota) + " totalUsedQuota: "
							+ Long.toString(totalUsedQuota));

			if (estimatedSize < MAX_QUOTA) {
				// increase for 1Mb
				long newQuota = estimatedSize;
				Logger.d(TAG,
						"calling quotaUpdater.updateQuota newQuota: "
								+ Long.toString(newQuota));
				quotaUpdater.updateQuota(newQuota);
			} else {
				// Set the quota to whatever it is and force an error
				// TODO: get docs on how to handle this properly
				quotaUpdater.updateQuota(currentQuota);
			}
		}

		@Override
		public void onGeolocationPermissionsShowPrompt(String origin,
				Callback callback) {
			// TODO Auto-generated method stub
			super.onGeolocationPermissionsShowPrompt(origin, callback);
			callback.invoke(origin, true, false);
		}

		// end

		public void onConsoleMessage(String message, int lineNumber,
				String sourceID) {
			Logger.f(TAG, message + " -- From line " + lineNumber + " of "
					+ sourceID);
			if (Logger.LogLevel < Logger.FILTER ) {
				if ( message != null && message.contains("Error")) {
					 context.setVisible(true);
					 new AlertDialog.Builder(context).setTitle("WebViewError")
					 .setMessage(message)
					 .setPositiveButton(android.R.string.ok, null)
					 .setCancelable(false).create().show();
				}
			}
		}

		// webview alert
		@Override
		public boolean onJsAlert(WebView view, String url, String message,
				final android.webkit.JsResult result) {

			Logger.f(TAG, "onJsAlert");
			Logger.f(TAG, "view::" + view);
			Logger.f(TAG, "url::" + url);
			Logger.f(TAG, "message::" + message);
			
			new AlertDialog.Builder(context)
					//.setTitle("AlertDialog")
					.setMessage(message)
					.setPositiveButton(android.R.string.ok,
							new AlertDialog.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									result.confirm();
								}
							}).setCancelable(false).create().show();

			return true;
		};

		// webview confirm
		@Override
		public boolean onJsConfirm(WebView view, String url, String message,
				final android.webkit.JsResult result) {

			Logger.f(TAG, "onJsConfirm");
			Logger.f(TAG, "view::" + view);
			Logger.f(TAG, "url::" + url);
			Logger.f(TAG, "message::" + message);
			Logger.f(TAG, "result::" + result);

			new AlertDialog.Builder(context)
					//.setTitle("ConfirmDialog")
					.setMessage(message)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									result.confirm();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									result.cancel();
								}
							}).setCancelable(false).create().show();

			return true;
		};

		// @Override
		// public boolean onJsPrompt(WebView view, String url, String
		// message,
		// String defaultValue, final JsPromptResult result) {
		// final LayoutInflater factory = LayoutInflater
		// .from(MailActivity.this);
		// final View v = factory.inflate(
		// R.layout.javascript_prompt_dialog, null);
		// ((TextView) v.findViewById(R.id.prompt_message_text))
		// .setText(message);
		// ((EditText) v.findViewById(R.id.prompt_input_field))
		// .setText(defaultValue);
		//
		// new AlertDialog.Builder(MailActivity.this)
		// .setTitle(R.string.title_dialog_prompt)
		// .setView(v)
		// .setPositiveButton(android.R.string.ok,
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int whichButton) {
		// String value = ((EditText) v
		// .findViewById(R.id.prompt_input_field))
		// .getText().toString();
		// result.confirm(value);
		// }
		// })
		// .setNegativeButton(android.R.string.cancel,
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int whichButton) {
		// result.cancel();
		// }
		// })
		// .setOnCancelListener(
		// new DialogInterface.OnCancelListener() {
		// public void onCancel(DialogInterface dialog) {
		// result.cancel();
		// }
		// }).show();
		//
		// return true;
		// };
		
		// For Android < 3.0
	    public void openFileChooser( ValueCallback<Uri> uploadMsg ){
	    	mUploadMessage = uploadMsg;
	        openFileChooser( uploadMsg, "" );
	    }

	    // For Android 3.0+
	    public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType ){  
	    	mUploadMessage = uploadMsg;
	        Intent i = new Intent(Intent.ACTION_GET_CONTENT);  
	        //i.addCategory(Intent.CATEGORY_OPENABLE);  
	        i.setType("*/*");  
	        i.putExtra("return-data", true);
	        
	        PackageManager pkManager = getPackageManager();
	        List<ResolveInfo> activities = pkManager.queryIntentActivities(i, 0);
			if (activities.size() > 1) {
				startActivityForResult(Intent.createChooser(i, FILECHOOSER_MESSAGE), FILECHOOSER_RESULTCODE);
			} else {
				startActivityForResult(i, FILECHOOSER_RESULTCODE);
			}
	    }
	    // For Android 4.1+
	    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
	    	mUploadMessage = uploadMsg;
	        openFileChooser( uploadMsg, "" );
	    }
	    
	    @Override
	    public boolean onCreateWindow(WebView view, boolean isDialog,
	    		boolean isUserGesture, Message resultMsg) {
	    	
	    	WebView childView = new BaseWebView(context); 
            final WebSettings settings = childView.getSettings();         
            settings.setJavaScriptEnabled(true); 
            settings.setJavaScriptCanOpenWindowsAutomatically(true);
            settings.setSupportMultipleWindows(true);
            childView.setWebViewClient(new setWebViewClient());
            childView.setWebChromeClient(this);
            bodyFramLayout.addView(childView, 1);
            transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);         
            resultMsg.sendToTarget();
            
	    	return true;
	    }
	    
	    @Override
	    public void onCloseWindow(WebView window) {
	    	
	    	bodyFramLayout.removeView(window);
	    	
	    	super.onCloseWindow(window);
	    }
	}
	
	class setWebViewClient extends WebViewClient{

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith("file://")){
				return super.shouldOverrideUrlLoading(view, url);
			} else {
				view.loadUrl(url);
			}
			return true;
		}
	}
	
	public class javaScriptInterface {
		
		Activity context;
		
		public javaScriptInterface(Activity context) {
			
			this.context = context;
		}
		
		public void back(){
			
			context.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					onBackPressed();
				}
			});
			
		}
		
		public void reload(final String key){
			
			context.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					
					System.out.println(key);
					
					webView.loadUrl("javascript:window.localStorage.removeItem('" + key + "')");
					webView.loadUrl("javascript:window.sessionStorage.removeItem('" + key + "')");
					webView.reload();
				}
			});
			
		}
	} 
}
