package com.kostya.wifilock;
import android.widget.*;
import android.content.*;
import android.view.*;
import android.app.*;
import android.graphics.*;
import java.util.*;
import java.io.*;
import android.widget.SeekBar.*;
import android.view.ViewGroup.*;

public class MyList extends ListView{
	static public boolean isLongPress=false;
	ArrayList<MyPoint> points;
	String[] arr;
	Context mc;
	MyDialog md;
	public MyList(Context c){
		super(c);
		points=new ArrayList<MyPoint>();
		mc=c;
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
		File[] pfiles=c.getFilesDir().listFiles();
		String pth;
		for(int i=0;i<pfiles.length;i++){
			pth=pfiles[i].getAbsolutePath();
			try{
				if(pth.indexOf("/point_") > 0 && pth.indexOf(".txt") > 0)points.add(new MyPoint(pth));
			}catch(IOException e){}
		}//*/
		arr=new String[points.size()];
		for(int i=0;i<points.size();i++){
			arr[i]=points.get(i).name+(points.get(i).isSwitchOn?"":"(off)");
		}
		ArrayAdapter adapt=new ArrayAdapter<>(c,android.R.layout.simple_list_item_1,arr);
		setAdapter(adapt);
		setOnItemClickListener(new AdapterView.OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4){
					// TODO: Implement this method
					md=new MyDialog(mc,points.get(p3));
					if(isLongPress){
						isLongPress=false;
						return;
					}
					md.show();
				}
			});
		setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
				@Override
				public boolean onItemLongClick(AdapterView<?> p1, View p2, final int p3, long p4){
					// TODO: Implement this method
					isLongPress=true;
					if(md!=null)try{md.cancel();}catch(Exception e){}
					String t=p1.getItemAtPosition(p3).toString();
					AlertDialog.Builder ad=new AlertDialog.Builder(mc);
					ad.setTitle(t);
					ad.setMessage("Удалить эту точку?");
					ad.setPositiveButton("Удалить", new AlertDialog.OnClickListener(){
							@Override
							public void onClick(DialogInterface p1, int p2){
								// TODO: Implement this method
								try{
									File tmp=new File(points.get(p3).path);
									tmp.delete();
									Intent i=new Intent();
									i.setAction(mc.getPackageName()+".UPDATE_LIST");
									mc.sendBroadcast(i);
								}catch(Exception e){}
							}
						});
					ad.setCancelable(true);
					ad.setNegativeButton("Отмена",null);
					ad.show();
					return false;
				}
			});
	}
	static public class MySeekBar extends LinearLayout{
		boolean isSigma=false;
		SeekBar bar;
		TextView out;
		public MySeekBar(Context c){
			super(c);
			setOrientation(HORIZONTAL);
			out=new TextView(c);
			out.setTextColor(Color.BLACK);
			LayoutParams lp1=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			out.setLayoutParams(lp1);
			bar=new SeekBar(c);
			LayoutParams lp2=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			bar.setLayoutParams(lp2);
			bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
					@Override
					public void onProgressChanged(SeekBar p1, int p2, boolean p3){
						// TODO: Implement this method
						if(isSigma){
							out.setText(""+(float)(Math.round(Math.sqrt(p1.getProgress()/5+4)*10))/10.0);
						}else{
							out.setText(""+p1.getProgress());
						}
					}
					@Override
					public void onStartTrackingTouch(SeekBar p1){}
					@Override
					public void onStopTrackingTouch(SeekBar p1){}
				});
			addView(out);
			addView(bar);
			setGravity(Gravity.CENTER_VERTICAL);
		}
		public void setMax(int m){
			bar.setMax(m);
		}
		public void setProgress(int m){
			if(isSigma){
				out.setText(""+(float)(Math.round(Math.sqrt(bar.getProgress()/5+4)*10))/10.0);
			}else{
				out.setText(""+bar.getProgress());
			}
			bar.setProgress(m);
		}
		public int getProgress(){
			return bar.getProgress();
		}
	}
	static public class MyCheckTextView extends LinearLayout{
		CheckBox cb;
		TextView tv;
		public MyCheckTextView(Context c){
			super(c);
			setOrientation(HORIZONTAL);
			setPadding(15,15,15,15);
			tv=new TextView(c);
			tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
			tv.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1){
						// TODO: Implement this method
						cb.setChecked(!cb.isChecked());
					}
				});
			tv.setTextColor(Color.BLACK);
			cb=new CheckBox(c);
			addView(cb);
			addView(tv);
		}
		public void setChecked(boolean c){
			cb.setChecked(c);
		}
		public void setText(String s){
			tv.setText(s);
		}
		public boolean isChecked(){
			return cb.isChecked();
		}
	}
	static public class MyDialog extends Dialog{
		LinearLayout mView;
		public EditText name;
		public MySeekBar home,nohome,sigma;
		public TextView desc;
		Context mc;
		public MyDialog(final Context c, final MyPoint myp){
			super(c);
			mc=c;
			mView=new LinearLayout(c);
			mView.setOrientation(mView.VERTICAL);
			mView.setPadding(20,20,20,20);
			
			setTitle(myp.name);
			setCancelable(true);
			
			name=new EditText(c);
			name.setHint("Имя точки");
			name.setText(myp.name);
			ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
			name.setLayoutParams(lp);
			mView.addView(name);
			
			TextView tv1=new TextView(c);
			tv1.setText("Порог доверия:");
			tv1.setLayoutParams(lp);
			tv1.setPadding(5,5,5,5);
			tv1.setTextColor(Color.BLACK);
			mView.addView(tv1);
			
			home=new MySeekBar(c);
			home.setMax(100);
			home.setProgress(myp.homeLevel);
			home.setLayoutParams(lp);
			mView.addView(home);
			
			TextView tv2=new TextView(c);
			tv2.setText("Порог потери доверия:");
			tv2.setLayoutParams(lp);
			tv2.setPadding(5,5,5,5);
			tv2.setTextColor(Color.BLACK);
			mView.addView(tv2);
			
			nohome=new MySeekBar(c);
			nohome.setMax(100);
			nohome.setProgress(myp.nohomeLevel);
			nohome.setLayoutParams(lp);
			mView.addView(nohome);
			
			TextView tv3=new TextView(c);
			tv3.setText("Квадрат дисперсии сигнала:");
			tv3.setLayoutParams(lp);
			tv3.setTextColor(Color.BLACK);
			mView.addView(tv3);
			
			sigma=new MySeekBar(c);
			sigma.isSigma=true;
			sigma.setMax(100);
			sigma.setProgress((int)(5*(myp.sigma2-4)));
			sigma.setLayoutParams(lp);
			mView.addView(sigma);
			
			final MyCheckTextView ison=new MyCheckTextView(c);
			ison.setText("Учитывать");
			ison.setChecked(myp.isSwitchOn);
			mView.addView(ison);
			
			desc=new TextView(c);
			desc.setText("Число точек: "+myp.levels.size());
			desc.setPadding(5,5,5,5);
			desc.setTextColor(Color.BLACK);
			mView.addView(desc);
			
			LinearLayout endlv=new LinearLayout(c);
			endlv.setOrientation(endlv.HORIZONTAL);
			
			Button cbt=new Button(c);
			cbt.setText("Закрыть");
			//cbt.setBackgroundResource(android.R.attr.buttonBarButtonStyle);
			cbt.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View p1){
						// TODO: Implement this method
						cancel();
					}
			});
			cbt.setGravity(Gravity.LEFT);
			endlv.addView(cbt);
			
			Button sbt=new Button(c);
			sbt.setText("Сохранить");
			//sbt.setBackgroundResource(android.R.attr.buttonBarButtonStyle);
			sbt.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View p1){
						// TODO: Implement this method
						try{
							myp.name=name.getText().toString();
							myp.homeLevel=home.getProgress();
							myp.nohomeLevel=nohome.getProgress();
							myp.sigma2=sigma.getProgress()/5+4;
							myp.isSwitchOn=ison.isChecked();
							if(myp.path.length()<2)myp.path=c.getFilesDir().toString()+"/point_"+Calendar.getInstance().getTimeInMillis()+".txt";
							myp.save(myp.path);
							onsave();
						}catch (IOException e){
							Toast.makeText(mc,"Сохранено",Toast.LENGTH_LONG).show();
						}
					}
				});
			sbt.setGravity(Gravity.RIGHT);
			endlv.addView(sbt);
			mView.addView(endlv);
			setContentView(mView);
		}
		public void onsave(){
			cancel();
			Intent i=new Intent();
			i.setAction(mc.getPackageName()+".UPDATE_LIST");
			mc.sendBroadcast(i);
			try{
				mc.startService(new Intent(mc,BackgroundService.class));
			}catch(Exception e){}
			Toast.makeText(mc,"Сохранено",Toast.LENGTH_LONG).show();
		}
	}
}
