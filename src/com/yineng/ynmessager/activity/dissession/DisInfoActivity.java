package com.yineng.ynmessager.activity.dissession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.contact.ContactPersonInfoActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.activity.session.FindChatRecordActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.db.dao.DisGroupChatDao;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.groupCreatedListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.HorizontalListView;

public class DisInfoActivity extends BaseActivity implements OnClickListener,
		ReceiveReqIQCallBack {

	private final int LOGOUT_SUCCESS = 0;
	private final int LOGOUT_FAILED = 1;
	private final int LOGOUT_NETWORK_ERROR = 2;
	private Button mAddPersonBtn;
	private Button mLogoutGroupBtn;
	private Button mGetChatrecordBtn;
	private TextView mGroupNameTV;
	private TextView mPersonNumTV;
	private HorizontalListView mHorizontalListView;
	private String mGroupId;
	private ContactGroup mContactGroup;
	private HorizontalListViewAdapter mHorizontalListViewAdapter;
	private ContactOrgDao mContactOrgDao;
	private RecentChatDao mRecentChatDao;
	private DisGroupChatDao mDisGroupChatDao;
	private XmppConnectionManager mXmppConnectionManager;
	private String mPacketId;

	/**
	 * 讨论组中的成员
	 */
	private List<User> mUserList = new ArrayList<User>();

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOGOUT_SUCCESS:
				ToastUtil.toastAlerMessage(DisInfoActivity.this, "退出成功",
						Toast.LENGTH_SHORT);
				finish();
				break;
			case LOGOUT_FAILED:
				ToastUtil.toastAlerMessage(DisInfoActivity.this, "退出失败",
						Toast.LENGTH_SHORT);
				break;
			case LOGOUT_NETWORK_ERROR:
				ToastUtil.toastAlerMessage(DisInfoActivity.this, "没有网络",
						Toast.LENGTH_SHORT);
				break;
			default:
				break;
			}
		}
	};

	private Context mContext;

	/**
	 * 讨论组名称
	 */
	private String mGroupName;
	private RelativeLayout mGroupNameRL;
	private RelativeLayout mGroupUserCountRL;
	private CommonReceiver mCommonReceiver;
	/**
	 * 是否退出讨论组对话框
	 */
	private Builder mQuitGroupDialog;
	protected boolean isFinishAcitivity = false;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Const.REQUEST_CODE && resultCode == Const.RESULT_CODE) {
			// 成功添加讨论组成员，刷新当前UI
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = DisInfoActivity.this;
		setContentView(R.layout.activity_dissession_info);
		init();
		initEvent();
	}

	/**
	 * 
	 */
	private void init() {
		mXmppConnectionManager = XmppConnectionManager.getInstance();
		mXmppConnectionManager
				.addReceiveReqIQCallBack("com:yineng:group", this);

		mPersonNumTV = (TextView) findViewById(R.id.tv_disgroup_info_personsum);
		mAddPersonBtn = (Button) findViewById(R.id.btn_disgroup_info_add_btn);
		mLogoutGroupBtn = (Button) findViewById(R.id.btn_disgroup_info_logout_group_btn);
		mGetChatrecordBtn = (Button) findViewById(R.id.btn_disgroup_info_chat_record_btn);
		mGroupNameTV = (TextView) findViewById(R.id.tv_disgroup_info_name);
		mHorizontalListView = (HorizontalListView) findViewById(R.id.gl_disgroup_info_HorizontalListView);
		mGroupNameRL= (RelativeLayout) findViewById(R.id.rl_disgroup_info_namelayout);
		mGroupUserCountRL= (RelativeLayout) findViewById(R.id.rl_disgroup_info_sum_layout);
		mContactOrgDao = new ContactOrgDao(this);

		mDisGroupChatDao = new DisGroupChatDao(this);
		mRecentChatDao = new RecentChatDao(this);
		mContactGroup = (ContactGroup) getIntent().getSerializableExtra(
				Const.INTENT_GROUP_EXTRA_NAME);
		//初始化该讨论组信息
		initDisGroupObject();
		initQuitGroupDialog();
		mHorizontalListViewAdapter = new HorizontalListViewAdapter(this);
		if (mUserList != null) {
			mPersonNumTV.setText(mUserList.size() + "人");
		} else {
			mPersonNumTV.setText(0 + "人");
		}

		if (mUserList != null) {
			mHorizontalListViewAdapter.setData(mUserList);
		}
		mHorizontalListView.setAdapter(mHorizontalListViewAdapter);
		mHorizontalListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				User mUser = (User) mHorizontalListViewAdapter.getItem(position);
				startPersonInfoActivity(mUser);
			}
		});
	}

	/**
	 * 
	 */
	private void initQuitGroupDialog() {
		mQuitGroupDialog = new AlertDialog.Builder(mContext);
		mQuitGroupDialog.setTitle("提示");
		mQuitGroupDialog.setMessage("确认要退出讨论组吗？");
		mQuitGroupDialog
				.setPositiveButton(
						"是",
						new android.content.DialogInterface.OnClickListener() {

							@Override
							public void onClick(
									android.content.DialogInterface dialog,
									int which) {
								if (NetWorkUtil.isNetworkAvailable(mContext)) {
									try {
										logoutAction();
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										mHandler.sendEmptyMessage(LOGOUT_FAILED);
									}
								} else {
									mHandler.sendEmptyMessage(LOGOUT_NETWORK_ERROR);
								}
							}
						}).setNegativeButton("否", null);		
	}

	/**
	 * 初始化该讨论组对象数据
	 */
	private void initDisGroupObject() {
		if (mContactGroup != null) {
			mGroupId = mContactGroup.getGroupName();
		} else {
			mGroupId = (String) getIntent().getCharSequenceExtra(
					DisCreateActivity.DIS_GROUP_ID_KEY);
			mContactGroup = mContactOrgDao.getGroupBeanById(mGroupId, Const.CONTACT_DISGROUP_TYPE);
		}
		mUserList = mContactOrgDao.queryUsersByGroupName(mGroupId, Const.CONTACT_DISGROUP_TYPE);
		initDisGroupChatTitle();
	}
	
	/**
	 * 初始化讨论组名称
	 */
	private void initDisGroupChatTitle() {
		if (mContactGroup != null) {
			if (mContactGroup.getSubject() != null && !mContactGroup.getSubject().isEmpty()) {
				mGroupName = mContactGroup.getSubject();
			} else {
				mGroupName = mContactGroup.getNaturalName();
			}
		} else {
			mGroupName = DisCreateActivity.calculateGroupName(mUserList,null);
		}
		mGroupNameTV.setText(mGroupName);
	}
	
	private void initEvent() {
		mAddPersonBtn.setOnClickListener(this);
		mLogoutGroupBtn.setOnClickListener(this);
		mGetChatrecordBtn.setOnClickListener(this);
		mPersonNumTV.setOnClickListener(this);
		mGroupNameRL.setOnClickListener(this);
		mGroupUserCountRL.setOnClickListener(this);
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
					if (!isFinishAcitivity) {
						mContactGroup = null;
						initDisGroupObject();
						if (mUserList != null) {
							mPersonNumTV.setText(mUserList.size() + "人");
						} else {
							mPersonNumTV.setText(0 + "人");
						}

						if (mUserList != null) {
							mHorizontalListViewAdapter.setData(mUserList);
							mHorizontalListViewAdapter.notifyDataSetChanged();
						}
					} else {
						isFinishAcitivity = false;
					}
				}
