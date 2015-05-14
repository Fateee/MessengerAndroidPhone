package com.yineng.ynmessager.smack;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.ContactGroupBean;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver.groupCreatedListener;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;

/**
 * Presence消息接收分发
 * @author YINENG
 *
 */
public class PresencePacketListenerImpl implements PacketListener {
	private XmppConnectionManager mXmppConnManager;
	private ContactOrgDao mContactOrgDao;
	private RecentChatDao mRecentChatDao;
	private DisGroupChatDao mDisGroupChatDao;
	private Context mContext;
	private String mPacketId;
	private LinkedList<ContactGroupUser> mGroupMemberAccountQueue = new LinkedList<ContactGroupUser>();
	private LinkedList<ContactGroupUser> mDisGroupMemberAccountQueue= new LinkedList<ContactGroupUser>();
	private groupCreatedListener mGroupCreatedListener;
	
	public PresencePacketListenerImpl(Context mContext) {
		mXmppConnManager = XmppConnectionManager.getInstance();
		mContactOrgDao = new ContactOrgDao(mContext);
		mRecentChatDao = new RecentChatDao(mContext);
		mDisGroupChatDao = new DisGroupChatDao(mContext);
		this.mContext = mContext;
	}

	/* (non-Javadoc)
	 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
	 */
	@Override
	public void processPacket(Packet arg0) {
		
		Presence p = (Presence) arg0;
		L.d("Presence", "receive Presence -- PacketID: " + p.getPacketID()
				+ ",from:" + p.getFrom() + ", to: " + p.getTo() + " ,status:"
				+ p.getStatus() +" , Type: "+p.getType()+" ,xNameSpace: "+p.getxNameSpace()+ "\n receive Presence xml:" + p.toXML());
		String xNameSpace = p.getxNameSpace();
		if (xNameSpace == null) {
			if (p.getType() == Presence.Type.available) {
				String mUserNo = JIDUtil.getAccountByJID(p.getFrom());
				mContactOrgDao.updateOneUserStatusByAble(mUserNo,Const.USER_ON_LINE);
			} else if (p.getType() == Presence.Type.unavailable) {
				String mUserNo = JIDUtil.getAccountByJID(p.getFrom());
				mContactOrgDao.updateOneUserStatusByAble(mUserNo,Const.USER_OFF_LINE);
			}
		} else {//群、讨论组的创建、删除
			//群、讨论组id
			String mGroupName = JIDUtil.getAccountByJID(p.getFrom().trim());
			//群、讨论组中成员id
			String mFromGroupMemberAccount = JIDUtil.getResouceNameByJID(p.getFrom().trim());
			int mGroupType;
			// 讨论组
			if (mGroupName.startsWith("dis")) {
				mGroupType = Const.CONTACT_DISGROUP_TYPE;
			} else { //群组
				mGroupType = Const.CONTACT_GROUP_TYPE;
			}
			
			/**添加成员到讨论组、群组；创建讨论组**/
			if (p.getType() == Presence.Type.available) {
				int ret = mContactOrgDao.isGroupUserRelationExist(mGroupName, mFromGroupMemberAccount, mGroupType);
				if (ret == 0 || ret == 2) {//关系不存在数据库
//					sendRequestIQPacket(mGroupName);
					mGroupCreatedListener.groupCreated(mGroupName);
				}
			}
			/**退出讨论组**/
			else if (p.getType() == Presence.Type.unavailable) {
				quitGroupByGroupType(mGroupName,mFromGroupMemberAccount,mGroupType);
			}
//			if (mAddGroupMemberThread == null) {
//				mAddGroupMemberThread = new AddGroupMemberThread();
//				mAddGroupMemberThread.start();
//			} 
			
//			//组员对象
//			ContactGroupUser tempGroupUser = new ContactGroupUser();
//			tempGroupUser.setGroupName(mGroupName);
//			tempGroupUser.setUserNo(mFromGroupMemberAccount);
//			tempGroupUser.setJid(mFromGroupMemberAccount);
//			tempGroupUser.setRole(50);
//			
//			// 讨论组
//			if (mGroupName.startsWith("dis")) {
//				mGroupExisted = mContactOrgDao.isContactGroupExist(mGroupName, Const.CONTACT_DISGROUP_TYPE);
//				if (mGroupExisted == 2) {//不存在
//					sendRequestIQPacket(mGroupName);
//				}
//				mDisGroupMemberAccountQueue.addLast(tempGroupUser);
//			} else { //群组
//				mGroupMemberAccountQueue.addLast(tempGroupUser);
//			}
//
//			notify();

		}
	
		// 往监听接口转发Presence
		if (mXmppConnManager != null) {
			mXmppConnManager.dispatchPresence(p);
		}
	} 
	
