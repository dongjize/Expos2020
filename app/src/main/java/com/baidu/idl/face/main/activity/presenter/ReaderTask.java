package com.baidu.idl.face.main.activity.presenter;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.greenrobot.eventbus.EventBus;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.baidu.idl.face.main.activity.model.AppendLogEvent;
import com.baidu.idl.face.main.activity.model.DeviceParamBean;
import com.baidu.idl.face.main.activity.model.ReportCardRemovedEvent;
import com.baidu.idl.face.main.activity.model.ReportReadACardEvent;
import com.baidu.idl.face.main.activity.model.ReportReadIDCardEvent;
import com.baidu.idl.face.main.activity.model.ReportReadcardThreadExitEvent;
import com.baidu.idl.face.main.activity.model.ReportStartReadcardResultEvent;
import com.routon.plsy.device.sdk.DeviceInfo;
import com.routon.plsy.device.sdk.DeviceModel;
import com.routon.plsy.reader.sdk.bt.BTImpl;
import com.routon.plsy.reader.sdk.ccid.CCIDImpl;
import com.routon.plsy.reader.sdk.common.Common;
import com.routon.plsy.reader.sdk.common.ErrorCode;
import com.routon.plsy.reader.sdk.common.Info.IDCardInfo;
import com.routon.plsy.reader.sdk.common.Info.MoreAddrInfo;
import com.routon.plsy.reader.sdk.common.Info.SAMIDInfo;
import com.routon.plsy.reader.sdk.common.ReaderType;
import com.routon.plsy.reader.sdk.intf.IReader;
import com.routon.plsy.reader.sdk.serial.SerialImpl;
import com.routon.plsy.reader.sdk.usb.USBImpl;


public class ReaderTask {
	private final String TAG = "ReaderTask";
    private static ReaderTask INSTANCE = null;
    public  static ReaderTask create(Context context, ReadIDCardMode mode){
        if(null == INSTANCE){
            INSTANCE = new ReaderTask(context, mode);
        }
        INSTANCE.setContext(context);
        INSTANCE.setReadIDCardMode(mode);
        return INSTANCE;
    }
    
    public enum ReadIDCardMode{
        /** 读一次就退出*/
        Single,
        /** 每次读完卡，不立即找卡，等卡片放置较长时间或卡片离开再开始找下一张卡*/
        loop,
        /** 一直不停的找卡选卡读卡*/
        continual,
        cmd //按命令执行读卡操作
    }
    
    private boolean is_quit = false;
    private Context mContext;
	private ReadIDCardMode mReadIDCardMode;
	public void setContext(Context context) {
		this.mContext = context;
	}
    public void setReadIDCardMode(ReadIDCardMode readIDCardMode) {
		this.mReadIDCardMode = readIDCardMode;
	}

	private DeviceParamBean mDeviceParamBean;
    private int mDeviceType = DeviceParamBean.DEV_TYPE_UNKNOWN;
    private ReadIDCardMode readCardMode;
    private HandlerThread threadAsyncHandler;
    private Handler handlerAsyncTask;
    private UsbDevPermissionMgr mUsbPermissionMgr;
    private UsbManager mUsbMgr;
    
    
	private final int DEF_SERIAL_BAUDRATE = 115200;
	private final String DEF_SERIAL_PORT_NAME_14T = "/dev/ttyS4";
	private final String DEF_SERIAL_PORT_NAME_iDR420 = "/dev/ttyS4";
	private final String DEF_SERIAL_PORT_NAME_iDR500_1 = "/dev/ttyMT1";
	private final String DEF_SERIAL_PORT_NAME_iDR420_1 = "/dev/ttyMT1";// lihuili add 20170829 for iDR420-1
    private String getPortname(){
    	DeviceModel mDevModel = DeviceInfo.getDeviceModel();
		if (mDevModel.equals(DeviceModel.iDR420)) {
			return DEF_SERIAL_PORT_NAME_iDR420;
		} else if (mDevModel.equals(DeviceModel.iDR500_1)) {
			return DEF_SERIAL_PORT_NAME_iDR500_1;
		} else if (mDevModel.equals(DeviceModel.CI_14T)) {
			return DEF_SERIAL_PORT_NAME_14T;
		} else if (mDevModel.equals(DeviceModel.iDR420_1)) {
			return DEF_SERIAL_PORT_NAME_iDR420_1;
		} else {
			return DEF_SERIAL_PORT_NAME_iDR420;
		}
    }
    
