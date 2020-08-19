package com.baidu.idl.face.main.activity.setting;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.face.main.R;


public class VersionMessageActivity extends BaseActivity {
    private TextView sdkVersion;
    private TextView systemVersion;
    private TextView activateStatus;
    private TextView activateType;
    private TextView activateData;
    private ImageView buttonVersionSave;
//    private LinearLayout linerBarVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versionmessage);

//        linerBarVersion = findViewById(R.id.linerbarversion);
//        setBarColor();
//        setLightStatusBarColor(this);
//        setBarLayout(linerBarVersion);

        init();
    }

    public void init() {
        buttonVersionSave = findViewById(R.id.button_version_save);
        sdkVersion = findViewById(R.id.sdkversion);
        systemVersion = findViewById(R.id.systemversion);
        activateStatus = findViewById(R.id.activatestatus);
        activateType = findViewById(R.id.activatetype);
        activateData = findViewById(R.id.activatedata);

        sdkVersion.setText(Utils.getVersionName(this));
        systemVersion.setText(android.os.Build.VERSION.RELEASE);
        if (FaceSDKManager.initStatus != FaceSDKManager.SDK_MODEL_LOAD_SUCCESS) {
            activateStatus.setText("未激活");
        } else {
            activateStatus.setText("已激活");
        }
        activateData.setText(FaceSDKManager.getInstance().getLicenseData(this));
        buttonVersionSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
