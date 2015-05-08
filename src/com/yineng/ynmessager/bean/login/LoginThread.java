package com.yineng.ynmessager.bean.login;

import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.util.DESUtil;

import android.os.Handler;
import android.os.Message;

/**
 * @author YINENG Date 2014-12-31 登录线程类，登录界面调用
 */
public class LoginThread implements Runnable {
	// 登陆状态
	public final static int LOGIN_START = 1;
	public final static int LOGIN_FAIL = 2;
	public final static int LOGIN_SUCCESS = 3;
	public final static int LOGIN_TIMEOUT = 4;
	// public final static int LOGIN_INIT_OK = 5;
	// public final static int LOGIN_GETORG_OK = 6;
	// public final static int LOGIN_START_INIT = 7;
	// 用户状态
	public final static int USER_STATUS_ONLINE = LOGIN_SUCCESS;// 正常上线
	public final static int USER_STATUS_OFFLINE = LOGIN_FAIL;// 下线
	public final static int USER_STATUS_NETOFF = LOGIN_TIMEOUT;// 服务器连接断开
	public final static int USER_STATUS_ONLOGIN = LOGIN_START;// 正在登陆
	public final static int USER_STATUS_LOGINED_OTHER = 9;// 其它地方已经登陆了
	private String mUserAccount;
	private String mPassword;
	private String mResource;
	private Handler mHandler;
	private XMPPConnection mConnection;
	private boolean isStarting = false;

	/**
	 * 构造函数
	 * 
	 * @param account
	 * @param password
	 * @param resource
	 * @param handler
	 * @param connection
	 */
	public LoginThread(String account, String password, String resource,
			Handler handler, XMPPConnection connection) {
		this.mUserAccount = account;
		this.mPassword = password;
		this.mResource = resource;
		this.mHandler = handler;
		this.mConnection = connection;
	}

	/**
	 * 发送到Handler消息队列
	 * 
	 * @param arg
	 * @param delaymillis
	 */
	private void sendHandlerMessage(int arg, long delaymillis) {
		Message msg = mHandler.obtainMessage();
		msg.arg1 = arg;
		mHandler.sendMessageDelayed(msg, delaymillis);
	}

	@Override
	public void run() {
		try {
			if (mConnection != null && !mConnection.isConnected()) {
				isStarting = true;
				sendHandlerMessage(LOGIN_START, 0);
				mConnection.connect();
			}
		} catch (Exception e) {
			sendHandlerMessage(LOGIN_TIMEOUT, 500);
			e.printStackTrace();
			return;
		}
		try {
			if (mConnection != null && mConnection.isConnected()) {
				if (!isStarting) {
					sendHandlerMessage(LOGIN_START, 0);
				}
				SASLAuthentication.supportSASLMechanism("PLAIN");
				if (!mConnection.isAuthenticated()) {
					mConnection.login(mUserAccount, formatPassword(mPassword),
							mResource);
				}
			} else {
				sendHandlerMessage(LOGIN_TIMEOUT, 500);
				return;
			}

		} catch (XMPPException e) {
			e.printStackTrace();

		} finally {
			if (mConnection != null && mConnection.isAuthenticated()) {
				Presence presence = new Presence(Presence.Type.available);
				presence.setStatus("online");
				mConnection.sendPacket(presence);
				sendHandlerMessage(LOGIN_SUCCESS, 1000);
			} else {
				sendHandlerMessage(LOGIN_FAIL, 1000);
			}
		}
	}

	/**
	 * @param password
	 *            对密码加密处理
	 * @return
	 */
	public static String formatPassword(String password) {
		String pass = "{'passwd':'" + password + "','cversion':'100'}";

		pass = DESUtil.encrypt(pass, Const.DES_KEY);
		return pass;
	}

	/**
	 * @param address
	 * @return 获取主机名
	 */
	public static String getHostFromAddress(String address) {

		if (address.contains(":")) {

			return address.split(":")[0];

		} else {

			return address;
		}
	}

	/**
	 * @param address
	 * @return 获取端口号
	 */
	public static int getPortFromAddress(String address) {

		if (address.contains(":")) {

			return Integer.parseInt(address.split(":")[1]);

		} else {

			return Const.SERVER_PORT;
		}
	}
}
