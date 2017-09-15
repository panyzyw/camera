package com.yongyida.robot.voice.camera.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.SpeechError;
import com.yongyida.robot.voice.camera.R;
import com.yongyida.robot.voice.camera.autophoto.CameraActivity;
import com.yongyida.robot.voice.camera.bean.CaremaBean;
import com.yongyida.robot.voice.camera.db.IntentConstant;
import com.yongyida.robot.voice.camera.exception.VoiceCameraApplication;
import com.yongyida.robot.voice.camera.utils.Constant;
import com.yongyida.robot.voice.camera.utils.TTS;
import com.yongyida.robot.voice.camera.utils.ThreadUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class CameraReceiver extends BroadcastReceiver {
	public static final String TAG = "success";
    private TTS tts;
    public static final int[] zks = new int[]{R.string.zk_1,R.string.zk_2,R.string.zk_3,R.string.zk_4,R.string.zk_5};
    public static CaremaBean caremaBean = new CaremaBean();
	@Override
	public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.e(TAG, "CameraReceiver___onReceive___" + action);
		final Context ct = context;
        if(Constant.INTENT_STOP.equals(action)){
            tts = new TTS(context);
            tts.stop();
            tts = null;
            return;
        }
		if (action.equals(IntentConstant.INTENT_CAMERA)) {
            //挚康不需要拍照功能，只需随机回答然后继续循环监听
            String isNeedCamera = VoiceCameraApplication.getSystemProperty("persist.yongyida.camera");
            if("false".equals(isNeedCamera)){
                tts = new TTS(context);
                tts.start(context.getString(zks[new Random().nextInt(5)]), new TTS.MySynthesizerListener() {
                    @Override
                    public void onCompleted(SpeechError error) {
                        Intent recycleIntent = new Intent(Constant.INTENT_RECYCLE);
                        recycleIntent.putExtra("from","camera");
                        context.sendBroadcast(recycleIntent);
                    }
                });
                return;
            }
			// 获取视频状态，若在视频下则不拍照
            String result = intent.getStringExtra("result");
            parseJson(result);
			String s = intent.getExtras().getString("video");
			if (s != null) {
				s = s.trim();
				if (s.equals("close")) {
                    checkScreenState(context);
                    ThreadUtil.getExecutor().schedule(new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(ct, CameraActivity.class);
                            intent.putExtra("isVoice",true);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
									| Intent.FLAG_ACTIVITY_CLEAR_TOP);
							ct.startActivity(intent);
						}
					}, 1000, TimeUnit.MILLISECONDS);
				}
			} else {

				return;
			}
		}
	}

    //需要加权限<uses-permission android:name="android.permission.WAKE_LOCK"/>
    private void checkScreenState(Context context){
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if(!powerManager.isScreenOn()){
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            wakeLock.acquire();
            wakeLock.release();
        }
    }

    public void parseJson(String result){
        if(TextUtils.isEmpty(result)){
            Log.e(TAG, "parseJson: result=null");
            return;
        }
        try {
            JSONObject resultObject = new JSONObject(result);
            caremaBean.service = resultObject.optString("service");
            caremaBean.text = resultObject.optString("text");
            caremaBean.rc = resultObject.optString("rc");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
