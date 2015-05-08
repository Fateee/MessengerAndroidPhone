package com.yineng.ynmessager.activity.dissession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.contact.ContactPersonInfoActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.util.CharacterParserUtils;
import com.yineng.ynmessager.view.SideBar;
import com.yineng.ynmessager.view.SideBar.OnTouchingLetterChangedListener;

/**
 * ClassName: DisGroupSearch <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015年3月23日 上午9:05:31 <br/>
 * 
 * @author YINENG
 * @version
 * @since JDK 1.6
 */
public class DisGroupPersonList extends BaseActivity {

	public static final String GROUP_TYPE = "GROUP_TYPE";

	private static final int REFRESH_UI = 0;
	private SideBar mSidBar;
	private TextView mDialog;
	private String mGroupId;
	private EditText mSearchET;
	private ListView mListView;
	private List<User> mUserList = new ArrayList<User>();
	private ContactOrgDao mContactOrgDao;
	private MyAdapter mMyAdapter;
	private PinyinComparator mPinyinComparator;
	private int mType;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case REFRESH_UI:
				mMyAdapter.setData(mUserList);
				mMyAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		}

	};

	/**
	 * 群组、讨论组信息变更广播
	 */
	private CommonReceiver mCommonReceiver;

	protected boolean isFinishAcitivity = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_person_search);
		findView();
		initOther();
		initEvent();
	}

	private void findView() {
		mGroupId = (String) getIntent().getCharSequenceExtra(
				DisAddActivity.DIS_GROUP_ID_KEY);
		mType = getIntent().getIntExtra(GROUP_TYPE, -1);
		mDialog = (TextView) this.findViewById(R.id.tv_group_dialog);
		mSidBar = (SideBar) this.findViewById(R.id.sb_group_sidrbar);
		mListView = (ListView) findViewById(R.id.lv_group_list);
		mSearchET = (EditText) findViewById(R.id.et_group_search);

		mSidBar.setTextView(mDialog);
	}

	private void initOther() {
		mPinyinComparator = new PinyinComparator();
		mContactOrgDao = new ContactOrgDao(this);
		initListviewData();
		mListView.setAdapter(mMyAdapter);
	}
	
	/**
	 * 初始化列表数据
	 */
	private void initListviewData() {
		switch (mType) {
		case Const.CONTACT_GROUP_TYPE:
			mUserList = mContactOrgDao.queryUsersByGroupName(mGroupId,
					Const.CONTACT_GROUP_TYPE);
			break;
		case Const.CONTACT_DISGROUP_TYPE:
			mUserList = mContactOrgDao.queryUsersByGroupName(mGroupId,
					Const.CONTACT_DISGROUP_TYPE);
			break;

		default:
			break;
		}
		if (mMyAdapter == null) {
			mMyAdapter = new MyAdapter(this);
		}
		if (mUserList != null) {
			addLettersForList(mUserList);
			Collections.sort(mUserList, mPinyinComparator);
			mMyAdapter.setData(mUserList);
		}		
	}
	
	private void addLettersForList(List<User> list) {
		if (list == null || list.isEmpty()) {
			return;
		}
		for (User entity : list) {
			String pinyin = CharacterParserUtils.getInstance().getSelling(
					entity.getUserName() == null ? "YNMessenger" : entity
							.getUserName());
			String sortString = pinyin.substring(0, 1).toUpperCase();

			// 正则表达式，判断首字母是否是英文字母

			if (sortString.matches("[A-Z]")) {
				entity.setSortLetters(sortString.toUpperCase());
			} else {
				entity.setSortLetters("#");
			}
			OrganizationTree tempOrg = mContactOrgDao.queryUserRelationByUserNo(
					entity.getUserNo());
			if (tempOrg != null) {
				entity.setOrgName(tempOrg.getOrgName());
			}
		}
	}

	private void initEvent() {
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				User mUser = (User) mMyAdapter.getItem(position);
				startPersonInfoActivity(mUser);
			}
		});
		addGroupUpdatedListener();
		mSidBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				int position = mMyAdapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					mListView.setSelection(position);
				}
			}
		});

		mSearchET.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				List<User> list;
				if (s.length() > 0) {
					list = mContactOrgDao.searchUserFromGroup(mGroupId,
							mType, s.toString());

				} else {
					list = mContactOrgDao.queryUsersByGroupName(mGroupId,
							mType);
				}
				if (list != null) {
					mUserList = list;
					addLettersForList(mUserList);
				} else {
					mUserList.clear();
				}
				mHandler.sendEmptyMessage(REFRESH_UI);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	/**
	 * 添加讨论组信息更改监听器
	 */
	private void addGroupUpdatedListener() {
		mCommonReceiver = new CommonReceiver();
		mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {
			
			@Override
			public void updateGroupData(int mGroupType) {
				if (mGroupType == mType) {
					if (!isFinishAcitivity) {
						initListviewData();
						mMyAdapter.notifyDataSetChanged();
					} else {
						isFinishAcitivity  = false;
					}
				}
			}
		});
		mCommonReceiver.setIQuitGroupListener(new IQuitGroupListener() {
			
			@Override
			public void IQuitMyGroup(int mGroupType) {
				if (mGroupType == mType) {
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

	
	/**
	 * 打开个人资料页
	 * @param mUser
	 */
	public void startPersonInfoActivity(User mUser) {
		Intent infoIntent = new Intent(DisGroupPersonList.this,ContactPersonInfoActivity.class);
		ContactOrgDao mContactOrgDao = new ContactOrgDao(this);
		OrganizationTree mParentOrg = mContactOrgDao.queryUserRelationByUserNo(mUser.getUserNo());
		infoIntent.putExtra("parentOrg", mParentOrg);
		infoIntent.putExtra("contactInfo", mUser);
		startActivity(infoIntent);
	}
	
	class MyAdapter extends BaseAdapter implements SectionIndexer {
		private List<User> list = new ArrayList<User>();
		private Context context;

		public MyAdapter(Context context) {
			this.context = context;
		}

		public void setData(List<User> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			final User user = list.get(position);
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.activity_group_person_list_item, null);
				viewHolder.nameTV = (TextView) convertView
						.findViewById(R.id.tv_group_person_list_item_name);
				viewHolder.groupNameTV = (TextView) convertView
						.findViewById(R.id.tv_group_person_list_item_groupname);
				viewHolder.tagTV = (TextView) convertView
						.findViewById(R.id.tv_group_person_list_item_catalog);
				viewHolder.headImg = (ImageView) convertView
						.findViewById(R.id.iv_group_person_list_item_head);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			// 根据position获取分类的首字母的Char ascii值

			int section = getSectionForPosition(position);

			// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现

			if (position == getPositionForSection(section)) {
				viewHolder.tagTV.setVisibility(View.VISIBLE);
				viewHolder.tagTV.setText(user.getSortLetters());
			} else {
				viewHolder.tagTV.setVisibility(View.GONE);
			}
			viewHolder.nameTV.setText(user.getUserName());
			viewHolder.groupNameTV.setText(user.getOrgName());

			return convertView;

		}

		@Override
		public Object[] getSections() {
			return null;
		}

		/**
		 * 根据ListView的当前位置获取分类的首字母的Char ascii值
		 */
		public int getSectionForPosition(int position) {
			return list.get(position).getSortLetters().charAt(0);

		}

		/**
		 * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
		 */
		public int getPositionForSection(int section) {

			if (list == null || list.isEmpty()) {
				return -1;
			}
			for (int i = 0; i < getCount(); i++) {
				String sortStr = list.get(i).getSortLetters();
				char firstChar = sortStr.toUpperCase().charAt(0);
				if (firstChar == section) {
					return i;
				}
			}

			return -1;
		}

		class ViewHolder {
			TextView nameTV;
			TextView groupNameTV;
			ImageView headImg;
			TextView tagTV;
		}

	}

	class PinyinComparator implements Comparator<User> {
		public int compare(User o1, User o2) {
			if (o1.getSortLetters().equals("@")
					|| o2.getSortLetters().equals("#")) {
				return -1;
			} else if (o1.getSortLetters().equals("#")
					|| o2.getSortLetters().equals("@")) {
				return 1;
			} else {
				return o1.getSortLetters().compareTo(o2.getSortLetters());
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mCommonReceiver);
	}
}
