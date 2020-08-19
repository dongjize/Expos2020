package com.baidu.idl.face.main.utils;

import android.graphics.Color;

public class ColorUtils {
    /**
     * 获取随机颜色
     * 
     * @return
     */
    public static int getRandomColorInt() {
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);
        return Color.rgb(red, green, blue);
    }
}
