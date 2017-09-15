package com.yongyida.robot.voice.camera.utils;

import android.content.Context;

import com.yongyida.robot.voice.camera.R;

/**
 * Created by panyzyw on 2016/11/22.
 */

public class Constant {
    //发音人
    public static String cloud_voice_name_xiaoai = "aisxa";
    public static String local_voice_name_jiajia = "jiajia";
    public static final String INTENT_RECYCLE = "com.yydrobot.RECYCLE";
    //停止语音广播
    public static final String INTENT_STOP = "com.yydrobot.STOP";

    //拍照提示音
    public static String camera_weclome;
    public static String camera_hint;
    public static String camera_start;
    public static String camera_end;
    public static String camera_timeout;
    public static String camera_nodetectface;
    public static String camera_three;
    public static String camera_two;
    public static String camera_one;
    private static Constant instance;
    private Constant(Context context){
        cloud_voice_name_xiaoai = context.getString(R.string.cloud_voice_name_xiaoai);
        local_voice_name_jiajia = context.getString(R.string.local_voice_name_jiajia);
        camera_weclome=context.getString(R.string.camera_welcome);
        camera_hint = context.getString(R.string.camera_hint);
        camera_start = context.getString(R.string.camera_start);
        camera_end = context.getString(R.string.camera_end);
        camera_timeout = context.getString(R.string.camera_timeout);
        camera_three = context.getString(R.string.camera_three);
        camera_two = context.getString(R.string.camera_two);
        camera_one = context.getString(R.string.camera_one);
        camera_nodetectface = context.getString(R.string.camera_nodetectface);
    }
    public static void initConstant(Context context){
        if( instance == null ){
            instance = new Constant(context);
        }
    }
}
