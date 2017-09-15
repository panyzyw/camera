package com.yongyida.robot.voice.camera.track;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.yongyida.robot.voice.camera.utils.EventUtil;

/**
 * Created by pc on 2016/8/12.
 */
public class FaceTrackControl implements FootMove.OnceListener{
    private Context mContext;
    //多个人脸大小
    private int[] face_list;
    private FootMove mFootMove;
    private static boolean isContinue = true;
    public static int smallDX,totalCount;
    private Handler mHander;
    public FaceTrackControl(Context context){
        this.mContext =  context;
        mFootMove = new FootMove(mContext);
        mFootMove.setListener(this);
    }


    public void startFaceTrack(Point point,Handler handler){
        if(!isContinue){
            return;
        }else{
            isContinue = false;
                int deviationX = point.x;
                int deviationY = point.y;
                mHander = handler;
                actionLR( deviationX, deviationY);
                totalCount ++;
        }
    }


    /**
     * left and right
     * @param deviationX
     */
    private void actionLR(int deviationX, int deviationY) {
        Log.e("facetrackControl","deviationX:="+deviationX+" deviationY:="+deviationY);
        int time  = getXTime(deviationX);
        mFootMove.motorLR(time, true);
    }

    private int getXTime(int deviationX) {
        int direction  = 0;
        if(deviationX >= 0){
            direction = 1;
        }else{
            direction = -1;
        }

        int time = 0;
        int abs = Math.abs(deviationX);
        if (abs > 250) {
            smallDX = 0;
            //time =  400;
            time =  250;
        } else if (abs >= 100 && abs <= 250) {
            smallDX = 0;
            //time =  300;
            time =  150;
        } else if (abs >= 50 && abs < 100) {
            smallDX = 0;
            //time =  200;
            time =  20;
        } else if (abs >= 20 && abs < 50){
            smallDX ++;
            //time =  100;
            time =  0;
        }else if (abs < 20) {
            smallDX ++;
             time =  0 ;
        }
        return time * direction;
    }
    private int getYTime(int deviationY) {
        int direction  = 0;
        if(deviationY >= 0){
            direction = 1;
        }else{
            direction = -1;
        }

        int time = 0;
        int abs = Math.abs(deviationY);
        if (abs > 250) {
            time =  10;
        } else if (abs >= 100 && abs <= 250) {
            time =  10;
        } else if (abs >= 50 && abs < 100) {
            time =  10;
        } else if (abs >= 20 && abs < 50){
            time =  10;
        }else if (abs < 20) {
            time =  0 ;
        }
        return time * direction;
    }
    /**
     * left and right
     * @param deviationX
     */
    private void actionLR(int deviationX) {
        int time  = 1;
        time = getXTime(deviationX);
        mFootMove.motorLR(time, true);
    }


    @Override
    public void onOnceComplet() {
        Log.e("FaceTrackControl", "----onOnceComplet" );
        try {
            Message message = mHander.obtainMessage();
            message.what = EventUtil.TRACE_COUNTS;
            message.arg1 = smallDX;
            message.sendToTarget();
            Thread.sleep(700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isContinue = true;
    }
}