package com.baidu.idl.face.main.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.face.main.utils.PWTextUtils;
import com.baidu.idl.face.main.R;


/**
 * author : shangrong
 * date : two019/five/two7 six:four8 PM
 * description :活体检测模式
 */
public class FaceLivinessTypeActivity extends BaseActivity {


    private RadioGroup flsCameraType;
    private RadioButton fltZero;
    private RadioButton fltOne;
    private RadioButton fltTwo;
    private RadioButton fltThree;
    private RadioButton fltFour;
    private RadioButton fltFive;
    private RadioButton fltSix;
    private int liveTypeValue;
    private int cameraTypeValue;

    // 0:奥比中光Astra Mini、Mini S系列(结构光)
    private static final int zero = 0;
    // 1:奥比中光 Astra Pro 、Pro S 、蝴蝶（结构光）
    private static final int one = 1;
    // 2:奥比中光Atlas（结构光）
    private static final int two = 2;
    // 3:奥比中光大白、海燕(结构光)
    private static final int three = 3;
    // 4:奥比中光Deeyea(结构光)
    private static final int four = 4;
    // 5:华捷艾米A100S、A200(结构光)
    private static final int five = 5;
    // 6:Pico DCAM710(ToF)
    private static final int six = 6;

    private Button cwLivetype;
    private Button cwRgb;
    private Button cwRgbAndNir;
    private Button cwRgbAndDepth;
    private Button cwCameraType;

    private LinearLayout linerLiveTpye;
    private LinearLayout linerCameraType;
    private TextView tvLivType;
    private TextView textCameraType;

    private RadioGroup flsLiveType;
    private RadioButton flsNoLive;
    private RadioButton flsRgbLive;
    private RadioButton flsRgbAndNirLive;
    private RadioButton flsRgbAndDepthLive;
    private String msgTag = "";
    private int showWidth;
    private int showXLocation;
    private LinearLayout flRepresent;
    private View rgbView;
    private View rgbAndNirView;
    private View rgbAndDepthView;
//    private LinearLayout linerBarLiveDetectModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facelivinesstype);

//        linerBarLiveDetectModel = findViewById(R.id.linerbarlivedetectmodel);
//        setBarColor();
//        setLightStatusBarColor(this);
//        setBarLayout(linerBarLiveDetectModel);

