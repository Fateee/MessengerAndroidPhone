package com.yineng.ynmessager.sharedpreference;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * @author YINENG
 * @Date 2014-12-29 保存当前用户的登陆信息
 * 
 */
public class LastLoginUserSP {
	private SharedPreferences mSharedPreferences;// 成员sp
	private SharedPreferences.Editor mEditor;// 成员Editor
	private static LastLoginUserSP lastLoginUserSP;// 类变量

	/**
	 * @param context
	 *            私有构造函数
	 */
	private LastLoginUserSP(Context context) {
		mSharedPreferences = context.getApplicationContext()
				.getSharedPreferences("lastusersp", Activity.MODE_PRIVATE);
		mEditor = this.mSharedPreferences.edit();
	}

	/**
	 * @param context
	 * @return 懒汉式单例方法
	 */
	public static synchronized LastLoginUserSP getInstance(Context context) {
		if (lastLoginUserSP == null) {
			lastLoginUserSP = new LastLoginUserSP(context);
		}
		return lastLoginUserSP;
	}

	/**
	 * @return 获取用户昵称
	 */
	public String getUserName() {
		return mSharedPreferences.getString("lastLoginUser_name", "");
	}

	/**
	 * @param name
	 *            保存用户昵称
	 */
	public void saveUserName(String name) {
		mEditor.putString("lastLoginUser_name", name);
		mEditor.commit();
	}

	/**
	 * @return获取用户登陆账号
	 */
	public String getUserAccount() {
		return mSharedPreferences.getString("lastLoginUser_account", "");
	}

	/**
	 * @param number
	 *            保存用户登陆账号
	 */
	public void saveUserAccount(String number) {
		mEditor.putString("lastLoginUser_account", number);
		mEditor.commit();
	}

	/**
	 * @return 获取用户登陆密码
	 */
	public String getUserPassword() {
		return mSharedPreferences.getString("lastLoginUser_password", null);

	}

	/**
	 * @param password
	 *            保存用户登陆密码
	 */
	public void saveUserPassword(String password) {
		mEditor.putString("lastLoginUser_password", password);
		mEditor.commit();
	}

	/**
	 * @param saddress
	 *            保存登陆地址
	 */
	public void saveUserServicesAddress(final String saddress) {
		mEditor.putString("lastLoginUser_saddress", saddress);
		mEditor.commit();
	}

	/**
	 * @return 获取登陆地址
	 */
	public String getUserServicesAddress() {
		return mSharedPreferences.getString("lastLoginUser_saddress", "");
	}

	public void clearAllUserInfo() {
		mEditor.clear();
		mEditor.commit();
	}

	/**
	 * @param accountKey 是否首次登陆
	 * @return
	 */
	public boolean isFirstLogin(String accountKey) {

		return mSharedPreferences.getBoolean(accountKey, true);
	}

	/**
	 * @param accountKey
	 * @param isFirstLogin 设置是否首次登陆
	 */
	public void setIsFirstLogin(String accountKey, boolean isFirstLogin) {

		mEditor.putBoolean(accountKey, isFirstLogin);
		mEditor.commit();
	}

	public boolean isExistsUser() {
		if (!TextUtils.isEmpty(this.getUserAccount())
				&& !TextUtils.isEmpty(this.getUserPassword())
				&& !TextUtils.isEmpty(this.getUserServicesAddress())) {

			return true;

		} else {

			return false;
		}
	}
}
