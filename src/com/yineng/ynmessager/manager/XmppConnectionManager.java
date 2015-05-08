package com.yineng.ynmessager.manager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.smack.ReceiveBroadcastChatCallBack;
import com.yineng.ynmessager.smack.ReceiveMessageCallBack;
import com.yineng.ynmessager.smack.ReceivePresenceCallBack;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQProvider;
import com.yineng.ynmessager.smack.StatusChangedCallBack;

import android.os.Handler;

/**
 * 整个xmpp链接的管理类，
 * 
 * @author Yutang
 * 
 */
public class XmppConnectionManager {
	private static XmppConnectionManager xmppConnectionManager;
	private XMPPConnection mConnection;// xmppconnection链接
	private ProviderManager mProviderManager;// IQ解析管理器
	private ConnectionConfiguration mConnectionConfig;// 连接配置
	private Map<String, ReceiveReqIQCallBack> mRevIQCallBackMap = new Hashtable<String, ReceiveReqIQCallBack>();// UI层的IQ监听实例容器
	private Map<String, ReceiveMessageCallBack> mRevMsgCallBackMap = new Hashtable<String, ReceiveMessageCallBack>();// UI层的Message监听实例容器
	private HashSet<ReceivePresenceCallBack> mRevPresCallBackSet = new HashSet<ReceivePresenceCallBack>();// UI层的Presence监听实例容器
	private HashSet<StatusChangedCallBack> mStatusCallBackSet = new HashSet<StatusChangedCallBack>();// UI层的Presence监听实例容器
	private ReceiveBroadcastChatCallBack mReceiveBroadcastCallBack;
	private boolean mIsInit;// 链接初始状态标志

	private XmppConnectionManager() {

	}

	/**
	 * 懒汉式单例方法
	 * 
	 * @return
	 */
	public static synchronized XmppConnectionManager getInstance() {
		if (xmppConnectionManager == null) {
			xmppConnectionManager = new XmppConnectionManager();
		}
		return xmppConnectionManager;
	}

	/**
	 * init ConnectionConfiguration and XMPPConnection listener
	 * 
	 * @param host
	 * @param port
	 * @param devicename
	 * @return
	 */
	public void init(String host, int port, String servicename) {
		//如果用户切换了服务器地址，需要重新初始化连接配置
		if (mConnectionConfig != null && !mConnectionConfig.getHost().equals(host)) {
			mIsInit = false;
		}
		if (!mIsInit) {
			clearUserCallbacks();// 清空用户注册的回调接口实例
			Connection.DEBUG_ENABLED = false;
			mProviderManager = ProviderManager.getInstance();
			configureProviders(mProviderManager);
			mConnectionConfig = new ConnectionConfiguration(host, port,
					servicename);
			// 是否压缩
			mConnectionConfig.setCompressionEnabled(false);
			// 开启调试模式
			mConnectionConfig.setDebuggerEnabled(true);
			// 是否SASL验证
			mConnectionConfig.setSASLAuthenticationEnabled(true);//
			// 不使用SASL验证，设置为false
			mConnectionConfig
					.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
			// 允许自动连接
			mConnectionConfig.setReconnectionAllowed(true);
//			mConnectionConfig.setTruststorePath("/system/etc/security/cacerts.bks");         
//			mConnectionConfig.setTruststorePassword("changeit");         
//			mConnectionConfig.setTruststoreType("bks");  
			// 允许登陆成功后更新在线状态̬
			mConnectionConfig.setSendPresence(true);
			// 收到好友邀请后manual表示需要经过同意,accept_all表示不经同意自动为好友
			// Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
			mConnection = new XMPPConnection(mConnectionConfig);
			mIsInit = true;

		}
	}

	public XMPPConnection getConnection() {
		return mConnection;
	}

