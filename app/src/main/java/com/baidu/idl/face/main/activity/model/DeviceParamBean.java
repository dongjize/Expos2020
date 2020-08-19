package com.baidu.idl.face.main.activity.model;

public class DeviceParamBean {
    public static int DEV_TYPE_INNER_OR_HID = 0;
    public static int DEV_TYPE_BT = 1;
    public static int DEV_TYPE_UNKNOWN = 10;
    private int device_type;
    private Object user_obj; //for BT: BluetoothDevice

    public int getDevice_type() {
        return device_type;
    }

    public void setDevice_type(int device_type) {
        this.device_type = device_type;
    }

    public Object getUser_obj() {
        return user_obj;
    }

    public void setUser_obj(Object user_obj) {
        this.user_obj = user_obj;
    }
}
