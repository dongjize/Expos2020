package com.baidu.idl.main.facesdk.model;

public class BDFaceIsOutBoundary {
    public int left;     // 0: 未出边界，1：出边界
    public int right;    // 0: 未出边界，1：出边界
    public int top;      // 0: 未出边界，1：出边界
    public int bottom;   // 0: 未出边界，1：出边界

    public BDFaceIsOutBoundary(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }
}