    private ReaderTask(Context context, ReadIDCardMode mode){
    	mContext = context;
    	mReadIDCardMode = mode;
    }

	/**
	 */
	public void init() {
		threadAsyncHandler = new HandlerThread("ReaderHandler");
    	threadAsyncHandler.start();
    	handlerAsyncTask = new ReaderAsyncHandler(threadAsyncHandler.getLooper());
        readCardMode = mReadIDCardMode;
        
        mUsbMgr = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
        mUsbPermissionMgr = new UsbDevPermissionMgr(mContext, mUsbCallBack);
	}
    
    public void release(){
        if(threadAsyncHandler != null && threadAsyncHandler.getLooper()!=null  && threadAsyncHandler.getLooper().getThread().isAlive()){
            Message msg = handlerAsyncTask.obtainMessage(MSG_NOTIFY_QUIT);
            handlerAsyncTask.sendMessage(msg);
            
        }else{
        	Log.e(TAG, "threadAsyncHandler is unavailable");
        }
    }
    
    public void startReadcard(DeviceParamBean devParamBean){
    	if(threadAsyncHandler != null  && threadAsyncHandler.getLooper()!=null && threadAsyncHandler.getLooper().getThread().isAlive()){
			Message msg = handlerAsyncTask.obtainMessage(MSG_START_READCARD);
			msg.obj = devParamBean;
			handlerAsyncTask.sendMessage(msg);         
    	}
    }
    
    public void stopReadcard(){
	    if(threadAsyncHandler != null && threadAsyncHandler.getLooper()!=null && threadAsyncHandler.getLooper().getThread().isAlive()){
	    	 Message msg = handlerAsyncTask.obtainMessage(MSG_STOP_READCARD);
	         handlerAsyncTask.sendMessage(msg);
	    }
    }
    
    public void sendCmd(int cmd){
    	if(threadAsyncHandler != null && threadAsyncHandler.getLooper()!=null && threadAsyncHandler.getLooper().getThread().isAlive()){
	    	 Message msg = handlerAsyncTask.obtainMessage(MSG_SEND_CMD);
	    	 msg.obj = cmd;
	         handlerAsyncTask.sendMessage(msg);
	    }
    }
    
    public void onReaderCardThreadQuited()
    {
    	if(threadAsyncHandler != null && threadAsyncHandler.getLooper()!=null && threadAsyncHandler.getLooper().getThread().isAlive()){
	    	Message msg = handlerAsyncTask.obtainMessage(MSG_READCARD_THREAD_QUITED);
	        handlerAsyncTask.sendMessage(msg);
    	}
    }
    
    private UsbDevPermissionMgr.UsbDevPermissionMgrCallback mUsbCallBack = new UsbDevPermissionMgr.UsbDevPermissionMgrCallback() {
        public void onUsbDevReady(UsbDevice device) {
        	if(threadAsyncHandler != null && threadAsyncHandler.getLooper()!=null  && threadAsyncHandler.getLooper().getThread().isAlive()){
	            Message msg = handlerAsyncTask.obtainMessage(MSG_USB_DEV_ATTACHED, device);
	            handlerAsyncTask.sendMessage(msg);
        	}
        }
        
        public void onUsbDevRemoved(UsbDevice device) {
        	if(threadAsyncHandler != null && threadAsyncHandler.getLooper()!=null && threadAsyncHandler.getLooper().getThread().isAlive()){
	            Message msg = handlerAsyncTask.obtainMessage(MSG_USB_DEV_DETACHED, device);
	            handlerAsyncTask.sendMessage(msg);
        	}
        }

        public void onNoticeStr(String notice) {
        	 AppendLogEvent appendLogEvent = new AppendLogEvent();
        	 appendLogEvent.setLog(notice);
             EventBus.getDefault().post(appendLogEvent);
        }

		@Override
		public void onUsbRequestPermission() {
			// TODO Auto-generated method stub
			AppendLogEvent appendLogEvent = new AppendLogEvent();
			appendLogEvent.setCode(AppendLogEvent.LOG_CODE_REQ_USB_PERMISSION);
			appendLogEvent.setLog("To request permission");
            EventBus.getDefault().post(appendLogEvent);
		}
    };
    private final int MSG_CHECK_INNER_READER = 1;
    private final int MSG_USB_DEV_ATTACHED = 2;
    private final int MSG_USB_DEV_DETACHED = 3;
    private final int MSG_NOTIFY_QUIT = 4; //被view层通知退出
    private final int MSG_READCARD_THREAD_QUITED = 5; //线程停止后汇报
    private final int MSG_START_READCARD = 6;
    private final int MSG_STOP_READCARD = 7;
    private final int MSG_SEND_CMD = 8;
    
