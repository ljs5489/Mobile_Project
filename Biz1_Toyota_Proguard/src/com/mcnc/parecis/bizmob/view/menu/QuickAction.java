package com.mcnc.parecis.bizmob.view.menu;

import org.json.JSONException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow.OnDismissListener;

import com.mcnc.hsmart.controller.Smart2ProcessController;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.def.AppConfig;
import com.mcnc.hsmart.interfaces.MemoryInterface;
import com.mcnc.hsmart.interfaces.SqliteInterface;
import com.mcnc.hsmart.plugin.BizmobPluginManager;
import com.mcnc.hsmart.util.JsonUtil;
import com.mcnc.hsmart.wrapper.ImageWrappter;
import com.mcnc.parecis.bizmob.configuration.provider.LocalFileProviderImp;
import com.mcnc.parecis.bizmob.util.ProjectUtils;
import com.mcnc.parecis.toyota.R;

/**
 * QuickAction dialog, shows action list as icon and text like the one in Gallery3D app. Currently supports vertical 
 * and horizontal layout.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 * Contributors:
 * - Kevin Peck <kevinwpeck@gmail.com>
 */
public class QuickAction extends PopupWindows implements OnDismissListener {
	
	private final String TAG = this.toString();
	
	private View mRootView;
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private LayoutInflater mInflater;
	private LinearLayout mTrack;
	private LinearLayout mScroller;
	private OnActionItemClickListener mItemClickListener;
	private OnDismissListener mDismissListener;
	
	private boolean mDidAction;
	
	private int mChildPos;
    private int mInsertPos;
    private int mAnimStyle;
    private int mOrientation;
    private int rootWidth=0;
    
    private Context context;
    private WebView webView;
    
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    
    public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_REFLECT = 4;
	public static final int ANIM_AUTO = 5;
	
    /**
     * Constructor for default vertical layout
     * 
     * @param context  Context
     */
    public QuickAction(Context context) {
    	
        this(context, VERTICAL, "", "", "");
    }

