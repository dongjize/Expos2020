package com.baidu.idl.face.main.activity.view;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import co.lujun.lmbluetoothsdk.BluetoothApi;
import co.lujun.lmbluetoothsdk.model.OnActionBondStateChangedEvent;
import co.lujun.lmbluetoothsdk.model.OnActionDeviceFoundEvent;
import co.lujun.lmbluetoothsdk.model.OnActionDiscoveryStateChangedEvent;
import co.lujun.lmbluetoothsdk.model.OnActionStateChangedEvent;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.baidu.idl.face.main.Config;
import com.baidu.idl.face.main.R;
import com.baidu.idl.face.main.activity.BaseActivity;
import com.baidu.idl.face.main.activity.model.AppendLogEvent;
import com.baidu.idl.face.main.activity.model.DeviceParamBean;
import com.baidu.idl.face.main.activity.presenter.ReaderPresenter;
import com.baidu.idl.face.main.activity.vbar.Vbar;
import com.baidu.idl.face.main.api.FaceApi;
import com.baidu.idl.face.main.exception.FaceError;
import com.baidu.idl.face.main.manager.FaceSDKManager;
import com.baidu.idl.face.main.model.User;
import com.baidu.idl.face.main.utils.ImageUtils;
import com.baidu.idl.face.main.utils.OnResultListener;
import com.baidu.idl.face.main.utils.Utils;
import com.baidu.idl.main.facesdk.model.BDFaceSDKCommon;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.routon.plsy.device.sdk.DeviceInfo;
import com.routon.plsy.reader.sdk.common.ErrorCode;
import com.routon.plsy.reader.sdk.common.Info;
import com.routon.plsy.reader.sdk.common.Info.IDCardInfo;
import com.routon.plsy.reader.sdk.intf.IReader;

public class PlsyReaderActivity extends BaseActivity implements IReaderView, OnClickListener, OnItemSelectedListener {
    private final String TAG = "PlsyReaderActivity";
    private ReaderPresenter mReaderPresenter;
    //Control begin----------------------------------
    private String mStrCardNo;
    private String mStrStatus;
    private TextView mTextViewName;
    private TextView mTextViewGender;
    private TextView mTextViewNationTitle;
    private TextView mTextViewNation;
    private TextView mTextViewYear;
    private TextView mTextViewVer;
    private TextView mTextViewAgencyCode;
    private TextView mTextViewMonth;
    private TextView mTextViewDay;
    private TextView mTextViewAddress;
    private TextView mTextViewIDNoTitle;
    private TextView mTextViewIDNo;
    private TextView mTextViewAgency;
    private TextView mTextViewExpire;
    private ImageView mImageViewPortrait;
    private TextView mTextViewStatus;
    private TextView mTextViewCardNo;
    private TextView mTextViewPassportNum, mTextViewIssuranceTimes;
    private String mCardNo; //卡体管理号
    private CheckBox mCheckBoxReadCardCid; //是否读卡体管理号
    private LinearLayout linearLayoutChName;
    private TextView mTextViewIName;
    private LinearLayout linearLayoutAddress, linearlayoutGATPassportNum, linearlayoutGATIssuanceTimes, card_no_linear_layout, extButtons;
    private Button buttonBack;
    private Button buttonStart;
    private Button buttonStop;

    private Button buttonOpen;
    private Button buttonFind;
    private Button buttonSelect;
    private Button buttonReadBaseMsg;
    private Button buttonClose;

    private TextView txtLog;
    private Spinner spinnerSelectDevice;
    private ListView mListViewBTDevices;
    private BaseAdapter mBTFoundAdapter;
    private AlertDialog mAlertDialogBT;
    private List<BluetoothDevice> mListBTDevice;
    private List<String> mListBTDeviceInfo;
    private BluetoothDevice mBTDeviceSelected;
    private String BT_DEVNAME_PREFIX = "iDR";
    private boolean isReqUSBPermission = false;

    private Handler mHandler;
    private TextView tvCheckResult;

    private Vbar vbar;
    private boolean state;
    private Thread vbarThread;
    private List<User> userResult;


