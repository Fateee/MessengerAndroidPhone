package com.yineng.ynmessager.activity;

import java.util.Iterator;
import java.util.LinkedList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IdPastListener;
import com.yineng.ynmessager.receiver.CommonReceiver.netWorkChangedListener;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.StatusChangedCallBack;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.ToastUtil;

/**
 * 所有Activity都应从此类继承
 * 
 * @author 贺毅柳
 * 
 */
public abstract class BaseActivity extends FragmentActivity implements StatusChangedCallBack
{
	private static final String BASE_TAG = "BaseActivity"; // BaseActivity的自己TAG
	protected final String TAG = this.getClass().getSimpleName(); // 每个Activity的TAG
	protected final AppController mApplication = AppController.getInstance(); // Application
	private static final LinkedList<Activity> mActivityList = new LinkedList<Activity>(); // 用来储存当前运行中的Activity实例的List
	private int mCurrentStats = 0;

	private Handler mHandler = new Handler() {
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case LoginThread.USER_STATUS_LOGINED_OTHER:
					mOfflineNoticeDialog.show();
					break;
				default:
					break;
			}
		}
	};

	/**
	 * 下线通知对话框
	 */
	private Builder mOfflineNoticeDialog;
	/**
	 * 网络状态监听器
	 */
	public CommonReceiver mNetWorkChangedReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mActivityList.add(this);
		L.v(BASE_TAG,"启动并添加了Activity：" + TAG);

		/*
		 * // 检测网络是否可用 mIsNetworkAvailable = NetWorkUtil
		 * .isNetworkAvailable(getApplicationContext()); // 注册网络监听器
		 * registerNetChangeReceiver();
		 */
		initIdPastDialog();
		initOfflineNoticeDialog();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter netWorkChangedFilter;
		/***** 添加用户状态监听器 *****/
		XmppConnectionManager.getInstance().addStatusChangedCallBack(this);
		//		mNetWorkChangedReceiver = new CommonReceiver();
