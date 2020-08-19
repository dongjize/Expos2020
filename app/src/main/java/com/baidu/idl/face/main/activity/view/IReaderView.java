package com.baidu.idl.face.main.activity.view;

import android.content.Context;

import com.routon.plsy.reader.sdk.common.Info.IDCardInfo;

public interface IReaderView {
    Context getContext();

    void onReadIDCardSuccess(IDCardInfo iDCardInfo);

    void onReadIDCardFailed();

    void onCardRemoved();

    void appendLog(int code, String log);

    void onGotStartReadcardResult(int error_code, boolean has_inner_reader);

    void onReadACardSuccess(byte[] card_sn);

    void onReaderStopped();
}
