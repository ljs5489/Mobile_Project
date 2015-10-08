package com.mcnc.parecis.bizmob.task;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONException;
import org.json.JSONObject;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.view.AbstractActivity;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.task.BaseTask;

public class ReloadTask extends BaseTask {

	private static final String RESPONSE_SUCCESS = "response::success";
	private static final String RESULT_FALSE = "result::false";
	private static final String DATA = "data";
	private static final String TR_CODE = "trCode";
	private static final String RESULT = "result";
	private static final String HEADER2 = "header::";
	private static final String HEADER = "header";
	private static final String TARGET_PAGE = "targetPage";
	private static final String REQUEST_LISTENER = "requestListener::";
	private static final String RESPONSE = "response::";
	private final String TAG = this.toString();

	@Override
	protected Response doInBackground(Object... arg0) {

		Response response = new Response();
		final Request request = getRequest();
		String callback = "";
		long start = System.currentTimeMillis();
		String progressMsg = "";
		final BaseActivity activity = request.getSrcActivity();
		try {
			JSONObject param = ((JSONObject) request.getData()).getJSONObject("param");
			if (param.has("callback")) {
				callback = param.getString("callback");
				response.setCallback( callback);
			}
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
			
			JSONObject result = sendReq(request);
			JSONObject header = result.getJSONObject(HEADER);
			if ( Logger.LogLevel < Logger.DEBUG ){
				Logger.d(TAG, RESPONSE + result);
				Logger.d(TAG, HEADER2 + header);
			}
			response.setData(result);
			response.setError(false);
		} catch (Exception e) {
			e.printStackTrace();
			JSONObject root = new JSONObject();
			JSONObject header = new JSONObject();
			try {
				root.put(HEADER,header);
				header.put(RESULT, false);
				if ( e instanceof HttpResponseException ) {			// HTTTP
					header.put("error_code", "HE0"+ ((HttpResponseException)e).getStatusCode() );
					header.put("error_text", e.getMessage());
				} else if (e instanceof ConnectTimeoutException  ){	// Connect
					header.put("error_code", "NE0001");
					header.put("error_text", e.getMessage());
				} else if (e instanceof HttpHostConnectException  ){	// Connect
					header.put("error_code", "NE0001");
					header.put("error_text", e.getMessage());
				} else if (e instanceof  SocketTimeoutException ){	// Read 
					header.put("error_code", "NE0002");
					header.put("error_text", "SocketTimeoutException");
				} else if (e instanceof  IOException ){			// 기타 예상치못한 네트워크 에러 
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
			response.setData(root);
			response.setError(false);
		} finally {
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
		// set response
		Logger.d(TAG, REQUEST_LISTENER + request.getListener());
		response.setRequest(request);
		setResponse(response);
		return response;
	}
}