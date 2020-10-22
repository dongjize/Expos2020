package com.baidu.idl.face.main.activity.testimony;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.R;
import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.model.DeviceParamBean;
import com.baidu.idl.face.main.activity.scangun.ScanGun;
import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.activity.vbar.Vbar;
import com.baidu.idl.face.main.callback.CameraDataCallback;
import com.baidu.idl.face.main.callback.FaceDetectCallBack;
import com.baidu.idl.face.main.callback.FaceFeatureCallBack;
import com.baidu.idl.face.main.camera.AutoTexturePreviewView;
import com.baidu.idl.face.main.camera.CameraPreviewManager;
import com.baidu.idl.face.main.exception.FaceError;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.LivenessModel;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.BitmapUtils;
import com.baidu.idl.face.main.utils.DensityUtils;
import com.baidu.idl.face.main.utils.FaceOnDrawTexturViewUtil;
import com.baidu.idl.face.main.utils.OnResultListener;
import com.baidu.idl.face.main.view.PreviewTexture;
import com.baidu.idl.main.facesdk.FaceInfo;
import com.baidu.idl.main.facesdk.model.BDFaceImageInstance;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.core.device.ParameterHelper;
import com.zkteco.android.biometric.core.device.TransportType;
import com.zkteco.android.biometric.core.utils.LogHelper;
import com.zkteco.android.biometric.module.idcard.IDCardReader;
import com.zkteco.android.biometric.module.idcard.IDCardReaderExceptionListener;
import com.zkteco.android.biometric.module.idcard.IDCardReaderFactory;
import com.zkteco.android.biometric.module.idcard.exception.IDCardReaderException;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;
import com.zkteco.android.biometric.module.idcard.meta.IDPRPCardInfo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IDReaderActivity2 extends BaseActivity implements View.OnClickListener, ScanGun.ScanGunCallBack {
    private static final String TAG = IDReaderActivity2.class.getSimpleName();

    private byte[] firstFeature = new byte[512];
    private byte[] secondFeature = new byte[512];

    private Context mContext;
    private RelativeLayout livenessRl;
    private RectF rectF;
    private Paint paint;
    private Paint paintBg;
    // 摄像头个数
    private int mCameraNum;
    // RGB+IR 控件
    private PreviewTexture[] mPreview;
    private Camera[] mCamera;
    private AutoTexturePreviewView mPreviewView;
    private ImageView testImageview;
    private TextureView mDrawDetectFaceView;
    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PREFER_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    // 摄像头采集数据
    private volatile byte[] rgbData;
    private volatile byte[] irData;
    private RelativeLayout livenessAgainRl;
    private ImageView livenessShowIv;
    private ImageView hintShowIv;
    private TextView tv_nir_live_score;
    private RelativeLayout livenessTipsFailRl;
    private TextView livenessTipsFailTv;
    private TextView livenessTipsPleaseFailTv;
    private TextureView irTexture;
    float score = 0;

    // 定义一个变量判断是预览模式还是开发模式
    boolean isDevelopment = false;
    private RelativeLayout kaifaRelativeLayout;
    private RelativeLayout test_nir_rl;
    private RelativeLayout test_rgb_ir_rl;
    private TextView hintAdainTv;
    private RelativeLayout layoutCompareStatus;
    private float rgbLiveScore;
    private float nirLiveScore;
    // 判断是否有人脸
    private boolean isFace = false;
    private ImageView livenessTipsFailIv;
    private float nirLivenessScore = 0.0f;
    private float rgbLivenessScore = 0.0f;

    private TextView mTvIDCardNo;
    private TextView mTvUserName;
    private boolean isReqUSBPermission = false;
    private Bitmap mIdCardPhoto;
    private int mDeviceType = DeviceParamBean.DEV_TYPE_INNER_OR_HID;
    private Handler mHandler;
    private long mLastOpenTime = 0;
    private long mUserScanTime = 0;
    private User mUser;
    private String lastUserIdNo;
    private static final int USER_SCAN_INTERVAL = 3 * 1000;
    private boolean checkable = false;

    private Vbar vbar;
    private boolean state;
    //    private String eid;
    private String pclass;
    private long mQRTime;

    private float[] mPointXY = new float[4];
    private byte[] mFeatures = new byte[512];
    private Bitmap mDetectedBitmap;
    private boolean mCollectSuccess;
    private boolean isQrFaceDetect = false;

    private UsbManager mUsbManager;
    private static final String ACTION_USB_PERMISSION = "com.example.scarx.idcardreader.USB_PERMISSION";

    // --------------------

    private static final int VID = 1024;    //IDR VID
    private static final int PID = 50010;     //IDR PID
    private IDCardReader idCardReader = null;
    private ScanGun mScanGun;

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("ACTION", action);

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openDevice();
                    } else {
                        Toast.makeText(mContext, "USB未授权", Toast.LENGTH_SHORT).show();
                        //mTxtReport.setText("USB未授权");
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_ir_idreader);
        mContext = getApplicationContext();
        mHandler = new Handler();

