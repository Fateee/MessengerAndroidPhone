package com.yineng.ynmessager.bean.contact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizationTree implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String orgNo;// 组织机构ID
	private String parentOrgNo;// 组织机构父ID
	private String orgName;// 组织机构名称
	private int orgType;// 1-组织机构 2-行政班 3-教学班
//	private int ordId;// 排序id，默认为0；组织机构按ordID降序排列
	private String ordId;// 排序id，默认为0；组织机构按ordID降序排列
	private int removeTag;// 删除标志，1：表示该组织机构无效，无需显示 0：有效
	private int childNum = 0;// 该组织机构包含的子组织机构和用户的总数

	// private Map<String, List<OrganizationTree>> childOrgTreeMap = new
	// HashMap<String, List<OrganizationTree>>();
	// private List<User> mOrgUsers = new ArrayList<User>();
	// public static OrganizationTree mOrganizationTree;
	//
	// public static OrganizationTree getInstance() {
	// if (mOrganizationTree == null) {
	// mOrganizationTree = new OrganizationTree();
	// }
	// return mOrganizationTree;
	// }

	public OrganizationTree() {
		super();
	}

	public OrganizationTree(String orgNo, String parentOrgNo, String orgName,
			int orgType, String ordId, int removeTag) {
		super();
		this.orgNo = orgNo;
		this.parentOrgNo = parentOrgNo;
		this.orgName = orgName;
		this.orgType = orgType;
		this.ordId = ordId;
		this.removeTag = removeTag;
	}

	public String getOrgNo() {
		return orgNo;
	}

	public void setOrgNo(String orgNo) {
		this.orgNo = orgNo;
	}

	public String getParentOrgNo() {
		return parentOrgNo;
	}

	public void setParentOrgNo(String parentOrgNo) {
		this.parentOrgNo = parentOrgNo;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public int getOrgType() {
		return orgType;
	}

	public void setOrgType(int orgType) {
		this.orgType = orgType;
	}

	public String getOrdId() {
		return ordId;
	}

	public void setOrdId(String ordId) {
		this.ordId = ordId;
	}

	public int getRemoveTag() {
		return removeTag;
	}

	public void setRemoveTag(int removeTag) {
		this.removeTag = removeTag;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public int getChildNum() {
		return childNum;
	}

	public void setChildNum(int childNum) {
		this.childNum = childNum;
	}

	// public Map<String, List<OrganizationTree>> getChildOrgTreeMap() {
	// return childOrgTreeMap;
	// }
	//
	// public void setChildOrgTreeMap(
	// Map<String, List<OrganizationTree>> childOrgTreeMap) {
	// this.childOrgTreeMap = childOrgTreeMap;
	// }
	//
	// public List<User> getmOrgUsers() {
	// return mOrgUsers;
	// }
	//
	// public void setmOrgUsers(List<User> mOrgUsers) {
	// this.mOrgUsers = mOrgUsers;
	// }

}
