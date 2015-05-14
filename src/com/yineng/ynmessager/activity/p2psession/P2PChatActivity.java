package com.yineng.ynmessager.activity.p2psession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.P2PChatMsgDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReceiveMessageCallBack;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.view.face.FaceConversionUtil;
import com.yineng.ynmessager.view.face.FaceRelativeLayout;
import com.yineng.ynmessager.view.face.gif.AnimatedImageSpan;

/**
 * 
 * 用户聊天界面
 * 
 * @author YINENG
 * 
 */
public class P2PChatActivity extends BaseActivity implements OnClickListener,
		ReceiveMessageCallBack, ReceiveReqIQCallBack {
	public static final String ACCOUNT = "Account";
	public static final String CHOOSE_IMAGE_PATHS = "choose_image_paths";// 选择图片的路径列表
	public final int CONFIRM_RESULT_OK = 1;// 选择了图片
	public final int CONCEL_RESULT_CODE = 2;// 取消选择图片
	public final int REQUESTCODE = 0;// 打开图片选择界面的请求码

	private final String RECEIPT_BROADCAST = "receipt_broadcast";// 回执广播
	private final String BREAK_THREAD_TAG = "break_thread_tag";// 销毁线程的标识

	private final int GET_RECEIPT = 2;// 获得回执的处理
	private final int BROADCAST = 3;// 收到广播的处理
	private final int RECEIVE_MSG = 4;// 收到别人发送的消息
	private final int REFRESH_UI = 5;// 刷新UI
	private final int REFRESH_FACE_UI = 6;//刷新表情UI

	private final int PAGE_SIZE = 20;// 分页查询的信息数量
	private final long TIME_INTERVAL = 60 * 5 * 1000;// 时间在5分钟内的消息不显示时间
	private final long RECEIPT_TIME_INTERVAL = 30 * 1000;// 超过半分钟未收到回执，则认为发送消息失败

	private ClipboardManager mClipboard;
	private ReceiptThread mReceiptThread;
	private ReceipMessageQueue mReceipMessageQueue = new ReceipMessageQueue();// 回执消息处理队列
	private LinkedList<P2PChatMsgEntity> mMessageList = new LinkedList<P2PChatMsgEntity>();
	private List<String> mImagePathList = new CopyOnWriteArrayList<String>();
	
	private TextView mUnReadTV;
	private Button mSendBtn;
	private XmppConnectionManager mXmppConnManager;
	private EditText mEditContentET;
	private ListView mListView;
	private PullToRefreshListView mPullToRefreshListView;
	/**
	 * 对方的聊天帐号
	 */
	private String mChatUserNum;
	private P2PChatMsgAdapter mAdapter;

	private PendingIntent mReceiptPendingIntent;
	private AlarmManager mAlarmManager;
	private ReceiptBroadcastReceiver mReceiptBroadcastReceiver;
	private RecentChatDao mRecentChatDao;// 消息列表操作
	/**
	 * 消息数据库工具
	 */
	private P2PChatMsgDao mP2PChatMsgDao;
	private int mPage = 0;
	private int mUnreadNum = 0;// 未读消息数量
	private boolean isBottom = true;// 消息显示界面是否在底部
	private boolean notBreak = true;// 销毁线程的标识
	private boolean isNeedBroadcast = false;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_RECEIPT:// 获得回执
				P2PChatMsgEntity entity = getEntityOfList(mMessageList,
						(String) msg.obj);
				if (entity != null
						&& entity.getIsSuccess() == P2PChatMsgEntity.SEND_ING) {
					entity.setIsSuccess(P2PChatMsgEntity.SEND_SUCCESS);
					// updateMessageList(entity);
					mP2PChatMsgDao.saveOrUpdate(entity);
				}
				notifyAdapterDataSetChanged();
				break;
			case REFRESH_UI:// 刷新UI
				addToLastOrupdateMessageList((P2PChatMsgEntity) msg.obj);
				notifyAdapterDataSetChanged();
				break;
			case BROADCAST:// 回执超时处理广播
				notifyAdapterDataSetChanged();
				break;
			case RECEIVE_MSG:// 收到消息
				addToLastOrupdateMessageList((P2PChatMsgEntity) msg.obj);
				notifyAdapterDataSetChanged();
				break;
			case REFRESH_FACE_UI:
//				List<P2PChatMsgEntity> mAdapterList = (List<P2PChatMsgEntity>) msg.obj;
//				int firstVisiblePos = msg.arg1;
//				int lastVisiblePos = msg.arg2;
//				for (int i = 0; i < mAdapterList.size(); i++) {
//					P2PChatMsgEntity p2pChatMsgEntity = mAdapterList.get(i);
//					SpannableString tempSpan = p2pChatMsgEntity.getSpannableString();
//					if (tempSpan != null) {
//						AnimatedImageSpan[] tem = tempSpan.getSpans(0, tempSpan.length()-1, AnimatedImageSpan.class);
//						if (i >= firstVisiblePos && i <= lastVisiblePos) {
//							for (AnimatedImageSpan animatedImageSpan : tem) {
//								animatedImageSpan.runGifImg();
//							}
//						} else {
//							for (AnimatedImageSpan animatedImageSpan : tem) {
//								animatedImageSpan.pauseGifImg();
//							}
//						}
//					}
//				}
				break;
			default:
				break;
			}
		}

	};
	private TextView mChatUserNameTV;
	private User myUserInfo;
	/**
	 * 对方用户信息
	 */
	private User mChatUserInfo;

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

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == CONFIRM_RESULT_OK) {
			mImagePathList = data.getStringArrayListExtra(CHOOSE_IMAGE_PATHS);
			for (String path : mImagePathList) {
				Message msg = new Message();
				//
				// mSendMessageQueue.putEntity(entity);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initUserObjectData();
		setContentView(R.layout.activity_p2p_chat_layout);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		initialize();
		updateUnreadCount();// 更新未读记录条数为0
		
		//加载草稿
		String draft = mRecentChatDao.queryDraftByUserNo(mChatUserNum);
		SpannableString spannableDraft = FaceConversionUtil.getInstace().getExpressionString(this,draft);
		mEditContentET.setText(spannableDraft);
		mEditContentET.setSelection(spannableDraft.length());
		
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	{
		menu.setHeaderTitle(R.string.p2pChatActivity_contextMenuTitle);
		menu.add(Menu.NONE,0,Menu.NONE,R.string.p2pChatActivity_copyChatMsg);
		menu.add(Menu.NONE,1,Menu.NONE,R.string.p2pChatActivity_RetrySendMsg);
		menu.add(Menu.NONE,2,Menu.NONE,R.string.p2pChatActivity_deleteChatMsg);
	}
	
	@SuppressLint("NewApi")
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo)item.getMenuInfo();
		P2PChatMsgEntity viewItem = mAdapter.getItem(menuInfo.position - 1);  //-1是因为这里position是从1开始的
		
		int itemId = item.getItemId();
		switch(itemId)
		{
			case 0:  //复制文本
				mClipboard.setPrimaryClip(ClipData.newPlainText(TAG,viewItem.getSpannableString().toString()));
				showToast(R.string.common_copyToClipboard);
				break;
			case 1:  //重发
				break;
			case 2:  //删除
				mP2PChatMsgDao.deleteByPacketId(viewItem.getPacketId());
				refreshUIByPageIndex();
				break;
		}
		return true;
	}
	
	/**
	 * 初始化聊天对象数据
	 */
	private void initUserObjectData() {
		ContactOrgDao mContactOrgDao = new ContactOrgDao(getApplicationContext());
		mChatUserInfo = (User) getIntent().getParcelableExtra(Const.INTENT_USER_EXTRA_NAME);
		if (mChatUserInfo == null) {
			mChatUserNum = getIntent().getStringExtra(ACCOUNT);
			mChatUserInfo = mContactOrgDao.queryUserInfoByUserNo(mChatUserNum);
		}
		String myUserAccount = LastLoginUserSP.getInstance(P2PChatActivity.this).getUserAccount();
		myUserInfo = mContactOrgDao.queryUserInfoByUserNo(myUserAccount);
		mChatUserNum = mChatUserInfo.getUserNo();
	}

	public void initialize() {
		initTitleView();
		mUnReadTV = (TextView) findViewById(R.id.tv_p2p_chat_tips);
		mSendBtn = (Button) findViewById(R.id.btn_send);
		mEditContentET = (EditText) findViewById(R.id.et_sendmessage);
		mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pl_p2p_chat_pull_refresh_list);
		mListView = mPullToRefreshListView.getRefreshableView();
		mListView.setOnCreateContextMenuListener(this);
		
		mXmppConnManager = XmppConnectionManager.getInstance();
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent intent = new Intent(RECEIPT_BROADCAST);
		mReceiptPendingIntent = PendingIntent.getBroadcast(this, 1, intent, 1);
		mReceiptBroadcastReceiver = new ReceiptBroadcastReceiver();
		IntentFilter filter = new IntentFilter(RECEIPT_BROADCAST);
		registerReceiver(mReceiptBroadcastReceiver, filter);
		mP2PChatMsgDao = new P2PChatMsgDao(this);
		mRecentChatDao = new RecentChatDao(this);
		mClipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
		
		mAdapter = new P2PChatMsgAdapter(this);
		mListView.setAdapter(mAdapter);

		// 消息发送线程，回执处理线程
		mReceiptThread = new ReceiptThread();
		// mSendThread = new SendThread();
		mReceiptThread.start();
		// mSendThread.start();

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

	private void initTitleView() {
		View mCommonTitleView = findViewById(R.id.group_chat_title_layout);
		mChatUserNameTV = (TextView) mCommonTitleView.findViewById(R.id.chat_common_title_view_name);
		mChatUserNameTV.setText(mChatUserInfo.getUserName());
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
					L.e("child count "+view.getChildCount()+" "+view.getCount());
					
					int firstVisiblePos = view.getFirstVisiblePosition();
					int lastVisiblePos = view.getLastVisiblePosition();
//					List<P2PChatMsgEntity> mAdapterList = mAdapter.getData();
//					if (mAdapterList != null) {
//						if (firstVisiblePos > 0) {
//							firstVisiblePos = firstVisiblePos -1;
//						}
//						if (lastVisiblePos <= mAdapterList.size()) {
//							lastVisiblePos = lastVisiblePos -1;
//						} else {
//							lastVisiblePos = mAdapterList.size()-1;
//						}
//
//						android.os.Message msg = mHandler.obtainMessage();
//						msg.what = REFRESH_FACE_UI;
//						msg.arg1 = firstVisiblePos;
//						msg.arg2 = lastVisiblePos;
//						msg.obj = mAdapterList;
//						mHandler.sendMessage(msg);
//					}
					
					// 停止...
					if (lastVisiblePos == (view.getCount() - 1)) {
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
				default:
					break;
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
//				L.e("onScroll");
//				TextView tempView = (TextView) view.findViewById(R.id.tv_sendtime);
//				if (tempView != null) {
//					L.e("tag -- "+tempView.getTag());
//					SpannableString spannableString = (SpannableString) tempView.getTag();
//					if (spannableString != null) {
//						spannableString.setSpan(null, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//					}
//				}
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
	}

	/**
	 * 修改数据的isShowTime字段（第一条消息显示时间，5分钟内的消息不显示时间），然后刷新UI
	 */
	private void notifyAdapterDataSetChanged() {
		List<P2PChatMsgEntity> list = new ArrayList<P2PChatMsgEntity>();
		long preShowTime = 0;
		for (int i = 0; i < mMessageList.size(); i++) {
			P2PChatMsgEntity entity = mMessageList.get(i);
			if (i == 0) {
				Log.i(TAG, "时间：  " + entity.getmTime());
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

	/**
	 * 本地分页查询数据，刷新UI
	 */
	public void refreshUIByPageIndex() {
		isBottom = true;
		LinkedList<P2PChatMsgEntity> list = mP2PChatMsgDao
				.getChatMsgEntitiesByPage(mChatUserNum, mPage, PAGE_SIZE);
		if (list != null && !list.isEmpty()) {
			for (P2PChatMsgEntity mP2PChatMsgEntity : list) {
				mMessageList.addFirst(mP2PChatMsgEntity);
			}
			mPage++;
		}
		notifyAdapterDataSetChanged();
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
			Intent intent = new Intent(this, P2PChatInfoActivity.class);
			intent.putExtra(GroupChatActivity.CHAT_ID_KEY, mChatUserNum);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

	/**
	 * 发送消息
	 */
	private void clickSend() {
		isBottom = true;
		mUnreadNum = 0;
		String contString = mEditContentET.getText().toString();
		//如果没有输入消息，不能发送
		if (contString.isEmpty()) {
			return;
		}
		updateRecentChatList(contString);// 更新最近会话列表显示内容
		P2PChatMsgEntity mP2PChatMsgEntity = new P2PChatMsgEntity();
		Message msg = new Message();
		// 封装body
		MessageBodyEntity body = new MessageBodyEntity();
		body.setContent(contString);
//		body.setSendName(System.currentTimeMillis() + "");
		body.setSendName(myUserInfo.getUserName());
		body.setMsgType(Const.CHAT_TYPE_P2P);
		String jsonString = JSON.toJSONString(body);

		// 封装P2PChatMsgEntity对象
		mP2PChatMsgEntity.setIsSuccess(P2PChatMsgEntity.SEND_ING);
		mP2PChatMsgEntity.setChatUserNo(mChatUserNum);
		mP2PChatMsgEntity.setIsReaded(P2PChatMsgEntity.IS_READED);
		mP2PChatMsgEntity.setMessage(jsonString);
		mP2PChatMsgEntity.setMessageType(P2PChatMsgEntity.MESSAGE);
		mP2PChatMsgEntity.setIsSend(P2PChatMsgEntity.SEND);
		mP2PChatMsgEntity.setmTime(System.currentTimeMillis() + "");
		mP2PChatMsgEntity.setPacketId(msg.getPacketID());
		// 发送操作
		send(mP2PChatMsgEntity);
		mEditContentET.setText("");
	}

	public void send(P2PChatMsgEntity mP2PChatMsgEntity) {
		Message msg = new Message();
		android.os.Message message = mHandler.obtainMessage();
		message.obj = mP2PChatMsgEntity;
		message.what = REFRESH_UI;
		mHandler.sendMessage(message);
		switch (mP2PChatMsgEntity.getMessageType()) {
		case P2PChatMsgEntity.MESSAGE:
//			msg.setBody(GZIPUtil.compress(mP2PChatMsgEntity.getMessage()));
			msg.setBody(mP2PChatMsgEntity.getMessage());
			msg.setFrom(JIDUtil.getJIDByAccount(myUserInfo.getUserNo()));
			msg.setTo(JIDUtil.getSendToMsgAccount(mChatUserNum));
//			msg.setTo(JIDUtil.getJIDByAccount(mChatUserNum));
			msg.setType(Type.chat);
			msg.setPacketID(mP2PChatMsgEntity.getPacketId());
			// msg.setPacketID(entity.getPacketId());
			try {
				mXmppConnManager.sendPacket(msg);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				mP2PChatMsgDao.saveOrUpdate(mP2PChatMsgEntity);
			}
			break;
		case P2PChatMsgEntity.IMAGE:

			break;
		case P2PChatMsgEntity.FILE:

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
	private void addToLastOrupdateMessageList(P2PChatMsgEntity entity) {
		if (entity == null || mMessageList == null) {
			return;
		}
		for (int i = (mMessageList.size() - 1); i >= 0; i--) {
			if (entity.getPacketId().equals(mMessageList.get(i).getPacketId())) {
				mMessageList.set(i, entity);
				return;
			}
		}
		mMessageList.addLast(entity);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:
			clickSend();
			break;

		}
	}

	/**
	 * 
	 * Compare the time difference is greater than TIME_INTERVAL minutes
	 * 
	 * @param fisrtTime
	 * @param lastTime
	 * @return
	 */
	public boolean compareTime(long preTime, long nextTime) {
		if ((nextTime - preTime) >= TIME_INTERVAL) {
			return true;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		/**销毁gif引用**/
		destroyGifValue();
		
		notBreak = false;

		mXmppConnManager.removeReceiveMessageCallBack(mChatUserNum);
		mXmppConnManager.removeReceiveReqIQCallBack("com:yineng:receipt");
		unregisterReceiver(mReceiptBroadcastReceiver);
		mAlarmManager.cancel(mReceiptPendingIntent);

		// 销毁回执线程
		mReceipMessageQueue.putEntity(BREAK_THREAD_TAG);
		NoticesManager.getInstance(this).updateRecentChatList(mChatUserNum,
				Const.CHAT_TYPE_P2P);// 更新最近会话列表
		
		handleDraft();
		
		System.gc();
	}
	
	/**
	 * 草稿的处理
	 */
	private void handleDraft()
	{
		CharSequence draft = mEditContentET.getText();
		RecentChat thisChat = mRecentChatDao.isChatExist(mChatUserNum,Const.CHAT_TYPE_P2P);
		if(!TextUtils.isEmpty(draft) && thisChat == null)  //最近会话列表中没有存在，但已经输入草稿，需保存草稿的情况
		{
			updateRecentChatList("",draft.toString());
		}else if(TextUtils.isEmpty(draft) && thisChat !=null && TextUtils.isEmpty(thisChat.getContent()))  //存在于最近会话里面，但没有任何聊天记录，也没有草稿的情况
		{
			mRecentChatDao.deleteRecentChatById(thisChat.getId());  //从最近会话列表里面删除
		}else  //适用于一般情况，直接更新草稿
		{
			mRecentChatDao.updateDraft(mChatUserNum,draft.toString());
		}
	}

	@Override
	public void receivedMessage(P2PChatMsgEntity message) {
		// 此方法被线程调用，不能刷新UI
		Log.i(TAG, "收到消息");
		message.setIsReaded(P2PChatMsgEntity.IS_READED);
		message.setIsSend(P2PChatMsgEntity.COM_MSG);
		message.setIsReaded(P2PChatMsgEntity.IS_READED);
		message.setIsSuccess(P2PChatMsgEntity.SEND_SUCCESS);
		mP2PChatMsgDao.saveOrUpdate(message);
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
			Log.i(TAG, "广播接收，处理回执");
			for (P2PChatMsgEntity entity : mMessageList) {
				if (entity.getIsSuccess() == P2PChatMsgEntity.SEND_ING
						&& System.currentTimeMillis()
								- Long.valueOf(entity.getmTime()) > RECEIPT_TIME_INTERVAL) {
					entity.setIsSuccess(P2PChatMsgEntity.SEND_FAILED);
					mP2PChatMsgDao.saveOrUpdate(entity);
					flag = true;
				}
			}
			// UI数据有更新，则添加刷新UI的操作到主线程消息队列中
			if (flag) {
				mHandler.sendEmptyMessage(BROADCAST);
			} else {

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
	private P2PChatMsgEntity getEntityOfList(final List<P2PChatMsgEntity> list,
			final String packetId) {
		for (int i = (list.size() - 1); i >= 0; i--) {
			P2PChatMsgEntity entity = list.get(i);
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
					Log.i(TAG, "退出线程ReceiptThread");
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

	/**
	 * 更新未读记录
	 */
	private void updateUnreadCount() {
		mRecentChatDao.updateUnreadCount(mChatUserNum, Const.CHAT_TYPE_P2P, 0);// 设置未读记录为0
		Intent intent = new Intent();
		intent.setAction(Const.ACTION_UPDATE_UNREAD_COUNT);
		this.sendBroadcast(intent);
	}

	/**
	 * 把消息内容更新到最近会话列表
	 */
	private void updateRecentChatList(String content) {
		updateRecentChatList(content,"");
	}
	
	private void updateRecentChatList(String content,String draft)
	{
		RecentChat recentChat = new RecentChat();
		recentChat.setChatType(Const.CHAT_TYPE_P2P);
		recentChat.setUserNo(mChatUserNum);
		recentChat.setTitle(mRecentChatDao.getUserNameByUserId(
				mChatUserNum, Const.CHAT_TYPE_P2P));
		recentChat.setContent(content);
		recentChat.setDateTime(TimeUtil
				.getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24_mic));
		recentChat.setUnReadCount(0);
		recentChat.setDraft(draft);
		mRecentChatDao.saveRecentChat(recentChat);
	}

	@Override
	public void receivedMessage(GroupChatMsgEntity msg) {

	}
	
	/**
	 * 回收bitmap暂用的内存空间
	 */
	private void destroyGifValue() {
		List<P2PChatMsgEntity> mAdapterList = mAdapter.getData();
		if (mAdapterList != null) {
			for (P2PChatMsgEntity p2pChatMsgEntity : mAdapterList) {
				SpannableString tempSpan = p2pChatMsgEntity.getSpannableString();
				if (tempSpan != null) {
					AnimatedImageSpan[] tem = tempSpan.getSpans(0, tempSpan.length()-1, AnimatedImageSpan.class);
					for (AnimatedImageSpan animatedImageSpan : tem) {
						animatedImageSpan.recycleBitmaps();
					}
					tempSpan.removeSpan(tem);
				}
			}
		}
	}
}
