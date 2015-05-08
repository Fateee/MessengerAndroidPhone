package com.yineng.ynmessager.activity.groupsession;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.contact.ContactPersonInfoActivity;
import com.yineng.ynmessager.activity.dissession.DisAddActivity;
import com.yineng.ynmessager.activity.dissession.DisGroupPersonList;
import com.yineng.ynmessager.activity.dissession.DisGroupRenameActivity;
import com.yineng.ynmessager.activity.dissession.HorizontalListViewAdapter;
import com.yineng.ynmessager.activity.session.FindChatRecordActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.HorizontalListView;

public class GroupInfoActivity extends BaseActivity {
	private String mGroupId;
	private Context mContext;
	private ContactOrgDao mContactOrgDao;
	private ContactGroup mGroupBean;
	private TextView mGroupInfoNameTV;
	private TextView mGroupInfoMemberCountTV;
	private List<User> mUserList;
	private HorizontalListView mGroupInfoMemberListHL;
	private HorizontalListViewAdapter mHorizontalListViewAdapter;
	private String mGroupName;
	private ContactGroupUser mContactGroupUser;
	private CommonReceiver mCommonReceiver;
	protected boolean isFinishAcitivity = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_info_layout);
		findViews();
		initData();
	}
	
	public void initData() {
		mContext = GroupInfoActivity.this;
		mContactOrgDao = new ContactOrgDao(mContext);
		
		//初始化该群组信息
		initGroupObject(false);
		
		mContactGroupUser = mContactOrgDao.getContactGroupUserById(mGroupBean.getGroupName(), 
				AppController.getInstance().mSelfUser.getUserNo(), Const.CONTACT_GROUP_TYPE);
		
		mHorizontalListViewAdapter = new HorizontalListViewAdapter(this);
//		mGroupInfoNameTV.setText(mGroupName);
		if (mUserList != null) {
			mGroupInfoMemberCountTV.setText(mUserList.size()+"人");
			mHorizontalListViewAdapter.setData(mUserList);
		} else {
			mGroupInfoMemberCountTV.setText("0人");
		}
		mGroupInfoMemberListHL.setAdapter(mHorizontalListViewAdapter);
		mGroupInfoMemberListHL.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				User mUser = (User) mHorizontalListViewAdapter.getItem(pos);
				startPersonInfoActivity(mUser);
			}
		});
		addGroupUpdatedListener();
	}
	
	/**
	 * 添加群组信息更改监听器
	 */
	private void addGroupUpdatedListener() {
		mCommonReceiver = new CommonReceiver();
		mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {
			
			@Override
			public void updateGroupData(int mGroupType) {
//				mGroupBean = mContactOrgDao.getGroupBeanById(mGroupId, Const.CONTACT_GROUP_TYPE);
//				initDisGroupChatTitle();
				if (mGroupType == Const.CONTACT_GROUP_TYPE) {
					if (isFinishAcitivity) {
						initGroupObject(true);
						if (mUserList != null) {
							mGroupInfoMemberCountTV.setText(mUserList.size()+"人");
							mHorizontalListViewAdapter.setData(mUserList);
						} else {
							mGroupInfoMemberCountTV.setText("0人");
						}
						mHorizontalListViewAdapter.notifyDataSetChanged();
					} else {
						isFinishAcitivity  = false;
					}
				}
			}
		});
		mCommonReceiver.setIQuitGroupListener(new IQuitGroupListener() {

			@Override
			public void IQuitMyGroup(int mGroupType) {
				if (mGroupType == Const.CONTACT_GROUP_TYPE){
					isFinishAcitivity = true;
					finish();
				}
			}
		});
		IntentFilter mIntentFilter = new IntentFilter(Const.BROADCAST_ACTION_UPDATE_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_QUIT_GROUP);
		mIntentFilter.addAction(Const.BROADCAST_ACTION_I_QUIT_GROUP);
		registerReceiver(mCommonReceiver, mIntentFilter);		
	}
	
	/**
	 * 初始化该群组对象数据
	 */
	private void initGroupObject(boolean isUpdateGroup) {
		if (isUpdateGroup) {
			mGroupBean = null;
		} else {
			mGroupBean = (ContactGroup) getIntent().getSerializableExtra(Const.INTENT_GROUP_EXTRA_NAME);
		}
		
		if (mGroupBean != null) {
			mGroupId = mGroupBean.getGroupName();
		} else {
			mGroupId = getIntent().getStringExtra(GroupChatActivity.CHAT_ID_KEY);
			mGroupBean = mContactOrgDao.getGroupBeanById(mGroupId, Const.CONTACT_GROUP_TYPE);
		}
		mUserList = mContactOrgDao.queryUsersByGroupName(mGroupId, Const.CONTACT_GROUP_TYPE);
		initDisGroupChatTitle();
	}
	
	/**
	 * 初始化群组名称
	 */
	private void initDisGroupChatTitle() {
		if (mGroupBean != null) {
			if (mGroupBean.getSubject() != null && !mGroupBean.getSubject().isEmpty()) {
				mGroupName = mGroupBean.getSubject();
			} else {
				mGroupName = mGroupBean.getNaturalName();
			}
		} else {
			mGroupName = "群组";
		}
		mGroupInfoNameTV.setText(mGroupName);
	}

	public void findViews() {
		mGroupInfoNameTV = (TextView) findViewById(R.id.tv_group_info_name);
		mGroupInfoMemberCountTV = (TextView) findViewById(R.id.tv_group_info_member_count);
		mGroupInfoMemberListHL = (HorizontalListView) findViewById(R.id.hl_group_info_member_list);
	}
	
	public void onClickListener(View v) {
		switch (v.getId()) {
		case R.id.rl_group_info_namelayout:
			if (mContactGroupUser.getRole() == 10 || mContactGroupUser.getRole() == 20) {
				final Intent renameIntent = new Intent(this, DisGroupRenameActivity.class);
				renameIntent.putExtra(Const.INTENT_GROUPID_EXTRA_NAME, mGroupId);
				renameIntent.putExtra(Const.INTENT_GROUP_EXTRA_NAME, mGroupBean);
				renameIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, Const.CONTACT_GROUP_TYPE);
				startActivity(renameIntent);
			} else {
				ToastUtil.toastAlerMessageCenter(mContext, "没有权限", 1000);
			}
			break;
		case R.id.rl_group_member_layout:
			final Intent intent1 = new Intent(this, DisGroupPersonList.class);
			intent1.putExtra(DisAddActivity.DIS_GROUP_ID_KEY, mGroupId);
			intent1.putExtra(DisGroupPersonList.GROUP_TYPE, Const.CONTACT_GROUP_TYPE);
			startActivityForResult(intent1, Const.REQUEST_CODE);
			break;
		case R.id.btn_group_info_find_chat_record_btn:
			Intent intent = new Intent(this, FindChatRecordActivity.class);
			intent.putExtra(GroupChatActivity.CHAT_ID_KEY, mGroupId);
			intent.putExtra(GroupChatActivity.CHAT_TYPE_KEY, Const.CHAT_TYPE_GROUP);
			startActivity(intent);
			break;
		case R.id.btn_group_info_add_btn:
			if (mContactGroupUser.getRole() == 10 || mContactGroupUser.getRole() == 20) {
				final Intent addIntent = new Intent(this, DisAddActivity.class);
				addIntent.putExtra(DisAddActivity.DIS_GROUP_ID_KEY, mGroupId);
				addIntent.putExtra(DisGroupPersonList.GROUP_TYPE, Const.CONTACT_GROUP_TYPE);
				startActivityForResult(addIntent, Const.REQUEST_CODE);
			} else {
				ToastUtil.toastAlerMessageCenter(mContext, "没有权限", 1000);
			}

			break;
		default:
			break;
		}
	}
	
	public void back(View v) {
		finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Const.REQUEST_CODE && resultCode == Const.RESULT_CODE) {
//			// 成功添加讨论组成员，刷新当前UI
//			Bundle mAddedUsersBundle = data.getExtras(); //data为返回传的Intent
//		    ArrayList<ContactGroupUser> mAddedUsers = (ArrayList<ContactGroupUser>) mAddedUsersBundle.getSerializable(Const.GROUP_ADD_USER);//str即为回传的值
//		    for (ContactGroupUser user : mAddedUsers) {
//		    	mGroupInfoDao.insertOneGroupUserRelationData(user,Const.CONTACT_GROUP_TYPE);
//			}
//		    mUserList = mGroupInfoDao.queryUsersByGroupName(mGroupId, Const.CONTACT_GROUP_TYPE);
//			if (mUserList != null) {
//				mGroupInfoMemberCountTV.setText(mUserList.size()+"");
//				mHorizontalListViewAdapter.setData(mUserList);
//				mHorizontalListViewAdapter.notifyDataSetChanged();
//			}
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
	}
}
