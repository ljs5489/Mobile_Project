package com.mcnc.parecis.bizmob.task;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import com.mcnc.hsmart.core.comm.CommManager;
import com.mcnc.hsmart.core.comm.ICommService;
import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.download.AbstractDownloadService;
import com.mcnc.hsmart.core.download.IDownload;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.def.Def;
import com.mcnc.hsmart.task.BaseTask;
import com.mcnc.hsmart.util.JsonUtil;

public class DownloadImageTask extends BaseTask {

	private static final String SRC_ACTIVITY = "srcActivity::";
	private static final String PARAM = "param";
	private static final String URI = "uri";
	private static final String DISPLAY_NAME = "display_name";
	private static final String CALLBACK = "callback";
	public static String ROOT_URL_IP = Def.ROOT_DOWNLOAD_URL_IP;
	public static String ROOT_URL_PORT = Def.ROOT_DOWNLOAD_URL_PORT;
	public static String ROOT_URL_DIR = Def.ROOT_DOWNLOAD_URL_DIR; // smart2-web/
	//private final String DOWNLOAD_ROOT = Environment
	//		.getExternalStorageDirectory() + "/download/";
	private final String REQUESTURL = "requestURL";
	private final String REQUESTURL_VALUE = "http://" + ROOT_URL_IP + ":"
			+ ROOT_URL_PORT + "/" + ROOT_URL_DIR + "";

	private final String TAG = this.toString();

	@Override
	protected Response doInBackground(Object... params) {

		Request request = getRequest();
		Response response = new Response();

		// get CommManager
		CommManager commManager = CommManager.getInstance();
		// get HttpService
		ICommService svc = commManager.getService(CommManager.HTTPSERVICE);
		svc.setReadTimeOut(1000*60*5);
		
		try {
			String uri = "", display_name = "", callback = "";
			JSONObject decodedData = (JSONObject) request.getData();
			String url = "";
			if (decodedData.has(PARAM)) {
				JSONObject param;
				try {
					param = decodedData.getJSONObject(PARAM);
					if (param.has(CALLBACK)) {
						callback = param.getString(CALLBACK);
					}

					if (param.has(URI)) {
						uri = param.getString(URI);
					}

					if (param.has(DISPLAY_NAME)) {
						display_name = param.getString(DISPLAY_NAME);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			Logger.d(TAG, "CALL DownloadImageTask");

			if (DownLoadReqeust(request, response, uri, display_name)) {
				Logger.d(TAG, "CALL DownloadImageTask: SUCCESS: "
						+ AbstractDownloadService.getDownloadPath() + display_name);
				response.setCallback(callback);
				//response.setData("\'" + DOWNLOAD_ROOT + display_name + "\'");
				response.setData("\'"+ AbstractDownloadService.getDownloadPath() + display_name + "\'");
				response.setError(false);
			} else {
				Logger.d(TAG, "CALL DownloadImageTask: FAIL");
				// response = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			svc.setReadTimeOut( 1000*60*1 );
		}

		return response;
	}

	public boolean DownLoadReqeust(final Request req, Response res, String uri,
			String fileName) {
		
		String callback = "";
		JSONObject decodedData = (JSONObject) req.getData();
		String url = "";
		if (decodedData.has(PARAM)) {
			JSONObject param;
			try {
				param = decodedData.getJSONObject(PARAM);
				if (param.has(CALLBACK)) {
					callback = param.getString(CALLBACK);
				}
			} catch (Exception e) {
				
			}
		}
		 
		boolean result = true;
		final BaseActivity activity = req.getSrcActivity();

		IDownload downloadService = getController().getDownloadService();
		Logger.d(TAG, "requestListener::" + req.getListener());
		try {
			JSONObject jsonObj = makeDownloadFileObject(fileName, uri, "direct");
			jsonObj.put(REQUESTURL, REQUESTURL_VALUE);
			Logger.e(TAG, jsonObj.toString());
			req.setData(jsonObj);
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
			Logger.e(TAG, e.toString());
		}
		res.setRequest(req);
		try {
			/*activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((AbstractActivity) activity)
							.showDialog(AbstractActivity.DIALOG_ID_PROGRESS_BAR);
					ProgressDialog dialog = ((AbstractActivity) activity)
							.getDlg();
					dialog.setMessage( "다운로드중입니다." );
					activity.dlg = dialog;
					Logger.d(TAG, "DownLoadReqeust : " + dialog);
				}
			});*/			
			req.setReplaceDownloadFile(true);
			downloadService.syncDownload(req, res);
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
			JSONObject root = new JSONObject();
			JSONObject header = new JSONObject();
			try {
				root.put("header",header);
				header.put("result", false);
				if ( e instanceof HttpResponseException ) {			// HTTTP
					header.put("error_code", "HE0"+ ((HttpResponseException)e).getStatusCode() );
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
			} finally {
				
			}
			
			final String callback1 = callback;
			final JSONObject root1 = root;
			
			req.getSrcActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					req.getSrcActivity().webView.loadUrl ( "javascript:" + callback1 + "("+ root1 +")");
					Logger.d(TAG, "javascript:" + callback1 + "("+ root1 +")");
				}
			});
			res.setError(true);
			res.setData(e);
		}
		setResponse(res);
		return result;
	}

	public JSONObject makeDownloadFileObject(String filename, String uri,
			String method) {
		JSONObject sendObj = null;
		try {
			sendObj = JsonUtil.addRequestHeader(sendObj, "", "sessionid1");
			JSONObject paramObj = new JSONObject();
			sendObj.put("param", paramObj);
			paramObj.put("display_name", filename);
			paramObj.put("uri", uri);
			paramObj.put("method", method);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return sendObj;
	}
}
