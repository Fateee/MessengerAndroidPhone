package com.yineng.ynmessager.bean.dissession;

import java.util.List;

public class DisSessionBean {
	/**
	 * groupType=GROUP 表示群
	 */
	public static final String GROUP = "1";
	/**
	 * groupType=DISSESSION 表示讨论组
	 */
	public static final String DISSESSION = "2";

	private String groupName;// 讨论组ID
	private String naturalName;// 讨论组名称
	private String subject;// 讨论组公告
	private String desc;// 讨论组名称
	private String groupType;// 讨论组类型
	private List<String> memberList;// 成员列表

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<String> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<String> memberList) {
		this.memberList = memberList;
	}

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}
}
