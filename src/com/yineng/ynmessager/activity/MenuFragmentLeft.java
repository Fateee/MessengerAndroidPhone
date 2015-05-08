package com.yineng.ynmessager.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.settings.SettingActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

public class MenuFragmentLeft extends Fragment implements OnClickListener
{
	private static final String TAG = "MenuFragmentLeft";
	private Button mBtn_darkMode;
	private Button mBtn_mySettings;
	private Button mBtn_logout;

	/*
	 * @Override public void onAttach(Activity activity) { // TODO
	 * Auto-generated method stub super.onAttach(activity); callBack =
	 * (CallBack) getActivity(); }
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.main_menu_left_layout,null);
		mBtn_darkMode = (Button)view.findViewById(R.id.main_btn_darkMode);
		mBtn_mySettings = (Button)view.findViewById(R.id.main_btn_mySettings);
		mBtn_logout = (Button)view.findViewById(R.id.main_btn_logout);
		mBtn_darkMode.setOnClickListener(this);
		mBtn_mySettings.setOnClickListener(this);
		mBtn_logout.setOnClickListener(this);
		
		registerBroadcaseReceiver();
		
		return view;
	}
	
	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		unregisterBroadcaseReceiver();
	}
	
	private void registerBroadcaseReceiver()
	{
		IntentFilter filter = new IntentFilter(Const.BROADCAST_ACTION_USER_LOGOUT);
		getActivity().registerReceiver(mLogoutBroadCastReceiver,filter);
	}
	
	private void unregisterBroadcaseReceiver()
	{
		getActivity().unregisterReceiver(mLogoutBroadCastReceiver);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.main_btn_darkMode:
				LoginUser loginUser = AppController.getInstance().mLoginUser;
				switch(loginUser.getTheme())
				{
					case R.style.AppTheme_Light:
						loginUser.setTheme(R.style.AppTheme_Dark);
						break;
					case R.style.AppTheme_Dark:
						loginUser.setTheme(R.style.AppTheme_Light);
						break;
				}
				switchTheme();
				break;
			case R.id.main_btn_mySettings:
				startActivity(new Intent(getActivity(),SettingActivity.class));
				break;
			case R.id.main_btn_logout:
				logout();
				break;
		}
	}
	
	private void logout()
	{
		LastLoginUserSP lastUser = LastLoginUserSP.getInstance(getActivity());
		lastUser.saveUserPassword("");
		XmppConnectionManager.getInstance().doExistThread();
		Intent intent = new Intent(getActivity(),LoginActivity.class);
		startActivity(intent);
		close();
	}

	private void close()
	{
		Intent serviceIntent = new Intent(this.getActivity(),XmppConnService.class);
		this.getActivity().stopService(serviceIntent);
		if(getActivity() instanceof MainActivity)
		{
			MainActivity activity = (MainActivity)getActivity();
			activity.finish();
		}
	}

	/**
	 * 切换主题（重新启动MainActivity来加载Style）
	 */
	private void switchTheme()
	{
		Activity parent = getActivity();
		Intent intent = parent.getIntent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		parent.finish();
		parent.overridePendingTransition(0,0);
		startActivity(intent);
	}

	/**
	 * 接收用户从设置中点击退出登陆广播并处理的广播接收器
	 */
	private BroadcastReceiver mLogoutBroadCastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if(Const.BROADCAST_ACTION_USER_LOGOUT.equals(action))
			{
				logout();
			}
		}
	};
}