//        if (mUsbManager != null) {
//            for (UsbDevice device : mUsbManager.getDeviceList().values()) {
//                Log.e("USB ==== ", "vid: " + device.getVendorId() + " pid:" + device.getProductId());
//                if (device.getVendorId() == 3141 && device.getProductId() == 57616) {
//                    Intent intent = new Intent(ACTION_USB_PERMISSION);
//                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
//                    mUsbManager.requestPermission(device, mPermissionIntent);
//                    Log.d(TAG, "QRCode device: checked");
//                }
//            }
//        }

        initView();

        // 屏幕的宽
        int displayWidth = DensityUtils.getDisplayWidth(mContext);
        // 屏幕的高
        int displayHeight = DensityUtils.getDisplayHeight(mContext);
        // 当屏幕的宽大于屏幕宽时
        if (displayHeight < displayWidth) {
            // 获取高
            int height = displayHeight;
            // 获取宽
            int width = (int) (displayHeight * ((9.0f / 16.0f)));
            // 设置布局的宽和高
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
            // 设置布局居中
            params.gravity = Gravity.CENTER;
            livenessRl.setLayoutParams(params);
        }


        requestDevicePermission();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        mContext.registerReceiver(mUsbReceiver, filter);

    }

    private void requestDevicePermission() {
        mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);

        for (UsbDevice device : mUsbManager.getDeviceList().values()) {
            if (device.getVendorId() == VID && device.getProductId() == PID) {
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
                mUsbManager.requestPermission(device, pendingIntent);
            }
        }
    }


    private boolean authenticate() {
        try {
            idCardReader.findCard(0);
            idCardReader.selectCard(0);
            return true;
        } catch (IDCardReaderException e) {
            return false;
        }
    }

    private void openDevice() {
        try {
            startIDCardReader();
            IDCardReaderExceptionListener listener = new IDCardReaderExceptionListener() {
                @Override
                public void OnException() {
                    //出现异常，关闭设备
//                    CloseDevice(); // TODO
                }
            };
            idCardReader.open(0);
            idCardReader.setIdCardReaderExceptionListener(listener);
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        int retType = 0;
                        try {
                            if (authenticate()) {
                                retType = idCardReader.readCardEx(0, 0);
                            }
                        } catch (IDCardReaderException e) {
                            Log.e(TAG, e.toString());
                        }
                        if (retType == 1 || retType == 2 || retType == 3) {
                            isQrFaceDetect = false;
                            pclass = "刷身份证";

                            updateIDCardInfo(retType);

                            final IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();

                            HttpRequester.requestUserById(new OnResultListener<List<User>>() {
                                @Override
                                public void onResult(List<User> result) {
                                    checkable = true;

                                    if (result != null && result.size() > 0) {
                                        if (mUser != null) {
                                            lastUserIdNo = mUser.getIdCardNo();
                                        }

                                        // 如果两次身份证号不相同，或同一个user刷卡时间间隔3秒以上，则更新闸机open time
                                        if (lastUserIdNo != null && !lastUserIdNo.equals(result.get(0).getIdCardNo()) || System.currentTimeMillis() - mUserScanTime > USER_SCAN_INTERVAL) {
                                            mUserScanTime = System.currentTimeMillis();
                                        }
                                        mUser = result.get(0);

                                        if (idCardInfo.getPhoto() != null) {
                                            mIdCardPhoto = BitmapFactory.decodeByteArray(idCardInfo.getPhoto(), 0, idCardInfo.getPhoto().length);
                                            FaceSDKManager.getInstance().personDetect(mIdCardPhoto, secondFeature);

                                            if (TextUtils.isEmpty(mUser.getImage())) {
                                                HttpRequester.uploadIDPhoto(new OnResultListener<String>() {
                                                    @Override
                                                    public void onResult(String result) {
                                                        if (result.equals("0")) {
                                                            // 如果更新成功，则人脸识别 TODO
                                                            Log.e(TAG, result);
                                                            toast("上传证件照成功");
                                                        } else {
                                                            toast("上传证件照失败，errCode: " + result);
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(FaceError error) {
                                                        Log.e(TAG, error.getErrorMessage());
                                                        toast(error.getErrorMessage());
                                                    }
                                                }, mIdCardPhoto, mUser);
                                            }
                                        } else {
                                            toast("身份证照片读取失败");
                                        }
                                    } else {
                                        toast("该用户不存在");
                                    }
                                }

                                @Override
                                public void onError(FaceError error) {
                                    Log.e(TAG, "ERROR ===== " + error);
                                }
                            }, idCardInfo.getId());
                        }
                    }
                }
            }).start();
        } catch (IDCardReaderException e) {
            Log.e(TAG, e.toString());
        }

    }

    private void startIDCardReader() {
        if (null != idCardReader) {
            IDCardReaderFactory.destroy(idCardReader);
            idCardReader = null;
        }
        // Define output log level
        LogHelper.setLevel(Log.VERBOSE);
        // Start fingerprint sensor
        Map<String, Object> idrparams = new HashMap<>();
        idrparams.put(ParameterHelper.PARAM_KEY_VID, VID);
        idrparams.put(ParameterHelper.PARAM_KEY_PID, PID);
        idCardReader = IDCardReaderFactory.createIDCardReader(this, TransportType.USB, idrparams);
    }


