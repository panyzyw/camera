package com.yongyida.robot.voice.camera.track;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.yongyida.robot.motorcontrol.MotorController;

public class MotorControl {
    public static MotorController mMotorController;

    public static MotorController getInstance() {
        if (mMotorController == null) {
            Log.e("MotorControl", "绑定服务实例化失败");
        }
        return mMotorController;
    }

    public static ServiceConnection motorService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            Log.e("MotorControl", "onServiceDisconnected");
            mMotorController = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            Log.e("MotorControl", "onServiceConnected");
            mMotorController = MotorController.Stub.asInterface(service);
            try {
                mMotorController.setDrvType(0);
                mMotorController.setSpeed(30);
                Log.d("binder", "+++++++++++++>");
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    };

    public static void unBinderMotorService(Context context) {
        if (mMotorController == null) {
            return;
        } else {
            context.unbindService(motorService);
        }
    }

}
