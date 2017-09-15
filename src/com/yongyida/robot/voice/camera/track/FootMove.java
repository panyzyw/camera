package com.yongyida.robot.voice.camera.track;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import static com.yongyida.robot.voice.camera.track.MotorControl.mMotorController;
import static com.yongyida.robot.voice.camera.track.MotorControl.motorService;


public class FootMove {
	Context mContext = null;
	boolean isMotorBusyLR = false;
	private OnceListener mListener;
	public FootMove(Context context) {
		mContext = context;
	}

	private void bindMotorService(){
		Intent intent = new Intent();
		intent.setAction("com.yongyida.robot.MotorService");
		intent.setPackage("com.yongyida.robot.motorcontrol");
		mContext.bindService(intent,motorService, Context.BIND_AUTO_CREATE);
	}

	public void setListener(OnceListener listener){
		this.mListener = listener;
	}
	public interface OnceListener{
		void onOnceComplet();
	}
	public void motorLR(int n , boolean isOnce) {

		int sleep = Math.abs(n);
		if (n < 0){
			Log.e("motorleft", "----" + sleep);
			try {
                if(mMotorController!=null){
                    mMotorController.left(sleep);
                }

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
			Log.e("motorleftright", "----" + sleep);
			try {
                if(mMotorController!=null) {
                    mMotorController.right(sleep);
                }
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		if(mListener != null){
			if(isOnce){
				mListener.onOnceComplet();
			}
		}
	}



}
