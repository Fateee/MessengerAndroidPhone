//***************************************************************
//*    2015-4-21  上午11:21:38
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.activity.settings;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.util.L;

/**
 * 关于界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class AboutActivity extends BaseActivity implements OnClickListener
{
	private ImageButton mImgb_previous; // 左上角的返回按钮
	private TextView mTxt_currentVersionName; // 显示当前版本号的TextView

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		initViews();
	}

	/**
	 * 初始化界面控件
	 */
	private void initViews()
	{
		mImgb_previous = (ImageButton)findViewById(R.id.about_imgb_previous);
		mImgb_previous.setOnClickListener(this);
		mTxt_currentVersionName = (TextView)findViewById(R.id.about_txt_currentVersionName);
		mTxt_currentVersionName.setText(getString(R.string.about_currentVersionName,getPackageVersionName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.about_imgb_previous:
				finish();
				break;
		}

	}

	/**
	 * 得到本应用程序的VersionName
	 * 
	 * @return AndroidManifest.xml文件中声明的android:versionName
	 */
	private String getPackageVersionName()
	{
		String versionName = "";
		try
		{
			versionName = getPackageManager().getPackageInfo(getPackageName(),0).versionName;
		}catch(NameNotFoundException e)
		{
			L.e(TAG,e.getMessage(),e);
		}
		return versionName;
	}
}
