package com.mcnc.parecis.bizmob.task;

import org.json.JSONException;
import org.json.JSONObject;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.task.BaseTask;
import com.mcnc.hsmart.util.device.contact.SearchHelperForDeviceContact;

public class DelContactTask extends BaseTask{
	private final String TAG = this.toString();

	private static final String RESULT = "result";
	private static final String PARAM = "param";
	private static final String CONTACT_UID = "contact_uid";
	private static final String CONTACT_NAME = "contact_name";
	private static final String CALLBACK = "callback";
	
	@Override
	protected Response doInBackground(Object... arg0) {
		Response response = new Response();
		final Request request = getRequest();
		JSONObject jsonData = (JSONObject) request.getData();
		String contactUid = "";
		String contactName = "";
		String callback = "";
		int resultData = -1;
		boolean result = false;
		JSONObject responseData = new JSONObject();
		
		try {
			responseData.put(RESULT, false);
			JSONObject param = jsonData.getJSONObject(PARAM);
			if(param.has(CONTACT_UID)) {
				contactUid = param.getString(CONTACT_UID);
			}
			if(param.has(CONTACT_NAME)) {
				contactName = param.getString(CONTACT_NAME);
			}
			
			callback = jsonData.getJSONObject(PARAM).getString(CALLBACK);
			resultData = SearchHelperForDeviceContact.deletePerson(request.getSrcActivity(), contactUid, contactName);
			result =  resultData != -1 ? true : false;
			responseData.put(RESULT, result);
		}catch (Exception e) {
			e.printStackTrace();
			try {
				responseData.put(RESULT, result);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		} /*catch (JSONException e) {
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
			response.setError(!result);
			response.setData(responseData);
			response.setRequest(request);
			response.setCallback(callback);
			setResponse(response);
		}*/
		
		response.setError(false);
		response.setData(responseData);
		response.setRequest(request);
		response.setCallback(callback);
		setResponse(response);
		return response;
	}
}
