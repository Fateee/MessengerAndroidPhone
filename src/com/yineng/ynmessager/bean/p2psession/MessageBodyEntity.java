package com.yineng.ynmessager.bean.p2psession;

import java.util.LinkedList;
import java.util.List;

public class MessageBodyEntity {
	private String Content;
	private int MsgType;
	private String SendName;
	private List<MessageImageEntity> Images = new LinkedList<MessageImageEntity>();
	private List<MessageCustomAvatarsEntity> CustomAvatars = new LinkedList<MessageCustomAvatarsEntity>();
	private List<MessageFileEntity> Files = new LinkedList<MessageFileEntity>();
	private MessageStyleEntity Style;
	public String getContent() {
		return Content;
	}
	public void setContent(String content) {
		Content = content;
	}
	public int getMsgType() {
		return MsgType;
	}
	public void setMsgType(int msgType) {
		MsgType = msgType;
	}
	public List<MessageImageEntity> getImages() {
		return Images;
	}
	public void setImages(List<MessageImageEntity> images) {
		Images = images;
	}
	public List<MessageCustomAvatarsEntity> getCustomAvatars() {
		return CustomAvatars;
	}
	public void setCustomAvatars(List<MessageCustomAvatarsEntity> customAvatars) {
		CustomAvatars = customAvatars;
	}
	public List<MessageFileEntity> getFiles() {
		return Files;
	}
	public void setFiles(List<MessageFileEntity> files) {
		Files = files;
	}
	public MessageStyleEntity getStyle() {
		return Style;
	}
	public void setStyle(MessageStyleEntity style) {
		Style = style;
	}
	public String getSendName() {
		return SendName;
	}
	public void setSendName(String sendName) {
		SendName = sendName;
	}
}
