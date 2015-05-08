package com.yineng.ynmessager.smack;

import com.yineng.ynmessager.activity.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;

public interface ReceiveMessageCallBack {
	public void  receivedMessage(P2PChatMsgEntity msg);
	public void receivedMessage(GroupChatMsgEntity msg);
}
