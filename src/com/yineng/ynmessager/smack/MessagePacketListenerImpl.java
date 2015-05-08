package com.yineng.ynmessager.smack;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.activity.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.BroadcastChat;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.BroadcastChatDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.GZIPUtil;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;

/**
 * Message消息总的接收处理类
 * 
 * @author YINENG
 * 
 */
public class MessagePacketListenerImpl implements PacketListener
{
	private Context mContext;
	private XmppConnectionManager mXmppConnManager;
	private NoticesManager mNoticesManager;
	private P2PChatMsgDao mP2pChartMsgDao;
	private GroupChatDao mGroupChatDao;
	private RecentChatDao mRecentChatDao;
	private BroadcastChatDao mBroadcastChatDao;
	private ReceiveMessageCallBack mCallback;
	private String tag = MessagePacketListenerImpl.class.getSimpleName();
	private ContactOrgDao mContactOrgDao;
	private DisGroupChatDao mDisGroupChatDao;

	public MessagePacketListenerImpl(Context context)
	{
		mXmppConnManager = XmppConnectionManager.getInstance();
		this.mContext = context;
		mNoticesManager = NoticesManager.getInstance(context);
		mP2pChartMsgDao = new P2PChatMsgDao(mContext);
		mGroupChatDao = new GroupChatDao(mContext);
		mDisGroupChatDao = new DisGroupChatDao(mContext);
		mRecentChatDao = new RecentChatDao(mContext);
		mBroadcastChatDao = new BroadcastChatDao(mContext);
		mContactOrgDao = new ContactOrgDao(mContext);
	}

	/**
	 * 根据消息生成广播
	 * 
	 * @param message
	 * @param senderNo
	 * @param senderName
	 * @return
	 */
	private BroadcastChat createBroadcastChatByMessage(Message message, String senderNo, String senderName)
	{
		BroadcastChat broadcast = new BroadcastChat();
		broadcast.setPacketId(message.getPacketID());
		broadcast.setTitle(message.getSubject());
		broadcast.setMessage(message.getBody());
		broadcast.setDateTime(message.getSendTime());
		broadcast.setUserNo(senderNo);
		broadcast.setUserName(senderName);
		broadcast.setIsSend(0);
		return broadcast;
	}

	/**
	 * 将文本中的HTML转义字符替换成（可能有bug）
	 * 
	 * @param htmlbody
	 * @return
	 */
	public static String formatHtmlBodyToJson(String htmlbody)
	{
		if(htmlbody == null)
		{
			return "";
		}
		String s = "";
		s = htmlbody.replace("&amp;","&");
		s = s.replace("&lt;","<");
		s = s.replace("&gt;",">");
		s = s.replace("&nbsp;"," ");
		s = s.replace("&#39;","\'");
		s = s.replace("&quot;","\"");
		return s;
	}

