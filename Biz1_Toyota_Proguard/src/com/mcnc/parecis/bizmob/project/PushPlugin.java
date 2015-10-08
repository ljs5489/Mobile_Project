package com.mcnc.parecis.bizmob.project;

import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

import com.mcnc.hsmart.plugin.BizmobPlugin;
import com.mcnc.parecis.bizmob.def.Def;
import com.mcnc.parecis.bizmob.project.push.PushManager;

public class PushPlugin extends BizmobPlugin{
	public String TAG = "PushPlugin";
	
	/**
	 * <pre>
	 * PUSH 서비스를 시작하기 위한 메서드
	 * <br>
	 * <b>Parameters:</b>
	 * 	id - 사용자 계정
	 * 	pass - 비밀번호
	 * 	url - 디바이스 등록일 위한 servlet URL
	 * 	serverEmail - PUSH Server 메일 계정
	 * </pre>
	 * */
	public void start(String id, String pass, String url, String serverEmail){
		PushManager.getInstance().pushStart(ctx, id, pass, url, serverEmail, "");
		Log.d(TAG, "pushStart");
	}
	
	/**
	 * <pre>
	 * PUSH 서비스를 시작하기 위한 메서드
	 * <br>
	 * <b>Parameters:</b>
	 * 	id - 사용자 계정
	 * 	pass - 비밀번호
	 * 	url - 디바이스 등록일 위한 servlet URL
	 * 	serverEmail - PUSH Server 메일 계정
	 * 	userEmail - 사용자 메일 계정
	 * </pre>
	 * */
	public void start(String id, String pass, String url, String serverEmail, String userEmail){
		PushManager.getInstance().pushStart(ctx, id, pass, url, serverEmail, userEmail);
		Log.d(TAG, "pushStart");
	}
	
	/**
	 * <pre>
	 * PUSH 서비스를 종료하기 위한 메서드
	 * </pre>
	 * */
	public void stop() {
		PushManager.getInstance().pushStop(ctx);
		Log.d(TAG, "pushStop");
	}
	
	/**
	 * <pre>
	 * PUSH 서비스가 실행중인지를 알려주는 메서드
	 * <br>
	 * <b>Return:</b>
	 * 	true or false
	 * </pre>
	 * */
	public boolean isRunning(){
		Log.d(TAG, "isRunning" + PushManager.getInstance().isRunning(ctx));
		return PushManager.getInstance().isRunning(ctx);
	}
	
	/**
	 * <pre>
	 * PUSH 서비스 시작시 표시 되는 Notification 메시지를 변경하는 메서드
	 * <br>
	 * <b>Parameters:</b>
	 * 	messageType - 메시지 타입
	 * 		0 = Notification 제목, 1 = PUSH 시작 Notification 메시지, 2 = PUSH 종료 Notification 메시지
	 * 	message - 변경할 메시지
	 * </pre>
	 * */
	public void setNotiMessage(int messageType, String message) {
		PushManager.getInstance().setNotiMessage(messageType, message);
	}

	/**
	 * <pre>
	 * PUSH 서비스 시작시 표시 되는 Notification 메시지를 가져오는 메서드
	 * <br>
	 * <b>Parameters:</b>
	 * 	messageType - 메시지 타입
	 * 		0 = Notification 제목, 1 = PUSH 시작 Notification 메시지, 2 = PUSH 종료 Notification 메시지
	 * <b>Return:</b>
	 * 	현재 표시되는 Notification 메시지
	 * </pre>
	 * */
	public String getNotiMessage(int messageType) {
		return PushManager.getInstance().getNotiMessage(messageType);
	}
	
	/**
	 * <pre>
	 * PUSH 서비스 시작시 표시 되는 Notification 메시지를 변경하는 메서드
	 * <br>
	 * <b>Parameters:</b>
	 * 	message - 변경할 메시지
	 * 		String[] = {"Notification 제목", "PUSH 시작 Notification 메시지", "PUSH 종료 Notification 메시지"}
	 * </pre>
	 * */
	public void setNotiMessages(String [] messages) {
		PushManager.getInstance().setNotiMessages(messages);
	}
	
	/**
	 * <pre>
	 * PUSH 서비스 시작시 표시 되는 Notification 메시지를 가져오는 메서드
	 * <br>
	 * <b>Return:</b>
	 * 	현재 표시되는 Notification 메시지
	 * 		String[] = {"Notification 제목", "PUSH 시작 Notification 메시지", "PUSH 종료 Notification 메시지"}
	 * </pre>
	 * */
	public String[] getNotiMessages() {
		return PushManager.getInstance().getNotiMessages();
	}
	
	
	public String getAccessInfo () {
		String result = "";
		JSONObject root = new JSONObject();
		try {
			root.put("domain", Def.ROOT_URL_IP);
			root.put("port", Def.ROOT_URL_PORT);
			root.put("context", Def.ROOT_URL_DIR);
			root.put("ssl", Def.IS_SSL);
			result = root.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public void setTerminate (boolean b) {
		PushManager.getInstance().setTerminate(b);
	}
	
	public boolean getTerminate () {
		return PushManager.getInstance().getTerminate();
	}
}
