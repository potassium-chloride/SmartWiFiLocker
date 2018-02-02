package com.kostya.wifilock;
import android.app.*;
import android.os.*;
import android.widget.*;
import android.webkit.*;
import android.widget.SearchView.*;
import android.view.*;
import java.util.*;
import android.content.*;

public class HelpActivity extends Activity{
	boolean isFirstStart=false;
	long startTime;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		LinearLayout ll=new LinearLayout(this);
		isFirstStart=(getFilesDir().listFiles().length==0);
		WebView wv=new WebView(this);
		wv.loadUrl("file:///android_asset/help.html");
		wv.setPadding(10,10,10,10);
		if(!isFirstStart){
			setContentView(wv);
			return;
		}
		startTime=Calendar.getInstance().getTimeInMillis();
		Button bt=new Button(this);
		bt.setText("Я прочитал(а)");
		bt.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p){
					// TODO: Implement this method
					long cur=Calendar.getInstance().getTimeInMillis();
					if(cur-startTime>127000)finish();
					else{
						alert();
					}
				}
			});
		ll.setOrientation(ll.VERTICAL);
		ll.addView(bt);
		ll.addView(wv);
		setContentView(ll);
	}
	
	void alert(){
		AlertDialog.Builder bld=new AlertDialog.Builder(HelpActivity.this);
		bld.setTitle("Важно!");
		bld.setMessage("ВНИМАТЕЛЬНО прочтите инструкцию к приложению!");
		bld.setCancelable(false);
		bld.setNegativeButton("Да, сейчас прочту", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2){
					// TODO: Implement this method
					p1.cancel();
				}
			});
		bld.create().show();
	}

	@Override
	protected void onDestroy(){
		// TODO: Implement this method
		super.onDestroy();
		long cur=Calendar.getInstance().getTimeInMillis();
		if(isFirstStart && cur-startTime<127000){
			alert();
			startActivity(getIntent());
		}
	}
	@Override
	public void onBackPressed(){
		// TODO: Implement this method
		super.onBackPressed();
		long cur=Calendar.getInstance().getTimeInMillis();
		if(cur-startTime>127000)finish();
		else{
			alert();
		}
	}
	
}
