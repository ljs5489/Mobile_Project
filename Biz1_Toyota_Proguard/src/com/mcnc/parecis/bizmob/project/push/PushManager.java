package com.mcnc.parecis.bizmob.project.push;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.HttpResponseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.os.PowerManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.mcnc.hsmart.configuration.model.ConfigurationModel;
import com.mcnc.hsmart.core.log.Logger;

/**
 * ServiceSwitch
 * 
 * @version 1.0 16 8 2010
 * @author nadir93@gmail.com
 */
public class PushManager {
	// PushManager Instance
	private static PushManager instance = new PushManager();
	
	public static final int PUSH_MESSAGE		 = 000001;
	public static final int PUSH_NOTICE			 = 000002;
	public static final int PUSH_REVIEW	 		 = 000003;
	public static final int PUSH_SUBKEYWORD	 	 = 000004;
	public static final int PUSH_URGENT_NOTICE	 = 000005;
	
	public static final int MESSAGE_TITLE = 0;
	public static final int MESSAGE_START = 1;
	public static final int MESSAGE_STOP = 2;
	
	public static final String [] messageKeys = {"MESSAGE_TITLE", "MESSAGE_START", "MESSAGE_STOP"};
	
	public String [] message = {"메세지", "PUSH 서비스를 시작합니다.", "PUSH 서비스를 종료합니다."};
	
	private static String id;
	private static String pass;
	private static String registUrl;
	private static String serverEmail;
	private static String userEmail;

	public static boolean isActivityEnable = false;

	private boolean isPushEnable;
	private PowerManager pm = null;
	private PowerManager.WakeLock wl = null;
	private Context context = null;
	private boolean bTerminate = false;

	public static PushManager getInstance() {
		if(instance == null) {
			instance = new PushManager();
		}
		return instance;
	}
	
