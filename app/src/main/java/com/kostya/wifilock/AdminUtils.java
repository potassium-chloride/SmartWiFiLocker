package com.kostya.wifilock;
import android.content.*;
import android.app.*;
import android.app.admin.*;
import java.io.*;
import android.util.*;

public class AdminUtils{
	Context mc;
	KeyguardManager km;
	KeyguardManager.KeyguardLock kml;
	DevicePolicyManager dpm;
	ComponentName cn;
	static int method;
	static String passwd;
	static int errlevel=0;
	static boolean isservicestart=false;
	public AdminUtils(Context c){
		mc=c;
		km=(KeyguardManager)c.getSystemService(c.KEYGUARD_SERVICE);
		kml=km.newKeyguardLock("WiFiLocker");
		method=0;
		passwd="";
		try{
			FileReader fr=new FileReader(c.getFilesDir().toString()+"/keyguardmethod");
			char cc=(char)fr.read();
			method=Integer.valueOf(cc+"");
			fr.close();
			Log.d("RuntimeWiFiLock","Method set to "+method);
		}catch(Exception e){
			Log.d("RuntimeWiFiLock",e.toString());
			e.printStackTrace();
		}
		if(method>0){
			dpm=(DevicePolicyManager)c.getSystemService(c.DEVICE_POLICY_SERVICE);
			cn=new ComponentName(c,AdminRec.class);
		}
		if(method>1){
			try{
				FileReader fr=new FileReader(c.getFilesDir().toString()+"/passwd");
				int cc;
				while((cc=fr.read())!=-1){
					passwd+=(char)cc;
				}
				passwd=passwd.replaceAll("\n","");
				fr.close();
			}catch(Exception e){}
		}
	}
	public void enableKeyguard(){
		boolean res=true;
		try{
		if(method==0){
			try{kml.reenableKeyguard();}catch(Exception e){}
		}else if(method==1){
			res=dpm.setKeyguardDisabled(cn,false);
		}else if(method==2){
			res=dpm.resetPassword(passwd,0);
		}else if(method==3){
			byte[] token={1,2,3,37,48,6};
			//dpm.setResetPasswordToken(cn,token);
			//dpm.resetPasswordWithToken();
		}
			if(!res){
				if(errlevel>1)setMethodNum(0);
				errlevel++;
			}else Log.d("RuntimeWiFiLock","Keyguard enabled by method "+method);
		}catch(Exception e){
			if(errlevel>1)setMethodNum(0);
			errlevel++;
			e.printStackTrace();
		}
	}
	public void disableKeyguard(){
		boolean res=true;
		try{
			if(method==0){
				try{
					kml.disableKeyguard();
					km.exitKeyguardSecurely(null);
				}catch(Exception e){}
			}else if(method==1){
				res=dpm.setKeyguardDisabled(null,true);
			}else if(method==2){
				res=dpm.resetPassword("",0);
			}else if(method==3){
				byte[] token={1,2,3,37,48,6};
				//dpm.setResetPasswordToken(null,token);
				//dpm.resetPasswordWithToken();
			}
			if(!res){
				if(errlevel>1)setMethodNum(0);
				errlevel++;
			}else Log.d("RuntimeWiFiLock","Keyguard disabled by method "+method);
		}catch(Exception e){
			if(errlevel>1)setMethodNum(0);
			errlevel++;
			e.printStackTrace();
		}
	}
	public void setMethodNum(int n){
		try{
			method=n;
			if(n<2)passwd="";
			FileWriter fw=new FileWriter(mc.getFilesDir().toString()+"/keyguardmethod");
			fw.append(n+"");
			fw.close();
			Log.d("RuntimeWiFiLock","Method set to "+method);
			BackgroundService.isFirstStart=true;
		}catch (Exception e){}
	}
}
