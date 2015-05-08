package com.yineng.ynmessager.bean;

import java.io.Serializable;

/**
 * 广播消息实体类
 * 
 * @author YINENG
 * 
 */
public class BroadcastChat implements Serializable{		
	private static final long serialVersionUID = 1L;
	private int id;// id
	private String packetId;// packetId;
	private String userNo;// 发送者ID
	private String userName;// 发送者昵称
	private String title;// 主题
	private String message;// 内容
	private String dateTime;// 接收时间
	private int messageType;// 消息类型 0:普通消息 1:图片 2：文件
	private int isSend;// 0:发送 1:接收
	private int isSendOk;// 0:发送失败;1成功
	private int isRead;// 0是未读,1是已读

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPacketId() {
		return packetId;
	}

	public void setPacketId(String packetId) {
		this.packetId = packetId;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
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

	public int getIsRead() {
		return isRead;
	}

	public void setIsRead(int isRead) {
		this.isRead = isRead;
	}

	public int getIsSendOk() {
		return isSendOk;
	}

	public void setIsSendOk(int isSendOk) {
		this.isSendOk = isSendOk;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof BroadcastChat) {
			BroadcastChat chat = (BroadcastChat) obj;
			if (chat.getPacketId() == null) {
				return false;
			}
			return (chat.getPacketId().equals(this.packetId));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.packetId.hashCode();
	}
}
