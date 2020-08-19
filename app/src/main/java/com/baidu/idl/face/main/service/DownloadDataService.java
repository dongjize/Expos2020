package com.baidu.idl.face.main.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.face.main.Config;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.db.DBManager;
import com.baidu.idl.face.main.exception.FaceError;
import com.baidu.idl.face.main.model.ImportFeatureResult;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.FileUtils;
import com.baidu.idl.face.main.utils.ImageUtils;
import com.baidu.idl.face.main.utils.OnResultListener;
import com.baidu.idl.main.facesdk.FaceDetect;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DownloadDataService extends Service {

    private static final String TAG = DownloadDataService.class.getSimpleName();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        requestUsers(new OnResultListener<List<User>>() {
//            @Override
//            public void onResult(List<User> result) {
//                for (User user : result) {
//                    Log.d(TAG, "=========== " + user.toString() + " ===========");
//
//                    List<User> listUsers = FaceApi.getInstance().getUserListByUserId(user.getUserId());
//                    if (listUsers != null && listUsers.size() > 0) {
//                        continue;
//                    }
//
//                    String img = user.getImage();
//                    Bitmap bitmap = ImageUtils.stringToBitmap(img);
//                    Log.e(TAG, "success ======= " + bitmap);
//
//                    FaceDetect faceDetect = new FaceDetect();
//                    BDFaceImageInstance bdFaceImageInstance = new BDFaceImageInstance(bitmap);
//                    FaceInfo[] faceInfos = faceDetect.detect(BDFaceSDKCommon.DetectType.DETECT_VIS, bdFaceImageInstance);
//
//                    Log.e(TAG, Arrays.toString(faceInfos));
//
//                    if (faceInfos != null) {
//                        FaceInfo faceInfo = faceInfos[0];
//
//                        byte[] features = new byte[512];
//
//                        ImportFeatureResult importFeatureResult = FaceApi.getInstance().getFeature(bitmap, features, BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);
//                        if (importFeatureResult.getResult() == -1) {
//                            Log.e(TAG, user.getUserName() + ".jpg" + "：bitmap参数为空");
//                        } else if (importFeatureResult.getResult() == -2) {
//                            Log.e(TAG, user.getUserName() + ".jpg" + "：未检测到人脸");
//                        } else if (importFeatureResult.getResult() == -3) {
//                            Log.e(TAG, user.getUserName() + ".jpg" + "：抠图失败");
//                        } else if (importFeatureResult.getResult() == 128) {
//                            Log.e(TAG, user.getUserName() + ".jpg" + "：OK");
//                        }
//
//                        user.setFeature(features);
////                        user.setImage(user.getUserName() + ".jpg");
//                        user.setImageName(user.getUserName() + ".jpg");
//
////                        boolean importDBSuccess = FaceApi.getInstance().registerUserIntoDBmanager(Config.groupID, user.getUserName(), user.getUserName() + ".jpg", user.getUserName(), features);
//                        boolean importDBSuccess = FaceApi.getInstance().registerUserIntoDBManager(user);
//                        if (importDBSuccess) {
//                            File facePicDir = FileUtils.getBatchImportSuccessDirectory();
//                            if (facePicDir != null) {
//                                File savePicPath = new File(facePicDir, user.getUserName() + ".jpg");
//                                if (FileUtils.saveBitmap(savePicPath, importFeatureResult.getBitmap())) {
//                                    Log.i(TAG, "图片保存成功");
////                                    success = true;
//                                } else {
//                                    Log.i(TAG, "图片保存失败");
//                                }
//                            }
//                        }
//                        Log.d(TAG, "=========== " + Arrays.toString(features) + " ===========");
//
//                        Log.d(TAG, "=========== " + importDBSuccess + " ===========");
//
//                    }
//
//                }
//
//            }
//
//            @Override
//            public void onError(FaceError error) {
//                Log.e(TAG, "error ======= " + error.toString());
//
//            }
//        });


        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + Config.REQUEST_INTERVAL; // 每隔5s同步一次
        Intent i = new Intent(this, DownloadReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

//    private void requestUsers(final OnResultListener<List<User>> listener) {
//        try {
//            OkHttpClient okHttpClient = new OkHttpClient();
//            String url = Config.API_URL;
//            //24.7f7f376d7f56251d1bdeec5a5336408f.2592000.1598022511.282335-20298063
//
//            String reqTime = FaceApi.getInstance().getLatestCTime();
//            Log.e(TAG, reqTime);
//            RequestBody body = new FormBody.Builder().add("reqTime", reqTime).build();
//            final Request request = new Request.Builder()
//                    .url(url)
//                    .post(body)
//                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
//                    .build();
//            Call call = okHttpClient.newCall(request);
//
//            call.enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                    FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "network request error", e);
//                    listener.onError(error);
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    String haha = response.body().string();
//                    if (response == null || response.body() == null || TextUtils.isEmpty(haha)) {
//                        FaceError error = new FaceError(FaceError.ErrorCode.ACCESS_TOKEN_PARSE_ERROR, "token is parse error, please rerequest token");
//                        listener.onError(error);
//
//                    }
//                    try {
//                        JSONObject jsonObject = new JSONObject(haha);
//
//                        Gson gson = new Gson();
//                        List<User> userList = gson.fromJson(jsonObject.getJSONArray("data").toString(), new TypeToken<List<User>>() {
//                        }.getType());
//
//                        listener.onResult(userList);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "response with error", e);
//                        listener.onError(error);
//                    }
//                }
//            });
////            Log.e(TAG, "===================" + s);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
