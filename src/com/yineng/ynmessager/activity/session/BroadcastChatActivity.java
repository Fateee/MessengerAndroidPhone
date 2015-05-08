package com.yineng.ynmessager.activity.session;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.BroadcastChat;
import com.yineng.ynmessager.db.dao.BroadcastChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.smack.ReceiveBroadcastChatCallBack;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 广播查看界面
 * 
 * @author Yutang
 * 
 */
public class BroadcastChatActivity extends BaseActivity implements
		ReceiveBroadcastChatCallBack {
	public static final String EXTRA_KEY_LIST = "list";
	public static final String EXTRA_KEY_INDEX = "index";
	/**
	 * 一页显示的条数
	 */
	private final int mPageSize = 10;
	// 显示广播内容的listView
	private PullToRefreshListView mRefreshListView;
	private LinkedList<BroadcastChat> mBroadcastList = new LinkedList<BroadcastChat>();
	private LinearLayout mEmptyView;
	private BroadcastChatDao mBroadcastChatDao;
	private RecentChatDao mRecentChatDao;// 消息列表操作
	private BroadcastListviewAdapter mBroadcastListviewAdapter;
	private Context mContext;
	/**
	 * 取消搜索框动画
	 */
	protected final int UPDATE_LIST_VIEW = 1;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressLint("NewApi")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_LIST_VIEW:
				mBroadcastListviewAdapter.notifyDataSetChanged();
				mRefreshListView.getRefreshableView().setSelection(
						mBroadcastListviewAdapter.getCount());
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session_broadcast_layout);
		init();
		initView();
		updateUnreadRecentChatCount();
		loadBroadcastByPage(0);
	}

	private void init() {
		mContext = this;
		mRecentChatDao = new RecentChatDao(mContext);
		mBroadcastChatDao = new BroadcastChatDao(mContext);
		mBroadcastListviewAdapter = new BroadcastListviewAdapter(mContext,
				mBroadcastList);
		XmppConnectionManager.getInstance().setReceiveBroadcastChatCallBack(
				this);
	}

	/**
	 * view初始化
	 */
	private void initView() {
		mRefreshListView = (PullToRefreshListView) findViewById(R.id.lv_session_broadcast_refresh);
		mEmptyView = (LinearLayout) findViewById(R.id.ll_session_broadcast_empty);
		mRefreshListView.setMode(Mode.BOTH);
		mRefreshListView.setEmptyView(mEmptyView);
		mRefreshListView.setAdapter(mBroadcastListviewAdapter);
		mRefreshListView
				.setOnRefreshListener(new OnRefreshListener2<ListView>() {
					@Override
					public void onPullDownToRefresh(// 下拉刷新
							PullToRefreshBase<ListView> refreshView) {
						if (mBroadcastList.size() >= mPageSize) {
							loadBroadcastByPage(mBroadcastList.size()
									/ mPageSize);
						} else {
							loadBroadcastByPage(0);
						}
					}

					@Override
					public void onPullUpToRefresh(// 上拉加载
							PullToRefreshBase<ListView> refreshView) {
						loadBroadcastByPage(0);
					}
				});
		// 单击查看记录
		mRefreshListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(mContext,
						BroadcastChatContentActivity.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable(EXTRA_KEY_LIST, mBroadcastList);
				bundle.putInt(EXTRA_KEY_INDEX, position - 1);
				intent.putExtras(bundle);
				startActivityForResult(intent, 0);				
			}
		});
	}

	@Override
	protected void onResume() {		
		super.onResume();
	}

	@Override
	protected void onStop() {		
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		XmppConnectionManager.getInstance().clearReceiveBroadcastChatCallBack();
		NoticesManager.getInstance(this).updateRecentChatList(Const.BROADCAST_ID,
				Const.CHAT_TYPE_BROADCAST);// 更新最近会话列表
		super.onDestroy();
	}

	@Override
	public void onReceiveBroadcastChat(BroadcastChat broadcast) {
		mBroadcastList.addLast(broadcast);
		mHandler.sendEmptyMessage(UPDATE_LIST_VIEW);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null) {
			@SuppressWarnings("unchecked")
			HashSet<Integer> set = (HashSet<Integer>) data
					.getSerializableExtra("set");

			if (set != null && set.size() > 0) {
				for (Integer i : set) {
					for (BroadcastChat bc : mBroadcastList) {
						if (i == bc.getId()) {
							bc.setIsRead(1);
							break;
						}
					}
				}
				mBroadcastListviewAdapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * 更新未读记录
	 */
	private void updateUnreadRecentChatCount() {
		mRecentChatDao.updateUnreadCount(Const.BROADCAST_ID,
				Const.CHAT_TYPE_BROADCAST, 0);// 设置未读记录为0
		Intent intent = new Intent();
		intent.setAction(Const.ACTION_UPDATE_UNREAD_COUNT);
		this.sendBroadcast(intent);
	}

	private void loadBroadcastByPage(int pages) {
		new AsyncTask<String, Integer, List<BroadcastChat>>() {
			private int mPageIndex = 0;

			// 访问数据库前执行
			protected void onPreExecute() {
				mRefreshListView.setRefreshing();
			};

			@Override
			protected List<BroadcastChat> doInBackground(String... params) {
				mPageIndex = Integer.valueOf(params[0]);
				return mBroadcastChatDao.queryBroadcastChatPage(mPageIndex,
						mPageSize);
			}

			// 返回数据
			protected void onPostExecute(List<BroadcastChat> result) {
				if (result != null) {
					if (mPageIndex == 0) {
						mBroadcastList.clear();
						mBroadcastList.addAll(result);
					} else {
						for (BroadcastChat bc : result) {
							if (!mBroadcastList.contains(bc)) {
								mBroadcastList.addFirst(bc);
							}
						}
					}
					mBroadcastListviewAdapter.notifyDataSetChanged();
				}
				mRefreshListView.onRefreshComplete();
			}
		}.execute(String.valueOf(pages));
	}

	public class BroadcastListviewAdapter extends BaseAdapter {
		private List<BroadcastChat> mList;
		private Context mContext;

		public BroadcastListviewAdapter(Context context,
				List<BroadcastChat> datalist) {
			mContext = context;
			this.mList = datalist;
		}

		@Override
		public int getCount() {			
			return mList.size();
		}

		@Override
		public Object getItem(int position) {			
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {			
			return position;
		}

		@Override
		public View getView(final int position, View v, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder viewHolder = null;
			BroadcastChat broadcast = mList.get(position);
			if (v == null) {
				viewHolder = new ViewHolder();
				v = LayoutInflater.from(mContext).inflate(
						R.layout.item_session_broadcastchat, null);
				viewHolder.itemlayout = (LinearLayout) v
						.findViewById(R.id.ll_session_broadcast_item_layout);
				viewHolder.sendername = (TextView) v
						.findViewById(R.id.tv_session_broadcast_item_sendername);
				viewHolder.headicon = (ImageView) v
						.findViewById(R.id.iv_session_broadcast_item_headicon);
				viewHolder.datatime = (TextView) v
						.findViewById(R.id.tv_session_broadcast_item_datetime);
				viewHolder.title = (TextView) v
						.findViewById(R.id.tv_session_broadcast_item_title);
				viewHolder.content = (TextView) v
						.findViewById(R.id.tv_session_broadcast_item_content);
				viewHolder.isread = (TextView) v
						.findViewById(R.id.tv_session_broadcast_item_isread);
				// 可移到item的mesure和layout中处理

				v.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) v.getTag();
			}
			if (broadcast.getIsRead() == 1) {
				viewHolder.isread.setVisibility(View.GONE);
			} else {
				viewHolder.isread.setVisibility(View.VISIBLE);
			}
			viewHolder.sendername.setText("发送者：" + broadcast.getUserName());
			viewHolder.datatime.setText(broadcast.getDateTime());
			viewHolder.title.setText("主题：" + broadcast.getTitle());
			viewHolder.content.setText(broadcast.getMessage());

			return v;
		}
	}

	private final class ViewHolder {
		@SuppressWarnings("unused")
		public LinearLayout itemlayout;
		@SuppressWarnings("unused")
		public ImageView headicon;
		public TextView sendername;
		public TextView title;
		public TextView datatime;
		public TextView isread;
		public TextView content;
	}
}
