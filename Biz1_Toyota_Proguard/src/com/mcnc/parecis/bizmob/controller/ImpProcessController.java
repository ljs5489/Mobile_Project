package com.mcnc.parecis.bizmob.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;

import com.mcnc.hsmart.controller.Smart2ProcessController;
import com.mcnc.hsmart.core.download.AbstractDownloadService;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.upload.AbstractUploadService;
import com.mcnc.hsmart.view.DownloadListActivity;
import com.mcnc.parecis.bizmob.def.TaskID;
import com.mcnc.parecis.bizmob.task.AddContactGroupTask;
import com.mcnc.parecis.bizmob.task.AddContactTask;
import com.mcnc.parecis.bizmob.task.AddContactsTask;
import com.mcnc.parecis.bizmob.task.DelContactTask;
import com.mcnc.parecis.bizmob.task.DownloadImageTask;
import com.mcnc.parecis.bizmob.task.GetContactGroupTask;
import com.mcnc.parecis.bizmob.task.HasContactGroupTask;
import com.mcnc.parecis.bizmob.task.Login20Task;
import com.mcnc.parecis.bizmob.task.ReloadTask;
import com.mcnc.parecis.bizmob.task.ShowInteranlBrowserTask;
import com.mcnc.parecis.bizmob.task.ShowPopupViewTask;
import com.mcnc.parecis.bizmob.task.SpeechRecognitionTask;
import com.mcnc.parecis.bizmob.view.ImpHomeActivity;
import com.mcnc.parecis.bizmob.view.ImpLoginActivity;
import com.mcnc.parecis.bizmob.view.ImpMainActivity;
import com.mcnc.parecis.bizmob.view.TabHomeActivity;
import com.mcnc.parecis.bizmob.view.TabMainActivity;

public class ImpProcessController extends Smart2ProcessController {

	private final String TAG = this.toString();
	private static ImpProcessController instance;
	public static boolean isSessionCheck = false;
	public static boolean isException = false;
	private Context mContext;
	
	private static final String DOWNLOAD_SERVICE_BINDING = "downloadServiceBinding...";
	/*
	 * 각 프로젝트 별 분리를 위한 코드.
	 */
	// FIXME NEW_PROJECT service name 변경 필요
	private static final String COM_MCNC_HSMART_DOWNLOAD_SERVICE = "com.mcnc.parecis.toyota.ImpDownloadService";
	private static final String ON_SERVICE_DISCONNECTED = "onServiceDisconnected";
	private static final String DOWNLOAD_SERVICE = "downloadService::";
	private static final String SERVICE = "service::";
	private static final String ON_SERVICE_CONNECTED = "onServiceConnected";
	private static final String COM_MCNC_HSMART_UPLOAD_SERVICE = "com.mcnc.parecis.toyota.ImpUploadService";
	private static final String UPLOAD_SERVICE_BINDING = "uploadServiceBinding...";
	
	public static ImpProcessController getInstance() {
		return instance;
	}
	
	@Override
	public void onCreate() {
		//super.onCreate();
		webSharedPreferences = getSharedPreferences("webConfigurationSharedPreferences", Activity.MODE_PRIVATE);
		ServiceConnection mDownConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				try {
					Logger.d(TAG, ON_SERVICE_CONNECTED);
					Logger.d(TAG, SERVICE + service);
					downloadService = ((AbstractDownloadService.DownloadServiceBinder) service)
							.getService();
					Logger.d(TAG, DOWNLOAD_SERVICE + downloadService);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Logger.d(TAG, ON_SERVICE_DISCONNECTED);
				downloadService = null;
			}
		};

		Intent bindIntent = new Intent();
		bindIntent.setAction(COM_MCNC_HSMART_DOWNLOAD_SERVICE);
		// Intent bindIntent = new Intent(this, DownloadService.class);
		Logger.d(TAG, DOWNLOAD_SERVICE_BINDING);
		bindService(bindIntent, mDownConnection, Context.BIND_AUTO_CREATE);
		
		
		ServiceConnection mUpConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				try {
					Logger.d(TAG, ON_SERVICE_CONNECTED);
					Logger.d(TAG, SERVICE + service);
					uploadService = ((AbstractUploadService.UploadServiceBinder) service)
							.getService();
					Logger.d(TAG, DOWNLOAD_SERVICE + downloadService);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				Logger.d(TAG, ON_SERVICE_DISCONNECTED);
				uploadService = null;
			}
		};

		Intent upBindIntent = new Intent();
		upBindIntent.setAction(COM_MCNC_HSMART_UPLOAD_SERVICE);
		// Intent bindIntent = new Intent(this, DownloadService.class);
		Logger.d(TAG, UPLOAD_SERVICE_BINDING);
		bindService(upBindIntent, mUpConnection, Context.BIND_AUTO_CREATE);	
		
		/*
		 * 해당 프로젝트에 필요한 Task를 추가하여 사용하도록 한다.
		 */
		super.initTask();
		tasks.put(TaskID.TASK_ID_ADD_CONTACT, AddContactTask.class);
		tasks.put(TaskID.TASK_ID_ADD_CONTACTS, AddContactsTask.class);
		tasks.put(TaskID.TASK_ID_ADD_CONTACT_GROUP, AddContactGroupTask.class);
		tasks.put(TaskID.TASK_ID_DEL_CONTACT, DelContactTask.class);
		tasks.put(TaskID.TASK_ID_HAS_CONTACT_GROUP, HasContactGroupTask.class);
		
		tasks.put(TaskID.TASK_ID_SPEECH_RECOGNITION, SpeechRecognitionTask.class);
		tasks.put(TaskID.TASK_ID_DOWNLOAD_IMAGE, DownloadImageTask.class);
		tasks.put(TaskID.TASK_ID_RELOAD_WEB, ReloadTask.class);
		tasks.put(TaskID.TASK_ID_SHOW_POPUP_VIEW, ShowPopupViewTask.class);
		
		tasks.put(TaskID.TASK_ID_AUTH, Login20Task.class);
		
		tasks.put(TaskID.TASK_ID_GET_CONTACT_GROUP, GetContactGroupTask.class);

		tasks.put("SHOW_INTERNAL_BROWSER", ShowInteranlBrowserTask.class);
		
		putActivityClass(ImpProcessController.START_ACTIVITY, ImpLoginActivity.class);
		if ( isTablet(getApplicationContext())){
			//기본은 폰 UI로 사용함.
			//putActivityClass(ImpProcessController.HOME_ACTIVITY, TabHomeActivity.class);
			//putActivityClass(ImpProcessController.MAIN_ACTIVITY, TabMainActivity.class);
			putActivityClass(ImpProcessController.HOME_ACTIVITY, ImpHomeActivity.class);
			putActivityClass(ImpProcessController.MAIN_ACTIVITY, ImpMainActivity.class);
			putActivityClass(ImpProcessController.DOWN_ACTIVITY, DownloadListActivity.class);
		} else {
			putActivityClass(ImpProcessController.HOME_ACTIVITY, ImpHomeActivity.class);
			putActivityClass(ImpProcessController.MAIN_ACTIVITY, ImpMainActivity.class);
			putActivityClass(ImpProcessController.DOWN_ACTIVITY, DownloadListActivity.class);
		}
	}

	public void setContext(Context context) {
		mContext = context;
	}

	public Context getContext() {
		Logger.d(TAG, mContext.toString());
		mContext = this;
		return mContext;
	}
	
	
	public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout&
            Configuration.SCREENLAYOUT_SIZE_MASK)==
            Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }
	
}
