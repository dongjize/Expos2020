package com.baidu.idl.face.main.activity.start;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.gate.FaceDepthGateActivity;
import com.baidu.idl.face.main.activity.gate.FaceNIRGateActivity;
import com.baidu.idl.face.main.activity.gate.FaceRGBGateActivity;
import com.baidu.idl.face.main.activity.payment.ZKIDReaderActivity;
import com.baidu.idl.face.main.activity.register.FaceRegisterNewDepthActivity;
import com.baidu.idl.face.main.activity.register.FaceRegisterNewNIRActivity;
import com.baidu.idl.face.main.activity.register.FaceRegisterNewActivity;
import com.baidu.idl.face.main.activity.scangun.ScanGunActivity;
import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.activity.testimony.IDReaderActivity;
import com.baidu.idl.face.main.activity.testimony.IDReaderActivity2;
import com.baidu.idl.face.main.activity.testimony.IDReaderActivity3;
import com.baidu.idl.face.main.activity.user.UserManagerActivity;
import com.baidu.idl.face.main.activity.vbar.VBarActivity;
import com.baidu.idl.face.main.activity.view.PlsyReaderActivity;
import com.baidu.idl.face.main.listener.SdkInitListener;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ToastUtils;
import com.baidu.idl.face.main.R;


public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private Context mContext;
    private Handler mHandler = new Handler();
    private PopupWindow popupWindow;
    private View view1;
    private RelativeLayout layout_home;
    private RelativeLayout home_personRl;
    private TextView home_dataTv;
    private int mLiveType;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mContext = this;
        initView();
        initListener();

        SharedPreferences sharedPreferences = this.getSharedPreferences("share", MODE_PRIVATE);
        boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isFirstRun) {
            mHandler.postDelayed(mRunnable, 500);
            editor.putBoolean("isFirstRun", false);
            editor.commit();
        }

