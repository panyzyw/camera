package com.yongyida.robot.voice.camera.bean;

import android.graphics.RectF;

/**
 * Created by pc on 2016/8/15.
 */
public class FaceBean {

    private RectF face;
    private int centerX;
    private int centerY;
    private int flag;
    private int faceSize;
    private int diffSize;

    public FaceBean(RectF face, int centerX, int centerY, int faceSize, int flag, int diffSize) {
        this.face = face;
        this.centerX = centerX;
        this.centerY = centerY;
        this.faceSize = faceSize;
        this.flag = flag;
        this.diffSize = diffSize;
    }

    public int getCenterX() {
        return centerX;
    }

    public void setCenterX(int centerX) {
        this.centerX = centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public void setCenterY(int centerY) {
        this.centerY = centerY;
    }

    public RectF getFace() {
        return face;
    }

    public void setFace(RectF face) {
        this.face = face;
    }

    public int getFaceSize() {
        return faceSize;
    }

    public void setFaceSize(int faceSize) {
        this.faceSize = faceSize;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getDiffSize() {
        return diffSize;
    }

    public void setDiffSize(int diffSize) {
        this.diffSize = diffSize;
    }
}