    private OnItemClickListener mBTDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            // Cancel discovery because it's costly and we're about to connect
            mBTDeviceSelected = mListBTDevice.get(position);
            Log.d(TAG, "select device:" + mBTDeviceSelected.getName() + "@" + mBTDeviceSelected.getAddress());
            mBluetoothApi.cancel_scan();
            mAlertDialogBT.dismiss();
            //去绑定选中的设备
            boolean is_bond_created = mBluetoothApi.creatBond(mBTDeviceSelected);
            Log.d(TAG, "createdBond?" + is_bond_created);
            appendLog(AppendLogEvent.LOG_CODE_ANY, "正在绑定设备" + mBTDeviceSelected.getName());

        }
    };

    private void initBTUI() {
        mListViewBTDevices = new ListView(this);
        mListViewBTDevices.setOnItemClickListener(mBTDeviceClickListener);
        mListBTDevice = new ArrayList<BluetoothDevice>();
        mListBTDeviceInfo = new ArrayList<String>();
        mBTFoundAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListBTDeviceInfo);
        mListViewBTDevices.setAdapter(mBTFoundAdapter);

        Builder builder = new Builder(this);
        builder.setTitle("蓝牙读卡器设备列表");
        builder.setView(mListViewBTDevices);
        mAlertDialogBT = builder.create();
        mAlertDialogBT.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d(TAG, "mAlertDialogBT onDismiss");
            }
        });

    }

    private void uninitBT() {
        if (mBluetoothApi != null) {
            mBluetoothApi.uninit();
            mBluetoothApi = null;
        }
    }

    private void checkBTDevice() {
        if (mBluetoothApi == null) {
            mBluetoothApi = BluetoothApi.getInstance();
            mBluetoothApi.init(this);
        }
        mBTDeviceSelected = null;
        if (!mBluetoothApi.isEnabled()) {
            //1.未开启蓝牙,先开启蓝牙
            appendLog(AppendLogEvent.LOG_CODE_ANY, "正在打开蓝牙");
            mBluetoothApi.openBluetooth();
        } else {
            //2. 查询是否有已绑定的蓝牙读卡器(设备名以"iDR"开头)
            for (BluetoothDevice device : mBluetoothApi.getBondedDevices()) {
                if (device.getName() != null && device.getName().startsWith(BT_DEVNAME_PREFIX)) {
                    appendLog(AppendLogEvent.LOG_CODE_ANY, "绑定设备" + device.getName() + "成功");
                    mBTDeviceSelected = device;

                    DeviceParamBean devParamBean = new DeviceParamBean();
                    devParamBean.setDeviceType(mDeviceType);
                    devParamBean.setUserObj(device);
                    mReaderPresenter.startReadcard(devParamBean);
                    break;
                }
            }

            //3. 没有已绑定的设备, 启动搜索
            if (mBTDeviceSelected == null) {
                appendLog(AppendLogEvent.LOG_CODE_ANY, "正在搜索设备名" + BT_DEVNAME_PREFIX + "XXX");
                mBluetoothApi.scan(BT_DEVNAME_PREFIX);
                mAlertDialogBT.show();
            }
        }
    }

    //Control end---------------------------------------

    private int mDeviceType = DeviceParamBean.DEV_TYPE_INNER_OR_HID;
    private BluetoothApi mBluetoothApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.app_name) + IReader.SDKVersion);
        setContentView(R.layout.activity_plsy_reader_sdkdemo);
        mTextViewVer = findViewById(R.id.textViewVer);
        mTextViewAgencyCode = findViewById(R.id.textViewAgencyCode);
        mTextViewPassportNum = findViewById(R.id.textViewPassportNum);// 通行证号码
        mTextViewIssuranceTimes = findViewById(R.id.textViewIssuanceTimes);// 签发次数
        mTextViewName = findViewById(R.id.textViewName);
        mTextViewGender = findViewById(R.id.textViewGender);
        mTextViewNationTitle = findViewById(R.id.textViewNationTitle);
        mTextViewNation = findViewById(R.id.textViewNation);
        mTextViewYear = findViewById(R.id.textViewYear);
        mTextViewMonth = findViewById(R.id.textViewMonth);
        mTextViewDay = findViewById(R.id.textViewDay);
        mTextViewAddress = findViewById(R.id.textViewAddress);
        mTextViewIDNoTitle = findViewById(R.id.textViewIDNoTitle);
        mTextViewIDNo = findViewById(R.id.textViewIDNo);
        mTextViewAgency = findViewById(R.id.textViewAgency);
        mTextViewExpire = findViewById(R.id.textViewExpire);
        mImageViewPortrait = findViewById(R.id.imageViewPortrait);
        mTextViewStatus = findViewById(R.id.textViewStatus);
        mTextViewCardNo = findViewById(R.id.textViewCardNo);
        linearLayoutChName = findViewById(R.id.linearLayoutChName);
        mTextViewIName = findViewById(R.id.textViewIName);
        linearLayoutAddress = findViewById(R.id.linearLayoutAddress);
        linearlayoutGATPassportNum = findViewById(R.id.GATPassportNum);
        linearlayoutGATIssuanceTimes = findViewById(R.id.GATIssuanceTimes);
        card_no_linear_layout = findViewById(R.id.card_no_linear_layout);
        card_no_linear_layout.setVisibility(View.GONE);
        mCheckBoxReadCardCid = findViewById(R.id.checkbox_readCardCid);
        mCheckBoxReadCardCid.setVisibility(View.GONE);
        buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);
        buttonStart = findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(this);
        buttonStop = findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(this);
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);

        extButtons = (LinearLayout) findViewById(R.id.extButtons);
        extButtons.setVisibility(View.GONE);
        buttonOpen = (Button) findViewById(R.id.buttonOpen);
        buttonOpen.setOnClickListener(this);
        buttonFind = (Button) findViewById(R.id.buttonFind);
        buttonFind.setOnClickListener(this);
        buttonSelect = (Button) findViewById(R.id.buttonSelect);
        buttonSelect.setOnClickListener(this);
        buttonReadBaseMsg = (Button) findViewById(R.id.buttonReadBaseMsg);
        buttonReadBaseMsg.setOnClickListener(this);
        buttonClose = (Button) findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(this);

        txtLog = (TextView) findViewById(R.id.txtLog);
        spinnerSelectDevice = (Spinner) findViewById(R.id.spinner_select_device);
        spinnerSelectDevice.setOnItemSelectedListener(this);
        spinnerSelectDevice.setSelection(0);

        mHandler = new Handler();
        tvCheckResult = findViewById(R.id.tvCheckResult);

        initBTUI();

        initVBar();

        EventBus.getDefault().register(this);
    }


    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (vbarThread != null) {
            vbarThread.interrupt();
        }
        if (vbar != null) {
            vbar.closeDev();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        //如果不是因为请求USB权限导致的Pause，才释放资源
        Log.d(TAG, "onPause isReqUSBPermission?" + isReqUSBPermission);
        if (isReqUSBPermission) {
            isReqUSBPermission = false;
        } else {
            //退出前要安全释放资源
            uninit();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mReaderPresenter == null) {
            mReaderPresenter = new ReaderPresenter(this);
        }
    }


    private void uninit() {
        //退出前要安全释放资源
        if (mReaderPresenter != null) {
            mReaderPresenter.stopReadcard();
            mReaderPresenter.release();
            mReaderPresenter = null;
        }
        uninitBT();
    }

    private void initVBar() {
        vbar = new Vbar();
        state = vbar.vbarOpen();

        vbarThread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    final String audienceCode = vbar.getResultsingle();
                    if (audienceCode != null) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Log.e(TAG, audienceCode);

                                requestUserByAudienceCode(new OnResultListener<List<User>>() {
                                    @Override
                                    public void onResult(List<User> result) {
                                        Log.e(TAG, "SUCCESS!");
                                        userResult = result;
                                        startActivityForResult(new Intent(PlsyReaderActivity.this, FaceNIRDetectActivity.class), 100);
                                    }

                                    @Override
                                    public void onError(FaceError error) {
                                        Log.e(TAG, "ERROR! " + error.toString());
                                    }
                                }, "91823750");
//                                }, audienceCode); // TODO
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Bundle extras = data.getExtras();
//            if (extras == null) {
//                Log.e(TAG, "EXTRAS is NULL");
//                return;
//            }
            byte[] features1 = (byte[]) extras.get("features");
//            Bitmap faceImg = (Bitmap) extras.get("bitmap");

            if (userResult != null) {
                User user = userResult.get(0);
                Bitmap bitmap = ImageUtils.stringToBitmap(user.getImage());
                byte[] features2 = new byte[512];
                FaceApi.getInstance().getFeature(bitmap, features2, BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO);

//                float ret1 = FaceSDKManager.getInstance().personDetect(iDCardInfo.photo, features1);
//                float ret2 = FaceSDKManager.getInstance().personDetect(bitmap, features2);

                float score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                        BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_LIVE_PHOTO,
                        features1, features2, true);
                Log.e(TAG, Arrays.toString(features1) + "");
                Log.e(TAG, Arrays.toString(features2) + "");
                Log.e(TAG, score + "");

                if (score >= Config.scoreThreshold) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvCheckResult.setText("认证通过");
                            tvCheckResult.setTextColor(getResources().getColor(R.color.green));
                        }
                    });

                    // TODO
                    uploadPassRecord(user);

                    try {
                        final FileOutputStream fos1 = new FileOutputStream("/sys/exgpio/relay1");
                        fos1.write("1".getBytes());
                        Log.e(TAG, "OPEN");


                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    fos1.write("0".getBytes());
                                    Log.e(TAG, "CLOSE");
                                    fos1.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    tvCheckResult.setText("认证失败");
                    tvCheckResult.setTextColor(getResources().getColor(R.color.red));
                }
            }
        }

    }


    private void uploadPassRecord(User user) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            long timestamp = System.currentTimeMillis() / 1000;

            HttpUrl url = Objects.requireNonNull(HttpUrl.parse("https://5jyw.com:444/bdface/api")).newBuilder()
                    .addQueryParameter("cmd", "getUsers")
                    .addQueryParameter("timestamp", timestamp + "")
                    .addQueryParameter("token", Utils.md5(timestamp + Config.API_KEY))
                    .build();

