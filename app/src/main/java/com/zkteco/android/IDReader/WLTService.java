package com.zkteco.android.IDReader;

public class WLTService {
    static {
        System.loadLibrary("wlt2bmp");
        System.loadLibrary("zkwltdecode");
    }

    public static int imgWidth = 102;
    public static int imgHeight = 126;
    public static int imgLength = 3 * 102 * 126;

    public static native int wlt2Bmp(byte[] inbuf, byte[] outbuf);
}
