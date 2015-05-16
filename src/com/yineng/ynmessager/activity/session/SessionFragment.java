package com.yineng.ynmessager.activity.session;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.smack.StatusChangedCallBack;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.SearchContactEditText.onCancelSearchAnimationListener;
import com.yineng.ynmessager.view.SwipeListViewItem;
import com.yineng.ynmessager.view.SwipeListViewItem.SwipeViewItemOpendListener;

public class SessionFragment extends Fragment implements StatusChangedCallBack, onCancelSearchAnimationListener,
		SwipeViewItemOpendListener
{
	private static final String TAG = "SessionFragment";
	private LinearLayout mFrameLinearLayout;
	private LinearLayout mStatusLinearLayout;
	private TextView mStatusText;
	private int mCurrentStats = 0;
	private PullToRefreshListView mPullToRefreshListView;
	private UnreadMsgCountReceiver mMsgCountReceiver;
	private SwipeListViewItem mSwipeListViewItem;
	private SwipeListviewAdapter mSwipeListviewAdapter;
	private RecentChatDao mRecentChatDao;
	private GroupChatDao mGroupChatDao;
	private DisGroupChatDao mDisGroupChatDao;
	private P2PChatMsgDao mP2pChatMsgDao;
	private LinkedList<RecentChat> mRecentChatsList;
	private ContactOrgDao mContactOrgDao;
	/**
	 * 上下动画滚动的高度
	 */
	protected float searchViewY;
	private EditText mEditText;
	/**
	 * 自定义搜索框
	 */
	private SearchContactEditText mSearchContactEditText;
	/**
	 * 显示搜索框动画
	 */
	protected final int SHOW_SEARCH_VIEW = 0;
	/**
	 * 取消搜索框动画
	 */
	protected final int CANCEL_SEARCH_VIEW = 1;
	/**
	 * 一页显示的条数
	 */
	private final int mPageSize = 80;
	@SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case SHOW_SEARCH_VIEW:
					mSearchContactEditText.show();
					mFrameLinearLayout.setY(-searchViewY);
					break;

				case CANCEL_SEARCH_VIEW:
					mFrameLinearLayout.setY(0);
					break;
				case LoginThread.USER_STATUS_ONLINE:
					mStatusLinearLayout.setVisibility(View.GONE);
					ToastUtil.toastAlerMessageCenter(getActivity(),"已经上线！",1000);
					break;

				case LoginThread.USER_STATUS_LOGINED_OTHER:
					mStatusText.setText("已经在其它设备上线！");
					mStatusLinearLayout.setVisibility(View.VISIBLE);
					break;

				case LoginThread.USER_STATUS_NETOFF:// 服务器连接断开
					mStatusText.setText("网络异常，请检查网络!");
					mStatusLinearLayout.setVisibility(View.VISIBLE);
					break;

				case LoginThread.USER_STATUS_OFFLINE:// 下线了
					mStatusText.setText("已经下线！");
					mStatusLinearLayout.setVisibility(View.VISIBLE);
					break;
				default:
					mStatusLinearLayout.setVisibility(View.GONE);
					break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_main_session_layout,null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view,savedInstanceState);
		initViews(view);
		registerUnreadMsgCountReceiver();
		registerRefreshListBroadcastReceiver();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		
		loadRecentChatByPage(0);
	}
	
	private void initViews(View view)
	{
		mRecentChatsList = new LinkedList<RecentChat>();
		mRecentChatDao = new RecentChatDao(getActivity().getApplicationContext());
		mGroupChatDao = new GroupChatDao(getActivity().getApplicationContext());
		mDisGroupChatDao = new DisGroupChatDao(getActivity().getApplicationContext());
		mP2pChatMsgDao = new P2PChatMsgDao(getActivity().getApplicationContext());
		mContactOrgDao = new ContactOrgDao(getActivity().getApplicationContext());
		mSwipeListviewAdapter = new SwipeListviewAdapter(this,getActivity(),mRecentChatsList);
		mStatusLinearLayout = (LinearLayout)view.findViewById(R.id.ll_main_session_alertlayer);
		mFrameLinearLayout = (LinearLayout)view.findViewById(R.id.ll_main_session_frame);
		mStatusText = (TextView)view.findViewById(R.id.tv_main_session_alert_text);
		mPullToRefreshListView = (PullToRefreshListView)view.findViewById(R.id.prlv_main_session_refresh);
		mPullToRefreshListView.setMode(Mode.BOTH);
		mPullToRefreshListView.setAdapter(mSwipeListviewAdapter);
		mEditText = (EditText)view.findViewById(R.id.et_main_session_search);
		mSearchContactEditText = new SearchContactEditText(this.getActivity());
		mSearchContactEditText.setSessionFragment(true,true);
		mEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View paramView)
			{
				showSearchContactAnimation();
			}
		});
		/*
		 * mPullToRefreshListView .setOnRefreshListener(new
		 * OnRefreshListener2<ListView>() {
		 * 
		 * @Override public void onPullDownToRefresh(// 下拉刷新
		 * PullToRefreshBase<ListView> refreshView) { loadRecentChatByPage(0); }
		 * 
		 * @Override public void onPullUpToRefresh(// 上拉加载
		 * PullToRefreshBase<ListView> refreshView) { if
		 * (mRecentChatsList.size() >= mPageSize) {
		 * loadRecentChatByPage(mRecentChatsList.size() / mPageSize); } else {
		 * loadRecentChatByPage(0); } } });
		 */
		onStatusChanged(XmppConnectionManager.getInstance().getUserCurrentStatus());
		XmppConnectionManager.getInstance().addStatusChangedCallBack(this);
		mSearchContactEditText.setOnCancelSearchAnimationListener(this);
	}

	TranslateAnimation mShowAnimation = null;
	TranslateAnimation mCancelAnimation = null;
	private AnimationListener mShowAnimationListener = new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation)
		{

		}

		@Override
		public void onAnimationRepeat(Animation animation)
		{

		}

		@Override
		public void onAnimationEnd(Animation animation)
		{
			if (isShowSearchEditText) {
				mHandler.sendEmptyMessage(SHOW_SEARCH_VIEW);
			} else {
				mHandler.sendEmptyMessage(CANCEL_SEARCH_VIEW);
			}
		}
	};
	
	private boolean isShowSearchEditText = false;

	public void showSearchContactAnimation()
	{
		isShowSearchEditText = true;
		LinearLayout.LayoutParams etParamTest = (LinearLayout.LayoutParams)mEditText.getLayoutParams();
		searchViewY = mEditText.getY() - (float)etParamTest.topMargin;
		mShowAnimation = new TranslateAnimation(0,0,0,-searchViewY);
		mShowAnimation.setDuration(200);
		mShowAnimation.setAnimationListener(mShowAnimationListener);
		mFrameLinearLayout.startAnimation(mShowAnimation);
	}

	@Override
	public void cancelSearchContactAnimation()
	{
		isShowSearchEditText = false;
		mSearchContactEditText.dismiss();
		mCancelAnimation = new TranslateAnimation(0,0, 0, searchViewY);
		mCancelAnimation.setDuration(200);
		mCancelAnimation.setAnimationListener(mShowAnimationListener);
		mFrameLinearLayout.startAnimation(mCancelAnimation);

	}

	@Override
	public void onDestroy()
	{
		XmppConnectionManager.getInstance().removeStatusChangedCallBack(this);
		unRegisterUnreadMsgCountReceiver();
		unregisterRefreshListBroadcaseReceiver();
		super.onDestroy();
	}

	private void loadRecentChatByPage(int pages)
	{
		new AsyncTask<String, Integer, List<RecentChat>>() {
			private int mPageIndex = 0;

			// 访问数据库前执行
			protected void onPreExecute()
			{
				mPullToRefreshListView.setRefreshing();
			};

			@Override
			protected List<RecentChat> doInBackground(String... params)
			{
				mPageIndex = Integer.valueOf(params[0]);
				return mRecentChatDao.queryRecentChatPage(mPageIndex,mPageSize);
			}

			// 返回数据
			protected void onPostExecute(List<RecentChat> list)
			{
				if(mPageIndex == 0)
				{
					mRecentChatsList.clear();
					mRecentChatsList.addAll(list);
				}else
				{
					for(RecentChat chat : list)
					{
						if(!mRecentChatsList.contains(chat))
						{
							mRecentChatsList.addLast(chat);
						}
					}
				}
				mSwipeListviewAdapter.notifyDataSetChanged();
				mPullToRefreshListView.onRefreshComplete();
			}
		}.execute(String.valueOf(pages));
	}

	/**
	 * 注册显示未读消息的接收者
	 * 
	 * @Title: registerUnreadNoticeCountReceiver
	 * @Description: 方法描述
	 */
	private void registerUnreadMsgCountReceiver()
	{
		if(mMsgCountReceiver == null)
		{
			mMsgCountReceiver = new UnreadMsgCountReceiver();
		}
		// 注册广播
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction(Const.ACTION_UPDATE_UNREAD_COUNT);
		this.getActivity().registerReceiver(mMsgCountReceiver,ifilter);
	}

	private void unRegisterUnreadMsgCountReceiver()
	{
		if(mMsgCountReceiver != null)
		{
			this.getActivity().unregisterReceiver(mMsgCountReceiver);
		}
	}

	private class UnreadMsgCountReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if(intent.getAction().equals(Const.ACTION_UPDATE_UNREAD_COUNT))
			{
				String account = intent.getStringExtra("userno");
				int type = intent.getIntExtra("chattype",Const.CHAT_TYPE_P2P);

				RecentChat recentChat = mRecentChatDao.isChatExist(account,type);
				if(recentChat != null)
				{
					if(mRecentChatsList.contains(recentChat))
					{
						mRecentChatsList.remove(recentChat);
					}
					sortRecentChatList(mRecentChatsList,recentChat);
					mSwipeListviewAdapter.notifyDataSetChanged();
				}else
				{ // 退出讨论组时已经删掉了最近会话的该讨论组的聊天记录
					for(RecentChat tempRecentChat : mRecentChatsList)
					{
						if(tempRecentChat.getUserNo().equals(account))
						{
							mRecentChatsList.remove(tempRecentChat);
							break;
						}
					}
					mSwipeListviewAdapter.notifyDataSetChanged();
				}
			}
		}
	};

	/**
	 * 注册广播接收器 设置-辅助功能中的清空会话消息列表
	 */
	private void registerRefreshListBroadcastReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Const.BROADCAST_ACTION_CLEAR_SESSION_LIST);
		filter.addAction(Const.BROADCAST_ACTION_CLEAR_ALL_CHAT_MSG);
		getActivity().registerReceiver(mRefreshListBroadcastReceiver,filter);
	}

	/**
	 * 反注册广播接收器 设置-辅助功能中的清空会话消息列表
	 */
	private void unregisterRefreshListBroadcaseReceiver()
	{
		getActivity().unregisterReceiver(mRefreshListBroadcastReceiver);
	}

	/**
	 * 广播接收器，接收处理 设置 - 辅助功能中的清空会话消息列表
	 */
	private BroadcastReceiver mRefreshListBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if(Const.BROADCAST_ACTION_CLEAR_SESSION_LIST.equals(action)) // 清空会话列表
			{
				mRecentChatDao.deleteAll(); // 先清空整个表
				loadRecentChatByPage(0); // 再加载数据库到UI
				ToastUtil.toastAlerMessageCenter(SessionFragment.this.getActivity(),
						getString(R.string.session_clearListDone),2000);
			}else if(Const.BROADCAST_ACTION_CLEAR_ALL_CHAT_MSG.equals(action)) // 清除所有聊天记录
			{
				mGroupChatDao.deleteAll();
				mDisGroupChatDao.deleteAll();
				mP2pChatMsgDao.deleteAll();
				mRecentChatDao.deleteAll(); // 先清空整个表
				loadRecentChatByPage(0); // 再加载数据库到UI
				ToastUtil.toastAlerMessageCenter(SessionFragment.this.getActivity(),
						getString(R.string.session_clearAllChatMsgDone),2000);
			}
		}
	};

	@Override
	public void onStatusChanged(int status)
	{
		if(mCurrentStats != status)
		{
			mHandler.sendEmptyMessage(status);
			mCurrentStats = status;
		}
	}

	@Override
	public void onSwipeViewItemOpend(SwipeListViewItem item)
	{
		if(mSwipeListViewItem != null && mSwipeListViewItem != item && mSwipeListViewItem.isOpen())
		{
			mSwipeListViewItem.smoothScrollTo(0,0);
		}
		mSwipeListViewItem = item;
	}

	/**
	 * 会话列表排序方法
	 * 
	 * @param list
	 * @param recentChat
	 */
	public static void sortRecentChatList(LinkedList<RecentChat> list, RecentChat recentChat)
	{
		int count = list.size();
		int maxIndex = count - 1;
		if(count > 0)
		{
			if(recentChat.getIsTop() == 1)
			{
				list.addFirst(recentChat);

			}else
			{
				for(int i = 0; i < count; i++)
				{
					if(list.get(i).getIsTop() == 0
							&& TimeUtil.getMillisecondByDate(list.get(i).getDateTime(),TimeUtil.FORMAT_DATETIME_24) <= TimeUtil
									.getMillisecondByDate(recentChat.getDateTime(),TimeUtil.FORMAT_DATETIME_24))
					{
						list.add(i,recentChat);// 插入到i位置
						break;
					}
					if(i == maxIndex)
					{// 插入到末尾
						list.addLast(recentChat);
					}
				}
			}
		}else
		{
			list.add(recentChat);
		}
	}

	private class SwipeListviewAdapter extends BaseAdapter
	{
		private LinkedList<RecentChat> mSessionDatas;
		private LayoutInflater mInflater;
		private int mScreenWidth;
		private Context mContext;
		private SwipeViewItemOpendListener mOpendListener;
		private SimpleDateFormat mDateFormat = new SimpleDateFormat(TimeUtil.FORMAT_DATETIME_24);

		public SwipeListviewAdapter(SwipeViewItemOpendListener listener, Context context,
				LinkedList<RecentChat> sessionDatas)
		{
			this.mSessionDatas = sessionDatas;
			mInflater = LayoutInflater.from(context);
			mScreenWidth = ((Activity)context).getWindowManager().getDefaultDisplay().getWidth();
			mContext = context;
			mOpendListener = listener;
		}

		@Override
		public int getCount()
		{
			return mSessionDatas.size();
		}

		@Override
		public Object getItem(int position)
		{
			return mSessionDatas.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			ViewHolder viewHolder = null;
			final RecentChat recentChat = (RecentChat)mSessionDatas.get(position);
			// 如果是群、讨论组消息，则先从数据库查询出这个对象
			ContactGroup contactGroup = null;
			int chatType = recentChat.getChatType();
			if(chatType == Const.CHAT_TYPE_GROUP || chatType == Const.CHAT_TYPE_DIS)
			{
				String groupName = recentChat.getUserNo();
				contactGroup = mContactOrgDao.queryGroupOrDiscussByGroupName(groupName);
			}

			if(convertView == null)
			{
				viewHolder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.item_main_session_swipelist,null);

				viewHolder.mSessionFront = (RelativeLayout)convertView.findViewById(R.id.ll_main_session_front);
				viewHolder.mSessionPhoto = (ImageView)convertView.findViewById(R.id.iv_main_session_item_headicon);
				viewHolder.mSessionDateTime = (TextView)convertView.findViewById(R.id.tv_main_session_item_datetime);
				viewHolder.mSessionContent = (TextView)convertView.findViewById(R.id.tv_main_session_item_content);
				viewHolder.mSessionUnreadCoun = (TextView)convertView
						.findViewById(R.id.tv_main_session_item_unreadcount);
				viewHolder.mSessionTitle = (TextView)convertView.findViewById(R.id.tv_main_session_item_title);
				viewHolder.mSessionSetTopButton = (Button)convertView.findViewById(R.id.bt_session_item_settop);
				viewHolder.mSessionTop = (ImageView)convertView.findViewById(R.id.iv_main_session_item_top);
				viewHolder.mSessionAlert = (Button)convertView.findViewById(R.id.bt_session_item_alert);
				// 可移到item的mesure和layout中处理
				// viewHolder.mSessionBack.getLayoutParams().width =
				// mScreenWidth;
				viewHolder.mSessionFront.getLayoutParams().width = mScreenWidth;

				viewHolder.mSessionDeleteButton = (Button)convertView.findViewById(R.id.bt_session_item_delete);

				convertView.setTag(viewHolder);
			}else
			{
				viewHolder = (ViewHolder)convertView.getTag();
			}

			final SwipeListViewItem swipeListViewItem = (SwipeListViewItem)convertView;

			swipeListViewItem.setRecentChat(recentChat);
			swipeListViewItem.setItemOpendListener(mOpendListener);

			// 获取会话中最后一次消息的聊天时间
			Date date = convertStringDate(recentChat.getDateTime());
			String relative = TimeUtil.getTimeRelationFromNow(getActivity().getApplicationContext(), date);
			// 转换成与当前时间的关系文字
			viewHolder.mSessionDateTime.setText(relative);
			L.d(TAG,"recentChat.getDateTime:" + recentChat.getDateTime());

			//设置显示草稿还是消息内容
			String draft = recentChat.getDraft();
			if(TextUtils.isEmpty(draft))  //如果没有草稿
			{
				if((recentChat.getChatType() == Const.CHAT_TYPE_DIS || recentChat.getChatType() == Const.CHAT_TYPE_GROUP)
						&& recentChat.getSenderName() != null)
				{
					viewHolder.mSessionContent.setText(recentChat.getSenderName() + ": " + recentChat.getContent());
				}else
				{
					viewHolder.mSessionContent.setText(recentChat.getContent());
				}
			}else  //如果有草稿
			{
				viewHolder.mSessionContent.setText(RecentChat.DRAFT_PREFIX + draft);
			}
			
			if(recentChat.getUnReadCount() > 0)
			{
				viewHolder.mSessionUnreadCoun.setText(recentChat.getUnReadCount() + "");
				viewHolder.mSessionUnreadCoun.setVisibility(View.VISIBLE);
			}else
			{
				viewHolder.mSessionUnreadCoun.setVisibility(View.GONE);
			}

			viewHolder.mSessionTitle.setText(recentChat.getTitle());

			viewHolder.mSessionSetTopButton.setTag(position);
			viewHolder.mSessionDeleteButton.setTag(position);
			if(recentChat.getIsTop() == 1)
			{
				viewHolder.mSessionTop.setVisibility(View.VISIBLE);
				viewHolder.mSessionSetTopButton.setText("取消置顶");
			}else
			{
				viewHolder.mSessionTop.setVisibility(View.INVISIBLE);
				viewHolder.mSessionSetTopButton.setText("置顶");
			}

			viewHolder.mSessionDeleteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v)
				{
					// 在这里根据位置获取itemview，空指针异常,对于已有的没有问题，对于新拉动出来的会有问题,可与onitemclicked结合
					// 参考swipelistview，在项view或者listview中添加消除动画以及恢复试图
					final int pos = (Integer)v.getTag();
					final SwipeListViewItem swipeListViewItem = (SwipeListViewItem)v.getParent().getParent()
							.getParent();

					Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.session_slide_out);
					animation.setAnimationListener(new AnimationListener() {
						@Override
						public void onAnimationStart(Animation animation)
						{

						}

						@Override
						public void onAnimationRepeat(Animation animation)
						{
						}

						@Override
						public void onAnimationEnd(Animation animation)
						{
							mRecentChatDao.deleteRecentChatById(mSessionDatas.get(pos).getId());
							mSessionDatas.remove(pos);
							swipeListViewItem.scrollTo(0,0);
							Intent intent = new Intent();
							intent.setAction(Const.ACTION_UPDATE_UNREAD_COUNT);
							mContext.sendBroadcast(intent);
							notifyDataSetChanged();
						}
					});
					swipeListViewItem.startAnimation(animation);
				}
			});

			viewHolder.mSessionSetTopButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v)
				{
					int pos = (Integer)v.getTag();
					RecentChat recentChat = mSessionDatas.get(pos);
					mSessionDatas.remove(pos);
					if(recentChat.getIsTop() == 1)
					{// 取消置顶
						recentChat.setIsTop(0);
						mRecentChatDao.updateIsTop(recentChat.getId(),0);
						SessionFragment.sortRecentChatList(mSessionDatas,recentChat);

					}else
					{// 置顶
						recentChat.setIsTop(1);
						mSessionDatas.addFirst(recentChat);
						mRecentChatDao.updateIsTop(recentChat.getId(),1);
					}

					final SwipeListViewItem swipeListViewItem = (SwipeListViewItem)v.getParent().getParent()
							.getParent();
					swipeListViewItem.scrollTo(0,0);
					notifyDataSetChanged();
				}
			});
			// 设置 “提醒” 按钮
			if(contactGroup != null)
			{
				// 设置按钮标题的显示
				int notifyMode = contactGroup.getNotifyMode();
				int buttonText = notifyMode == ContactGroup.NOTIFYMODE_YES ? R.string.session_notifyModeAlarm
						: R.string.session_notifyModeNoAlarm;
				viewHolder.mSessionAlert.setText(getString(buttonText));
				// 更新到数据库
				final ContactGroup newGroup = contactGroup;
				viewHolder.mSessionAlert.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v)
					{
						// 改变提醒方式，更新到数据库
						newGroup.setNotifyMode(newGroup.getNotifyMode() == ContactGroup.NOTIFYMODE_YES ? ContactGroup.NOTIFYMODE_NO
								: ContactGroup.NOTIFYMODE_YES);
						mContactOrgDao.updateGroupOrDiscuss(newGroup);
						// 更新列表UI
						notifyDataSetChanged();
					}
				});
			}

			// 在被更新之后自动关闭有有划开的界面
			if(convertView.getScrollX() > 0)
			{
				convertView.scrollTo(0,0);
			}
			
			return convertView;
		}
		
		/**
		 * 将日期时间字符串转换为Date对象
		 * @param date 要转换的日期时间字符串
		 * @return 想要获得的Date对象
		 */
		private Date convertStringDate(String date)
		{
			Date d = null;
			// 2015-05-08 09:56:58
			try
			{
				d = mDateFormat.parse(date);
			}catch(ParseException e)
			{
				L.e(TAG,e.getMessage(),e);
			}
			return d;
		}

		private final class ViewHolder
		{
			RelativeLayout mSessionFront;
			ImageView mSessionPhoto;
			ImageView mSessionTop;
			TextView mSessionTitle;
			TextView mSessionContent;
			TextView mSessionDateTime;
			TextView mSessionUnreadCoun;
			Button mSessionDeleteButton;
			Button mSessionSetTopButton;
			Button mSessionAlert;
		}
	}
}
