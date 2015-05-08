package com.yineng.ynmessager.smack;

import org.jivesoftware.smack.packet.Presence;

public interface ReceivePresenceCallBack {
	public void receivedPresence(Presence packet);
}
