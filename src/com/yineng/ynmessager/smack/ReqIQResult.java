package com.yineng.ynmessager.smack;

import org.jivesoftware.smack.packet.IQ;

public class ReqIQResult extends IQ {
	private String nameSpace;

	private String resp;

	private int code = -1;

	private String id;

	private String action;
	
	/**
	 * 消息回执时间
	 */
	private String sendTime;

	/**
	 * 组织机构变更通知的类型
	 */
	private String typeStr;

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	@Override
	public String getChildElementXML() {
		// TODO Auto-generated method stub
		StringBuilder buf = new StringBuilder();
		buf.append("<req xmlns=\"");
		buf.append(nameSpace);
		buf.append("\">");
		if (resp != null && !resp.equals("")) {
			buf.append("<resp>");
			buf.append(resp);
			buf.append("</resp>");
		}
		if (code != -1) {
			buf.append("<code>");
			buf.append(code);
			buf.append("</code>");
		}
		if (action != null && !action.equals("")) {
			buf.append("<action>");
			buf.append(action);
			buf.append("</action>");
		}
		if (id != null && !id.equals("")) {
			buf.append("<id>");
			buf.append(id);
			buf.append("</id>");
		}
		//消息回执时间
		if (sendTime != null && !sendTime.equals("")) {
			buf.append("<sendTime>");
			buf.append(sendTime);
			buf.append("</sendTime>");
		}
		buf.append("</req>");

		return buf.toString();
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getResp() {
		return resp;
	}

	public void setResp(String resp) {
		this.resp = resp;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getTypeStr() {
		return typeStr;
	}

	public void setTypeStr(String typeStr) {
		this.typeStr = typeStr;
	}

}
