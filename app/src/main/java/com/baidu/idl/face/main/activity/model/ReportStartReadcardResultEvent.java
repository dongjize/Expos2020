package com.baidu.idl.face.main.activity.model;

/**
 * 启动读卡成功
 * 发送完成事件，使用eventbus发出，应用端注册eventbus监听事件。
 */
public class ReportStartReadcardResultEvent {
    private int error_code;
    private boolean have_inner_reader;

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public boolean isHave_inner_reader() {
        return have_inner_reader;
    }

    public void setHave_inner_reader(boolean have_inner_reader) {
        this.have_inner_reader = have_inner_reader;
    }
}
