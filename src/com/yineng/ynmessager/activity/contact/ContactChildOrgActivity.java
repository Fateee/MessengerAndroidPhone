package com.yineng.ynmessager.activity.contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.Presence;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.adapter.ContactCommonAdapter;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.app.ContactActivityManager;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.smack.ReceivePresenceCallBack;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.SearchContactEditText.onCancelSearchAnimationListener;

public class ContactChildOrgActivity extends BaseActivity implements
		ReceivePresenceCallBack, onCancelSearchAnimationListener {

	private final int VIEW_ENTER = 0;
	private final int VIEW_BACK = 1;
	private List<OrganizationTree> mChildOrg;
	private List<User> mChildOrgUser;
	private List<Object> mChildContactObjectList = new ArrayList<Object>();
	private String mParentOrgNo;
	private ListView mContactOrgLV;
	private Context mContext;
	private TextView mContactOrgTitleTV;
	private OrganizationTree mParentOrg;
	private ContactOrgDao mContactOrgDao;
	// private SampleAdapter mChildAdapter;
	private ContactCommonAdapter mChildContactAdapter;

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

	@SuppressLint({ "HandlerLeak", "NewApi" })
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SHOW_SEARCH_VIEW:
				mSearchContactEditText.show();
				mContactRelativeLayout.setY(-searchViewY);
				break;
			case CANCEL_SEARCH_VIEW:
				mContactRelativeLayout.setY(0);
				break;
			case Const.USER_STATUS_CHANGED:
				mChildContactAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};

	/**
	 * 搜索框
	 */
	private SearchContactEditText mSearchContactEditText;
	private Button mContactOrgBackBT;

	/**
	 * 组织机构快速跳转的弹出界面
	 */
	private PopupWindow mOrgTitlePopWindow;
	/**
	 * 弹出界面的路径列表
	 */
	private ListView mOrgTitlePopwinList;
	private View view;
	/**
	 * 弹出搜索框的动画
	 */
	private TranslateAnimation showAnimation = null;
	/**
	 * 隐藏搜索框的动画
	 */
	private TranslateAnimation cancelAnimation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = ContactChildOrgActivity.this;
		initData();
		setContentView(R.layout.fragment_main_contact_layout);
		findViews();
		initListenerByCommonAdapter();
	}

	private void initData() {
		Intent orgDataIntent = getIntent();
		mParentOrg = (OrganizationTree) orgDataIntent
				.getSerializableExtra("parentOrg");
		if (mParentOrg == null) {
			// orgDataIntent.getStringExtra("childOrgTitle");
			mParentOrgNo = orgDataIntent.getStringExtra("parentOrgNo");
		}

		int isViewBack = orgDataIntent.getIntExtra("isBack", 0);
		if (isViewBack == 0) {
			ContactActivityManager.mTitleOrgList.add(mParentOrg);
		}

		mChildOrg = (List<OrganizationTree>) orgDataIntent
				.getSerializableExtra("childOrgList");
		mChildOrgUser = (List<User>) orgDataIntent
				.getSerializableExtra("childOrgUser");
		if (mChildOrgUser != null) {
			Log.e("childOrgUser",
					"childOrgUser.size() == " + mChildOrgUser.size());
			mChildContactObjectList.addAll(mChildOrgUser);
		} else {
			mChildOrgUser = new ArrayList<User>();
		}
		if (mChildOrg != null) {
			Log.e("childOrg", "childOrg.size() == " + mChildOrg.size());
			mChildContactObjectList.addAll(mChildOrg);
		} else {
			mChildOrg = new ArrayList<OrganizationTree>();
		}
		mContactOrgDao = new ContactOrgDao(mContext);
	}

	private void findViews() {
		mContactOrgTitleTV = (TextView) findViewById(R.id.contact_org_title);
		mContactOrgBackBT = (Button) findViewById(R.id.bt_contact_org_title_return_button);

		mContactOrgTitleTV.setText(mParentOrg.getOrgName());

		mContactOrgLV = (ListView) findViewById(R.id.contact_org_listview);
		findSearchContactView();

	}

	private void findSearchContactView() {
		mSearchContactEditText = new SearchContactEditText(mContext);
		mContactEditView = (EditText) findViewById(R.id.se_contact_org_search_dis);
		mContactRelativeLayout = (RelativeLayout) findViewById(R.id.ll_contact_org_frame);
	}

	private void initListenerByCommonAdapter() {
		initSearchContactViewListener();
		mContactOrgTitleTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showWindow(v);
			}
		});
		mChildContactAdapter = new ContactCommonAdapter(mContext,
				mChildContactObjectList);
		mContactOrgLV.setAdapter(mChildContactAdapter);
		mContactOrgLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Object mObject = mChildContactAdapter.getItem(arg2);
				if (mObject instanceof User) {// 用户
					User mUser = (User) mObject;
					startPersonInfoActivity(mUser);
				} else if (mObject instanceof OrganizationTree) {// 组织机构
					OrganizationTree tempOrg = (OrganizationTree) mObject;
					startChildOrgActivity(tempOrg, VIEW_ENTER);
				}
			}
		});
		XmppConnectionManager.getInstance().addReceivePresCallBack(this);
	}

	/**
	 * 打开个人详情页
	 * 
	 * @param mUser
	 */
	public void startPersonInfoActivity(User mUser) {
		Intent infoIntent = new Intent(mContext,
				ContactPersonInfoActivity.class);
		infoIntent.putExtra("parentOrg", mParentOrg);
		infoIntent.putExtra("contactInfo", mUser);
		startActivity(infoIntent);
	}

	/**
	 * 打开下级界面或返回上级界面
	 * 
	 * @param mOrgTree
	 *            要返回到组织机构
	 * @param i
	 *            0:进入子界面 1:返回上级界面
	 */
	protected void startChildOrgActivity(OrganizationTree mOrgTree, int i) {
		// 表示进入子组织机构界面
		Intent childOrgIntent = new Intent(mContext,
				ContactChildOrgActivity.class);
		childOrgIntent.putExtra("isBack", i);
		childOrgIntent.putExtra("parentOrg", mOrgTree);
		// 必须转为ArrayList 才能通过PutExtra把list传到子activity
		ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>) mContactOrgDao
				.queryOrgListByParentId(mOrgTree.getOrgNo());
		if (tempList != null) {
			childOrgIntent.putExtra("childOrgList", tempList);
		}
		// 通过数据库获取用户列表，主要是获取用户当前状态
		ArrayList<User> tempUsers = (ArrayList<User>) mContactOrgDao
				.queryUsersByOrgNo(mOrgTree.getOrgNo());
		if (tempUsers != null) {
			childOrgIntent.putExtra("childOrgUser", tempUsers);
		}

		startActivity(childOrgIntent);
		finish();
		if (i == 0) {
			mChildContactAdapter.enterMenuAnimation();
		} else {
			mChildContactAdapter.backMenuAnimation();
		}

		/**************** 通过对象去获取组织机构 *********************/
		// // 表示进入子组织机构界面
		// OrganizationTree mOrgTree = mChildOrg.get(arg2);
		// Intent childOrgIntent = new Intent(mContext,
		// ContactChildOrgActivity.class);
		// childOrgIntent.putExtra("childOrgTitle",
		// mOrgTree.getOrgName());
		// // 必须转为ArrayList 才能通过PutExtra把list传到子activity
		// ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>)
		// mOrgTree
		// .getChildOrgTreeMap().get(mOrgTree.getOrgNo());
		// if (tempList != null) {
		// childOrgIntent.putExtra("childOrg", tempList);
		// }
		// ArrayList<User> tempUsers = (ArrayList<User>) mOrgTree
		// .getmOrgUsers();
		// if (tempUsers != null && tempUsers.size() > 0) {
		// childOrgIntent.putExtra("childOrgUser", tempUsers);
		// }
		// startActivity(childOrgIntent);
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

	/**
	 * 动画过程监听
	 */
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

	@SuppressLint("NewApi")
	public void showSearchContactAnimation() {
		RelativeLayout.LayoutParams etParamTest = (RelativeLayout.LayoutParams) mContactEditView
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
		XmppConnectionManager.getInstance().removeReceivePresCallBack(this);
	}

	/**
	 * 监听Presence消息
	 */
	@Override
	public void receivedPresence(Presence packet) {
		if ((packet.getType() == Presence.Type.available)
				|| (packet.getType() == Presence.Type.unavailable)) {
			refreshOrgUserListUI(packet);
		}
	}

	/**
	 * 刷新联系人列表用户的在线离线状态
	 * 
	 * @param packet
	 */
	private void refreshOrgUserListUI(Presence packet) {
		String mUserNo = JIDUtil.getAccountByJID(packet.getFrom());
		// mContactOrgDao.updateOneUserStatusByAble(mUserNo,i);
		if (mChildOrgUser != null) {
			String mOrgNo = null;
			if (mParentOrg == null) {
				mOrgNo = mParentOrgNo;
			} else {
				mOrgNo = mParentOrg.getOrgNo();
			}
			boolean isExist = mContactOrgDao.isUserRelationExist(mUserNo,
					mOrgNo);
			if (isExist) {// 如果该User属于当前页面，则执行刷新UI的操作
				mChildOrgUser.clear();
				mChildOrgUser = mContactOrgDao.queryUsersByOrgNo(mOrgNo);
				refreshChildContactList();
			}
		}
	}

	private void refreshChildContactList() {
		mChildContactObjectList.clear();
		mChildContactObjectList.addAll(mChildOrgUser);
		if (mChildOrg != null) {
			mChildContactObjectList.addAll(mChildOrg);
		}
		mChildContactAdapter.setnListObjects(mChildContactObjectList);
		mHandler.sendEmptyMessage(Const.USER_STATUS_CHANGED);
	}

	private void showWindow(View parent) {

		if (mOrgTitlePopWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			view = layoutInflater.inflate(R.layout.contact_orgtitle_popwindow,
					null);

			mOrgTitlePopwinList = (ListView) view
					.findViewById(R.id.lv_contact_title_current_path);

			OrgPathAdapter groupAdapter = new OrgPathAdapter(this,
					ContactActivityManager.mTitleOrgList);
			mOrgTitlePopwinList.setAdapter(groupAdapter);
			// 创建一个PopuWidow对象
			mOrgTitlePopWindow = new PopupWindow(view, parent.getWidth(),
					LayoutParams.WRAP_CONTENT);
		}

		mOrgTitlePopWindow.setFocusable(true);
		mOrgTitlePopWindow.update();
		// 设置允许在外点击消失
		ColorDrawable dw = new ColorDrawable(-00000);
		mOrgTitlePopWindow.setBackgroundDrawable(dw);
		mOrgTitlePopWindow.setOutsideTouchable(true);

		mOrgTitlePopWindow.showAsDropDown(parent);
		mOrgTitlePopwinList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				if (mOrgTitlePopWindow != null) {
					mOrgTitlePopWindow.dismiss();
				}
				if (position == ContactActivityManager.mTitleOrgList.size() - 1) {// 最后一个
																					// 不跳转
					return;
				} else {
					OrganizationTree mOrganizationTree = new OrganizationTree();
					mOrganizationTree = ContactActivityManager.mTitleOrgList
							.get(position);
					ContactActivityManager
							.finishAllActivityAndOrgFromIndex(position);
					startChildOrgActivity(mOrganizationTree, VIEW_ENTER);
				}
			}
		});
	}

	@Override
	public void onBackPressed() {
		ContactActivityManager.finishTopOrg();
		if (ContactActivityManager.mTitleOrgList.size() > 0) {
			int lastIndex = ContactActivityManager.mTitleOrgList.size() - 1;
			startChildOrgActivity(
					ContactActivityManager.mTitleOrgList.get(lastIndex),
					VIEW_BACK);
		} else {
			super.onBackPressed();
		}
	}

	/**
	 * 跳转到我的组织机构
	 * 
	 * @param v
	 */
	public void onTurnToMyOrgListener(View v) {
		switch (v.getId()) {
		case R.id.tv_contact_title_jump_my_org:
			if (mOrgTitlePopWindow != null) {
				mOrgTitlePopWindow.dismiss();
			}
			OrganizationTree myOrg = mContactOrgDao.queryMyOrg(mContext);
			resetOrgPathList(myOrg);
			startChildOrgActivity(myOrg, VIEW_ENTER);
			break;
		default:
			break;
		}
	}

	/**
	 * 根据跳转到的org，寻找该org所属的组织机构
	 * 
	 * @param tempOrg
	 */
	protected void resetOrgPathList(OrganizationTree tempOrg) {
		ArrayList<OrganizationTree> mMyOrgPathList = new ArrayList<OrganizationTree>();
		if (ContactActivityManager.mTitleOrgList.size() > 0) {
			OrganizationTree rootOrgZZJG = ContactActivityManager.mTitleOrgList
					.get(0);
			if (rootOrgZZJG.getParentOrgNo().equals("0")) {
				ContactActivityManager.mTitleOrgList.add(0,
						new OrganizationTree("0", "-1", "组织机构", 0, "0", 0));
			}
		} else {
			ContactActivityManager.mTitleOrgList.add(0, new OrganizationTree(
					"0", "-1", "组织机构", 0, "0", 0));
		}
		ContactActivityManager.finishAllOrgFromIndex(1);// 不删除组织机构节点
		mContactOrgDao.queryOrgBelongListByOrgNo(tempOrg, mMyOrgPathList);
		if (mMyOrgPathList.size() > 0) {
			Collections.reverse(mMyOrgPathList);
		}
		ContactActivityManager.mTitleOrgList.addAll(mMyOrgPathList);
	}

	/**
	 * @author huyi 组织机构快速跳转的适配器
	 */
	public class OrgPathAdapter extends BaseAdapter {
		private Context context;
		private List<OrganizationTree> list;

		public OrgPathAdapter(Context context,
				ArrayList<OrganizationTree> mTitleOrgList) {
			this.context = context;
			this.list = mTitleOrgList;
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
		public View getView(int position, View convertView, ViewGroup viewGroup) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.contact_orgtitle_list_item, null);
				holder = new ViewHolder();
				convertView.setTag(holder);
				holder.mPopOrgName = (TextView) convertView
						.findViewById(R.id.tv_contact_title_poplist_item);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.mPopOrgName.setText(list.get(position).getOrgName());
			if (position == list.size() - 1) {
				holder.mPopOrgName.setTextColor(Color.YELLOW);
			} else {
				holder.mPopOrgName.setTextColor(Color.WHITE);
			}
			return convertView;
		}

		class ViewHolder {
			TextView mPopOrgName;
		}
	}

}