	/**
	 * 退出群组、讨论组
	 * @param mGroupType 组类型 	8：群组 9：讨论组
	 * @param mFromGroupMemberAccount 来自组里的成员账号
	 * @param mGroupName 组ID
	 */
	private void quitGroupByGroupType(String mGroupName, String mFromGroupMemberAccount, int mGroupType) {
		Intent updateViewIntent = new Intent();
		//我自己退出
		if (mFromGroupMemberAccount.equals(AppController.getInstance().mSelfUser.getUserNo())) {
			//删除讨论组会话里面的聊天记录
			mDisGroupChatDao.deleteRecentChatByGroupId(mGroupName);
			//删除消息列表的聊天记录
			mRecentChatDao.deleteRecentChatByNumber(mGroupName);
			//删除数据库中群组、讨论组信息和群组、讨论组与用户关系的信息
			mContactOrgDao.quitContactGroup(mGroupName,mFromGroupMemberAccount,mGroupType);
			updateViewIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, mGroupType);
			updateViewIntent.setAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
			mContext.sendBroadcast(updateViewIntent);
		} 
		//别人退出
		else {
			//删除数据库中讨论组与用户关系的信息
			mContactOrgDao.deleteOneGroupUserRelation(mGroupName,mFromGroupMemberAccount,mGroupType);
			updateViewIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, mGroupType);
			updateViewIntent.setAction(Const.BROADCAST_ACTION_QUIT_GROUP);
			mContext.sendBroadcast(updateViewIntent);
		}
		if (mGroupType == Const.CONTACT_GROUP_TYPE) {
			NoticesManager.getInstance(mContext).updateRecentChatList(mGroupName,Const.CHAT_TYPE_GROUP);// 更新最近会话列表
		} else {
			NoticesManager.getInstance(mContext).updateRecentChatList(mGroupName,Const.CHAT_TYPE_DIS);// 更新最近会话列表
		}
	}
	
//	/**
//	 * 发送IQ请求
//	 * @param mGroupName 
//	 * 
//	 * @param action
//	 *            接口方向
//	 * @param nameSpace
//	 *            命名空间
//	 */
//	private void sendRequestIQPacket(String mGroupName) {
//		ReqIQ iq = new ReqIQ();
//		iq.setAction(13);
//		iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_GROUP);
//		String jsonParam = getParamsJson(mGroupName);
//		iq.setParamsJson(jsonParam);
//		LastLoginUserSP lastUser = LastLoginUserSP.getInstance(mContext);
//		iq.setFrom(lastUser.getUserAccount()+ "@" + mXmppConnManager.getServiceName());
//		iq.setTo("admin@" + mXmppConnManager.getServiceName());
//		L.i("PresencePacketListenerImpl", "iq request xml ->" + iq.toXML());
//		mPacketId = iq.getPacketID();
//		try {
//			mXmppConnManager.sendPacket(iq);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * 拼接字符串 
//	 * @return  eg： {"groupName":"test"}
//	 */
//	private String getParamsJson(String mGroupName) {
//		StringBuilder mBuilder = new StringBuilder();
//		mBuilder.append("{\"groupName\":\"");
//		mBuilder.append(mGroupName);
//		mBuilder.append("\"}");
//		return mBuilder.toString();
//	}

//	/* (non-Javadoc)
//	 * @see com.yineng.ynmessager.smack.ReceiveReqIQCallBack#receivedReqIQResult(com.yineng.ynmessager.smack.ReqIQResult)
//	 */
//	@Override
//	public void receivedReqIQResult(ReqIQResult packet) {
//		L.e("1111111111111111");
//		if (packet.getPacketID().equals(mPacketId)) {
//			L.e("????????????");
//			if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
//				L.e("2222222");
//				ContactGroupBean mContactGroupBean = JSON.parseObject(
//						packet.getResp(), ContactGroupBean.class);
//				if (mContactGroupBean.getGroupType() == 1) {
////					mGroupExisted = 1;
//				} else {
//					for (ContactGroup contactGroup : mContactGroupBean.getRoomList()) {
//						mContactOrgDao.insertOneContactGroupData(contactGroup, Const.CONTACT_DISGROUP_TYPE);
//					}
//					for (ContactGroupUser mContactGroupUser : mContactGroupBean.getUserList()) {
//						mContactOrgDao.insertOneGroupUserRelationData(mContactGroupUser, Const.CONTACT_DISGROUP_TYPE);
//					}
//					//发送创建讨论组成功的广播
//					Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_CREATE_GROUP);
//					mContext.sendBroadcast(updateViewIntent);
//				}
//
//			}
//		}
//	}
	
	//	/**
//	 * 
//	 * 添加群组、讨论组成员的处理线程
//	 * 
//	 * @author 胡毅
//	 * 
//	 */
//	class AddGroupMemberThread extends Thread {
//		@Override
//		public void run() {
//			super.run();
//			while (mGroupExisted == 3) {
//				ContactGroupUser tempGroupUser = mDisGroupMemberAccountQueue.removeFirst();
//				//插入一条讨论组成员信息
//				mContactOrgDao.insertOneGroupUserRelationData(tempGroupUser,Const.CONTACT_DISGROUP_TYPE);
//				if (mDisGroupMemberAccountQueue.size() <= 0) {
//					try {
//						mDisGroupMemberAccountQueue.clear();
//						//发送创建讨论组成功的广播
//						Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_CREATE_GROUP);
//						mContext.sendBroadcast(updateViewIntent);
//						wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//	}
	public interface groupCreatedListener{
		public void groupCreated(String mGroupName);
	}
	public void setGroupCreatedListener(groupCreatedListener mGroupCreatedListener) {
		this.mGroupCreatedListener = mGroupCreatedListener;
	}
}
