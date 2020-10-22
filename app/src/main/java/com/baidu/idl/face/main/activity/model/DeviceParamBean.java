package com.baidu.idl.face.main.activity.model;

public class DeviceParamBean {
    public static int DEV_TYPE_INNER_OR_HID = 0;
    public static int DEV_TYPE_BT = 1;
    public static int DEV_TYPE_UNKNOWN = 10;
    private int deviceType;
    private Object userObj; //for BT: BluetoothDevice

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int device_type) {
        this.deviceType = device_type;
    }

    public Object getUserObj() {
        return userObj;
    }

    public void setUserObj(Object userObj) {
        this.userObj = userObj;
    }
}
