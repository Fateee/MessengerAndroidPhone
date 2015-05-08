package com.yineng.ynmessager.activity;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.view.slidingmenu.SlidingMenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class MainFragment extends Fragment {
	// private Button tab1,tab2,tab3,tab4;
	private TabHost mTabHost;
	// private SlidingMenu mSlidingMenu;
	/**
	 * 未读通知的标签.
	 */
	private TextView mUreadNoticeTV;
	private RecentChatDao mRecentchatDao;
	private UnreadMsgCountReceiver mMsgCountReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		/*
		 * tab1 = (Button)view.findViewById(R.id.tab1_btn);
		 * tab1.setOnClickListener(this); tab2 =
		 * (Button)view.findViewById(R.id.tab2_btn);
		 * tab2.setOnClickListener(this); tab3 =
		 * (Button)view.findViewById(R.id.tab3_btn);
		 * tab3.setOnClickListener(this); tab4 =
		 * (Button)view.findViewById(R.id.tab4_btn);
		 * tab4.setOnClickListener(this);
		 */
		mUreadNoticeTV = (TextView) view
				.findViewById(R.id.tv_main_notice_ureadnumbers);
		/* mSlidingMenu = ((MainActivity) getActivity()).getSlidingMenu(); */

		mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
		mTabHost.setup();
		installTabs();
		mRecentchatDao = new RecentChatDao(this.getActivity());
		int defaultTabIndex = this.getActivity().getIntent()
				.getIntExtra("tabIndex", 0);
		mTabHost.setCurrentTab(defaultTabIndex);
		setUnreadMsgCount();
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				// TODO Auto-generated method stub
				/*
				 * if (mSlidingMenu != null) { if ("消息".equals(tabId)) {
				 * mSlidingMenu.setSlidingEnabled(false); } else {
				 * mSlidingMenu.setSlidingEnabled(true); } }
				 */
			}
		});
		registerUnreadMsgCountReceiver();
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unRegisterUnreadMsgCountReceiver();
	}

	public void setCurrentTab() {

	}

	// maybe activities instead
	private void installTabs() {
		installHomeTab();
		installSessionTab();
		installContactTab();
		// installNoticeTab();
	}

	/*
	 * private void installIndexTab() {
	 * mTabHost.addTab(getTab("tab1").setContent
	 * (R.id.tab1).setIndicator("tab1")); }
	 * 
	 * private void installOrganizationTab() {
	 * mTabHost.addTab(getTab("tag2").setContent(
	 * R.id.tab2).setIndicator("tab2")); }
	 * 
	 * private void installSessionTab() {
	 * mTabHost.addTab(getTab("tag3").setContent
	 * (R.id.tab3).setIndicator("tab3")); }
	 * 
	 * private void installNoticeTab() {
	 * mTabHost.addTab(getTab("tag4").setContent
	 * (R.id.tab4).setIndicator("tab4")); }
	 * 
	 * private TabSpec getTab(String tag) { TabSpec tabSpec =
	 * mTabHost.newTabSpec(tag); return tabSpec; }
	 */

	/**
	 * 安装主页的Tab
	 * 
	 * @Title: installIndexTab
	 * @Description: 方法描述
	 */
	private void installHomeTab() {
		mTabHost.addTab(getTab("应用", R.drawable.main_tabitem_home_selector)
				.setContent(R.id.tab1));
	}

	/**
	 * 安装会话的tab
	 * 
	 * @Title: installSessionTab
	 * @Description: 方法描述
	 */
	private void installSessionTab() {
		mTabHost.addTab(getTab("消息", R.drawable.main_tabitem_session_selector)
				.setContent(R.id.tab2));
	}

	/**
	 * 安装组织机构的Tab
	 * 
	 * @Title: installOrganizationTab
	 * @Description: 方法描述
	 */
	private void installContactTab() {
		mTabHost.addTab(getTab("联系人", R.drawable.main_tabitem_contact_selector)
				.setContent(R.id.tab3));
	}

	/**
	 * @Title: installNoticeTab
	 * @Description: 安装通知的TAB
	 */

	/*
	 * private void installNoticeTab() { mTabHost.addTab(getTab("个人中心",
	 * R.drawable.main_tabitem_notice_selector) .setContent(R.id.tab4)); }
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main_layout, null);
	}

	/*
	 * @Override public void onClick(View v) { // TODO Auto-generated method
	 * stub switch (v.getId()) { case R.id.tab1_btn: mTabHost.setCurrentTab(0);
	 * break; case R.id.tab2_btn: mTabHost.setCurrentTab(1); break; case
	 * R.id.tab3_btn: mTabHost.setCurrentTab(2); break; case R.id.tab4_btn:
	 * mTabHost.setCurrentTab(3); break;
	 * 
	 * default: break; } }
	 */

	/**
	 * 得到一个tab.
	 * 
	 * @Title: getTab
	 * @Description: 方法描述
	 * @param titleId
	 * @return tab
	 */
	private TabSpec getTab(String tabtitle, int iconId) {

		View v = this.getActivity().getLayoutInflater()
				.inflate(R.layout.main_tabitem_indicator, null);

		TextView tv = (TextView) v.findViewById(R.id.tv_title);
		tv.setText(tabtitle);

		Drawable icon = getResources().getDrawable(iconId);
		icon.setBounds(
				0,
				0,
				getResources().getDimensionPixelSize(
						R.dimen.main_item_icon_width), getResources()
						.getDimensionPixelSize(R.dimen.main_item_icon_height));

		tv.setCompoundDrawables(null, icon, null, null);
		TabSpec tabSpec = mTabHost.newTabSpec(tabtitle);
		tabSpec.setIndicator(v);
		return tabSpec;
	}

	/**
	 * 注册显示未读消息的接收者
	 * 
	 * @Title: registerUnreadNoticeCountReceiver
	 * @Description: 方法描述
	 */
	private void registerUnreadMsgCountReceiver() {
		if (mMsgCountReceiver == null) {
			mMsgCountReceiver = new UnreadMsgCountReceiver();
		}
		// 注册广播
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction(Const.ACTION_UPDATE_UNREAD_COUNT);
		this.getActivity().registerReceiver(mMsgCountReceiver, ifilter);
	}

	private void unRegisterUnreadMsgCountReceiver() {

		if (mMsgCountReceiver != null) {
			this.getActivity().unregisterReceiver(mMsgCountReceiver);
		}
	}

	/**
	 * 设置未读消息数
	 * 
	 * @Title: setUnreadNoticeCount
	 * @Description: 方法描述
	 */
	private void setUnreadMsgCount() {
		if (mUreadNoticeTV != null) {// 更新未读通知条数
			int unreadNoticesCount = mRecentchatDao.getUnReadMsgCount();
			if (unreadNoticesCount != 0) {
				mUreadNoticeTV.setText(String.valueOf(unreadNoticesCount));
				mUreadNoticeTV.setVisibility(View.VISIBLE);
			} else {
				mUreadNoticeTV.setText("");
				mUreadNoticeTV.setVisibility(View.INVISIBLE);
			}
		}
	}

	class UnreadMsgCountReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Const.ACTION_UPDATE_UNREAD_COUNT)) {
				setUnreadMsgCount();
			}
		}
	};
}
