package com.baidu.idl.face.main;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.baidu.idl.face.main.activity.start.StartActivity;
import com.baidu.idl.face.main.service.DownloadDataService;

import okhttp3.OkHttpClient;


public class MainApplication extends Application {
    private static MainApplication app;

    public static OkHttpClient okHttpClient;

    public static String accessToken;


    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        okHttpClient = new OkHttpClient();
        SharedPreferences sp = getSharedPreferences("api_url", Context.MODE_PRIVATE);
        Config.API_URL = sp.getString("api_url", Config.API_URL);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
//        Intent intent = new Intent(getApplicationContext(), DownloadDataService.class);
//        stopService(intent);
    }
}
