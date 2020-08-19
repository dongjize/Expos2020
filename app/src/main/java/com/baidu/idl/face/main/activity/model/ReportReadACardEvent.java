package com.baidu.idl.face.main.activity.model;

/**
 * 发送完成事件，使用eventbus发出，应用端注册eventbus监听事件。
 *
 */
public class ReportReadACardEvent {
	/**
	 * 读卡成功还是失败
	 */
	private boolean isSuccess = false;
	/**
	 * 读卡任务上报的读卡结果
	 */
	private byte[] card_sn;
	public boolean isSuccess() {
		return isSuccess;
	}
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
	public byte[] getCard_sn() {
		return card_sn;
	}
	public void setCard_sn(byte[] card_sn) {
		this.card_sn = card_sn;
	}	
}
