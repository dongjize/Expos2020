package com.baidu.idl.face.main.activity.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.idl.face.main.Config;
import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.R;

public class ApiUrlSettingActivity extends BaseActivity {

    private EditText editText;
    private Button button, resetBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_url_setting);

        editText = findViewById(R.id.etApiUrl);
        button = findViewById(R.id.btnConfirm);
        resetBtn = findViewById(R.id.btnReset);

        editText.setHint(Config.API_URL);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText() != null) {
                    SharedPreferences sp = getSharedPreferences("api_url", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("api_url", editText.getText().toString());
                    editor.apply();
                    Toast.makeText(ApiUrlSettingActivity.this,
                            editText.getText().toString(), Toast.LENGTH_LONG).show();
                    Config.API_URL = editText.getText().toString();

                } else {
                    Toast.makeText(ApiUrlSettingActivity.this,
                            "url不能为空", Toast.LENGTH_LONG).show();
                }
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("api_url", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("api_url", Config.ORIGINAL_API_URL);
                editor.apply();

                Config.API_URL = Config.ORIGINAL_API_URL;
                Toast.makeText(ApiUrlSettingActivity.this,
                        "恢复默认：" + Config.ORIGINAL_API_URL, Toast.LENGTH_LONG).show();
                editText.setHint(Config.API_URL);

            }
        });


    }
}