//            String reqTime = FaceApi.getInstance().getLatestCTime();
//            Log.e(TAG, reqTime);

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String todayAsString = df.format(today);

            RequestBody body = new FormBody.Builder()
                    .add("user_id", user.getUserId())
                    .add("eid", user.getItemEId())

                    .add("ptime", df.format(today))
                    .build();

            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "network request error", e);
//                    listener.onError(error);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String haha = response.body().string();
                    if (response == null || response.body() == null || TextUtils.isEmpty(haha)) {
                        FaceError error = new FaceError(FaceError.ErrorCode.ACCESS_TOKEN_PARSE_ERROR, "token is parse error, please rerequest token");
//                        listener.onError(error);

                    }
                    try {
                        JSONObject jsonObject = new JSONObject(haha);

                        Gson gson = new Gson();
                        List<User> userList = gson.fromJson(jsonObject.getJSONArray("data").toString(), new TypeToken<List<User>>() {
                        }.getType());

//                        listener.onResult(userList);

                    } catch (Exception e) {
                        e.printStackTrace();
                        FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "response with error", e);
//                        listener.onError(error);
                    }
                }
            });
//            Log.e(TAG, "===================" + s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == buttonBack) {
            finish();
        } else if (v == buttonStart) {
            if (mDeviceType == DeviceParamBean.DEV_TYPE_INNER_OR_HID) {
                appendLog(AppendLogEvent.LOG_CODE_ANY, "startReadcard");
                DeviceParamBean devParamBean = new DeviceParamBean();
                devParamBean.setDeviceType(mDeviceType);
                mReaderPresenter.startReadcard(devParamBean);
            } else {
                checkBTDevice();
            }
            buttonStart.setEnabled(false);
            buttonStop.setEnabled(true);
        } else if (v == buttonStop) {
            if (mDeviceType == DeviceParamBean.DEV_TYPE_BT && mBluetoothApi != null) {
                mBluetoothApi.cancel_scan();
            }
            mReaderPresenter.stopReadcard();
            updateStatus(getText(R.string.readcard_hint).toString());
            buttonStart.setEnabled(true);
            buttonStop.setEnabled(false);
        }
