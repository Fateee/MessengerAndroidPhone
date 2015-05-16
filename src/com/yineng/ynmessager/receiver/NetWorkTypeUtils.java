package com.yineng.ynmessager.receiver;

import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.util.L;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * 网络信息工具类
 * @author 胡毅
 *
 */
public class NetWorkTypeUtils {

	public static final int NETTYPE_NO = 0;
	public static final int NETTYPE_WIFI = 1;
	public static final int NETTYPE_2G = 2;
	public static final int NETTYPE_3G = 3;
	public static final int NETTYPE_4G = 4;
	public static final int NETTYPE_UNKOWN = -1;
	public static final String China_Mobile = "CMCC";
	public static final String China_Unicom = "CUCC";
	public static final String China_Telecom = "CTCC";
	public static final String China_Unkown = "unkown";

	/**
	 * 网络是否可用
	 * @return
	 */
	public static boolean isNetAvailable() {
		return NetWorkTypeUtils.getAvailableNetWorkInfo() == null;
	}

	public static boolean isThirdGeneration() {
		TelephonyManager telephonyManager = (TelephonyManager) AppController
				.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		int netWorkType = telephonyManager.getNetworkType();
		switch (netWorkType) {
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_EDGE:

			return false;
		default:
			return true;
		}
	}

	public static boolean isWifi() {

		NetworkInfo networkInfo = getAvailableNetWorkInfo();

		if (networkInfo != null) {

			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				return true;
			}

		}

		return false;
	}

	/**
	 * 获取网络信息对象，如果获取不到，证明网络不好
	 * @return
	 */
	public static NetworkInfo getAvailableNetWorkInfo() {
		NetworkInfo activeNetInfo = null;
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) AppController
					.getInstance().getSystemService(
							Context.CONNECTIVITY_SERVICE);
			activeNetInfo = connectivityManager.getActiveNetworkInfo();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		if (activeNetInfo != null && activeNetInfo.isAvailable()) {
			return activeNetInfo;
		} else {
			return null;
		}
	}

	public static String getNetWorkType() {

		String netWorkType = "";
		NetworkInfo netWorkInfo = getAvailableNetWorkInfo();

		if (netWorkInfo != null) {
			if (netWorkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				netWorkType = "1";
			} else if (netWorkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

				TelephonyManager telephonyManager = (TelephonyManager) AppController
						.getInstance().getSystemService(
								Context.TELEPHONY_SERVICE);

				switch (telephonyManager.getNetworkType()) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
					netWorkType = "2";
					break;
				case TelephonyManager.NETWORK_TYPE_EDGE:
					netWorkType = "3";
					break;
				case TelephonyManager.NETWORK_TYPE_UMTS:
					netWorkType = "4";
					break;
				// case TelephonyManager.NETWORK_TYPE_HSDPA:
				// netWorkType = "5";
				// break;
				// case TelephonyManager.NETWORK_TYPE_HSUPA:
				// netWorkType = "6";
				// break;
				// case TelephonyManager.NETWORK_TYPE_HSPA:
				// netWorkType = "7";
				// break;
				case TelephonyManager.NETWORK_TYPE_CDMA:
					netWorkType = "8";
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
					netWorkType = "9";
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
					netWorkType = "10";
					break;
				case TelephonyManager.NETWORK_TYPE_1xRTT:
					netWorkType = "11";
				default:
					netWorkType = "-1";
				}

			}

		}
		return netWorkType;
	}

	public static int getNetType() {

		ConnectivityManager connectivityManager = (ConnectivityManager) AppController
				.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isAvailable()) {
			if (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
				return NETTYPE_WIFI;
			} else {
				TelephonyManager telephonyManager = (TelephonyManager) AppController
						.getInstance().getSystemService(
								Context.TELEPHONY_SERVICE);

				switch (telephonyManager.getNetworkType()) {
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_EDGE:
					return NETTYPE_2G;
				case TelephonyManager.NETWORK_TYPE_LTE:
					return NETTYPE_4G;
				default:
					return NETTYPE_3G;
				}
			}
		} else {
			return NETTYPE_NO;
		}
	}

	public static boolean getNetTypeForWo() {

		return getNetTypeForWo(false);
	}

	/**
	 * 是否为联通3G
	 * */
	public static boolean getNetTypeForWo(boolean isFirst) {

		ConnectivityManager connectivityManager = (ConnectivityManager) AppController
				.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isAvailable()) {
			if (ConnectivityManager.TYPE_WIFI == networkInfo.getType()) {
				return false;
			}
			TelephonyManager telephonyManager = (TelephonyManager) AppController
					.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
			L.i("king telephonyManager.getNetworkType() = "
					+ telephonyManager.getNetworkType());
			switch (telephonyManager.getNetworkType()) {
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				/*
				 * if(isFirst){ return true; }else { return
				 * PreferencesManager.getInstance().isChinaUnicomSwitch(); }
				 */
				return false;
			}
		}
		return false;
	}

	/**
	 * 获取网络类型（for WebView）
	 */
	public static String getNetTypeForView(Context context) {
		String type = "unknown";
		if (NetWorkTypeUtils.isWifi()) {
			type = "wifi";
		} else {
			int netGeneration = NetWorkTypeUtils.getNetGeneration(context);
			switch (netGeneration) {
			case NetWorkTypeUtils.NETTYPE_2G:
				type = "2G";
				break;
			case NetWorkTypeUtils.NETTYPE_3G:
				type = "3G";
				break;
			case NetWorkTypeUtils.NETTYPE_4G:
				type = "4G";
				break;
			case NetWorkTypeUtils.NETTYPE_UNKOWN:
				type = "unknown";
				break;
			case NetWorkTypeUtils.NETTYPE_NO:
				type = "none";
				break;
			}
		}
		return type;
	}

	/**
	 * 判断sim卡网络为几代网络
	 */
	public static int getNetGeneration(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			switch (telephonyManager.getNetworkType()) {
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return NETTYPE_2G;
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return NETTYPE_3G;
			case TelephonyManager.NETWORK_TYPE_LTE:

				return NETTYPE_4G;
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				return NETTYPE_UNKOWN;
			default:
				return NETTYPE_UNKOWN;
			}
		} else {
			return NETTYPE_NO;
		}
	}

	/**
	 * 判断为哪个运营商
	 */
	public static String getTelecomOperators(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		String imsi = "";
		if (networkInfo != null && networkInfo.isAvailable()) {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			imsi = telephonyManager.getSubscriberId();
		}
		if (!TextUtils.isEmpty(imsi)) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
				return China_Mobile;
			} else if (imsi.startsWith("46001")) {
				return China_Unicom;
			} else if (imsi.startsWith("46003")) {
				return China_Telecom;
			}
		}
		return China_Unkown;
	}
}
