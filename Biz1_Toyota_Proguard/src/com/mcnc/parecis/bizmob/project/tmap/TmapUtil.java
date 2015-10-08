package com.mcnc.parecis.bizmob.project.tmap;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.mcnc.hsmart.core.log.Logger;

public class TmapUtil {
	private static String TAG = "TmapUtil";
	
	private static String tmapVersion = null;
	public static String TMAP_2_0 = "T map 2.0";
	public static String TMAP_2_5 = "T map 2.5";
	public static String TMAP_3_0 = "T map 3.0";
	
	public static void showTmap(Context context, String argv) {
		if(tmapVersion == null) {
			tmapVersion = getVersion(context);
		}
		
		try{
			if(tmapVersion.equals(TMAP_2_0)) {
				showTmap20(context, argv);
			} else if(tmapVersion.equals(TMAP_2_5)) {
				showTmap25(context, argv);
			} else if(tmapVersion.equals(TMAP_3_0)) {
				showTmap30(context, argv);
			}
		}catch (ActivityNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(context, "tmap 어플리케이션이 설치되지 않았습니다.", Toast.LENGTH_LONG);
		}
	}
	
	// Tmap 2.0
	private static void showTmap20(Context context, String argv) throws ActivityNotFoundException{
		Intent intent = new Intent();
		intent.setClassName("com.skt.skaf.l001mtm091",
		"com.skt.skaf.l001mtm091.l001mtm091");
//		intent.putExtra("TMAP", "A1,126.984098,37.566385,T타워");
		intent.putExtra("TMAP", argv);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

	}

	// Tmap 2.5 - tablet pc
	private static void showTmap25(Context context, String argv) throws ActivityNotFoundException{
		Intent intent = new Intent();
		intent.setClassName("com.skt.skaf.l001mtm091",
		"com.skt.skaf.l001mtm091.MainActivity");
//		intent.putExtra("TMAP", "A1,126.984098,37.566385,T타워");
		intent.putExtra("TMAP", argv);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

	}

	// Tmap 3.0
	private static void showTmap30(Context context, String argv) throws ActivityNotFoundException{
		Intent intent = new Intent();
		intent.setClassName("com.skt.skaf.l001mtm091",
		"com.skt.skaf.l001mtm091.IntroActivity");
//		intent.putExtra("TMAP", "A1,126.984098,37.566385,T타워");
		intent.putExtra("TMAP", argv);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	public static String getVersion(Context context) {
		String result = ""; 
			
		PackageManager manager = context.getPackageManager();
		List<PackageInfo> appInfoList = manager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		boolean isPackage = false;
		for (int j = 0, jend = appInfoList.size(); j < jend ; j++) {
		   PackageInfo pi = appInfoList.get(j);
		   if(pi.packageName.equals("com.skt.skaf.l001mtm091")) {
			   isPackage = true;
			   break;
		   }
		}

		/*
		 * 프로그램 설치 유무 확인
		 */
		Logger.d(TAG, "com.skt.skaf.l001mtm091: " + isPackage);
		if(!isPackage) {
			return "It's not installed.";
		}
		
		/*
		 * SDK 버전에 따른 타블릿 버전 확인
		 */
		if(Build.VERSION.SDK_INT > 10) {
			return TMAP_2_5;
		}
		
		/*
		 * Tmap3 폴더 유무로 2.0과 3.0의 버전 체크
		 */
		File file = new File("/mnt/sdcard/Tmap3");
		if(file.exists()) {
			result = TMAP_3_0;
		} else {
			result = TMAP_2_0;
		}
		
		return result;
	}
	
	public static List<Address> getLocation(Context context, String addr, int maxResult) {
		Geocoder geocoder = new Geocoder(context.getApplicationContext(), Locale.KOREA);
		List<Address> addresses = null;

		while(addr.length() > 0){
			
			try {
				addresses = geocoder.getFromLocationName(addr, maxResult);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(addresses != null &&  addresses.size() > 0) {
				Log.d("", addresses.get(0).getLatitude() +", " + addresses.get(0).getLongitude());
				break;
			}else{
				int lastIndex = addr.lastIndexOf(" ");
				if ( lastIndex == -1 ) {
					addr ="";
				} else {
					addr = addr.substring(0, lastIndex);
				}
			}
		}
		return addresses;
	}
}
