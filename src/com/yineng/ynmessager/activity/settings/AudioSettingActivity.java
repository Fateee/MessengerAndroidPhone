//***************************************************************
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

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

/**
 * 声音设置界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class AudioSettingActivity extends BaseActivity implements OnClickListener
{
	private ImageButton mImgb_previous; // 左上角[返回]按钮
	private CheckBox mChk_isAlarm; // [声音]开关勾选框
	private TextView mTxt_groupIsAlarm; // [群讨论组声音]显示TextView
	private CheckBox mChk_groupIsAlarm; // [群讨论组声音]开关勾选框
	private CheckBox mChk_isVibrate; // [震动]开关勾选框
	private TextView mTxt_groupIsVibrate; // [群讨论组震动]显示TextView
	private CheckBox mChk_groupIsVibrate; // [群讨论组震动]开关勾选框
	private SettingsTbDao mDao = mApplication.mSettingsTbDao; // Setting表DAO实例

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_setting);

		initViews();
	}

	/**
	 * 初始化界面控件
	 */
	private void initViews()
	{
		mImgb_previous = (ImageButton)findViewById(R.id.audioSetting_imgb_previous);
		mImgb_previous.setOnClickListener(this);
		IsAlarm_OnCheckListener isAlarm_onCheckListener = new IsAlarm_OnCheckListener();
		mChk_isAlarm = (CheckBox)findViewById(R.id.audioSetting_chk_isAlarm);
		mChk_isAlarm.setOnCheckedChangeListener(isAlarm_onCheckListener);
		mTxt_groupIsAlarm = (TextView)findViewById(R.id.audioSetting_txt_groupIsAlarm);
		mChk_groupIsAlarm = (CheckBox)findViewById(R.id.audioSetting_chk_groupIsAlarm);
		mChk_groupIsAlarm.setOnCheckedChangeListener(isAlarm_onCheckListener);

		IsVibrate_OnCheckListener isVibrate_onCheckListener = new IsVibrate_OnCheckListener();
		mChk_isVibrate = (CheckBox)findViewById(R.id.audioSetting_chk_isVibrate);
		mChk_isVibrate.setOnCheckedChangeListener(isVibrate_onCheckListener);
		mTxt_groupIsVibrate = (TextView)findViewById(R.id.audioSetting_txt_groupIsVibrate);
		mChk_groupIsVibrate = (CheckBox)findViewById(R.id.audioSetting_chk_groupIsVibrate);
		mChk_groupIsVibrate.setOnCheckedChangeListener(isVibrate_onCheckListener);
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		Setting setting = mApplication.mUserSetting;
		// 设置勾选
		mChk_isAlarm.setChecked(setting.getAudio() != 0);
		mChk_groupIsAlarm.setChecked(setting.getAudio_group() != 0);
		mChk_isVibrate.setChecked(setting.getVibrate() != 0);
		mChk_groupIsVibrate.setChecked(setting.getVibrate_group() != 0);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.audioSetting_imgb_previous:
				finish();
				break;
		}

	}

	/**
	 * 声音、群讨论组声音的勾选监听器
	 * 
	 * @author 贺毅柳
	 * @category Listener
	 */
	private class IsAlarm_OnCheckListener implements OnCheckedChangeListener
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			Setting setting = mApplication.mUserSetting;
			int id = buttonView.getId();
			switch(id)
			{
				case R.id.audioSetting_chk_isAlarm:
					setting.setAudio(isChecked ? 1 : 0);
					mTxt_groupIsAlarm.setEnabled(isChecked);
					mChk_groupIsAlarm.setChecked(false);
					mChk_groupIsAlarm.setEnabled(isChecked);
					break;
				case R.id.audioSetting_chk_groupIsAlarm:
					setting.setAudio_group(isChecked ? 1 : 0);
					break;
			}
			mDao.update(setting);
			mApplication.mUserSetting = mDao.obtainSettingFromDb();
		}
	}

	/**
	 * 震动、群讨论组震动的勾选监听器
	 * 
	 * @author 贺毅柳
	 * @category Listener
	 */
	private class IsVibrate_OnCheckListener implements OnCheckedChangeListener
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			Setting setting = mApplication.mUserSetting;
			int id = buttonView.getId();
			switch(id)
			{
				case R.id.audioSetting_chk_isVibrate:
					setting.setVibrate(isChecked ? 1 : 0);
					mTxt_groupIsVibrate.setEnabled(isChecked);
					mChk_groupIsVibrate.setChecked(false);
					mChk_groupIsVibrate.setEnabled(isChecked);
					break;
				case R.id.audioSetting_chk_groupIsVibrate:
					setting.setVibrate_group(isChecked ? 1 : 0);
					break;
			}
			mDao.update(setting);
			mApplication.mUserSetting = mDao.obtainSettingFromDb();
		}
	}
}
