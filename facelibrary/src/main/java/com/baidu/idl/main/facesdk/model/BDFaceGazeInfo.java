package com.baidu.idl.main.facesdk.model;

import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceGazeDirection;

import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceGazeDirection.BDFACE_GACE_DIRECTION_DOWN;
import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceGazeDirection.BDFACE_GACE_DIRECTION_EYE_CLOSE;
import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceGazeDirection.BDFACE_GACE_DIRECTION_FRONT;
import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceGazeDirection.BDFACE_GACE_DIRECTION_LEFT;
import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceGazeDirection.BDFACE_GACE_DIRECTION_RIGHT;
import static com.baidu.idl.main.facesdk.model.BDFaceSDKCommon.BDFaceGazeDirection.BDFACE_GACE_DIRECTION_UP;

public class BDFaceGazeInfo {

    public float leftEyeConf;
    public float rightEyeConf;
    public float softmaxEyeConf;
    public BDFaceGazeDirection leftEyeGaze;
    public BDFaceGazeDirection rightEyeGaze;
    public BDFaceGazeDirection softmaxEyeGaze;
    private int[] eyeNum = new int[6];

    public BDFaceGazeInfo(int leftEye, float leftConf, int rightEye,
                          float rightConf, int softmaxEye, float softmaxConf) {
        leftEyeGaze = BDFaceGazeDirection.values()[leftEye];
        leftEyeConf = leftConf;
        eyeNum[leftEye]++;
        rightEyeGaze = BDFaceGazeDirection.values()[rightEye];
        rightEyeConf = rightConf;
        eyeNum[rightEye]++;
        softmaxEyeGaze = BDFaceGazeDirection.values()[softmaxEye];
        softmaxEyeConf = softmaxConf;
        eyeNum[softmaxEye]++;
    }

    public int upCount() {
        if (eyeNum != null) {
            return eyeNum[BDFACE_GACE_DIRECTION_UP.ordinal()];
        }
        return -1;
    }

    public int downCount() {
        if (eyeNum != null) {
            return eyeNum[BDFACE_GACE_DIRECTION_DOWN.ordinal()];
        }
        return -1;
    }

    public int leftCount() {
        if (eyeNum != null) {
            return eyeNum[BDFACE_GACE_DIRECTION_LEFT.ordinal()];
        }
        return -1;
    }

    public int rightCount() {
        if (eyeNum != null) {
            return eyeNum[BDFACE_GACE_DIRECTION_RIGHT.ordinal()];
        }
        return -1;
    }

    public int frontCount() {
        if (eyeNum != null) {
            return eyeNum[BDFACE_GACE_DIRECTION_FRONT.ordinal()];
        }
        return -1;
    }

    public int eyecloseCount() {
        if (eyeNum != null) {
            return eyeNum[BDFACE_GACE_DIRECTION_EYE_CLOSE.ordinal()];
        }
        return -1;
    }
}