package com.yineng.ynmessager.activity.p2psession;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.contact.ContactGroupOrgActivity;
import com.yineng.ynmessager.activity.contact.ContactPersonInfoActivity;
import com.yineng.ynmessager.activity.dissession.DisAddActivity;
import com.yineng.ynmessager.activity.dissession.DisCreateActivity;
import com.yineng.ynmessager.activity.dissession.DisGroupPersonList;
import com.yineng.ynmessager.activity.dissession.HorizontalListViewAdapter;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.activity.session.FindChatRecordActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.view.HorizontalListView;

public class P2PChatInfoActivity extends BaseActivity {

	private String mChatId;
	private Context mContext;
	private ContactOrgDao mContactOrgDao;
	private List<User> mUserList = new LinkedList<User>();
	private HorizontalListView mGroupInfoMemberListHL;
	private HorizontalListViewAdapter mHorizontalListViewAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_person_info_layout);
		initData();
	}
	
	public void initData() {
		mContext = P2PChatInfoActivity.this;
		mChatId = getIntent().getStringExtra(GroupChatActivity.CHAT_ID_KEY);
		mContactOrgDao = new ContactOrgDao(mContext);
		mHorizontalListViewAdapter = new HorizontalListViewAdapter(this);
		if (mChatId != null) {
			User mUser = mContactOrgDao.queryUserInfoByUserNo(mChatId);
			if (mUser != null) {
				mUserList.add(mUser);
			}
			mHorizontalListViewAdapter.setData(mUserList);
		}
		findViews();
		mGroupInfoMemberListHL.setAdapter(mHorizontalListViewAdapter);
		mGroupInfoMemberListHL.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				User mUser = (User) mHorizontalListViewAdapter.getItem(pos);
				startPersonInfoActivity(mUser);
			}
			
		});
	}
	
	public void findViews() {
		mGroupInfoMemberListHL = (HorizontalListView) findViewById(R.id.hl_chat_person_member_list);
	}
	
	public void onClickListener(View v) {
		switch (v.getId()) {
		case R.id.iv_chat_person_info_back:
			finish();
			break;
		case R.id.btn_chat_person_add_btn:
			final Intent intent = new Intent(mContext,DisCreateActivity.class);
			intent.putExtra("disGroupAddedUser", mUserList.get(0));
			startActivity(intent);
//			final Intent intent = new Intent(this, DisAddActivity.class);
//			intent.putExtra(DisAddActivity.DIS_GROUP_ID_KEY, mGroupId);
//			intent.putExtra(DisGroupPersonList.GROUP_TYPE, Const.CONTACT_DISGROUP_TYPE);
//			startActivity(intent);
			break;
		case R.id.btn_chat_person_find_chat_record_btn:
			Intent chatRecordintent = new Intent(this, FindChatRecordActivity.class);
			chatRecordintent.putExtra(GroupChatActivity.CHAT_ID_KEY, mChatId);
			chatRecordintent.putExtra(GroupChatActivity.CHAT_TYPE_KEY, Const.CHAT_TYPE_P2P);
			startActivity(chatRecordintent);
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * 打开个人资料页
	 * @param mUser
	 */
	public void startPersonInfoActivity(User mUser) {
		Intent infoIntent = new Intent(mContext,ContactPersonInfoActivity.class);
		OrganizationTree mParentOrg = mContactOrgDao.queryUserRelationByUserNo(mUser.getUserNo());
		infoIntent.putExtra("parentOrg", mParentOrg);
		infoIntent.putExtra("contactInfo", mUser);
		mContext.startActivity(infoIntent);
	}
}
