package com.mcnc.parecis.bizmob.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.task.BaseTask;
import com.mcnc.parecis.bizmob.nativeView.browser.InternalBrowserActivity;

public class ShowInteranlBrowserTask extends BaseTask {

	private static final String TARGET_PAGE = "targetPage";
	private static final String DATA = "data";
	private static final String PARAM = "param";
	private static final String MESSAGE = "message";
	private static final String REQUEST_LISTENER = "requestListener::";
	public static final int REQUESTCODE_INTERNAL_BROWSER = 11223;
	private final String TAG = this.toString();

	@Override
	protected Response doInBackground(Object... arg0) {
		Response response = new Response();
		Request request = getRequest();

		Intent intent = null;
		intent = new Intent(request.getSrcActivity(), InternalBrowserActivity.class);
		JSONObject jsonData = (JSONObject) request.getData();
		try {
			JSONObject param = jsonData.getJSONObject(PARAM);
			if (param.has(MESSAGE)) {
				intent.putExtra(DATA,
						jsonData.getJSONObject(PARAM).getString(MESSAGE));
			} else {
				intent.putExtra(DATA, "");
			}
			if(param.has("orientation")) {
				intent.putExtra("orientation", param.getString("orientation"));
			}
			if(param.has("callback")) {
				intent.putExtra("callback", param.getString("callback"));
			}
			if(param.has("target_page")) {
				intent.putExtra(TARGET_PAGE, param.getString("target_page"));
			}
			if(param.has("title")) {
				intent.putExtra("title", param.getString("title"));
			}
			if(param.has("sms_button")) {
				intent.putExtra("sms_button", param.getBoolean("sms_button"));
			} else {
				intent.putExtra("sms_button", false );
			}
			
			request.getSrcActivity().startActivityForResult(intent, REQUESTCODE_INTERNAL_BROWSER);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Logger.d(TAG, REQUEST_LISTENER + request.getListener());
		response.setError(false);
		response.setRequest(request);
		setResponse(response);
		return response;
	}
}