    /**
     * Constructor allowing orientation override
     * 
     * @param context    Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public QuickAction(Context context, int orientation, String target_page, String height, String width) {
        super(context);
        
        this.context = context;
        mOrientation = orientation;
        
        mInflater 	 = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setRootViewId(R.layout.popup_vertical, target_page, height, width);

        mAnimStyle 	= ANIM_AUTO;
        mChildPos 	= 0;
    }

	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id
	 */
	public void setRootViewId(int id, String target_page, String height, String width) {
		mRootView	= (ViewGroup) mInflater.inflate(id, null);
		mTrack 		= (LinearLayout) mRootView.findViewById(R.id.tracks);

		mArrowDown 	= (ImageView) mRootView.findViewById(R.id.arrow_down);
		mArrowUp 	= (ImageView) mRootView.findViewById(R.id.arrow_up);

		mScroller	= (LinearLayout) mRootView.findViewById(R.id.scroller);
		
		//This was previously defined on show() method, moved here to prevent force close that occured
		//when tapping fastly on a view to show quickaction dialog.
		//Thanx to zammbi (github.com/zammbi)
		mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		webView = new WebView(context);
		webViewSetting();
		
		float tWidth = Float.parseFloat(width);
		float tHeight = Float.parseFloat(height);

		LinearLayout.LayoutParams webviewParam = new LinearLayout.LayoutParams(
				(int) tWidth, (int) tHeight, 0);	
		
		mTrack.setLayoutParams(webviewParam);
		mTrack.addView(webView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		String url = ImageWrappter.getUri(target_page);
		webView.loadUrl(url);
		
		setContentView(mRootView);
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void webViewSetting(){
		
		this.webView.setFocusable(true);
		this.webView.setScrollBarStyle(Context.BIND_AUTO_CREATE);
		// Bridge 인스턴스 등록
		this.webView.setWebViewClient(new BaseWebViewClient((BaseActivity)context)); // WebViewClient
		this.webView.setWebChromeClient(new BaseWebChromeClient());
		
		this.webView.setInitialScale(100);
		this.webView.setVerticalScrollBarEnabled(true);
		this.webView.requestFocusFromTouch();
		this.webView.setFocusable(true);
		
		WebSettings settings = this.webView.getSettings();
		
		settings.setDefaultTextEncodingName("UTF-8");
		settings.setJavaScriptEnabled(true);
		settings.setDomStorageEnabled(true);
		settings.setBuiltInZoomControls(true);
		settings.setUseWideViewPort(true);
		settings.setAppCacheEnabled(false);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setAppCacheMaxSize(0);

		if(android.os.Build.VERSION.SDK_INT >= 11) {
			settings.setDisplayZoomControls(false);
		}
		settings.setSupportZoom(true);
		
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		
		this.webView.addJavascriptInterface( SqliteInterface.getInstance(), "AndroidSqlite");
		this.webView.addJavascriptInterface( MemoryInterface.getInstance(), "MemoryMap");
		//plugin 추가
		this.webView.addJavascriptInterface(new BizmobPluginManager((Activity) context, webView), "BizmobPluginMgr");
		this.webView.setBackgroundColor(AppConfig.WEBVIEW_BACKGROUNDCOLOR_NORMAL);
		SqliteInterface.getInstance().setContext( LocalFileProviderImp.URI_PREFIX );
	}
	
	/**
	 * Set animation style
	 * 
	 * @param mAnimStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int mAnimStyle) {
		this.mAnimStyle = mAnimStyle;
	}
	
	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	public void setOnActionItemClickListener(OnActionItemClickListener listener) {
		mItemClickListener = listener;
	}
	
	/**
	 * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
	 * 
	 */
	public void show (View anchor) {
		preShow();
		
		int xPos, yPos, arrowPos;
		
		mDidAction 			= false;
		
		int[] location 		= new int[2];
	
		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());

		//mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
		int rootHeight 		= mRootView.getMeasuredHeight();
		
		if (rootWidth == 0) {
			rootWidth		= mRootView.getMeasuredWidth();
		}
		
		int screenWidth 	= mWindowManager.getDefaultDisplay().getWidth();
		int screenHeight	= mWindowManager.getDefaultDisplay().getHeight();
		
		//automatically get X coord of popup (top left)
		if ((anchorRect.left + rootWidth) > screenWidth) {
			xPos 		= anchorRect.left - (rootWidth-anchor.getWidth());			
			xPos 		= (xPos < 0) ? 0 : xPos;
			
			arrowPos 	= anchorRect.centerX()-xPos;
			
		} else {
			if (anchor.getWidth() > rootWidth) {
				xPos = anchorRect.centerX() - (rootWidth/2);
			} else {
				xPos = anchorRect.left;
			}
			
			arrowPos = anchorRect.centerX()-xPos;
		}
		
		int dyTop			= anchorRect.top;
		int dyBottom		= screenHeight - anchorRect.bottom;

		boolean onTop		= (dyTop > dyBottom) ? true : false;

		if (onTop) {
			if (rootHeight > dyTop) {
				yPos 			= 15;
				LayoutParams l 	= mScroller.getLayoutParams();
				l.height		= dyTop - anchor.getHeight();
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;
			
			if (rootHeight > dyBottom) { 
				LayoutParams l 	= mScroller.getLayoutParams();
				l.height		= dyBottom;
			}
		}
		
		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);
		
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos-10);
	}
	
	public void setCallback(String callback, Object data){
		
		webView.loadUrl("javascript:" + callback + "(" + data + ")");
	}
	
	/**
	 * Set animation style
	 * 
	 * @param screenWidth screen width
	 * @param requestedX distance from left edge
	 * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor view
	 * 		  and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
		int arrowPos = requestedX - mArrowUp.getMeasuredWidth()/2;

		switch (mAnimStyle) {
		case ANIM_GROW_FROM_LEFT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			break;
					
		case ANIM_GROW_FROM_RIGHT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			break;
					
		case ANIM_GROW_FROM_CENTER:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
		break;
			
		case ANIM_REFLECT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
		break;
		
		case ANIM_AUTO:
			if (arrowPos <= screenWidth/4) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			} else if (arrowPos > screenWidth/4 && arrowPos < 3 * (screenWidth/4)) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
			} else {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			}
					
			break;
		}
	}
	
	/**
	 * Show arrow
	 * 
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);
        
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
       
        param.leftMargin = requestedX - arrowWidth / 2;
        
        hideArrow.setVisibility(View.INVISIBLE);
    }
	
	/**
	 * Set listener for window dismissed. This listener will only be fired if the quicakction dialog is dismissed
	 * by clicking outside the dialog or clicking on sticky item.
	 */
	public void setOnDismissListener(QuickAction.OnDismissListener listener) {
		setOnDismissListener(this);
		
		mDismissListener = listener;
	}
	
	@Override
	public void onDismiss() {
		if (!mDidAction && mDismissListener != null) {
			mDismissListener.onDismiss();
		}
	}
	
