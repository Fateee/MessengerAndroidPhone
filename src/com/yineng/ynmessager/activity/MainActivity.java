package com.yineng.ynmessager.activity;

import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.slidingmenu.SlidingFragmentActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.SettingsTb;
import com.yineng.ynmessager.db.dao.LoginUserDao;
import com.yineng.ynmessager.db.dao.SettingsTbDao;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IdPastListener;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.slidingmenu.SlidingMenu;
import com.yineng.ynmessager.view.slidingmenu.SlidingMenu.CanvasTransformer;

public class MainActivity extends SlidingFragmentActivity
{
	private CanvasTransformer mTransformer;
	private CanvasTransformer mTransformerMain;

	/* 退出的间隔时间 */
	private static final long EXIT_INTERVAL_TIME = 2000;

	/**
	 * 触碰返回键时间
	 */
	private long mBackKeyTouchTime = 0;
	private PopupWindow mMenuWindow;
	/**
	 * 身份验证过期的广播
	 */
	private CommonReceiver mIdPastedReceiver;

	public MainActivity()
	{
		mTransformer = new CanvasTransformer() {
			@Override
			public void transformCanvas(Canvas canvas, float percentOpen)
			{
				float scale = (float)(percentOpen * 0.25 + 0.75);
				canvas.scale(scale,scale,canvas.getWidth() / 2,canvas.getHeight() / 2);
			}
		};
		mTransformerMain = new CanvasTransformer() {
			@Override
			public void transformCanvas(Canvas canvas, float percentOpen)
			{
				float scale = (float)(1 - percentOpen * 0.25);
				canvas.scale(scale,scale,canvas.getWidth() / 2,canvas.getHeight() / 2);
			}
		};
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//注册身份过期的广播
		initIdPastDueBroadcast();
		super.onCreate(savedInstanceState);
		// 获取最后登录的用户名
		String loginUserAccount = LastLoginUserSP.getInstance(getApplicationContext()).getUserAccount();
		if(AppController.getInstance().mLoginUser == null)
		{
			// 从数据库取得当前登陆的用户对象，保存到Application当中
			LoginUserDao loginUserDao = new LoginUserDao(getApplicationContext());
			AppController.getInstance().mLoginUser = loginUserDao.getLoginUserByAccount(loginUserAccount);
		}
		if(AppController.getInstance().mSelfUser == null)
		{
			// 全局初始化当前登录的用户信息
			ContactOrgDao mContactOrgDao = new ContactOrgDao(getApplicationContext());
			AppController.getInstance().mSelfUser = mContactOrgDao.queryUserInfoByUserNo(loginUserAccount);
		}

		initSettings(loginUserAccount); // 初始化读取用户的数据库保存的设置信息

		initTheme(); // 初始化主题

		initView();

		setBehindContentView(R.layout.main_menu_frame_left);
		setContentView(R.layout.main_content_frame);

		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		t.replace(R.id.menu_frame,new MenuFragmentLeft());
		t.commit();

		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame_two,new MenuFragmentRight()).commit();

		getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,new MainFragment()).commit();

		SlidingMenu sm = getSlidingMenu();
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);

		sm.setFadeDegree(0.9f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setSecondaryMenu(R.layout.main_menu_frame_right);
		setSlidingActionBarEnabled(true);
		sm.setBehindScrollScale(0.3f);
		sm.setBehindCanvasTransformer(mTransformer);
		sm.setAboveCanvasTransformer(mTransformerMain);
		sm.setMode(SlidingMenu.LEFT);
		// sm.setBackgroundColor(Color.GRAY);
		sm.setBackgroundImage(R.drawable.img_frame_background);

	}

	/**
	 * 注册身份过期的广播
	 */
	private void initIdPastDueBroadcast() {
		CommonReceiver.mNetWorkTypeStr = "";
		mIdPastedReceiver = new CommonReceiver();
		IntentFilter mIdPastedFilter = new IntentFilter(Const.BROADCAST_ACTION_ID_PAST);
		mIdPastedReceiver.setIdPastListener(new IdPastListener() {
			
			@Override
			public void idPasted() {
				if (!mIdPastDialog.isShowing()) {
					mIdPastDialog.show();
				}
			}
		});
		registerReceiver(mIdPastedReceiver, mIdPastedFilter);		
	}

	/**
	 * 读取用户的数据库保存的设置信息
	 */
	private void initSettings(String account)
	{
		AppController.getInstance().mSettingsTbDao = new SettingsTb(getApplicationContext(),account);
		SettingsTbDao dao = AppController.getInstance().mSettingsTbDao;
		long rowId = dao.insert(); // 尝试插入默认设置数据
		if(rowId != 0)
		{
			L.d(TAG,"初始化设置数据");
		}
		// 将设置数据库数据读到Application->Setting中
		AppController.getInstance().mUserSetting = dao.obtainSettingFromDb();
	}

	/**
	 * 根据数据库保存的信息来初始化加载主题
	 */
	private void initTheme()
	{
		setTheme(AppController.getInstance().mLoginUser.getTheme());
	}

	/**
	 * 初始化弹出框
	 */
	private void initView()
	{
		View mMenuPopView = LayoutInflater.from(MainActivity.this).inflate(R.layout.main_menu_popwindow,null);
		mMenuWindow = new PopupWindow(mMenuPopView,LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT,true);
	}

	/**
	 * 打开弹出框
	 */
	public void showPhonePopWindow()
	{
		mMenuWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
		mMenuWindow.showAtLocation(findViewById(R.id.content_frame),Gravity.BOTTOM,0,0);
		mMenuWindow.setAnimationStyle(R.style.AnimBottom);
		mMenuWindow.setOutsideTouchable(true);
		mMenuWindow.setFocusable(false);
		mMenuWindow.update();
	}

	/*
	 * (non-Javadoc) 键盘事件
	 * 
	 * @see android.support.v4.app.FragmentActivity#onKeyDown(int,
	 * android.view.KeyEvent)
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch(keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				long currentTime = System.currentTimeMillis();

				if((currentTime - mBackKeyTouchTime) >= EXIT_INTERVAL_TIME)
				{
					ToastUtil.toastAlerMessageBottom(MainActivity.this,"再按一次退出程序",1000);
					mBackKeyTouchTime = currentTime;
				}else
				{
					//如果设置中没有勾选“退出仍然接收消息”，则完全关闭所有界面以及进程
					if(AppController.getInstance().mUserSetting.getReceiveWhenExit() == 0)
					{
						super.exit();
					}else
					{
						finish();
					}
				}
				return false;
			case KeyEvent.KEYCODE_MENU:
				if(mMenuWindow != null)
				{
					if(mMenuWindow.isShowing())
					{
						mMenuWindow.dismiss();
					}else
					{
						showPhonePopWindow();
					}
				}
				break;
			default:
				break;
		}
		return true;
	}

	/**
	 * 界面监听事件
	 * 
	 * @param v
	 */
	public void onClickListener(View v)
	{
		switch(v.getId())
		{
			case R.id.tv_main_menu_popwindow_help:// /帮助反馈
				ToastUtil.toastAlerMessage(MainActivity.this,"敬请期待...",1000);
				break;
			case R.id.tv_main_menu_popwindow_exit:// 退出
				//如果设置中没有勾选“退出仍然接收消息”，则完全关闭所有界面以及进程
				if(AppController.getInstance().mUserSetting.getReceiveWhenExit() == 0)
				{
					super.exit();
				}else
				{
					finish();
				}
				break;
			default:
				break;
		}
		mMenuWindow.dismiss();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mIdPastDialog.isShowing()) {
			mIdPastDialog.dismiss();
		}
		unregisterReceiver(mIdPastedReceiver);
	}
	
}
