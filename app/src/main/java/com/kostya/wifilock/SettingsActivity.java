package com.kostya.wifilock;
import android.app.*;
import android.os.*;
import java.io.*;
import android.widget.*;
import android.view.View.*;
import android.view.*;
import android.net.wifi.*;
import android.content.*;
import java.util.*;
import android.app.admin.*;
import android.text.*;
import android.graphics.*;
import android.widget.TextView.*;

public class SettingsActivity extends Activity{
	TextView tv2;
	int method=0;
	Button nbt;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		LinearLayout ll=new LinearLayout(this);
		ll.setOrientation(ll.VERTICAL);
		
		TextView tv1=new TextView(this);
		tv1.setText("Способ вкл/выкл блокировки.");//\n\"Общий\" работает на большинстве устройств до Android 5 включительно и не требует прав администратора, при очистке ОЗУ вызывает появление экрана блокировки.\n\"Администраторский\" требует прав администратора, работает на устройствах с Android 6.0 и выше, но непонятно как, когда и при каких условиях.\n\"Механический\" с помощью прав администратора механически меняет пароль на устройстве. Работает до версии Android O.");
		tv1.setTextColor(Color.BLACK);
		ll.addView(tv1);
		
		tv2=new TextView(this);
		try{
			FileReader fr=new FileReader(getFilesDir().toString()+"/keyguardmethod");
			char cc=(char)fr.read();
			method=Integer.valueOf(cc+"");
			fr.close();
		}catch(Exception e){}
		if(method==0)tv2.setText("Текущий: Общий");
		else if(method==1)tv2.setText("Текущий: Администраторский 1");
		else if(method==2)tv2.setText("Текущий: Администраторский 2");
		ll.addView(tv2);
		
		Button change=new Button(this);
		change.setText("Сменить метод");
		change.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					// TODO: Implement this method
					final Dialog d=new Dialog(SettingsActivity.this);
					d.setTitle("Метод блокировки");
					LinearLayout dll=new LinearLayout(SettingsActivity.this);
					dll.setOrientation(dll.VERTICAL);
					
