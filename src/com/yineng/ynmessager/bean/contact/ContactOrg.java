package com.yineng.ynmessager.bean.contact;

import java.util.LinkedList;
import java.util.List;

public class ContactOrg {
	private List<OrganizationTree> orgList = new LinkedList<OrganizationTree>();
	private List<UserTreeRelation> relList = new LinkedList<UserTreeRelation>();
	private List<UserStatus> statusList = new LinkedList<UserStatus>();
	private List<User> userList = new LinkedList<User>();
	private Long servertime;

	public List<OrganizationTree> getOrgList() {
		return orgList;
	}

	public void setOrgList(List<OrganizationTree> orgList) {
		this.orgList = orgList;
	}

	public List<UserTreeRelation> getRelList() {
		return relList;
	}

	public void setRelList(List<UserTreeRelation> relList) {
		this.relList = relList;
	}

	public List<UserStatus> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<UserStatus> statusList) {
		this.statusList = statusList;
	}

	public List<User> getUserList() {
		return userList;
	}

	public void setUserList(List<User> userList) {
		this.userList = userList;
	}

	public Long getServertime() {
		return servertime;
	}

	public void setServertime(Long servertime) {
		this.servertime = servertime;
	}

}
