//***************************************************************
//*    2015-4-20  下午2:08:03
//*    成都依能科技有限公司
//*    Copyright© 2015-2025 All Rights Reserved
//*    Author HuYi
//*    Des:
//***************************************************************
package com.yineng.ynmessager.bean;

import java.util.ArrayList;
import java.util.List;


/**
 * 离线消息
 * @author 胡毅
 *
 */
public class OfflineMessageList {
	private List<OfflineMsg> messageList = new ArrayList<OfflineMsg> ();
	private int total;
	
	public OfflineMessageList() {
		super();
	}


	public OfflineMessageList(List<OfflineMsg> messageList, int total) {
		super();
		this.messageList = messageList;
		this.total = total;
	}


	public List<OfflineMsg> getMessageList() {
		return messageList;
	}


	public void setMessageList(List<OfflineMsg> messageList) {
		this.messageList = messageList;
	}


	public int getTotal() {
		return total;
	}


	public void setTotal(int total) {
		this.total = total;
	}


	public class OfflineMsg {
		private String body;
		private String from;
		private String sendTime;
		private String type;
		
		public OfflineMsg() {
			super();
		}
		
		public OfflineMsg(String body, String from, String sendTime, String type) {
			super();
			this.body = body;
			this.from = from;
			this.sendTime = sendTime;
			this.type = type;
		}

		public String getBody() {
			return body;
		}
		
		public void setBody(String body) {
			this.body = body;
		}
		
		public String getFrom() {
			return from;
		}
		
		public void setFrom(String from) {
			this.from = from;
		}
		
		public String getSendTime() {
			return sendTime;
		}
		
		public void setSendTime(String sendTime) {
			this.sendTime = sendTime;
		}
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
	}
}
