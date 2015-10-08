package com.mcnc.parecis.bizmob.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mcnc.hsmart.core.log.Logger;
import com.mcnc.hsmart.util.RUtil;

public class ForegroundService extends Service {
	// FIXME NEW_PROJECT
    static final String ACTION_FOREGROUND =  "parecis.toyota.FOREGROUND";

    private static final Class<?>[] mSetForegroundSignature = new Class[] {
        boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[] {
        boolean.class};
    
    private NotificationManager mNM;
    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    
    void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (InvocationTargetException e) {
            Logger.d("ForegroundService", "Unable to invoke method : " + e );
        } catch (IllegalAccessException e) {
            Logger.d("ForegroundService", "Unable to invoke method : " + e );
        }
    }
    
    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            invokeMethod(mStartForeground, mStartForegroundArgs);
            return;
        }
        // Fall back on the old API.
        mSetForegroundArgs[0] = Boolean.TRUE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
        mNM.notify(id, notification);
    }
    
    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mStopForeground, mStopForegroundArgs);
            return;
        }
        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        mSetForegroundArgs[0] = Boolean.FALSE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }
    
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
            return;
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        try {
            mSetForeground = getClass().getMethod("setForeground",
                    mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "OS doesn't have Service.startForeground OR Service.setForeground!");
        }
    }

    @Override
    public void onDestroy() {
        // Make sure our notification is gone.
        stopForegroundCompat( RUtil.getStringR(this, "app_name"));
    }

    @Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    void handleCommand(Intent intent) {
    	try {
            if (ACTION_FOREGROUND.equals(intent.getAction())) {
                // In this sample, we'll use the same text for the ticker and the expanded notification
                CharSequence text = getText( RUtil.getStringR(this, "app_name"));
                // Set the icon, scrolling text and timestamp
                Notification notification = new Notification(  RUtil.getDrawableR(this, "app_icon"), text,
                        System.currentTimeMillis());
                // The PendingIntent to launch our activity if the user selects this notification
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, DummyActivity.class), 0);
                // Set the info for the views that show in the notification panel.
                notification.setLatestEventInfo(this, getText( RUtil.getStringR(this, "app_name") ),
                               text, contentIntent);
                startForegroundCompat( RUtil.getStringR(this, "app_name"), notification);
            }
    	} catch ( RuntimeException e) {
    		e.printStackTrace();
    	}
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
