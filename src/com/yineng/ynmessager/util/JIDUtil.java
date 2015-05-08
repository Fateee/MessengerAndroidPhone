package com.yineng.ynmessager.util;

import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.manager.XmppConnectionManager;

public class JIDUtil {
	public static String getJIDByAccount(String account) {
		String str = account + "@"
				+ XmppConnectionManager.getInstance().getServiceName() + "/"
				+ Const.RESOURSE_NAME;
		return str;
	}

	/**
	 * 发送msg消息时，msg中的setTo方法的账号，不能带资源名
	 * @param account
	 * @return
	 */
	public static String getSendToMsgAccount(String account) {
		String str = account + "@"
				+ XmppConnectionManager.getInstance().getServiceName();
		return str;
	}
	
	public static String getAccountByJID(String jid) {
		String str = jid.split("@")[0];
		return str;
	}

	public static String getResouceNameByJID(String jid) {
		String str = jid.split("/")[1];
		return str;
	}
	
	public static String getGroupJIDByAccount(String toAccount) {
		String str = toAccount + "@conference."
				+ XmppConnectionManager.getInstance().getServiceName();
		return str;
	}
}
