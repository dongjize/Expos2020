package com.baidu.idl.face.main.activity.model;

import com.routon.plsy.reader.sdk.common.Info.IDCardInfo;

/**
 * 发送完成事件，使用eventbus发出，应用端注册eventbus监听事件。
 */
public class ReportReadIDCardEvent {
    /**
     * 读卡成功还是失败
     */
    private boolean isSuccess = false;
    /**
     * 读卡任务上报的读卡结果@see IDCardInfo
     */
    private IDCardInfo iDCardInfo;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public IDCardInfo getiDCardInfo() {
        return iDCardInfo;
    }

    public void setiDCardInfo(IDCardInfo iDCardInfo) {
        this.iDCardInfo = iDCardInfo;
    }
}
