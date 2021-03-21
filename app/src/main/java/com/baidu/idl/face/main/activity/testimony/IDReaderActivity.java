package com.baidu.idl.face.main.activity.testimony;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.R;
import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.model.AppendLogEvent;
import com.baidu.idl.face.main.activity.model.DeviceParamBean;
import com.baidu.idl.face.main.activity.presenter.ReaderPresenter;
import com.baidu.idl.face.main.activity.setting.SettingMainActivity;
import com.baidu.idl.face.main.activity.vbar.Vbar;
import com.baidu.idl.face.main.activity.view.IReaderView;
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
import com.routon.plsy.reader.sdk.common.Info;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class IDReaderActivity extends BaseActivity implements View.OnClickListener, IReaderView {
    private static final String TAG = IDReaderActivity.class.getSimpleName();

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
    //    private ImageView testimonyPreviewLineIv;
//    private ImageView testimonyDevelopmentLineIv;
    // 图片越大，性能消耗越大，也可以选择640*480， 1280*720
    private static final int PREFER_WIDTH = SingleBaseConfig.getBaseConfig().getRgbAndNirWidth();
    private static final int PREFER_HEIGHT = SingleBaseConfig.getBaseConfig().getRgbAndNirHeight();
    // 摄像头采集数据
    private volatile byte[] rgbData;
    private volatile byte[] irData;
    private ImageView idCardPhotoIv; //用户照片
    private ImageView livePhotoIv; //现场照片
    private ImageView hintShowIv;
    private TextView tv_nir_live_score;
    private RelativeLayout livenessTipsFailRl;
    private TextView livenessTipsFailTv;
    private TextView livenessTipsPleaseFailTv;
    private TextView tv_nir_live_time;
    private TextureView irTexture;
    float score = 0;

    // 定义一个变量判断是预览模式还是开发模式
    boolean isDevelopment = false;
    //    private LinearLayout livenessButtomLl;
    private RelativeLayout kaifaRelativeLayout;
    private RelativeLayout test_nir_rl;
    private RelativeLayout test_rgb_ir_rl;
    private TextView hintAdainTv;
    private View view;
    private RelativeLayout layoutCompareStatus;
    private TextView textCompareStatus;
    private ImageView test_nir_iv;
    private ImageView test_rgb_iv;
    private TextView tv_feature_time;
    private TextView tv_feature_search_time;
    private TextView tv_all_time;
    private RelativeLayout hintShowRl;
    private RelativeLayout developmentAddRl;

    private float rgbLiveScore;
    private float nirLiveScore;
    // 判断是否有人脸
    private boolean isFace = false;
    private ImageView livenessTipsFailIv;
    private float nirLivenessScore = 0.0f;
    private float rgbLivenessScore = 0.0f;

    private TextView mTvIDCardNo;
    private TextView mTvUserName;
    private ReaderPresenter mReaderPresenter;
    private boolean isReqUSBPermission = false;
    private Bitmap mUserCloudPhoto;
    private int mDeviceType = DeviceParamBean.DEV_TYPE_INNER_OR_HID;
    private Handler mHandler;
    private long mLastOpenTime = 0;
    private long mScanTime = 0;
    private User mUser;
    private String lastUserIdNo;
    private static final int USER_SCAN_INTERVAL = 3 * 1000;
    private boolean checkable = false;

    private Vbar vbar;
    private boolean state;
    private Thread vbarThread;
    //    private String eid;
    private String pclass;

    private float[] mPointXY = new float[4];
    private byte[] mFeatures = new byte[512];
    private Bitmap mDetectedBitmap;
    private boolean mCollectSuccess;
    private boolean isQrFaceDetect = false;

    private UsbManager mUsbManager;
    private static final String ACTION_USB_PERMISSION = "com.baidu.idl.face.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_ir_idreader);
        mContext = this;
        mHandler = new Handler();

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (mUsbManager != null) {
            for (UsbDevice device : mUsbManager.getDeviceList().values()) {
                Log.e("USB ==== ", "vid: " + device.getVendorId() + " pid:" + device.getProductId());
                if (device.getVendorId() == 3141 && device.getProductId() == 57616) {
                    Intent intent = new Intent(ACTION_USB_PERMISSION);
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
                    mUsbManager.requestPermission(device, mPermissionIntent);
                    Log.d(TAG, "IDCard device: checked");
                }
            }
        }

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

        registerReceiver();

    }

    private VBarBroadcastReceiver mUsbReceiver;

    private class VBarBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            Log.e("VBAR ===== ", device.getVendorId() + " " + device.getProductId());
            if (device.getVendorId() == 1317 && device.getProductId() == 42156) {
                switch (intent.getAction()) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED: // 插入USB设备
                        Toast.makeText(context, "VBAR ATTACHED", Toast.LENGTH_SHORT).show();
                        if (vbarThread != null) {
                            vbarThread.interrupt();
                        }
                        if (vbar != null) {
                            vbar.closeDev();
                        }
                        initVBar();
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED: // 拔出USB设备
                        Toast.makeText(context, "VBAR DETACHED", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mUsbReceiver = new VBarBroadcastReceiver();
        mContext.registerReceiver(mUsbReceiver, filter);
    }

    private void unregisterReceiver() {
        mContext.unregisterReceiver(mUsbReceiver);
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

        view = findViewById(R.id.mongolia_view);
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
        test_rgb_iv = findViewById(R.id.test_rgb_iv);
        // 开发模式buttom
        // 图片显示
        hintShowIv = findViewById(R.id.hint_showIv);
        // 重新上传
        hintAdainTv = findViewById(R.id.hint_adainTv);
        hintAdainTv.setOnClickListener(this);
        hintShowRl = findViewById(R.id.hint_showRl);
        // 上传图片
        ImageView DevelopmentAddIv = findViewById(R.id.Development_addIv);
        DevelopmentAddIv.setOnClickListener(this);
        developmentAddRl = findViewById(R.id.Development_addRl);
        // nir
        test_nir_rl = findViewById(R.id.test_nir_Rl);
        test_nir_rl.setVisibility(View.GONE);
        test_rgb_ir_rl = findViewById(R.id.test_rgb_ir_Rl);
        test_rgb_ir_rl.setVisibility(View.GONE);
        test_nir_iv = findViewById(R.id.test_nir_iv);
        // 提示
        layoutCompareStatus = findViewById(R.id.layout_compare_status);
        textCompareStatus = findViewById(R.id.text_compare_status);
        // 相似度分数
        tv_nir_live_time = findViewById(R.id.tv_nir_live_time);
        // 活体检测耗时
        tv_nir_live_score = findViewById(R.id.tv_nir_live_score);
        // 特征抽取耗时
        tv_feature_time = findViewById(R.id.tv_feature_time);
        // 特征比对耗时
        tv_feature_search_time = findViewById(R.id.tv_feature_search_time);
        // 总耗时
        tv_all_time = findViewById(R.id.tv_all_time);

        // ****************预览模式****************
        // 未通过提示
        livenessTipsFailRl = findViewById(R.id.liveness_tips_failRl);
        livenessTipsFailTv = findViewById(R.id.liveness_tips_failTv);
        livenessTipsPleaseFailTv = findViewById(R.id.liveness_tips_please_failTv);
        livenessTipsFailIv = findViewById(R.id.liveness_tips_failIv);
        idCardPhotoIv = findViewById(R.id.liveness_showIv);
        livePhotoIv = findViewById(R.id.iv_live_photo);

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
        Log.e("VBAR STATE: ", state + "");
        vbarThread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    final String audienceCode = vbar.getResultSingle();
                    if (audienceCode != null && System.currentTimeMillis() - mScanTime > USER_SCAN_INTERVAL) {

                        checkable = true;
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.e(TAG, audienceCode);
                                isQrFaceDetect = false;

                                HttpRequester.requestUserByAudienceCode(new OnResultListener<List<User>>() {
                                    @Override
                                    public void onResult(List<User> result) {
                                        pclass = "人证核验";
                                        if (result != null && result.size() > 0) {
                                            if (mUser != null) {
                                                lastUserIdNo = mUser.getIdCardNo();
                                            }
                                            // 如果两次身份证号不相同，或同一个user刷卡时间间隔3秒以上，则更新闸机open time
                                            if (lastUserIdNo != null && !lastUserIdNo.equals(result.get(0).getIdCardNo()) || System.currentTimeMillis() - mScanTime > USER_SCAN_INTERVAL) {
                                                mScanTime = 0;
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
                                                mUserCloudPhoto = BitmapUtils.base64ToBitmap(mUser.getImage());
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        idCardPhotoIv.setImageBitmap(mUserCloudPhoto);
                                                    }
                                                });
                                                FaceSDKManager.getInstance().personDetect(mUserCloudPhoto, secondFeature);
                                            } else {
                                                isQrFaceDetect = true;
                                            }
                                        } else {
                                            toast("该用户不存在");
                                            Arrays.fill(secondFeature, (byte) 0);
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
        vbarThread.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mReaderPresenter == null) {
            mReaderPresenter = new ReaderPresenter(this);
        }
        if (mDeviceType == DeviceParamBean.DEV_TYPE_INNER_OR_HID) {
            appendLog(AppendLogEvent.LOG_CODE_ANY, "startReadcard");
            DeviceParamBean devParamBean = new DeviceParamBean();
            devParamBean.setDeviceType(mDeviceType);
            mReaderPresenter.startReadcard(devParamBean);
        }

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

        initVBar();

    }

    private void startCameraPreview() {
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
        if (vbarThread != null) {
            vbarThread.interrupt();
        }
        if (vbar != null) {
            vbar.closeDev();
        }

        unregisterReceiver(); // TODO

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        //如果不是因为请求USB权限导致的Pause，才释放资源
        Log.d(TAG, "onPause isReqUSBPermission?" + isReqUSBPermission);

//      CameraPreviewManager.getInstance().stopPreview();
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
            uninit();
        }
        super.onPause();
    }

    private void uninit() {
        //退出前要安全释放资源
        if (mReaderPresenter != null) {
            mReaderPresenter.stopReadcard();
            mReaderPresenter.release();
            mReaderPresenter = null;
        }
//        uninitBT();
    }

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
            if (idCardPhotoIv.getDrawable() != null || hintShowIv.getDrawable() != null) {
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
                                public void onFaceFeatureCallBack(float featureSize, byte[] feature, long time) {
                                    firstFeature = feature;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                                                    BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO,
                                                    firstFeature, secondFeature, true);

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
                                                        BDFaceImageInstance image = livenessModel.getBdFaceImageInstance();

                                                        if (image != null) {
                                                            Bitmap livePhoto = BitmapUtils.scale(BitmapUtils.getInstaceBmp(image), 0.3F);
                                                            if (livePhotoIv.getDrawable() == null) {
                                                                livePhotoIv.setImageBitmap(livePhoto);
                                                            }
                                                            livenessTipsFailTv.setText("人证核验通过");
                                                            livenessTipsFailTv.setTextColor(Color.parseColor("#FF00BAF2"));
                                                            livenessTipsPleaseFailTv.setText("识别成功");
                                                            livenessTipsFailIv.setImageResource(R.mipmap.tips_success);
                                                            if (System.currentTimeMillis() - mLastOpenTime >= USER_SCAN_INTERVAL) {
                                                                openDoor(mUser.getItemEId(), pclass, livePhoto);
                                                            }
                                                            image.destory();
                                                        }

                                                    } else {
                                                        livenessTipsFailTv.setText("人证核验未通过");
                                                        livenessTipsFailTv.setTextColor(Color.parseColor("#FFFEC133"));
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

                                            mHandler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mTvIDCardNo.setText("");
                                                    mTvUserName.setText("");
                                                    if (idCardPhotoIv != null) {
                                                        idCardPhotoIv.setImageBitmap(null);
                                                    }
                                                    if (livePhotoIv != null) {
                                                        livePhotoIv.setImageDrawable(null);
                                                    }
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

    private void openDoor(final String eid, final String pclass, final Bitmap livePhoto) {
        try {
            final FileOutputStream fos1 = new FileOutputStream("/sys/exgpio/relay1");
            fos1.write("1".getBytes());
            Log.e(TAG, "OPEN");
            checkable = false;
            score = 0;

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        fos1.write("0".getBytes());
                        Log.e(TAG, "CLOSE");
                        fos1.close();
                        mLastOpenTime = System.currentTimeMillis();

                        long deltaTime = System.currentTimeMillis() - mScanTime;
                        Log.e(TAG, "Delta: " + deltaTime);
                        if (deltaTime >= USER_SCAN_INTERVAL) {
                            mScanTime = System.currentTimeMillis();
                            HttpRequester.uploadPassRecord(new OnResultListener<String>() {
                                @Override
                                public void onResult(String result) {
                                    Log.e(TAG, "通行成功");
                                }

                                @Override
                                public void onError(FaceError error) {
                                    Log.e(TAG, error.toString());
                                }
                            }, eid, pclass, mUser, livePhoto);
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
                    return;
                }

                mPointXY[0] = livenessModel.getFaceInfo().centerX;   // 人脸X坐标
                mPointXY[1] = livenessModel.getFaceInfo().centerY;   // 人脸Y坐标
                mPointXY[2] = livenessModel.getFaceInfo().width;     // 人脸宽度
                mPointXY[3] = livenessModel.getFaceInfo().height;    // 人脸高度

                FaceOnDrawTexturViewUtil.converttPointXY(mPointXY, mPreviewView, livenessModel.getBdFaceImageInstance(), livenessModel.getFaceInfo().width);


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
                    // 释放内存
                    Log.e("mPointXY", "请向后远离镜头");
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

                if (mPointXY[0] - mPointXY[2] / 2 < leftLimitX
                        || mPointXY[0] + mPointXY[2] / 2 > rightLimitX
                        || mPointXY[1] - mPointXY[3] / 2 < topLimitY
                        || mPointXY[1] + mPointXY[3] / 2 > bottomLimitY) {
                    Log.e("mPointXY", "保持面部在取景框内");
                    // 释放内存
                    destroyImageInstance(livenessModel.getBdFaceImageInstanceCrop());
                    return;
                }

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
            return;
        }

        float rgbLivenessScore = livenessModel.getRgbLivenessScore();
        float irLivenessScore = livenessModel.getIrLivenessScore();
        float liveThreadHold = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        float liveIrThreadHold = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        Log.e(TAG, "score = " + rgbLivenessScore + ", ns = " + irLivenessScore);
        if (rgbLivenessScore < liveThreadHold || irLivenessScore < liveIrThreadHold) {
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
                idCardPhotoIv.setImageBitmap(mDetectedBitmap);

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
                        canvas.drawCircle(mPreviewView.getWidth() - rectF.centerX(), rectF.centerY(), rectF.width() / 2 - 8, paintBg);
                        canvas.drawCircle(mPreviewView.getWidth() - rectF.centerX(), rectF.centerY(), rectF.width() / 2, paint);
                    } else {
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.width() / 2 - 8, paintBg);
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.width() / 2, paint);
                    }

                } else {
                    if (!SingleBaseConfig.getBaseConfig().getRgbRevert()) {
                        canvas.drawCircle(mPreviewView.getWidth() - rectF.centerX(), rectF.centerY(), rectF.height() / 2 - 8, paintBg);
                        canvas.drawCircle(mPreviewView.getWidth() - rectF.centerX(), rectF.centerY(), rectF.height() / 2, paint);
                    } else {
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.height() / 2 - 8, paintBg);
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.height() / 2, paint);
                    }
                }
                // 清空canvas
                mDrawDetectFaceView.unlockCanvasAndPost(canvas);
            }
        });
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }


    @Override
    public void onReadIDCardSuccess(final Info.IDCardInfo iDCardInfo) {
        isQrFaceDetect = false;
        pclass = "身份证比对";

        if (iDCardInfo != null) {
            flushIDCardInfo(iDCardInfo, true);

            HttpRequester.requestUserById(new OnResultListener<List<User>>() {
                @Override
                public void onResult(List<User> result) {
                    checkable = true;

                    if (result != null && result.size() > 0) {
                        if (mUser != null) {
                            lastUserIdNo = mUser.getIdCardNo();
                        }

                        // 如果两次身份证号不相同，或同一个user刷卡时间间隔3秒以上，则更新闸机open time
                        if (lastUserIdNo != null && !lastUserIdNo.equals(result.get(0).getIdCardNo()) || System.currentTimeMillis() - mScanTime > USER_SCAN_INTERVAL) {
                            mScanTime = 0;
                        }
                        mUser = result.get(0);

                        if (iDCardInfo.photo != null) {
                            mUserCloudPhoto = iDCardInfo.photo;
                            FaceSDKManager.getInstance().personDetect(mUserCloudPhoto, secondFeature);

                            if (TextUtils.isEmpty(mUser.getImage())) {
                                HttpRequester.uploadIDPhoto(new OnResultListener<String>() {
                                    @Override
                                    public void onResult(String result) {
                                        if (result.equals("0")) {
                                            // 如果更新成功，则人脸识别
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
                                }, iDCardInfo.photo, mUser);
                            }
                        } else {
                            toast("身份证照片读取失败");
                        }
                    } else {
                        toast("该用户不存在");
                        Arrays.fill(secondFeature, (byte) 0);
                    }
                }

                @Override
                public void onError(FaceError error) {
                    Log.e(TAG, "ERROR ===== " + error);
                }
            }, iDCardInfo.id);
        }

    }


    private void flushIDCardInfo(final Info.IDCardInfo idCardInfo, boolean display) {
        if (display) {
            if (idCardInfo.cardType == Info.ID_CARD_TYPE_CHINA) {// 身份证
                idCardPhotoIv.setImageBitmap(idCardInfo.photo);
                mTvIDCardNo.setText(idCardInfo.id);
                mTvUserName.setText(idCardInfo.name);
            } else if (idCardInfo.cardType == Info.ID_CARD_TYPE_FOREIGN) {// 外国人证
                mTvUserName.setText(idCardInfo.englishName);
            } else if (idCardInfo.cardType == Info.ID_CARD_TYPE_GAT) {// 港澳通行证
                mTvIDCardNo.setText(idCardInfo.passportNum);
                mTvUserName.setText(idCardInfo.name);
            }
        }
    }

    @Override
    public void onReadIDCardFailed() {
        Log.d(TAG, "read card fail");
    }

    @Override
    public void onCardRemoved() {
        Log.d(TAG, "card removed");
    }

    @Override
    public void appendLog(int code, String log) {

    }

    @Override
    public void onGotStartReadcardResult(int error_code, boolean has_inner_reader) {

    }

    @Override
    public void onReadACardSuccess(byte[] card_sn) {

    }

    @Override
    public void onReaderStopped() {
        Log.d(TAG, "onReaderStopped");
    }
}
