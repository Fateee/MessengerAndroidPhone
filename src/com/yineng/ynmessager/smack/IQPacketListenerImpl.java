
package com.yineng.ynmessager.smack;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.util.L;

/**
 * @author Yutang
 * Date 2014-12-29 所有IQ消息总的监听接口实例
 *
 */
public class IQPacketListenerImpl implements PacketListener {
	private XmppConnectionManager mXmppConnManager;

	public IQPacketListenerImpl() {
		mXmppConnManager = XmppConnectionManager.getInstance();
	}

	@Override
	public void processPacket(Packet arg0) {
		// TODO
		L.v("IQPacketListenerImpl",
				"IQPacketListenerImpl receive packet xml ->:"
						+ arg0.toXML());
		ReqIQResult response = (ReqIQResult) arg0;
		ReceiveReqIQCallBack callback;
		if ((callback = mXmppConnManager.getReceiveReqIQCallBack(response
				.getNameSpace())) != null) {//根据命名空间转发IQ消息
			callback.receivedReqIQResult(response);
			
		}else{
			//如果是message回执IQ 
			
			
		}
	}
}