	/**
	 * Listener for item click
	 *
	 */
	public interface OnActionItemClickListener {
		public abstract void onItemClick(QuickAction source, int pos, int actionId, String url);
	}
	
	/**
	 * Listener for window dismiss
	 * 
	 */
	public interface OnDismissListener {
		public abstract void onDismiss();
	}
	
	public void destroy(){
		if(webView != null && webView.getUrl() != null){
			webView.clearCache(false);
			webView.clearAnimation();
			webView.clearHistory();
			webView.freeMemory();
			webView.clearView();
			webView.destroyDrawingCache();
			webView.destroy();
			webView = null;
		}
	}
	
	private class BaseWebViewClient extends WebViewClient {

		BaseActivity ctx;

		/**
		 * Constructor.
		 * 
		 * @param ctx
		 */
		public BaseWebViewClient(BaseActivity ctx) {
			this.ctx = ctx;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String requestURL) {
			if (requestURL.startsWith("http://") || requestURL.startsWith("https://") ) {
				
				if ( ProjectUtils.isAllowUrl(requestURL)) {
					
					super.shouldOverrideUrlLoading(view, requestURL);
					
					view.loadUrl(requestURL);
				}	
			}else if (requestURL.startsWith("file://")) {
				
				super.shouldOverrideUrlLoading(view, requestURL);
			} else if (!requestURL.startsWith("mcnc://")) {
				
			}else{
				
				if(mItemClickListener != null){
					
					mItemClickListener.onItemClick(QuickAction.this, 0, 0, requestURL);
				}
			}
//			(ImpMainActivity) activity).onAction(view, url);
			
			return true;
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			if( Logger.LogLevel < Logger.DEBUG ) {
				Logger.d(TAG, "onLoadResource");
				Logger.d(TAG, "view::" + view);
				Logger.d(TAG, "url::" + url);
			}
			super.onLoadResource(view, url);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			if( Logger.LogLevel < Logger.DEBUG ) {
				Logger.d(TAG, "onPageFinished");
				Logger.d(TAG, "view::" + view);
				Logger.d(TAG, "url::" + url);
				Logger.d(TAG, "thread::" + Thread.currentThread());
			}

			try {
				
				if(webView != null){
					
					webView.loadUrl("javascript:" + "bizMOB.MStorage = " + JsonUtil.getMapToJson(Smart2ProcessController.memoryData).toString());
					webView.loadUrl("javascript:" + "bizMOB.FStorage = " + JsonUtil.getPreferencesToJson(Smart2ProcessController.webSharedPreferences));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			webView.loadUrl("javascript:appcallOnLoad(" + null + ")");
			
			super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// Logger.d(TAG, "onPageStarted");
			// Logger.d(TAG, "view::" + view);
			// Logger.d(TAG, "url::" + url);
			super.onPageStarted(view, url, favicon);
		}

		/**
		 * Report an error to the host application. These errors are
		 * unrecoverable (i.e. the main resource is unavailable). The errorCode
		 * parameter corresponds to one of the ERROR_* constants.
		 * 
		 * @param view
		 *            The WebView that is initiating the callback.
		 * @param errorCode
		 *            The error code corresponding to an ERROR_* value.
		 * @param description
		 *            A String describing the error.
		 * @param failingUrl
		 *            The url that failed to load.
		 */
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			Logger.f(TAG, "onReceivedError");
			Logger.f(TAG, "errorCode::" + errorCode);
			Logger.f(TAG, "description::" + description);
			Logger.f(TAG, "failingUrl::" + failingUrl);

			super.onReceivedError(view, errorCode, description, failingUrl);
		}
		
		// SSL 예외처리
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			if (error.getPrimaryError() == SslError.SSL_IDMISMATCH) {
				Log.e(TAG, "SslError.SSL_IDMISMATCH");
				handler.proceed();
			} else if (error.getPrimaryError() == SslError.SSL_EXPIRED) {
				Log.e(TAG, "SslError.SSL_EXPIRED");
				handler.proceed();
			} else if (error.getPrimaryError() == SslError.SSL_MAX_ERROR) {
				Log.e(TAG, "SslError.SSL_MAX_ERROR");
				handler.proceed();
			} else if (error.getPrimaryError() == SslError.SSL_NOTYETVALID) {
				Log.e(TAG, "SslError.SSL_NOTYETVALID");
				handler.proceed();
			} else if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
				Log.e(TAG, "SslError.SSL_UNTRUSTED");
				handler.proceed();
			} else {
				Log.e(TAG, "error.getPrimaryError()");
				handler.proceed();
			}
		}
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
	}
}