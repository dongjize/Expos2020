package com.baidu.idl.face.main.activity.scangun;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.idl.face.main.R;
import com.baidu.idl.face.main.activity.BaseActivity;

public class ScanGunActivity extends BaseActivity {
    private ScanGun mScanGun = null;
    private TextView tvScanResult;
    private Button BtStartScan;
    private Button BtStopScan;
    private Boolean needScan = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scangun);
        tvScanResult = (TextView) findViewById(R.id.tvScanResult);
        BtStartScan = (Button) findViewById(R.id.StartScan);
        BtStopScan = (Button) findViewById(R.id.StopScan);
        BtStartScan.setEnabled(true);
        BtStopScan.setEnabled(false);

        Log.d("jason", "onCreate");

        BtStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                needScan = true;
                BtStartScan.setEnabled(false);
                BtStopScan.setEnabled(true);

            }
        });


        BtStopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                needScan = false;
                BtStartScan.setEnabled(true);
                BtStopScan.setEnabled(false);


            }
        });

        // 设置key事件最大间隔
        ScanGun.setMaxKeysInterval(50);
        mScanGun = new ScanGun(new ScanGun.ScanGunCallBack() {
            @Override
            public void onScanFinish(String scanResult) {
                Log.d("jason", "onScanFinish : " + scanResult);
                //Toast.makeText(MainActivity.this, "数据： "+scanResult, Toast.LENGTH_SHORT).show();
                tvScanResult.setText(scanResult);
                tvScanResult.setTextColor(getRandomColorInt());

            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Log.d("jason","onKeyDown");
        //  Log.d("jason","main  KeyCode :  " + keyCode );

        if (needScan == false)
            return super.onKeyDown(keyCode, event);

        mScanGun.isScanning(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 拦截物理键盘事件
        //  Log.d("jason","dispatchKeyEvent KeyCode :  " + event.getKeyCode() );

        if (needScan == false)
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


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        needScan = false;
        BtStartScan.setEnabled(true);
        BtStopScan.setEnabled(false);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        needScan = false;
        BtStartScan.setEnabled(true);
        BtStopScan.setEnabled(false);

    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        needScan = false;
        BtStartScan.setEnabled(true);
        BtStopScan.setEnabled(false);

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        needScan = false;
        BtStartScan.setEnabled(true);
        BtStopScan.setEnabled(false);

    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        needScan = false;
        BtStartScan.setEnabled(true);
        BtStopScan.setEnabled(false);

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        needScan = false;
        BtStartScan.setEnabled(true);
        BtStopScan.setEnabled(false);
    }

    public int getRandomColorInt() {
        int red = (int) (Math.random() * 256);
        int green = (int) (Math.random() * 256);
        int blue = (int) (Math.random() * 256);
        return Color.rgb(red, green, blue);
    }
}
