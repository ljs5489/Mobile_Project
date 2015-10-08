package com.mcnc.parecis.bizmob.view;

import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.util.ActivityStack;

public class DummyActivity extends Activity {

	private String TAG = this.toString();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Logger.d(TAG, "DummyActivity : onCreate" );
		String callback = null;
		String data = null;
		String target_page = null;
		// Action으로 시작이 될때.
		Intent intent = getIntent();
		if(Intent.ACTION_VIEW.equals(intent.getAction())) { 
			try {
		         Uri uri = intent.getData(); 
		         String paramStr = uri.getQueryParameter("param");
		         JSONObject param = new JSONObject(paramStr);
		         if (param.has("callback")) {
		        	 callback = param.getString("callback");
		         }
		         if (param.has("data")) {
		        	 data = param.getString("data");
		         }
		         if (param.has("target_page")) {
		        	 target_page = param.getString("target_page");
		         }
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if ( target_page != null && target_page.length() >  0 ) {
			startActivity (callback, data, target_page );
		} else {
			startActivity (callback, data);
		}
		finish();
	}
	
	private void startActivity ( final String callback, final String data ) {
		ArrayList<Activity> activityList = ActivityStack.getActivityList();
		if ( activityList.size()  > 0) {
			final BaseActivity activity = (BaseActivity) activityList.get((activityList.size() -1));
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if ( activity.webView != null ) {
						Logger.d(TAG, "javascript:" + callback  + "(" + data + ")");
						if ( callback!= null && callback.length() > 0 ) {
							if (data != null ) {
								activity.webView.loadUrl("javascript:" + callback  + "(" + data + ")");
							} else {
								activity.webView.loadUrl("javascript:" + callback  + "()");
							}
						}
					}
				}
			});
		} else {
			Intent intent = new Intent(this, ImpLoginActivity.class);
			Logger.d(TAG, "callback:" + callback  + "(" + data + ")");
			if ( callback!= null && callback.length() > 0 ) {
				intent.putExtra("external_callback", callback );
			}
			if ( data != null ) {
				intent.putExtra("external_data", data );
			}
			startActivity(intent);
		}
	}
	private void startActivity ( final String callback, final String data, final String target_page ) {
		final ArrayList<Activity> activityList = ActivityStack.getActivityList();
		if ( activityList.size()  > 0) {
			// start page까지 못찾을 경우 start page에 콜백을 함.
			for ( int i = 0; i < activityList.size() -1 ; i++ ) {
				BaseActivity activity = (BaseActivity) activityList.get((activityList.size()-1));
				if ( activity.webView != null ) {
					String url = activity.webView.getUrl();
					if (url != null &&  url.contains(target_page )){
						break;
					} else {
						activityList.remove(activity);
						activity.finish();
					}
				} else {
					activityList.remove(activity);
					activity.finish();
				}
			}
			
			final BaseActivity activity = (BaseActivity) activityList.get((activityList.size() -1));
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if ( activity.webView != null ) {
						Logger.d(TAG, "javascript:" + callback  + "(" + data + ")");
						if ( callback!= null && callback.length() > 0 ) {
							if (data != null ) {
								activity.webView.loadUrl("javascript:" + callback  + "(" + data + ")");
							} else {
								activity.webView.loadUrl("javascript:" + callback  + "()");
							}
						}
					}
				}
			});
		} else {
			Intent intent = new Intent(this, ImpLoginActivity.class);
			Logger.d(TAG, "callback:" + callback  + "(" + data + ")");
			if ( callback!= null && callback.length() > 0 ) {
				intent.putExtra("external_callback", callback );
			}
			if ( data != null ) {
				intent.putExtra("external_data", data );
			}
			startActivity(intent);
		}
	}
}
