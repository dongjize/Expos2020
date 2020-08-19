package com.baidu.idl.face.main.db;

public class CloudDBManager {
    private static final String TAG = "CloudDBManager";

    private static CloudDBManager instance;
    public static final String GROUP_ID = "0";


    public static synchronized CloudDBManager getInstance() {
        if (instance == null) {
            instance = new CloudDBManager();
        }
        return instance;
    }




}
