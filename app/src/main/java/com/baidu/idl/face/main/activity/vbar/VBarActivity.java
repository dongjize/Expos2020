package com.baidu.idl.face.main.activity.vbar;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.idl.face.main.R;
import com.baidu.idl.face.main.activity.BaseActivity;
import com.sun.jna.ptr.IntByReference;

public class VBarActivity extends BaseActivity {

    private static final String TAG = VBarActivity.class.getSimpleName();

    private TextView decodeStr;
    private Button opendev, lignton, lightoff, close, decodestart, closedev;

    boolean state = false;
    boolean devicestate = false;
    IntByReference device;
    Vbar b = new Vbar();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vbar);
        decodeStr = (TextView) findViewById(R.id.decodeStr);
        decodeStr.setMovementMethod(ScrollingMovementMethod.getInstance());

        lignton = (Button) findViewById(R.id.lighton);
        lignton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                b.vbarLight(true);

            }
        });

        close = (Button) findViewById(R.id.close);
        close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                b.closeDev();
                System.exit(0);
            }
        });
        closedev = (Button) findViewById(R.id.closedev);
        closedev.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                devicestate = false;
                b.closeDev();
                lignton.setEnabled(true);
                lightoff.setEnabled(true);

            }
        });
        lightoff = (Button) findViewById(R.id.lightoff);
        lightoff.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                b.vbarLight(false);

            }
        });
        opendev = (Button) findViewById(R.id.openDev);
        opendev.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                state = b.vbarOpen();
                if (state) {
                    AlertDialog.Builder builders = new AlertDialog.Builder(VBarActivity.this);
                    builders.setTitle("设备连接状态");
                    builders.setMessage("连接成功");
                    builders.setPositiveButton("确认", null);
                    builders.show();
                    Log.v("######################", "open success");
                    lignton.setEnabled(true);
                    lightoff.setEnabled(true);

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(VBarActivity.this);
                    builder.setTitle("设备连接状态");
                    builder.setMessage("连接失败");
                    builder.setPositiveButton("确认", null);
                    builder.show();
                    devicestate = false;
                    Log.v("#####################", "open fail");
                }
            }
        });
        decodestart = (Button) findViewById(R.id.begindecode);
        decodestart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (true) {
                            final String str = b.getResultSingle();
                            if (str != null) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        decodeStr.setText(str + "\r\n");
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
                t.start();
                if (devicestate) {
                    lignton.setEnabled(false);
                    lightoff.setEnabled(false);
                }

            }
        });
    }

    public void refreshAlarmView(TextView textView, String msg) {
        textView.setText(msg);
        int offset = textView.getLineCount() * textView.getLineHeight();
        if (offset > (textView.getHeight() - textView.getLineHeight() - 20)) {
            textView.scrollTo(0, offset - textView.getHeight() + textView.getLineHeight() + 20);
        }
    }


}