        init();

    }

    public void init() {
        flRepresent = findViewById(R.id.flRepresent);
        rgbView = findViewById(R.id.rgbView);
        rgbAndNirView = findViewById(R.id.rgbAndNirView);
        rgbAndDepthView = findViewById(R.id.rgbAndDepthView);
        linerLiveTpye = findViewById(R.id.linerlivetpye);
        linerCameraType = findViewById(R.id.linercameratype);
        tvLivType = findViewById(R.id.tvlivetype);
        textCameraType = findViewById(R.id.textcameratype);

        cwLivetype = findViewById(R.id.cw_livetype);
        cwRgb = findViewById(R.id.cw_rgb);
        cwRgbAndNir = findViewById(R.id.cw_rgbandnir);
        cwRgbAndDepth = findViewById(R.id.cw_rgbanddepth);
        cwCameraType = findViewById(R.id.cw_cameratype);

        flsLiveType = findViewById(R.id.fls_live_type);
        flsNoLive = findViewById(R.id.fls_no_live);
        flsRgbLive = findViewById(R.id.fls_rgb_live);
        flsRgbAndNirLive = findViewById(R.id.fls_rgbandnir_live);
        flsRgbAndDepthLive = findViewById(R.id.fls_rgbanddepth_live);

        fltZero = findViewById(R.id.flt_zero);
        fltOne = findViewById(R.id.flt_one);
        fltTwo = findViewById(R.id.flt_two);
        fltThree = findViewById(R.id.flt_three);
        fltFour = findViewById(R.id.flt_four);
        fltFive = findViewById(R.id.flt_five);
        fltSix = findViewById(R.id.flt_six);
        flsCameraType = findViewById(R.id.fls_camera_type);
        ImageView flsSave = findViewById(R.id.fls_save);

        flsLiveType.setOnCheckedChangeListener(liveType);
        flsCameraType.setOnCheckedChangeListener(cameraType);

        liveTypeValue = SingleBaseConfig.getBaseConfig().getType();
        cameraTypeValue = SingleBaseConfig.getBaseConfig().getCameraType();

        if (liveTypeValue == one) {
            flsNoLive.setChecked(true);
            linerCameraType.setVisibility(View.GONE);
            flsCameraType.setVisibility(View.GONE);
        }
        if (liveTypeValue == two) {
            flsRgbLive.setChecked(true);
            linerCameraType.setVisibility(View.GONE);
            flsCameraType.setVisibility(View.GONE);
        }
        if (liveTypeValue == three) {
            flsRgbAndNirLive.setChecked(true);
            linerCameraType.setVisibility(View.GONE);
            flsCameraType.setVisibility(View.GONE);
        }
        if (liveTypeValue == four) {
            flsRgbAndDepthLive.setChecked(true);
            linerCameraType.setVisibility(View.VISIBLE);
            flsCameraType.setVisibility(View.VISIBLE);
            setlectCamera();
        }

        PWTextUtils.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                cwLivetype.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRgb.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRgbAndNir.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwRgbAndDepth.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwCameraType.setBackground(getDrawable(R.mipmap.icon_setting_question));
            }
        });

        flsSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                justify();
                ConfigUtils.modityJson();
                finish();
            }
        });

        showClickListener();
    }

    public void showClickListener() {
        cwLivetype.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgTag.equals(getString(R.string.cw_livedetecttype))) {
                    msgTag = "";
                    return;
                }
                msgTag = getString(R.string.cw_livedetecttype);
                cwLivetype.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(linerLiveTpye, tvLivType, FaceLivinessTypeActivity.this,
                        getString(R.string.cw_livedetecttype)
                        , showWidth, showXLocation);
            }
        });
        cwRgb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgTag.equals(getString(R.string.cw_rgblive))) {
                    msgTag = "";
                    return;
                }
                msgTag = getString(R.string.cw_rgblive);
                cwRgb.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(rgbView, rgbView, FaceLivinessTypeActivity.this,
                        getString(R.string.cw_rgblive)
                        , showWidth, 0);
            }
        });
        cwRgbAndNir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgTag.equals(getString(R.string.cw_rgbandnir))) {
                    msgTag = "";
                    return;
                }
                msgTag = getString(R.string.cw_rgbandnir);
                cwRgbAndNir.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(rgbAndNirView, rgbAndNirView,
                        FaceLivinessTypeActivity.this, getString(R.string.cw_rgbandnir)
                        , showWidth, 0);
            }
        });
        cwRgbAndDepth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgTag.equals(getString(R.string.cw_rgbanddepth))) {
                    msgTag = "";
                    return;
                }
                msgTag = getString(R.string.cw_rgbanddepth);
                cwRgbAndDepth.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(rgbAndDepthView, rgbAndDepthView,
                        FaceLivinessTypeActivity.this, getString(R.string.cw_rgbanddepth)
                        , showWidth, 0);
            }
        });
        cwCameraType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgTag.equals(getString(R.string.cw_cameratype))) {
                    msgTag = "";
                    return;
                }
                msgTag = getString(R.string.cw_cameratype);
                cwCameraType.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(linerCameraType, textCameraType,
                        FaceLivinessTypeActivity.this,
                        getString(R.string.cw_cameratype), showWidth, showXLocation);
            }
        });
    }

    public RadioGroup.OnCheckedChangeListener liveType = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (group.getCheckedRadioButtonId()) {
                case R.id.fls_no_live:
                    liveTypeValue = one;
                    linerCameraType.setVisibility(View.GONE);
                    flsCameraType.setVisibility(View.GONE);
                    break;
                case R.id.fls_rgb_live:
                    liveTypeValue = two;
                    linerCameraType.setVisibility(View.GONE);
                    flsCameraType.setVisibility(View.GONE);
                    break;
                case R.id.fls_rgbandnir_live:
                    liveTypeValue = three;
                    linerCameraType.setVisibility(View.GONE);
                    flsCameraType.setVisibility(View.GONE);
                    break;
                case R.id.fls_rgbanddepth_live:
                    liveTypeValue = four;
                    linerCameraType.setVisibility(View.VISIBLE);
                    flsCameraType.setVisibility(View.VISIBLE);
                    setlectCamera();
                    break;
                default:
                    break;
            }
        }
    };

    public RadioGroup.OnCheckedChangeListener cameraType = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (group.getCheckedRadioButtonId()) {
                case R.id.flt_zero:
                    cameraTypeValue = zero;
                    break;
                case R.id.flt_one:
                    cameraTypeValue = one;
                    break;
                case R.id.flt_two:
                    cameraTypeValue = two;
                    break;
                case R.id.flt_three:
                    cameraTypeValue = three;
                    break;
                case R.id.flt_four:
                    cameraTypeValue = four;
                    break;
                case R.id.flt_five:
                    cameraTypeValue = five;
                    break;
                case R.id.flt_six:
                    cameraTypeValue = six;
                    break;
                default:
                    break;
            }
        }
    };


    public void setlectCamera() {
        if (cameraTypeValue == zero) {
            fltZero.setChecked(true);
        }
        if (cameraTypeValue == one) {
            fltOne.setChecked(true);
        }
        if (cameraTypeValue == two) {
            fltTwo.setChecked(true);
        }
        if (cameraTypeValue == three) {
            fltThree.setChecked(true);
        }
        if (cameraTypeValue == four) {
            fltFour.setChecked(true);
        }
        if (cameraTypeValue == five) {
            fltFive.setChecked(true);
        }
        if (cameraTypeValue == six) {
            fltSix.setChecked(true);
        }
    }

    public void justify() {
        if (liveTypeValue == one) {
            SingleBaseConfig.getBaseConfig().setType(one);
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
        }
        if (liveTypeValue == two) {
            SingleBaseConfig.getBaseConfig().setType(two);
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
        }
        if (liveTypeValue == three) {
            SingleBaseConfig.getBaseConfig().setType(three);
            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
        }
        if (liveTypeValue == four) {
            SingleBaseConfig.getBaseConfig().setType(four);
        }

        cameraSelect();
    }

    public void cameraSelect() {
        if (cameraTypeValue == zero) {
            SingleBaseConfig.getBaseConfig().setCameraType(zero);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
        }
        if (cameraTypeValue == one) {
            SingleBaseConfig.getBaseConfig().setCameraType(one);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
        }
        if (cameraTypeValue == two) {
            SingleBaseConfig.getBaseConfig().setCameraType(two);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(400);
        }
        if (cameraTypeValue == three) {
            SingleBaseConfig.getBaseConfig().setCameraType(three);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(400);
        }
        if (cameraTypeValue == four) {
            SingleBaseConfig.getBaseConfig().setCameraType(four);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(400);
        }
        if (cameraTypeValue == five) {
            SingleBaseConfig.getBaseConfig().setCameraType(five);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
        }
        if (cameraTypeValue == six) {
            SingleBaseConfig.getBaseConfig().setCameraType(six);

            SingleBaseConfig.getBaseConfig().setRgbAndNirWidth(640);
            SingleBaseConfig.getBaseConfig().setRgbAndNirHeight(480);
            SingleBaseConfig.getBaseConfig().setDepthWidth(640);
            SingleBaseConfig.getBaseConfig().setDepthHeight(480);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        showWidth = flsLiveType.getWidth();
        showXLocation = (int) flRepresent.getLeft();
    }

}
