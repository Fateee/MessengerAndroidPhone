package com.yineng.ynmessager.bean.contact;

import java.io.Serializable;


public class ContactGroup implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String groupName;	//群组、讨论组ID
	private String createUser;
	private String createTime;
	private String naturalName;	//群组、讨论组（默认）名称
	private String subject;//讨论组名称
	private int maxUsers;//最大成员数
	private String desc;
	private int groupType = 0;	//8:群组  9:讨论组
	private int notifyMode;  //该群的消息提醒方式
	public static final int NOTIFYMODE_YES = 1;  //接收并声音提醒
	public static final int NOTIFYMODE_NO = 0;  //接收消息但不声音提醒
	
	
	public ContactGroup() {
		super();
	}

	public ContactGroup(String groupName, String createUser, String createTime,
			String naturalName, String subject, int maxUsers, String desc,int notifyMode) {
		super();
		this.groupName = groupName;
		this.createUser = createUser;
		this.createTime = createTime;
		this.naturalName = naturalName;
		this.subject = subject;
		this.maxUsers = maxUsers;
		this.desc = desc;
		this.notifyMode = notifyMode;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getNaturalName() {
		return naturalName;
	}

	public void setNaturalName(String naturalName) {
		this.naturalName = naturalName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	public int getNotifyMode()
	{
		return notifyMode;
	}

	public void setNotifyMode(int notifyMode)
	{
		this.notifyMode = notifyMode;
	}
	
}
