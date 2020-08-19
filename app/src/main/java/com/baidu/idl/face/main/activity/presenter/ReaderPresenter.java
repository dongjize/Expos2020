package com.baidu.idl.face.main.activity.presenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.util.Log;

import com.baidu.idl.face.main.activity.model.AppendLogEvent;
import com.baidu.idl.face.main.activity.model.DeviceParamBean;
import com.baidu.idl.face.main.activity.model.ReportCardRemovedEvent;
import com.baidu.idl.face.main.activity.model.ReportReadACardEvent;
import com.baidu.idl.face.main.activity.model.ReportReadIDCardEvent;
import com.baidu.idl.face.main.activity.model.ReportReadcardThreadExitEvent;
import com.baidu.idl.face.main.activity.model.ReportStartReadcardResultEvent;
import com.baidu.idl.face.main.activity.view.IReaderView;


public class ReaderPresenter implements IReaderPresenter {
    private final String TAG = "ReaderPresenter";
    private IReaderView mReaderView;
    private ReaderTask mReaderTask;

    public ReaderPresenter(IReaderView readerView) {
        mReaderView = readerView;
        EventBus.getDefault().register(this);//注册eventbus监听
        mReaderTask = ReaderTask.create(mReaderView.getContext(), ReaderTask.ReadIDCardMode.loop);
        mReaderTask.init();
    }

    /**
     * 监听读身份证结果
     * 示例程序中因为只需要在界面上提示当前连接状态，需要修改UI，因此，设置threadMode = ThreadMode.MAIN，在主线程中。
     * 具体运行在哪个线程，请结合自己的业务决定。
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReportReadIDCardEvent event) {
        Log.d(TAG, "onEvent ReportReadIDCardEvent");
        if (event != null) {
            if (event.isSuccess() && event.getiDCardInfo() != null) {
                mReaderView.onReadIDCardSuccess(event.getiDCardInfo());
            } else {
                mReaderView.onReadIDCardFailed();
            }
        }
    }

    /**
     * 监听卡片离位结果
     * 示例程序中因为只需要在界面上提示当前连接状态，需要修改UI，因此，设置threadMode = ThreadMode.MAIN，在主线程中。
     * 具体运行在哪个线程，请结合自己的业务决定。
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReportCardRemovedEvent event) {
        Log.d(TAG, "onEvent ReportCardRemovedEvent");
        if (event != null) {
            mReaderView.onCardRemoved();
        }
    }

    /**
     * 监听卡片离位结果
     * 示例程序中因为只需要在界面上提示当前连接状态，需要修改UI，因此，设置threadMode = ThreadMode.MAIN，在主线程中。
     * 具体运行在哪个线程，请结合自己的业务决定。
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AppendLogEvent event) {
//		Log.d(TAG, "onEvent AppendLogEvent");
        if ((event != null) && (event.getLog() != null)) {
            mReaderView.appendLog(event.getCode(), event.getLog());
        }
    }

    /**
     * 监听读卡线程退出事件
     * 示例程序中因为只需要在界面上提示当前连接状态，需要修改UI，因此，设置threadMode = ThreadMode.MAIN，在主线程中。
     * 具体运行在哪个线程，请结合自己的业务决定。
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReportReadcardThreadExitEvent event) {
        Log.d(TAG, "onEvent ReportReadcardThreadExitEvent");
        if (event != null) {
            mReaderTask.onReaderCardThreadQuited();
            mReaderView.onReaderStopped();
        }
    }

    /**
     * 监听启动读卡结果
     * 示例程序中因为只需要在界面上提示当前连接状态，需要修改UI，因此，设置threadMode = ThreadMode.MAIN，在主线程中。
     * 具体运行在哪个线程，请结合自己的业务决定。
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReportStartReadcardResultEvent event) {
        Log.d(TAG, "onEvent ReportStartReadcardResultEvent");
        if (event != null) {
            mReaderView.onGotStartReadcardResult(event.getError_code(), event.isHave_inner_reader());
        }
    }

    /**
     * 监听读A卡结果
     * 示例程序中因为只需要在界面上提示当前连接状态，需要修改UI，因此，设置threadMode = ThreadMode.MAIN，在主线程中。
     * 具体运行在哪个线程，请结合自己的业务决定。
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ReportReadACardEvent event) {
        Log.d(TAG, "onEvent ReportReadACardEvent");
        if (event != null) {
            if (event.isSuccess() && event.getCard_sn() != null) {
                mReaderView.onReadACardSuccess(event.getCard_sn());
            }
        }
    }

    @Override
    public void release() {
        // TODO Auto-generated method stub
        if (mReaderTask != null) {
            mReaderTask.release();
        }

        EventBus.getDefault().unregister(this);//取消注册eventbus监听
    }

    @Override
    public void startReadcard(DeviceParamBean devParamBean) {
        // TODO Auto-generated method stub
        mReaderTask.startReadcard(devParamBean);
    }

    @Override
    public void stopReadcard() {
        // TODO Auto-generated method stub
        mReaderTask.stopReadcard();
    }

    @Override
    public void sendCmd(int cmd) {
        // TODO Auto-generated method stub
        mReaderTask.sendCmd(cmd);
    }

}
