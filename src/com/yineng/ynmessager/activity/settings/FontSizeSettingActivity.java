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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;

/**
 * 字体大小设置界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class FontSizeSettingActivity extends BaseActivity implements OnClickListener
{

	private ImageButton mImgb_previous; // 左上角返回按钮
	private RadioButton[] mRadb_FontSize = new RadioButton[4]; // 用于单选字体大小的一组RadioButton

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_font_size_setting);

		initViews();
	}

	/**
	 * 初始化界面组件
	 */
	private void initViews()
	{
		mImgb_previous = (ImageButton)findViewById(R.id.fontSizeSetting_imgb_previous);
		mImgb_previous.setOnClickListener(this);

		mRadb_FontSize[0] = (RadioButton)findViewById(R.id.fontSize_radb_small);
		mRadb_FontSize[1] = (RadioButton)findViewById(R.id.fontSize_radb_medium);
		mRadb_FontSize[2] = (RadioButton)findViewById(R.id.fontSize_radb_huge);
		mRadb_FontSize[3] = (RadioButton)findViewById(R.id.fontSize_radb_xhuge);
		// 给每个RadioButton都设置Listener
		FontSize_OnCheckedListener listener = new FontSize_OnCheckedListener();
		for(RadioButton button : mRadb_FontSize)
		{
			button.setOnCheckedChangeListener(listener);
		}
		// 默认显示设置字体大小为中（以后应该是根据数据库中保存的配置来显示默认）
		mRadb_FontSize[1].setChecked(true);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();

		switch(id)
		{
			case R.id.fontSizeSetting_imgb_previous:
				finish();
				break;
		}
	}

	/**
	 * 所有字体大小RadioButton选择的监听器<br/>
	 * 界面效果：当其中一个RadioButton被点击选择后，其他RadioButton会被设置为未选中
	 * 
	 * @author 贺毅柳
	 * @category Listener
	 */
	class FontSize_OnCheckedListener implements OnCheckedChangeListener
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if(isChecked)
			{
				for(CompoundButton button : mRadb_FontSize)
				{
					if(buttonView != button)
					{
						button.setChecked(false);
					}
				}
			}
		}

	}

}
