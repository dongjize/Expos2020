package com.baidu.idl.face.main;

public class Config {

    public static final int REQUEST_INTERVAL = 5 * 1000;

    //    // 为了apiKey,secretKey为您调用百度人脸在线接口的，如注册，比对等。
//    // 为了的安全，建议放在您的服务端，端把人脸传给服务器，在服务端端
//    // license为调用sdk的人脸检测功能使用，人脸识别 = 人脸检测（sdk功能）  + 人脸比对（服务端api）

    public static String apiKey = "aqXouzuFy7ynnFt8SAiTyeTV";
    public static String secretKey = "AreN24TW1lRdmTbWQj4p4k9hS60afLw2";
    public static String licenseID = "ExposApp-face-android";
    public static String licenseFileName = "idl-license.face-android";

//    public static String groupID = "expo1";
//    public static String groupID = "user_01";

    public static final String ORIGINAL_DOMAIN = "http://ai3.exposdata.com";
    public static final String ORIGINAL_API_URL = ORIGINAL_DOMAIN + "/api";
    //    public static String ORIGINAL_API_URL = "https://ai.exposdata.com/api";
//    public static String ORIGINAL_API_URL = "http://ai2.exposdata.com/api";
    public static String DOMAIN = "http://ai3.exposdata.com";
    public static String API_URL = DOMAIN + "/api";
    //    public static String API_URL = "https://ai.exposdata.com/api";
//    public static String API_URL = "http://ai2.exposdata.com/api";
    public static String SECONDARY_API_URL = "https://5jyw.com:444/bdface/api";

    /*
     * <p>
     * 每个开发者账号只能创建一个人脸库；groupID用于标识人脸库
     * <p>
     * 人脸识别 接口 https://aip.baidubce.com/rest/2.0/face/v2/identify
     * 人脸注册 接口 https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/add
     */

    public static final String API_KEY = "309fede86ea1903c90f1718f66c7da9c";
    public static int scoreThreshold = 70;
}
