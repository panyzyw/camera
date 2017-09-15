package com.yongyida.robot.voice.camera.preview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Face;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.yongyida.robot.voice.camera.R;
import com.yongyida.robot.voice.camera.autophoto.CameraInterface;
import com.yongyida.robot.voice.camera.utils.Util;

public class FaceView extends ImageView{
	private static final String TAG = "FaceView";
	private Context mContext;
	private Paint mLinePaint;
	private Face[] mFaces;
    
	private Matrix mMatrix = new Matrix();
	private RectF mRect;
	private RectF[] mRects;
	private Drawable mFaceIndicator = null;
    private static FaceLocationCallback mCallback;
    
    public static void setFaceCallback(FaceLocationCallback callback){
        mCallback = callback;
	}
	public interface FaceLocationCallback{
        void onFaceLocation(RectF[] mFaces);
    }
	public FaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initPaint();
		mContext = context;
		mFaceIndicator = getResources().getDrawable(R.drawable.ic_face);

	}


	public void setFaces(Face[] faces){
		this.mFaces = faces;
		invalidate();
	}
	public void clearFaces(){
		mFaces = null;
		invalidate();
	}
	

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
        Log.e(TAG,"onDraw_enter");
        if(mFaces == null || mFaces.length < 1){
            Log.e(TAG,"onDraw_return");
			return;
		}
		boolean isMirror = false;
		int Id = CameraInterface.getInstance(mContext).getCameraId();
		if(Id == CameraInfo.CAMERA_FACING_BACK){
			isMirror = false; //后置Camera无需mirror
		}else if(Id == CameraInfo.CAMERA_FACING_FRONT){
			isMirror = true;  //前置Camera需要mirror
		}
		Util.prepareMatrix(mMatrix, isMirror, 0, getWidth(), getHeight());
		canvas.save();
		mMatrix.postRotate(0); //Matrix.postRotate默认是顺时针
		canvas.rotate(-0);   //Canvas.rotate()默认是逆时针
        mRects = new RectF[mFaces.length];
		for(int i = 0; i< mFaces.length; i++){
            mRect = new RectF();
			mRect.set(mFaces[i].rect);
			mMatrix.mapRect(mRect);
            mRects[i] = mRect;
			Log.e("FaceViewLocation", "left"+mRect.left+"top"+mRect.top+"right"+mRect.right+"bottom"+mRect.bottom
					+"mFaces.length" + mFaces.length);
			mFaceIndicator.setBounds(Math.round(mRect.left), Math.round(mRect.top), Math.round(mRect.right), Math.round(mRect.bottom));
            mFaceIndicator.draw(canvas);
//			canvas.drawRect(mRect, mLinePaint);
		}
		canvas.restore();
        mCallback.onFaceLocation(mRects);
        super.onDraw(canvas);
	}

	private void initPaint(){
		mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		int color = Color.rgb(0, 150, 255);
		int color = Color.rgb(98, 212, 68);
//		mLinePaint.setColor(Color.RED);
		mLinePaint.setColor(color);
		mLinePaint.setStyle(Style.STROKE);
		mLinePaint.setStrokeWidth(5f);
		mLinePaint.setAlpha(180);
	}


}
