package com.mcnc.parecis.bizmob.task;

import java.io.File;

import android.content.Intent;

import com.mcnc.hsmart.configuration.provider.LocalFileProviderHsmart;
import com.mcnc.hsmart.controller.Smart2ProcessController;
import com.mcnc.hsmart.core.common.Request;
import com.mcnc.hsmart.core.common.Response;
import com.mcnc.hsmart.core.download.AbstractDownloadService;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.util.FileUtils;
import com.mcnc.hsmart.def.Def;
import com.mcnc.hsmart.task.BaseTask;

/**
 * LogoutTask
 * 
 * @version 1.0 1 31 2011
 * @author nadir93@gmail.com
 */
public class LogoutTask extends BaseTask {

	private final String TAG = this.toString();

	@Override
	protected Response doInBackground(Object... arg0) {

		Logger.d(TAG, "doInBackground");
		
		// 로컬 저장일 경우 폴더를 지운다.
		AbstractDownloadService.setDownloadPath( LocalFileProviderHsmart.URI_PREFIX + Def.ROOT_DOWNLOAD_DIR);
		// 로컬 저장일 경우 폴더를 반드시 지운다...
		String path = LocalFileProviderHsmart.URI_PREFIX + Def.ROOT_DOWNLOAD_DIR;
		File f = new File(path);
		if (f.exists()) {
			FileUtils.delete(f);
			Logger.d(TAG, "delete : " + LocalFileProviderHsmart.URI_PREFIX + Def.ROOT_DOWNLOAD_DIR);
		}
		
		// Response response = new Response();
		final Request request = getRequest();
		Smart2ProcessController controller = (Smart2ProcessController)request.getSrcActivity().getApplication();
		Class activity = controller.getActivityClass(Smart2ProcessController.START_ACTIVITY);
		final Intent intent = new Intent(request.getSrcActivity(),
					activity);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("Logout", true);

		request.getSrcActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				request.getSrcActivity().startActivity(intent);
				/*request.getSrcActivity().overridePendingTransition(
						R.anim.zoom_enter, R.anim.zoom_exit);*/
			}
		});
		return null;
	}
}
