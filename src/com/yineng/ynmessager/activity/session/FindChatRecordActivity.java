package com.yineng.ynmessager.activity.session;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.dissession.DisGroupRenameActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.GroupChatDao;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.view.SearchChatRecordEditText;
import com.yineng.ynmessager.view.SearchChatRecordEditText.onCancelSearchAnimationListener;
import com.yineng.ynmessager.view.SearchChatRecordEditText.onResultListItemCLickListener;

public class FindChatRecordActivity extends BaseActivity {
	private static final int PAGE_SIZE = 20;
	/**
	 * 显示搜索框动画
	 */
	protected final int SHOW_SEARCH_VIEW = 0;
	/**
	 * 取消搜索框动画
	 */
	protected final int CANCEL_SEARCH_VIEW = 1;

	private final int ANIMATION_DURATION = 120;
	
	private final int INIT_REFRESH = 0;
	private final int PULL_DOWN_REFRESH = 1;
	private final int PULL_UP_REFRESH = 2;

	private SearchChatRecordEditText mSearchChatRecordEditText;
	private Context mContext;
	private EditText mChatRecordEditView;
	private LinearLayout mChatRecordLayout;

	private Handler mHandler = new Handler() {
		@SuppressLint("NewApi")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_SEARCH_VIEW:
				mSearchChatRecordEditText.show();
				mChatRecordLayout.setY(-mChatEditTextDefaultViewY);
				break;
			case CANCEL_SEARCH_VIEW:
				mChatRecordLayout.setY(0);
				break;

			default:
				break;
			}
		};
	};
	private String mChatId;
	private int mChatType;
	private PullToRefreshListView mPullLoadMoreLV;
	private ListView mChatMsgLV;
	private int mPullDownPageIndex = 0;
	private int mPullUPPageIndex = 0;
	private P2PChatMsgDao mP2PChatMsgDao;
	private GroupChatDao mGroupChatDao;
	private DisGroupChatDao mDisGroupChatDao;
	private ArrayList<GroupChatMsgEntity> mGroupMessageList = new ArrayList<GroupChatMsgEntity>();
	private ChatMsgAdapter mChatMsgAdapter;
	private boolean mNoMoreMsg = false;
	LinkedList<GroupChatMsgEntity> mMsgList = new LinkedList<GroupChatMsgEntity>();
	private CommonReceiver mCommonReceiver;
	protected boolean isFinishAcitivity = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = FindChatRecordActivity.this;
		setContentView(R.layout.activity_session_find_chat_record_layout);
		initData();
		findViews();
		// 初始状态，从本地获得20条数据，刷新UI
		refreshUIByPageIndex(INIT_REFRESH,mPullDownPageIndex);
		mChatMsgLV.setSelection(mChatMsgAdapter.getCount());
	}

	private void findViews() {
		mPullLoadMoreLV = (PullToRefreshListView) findViewById(R.id.ptrl_chat_pull_refresh_list);
		mChatMsgLV = mPullLoadMoreLV.getRefreshableView();
		mChatMsgLV.setAdapter(mChatMsgAdapter);
		findSearchChatRecordView();
	}

	private void initData() {
		Intent mGetIntent = getIntent();
		mChatId = mGetIntent.getStringExtra(GroupChatActivity.CHAT_ID_KEY);
		mChatType = mGetIntent.getIntExtra(GroupChatActivity.CHAT_TYPE_KEY,
				Const.CHAT_TYPE_P2P);
		switch (mChatType) {
		case Const.CHAT_TYPE_P2P:
			mP2PChatMsgDao = new P2PChatMsgDao(mContext);
			mGroupMessageList = mP2PChatMsgDao.getChatMsgEntitiesToFindRecord(mChatId);
			break;
		case Const.CHAT_TYPE_GROUP:
			mGroupChatDao = new GroupChatDao(mContext);
			mGroupMessageList = mGroupChatDao.getChatMsgEntities(mChatId);
			break;
		case Const.CHAT_TYPE_DIS:
			mDisGroupChatDao = new DisGroupChatDao(mContext);
			mGroupMessageList = mDisGroupChatDao.getChatMsgEntities(mChatId);
			break;
		default:
			break;
		}
		mChatMsgAdapter = new ChatMsgAdapter(mContext);
	}

	private void findSearchChatRecordView() {
		mSearchChatRecordEditText = new SearchChatRecordEditText(mContext,
				mChatId, mChatType);
		mChatRecordEditView = (EditText) findViewById(R.id.et_find_chat_record_edittext);
		mChatRecordLayout = (LinearLayout) findViewById(R.id.ll_find_chat_record_frame);
		initSearchContactViewListener();
	}

	private void initSearchContactViewListener() {
		mChatRecordEditView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				showDismissSearchView(true);
			}
		});

		mSearchChatRecordEditText
				.setOnCancelSearchAnimationListener(new onCancelSearchAnimationListener() {

					@Override
					public void cancelSearchContactAnimation() {
						showDismissSearchView(false);
					}
				});

		mSearchChatRecordEditText.setOnResultListItemCLickListener(new onResultListItemCLickListener() {

			@Override
			public void scrollToClickItem(Object clickObject) {
				showDismissSearchView(false);
				int mSelectIndex = 0;
				mMsgList.clear();
				GroupChatMsgEntity msgEntity = (GroupChatMsgEntity) clickObject;
				
				//得到点击的item在数据源列表的索引
				int mItemIndex = mGroupMessageList.indexOf(msgEntity);
				
				//根据item的索引计算出该item应该出现在第几页
				mPullDownPageIndex = mItemIndex/PAGE_SIZE;
				
				//加载该页数据
				refreshUIByPageIndex(INIT_REFRESH,mPullDownPageIndex);
				
				//计算出该listview应该选中的位置
				mSelectIndex = mChatMsgAdapter.getmMsgList().indexOf(msgEntity);
				
//				L.d("mItemIndex == "+mItemIndex+" mPullDownPageIndex == "+mPullDownPageIndex+" selectindex == "+mSelectIndex);
//				L.d("select pos == "+mChatMsgLV.getSelectedItemPosition());
				if (mSelectIndex > -1) {
					mChatMsgLV.setSelection(mSelectIndex);
				}
//				L.e("selected pos == "+mChatMsgLV.getSelectedItemPosition());
				//如果是第一页，则不让他上拉加载更多；否则上下拉都可以
				if (mPullDownPageIndex == 0) {
					mPullLoadMoreLV.setMode(com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
				} else {
					mPullUPPageIndex = mPullDownPageIndex;
					mPullLoadMoreLV.setMode(com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.BOTH);
				}
			}
		});
		
		mPullLoadMoreLV.setOnRefreshListener(new OnRefreshListener2<ListView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						//如果页数X每页个数小于消息记录总数，说明还有记录
						if ((mPullDownPageIndex+1)*PAGE_SIZE < mGroupMessageList.size()) {
							mPullDownPageIndex++;
							int mBeforeMoreCount = mChatMsgAdapter.getCount();
							refreshUIByPageIndex(PULL_DOWN_REFRESH,mPullDownPageIndex);
							mPullLoadMoreLV.onRefreshComplete();
							int mAfterMoreCount = mChatMsgAdapter.getCount();
							int selectIndex = mAfterMoreCount-mBeforeMoreCount;
							L.e("selectIndex == "+selectIndex);
							mChatMsgLV.setSelection(selectIndex);
						}else {
							mPullLoadMoreLV.onRefreshComplete();
						}
					}
				}, 1000);
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mPullUPPageIndex > 0) {
							mPullUPPageIndex--;
							refreshUIByPageIndex(PULL_UP_REFRESH,mPullUPPageIndex);
							mPullLoadMoreLV.onRefreshComplete();
							if (mPullUPPageIndex == 0) {
								mPullLoadMoreLV.setMode(com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.PULL_DOWN_TO_REFRESH);
							}
						} 
					}
				}, 1000);
			}
		});
		addGroupUpdatedListener();
	}
	
	/**
	 * 添加讨论组信息更改监听器
	 */
	private void addGroupUpdatedListener() {
		mCommonReceiver = new CommonReceiver();
		mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {
			
			@Override
			public void updateGroupData(int mGroupType) {
			}
		});
		mCommonReceiver.setIQuitGroupListener(new IQuitGroupListener() {
			
			@Override
			public void IQuitMyGroup(int mGroupType) {
				if ((mGroupType == Const.CONTACT_GROUP_TYPE && mChatType == Const.CHAT_TYPE_GROUP)
						||(mGroupType == Const.CONTACT_DISGROUP_TYPE && mChatType == Const.CHAT_TYPE_DIS)) {
					isFinishAcitivity  = true;
					finish();
				}
			}
		});
		IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_UPDATE_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
		registerReceiver(mCommonReceiver, mIntentFilter);		
	}
	
	/**
	 * 本地分页查询数据，刷新UI
	 * @param refreshType 
	 */
	public void refreshUIByPageIndex(int refreshType,int pageIndex) {
		if (mGroupMessageList.size() > 0) {
			LinkedList<GroupChatMsgEntity> tempChatMsgEntities = new LinkedList<GroupChatMsgEntity>();
			for (int i = pageIndex*PAGE_SIZE; i < mGroupMessageList.size(); i++) {
				tempChatMsgEntities.addFirst(mGroupMessageList.get(i));
				if (tempChatMsgEntities.size()%PAGE_SIZE == 0 ) {
					break;
				}
			}
			if (refreshType == PULL_UP_REFRESH) {
				mMsgList.addAll(tempChatMsgEntities);
			} else {
				mMsgList.addAll(0, tempChatMsgEntities);
			}
			notifyAdapterDataSetChanged();
		} else {
			mNoMoreMsg  = true;
		}
	}

	/**
	 * 修改数据的isShowTime字段（第一条消息显示时间，5分钟内的消息不显示时间），然后刷新UI
	 * @param list2 
	 */
	private void notifyAdapterDataSetChanged() {
		List<GroupChatMsgEntity> list = new ArrayList<GroupChatMsgEntity>();
		long preShowTime = 0;
		for (int i = 0; i < mMsgList.size(); i++) {
			GroupChatMsgEntity entity = mMsgList.get(i);
			if (i == 0) {
				entity.setShowTime(true);
				preShowTime = Long.valueOf(entity.getmTime().trim());
			} else {
				if (compareTime(preShowTime, Long.valueOf(entity.getmTime()))) {
					preShowTime = Long.valueOf(entity.getmTime());
					entity.setShowTime(true);
				} else {
					entity.setShowTime(false);
				}
			}
			list.add(entity);
		}
		// list作为临时的数据缓存，避免数据变更后，没有及时通知适配器，出现
		// The content of the adapter has changed but ListView did not receive a
		// notification的错误
		mChatMsgAdapter.setmMsgList(list);
		mChatMsgAdapter.notifyDataSetChanged();
//		refreshUnreadNumUI();

	}
	
	private static final long TIME_INTERVAL = 60 * 5 * 1000;// 时间在5分钟内的消息不显示时间
	/**
	 * 
	 * Compare the time difference is greater than TIME_INTERVAL minutes
	 * 
	 * @param fisrtTime
	 * @param lastTime
	 * @return
	 */
	public static boolean compareTime(long preTime, long nextTime) {
		if ((nextTime - preTime) >= TIME_INTERVAL) {
			return true;
		}
		return false;
	}
	
	TranslateAnimation showAnimation = null;
	TranslateAnimation cancelAnimation = null;
	private float mChatEditTextDefaultViewY;
	private AnimationListener showAnimationListener = new AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mHandler.sendEmptyMessage(SHOW_SEARCH_VIEW);
		}
	};

	/**
	 * 显示/关闭搜素框的动画
	 * 
	 * @param isShow
	 */
	@SuppressLint("NewApi")
	public void showDismissSearchView(boolean isShow) {
		if (isShow) {
			LinearLayout.LayoutParams etParamTest = (LinearLayout.LayoutParams) mChatRecordEditView
					.getLayoutParams();
			mChatEditTextDefaultViewY = mChatRecordEditView.getY()
					- etParamTest.topMargin;
			showAnimation = new TranslateAnimation(0, 0, 0,
					-mChatEditTextDefaultViewY);
			showAnimation.setDuration(ANIMATION_DURATION);
			showAnimation.setAnimationListener(showAnimationListener);
			mChatRecordLayout.startAnimation(showAnimation);
		} else {
			mSearchChatRecordEditText.dismiss();
			mHandler.sendEmptyMessage(CANCEL_SEARCH_VIEW);
			cancelAnimation = new TranslateAnimation(0, 0,
					-mChatEditTextDefaultViewY, 0);
			cancelAnimation.setDuration(ANIMATION_DURATION);
			mChatRecordLayout.startAnimation(cancelAnimation);
		}
	}

	public void back(View view) {
		finish();
	}
	
	public class ChatMsgAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private Context context;
		private List<GroupChatMsgEntity> mMsgList = new ArrayList<GroupChatMsgEntity>();
		
		public ChatMsgAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			this.context = context;
		}

		public List<GroupChatMsgEntity> getmMsgList() {
			return mMsgList;
		}

		public void setmMsgList(List<GroupChatMsgEntity> mMsgList) {
			this.mMsgList = mMsgList;
		}

		@Override
		public int getCount() {
			return mMsgList.size();
		}

		@Override
		public Object getItem(int position) {
			return mMsgList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			GroupChatMsgEntity entity = mMsgList.get(position);
			if (entity.getIsSend() == GroupChatMsgEntity.COM_MSG) {
				return GroupChatMsgEntity.COM_MSG;
			} else {
				return GroupChatMsgEntity.TO_MSG;
			}

		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			GroupChatMsgEntity entity = mMsgList.get(position);
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				if (entity.getIsSend() == GroupChatMsgEntity.COM_MSG) {
					convertView = mInflater.inflate(
							R.layout.chatting_item_msg_text_left, null);
					viewHolder.tvSenderName = (TextView) convertView.findViewById(R.id.tv_chat_sender_name);
					if (mChatType != Const.CHAT_TYPE_P2P) {
						viewHolder.tvSenderName.setVisibility(View.VISIBLE);
					}
				} else {
					convertView = mInflater.inflate(
							R.layout.chatting_item_msg_text_right, null);
				}

				viewHolder.tvSendTime = (TextView) convertView
						.findViewById(R.id.tv_sendtime);
				viewHolder.tvContent = (TextView) convertView
						.findViewById(R.id.tv_chatcontent);
				viewHolder.tvSendStatus = (TextView) convertView
						.findViewById(R.id.tv_chat_tag);
				viewHolder.mLayout = (RelativeLayout) convertView
						.findViewById(R.id.chat_item_layout);
				viewHolder.tvSendStatus.setVisibility(View.INVISIBLE);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			if (entity.getIsSend() == GroupChatMsgEntity.COM_MSG) {
				viewHolder.tvSenderName.setText(entity.getSenderName());
			}
//			switch (entity.getIsSuccess()) {
//			case GroupChatMsgEntity.SEND_SUCCESS:
//				viewHolder.tvSendStatus.setText("发送成功");
//				break;
//			case GroupChatMsgEntity.SEND_FAILED:
//				viewHolder.tvSendStatus.setText("发送失败");
//				break;
//			case GroupChatMsgEntity.SEND_ING:
//				viewHolder.tvSendStatus.setText("发送中");
//				break;
//			default:
//				break;
//			}
			viewHolder.tvSendTime.setVisibility(View.INVISIBLE);
			if (entity.isShowTime()) {
				viewHolder.tvSendTime.setVisibility(View.VISIBLE);
				viewHolder.tvSendTime.setText(TimeUtil.getDateByMillisecond(
						entity.getmTime(), TimeUtil.FORMAT_DATETIME_24));
			}

			viewHolder.tvContent.setTag(entity);
//			viewHolder.tvContent.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					GroupChatMsgEntity entity = (GroupChatMsgEntity) v.getTag();
//					if (entity.getIsSuccess() == GroupChatMsgEntity.SEND_FAILED) {
//						entity.setIsSuccess(GroupChatMsgEntity.SEND_ING);
//						send(entity);
//					}
//				}
//			}); 
			if (entity.getMessage() != null) { 
				MessageBodyEntity body = JSON.parseObject(entity.getMessage(),
						MessageBodyEntity.class);
				SpannableString spannableString = FaceConversionUtil.getInstace()
						.getExpressionString(context, body.getContent());
				viewHolder.tvContent.setText(spannableString);
			}
			return convertView;
		}

		class ViewHolder {
			public TextView tvSendTime;
			public TextView tvContent;
			public TextView tvSendStatus;
			public RelativeLayout mLayout;
			public TextView tvSenderName;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mCommonReceiver);
	}
}
