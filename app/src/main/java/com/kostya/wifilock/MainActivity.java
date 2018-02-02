package com.kostya.wifilock;

import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;
import android.app.admin.*;
import android.view.*;
import java.util.*;
import android.view.View.*;
import android.text.*;

public class MainActivity extends Activity 
{
	public Context mc;
	boolean isAuth=false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		mc=MainActivity.this;
        setContentView(R.layout.main);
		if(getFilesDir().listFiles().length==0)startActivity(new Intent(MainActivity.this,HelpActivity.class));
		MyAt at=new MyAt();
		at.execute();
		
		try{
			KeyguardManager km=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
			Intent i=km.createConfirmDeviceCredentialIntent(null,"Введите свой пароль, чтобы получить доступ к приложению");
			i.setAction("android.app.action.CONFIRM_DEVICE_CREDENTIAL");
			startActivityForResult(i,1);
		}catch(Exception e){
			new AdminUtils(MainActivity.this);
			if(AdminUtils.passwd.length()>0){
				final Dialog d=new Dialog(MainActivity.this);
				d.setTitle("Введите пароль");
				d.setCanceledOnTouchOutside(false);
				d.setCancelable(false);
				LinearLayout pwdll=new LinearLayout(this);
				pwdll.setOrientation(pwdll.VERTICAL);
				TextView tv0=new TextView(this);
				tv0.setText("Введите свой пароль, чтобы получить доступ к приложению");
				pwdll.addView(tv0);
				final EditText pass0=new EditText(this);
				pass0.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
				pwdll.addView(pass0);
				Button bt=new Button(this);
				bt.setText("OK");
				bt.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View p1){
							// TODO: Implement this method
							if(pass0.getText().toString().equals(AdminUtils.passwd)){
								isAuth=true;
								d.setCancelable(true);
								d.cancel();
							}else{
								Toast.makeText(MainActivity.this,"Пароли не совпадают",Toast.LENGTH_LONG).show();
							}
						}
					});
				pwdll.addView(bt);
				d.setContentView(pwdll);
				d.show();
			}
		}
		IntentFilter inf=new IntentFilter();
		inf.addAction(getPackageName()+".UPDATE_LIST");
		registerReceiver(mybr,inf);
		startService(new Intent(MainActivity.this,BackgroundService.class));
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO: Implement this method
		//super.onActivityResult(requestCode, resultCode, data);
		if(resultCode!=RESULT_OK){
			Toast.makeText(this,"Вы должны сначала ввести пароль",Toast.LENGTH_LONG).show();
			Toast.makeText(this,"Вы должны сначала ввести пароль",Toast.LENGTH_LONG).show();
			finish();
		}else isAuth=true;
	}
	BroadcastReceiver mybr=new BroadcastReceiver(){
		@Override
		public void onReceive(Context p1, Intent p2){
			// TODO: Implement this method
			MyAt at=new MyAt();
			at.execute();
		}
	};
	class MyAt extends AsyncTask<Void,Void,Void>{
		LinearLayout ll;
		@Override
		protected Void doInBackground(Void[] p1){
			// TODO: Implement this method
			MyList lv=new MyList(MainActivity.this);
			Button bt=new Button(MainActivity.this);
			bt.setText("Настройки");
			ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
			bt.setLayoutParams(lp);
			bt.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View p1){
						// TODO: Implement this method
						startActivity(new Intent(MainActivity.this, SettingsActivity.class));
					}
				});

			Button bt2=new Button(MainActivity.this);
			bt2.setText("Добавить точку");
			bt2.setLayoutParams(lp);
			bt2.setOnClickListener(new View.OnClickListener(){
					@Override
					public void onClick(View p1){
						// TODO: Implement this method
						MyPoint p=new MyPoint(MainActivity.this);
						p.setOnCompleteListener(new MyPoint.onCompleteListener(){
								@Override
								public void onComplite(MyPoint p)
								{
									// TODO: Implement this method
									try{
										p.save(getApplicationContext().getFilesDir().toString()+"/point_"+Calendar.getInstance().getTimeInMillis()+".txt");
										Intent i=new Intent();
										i.setAction(getPackageName()+".UPDATE_LIST");
										getApplicationContext().sendBroadcast(i);
									}catch(Exception e){
										Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
										Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
									}
								}
							});
					}
				});

			ll=new LinearLayout(MainActivity.this);
			ll.setOrientation(ll.VERTICAL);
			ll.addView(bt);
			ll.addView(bt2);
			TextView tv=new TextView(MainActivity.this);
			tv.setText("Сохранённые точки:");
			try{
				tv.append("\nАктивная точка: "+BackgroundService.activePoint.name);
				tv.append("\nУверенность в ней: "+BackgroundService.activePoint.compare(BackgroundService.lastwi,BackgroundService.lastScanRes));
			}catch(Exception e){}
			ll.addView(tv);
			ll.addView(lv);
			return null;
		}
		@Override
		protected void onPostExecute(Void result){
			// TODO: Implement this method
			super.onPostExecute(result);
			setContentView(ll);
		}
	}
	@Override
	protected void onDestroy(){
		// TODO: Implement this method
		super.onDestroy();
		unregisterReceiver(mybr);
	}
}
