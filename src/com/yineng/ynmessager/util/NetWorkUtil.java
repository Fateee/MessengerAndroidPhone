package com.yineng.ynmessager.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

/**
 * @author Yutang
 * 网络连接工具
 */
public class NetWorkUtil {
	/**
	 * 0 ：没网络
	 */
	public static final int NETWORN_NONE = 0;
	/**
	 * 1：WIFI
	 */
	public static final int NETWORN_WIFI = 1;
	/**
	 * 2:移动数据
	 */
	public static final int NETWORN_MOBILE = 2;

	/**
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		context = context.getApplicationContext();
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();			
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].isConnected()) {
						return true;
					}
				}
			}
		}
		return false;
	}	

	/**
	 * 获取网络连接类型 0 ：没网络；1：WIFI;2:移动数据
	 * @param context
	 * @return
	 */
	public static int getNetworkState(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		// Wifi
		State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if (state == State.CONNECTED || state == State.CONNECTING) {
			return NETWORN_WIFI;
		}

		// 3G
		state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
				.getState();
		if (state == State.CONNECTED || state == State.CONNECTING) {
			return NETWORN_MOBILE;
		}
		return NETWORN_NONE;
	}
}
