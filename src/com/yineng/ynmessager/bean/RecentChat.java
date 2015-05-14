package com.yineng.ynmessager.bean;

import java.io.Serializable;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;

/**
 * 最近会话列表实体类
 * 
 * @author YINENG
 * 
 */
public class RecentChat implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String DRAFT_PREFIX = AppController.getInstance().getString(R.string.session_draftPrefix);
	
	private int Id;// Id
	private String userNo;//
	private String title;// 主题
	private String content;// 内容
	private int chatType;// 消息类型
	private String senderName;// 发送者名称
	private String senderNo;// 发送者ID
	private String dateTime;// 时间
	private String headUrl;//
	private String headLocalPath;// 本地路径
	private int unReadCount; // 未读条数
	private int isTop;// 是否置顶 1：置顶，0：不置顶
	private String draft;

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getChatType() {
		return chatType;
	}

	public void setChatType(int chatType) {
		this.chatType = chatType;
	}

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getSenderNo() {
		return senderNo;
	}

	public void setSenderNo(String senderNo) {
		this.senderNo = senderNo;
	}

	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getHeadUrl() {
		return headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

	public String getHeadLocalPath() {
		return headLocalPath;
	}

	public void setHeadLocalPath(String headLocalPath) {
		this.headLocalPath = headLocalPath;
	}

	public int getUnReadCount() {
		return unReadCount;
	}

	public void setUnReadCount(int unReadCount) {
		this.unReadCount = unReadCount;
	}

	public int getIsTop() {
		return isTop;
	}

	public void setIsTop(int isTop) {
		this.isTop = isTop;
	}

	public int getId() {
		return Id;
	}

	public void setId(int id) {
		Id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof RecentChat) {
			RecentChat chat = (RecentChat) obj;
			if (chat.getUserNo() == null) {
				return false;
			}
			return (chat.getUserNo().equals(this.userNo) && this.chatType == chat
					.getChatType());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.userNo.hashCode()
				+ Integer.valueOf(this.chatType).hashCode();
	}

	public String getDraft()
	{
		return draft;
	}

	public void setDraft(String draft)
	{
		this.draft = draft;
	}
}
