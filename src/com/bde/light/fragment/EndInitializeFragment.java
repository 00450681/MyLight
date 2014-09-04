package com.bde.light.fragment;

import org.apache.http.util.ByteArrayBuffer;

import com.bde.light.activity.R;
import com.bde.light.fragment.BaseFragment.Operation;
import com.bde.light.utils.NumConversion;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class EndInitializeFragment extends BaseFragment {
	Button upBtn, downBtn, endtBtn;
	private int mId;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//return super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.end_init_fragment_layout, container, false);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		upBtn = (Button) this.getView().findViewById(R.id.up);
		downBtn = (Button) this.getView().findViewById(R.id.down);
		endtBtn = (Button) this.getView().findViewById(R.id.confirm);
		
		upBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Operation operation = getOperation();
				if (operation != null) {
					operation.sendOperation(new byte[]{0x20}, TYPE_UP);
				}
			}
		});
		
		downBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Operation operation = getOperation();
				if (operation != null) {
					operation.sendOperation(new byte[]{0x21}, TYPE_DOWN);
				}
			}
		});
		
		endtBtn.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Operation operation = getOperation();
			if (operation != null) {
				operation.sendOperation(new byte[]{0x29}, TYPE_END);
			}
		}
		});
	}
}
