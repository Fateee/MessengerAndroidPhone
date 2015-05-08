package com.yineng.ynmessager.bean.contact;



public class UserTreeRelation {
	private int id;//ID主键自增
	private String userNo;//用户ID
	private String orgNo;//组织机构ID
//	private int ordId;//排序ID
	private String ordId;//排序ID
	private String relationType;
	private int removeTag;//删除标志 0-有效  1-无效
	
	public UserTreeRelation() {
		super();
	}

	public UserTreeRelation(int id, String userNo, String orgNo, String ordId,
			String relationType, int removeTag) {
		super();
		this.id = id;
		this.userNo = userNo;
		this.orgNo = orgNo;
		this.ordId = ordId;
		this.relationType = relationType;
		this.removeTag = removeTag;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getOrgNo() {
		return orgNo;
	}

	public void setOrgNo(String orgNo) {
		this.orgNo = orgNo;
	}

	public String getOrdId() {
		return ordId;
	}

	public void setOrdId(String ordId) {
		this.ordId = ordId;
	}

	public String getRelationType() {
		return relationType;
	}

	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	public int getRemoveTag() {
		return removeTag;
	}

	public void setRemoveTag(int removeTag) {
		this.removeTag = removeTag;
	}
	
}
