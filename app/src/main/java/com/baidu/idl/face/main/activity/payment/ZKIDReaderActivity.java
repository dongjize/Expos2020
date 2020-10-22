package com.baidu.idl.face.main.activity.payment;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.idl.face.main.R;
import com.baidu.idl.face.main.activity.BaseActivity;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZKIDReaderActivity extends BaseActivity {

    private static final int VID = 1024;    //IDR VID
    private static final int PID = 50010;     //IDR PID
    private IDCardReader idCardReader = null;
    private TextView textView = null;
    private ImageView imageView = null;
    private boolean bOpen = false;
    private boolean bStopped = false;
    private int mReadCount = 0;
    private CountDownLatch countdownLatch = null;

    private Context mContext = null;
    private UsbManager mUsbManager = null;
    private final String ACTION_USB_PERMISSION = "com.example.scarx.idcardreader.USB_PERMISSION"; // TODO

    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        OpenDevice();
                    } else {
                        Toast.makeText(mContext, "USB未授权", Toast.LENGTH_SHORT).show();
                        //mTxtReport.setText("USB未授权");
                    }
                }
            }
        }
    };

    private void RequestDevicePermission() {
        mUsbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);

        for (UsbDevice device : mUsbManager.getDeviceList().values()) {
            if (device.getVendorId() == VID && device.getProductId() == PID) {
                Intent intent = new Intent(ACTION_USB_PERMISSION);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);
                mUsbManager.requestPermission(device, pendingIntent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zk_idreader);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);
        mContext = this.getApplicationContext();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        mContext.registerReceiver(mUsbReceiver, filter);

    }

    public Context getContext() {
        return this.getApplicationContext();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Destroy fingerprint sensor when it's not used
        IDCardReaderFactory.destroy(idCardReader);
        mContext.unregisterReceiver(mUsbReceiver);
    }


    public static void writeLogToFile(String log) {
        try {
            File dirFile = new File("/sdcard/zkteco/");  //目录转化成文件夹
            if (!dirFile.exists()) {              //如果不存在，那就建立这个文件夹
                dirFile.mkdirs();
            }
            String path = "/sdcard/zkteco/idrlog.txt";
            File file = new File(path);
            if (!file.exists()) {
                File dir = new File(file.getParent());
                dir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream outStream = new FileOutputStream(file, true);
            log += "\r\n";
            outStream.write(log.getBytes());
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
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

    public void OpenDevice() {
        if (bOpen) {
            textView.setText("设备已连接");
            return;
        }
        try {
            startIDCardReader();
            IDCardReaderExceptionListener listener = new IDCardReaderExceptionListener() {
                @Override
                public void OnException() {
                    //出现异常，关闭设备
                    CloseDevice();
                    //当前线程为工作线程，若需操作界面，请在UI线程处理
                    runOnUiThread(new Runnable() {
                        public void run() {
                            textView.setText("设备发生异常，断开连接！");
                        }
                    });
                }
            };
            idCardReader.open(0);
            idCardReader.setIdCardReaderExceptionListener(listener);
            bStopped = false;
            mReadCount = 0;
            writeLogToFile("连接设备成功");
            textView.setText("连接成功");
            bOpen = true;
            countdownLatch = new CountDownLatch(1);
            new Thread(new Runnable() {
                public void run() {
                    while (!bStopped) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        boolean ret = false;
                        final long nTickstart = System.currentTimeMillis();
                        try {
                            idCardReader.findCard(0);
                            idCardReader.selectCard(0);
                        } catch (IDCardReaderException e) {
                            //continue;
                        }
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int retType = 0;
                        try {
                            if (authenticate()) {
                                retType = idCardReader.readCardEx(0, 0);
                            }
                        } catch (IDCardReaderException e) {
                            writeLogToFile("读卡失败，错误信息：" + e.getMessage());
                        }
                        if (retType == 1 || retType == 2 || retType == 3) {
                            final long nTickUsed = (System.currentTimeMillis() - nTickstart);
                            final int final_retType = retType;
                            writeLogToFile("读卡成功：" + (++mReadCount) + "次" + "，耗时：" + nTickUsed + "毫秒");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (final_retType == 1) {
                                        final IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                                        //姓名adb
                                        String strName = idCardInfo.getName();
                                        //民族
                                        String strNation = idCardInfo.getNation();
                                        //出生日期
                                        String strBorn = idCardInfo.getBirth();
                                        //住址
                                        String strAddr = idCardInfo.getAddress();
                                        //身份证号
                                        String strID = idCardInfo.getId();
                                        //有效期限
                                        String strEffext = idCardInfo.getValidityTime();
                                        //签发机关
                                        String strIssueAt = idCardInfo.getDepart();
                                        textView.setText("读取次数：" + mReadCount + ",耗时：" + nTickUsed + "毫秒, 卡类型：居民身份证,姓名：" + strName +
                                                "，民族：" + strNation + "，住址：" + strAddr + ",身份证号：" + strID);
                                        if (idCardInfo.getPhotolength() > 0) {
                                            byte[] buf = new byte[WLTService.imgLength];
                                            if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                                                imageView.setImageBitmap(IDPhotoHelper.Bgr2Bitmap(buf));
                                            }
                                        }
                                    } else if (final_retType == 2) {
                                        final IDPRPCardInfo idprpCardInfo = idCardReader.getLastPRPIDCardInfo();
                                        //中文名
                                        String strCnName = idprpCardInfo.getCnName();
                                        //英文名
                                        String strEnName = idprpCardInfo.getEnName();
                                        //国家/国家地区代码
                                        String strCountry = idprpCardInfo.getCountry() + "/" + idprpCardInfo.getCountryCode();//国家/国家地区代码
                                        //出生日期
                                        String strBorn = idprpCardInfo.getBirth();
                                        //身份证号
                                        String strID = idprpCardInfo.getId();
                                        //有效期限
                                        String strEffext = idprpCardInfo.getValidityTime();
                                        //签发机关
                                        String strIssueAt = "公安部";
                                        textView.setText("读取次数：" + mReadCount + ",耗时：" + nTickUsed + "毫秒, 卡类型：外国人永居证,中文名：" + strCnName + ",英文名：" +
                                                strEnName + "，国家：" + strCountry + ",证件号：" + strID);
                                        if (idprpCardInfo.getPhotolength() > 0) {
                                            byte[] buf = new byte[WLTService.imgLength];
                                            if (1 == WLTService.wlt2Bmp(idprpCardInfo.getPhoto(), buf)) {
                                                imageView.setImageBitmap(IDPhotoHelper.Bgr2Bitmap(buf));
                                            }
                                        }
                                    } else {
                                        final IDCardInfo idCardInfo = idCardReader.getLastIDCardInfo();
                                        //姓名
                                        String strName = idCardInfo.getName();
                                        //民族,港澳台不支持该项
                                        String strNation = "";
                                        //出生日期
                                        String strBorn = idCardInfo.getBirth();
                                        //住址
                                        String strAddr = idCardInfo.getAddress();
                                        //身份证号
                                        String strID = idCardInfo.getId();
                                        //有效期限
                                        String strEffext = idCardInfo.getValidityTime();
                                        //签发机关
                                        String strIssueAt = idCardInfo.getDepart();
                                        //通行证号
                                        String strPassNum = idCardInfo.getPassNum();
                                        //签证次数
                                        int visaTimes = idCardInfo.getVisaTimes();
                                        textView.setText("读取次数：" + mReadCount + ",耗时：" + nTickUsed + "毫秒, 卡类型：港澳台居住证,姓名：" + strName +
                                                "，住址：" + strAddr + ",身份证号：" + strID + "，通行证号码：" + strPassNum +
                                                ",签证次数：" + visaTimes);
                                        if (idCardInfo.getPhotolength() > 0) {
                                            byte[] buf = new byte[WLTService.imgLength];
                                            if (1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(), buf)) {
                                                imageView.setImageBitmap(IDPhotoHelper.Bgr2Bitmap(buf));
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                    countdownLatch.countDown();
                }
            }).start();
        } catch (IDCardReaderException e) {
            writeLogToFile("连接设备失败");
            textView.setText("连接失败");
            textView.setText("开始读卡失败，错误码：" + e.getErrorCode() + "\n错误信息：" + e.getMessage() + "\n内部代码=" + e.getInternalErrorCode());
        }

    }

    public void OnBnBegin(View view) throws IDCardReaderException {
        if (bOpen) {
            textView.setText("设备已连接");
            return;
        }
        RequestDevicePermission();

    }

    private void CloseDevice() {
        if (!bOpen) {
            return;
        }
        bStopped = true;
        mReadCount = 0;
        if (null != countdownLatch) {
            try {
                countdownLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            idCardReader.close(0);
        } catch (IDCardReaderException e) {
            e.printStackTrace();
        }
        bOpen = false;
    }


    public void OnBnStop(View view) {
        if (!bOpen) {
            return;
        }
        CloseDevice();
        textView.setText("设备断开连接");

    }

}
