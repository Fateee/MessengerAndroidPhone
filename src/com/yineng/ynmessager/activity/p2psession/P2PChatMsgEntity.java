package com.yineng.ynmessager.activity.p2psession;

import android.text.SpannableString;

public class P2PChatMsgEntity {
	// 已读和未读标识
	public static final int IS_READED = 1;
	public static final int IS_NOT_READED = 0;

	// 发送成功或失败的标识
	public static final int SEND_SUCCESS = 0;
	public static final int SEND_FAILED = 1;
	public static final int SEND_ING = 2;

	// 消息发送类型
	public static final int COM_MSG = 1;
	public static final int TO_MSG = 0;

	// 0:普通消息 1:图片 2：文件
	public static final int MESSAGE = 0;
	public static final int IMAGE = 1;
	public static final int FILE = 2;

	// 0:是发送 1:不是发送（即接收）
	public static final int SEND = 0;
	public static final int RECEIVE = 0;

	private String packetId;// 数据包id
	private String chatUserNo;// 对方的聊天帐号
	private int messageType;// 发送消息类型 0:普通消息 1:图片 2：文件
	private String message;
	private String mTime;// 发送时间
	private int isSend;// 0:是发送 1:不是发送（即接收）
	private int isSuccess;// 0发送成功,1发送失败，2发送中
	private int isReaded;// 1:已读/0:未读
	private boolean isShowTime;// UI显示时，指定是否显示时间
	private String fileId;// 文件id
	/**
	 * 用于界面显示的文本
	 */
	private SpannableString  spannableString;
	
	public String getPacketId() {
		return packetId;
	}

	public void setPacketId(String packetId) {
		this.packetId = packetId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getmTime() {
		return mTime;
	}

	public void setmTime(String mTime) {
		this.mTime = mTime;
	}

	public int getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(int isSuccess) {
		this.isSuccess = isSuccess;
	}

	public String getChatUserNo() {
		return chatUserNo;
	}

	public void setChatUserNo(String chatUserNo) {
		this.chatUserNo = chatUserNo;
	}

	public int getIsReaded() {
		return isReaded;
	}

	public void setIsReaded(int isReaded) {
		this.isReaded = isReaded;
	}

	public boolean isShowTime() {
		return isShowTime;
	}

	public void setShowTime(boolean isShowTime) {
		this.isShowTime = isShowTime;
	}

	public int getMessageType() {
		return messageType;
	}

	public void setMessageType(int messageType) {
		this.messageType = messageType;
	}

	public int getIsSend() {
		return isSend;
	}

	public void setIsSend(int isSend) {
		this.isSend = isSend;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public SpannableString getSpannableString() {
		return spannableString;
	}

	public void setSpannableString(SpannableString spannableString) {
		this.spannableString = spannableString;
	}

}
