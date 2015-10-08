package com.mcnc.parecis.bizmob.project;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;

import com.mcnc.hsmart.plugin.BizmobPlugin;
import com.mcnc.parecis.bizmob.project.tmap.TmapUtil;

/**
 * TMAP 연동을 위해서 만들어진 플러그인 클래스
 * 
 * @author lyoul
 *
 */
public class TmapPlugin extends BizmobPlugin{
	private String TAG = "TmapPlugin";
	
	/**
	 * 각 안드로이드 버전에 맞게 내부에서 적용 버전으로 TMAP의 기능을 사용할 수 있도록 하는 함수
	 * @param argv Tmap에서 제공한 문서 참조 (대외비)
	 */
	public void showTmap(String argv) {
		TmapUtil.showTmap(ctx, argv);
	}
	
	/**
	 * 원하는 주소의 위도 경도를 리턴하는 함수
	 * 
	 * 이 함수를 이용하여 showTmap()의 argv를 설정할 수 있음
	 * 현재는 한국을 기준으로 작업되어 있음
	 * 
	 * return JsonObject
	 * lat, lng (double, default = -1) 
	 * ex) {"lat":37.52843806666666,"lng":126.9686338333333}
	 * 
	 * @param addrName 원하는 위도, 경도의 주소
	 * @return JsonObject String
	 */
	public String getLocation(String addrName) {
		List<Address> addresses = TmapUtil.getLocation(ctx, addrName, 1);
		double lat = -1;
		double lng = -1;
		if(addresses.size() > 0) {
			Address address = addresses.get(0);
			if(address != null) {
				lat = address.getLatitude();
				lng = address.getLongitude();
			}
		}
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("lat", lat);
			jsonObject.put("lng", lng);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	/**
	 * 현재 설치되어 있는 Tmap 어플의 버전 정보
	 * 
	 * "T map 2.0" / "T map 2.5"/ "T map 3.0"
	 * 
	 * @return 어플의 버전 정보
	 */
	public String getVersion() {
		//"T map 2.0" / "T map 2.5"/ "T map 3.0"
		return TmapUtil.getVersion(ctx);
	}
}