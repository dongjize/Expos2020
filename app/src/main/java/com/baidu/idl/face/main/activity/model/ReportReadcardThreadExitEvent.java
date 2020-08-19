package com.baidu.idl.face.main.activity.model;


/**
 * 发送完成事件，使用eventbus发出，应用端注册eventbus监听事件。
 *
 */
public class ReportReadcardThreadExitEvent {
	private String log;

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}
}
