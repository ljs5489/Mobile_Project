package com.mcnc.parecis.bizmob.task;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.mcnc.hsmart.configuration.model.ConfigurationModel;
import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.util.FileUtils;
import com.mcnc.hsmart.core.util.ImageUtil;
import com.mcnc.hsmart.core.view.AbstractActivity;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.def.Def;
import com.mcnc.hsmart.task.BaseTask;

public class Login20Task extends BaseTask {

	private final String TAG = this.toString();
	private final String MSG_CHANGE_USER_TYPE = "업데이트 권한이 변경되었습니다.\n프로그램이 종료후 새로운 권한이 적용됩니다.";
	private final String MSG_REMOTEWPE = "run remote wipe";
	private final String MSG_ACCESS_LIMIIT = "로그인이 제한되었습니다.";
	
	@Override
	protected Response doInBackground(Object... arg0) {
		Response response = new Response();
		final Request request = getRequest();
		
		final BaseActivity activity = request.getSrcActivity();
		String callback = "";
		String legacy_trcode = "";
		
		JSONObject root = null;
		JSONObject param = null;
		JSONObject auth_info = null;
		JSONObject legacy_message = null;
		String progressMsg = "";
		
		ConfigurationModel cm =  ConfigurationModel.getConfigurationModel();
		
		long start = System.currentTimeMillis();
		
		try {
			Logger.d(TAG, "doInBackground");

			root = (JSONObject) request.getData();
			param = root.getJSONObject("param");
			// 프로그레스 메시지 있으면 변경
			if ( param.has("progress_message")) {
				progressMsg = param.getString("progress_message");
				AbstractActivity.PROGRESS_MESSAGE = progressMsg;
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if ( activity.getDlg() != null ) {
							activity.getDlg().setMessage(AbstractActivity.PROGRESS_MESSAGE);
						}
					}
				});
			}
			
			callback = param.getString("callback");
			// 사용시점에 뽑는다.
			//message = param.getJSONObject("message");
			auth_info = param.getJSONObject("auth_info");
			legacy_trcode = param.getString("legacy_trcode");
			legacy_message = param.getJSONObject("legacy_message");
			
			String password = "";
			String user_id = "";
			
			password = auth_info.getString("password");
			user_id = auth_info.getString("user_id");
			
			JSONObject data = new JSONObject();
			JSONObject newParam = new JSONObject();
			JSONObject newMassage = new JSONObject();
			JSONObject newHeader = new JSONObject();
			JSONObject newBody = new JSONObject();
			
			data.put("param", newParam);
			newParam.put("message", newMassage);
			newMassage.put("header", newHeader);
			newMassage.put("body", newBody);
			
			newHeader.put("result", true);
			newHeader.put("error_code", "");
			newHeader.put("error_text", "");
			newHeader.put("info_text", "");
			newHeader.put("message_version", "0.9");
			newHeader.put("login_session_id", "");
			newHeader.put("trcode", "LOGIN");
			
			newBody.put("password", password);
			newBody.put("legacy_trcode", legacy_trcode);
			newBody.put("legacy_message", legacy_message);
			newBody.put("user_id", user_id);
			
			newBody.put("os_type", Def.OS_TYPE_NAME);
			
			boolean emulFlag = false;
			if(android.os.Build.MODEL.contains("sdk")) {
				emulFlag = true;
			}
			
			TelephonyManager manager = (TelephonyManager) request.getSrcActivity().getSystemService(Context.TELEPHONY_SERVICE);
			boolean manual_phone_number = false;
			
			String mobilenum = "";
			String tempNumber = "";
			if ( manager != null ) {
				tempNumber = manager.getLine1Number();
				if ( tempNumber == null ) {
					mobilenum = cm.getStringSharedPreferences("MANUAL_NUMBER");
					manual_phone_number = true;
				} else if (tempNumber.equals("")) {
					manual_phone_number = true;
					mobilenum = cm.getStringSharedPreferences("MANUAL_NUMBER");
				} else {
					if ( emulFlag == true ) {
						manual_phone_number = true;
						mobilenum = cm.getStringSharedPreferences("MANUAL_NUMBER");
					} else {
						manual_phone_number = false;
						mobilenum = tempNumber;
					}
				}
			} else {
				mobilenum = cm.getStringSharedPreferences("MANUAL_NUMBER");
				if ( mobilenum.equals(ConfigurationModel.PREFERENCE_NOTFOUND) ) {
					mobilenum = "";
				}
				manual_phone_number = true;
			}
			String device_id = "";
			if ( manager != null ) {
				device_id =  manager.getDeviceId();
				if ( device_id == null) {
					device_id = Secure.getString( activity.getContentResolver(), Secure.ANDROID_ID);
				}
			} else {
				device_id = Secure.getString( activity.getContentResolver(), Secure.ANDROID_ID);			
			}
			newBody.put("manual_phone_number", manual_phone_number);
			newBody.put("device_id", device_id);
			
			// emulator flag가 true 일경우 device체크를 하지 않음			
			if ( auth_info.has("emulator_flag")){
				newBody.put("emulator_flag", auth_info.getBoolean("emulator_flag"));
			} else {
				newBody.put("emulator_flag", emulFlag );
			}
			newBody.put("app_key", Def.APPLICTION_NAME );
			newBody.put("phone_number", mobilenum);
			
			request.setData(data);
			request.setTrCode("LOGIN");

			response.setCallback(callback);
			
			JSONObject result = sendReq(request);
			JSONObject header = null;
			JSONObject body = null;
			if ( result.has("header")){
				header = result.getJSONObject("header");
			}
			
			boolean remote = false;
			boolean access_limit = false;
			
			if ( header.getBoolean("result")){
				if ( result.has("body")){
					body = result.getJSONObject("body");
				}
				// License 등록 관련 Login20Task 가 SUCCESS 일경우 별도의 Task로 처리 로그인 속도이슈
				// License 등록에 필요한 값을 설정 
				String device_type = cm.getStringSharedPreferences("device_type");
				Def.LICENSE_NO_VALUE =  body.getString("license_no");
				Def.USER_TYPE_VALUE = body.getString("user_type");
				//ActivationTask에서 저장해 놓았던 값을 읽어놓음
				Def.DEVICE_TYPE_VALUE = device_type;
				Def.USER_ID_VALUE = user_id;
				Def.DEVICE_ID_VALUE = device_id;
				Def.COMPANY_ID_VALUE = body.getString("company_id");		

		        //"attachment_download": true,
				//"access_limit": false,
				//"legacy_message": "",
		        //"legacy_trcode": ""

				// App Tester 유무
				boolean app_tester = body.getBoolean("app_tester");
				boolean app_tester_before = cm.getBooleanSharedPreferences( ConfigurationModel.APP_TESTER);
				// Update 권한이 변경될경우 프로그램을 종료하여 새로이 받도록한다.
				if ( app_tester  != app_tester_before ) {
					String CONTENT_ROOT = ImageUtil.ROOT_PATH + ImageUtil.CONTENT_ROOT + "/";
					
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText( activity, "데이터 초기화중입니다.", Toast.LENGTH_LONG).show();
						}
					});
					
					File f = new File (CONTENT_ROOT);
					if (f.exists()){
						FileUtils.delete(f);
					}
					
					
					cm.setBooleanSharedPreferences( ConfigurationModel.APP_TESTER, app_tester);
					cm.setVersionContentMajor(0);
					cm.setVersionContentMinor(0);
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText( activity, MSG_CHANGE_USER_TYPE, Toast.LENGTH_LONG).show();
						}
					});
					activity.finish();
					return null;
				}
				// remote wipe
				remote = body.getBoolean("remote_wipe");
				if ( remote ){
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText( activity, MSG_REMOTEWPE, Toast.LENGTH_LONG).show();
						}
					});
					activity.finish();
					return null;
				}
				// assess limmit
				access_limit = body.getBoolean("access_limit");
				if ( access_limit ){
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Toast.makeText( activity, MSG_ACCESS_LIMIIT, Toast.LENGTH_LONG).show();
						}
					});
					activity.finish();
					return null;
				}
				
				JSONObject reust_legacy_message = body.getJSONObject("legacy_message");
				response.setData(reust_legacy_message);
			} else {
				Logger.d(TAG, "bizMob result false : " +  result);
				response.setError(false);
				response.setData(result);
			}
		} catch (Exception e) {	
			e.printStackTrace();
			Logger.d(TAG, "Send Error : " + e.getMessage());
			JSONObject root1 = new JSONObject();
			JSONObject header = new JSONObject();
			try {
				root1.put("header",header);
				header.put("result", false);
				if ( e instanceof HttpResponseException ) {			// HTTTP
					header.put("error_code", "HTTP"+ ((HttpResponseException)e).getStatusCode() );
					header.put("error_text", e.getMessage());
				} else if (e instanceof HttpHostConnectException  ){	// Connect
					header.put("error_code", "NE0001");
					header.put("error_text", e.getMessage());
				} else if (e instanceof ConnectTimeoutException  ){	// Connect
					header.put("error_code", "NE0001");
					header.put("error_text", e.getMessage());
				} else if (e instanceof  SocketTimeoutException ){	// Read 
					header.put("error_code", "NE0002");
					header.put("error_text", e.getMessage());
				}  else if (e instanceof  IOException ){			// 기타 예상치못한 네트워크 에러 
					header.put("error_code", "NE0003");
					header.put("error_text", e.getMessage());
				} else if (e instanceof NullPointerException ){
					header.put("error_code", "CE0001");				// 기타 컨테이너 에러
					header.put("error_text", "NullPointerException");
				} else {
					header.put("error_code", "CE0001");				// 기타 컨테이너 에러
					header.put("error_text", e.getMessage());
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			response.setError(false);
			response.setData(root1);
		}  finally {
			// 프로그레스 메시지를 변경했으면 되돌림
			if ( progressMsg.length() > 0 ) {
				AbstractActivity.PROGRESS_MESSAGE = AbstractActivity.DEFAULT_PROGRESS_MESSAGE;
			}
			// 전문이 너무 빨리 끝날 경우 프로그레스가 닫히지 않을 수가 있음. 
			long end = System.currentTimeMillis();
			long processTime = end - start;
			if ( processTime < 200 && processTime > 0) {
				try {
					Thread.sleep( 200 - processTime );
				} catch (InterruptedException e) {
				}
			}
		}
		response.setRequest(request);
		setResponse(response);
		return response;		

	}
}