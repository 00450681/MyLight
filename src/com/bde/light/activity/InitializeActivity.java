package com.bde.light.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bde.light.model.Light;

public class InitializeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enter_init_fragment_layout);
		
		/*if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create an instance of ExampleFragment
            EnterInitializeFragment firstFragment = new EnterInitializeFragment();
            
            // In case this activity was started with special instructions from an Intent,
            // pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());
            
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            
            // Add the fragment to the 'fragment_container' FrameLayout
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_in);
            transaction.add(R.id.fragment_container, firstFragment).commit();
        }*/

		Button bt_back = (Button) findViewById(R.id.bt_back);
		bt_back.setText(R.string.setting);
		bt_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Bundle bundle = getIntent().getExtras();
				if (bundle != null) {
					Light myLight = (Light) bundle.getSerializable(Light.LIGHT);
					intent.putExtra(Light.LIGHT, myLight);
				}
				
				intent.setClass(InitializeActivity.this, StartInitActivity.class);
				//intent.setClass(InitializeActivity.this, InitActivity.class);
				startActivityForResult(intent, 1);
			}
		});
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		switch(requestCode) {
		case 1:
			finish();
			break;
		}
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	
}
