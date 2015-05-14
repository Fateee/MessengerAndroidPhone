package com.yineng.ynmessager.bean.contact;

import java.util.LinkedList;
import java.util.List;


public class UserStatus {
//	{"statusList":[
//    {"resources":[{"resource":"Msg_PC","status":"busy"}],"userNo":"liuxin"},
//    {"resources":[{"resource":"Msg_Phone","status":"online"}],"userNo":"zouyanxia"},
//    {"resources":[{"resource":"Msg_PC","status":"busy"}],"userNo":"chenting"},
//    {"resources":[{"resource":"Msg_Phone","status":"online"}],"userNo":"lijian"},
//    {"resources":[{"resource":"Msg_PC","status":"online"}],"userNo":"xiangyujie"},
//    {"resources":[{"resource":"Msg_PC","status":"busy"}],"userNo":"sunlang"},
//    {"resources":[{"resource":"Msg_PC","status":"busy"}],"userNo":"gaopeng"}
//    ]}
	private String userNo;
	private List<Resource> resources = new LinkedList<Resource>();
	private int statusID;
	private List<UserStatus> statusList = new LinkedList<UserStatus>();
	public UserStatus() {
		super();
	}

	public UserStatus(String userNo, String status, int statusID) {
		super();
		this.userNo = userNo;
		this.statusID = statusID;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public int getStatusID() {
		return statusID;
	}

	public void setStatusID(int statusID) {
		this.statusID = statusID;
	}
	
	public List<Resource> getResources() {
		return resources;
	}

	public void setResources(List<Resource> resources) {
		this.resources = resources;
	}

	public List<UserStatus> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<UserStatus> statusList) {
		this.statusList = statusList;
	}
	
	public class Resource {
		private String resource;
		private String status;

		public String getResource() {
			return resource;
		}

		public void setResource(String resource) {
			this.resource = resource;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
		
	}
	
}
