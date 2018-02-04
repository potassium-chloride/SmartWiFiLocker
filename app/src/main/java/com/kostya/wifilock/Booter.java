package com.kostya.wifilock;
import android.content.*;
import android.util.*;
import android.app.*;

public class Booter extends BroadcastReceiver{
	@Override
	public void onReceive(Context p1, Intent p2){
		// TODO: Implement this method
		AdminUtils au=new AdminUtils(p1);
		if(!p2.getAction().equals(Intent.ACTION_USER_PRESENT)||!au.isservicestart){
			if(au.method>1 && au.passwd.length()>0)try{
				//au.dpm.isActivePasswordSufficient();
				au.enableKeyguard();
				au.dpm.lockNow();
				Log.d("WiFiLockerRuntime","Boot completed. Enable keyguard");
			}catch(Exception e){}
		}
		p1.startService(new Intent(p1, BackgroundService.class));
	}
	
}
