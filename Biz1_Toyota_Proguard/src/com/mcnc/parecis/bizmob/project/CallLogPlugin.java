package com.mcnc.parecis.bizmob.project;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.provider.CallLog;

import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.plugin.BizmobPlugin;

/**
 * 통화 목록을 얻어오는 플러그인 클래스
 *  
 * @author lyoul
 *
 */
public class CallLogPlugin extends BizmobPlugin {

	private String TAG = "CallLogPlugin";
	
	// 24시간에 대한 millisecond 값
	private final int oneDay = 86400000;
	
	private long toDay;
	
	private JSONObject result;
 
	/**
	 * <pre>
	 * 모든 통화 목록을 가져오는 메서드
	 * <br>
	 * <b>Parameters:</b>
	 * 	day - 가져올 통화목록의 날짜 
	 * 		0 = 오늘, 1 = 1일 전, 2 = 2일 전 ... 24일까지 가능하며 25일부터는 return 값이 내려오지 않는다.
	 * 	maxCount - 가져올 최대 통화목록 갯수
	 * <b>return:</b> 
	 * 	Json Object
	 * 		result : [{"callType":1 = 수신 or 2 = 발신 or 3 = 부재중,
	 * 				"name":"이름", (주소록에 있는경우에는 저장된 이름, 주소록에 없는 경우에는 "")
	 * 				"number":"전화번호", 
	 * 				"timeMillisec":기록시간(msec)}]
	 * </pre>
	 */
	// type 값이 없으면 모든 통화목록을 가져온다.
	public String getCallLog(int day, int maxCount){
		return getCallLog(-1, day, maxCount);
	}

	/**
	 * <pre>
	 * Type에 따른 통화 목록을 가져오는 메서드
	 * <br>
	 * <b>Parameters:</b>
	 * 	type - CallLog의 타입
	 * 		1 = 수신, 2 = 발신, 3 = 부재중
	 * 	day - 가져올 통화목록의 날짜 
	 * 		0 = 오늘, 1 = 1일 전, 2 = 2일 전 ... 24일까지 가능하며 25일부터는 return 값이 내려오지 않는다.
	 * 	maxCount - 가져올 최대 통화목록 갯수
	 * <b>return:</b> 
	 * 	Json Object
	 * 		result : [{"callType":1 = 수신 or 2 = 발신 or 3 = 부재중,
	 * 				"name":"이름", (주소록에 있는경우에는 저장된 이름, 주소록에 없는 경우에는 "")
	 * 				"number":"전화번호", 
	 * 				"timeMillisec":기록시간(msec)}]
	 * </pre>
	 */
	public String getCallLog(int type, int day, int maxCount)
	{
		Logger.d(TAG, "vanStart");

		// 오늘 날짜를 0시 0분 0초로 셋팅하여 millisecond 로 반환함
		Calendar cal = Calendar.getInstance();
		int y = cal.get(Calendar.YEAR);		
		int m = cal.get(Calendar.MONDAY);
		int d = cal.get(Calendar.DAY_OF_MONTH);
		
		cal.set(y, m, d, 00, 00, 00);
		
		toDay = cal.getTimeInMillis();
		// ------------------------
		
		JSONArray jArray = new JSONArray();
		result = new JSONObject();

		String name;
		int index = 1;

		String[] CALL_PROJECTION = {
				CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.DATE};
		
		// selection - 오늘 날짜의 0시 0분 0초의 시간에서 24시간 만큼을 뺀 값으로 계산한다
		Cursor cursor = ctx.getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				CALL_PROJECTION,
				CallLog.Calls.DATE + " >= " + (toDay - (oneDay * day)),
				null,
				CallLog.Calls.DATE + " desc");
		
		if(cursor.moveToFirst() && cursor.getCount() > 0){
			while(cursor.isAfterLast() == false){
				
				JSONObject obj = new JSONObject();
				
				// type이 null 인 경우 모든 통화목록을 가져옴
				if(!(type == 1 || type== 2 ||type == 3)){
					
					if(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)) == null){
						name = "";
					}else{
						name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
					}
					
					try {
						obj.put("callType", Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))));
						obj.put("name", name);
						obj.put("number", cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
						obj.put("timeMillisec", cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					jArray.put(obj);
					
					index++;
					
				// type이 있는경우 해당 type의 목록만 가져옴
				}else if(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)).equals(String.valueOf(type))){
					
					if(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)) == null){
						name = "";
					}else{
						name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
					}
					
					try {
						obj.put("callType", Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE))));
						obj.put("name", name);
						obj.put("number", cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
						obj.put("timeMillisec", cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
					jArray.put(obj);
					
					index++;
				}

				if(index <= maxCount){
					cursor.moveToNext();
				}else{
					break;
				}
			}
		}
		
		try {
			result.put("result", jArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Logger.d(" ------------------------ ", result.toString());

		return result.toString();
	}
}
