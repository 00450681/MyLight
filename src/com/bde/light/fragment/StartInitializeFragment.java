package com.bde.light.fragment;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bde.light.activity.R;
import com.bde.light.service.BleService;
import com.bde.light.utils.NumConversion;

public class StartInitializeFragment extends BaseFragment {

	Button upBtn, downBtn, startBtn;
	private int mId;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		//return super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.start_init_fragment_layout, container, false);
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		upBtn = (Button) this.getView().findViewById(R.id.up);
		downBtn = (Button) this.getView().findViewById(R.id.down);
		startBtn = (Button) this.getView().findViewById(R.id.confirm);
		
		upBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte []data = NumConversion.int2LittleEndianByteArray16(300);
				ByteArrayBuffer bab = new ByteArrayBuffer(data.length + 1);
				
				bab.append(0x22);
				bab.append(data, 0, data.length);
				/*if (mService != null) {
					mService.write(bab.buffer(), BleService.MY_TIMER_CHARACTERISTIC);
				}*/
				Operation operation = getOperation();
				if (operation != null) {
					operation.sendOperation(bab.buffer(), TYPE_UP);
				}
			}
		});
		
		downBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte []data = NumConversion.int2LittleEndianByteArray16(300);
				ByteArrayBuffer bab = new ByteArrayBuffer(data.length + 1);
				
				bab.append(0x23);
				bab.append(data, 0, data.length);
				/*if (mService != null) {
					mService.write(bab.buffer(), BleService.MY_TIMER_CHARACTERISTIC);
				}*/
				Operation operation = getOperation();
				if (operation != null) {
					operation.sendOperation(bab.buffer(), TYPE_DOWN);
				}
			}
		});
		
		startBtn.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Operation operation = getOperation();
			if (operation != null) {
				operation.sendOperation(new byte[]{0x28}, TYPE_START);
			}
		}
		});
	}
}
