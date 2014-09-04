package com.bde.light.activity;

import com.bde.light.fragment.BaseFragment;
import com.bde.light.fragment.BaseFragment.Operation;
import com.bde.light.fragment.EndInitializeFragment;
import com.bde.light.fragment.StartInitializeFragment;
import com.bde.light.model.Light;
import com.bde.light.service.BleService;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class InitActivity extends Activity {
	
	private Light myLight;
	private ServiceConnection onService;
	private BleService mService;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init_window_layout);
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			myLight = (Light) bundle.getSerializable(Light.LIGHT);
		}
		
		onService = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder rawBinder) {
                mService = ((BleService.LocalBinder) rawBinder).getService();
                if (mService != null) {
                	//mService.scan(true);
                    mService.setActivityHandler(mHandler);
                    if (myLight != null) {
                    	mService.setMyLight(myLight);
                    	mService.setOperation(BleService.WINDOW);
                    	mService.connect(myLight.address);
                    }
                }
            }
            
            public void onServiceDisconnected(ComponentName classname) {
                mService = null;
            }
        };
        startService(new Intent(this, BleService.class));
        Intent bindIntent = new Intent(this, BleService.class);
        bindService(bindIntent, onService, Context.BIND_AUTO_CREATE);
		
		
		
		if (findViewById(R.id.fragment_container) != null) {
			
		    // However, if we're being restored from a previous state,
		    // then we don't need to do anything and should return or else
		    // we could end up with overlapping fragments.
		    if (savedInstanceState != null) {
		        return;
		    }
		
		    // Create an instance of ExampleFragment
		    StartInitializeFragment firstFragment = new StartInitializeFragment();
		    
		    // In case this activity was started with special instructions from an Intent,
		    // pass the Intent's extras to the fragment as arguments
		    firstFragment.setArguments(getIntent().getExtras());
		    
		    FragmentTransaction transaction = getFragmentManager().beginTransaction();
		    
		    // Add the fragment to the 'fragment_container' FrameLayout
		    //transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_in);
		    transaction.add(R.id.fragment_container, firstFragment).commit();
		    
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		mService.disconnect();
        unbindService(onService);
        finish();
	}
		
	private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BleService.WINDOW_CONNECT:
            	/*upBtn.setEnabled(true);
            	downBtn.setEnabled(true);
            	confirmBtn.setEnabled(true);*/
                break;
            case BleService.WINDOW:
            	finish();
            	break;
            case BleService.WINDOW_RESULT:
            	byte []buffer = msg.getData().getByteArray(BleService.RESULT);
            	/*switch (buffer[0]) {
            	case 0x24:
            		
            		break;
            	case 0x25:
            		break;
            	}*/
            	break;
            case BleService.WINDOW_FINISH:
            	/*if (isConfirm) {
            		Intent intent = new Intent();
    				intent.putExtra(Light.LIGHT, myLight);
    				intent.setClass(StartInitActivity.this, EndInitActivity.class);
    				startActivityForResult(intent, 0x01);
                	//finish();
            	}*/
            	
            	break;
            }
        }
	};
	
	BaseFragment.Operation operation = new Operation() {
		
		@Override
		public void sendOperation(byte[] data, int type) {
			// TODO Auto-generated method stub
			if (mService != null) {
				mService.write(data, BleService.MY_TIMER_CHARACTERISTIC);
			}
			switch (type) {
			case BaseFragment.TYPE_UP:
				break;
			case BaseFragment.TYPE_DOWN:
				break;
			case BaseFragment.TYPE_START:
				// replace fragment
			    EndInitializeFragment secondFragment = new EndInitializeFragment();

			    secondFragment.setArguments(getIntent().getExtras());
			    
			    FragmentTransaction transaction = getFragmentManager().beginTransaction();

			    //transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_in);
			    transaction.replace(R.id.fragment_container, secondFragment).commit();
			    
				break;
			case BaseFragment.TYPE_END:
				// finish
				finish();
				break;
			}
		}
	};
}
