package com.bde.light.fragment;

import android.app.Fragment;

public class BaseFragment extends Fragment {

	private Operation mOperation;
	public static final int TYPE_UP = 1;
	public static final int TYPE_DOWN = 2;
	public static final int TYPE_START = 3;
	public static final int TYPE_END = 4;
	public void setOperation(Operation operation) {
		mOperation = operation;
	}
	protected Operation getOperation() {
		return mOperation;
	}
	public interface Operation {
		public void sendOperation(byte []data, int type);
	}
}
