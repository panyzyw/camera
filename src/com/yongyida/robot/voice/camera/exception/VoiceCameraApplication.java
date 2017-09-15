package com.yongyida.robot.voice.camera.exception;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.SpeechUtility;
import com.yongyida.robot.voice.camera.R;
import com.yongyida.robot.voice.camera.utils.Constant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by pc on 2016/7/20.
 */
public class VoiceCameraApplication extends Application{
    public static Context mAppContext;
    @Override
    public void onCreate() {
        super.onCreate();
        //配置程序异常退出处理
        Thread.setDefaultUncaughtExceptionHandler(new LocalFileHandler(this));
        mAppContext = getApplicationContext();
        Constant.initConstant(this);

        //讯飞id
        SpeechUtility.createUtility(this, "appid=" + getString(R.string.appid));
    }

    public static String getSystemProperty(String propertyName) {
        Class<?> clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method method = clazz.getDeclaredMethod("get", String.class);
            return (String) method.invoke(clazz.newInstance(), propertyName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return "";
    }
}
