package com.baidu.idl.main.facesdk.license;

public class BDFaceLicenseLocalInfo {
    public String licenseKey;              // 用户设定的license key
    public int algorithmId;                // SDK使用的算法ID
    public String packageName;             // 本地包名
    public String md5;                     // 本地包MD5签名
    public String deviceId;                // 本地设备ID
    public String fingerVersion;           // 获取设备指纹库版本号
    public String licenseSdkVersion;       // 鉴权库版本号
}
