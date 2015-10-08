package com.mcnc.parecis.bizmob.task;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.speech.RecognizerIntent;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.task.BaseTask;

public class SpeechRecognitionTask extends BaseTask {

	private static final String SRC_ACTIVITY = "srcActivity::";
	private static final String PARAM = "param";
	private static final String MESSAGE = "message";
	private static final String CALLBACK = "callback";
	public static final int SPEECH_RECOGNITION_REQUEST_CODE=1234; 
	private final String TAG = this.toString();

	@Override
	protected Response doInBackground(Object... params) {
		final Request request = getRequest();
		Response response = new Response();
		JSONObject jsonData = (JSONObject) request.getData();
		String message = "";
		String callback = "";
		try {
			message = jsonData.getJSONObject(PARAM).getString(MESSAGE);
			callback = jsonData.getJSONObject(PARAM).getString(CALLBACK);
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); 
	        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); 
	        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, message); 
	        request.getSrcActivity().startActivityForResult(intent, SPEECH_RECOGNITION_REQUEST_CODE);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.d(TAG, "CALL TELEPHONE");
		Logger.d(TAG, SRC_ACTIVITY + request.getSrcActivity());
		response.setRequest(request);
		response.setData(callback);
		setResponse(response);
		return response;
	}
}
