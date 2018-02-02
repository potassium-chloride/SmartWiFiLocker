package com.kostya.wifilock;
import android.app.*;
import android.os.*;
import android.content.*;
import java.util.*;
import java.io.*;
import android.net.wifi.*;
import android.util.*;
import android.hardware.*;

public class BackgroundService extends Service{
	@Override
	public IBinder onBind(Intent p1){return null;}
	static int timeout=23000;
	Intent myInt;
	ArrayList<MyPoint> points;
	static MyPoint activePoint;
	static WifiInfo lastwi;
	static List<ScanResult> lastScanRes;
	boolean isActiveNossid=false;
	AdminUtils util;
	WifiManager wfm;
	WifiManager.WifiLock wfl;
	static boolean isFirstStart=true;
	boolean isLocked=true;
	boolean batterySave=false;
	long lastCall=0;
	BroadcastReceiver receiver=new BroadcastReceiver(){
		@Override
		public void onReceive(Context p1, Intent p2){
			// TODO: Implement this method
			String act=p2.getAction();
			if(act.equals(p1.getPackageName()+".UPDATE_LIST")){
				isActiveNossid=false;
				initArray();
				act=Intent.ACTION_TIME_TICK;
			}
			if(act.equals(Intent.ACTION_BATTERY_LOW)){
				batterySave=true;
				util.enableKeyguard();
			}else if(act.equals(Intent.ACTION_BATTERY_OKAY)){
				batterySave=false;
				act=Intent.ACTION_TIME_TICK;
			}
			if(!wfm.isWifiEnabled()){
				if(!isLocked || isFirstStart){
					util.enableKeyguard();
					isLocked=true;
					isFirstStart=false;
				}
				return;
			}
			WifiInfo wi=wfm.getConnectionInfo();
			lastwi=wi;
			if(!isActiveNossid && (wi.getBSSID().equals("02:00:00:00:00:00")||wi.getBSSID().equals("")))return;
			if(act.equals(Intent.ACTION_TIME_TICK)||act.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)||act.equals(Intent.ACTION_SCREEN_OFF)){
				long curCall=Calendar.getInstance().getTimeInMillis();
				if(curCall-lastCall>timeout){
					if(!wfl.isHeld())wfl.acquire();
					wfm.startScan();
					lastCall=curCall;
				}
			}else if(act.equals(wfm.SCAN_RESULTS_AVAILABLE_ACTION)){
				List<ScanResult> lsc=wfm.getScanResults();
				if(wfl.isHeld())wfl.release();
				lastScanRes=lsc;
				if(activePoint!=null)if(activePoint.isInHome(wi,lsc)){
					if(isLocked || isFirstStart){
						util.disableKeyguard();
						isLocked=false;
						isFirstStart=false;
					}
					return;
				}
				for(int i=0;i<points.size();i++)
				if(points.get(i).isInHome(wi,lsc)){
						activePoint=points.get(i);
						if(isLocked || isFirstStart){
							util.disableKeyguard();
							isLocked=false;
							isFirstStart=false;
						}
						return;
					}
				if(!isLocked || isFirstStart){
					util.enableKeyguard();
					isLocked=true;
					isFirstStart=false;
				}
			}else if(act.equals(Intent.ACTION_SHUTDOWN)){
			//	if(!isLocked){
					util.enableKeyguard();
			//		isLocked=true;
			//	}
			}
		}
	};
	
	void initArray(){
		points=new ArrayList<MyPoint>();
		File[] pfiles=getFilesDir().listFiles();
		String pth,tmp;
		MyPoint tmpp;
		for(int i=0;i<pfiles.length;i++){
			pth=pfiles[i].getAbsolutePath();
			try{
				if(pth.indexOf("/point_") > 0 && pth.indexOf(".txt") > 0){
					tmpp=new MyPoint(pth);
					if(!tmpp.isSwitchOn)continue;
					points.add(tmpp);
					tmp=points.get(points.size()-1).CBSSID;
					if(tmp.equals("")||tmp.equals("02:00:00:00:00:00")||tmp.equals("ff:ff:ff:ff:ff:ff"))isActiveNossid=true;
				}
			}catch(IOException e){}
		}
		activePoint=null;
	}
	
	AccelHooker ah;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		// TODO: Implement this method
		myInt=intent;
		AdminUtils.isservicestart=true;
		util=new AdminUtils(BackgroundService.this);
		wfm=(WifiManager)getSystemService(WIFI_SERVICE);
		wfl=wfm.createWifiLock(wfm.WIFI_MODE_SCAN_ONLY,"WiFiLocker");
		PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);
		batterySave=pm.isPowerSaveMode();
		initArray();
		timeout=(int)(getTimeout()*1000);
		IntentFilter filt=new IntentFilter();
		filt.addAction(getPackageName()+".UPDATE_LIST");
		filt.addAction(Intent.ACTION_TIME_TICK);
		filt.addAction(Intent.ACTION_SCREEN_OFF);
		filt.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filt.addAction(wfm.SCAN_RESULTS_AVAILABLE_ACTION);
		filt.addAction(Intent.ACTION_SHUTDOWN);
		filt.addAction(Intent.ACTION_BATTERY_LOW);
		filt.addAction(Intent.ACTION_BATTERY_OKAY);
		registerReceiver(receiver,filt);
		receiver.onReceive(this,new Intent(Intent.ACTION_TIME_TICK));
		ah = new AccelHooker(this){
			@Override
			public void onHook(){
				// TODO: Implement this method
				receiver.onReceive(BackgroundService.this,new Intent(Intent.ACTION_TIME_TICK));
			}
		};
		Log.d("RuntimeWiFiLock","Service started");
		return super.onStartCommand(intent, flags, startId);
	}
	boolean isOff=false;

	@Override
	public void onDestroy(){
		// TODO: Implement this method
		super.onDestroy();
		if(!isOff){
			Log.d("RuntimeWiFiLock","Service stoped");
			util.enableKeyguard();
			unregisterReceiver(receiver);
			startService(myInt);
			isOff=true;
		}
	}

	@Override
	public void onTaskRemoved(Intent rootIntent){
		// TODO: Implement this method
		super.onTaskRemoved(rootIntent);
		if(!isOff){
			Log.d("RuntimeWiFiLock","Service stoped");
			util.enableKeyguard();
			unregisterReceiver(receiver);
			startService(myInt);
			isOff=true;
		}
	}
	public float getTimeout(){
		try{
			FileReader fr=new FileReader(getFilesDir().getAbsolutePath()+"/mintimeout");
			int cc;
			String res="";
			while((cc=fr.read())!=-1){
				res+=(char)cc;
			}
			float out=Float.valueOf(res);
			if(out>5000)out=23;
			return out;
		}catch(Exception e){}
		return 23;
	}
	
	public static abstract class AccelHooker{
		Sensor accel;
		int size=50,cur=0;
		float[] Gs=new float[size];
		float lastdisp=150,lastmean=150;
		public AccelHooker(Context c){
			SensorManager sensMan=(SensorManager)c.getSystemService(c.SENSOR_SERVICE);
			accel=sensMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensMan.registerListener(new SensorEventListener(){
					@Override
					public void onSensorChanged(SensorEvent p1){
						// TODO: Implement this method
						float X=p1.values[0];
						float Y=p1.values[1];
						float Z=p1.values[2];
						float g=(float)Math.sqrt(X*X+Y*Y+Z*Z);
						if(g>15)return;
						if(cur<size){
							Gs[cur]=g;
							cur++;
							return;
						}
						float disp=0;//,dispY=0,dispZ=0;
						float mean=0;//,meanY=0,meanZ=0;
						for(int i=0;i<size-1;i++){
							disp+=Gs[i]*Gs[i];
							//dispY+=Ys[i]*Ys[i];
							//dispZ+=Zs[i]*Zs[i];
							mean+=Gs[i];
							//meanY+=Ys[i];
							//meanZ+=Zs[i];
							Gs[i]=Gs[i+1];
							//Ys[i]=Ys[i+1];
							//Zs[i]=Zs[i+1];
						}
						Gs[size-1]=g;
						//Ys[size-1]=Y;
						//Zs[size-1]=Z;
						mean+=g;
						//meanY+=Y;
						//meanZ+=Z;
						disp+=g*g;
						//dispY+=Y*Y;
						//dispZ+=Z*Z;
						mean/=size;
						//meanY/=size;
						//meanZ/=size;
						disp=disp/size-mean;
						//dispY=dispY/size-meanY;
						//dispZ=dispZ/size-meanZ;
						//float disp=dispX+dispY+dispZ;
						//float mean=(float)Math.sqrt(meanX*meanX+meanY*meanY+meanZ*meanZ);
						if(Math.abs(lastdisp-disp)>2 || Math.abs(lastmean-mean)>2){
							Log.d("RuntimeWifiAccel","disp="+disp+", mean="+mean);
							onHook();
						}
						lastdisp=disp;
						lastmean=mean;
					}
					@Override
					public void onAccuracyChanged(Sensor p1, int p2){}
				}, accel, sensMan.SENSOR_DELAY_NORMAL);
		}
		public abstract void onHook();
	}
}
