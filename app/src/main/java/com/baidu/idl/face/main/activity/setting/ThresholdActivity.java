package com.baidu.idl.face.main.activity.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.model.SingleBaseConfig;
import com.baidu.idl.face.main.utils.ConfigUtils;
import com.baidu.idl.face.main.utils.PWTextUtils;
import com.baidu.idl.face.main.R;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class ThresholdActivity extends BaseActivity {

    // 生活照模型阀值
    private ImageView thLiveDecrease;
    private ImageView thLiveIncrease;
    private EditText thLiveEtThreshold;

    // 证件照模型阀值
    private ImageView thIDDecrease;
    private ImageView thIDIncrease;
    private EditText thIDEtThreshold;

    // RGB活体阀值
    private ImageView thRgbLiveDecrease;
    private ImageView thRgbLiveIncrease;
    private EditText thRgbLiveEtThreshold;

    // NIR活体阀值
    private ImageView thNirLiveDecrease;
    private ImageView thNirLiveIncrease;
    private EditText thNirLiveEtThreshold;

    // Depth活体阀值
    private ImageView thdepthLiveDecrease;
    private ImageView thdepthLiveIncrease;
    private EditText thDepthLiveEtThreshold;

    private float rgbInitValue;
    private float nirInitValue;
    private float depthInitValue;

    private BigDecimal rgbDecimal;
    private BigDecimal nirDecimal;
    private BigDecimal depthDecimal;
    private BigDecimal nonmoralValue;
    private static final float templeValue = 0.05f;

    private int zero = 0;
    private int one = 1;
    private static final int hundered = 100;

    private int liveInitValue;
    private int idInitValue;

    private ImageView thSave;

    private LinearLayout linerRecognizeThrehold;
    private LinearLayout linerLiveThreshold;
    private Button cwRecognizeThrehold;
    private Button cwLiveThrehold;
    private TextView tvThreshold;
    private TextView tvLive;
    private String msgTag = "";
    //    private LinearLayout linerThreholdBar;
    private LinearLayout thRepresent;
    private int showWidth;
    private int showXLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threshold);

