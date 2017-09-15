package com.yongyida.robot.voice.camera.track;

import android.graphics.Point;
import android.graphics.RectF;

import com.yongyida.robot.voice.camera.bean.FaceBean;

import java.util.List;

/**
 * Created by pc on 2016/8/15.
 */
public abstract class FaceCenterParent {
    public abstract void startCalcCenter(RectF[] faces);
    public abstract Point getFaceXY();
    public abstract List<FaceBean> getDiffFaceSize(List<FaceBean> faceBeanLists);
}
