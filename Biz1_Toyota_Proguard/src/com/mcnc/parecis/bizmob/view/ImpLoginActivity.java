package com.mcnc.parecis.bizmob.view;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.mcnc.hsmart.configuration.model.ConfigurationModel;
import com.mcnc.hsmart.configuration.model.InitializeConfiguration;
import com.mcnc.hsmart.configuration.provider.LocalFileProviderHsmart;
import com.mcnc.hsmart.configuration.view.NetworkConfigurationActivity;
import com.mcnc.hsmart.controller.Smart2ProcessController;
import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.download.AbstractDownloadService;
import com.mcnc.hsmart.core.exception.RequestException;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.util.FileUtils;
import com.mcnc.hsmart.core.util.ImageUtil;
import com.mcnc.hsmart.core.util.StringUtil;
import com.mcnc.hsmart.core.view.AbstractActivity;
import com.mcnc.hsmart.core.view.param.LayoutParam;
import com.mcnc.hsmart.def.AppConfig;
import com.mcnc.hsmart.interfaces.SqliteInterface;
import com.mcnc.hsmart.potal.DeviceRegistActivity;
import com.mcnc.hsmart.potal.PotalLogic;
import com.mcnc.hsmart.util.ActivityObserver;
import com.mcnc.hsmart.util.AppConfigReader;
import com.mcnc.hsmart.view.ContextMenuButton;
import com.mcnc.hsmart.wrapper.ImageWrappter;
import com.mcnc.parecis.bizmob.configuration.provider.LocalFileProviderImp;
import com.mcnc.parecis.bizmob.def.Def;
import com.mcnc.parecis.bizmob.def.TaskID;
import com.mcnc.parecis.bizmob.util.ProjectUtils;

public class ImpLoginActivity extends ImpMainActivity {
	private final String START_PAGE = "login/html/login.html";
	private final String TAG = this.toString();

	private Context mContext;
	private boolean bCreate = true;
	private boolean bVersionCheck = false;
	
	private static final String TARGET_PAGE = "target_page";
	private final String APP_CONFIG = "bizMOB/config/app/app.config";
	private final int REQUEST_CODE_CONFIGURATION = 100;
	private final int REQUEST_CODE_DEVICE_REGIST = 1020;
	
	@Override
	public void onCreateContent(Bundle savedInstanceState) {
		// Low Memory Killer에 의해서 죽고  프로그램을 다시 실행시킬경우 초기화된 클래스만 로드가된다.
		// 아래의 플래그를 보고 프로그램이 강제종료됐음을 체크한다.
		Def.IS_RESTART_BY_LOW_MEMORY = false;
		mContext = this;
		/*
		 * 해당 프로젝트의 기본 설정 값
		 */
		initProject();
		// 프로그레스 설정
		initTaskProgress();
		// 환경 설정 및 개발 모드 설정
		setRunConfiguration();
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		startService  ();

		if ( !Def.IS_RELEASE ) {
			if (! ConfigurationModel.getConfigurationModel().getDevSetting()) {
				Intent intent = new Intent(this, NetworkConfigurationActivity.class);
				intent.putExtra("isDev", true);
				startActivityForResult(intent, REQUEST_CODE_CONFIGURATION );
				bCreate = false;
				bVersionCheck = false;
			} else { //운영용이면
				ConfigurationModel cm = ConfigurationModel.getConfigurationModel();
				if ( cm.getDeviceRegistyMode().toLowerCase().equals("true") && !PotalLogic.checkRestry() ){
					// 등록 페이지 옮기기
					bCreate = false;
					Intent intent = new Intent(this, DeviceRegistActivity.class);
					startActivityForResult(intent, REQUEST_CODE_DEVICE_REGIST);
					
					return;
				}
			}
		} else {
			if(!PotalLogic.checkRestry()) {
				// 등록 페이지 옮기기
				bCreate = false;
				Intent intent = new Intent(this, DeviceRegistActivity.class);
				startActivityForResult(intent, REQUEST_CODE_DEVICE_REGIST);
				
				return;
			}	
		}

		if (Def.IMAGE_MODE == ImageUtil.MODE_LOCAL) {
			
			bCreate = false;
			processDownloadCheck();
			return;
		}
		
		Intent intent = getIntent();
		intent.putExtra("callback", "appcallOnLoad");
		intent.putExtra("data", "");
		intent.putExtra("dataKey", "");
		
		super.onCreateContent(savedInstanceState);
		// 메모리 한번 싹 정리 
		//LMCServiceUtil.requestKillProcess (this);
		startService  ();
	}
	
