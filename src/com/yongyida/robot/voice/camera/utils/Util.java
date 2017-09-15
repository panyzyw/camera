package com.yongyida.robot.voice.camera.utils;

import android.content.Intent;
import android.graphics.Matrix;
import android.util.Log;

import com.yongyida.robot.voice.camera.bean.CaremaBean;

import org.json.JSONException;
import org.json.JSONObject;

import static com.yongyida.robot.voice.camera.exception.VoiceCameraApplication.mAppContext;

public class Util {
    public static final String APP_NAME = "YYDRobotCamera";
    public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
            int viewWidth, int viewHeight) {
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
    }

    public static void collectInfoToServer(CaremaBean bean, String answer){
        String info = "";
        if(bean != null ){
            try {
                JSONObject infoJsonObject = new JSONObject();
                infoJsonObject.put("semantic","");
                infoJsonObject.put("service",bean.service);
                infoJsonObject.put("operation","");
                infoJsonObject.put("text",bean.text);
                infoJsonObject.put("answer",answer);
                info = infoJsonObject.toString();
                Log.i("collectInfoToServer:", info);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            Log.i("collectInfoToServer:", "bean=null");
        }
        Intent intent = new Intent("com.yongyida.robot.COLLECT");
        intent.putExtra("collect_result",info);
        intent.putExtra("collect_from",APP_NAME);
        mAppContext.sendBroadcast(intent);
        Log.i("collectInfoToServer:", "com.yongyida.robot.COLLECT");
    }
}