//		else if (v == buttonOpen) {
//			mReaderPresenter.sendCmd(IReaderPresenter.CMD_OPEN);			
//		}else if (v == buttonFind) {
//			mReaderPresenter.sendCmd(IReaderPresenter.CMD_FIND);
//		}else if (v == buttonSelect) {
//			mReaderPresenter.sendCmd(IReaderPresenter.CMD_SELECT);
//		}else if (v == buttonReadBaseMsg) {
//			mReaderPresenter.sendCmd(IReaderPresenter.CMD_READBASEMSG);
//		}else if (v == buttonClose) {
//			mReaderPresenter.sendCmd(IReaderPresenter.CMD_CLOSE);
//		}
    }

    private void updateStatus(String status) {
        mTextViewStatus.setText(status);
    }

    private void updateIDCardInfo(IDCardInfo idCardInfo, boolean display) {
        if (display) {
            if (idCardInfo.cardType == Info.ID_CARD_TYPE_CHINA) {// 身份证
                mTextViewName.setText(idCardInfo.name);
                mTextViewNationTitle.setText(R.string.nation);
                linearlayoutGATPassportNum.setVisibility(View.GONE);// 通行证号码
                linearlayoutGATIssuanceTimes.setVisibility(View.GONE);// 签发次数
                linearLayoutChName.setVisibility(View.GONE);
                mTextViewIDNoTitle.setText(R.string.idno);
                linearLayoutAddress.setVisibility(View.VISIBLE);
            } else if (idCardInfo.cardType == Info.ID_CARD_TYPE_FOREIGN) {// 外国人证
                linearLayoutAddress.setVisibility(View.GONE);
                linearlayoutGATPassportNum.setVisibility(View.GONE);// 通行证号码
                linearlayoutGATIssuanceTimes.setVisibility(View.GONE);// 签发次数
                linearLayoutChName.setVisibility(View.VISIBLE);
                mTextViewName.setText(idCardInfo.englishName);
                mTextViewIName.setText(idCardInfo.name);
                mTextViewNationTitle.setText(R.string.nation_ex);
                mTextViewIDNoTitle.setText(R.string.idno_foreign);
                mTextViewAgencyCode.setText(idCardInfo.agencyCode);
                mTextViewVer.setText(idCardInfo.ver);
            } else if (idCardInfo.cardType == Info.ID_CARD_TYPE_GAT) {// 港澳通行证
                linearLayoutAddress.setVisibility(View.VISIBLE);
                linearlayoutGATPassportNum.setVisibility(View.VISIBLE);// 通行证号码
                linearlayoutGATIssuanceTimes.setVisibility(View.VISIBLE);// 签发次数
                mTextViewName.setText(idCardInfo.name);
                mTextViewPassportNum.setText(idCardInfo.passportNum);
                mTextViewIssuranceTimes.setText(idCardInfo.issuanceTimes);
            }
            if (!TextUtils.isEmpty(mCardNo)) {
                card_no_linear_layout.setVisibility(View.VISIBLE);
                mTextViewCardNo.setText(mCardNo);
            } else {
                card_no_linear_layout.setVisibility(View.GONE);
            }

            mTextViewGender.setText(idCardInfo.gender);
            mTextViewNation.setText(idCardInfo.nation);
            mTextViewYear.setText(idCardInfo.birthday.substring(0, 4));
            mTextViewMonth.setText(idCardInfo.birthday.substring(4, 6));
            mTextViewDay.setText(idCardInfo.birthday.substring(6, 8));
            if (idCardInfo.address != null) {
                mTextViewAddress.setText(idCardInfo.address);
            }
            mTextViewIDNo.setText(idCardInfo.id);
            if (idCardInfo.agency != null) {
                mTextViewAgency.setText(idCardInfo.agency);
            }
            if (isExpire(idCardInfo.expireEnd)) {
                mTextViewExpire.setTextColor(Color.RED);
                mTextViewExpire.setText(idCardInfo.expireStart + " - " + idCardInfo.expireEnd + "(过期)");
            } else {
                mTextViewExpire.setTextColor(Color.BLACK);
                mTextViewExpire.setText(idCardInfo.expireStart + " - " + idCardInfo.expireEnd);
            }
            mImageViewPortrait.setImageBitmap(idCardInfo.photo);
        } else {
            mTextViewAgencyCode.setText("");
            mTextViewVer.setText("");
            linearLayoutAddress.setVisibility(View.VISIBLE);
            linearLayoutChName.setVisibility(View.GONE);
            mTextViewNationTitle.setText(R.string.nation);
            mTextViewName.setText("");
            mTextViewGender.setText("");
            mTextViewNation.setText("");
            mTextViewYear.setText("");
            mTextViewMonth.setText("");
            mTextViewDay.setText("");
            mTextViewAddress.setText("");
            mTextViewIDNoTitle.setText(R.string.idno);
            mTextViewIDNo.setText("");
            mTextViewAgency.setText("");
            mTextViewExpire.setText("");
            mTextViewPassportNum.setText("");
            mTextViewIssuranceTimes.setText("");
            mImageViewPortrait.setImageBitmap(null);
            //卡体管理号
            card_no_linear_layout.setVisibility(View.GONE);
        }
    }

    private void updateACardInfo(byte[] card_no, boolean display) {
        if (display) {
            linearLayoutAddress.setVisibility(View.VISIBLE);
            linearLayoutChName.setVisibility(View.GONE);
            mTextViewNationTitle.setText(R.string.nation);
            mTextViewName.setText("");
            mTextViewGender.setText("");
            mTextViewNation.setText("");
            mTextViewYear.setText("");
            mTextViewMonth.setText("");
            mTextViewDay.setText("");
            mTextViewAddress.setText("");
            mTextViewIDNoTitle.setText(R.string.idno_A);

            String cardANo = "";
            //cardANo=String.format("%02X%02X%02X%02X", data[0], data[1], data[2], data[3]);
            for (int i = 0; i < card_no.length; i++) {
                cardANo += String.format("%02X", card_no[i]);
            }
            mTextViewIDNo.setText(cardANo);
            mTextViewAgency.setText("");
            mTextViewExpire.setText("");
            mImageViewPortrait.setImageBitmap(null);
        } else {
            mTextViewAgencyCode.setText("");
            mTextViewVer.setText("");
            linearLayoutAddress.setVisibility(View.VISIBLE);
            linearLayoutChName.setVisibility(View.GONE);
            mTextViewNationTitle.setText(R.string.nation);
            mTextViewName.setText("");
            mTextViewGender.setText("");
            mTextViewNation.setText("");
            mTextViewYear.setText("");
            mTextViewMonth.setText("");
            mTextViewDay.setText("");
            mTextViewAddress.setText("");
            mTextViewIDNoTitle.setText(R.string.idno);
            mTextViewIDNo.setText("");
            mTextViewAgency.setText("");
            mTextViewExpire.setText("");
            mTextViewPassportNum.setText("");
            mTextViewIssuranceTimes.setText("");
            mImageViewPortrait.setImageBitmap(null);
        }
        //卡体管理号
        card_no_linear_layout.setVisibility(View.GONE);
    }

    /**
     * 判断身份证是否过期
     *
     * @param expireEnd
     * @return
     */
    private boolean isExpire(String expireEnd) {
        if (expireEnd.startsWith("长期")) {
            return false;
        } else {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(format.parse(expireEnd));
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
            cal.roll(Calendar.DAY_OF_MONTH, 1);
            return cal.getTime().compareTo(new Date()) < 0;
        }
    }

    @Override
    public void onReadIDCardSuccess(final IDCardInfo iDCardInfo) {
        if (iDCardInfo != null) {
            Log.d(TAG, "name is " + iDCardInfo.name);
            updateIDCardInfo(iDCardInfo, true);
            updateStatus("读卡成功");

            requestUserById(new OnResultListener<List<User>>() {
                @Override
                public void onResult(List<User> result) {
                    if (result != null) {
                        final User user = result.get(0);

                        Bitmap bitmap = ImageUtils.stringToBitmap(user.getImage());

                        byte[] features1 = new byte[512];
                        byte[] features2 = new byte[512];

                        FaceApi.getInstance().getFeature(iDCardInfo.photo, features1, BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO);
                        FaceApi.getInstance().getFeature(bitmap, features2, BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO);

//                        float ret1 = FaceSDKManager.getInstance().personDetect(iDCardInfo.photo, features1);
//                        float ret2 = FaceSDKManager.getInstance().personDetect(bitmap, features2);

                        float score = FaceSDKManager.getInstance().getFaceFeature().featureCompare(
                                BDFaceSDKCommon.FeatureType.BDFACE_FEATURE_TYPE_ID_PHOTO,
                                features1, features2, true);
                        Log.e(TAG, Arrays.toString(features1) + "");
                        Log.e(TAG, Arrays.toString(features2) + "");
                        Log.e(TAG, score + "");

                        if (score >= Config.scoreThreshold) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTextViewCardNo.setText(user.getIdCardNo());
                                    tvCheckResult.setText("认证通过");
                                    tvCheckResult.setTextColor(getResources().getColor(R.color.green));
                                }
                            });

                            try {
                                final FileOutputStream fos1 = new FileOutputStream("/sys/exgpio/relay1");
                                fos1.write("1".getBytes());
                                Log.e(TAG, "OPEN");


                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            fos1.write("0".getBytes());
                                            Log.e(TAG, "CLOSE");
                                            fos1.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            tvCheckResult.setText("认证失败");
                            tvCheckResult.setTextColor(getResources().getColor(R.color.red));
                        }

                    }
                }

                @Override
                public void onError(FaceError error) {
                    Log.e(TAG, "ERROR ===== " + error);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvCheckResult.setText("认证失败");
                            tvCheckResult.setTextColor(getResources().getColor(R.color.red));
                        }
                    });
                }
            }, iDCardInfo.id);

        }
    }


    private void requestUserByAudienceCode(final OnResultListener<List<User>> listener, String audienceCode) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            long timestamp = System.currentTimeMillis() / 1000;

            HttpUrl url = Objects.requireNonNull(HttpUrl.parse("https://5jyw.com:444/bdface/api")).newBuilder()
                    .addQueryParameter("cmd", "getUsers")
                    .addQueryParameter("timestamp", timestamp + "")
                    .addQueryParameter("token", Utils.md5(timestamp + Config.API_KEY))
                    .build();

