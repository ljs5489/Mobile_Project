package com.mcnc.parecis.bizmob.task;

import org.json.JSONException;
import org.json.JSONObject;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.task.BaseTask;
import com.mcnc.hsmart.util.device.contact.SearchHelperForDeviceContact;

public class GetContactGroupTask extends BaseTask{
	private static final String RESULT = "result";
	private static final String PARAM = "param";
	private static final String CALLBACK = "callback";
	
	@Override
	protected Response doInBackground(Object... arg0) {
		Response response = new Response();
		final Request request = getRequest();
		final BaseActivity activity = request.getSrcActivity();
		JSONObject jsonData = (JSONObject) request.getData();
		String callback = "";
		JSONObject responseData = new JSONObject();
		
		try {
			responseData.put(RESULT, false);
			JSONObject param = jsonData.getJSONObject(PARAM);
			callback = param.getString(CALLBACK); 
			response.setCallback(callback);
			JSONObject data = SearchHelperForDeviceContact.getGroupList(activity);
			response.setData(data);
		}catch (Exception e) {
			e.printStackTrace();
			JSONObject data = new JSONObject();
			try {
				data.put("result", false);
				data.put("error_text", e.getMessage());
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			response.setData(data);
		}
		response.setError(false);
		response.setRequest(request);
		setResponse(response);
		return response;
	}
}
