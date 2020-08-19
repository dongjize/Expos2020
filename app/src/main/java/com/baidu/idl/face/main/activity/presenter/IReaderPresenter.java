package com.baidu.idl.face.main.activity.presenter;


import com.baidu.idl.face.main.activity.model.DeviceParamBean;

public interface IReaderPresenter {
    int CMD_OPEN = 0;
    int CMD_FIND = 1;
    int CMD_SELECT = 2;
    int CMD_READBASEMSG = 3;
    int CMD_CLOSE = 4;

    void startReadcard(DeviceParamBean devParamBean);

    void stopReadcard();

    void release();

    void sendCmd(int cmd);
}