//            String reqTime = FaceApi.getInstance().getLatestCTime();
//            Log.e(TAG, reqTime);
            RequestBody body = new FormBody.Builder().add("audience_code", audienceCode).build();

            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "network request error", e);
                    listener.onError(error);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String haha = response.body().string();
                    if (response == null || response.body() == null || TextUtils.isEmpty(haha)) {
                        FaceError error = new FaceError(FaceError.ErrorCode.ACCESS_TOKEN_PARSE_ERROR, "token is parse error, please rerequest token");
                        listener.onError(error);

                    }
                    try {
                        JSONObject jsonObject = new JSONObject(haha);

                        Gson gson = new Gson();
                        List<User> userList = gson.fromJson(jsonObject.getJSONArray("data").toString(), new TypeToken<List<User>>() {
                        }.getType());

                        listener.onResult(userList);

                    } catch (Exception e) {
                        e.printStackTrace();
                        FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "response with error", e);
                        listener.onError(error);
                    }
                }
            });
//            Log.e(TAG, "===================" + s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void requestUserById(final OnResultListener<List<User>> listener, String idCardNo) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();

            long timestamp = System.currentTimeMillis() / 1000;

            HttpUrl url = Objects.requireNonNull(HttpUrl.parse("https://5jyw.com:444/bdface/api")).newBuilder()
                    .addQueryParameter("cmd", "getUsers")
                    .addQueryParameter("timestamp", timestamp + "")
                    .addQueryParameter("token", Utils.md5(timestamp + Config.API_KEY))
                    .build();