	public void initProject() {
		// 최종 배포시 true
		Def.IS_RELEASE = false;//이거 된다.
		Def.USE_VERSION_CHECK = true;		
		Def.PROJECT_NAME = "TOYOANP0";
		Def.APPLICTION_NAME = "TOYOANP0";
		
		// config version 을 올릴 경우 기존  contents version 외에는 다시 설정을 함.
		Def.CONFIG_VERSION = 1;
		
		Def.TRCODE_LOGIN = "LOGIN";
		Def.IMAGE_ROOT = "http://10.1.209.78:8080/TFS_Mobile_app";
		
		if( Def.IS_RELEASE ) {
			
			Def.ROOT_URL_IP = "tfskr.com";	
			Def.ROOT_URL_PORT = "443";
			Def.ROOT_URL_DIR = "bizmob";	
			Def.LOG_LEVEL = Logger.ERROR;
			Def.IMAGE_MODE = Def.MODE_LOCAL;
			Def.IS_SSL = true;
		} else {
			Def.ROOT_URL_IP = "10.1.209.78";	
			Def.ROOT_URL_PORT = "8070";
			Def.ROOT_URL_DIR = "bizmob";	
			Def.LOG_LEVEL = Logger.DEBUG;
			Def.IMAGE_MODE = Def.MODE_HTTP;
			Def.IS_SSL = false;
		}
		
		Def.ROOT_URL_EXT = ".json";
		Def.ROOT_DOWNLOAD_URL_IP 	= Def.ROOT_URL_IP;
		Def.ROOT_DOWNLOAD_URL_PORT 	= Def.ROOT_URL_PORT;
		Def.ROOT_DOWNLOAD_URL_DIR 	= Def.ROOT_URL_DIR;
		Def.IS_SSL_DOWNLOAD 		= Def.IS_SSL;

		Def.UPDATE_URL_IP 	= Def.ROOT_URL_IP;
		Def.UPDATE_URL_PORT = Def.ROOT_URL_PORT;
		Def.UPDATE_URL_DIR 	= Def.ROOT_URL_DIR;
		Def.UPDATE_URL_SSL 	= Def.IS_SSL;

		Def.PUSH_URL_IP 		= Def.ROOT_URL_IP;
		Def.PUSH_URL_PORT	 	= Def.ROOT_URL_PORT;
		Def.PUSH_URL_CONTEXT 	= Def.ROOT_URL_DIR;
		Def.PUSH_URL_SSL 		= Def.IS_SSL;
		
		
		Def.CONTENT_UPGRADE_URI = "download/update";
		Def.ROOT_DIR_NATIVE = "native";
		Def.ROOT_DIR_TEMP = "temp";

		Def.TITLE_H = 65;
		Def.BOTTOM_H = 80;
		Def.TRCODE_DOWNLOAD = "ZZ0006";
		
		Def.topTitleColor_02  = Color.rgb(255, 255, 255);

		DisplayMetrics metrics = new DisplayMetrics();     
		getWindowManager().getDefaultDisplay().getMetrics(metrics);     
		Def.screenDensity = metrics.densityDpi;
		ImageUtil.setDensity( Def.screenDensity );
		if ( Def.screenDensity == 160 ) {
			Def.TOP_BUTTON_W = 300;
		} else if ( Def.screenDensity == 320) {
			Def.TITLE_H = 91;
			Def.BOTTOM_H = 116;
			Def.TOP_BUTTON_W = 180;
		} else if ( Def.screenDensity == 480) {
			Def.TITLE_H = 135;
			Def.BOTTOM_H = 160;
			Def.TOP_BUTTON_W = 240;
		}
		
		ImageWrappter.setUseTargetDensity( true );
		if ( Def.screenDensity == 320 ) {
			ImageWrappter.setTargetDensity(320);
		} else if ( Def.screenDensity == 240 ) {
			ImageWrappter.setTargetDensity(160);
		}  else if ( Def.screenDensity == 160 ) {
			ImageWrappter.setTargetDensity(120);
		} if ( Def.screenDensity == 480 ) {
			ImageWrappter.setTargetDensity( 720 );
		}
		
		ContextMenuButton.setContextMenuButtonBgColor(0);
		ContextMenuButton.setContextLayoutColor( 0xD9534334 );
		ContextMenuButton.setContextMenuButtonTextColor(0xFFFFFFFF);
		
		// 필요할 경우 메시지 바꾸기 바람
		// ExceptionUtil.HTTPHOSTCONNECTEXCEPTION =
		// "서버와 연결할수 없습니다. 잠시 후 다시 시도해 주세요";
		// ExceptionUtil.SOCKETEXCEPTION = "통신상태가 양호하지 않습니다. 네트워크 연결을 확인해 주세요.";
		// ExceptionUtil.IOEXCEPTION = "통신상태가 양호하지 않습니다. 네트워크 연결을 확인해 주세요.";
		// ExceptionUtil.REQCANCELEXCEPTION = "요청이 취소되었습니다.";
		// ExceptionUtil.JSONEXCEPTION = "InvalidResponseData::";
		// ExceptionUtil.NULLPOINTEREXCEPTION = "NullPointerException";
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		if( requestCode == REQUEST_CODE_DEVICE_REGIST) {
			if ( !PotalLogic.checkRestry() ) {
				terminate();
			} else {
				processDownloadCheck();
			}
			return;
		}
		if ( requestCode == REQUEST_CODE_CONFIGURATION ) {
			AppConfigReader.LoadAppConfig ( this, APP_CONFIG );
			String url = ImageWrappter.getUri(START_PAGE);
			if (AppConfig.START_PAGE != null && AppConfig.START_PAGE.length() > 0 ) {
				url = ImageWrappter.getUri( AppConfig.START_PAGE);
			}
			//loadUrl(url);
			Intent intent2 = getIntent();
			loadUrl(webView, url, intent2);
		}
		if ( Def.USE_VERSION_CHECK &&  ! bVersionCheck ) { 
			processDownloadCheck();
			return; 
		}
		if (!bCreate) {
			if ( ! bVersionCheck ) { 
				processDownloadCheck(); 
				return; 
			}
			CreateContents();
		}
	}

