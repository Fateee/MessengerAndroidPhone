package com.yineng.ynmessager.activity.contact;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.dissession.DisChatActivity;
import com.yineng.ynmessager.activity.dissession.DisCreateActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.ClientInitConfig;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.groupCreatedListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.SearchContactEditText.onCancelSearchAnimationListener;

public class ContactGroupOrgActivity extends BaseActivity implements
		onCancelSearchAnimationListener {
	private List<ContactGroup> mContactGroupList;
	// private List<User> mChildOrgUser;
	private ListView mContactGroupOrgLV;
	private Context mContext;
	private TextView mContactOrgTitleTV;
	private Button mCreateDisGroupBtn;
	private String mChildGroupTitle;
	private ContactOrgDao mContactOrgDao;
	private int mGroupType = 0;

	private RelativeLayout mContactRelativeLayout;

	/*** 搜索联系人功能 ***/

	/**
	 * 显示搜索框动画
	 */
	protected final int SHOW_SEARCH_VIEW = 0;
	/**
	 * 取消搜索框动画
	 */
	protected final int CANCEL_SEARCH_VIEW = 1;
	/**
	 * 默认用于显示的搜索框
	 */
	private EditText mContactEditView;
	/**
	 * 上下动画滚动的高度
	 */
	protected float searchViewY;
	/**
	 * 自定义搜索框
	 */
	private SearchContactEditText mSearchContactEditText;

	private Handler mHandler = new Handler() {
		@SuppressLint("NewApi")
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_SEARCH_VIEW:
				mSearchContactEditText.show();
				mContactRelativeLayout.setY(-searchViewY);
				break;
			case CANCEL_SEARCH_VIEW:
				mContactRelativeLayout.setY(0);
				break;

			default:
				break;
			}
		};
	};
	private CommonReceiver mCommonReceiver;
	private GroupListAdapter mGroupListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = ContactGroupOrgActivity.this;
		initData();
		setContentView(R.layout.fragment_main_contact_layout);
		findViews();
		initListener();
	}

	private void initData() {
		mContactOrgDao = new ContactOrgDao(mContext);
		final Intent orgDataIntent = getIntent();
		mChildGroupTitle = orgDataIntent.getStringExtra("childGroupTitle");
		mContactGroupList = (List<ContactGroup>) orgDataIntent
				.getSerializableExtra(Const.INTENT_GROUP_LIST_EXTRA_NAME);
		mGroupType = orgDataIntent.getIntExtra("groupType", 0);
		if (mContactGroupList != null) {
			Log.e("GroupOrg", "mGroupOrg.size() == " + mContactGroupList.size());
		} else {
			mContactGroupList = (ArrayList<ContactGroup>) mContactOrgDao.queryGroupList(mGroupType);
		}
	}
	
	private void findViews() {
		findSearchContactView();
		mCreateDisGroupBtn = (Button) findViewById(R.id.contact_org_create_dis_group);
		mContactOrgTitleTV = (TextView) findViewById(R.id.contact_org_title);
		mContactGroupOrgLV = (ListView) findViewById(R.id.contact_org_listview);
		
		mContactOrgTitleTV.setText(mChildGroupTitle);
		mGroupListAdapter = new GroupListAdapter(mContext,mContactGroupList);
		mContactGroupOrgLV.setAdapter(mGroupListAdapter);
		if (mGroupType != Const.CONTACT_GROUP_TYPE) {
			mCreateDisGroupBtn.setVisibility(View.VISIBLE);
		}
	}
	
	private void initListener() {
		initSearchContactViewListener();
		mCreateDisGroupBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
				if (mClientInitConfig != null) {
					int maxDisGroups = Integer.parseInt(mClientInitConfig.getMax_disdisgroup_can_create());
					if (mContactGroupList.size() >= maxDisGroups) {
						ToastUtil.toastAlerMessage(mContext, "讨论组数量超过上限", Toast.LENGTH_SHORT);
						return;
					}
				}
				final Intent intent = new Intent(ContactGroupOrgActivity.this,
						DisCreateActivity.class);
				startActivity(intent);
			}
		});
		mContactGroupOrgLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					final long arg3) {
				// if (mChildOrg != null) {// 有组织机构
				// if (mChildOrgUser != null) {// 有用户
				// if (arg2 < mChildOrgUser.size()) {// 进入用户个人信息界面
				// return;
				// } else {
				// arg2 = arg2 - mChildOrgUser.size();
				// }
				// }
				//
				// /**************** 通过对象去获取组织机构 *********************/
				// // 表示进入子组织机构界面
				// OrganizationTree mOrgTree = mChildOrg.get(arg2);
				// Intent childOrgIntent = new Intent(mContext,
				// ContactChildOrgActivity.class);
				// childOrgIntent.putExtra("childOrgTitle",mOrgTree.getOrgName());
				// // 必须转为ArrayList 才能通过PutExtra把list传到子activity
				// ArrayList<OrganizationTree> tempList =
				// (ArrayList<OrganizationTree>)
				// mOrgTree.getChildOrgTreeMap().get(mOrgTree.getOrgNo());
				// if (tempList != null) {
				// childOrgIntent.putExtra("childOrg", tempList);
				// }
				// ArrayList<User> tempUsers = (ArrayList<User>)
				// mOrgTree.getmOrgUsers();
				// if (tempUsers != null && tempUsers.size() > 0) {
				// childOrgIntent.putExtra("childOrgUser", tempUsers);
				// }
				// startActivity(childOrgIntent);

				final ContactGroup tempGroup = mContactGroupList.get(arg2);
				final Intent chatIntent = new Intent();
				chatIntent.putExtra(Const.INTENT_GROUP_EXTRA_NAME, tempGroup);
				chatIntent.putExtra("Account", tempGroup.getGroupName());
				if (mGroupType == Const.CONTACT_GROUP_TYPE) { // 群组
					chatIntent.setClass(mContext, GroupChatActivity.class);
				} else { // 讨论组
					chatIntent.setClass(mContext, DisChatActivity.class);
				}
				startActivity(chatIntent);
			}
		});
		addGroupUpdatedListener();
	}
	
	/**
	 * 添加群组、讨论组信息更改监听器
	 */
	private void addGroupUpdatedListener() {
		mCommonReceiver = new CommonReceiver();
		mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {

			@Override
			public void updateGroupData(int mGroupType) {
				if (mGroupType == ContactGroupOrgActivity.this.mGroupType) {
					mContactGroupList = (ArrayList<ContactGroup>) mContactOrgDao.queryGroupList(mGroupType);
					mGroupListAdapter.setContactGroupList(mContactGroupList);
					mGroupListAdapter.notifyDataSetChanged();
				}
			}
		});
		IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_UPDATE_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_QUIT_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
		registerReceiver(mCommonReceiver, mIntentFilter);
	}
	
