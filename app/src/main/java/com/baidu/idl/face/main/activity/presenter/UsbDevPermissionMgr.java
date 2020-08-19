package com.baidu.idl.face.main.activity.presenter;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

public final class UsbDevPermissionMgr{
	private final String TAG = "UsbDevPermissionMgr";
    private static final String ACTION_USB_PERMISSION = "com.routon.idr.USB_PERMISSION";
    private Context mContext;
    private UsbManager mUsbManager;
    private UsbReceiver mUsbReceiver;
    private final ArrayList<UsbDevTask> TASK = new ArrayList<UsbDevTask>(){
        private static final long serialVersionUID = 7732546546739239689L;
        {
            add(new UsbDevTask(0x425, 0x8159, "HIDIDR"));
            add(new UsbDevTask(0x400, 0xc35a, "USBIDR"));
        }
    };
    private UsbDevPermissionMgrCallback mCb;

    public interface UsbDevPermissionMgrCallback{
        public void onNoticeStr(String notice);
        public void onUsbDevReady(UsbDevice device);
        public void onUsbDevRemoved(UsbDevice device);
        public void onUsbRequestPermission();
    }

    public UsbDevPermissionMgr(Context context, UsbDevPermissionMgrCallback cb){
        mContext = context;
        mCb = cb;
        mUsbManager = (UsbManager)mContext.getSystemService(Context.USB_SERVICE);
    }
    
    public boolean initMgr(){
    	boolean has_outer_device = false;
    	mUsbReceiver = new UsbReceiver();
    	IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        Log.d(TAG, "initMgr registerReceiver mUsbReceiver " + mUsbReceiver);
        mContext.registerReceiver(mUsbReceiver, filter);

        for (UsbDevice device : mUsbManager.getDeviceList().values()) {
            UsbDevTask task = isUsbTaskDevice(device);
            if(task != null){
            	has_outer_device = true;
                if(mUsbManager.hasPermission(device)){
                    mCb.onUsbDevReady(device);
                }else{
                    usbDevPermissinRequest(device);
                }
                break;
            }
        }
        return has_outer_device;
    }

    public void releaseMgr(){
    	if(mUsbReceiver!=null){
    		Log.d(TAG, "releaseMgr unregisterReceiver mUsbReceiver " + mUsbReceiver);
    		mContext.unregisterReceiver(mUsbReceiver);
    		mUsbReceiver = null;
    	}
    }
    
    private void usbDevPermissinRequest(UsbDevice device){
        if(mCb!=null){
        	mCb.onUsbRequestPermission();
        }
        Intent permissionIntent = new Intent(ACTION_USB_PERMISSION);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(mContext, 0, permissionIntent, 0);
        mUsbManager.requestPermission(device, mPermissionIntent);

    }
    
    private void appendLog(String s){
    	if(mCb!=null){
    		mCb.onNoticeStr(s);
    	}
    }
    
    private class UsbReceiver extends BroadcastReceiver
    {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            UsbDevTask task = isUsbTaskDevice(device);
            String action = intent.getAction();
//            Log.d(TAG, "mUsbReceiver action is " + action);
            if(task != null){
                if (ACTION_USB_PERMISSION.equals(action)) {
                    boolean succ = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    if(succ){
                        mCb.onUsbDevReady(device);
                    }
                }else if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)){
                    if(mUsbManager.hasPermission(device)){
                        mCb.onUsbDevReady(device);
                    }else{
                        usbDevPermissinRequest(device);
                    }
                }else if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)){
                    mCb.onUsbDevRemoved(device);
                }
            }
            appendLog(action + "  " + task);
		}
    	
    }

    public UsbDevTask isUsbTaskDevice(UsbDevice dev){
        UsbDevTask task = null;
        final int vid =dev.getVendorId();
        final int pid = dev.getProductId();
        for(UsbDevTask t:TASK){
            if(( vid == t.vid)&&(pid == t.pid)){
                task = t;
                break;
            }
        }
        return task;
    }

    private class UsbDevTask{
        public int vid, pid;
        public String name;

        public UsbDevTask(int vid, int pid, String name){
            this.vid = vid;
            this.pid = pid;
            this.name = name;
        }

        @Override
        public String toString() {
            return String.format("<%04x,%04x> %s", vid,pid,name);
        }
    }
}
