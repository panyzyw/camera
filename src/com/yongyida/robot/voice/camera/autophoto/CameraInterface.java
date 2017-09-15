package com.yongyida.robot.voice.camera.autophoto;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;

import com.yongyida.robot.voice.camera.preview.CameraSurfaceView;
import com.yongyida.robot.voice.camera.utils.CameraUtils;
import com.yongyida.robot.voice.camera.utils.DisplayUtil;
import com.yongyida.robot.voice.camera.utils.FileUtil;

import java.util.List;

public class CameraInterface {
	private static final String TAG = "YanZi";
	private Camera mCamera;
	private Camera.Parameters mParams;
	private boolean isPreviewing = false;
	private float mPreviwRate = -1f;
	private int mCameraId = -1;
	private boolean isGoolgeFaceDetectOn = false;
	private static CameraInterface mCameraInterface;
	CameraSurfaceView view;
	final int SUCCESS = 233; 
	private Context context;

	private StratCameraCallback startFace;


	public interface StratCameraCallback{
		public void startCameraFace();
	}

	private CameraInterface(Context context) {
		this.context = context;
	}
	public static synchronized CameraInterface getInstance(Context context){
		if(mCameraInterface == null){
			mCameraInterface = new CameraInterface(context);
		}
		return mCameraInterface;
	}
	/**打开Camera
	 * @param
	 */
	public void doOpenCamera(){
		Log.i(TAG, "Camera open....");
		if (checkCameraHardware()) {
			mCamera = Camera.open();
		}
		
	}
	
	/**打开Camera
	 * @param callback
	 */
//	public void doStartTakePicture(CamOpenOverCallback callback){
//
//		if(callback != null){
//			callback.cameraHasOpened();
//		}
//	}

	/**打开Camera
	 * @param callback
	 */
	public void doStartFace(StratCameraCallback callback){

		if(callback != null){
			startFace = callback;
		}
	}
	
	private boolean checkCameraHardware() {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}
	/**开启预览
	 * @param holder
	 * @param
	 */
	public void doStartPreview(CameraSurfaceView v, SurfaceHolder holder, int width, int height){
		Log.i(TAG, "doStartPreview...");
		if(isPreviewing){
			mCamera.stopPreview();
			return;
		}


		Point p = DisplayUtil.getScreenMetrics(context);
		float previewRate = (float)p.x / (float)p.y;
		/* 默认全屏的比例预览 */


		this.view = v;
		if(mCamera != null){
			mParams = mCamera.getParameters();
			mParams.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
//			float previewRate = ((float)height / width);
			

			
	        /*从列表中选取合适的分辨率*/
			Size picSize1 = mParams.getPictureSize();
			Log.e("系统默认尺寸", "宽"+picSize1.width+"高"+picSize1.height);
			Size picSize = CameraUtils.getInstance().getBestPictureSize(mParams.getSupportedPictureSizes(), previewRate, 2560);
	        if(null != picSize)
	        {
	        	mParams.setPictureSize(picSize.width, picSize.height);
	        }
	        else
	        {
	        	picSize = mParams.getPictureSize();
	        }
	        
			List<Size> sizes = mParams.getSupportedPreviewSizes();

			Size preSize = CameraUtils.getOptimalPreviewSize((Activity)context,
					sizes, (double) picSize.width / picSize.height);
			mParams.setPreviewSize(preSize.width, preSize.height);

	        /*根据选出的PictureSize重新设置SurfaceView大小*/
	        float w = preSize.width;
	        float h = preSize.height;
			Point p2 = DisplayUtil.getScreenMetrics(context);
	        v.setLayoutParams(new RelativeLayout.LayoutParams( p.x, p.y));
	        
	        mParams.setJpegQuality(100); // 设置照片质量
	        
	        //先判断是否支持，否则会报错
	        if (mParams.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
	        {
	        	mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
	        }
//	        mParams.setAutoWhiteBalanceLock(true);
	        mCamera.setParameters(mParams);
	        
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();//开启预览
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mCamera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。  
	        mCamera.setDisplayOrientation(0);
			isPreviewing = true;
			mPreviwRate = ((float)height / width);

			startFace.startCameraFace();
		//	startFace.cameraHasOpened();



		}
	}
	/**
	 * 停止预览，释放Camera
	 */
	public void doStopCamera(){
		if(null != mCamera)
		{
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			isPreviewing = false;
			mPreviwRate = -1f;
			mCamera.release();
			mCamera = null;     
		}
	}
	/**
	 * 拍照
	 */
	public void doTakePicture(){
		if(isPreviewing && (mCamera != null)){
			mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
		}
	}
	
	/**获取Camera.Parameters
	 * @return
	 */
	public Camera.Parameters getCameraParams(){
		if(mCamera != null){
			mParams = mCamera.getParameters();
			return mParams;
		}
		return null;
	}
	/**获取Camera实例
	 * @return
	 */
	public Camera getCameraDevice(){
		return mCamera;
	}
	

	public int getCameraId(){
		return mCameraId;
	}
	
	
	
	
		
	

	/*为了实现拍照的快门声音及拍照保存照片需要下面三个回调变量*/
	ShutterCallback mShutterCallback = new ShutterCallback() 
	//快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
	{
		public void onShutter() {
			// TODO Auto-generated method stub
			Log.i(TAG, "myShutterCallback:onShutter...");
		}
	};
	PictureCallback mRawCallback = new PictureCallback() 
	// 拍摄的未压缩原数据的回调,可以为null
	{

		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.i(TAG, "myRawCallback:onPictureTaken...");

		}
	};
	PictureCallback mJpegPictureCallback = new PictureCallback() 
	//对jpeg图像数据的回调,最重要的一个回调
	{
		public void onPictureTaken(byte[] data, Camera camera) {
			

            // TODO Auto-generated method stub
       	 Log.i("takePicture", "开始");
            final byte[] tempdata = data;
            Thread thread = new Thread(new Runnable(){

                @Override
                public void run() {
                     //TODO Auto-generated method stub
        			Log.i(TAG, "myJpegCallback:onPictureTaken...");
        			Bitmap b = null;
        			if(null != tempdata){
        				b = BitmapFactory.decodeByteArray(tempdata, 0, tempdata.length);//data是字节数据，将其解析成位图
        				if(mCamera != null){
							mCamera.stopPreview();
						}
//        				isPreviewing = false;
        			}
        			//保存图片到sdcard
        			if(null != b)
        			{
        				//设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。
        				//图片竟然不能旋转了，故这里要旋转下
						new FileUtil().saveBitmap(context, b);
        			}

                }
                
            });
            //启动存储照片的线程
            thread.start();

		}
	};

}
