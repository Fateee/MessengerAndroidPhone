package com.yineng.ynmessager.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Presence;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.ClientInitConfig;
import com.yineng.ynmessager.bean.OfflineMessageList;
import com.yineng.ynmessager.bean.OfflineMessageList.OfflineMsg;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.ContactGroupBean;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.ContactOrg;
import com.yineng.ynmessager.bean.contact.UserStatus;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.IQPacketListenerImpl;
import com.yineng.ynmessager.smack.MessagePacketListenerImpl;
import com.yineng.ynmessager.smack.PresencePacketListenerImpl;
import com.yineng.ynmessager.smack.PresencePacketListenerImpl.groupCreatedListener;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;

/**
 * 后台服务，负责消息监听及用户状态监听
 * 
 * @author YINENG
 * 
 */
public class XmppConnService extends Service implements ReceiveReqIQCallBack {
	public static final int MAX_TIME = 10;
	public final int OFFLINE_MESSAGE_LOADED = 100;
	private Context mContext;// 全局上下文
	private XmppConnectionManager mXmppConnManager;
	private PacketListener mIQListener;
	private PresencePacketListenerImpl mPresenceListener;
	private PacketListener mMessageListener;
	private int mCurrentStats = 0;
	private XMPPConnection mXMPPConnection;
	// filter
	private PacketFilter mIqFilter;
	private PacketTypeFilter mPresenceFilter;
	private PacketTypeFilter mMSGFilter;
	private int mLoginedTimes = 0;
	/**
	 * 离线消息
	 */
	private List<OfflineMsg> mOffMsgList = new ArrayList<OfflineMessageList.OfflineMsg>();
	/**
	 * 启动登陆初始化的操作
	 */
	private Runnable mDoInitRunnable = new Runnable() {
		@Override
		public void run() {
			init();
		}
	};
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			int x = msg.arg1;
			switch (x) {

			case LoginThread.LOGIN_START:// 登陆中，登陆按钮不可用
				L.v("XmppConnService ", "handleMessage ->LOGIN_START");
				mCurrentStats = LoginThread.USER_STATUS_ONLOGIN;
				break;
			case LoginThread.LOGIN_FAIL:// 登陆失败
				L.v("XmppConnService ", "handleMessage ->LOGIN_FAIL");
				mHandler.removeCallbacks(mDoInitRunnable);
				if (NetWorkUtil.isNetworkAvailable(mContext)
						&& mLoginedTimes < MAX_TIME) {// 如果网络可用
					mHandler.postDelayed(mDoInitRunnable, 30000);// 每半分钟自动登陆
				}
				break;
			case LoginThread.LOGIN_SUCCESS:// 登陆成功
				L.v("XmppConnService ", "handleMessage ->LOGIN_SUCCESS");
				initListener();// 注册消息接收监听器
				// 获取联系人信息
				initContactsInfoData();
				break;

			case LoginThread.LOGIN_TIMEOUT:// 建立连接失败
				L.v("XmppConnService ", "handleMessage ->LOGIN_TIMEOUT");
				mHandler.removeCallbacks(mDoInitRunnable);
				if (NetWorkUtil.isNetworkAvailable(mContext)
						&& mLoginedTimes < MAX_TIME) {// 如果网络可用
					mHandler.postDelayed(mDoInitRunnable, 10000 * mLoginedTimes);
				}
				break;
			default:
				break;
			}
			if (msg.what == OFFLINE_MESSAGE_LOADED) {
				for (int i = mOffMsgList.size() -1 ; i >=0; i--) {
					OfflineMsg tempOffMsg = mOffMsgList.get(i);
					Message message = new Message();
					message.setBody(tempOffMsg.getBody());
					message.setFrom(tempOffMsg.getFrom());
					message.setSendTime(tempOffMsg.getSendTime());
					if (tempOffMsg.getType().equals("groupchat")) {
						message.setType(Type.groupchat);
					} else {
						message.setType(Type.chat);
					}
					mMessageListener.processPacket(message);
				}
			}
		}
	};
	// /**
	// * 监听用户
	// */
	// private Runnable mStatusChangedRunnable = new Runnable() {
	// @Override
	// public void run() {
	// dispachtUserStatus();
	// // perform it every 0.5 minute
	// mHandler.removeCallbacks(this);
	// mHandler.postDelayed(this, 30000);
	// }
	// };
	private ContactOrgDao mContactOrgDao;
	private String mPacketId;

	@Override
	public void onCreate() {
		super.onCreate();
		L.v("XmppService", " onCreate: ");
		mContext = this;
		// filter
		mIqFilter = new PacketTypeFilter(IQ.class);
		mPresenceFilter = new PacketTypeFilter(Presence.class);
		mMSGFilter = new PacketTypeFilter(Message.class);
		// new listener
		mIQListener = new IQPacketListenerImpl();
		mPresenceListener = new PresencePacketListenerImpl(this);
		mMessageListener = new MessagePacketListenerImpl(this);
	}

	/**
	 * 初始网络链接及监听器
	 */
	private void init() {
		L.v(XmppConnService.class, "start init function");
		LastLoginUserSP lastUser = LastLoginUserSP.getInstance(mContext);
		if (lastUser.isExistsUser()) {// 如果有用户登陆记录
			mContactOrgDao = new ContactOrgDao(mContext,
					lastUser.getUserAccount());
			// mHandler.postDelayed(mStatusChangedRunnable, 3000);// 开启用户状态监听
			if (mXmppConnManager == null) {
				mXmppConnManager = XmppConnectionManager.getInstance();
			}
			if (!mXmppConnManager.isInit()) {
				mXmppConnManager.init(LoginThread.getHostFromAddress(lastUser
						.getUserServicesAddress()), LoginThread
						.getPortFromAddress(lastUser.getUserServicesAddress()),
						Const.RESOURSE_NAME);
			}
//			L.v("XmppConnService ", "mXmppConnManager.getConnection().isConnected() == "+mXmppConnManager.getConnection().isConnected());
//			if (mXmppConnManager.getConnection().isConnected() && mXmppConnManager.isAuthenticated()) {
//				L.v("XmppConnService ", "init  ->isAuthenticated() == true");
//				initListener();// 注册消息接收监听器
//				// 获取联系人信息
//				initContactsInfoData();
			if (mXmppConnManager.isAuthenticated()) {
				L.v("XmppConnService ", "init  ->isAuthenticated() == true");
				initListener();// 注册消息接收监听器
				// 获取联系人信息
				initContactsInfoData();
			} else {
				// 如果网络可用,开启登陆线程
				if (NetWorkUtil.isNetworkAvailable(mContext)) {
					mLoginedTimes = mLoginedTimes + 1;
					mXmppConnManager.doReLoginThread(lastUser.getUserAccount(),
							lastUser.getUserPassword(), Const.RESOURSE_NAME,
							mHandler);
				}
			}
		}
	}

	/**
	 * 初始化所有消息监听器
	 */
	private void initListener() {
		// cancel listener
		unRegisterPacketListener();
		// register
		registerPacketListener();
	}

	/**
	 * 注册消息监听器
	 */
	private void registerPacketListener() {
		mXmppConnManager.addPacketListener(mIQListener, mIqFilter);
		mXmppConnManager.addPacketListener(mPresenceListener, mPresenceFilter);
		mXmppConnManager.addPacketListener(mMessageListener, mMSGFilter);
		if (mXMPPConnection == null) {
			mXMPPConnection = mXmppConnManager.getConnection();
		}
		mXMPPConnection.addConnectionListener(new ConnectionListener() {

			@Override
			public void reconnectionSuccessful() {
				L.i("reconnectionSuccessful");
				dispachtUserStatus();
			}

			@Override
			public void reconnectionFailed(Exception arg0) {
				L.e("reconnectionFailed");
				dispachtUserStatus();
			}

			@Override
			public void reconnectingIn(int arg0) {
				// TODO Auto-generated method stub
				L.e("reconnectingIn");
				dispachtUserStatus();
			}

			@Override
			public void connectionClosedOnError(Exception arg0) {
				// TODO Auto-generated method stub
				L.e("connectionClosedOnError");
				dispachtUserStatus();
			}

			@Override
			public void connectionClosed() {
				// TODO Auto-generated method stub
				L.e("connectionClosed");
				dispachtUserStatus();
			}
		});
		mPresenceListener.setGroupCreatedListener(new groupCreatedListener() {
			
			@Override
			public void groupCreated(String mGroupName) {
				sendRequestIQPacket(mGroupName);
			}
		});
	}

	/**
	 * 取消所有消息监听器
	 */
	private void unRegisterPacketListener() {
		if (mXmppConnManager != null) {
			if (mIQListener != null) {
				mXmppConnManager.removePacketListener(mIQListener);
			}
			if (mPresenceListener != null) {
				mXmppConnManager.removePacketListener(mPresenceListener);
			}
			if (mMessageListener != null) {
				mXmppConnManager.removePacketListener(mMessageListener);
			}
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		L.v("onStartCommand", " onStartCommand! ");
		mLoginedTimes = 0;
		init();// 初始网络链接及监听器
		return START_STICKY;// 服务应该一直运行除非我们手动停止它

	}

	@Override
	public IBinder onBind(Intent arg0) {
		L.v("onStartCommand", " onBind! ");
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		L.v("XmppService", " onUnbind: ");
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		L.v("XmppService", " onDestroy: ");
		mHandler.removeCallbacksAndMessages(null);
		unRegisterPacketListener();
		if (mXmppConnManager != null) {
			mXmppConnManager.doExistThread();
		}
	}

	/**
	 * 当用户状态发生变化时，把最新状态分发给状态监听器
	 */
	private void dispachtUserStatus() {
		if (mXmppConnManager != null) {
			int status = mXmppConnManager.getUserCurrentStatus();
			L.v("XmppService", " dispachtUserStatus:status = " + status
					+ " ;mCurrentStats=" + mCurrentStats + " mLoginedTimes="
					+ mLoginedTimes);
			if (mCurrentStats != status) {

				mXmppConnManager.doStatusChangedCallBack(status);// 分发用户状态给所有状态监听器

				mCurrentStats = status;

				if (NetWorkUtil.isNetworkAvailable(mContext)) {
					switch (mCurrentStats) {

					case LoginThread.USER_STATUS_LOGINED_OTHER:

						break;
					case LoginThread.USER_STATUS_NETOFF:// 服务器连接断开
					case LoginThread.USER_STATUS_OFFLINE:// 下线了
						if (mLoginedTimes < MAX_TIME) {
							mHandler.removeCallbacks(mDoInitRunnable);
							mHandler.postDelayed(mDoInitRunnable, 1000);// 如果网络正常，但是下线了，重新登陆
						}
						break;
					default:
						break;
					}
				}
			}
		}
	}

	/**
	 * 获取联系人（组织机构，群组，讨论组信息）数据
	 */
	public void initContactsInfoData() {
		mOffMsgList.clear();
		removeContactIQCallback();
		sendRequestIQPacket(0, Const.REQ_IQ_XMLNS_CLIENT_INIT);// 客户端初始化请求
		ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
		if (mClientInitConfig != null) {
			if (mClientInitConfig.getOrg_update_type().equals("0")) { // 全量更新
				sendRequestIQPacket(Const.ORG_UPDATE_ALL,
						Const.REQ_IQ_XMLNS_GET_ORG);// 组织机构请求
			} else { // 增量更新
				sendRequestIQPacket(Const.ORG_UPDATE_SOME,
						Const.REQ_IQ_XMLNS_GET_ORG);// 组织机构请求
			}
			sendRequestIQPacket(Const.CONTACT_GROUP_TYPE,
					Const.REQ_IQ_XMLNS_GET_GROUP);// 群请求
			sendRequestIQPacket(Const.CONTACT_DISGROUP_TYPE,
					Const.REQ_IQ_XMLNS_GET_GROUP);// 讨论组请求
		} else {
			sendRequestIQPacket(Const.ORG_UPDATE_ALL,
					Const.REQ_IQ_XMLNS_GET_ORG);// 组织机构请求
			sendRequestIQPacket(Const.CONTACT_GROUP_TYPE,
					Const.REQ_IQ_XMLNS_GET_GROUP);// 群请求
			sendRequestIQPacket(Const.CONTACT_DISGROUP_TYPE,
					Const.REQ_IQ_XMLNS_GET_GROUP);// 讨论组请求
		}
		sendRequestIQPacket(0, Const.REQ_IQ_XMLNS_GET_STATUS);
		loadOfflineMessages();
	}

	/**
	 * 发送IQ请求
	 * 
	 * @param action
	 *            接口方向
	 * @param nameSpace
	 *            命名空间
	 */
	private void sendRequestIQPacket(int action, String nameSpace) {
		mXmppConnManager.addReceiveReqIQCallBack(nameSpace,
				XmppConnService.this);
		ReqIQ iq = new ReqIQ();
		switch (action) {
		case Const.ORG_UPDATE_SOME:// 组织机构增量更新
			iq.setParamsJson("{\"servertime\":\""
					+ mContactOrgDao.getInitServerTime() + "\"}");
			break;
		case Const.GET_OFFLINE_MSG:// 离线消息
			iq.setParamsJson("{\"messageNum\":\"" + Const.GET_OFFLINE_MSG_NUM
					+ "\"}");
			break;
		case Const.CONTACT_GROUP_TYPE:// 群组
		case Const.CONTACT_DISGROUP_TYPE:// 讨论组
			iq.setAction(action);
			break;
		default:
			break;
		}
		iq.setNameSpace(nameSpace);
		LastLoginUserSP lastUser = LastLoginUserSP.getInstance(mContext);
		iq.setFrom(lastUser.getUserAccount()+ "@" + mXmppConnManager.getServiceName());
		L.i("XmppConnService", "iq xml ->" + iq.toXML());
		mPacketId = iq.getPacketID();
		try {
			mXmppConnManager.sendPacket(iq);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void receivedReqIQResult(ReqIQResult packet) {
		String nameSpace = packet.getNameSpace();
		if (Const.REQ_IQ_XMLNS_CLIENT_INIT.equals(nameSpace)) {// 初始化消息
			L.i("XmppConnService", "init  iq xml ->" + packet.toXML());
			if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
				// 解析初始化信息并保存
				// ClientInitConfig mClientInitConfig =
				// JSON.parseObject(packet.getResp(), ClientInitConfig.class);
				// mContactOrgDao.saveClientInitInfo(mClientInitConfig);
				try {
					// {"disgroup_max_user":"100","group_max_user":"200","max_disdisgroup_can_create":"10","max_group_can_create":"5","org_update_type":"0","rightList":[]}
					String initStr = packet.getResp();
					JSONObject initObject = new JSONObject(initStr);
					String disgroup_max_user = initObject
							.optString("disgroup_max_user");
					String group_max_user = initObject.optString("group_max_user");
					String max_disdisgroup_can_create = initObject
							.optString("max_disdisgroup_can_create");
					String max_group_can_create = initObject
							.optString("max_group_can_create");
					String org_update_type = initObject
							.optString("org_update_type");
					ClientInitConfig mClientInitConfig = new ClientInitConfig(
							disgroup_max_user, group_max_user,
							max_disdisgroup_can_create, max_group_can_create,
							org_update_type);
					mContactOrgDao.saveClientInitInfo(mClientInitConfig);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		} else if (Const.REQ_IQ_XMLNS_GET_ORG.equals(nameSpace)) {// 获取组织机构
			L.v("XmppConnService", "org iq xml ->" + packet.getResp());
			if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
				final ContactOrg allContactOrg = JSON.parseObject(packet.getResp(),
						ContactOrg.class);
				mContactOrgDao.saveAllOrgData(allContactOrg.getOrgList());
				mContactOrgDao.saveAllUserData(allContactOrg.getUserList());
				mContactOrgDao.saveAllUserRelationData(allContactOrg.getRelList());
//				mContactOrgDao.saveAllUserStatusData(allContactOrg.getStatusList());
				L.e("XmppConnService",
						"getServertime ->" + allContactOrg.getServertime());
				String mServerTime = String.valueOf(allContactOrg.getServertime());
				mContactOrgDao.saveInitServerTime(mServerTime);
				LastLoginUserSP lastUser = LastLoginUserSP.getInstance(mContext);
				
				/**全局初始化当前登录的用户信息*/
				AppController.getInstance().mSelfUser = mContactOrgDao.queryUserInfoByUserNo(lastUser.getUserAccount());
			}
		} else if (Const.REQ_IQ_XMLNS_GET_STATUS.equals(nameSpace)) {
			L.v("XmppConnService", "status iq xml ->" + packet.getResp());
			if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
				UserStatus allOnlineUsers = JSON.parseObject(packet.getResp(),UserStatus.class);
				mContactOrgDao.saveAllUserStatusData(allOnlineUsers.getStatusList());
				//把我的信息插入到数据库
				mContactOrgDao.updateOneUserStatus(new UserStatus(AppController.getInstance().mSelfUser.getUserNo(), "online", 1));
			}
		} else if (Const.REQ_IQ_XMLNS_GET_GROUP.equals(nameSpace)) {//群组、讨论组
			if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
				ContactGroupBean mContactGroupBean = JSON.parseObject(
						packet.getResp(), ContactGroupBean.class);
				if (mContactGroupBean.getGroupType() != 0) {
					int mGroupType;
					if (mContactGroupBean.getGroupType() == 1) {
//						mGroupExisted = 1;
						mGroupType = Const.CONTACT_GROUP_TYPE;
					} else {
						mGroupType = Const.CONTACT_DISGROUP_TYPE;
					}
					for (ContactGroup contactGroup : mContactGroupBean.getRoomList()) {
						mContactOrgDao.insertOneContactGroupData(contactGroup, mGroupType);
					}
					for (ContactGroupUser mContactGroupUser : mContactGroupBean.getUserList()) {
						mContactOrgDao.insertOneGroupUserRelationData(mContactGroupUser, mGroupType);
					}
					//发送创建讨论组成功的广播
					Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_UPDATE_GROUP);
					updateViewIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, mGroupType);
					mContext.sendBroadcast(updateViewIntent);
				} else {
					String mAction = packet.getAction();
					if (mAction.equals("8")) {
						mContactOrgDao.saveAllContactGroupData(
								mContactGroupBean.getRoomList(),
								Const.CONTACT_GROUP_TYPE);
						mContactOrgDao.saveAllGroupUserRelationData(
								mContactGroupBean.getUserList(),
								Const.CONTACT_GROUP_TYPE);
					} else if (mAction.equals("9")) {
						mContactOrgDao.saveAllContactGroupData(
								mContactGroupBean.getRoomList(),
								Const.CONTACT_DISGROUP_TYPE);
						mContactOrgDao.saveAllGroupUserRelationData(
								mContactGroupBean.getUserList(),
								Const.CONTACT_DISGROUP_TYPE);
					}
				}
			}
		} else if (Const.REQ_IQ_XMLNS_GET_OFFLINE_MSG.equals(nameSpace)) {
			L.v("XmppConnService", "off msg iq xml ->" + packet.getResp());
			if (packet.getCode() == Const.IQ_RESPONSE_CODE_SUCCESS) {
				OfflineMessageList mOfflineMessageList = JSON.parseObject(packet.getResp(), OfflineMessageList.class);
				L.i("离线消息 == "+mOfflineMessageList.getMessageList().size());
				L.i("离线消息rest == "+mOfflineMessageList.getTotal());
				mOffMsgList.addAll(mOfflineMessageList.getMessageList());
				if (mOfflineMessageList.getTotal() > 0) {
					loadOfflineMessages();
				} else {
					//加载完毕
					mHandler.sendEmptyMessage(OFFLINE_MESSAGE_LOADED);
				}
			}
		} 
	}
	
	/**
	 * 删除联系人IQ消息监听
	 */
	public void removeContactIQCallback() {
		if (mXmppConnManager != null) {
			mXmppConnManager
					.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_CLIENT_INIT);
			mXmppConnManager
					.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_ORG);
			mXmppConnManager
					.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP);
			mXmppConnManager
					.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_OFFLINE_MSG);
		}
	}

	/**
	 * 从服务器获取离线消息
	 */
	public void loadOfflineMessages() {
		sendRequestIQPacket(Const.GET_OFFLINE_MSG,
				Const.REQ_IQ_XMLNS_GET_OFFLINE_MSG);
	}
	
	/**
	 * 发送IQ请求
	 * @param mGroupName 
	 * 
	 * @param action
	 *            接口方向
	 * @param nameSpace
	 *            命名空间
	 */
	private void sendRequestIQPacket(String mGroupName) {
		mXmppConnManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP,
				XmppConnService.this);
		ReqIQ iq = new ReqIQ();
		iq.setAction(13);
		iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_GROUP);
		String jsonParam = getParamsJson(mGroupName);
		iq.setParamsJson(jsonParam);
		LastLoginUserSP lastUser = LastLoginUserSP.getInstance(mContext);
		iq.setFrom(lastUser.getUserAccount()+ "@" + mXmppConnManager.getServiceName());
		iq.setTo("admin@" + mXmppConnManager.getServiceName());
		L.i("PresencePacketListenerImpl", "iq request xml ->" + iq.toXML());
		mPacketId = iq.getPacketID();
		try {
			mXmppConnManager.sendPacket(iq);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 拼接字符串 
	 * @return  eg： {"groupName":"test"}
	 */
	private String getParamsJson(String mGroupName) {
		StringBuilder mBuilder = new StringBuilder();
		mBuilder.append("{\"groupName\":\"");
		mBuilder.append(mGroupName);
		mBuilder.append("\"}");
		return mBuilder.toString();
	}
	
}
