package com.mcnc.parecis.bizmob.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.task.BaseTask;
import com.mcnc.hsmart.util.device.contact.SearchHelperForDeviceContact;

public class AddContactGroupTask extends BaseTask{
	private final String TAG = this.toString();

	private static final String RESULT = "result";
	private static final String PARAM = "param";
	private static final String GROUP_ID = "group_id";
	private static final String GROUP_NAME = "group_name";
	private static final String CALLBACK = "callback";
	
	@Override
	protected Response doInBackground(Object... arg0) {
		Response response = new Response();
		final Request request = getRequest();
		JSONObject jsonData = (JSONObject) request.getData();
		String groupID = "";
		String groupName = "";
		String callback = "";
		Uri resultData = null;
		boolean result = false;
		JSONObject responseData = new JSONObject();
		
		try {
			responseData.put(RESULT, false);
			JSONObject param = jsonData.getJSONObject(PARAM);

			if(param.has(GROUP_ID)) {
				groupID = param.getString(GROUP_ID);
			}
			if(param.has(GROUP_NAME)) {
				groupName = param.getString(GROUP_NAME);
			}
			callback = jsonData.getJSONObject(PARAM).getString(CALLBACK);
			resultData = SearchHelperForDeviceContact.addGroupContact(request.getSrcActivity(), groupID, groupName);
			result =  (resultData != null ? true : false);
			responseData.put(RESULT, result);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				responseData.put(RESULT, result);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		response.setError(false);
		response.setData(responseData);
		response.setRequest(request);
		response.setCallback(callback);
		setResponse(response);

		return response;
	}
}