    private class ReaderAsyncHandler extends Handler{
    	private final String TAG = "ReaderAsyncHandler";
    	private final int READER_ST_INIT = 0;
    	private final int READER_ST_WORK = 1;
    	private final int READER_ST_CMD = 2;
    	private int mReader_st = READER_ST_INIT;
    	
        private ReadCardThread threadReadCard;
        private UsbDevice waitingDevice = null;
        private IReader mReader;
        
        private int setReaderState(int new_st){
        	if(mReader_st!=new_st){
        		Log.d(TAG, "ReaderState from " + mReader_st + " to " + new_st);
        	}
        	mReader_st = new_st;
        	return mReader_st;
        }
        private boolean processAny(Message msg){
        	boolean is_process = false;
        	switch(msg.what){
        	case MSG_STOP_READCARD:
        	{
        		if(threadReadCard!=null){
        			threadReadCard.interrupt();
        		}
        		is_process = true;
        	}
        		break;
        	case MSG_NOTIFY_QUIT:
        	{
        		Log.d(TAG, "MSG_NOTIFY_QUIT");
        		if(threadReadCard!=null){
        			threadReadCard.interrupt();
        			threadReadCard = null;
        		}
    			postStopReadCard();
    			
    			if(mUsbPermissionMgr!=null){
    				mUsbPermissionMgr.releaseMgr();
    			}
    			threadAsyncHandler = null;
                handlerAsyncTask = null;
        		is_process = true;
        	}
        	break;
        	case MSG_READCARD_THREAD_QUITED:
        	{
        		Log.d(TAG, "MSG_READCARD_THREAD_QUITED");
        		threadReadCard = null;
        		postStopReadCard();
        		setReaderState(READER_ST_INIT);
        		is_process = true;
        	}
        		break;
        	
        	default:break;
        	}
        	return is_process;
        }
		/**
		 * 
		 */
		private void postStopReadCard() {
			if(mReader!=null){				
				// 给安全模块下电
				mReader.RTN_SetSAMPower(0);
				mReader.SDT_ClosePort();
				mReader = null;
			}
			DeviceInfo.PowerOffReader();
			
			
		}
		