//            String reqTime = FaceApi.getInstance().getLatestCTime();
//            Log.e(TAG, reqTime);
            RequestBody body = new FormBody.Builder().add("idCardNo", idCardNo).build();

            final Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "network request error", e);
                    listener.onError(error);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String haha = response.body().string();
                    if (response == null || response.body() == null || TextUtils.isEmpty(haha)) {
                        FaceError error = new FaceError(FaceError.ErrorCode.ACCESS_TOKEN_PARSE_ERROR, "token is parse error, please rerequest token");
                        listener.onError(error);

                    }
                    try {
                        JSONObject jsonObject = new JSONObject(haha);

                        Gson gson = new Gson();
                        List<User> userList = gson.fromJson(jsonObject.getJSONArray("data").toString(), new TypeToken<List<User>>() {
                        }.getType());

                        listener.onResult(userList);

                    } catch (Exception e) {
                        e.printStackTrace();
                        FaceError error = new FaceError(FaceError.ErrorCode.NETWORK_REQUEST_ERROR, "response with error", e);
                        listener.onError(error);
                    }
                }
            });
//            Log.e(TAG, "===================" + s);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onReadIDCardFailed() {
        Log.d(TAG, "read card fail");
        updateStatus("读卡失败");
    }


    @Override
    public void onCardRemoved() {
        Log.d(TAG, "card removed");
        updateIDCardInfo(null, false);
        updateStatus("卡已离开,请放卡");
    }

    private String getDate() {    // 得到的是一个日期：格式为：yyyy-MM-dd HH:mm:ss.SSS
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date());// 将当前日期进行格式化操作
    }

    private void writeDevInfo(String devinfo, String writePath) {
        File file = new File(writePath);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(writePath);
            fos.write(devinfo.getBytes());
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void appendLog(int code, String log) {
//		Log.d(TAG, "appendLog code is " + code + ";log is " + log);
        if (code == AppendLogEvent.LOG_CODE_SAMID) {
            Log.d(TAG, "got samid");
            String termsn = DeviceInfo.getTermSnMd5();
            String samId = log.replace("-", "").replace(".", "");
            samId = samId.substring(0, 22);
            String devinfo = termsn + "," + samId + "\n";
            writeDevInfo(devinfo, Environment.getExternalStorageDirectory().getPath() + "/devinfo.txt");
        } else if (code == AppendLogEvent.LOG_CODE_REQ_USB_PERMISSION) {
            isReqUSBPermission = true;
        }

        String notice = txtLog.getText().toString();
        if (!TextUtils.isEmpty(notice) && notice.endsWith(log)) {
            return;
        }
        notice += "\n" + getDate() + ":" + log;
        if (!TextUtils.isEmpty(notice)) {
            int num = notice.length() - notice.replaceAll("\n", "").length();
            if (num > 15) {
                notice = notice.substring(notice.indexOf("\n", 1));
            }
        }
        txtLog.setText(notice);
    }

    @Override
    public void onGotStartReadcardResult(int error_code, boolean has_inner_reader) {
        appendLog(AppendLogEvent.LOG_CODE_ANY, "onGotStartReadcardResult " + error_code);
        if (error_code == ErrorCode.SUCCESS) {
            updateStatus("请放卡");
        } else {
            updateStatus("启动读卡失败(" + error_code + ")");
        }
    }

    @Override
    public void onReadACardSuccess(byte[] card_sn) {
        if (card_sn != null) {
            updateACardInfo(card_sn, true);
        }
    }

    @Override
    public void onReaderStopped() {
        Log.d(TAG, "onReaderStopped");
//		updateStatus(getText(R.string.readcard_hint).toString());
//		buttonStart.setEnabled(true);
//		buttonStop.setEnabled(false);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            mDeviceType = DeviceParamBean.DEV_TYPE_INNER_OR_HID;
        } else if (position == 1) {
            mDeviceType = DeviceParamBean.DEV_TYPE_BT;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * 监听蓝牙开关状态
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OnActionStateChangedEvent event) {
        if (event != null && event.getState() != null) {
            if (mDeviceType != DeviceParamBean.DEV_TYPE_BT) {
                return;
            }
            //蓝牙已开启
            if (event.getState().equals("ON")) {
                appendLog(AppendLogEvent.LOG_CODE_ANY, "已开启蓝牙");
                //检查蓝牙设备
                checkBTDevice();
            }
        }
    }

    /**
     * 监听扫描设备结果
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OnActionDeviceFoundEvent event) {
        Log.d(TAG, "onEvent OnActionDeviceFoundEvent");
        if (event != null && event.getDevice() != null) {
            if (mDeviceType != DeviceParamBean.DEV_TYPE_BT) {
                return;
            }
            BluetoothDevice device = event.getDevice();
            String device_info = "";
            if (device.getName() != null) {
                device_info += device.getName() + "@";
            }
            if (device.getAddress() != null) {
                device_info += device.getAddress();
            }
            Log.d(TAG, "device_info " + device_info);
            //搜索到以iDR开头的蓝牙设备，添加到列表
            if (!mListBTDeviceInfo.contains(device_info)) {
                mListBTDeviceInfo.add(device_info);
                mListBTDevice.add(device);
                mBTFoundAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 监听扫描结束事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OnActionDiscoveryStateChangedEvent event) {
        Log.d(TAG, "onEvent OnActionDiscoveryStateChangedEvent");
        if (event != null && event.getState() != null) {
            if (mDeviceType != DeviceParamBean.DEV_TYPE_BT) {
                return;
            }
            if (event.getState().equals("STARTED")) {
                mAlertDialogBT.setTitle("搜索启动");
            } else if (event.getState().equals("FINISHED")) {
                mAlertDialogBT.setTitle("搜索结束");
            }
        }
    }

    /**
     * 监听Bond状态改变结果
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OnActionBondStateChangedEvent event) {
        if (event != null && event.getDevice() != null) {
            if (mDeviceType != DeviceParamBean.DEV_TYPE_BT) {
                return;
            }
            if (event.getDevice().equals(mBTDeviceSelected) &&
                    event.getDevice().getBondState() == BluetoothDevice.BOND_BONDED) {
                appendLog(AppendLogEvent.LOG_CODE_ANY, "绑定设备" + mBTDeviceSelected.getName() + "成功");
                DeviceParamBean devParamBean = new DeviceParamBean();
                devParamBean.setDeviceType(mDeviceType);
                devParamBean.setUserObj(mBTDeviceSelected);
                //绑定成功,去启动读卡
                mReaderPresenter.startReadcard(devParamBean);
            }
        }
    }
}
