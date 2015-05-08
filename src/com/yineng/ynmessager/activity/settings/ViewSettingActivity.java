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
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.dao.SettingsTbDao;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * 浏览设置界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class ViewSettingActivity extends BaseActivity implements OnClickListener, OnCheckedChangeListener
{
	private ImageButton mImgb_previous; // 左上角返回按钮
	private TextView mTxt_fontSize; // 字体大小按钮
	private TextView mTxt_backgroundSkin;
	private ToggleButton mTogb_autoRecImg;
	private SettingsTbDao mSettingsTbDao = AppController.getInstance().mSettingsTbDao;;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_setting);

		initViews();
	}

	/**
	 * 初始化界面控件
	 */
	private void initViews()
	{
		mImgb_previous = (ImageButton)findViewById(R.id.viewSetting_imgb_previous);
		mImgb_previous.setOnClickListener(this);
		mTxt_fontSize = (TextView)findViewById(R.id.viewSetting_txt_fontSize);
		mTxt_fontSize.setOnClickListener(this);
		mTxt_backgroundSkin = (TextView)findViewById(R.id.viewSetting_txt_backgroundSkin);
		mTxt_backgroundSkin.setOnClickListener(this);
		mTogb_autoRecImg = (ToggleButton)findViewById(R.id.viewSetting_togb_autoRecImg);
		mTogb_autoRecImg.setOnCheckedChangeListener(this);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		Setting setting = AppController.getInstance().mUserSetting;
		
		mTogb_autoRecImg.setChecked(setting.getAlwaysAutoReceiveImg() != 0);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.viewSetting_imgb_previous: // 返回
				finish();
				break;
			case R.id.viewSetting_txt_fontSize: // 字体大小
				// startActivity(new
				// Intent(this,FontSizeSettingActivity.class));
				showToast("此功能开发中，敬请期待……");
				break;
			case R.id.viewSetting_txt_backgroundSkin: // 背景图片
				showToast("此功能开发中，敬请期待……");
				break;
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		int id = buttonView.getId();
		switch(id)
		{
			case R.id.viewSetting_togb_autoRecImg:
				// 改变Application中的Settings对象
				Setting setting = AppController.getInstance().mUserSetting;
				setting.setAlwaysAutoReceiveImg(isChecked ? 1 : 0);
				// 然后更新到数据库
				mSettingsTbDao.update(setting);
				// 再从数据库中加载到Application的Setting
				AppController.getInstance().mUserSetting = mSettingsTbDao.obtainSettingFromDb();
				break;
		}
	}
}