        private void processInit(Message msg){
        	switch(msg.what){
        	case MSG_SEND_CMD:{
        		Integer cmd = (Integer)msg.obj;
        		if(cmd!=null && cmd == IReaderPresenter.CMD_OPEN){
//	        		readCardMode = ReadIDCardMode.cmd;
//	        		Message send_msg = obtainMessage();
//	        		send_msg.what = MSG_START_READCARD;
//	        		sendMessage(send_msg);
        		}
        		break;
        	}
        	case MSG_START_READCARD:
        	{
        		mDeviceParamBean = (DeviceParamBean)msg.obj;
        		mDeviceType = mDeviceParamBean.getDeviceType();
        		if(mDeviceType==DeviceParamBean.DEV_TYPE_BT){
        			if(mDeviceParamBean.getUserObj() instanceof BluetoothDevice){
        				BluetoothDevice device = (BluetoothDevice)mDeviceParamBean.getUserObj();
        				int ret = ErrorCode.ErrCodeCCID_E_INIT_FAIL;
        				mReader = new BTImpl();
//        				mReader.setDebug(true);
        	    		ret = mReader.SDT_OpenPort(device);
        	    		Log.d(TAG, "SDT_OpenPort ret="+ret);
                		if(ret >= 0){
                			setReaderState(READER_ST_WORK);
    	                	if(threadReadCard!=null){
    	                		threadReadCard.interrupt();
    	                	}
    	                	threadReadCard = new ReadBCardThread(mReader);
//    	                	threadReadCard = new ReadABCardThread(mReader);
    	                    threadReadCard.start();
    	                    Log.d(TAG, "threadReadCard started");
    	                    ReportStartReadcardResultEvent reportStartReadcardSuccessEvent = new ReportStartReadcardResultEvent();
    	                    reportStartReadcardSuccessEvent.setError_code(ErrorCode.SUCCESS);
    	                    reportStartReadcardSuccessEvent.setHave_inner_reader(true);
    	                	EventBus.getDefault().post(reportStartReadcardSuccessEvent);
                		}
        			}
        		}else{
	        		//触发接受USB设备拔插广播
	        		boolean has_outer_device = mUsbPermissionMgr.initMgr();
	        		if(!has_outer_device){
		        		//触发检测内置读卡器
		        		Message send_msg = obtainMessage(MSG_CHECK_INNER_READER);
		                sendMessage(send_msg);
	        		}
        		}
        	}
        		break;
        	case MSG_CHECK_INNER_READER:{
        		// 给读卡器上电
    			DeviceInfo.PowerOnReader();
    			ReaderType  rdrType = Common.getReaderType();
    			int ret = ErrorCode.ErrCodeCCID_E_INIT_FAIL;
    			switch(rdrType){
    			case SERIAL:{
    				mReader = new SerialImpl();
//    				mReader.setDebug(true);
    	    		ret = mReader.SDT_OpenPort(getPortname(), DEF_SERIAL_BAUDRATE);
    			}
    				break;
    			case USBCCID:{ //TODO:调试CCID读卡器
    				mReader = new CCIDImpl();
    	    		ret = mReader.SDT_OpenPort();
    			}
    				break;
    			default:
    				break;
    			}
    			
        		Log.d(TAG, "SDT_OpenPort ret="+ret);
        		if(ret >= 0){
        			// 返回：>0版本号长度;其他为失败
        			byte[] outVer = new byte[6];
        			int verRet = mReader.RTN_TypeAGetMcuVersion(outVer);
        			Log.d(TAG, "RTN_TypeAGetMcuVersion verRet="+verRet);
        			if(verRet > 0){
        				// 给安全模块上电
						ret = mReader.RTN_SetSAMPower(1);
						//安全模块初始化需要时间
						SystemClock.sleep(2000);
						Log.d(TAG, "after RTN_SetSAMPower 1");
						
						setReaderState(READER_ST_WORK);
	                	if(threadReadCard!=null){
	                		threadReadCard.interrupt();
	                	}
	                	threadReadCard = new ReadBCardThread(mReader);
//	                	threadReadCard = new ReadABCardThread(mReader);
	                    threadReadCard.start();
	                    Log.d(TAG, "threadReadCard started");
	                    ReportStartReadcardResultEvent reportStartReadcardSuccessEvent = new ReportStartReadcardResultEvent();
	                    reportStartReadcardSuccessEvent.setError_code(ErrorCode.SUCCESS);
	                    reportStartReadcardSuccessEvent.setHave_inner_reader(true);
	                	EventBus.getDefault().post(reportStartReadcardSuccessEvent);
        			}else{
        				//给读卡器下电
        				DeviceInfo.PowerOffReader();
        			}
        		}else{
        			ReportStartReadcardResultEvent reportStartReadcardSuccessEvent = new ReportStartReadcardResultEvent();
        			reportStartReadcardSuccessEvent.setError_code(ret);
        			EventBus.getDefault().post(reportStartReadcardSuccessEvent);
        		}
        	}
        		break;
        	case MSG_USB_DEV_ATTACHED:{
        		waitingDevice = (UsbDevice) msg.obj;
                
                mReader = new USBImpl();
                int ret = mReader.SDT_OpenPort(mUsbMgr, waitingDevice);
                Log.d(TAG, "SDT_OpenPort ret="+ret);
                ReportStartReadcardResultEvent reportStartReadcardResultEvent = new ReportStartReadcardResultEvent();
                if(ret >= 0){
                	if(readCardMode.equals(ReadIDCardMode.cmd)){
						setReaderState(READER_ST_CMD);
						break;
					}
                	
                	setReaderState(READER_ST_WORK);
                	if(threadReadCard!=null){
                		threadReadCard.interrupt();
                	}
                	threadReadCard = new ReadBCardThread(mReader);
                    threadReadCard.start();
                    
                    reportStartReadcardResultEvent.setError_code(ErrorCode.SUCCESS);
                    reportStartReadcardResultEvent.setHave_inner_reader(false);
                }else{
        			reportStartReadcardResultEvent.setError_code(ret);
        		}
                EventBus.getDefault().post(reportStartReadcardResultEvent);
                break;
        	}
        	default:break;
        	}
        }
        private void processWork(Message msg){
        	switch(msg.what){
			case MSG_USB_DEV_DETACHED:{
				setReaderState(READER_ST_INIT);
				if(waitingDevice != null){
					waitingDevice = null;
				}
				if(threadReadCard != null){
				    threadReadCard.setDevRemoved();
				}
			}
			break;			
        	default:break;
        	}
        }
        
