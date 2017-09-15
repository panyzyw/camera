package com.yongyida.robot.voice.camera.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.yongyida.robot.voice.camera.autophoto.CameraActivity;
import com.yongyida.robot.voice.camera.broadcast.CameraReceiver;

import static com.iflytek.cloud.SpeechConstant.PITCH;
import static com.iflytek.cloud.SpeechConstant.SPEED;
import static com.iflytek.cloud.SpeechConstant.VOLUME;

/**
 * Created by panyzyw on 2016/11/22.
 */

public class TTS {
    private static final String TAG="TTS";
    public static String speeker = "jiajia";
    private Context mContext;
    public SpeechSynthesizer mTts;// 语音合成器
    public SynthesizerListener mSynthesizerListener = new SynthesizerListener(){
        @Override
        public void onSpeakBegin() {}
        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {}
        @Override
        public void onSpeakPaused() {}
        @Override
        public void onSpeakResumed() {}
        @Override
        public void onSpeakProgress(int i, int i1, int i2) {}
        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {}
        @Override
        public void onCompleted(SpeechError speechError) {
            if(mySynthesizerListener!=null) {
                mySynthesizerListener.onCompleted(speechError);
            }
        }
    };

    public MySynthesizerListener mySynthesizerListener;

    public TTS(Context context){
        this.mContext = context;
        init("50","50","50","3","true","wav");
    }

    public void init(String speed,String pitch,String volume,String stream_type,String key_request_focus,String audio_format){
        mTts = SpeechSynthesizer.createSynthesizer(mContext, new InitListener() {
            @Override
            public void onInit(int i) {
                Log.e(TAG,"onInit = "+i);
            }
        });

        mTts.setParameter(SpeechConstant.PARAMS, null);
        if (speeker.equals(Constant.cloud_voice_name_xiaoai)) {
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            mTts.setParameter(SpeechConstant.VOICE_NAME, speeker);
            mTts.setParameter(SPEED, SPEED);
            mTts.setParameter(PITCH, PITCH);
            mTts.setParameter(VOLUME, VOLUME);

        } else {
            if(speeker.equals("")) {
                mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
                mTts.setParameter(SpeechConstant.VOICE_NAME, "");
            }else {
                mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
                mTts.setParameter(SpeechConstant.VOICE_NAME, speeker);
                //设置发音人资源路径
                mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());

                mTts.setParameter(SpeechConstant.SPEED, speed);
                mTts.setParameter(SpeechConstant.PITCH, pitch);
                mTts.setParameter(SpeechConstant.VOLUME, volume);
            }
        }

        mTts.setParameter(SpeechConstant.STREAM_TYPE, stream_type);
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, key_request_focus);
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, audio_format);
    }

    /**
     * 获取发音人资源路径
     */
    public String getResourcePath(){
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(mContext, ResourceUtil.RESOURCE_TYPE.assets, "tts/"+speeker+".jet"));
        return tempBuffer.toString();
    }

    public void start(String mVoiceText){
        Log.e(TAG,"start = " + mVoiceText);
        mTts.startSpeaking(mVoiceText, mSynthesizerListener);
        if(Constant.camera_end.equals(mVoiceText)){
            Util.collectInfoToServer(CameraReceiver.caremaBean, CameraActivity.allSpeakText);
        }
    }

    public void start(String mVoiceText, MySynthesizerListener mySynthesizerListener){
        this.mySynthesizerListener = mySynthesizerListener;
        mTts.startSpeaking(mVoiceText, mSynthesizerListener);
        if(Constant.camera_end.equals(mVoiceText)){
            Util.collectInfoToServer(CameraReceiver.caremaBean, CameraActivity.allSpeakText);
        }
    }

    public boolean isSpeaking(){
        return mTts.isSpeaking();
    }

    public void stop(){
        mTts.stopSpeaking();
    }

    public interface MySynthesizerListener{
        void onCompleted(SpeechError error);
    }
}