	/**
	 * 根据body生成最近会话内容
	 * 
	 * @param body解压缩后的json字符串
	 * @return
	 */
	private MessageBodyEntity getContentByBody(String body)
	{
		String content = null;
		MessageBodyEntity bodyEntity = JSON.parseObject(body,MessageBodyEntity.class);
		L.v(tag,"MessagePacketListenerImpl :getContentByBody->body" + body);
		try
		{
			if(bodyEntity != null)
			{
				switch(bodyEntity.getMsgType())
				{

					case Const.CHAT_TYPE_P2P:
						if(bodyEntity.getImages() != null && bodyEntity.getImages().size() > 0)
						{
							content = "[图片...]";
						}else
						{
							content = bodyEntity.getContent();
						}
						break;
					case Const.CHAT_TYPE_DIS:
					case Const.CHAT_TYPE_GROUP:
					case Const.CHAT_TYPE_BROADCAST:
					case Const.CHAT_TYPE_NOTICE:
						if(bodyEntity.getImages() != null && bodyEntity.getImages().size() > 0)
						{
							if(bodyEntity.getSendName() != null)
							{
								content = bodyEntity.getSendName() + "  " + "[图片...]";
							}else
							{
								content = "[图片...]";
							}

						}else
						{
							if(bodyEntity.getSendName() != null)
							{
								content = bodyEntity.getContent();
							}else
							{
								content = bodyEntity.getContent();
							}
						}
						break;
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();

		}
		L.v(tag,"MessagePacketListenerImpl :getContentByBody->content:" + content);
		return bodyEntity;
		// return content == null ? " " : content;
	}

	@Override
	public void processPacket(Packet arg0)
	{
		Message message = (Message)arg0;
		L.v(tag,"MessagePacketListenerImpl :toxml   " + message.toXML());

		String mFromAccount = null;
		Message.Type mType = null;
		RecentChat recentChat = new RecentChat();
		String mJsonBody = null;

		mType = message.getType();
		mFromAccount = JIDUtil.getAccountByJID(message.getFrom().trim());
		if(mFromAccount != null)
		{
			recentChat.setSenderNo(mFromAccount);
			recentChat.setUserNo(mFromAccount);
			// 修改讨论组名称
			if(message.getBody() == null || message.getSubject() != null)
			{
				updateGroupSubject(mFromAccount,message);
				return;
			}
			mJsonBody = formatHtmlBodyToJson(message.getBody());
			MessageBodyEntity tempBodyEntity = getContentByBody(mJsonBody);
			if(tempBodyEntity != null)
			{
				recentChat.setContent(tempBodyEntity.getContent());
				recentChat.setSenderName(tempBodyEntity.getSendName());
			}
			recentChat.setDateTime(message.getSendTime());
			// 广播或者个人回话
			if(mType == Message.Type.chat)
			{
				// broadcast
				if(message.getExtension(Const.BROADCAST_ID) != null)
				{
					L.v(tag,"MessagePacketListenerImpl broadcast: " + "sendtime:" + message.getSendTime() + "subject:"
							+ message.getSubject());
					recentChat.setUserNo(Const.BROADCAST_ID);
					recentChat.setChatType(Const.CHAT_TYPE_BROADCAST);// 设置广播类型
					recentChat.setTitle(Const.BROADCAST_NAME);
					recentChat.setContent(message.getBody());
					BroadcastChat broadcast = createBroadcastChatByMessage(message,mFromAccount,mFromAccount);

					if(mXmppConnManager.getReceiveBroadcastChatCallBack() != null)
					{// 广播显示界面打开
						broadcast.setIsRead(1);// 标记已读
						mBroadcastChatDao.insertBroadcastChat(broadcast);
						recentChat.setUnReadCount(0);
						mRecentChatDao.saveRecentChat(recentChat);// 保存最近会话
						mXmppConnManager.getReceiveBroadcastChatCallBack().onReceiveBroadcastChat(broadcast);// 回调
						mNoticesManager.updateRecentChatList(Const.BROADCAST_ID,recentChat.getChatType());// 更新最近会话列表
					}else
					{// 广播显示界面没有打开，提醒
						broadcast.setIsRead(0);// 标记未读
						mBroadcastChatDao.insertBroadcastChat(broadcast);
						recentChat.setUnReadCount(1);
						mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话
						mNoticesManager.sendMessageTypeNotice(Const.BROADCAST_ID,recentChat.getChatType());// 发送消息提醒

					}
					// p2pchat
				}else
				{
					recentChat.setChatType(Const.CHAT_TYPE_P2P);// 设置会话类型
					recentChat.setTitle(mRecentChatDao.getUserNameByUserId(mFromAccount,Const.CHAT_TYPE_P2P));
					P2PChatMsgEntity msg = new P2PChatMsgEntity();
					msg.setChatUserNo(mFromAccount);
					msg.setIsReaded(P2PChatMsgEntity.IS_NOT_READED);
					msg.setIsSuccess(P2PChatMsgEntity.SEND_SUCCESS);
					msg.setIsSend(P2PChatMsgEntity.COM_MSG);
					msg.setMessageType(P2PChatMsgEntity.MESSAGE);
					// msg.setMessage(GZIPUtil.uncompress(message.getBody()));
					msg.setMessage(mJsonBody);
					msg.setmTime(String.valueOf(System.currentTimeMillis()));
					msg.setPacketId(message.getPacketID());
					// if (message.getSendTime() != null) {
					// msg.setmTime(message.getSendTime());
					// } else {
					// msg.setmTime(TimeUtil
					// .getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24_mic));
					// }

					// 如果个人会话窗口打开，mCallback!= null
					if((mCallback = mXmppConnManager.getReceiveMessageCallBack(mFromAccount)) != null)
					{// 根据发送方账号转发消息
						mCallback.receivedMessage(msg);

						recentChat.setUnReadCount(0);
						mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话
						mNoticesManager.updateRecentChatList(mFromAccount,recentChat.getChatType());// 更新最近会话列表
					}// 未打开会话窗，保存到个人记录，发送消息提醒
					else
					{
						recentChat.setUnReadCount(1);
						mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话

						mNoticesManager.sendMessageTypeNotice(mFromAccount,recentChat.getChatType());// 发送消息提醒

					}
					mP2pChartMsgDao.saveMsg(msg);// 保存消息到个人会话记录
				}
			}
			// 群或讨论组消息
			else if(mType == Message.Type.groupchat)
			{
				String mFromGroupMemberAccount = JIDUtil.getResouceNameByJID(message.getFrom().trim());
				L.e("mFromGroupMemberAccount == " + mFromGroupMemberAccount);
				L.e("LastSPAccount == " + LastLoginUserSP.getInstance(mContext).getUserAccount());
				if(mFromGroupMemberAccount.equals(LastLoginUserSP.getInstance(mContext).getUserAccount()))
				{
					return;
				}
				// 从数据库表中获取当前接收消息的群\讨论组详细信息
				ContactGroup contactGroup = mContactOrgDao.queryGroupOrDiscussByGroupName(mFromAccount);
				// 讨论组消息
				if(mFromAccount.startsWith("dis"))
				{
					recentChat.setChatType(Const.CHAT_TYPE_DIS);// 设置会话类型
					recentChat.setTitle(mRecentChatDao.getUserNameByUserId(mFromAccount,Const.CHAT_TYPE_DIS));

					// 发送者ID
					recentChat.setSenderNo(mFromGroupMemberAccount);
					// User mUser =
					// mContactOrgDao.queryUserInfoByUserNo(mFromGroupMemberAccount);
					// if (mUser != null) {
					// //发送者名称
					// recentChat.setSenderName(mUser.getUserName());
					// }
					GroupChatMsgEntity msg = new GroupChatMsgEntity();
					msg.setGroupId(mFromAccount); // 群ID
					msg.setChatUserNo(mFromGroupMemberAccount); // 发送者ID
					if(tempBodyEntity != null)
					{
						// 发送者名称
						msg.setSenderName(tempBodyEntity.getSendName());
					}
					msg.setIsReaded(GroupChatMsgEntity.IS_NOT_READED);
					msg.setIsSuccess(GroupChatMsgEntity.SEND_SUCCESS);
					msg.setIsSend(GroupChatMsgEntity.COM_MSG);
					msg.setMessageType(GroupChatMsgEntity.MESSAGE);
					// msg.setMessage(GZIPUtil.uncompress(message.getBody()));
					msg.setMessage(mJsonBody);
					msg.setmTime(String.valueOf(System.currentTimeMillis()));
					msg.setPacketId(message.getPacketID());

					// 如果该讨论组会话窗口打开，mCallback!= null
					if((mCallback = mXmppConnManager.getReceiveMessageCallBack(mFromAccount)) != null)
					{// 根据讨论组账号转发消息

						// 回调接口分发消息到会话界面
						mCallback.receivedMessage(msg);

						recentChat.setUnReadCount(0);
						mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话
						mNoticesManager.updateRecentChatList(mFromAccount,recentChat.getChatType());// 更新最近会话列表
					}// 未打开该讨论组会话窗，保存到讨论组记录，发送消息提醒
					else
					{

						recentChat.setUnReadCount(1);
						mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话

						if(contactGroup != null)
						{
							mNoticesManager.sendMessageTypeNotice(mFromAccount,recentChat.getChatType(),
									contactGroup.getNotifyMode() != 0);// 发送消息提醒
						}else
						{
							mNoticesManager.sendMessageTypeNotice(mFromAccount,recentChat.getChatType());// 发送消息提醒
						}

					}
					mDisGroupChatDao.saveGroupChatMsg(msg);
				}
				// 群组消息
				else if(mFromAccount.startsWith("group"))
				{
					recentChat.setChatType(Const.CHAT_TYPE_GROUP);// 设置会话类型
					recentChat.setTitle(mRecentChatDao.getUserNameByUserId(mFromAccount,Const.CHAT_TYPE_GROUP));
					// 发送者ID
					recentChat.setSenderNo(mFromGroupMemberAccount);
					// User mUser =
					// mContactOrgDao.queryUserInfoByUserNo(mFromGroupMemberAccount);
					// if (mUser != null) {
					// //发送者名称
					// recentChat.setSenderName(mUser.getUserName());
					// }
					GroupChatMsgEntity msg = new GroupChatMsgEntity();
					msg.setGroupId(mFromAccount); // 群ID
					msg.setChatUserNo(mFromGroupMemberAccount); // 发送者ID
					if(tempBodyEntity != null)
					{
						// 发送者名称
						msg.setSenderName(tempBodyEntity.getSendName());
					}
					msg.setIsReaded(GroupChatMsgEntity.IS_NOT_READED);
					msg.setIsSuccess(GroupChatMsgEntity.SEND_SUCCESS);
					msg.setIsSend(GroupChatMsgEntity.COM_MSG);
					msg.setMessageType(GroupChatMsgEntity.MESSAGE);
					// msg.setMessage(GZIPUtil.uncompress(message.getBody()));
					msg.setMessage(mJsonBody);
					msg.setmTime(String.valueOf(System.currentTimeMillis()));
					msg.setPacketId(message.getPacketID());
					// 如果该群组会话窗口打开，mCallback!= null
					if((mCallback = mXmppConnManager.getReceiveMessageCallBack(mFromAccount)) != null)
					{// 根据群组账号转发消息

						mCallback.receivedMessage(msg);

						recentChat.setUnReadCount(0);
						mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话
						mNoticesManager.updateRecentChatList(mFromAccount,recentChat.getChatType());// 更新最近会话列表
					}// 未打开该群组会话窗，保存到讨论组记录，发送消息提醒
					else
					{
						recentChat.setUnReadCount(1);
						mRecentChatDao.saveRecentChat(recentChat);// 发送消息提醒之前保存最近会话

						if(contactGroup != null) // 第一次安装登陆时contactGroup会未null，如果刚登陆时有消息来则默认声音提醒
						{
							mNoticesManager.sendMessageTypeNotice(mFromAccount,recentChat.getChatType(),
									contactGroup.getNotifyMode() != 0);// 发送消息提醒
						}else
						{
							mNoticesManager.sendMessageTypeNotice(mFromAccount,recentChat.getChatType());// 发送消息提醒
						}
					}
					mGroupChatDao.saveGroupChatMsg(msg);
				}
			}
		}
	}

	/**
	 * 收到群、讨论组名称被修改的msg
	 * 
	 * @param mGroupId
	 * @param message
	 */
	private void updateGroupSubject(String mGroupId, Message message)
	{
		RecentChat mRecentChat;
		int mGroupType;
		// 更新联系人中讨论组列表的名字
		if(mGroupId.startsWith("dis"))
		{
			mGroupType = Const.CONTACT_DISGROUP_TYPE;
			mRecentChat = mRecentChatDao.isChatExist(mGroupId,Const.CHAT_TYPE_DIS);
		}else
		{
			mGroupType = Const.CONTACT_GROUP_TYPE;
			mRecentChat = mRecentChatDao.isChatExist(mGroupId,Const.CHAT_TYPE_GROUP);
		}
		// 更改群组、讨论组名称
		mContactOrgDao.updateGroupSubject(mGroupId,message.getSubject(),mGroupType);

		Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_UPDATE_GROUP);
		updateViewIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME,mGroupType);
		mContext.sendBroadcast(updateViewIntent);

		// 更新会话列表中讨论组名称
		if(mRecentChat != null)
		{
			mRecentChat.setTitle(message.getSubject());
			mRecentChatDao.updateRecentChat(mRecentChat);// 发送消息提醒之前保存最近会话
			mNoticesManager.updateRecentChatList(mGroupId,mRecentChat.getChatType());// 更新最近会话列表
		}
	}
}
