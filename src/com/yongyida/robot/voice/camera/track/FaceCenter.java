package com.yongyida.robot.voice.camera.track;

import android.content.Context;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.yongyida.robot.voice.camera.bean.FaceBean;
import com.yongyida.robot.voice.camera.utils.DisplayUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by pc on 2016/8/15.
 */
public class FaceCenter extends FaceCenterParent {

    private static int screenWidth;
    private static int screenHeight;
    private static int FACE_DIFF = 150;
    private int centerX;
    private int centerY;
    private int faceX;
    private int faceY;
    private List<FaceBean> srcList;
    private Context mContext;
    private DiffComparator diffComparator = new DiffComparator();
    private SizeComparator sizeComparator = new SizeComparator();
    private IdentifyXComparator identifyXComparator = new IdentifyXComparator();
    private IdentifyYComparator identifyYComparator = new IdentifyYComparator();
    public FaceCenter(Context context){
        this.mContext =  context;
        screenWidth = DisplayUtil.getScreenMetrics(mContext).x;
        screenHeight = DisplayUtil.getScreenMetrics(mContext).y;
        centerX = screenWidth /2;
        centerY = screenHeight /2;
        Log.e("center", "centerX" + centerX +"centerY"+centerY);
    }

    @Override
    public void startCalcCenter(RectF[] mRect){
        faceX = -1;
        faceY = -1;
        List<FaceBean> list = new ArrayList<FaceBean>();
        if(mRect.length == 0) return;
        for(int i = 0; i< mRect.length; i++){
            int faceCenterX = (int) (mRect[i].right - mRect[i].left) / 2 + (int)mRect[i].left;
            int faceCenterY = (int) (mRect[i].bottom - mRect[i].top) / 2 + (int)mRect[i].top;
            int faceSize = (int) (mRect[i].right - mRect[i].left);
            Log.e("start", "X" + faceCenterX);
            list.add(new FaceBean(mRect[i], faceCenterX, faceCenterY,faceSize, i, 0));
        }
        Collections.sort(list, sizeComparator);
        srcList = list;

        if(list.size() > 1){
            list = getDiffFaceSize(list);
            Collections.sort(list, diffComparator);
            if(list.get(0).getDiffSize() > FACE_DIFF){
                Log.e("type", "face>1+diff>150");
                int size =  list.get(0).getFaceSize();
                List<FaceBean> identifyList = getIdentifyList(srcList, size);
                if(identifyList == null || identifyList.size() == 0) return;
                if(identifyList.size() > 0){
                    Collections.sort(identifyList, identifyXComparator);
                    faceX =(identifyList.get(0).getCenterX()
                            -
                            identifyList.get(identifyList.size() - 1).getCenterX())/2
                            +
                            identifyList.get(identifyList.size() - 1).getCenterX();
                    Collections.sort(identifyList, identifyYComparator);
                    faceY =(identifyList.get(0).getCenterY()
                            -
                            identifyList.get(identifyList.size() - 1).getCenterY())/2
                            +
                            identifyList.get(identifyList.size() - 1).getCenterY();
                }else{

                    Log.e("type", "face=1+diff>150");
                    faceX =list.get(0).getCenterX();

                    faceY =list.get(0).getCenterY();
                }

            }else{
                Collections.sort(list, identifyXComparator);
                 Log.e("type", "face>1+diff<150");
                faceX =(list.get(0).getCenterX()
                        -
                        list.get(list.size() - 1).getCenterX())/2
                        +
                        list.get(list.size() - 1).getCenterX();
                Collections.sort(list, identifyYComparator);
                faceY =(list.get(0).getCenterY()
                        -
                        list.get(list.size() - 1).getCenterY())/2
                        +
                        list.get(list.size() - 1).getCenterY();
            }
        }else{
            Log.e("type", "face=1+diff>150");
            faceX =list.get(0).getCenterX();
            faceY =list.get(0).getCenterY();
        }
    }

    private List<FaceBean> getIdentifyList(List<FaceBean> list, int size){
        List<FaceBean> identifyList = new ArrayList<FaceBean>();
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getFaceSize() >= size){
                identifyList.add(list.get(i));
            }else{
                break;
            }
        }
        return identifyList;

    }
    @Override
    public Point getFaceXY() {
        if(faceX != -1 && faceY != -1){
            Log.e("face", "faceX" + faceX +"faceY"+ faceY);
            return new Point(centerX - faceX, centerY - faceY);
        }
        return new Point(0, 0);
    }

    @Override
    public List<FaceBean> getDiffFaceSize(List<FaceBean> faceBeanList) {
        for(int i = 0; i < faceBeanList.size() - 1; i++){
            int diff = faceBeanList.get(i).getFaceSize() - faceBeanList.get(i+1).getFaceSize();
            faceBeanList.get(i).setDiffSize(diff);
        }
        faceBeanList.get(faceBeanList.size() - 1).setDiffSize(0);
        return faceBeanList;
    }

    private static class SizeComparator implements Comparator<FaceBean> {
        public int compare(FaceBean lhs, FaceBean rhs) {
            if (lhs.getFaceSize() == rhs.getFaceSize())
                return 0;
            else if (lhs.getFaceSize() > rhs.getFaceSize())
                return -1;
            else
                return 1;
        }
    }

    private static class IdentifyXComparator implements Comparator<FaceBean> {
        public int compare(FaceBean lhs, FaceBean rhs) {
            if (lhs.getCenterX() == rhs.getCenterX())
                return 0;
            else if (lhs.getCenterX() > rhs.getCenterX())
                return -1;
            else
                return 1;
        }
    }
    private static class IdentifyYComparator implements Comparator<FaceBean> {
        public int compare(FaceBean lhs, FaceBean rhs) {
            if (lhs.getCenterY() == rhs.getCenterY())
                return 0;
            else if (lhs.getCenterY() > rhs.getCenterY())
                return -1;
            else
                return 1;
        }
    }
    private static class DiffComparator implements Comparator<FaceBean> {
        public int compare(FaceBean lhs, FaceBean rhs) {
            if (lhs.getDiffSize() == rhs.getDiffSize())
                return 0;
            else if (lhs.getDiffSize() > rhs.getDiffSize())
                return -1;
            else
                return 1;
        }
    }
}
