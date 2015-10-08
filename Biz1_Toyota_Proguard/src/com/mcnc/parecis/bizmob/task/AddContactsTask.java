package com.mcnc.parecis.bizmob.task;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.ContentProviderResult;
import android.util.Log;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.view.AbstractActivity;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.def.Def;
import com.mcnc.hsmart.task.BaseTask;
import com.mcnc.hsmart.util.device.contact.SearchHelperForDeviceContact;

public class AddContactsTask extends BaseTask{
	private final String TAG = this.toString();

	private static final String RESULT = "result";
	private static final String PARAM = "param";
	private static final String CONTACT_NAME = "contact_name";
	private static final String PHONE_NUMBER = "phone_number";
	private static final String TEL_NUMBER = "tel_number";
	private static final String GROUP_ID = "group_id";
	private static final String GROUP_NAME = "group_name";
	private static final String CALLBACK = "callback";
	
	@Override
	protected Response doInBackground(Object... arg0) {
		Response response = new Response();
		final Request request = getRequest();
		final BaseActivity activity = request.getSrcActivity();
		JSONObject jsonData = (JSONObject) request.getData();
		String contactName = "";
		String phoneNumber = "";
		String telNumber = "";
		String groupID = "";
		String groupName = "";
		String callback = "";
		boolean flag = false;
		ContentProviderResult[] resultData = null;
		boolean result = true;
		JSONObject responseData = new JSONObject();
		
		long start = System.currentTimeMillis();
		
		int contackCount = 0;
		
		try {
			responseData.put(RESULT, false);
			JSONObject param = jsonData.getJSONObject(PARAM);
			
			JSONArray array = param.getJSONArray("contacts");
			contackCount = array.length();
			flag = param.getBoolean("fail_flag");
			
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((AbstractActivity) activity)
							.showDialog(AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
					ProgressDialog dialog = ((AbstractActivity) activity)
							.getDlg();
					dialog.setMessage("주소록 등록중...");
					activity.dlg = dialog;
				}
			});
			
			JSONArray resArr = new JSONArray();
			responseData.put("result_array", resArr);
			
			if(flag){
				for(int i = 0; i < array.length(); i++){
					if(array.getJSONObject(i).has(CONTACT_NAME)) {
						contactName = array.getJSONObject(i).getString(CONTACT_NAME);
					}
					if(array.getJSONObject(i).has(PHONE_NUMBER)) {
						phoneNumber = array.getJSONObject(i).getString(PHONE_NUMBER);
					}
					if(array.getJSONObject(i).has(TEL_NUMBER)) {
						telNumber = array.getJSONObject(i).getString(TEL_NUMBER);
					}
					if(array.getJSONObject(i).has(GROUP_ID)) {
						groupID = array.getJSONObject(i).getString(GROUP_ID);
					}
					if(array.getJSONObject(i).has(GROUP_NAME)) {
						groupName = array.getJSONObject(i).getString(GROUP_NAME);
					}
					
					resultData = SearchHelperForDeviceContact.addPersonContact(request.getSrcActivity(), Def.ACCOUNT_TYPE, Def.ACCOUNT_NAME, contactName, phoneNumber, telNumber, groupID);

					Log.d("Contact Add ------------ ", String.valueOf(i));
					
					if(resultData.length > 0){
						resArr.put( true );
					}else{
						result =  false;
						resArr.put (false );
					}
				}
			}else{
				for(int i = 0; i < array.length(); i++){
					if(array.getJSONObject(i).has(CONTACT_NAME)) {
						contactName = array.getJSONObject(i).getString(CONTACT_NAME);
					}
					if(array.getJSONObject(i).has(PHONE_NUMBER)) {
						phoneNumber = array.getJSONObject(i).getString(PHONE_NUMBER);
					}
					if(array.getJSONObject(i).has(TEL_NUMBER)) {
						telNumber = array.getJSONObject(i).getString(TEL_NUMBER);
					}
					if(array.getJSONObject(i).has(GROUP_ID)) {
						groupID = array.getJSONObject(i).getString(GROUP_ID);
					}
					if(array.getJSONObject(i).has(GROUP_NAME)) {
						groupName = array.getJSONObject(i).getString(GROUP_NAME);
					}
					
					resultData = SearchHelperForDeviceContact.addPersonContact(request.getSrcActivity(), Def.ACCOUNT_TYPE, Def.ACCOUNT_NAME, contactName, phoneNumber, telNumber, groupID);

					Log.d("Contact Add ------------ ", String.valueOf(i));
					
					if(resultData.length > 0){
						resArr.put(true);
					}else{
						result =  false;
						resArr.put(false);
						break;
					}
				}
			}

			callback = jsonData.getJSONObject(PARAM).getString(CALLBACK);
			
			responseData.put(RESULT, result);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				responseData.put(RESULT, result);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if ( ! responseData.has("result_array")) {
					JSONArray array = new JSONArray();
					jsonData.put("result_array", array );
				}
				JSONArray array2 = jsonData.getJSONArray("result_array");
				int count = array2.length();
				if ( contackCount > count ) {
					for ( int i= 0; i < contackCount -count; i++ ) {
						array2.put(false);
					}
				}
			} catch ( JSONException e) {
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
			activity.removeDialog(AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
		}
		response.setError(false);
		response.setData(responseData);
		response.setRequest(request);
		response.setCallback(callback);
		setResponse(response);

		return response;
	}
}
