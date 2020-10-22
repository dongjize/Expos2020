package com.baidu.idl.face.main.activity.testimony;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.idl.face.main.Config;
import com.baidu.idl.face.main.exception.FaceError;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.OnResultListener;
import com.baidu.idl.face.main.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpRequester {

    private static final String TAG = HttpRequester.class.getSimpleName();

    public static void requestUserById(final OnResultListener<List<User>> listener, String idCardNo) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            long timestamp = System.currentTimeMillis() / 1000;

            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Config.API_URL)).newBuilder()
                    .addQueryParameter("cmd", "getUsers")
                    .addQueryParameter("timestamp", timestamp + "")
                    .addQueryParameter("token", Utils.md5(timestamp + Config.API_KEY))
                    .build();

            RequestBody body = new FormBody.Builder().add("idCardNo", idCardNo).build();

            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "network request error", e);
                    listener.onError(error);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String haha = response.body().string();
                    if (response == null || response.body() == null || TextUtils.isEmpty(haha)) {
                        FaceError error = new FaceError(FaceError.ErrorCode.ACCESS_TOKEN_PARSE_ERROR, "token is parse error, please rerequest token");
                        listener.onError(error);

                    }
                    try {
                        JSONObject jsonObject = new JSONObject(haha);

                        Gson gson = new Gson();
                        List<User> userList = gson.fromJson(jsonObject.getJSONArray("data").toString(), new TypeToken<List<User>>() {
                        }.getType());

                        listener.onResult(userList);

                    } catch (Exception e) {
                        e.printStackTrace();
                        FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "response with error", e);
                        listener.onError(error);
                    }
                }
            });
//            Log.e(TAG, "===================" + s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void requestUserByAudienceCode(final OnResultListener<List<User>> listener, String audienceCode) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            long timestamp = System.currentTimeMillis() / 1000;

            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Config.API_URL)).newBuilder()
                    .addQueryParameter("cmd", "getUsers")
                    .addQueryParameter("timestamp", timestamp + "")
                    .addQueryParameter("token", Utils.md5(timestamp + Config.API_KEY))
                    .build();

            RequestBody body = new FormBody.Builder().add("audience_code", audienceCode).build();

            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "network request error", e);
                    listener.onError(error);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String haha = response.body().string();
                    if (response.body() == null || TextUtils.isEmpty(haha)) {
                        FaceError error = new FaceError(FaceError.ErrorCode.ACCESS_TOKEN_PARSE_ERROR, "token is parse error, please rerequest token");
                        listener.onError(error);

                    }
                    try {
                        JSONObject jsonObject = new JSONObject(haha);

                        Gson gson = new Gson();
                        List<User> userList = gson.fromJson(jsonObject.getJSONArray("data").toString(), new TypeToken<List<User>>() {
                        }.getType());

                        listener.onResult(userList);

                    } catch (Exception e) {
                        e.printStackTrace();
                        FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "response with error", e);
                        listener.onError(error);
                    }
                }
            });
//            Log.e(TAG, "===================" + s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void uploadIDPhoto(final OnResultListener<String> listener, Bitmap photo, User user) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            long timestamp = System.currentTimeMillis() / 1000;

            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Config.API_URL)).newBuilder()
                    .addQueryParameter("cmd", "userEdit")
                    .addQueryParameter("timestamp", timestamp + "")
                    .addQueryParameter("token", Utils.md5(timestamp + Config.API_KEY))
                    .build();

            String img = BitmapUtils.bitmapToBase64(photo, 60);
            Log.e(TAG, "image ======== " + img);

            RequestBody body = new FormBody.Builder()
                    .add("image", img)
                    .add("group_id", user.getGroupId())
                    .add("user_id", user.getUserId())
                    .add("user_info", user.getUserInfo())
                    .add("user_name", user.getUserName())
                    .add("item_eid", user.getItemEId())
                    .add("idCardNo", user.getIdCardNo())
                    .add("audience_code", user.getAudienceCode())
                    .build();

            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "network request error", e);
                    listener.onError(error);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String haha = response.body().string();
                    if (response == null || response.body() == null || TextUtils.isEmpty(haha)) {
                        FaceError error = new FaceError(FaceError.ErrorCode.ACCESS_TOKEN_PARSE_ERROR, "token is parse error, please rerequest token");
                        listener.onError(error);

                    }
                    try {
                        JSONObject jsonObject = new JSONObject(haha);

                        Gson gson = new Gson();
//                        BaseHttpResult result = gson.fromJson(jsonObject.getJSONArray("data").toString(), new TypeToken<BaseHttpResult>() {
//                        }.getType());
                        Log.e(TAG, "errMsg ===" + jsonObject.getString("msg"));
                        String result = gson.fromJson(jsonObject.getString("code"), new TypeToken<String>() {
                        }.getType());
                        listener.onResult(result);

                    } catch (Exception e) {
                        e.printStackTrace();
                        FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "response with error", e);
                        listener.onError(error);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void uploadPassRecord(final OnResultListener<String> listener, String eid, String pclass, User user, Bitmap idCardPhoto) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            long timestamp = System.currentTimeMillis() / 1000;

            HttpUrl url = Objects.requireNonNull(HttpUrl.parse(Config.API_URL)).newBuilder()
                    .addQueryParameter("cmd", "passRecord")
                    .addQueryParameter("timestamp", timestamp + "")
                    .addQueryParameter("token", Utils.md5(timestamp + Config.API_KEY))
                    .build();

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String todayAsString = df.format(today);

            RequestBody body = new FormBody.Builder()
                    .add("pimg", BitmapUtils.bitmapToBase64(idCardPhoto, 60))
                    .add("user_id", user.getUserId())
                    .add("user_name", user.getUserName())
                    .add("pclass", pclass)
                    .add("eid", eid)
                    .add("ptime", todayAsString)
                    .build();

            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "network request error", e);
                    listener.onError(error);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String haha = response.body().string();
                    if (response == null || response.body() == null || TextUtils.isEmpty(haha)) {
                        FaceError error = new FaceError(FaceError.ErrorCode.ACCESS_TOKEN_PARSE_ERROR, "token is parse error, please rerequest token");
                        listener.onError(error);

                    }
                    try {
                        JSONObject jsonObject = new JSONObject(haha);

                        Gson gson = new Gson();
                        String result = gson.fromJson(jsonObject.getString("code"), new TypeToken<String>() {
                        }.getType());

                        listener.onResult(result);

                    } catch (Exception e) {
                        e.printStackTrace();
                        FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "response with error", e);
                        listener.onError(error);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
