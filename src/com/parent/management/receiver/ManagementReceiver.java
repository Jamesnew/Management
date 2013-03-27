package com.parent.management.receiver;

import java.util.Iterator;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parent.management.ManagementApplication;
import com.parent.management.service.CommunicationService;
import com.parent.management.service.MonitorService;

public class ManagementReceiver extends BroadcastReceiver {

    public static boolean isServiceRunning(String paramString, Context context)
    {
        Iterator<RunningServiceInfo> mIterator = ((ActivityManager)context.
                getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(5000).iterator();
      
        while (mIterator.hasNext()) {
            RunningServiceInfo si = (RunningServiceInfo) mIterator.next();
            if (si.service.getClassName().equals(paramString)) {
                return true;
            }
        }
        return false;
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
	    ManagementApplication.monitorList.clear();
	    
	    if (isServiceRunning("com.parent.management.service.CommunicationService", context)) {
	        Log.d("ManagementReceiver", "----> stop communication service");
	        context.stopService(new Intent(context, CommunicationService.class));
	    }
        Log.d("ManagementReceiver", "----> starting communication service");
        context.startService(new Intent(context, CommunicationService.class));
        
        if (isServiceRunning("com.parent.management.service.MonitorService", context)) {
            Log.d("ManagementReceiver", "----> stop monitor service");
            context.stopService(new Intent(context, MonitorService.class));
        }
        Log.d("ManagementReceiver", "----> starting monitor service");
	    context.startService(new Intent(context, MonitorService.class));
	}

}