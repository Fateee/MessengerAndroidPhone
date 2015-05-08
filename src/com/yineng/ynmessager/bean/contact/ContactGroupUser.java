package com.yineng.ynmessager.bean.contact;

public class ContactGroupUser {
	private String groupName;//群组、讨论组ID
	private String jid;//一个用户实体
	private int role;//群成员类型：10-创建人 20-管理员 50-一般用户
	private String userNo;

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

}
