//***************************************************************
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.dao.SettingsTbDao;
import com.yineng.ynmessager.util.TimeUtil;

/**
 * 免打扰设置界面
 * 
 * @category Activity
 * @author 贺毅柳
 * 
 */
public class DistractionFreeSettingActivity extends BaseActivity implements OnClickListener
{

	private ImageButton mImgb_previous; // 左上角[返回]按钮
	private TextView mTxt_beginTime; // [开始时间]设置按钮
	private TextView mTxt_beginTimeDisplay; // 用于显示已设置的开始时间的TextView
	private TextView mTxt_endTime; // [结束时间]设置按钮
	private TextView mTxt_endTimeDisplay; // 用于显示已设置的开始时间的TextView
	private TimePickerDialog mBeginTimeDialog; // 设置开始时间的TimePickerDialog对话框
	private TimePickerDialog mEndTimeDialog; // 设置结束时间的TimePickerDialog对话框

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_distraction_free_setting);

		initViews();
	}

	/**
	 * 初始化界面组件
	 */
	private void initViews()
	{
		mImgb_previous = (ImageButton)findViewById(R.id.distractionFreeSetting_imgb_previous);
		mImgb_previous.setOnClickListener(this);
		mTxt_beginTime = (TextView)findViewById(R.id.distractionFreeSetting_txt_beginTime);
		mTxt_beginTime.setOnClickListener(this);
		mTxt_beginTimeDisplay = (TextView)findViewById(R.id.distractionFreeSetting_txt_beginTimeDisplay);
		mTxt_endTime = (TextView)findViewById(R.id.distractionFreeSetting_txt_endTime);
		mTxt_endTime.setOnClickListener(this);
		mTxt_endTimeDisplay = (TextView)findViewById(R.id.distractionFreeSetting_txt_endTimeDisplay);

		Setting setting = AppController.getInstance().mUserSetting;

		// 初始化TimePickerDialog
		mBeginTimeDialog = new TimePickerDialog(this,new BeginTimeDialog_OnTimeSetListener(),
				setting.getDistractionFree_begin_h(),setting.getDistractionFree_begin_m(),true);
		mBeginTimeDialog.setTitle(R.string.distractionFreeSetting_beginTime);
		// 初始化TimePickerDialog
		mEndTimeDialog = new TimePickerDialog(this,new EndTimeDialog_OnTimeSetListener(),
				setting.getDistractionFree_end_h(),setting.getDistractionFree_end_m(),true);
		mEndTimeDialog.setTitle(R.string.distractionFreeSetting_endTime);

		//初始化UI显示
		String[] begin = TimeUtil.betterTimeDisplay(setting.getDistractionFree_begin_h(),
				setting.getDistractionFree_begin_m());
		String[] end = TimeUtil
				.betterTimeDisplay(setting.getDistractionFree_end_h(),setting.getDistractionFree_end_m());
		mTxt_beginTimeDisplay.setText(getString(R.string.distractionFreeSetting_beginTimeDisplay,begin[0],begin[1]));
		mTxt_endTimeDisplay.setText(getString(R.string.distractionFreeSetting_endTimeDisplay,end[0],end[1]));
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.distractionFreeSetting_imgb_previous:
				finish();
				break;
			case R.id.distractionFreeSetting_txt_beginTime:
				mBeginTimeDialog.show();
				break;
			case R.id.distractionFreeSetting_txt_endTime:
				mEndTimeDialog.show();
				break;
		}

	}

	/**
	 * 设置开始时间的TimePickerDialog对话框的监听器
	 * 
	 * @category Listener
	 * @author 贺毅柳
	 * 
	 */
	class BeginTimeDialog_OnTimeSetListener implements OnTimeSetListener
	{
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute)
		{
			// 修改Application中的Setting
			Setting setting = AppController.getInstance().mUserSetting;
			setting.setDistractionFree_begin_h(hourOfDay);
			setting.setDistractionFree_begin_m(minute);
			// 更新到数据库
			SettingsTbDao dao = AppController.getInstance().mSettingsTbDao;
			dao.update(setting);
			// 从数据库更新Application中的Setting
			AppController.getInstance().mUserSetting = dao.obtainSettingFromDb();
			// 更新UI
			String[] betterTime = TimeUtil.betterTimeDisplay(hourOfDay,minute);
			mTxt_beginTimeDisplay.setText(getString(R.string.distractionFreeSetting_beginTimeDisplay,betterTime[0],
					betterTime[1]));
		}
	}

	/**
	 * 设置结束时间的TimePickerDialog对话框的监听器
	 * 
	 * @category Listener
	 * @author 贺毅柳
	 * 
	 */
	class EndTimeDialog_OnTimeSetListener implements OnTimeSetListener
	{

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute)
		{
			// 修改Application中的Setting
			Setting setting = AppController.getInstance().mUserSetting;
			setting.setDistractionFree_end_h(hourOfDay);
			setting.setDistractionFree_end_m(minute);
			// 更新到数据库
			SettingsTbDao dao = AppController.getInstance().mSettingsTbDao;
			dao.update(setting);
			// 从数据库更新Application中的Setting
			AppController.getInstance().mUserSetting = dao.obtainSettingFromDb();
			// 更新UI
			String[] betterTime = TimeUtil.betterTimeDisplay(hourOfDay,minute);
			mTxt_endTimeDisplay.setText(getString(R.string.distractionFreeSetting_endTimeDisplay,betterTime[0],
					betterTime[1]));
		}

	}

}
