package com.yineng.ynmessager.bean.contact;

/**
 * 
 * 联系界面顶层分类（组织机构、群、讨论组），此类方便刷新UI
 * 
 * @author YINENG
 *
 */
public class ContactCommonBean {
	private String name;
	private String num;// 组织ID

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

}