	@Override
	protected void setBody(LinearLayout baseLayout) {
		bodyFramLayout = new FrameLayout(this);
		LinearLayout.LayoutParams bodyLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1); 
		init();

		if ( ImageUtil.MODE_LOCAL != Def.IMAGE_MODE ) {
			AppConfigReader.LoadAppConfig ( this, APP_CONFIG );
			String url = ImageWrappter.getUri(START_PAGE);
			if (AppConfig.START_PAGE != null && AppConfig.START_PAGE.length() > 0 ) {
				url = ImageWrappter.getUri( AppConfig.START_PAGE);
			}
			//loadUrl(url);
			Intent intent  = getIntent();
			loadUrl(webView, url, intent);
		}
		bodyFramLayout.addView(webView, 0);
		baseLayout.addView(bodyFramLayout, bodyLayoutParam);
	}

	@Override
	protected void setBottom(LinearLayout baseLayout) {
		super.setBottom(baseLayout);
		btoolbarVisible = false;
		bottomLayout.setVisibility(View.GONE);
		emptyLayout.setVisibility(View.GONE);
	}

	@Override
	protected void setHeader(LinearLayout baseLayout) {
		super.setHeader(baseLayout);
		headerLayout.setVisibility(View.GONE);
	}

	@Override
	public void onSuccess(Response response) {
		// html이 로드 되지 않은 상황일수가 있으므로 따로 처리
		Request req = response.getRequest();
		if ( req != null && req.getID().equals(TaskID.TASK_ID_UPDATE20)){
			AppConfigReader.LoadAppConfig ( this, APP_CONFIG );
			String url = ImageWrappter.getUri(START_PAGE);
			if (AppConfig.START_PAGE != null && AppConfig.START_PAGE.length() > 0 ) {
				url = ImageWrappter.getUri( AppConfig.START_PAGE);
			}
			hideProgress();
			//loadUrl(url);
			
			CreateContents();
			
			Intent intent = getIntent();
			loadUrl(webView, url, intent);
			return;
		}
		super.onSuccess(response);
	}
	
	protected void CreateContents() {
		Intent oldIntent = getIntent();
		oldIntent.putExtra("callback", "appcallOnLoad");
		oldIntent.putExtra("data", "");
		oldIntent.putExtra("dataKey", "");
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Logger.d(TAG, "initent::" + getIntent());
		frameLayout = new FrameLayout(this);
		// baseLayout
		baseLayout = new LinearLayout(this);
		baseLayout.setLayoutParams(LayoutParam.FF);
		Logger.d(TAG, "baseLayout::" + baseLayout);
		baseLayout.setOrientation(LinearLayout.VERTICAL);
		// setHeader
		setHeader(baseLayout);
		// setBody
		setBody(baseLayout);
		frameLayout.addView(baseLayout);
		// setBottom
		setBottom(baseLayout);
		// setContentView
		setContentView(frameLayout);
		
		bCreate = true;
	}

	@Override
	public void onBackPressed() {
		if ( back_action.length() > 0 ){
			Logger.d (TAG, "javascript:back_action()");
			webView.loadUrl("javascript:"+back_action+"()");
		} else {
			terminate();
		}	
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		if (intent.getBooleanExtra("session_timeout", false)){
			Toast.makeText(ImpLoginActivity.this, "1", Toast.LENGTH_SHORT).show();
			terminate();
		} else if (intent.getBooleanExtra("Exit", false) == true) {
			Toast.makeText(ImpLoginActivity.this, "2", Toast.LENGTH_SHORT).show();
			terminate();
		} else if (intent.getBooleanExtra("Logout", false) == true) {
			Toast.makeText(ImpLoginActivity.this, "3", Toast.LENGTH_SHORT).show();
			terminate();
		} else if (intent.getBooleanExtra("Restart", false) == true) {
			Toast.makeText(ImpLoginActivity.this, "4", Toast.LENGTH_SHORT).show();
			restart ();
		}
	}
	
	@Override
	public void onDestroy() {
		stopService ();
		super.onDestroy();
	}
	
	private void startService () {
        Intent intent = new Intent(ForegroundService.ACTION_FOREGROUND);
        intent.setClass( ImpLoginActivity.this, ForegroundService.class);
        startService(intent);
		
	}
	
	private void stopService () {
        stopService(new Intent( this, ForegroundService.class));
	}

	private void terminate () {
		stopService ();
		deleteCashe();
		SqliteInterface.getInstance().closeDatabase();
		requestKillProcess(this);
		((Activity) mContext).finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	private void restart () {
		// MStorage 초기화
		Smart2ProcessController.memoryData = new HashMap<String, String>();
		// 버전체크 하도록. 
		bVersionCheck = false;
		processDownloadCheck();
		
		// 페이지 로드
		Intent intent2 = getIntent();
		intent2.putExtra("callback", "appcallOnLoad");
		intent2.putExtra("data", "");
		intent2.putExtra("dataKey", "");
		AppConfigReader.LoadAppConfig ( this, APP_CONFIG );
		String url = ImageWrappter.getUri(START_PAGE);
		if (AppConfig.START_PAGE != null && AppConfig.START_PAGE.length() > 0 ) {
			url = ImageWrappter.getUri( AppConfig.START_PAGE);
		}
		//loadUrl(url);
		Intent intent = getIntent();
		loadUrl(webView, url, intent);
	}

	public void processDownloadCheck() {
		Logger.d(TAG, "processDownloadCheck USE_VERSION_CHECK: " + Def.USE_VERSION_CHECK + "   bVersionCheck : " + bVersionCheck  );
		if ( ! Def.USE_VERSION_CHECK || bVersionCheck ){
			return;
		}
		if (Def.IMAGE_MODE != ImageUtil.MODE_LOCAL) {
			
			if(!bCreate){
				
				CreateContents();
				
				AppConfigReader.LoadAppConfig ( this, APP_CONFIG );
				String url = ImageWrappter.getUri(START_PAGE);
				if (AppConfig.START_PAGE != null && AppConfig.START_PAGE.length() > 0 ) {
					url = ImageWrappter.getUri( AppConfig.START_PAGE);
				}
				//loadUrl(url);
				Intent intent2 = getIntent();
				loadUrl(webView, url, intent2);
			}
			
			return;
		}
		
		bVersionCheck = true;
		
		Def.TRCODE_DOWNLOAD = "ZZ0006";
		request = new Request();
		request.setTrCode(Def.TRCODE_DOWNLOAD);
		request.setTargetPage("targetPage");
		request.setSrcActivity(ImpLoginActivity.this);
		execute(TaskID.TASK_ID_UPDATE20, request, false, 0);		
	}

	@Override
	public void onError(Response response) {
		Request req = response.getRequest();
		if ( req != null && req.getID().equals(TaskID.TASK_ID_UPDATE20)){
			hideProgress();
			AppConfigReader.LoadAppConfig ( this, APP_CONFIG );
			String url = ImageWrappter.getUri(START_PAGE);
			if (AppConfig.START_PAGE != null && AppConfig.START_PAGE.length() > 0 ) {
				url = ImageWrappter.getUri( AppConfig.START_PAGE);
			}
			//loadUrl(url);
			Intent intent = getIntent();
			loadUrl(webView, url, intent);
		}
		super.onError(response);
	}

	public void requestKillProcess(final Activity context) {
		// #1. first check api level.
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion < 8) {
			// #2. if we can use restartPackage method, just use it.
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			am.restartPackage(context.getPackageName());
		} else {
			// #3. else, we should use killBackgroundProcesses method.
			new Thread(new Runnable() {
				@Override
				public void run() {
					ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
					String name = context.getApplicationInfo().processName;

					// pooling the current application process importance
					// information.
					while (true) {
						List<RunningAppProcessInfo> list = am
								.getRunningAppProcesses();
						for (RunningAppProcessInfo i : list) {
							if (i.processName.equals(name) == true) {
								if (i.importance >= RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
									String pname = context.getPackageName();
									// am.killBackgroundProcesses(pname);
									// //simple wrapper of killBackgrounProcess
									am.restartPackage(pname);
								} else
									Thread.yield();

								break;
							}
						}
					}
				}
			}, "Process Killer").start();
		}
	}

	@Override
	protected void onAction(View view) {
		super.onAction(view);
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
			if (id.equals("SET_APP")) {
				setContainer(param);
				return;
			} else if (id.equals(TaskID.TASK_ID_GOEXITTASK)) {
				terminate();
				return;
			} else if (id.equals(TaskID.TASK_ID_APPLICATION_RESTART)) {
				restart();
				return;
			} else {
				// 짧은시간 안에 같은 TRCODE는 정송하지 못하도록 함.
				if ( ! checkUrlAction(param) ) {
					return;
				}
				
				String targetPage = StringUtil.getString(param, TARGET_PAGE);
				Logger.d(TAG, "targetPage::" + targetPage);
				
				// create request
				request = new Request();
				request.setSrcActivity(this);
				Smart2ProcessController controller = (Smart2ProcessController)getController();
				Class activity = null;
				activity = controller.getActivityClass(Smart2ProcessController.MAIN_ACTIVITY);
				request.setTargetActivity(activity);
				
				
				request.setCallType(StringUtil.getString(decodedData,
						"call_type"));
				request.setTargetPage(targetPage);
				request.setData(decodedData);
				request.setTrCode(StringUtil.getString(param, "trcode"));
				
				if ( id.equals("GOTO_WEB_MODAL") || id.equals("GOTO_WEB")
						|| id.equals("GOTO_FILE_UPLOAD")) {
					execute(id,
							request,
							true,
							AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
				} else if (id.equals("RELOAD_WEB") || id.equals("REPLACE_WEB")
						|| id.equals(TaskID.TASK_ID_AUTH)){
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

	protected void setRunConfiguration() {
		try {
			new InitializeConfiguration(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
		// MODE_LOCAL: update하는 내용에 hybrid 폴더가 전부 있어야함
		// MODE_ASSET: assest 폴더에 hybrid가 전부 있어야함
		// MODE_HTTP: webResource를 모두 서버에서 받아옴
		ConfigurationModel cm = ConfigurationModel.getConfigurationModel();
		ImageUtil.setProviderPath(LocalFileProviderImp.URI_PREFIX);
		LocalFileProviderHsmart
				.setProviderPath(LocalFileProviderImp.URI_PREFIX);
		LocalFileProviderHsmart
				.setProviderName(LocalFileProviderImp.URI_LOCALFILE);
		
		// 환경설정 적용
		cm.applyConfiguration();

		// AbstractDownloadService.setDownloadPath(
		// Environment.getExternalStorageDirectory().getAbsolutePath()
		// +"/"+Def.ROOT_DOWNLOAD_DIR);
		AbstractDownloadService
				.setDownloadPath(LocalFileProviderImp.URI_PREFIX
						+ Def.ROOT_DOWNLOAD_DIR);
		// 로컬 저장일 경우 폴더를 반드시 지운다...
		String path = LocalFileProviderImp.URI_PREFIX + Def.ROOT_DOWNLOAD_DIR;
		File f = new File(path);
		if (f.exists()) {
			FileUtils.delete(f);
			Logger.d(TAG, "delete : " + LocalFileProviderImp.URI_PREFIX
					+ Def.ROOT_DOWNLOAD_DIR);
		}
		
		// 세션 타임아웃 20분
		ActivityObserver.setTime(60*30);
		ActivityObserver.AUTO_LOGOUT = "장시간 미사용으로 접속이 끊어졌습니다. 다시 앱을 실행해주시기 바랍니다.";
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ( ! Def.IS_RELEASE ) {
			if (keyCode == KeyEvent.KEYCODE_MENU) {
				Logger.d(TAG, "onKeyDown MENU");
				Intent intent2 = new Intent(this, NetworkConfigurationActivity.class);
				intent2.putExtra("isDev", true);
				startActivityForResult(intent2, REQUEST_CODE_CONFIGURATION);
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	private void deleteCashe () {
		String path = LocalFileProviderImp.URI_PREFIX + "cache/webviewCache";
		File f = new File(path);
		if (f.exists()) {
			FileUtils.delete(f);
			Logger.d(TAG, "delete : " + LocalFileProviderImp.URI_PREFIX + "cache/webviewCache" );
		}
		this.deleteDatabase("webview.db");
		this.deleteDatabase("webviewCache.db");
	}	
	
	@Override
	protected void onPageFinished(WebView arg0, String arg1) {
		super.onPageFinished(arg0, arg1);
		
		Intent intent = getIntent();
		if (intent.hasExtra("external_callback")){
			String callback = null;
			String data = null;
			callback = intent.getStringExtra("external_callback");
			if ( intent.hasExtra("external_data")){
				data = intent.getStringExtra("external_data");
				intent.removeExtra("external_data");
			}
			intent.removeExtra("external_callback");

			Logger.d(TAG, "onPageFinished external_data:" + data);
			Logger.d(TAG, "onPageFinished external_callback:" + callback);
			
			if ( webView != null && callback!= null && callback.length() > 0 ) {
				if (data != null ) {
					Logger.d(TAG, "javascript:" + callback  + "(" + data + ")");
					webView.loadUrl("javascript:" + callback  + "(" + data + ")");
				} else {
					Logger.d(TAG, "javascript:" + callback  + "()");
					webView.loadUrl("javascript:" + callback  + "()");
				}
			}
		}
	}
	
	protected void initTaskProgress() {
		progressbarMap.put(TaskID.TASK_ID_CLEAR_FSTORAGE, false);
		progressbarMap.put(TaskID.TASK_ID_GET_DIRECTORY_INFO, false);
		progressbarMap.put(TaskID.TASK_ID_COPY_FILE, false);
		progressbarMap.put(TaskID.TASK_ID_MOVE_FILE, false);
		progressbarMap.put(TaskID.TASK_ID_REMOVE_FILES, false);
		progressbarMap.put(TaskID.TASK_ID_EXISTS_FILE, false);
		progressbarMap.put(TaskID.TASK_ID_OPEN_FILE, false);
		progressbarMap.put(TaskID.TASK_ID_READ_FILE, false);
		progressbarMap.put(TaskID.TASK_ID_WRITE_FILE, false);
		progressbarMap.put(TaskID.TASK_ID_SET_FSTORAGE, false);
		progressbarMap.put(TaskID.TASK_ID_SET_MSTORAGE, false);
		progressbarMap.put(TaskID.TASK_ID_GET_DEVICEINFO, false);
		progressbarMap.put(TaskID.TASK_ID_SET_SESSION_TIMEOUT, false);
		progressbarMap.put(TaskID.TASK_ID_GET_SYSTEM_SETTING, false);
		progressbarMap.put(TaskID.TASK_ID_GET_LOCATION, false);
		progressbarMap.put(TaskID.TASK_ID_DOWNLOAD, false);
	}
	
}
