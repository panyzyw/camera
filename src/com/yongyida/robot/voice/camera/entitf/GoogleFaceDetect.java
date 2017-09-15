package com.yongyida.robot.voice.camera.entitf;


import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yongyida.robot.voice.camera.utils.EventUtil;

public class GoogleFaceDetect implements FaceDetectionListener {
	private static final String TAG = "GoogleFaceDetect";
	private Context mContext;
	private Handler mHander;
	public GoogleFaceDetect(Context c, Handler handler){
		mContext = c;
		mHander = handler;
	}
	@Override
	public void onFaceDetection(Face[] faces, Camera camera) {
		// TODO Auto-generated method stub
		
		Log.e(TAG, "onFaceDetection...");
		if(faces != null){
			Message m = mHander.obtainMessage();
			m.what = EventUtil.UPDATE_FACE_RECT;
			m.obj = faces;
			m.sendToTarget();
		}
	}


}
