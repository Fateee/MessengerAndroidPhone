//***************************************************************
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.dao.SettingsTbDao;
import com.yineng.ynmessager.util.TimeUtil;

/**
 * 消息提醒设置界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class MsgNotifySettingActivity extends BaseActivity implements OnClickListener
{
	private ImageButton mImgb_previous; // 左上角返回按钮
	private CheckBox mChk_distractionFree; // [免打扰]功能开关的勾选框
	private TextView mTxt_timeSetting; // 免打扰时间段设置按钮
	private TextView mTxt_timeSettingDisplay; // 免打扰时间段显示用的TextView
	private TextView mTxt_audio; // 打开[声音]设置界面的按钮
	private TextView mTxt_groupMsgNotifySetting;  //群讨论组消息设置按钮
	private CheckBox mChk_notifyWhenExit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_notify_setting);

		initViews();
	}
	

	@Override
	protected void onStart()
	{
		super.onStart();
		// 加载用户设置
		Setting setting = mApplication.mUserSetting;
		// 用户是否已经开启免打扰
		mChk_distractionFree.setChecked(setting.getDistractionFree() != 0);
		// 显示用户的免打扰时间段设置
		int hourBegin = setting.getDistractionFree_begin_h();
		int minBegin = setting.getDistractionFree_begin_m();
		int hourEnd = setting.getDistractionFree_end_h();
		int minEnd = setting.getDistractionFree_end_m();
		String[] betterDisplayBegin = TimeUtil.betterTimeDisplay(hourBegin,minBegin);
		String[] betterDisplayEnd = TimeUtil.betterTimeDisplay(hourEnd,minEnd);
		mTxt_timeSettingDisplay.setText(getString(R.string.msgNotifySetting_timeDisplay,betterDisplayBegin[0],
				betterDisplayBegin[1],betterDisplayEnd[0],betterDisplayEnd[1]));
		//加载是否退出仍然接收消息提醒
		mChk_notifyWhenExit.setChecked(setting.getReceiveWhenExit()!=0);
	}
	
	/**
	 * 初始化界面控件
	 */
	private void initViews()
	{
		mImgb_previous = (ImageButton)findViewById(R.id.msgNotifySetting_imgb_previous);
		mImgb_previous.setOnClickListener(this);
		mChk_distractionFree = (CheckBox)findViewById(R.id.msgNotifySetting_chk_distractionFree);
		mChk_distractionFree.setOnCheckedChangeListener(new DistractionFree_OnCheckedListener());
		mTxt_timeSetting = (TextView)findViewById(R.id.msgNotifySetting_txt_timeSetting);
		mTxt_timeSetting.setOnClickListener(this);
		mTxt_timeSettingDisplay = (TextView)findViewById(R.id.msgNotifySetting_txt_timeSettingDisplay);
		mTxt_audio = (TextView)findViewById(R.id.msgNotifySetting_txt_audio);
		mTxt_audio.setOnClickListener(this);
		mTxt_groupMsgNotifySetting = (TextView)findViewById(R.id.msgNotifySetting_groupMsgNotifySetting);
		mTxt_groupMsgNotifySetting.setOnClickListener(this);
		mChk_notifyWhenExit = (CheckBox)findViewById(R.id.msgNotifySetting_chk_notifyWhenExit);
		mChk_notifyWhenExit.setOnCheckedChangeListener(new NotifyWhenExit_OnCheckedListener());
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.msgNotifySetting_imgb_previous:
				finish();
				break;
			case R.id.msgNotifySetting_txt_timeSetting:
				startActivity(new Intent(this,DistractionFreeSettingActivity.class));
				break;
			case R.id.msgNotifySetting_txt_audio:
				startActivity(new Intent(this,AudioSettingActivity.class));
				break;
			case R.id.msgNotifySetting_groupMsgNotifySetting:
				startActivity(new Intent(this,GroupMsgNotifySettingActivity.class));
				break;
		}
	}

	/**
	 * 免打扰开关勾选框的监听器
	 * 
	 * @author 贺毅柳
	 * @category Listener
	 */
	private class DistractionFree_OnCheckedListener implements OnCheckedChangeListener
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			// 先修改Application中的Setting对象
			Setting setting = mApplication.mUserSetting;
			setting.setDistractionFree(isChecked ? 1 : 0);
			// 将Setting对象更新到数据库
			SettingsTbDao dao = mApplication.mSettingsTbDao;
			dao.update(setting);
			// 重写将Applicaton中的Setting对象从数据库中更新
			mApplication.mUserSetting = dao.obtainSettingFromDb();

			mTxt_timeSetting.setEnabled(isChecked);
			mTxt_timeSettingDisplay.setEnabled(isChecked);

		}

	}
	
	
	/**
	 * 退出后是否接受消息提醒的CheckBox勾选监听器
	 * @author 贺毅柳
	 *
	 */
	private class NotifyWhenExit_OnCheckedListener implements OnCheckedChangeListener
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			// 先修改Application中的Setting对象
			Setting setting = mApplication.mUserSetting;
			setting.setReceiveWhenExit(isChecked ? 1 : 0);
			// 将Setting对象更新到数据库
			SettingsTbDao dao = mApplication.mSettingsTbDao;
			dao.update(setting);
			// 重写将Applicaton中的Setting对象从数据库中更新
			mApplication.mUserSetting = dao.obtainSettingFromDb();

		}
		
	}

}
