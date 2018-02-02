package com.kostya.wifilock;
import android.content.*;
import android.net.wifi.*;
import java.util.*;
import android.app.*;
import android.widget.*;
import android.graphics.*;
import android.view.View.*;
import android.view.*;
import java.io.*;
import android.util.*;

public class MyPoint{
	public boolean isGood=false;
	public String path="";
	public String name="";
	String CBSSID="ff:ff:ff:ff:ff:ff";
	//int Clevel=0;
	public ArrayList<String> bssids=new ArrayList<String>();
	public ArrayList<Float> levels=new ArrayList<Float>();
	public int homeLevel=50;
	public int nohomeLevel=30;
	public float sigma2=9;
	public boolean isSwitchOn=true;
	BroadcastReceiver br=null;
	public MyPoint(final Context c){
		final Dialog d=new Dialog(c);
		try{
		d.setCanceledOnTouchOutside(false);
		d.setTitle("Новая точка");
		LinearLayout lv=new LinearLayout(c);
		lv.setOrientation(lv.HORIZONTAL);
		lv.setGravity(Gravity.CENTER_VERTICAL);
		ProgressBar w=new ProgressBar(c);
		w.setPadding(10,10,5,10);
		lv.addView(w);
		TextView tv=new TextView(c);
		tv.setText("Не двигайте устройство\nСоздание снимка...");
		tv.setTextColor(Color.BLACK);
		tv.setPadding(10,10,10,10);
		lv.addView(tv);
		ImageView iv=new ImageView(c);
		iv.setBackgroundResource(android.R.drawable.ic_delete);
		iv.setPadding(8,8,8,8);
		iv.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View p1){
					// TODO: Implement this method
					try{c.unregisterReceiver(br);}catch(Exception e){}
					d.cancel();
					Toast.makeText(c,"Отменено",Toast.LENGTH_LONG).show();
				}
			});
		lv.addView(iv);
		lv.setPadding(10,10,10,10);
		d.setContentView(lv);
		d.show();
		final WifiManager wfm=(WifiManager)c.getSystemService(c.WIFI_SERVICE);
		if(!wfm.isWifiEnabled())wfm.setWifiEnabled(true);
		final MyPoint cache=this;
		
		br=new BroadcastReceiver(){
			int netcount=0;
			ArrayList<Integer> curcount=new ArrayList<Integer>();
			@Override
			public void onReceive(Context p1, Intent p2){
				// TODO: Implement this method
				WifiInfo wi=wfm.getConnectionInfo();
				if(wi!=null)if(!wi.getBSSID().equals("02:00:00:00:00:00")){
					name=wi.getSSID();
					CBSSID=wi.getBSSID();
					//Clevel=wi.getRssi();
				}
				List<ScanResult> lsc=wfm.getScanResults();
				if(lsc.size()<3){
					d.cancel();
					Toast.makeText(c,"Недостаточно сетей для создания точек",Toast.LENGTH_LONG).show();
				}
				for(int i=0;i<lsc.size();i++){
					String ss=lsc.get(i).SSID;
					if(
					ss.contains("Phone")||ss.contains("Pad")||ss.contains("Android")||ss.toLowerCase().contains("mobile")
					)continue;
					if(lsc.get(i).level<-78 && lsc.size()>3)continue;
					if(lsc.get(i).level<-73 && lsc.size()>5)continue;
					if(!bssids.contains(lsc.get(i).BSSID)){
						bssids.add(lsc.get(i).BSSID);
						levels.add((float)lsc.get(i).level);
						curcount.add(1);
					}else{
						int pos=bssids.indexOf(lsc.get(i).BSSID);
						float curlev=levels.get(pos);
						curlev=curlev*curcount.get(pos)+(float)lsc.get(i).level;
						curcount.set(pos,curcount.get(pos)+1);
						levels.set(pos,curlev/curcount.get(pos));
					}
				}
				if(netcount>3){
				try{c.unregisterReceiver(br);}catch(Exception e){}
				d.cancel();
				isGood=true;
				if(oncl!=null)oncl.onComplite(cache);
				MyList.MyDialog doset=new MyList.MyDialog(c,cache);
				doset.desc.setText("Число точек: "+bssids.size());
				if(name.length()>0)doset.desc.append("\nSSID: "+name);
				doset.show();
				}else{
					netcount++;
					wfm.startScan();
				}
			}
		};
		c.registerReceiver(br,new IntentFilter(wfm.SCAN_RESULTS_AVAILABLE_ACTION));
		wfm.startScan();
		}catch(Exception e){
			Toast.makeText(c,e.toString(),Toast.LENGTH_LONG).show();
			Toast.makeText(c,e.toString(),Toast.LENGTH_LONG).show();
			Toast.makeText(c,e.toString(),Toast.LENGTH_LONG).show();
		}
	}
	public MyPoint(String filePath) throws FileNotFoundException, IOException{
		path=filePath;
		String txt="";
		FileReader fr=new FileReader(path);
		int c;
		while((c=fr.read())!=-1){
			txt+=(char)c;
		} 
		fr.close();
		String[] lines=txt.split("\n");
		for(int i=0;i<lines.length;i++){
			if(lines[i].indexOf("Name:")==0)name=lines[i].substring(5).replaceAll("\n","");
			else if(lines[i].indexOf("BSSID:")==0)CBSSID=lines[i].substring(6).replaceAll("\n","");
			else if(lines[i].indexOf("nohomelvel:")==0)nohomeLevel=Integer.valueOf(lines[i].substring("nohomelvel:".length()).replaceAll("\n",""));
			else if(lines[i].indexOf("homelevel:")==0)homeLevel=Integer.valueOf(lines[i].substring("homelevel:".length()).replaceAll("\n",""));
			else if(lines[i].indexOf("sigma2:")==0)sigma2=Float.valueOf(lines[i].substring("sigma2:".length()).replaceAll("\n",""));
			else if(lines[i].indexOf("isSwitchOn:")==0)isSwitchOn=(lines[i].substring("isSwitchOn:".length()).indexOf("1")>-1);
			else if(lines[i].indexOf("bssids:")==0){
				String tmp=lines[i].substring("bssids:".length()).replaceAll("\n","");
				String[] tmp2=tmp.split(",");
				bssids=new ArrayList<String>();
				for(int j=0;j<tmp2.length;j++)bssids.add(tmp2[j].replaceAll(",",""));
			}
			else if(lines[i].indexOf("levels:")==0){
				String tmp=lines[i].substring("levels:".length()).replaceAll("\n","");
				String[] tmp2=tmp.split(",");
				levels=new ArrayList<Float>();
				for(int j=0;j<tmp2.length;j++)levels.add(Float.valueOf(tmp2[j].replaceAll(",","")));
			}
		}
		isGood=true;
		if(oncl!=null)oncl.onComplite(this);
	}
	public float compare(WifiInfo wi,List<ScanResult> lsc){
		//Чем больше, тем лучше
		//100 -- идеальное совпадение
		//0 -- мимо вообще
		//-1 принципиальное несовпадение
		if(!CBSSID.equals("ff:ff:ff:ff:ff:ff") && wi!=null)
			if(!CBSSID.equals(wi.getBSSID()))
				return -1;
		float sum=0;
		int N=0,N2=0;
		float l1,l2;
		String ss;
		for(int i=0;i<lsc.size();i++){
			if(!bssids.contains(lsc.get(i).BSSID))continue;
			l1=levels.get(bssids.indexOf(lsc.get(i).BSSID));
			l2=lsc.get(i).level;
			
			ss=lsc.get(i).SSID;
			if(!
				(ss.contains("Phone")||ss.contains("Pad")||ss.contains("Android")||ss.toLowerCase().contains("mobile"))
			   )if(!(lsc.get(i).level<-78 && lsc.size()>3) && !(lsc.get(i).level<-73 && lsc.size()>5))N2++;
			
			N++;
			sum+=Math.exp((l1-l2)*(l2-l1)/2/(sigma2+(Math.abs(l1+l2)-90)/5));
		}
		N2=bssids.size();
		float effN=(N2+N+Math.min(N2,N))/3;
		float res=(float)(sum/effN*100*Math.exp( (N-N2)*(N2-N)/60));
		Log.d("RuntimeWiFiLock","Compared:"+name+" -- "+res+"; home="+homeLevel+"; nohome="+nohomeLevel);
		return res;
	}
	public void save(String filePath) throws IOException{
		path=filePath;
		FileWriter fw=new FileWriter(path);
		fw.append("Name:"+name+"\n");
		fw.append("BSSID:"+CBSSID+"\n");
		fw.append("nohomelvel:"+nohomeLevel+"\n");
		fw.append("homelevel:"+homeLevel+"\n");
		fw.append("sigma2:"+sigma2+"\n");
		fw.append("isSwitchOn:"+(isSwitchOn?"1":"0")+"\n");
		String tmp="";
		for(int i=0;i<bssids.size();i++){
			tmp+=bssids.get(i)+",";
		}
		fw.append("bssids:"+tmp+"\n");
		tmp="";
		for(int i=0;i<bssids.size();i++){
			tmp+=levels.get(i)+",";
		}
		fw.append("levels:"+tmp+"\n");
		fw.close();
	}
	boolean lastIsInHome=false;
	public boolean isInHome(WifiInfo wi,List<ScanResult> lsc){
		if(!isSwitchOn)return false;
		float comp=compare(wi,lsc);
		if(comp<nohomeLevel){
			lastIsInHome=false;
			return false;
		}else if(comp>homeLevel){
			lastIsInHome=true;
			return true;
		}
		return lastIsInHome;
	}
	onCompleteListener oncl=null;
	public void setOnCompleteListener(onCompleteListener listener){
		oncl=listener;
		if(isGood)oncl.onComplite(this);
	}
	public static abstract class onCompleteListener{
		public onCompleteListener(){}
		public abstract void onComplite(MyPoint p);
	}
}
