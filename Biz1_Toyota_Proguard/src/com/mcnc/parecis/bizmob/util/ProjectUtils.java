package com.mcnc.parecis.bizmob.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.def.AppConfig;
import com.mcnc.parecis.bizmob.def.Def;
import com.mcnc.parecis.bizmob.def.TaskID;

public class ProjectUtils {
	
	public static boolean isWifiConneted ( Context context ) {
		boolean result = false;
		BaseActivity activity = (BaseActivity) context;
		ConnectivityManager manager =  (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);  
		//NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
		NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
		//NetworkInfo ni = manager.getActiveNetworkInfo();  

		result = wifi.isConnected();
		return result;
	}
	
	public static boolean checkWifi ( Context context ,  String id ) {
		boolean result = false;
		
		if ( id.equals(TaskID.TASK_ID_RELOAD_WEB)|| id.equals(TaskID.TASK_ID_AUTH ) 
				|| id.equals(TaskID.TASK_ID_DOWNLOAD_IMAGE ) ||  id.equals(TaskID.TASK_ID_GOTO_FILE_UPLOAD )
				|| id.equals(TaskID.TASK_ID_UPDATE20)){
			result = isWifiConneted (context);
			// FIXME 개발 모드일경우 wifi 사용가능하도록...
			if ( ! Def.IS_RELEASE ) {
				result = false;
			}
		}
		return result;
	}
	
	public static boolean isAllowUrl ( String url ) {
		boolean bResult = false;
		try {
			if (  AppConfig.ALLOWED_DOMAIN == null ) {
				
			} else {
				String item = null;
				for ( int i = 0; i < AppConfig.ALLOWED_DOMAIN.length(); i ++ ) {
					item = AppConfig.ALLOWED_DOMAIN.getString(i);
					if ( url.contains( item ) || item.equals("*")){
						bResult = true;
						break;
					}
				}
			}
		} catch (Exception e) {
		}
		if ( ! bResult ) {
			Logger.d("ProjectUtils", "Not Allowed Url : "  + url );
		}
		return bResult;
	}

	public static String copyFile ( String orgFileNmae, String orgPath, String directory, String file_name ) {
		FileInputStream inputStream = null; //임시파일을 복사할 때 원본 파일을 읽어오기 위해 선언   
		FileOutputStream outputStream = null; //임시파일을 복사할 때 원본 파일을 임시파일에 쓰기 위해 선언 
		File fromFile = null; 				//불러올 파일의 경로를 저장하기 위해 선언 
		File toFile = null; 				//임시파일을 복사할 경로를 지정하기 위해 선언 
		String targetPath = "";
		
		try {
			if ( directory.length() > 0 && file_name.length() > 0 ) {
				if ( directory.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
					targetPath = directory+"/" + file_name; 
				} else {
					targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+directory+"/" + file_name; 
				}
			} else if ( directory.length() > 0 ) {
				if ( directory.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())) {
					targetPath = directory+"/" + orgFileNmae; 
				} else {
					targetPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+directory+"/" + orgFileNmae; 
				}
			} else {
				targetPath = orgPath.substring(0, orgPath.lastIndexOf("/")) + file_name; 
			}
			fromFile = new File ( orgPath );
			toFile = new File ( targetPath );
			
			inputStream = new FileInputStream(fromFile); 
			outputStream = new FileOutputStream(toFile); 
			FileChannel fcin = inputStream.getChannel(); 
			FileChannel fcout = outputStream.getChannel(); 
			long size = fcin.size(); 
			fcin.transferTo(0, size, fcout); 
			fcout.close(); 
			fcin.close(); 
			outputStream.close(); 
			inputStream.close();
			
			//그리고 원본은 지웁니다. 
			fromFile.delete();

		} catch (Exception e) {
			e.printStackTrace();
			targetPath = null;
		}
		return targetPath;
	}
	
}
