package com.baidu.idl.main.facesdk.model;

public class BDFaceCropParam {
    public float foreheadExtend;      // 额头扩展，大于等于0，0：不进行扩展
    public float chinExtend;          // 下巴扩展，大于等于0，0：不进行扩展
    public float enlargeRatio;        // 人脸框与背景比例，大于等于1，1：不进行扩展
    public int width;                  // 输出图像宽，设置为有效值(大于0)则对图像进行缩放，否则输出原图抠图结果
    public int height;                 // 输出图像宽，设置为有效值(大于0)则对图像进行缩放，否则输出原图抠图结果

    public BDFaceCropParam() {
        foreheadExtend = 0.0f;
        chinExtend = 0.0f;
        enlargeRatio = 1.0f;
        width = 0;
        height = 0;
    }
}