//    private void CloseDevice() {
//        if (!bOpen) {
//            return;
//        }
//        bStopped = true;
//        try {
//            idCardReader.close(0);
//        } catch (IDCardReaderException e) {
//            e.printStackTrace();
//        }
//        bOpen = false;
//    }


    private void unregisterReceiver() {
//        mContext.unregisterReceiver(mUsbReceiver);
    }


    private void initView() {
        // 获取整个布局
        livenessRl = findViewById(R.id.liveness_Rl);
        // 画人脸框
        rectF = new RectF();
        paint = new Paint();
        paintBg = new Paint();

        mPreviewView = findViewById(R.id.detect_ir_image_view);
        // 双目摄像头IR 图像预览
        irTexture = findViewById(R.id.texture_preview_ir);
        if (SingleBaseConfig.getBaseConfig().getMirrorNIR() == 1) {
            irTexture.setRotationY(180);
        }
        // 不需要屏幕自动变黑
        mDrawDetectFaceView = findViewById(R.id.texture_view_draw);
        mDrawDetectFaceView.setKeepScreenOn(true);
        mDrawDetectFaceView.setOpaque(false);
        // RGB 阈值
        rgbLiveScore = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        // Live 阈值
        nirLiveScore = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        // 设置
        ImageView testimonySettingIv = findViewById(R.id.btn_setting);
        testimonySettingIv.setOnClickListener(this);
        mTvIDCardNo = findViewById(R.id.tvCardNo);
        mTvUserName = findViewById(R.id.tvUserName);


        // ****************开发模式****************
        // RGB
        testImageview = findViewById(R.id.test_rgb_ir_view);
        testImageview.setVisibility(View.GONE);
        // 开发模式buttom
        // 图片显示
        hintShowIv = findViewById(R.id.hint_showIv);
        // 重新上传
        hintAdainTv = findViewById(R.id.hint_adainTv);
        hintAdainTv.setOnClickListener(this);
        // 上传图片
        ImageView DevelopmentAddIv = findViewById(R.id.Development_addIv);
        DevelopmentAddIv.setOnClickListener(this);
        // nir
        test_nir_rl = findViewById(R.id.test_nir_Rl);
        test_nir_rl.setVisibility(View.GONE);
        test_rgb_ir_rl = findViewById(R.id.test_rgb_ir_Rl);
        test_rgb_ir_rl.setVisibility(View.GONE);
        // 提示
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        // 活体检测耗时
        tv_nir_live_score = findViewById(R.id.tv_nir_live_score);

        // ****************预览模式****************
        // 未通过提示
        livenessTipsFailRl = findViewById(R.id.liveness_tips_failRl);
        livenessTipsFailTv = findViewById(R.id.liveness_tips_failTv);
        livenessTipsPleaseFailTv = findViewById(R.id.liveness_tips_please_failTv);
        livenessTipsFailIv = findViewById(R.id.liveness_tips_failIv);
        livenessAgainRl = findViewById(R.id.liveness_againRl);
        livenessShowIv = findViewById(R.id.liveness_showIv);
        // 双摄像头
        mCameraNum = Camera.getNumberOfCameras();
        if (mCameraNum < 2) {
            toast("未检测到2个摄像头");
        } else {
            mPreview = new PreviewTexture[mCameraNum];
            mCamera = new Camera[mCameraNum];
            mPreview[1] = new PreviewTexture(this, irTexture);
        }
    }


    private void initVBar() {
        vbar = new Vbar();
        state = vbar.vbarOpen();
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    final String audienceCode = vbar.getResultsingle();
                    if (audienceCode != null && System.currentTimeMillis() - mQRTime > USER_SCAN_INTERVAL) {
                        mQRTime = System.currentTimeMillis();
                        checkable = true;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.e(TAG, audienceCode);
                                isQrFaceDetect = false;

                                HttpRequester.requestUserByAudienceCode(new OnResultListener<List<User>>() {
                                    @Override
                                    public void onResult(List<User> result) {
                                        pclass = "扫二维码";
                                        if (result != null && result.size() > 0) {

                                            if (mUser != null) {
                                                lastUserIdNo = mUser.getIdCardNo();
                                            }

                                            // 如果两次身份证号不相同，或同一个user刷卡时间间隔3秒以上，则更新闸机open time
                                            if (lastUserIdNo != null && !lastUserIdNo.equals(result.get(0).getIdCardNo()) || System.currentTimeMillis() - mUserScanTime > USER_SCAN_INTERVAL) {
                                                mUserScanTime = System.currentTimeMillis();
                                            }
                                            mUser = result.get(0);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mTvIDCardNo.setText(mUser.getIdCardNo());
                                                    mTvUserName.setText(mUser.getUserName());
                                                }
                                            });

                                            if (!TextUtils.isEmpty(mUser.getImage())) {
                                                mIdCardPhoto = BitmapUtils.base64ToBitmap(mUser.getImage());
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        livenessShowIv.setImageBitmap(mIdCardPhoto);
                                                    }
                                                });
                                                FaceSDKManager.getInstance().personDetect(mIdCardPhoto, secondFeature);
                                            } else {
//                                                startActivityForResult(new Intent(IDReaderActivity.this, FaceNIRDetectActivity.class), 100);
                                                isQrFaceDetect = true;
                                            }
                                        } else {
                                            toast("该用户不存在");
                                        }
                                    }

                                    @Override
                                    public void onError(FaceError error) {
                                        Log.e(TAG, "ERROR! " + error.toString());
                                    }
                                }, audienceCode);
                            }
                        });
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mCameraNum < 2) {
            toast("未检测到2个摄像头");
        } else {
            try {
                startCameraPreview();
                mCamera[1] = Camera.open(1);
                mPreview[1].setCamera(mCamera[1], PREFER_WIDTH, PREFER_HEIGHT);
                mCamera[1].setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        dealIr(data);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        ScanGun.setMaxKeysInterval(50);
        mScanGun = new ScanGun(this);

    }

    private void startCameraPreview() {
        // 设置前置摄像头
        // CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_FRONT);
        // 设置后置摄像头
        //  CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_FACING_BACK);
        // 设置USB摄像头
        CameraPreviewManager.getInstance().setCameraFacing(CameraPreviewManager.CAMERA_USB);

        CameraPreviewManager.getInstance().startPreview(this, mPreviewView,
                PREFER_WIDTH, PREFER_HEIGHT, new CameraDataCallback() {
                    @Override
                    public void onGetCameraData(byte[] data, Camera camera, int width, int height) {
                        // 摄像头预览数据进行人脸检测
                        dealRgb(data);
                    }
                });
    }

    @Override
    protected void onDestroy() {
//        EventBus.getDefault().unregister(this);
//        if (vbarThread != null) {
//            vbarThread.interrupt();
//        }
//        if (vbar != null) {
//            vbar.closeDev();
//        }

        unregisterReceiver(); // TODO

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        //如果不是因为请求USB权限导致的Pause，才释放资源
        Log.d(TAG, "onPause isReqUSBPermission?" + isReqUSBPermission);

        if (mCameraNum > 0) {
            for (int i = 0; i < mCameraNum; i++) {
                if (mCamera[i] != null) {
                    mCamera[i].setPreviewCallback(null);
                    mCamera[i].stopPreview();
                    mPreview[i].release();
                    mCamera[i].release();
                    mCamera[i] = null;
                }
            }
        }
        if (isReqUSBPermission) {
            isReqUSBPermission = false;
        } else {
            //退出前要安全释放资源
//            uninit();
        }
        super.onPause();
    }

//    private void uninit() {
//        //退出前要安全释放资源
//        if (mReaderPresenter != null) {
//            mReaderPresenter.stopReadcard();
//            mReaderPresenter.release();
//            mReaderPresenter = null;
//        }
////        uninitBT();
//    }

    private void dealRgb(byte[] data) {
        rgbData = data;
        if (isQrFaceDetect) {
            faceDetect();
        } else {
            checkData();
        }
    }

    private void dealIr(byte[] data) {
        irData = data;
        if (isQrFaceDetect) {
            faceDetect();
        } else {
            checkData();
        }
    }


    private synchronized void checkData() {
        if (rgbData != null && irData != null) {
            if (livenessShowIv.getDrawable() != null || hintShowIv.getDrawable() != null) {
                FaceSDKManager.getInstance().onDetectCheck(rgbData, irData, null,
                        PREFER_HEIGHT, PREFER_WIDTH, 3, new FaceDetectCallBack() {
                            @Override
                            public void onFaceDetectCallback(final LivenessModel livenessModel) {
                                checkResult(livenessModel);
                            }

                            @Override
                            public void onTip(int code, String msg) {

                            }

                            @Override
                            public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                                showFrame(livenessModel);
                            }
                        });

                rgbData = null;
                irData = null;
            }
        }
    }

    // 预览模式
    private void checkResult(final LivenessModel livenessModel) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!checkable || livenessModel == null) {
                    livenessTipsFailRl.setVisibility(View.GONE);
                } else {
                    BDFaceImageInstance bdFaceImageInstance = livenessModel.getBdFaceImageInstance();

                    FaceSDKManager.getInstance().onFeatureCheck(bdFaceImageInstance, livenessModel.getLandmarks(),
                            BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO, new FaceFeatureCallBack() {

                                @Override
                                public void onFaceFeatureCallBack(float featureSize,
                                                                  byte[] feature, long time) {
                                    firstFeature = feature;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            long startCompareTime = System.currentTimeMillis();

                                            score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                                                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO,
                                                    firstFeature, secondFeature, true);
                                            if (!isDevelopment) {
                                                layoutCompareStatus.setVisibility(View.GONE);
                                                livenessTipsFailRl.setVisibility(View.VISIBLE);
                                                if (isFace) {
                                                    livenessTipsFailTv.setText("上传图片不包含人脸");
                                                    livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                                                    livenessTipsPleaseFailTv.setText("无法进行人证比对");
                                                    livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                                } else {
                                                    rgbLivenessScore = livenessModel.getRgbLivenessScore();
                                                    nirLivenessScore = livenessModel.getIrLivenessScore();
                                                    if (rgbLivenessScore > rgbLiveScore && nirLivenessScore >
                                                            nirLiveScore) {
                                                        if (score > SingleBaseConfig.getBaseConfig().getIdThreshold()) {
                                                            livenessTipsFailTv.setText("人证核验通过");
                                                            livenessTipsFailTv.setTextColor(Color.parseColor("#FF00BAF2"));
                                                            livenessTipsPleaseFailTv.setText("识别成功");
                                                            livenessTipsFailIv.setImageResource(R.mipmap.tips_success);

                                                            if (System.currentTimeMillis() - mLastOpenTime >= USER_SCAN_INTERVAL) {
                                                                openDoor(mUser.getItemEId(), pclass); // TODO
                                                            }

                                                        } else {
                                                            livenessTipsFailTv.setText("人证核验未通过");
                                                            livenessTipsFailTv.setTextColor(
                                                                    Color.parseColor("#FFFEC133"));
                                                            livenessTipsPleaseFailTv.setText("请上传正面人脸照片");
                                                            livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                                        }
                                                    } else {
                                                        livenessTipsFailTv.setText("人证核验未通过");
                                                        livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
                                                        livenessTipsPleaseFailTv.setText("请上传正面人脸照片");
                                                        livenessTipsFailIv.setImageResource(R.mipmap.tips_fail);
                                                    }
                                                }

                                            }

                                            mHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mTvIDCardNo.setText("");
                                                    mTvUserName.setText("");
                                                    livenessShowIv.setImageBitmap(null);
                                                    checkable = false;
                                                }
                                            }, 5 * 1000);
                                        }
                                    });
                                }
                            });
                }
            }
        });
    }

    private void openDoor(final String eid, final String pclass) {
        try {
            final FileOutputStream fos1 = new FileOutputStream("/sys/exgpio/relay1");
            fos1.write("1".getBytes());
            Log.e(TAG, "OPEN");

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        fos1.write("0".getBytes());
                        Log.e(TAG, "CLOSE");
                        fos1.close();
                        mLastOpenTime = System.currentTimeMillis();

                        long deltaTime = System.currentTimeMillis() - mUserScanTime;
                        Log.e(TAG, "Delta: " + deltaTime);
                        if (deltaTime >= USER_SCAN_INTERVAL) {
                            mUserScanTime = System.currentTimeMillis();
                            HttpRequester.uploadPassRecord(new OnResultListener<String>() {
                                @Override
                                public void onResult(String result) {
                                    Log.e(TAG, result);
                                }

                                @Override
                                public void onError(FaceError error) {
                                    Log.e(TAG, error.toString());
                                }
                            }, eid, pclass, mUser, mIdCardPhoto);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void faceDetect() {
        if (mCollectSuccess) {
            isQrFaceDetect = false;
            return;
        }

        // 摄像头预览数据进行人脸检测
        FaceSDKManager.getInstance().onDetectCheck(rgbData, irData, null, PREFER_HEIGHT,
                PREFER_WIDTH, 3, 2, new FaceDetectCallBack() {
                    @Override
                    public void onFaceDetectCallback(LivenessModel livenessModel) {
                        checkFaceBound(livenessModel);
                    }

                    @Override
                    public void onTip(int code, final String msg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                mFaceRoundProView.setTipText("保持面部在取景框内");
//                                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_red);
                            }
                        });
                    }

                    @Override
                    public void onFaceDetectDarwCallback(LivenessModel livenessModel) {
                        showFrame(livenessModel);
                    }
                });
    }


    private void checkFaceBound(final LivenessModel livenessModel) {
        // 当未检测到人脸UI显示
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (livenessModel == null || livenessModel.getFaceInfo() == null) {
//                    mFaceRoundProView.setTipText("保持面部在取景框内");
//                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_red);
                    return;
                }

                mPointXY[0] = livenessModel.getFaceInfo().centerX;   // 人脸X坐标
                mPointXY[1] = livenessModel.getFaceInfo().centerY;   // 人脸Y坐标
                mPointXY[2] = livenessModel.getFaceInfo().width;     // 人脸宽度
                mPointXY[3] = livenessModel.getFaceInfo().height;    // 人脸高度

                FaceOnDrawTexturViewUtil.converttPointXY(mPointXY, mPreviewView,
                        livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);


                float leftLimitX = 0;
                float rightLimitX = mPreviewView.getWidth();
                float topLimitY = 0;
                float bottomLimitY = mPreviewView.getHeight();
                float previewWidth = mPreviewView.getWidth();

                Log.e("mPointXY", "previewWidth: " + previewWidth);

                if (mPointXY[2] < 50 || mPointXY[3] < 50) {
                    // 释放内存
                    Log.e("mPointXY", "请向前靠近镜头");
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

                if (mPointXY[2] > previewWidth || mPointXY[3] > previewWidth) {
//                    mFaceRoundProView.setTipText("请向后远离镜头");
//                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_red);
                    // 释放内存
                    Log.e("mPointXY", "请向后远离镜头");
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

                if (mPointXY[0] - mPointXY[2] / 2 < leftLimitX
                        || mPointXY[0] + mPointXY[2] / 2 > rightLimitX
                        || mPointXY[1] - mPointXY[3] / 2 < topLimitY
                        || mPointXY[1] + mPointXY[3] / 2 > bottomLimitY) {
//                    mFaceRoundProView.setTipText("保持面部在取景框内");
                    Log.e("mPointXY", "保持面部在取景框内");
//                    mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_red);
                    // 释放内存
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

//                mFaceRoundProView.setTipText("请保持面部在取景框内");
//                mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_blue);
                // 检验活体分值
                checkLiveScore(livenessModel);
            }
        });
    }

    /**
     * 检验活体分值
     *
     * @param livenessModel LivenessModel实体
     */
    private void checkLiveScore(LivenessModel livenessModel) {
        if (livenessModel == null || livenessModel.getFaceInfo() == null) {
//            mFaceRoundProView.setTipText("保持面部在取景框内");
            return;
        }

        float rgbLivenessScore = livenessModel.getRgbLivenessScore();
        float irLivenessScore = livenessModel.getIrLivenessScore();
        float liveThreadHold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        float liveIrThreadHold = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        Log.e(TAG, "score = " + rgbLivenessScore + ", ns = " + irLivenessScore);
        if (rgbLivenessScore < liveThreadHold || irLivenessScore < liveIrThreadHold) {
//            mFaceRoundProView.setTipText("活体检测未通过");
//            mFaceRoundProView.setBitmapSource(R.mipmap.ic_loading_red);
            // 释放内存
            destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
            return;
        }
        // 提取特征值
        getFeatures(livenessModel);
    }

    /**
     * 提取特征值
     *
     * @param model 人脸数据
     */
    private void getFeatures(final LivenessModel model) {
        if (model == null) {
            return;
        }

        float ret = model.getFeatureCode();
        Log.e("mPointXY", "ret: " + ret);
        displayCompareResult(ret, model.getFeature(), model);
    }

    private void displayCompareResult(float ret, byte[] faceFeature, LivenessModel model) {
        if (model == null) {
            return;
        }

        // 特征提取成功
        toast("人脸信息提取成功");
        if (ret == 128) {
            BDFaceImageInstance bdFaceImageInstance = model.getBdFaceImageInstance();
            if (bdFaceImageInstance == null) {
                return;
            }

            Bitmap bitmap = BitmapUtils.getInstaceBmp(bdFaceImageInstance);
            mDetectedBitmap = BitmapUtils.scale(bitmap, 0.5F);
            // 获取头像
            if (mDetectedBitmap != null) {
                mCollectSuccess = true;
                livenessShowIv.setImageBitmap(mDetectedBitmap);

                HttpRequester.uploadIDPhoto(new OnResultListener<String>() {
                    @Override
                    public void onResult(String result) {
                        Log.e(TAG, result);
                        if (result.equals("0")) {
                            // 如果更新成功，则人脸识别 TODO
                            toast("上传脸部照片成功，请重扫二维码");
                        } else {
                            toast("上传脸部照片失败，errCode: " + result);
                        }
                    }

                    @Override
                    public void onError(FaceError error) {
                        toast(error.getErrorMessage());
                        Log.e(TAG, error.getErrorMessage());
                    }
                }, mDetectedBitmap, mUser);
            }
            bdFaceImageInstance.destory();

            for (int i = 0; i < faceFeature.length; i++) {
                mFeatures[i] = faceFeature[i];
            }

        }
    }

    private void destroyImageInstance(BDFaceImageInstance imageInstance) {
        if (imageInstance != null) {
            imageInstance.destory();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_setting:
                startActivity(new Intent(mContext, SettingMainActivity.class));
                finish();
                break;

        }
    }


    private void toast(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toastLong(final String tip) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, tip, Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * 绘制人脸框
     */
    private void showFrame(final LivenessModel model) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Canvas canvas = mDrawDetectFaceView.lockCanvas();
                if (canvas == null) {
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }

                if (!checkable || model == null) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                FaceInfo[] faceInfos = model.getTrackFaceInfo();
                if (faceInfos == null || faceInfos.length == 0) {
                    // 清空canvas
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mDrawDetectFaceView.unlockCanvasAndPost(canvas);
                    return;
                }
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                FaceInfo faceInfo = faceInfos[0];

                rectF.set(FaceOnDrawTexturViewUtil.getFaceRectTwo(faceInfo));
                // 检测图片的坐标和显示的坐标不一样，需要转换。
                FaceOnDrawTexturViewUtil.mapFromOriginalRect(rectF,
                        mPreviewView, model.getBdFaceImageInstance());
                if (score < SingleBaseConfig.getBaseConfig().getIdThreshold()
                        || rgbLivenessScore < SingleBaseConfig.getBaseConfig().getRgbLiveScore()
                        || nirLivenessScore < SingleBaseConfig.getBaseConfig().getNirLiveScore()) {
                    paint.setColor(Color.parseColor("#FEC133"));
                    paintBg.setColor(Color.parseColor("#FEC133"));
                } else {
                    if (isFace) {
                        paint.setColor(Color.parseColor("#FEC133"));
                        paintBg.setColor(Color.parseColor("#FEC133"));
                    } else {
                        paint.setColor(Color.parseColor("#00baf2"));
                        paintBg.setColor(Color.parseColor("#00baf2"));
                    }
                }
                paint.setStyle(Paint.Style.STROKE);
                paintBg.setStyle(Paint.Style.STROKE);
                // 画笔粗细
                paint.setStrokeWidth(8);
                // 设置线条等图形的抗锯齿
                paint.setAntiAlias(true);
                paintBg.setStrokeWidth(13);
                paintBg.setAlpha(90);
                // 设置线条等图形的抗锯齿
                paintBg.setAntiAlias(true);
                if (faceInfo.width > faceInfo.height) {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                        canvas.drawCircle(mPreviewView.getWidth() - rectF.centerX(),
                                rectF.centerY(), rectF.width() / 2 - 8, paintBg);
                        canvas.drawCircle(mPreviewView.getWidth() - rectF.centerX(),
                                rectF.centerY(), rectF.width() / 2, paint);
                    } else {
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.width() / 2 - 8, paintBg);
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.width() / 2, paint);
                    }

                } else {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                        canvas.drawCircle(mPreviewView.getWidth() - rectF.centerX(),
                                rectF.centerY(), rectF.height() / 2 - 8, paintBg);
                        canvas.drawCircle(mPreviewView.getWidth() - rectF.centerX(),
                                rectF.centerY(), rectF.height() / 2, paint);
                    } else {
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.height() / 2 - 8, paintBg);
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(),
                                rectF.height() / 2, paint);
                    }
                }
                // 清空canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }


    private void updateIDCardInfo(int retType) {
        final long nTickStart = System.currentTimeMillis();
        final long nTickUsed = (System.currentTimeMillis() - nTickStart);
        if (retType == 1) {
            final IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvIDCardNo.setText(idCardInfo.getId());
                    mTvUserName.setText(idCardInfo.getName());
                    if (idCardInfo.getPhotolength() > 0) {
                        byte[] buf = new byte[WLTService.imgLength];
                        if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                            livenessShowIv.setImageBitmap(IDPhotoHelper.Bgr2Bitmap(buf));
                        }
                    }

                }
            });
        } else if (retType == 2) {
            final IDPRPCardInfo idprpCardInfo = idCardReader.getLastPRPIDCardInfo();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvIDCardNo.setText(idprpCardInfo.getId());
                    mTvUserName.setText(idprpCardInfo.getCnName());
                    if (idprpCardInfo.getPhotolength() > 0) {
                        byte[] buf = new byte[WLTService.imgLength];
                        if (1 == WLTService.wlt2Bmp(idprpCardInfo.getPhoto(), buf)) {
                            livenessShowIv.setImageBitmap(IDPhotoHelper.Bgr2Bitmap(buf));
                        }
                    }
                }
            });
        } else if (retType == 3) {
            final IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvIDCardNo.setText(idCardInfo.getId());
                    mTvUserName.setText(idCardInfo.getName());
                    if (idCardInfo.getPhotolength() > 0) {
                        byte[] buf = new byte[WLTService.imgLength];
                        if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                            livenessShowIv.setImageBitmap(IDPhotoHelper.Bgr2Bitmap(buf));
                        }
                    }
                }
            });
        }

    }

    @Override
    public void onScanFinish(String audienceCode) {
        Log.e("OnScanFinish", audienceCode);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Log.d("jason","onKeyDown");
        //  Log.d("jason","main  KeyCode :  " + keyCode );

        if (!isQrFaceDetect)
            return super.onKeyDown(keyCode, event);

        mScanGun.isScanning(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 拦截物理键盘事件
        //  Log.d("jason","dispatchKeyEvent KeyCode :  " + event.getKeyCode() );

        if (!isQrFaceDetect)
            return super.dispatchKeyEvent(event);

        if (event.getKeyCode() > 6) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {//将键盘的删除键传递下去
                    return super.dispatchKeyEvent(event);
                }
                this.onKeyDown(event.getKeyCode(), event);
            }
        } else {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {//将键盘的返回事件传递下去
                return super.dispatchKeyEvent(event);
            }
        }
        return true;
    }
}
