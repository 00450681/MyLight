package com.bde.light.activity;

import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class LaunchActivity extends Activity implements Runnable {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.launch_activity);
		
		Random random=new Random();
        int launcheTime=random.nextInt(2000)+1000;
		Handler handler = new Handler();
		handler.postDelayed(this, launcheTime);
	}
	
	@Override
	public void run() {
		Intent intent = new Intent(this,MainActivity.class);
		startActivity(intent);
		finish();
	}

}
