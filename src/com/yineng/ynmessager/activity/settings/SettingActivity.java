//***************************************************************
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.LoginActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 我的设置 界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class SettingActivity extends BaseActivity implements OnClickListener
{
	private ImageButton mImgb_previous; // 左上角返回按钮
	private TextView mTxt_msgNotifySetting; // 打开[消息提醒]设置界面的按钮
	private TextView mTxt_viewSetting; // 打开[浏览设置]界面的按钮
	private TextView mTxt_additionalFunction; // 打开[辅助功能]设置界面的按钮
	private TextView mTxt_about; // 打开[关于]的按钮
	private TextView mTxt_logout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		initViews();
	}

	/**
	 * 初始化界面控件
	 */
	private void initViews()
	{
		mImgb_previous = (ImageButton)findViewById(R.id.setting_imgb_previous);
		mImgb_previous.setOnClickListener(this);
		mTxt_msgNotifySetting = (TextView)findViewById(R.id.setting_txt_msgNotifySetting);
		mTxt_msgNotifySetting.setOnClickListener(this);
		mTxt_viewSetting = (TextView)findViewById(R.id.setting_txt_viewSetting);
		mTxt_viewSetting.setOnClickListener(this);
		mTxt_additionalFunction = (TextView)findViewById(R.id.setting_txt_additionalFunction);
		mTxt_additionalFunction.setOnClickListener(this);
		mTxt_about = (TextView)findViewById(R.id.setting_txt_about);
		mTxt_about.setOnClickListener(this);
		mTxt_logout = (TextView)findViewById(R.id.setting_txt_logout);
		mTxt_logout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.setting_imgb_previous:
				finish();
				break;
			case R.id.setting_txt_msgNotifySetting:
				startActivity(new Intent(this,MsgNotifySettingActivity.class));
				break;
			case R.id.setting_txt_viewSetting:
				startActivity(new Intent(this,ViewSettingActivity.class));
				break;
			case R.id.setting_txt_additionalFunction:
				startActivity(new Intent(this,AdditionalFunctionActivity.class));
				break;
			case R.id.setting_txt_about:
				startActivity(new Intent(this,AboutActivity.class));
				break;
			case R.id.setting_txt_logout:
				finish();
				sendBroadcast(new Intent(Const.BROADCAST_ACTION_USER_LOGOUT));
				break;
		}
	}

}
