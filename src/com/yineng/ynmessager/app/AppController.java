package com.yineng.ynmessager.app;

import com.yineng.ynmessager.bean.contact.SelfUser;
import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.dao.SettingsTbDao;

import android.app.Application;
import android.util.Log;

/**
 * 绑定的Application
 * 
 * @author 贺毅柳
 * @see android.app.Application
 */
public class AppController extends Application
{
	private static final String TAG = "AppController";

	private static AppController sInstance; // 当前Application的唯一实例
	public LoginUser mLoginUser; // 当前登陆的用户对象
	public SelfUser mSelfUser; // 我的详细信息
	public SettingsTbDao mSettingsTbDao;  //用来读写Setting表的DAO，只有进入MainActivity后才会被初始化
	public Setting mUserSetting;  //当前登陆用户的设置信息
	
	@Override
	public void onCreate()
	{
		super.onCreate();

		sInstance = this;
		int runtimeMemory = (int)(Runtime.getRuntime().maxMemory() / (1024 * 1024));
		Log.i(TAG,"分配的程序运行时内存大小：" + runtimeMemory + "MB");
	}

	/**
	 * 获取Application实例
	 * 
	 * @return 唯一的实例
	 */
	public static synchronized AppController getInstance()
	{
		return sInstance;
	}

}
