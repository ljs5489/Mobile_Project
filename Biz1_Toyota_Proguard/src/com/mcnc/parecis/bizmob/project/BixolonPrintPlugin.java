package com.mcnc.parecis.bizmob.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Environment;

import com.bixolon.android.library.BxlService;
import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.core.util.ImageUtil;
import com.mcnc.hsmart.core.view.AbstractActivity;
import com.mcnc.hsmart.core.view.BaseActivity;
import com.mcnc.hsmart.plugin.BizmobPlugin;
import com.mcnc.hsmart.util.DialogUtility;
import com.mcnc.parecis.bizmob.def.Def;

public class BixolonPrintPlugin extends BizmobPlugin{
	private BixServiceExtends printer = new BixServiceExtends();
	private BluetoothAdapter mBtAdapter;
	
	public BixServiceExtends getBixlonPrinter() {
		if(printer == null) {
			printer = new BixServiceExtends();			
		}
		return printer;
	}
	/**
	 * 블루투스의 on/off 상황을 체크하는 함수
	 * 
	 * @return 블루투스 사용 유무 결과 값
	 */
	public boolean isBluetoothEnable(){
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		return mBtAdapter.isEnabled();
	}
	
	public String getPairedList() {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		try {

			// If there are paired devices, add each one to the ArrayAdapter
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					JSONObject deviceJson = new JSONObject();
					deviceJson.put("paired_name", device.getName());
					deviceJson.put("paired_address", device.getAddress());
					jsonArray.put(deviceJson);
				}
			}
			jsonObject.put("list", jsonArray);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject.toString();
	}
	
	class BixServiceExtends  extends BxlService{
		
		@Override
		public int PrintText(String Data, int Alignment, int Attribute,
				int TextSize, String encoding) {
			Logger.d("BixServiceExtends", "PrintText1: " + Data + ", " + Alignment + ", " + Attribute + ", " + TextSize + ", " + encoding);
			return super.PrintText(Data, Alignment, Attribute, TextSize, encoding);
		}

		@Override
		public int PrintText(String Data, int Alignment, int Attribute, int TextSize) {
			Logger.d("BixServiceExtends", "PrintText2: " + Data + ", " + Alignment + ", " + Attribute + ", " + TextSize);
			return super.PrintText(Data, Alignment, Attribute, TextSize);
		}

		
		@Override
		public synchronized int Connect() {
			Logger.d("BixServiceExtends", "Connect Try .....");
			return super.Connect();
		}

		public int PrintBarcode( String data, int Symbology,
				int Height, int Width, int Alignment, int TextPosition) {
			return super.PrintBarcode(data.getBytes(), data.getBytes().length, Symbology, 
					Height, Width, Alignment, TextPosition);
		}

		@Override
		public int PrintImage(String FileName, int Width, int Alignment, int Level) {
			// SD 옮기기
			String sdFile = "";
			//KtngProcessController controller = KtngProcessController.getInstance();
			//Context context = controller.getApplicationContext();
			sdFile = copyToSdcard (ctx, FileName);
			
			if (sdFile.length() <= 0){
				Logger.d("BixolonInterface", "File Not Found");
				return 201;
			}
			
			return super.PrintImage(sdFile, Width, Alignment, Level );
		}
		
		public int PrintSdImage(String FileName, int Width, int Alignment, int Level ) {
			String sdFile = "";
//			Logger.d( "BixServiceExtends", "Copy to " + FileName + " = >" + Width );
			Logger.d("BixServiceExtends", "PrintSdImage: " + FileName +", " + Width + ", " + Alignment +", " + Level);
			//KtngProcessController controller = KtngProcessController.getInstance();
			//Context context = controller.getApplicationContext();
			if (FileName.length() <= 0){
				Logger.d("BixServiceExtends", "File Not Found");
				return 201;
			}
			if ( FileName.contains(Environment.getExternalStorageDirectory().getAbsolutePath()) ) {
				sdFile = FileName;
			} else {
				sdFile = Environment.getExternalStorageDirectory() + "/" + FileName;
			}
			Logger.d("BixServiceExtends", "PrintSdImage: " + sdFile);
			return super.PrintImage(sdFile, Width, Alignment, Level );
		}	
		
	    public void log(String logMessage){
	        Logger.i("TEST", logMessage);
	    }

	    public void logInt(int logMessage){
	        Logger.i("TEST", logMessage +"");
	    }
	    
	    public void clearRefTable() {
	    	Logger.d("TEST", "clearRefTable BIX");
	    	return;
	    }
	    
		public String copyToSdcard ( Context context, String src ) {
			//src = "IRSK00/images/bixolon_img_1.png";
			//src = "IRSK00/images/bixolon_img_1.png";
			String fileName = src.substring( src.lastIndexOf("/"));
			String dstPath = "";
			InputStream ins = null;
			OutputStream outs = null;
			
			if ( src.length() <= 0 ){
				return dstPath;
			}
			
			try {
				// local to sd
				if ( Def.IMAGE_MODE == ImageUtil.MODE_HTTP){
					File dstFile = new File( Environment.getExternalStorageDirectory()
										.getAbsolutePath() + fileName ); 
					if ( dstFile.exists() ) {
						dstFile.delete();
					}
					String url = ImageUtil.getUri(Def.MODE_HTTP, src);
					InputStream stream = (InputStream) new URL(url).getContent();
					outs = new FileOutputStream(dstFile);
					byte[] buffer = new byte[10240];
					int length;
			    	Logger.d( "BixServiceExtends", "Copy to " + src + " = >" + dstFile );
			    	while ((length = stream.read(buffer))>0){
			    		outs.write(buffer, 0, length);
			    	}
			    	outs.flush();
			    	outs.close();
			    	stream.close();
			    	dstPath = dstFile.getAbsolutePath();
			    	Logger.d(TAG, "Def.IMAGE_MODE :" + dstPath);
			    	
				} else if ( Def.IMAGE_MODE == ImageUtil.MODE_LOCAL ){
					File srcFile = new File( ImageUtil.ROOT_PATH + "contents/" + src );
					File dstFile = new File( Environment.getExternalStorageDirectory()
										.getAbsolutePath() + fileName ); 
					if ( dstFile.exists() ) {
						dstFile.delete();
					}
					ins = new FileInputStream(srcFile);
					outs = new FileOutputStream(dstFile);
					byte[] buffer = new byte[10240];
					int length;
			    	Logger.d( "BixServiceExtends", "Copy to " + src + " = >" + dstFile );
			    	while ((length = ins.read(buffer))>0){
			    		outs.write(buffer, 0, length);
			    	}
			    	outs.flush();
			    	outs.close();
			    	ins.close();
					
			    	dstPath = dstFile.getAbsolutePath();
				} else {
					File dstFile = new File( Environment.getExternalStorageDirectory()
										.getAbsolutePath() + fileName ); 
					if ( dstFile.exists() ) {
						Logger.d( "BixServiceExtends", "dstFile to1 == " + src + " = >" + dstFile);
						dstFile.delete();
					}
					try {
						Logger.d( "BixServiceExtends", "Copy to1 == " + src + " = >" + dstFile);
						ins = context.getAssets().open("contents/" + src );
						Logger.d( "BixServiceExtends", "Copy to " + src + " = >" + dstFile);
					} catch ( Exception e){
						e.printStackTrace();
						
						File srcFile = new File( ImageUtil.ROOT_PATH + "contents/" + src );
						ins = new FileInputStream(srcFile);
						Logger.d( "BixServiceExtends", "Assets에 파일이 없어 Local에서 찾아봄 Copy to " + srcFile + " = >" + dstFile );
					}
					outs = new FileOutputStream(dstFile);
					byte[] buffer = new byte[10240];
					int length;
			    	while ((length = ins.read(buffer))>0){
			    		outs.write(buffer, 0, length);
			    	}
			    	outs.flush();
			    	outs.close();
			    	ins.close();
			    	dstPath = dstFile.getAbsolutePath();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if ( ins != null ) {
					try {
						ins.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if ( outs != null ){
					try {
						outs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return dstPath;
		}
		
	    public void ReadCard (String listener){
	    	//if ( Connect() == 0) {
	        	CradReadThread t = new CradReadThread (listener);
	        	t.start();
	    	//}
	    }
	    
	    public void ReadCard (String listener, int sec){
	    	//if ( Connect() == 0) {
	        	CradReadThread t = new CradReadThread (listener, sec);
	        	t.start();
	    	//}
	    }	    
		
		
		class CradReadThread extends Thread {
			private int retryCount = 0;
			private int waitingTime = 30; 
			private int status;
			private String listener = "";
			private String errMsg = "";
			private boolean bSuccess = false;
			
			public CradReadThread ( String listener){
				retryCount = 0;
				this.listener = listener;
			}
			
			public CradReadThread ( String listener, int sec){
				retryCount = 0;
				this.listener = listener;
			}			
			
			public void run() {
				final BaseActivity activity = (BaseActivity) ctx;
				try {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							activity.showDialog(AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
							ProgressDialog dlg = activity.getDlg();
							dlg.setMessage("Read Card");
						}
					});
					status = MsrReady();
					if(status == 0) {
						status = MsrGetData();
						if(status == 0) {
							while ( retryCount < waitingTime) {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								byte[] msr = MsrTrack2();
								if ( msr == null ){
									
								} else {
									String sss = new String (msr);
									Logger.e(TAG, "MsrTrack2:" + sss);
									
									int pos = sss.indexOf("=");
									
									String cardNum = sss.substring(0, pos );
									String cardExp = sss.substring( pos +1, pos + 5);
									String exp = cardExp.substring(2,4) + cardExp.substring(0,2);
									
									char temchar1;
									String tem_buffer = new String();
									Logger.e(TAG, "MsrTrack1:" + msr.toString()); 
									for (int i = 0; i < msr.length; i++) {
										temchar1 = (char) msr[i];
										tem_buffer = tem_buffer + " " + temchar1;
									}
									
									byte [] bCard = new byte [16];
									byte [] bExp = new byte [4];
	 								
									System.arraycopy(msr, 1, bCard, 0, 16);
									System.arraycopy(msr, 45, bExp, 0, 4);

									bSuccess = true;
									JSONObject root = new JSONObject();
									JSONObject data = new JSONObject();
									root.put("success", bSuccess);
									root.put("data", data);
									//data.put("card_number", new String (bCard));
									//data.put("expiration", new String(bExp));
									data.put("card_number", cardNum );
									data.put("expiration", exp);
									data.put("track2", sss.trim());
									
									Logger.d(TAG, "javascript:" + listener + "('"+root+"');");
									activity.webView.loadUrl("javascript:" + listener + "('"+root+"');");
									
									break;
								}
								retryCount ++;
								if ( retryCount == waitingTime){
									errMsg = "Time Over";
								}
							}
						} else {
							errMsg = "Fail to Read Card";
						}
					} else {
						errMsg = "Fail to Read Card";
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					MsrCancel();
					
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							activity.removeDialog(AbstractActivity.DIALOG_ID_PROGRESS_DEFAULT);
						}
					});
					if ( ! bSuccess ) {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								JSONObject root = new JSONObject();
								try {
									root.put("success", bSuccess);
									root.put("data", new JSONObject());
								} catch (JSONException e) {
									e.printStackTrace();
								}
								activity.webView.loadUrl("javascript:" + listener + "('"+root+"');");
								DialogUtility.alert(activity, errMsg);
							}
						});
					}
				}
			}
		}
	}
}
