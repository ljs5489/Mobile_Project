package com.mcnc.parecis.bizmob.util;

import java.text.DecimalFormat;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.parecis.bizmob.def.TaskID;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

public class LMCServiceUtil {
	private Context context;
	private int sleepTime = 15*60;	// 15분
	private JSONArray pkgJsonArr = null;
	private int MAX_HEAP_SIZE = 32;
	private Handler handler = new Handler();
	private Double mTotalMemory = 0.0;
	private String TAG = "LMCServiceUtil";
	
	public LMCServiceUtil ( Context context ){
		this.context = context;
	}
	
	public void addPackage (String packageName) {
		if ( pkgJsonArr == null ) {
			pkgJsonArr = new JSONArray();
		}
		pkgJsonArr.put(packageName);
	}
	
	public void startLowMemoryCheckService( String jsonPackage, int sec){
		this.setSleepTime(sec);
		try {
			JSONObject json = new JSONObject(jsonPackage);
			pkgJsonArr = json.getJSONArray("package");
		
		} catch (JSONException e1) {
			if( pkgJsonArr == null){
				pkgJsonArr = new JSONArray();
			}
		}
		
		new Thread(new Runnable() {              
			public void run() {                  
				ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				while( true ) {
					if( isLowMemory() ) {
						List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
						for(RunningAppProcessInfo i : list){
							if(isUsePackage(i) == false)
								am.restartPackage(i.processName);
							Thread.yield();
						}
					}
					try {
						if( getSleepTime() == 0){
							break;
						}
						Thread.sleep(getSleepTime()*1000);	// 15분
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}// while end
			}
		}, "Process Killer").start();
	}
	
	private boolean isUsePackage(RunningAppProcessInfo info){
		if ( pkgJsonArr == null ) {
			return false;
		}
		for(int i = 0; i < pkgJsonArr.length(); i++){
			try {
				if(info.processName.equals(pkgJsonArr.getString(i)))
					return true;
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public void stopLowMemoryCheckService(){
		this.setSleepTime(0);
	}
	
	public void setSleepTime(int sec){
		this.sleepTime = sec; 
	}
	
	public int getSleepTime(){
		return this.sleepTime; 
	}
	
	public boolean isLowMemory(){
		Double maxMemory = new Double(Runtime.getRuntime().maxMemory()/new Double(1024*1024)); 		// app의 최대 heap 메모리 용량.
		Double totalMemory = new Double(Runtime.getRuntime().totalMemory()/new Double(1024*1024));  //app의 heap size 메모리 보기
		
		DecimalFormat df  = new DecimalFormat(); // format 변경을 위한 추가.
		df.setMaximumFractionDigits(2); //소수점 2 자리까지
		df.setMinimumFractionDigits(2);
		
		//system 전체에 대한 메모리를 보기 위한 추가.
		MemoryInfo info = new MemoryInfo();
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		manager.getMemoryInfo(info);
		
		MAX_HEAP_SIZE = manager.getMemoryClass();
		Logger.d(TAG, "MAX_HEAP_SIZE = " + MAX_HEAP_SIZE);
		
		Double mem_avail = new Double(info.availMem/new Double(1024*1024)); //system 전체 available 용량.
		CheckGC ("isLowMemory");
		
		Logger.d(TAG, "mem_avail : " + info.availMem);
		
		if(info.lowMemory){ //check system total memory - lowMemory시 ture 반환.
			handler.post(new Runnable(){
				@Override
				public void run() {
				     Toast.makeText( context, "가용 메모리가 부족합니다. 불필요한 프로그램을 종료해 주십시오.", android.widget.Toast.LENGTH_LONG).show();
				}
			});
			return true;
		}else{
			this.mTotalMemory = totalMemory;
			if(totalMemory > MAX_HEAP_SIZE){ //check heap size
				handler.post(new Runnable(){
					@Override
					public void run() {
						Toast.makeText( context, "가용 메모리가 부족합니다. 프로그램을 다시 시작해 주십시오.", android.widget.Toast.LENGTH_LONG).show();
					}
				});
				return true;
			}else{
				//Toast.makeText(this.ctx.getApplication(), "Heap Memory Checked!!!", android.widget.Toast.LENGTH_LONG).show();
				return false;
			}
		}
	}
	void CheckGC(String FunctionName) {
		long VmfreeMemory = Runtime.getRuntime().freeMemory();
		long VmmaxMemory = Runtime.getRuntime().maxMemory();
		long VmtotalMemory = Runtime.getRuntime().totalMemory();
		long waittime = 53;
		long Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
				/ VmtotalMemory;

		Logger.d(TAG, FunctionName + "Before Memorypercentage" + Memorypercentage
				+ "% VmtotalMemory[" + VmtotalMemory + "] " + "VmfreeMemory["
				+ VmfreeMemory + "] " + "VmmaxMemory[" + VmmaxMemory + "] ");

		// Runtime.getRuntime().gc();
		System.runFinalization();
		System.gc();
		VmfreeMemory = Runtime.getRuntime().freeMemory();
		VmmaxMemory = Runtime.getRuntime().maxMemory();
		VmtotalMemory = Runtime.getRuntime().totalMemory();
		Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
				/ VmtotalMemory;
		Logger.d(TAG, FunctionName + "_After Memorypercentage" + Memorypercentage
				+ "% VmtotalMemory[" + VmtotalMemory + "] " + "VmfreeMemory["
				+ VmfreeMemory + "] " + "VmmaxMemory[" + VmmaxMemory + "] ");
	}	
	
	
	
	// 따로 노는 함수.
	public static void requestKillProcess(final Context context) {
		// #1. first check api level.
		int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
		if (sdkVersion < 8) {
			// #2. if we can use restartPackage method, just use it.
			ActivityManager am = (ActivityManager) context
					.getSystemService(Context.ACTIVITY_SERVICE);
			am.restartPackage(context.getPackageName());
		} else {
			// #3. else, we should use killBackgroundProcesses method.
			new Thread(new Runnable() {
				@Override
				public void run() {
					ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
					String name = context.getApplicationInfo().processName;
					List<RunningAppProcessInfo> list = am.getRunningAppProcesses();
					for (RunningAppProcessInfo i : list) {
						if ( ! i.processName.equals(name)) {
							am.restartPackage(i.processName);
						}
						Thread.yield();
					}
					System.gc();
				}
			}, "Process Killer").start();
		}
	}
	
	public static void checkKillTaskID ( Context context, String taskId ) {
		String [] checkList = { TaskID.TASK_ID_SMS,
								TaskID.TASK_ID_GET_IMAGE_PICK, TaskID.TASK_ID_TEL,
								TaskID.TASK_ID_CAMERA_CAPTURE, TaskID.TASK_ID_MAIL_DOWNLOAD,
								TaskID.TASK_ID_SHOW_WEBSITE};
		
		for ( int i = 0;  i < checkList.length; i ++ ) {
			if ( taskId.equals(checkList[i]) ) {
				requestKillProcess (context);
				break;
			}
		}
	}
}