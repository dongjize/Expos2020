package com.baidu.idl.face.main.activity.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.idl.face.main.Config;
import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.R;

public class ApiUrlSettingActivity extends BaseActivity {

    private EditText editText;
    private Button confirmBtn, resetBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_url_setting);

        editText = findViewById(R.id.etApiUrl);
        confirmBtn = findViewById(R.id.btnConfirm);
        resetBtn = findViewById(R.id.btnReset);

        editText.setText(Config.DOMAIN);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText() != null) {
                    SharedPreferences sp = getSharedPreferences("api_url", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("api_url", editText.getText().toString());
                    editor.apply();
                    Toast.makeText(ApiUrlSettingActivity.this, editText.getText().toString(), Toast.LENGTH_LONG).show();
                    Config.DOMAIN = editText.getText().toString();
                } else {
                    Toast.makeText(ApiUrlSettingActivity.this, "url不能为空", Toast.LENGTH_LONG).show();
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("api_url", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("api_url", Config.ORIGINAL_DOMAIN);
                editor.apply();
                Config.DOMAIN = Config.ORIGINAL_DOMAIN;
                Toast.makeText(ApiUrlSettingActivity.this, "恢复默认：" + Config.ORIGINAL_DOMAIN, Toast.LENGTH_LONG).show();
                editText.setText(Config.DOMAIN);

            }
        });


    }
}
