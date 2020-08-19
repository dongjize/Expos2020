package com.baidu.idl.face.main.activity.setting;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.face.main.utils.PWTextUtils;
import com.baidu.idl.face.main.R;

import me.jessyan.autosize.internal.CustomAdapt;

public class SettingMainActivity extends BaseActivity {

    //    private LinearLayout topliner;
    // 识别模型和模态
    private LinearLayout configRecognizeModelAndModal;
    // 活体检测模态
    private LinearLayout configLiveAbout;
    private LinearLayout showIdText;
    private LinearLayout showLiveText;
    private LinearLayout showRMText;
    private ImageView cwRecgnizeModel;
    // 阀值设定
    private LinearLayout configThreshold;
    // 质量检测设置
    private LinearLayout configQualtify;
    // 最小检测人脸
    private LinearLayout configMinFace;
    // 人脸检测角度
    private LinearLayout configFaceDetectAngle;
    // 视频流回显角度
    private LinearLayout configDisplayAngle;
    // 镜像设置
    private LinearLayout configMirror;
    // 版本信息
    private LinearLayout configVersionMessage;
    // api url
    private LinearLayout configApiUrl;

    // 镜头类型弹窗
    private PopupWindow recognizeModel;



    private LinearLayout configPopREBack;

    private View popShowRely;

    private View recognizeTypeView;
    private RadioButton crtIdphoto;
    private RadioButton crtLivephoto;
    // RGB(证件照)
    private TextView tvSettingRMAM;
    // RGB+Depth活体
    private TextView tvSettingLiveAbout;
    // 质量检测设置
    private TextView tvSettingQualtify;
    // 最小检测人脸
    private TextView tvSettingMinFace;
    // 人脸检测角度
    private TextView tvSettingFaceDetectAngle;
    // 视频流回显角度
    private TextView tvSettingDisplayAngle;
    // 有效日期
    private TextView tvSettingEffectiveDate;

    private static final int zero = 0;
    private static final int one = 1;
    private static final int two = 2;
    private static final int three = 3;
    private static final int four = 4;

    private Button cwIdPhoto;
    private Button cwLivePhoto;

    private TextView cwShowIdText;
    private TextView cwShowLiveText;
    private TextView cwRMText;
    private ImageView settingmiManBack;
    private RelativeLayout configLayout;
    private float density;
    private int recognizeModelHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

//        topliner = findViewById(R.id.topliner);
//        setBarColor();
//        setWhiteStatusBarColor(this);
//        setBarLayout(topliner);

