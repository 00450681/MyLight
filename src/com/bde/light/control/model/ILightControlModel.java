package com.bde.light.control.model;

import java.util.ArrayList;

public interface ILightControlModel {

	public boolean lightStateJump(String address);
	public boolean openLock(String address);
	public boolean switchStateJump(String address);
	
	public boolean startWindowControl();
	public boolean endWindowControl();
	
	public boolean startBrightnessControl();
	public boolean setBrightness();
	public boolean endBrightnessControl();
	
	public boolean arrayStateJump(ArrayList<String> addresses);
	
}
