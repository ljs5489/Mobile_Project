package com.mcnc.parecis.bizmob.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderResult;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.def.Def;
import com.mcnc.hsmart.task.BaseTask;
import com.mcnc.hsmart.util.device.contact.SearchHelperForDeviceContact;

public class AddContactTask extends BaseTask{
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
		JSONObject jsonData = (JSONObject) request.getData();
		String contactName = "";
		String phoneNumber = "";
		String telNumber = "";
		String groupID = "";
		String groupName = "";
		String callback = "";
		ContentProviderResult[] resultData = null;
		boolean result = false;
		JSONObject responseData = new JSONObject();
		
		try {
			responseData.put(RESULT, false);
			JSONObject param = jsonData.getJSONObject(PARAM);
			if(param.has(CONTACT_NAME)) {
				contactName = param.getString(CONTACT_NAME);
			}
			if(param.has(PHONE_NUMBER)) {
				phoneNumber = param.getString(PHONE_NUMBER);
			}
			if(param.has(TEL_NUMBER)) {
				telNumber = param.getString(TEL_NUMBER);
			}
			if(param.has(GROUP_ID)) {
				groupID = param.getString(GROUP_ID);
			}
			if(param.has(GROUP_NAME)) {
				groupName = param.getString(GROUP_NAME);
			}
			callback = jsonData.getJSONObject(PARAM).getString(CALLBACK);
			resultData = SearchHelperForDeviceContact.addPersonContact(request.getSrcActivity(), Def.ACCOUNT_TYPE, Def.ACCOUNT_NAME, contactName, phoneNumber, telNumber, groupID);
			result =  resultData.length > 0 ? true : false;
			responseData.put(RESULT, result);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				responseData.put(RESULT, result);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}/* catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		} catch (OperationApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result = false;
		} finally {
			// set response
		}*/
		response.setError(false);
		response.setData(responseData);
		response.setRequest(request);
		response.setCallback(callback);
		setResponse(response);

		return response;
	}
}