					TextView var0=new TextView(SettingsActivity.this);
					var0.setText("Общий");
					var0.setPadding(15,15,15,15);
					var0.setTextColor(Color.BLACK);
					var0.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View p1){
								// TODO: Implement this method
								(new AdminUtils(SettingsActivity.this)).setMethodNum(0);
								tv2.setText("Текущий: Общий");
								d.cancel();
							}
						});
					dll.addView(var0);
					
					TextView var1=new TextView(SettingsActivity.this);
					var1.setText("Администраторский 1");
					var1.setPadding(15,15,15,15);
					var1.setTextColor(Color.BLACK);
					var1.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View p1){
								// TODO: Implement this method
								(new AdminUtils(SettingsActivity.this)).setMethodNum(1);
								tv2.setText("Текущий: Администраторский 1");
								d.cancel();
							}
						});
					dll.addView(var1);
					
					TextView var2=new TextView(SettingsActivity.this);
					var2.setText("Администраторский 2");
					var2.setPadding(15,15,15,15);
					var2.setTextColor(Color.BLACK);
					var2.setOnClickListener(new OnClickListener(){
							@Override
							public void onClick(View p1){
								// TODO: Implement this method
								(new AdminUtils(SettingsActivity.this)).setMethodNum(2);
								tv2.setText("Текущий: Администраторский 2");
								d.cancel();
								if(getPasswd().length()<3)setPasswd(true);
								nbt.setVisibility(nbt.VISIBLE);
							}
						});
					dll.addView(var2);
					dll.setPadding(15,15,15,15);

					d.setContentView(dll);
					d.show();
				}
			});
		ll.addView(change);
		
		Button adminon=new Button(this);
		adminon.setText("Сделать администратором устройства");
		adminon.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					// TODO: Implement this method
					Intent i=new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,new ComponentName(SettingsActivity.this, AdminRec.class));
					try{
						startActivity(i);
					}catch(Exception e){}
				}
			});
		ll.addView(adminon);
		
		
		nbt=new Button(this);
		nbt.setText("Сменить пароль");
		nbt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					// TODO: Implement this method
					setPasswd(false);
				}
			});
		ll.addView(nbt);
		if(method<2){
			nbt.setVisibility(nbt.GONE);
		}
		
		TextView tmout=new TextView(this);
		tmout.setText("Минимальное время между сканированием WiFi(секунды)\nСлишком маленькое значение может сильно расходовать батарею");
		tmout.setTextColor(Color.BLACK);
		ll.addView(tmout);
		
		EditText mint=new EditText(this);
		mint.setInputType(InputType.TYPE_CLASS_NUMBER);
		mint.setText(String.valueOf(getTimeout()));
		mint.setOnKeyListener(new OnKeyListener(){
				@Override
				public boolean onKey(View p1, int p2, KeyEvent p3){
					// TODO: Implement this method
					try{setTimeout(Float.valueOf(((EditText)p1).getText().toString().replace(",",".")));}catch(Exception e){}
					return false;
				}
		});
		ll.addView(mint);
		
		Button hlp=new Button(this);
		hlp.setText("Справка");
		hlp.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					// TODO: Implement this method
					startActivity(new Intent(SettingsActivity.this,HelpActivity.class));
				}
			});
		ll.addView(hlp);
		
		setContentView(ll);
	}
	public String getPasswd(){
		String passwd="";
		try{
			FileReader fr=new FileReader(getFilesDir().toString()+"/passwd");
			int cc;
			while((cc=fr.read())!=-1){
				passwd+=(char)cc;
			}
			passwd=passwd.replaceAll("\n","");
			fr.close();
		}catch(Exception e){}
		return passwd;
	}
	public void setPasswd(boolean isNessary){
		final Dialog sd=new Dialog(SettingsActivity.this);
		sd.setCancelable(!isNessary);
		sd.setCanceledOnTouchOutside(!isNessary);
		sd.setTitle("Введите свой пароль");
		LinearLayout pwdll=new LinearLayout(this);
		pwdll.setOrientation(pwdll.VERTICAL);
		
		final EditText pass0=new EditText(this);
		if(AdminUtils.passwd.length()>0){
			TextView tv0=new TextView(this);
			tv0.setText("Ваш старый пароль:");
			pwdll.addView(tv0);
			pass0.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
			pwdll.addView(pass0);
		}
		
		TextView tv1=new TextView(this);
		tv1.setText("Ваш новый пароль:");
		pwdll.addView(tv1);
		final EditText pass1=new EditText(this);
		pass1.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
		pwdll.addView(pass1);
		
		TextView tv2=new TextView(this);
		tv2.setText("Ваш новый пароль (подтверждение):");
		pwdll.addView(tv2);
		final EditText pass2=new EditText(this);
		pass2.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
		pwdll.addView(pass2);
		
		Button ok=new Button(this);
		ok.setText("Сохранить");
		ok.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1){
					// TODO: Implement this method
					if(AdminUtils.passwd.length()>0){
						String pwd0=pass1.getText().toString().replace("\n","");
						if(!AdminUtils.passwd.equals(pwd0)){
							Toast.makeText(SettingsActivity.this,"Неверен старый пароль!",Toast.LENGTH_LONG).show();
							return;
						}
					}
					String pwd1=pass1.getText().toString().replace("\n","");
					String pwd2=pass2.getText().toString().replace("\n","");
					if(!pwd1.equals(pwd2)){
						Toast.makeText(SettingsActivity.this,"Пароли не совпадают!",Toast.LENGTH_LONG).show();
						return;
					}else{
						try{
							FileWriter fw=new FileWriter(SettingsActivity.this.getFilesDir().toString()+"/passwd");
							fw.append(pwd1);
							fw.close();
							AdminUtils.passwd=pwd1;
							Toast.makeText(SettingsActivity.this,"Записано успешно",Toast.LENGTH_LONG).show();
							sd.setCancelable(true);
							sd.cancel();
						}catch (IOException e){}
					}
				}
			});
		pwdll.addView(ok);
		sd.setContentView(pwdll);
		sd.show();
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
	public void setTimeout(float t){
		try{
			FileWriter fw=new FileWriter(getFilesDir().getAbsolutePath()+"/mintimeout");
			fw.append(t+"");
			fw.close();
			BackgroundService.timeout=(int)(t*1000);
		}catch(Exception e){}
	}
}