        density = getDensity();
        init();
        initPopupWindow();
    }

    public void init() {
        density = getDensity();
        configLayout = findViewById(R.id.configLayout);
        settingmiManBack = findViewById(R.id.settingmianback);
        popShowRely = findViewById(R.id.popShowRely);

        configRecognizeModelAndModal = findViewById(R.id.configRecognizeModelAndModal);
        configLiveAbout = findViewById(R.id.configLiveAbout);

        recognizeTypeView = LayoutInflater.from(this).inflate(R.layout.config_recognizetype, null);
        crtIdphoto = recognizeTypeView.findViewById(R.id.crtIdphoto);
        crtLivephoto = recognizeTypeView.findViewById(R.id.crtLivephoto);
        configPopREBack = (LinearLayout) recognizeTypeView.findViewById(R.id.configPopREBack);
        cwIdPhoto = recognizeTypeView.findViewById(R.id.cwIdPhoto);
        cwLivePhoto = recognizeTypeView.findViewById(R.id.cwLivePhoto);
        showIdText = recognizeTypeView.findViewById(R.id.showIdText);
        showLiveText = recognizeTypeView.findViewById(R.id.showLiveText);
        cwRecgnizeModel = recognizeTypeView.findViewById(R.id.cwRecgnizeModel);
        showRMText = recognizeTypeView.findViewById(R.id.showRMText);
        cwShowIdText = recognizeTypeView.findViewById(R.id.cwShowIdText);
        cwShowLiveText = recognizeTypeView.findViewById(R.id.cwShowLiveText);
        cwRMText = recognizeTypeView.findViewById(R.id.cwRMText);

        cwShowIdText.setText(getString(R.string.cw_idphoto));
        cwShowLiveText.setText(getString(R.string.cw_livephoto));
        cwRMText.setText(getString(R.string.cw_rgcognizemodelandtype));

        configThreshold = findViewById(R.id.configThreshold);
        configQualtify = findViewById(R.id.configQualtify);
        configMinFace = findViewById(R.id.configMinFace);
        configFaceDetectAngle = findViewById(R.id.configFaceDetectAngle);
        configDisplayAngle = findViewById(R.id.configDisplayAngle);
        configMirror = findViewById(R.id.configMirror);
        configVersionMessage = findViewById(R.id.configVersionMessage);
        configApiUrl = findViewById(R.id.configApiUrl);

        tvSettingEffectiveDate = findViewById(R.id.tvSettingEffectiveDate);

        tvSettingRMAM = findViewById(R.id.tvSettingRMAM);
        tvSettingLiveAbout = findViewById(R.id.tvSettingLiveAbout);
        tvSettingQualtify = findViewById(R.id.tvSettingQualtify);
        tvSettingMinFace = findViewById(R.id.tvSettingMinFace);
        tvSettingFaceDetectAngle = findViewById(R.id.tvSettingFaceDetectAngle);
        tvSettingDisplayAngle = findViewById(R.id.tvSettingDisplayAngle);
        tvSettingEffectiveDate = findViewById(R.id.tvSettingEffectiveDate);
        tvSettingEffectiveDate.setText("有效期至" + FaceSDKManager.getInstance().getLicenseData(this));
        if (SingleBaseConfig.getBaseConfig().getActiveModel() == 1) {
            tvSettingRMAM.setText("RGB(生活照)");
            crtIdphoto.setChecked(false);
            crtLivephoto.setChecked(true);
        }
        if (SingleBaseConfig.getBaseConfig().getActiveModel() == 2) {
            tvSettingRMAM.setText("RGB(证件照)");
            crtIdphoto.setChecked(true);
            crtLivephoto.setChecked(false);
        }
        setClickListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int liveTypeValue = SingleBaseConfig.getBaseConfig().getType();
        if (liveTypeValue == one) {
            tvSettingLiveAbout.setText("不使用活体检测");
        }
        if (liveTypeValue == two) {
            tvSettingLiveAbout.setText("RGB单目活体");
        }
        if (liveTypeValue == three) {
            tvSettingLiveAbout.setText("RGB+NIR活体");
        }
        if (liveTypeValue == four) {
            tvSettingLiveAbout.setText("RGB+Depth活体");
        }
        if (SingleBaseConfig.getBaseConfig().isQualityControl()) {
            tvSettingQualtify.setText("开启");
        } else {
            tvSettingQualtify.setText("关闭");
        }
        tvSettingMinFace.setText(SingleBaseConfig.getBaseConfig().getMinimumFace() + "");
        tvSettingFaceDetectAngle.setText(SingleBaseConfig.getBaseConfig().getDetectDirection() + "");
        tvSettingDisplayAngle.setText(SingleBaseConfig.getBaseConfig().getVideoDirection() + "");
//        tvSettingEffectiveDate.setText("");
        settingmiManBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void setClickListener() {
        recognizeTypeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cwRecgnizeModel.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLivePhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwIdPhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                showIdText.setVisibility(View.GONE);
                showLiveText.setVisibility(View.GONE);
                showRMText.setVisibility(View.GONE);
            }
        });
        cwIdPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showIdText.getVisibility() == View.VISIBLE) {
                    cwIdPhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                    showIdText.setVisibility(View.GONE);
                    return;
                }
                cwIdPhoto.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                cwRecgnizeModel.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLivePhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                showLiveText.setVisibility(View.GONE);
                showRMText.setVisibility(View.GONE);

                showIdText.setVisibility(View.VISIBLE);
            }
        });
        cwLivePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showLiveText.getVisibility() == View.VISIBLE) {
                    cwLivePhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                    showLiveText.setVisibility(View.GONE);
                    return;
                }
                cwIdPhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRecgnizeModel.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLivePhoto.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                showIdText.setVisibility(View.GONE);
                showRMText.setVisibility(View.GONE);

                showLiveText.setVisibility(View.VISIBLE);
            }
        });
        cwRecgnizeModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showRMText.getVisibility() == View.VISIBLE) {
                    cwRecgnizeModel.setBackground(getDrawable(R.mipmap.icon_setting_question));
                    showRMText.setVisibility(View.GONE);
                    return;
                }
                cwIdPhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLivePhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRecgnizeModel.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                showIdText.setVisibility(View.GONE);
                showLiveText.setVisibility(View.GONE);

                showRMText.setVisibility(View.VISIBLE);
            }
        });


        configPopREBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showIdText.setVisibility(View.GONE);
                showLiveText.setVisibility(View.GONE);
                showRMText.setVisibility(View.GONE);
                cwIdPhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRecgnizeModel.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLivePhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                PWTextUtils.closePop(getWindow());
                recognizeModel.dismiss();
                screnBackground(1.0f);
            }
        });

        crtIdphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crtIdphoto.setChecked(true);
                crtLivephoto.setChecked(false);
                SingleBaseConfig.getBaseConfig().setActiveModel(2);
                ConfigUtils.modityJson();
                PWTextUtils.closePop(getWindow());
                cwIdPhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRecgnizeModel.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLivePhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                recognizeModel.dismiss();
            }
        });
        crtLivephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crtIdphoto.setChecked(false);
                crtLivephoto.setChecked(true);
                SingleBaseConfig.getBaseConfig().setActiveModel(1);
                ConfigUtils.modityJson();
                PWTextUtils.closePop(getWindow());
                cwIdPhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRecgnizeModel.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLivePhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                recognizeModel.dismiss();
            }
        });


        configRecognizeModelAndModal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recognizeModel.showAtLocation(popShowRely, Gravity.BOTTOM, 0, 0);
                screnBackground(0.8f);
            }
        });

        configLiveAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingMainActivity.this, FaceLivinessTypeActivity.class);
                startActivity(intent);
            }
        });

        configThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingMainActivity.this, ThresholdActivity.class);
                startActivity(intent);
            }
        });

        configQualtify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingMainActivity.this, QualityControlActivity.class);
                startActivity(intent);
            }
        });

        configMinFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingMainActivity.this, MinFaceActivity.class);
                startActivity(intent);
            }
        });

        configFaceDetectAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingMainActivity.this, FaceDetectAngleActivity.class);
                startActivity(intent);
            }
        });

        configDisplayAngle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingMainActivity.this, CameraDisplayAngleActivity.class);
                startActivity(intent);
            }
        });

        configMirror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingMainActivity.this, MirrorSettingActivity.class);
                startActivity(intent);
            }
        });

        configVersionMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingMainActivity.this, VersionMessageActivity.class);
                startActivity(intent);
            }
        });

        configApiUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingMainActivity.this, ApiUrlSettingActivity.class);
                startActivity(intent);
            }
        });
    }

    public void initPopupWindow() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // 壁虎
        if (1 >= density) {
            recognizeModelHeight = (int) (0.39 * wm.getDefaultDisplay().getHeight());
        }
        // 3399
        if (2 >= density && density > 1) {
            recognizeModelHeight = (int) (0.41 * wm.getDefaultDisplay().getHeight());
        }
        // 部分手机
        if (density > 2) {
            recognizeModelHeight = (int) (0.32 * wm.getDefaultDisplay().getHeight());
        }


        recognizeModel = new PopupWindow();
        recognizeModel.setAnimationStyle(R.style.configPopupWindow);
        recognizeModel.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        recognizeModel.setHeight(recognizeModelHeight);

        // 必须设置背景在壁虎上点击外部才会消失
        recognizeModel.setBackgroundDrawable(new BitmapDrawable());
        recognizeModel.setFocusable(true);
        // 设置同意在外点击消失
        recognizeModel.setOutsideTouchable(true);
        recognizeModel.setContentView(recognizeTypeView);
        recognizeModel.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

                screnBackground(1.0f);
                cwRecgnizeModel.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLivePhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwIdPhoto.setBackground(getDrawable(R.mipmap.icon_setting_question));
                showIdText.setVisibility(View.GONE);
                showLiveText.setVisibility(View.GONE);
                showRMText.setVisibility(View.GONE);
                if (SingleBaseConfig.getBaseConfig().getActiveModel() == 1) {
                    tvSettingRMAM.setText("RGB(生活照)");
                } else {
                    tvSettingRMAM.setText("RGB(证件照)");
                }
            }
        });

    }

//    /**
//     * 黑色字体状态栏
//     *
//     * @param activity
//     */
//    public static void setLightStatusBarColor(Activity activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            activity.getWindow()
//                    .getDecorView()
//                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }
//    }
//
//    /**
//     * 白色字体状态栏
//     *
//     * @param activity
//     */
//    public static void setWhiteStatusBarColor(Activity activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            activity.getWindow()
//                    .getDecorView()
//                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        }
//    }

//    public void setBarLayout(View objectView) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Resources resources = this.getResources();
//            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
//            int height = resources.getDimensionPixelSize(resourceId);
//
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            lp.setMargins(0, height, 0, 0);
//            objectView.setLayoutParams(lp);
//        }
//    }

    // 屏幕背景变暗
    private void screnBackground(float colorScore) {
        if (colorScore == 0.8f) {
            configLayout.setBackgroundColor(Color.parseColor("#000000"));
        } else {
            configLayout.setBackgroundColor(Color.parseColor("#121212"));
        }
        configLayout.setAlpha(colorScore);
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        lp.alpha = colorScore;
//        getWindow().setAttributes(lp);
    }

    private float getDensity() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.density;
    }

}

