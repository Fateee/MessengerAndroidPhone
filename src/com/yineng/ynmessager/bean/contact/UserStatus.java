package com.yineng.ynmessager.bean.contact;


public class UserStatus {
	private String userNo;
	private String status;
	private int statusID;
	
	public UserStatus() {
		super();
	}

	public UserStatus(String userNo, String status, int statusID) {
		super();
		this.userNo = userNo;
		this.status = status;
		this.statusID = statusID;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getStatusID() {
		return statusID;
	}

	public void setStatusID(int statusID) {
		this.statusID = statusID;
	}
	
}
