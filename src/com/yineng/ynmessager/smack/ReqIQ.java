package com.yineng.ynmessager.smack;

import org.jivesoftware.smack.packet.IQ;

public class ReqIQ extends IQ {

	private String paramsJson;

	private String nameSpace;

	private int action = -1;

	public void setParamsJson(String JsonString) {

		this.paramsJson = JsonString;
	}

	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<req xmlns=\"");
		buf.append(nameSpace);
		buf.append("\">");
		if (action > 0) {
			buf.append("<action>");
			buf.append(action);
			buf.append("</action>");
		}
		buf.append("<params>");
		if (paramsJson != null) {
			buf.append(paramsJson);
		} else {

			buf.append("{}");
		}
		buf.append("</params>");

		buf.append("</req>");

		return buf.toString();
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

}
