package com.yineng.ynmessager.smack;
/**
 * IQ请求回执
 * @author yineng
 *
 */
public interface ReceiveReqIQCallBack {
	public void  receivedReqIQResult(ReqIQResult packet);
}
