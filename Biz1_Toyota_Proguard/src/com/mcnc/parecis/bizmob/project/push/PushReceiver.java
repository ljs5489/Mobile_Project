package com.mcnc.parecis.bizmob.project.push;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.mcnc.hsmart.controller.Smart2ProcessController;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.util.RUtil;
import com.mcnc.parecis.bizmob.view.DummyActivity;

public class PushReceiver extends BroadcastReceiver 
{
	private Context context = null;
	private String pushKey;
	private PowerManager pm = null;
	private PowerManager.WakeLock wl = null;
	
	private final int ERROR_C2DM = 0;
	private final int ERROR_PUSHSERVER = 1;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			String message = (String) msg.obj;
			if ( PushManager.getInstance().getTerminate()) {
				final Activity ctx = (Activity) PushManager.getInstance().getContext();
				if ( ctx == null ) {
					Toast.makeText( context , message , Toast.LENGTH_LONG).show();
				} else {
					new AlertDialog.Builder(ctx)
					.setCancelable(false)
					.setMessage(message)
					.setPositiveButton( "확인",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog1, int which) {
									dialog1.dismiss();
									Smart2ProcessController controller = (Smart2ProcessController) ctx.getApplication();
									Class activity = controller.getActivityClass(Smart2ProcessController.START_ACTIVITY);
									final Intent intent = new Intent( ctx, activity);
									intent.putExtra("Exit", true);
									intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
									ctx.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											ctx.startActivity(intent);
											ctx.overridePendingTransition( 
													RUtil.getR( ctx, "anim", "zoom_enter"), 
													RUtil.getR( ctx, "anim", "zoom_exit"));
										}
									});
								}
							}).create().show();
				}
				
			} else {
				Toast.makeText( context , message , Toast.LENGTH_LONG).show();
			}
		}
	};
	@Override
	public void onReceive(Context context, Intent intent) {

		//recieve regist key
		if (intent.getAction().equals("com.google.android.c2dm.intent.REGISTRATION"))
		{
			this.context = context;
			Log.i("C2DMReceiver", "com.google.android.c2dm.intent.REGISTRATION");
			handleRegistration(context, intent);
		}
		else if(intent.getAction().equals("com.google.android.c2dm.intent.RECEIVE"))
		{
			this.context = context;
			Log.i("C2DMReceiver", "com.google.android.c2dm.intent.RECEIVE");
			handleMessage(context, intent);
		}
	}

	private void handleRegistration( final Context context, final Intent intent) {
		// TODO Auto-generated method stub
		String 	registration_id = null;

		if (intent.getStringExtra("error") != null) {
			Log.i("intent", intent.toString());
			Log.i("errLog", intent.getStringExtra("error"));
			Log.i("handleRegistration", "@@@ Registration failed @@@");
			String error = intent.getStringExtra("error");
			if(error.equals("SERVICE_NOT_AVAILABLE")){
				error = "서비스가 불가능한 상태입니다.\n네트워크 연결을 확인해 주세요.";
            }else if(error.equals("ACCOUNT_MISSING")){
            	error = "단말기에 Google 계정을 등록해주세요.";
            }else if(error.equals("AUTHENTICATION_FAILED")){
            	error = "Google 계정 인증 실패입니다.";
            }else if(error.equals("TOO_MANY_REGISTRATIONS")){
            	error = "단말기에 Push 서비스를 이용하는 어플리케이션이 너무 많아 더이상 등록이 불가능 합니다.";
            }else if(error.equals("INVALID_SENDER")){
            	error = "Push Google 계정을 확인해주세요.";
            }else if(error.equals("PHONE_REGISTRATION_ERROR")){
            	error = "Push 서비스를  지원하지 않는 단말기입니다.";
            }
			String message = "C2DM 단말기 등록 오류\n" + error;
			//Toast.makeText(context, message , Toast.LENGTH_LONG).show();
			Message msg = new Message();
			msg.what = ERROR_C2DM;
			msg.obj = message;
			handler.sendMessage(msg);
			
		} else if (intent.getStringExtra("unregistered") != null) {
			Log.i("handleRegistration", "@@@ unregistration done @@@");
			PushManager.getInstance().pushNoti(context, PushManager.getInstance().getNotiMessage(PushManager.MESSAGE_STOP));
			SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putString("registration_id", "");
			editor.commit();
			Logger.d( "PushReceiver", preferences.getString("registration_id", ""));
		} else if ( intent.getStringExtra("registration_id") != null) {
			registration_id = intent.getStringExtra("registration_id");
			Log.i("handleRegistration", "@@@ registration_id: " + registration_id);
			///////////////////////////////////////////////////////////
			pushKey = registration_id;
			Thread t = new Thread (new Runnable() {
				@Override
				public void run() {
					try {
						SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
						Editor editor = preferences.edit();
						editor.putString("registration_id", pushKey);
						editor.commit();
						PushManager.getInstance().registDevice ( context, pushKey);
					} catch ( final Exception e) {
						e.printStackTrace();
						Message msg = new Message();
						msg.what = ERROR_PUSHSERVER;
						msg.obj = e.getMessage();
						handler.sendMessage(msg);
					}
				}
			});
			t.start();
		}
	}

	private void handleMessage(final Context context, Intent intent) {
		//push를 사용 안할 경우 메시지가 오더라도 무시 함.
		SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
		boolean isPushEnable = preferences.getBoolean("isPushEnable", true);
		if ( ! isPushEnable ){
			Logger.d("handleMessage", "isPushEnable : " + isPushEnable);
			return;
		}
		
		// 화면을 켬
		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE); 
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "bbbb"); 
		wl.acquire();
		
		Log.i("handleMessage", "------------ COME MESSAGE: " + pushKey);
		// TODO Auto-generated method stub
		String count = intent.getExtras().getString("badge");
		String message = intent.getExtras().getString("message");
		//String collaspe_key = intent.getExtras().getString("collaspe_key");
		
		Log.i("handleMessage", "------------ COME MESSAGE: "  + message + ", " + count);

		Notification notification = new Notification( RUtil.getDrawableR(context, "app_icon") ,
				message, System.currentTimeMillis());

		// notiId
		int notiID = (int) System.currentTimeMillis();
		// TODO 현재는 시작 화면으로 가게 했지만 추후 수정해야함.
		String title = PushManager.getInstance().getNotiMessage(PushManager.MESSAGE_TITLE);
		Intent in = new Intent(Intent.ACTION_MAIN);
		in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		in.addCategory(Intent.CATEGORY_LAUNCHER);
		in.setComponent(new ComponentName( context, DummyActivity.class));
		in.putExtra("push", message );
		in.putExtra("push_noti_id", notiID );
		PendingIntent pi = PendingIntent.getActivity(context, notiID, in, Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_ONE_SHOT);
		notification.setLatestEventInfo(context, title, message, pi);
		notification.flags |= Notification.FLAG_AUTO_CANCEL; //선택시 자동삭제

		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		AudioManager audioManager =  (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		// 전체 제거 고려
		if( audioManager.getRingerMode()== AudioManager.RINGER_MODE_NORMAL) {
			notification.defaults |= android.app.Notification.DEFAULT_SOUND;
		} else {
			// 진동 효과 구성 
			long[] vibrate = {1000, 1000, 1000, 1000, 1000}; 
			notification.vibrate = vibrate; 
		}
		//nm.notify(PushManager.PUSH_MESSAGE, notification);
		nm.notify(notiID, notification);
		run.run();
	}

	private Runnable run =  new Runnable() {
		@Override
		public void run() {
			if (wl != null ) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				wl.release();
			}
		}
	};
}