//				mContactGroup = mContactOrgDao.getGroupBeanById(mGroupId, Const.CONTACT_DISGROUP_TYPE);
//				initDisGroupChatTitle();
			}

//			@Override
//			public void updateGroupUserList() {
//				mUserList = mContactOrgDao.queryUsersByGroupName(mGroupId, Const.CONTACT_DISGROUP_TYPE);
//				if (mUserList != null) {
//					mPersonNumTV.setText(mUserList.size() + "人");
//				} else {
//					mPersonNumTV.setText(0 + "人");
//				}
//
//				if (mUserList != null) {
//					mHorizontalListViewAdapter.setData(mUserList);
//					mHorizontalListViewAdapter.notifyDataSetChanged();
//				}
//			}
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
//		addGroupUpdatedListener(mIntentFilter);
		registerReceiver(mCommonReceiver, mIntentFilter);		
	}

//	/**
//	 * 添加更新讨论组监听器
//	 * @param mIntentFilter 
//	 */
//	private void addGroupUpdatedListener(IntentFilter mIntentFilter) {
//		mCommonReceiver.setGroupCreatedListener(new groupCreatedListener() {
//			
//			@Override
//			public void groupCreated() {
//				mContactGroup = null;
//				initDisGroupObject();
//				if (mUserList != null) {
//					mPersonNumTV.setText(mUserList.size() + "人");
//				} else {
//					mPersonNumTV.setText(0 + "人");
//				}
//
//				if (mUserList != null) {
//					mHorizontalListViewAdapter.setData(mUserList);
//					mHorizontalListViewAdapter.notifyDataSetChanged();
//				}
//			}
//		});
//		mIntentFilter.addAction(Const.BROADCAST_ACTION_CREATE_GROUP);
//	}
	
	/**
	 * 
	 * 封装退出讨论组的IQ请求信息包，发送信息包
	 * 
	 * @throws IOException
	 */
	private void logoutAction() throws IOException {
		ReqIQ iq = new ReqIQ();
		mPacketId = iq.getPacketID();
		iq.setAction(5);
		iq.setNameSpace(Const.REQ_IQ_XMLNS_GET_GROUP);
		iq.setType(org.jivesoftware.smack.packet.IQ.Type.SET);
		String jsonString = "{\"groupName\"" + ":" + "\"" + mGroupId + "\"}";
		iq.setParamsJson(jsonString);
		mXmppConnectionManager.sendPacket(iq);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_disgroup_info_namelayout:
			final Intent renameIntent = new Intent(this, DisGroupRenameActivity.class);
			renameIntent.putExtra(Const.INTENT_GROUPID_EXTRA_NAME, mGroupId);
			renameIntent.putExtra(Const.INTENT_GROUP_EXTRA_NAME, mContactGroup);
			renameIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, Const.CONTACT_DISGROUP_TYPE);
			startActivity(renameIntent);
			break;
		case R.id.btn_disgroup_info_add_btn:
			final Intent intent = new Intent(this, DisAddActivity.class);
			intent.putExtra(DisAddActivity.DIS_GROUP_ID_KEY, mGroupId);
			intent.putExtra(DisGroupPersonList.GROUP_TYPE, Const.CONTACT_DISGROUP_TYPE);
			startActivityForResult(intent, Const.REQUEST_CODE);
			break;
		case R.id.btn_disgroup_info_chat_record_btn:
			Intent chatRecordintent = new Intent(this, FindChatRecordActivity.class);
			chatRecordintent.putExtra(GroupChatActivity.CHAT_ID_KEY, mGroupId);
			chatRecordintent.putExtra(GroupChatActivity.CHAT_TYPE_KEY, Const.CHAT_TYPE_DIS);
			startActivity(chatRecordintent);
			break;
		case R.id.btn_disgroup_info_logout_group_btn:
			mQuitGroupDialog.show();
			break;

		case R.id.rl_disgroup_info_sum_layout:
			final Intent intent1 = new Intent(this, DisGroupPersonList.class);
			intent1.putExtra(DisAddActivity.DIS_GROUP_ID_KEY, mGroupId);
			intent1.putExtra(DisGroupPersonList.GROUP_TYPE, Const.CONTACT_DISGROUP_TYPE);
			startActivityForResult(intent1, Const.REQUEST_CODE);
			break;

		default:
			break;
		}
	}

	public void back(View v) {
		finish();
	}

	@Override
	public void receivedReqIQResult(ReqIQResult packet) {
		// 接收到回执信息，判断退出操作是否成功
		L.i(TAG, "收到回执信息");
		if (mPacketId.equals(packet.getPacketID())) {
			if (packet.getCode() == 200) {
//				mDisGroupChatDao.deleteRecentChatByGroupId(mGroupId);
//				mRecentChatDao.deleteRecentChatByNumber(mGroupId);
				mHandler.sendEmptyMessage(LOGOUT_SUCCESS);
			} else {
				mHandler.sendEmptyMessage(LOGOUT_FAILED);
			}
		}
	}

	/**
	 * 打开个人资料页
	 * @param mUser
	 */
	public void startPersonInfoActivity(User mUser) {
		Intent infoIntent = new Intent(mContext,ContactPersonInfoActivity.class);
		ContactOrgDao mContactOrgDao = new ContactOrgDao(mContext);
		OrganizationTree mParentOrg = mContactOrgDao.queryUserRelationByUserNo(mUser.getUserNo());
		infoIntent.putExtra("parentOrg", mParentOrg);
		infoIntent.putExtra("contactInfo", mUser);
		mContext.startActivity(infoIntent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mCommonReceiver);
		mXmppConnectionManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP);
	}
	
}
