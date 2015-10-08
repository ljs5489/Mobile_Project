package com.mcnc.parecis.bizmob.task;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.view.AbstractActivity;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.task.BaseTask;
import com.mcnc.hsmart.util.device.contact.SearchHelperForDeviceContact;
import com.mcnc.hsmart.util.device.contact.SimpleContactData;

public class DelContactGroupTask extends BaseTask{
	private final String TAG = this.toString();

	private static final String RESULT = "result";
	private static final String PARAM = "param";
	private static final String GROUP_ID = "group_id";
	private static final String DELETE_FLAG = "del_contacts";
	private static final String CALLBACK = "callback";
	
	@Override
	protected Response doInBackground(Object... arg0) {
		Response response = new Response();
		final Request request = getRequest();
		final BaseActivity activity = request.getSrcActivity();
		JSONObject jsonData = (JSONObject) request.getData();
		String groupId = "";
		boolean flag = false;
		String callback = "";
		int resultData = -1;
		boolean result = false;
		JSONObject responseData = new JSONObject();
		
		Context context = request.getSrcActivity();
		
		try {
			responseData.put(RESULT, false);
			JSONObject param = jsonData.getJSONObject(PARAM);
			if(param.has(GROUP_ID)) {
				groupId = param.getString(GROUP_ID);
			}
			if(param.has(DELETE_FLAG)) {
				flag = param.getBoolean(DELETE_FLAG);
			}
			
//			if(flag){
//				Cursor grouCursor = context.getContentResolver().query(
//						Data.CONTENT_URI,
//						new String[] {ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, 
//								ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID},
//						Data.DATA1 + " = " + contactUid + " AND " + Data.MIMETYPE + " = " +
//								ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "''",
//						null, null);
//			}
			
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((AbstractActivity) activity)
							.showDialog(AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
					ProgressDialog dialog = ((AbstractActivity) activity)
							.getDlg();
					dialog.setMessage("주소록 삭제중...");
					activity.dlg = dialog;
				}
			});
			
//			long sTime = System.currentTimeMillis();	
//			Log.d("Start Time ---------------- ", String.valueOf(sTime));
			
			if(flag){
				
				ArrayList<SimpleContactData> list;
				
				list = SearchHelperForDeviceContact.getGroupContacts(request.getSrcActivity(), groupId);
				
				for(int i = 0; i < list.size(); i++){
					resultData = SearchHelperForDeviceContact.deletePerson(request.getSrcActivity(),
								list.get(i).getContactId(), list.get(i).getDisplayName());
					
					Log.d("delContact -------------------- ", "ID : " + list.get(i).getContactId() + " NAME : " + list.get(i).getDisplayName());
					
					if(resultData == -1){
						break;
					}
				}
			}
			
			//그룹 삭제
			context.getContentResolver().delete(ContactsContract.Groups.CONTENT_URI, 
				ContactsContract.Groups._ID + "=" + groupId, null);
			
//			long eTime = System.currentTimeMillis();	
//			Log.d("End Time ---------------- ", String.valueOf(eTime));
//			
//			long t = eTime - sTime;
//			Log.d("End Time ---------------- ", String.valueOf(t));
			
			activity.removeDialog(AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);

			callback = jsonData.getJSONObject(PARAM).getString(CALLBACK);
//			resultData = SearchHelperForDeviceContact.deletePerson(request.getSrcActivity(), contactUid, contactName);
			result = true;
			responseData.put(RESULT, result);
		}catch (Exception e) {
			e.printStackTrace();
			try {
				responseData.put(RESULT, result);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}
		response.setError(false);
		response.setData(responseData);
		response.setRequest(request);
		response.setCallback(callback);
		setResponse(response);
		return response;
	}
}
