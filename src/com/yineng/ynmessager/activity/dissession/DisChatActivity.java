package com.yineng.ynmessager.activity.dissession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.imageloader.ImageLoaderActivity;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReceiveMessageCallBack;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.view.face.FaceConversionUtil;
import com.yineng.ynmessager.view.face.FaceRelativeLayout;

/**
 * 
 * 讨论组界面
 * 
 * @author YINENG
 *
 */
public class DisChatActivity extends BaseActivity implements OnClickListener,
		ReceiveMessageCallBack, ReceiveReqIQCallBack {

	private RecentChatDao mRecentChatDao;// 消息列表操作
	private Context mContext;
	private TextView mUnReadTV;
	private Button mSendBtn;
	private XmppConnectionManager mXmppConnManager;
	private EditText mEditContentET;
	private ListView mListView;
	private PullToRefreshListView mPullToRefreshListView;
	private String mChatUserNum;// 对方的聊天帐号
	private GroupChatMsgAdapter mAdapter;
	private PendingIntent mReceiptPendingIntent;
	private AlarmManager mAlarmManager;
	private ReceiptBroadcastReceiver mReceiptBroadcastReceiver;
	private static final String RECEIPT_BROADCAST = "receipt_broadcast";// 回执广播
	private ReceiptThread mReceiptThread;

	private ReceipMessageQueue mReceipMessageQueue = new ReceipMessageQueue();// 回执消息处理队列
	private LinkedList<GroupChatMsgEntity> mMessageList = new LinkedList<GroupChatMsgEntity>();
	private static final String BREAK_THREAD_TAG = "break_thread_tag";// 销毁线程的标识
	private static final long RECEIPT_TIME_INTERVAL = 30 * 1000;// 超过半分钟未收到回执，则认为发送消息失败
	private boolean notBreak = true;// 销毁线程的标识
	private static final int PAGE_SIZE = 20;// 分页查询的信息数量
	private int mPage = 0;
	private int mUnreadNum = 0;// 未读消息数量
	private boolean isBottom = true;// 消息显示界面是否在底部
	private ContactGroup mGroupObject;
	private DisGroupChatDao mDisGroupChatDao;

	private static final int GET_RECEIPT = 2;// 获得回执的处理
	private static final int BROADCAST = 3;// 收到广播的处理
	private static final int RECEIVE_MSG = 4;// 收到别人发送的消息
	private static final int REFRESH_UI = 5;// 刷新UI

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_RECEIPT:// 获得回执
				GroupChatMsgEntity entity = getEntityOfList(mMessageList,
						(String) msg.obj);
				if (entity != null
						&& entity.getIsSuccess() == GroupChatMsgEntity.SEND_ING) {
					entity.setIsSuccess(GroupChatMsgEntity.SEND_SUCCESS);
					// updateMessageList(entity);
					mDisGroupChatDao.saveOrUpdate(entity);
				}
				notifyAdapterDataSetChanged();
				break;
			case REFRESH_UI:// 刷新UI
				addToLastOrupdateMessageList((GroupChatMsgEntity) msg.obj);
				notifyAdapterDataSetChanged();
				break;
			case BROADCAST:// 回执超时处理广播
				notifyAdapterDataSetChanged();
				break;
			case RECEIVE_MSG:// 收到消息
				addToLastOrupdateMessageList((GroupChatMsgEntity) msg.obj);
				notifyAdapterDataSetChanged();
				break;
			default:
				break;
			}
		}

	};
	private String mGroupName;
	private ContactOrgDao mContactOrgDao;
	private User myUserInfo;
	private List<User> mUserList;
	/**
	 * 监听名称更改的广播
	 */
	private CommonReceiver mCommonReceiver;
	//是否销毁Activity
	protected boolean isFinishAcitivity = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = DisChatActivity.this;
		setContentView(R.layout.activity_group_chat_layout); 
		initialize();
		updateUnreadCount();// 更新未读记录条数为0
	}

	public void initialize() {
		mContactOrgDao = new ContactOrgDao(mContext);
		
		String myUserNo = LastLoginUserSP.getInstance(mContext)
				.getUserAccount();
		myUserInfo = mContactOrgDao.queryUserInfoByUserNo(myUserNo);
		mGroupObject = (ContactGroup) getIntent().getSerializableExtra(
				Const.INTENT_GROUP_EXTRA_NAME);
		//初始化该讨论组对象数据
		initDisGroupObject();
		mUnReadTV = (TextView) findViewById(R.id.tv_p2p_chat_tips);
		mSendBtn = (Button) findViewById(R.id.btn_send);
		mEditContentET = (EditText) findViewById(R.id.et_sendmessage);
		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.chat_pull_refresh_list);
		mListView = mPullToRefreshListView.getRefreshableView();

		mXmppConnManager = XmppConnectionManager.getInstance();
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(RECEIPT_BROADCAST);
		mReceiptPendingIntent = PendingIntent.getBroadcast(this, 1, intent, 1);
		mReceiptBroadcastReceiver = new ReceiptBroadcastReceiver();
		IntentFilter filter = new IntentFilter(RECEIPT_BROADCAST);
		registerReceiver(mReceiptBroadcastReceiver, filter);
		mDisGroupChatDao = new DisGroupChatDao(this);
		mRecentChatDao = new RecentChatDao(this);

		mAdapter = new GroupChatMsgAdapter(this);
		mListView.setAdapter(mAdapter);

		// 消息发送线程，回执处理线程
		mReceiptThread = new ReceiptThread();
		mReceiptThread.start();

		// 初始状态，从本地获得20条数据，刷新UI
		refreshUIByPageIndex();
		mXmppConnManager.addReceiveMessageCallBack(mChatUserNum, this);
		mXmppConnManager.addReceiveReqIQCallBack("com:yineng:receipt", this);
		// 回执处理广播
		mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
				RECEIPT_TIME_INTERVAL, RECEIPT_TIME_INTERVAL,
				mReceiptPendingIntent);

		initEvent();

	}

	/**
	 * 初始化该讨论组对象数据
	 */
	private void initDisGroupObject() {
		if (mGroupObject != null) {
			mChatUserNum = mGroupObject.getGroupName();
		} else {
			mChatUserNum = getIntent().getStringExtra("Account");
			mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
		}
		mUserList = mContactOrgDao.queryUsersByGroupName(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
		initDisGroupChatTitle();
	}

	/**
	 * 初始化讨论组名称
	 */
	private void initDisGroupChatTitle() {
		if (mGroupObject != null) {
			if (mGroupObject.getSubject() != null && !mGroupObject.getSubject().isEmpty()) {
				mGroupName = mGroupObject.getSubject();
			} else {
				mGroupName = mGroupObject.getNaturalName();
			}
		} else {
			mGroupName = "讨论组";
		}
		TextView mTitleTextView = (TextView) findViewById(R.id.chat_common_title_view_name);
		if (mUserList != null) {
			mTitleTextView.setText(mGroupName+"("+mUserList.size()+")");
		} else {
			mTitleTextView.setText(mGroupName);
		}
	}

	private void initEvent() {
		mSendBtn.setOnClickListener(this);

		mPullToRefreshListView
				.setOnRefreshListener(new OnRefreshListener<ListView>() {

					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						mHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								refreshUIByPageIndex();
								mHandler.sendEmptyMessage(0);
								mPullToRefreshListView.onRefreshComplete();
							}
						}, 3000);
					}
				});
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_IDLE: //
					// 停止...
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						isBottom = true;
						mUnreadNum = 0;
						refreshUnreadNumUI();
					} else {
						isBottom = false;
					}
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					// 正在滑动...
					break;
				case OnScrollListener.SCROLL_STATE_FLING:
					// 开始滚动...
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});
		mListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(arg1.getAction()==MotionEvent.ACTION_DOWN){
					((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout)).hideFaceView();
				}
				return false;
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
				if (mGroupType == Const.CONTACT_DISGROUP_TYPE) {
//					mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
//					initDisGroupChatTitle();
					if (!isFinishAcitivity) {
						mGroupObject = null;
						initDisGroupObject();
					} else {
						isFinishAcitivity = false;
					}
				}
			}
		});
		mCommonReceiver.setIQuitGroupListener(new IQuitGroupListener() {
			
			@Override
			public void IQuitMyGroup(int mGroupType) {
				if (mGroupType == Const.CONTACT_DISGROUP_TYPE) {
					isFinishAcitivity  = true;
					finish();	
				}
			}
		});
		IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_UPDATE_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_QUIT_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
		registerReceiver(mCommonReceiver, mIntentFilter);		
	}
	
	@Override
	public void onBackPressed() {
		if (((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout))
				.hideFaceView()) {
			return;
		}
		super.onBackPressed();
	}
	
	public void onTitleViewClickListener(View v) {
		switch (v.getId()) {
		case R.id.chat_common_title_view_back:
			((FaceRelativeLayout) findViewById(R.id.FaceRelativeLayout)).hideFaceView();
			finish();
			break;
		case R.id.chat_common_title_view_infomation:
			Intent intent = new Intent(this, DisInfoActivity.class);
			intent.putExtra(Const.INTENT_GROUP_EXTRA_NAME, mGroupObject);
			intent.putExtra(DisCreateActivity.DIS_GROUP_ID_KEY, mChatUserNum);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * 更新整个消息链表，跟新本地存储信息
	 * 
	 * @author YINENG
	 * 
	 */
	class ReceiptBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean flag = false;
			Log.i("group", "广播接收，处理回执");
			for (GroupChatMsgEntity entity : mMessageList) {
				synchronized (mMessageList) {
					if (entity.getIsSuccess() == GroupChatMsgEntity.SEND_ING
							&& System.currentTimeMillis()
									- Long.valueOf(entity.getmTime()) > RECEIPT_TIME_INTERVAL) {
						entity.setIsSuccess(GroupChatMsgEntity.SEND_FAILED);
						// updateMessageList(entity);
						mDisGroupChatDao.saveOrUpdate(entity);
						flag = true;
					}
				}
			}
			// UI数据有更新，则添加刷新UI的操作到主线程消息队列中
			if (flag) {
				mHandler.sendEmptyMessage(BROADCAST);
			}
		}
	}

	/**
	 * 
	 * 返回指定packetId在list中的存储位置， 如果返回-1，则表示 list中未存储
	 * 
	 * @param packetId
	 * @return
	 */
	private GroupChatMsgEntity getEntityOfList(
			LinkedList<GroupChatMsgEntity> List, String packetId) {
		for (int i = (List.size() - 1); i >= 0; i--) {
			GroupChatMsgEntity entity = List.get(i);
			if (packetId != null
					&& packetId.trim().equals(entity.getPacketId().trim())) {
				return entity;
			}
		}
		return null;
	}

	/**
	 * 
	 * 收到回执的处理线程
	 * 
	 * @author YINENG
	 * 
	 */
	class ReceiptThread extends Thread {
		@Override
		public void run() {
			super.run();
			while (true) {
				String packetId = mReceipMessageQueue.getEntity();
				if (!notBreak && packetId.equals(BREAK_THREAD_TAG)) {
					break;
				} else {
					android.os.Message message = mHandler.obtainMessage();
					message.obj = packetId;
					message.what = GET_RECEIPT;
					mHandler.sendMessage(message);
				}
			}
		}
	}

	/**
	 * 
	 * 收到回执消息的队列
	 * 
	 * @author YINENG
	 * 
	 */
	class ReceipMessageQueue {
		private final LinkedList<String> queue = new LinkedList<String>();

		public synchronized String getEntity() {
			while (queue.size() <= 0) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return queue.removeFirst();
		}

		public synchronized void putEntity(String entity) {

			queue.addLast(entity);
			notifyAll();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:
			clickSend();
			break;
		default:
			break;

		}
	}

	/**
	 *
	 * 添加或更新消息列表
	 *
	 * @param entity
	 */
	private void addToLastOrupdateMessageList(GroupChatMsgEntity entity) {
		if (entity == null || mMessageList == null) {
			return;
		}
		for (int i = (mMessageList.size() - 1); i > 0; i--) {
			if (entity.getPacketId().equals(mMessageList.get(i).getPacketId())) {
				mMessageList.set(i, entity);
				return;
			}
		}
		mMessageList.addLast(entity);
	}

	/**
	 * 本地分页查询数据，刷新UI
	 */
	public void refreshUIByPageIndex() {
		isBottom = true;
		LinkedList<GroupChatMsgEntity> list = mDisGroupChatDao
				.getChatMsgEntitiesByPage(mChatUserNum, mPage, PAGE_SIZE);
		if (list != null && !list.isEmpty()) {
			for (GroupChatMsgEntity mGroupChatMsgEntity : list) {
				mMessageList.addFirst(mGroupChatMsgEntity);
			}
			mPage++;
		}
		notifyAdapterDataSetChanged();
	}

	@Override
	public void receivedMessage(GroupChatMsgEntity message) {
		// 此方法被线程调用，不能刷新UI
		message.setIsReaded(GroupChatMsgEntity.IS_READED);
		message.setIsSend(GroupChatMsgEntity.COM_MSG);
		message.setIsSuccess(GroupChatMsgEntity.SEND_SUCCESS);
		mDisGroupChatDao.saveOrUpdate(message);
		android.os.Message msgMessage = mHandler.obtainMessage();
		msgMessage.what = RECEIVE_MSG;
		msgMessage.obj = message;
		mHandler.sendMessage(msgMessage);
	}

	@Override
	public void receivedReqIQResult(ReqIQResult packet) {
		mReceipMessageQueue.putEntity(packet.getId());
	}

	/**
	 * 修改数据的isShowTime字段（第一条消息显示时间，5分钟内的消息不显示时间），然后刷新UI
	 */
	private void notifyAdapterDataSetChanged() {
		List<GroupChatMsgEntity> list = new ArrayList<GroupChatMsgEntity>();
		long preShowTime = 0;
		for (int i = 0; i < mMessageList.size(); i++) {
			GroupChatMsgEntity entity = mMessageList.get(i);
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
		mAdapter.setData(list);
		mAdapter.notifyDataSetChanged();
		refreshUnreadNumUI();

	}

	private void clickSend() {
		isBottom = true;
		mUnreadNum = 0;
		String contString = mEditContentET.getText().toString();
		//如果没有输入消息，不能发送
		if (contString.isEmpty()) {
			return;
		}
		updateRecentChatList(contString);// 更新最近会话列表显示内容
		GroupChatMsgEntity mGroupChatMsgEntity = new GroupChatMsgEntity();
		Message msg = new Message();
		// 封装body
		MessageBodyEntity body = new MessageBodyEntity();
		body.setContent(contString);
		// body.setSendName(System.currentTimeMillis() + "");
		body.setSendName(myUserInfo.getUserName());
		body.setMsgType(Const.CHAT_TYPE_DIS);
		String jsonString = JSON.toJSONString(body);
		mGroupChatMsgEntity.setGroupId(mChatUserNum);
		mGroupChatMsgEntity.setChatUserNo(myUserInfo.getUserNo());
		mGroupChatMsgEntity.setIsSuccess(GroupChatMsgEntity.SEND_ING);
		mGroupChatMsgEntity.setIsReaded(GroupChatMsgEntity.IS_READED);
		mGroupChatMsgEntity.setMessage(jsonString);
		mGroupChatMsgEntity.setMessageType(GroupChatMsgEntity.MESSAGE);
		mGroupChatMsgEntity.setIsSend(GroupChatMsgEntity.SEND);
		mGroupChatMsgEntity.setmTime(System.currentTimeMillis() + "");
		mGroupChatMsgEntity.setPacketId(msg.getPacketID());
		mEditContentET.setText("");
		// 发送操作
		send(mGroupChatMsgEntity);
	}

	public void send(GroupChatMsgEntity mGroupChatMsgEntity) {
		Message msg = new Message();
		android.os.Message message = mHandler.obtainMessage();
		message.obj = mGroupChatMsgEntity;
		message.what = REFRESH_UI;
		mHandler.sendMessage(message);
		switch (mGroupChatMsgEntity.getMessageType()) {
		case GroupChatMsgEntity.MESSAGE:
			// msg.setBody(GZIPUtil.compress(entity.getMessage()));
			msg.setBody(mGroupChatMsgEntity.getMessage());
			msg.setFrom(JIDUtil.getJIDByAccount(myUserInfo.getUserNo()));
//			msg.setTo(JIDUtil.getSendToMsgAccount(mChatUserNum));
			msg.setTo(JIDUtil.getGroupJIDByAccount(mChatUserNum));
			msg.setType(Type.groupchat);
			msg.setPacketID(mGroupChatMsgEntity.getPacketId());
			try {
				mXmppConnManager.sendPacket(msg);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mDisGroupChatDao.saveOrUpdate(mGroupChatMsgEntity);
			}
			break;
		case GroupChatMsgEntity.IMAGE:

			break;
		case GroupChatMsgEntity.FILE:

			break;

		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		notBreak = false;

		mXmppConnManager.removeReceiveMessageCallBack(mChatUserNum);
		mXmppConnManager.removeReceiveReqIQCallBack("com:yineng:receipt");
		unregisterReceiver(mReceiptBroadcastReceiver);
		mAlarmManager.cancel(mReceiptPendingIntent);

		// 销毁回执线程
		mReceipMessageQueue.putEntity(BREAK_THREAD_TAG);

		NoticesManager.getInstance(this).updateRecentChatList(mChatUserNum,
				Const.CHAT_TYPE_DIS);// 更新最近会话列表
		unregisterReceiver(mCommonReceiver);
	}

	/**
	 * 更新未读记录
	 */
	private void updateUnreadCount() {
		mRecentChatDao.updateUnreadCount(mChatUserNum, Const.CHAT_TYPE_DIS, 0);// 设置未读记录为0
		Intent intent = new Intent();
		intent.setAction(Const.ACTION_UPDATE_UNREAD_COUNT);
		this.sendBroadcast(intent);
	}

	/**
	 * 刷新未读消息提示数
	 */
	private void refreshUnreadNumUI() {
		if (!isBottom) {
			mUnreadNum++;
			mUnReadTV.setVisibility(View.VISIBLE);
			mUnReadTV.setText(mUnreadNum + "");
		} else {
			mUnReadTV.setVisibility(View.GONE);
			mUnreadNum = 0;
			mListView.setSelection(mAdapter.getCount());
		}
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

	/**
	 * 把消息内容更新到最近会话列表
	 */
	private void updateRecentChatList(String content) {
		RecentChat recentChat = new RecentChat();
		recentChat.setChatType(Const.CHAT_TYPE_DIS);
		recentChat.setUserNo(mChatUserNum);
		//保存名字
		if (mGroupObject == null) {
			mGroupObject = mContactOrgDao.getGroupBeanById(mChatUserNum, Const.CONTACT_DISGROUP_TYPE);
			if (mGroupObject.getSubject() != null && !mGroupObject.getSubject().isEmpty()) {
				mGroupName = mGroupObject.getSubject();
			} else {
				mGroupName = mGroupObject.getNaturalName();
			}
		}
		recentChat.setTitle(mGroupName);
		recentChat.setContent(content);
		recentChat.setDateTime(TimeUtil
				.getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24_mic));
		recentChat.setUnReadCount(0);
		mRecentChatDao.saveRecentChat(recentChat);
	}

	public class GroupChatMsgAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private Context context;
		private List<GroupChatMsgEntity> coll = new ArrayList<GroupChatMsgEntity>();

		public GroupChatMsgAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
			this.context = context;
		}

		public void setData(List<GroupChatMsgEntity> coll) {
			this.coll = coll;
		}

		@Override
		public int getCount() {
			return coll.size();
		}

		@Override
		public Object getItem(int position) {
			return coll.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getItemViewType(int position) {
			GroupChatMsgEntity entity = coll.get(position);
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
			GroupChatMsgEntity entity = coll.get(position);
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				if (entity.getIsSend() == GroupChatMsgEntity.COM_MSG) {
					convertView = mInflater.inflate(
							R.layout.chatting_item_msg_text_left, null);
					viewHolder.tvSenderName = (TextView) convertView.findViewById(R.id.tv_chat_sender_name);
					viewHolder.tvSenderName.setVisibility(View.VISIBLE);
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
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			if (entity.getIsSend() == GroupChatMsgEntity.COM_MSG) {
				viewHolder.tvSendStatus.setVisibility(View.INVISIBLE);
				viewHolder.tvSenderName.setText(entity.getSenderName());
			}
			switch (entity.getIsSuccess()) {
			case GroupChatMsgEntity.SEND_SUCCESS:
				viewHolder.tvSendStatus.setText("发送成功");
				break;
			case GroupChatMsgEntity.SEND_FAILED:
				viewHolder.tvSendStatus.setText("发送失败");
				break;
			case GroupChatMsgEntity.SEND_ING:
				viewHolder.tvSendStatus.setText("发送中");
				break;
			default:
				break;
			}
			viewHolder.tvSendTime.setVisibility(View.INVISIBLE);
			if (entity.isShowTime()) {
				viewHolder.tvSendTime.setVisibility(View.VISIBLE);
				viewHolder.tvSendTime.setText(TimeUtil.getDateByMillisecond(
						entity.getmTime(), TimeUtil.FORMAT_DATETIME_24));
			}

			viewHolder.tvContent.setTag(entity);
			viewHolder.tvContent.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					GroupChatMsgEntity entity = (GroupChatMsgEntity) v.getTag();
					if (entity.getIsSuccess() == GroupChatMsgEntity.SEND_FAILED) {
						entity.setIsSuccess(GroupChatMsgEntity.SEND_ING);
						send(entity);
					}
				}
			});
			if (entity.getMessage() != null) {
				L.i(TAG, "entity:   " + entity.getMessage());
				SpannableString  spannableString;
				if (viewHolder.tvSendTime.getTag() != null) {
					spannableString = (SpannableString) viewHolder.tvSendTime.getTag();
				} else {
					MessageBodyEntity body = JSON.parseObject(entity.getMessage(),
							MessageBodyEntity.class);
//					SpannableString spannableString = FaceConversionUtil.getInstace()
//							.getExpressionString(context, body.getContent());
					
					// 对内容做处理
					spannableString = FaceConversionUtil
							.getInstace().handlerContent(this.context,viewHolder.tvContent,
							body.getContent());
					viewHolder.tvSendTime.setTag(spannableString);
				}
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

	public void back(View v) {
		finish();
	}

	@Override
	public void receivedMessage(P2PChatMsgEntity msg) {

	}
}