//		IntentFilter netWorkChangedFilter = new IntentFilter(Const.BROADCAST_ACTION_ID_PAST);
//		mNetWorkChangedReceiver.setIdPastListener(new IdPastListener() {
//			
//			@Override
//			public void idPasted() {
//				L.e("mIdPastDialog.isShowing() == "+mIdPastDialog.isShowing());
//				if (!mIdPastDialog.isShowing()) {
//					mIdPastDialog.show();
//				}
//			}
//		});
		if (mActivityList.getLast().getClass().getSimpleName().equals("SplashActivity") ||
				mActivityList.getLast().getClass().getSimpleName().equals("LoginActivity")) {
			return;
		} else {
			mNetWorkChangedReceiver = new CommonReceiver();
			netWorkChangedFilter = new IntentFilter(Const.BROADCAST_ACTION_ID_PAST);
			netWorkChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
			mNetWorkChangedReceiver.setNetWorkChangedListener(new netWorkChangedListener() {
				
				@Override
				public void netWorkChanged(String netWorkInfo) {
					// TODO Auto-generated method stub
					if (netWorkInfo.equals("none")) {
						ToastUtil.toastAlerMessageCenter(BaseActivity.this, "当前无网络", 1000);
					} else {
						ToastUtil.toastAlerMessageCenter(BaseActivity.this, "您当前在使用"+netWorkInfo+"网络", 1000);
					}
				}
			});
		}
		registerReceiver(mNetWorkChangedReceiver, netWorkChangedFilter);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		/***** 删除用户状态监听器 *****/
		XmppConnectionManager.getInstance().removeStatusChangedCallBack(this);
		if (mNetWorkChangedReceiver != null) {
			unregisterReceiver(mNetWorkChangedReceiver);
		}
		if (mIdPastDialog!=null&&mIdPastDialog.isShowing()) {
			mIdPastDialog.dismiss();
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mActivityList.remove(this);
		L.v(BASE_TAG,"关闭并移除了Activity：" + TAG);
	}

	/**
	 * 下线通知对话框
	 */
	private void initOfflineNoticeDialog()
	{
		mOfflineNoticeDialog = new AlertDialog.Builder(BaseActivity.this);
		mOfflineNoticeDialog.setTitle(R.string.common_offline_notice_title);
		mOfflineNoticeDialog.setMessage(R.string.common_offline_notice_msg);
		mOfflineNoticeDialog.setCancelable(false);
		mOfflineNoticeDialog.setPositiveButton("重新登录",new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(android.content.DialogInterface dialog, int which)
			{
				if(NetWorkUtil.isNetworkAvailable(BaseActivity.this))
				{// 如果网络可用自动登陆
					Intent serviceIntent = new Intent(BaseActivity.this,XmppConnService.class);
					startService(serviceIntent);// 开启服务，自动登陆
				}
			}
		}).setNegativeButton("退出",new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(android.content.DialogInterface dialog, int which)
			{
				logOutAndCloseApp(true);
			}
		});
	}

	/**
	 * 注销账户，停止服务，关闭应用
	 * @param closeApp 是否关闭app;
	 * 	 true 关闭;false不关
	 */
	protected void logOutAndCloseApp(boolean closeApp) {
		// 注销并退出app
		LastLoginUserSP lastUser = LastLoginUserSP.getInstance(BaseActivity.this);
		lastUser.saveUserPassword("");
		XmppConnectionManager.getInstance().doExistThread();
		stopServiceAndCloseApp(closeApp);
	}

	/**
	 * 关闭服务，返回登录界面或退出应用程序
	 * @param closeApp 是否退出APP
	 */ 
	private void stopServiceAndCloseApp(boolean closeApp)
	{
		Intent serviceIntent = new Intent(BaseActivity.this,XmppConnService.class);
		stopService(serviceIntent);
		if (closeApp) {
			exit();
		} else {
			// 清理Activity
			Iterator<Activity> iterator = mActivityList.iterator();
			Activity activity;
			while(iterator.hasNext())
			{
				activity = iterator.next();
				if(activity != null)
				{
					activity.finish();
				}
			}
			Intent intent = new Intent(BaseActivity.this,LoginActivity.class);
			startActivity(intent);
		}
		
	}

	@Override
	public void onStatusChanged(int status)
	{
		if(mCurrentStats != status)
		{
			mHandler.sendEmptyMessage(status);
			mCurrentStats = status;
		}
	}

	/*
	 * @Override public void setContentView(int layoutResID) {
	 * super.setContentView(layoutResID); if (!mIsNetworkAvailable) {
	 * networkStateChanged(mIsNetworkAvailable); } }
	 * 
	 * @Override public void setContentView(View view) {
	 * super.setContentView(view); if (!mIsNetworkAvailable) {
	 * networkStateChanged(mIsNetworkAvailable); } }
	 * 
	 * @Override public void setContentView(View view, LayoutParams params) {
	 * super.setContentView(view, params); if (!mIsNetworkAvailable) {
	 * networkStateChanged(mIsNetworkAvailable); } }
	 */

	/**
	 * 显示Toast
	 * 
	 * @param text
	 *            要显示的字符串
	 */
	protected void showToast(CharSequence text)
	{
		Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
	}

	/**
	 * 根据格式化的字符串来显示Toast
	 * 
	 * @param text
	 *            要显示的格式化字符串
	 * @param args
	 *            格式化参数
	 */
	protected void showToast(String text, Object... args)
	{
		showToast(String.format(text,args));
	}

	/**
	 * 从R.string中显示字符串
	 * 
	 * @param resId
	 *            字符串的资源ID
	 */
	protected void showToast(int resId)
	{
		Toast.makeText(this,resId,Toast.LENGTH_SHORT).show();
	}

	/**
	 * 从R.string中显示字符串
	 * 
	 * @param resId
	 *            字符串的资源ID
	 * @param args
	 *            格式化参数
	 */
	protected void showToast(int resId, Object... args)
	{
		Toast.makeText(this,getString(resId,args),Toast.LENGTH_SHORT).show();
	}

	/**
	 * 退出时调用此方法
	 */
	protected void exit()
	{
		// 清理Activity
		Iterator<Activity> iterator = mActivityList.iterator();
		Activity activity;
		while(iterator.hasNext())
		{
			activity = iterator.next();
			if(activity != null)
			{
				activity.finish();
			}
			L.d(BASE_TAG,"释放了" + activity.getClass().getSimpleName());
		}
		L.d(BASE_TAG,"已释放所有存在的Activity");

		// 退出
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}

	/*
	 * private void registerNetChangeReceiver() { if (mNetChangeReceiver !=
	 * null) { unregisterNetChangeReceiver(); } mNetChangeReceiver = new
	 * NetChangeReceiver(); registerReceiver(mNetChangeReceiver, new
	 * IntentFilter( ConnectivityManager.CONNECTIVITY_ACTION)); }
	 * 
	 * private void unregisterNetChangeReceiver() { if (mNetChangeReceiver !=
	 * null) { unregisterReceiver(mNetChangeReceiver); mNetChangeReceiver =
	 * null; } }
	 * 
	 * class NetChangeReceiver extends BroadcastReceiver {
	 *//**
	 * 
	 * <p>
	 * Title: onReceive
	 * </p>
	 * <p>
	 * Description: 当网络改变时接收的方法
	 * </p>
	 * 
	 * @param context
	 * @param intent
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
	 *      android.content.Intent)
	 */
	/*
	 * 
	 * @Override public void onReceive(Context context, Intent intent) {
	 * mIsNetworkAvailable = NetWorkUtil
	 * .isNetworkAvailable(getApplicationContext());
	 * networkStateChanged(mIsNetworkAvailable); } }
	 * 
	 * public void networkStateChanged(boolean isNetAvailable) {
	 * 
	 * }
	 */
	/**
	 * 身份验证过期对话框
	 */
	private void initIdPastDialog()
	{
		Builder tempIdPastDialog = new AlertDialog.Builder(BaseActivity.this);
		tempIdPastDialog.setTitle(R.string.common_dialog_title);
		tempIdPastDialog.setMessage(R.string.common_id_past_msg);
		tempIdPastDialog.setCancelable(false);
		tempIdPastDialog.setPositiveButton("确定",new android.content.DialogInterface.OnClickListener() {

			@Override
			public void onClick(android.content.DialogInterface dialog, int which)
			{
				logOutAndCloseApp(false);
			}
		});
		mIdPastDialog = tempIdPastDialog.create();
	}
	/**
	 * 身份验证过期对话框
	 */
	public AlertDialog mIdPastDialog;
}
