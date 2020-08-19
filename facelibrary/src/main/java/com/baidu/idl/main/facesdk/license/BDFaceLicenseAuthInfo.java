package com.baidu.idl.main.facesdk.license;

public class BDFaceLicenseAuthInfo {
    public String licenseKey;              // 用户设定的license key
    public int algorithmId;                // SDK使用的算法ID
    public String packageName;             // 本地包名
    public String md5;                     // 本地包MD5签名
    public String deviceId;                // 本地设备ID
    public long expireTime;                // 过期时间
    public String functionList;            // 授权功能
}