	public void pushStart(final Context context, String id, String pass, String url, String serverEmail, String userEmail){
		if ( ! url.startsWith("http://") && ! url.startsWith("https://")) {
			ConfigurationModel cm = ConfigurationModel.getConfigurationModel();
			boolean bSsl = cm.getPushSsl().equals("true");
			String ip = cm.getPushIp();
			String port = cm.getPushPort();
			String url2 = bSsl ? "https://" : "http://";
			url2 = url2 + ip + ":" + port;
			if ( url.startsWith("/")) {
				url2 = url2 + url;
			} else {
				url2 = url2 +"/"+ url;
			}
			url = url2;
		}
		
		PushManager.id = id;
		PushManager.pass = pass;
		PushManager.registUrl = url;
		PushManager.serverEmail = serverEmail;
		PushManager.userEmail = userEmail;
		
		SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
		isPushEnable = preferences.getBoolean("isPushEnable", true);
		
		final String registration_id = preferences.getString( "registration_id", "");
		if ( registration_id.equals("") && isPushEnable ) {
			this.context = context;
			synchronized (PushManager.class) {
				//Android C2DM에 Push메시지를 받겠다는 메시지를 보내는 Intent             
				//정상적으로 등록이되면 Android C2DM Server쪽에서 인증키를 보내준다.             
				//이 인증키는 해당 어플리케이션과 해당 기기를 대표하는 인증키로 서버에서 메시지를 보낼 때 사용되며             
				//서버에 등록을 할 때마다 인증키는 달라진다.             
				Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
				registrationIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));             
				registrationIntent.putExtra("sender", serverEmail);   
				context.startService(registrationIntent);
			}
		} else if ( !registration_id.equals("") && isPushEnable ){
			this.context = context;
			// C2DM 서버에 등록이 한번 되면 삭제를 해도 계속해서 푸시가 내려옴. 
			Thread t = new Thread (new Runnable() {
				@Override
				public void run() {
					try {
						registDevice ( context, registration_id);
					} catch (final Exception e) {
						e.printStackTrace();
						((Activity)context).runOnUiThread(new Runnable() {
							@Override
							public void run() {
								String message = "Push Server 등록 오류\n" + e.getMessage();
								Toast.makeText(context, message, Toast.LENGTH_LONG).show();
							}
						});
					}
				}
			});
			t.start();
		} else {
			Logger.d("PushManager", "isPushEnable : " + isPushEnable );
		}
	}

	public void pushStop(Context context){
		SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
		isPushEnable = preferences.getBoolean("isPushEnable", true);
		final String registration_id = preferences.getString( "registration_id", "");
		if( ! registration_id.equals("") && isPushEnable ){
			this.context = context;
			synchronized (PushManager.class) {
				//Android C2DM에 Push메시지를 그만 받겠다는 메시지를 보내는 Intent             
				Intent unregIntent = new Intent("com.google.android.c2dm.intent.UNREGISTER");             
				unregIntent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));             
				context.startService(unregIntent); 
			}
		}
	}
	
	public boolean isRunning(Context context ){
		SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
		boolean bEnable = preferences.getBoolean("isPushEnable", true);
		boolean bRegistration = ! preferences.getString( "registration_id", "").equals(""); 
		Logger.d("PushManager", "isRunning : " + (bEnable && bRegistration ) );
		return  ( bEnable && bRegistration) ;
	}
	
	public void pushNoti(Context context, String message){
		// 화면을 켬
		pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE); 
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, "bbbb"); 
		wl.acquire();
		
		int notiID = (int) System.currentTimeMillis();
		Notification notification = new Notification(R.drawable.btn_plus, message, System.currentTimeMillis());
		String title = PushManager.getInstance().getNotiMessage(PushManager.MESSAGE_TITLE);
		PendingIntent pi = PendingIntent.getActivity(context, notiID, new Intent() , Intent.FLAG_ACTIVITY_NEW_TASK | PendingIntent.FLAG_ONE_SHOT);
		notification.setLatestEventInfo(context, title, message, pi);
		notification.flags |= Notification.FLAG_AUTO_CANCEL; //선택시 자동삭제

		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		AudioManager audioManager =  (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		// 전체 제거 고려
		if( audioManager.getRingerMode()== AudioManager.RINGER_MODE_NORMAL) {
			notification.defaults |= android.app.Notification.DEFAULT_SOUND;
		} else {
			// 진동 효과 구성 
			long[] vibrate = {1000, 1000, 1000, 1000, 1000}; 
			notification.vibrate = vibrate; 
		}
		nm.notify( PUSH_MESSAGE, notification);
		run.run();
	}
	
	private Runnable run =  new Runnable() {
		@Override
		public void run() {
			if (wl != null ) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				wl.release();
			}
		}
	};
	

	public void registDevice(Context context, String pushKey) throws Exception{
		byte[] postData = null;
		int responseCode;
		
		
		String CRYPTO_SEED_PASSWORD = "1234!@#$";
		try {
			SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
			String registration_info = preferences.getString( "registration_info", "");
			String before_info = "";
			if ( registration_info.length() > 0 ){
				before_info = SimpleCrypto.decrypt(CRYPTO_SEED_PASSWORD, registration_info );
			}
			String info = registUrl + id; 
			// 기존 등록 했던 정와 같으면 등록 안함
			if ( before_info.equals(info)) {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		TelephonyManager telMgr = 
			(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

		JSONObject RData = new JSONObject();
		JSONObject KeyValueData = new JSONObject();

		String device_id = null;
		if ( telMgr != null  ) {
			device_id = telMgr.getDeviceId();
			if ( device_id == null) {
				device_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			}
		} else {
			device_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		}
		String packageName = context.getPackageName();

		RDataDTO dto = new RDataDTO();
		dto.addKeyValueData(new KeyValueDataDTO("email", userEmail));
		dto.addKeyValueData(new KeyValueDataDTO("password", pass));
		dto.addKeyValueData(new KeyValueDataDTO("device_model", android.os.Build.MODEL));
		dto.addKeyValueData(new KeyValueDataDTO("device_os", "Android"));
		dto.addKeyValueData(new KeyValueDataDTO("os_version", android.os.Build.VERSION.RELEASE));
		dto.addKeyValueData(new KeyValueDataDTO("device_id", device_id ));
		dto.addKeyValueData(new KeyValueDataDTO("app_name", packageName ));
		dto.addKeyValueData(new KeyValueDataDTO("app_version", "1.0"));
		dto.addKeyValueData(new KeyValueDataDTO("push_key", pushKey));

		KeyValueData = toRDataJSONObject(dto);

		RData.put("RData", KeyValueData);
		RData.put("UserID", id);
		
		Logger.d("json Data ---------------- ", RData.toString());
		
		postData = RData.toString().getBytes();

		// Push Server�� ���
		URL url;
		try {
			_FakeX509TrustManager manger = new _FakeX509TrustManager();
			manger.allowAllSSL();	
			
			url = new URL(registUrl);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
			
			OutputStream out = conn.getOutputStream();
			out.write(postData);
			out.close();
			
			responseCode = conn.getResponseCode();
			System.out.println("ResponseCode : " + responseCode);		
			
			if(responseCode == 200){
				pushNoti(context, this.getNotiMessage(MESSAGE_START));
				Logger.d("PushManager", "registration_id : " + pushKey );
				try {
					String info = registUrl + id; 
					String encript_info =  SimpleCrypto.encrypt( CRYPTO_SEED_PASSWORD, info );
					SharedPreferences preferences = context.getSharedPreferences("pushPref", Activity.MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putString("registration_info",  encript_info);
					editor.commit();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else{
				throw new HttpResponseException(responseCode, registUrl + "\n HTTP" + responseCode + " Error");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			throw e;
		}
	}

	public class KeyValueDataDTO {

		private String key;
		private String value;
		
		public KeyValueDataDTO() {
		}
		
		public KeyValueDataDTO( String key, String value ) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
	
	public class RDataDTO {

		private List< KeyValueDataDTO > keyValueData;
		
		public RDataDTO() {
			keyValueData	= new ArrayList<KeyValueDataDTO>();
		}
		
		public List<KeyValueDataDTO> getKeyValueData() {
			return keyValueData;
		}

		public void setKeyValueData(List<KeyValueDataDTO> keyValueData) {
			this.keyValueData = keyValueData;
		}
		public void addKeyValueData( KeyValueDataDTO data ) {
			keyValueData.add( data );
		}
		
		public void removeKeyValueData( KeyValueDataDTO data ) {
			keyValueData.remove( data );
		}
		
		public void clearAllKeyValueData() {
			keyValueData.clear();
		}
	}
	
	private JSONObject toRDataJSONObject( RDataDTO dto ) throws JSONException {
		JSONObject	rDataObj	= new JSONObject();
		if ( dto != null && dto.getKeyValueData() != null ) {
			JSONArray	arrayObj	= new JSONArray();
			for ( KeyValueDataDTO keyValueDataDTO : dto.getKeyValueData() ) {
				JSONObject	keyValueDataObj	= new JSONObject();
				keyValueDataObj.put( "Key", keyValueDataDTO.getKey() );
				keyValueDataObj.put( "Value", keyValueDataDTO.getValue() );
				arrayObj.put( keyValueDataObj );
			}
			rDataObj.put( "KeyValueData", arrayObj );
		} else {
			rDataObj.put( "KeyValueData", null );
		}
		return rDataObj;
	}
	
	public void setNotiMessage(int messageType, String message) {
		this.message[messageType] = message;
	}

	public String getNotiMessage(int messageType) {
		return this.message[messageType];
	}
	
	public void setNotiMessages(String [] messages) {
		this.message = messages;
	}
	
	public String[] getNotiMessages() {
		return this.message;
	}
	
	public void setContext ( Context context ) {
		this.context = context;
	}
	public Context getContext ( ) {
		return this.context;
	}
	
	public void setTerminate (boolean b) {
		bTerminate = b;
	}
	
	public boolean getTerminate () {
		return bTerminate;
	}
	
	// 1. Create a Fake Trust Manager 
	public class _FakeX509TrustManager implements X509TrustManager { 
		private TrustManager[] trustManagers; 
		private final X509Certificate[] _AcceptedIssuers = new X509Certificate[] {}; 
		@Override 
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		} 

		@Override 
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { 
		} 

		public boolean isClientTrusted(X509Certificate[] chain) { 
			return true; 
		} 

		public boolean isServerTrusted(X509Certificate[] chain) { 
			return true; 
		} 

		@Override 
		public X509Certificate[] getAcceptedIssuers() { 
			return _AcceptedIssuers; 
		} 

		public void allowAllSSL() { 
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() { 
		                    @Override 
		                    public boolean verify(String hostname, SSLSession session) { 
		                            return true; 
		                    } 
						}); 
	        SSLContext context = null; 
	        if (trustManagers == null) { 
	                trustManagers = new TrustManager[] { new _FakeX509TrustManager() }; 
	        } 
	
	        try { 
	            context = SSLContext.getInstance("TLS"); 
	            context.init(null, trustManagers, new SecureRandom()); 
	        } catch (NoSuchAlgorithmException e) { 
	            e.printStackTrace(); 
	        } catch (KeyManagementException e) { 
	            e.printStackTrace(); 
	        } 
			HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory()); 
		} 
	}	
}
