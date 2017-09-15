package com.yongyida.robot.voice.camera.autophoto;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Face;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;

import com.iflytek.cloud.SpeechError;
import com.yongyida.robot.voice.camera.R;
import com.yongyida.robot.voice.camera.entitf.GoogleFaceDetect;
import com.yongyida.robot.voice.camera.preview.CameraSurfaceView;
import com.yongyida.robot.voice.camera.preview.CircleImageView;
import com.yongyida.robot.voice.camera.preview.FaceView;
import com.yongyida.robot.voice.camera.track.FaceCenter;
import com.yongyida.robot.voice.camera.track.FaceTrackControl;
import com.yongyida.robot.voice.camera.track.MotorControl;
import com.yongyida.robot.voice.camera.utils.Constant;
import com.yongyida.robot.voice.camera.utils.DisplayUtil;
import com.yongyida.robot.voice.camera.utils.EventUtil;
import com.yongyida.robot.voice.camera.utils.FileUtil;
import com.yongyida.robot.voice.camera.utils.MediaPlayBiz;
import com.yongyida.robot.voice.camera.utils.TTS;
import com.yongyida.robot.voice.camera.utils.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import static com.yongyida.robot.voice.camera.track.MotorControl.motorService;

@SuppressWarnings("unused")
public class CameraActivity extends Activity implements CameraInterface.StratCameraCallback,
            FileUtil.SaveCompleteListener,FaceView.FaceLocationCallback{

    private final String TAG="CameraActivity";

    private CameraSurfaceView surfaceView = null;

    public static List<String> imagePathList;

    private CountDownTimer mTimer;

    private float previewRate = 1f;

    private static ImageView mImageView;

    public static CircleImageView mivAlbum;

    private ImageView mivTakePicture;

    private ImageView mivCountDown;

    private static CameraInterface camera;

    private boolean isDestroy = false;

    private int time = 1000;

    private boolean mIsVoice;

    private static CameraActivity instance;

    static ScheduledThreadPoolExecutor executor;

    public boolean isCamera = false;

    private static MediaPlayBiz mPlayer;

    private static final String ACTION = "com.yydrobot.STOP";

    FaceView faceView;
    GoogleFaceDetect googleFaceDetect = null;
    Point p;
    private MainHandler mMainHandler = null;
    private FaceTrackControl faceTrackControl;
    private FaceCenter faceCenter;
    private int trace_counts;
    private boolean isFaceTrack = true;
    private TTS tts;
    private boolean isDectect;
    private boolean isBindMotorService;
    public static String allSpeakText = "";
    private static Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what){
                case 0:
                    Intent intent = new Intent();
                    intent.setAction("com.yongyida.action.lockscreen.ACTION_NORMAL");
                    instance.sendBroadcast(intent);
                    instance.finish();
                    break;
                case 1:
                    if(imagePathList.size()==0){
                        mivAlbum.setVisibility(View.INVISIBLE);
                    }else{
                        if((boolean)msg.obj){
                            mivAlbum.setVisibility(View.VISIBLE);
                            mivAlbum.setImageBitmap(FileUtil.getDiskBitmap(imagePathList.get(0)));
                        }
                    }

                    break;
            }
		}
	};

    @Override
    public void startCameraFace() {
        isFaceTrack = true;
        googleFaceDetect = new GoogleFaceDetect(getApplicationContext(), mMainHandler);
        mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STARTED_PREVIEW, 500);
        mIsVoice=getIntent().getBooleanExtra("isVoice",false);
        if(mIsVoice&&!isCamera){
            doPhotograph();
            isCamera=true;
        }
    }
    @Override
    public void onFaceLocation(RectF[] mFaces) {
        if(!isFaceTrack) return;
        if(mFaces != null && mFaces.length != 0){
            faceCenter.startCalcCenter(mFaces);
            faceTrackControl.startFaceTrack( faceCenter.getFaceXY(),mMainHandler);
        }
    }

    @Override
    public void onSaveComplete() {
        isCamera = false;
        getImageList(false);//重新获取image path列表
		if(camera.getCameraDevice()!=null)
           camera.getCameraDevice().startPreview();
        handler.sendEmptyMessageDelayed(0, 60000);//拍照60秒后无反应，退出拍照界面
    }

    @Override
    public void onShowImage(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mivAlbum!=null){
                    mivAlbum.setVisibility(View.VISIBLE);
                    mivAlbum.setImageBitmap(bitmap);
                }
                mivCountDown.setVisibility(View.INVISIBLE);
            }
        });
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case EventUtil.UPDATE_FACE_RECT:
                    isDectect = true;
                    Face[] faces = (Face[]) msg.obj;
                    faceView.setFaces(faces);
                    break;
                case EventUtil.CAMERA_HAS_STARTED_PREVIEW:
                    startGoogleFaceDetect();
                    break;
                case EventUtil.TRACE_COUNTS:
                    trace_counts = msg.arg1;
                    break;
            }
            super.handleMessage(msg);
        }

    }

    public static void actionStart(Context sontext) {
        Intent intent = new Intent(sontext, CameraActivity.class);
        Log.d("success", "public static void actionStart(Context sontext)");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sontext.startActivity(intent);
    }

    @Override
    protected void onPause() {
        isCamera = false;
        isDestroy = true;
        if (mPlayer != null) {
            mPlayer.stopMusic();
            mPlayer = null;
        }
        if (camera != null) {
            camera.doStopCamera();
        }
        if(tts!=null){
            tts.stop();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        instance.finish();
        unregisterReceiver(myReceiver);
        if(isBindMotorService){
            isBindMotorService = false;
            MotorControl.unBinderMotorService(instance);
        }
        tts.stop();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        getImageList(true);
        super.onResume();
    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION)) {
                CameraActivity.this.finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        tts = new TTS(CameraActivity.this);
        instance = this;
        Intent intent=new Intent();
        intent.setAction("com.yongyida.robot.MotorService");
        intent.setPackage("com.yongyida.robot.motorcontrol");
        this.bindService(intent, motorService, Context.BIND_AUTO_CREATE);
        isBindMotorService = true;
        FileUtil.setSaveListener(this);
        isDestroy = false;
        FaceView.setFaceCallback(this);
        camera = CameraInterface.getInstance(this);
        mPlayer = MediaPlayBiz.getInstance();
        executor = ThreadUtil.getExecutor();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);
        registerReceiver(myReceiver, filter);
        faceTrackControl =  new FaceTrackControl(this);
        faceCenter = new FaceCenter(this);
        mMainHandler = new MainHandler();
        camera.doStartFace(this);
        init();
    }

    private void init() {
        surfaceView = (CameraSurfaceView) findViewById(R.id.camera_textureview);
        surfaceView.setAlpha(1.0f);
        faceView = (FaceView) findViewById(R.id.face_view);

        mImageView = (ImageView) findViewById(R.id.camera_imageView);
        mivAlbum=(CircleImageView) findViewById(R.id.iv_album);
        mivTakePicture=(ImageView) findViewById(R.id.iv_take_picture);
        mivCountDown=(ImageView) findViewById(R.id.iv_count_down);

        LayoutParams params = surfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = p.x;
        params.height = p.y;
        /* 默认全屏的比例预览 */
        previewRate = DisplayUtil.getScreenRate(this);
        surfaceView.setLayoutParams(params);

        mivTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isCamera){
                    doPhotograph();
                    isCamera=true;
                }
            }
        });

        mivAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cn = new ComponentName("com.yongyida.robot.photos", "com.yongyida.robot.photos.ui.ImagePagerActivity");
                try {
                    intent.setComponent(cn);
                    for(int i=0;i<imagePathList.size();i++){
                        imagePathList.set(i,"file://"+imagePathList.get(i));
                    }
                    intent.putStringArrayListExtra("image_urls",(ArrayList<String>) imagePathList);
                    startActivity(intent);
                } catch (Exception e) {
                }

            }
        });
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                playWelcome();//YYD-tianchunming-add-20170727 for 增加小帅酷拍欢迎提示音
            }
        },500);
    }

    private void takePicture() {
        camera.doTakePicture();
        mMainHandler.sendEmptyMessageDelayed(EventUtil.CAMERA_HAS_STOP_PREVIEW, 1500);
    }

    private void startGoogleFaceDetect() {
        Camera.Parameters params = CameraInterface.getInstance(this).getCameraParams();
        if (params != null) {
            if (params.getMaxNumDetectedFaces() > 0) {
                if (faceView != null) {
                    faceView.clearFaces();
                    faceView.setVisibility(View.VISIBLE);
                }
                CameraInterface.getInstance(this).getCameraDevice().setFaceDetectionListener(googleFaceDetect);
                CameraInterface.getInstance(this).getCameraDevice().startFaceDetection();
            }
        }
    }

    public void stopGoogleFaceDetect() {
        try {
            Camera.Parameters params = CameraInterface.getInstance(this).getCameraParams();
            CameraInterface.getInstance(this).getCameraDevice().setFaceDetectionListener(null);
            CameraInterface.getInstance(this).getCameraDevice().stopFaceDetection();
            if (faceView != null) {
                faceView.clearFaces();
                faceView.setVisibility(View.GONE);
            }
            faceView.clearFaces();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void doPhotograph(){
        handler.removeMessages(0);//拍照60秒内又重新有拍照的操作，清除之前发送的延时关闭消息
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                if(!isDectect){
                    playNoDectectFace();
                }else{
                    if(trace_counts >= 2){
                        playStart();
                    }else {
                        playHint();
                    }
                }
            }
        },3000, TimeUnit.MILLISECONDS);
    }

    private void playNoDectectFace(){ //没检测到人脸
        allSpeakText += Constant.camera_nodetectface;
        tts.start(Constant.camera_nodetectface, new TTS.MySynthesizerListener() {
            @Override
            public void onCompleted(SpeechError error) {
                try {
                    Thread.sleep(4000);
                    if(isDectect){
                        if(trace_counts >= 2){
                            playStart();
                        }else {
                            playHint();
                        }
                    }else{
                        playTimeout();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void playHint(){
            allSpeakText += Constant.camera_hint;
	        tts.start(Constant.camera_hint, new TTS.MySynthesizerListener() {
            @Override
            public void onCompleted(SpeechError error) {
                try {
                    Thread.sleep(3000);
                    if(trace_counts >= 3){
                        playStart();
                    }else{
                        playTimeout();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void playStart(){
        allSpeakText += Constant.camera_start;
                tts.start(Constant.camera_start, new TTS.MySynthesizerListener() {
            @Override
            public void onCompleted(SpeechError error) {
                playThree();
                playEnd();
            }
        });
    }

    private void playThree(){
        Log.e("doPhotograph","doPhotograph=playThree");
        allSpeakText += Constant.camera_three;
        tts.start(Constant.camera_three, null);
        setCountDown(R.drawable.iv_3);
/*        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setCountDown(R.drawable.iv_2);
                allSpeakText += Constant.camera_two;
                tts.start(Constant.camera_two, null);
            }
        },800);
/*        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setCountDown(R.drawable.iv_1);
                allSpeakText += Constant.camera_one;
                tts.start(Constant.camera_one, null);
                isFaceTrack = false;
                stopGoogleFaceDetect();
                isDectect = false;
                camera.doTakePicture();
            }
        },1600);

/*        try {
            mivCountDown.setImageResource(R.drawable.iv_1);
            Thread.sleep(800);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

    }

    private void playWelcome(){
        allSpeakText += Constant.camera_weclome;
        tts.start(Constant.camera_weclome);
    }

    private void playEnd(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setCountDown(R.drawable.iv_ok);
                allSpeakText += Constant.camera_end;
                tts.start(Constant.camera_end);
            }
        },2400);
    }

    private void playTimeout(){
        allSpeakText += Constant.camera_timeout;
        tts.start(Constant.camera_timeout, new TTS.MySynthesizerListener() {
            @Override
            public void onCompleted(SpeechError error) {
                playThree();
                playEnd();
            }
        });
    }

    public void onBackBtClk(View v){
        finish();
    }

    private void getImageList(final boolean isShowImg){
        new Thread(new Runnable() {
            @Override
            public void run() {
                imagePathList=new FileUtil().getImagePathFromSD("PhotosCamera");
                Message message=new Message();
                message.obj=isShowImg;
                message.what=1;
                handler.sendMessage(message);
            }
        }).start();
    }

    private void setCountDown(int resId){
        if(mIsVoice){
            mivCountDown.setVisibility(View.VISIBLE);
            mivCountDown.setImageResource(resId);
        }
    }

}
