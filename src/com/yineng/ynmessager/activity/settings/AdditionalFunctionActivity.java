//***************************************************************
//*    2015-4-20  下午2:00:38
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.Const;

/**
 * 辅助功能设置界面
 * 
 * @author 贺毅柳
 * @category Activity
 */
public class AdditionalFunctionActivity extends BaseActivity implements OnClickListener
{

	private ImageButton mImgb_previous; // 左上角返回按钮
	private TextView mTxt_clearChatMsgList; // 清除消息列表
	private TextView mTxt_clearAllChatMsg; // 清除所有聊天记录按钮
	private TextView mTxt_clearCache; // 清除缓存按钮
	private PopupWindow mPopw_confirmedMenu; // 清除时底部弹出的确认框
	private TextView mTxt_confirmYes; // 底部弹出确认框中的“确认”
	private TextView mTxt_confirmNo; // 底部弹出确认框中的“取消”
	private int mFlag = 0;
	private static final int FLAG_CLEAR_CHAT_MSG_LIST = 1;
	private static final int FLAG_CLEAR_ALL_CHAT_MSG = 2;
	private static final int FLAG_CLEAR_CACHE = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_additional_function);

		initViews();
	}

	/**
	 * 初始化各种界面控件
	 */
	private void initViews()
	{
		// 先填充PopupWindow要显示布局
		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		View confirmLayout = inflater.inflate(R.layout.additionalfunction_popupwindow_confirmedmenu,null,false);
		mTxt_confirmYes = (TextView)confirmLayout.findViewById(R.id.additionalFunction_txt_confirmYes);
		mTxt_confirmNo = (TextView)confirmLayout.findViewById(R.id.additionalFunction_txt_confirmNo);
		mTxt_confirmYes.setOnClickListener(this);
		mTxt_confirmNo.setOnClickListener(this);
		// 初始化PopupWindow
		mPopw_confirmedMenu = new PopupWindow(this);
		mPopw_confirmedMenu.setContentView(confirmLayout);
		mPopw_confirmedMenu.setWidth(LayoutParams.MATCH_PARENT);
		mPopw_confirmedMenu.setHeight(LayoutParams.WRAP_CONTENT);
		mPopw_confirmedMenu.setFocusable(true);
		mPopw_confirmedMenu.setOutsideTouchable(false);
		mPopw_confirmedMenu.setBackgroundDrawable(getResources().getDrawable(android.R.color.transparent));
		mPopw_confirmedMenu.setAnimationStyle(R.style.Anim_popupwindow_additionalFunction);

		// 查找、初始化设置界面上的控件
		mImgb_previous = (ImageButton)findViewById(R.id.additionalFunction_imgb_previous);
		mImgb_previous.setOnClickListener(this);
		mTxt_clearChatMsgList = (TextView)findViewById(R.id.additionalFunction_txt_clearChatMsgList);
		mTxt_clearChatMsgList.setOnClickListener(this);
		mTxt_clearAllChatMsg = (TextView)findViewById(R.id.additionalFunction_txt_clearAllChatMsg);
		mTxt_clearAllChatMsg.setOnClickListener(this);
		mTxt_clearCache = (TextView)findViewById(R.id.additionalFunction_txt_clearCache);
		mTxt_clearCache.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		switch(id)
		{
			case R.id.additionalFunction_imgb_previous: // 左上角返回按钮
				finish();
				break;
			case R.id.additionalFunction_txt_clearChatMsgList: // 清除消息列表
				mFlag = FLAG_CLEAR_CHAT_MSG_LIST;
				mPopw_confirmedMenu.showAtLocation(findViewById(R.id.additionalFunction_lin_rootView),Gravity.BOTTOM,0,
						0); // 底部弹出确认框
				break;
			case R.id.additionalFunction_txt_clearAllChatMsg: // 清除所有聊天记录
				mFlag = FLAG_CLEAR_ALL_CHAT_MSG;
				mPopw_confirmedMenu.showAtLocation(findViewById(R.id.additionalFunction_lin_rootView),Gravity.BOTTOM,0,
						0); // 底部弹出确认框
				break;
			case R.id.additionalFunction_txt_clearCache: // 清除缓存
				mFlag = FLAG_CLEAR_CACHE;
				mPopw_confirmedMenu.showAtLocation(findViewById(R.id.additionalFunction_lin_rootView),Gravity.BOTTOM,0,
						0); // 底部弹出确认框
				break;
			case R.id.additionalFunction_txt_confirmYes: // 确认
				switch(mFlag)
				{
					case FLAG_CLEAR_CHAT_MSG_LIST:
						sendBroadcast(new Intent(Const.BROADCAST_ACTION_CLEAR_SESSION_LIST));
						break;
					case FLAG_CLEAR_ALL_CHAT_MSG:
						sendBroadcast(new Intent(Const.BROADCAST_ACTION_CLEAR_ALL_CHAT_MSG));
						break;
					case FLAG_CLEAR_CACHE:
						showToast(R.string.session_clearCacheDone);
						break;
				}
				mPopw_confirmedMenu.dismiss();
				break;
			case R.id.additionalFunction_txt_confirmNo: // 取消
				mPopw_confirmedMenu.dismiss();
				break;
		}
	}

}