//        linerThreholdBar = findViewById(R.id.linerbarthrehold);
//        setBarColor();
//        setLightStatusBarColor(this);
//        setBarLayout(linerThreholdBar);

        init();
    }

    public void init() {
        thRepresent = findViewById(R.id.thRepresent);

        linerRecognizeThrehold = findViewById(R.id.linerrecognizethrehold);
        linerLiveThreshold = findViewById(R.id.linerlivethreshold);
        cwRecognizeThrehold = findViewById(R.id.cw_recognizethrehold);
        cwLiveThrehold = findViewById(R.id.cw_livethrehold);
        tvThreshold = findViewById(R.id.tvthreshold);
        tvLive = findViewById(R.id.tvlive);

        thSave = findViewById(R.id.th_save);

        thLiveDecrease = findViewById(R.id.th_LiveDecrease);
        thLiveIncrease = findViewById(R.id.th_LiveIncrease);
        thLiveEtThreshold = findViewById(R.id.th_LiveEtThreshold);

        thIDDecrease = findViewById(R.id.th_IDDecrease);
        thIDIncrease = findViewById(R.id.th_IDIncrease);
        thIDEtThreshold = findViewById(R.id.th_IDEtThreshold);

        thRgbLiveDecrease = findViewById(R.id.th_RgbLiveDecrease);
        thRgbLiveIncrease = findViewById(R.id.th_RgbLiveIncrease);
        thRgbLiveEtThreshold = findViewById(R.id.th_RgbLiveEtThreshold);

        thNirLiveDecrease = findViewById(R.id.th_NirLiveDecrease);
        thNirLiveIncrease = findViewById(R.id.th_NirLiveIncrease);
        thNirLiveEtThreshold = findViewById(R.id.th_NirLiveEtThreshold);

        thdepthLiveDecrease = findViewById(R.id.th_depthLiveDecrease);
        thdepthLiveIncrease = findViewById(R.id.th_depthLiveIncrease);
        thDepthLiveEtThreshold = findViewById(R.id.th_depthLiveEtThreshold);

        rgbInitValue = SingleBaseConfig.getBaseConfig().getRgbLiveScore();
        nirInitValue = SingleBaseConfig.getBaseConfig().getNirLiveScore();
        depthInitValue = SingleBaseConfig.getBaseConfig().getDepthLiveScore();

        liveInitValue = SingleBaseConfig.getBaseConfig().getLiveThreshold();
        idInitValue = SingleBaseConfig.getBaseConfig().getIdThreshold();

        nonmoralValue = new BigDecimal(templeValue + "");

        thLiveEtThreshold.setText(liveInitValue + "");
        thIDEtThreshold.setText(idInitValue + "");
        thRgbLiveEtThreshold.setText(roundByScale(rgbInitValue));
        thNirLiveEtThreshold.setText(roundByScale(nirInitValue));
        thDepthLiveEtThreshold.setText(roundByScale(depthInitValue));

        settingOnclick();
    }

    public void settingOnclick() {

        thLiveDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rgbInitValue > zero && rgbInitValue <= one) {
                    if (liveInitValue > zero && liveInitValue <= hundered) {
                        liveInitValue = liveInitValue - 5;
                        thLiveEtThreshold.setText(liveInitValue + "");
                    }
                }
            }
        });

        thLiveIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liveInitValue >= zero && liveInitValue < hundered) {
                    liveInitValue = liveInitValue + 5;
                    thLiveEtThreshold.setText(liveInitValue + "");
                }
            }
        });

        thIDDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idInitValue > zero && idInitValue <= hundered) {
                    idInitValue = idInitValue - 5;
                    thIDEtThreshold.setText(idInitValue + "");
                }
            }
        });
        thIDIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idInitValue >= zero && idInitValue < hundered) {
                    idInitValue = idInitValue + 5;
                    thIDEtThreshold.setText(idInitValue + "");
                }
            }
        });


        thRgbLiveDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rgbInitValue > zero && rgbInitValue <= one) {
                    rgbDecimal = new BigDecimal(rgbInitValue + "");
                    rgbInitValue = rgbDecimal.subtract(nonmoralValue).floatValue();
                    thRgbLiveEtThreshold.setText(roundByScale(rgbInitValue));
                }
            }
        });
        thRgbLiveIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rgbInitValue >= zero && rgbInitValue < one) {
                    rgbDecimal = new BigDecimal(rgbInitValue + "");
                    rgbInitValue = rgbDecimal.add(nonmoralValue).floatValue();
                    thRgbLiveEtThreshold.setText(roundByScale(rgbInitValue));
                }
            }
        });

        thNirLiveDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nirInitValue > zero && nirInitValue <= one) {
                    nirDecimal = new BigDecimal(nirInitValue + "");
                    nirInitValue = nirDecimal.subtract(nonmoralValue).floatValue();
                    thNirLiveEtThreshold.setText(roundByScale(nirInitValue));
                }
            }
        });
        thNirLiveIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nirInitValue >= zero && nirInitValue < one) {
                    nirDecimal = new BigDecimal(nirInitValue + "");
                    nirInitValue = nirDecimal.add(nonmoralValue).floatValue();
                    thNirLiveEtThreshold.setText(roundByScale(nirInitValue));
                }
            }
        });

        thdepthLiveDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (depthInitValue > zero && depthInitValue <= one) {
                    depthDecimal = new BigDecimal(depthInitValue + "");
                    depthInitValue = depthDecimal.subtract(nonmoralValue).floatValue();
                    thDepthLiveEtThreshold.setText(roundByScale(depthInitValue));
                }
            }
        });

        thdepthLiveIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (depthInitValue >= zero && depthInitValue < one) {
                    depthDecimal = new BigDecimal(depthInitValue + "");
                    depthInitValue = depthDecimal.add(nonmoralValue).floatValue();
                    thDepthLiveEtThreshold.setText(roundByScale(depthInitValue));
                }
            }
        });

        thSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleBaseConfig.getBaseConfig().setRgbLiveScore(
                        Float.valueOf(thRgbLiveEtThreshold.getText().toString()));
                SingleBaseConfig.getBaseConfig().setNirLiveScore(
                        Float.valueOf(thNirLiveEtThreshold.getText().toString()));
                SingleBaseConfig.getBaseConfig().setDepthLiveScore(
                        Float.valueOf(thDepthLiveEtThreshold.getText().toString()));

                SingleBaseConfig.getBaseConfig().setLiveThreshold(
                        Integer.valueOf(thLiveEtThreshold.getText().toString()));
                SingleBaseConfig.getBaseConfig().setIdThreshold(
                        Integer.valueOf(thIDEtThreshold.getText().toString()));

                ConfigUtils.modityJson();
                finish();
            }
        });

        PWTextUtils.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                cwRecognizeThrehold.setBackground(getDrawable(R.mipmap.icon_setting_question));
                cwLiveThrehold.setBackground(getDrawable(R.mipmap.icon_setting_question));

            }
        });

        cwRecognizeThrehold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgTag.equals(getString(R.string.cw_recognizethrehold))) {
                    msgTag = "";
                    return;
                }
                msgTag = getString(R.string.cw_recognizethrehold);
                cwRecognizeThrehold.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(linerRecognizeThrehold, tvThreshold,
                        ThresholdActivity.this, getString(R.string.cw_recognizethrehold), showWidth, showXLocation);
            }
        });

        cwLiveThrehold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgTag.equals(getString(R.string.cw_livethrehold))) {
                    msgTag = "";
                    return;
                }
                msgTag = getString(R.string.cw_livethrehold);
                cwLiveThrehold.setBackground(getDrawable(R.mipmap.icon_setting_question_hl));
                PWTextUtils.showDescribeText(linerLiveThreshold, tvLive, ThresholdActivity.this,
                        getString(R.string.cw_livethrehold), showWidth, showXLocation);
            }
        });
    }


    public static String roundByScale(float numberValue) {
        // 构造方法的字符格式这里如果小数不足2位,会以0补足.
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        // format 返回的是字符串
        String resultNumber = decimalFormat.format(numberValue);
        return resultNumber;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        showWidth = thRepresent.getWidth();
        showXLocation = (int) thRepresent.getX();
    }
}