        private void processCmd(Message msg){
        	switch(msg.what){
			case MSG_USB_DEV_DETACHED:{
				setReaderState(READER_ST_INIT);
				if(waitingDevice != null){
					waitingDevice = null;
				}
				if(threadReadCard != null){
				    threadReadCard.setDevRemoved();
				}
			}
			break;
			case MSG_SEND_CMD:
			{
				Integer cmd = (Integer)msg.obj;
        		if(cmd!=null){
        			switch(cmd){
        			case IReaderPresenter.CMD_FIND:
        			{
        				if(mReader!=null){
        					byte[] sn = new byte[8];
        					int ret = mReader.SDT_FindIDCard(sn);
        					appendLog(AppendLogEvent.LOG_CODE_ANY, "SDT_FindIDCard return " + ret);
        				}
        			}
        				break;
        			case IReaderPresenter.CMD_SELECT:
        			{
        				if(mReader!=null){
        					byte[] sn = new byte[8];
        					int ret = mReader.SDT_SelectIDCard(sn);
        					appendLog(AppendLogEvent.LOG_CODE_ANY, "SDT_SelectIDCard return " + ret);
        				}
        			}
        				break;
        			case IReaderPresenter.CMD_READBASEMSG:
        			{
        				if(mReader!=null){
        					IDCardInfo iDCardInfo = new IDCardInfo();
                            int ret = mReader.RTN_ReadBaseMsg(iDCardInfo);
                            appendLog(AppendLogEvent.LOG_CODE_ANY, "RTN_ReadBaseMsg return " + ret);
        				}
        			}
        				break;
        			case IReaderPresenter.CMD_CLOSE:
        			{
        				readCardMode = ReadIDCardMode.loop;
        				if(mReader!=null){
        					mReader.SDT_ClosePort();
        					mReader = null;
                		}
                		DeviceInfo.PowerOffReader();
                		setReaderState(READER_ST_INIT);
        			}
        				break;
        			default:
        				break;
        			}
        		}
			}
				break;
        	default:break;
        	}
        }
        public ReaderAsyncHandler(Looper looper){
            super(looper);
            setReaderState(READER_ST_INIT);
        }
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            Log.d(TAG, "recv msg " + msg.what);
            boolean is_process = processAny(msg);
            if(!is_process){
	            switch(mReader_st){
	            case READER_ST_INIT:
	            {
	            	processInit(msg);
	            }
	            	break;
	            case READER_ST_WORK:
	            {
	            	processWork(msg);
	            }
	            	break;
	            case READER_ST_CMD:
	            {
	            	processCmd(msg);
	            }
	            	break;
	            default:break;
	            }
            }
        }
    }
    
    private String getDate(){    // 得到的是一个日期：格式为：yyyy-MM-dd HH:mm:ss.SSS
		SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS") ;
	    return sdf.format(new Date()) ;// 将当前日期进行格式化操作
	}
    private String last_log = "";
    private void appendLog(int code, String log){
    	if(!last_log.equals(log)){
//	    	Log.d(TAG, "appendLog " + log);
	    	AppendLogEvent appendLogEvent = new AppendLogEvent();
	    	appendLogEvent.setCode(code);
//    	appendLogEvent.setLog(getDate() + ":" + log);
    		appendLogEvent.setLog(log);
    		EventBus.getDefault().post(appendLogEvent);
    		
    		last_log = log;
    	}
    }
    private void reportExit(){
    	ReportReadcardThreadExitEvent reportReadcardThreadExitEvent = new ReportReadcardThreadExitEvent();
    	EventBus.getDefault().post(reportReadcardThreadExitEvent);
    }
    
    private abstract class ReadCardThread extends Thread{
    	abstract public void setDevRemoved();
    	public int B_ROLL_INTERVAL = 100; //ms
    	public int AB_ROLL_INTERVAL = 200; //ms
    }
    
    private class ReadBCardThread extends ReadCardThread{
    	private final String TAG = "ReadCardThread";
        private IDCardInfo iDCardInfo = null;
        private volatile boolean bDevRemoved = false;
        ReadIDCardMode mode = ReadIDCardMode.loop;
        private IReader mReader;
        

        public ReadBCardThread(IReader reader){
            this.mReader = reader;
            this.mode = readCardMode;
            bDevRemoved = false;
        }
        
        public void setDevRemoved(){
            bDevRemoved = true;
        }
        
        @Override
        public void run() {
            super.run();
            int ret = 0;
            do{
	            if(isInterrupted()){
	            	break;
	            }
	            // read samId, when success, sam is ready
	            SAMIDInfo samIDInfo = new SAMIDInfo();
	            ret = mReader.SDT_GetSAMIDToStr(samIDInfo);
	            if(ret == 0){
	            	Log.d(TAG, "SDT_GetSAMIDToStr: SAMID is " + samIDInfo.SAMID);
	            	appendLog(AppendLogEvent.LOG_CODE_SAMID, samIDInfo.SAMID);
	            	break;
	            }else{
	            	SystemClock.sleep(20);
	            }
            }while(true);
            if (0 == ret) {
                // start to read card
                READ_CARD_LOOP:
                do {
                	if(isInterrupted()){
    	            	break;
    	            }
                    if(bDevRemoved){
                        break;
                    }
                    

                    byte[] sn = new byte[8];
                    ret = mReader.SDT_FindIDCard(sn);                    
                    if(ret < ErrorCode.SUCCESS){
                    	appendLog(AppendLogEvent.LOG_CODE_ANY, "SDT_FindIDCard return " + ret);
                    	SystemClock.sleep(B_ROLL_INTERVAL);
                    	continue READ_CARD_LOOP;
                    }
                    appendLog(AppendLogEvent.LOG_CODE_ANY, "Found B Card");
                    ret = mReader.SDT_SelectIDCard(sn);
                    if(ret < ErrorCode.SUCCESS){
                    	appendLog(AppendLogEvent.LOG_CODE_ANY, "SDT_SelectIDCard return " + ret);
                    	SystemClock.sleep(B_ROLL_INTERVAL);
                    	continue READ_CARD_LOOP;
                    }
                    boolean readcardSucc = false;
                   
                    // find card success. notice UI clean idcardInfo
                    if(iDCardInfo != null && iDCardInfo.photo != null){
                    	iDCardInfo.photo.recycle();
                    }
                    iDCardInfo = new IDCardInfo();
                    ret = mReader.RTN_ReadBaseMsg(iDCardInfo);
                    appendLog(AppendLogEvent.LOG_CODE_ANY, "RTN_ReadBaseMsg return " + ret);
//                      ret = mReader.RTN_ReadBaseFPMsg(iDCardInfo);
                    ReportReadIDCardEvent reportReadIDCardEvent = new ReportReadIDCardEvent();
                    if (ErrorCode.SUCCESS == ret) { // read card successful
                        readcardSucc = true;
                        reportReadIDCardEvent.setiDCardInfo(iDCardInfo);
                    } else {
                    	readcardSucc = false;
                    }
                    reportReadIDCardEvent.setSuccess(readcardSucc);
                    EventBus.getDefault().post(reportReadIDCardEvent);
                    
                    if(ReadIDCardMode.Single.equals(mode)){
                        break;
                    }else if(ReadIDCardMode.continual.equals(mode)){
                        // do nothing;
                    }else if(ReadIDCardMode.loop.equals(mode) && readcardSucc){
                        long startTime = System.currentTimeMillis();
                        do{
                        	if(isInterrupted()){
            	            	break;
            	            }
                            if(bDevRemoved){
                                break READ_CARD_LOOP;
                            }
                            ret = mReader.RTN_ReadNewAppMsg(new MoreAddrInfo());
                            appendLog(AppendLogEvent.LOG_CODE_ANY, "RTN_ReadNewAppMsg return " + ret);
                            if(ret < 0){// card removed
                                ReportCardRemovedEvent reportCardRemovedEvent = new ReportCardRemovedEvent();
                                EventBus.getDefault().post(reportCardRemovedEvent);
                                break;
                            }else{
                            	//lihuili：有判断卡在位超过10秒的需要时可还原代码
//                            	long currentTime = System.currentTimeMillis();
//                            	if((currentTime - startTime) > 10000){
//	                                // 有客户要求 5s、10s内不重复读卡，超过即便卡没离开也要重复读， 可按照需求决定是否需要这个判断。
//                            		// 卡在位超过10秒
//                            		appendLog("card on over 10s");
//	                                break;
//	                            }else{
	                                if(bDevRemoved){
	                                    break READ_CARD_LOOP;
	                                }
	                                SystemClock.sleep(50);
//	                            }
                            }
                        }while(true);
                    }
                    if(bDevRemoved){
                        break;
                    }
                    // 读完卡需要间隔一段时间再找卡。
                    SystemClock.sleep(B_ROLL_INTERVAL);
                } while (true);
            }
            appendLog(AppendLogEvent.LOG_CODE_ANY, "finish read card");
            reportExit();           
        }
    }
    
    private class ReadABCardThread extends ReadCardThread{
    	private final String TAG = "ReadABCardThread";
    	private final int CARD_TYPE_NO = 0; //无卡
        private final int CARD_TYPE_A = 1;  //有A卡
        private final int CARD_TYPE_B = 2;  //有B卡
        private int mCardType = CARD_TYPE_NO;
    	private IDCardInfo iDCardInfo = null;
        private volatile boolean bDevRemoved = false;
        ReadIDCardMode mode = ReadIDCardMode.loop;
        private IReader mReader;
        /**
         * 找A卡
         * 特别说明：A卡在位时，找A卡偶尔会失败，所以要增加重试机制，连续n次找不到A卡才认为卡不在位
         */
        private final int MAX_A_CARD_OFF_CNT = 2;
        private int Find_A_Card() {
    		int ret = 0;
    		int find_cnt = 0;
			while (find_cnt < MAX_A_CARD_OFF_CNT) {
				ret = mReader.RTN_TypeASearch();
				if (ret > 0) {
					// 找到A卡
					break;
				} else {
					find_cnt++;
				}
			}
    		return ret;
    	}
        
        /**
         * 读A卡序号
         * 特别说明：读A卡序号偶尔会失败，所以要增加重试机制，连续n次读A卡序号失败才认为读失败
         */
        private final int MAX_A_CARD_READ_CNT = 3;
    	int ReadCardASn(byte[] a_sn) {
			int read_a_cnt = 0;
			while (read_a_cnt < MAX_A_CARD_READ_CNT) {
				read_a_cnt = mReader.RTN_TypeAReadCID(a_sn);
				if (read_a_cnt > 0) {
					break;
				} else {
					// 读A卡失败
					read_a_cnt++;
					SystemClock.sleep(10);
				}
			}
    		return read_a_cnt;
    	}

        public ReadABCardThread(IReader reader){
            this.mReader = reader;
            this.mode = readCardMode;
            bDevRemoved = false;
        }
        
        public void setDevRemoved(){
            bDevRemoved = true;
        }
        
        @Override
        public void run() {
            super.run();
            int ret = 0;
            do{
	            if(isInterrupted()){
	            	break;
	            }
	            // read samId, when success, sam is ready
	            SAMIDInfo samIDInfo = new SAMIDInfo();
	            ret = mReader.SDT_GetSAMIDToStr(samIDInfo);
	            if(ret == 0){
	            	Log.d(TAG, "SDT_GetSAMIDToStr: SAMID is " + samIDInfo.SAMID);
	            	appendLog(AppendLogEvent.LOG_CODE_SAMID ,samIDInfo.SAMID);
	            	break;
	            }else{
	            	SystemClock.sleep(20);
	            }
            }while(true);
            if (0 == ret) {
                // start to read card
                READ_CARD_LOOP:
                do {
                	if(isInterrupted()){
    	            	break;
    	            }
                    if(bDevRemoved){
                        break;
                    }
                    
                    //无卡或A卡, 需要找A卡
                    if(mCardType != CARD_TYPE_B){
	                    //找A卡
	    				ret = Find_A_Card();
	    				if(ret > 0){
	    					//找到A卡
	    					//读A卡序号
	    					byte[] a_sn = new byte[32];
	    					ret = ReadCardASn(a_sn);
	    					if(ret > 0){    						
	    						//A卡在位
	    						if(mCardType != CARD_TYPE_A){
	    							mCardType = CARD_TYPE_A;
	    							byte[] card_sn = new byte[ret];
	    							System.arraycopy(a_sn, 0, card_sn, 0, ret); 
	    							
	    							ReportReadACardEvent reportReadACardEvent = new ReportReadACardEvent();
	    							reportReadACardEvent.setSuccess(true);
	    							reportReadACardEvent.setCard_sn(card_sn);
	    							EventBus.getDefault().post(reportReadACardEvent);
	    						}
	    					}else{
	    						if(mCardType==CARD_TYPE_A){
			    					ReportCardRemovedEvent reportCardRemovedEvent = new ReportCardRemovedEvent();
		                            EventBus.getDefault().post(reportCardRemovedEvent);
		    					}
	    						mCardType = CARD_TYPE_NO;
	    					}
	    				}else{
	    					if(mCardType==CARD_TYPE_A){
		    					ReportCardRemovedEvent reportCardRemovedEvent = new ReportCardRemovedEvent();
	                            EventBus.getDefault().post(reportCardRemovedEvent);
	    					}
	    					mCardType = CARD_TYPE_NO;
	    				}
    				}
                    
                    //无卡，需要找B卡
                    if(mCardType == CARD_TYPE_NO){            
    					//找B卡
	                    ret = mReader.RTN_Authenticate();
	                    appendLog(AppendLogEvent.LOG_CODE_ANY, "RTN_Authenticate return " + ret);
	                    if(ErrorCode.SUCCESS == ret){
	                        // find card success. notice UI clean idcardInfo
	                        if(iDCardInfo != null && iDCardInfo.photo != null){
	                        	iDCardInfo.photo.recycle();
	                        }
	                        iDCardInfo = new IDCardInfo();
	                        ret = mReader.RTN_ReadBaseMsg(iDCardInfo);
//                          ret = mReader.RTN_ReadBaseFPMsg(iDCardInfo);
	                        appendLog(AppendLogEvent.LOG_CODE_ANY, "RTN_ReadBaseMsg return " + ret);
	                        ReportReadIDCardEvent reportReadIDCardEvent = new ReportReadIDCardEvent();
	                        if (ErrorCode.SUCCESS == ret) { // read card successful
	                        	mCardType = CARD_TYPE_B;
	                            reportReadIDCardEvent.setiDCardInfo(iDCardInfo);
	                            reportReadIDCardEvent.setSuccess(true);
	                        }else{
	                        	mCardType = CARD_TYPE_NO;
	                        	reportReadIDCardEvent.setSuccess(false);
	                        }
	                        EventBus.getDefault().post(reportReadIDCardEvent);
	                    }else{
	                    	mCardType = CARD_TYPE_NO;
	                    }
    				}
    				
    				if(ReadIDCardMode.Single.equals(mode)){
                        break;
                    }else if(ReadIDCardMode.continual.equals(mode)){
                        // do nothing;
                    }else if(ReadIDCardMode.loop.equals(mode)){
                    	if(mCardType==CARD_TYPE_B){
	                        long startTime = System.currentTimeMillis();
	                        do{
	                        	if(isInterrupted()){
	            	            	break;
	            	            }
	                            if(bDevRemoved){
	                                break READ_CARD_LOOP;
	                            }
	                            ret = mReader.RTN_ReadNewAppMsg(new MoreAddrInfo());
	                            appendLog(AppendLogEvent.LOG_CODE_ANY, "RTN_ReadNewAppMsg return " + ret);
	                            if(ret < 0){// card removed
	                            	mCardType = CARD_TYPE_NO;
	                                ReportCardRemovedEvent reportCardRemovedEvent = new ReportCardRemovedEvent();
	                                EventBus.getDefault().post(reportCardRemovedEvent);
	                                break;
	                            }else{
	                            	//lihuili：有判断卡在位超过10秒的需要时可还原代码
	//                            	long currentTime = System.currentTimeMillis();
	//                            	if((currentTime - startTime) > 10000){
	//	                                // 有客户要求 5s、10s内不重复读卡，超过即便卡没离开也要重复读， 可按照需求决定是否需要这个判断。
	//                            		// 卡在位超过10秒
	//                            		appendLog("card on over 10s");
	//	                                break;
	//	                            }else{
		                                if(bDevRemoved){
		                                    break READ_CARD_LOOP;
		                                }
		                                SystemClock.sleep(100);
	//	                            }
	                            }
	                        }while(true);
                    	}
                    }
                    if(bDevRemoved){
                        break;
                    }
    				
                    // 读完卡需要间隔一段时间再找卡。
                    SystemClock.sleep(AB_ROLL_INTERVAL);
                } while (true);
            }
            appendLog(AppendLogEvent.LOG_CODE_ANY, "finish read card");
            reportExit();           
        }
    }
}
