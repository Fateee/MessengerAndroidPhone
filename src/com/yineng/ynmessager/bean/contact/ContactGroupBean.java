package com.yineng.ynmessager.bean.contact;

import java.util.ArrayList;
import java.util.List;

public class ContactGroupBean {
	//群/讨论组列表
	private List<ContactGroup> roomList = new ArrayList<ContactGroup>();
	//用户列表
	private List<ContactGroupUser> userList = new ArrayList<ContactGroupUser>();
	//类型
	private int groupType;

	public List<ContactGroup> getRoomList() {
		return roomList;
	}

	public void setRoomList(List<ContactGroup> roomList) {
		this.roomList = roomList;
	}

	public List<ContactGroupUser> getUserList() {
		return userList;
	}

	public void setUserList(List<ContactGroupUser> userList) {
		this.userList = userList;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}
	
}
