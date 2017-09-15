package com.yongyida.robot.voice.camera.entitf;

import android.hardware.Camera.Size;

public class FitSize {
	private Size size;
	private int dif;
	public FitSize(Size size, int dif) {
		super();
		this.size = size;
		this.dif = dif;
	}
	public Size getSize() {
		return size;
	}
	public void setSize(Size size) {
		this.size = size;
	}
	public int getDif() {
		return dif;
	}
	public void setDif(int dif) {
		this.dif = dif;
	}
	
}
