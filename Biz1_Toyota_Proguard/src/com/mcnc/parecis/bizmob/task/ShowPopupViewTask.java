package com.mcnc.parecis.bizmob.task;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.view.Display;
import android.view.WindowManager;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.controller.BaseProcessController;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.task.BaseTask;
import com.mcnc.parecis.bizmob.view.PopupViewActivity;

public class ShowPopupViewTask extends BaseTask {
	private static final String PARAM = "param";
	private final String TAG = this.toString();
	final int ORIENTATION_NONE = 0;
	final int ORIENTATION_VERTICAL  = 1;
	final int ORIENTATION_HORIZONTAL  = 2;
	final int ORIENTATION_AUTO  = 3;
	
	@Override
	protected Response doInBackground(Object... params) {
		Logger.d(TAG, "doInBackground");
		Response response = new Response();
		Request request = getRequest();
		JSONObject jsonData = (JSONObject) request.getData();
		
		String targetPage = null;
		String title = null;
		String callback = "";
		JSONObject message = null;
		String android_hardware_backbutton = "";
		
		int width = 0;
		int height = 0;
		int orientation = ORIENTATION_NONE;
		String base_size_orientation = "";
		
		try {
			JSONObject param = jsonData.getJSONObject(PARAM);
			targetPage = param.getString("target_page");
			if ( param.has("width")){
				width = param.getInt("width");
			}
			if ( param.has("height") ){
				height = param.getInt("height");
			}
			
	        if ( param.has("base_size_orientation")) {
	        	int height_percent = Integer.valueOf( param.getString("height_percent"));
	        	int width_percent = Integer.valueOf( param.getString("width_percent"));
	        	
		        int screen_width = 0;
		        int screen_height = 0;
	        	
				// % 적용
				Configuration config = request.getSrcActivity().getResources().getConfiguration();

	    		WindowManager wm = (WindowManager) request.getSrcActivity().getSystemService(Context.WINDOW_SERVICE);  
	    		Display display = wm.getDefaultDisplay();
	    		if ( config.orientation == Configuration.ORIENTATION_PORTRAIT ) {
			        screen_width = display.getWidth();
			        screen_height = display.getHeight();
	    		} else {
			        screen_width = display.getHeight();
			        screen_height = display.getWidth();
	    		}
	    		
	    		base_size_orientation = param.getString("base_size_orientation");
		        if (base_size_orientation.equals("vertical")){
		    		width = screen_width * width_percent / 100;
		    		height = screen_height * height_percent / 100;
		    		orientation = ORIENTATION_VERTICAL;
		        } else if (base_size_orientation.equals("auto")){
		    		width = screen_width * width_percent / 100;
		    		height = screen_height * height_percent / 100;
		    		orientation = ORIENTATION_AUTO;
		        } else if (base_size_orientation.equals("horizontal")){
		    		width = screen_height * width_percent / 100;
		    		height = screen_width * height_percent / 100;
		    		orientation = ORIENTATION_HORIZONTAL;
		        } else {
		        	Logger.d(TAG, "base_size_orientation ERROR : " + base_size_orientation);
		        }
	        }
			
			message = param.getJSONObject("message");
			callback = param.getString("callback");
			if ( param.has("android_hardware_backbutton") ){
				android_hardware_backbutton = param.getString("android_hardware_backbutton");
			}
					
			Intent intent = new Intent(request.getSrcActivity(), PopupViewActivity.class);
			intent.putExtra( "target_page", targetPage );
			intent.putExtra( "title", title);
			
			intent.putExtra( "width", width);
			intent.putExtra( "height", height);
			intent.putExtra( "orientation", orientation);
			
			// data 로 넣어야 웹뷰 로드후 바로 콜백에 담아준다.
			intent.putExtra( "data", message.toString());
			intent.putExtra( "callback", callback );
			intent.putExtra("modal", true);
			intent.putExtra("android_hardware_backbutton", android_hardware_backbutton);
			
			// putData
			BaseProcessController app = ((BaseProcessController) request
					.getSrcActivity().getApplication());

			String randomNumber = app.putDataMap(message.toString());
			intent.putExtra("dataKey", randomNumber);
			
			request.getSrcActivity().startActivityForResult(intent, 10);
			
			response.setError(false);
		} catch (Exception e) {
			response.setError(true);
			response.setData(e);
			Logger.e(TAG, "response::fail::" + e);
		}
		response.setRequest(request);
		setResponse(response);
		return response;
	}

}