//	/**
//	 * 添加创建讨论组监听器
//	 * @param mIntentFilter 
//	 */
//	private void addGroupCreatedListener(IntentFilter mIntentFilter) {
//		mCommonReceiver.setGroupCreatedListener(new groupCreatedListener() {
//			
//			@Override
//			public void groupCreated() {
//				mContactGroupList = (ArrayList<ContactGroup>) mContactOrgDao.queryGroupList(mGroupType);
//				mGroupListAdapter.setContactGroupList(mContactGroupList);
//				mGroupListAdapter.notifyDataSetChanged();
//			}
//		});
//		mIntentFilter.addAction(Const.BROADCAST_ACTION_CREATE_GROUP);
//	}
	
	private void findSearchContactView() {
		mSearchContactEditText = new SearchContactEditText(mContext);
		mContactEditView = (EditText) findViewById(R.id.se_contact_org_search_dis);
		mContactRelativeLayout = (RelativeLayout) findViewById(R.id.ll_contact_org_frame);
	}

	private void initSearchContactViewListener() {
		mContactEditView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				showSearchContactAnimation();
			}
		});

		mSearchContactEditText.setOnCancelSearchAnimationListener(this);
	}

	TranslateAnimation showAnimation = null;
	TranslateAnimation cancelAnimation = null;
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

	public void showSearchContactAnimation() {
		final RelativeLayout.LayoutParams etParamTest = (RelativeLayout.LayoutParams) mContactEditView
				.getLayoutParams();
		searchViewY = mContactEditView.getY() - etParamTest.topMargin;
		showAnimation = new TranslateAnimation(0, 0, 0, -searchViewY);
		showAnimation.setDuration(200);
		showAnimation.setAnimationListener(showAnimationListener);
		mContactRelativeLayout.startAnimation(showAnimation);
	}

	@Override
	public void cancelSearchContactAnimation() {
		mSearchContactEditText.dismiss();
		mHandler.sendEmptyMessage(CANCEL_SEARCH_VIEW);
		cancelAnimation = new TranslateAnimation(0, 0, -searchViewY, 0);
		cancelAnimation.setDuration(200);
		mContactRelativeLayout.startAnimation(cancelAnimation);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mCommonReceiver);
	}
	
	/**
	 * 群/讨论组适配器
	 * @author yineng
	 *
	 */
	public class GroupListAdapter extends BaseAdapter {
		private Context nContext;
		private List<ContactGroup> nContactGroupList;
		
		public GroupListAdapter(Context context, List<ContactGroup> mContactGroupList) {
			nContext = context;
			nContactGroupList = mContactGroupList;
		}

		public void setContactGroupList(List<ContactGroup> mContactGroupList) {
			nContactGroupList = mContactGroupList;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(nContext).inflate(
						R.layout.contact_org_orglist_item, null);
			}
			if (nContactGroupList != null) {// 组织结构
				TextView mOrgTagTV = (TextView) convertView
						.findViewById(R.id.tv_contact_orglist_item_tag);
				TextView mOrgNameTV = (TextView) convertView
						.findViewById(R.id.tv_contact_orglist_item_name);
				TextView mOrgUserCountTV = (TextView) convertView
						.findViewById(R.id.tv_contact_orglist_item_count);
				if (position == 0) {
					mOrgTagTV.setVisibility(View.VISIBLE);
				} else {
					mOrgTagTV.setVisibility(View.GONE);
				}
				ContactGroup tempGroupOrg = nContactGroupList.get(position);
				String mGroupName;
				if (mGroupType == Const.CONTACT_GROUP_TYPE) {
					mGroupName = tempGroupOrg.getNaturalName();
				} else {
					if (tempGroupOrg.getSubject() != null && !tempGroupOrg.getSubject().isEmpty()) {
						mGroupName = tempGroupOrg.getSubject();
					} else {
						mGroupName = tempGroupOrg.getNaturalName();
					}
				}
				mOrgNameTV.setText(mGroupName);
				List<User> mTempList = mContactOrgDao.queryUsersByGroupName(
						tempGroupOrg.getGroupName(), mGroupType);
				int mOrgCount = 0;
				if (mTempList != null) {
					mOrgCount = mTempList.size();// 总人数
				}
				mOrgUserCountTV.setText(mOrgCount + "");
			}

			return convertView;
		}

		@Override
		public int getCount() {
			if (nContactGroupList == null) {
				return 0;
			}
			return nContactGroupList.size();
		}

		@Override
		public Object getItem(int arg0) {
			if (nContactGroupList == null) {
				return 0;
			}
			return nContactGroupList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

	}
}
