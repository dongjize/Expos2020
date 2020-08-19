package com.baidu.idl.face.main.activity.model;


/**
 * 发送完成事件，使用eventbus发出，应用端注册eventbus监听事件。
 *
 */
public class AppendLogEvent {
	private int code = LOG_CODE_ANY;
	private String log;
	
	public static int LOG_CODE_ANY = 0;
	public static int LOG_CODE_SAMID = 1;
	public static int LOG_CODE_REQ_USB_PERMISSION = 2;
	
	
	public int getCode() {
		return code;
	}
	
	public void setCode(int code) {
		this.code = code;
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

}