//        String accessToken = AuthService.getAuth();
//        Log.e(TAG, accessToken);

    }


    @Override
    protected void onResume() {
        super.onResume();
        mLiveType = SingleBaseConfig.getBaseConfig().getType();
    }


    private Runnable mRunnable = new Runnable() {
        public void run() {
            // 弹出PopupWindow的具体代码
            initPopupWindow();
        }
    };

    private void initPopupWindow() {
        view1 = View.inflate(mContext, R.layout.layout_popup, null);
        popupWindow = new PopupWindow(view1, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 点击框外可以使得popupwindow消失
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(layout_home, Gravity.CENTER, 0, 0);
        initHandler();
    }

    private void initHandler() {
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                // 实现页面跳转
                popupWindow.dismiss();
                return false;
            }
        }).sendEmptyMessageDelayed(0, 3000);
    }

    private void initView() {
        layout_home = findViewById(R.id.layout_home);
        ImageView home_settingImg = findViewById(R.id.home_settingImg);
        home_settingImg.setOnClickListener(this);
        RelativeLayout home_gateRl = findViewById(R.id.home_gateRl);
//        home_gateRl.setOnClickListener(this);
        RelativeLayout home_checkRl = findViewById(R.id.home_checkRl);
        home_checkRl.setOnClickListener(this);
        RelativeLayout home_payRl = findViewById(R.id.home_payRl);
        home_payRl.setOnClickListener(this);
        RelativeLayout home_attributeRl = findViewById(R.id.home_attributeRl);
        home_attributeRl.setOnClickListener(this);
        home_personRl = findViewById(R.id.home_personRl);
        home_personRl.setOnClickListener(this);
        RelativeLayout home_driveRl = findViewById(R.id.home_driveRl);
//        home_driveRl.setOnClickListener(this);
        RelativeLayout home_attentionRl = findViewById(R.id.home_attentionRl);
        home_attentionRl.setOnClickListener(this);
        ImageView home_faceTv = findViewById(R.id.home_faceTv);
        home_faceTv.setOnClickListener(this);
        ImageView home_faceLibraryTv = findViewById(R.id.home_faceLibraryTv);
        home_faceLibraryTv.setOnClickListener(this);
//        home_dataTv = findViewById(R.id.home_dataTv);
//        home_dataTv.setText("有效期至" + FaceSDKManager.getInstance().getLicenseData(this));

    }

    private void initListener() {
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            FaceSDKManager.getInstance().initModel(mContext, new SdkInitListener() {
                @Override
                public void initStart() {
                }

                @Override
                public void initLicenseSuccess() {
                }

                @Override
                public void initLicenseFail(int errorCode, String msg) {
                }

                @Override
                public void initModelSuccess() {
                    ToastUtils.toast(mContext, "模型加载成功，欢迎使用");
                }

                @Override
                public void initModelFail(int errorCode, String msg) {
                    if (errorCode != -12) {
                        ToastUtils.toast(mContext, "模型加载失败，请尝试重启应用");
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_settingImg:
                startActivity(new Intent(mContext, SettingMainActivity.class));
                break;
            case R.id.home_gateRl:
                // 闸机模块
                if (!initSuccess()) {
                    return;
                }
                judgeLiveType(mLiveType,
                        FaceRGBGateActivity.class,
                        FaceNIRGateActivity.class,
                        FaceDepthGateActivity.class);
                break;
            case R.id.home_checkRl:
                // 考勤模块
                if (!initSuccess()) {
                    return;
                }
                startActivity(new Intent(HomeActivity.this, ZKIDReaderActivity.class));
                break;
            case R.id.home_payRl:
                // 支付模块
                if (!initSuccess()) {
                    return;
                }
                startActivity(new Intent(HomeActivity.this, IDReaderActivity2.class)); // 中控+小二维码
                break;
            case R.id.home_attributeRl:
                // 属性模块
                if (!initSuccess()) {
                    return;
                }
                startActivity(new Intent(HomeActivity.this, IDReaderActivity3.class)); // 中控+小二维码（简化）
                break;
            case R.id.home_personRl:
                // 人证核验
                if (!initSuccess()) {
                    return;
                }
                startActivity(new Intent(HomeActivity.this, IDReaderActivity.class)); // 白色+大二维码
                break;
            case R.id.home_driveRl:
                // 驾驶行为模块
                if (!initSuccess()) {
                    return;
                }
                startActivity(new Intent(HomeActivity.this, PlsyReaderActivity.class));
                break;
            case R.id.home_attentionRl:
                // 注意力模块
                if (!initSuccess()) {
                    return;
                }
                startActivity(new Intent(HomeActivity.this, VBarActivity.class));
                break;
            case R.id.home_faceTv:    // 人脸注册
                if (!initSuccess()) {
                    return;
                }
                judgeLiveType(mLiveType, FaceRegisterNewActivity.class, FaceRegisterNewNIRActivity.class,
                        FaceRegisterNewDepthActivity.class);
                break;
            case R.id.home_faceLibraryTv:   // 人脸库管理
                startActivity(new Intent(HomeActivity.this, UserManagerActivity.class));
                break;
        }
    }

    private boolean initSuccess() {
        if (FaceSDKManager.getInstance().initStatus == FaceSDKManager.SDK_UNACTIVATION) {
            Toast.makeText(HomeActivity.this, "SDK正在加载模型，请稍后再试",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (FaceSDKManager.getInstance().initStatus == FaceSDKManager.SDK_INIT_FAIL) {
            Toast.makeText(HomeActivity.this, "SDK初始化失败，请重新激活初始化",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        if (FaceSDKManager.getInstance().initStatus == FaceSDKManager.SDK_INIT_SUCCESS) {
            Toast.makeText(HomeActivity.this, "SDK正在加载模型，请稍后再试",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void judgeLiveType(int type, Class<?> rgbCls, Class<?> nirCls, Class<?> depthCls) {
        switch (type) {
            case 1: { // 非活体
                startActivity(new Intent(HomeActivity.this, rgbCls));
                break;
            }

            case 2: { // RGB活体
                startActivity(new Intent(HomeActivity.this, rgbCls));
                break;
            }

            case 3: { // IR活体
                startActivity(new Intent(HomeActivity.this, nirCls));
                break;
            }

            case 4: { // 深度活体
                int cameraType = SingleBaseConfig.getBaseConfig().getCameraType();
                judgeCameraType(cameraType, depthCls);
            }
        }
    }

    private void judgeCameraType(int cameraType, Class<?> depthCls) {
        switch (cameraType) {
            case 1: { // pro
                startActivity(new Intent(HomeActivity.this, depthCls));
                break;
            }

            case 2: { // atlas
                startActivity(new Intent(HomeActivity.this, depthCls));
                break;
            }

            case 6: { // Pico
                //  startActivity(new Intent(HomeActivity.this,
                // PicoFaceDepthLivenessActivity.class));
                break;
            }

            default:
                startActivity(new Intent(HomeActivity.this, depthCls));
                break;
        }
    }
}