	/**
	 * 为不同的IQ设置相应的解析器，
	 * 
	 * @param pm
	 */
	private void configureProviders(ProviderManager pm) {
		// add ReqIQProvider
		ReqIQProvider reqIQProvider = new ReqIQProvider();
		//客户端初始化
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_CLIENT_INIT, reqIQProvider);
		//组织机构
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_ORG, reqIQProvider);
		//用户状态
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_STATUS, reqIQProvider);
		//群与讨论组
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_GROUP, reqIQProvider);
		//查看某用户详细信息
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL,
				reqIQProvider);
		//离线消息
		pm.addIQProvider("req", Const.REQ_IQ_XMLNS_GET_OFFLINE_MSG,
				reqIQProvider);
		pm.addIQProvider("notice", "com:yineng:notice", reqIQProvider);
		// add delivery receipts
		pm.addIQProvider("req", "com:yineng:receipt", reqIQProvider);
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT,
				DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT,
				DeliveryReceipt.NAMESPACE,
				new DeliveryReceiptRequest.Provider());
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		// add XMPP Ping (XEP-0199)
		pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
	}

	/**
	 * 注册msg消息回调器
	 * 
	 * @param account
	 *            回话对方的 account 或讨论组、群名称
	 * @param callback
	 */
	public void addReceiveMessageCallBack(String account,
			ReceiveMessageCallBack callback) {
		mRevMsgCallBackMap.put(account, callback);
	}

	/**
	 * 通过Key获取msg消息回调器
	 * 
	 * @param account
	 *            回话对方的 account 或讨论组、群名称
	 * @return
	 */
	public ReceiveMessageCallBack getReceiveMessageCallBack(String account) {
		return mRevMsgCallBackMap.get(account);
	}

	/**
	 * 删除msg消息回调器
	 * 
	 * @param account
	 *            回话对方的 account或讨论组、群名称
	 */
	public void removeReceiveMessageCallBack(String account) {
		mRevMsgCallBackMap.remove(account);
	}

	/**
	 * 获取IQ消息回调器
	 * 
	 * @param namespace
	 * @return
	 */
	public ReceiveReqIQCallBack getReceiveReqIQCallBack(String namespace) {
		return mRevIQCallBackMap.get(namespace);
	}

	/**
	 * 删除IQ消息回调器
	 * 
	 * @param namespace
	 */
	public void removeReceiveReqIQCallBack(String namespace) {
		mRevIQCallBackMap.remove(namespace);
	}

	/**
	 * 注册IQ消息回调器
	 * 
	 * @param namespace
	 * @param callback
	 */
	public void addReceiveReqIQCallBack(String namespace,
			ReceiveReqIQCallBack callback) {
		mRevIQCallBackMap.put(namespace, callback);
	}

	/**
	 * 注册Presence消息回调器
	 * 
	 * @param callback
	 */
	public void addReceivePresCallBack(ReceivePresenceCallBack callback) {

		mRevPresCallBackSet.add(callback);
	}

	/**
	 * 删除Presence消息回调器
	 * 
	 * @param callback
	 */
	public void removeReceivePresCallBack(ReceivePresenceCallBack callback) {

		mRevPresCallBackSet.remove(callback);
	}

	/**
	 * 分发presence 消息
	 * 
	 * @param packet
	 */
	public void dispatchPresence(Presence packet) {
		ReceivePresenceCallBack callback = null;
		Iterator<ReceivePresenceCallBack> iterator = mRevPresCallBackSet
				.iterator();
		while (iterator.hasNext()) {
			callback = iterator.next();
			callback.receivedPresence(packet);
		}
	}

	/**
	 * 注册用户状态变动回调器
	 * 
	 * @param callback
	 */
	public void addStatusChangedCallBack(StatusChangedCallBack callback) {

		mStatusCallBackSet.add(callback);
	}

	/**
	 * 删除用户状态变动回调器
	 * 
	 * @param callback
	 */
	public void removeStatusChangedCallBack(StatusChangedCallBack callback) {

		mStatusCallBackSet.remove(callback);
	}

	/**
	 * 回调用户状态变动的所有监听
	 * 
	 * @return
	 */
	public void doStatusChangedCallBack(int status) {
		StatusChangedCallBack callback = null;
		Iterator<StatusChangedCallBack> iterator = mStatusCallBackSet
				.iterator();
		while (iterator.hasNext()) {
			callback = iterator.next();
			callback.onStatusChanged(status);
		}
	}

	/**
	 * 发送packet
	 * 
	 * @param packet
	 * @throws IOException
	 */
	public void sendPacket(Packet packet) throws IOException {
		if (mConnection != null && mConnection.isConnected()) {
			mConnection.sendPacket(packet);
		} else {
			throw new IOException();
		}
	}

	/**
	 * 注册packet监听器
	 * 
	 * @param listener
	 * @param filter
	 */
	public void addPacketListener(PacketListener listener, PacketFilter filter) {
		if (mConnection != null) {
			mConnection.addPacketListener(listener, filter);
		}
	}

	/**
	 * 删除packet监听器
	 * 
	 * @param listener
	 */
	public void removePacketListener(PacketListener listener) {
		if (mConnection != null) {
			mConnection.removePacketListener(listener);
		}
	}

	/**
	 * 判断链接是否登陆
	 * 
	 * @return
	 */
	public boolean isAuthenticated() {
		if (mConnection != null) {
			return mConnection.isAuthenticated();
		} else {
			return false;
		}
	}

	/**
	 * 判断链接是否断开
	 * 
	 * @return
	 */
	public boolean isConnected() {
		if (mConnection != null) {
			return mConnection.isConnected();
		} else {
			return false;
		}
	}

	/**
	 * 获取服务器域名 如：m.com
	 * 
	 * @return
	 */
	public String getServiceName() {
		if (mConnection != null && mConnection.isConnected()) {
			return mConnection.getServiceName();
		} else {
			return null;
		}
	}

	/**
	 * 获取当前链接的账号名
	 * 
	 * @return
	 */
	public String getUser() {
		if (mConnection != null) {
			return mConnection.getUser();
		} else {
			return "";
		}
	}

	/**
	 * @return 获取用户状态：
	 */
	public int getUserCurrentStatus() {
		if (mConnection != null) {
			if (mConnection.isLoginedOther()) {

				return LoginThread.USER_STATUS_LOGINED_OTHER;
			}
			if (mConnection.isConnected()) {

				if (mConnection.isAuthenticated()) {

					return LoginThread.USER_STATUS_ONLINE;
				} else {

					return LoginThread.USER_STATUS_OFFLINE;
				}

			} else {
				return LoginThread.USER_STATUS_NETOFF;
			}
		} else {

			return LoginThread.USER_STATUS_NETOFF;
		}
	}

	/**
	 * 开启登陆请求线程/登录界面时调用
	 * 
	 * @param username
	 *            用户名
	 * @param password
	 *            密码
	 * @param resourcename
	 *            资源名称
	 * @param mHandler
	 *            消息接收的handler实体
	 */
	public void doLoginThread(String username, String password,
			String resourcename, Handler mHandler) {
		LoginThread thread = new LoginThread(username, password, resourcename,
				mHandler, mConnection);
		new Thread(thread).start();
//		if (mThread == null) {
//			mThread = new Thread() {
//				@Override
//				public void run() {
//					// 工作
//					synchronized (mThread) {
//						mThread = null;
//					}
//				}
//			};
//		}
	}

	/**
	 * 后台重新登录时调用
	 * 
	 * @param username
	 * @param password
	 * @param resourcename
	 * @param mHandler
	 */
	public void doReLoginThread(String username, String password,
			String resourcename, Handler mHandler) {
		LoginThread thread = new LoginThread(username, password, resourcename,
				mHandler, mConnection);
		new Thread(thread).start();
	}

	/**
	 * 开启下线请求线程
	 */
	public void doExistThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (mConnection != null && mConnection.isConnected()) {
					Presence presence = new Presence(Presence.Type.unavailable);
					presence.setStatus("offline");
					mConnection.disconnect(presence);
				}
			}
		}).start();
	}

	/**
	 * 判断XmppConnectionManager是否初始化
	 * 
	 * @return
	 */
	public boolean isInit() {
		return mIsInit;
	}

	/**
	 * 获取广播消息回调接口实例
	 * 
	 * @return
	 */
	public ReceiveBroadcastChatCallBack getReceiveBroadcastChatCallBack() {
		return mReceiveBroadcastCallBack;
	}

	/**
	 * 设置广播消息回调函数
	 * 
	 * @param receiveBroadcastCallBack
	 */
	public void setReceiveBroadcastChatCallBack(
			ReceiveBroadcastChatCallBack receiveBroadcastCallBack) {
		this.mReceiveBroadcastCallBack = receiveBroadcastCallBack;
	}

	/**
	 * 释放广播消息回调接口实例
	 */
	public void clearReceiveBroadcastChatCallBack() {
		this.mReceiveBroadcastCallBack = null;
	}

	/**
	 * 清空用户注册的回调接口实例
	 */
	public void clearUserCallbacks() {
		mRevIQCallBackMap.clear();// 清空监听器
		mRevMsgCallBackMap.clear();// 清空监听器
		mRevPresCallBackSet.clear();// 清空监听器
		mStatusCallBackSet.clear();// 清空监听器
		mReceiveBroadcastCallBack = null;
	}
}
