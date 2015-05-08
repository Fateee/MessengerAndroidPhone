package com.yineng.ynmessager.bean;

public class ClientInitConfig {
	private String disgroup_max_user; // 讨论组的最大人数
	private String group_max_user; // 群的最大人数
	private String max_disdisgroup_can_create; // 可创建的最大讨论组个数
	private String max_group_can_create; // 可创建的最大群个数
	private String org_update_type; // 组织机构数据更新方式，如果为0则表示全量更新，1则表示增量更新
	private Object rightList;
	private String servertime; //获取到组织机构后得到的服务器时间
	
	public ClientInitConfig(String disgroup_max_user, String group_max_user,
			String max_disdisgroup_can_create, String max_group_can_create,
			String org_update_type) {
		super();
		this.disgroup_max_user = disgroup_max_user;
		this.group_max_user = group_max_user;
		this.max_disdisgroup_can_create = max_disdisgroup_can_create;
		this.max_group_can_create = max_group_can_create;
		this.org_update_type = org_update_type;
	}

	public String getDisgroup_max_user() {
		return disgroup_max_user;
	}

	public void setDisgroup_max_user(String disgroup_max_user) {
		this.disgroup_max_user = disgroup_max_user;
	}

	public String getGroup_max_user() {
		return group_max_user;
	}

	public void setGroup_max_user(String group_max_user) {
		this.group_max_user = group_max_user;
	}

	public String getMax_disdisgroup_can_create() {
		return max_disdisgroup_can_create;
	}

	public void setMax_disdisgroup_can_create(String max_disdisgroup_can_create) {
		this.max_disdisgroup_can_create = max_disdisgroup_can_create;
	}

	public String getMax_group_can_create() {
		return max_group_can_create;
	}

	public void setMax_group_can_create(String max_group_can_create) {
		this.max_group_can_create = max_group_can_create;
	}

	public String getOrg_update_type() {
		return org_update_type;
	}

	public void setOrg_update_type(String org_update_type) {
		this.org_update_type = org_update_type;
	}

	public Object getRightList() {
		return rightList;
	}

	public void setRightList(Object rightList) {
		this.rightList = rightList;
	}

	public String getServertime() {
		return servertime;
	}

	public void setServertime(String servertime) {
		this.servertime = servertime;
	}

	
}
