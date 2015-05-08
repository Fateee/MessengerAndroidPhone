package com.yineng.ynmessager.activity.dissession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ.Type;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.ClientInitConfig;
import com.yineng.ynmessager.bean.contact.ContactCommonBean;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.dissession.DisSessionBean;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.HorizontalListView;

/**
 * 创建讨论组的界面
 * @author 刘大砚 
 */
public class DisCreateActivity extends BaseActivity{
	public static final String DIS_GROUP_ID_KEY = "discussion_group_id";// 获得传过来的讨论组IDkey
	/**
	 * 搜索框
	 */
	private EditText mSearchET;
	private ListView mOrgListLV;
	/**
	 * 标题栏
	 */
	private TextView mTitleTV;
	/**
	 * 完成按钮
	 */
	private Button mCreateBtn;
	private HorizontalListView mHorizontalListView;
	private ListView mOrgTitlePopwinList;
	private PopupWindow mOrgTitlePopWindow;
	private ContactOrgDao mContactOrgDao;
	private boolean isRootList = true;
	public static final int REFRESH_GALLARY_UI = 0;// 刷新画廊UI
	public static final int REFRESH_LIST_UI = 1;// 刷新listview的UI
	public static final int REFRESH_GUID_UI = 2;// 刷新组织机构跳转UI
	public static final int CREATE_SUCCESS = 3;// 成功创建讨论组
	public static final int CREATE_FAILED = 4;// 创建讨论组失败
	public static final int OVER_FLOW_MAX_MEMBERS = 5;//成员超限
	private String mGroupId;
	private XmppConnectionManager mXmppConnectionManager;
	private String mCreatePacketId;// 创建讨论组的请求ID
	private String mCurrentGroupId;

	/**
	 * 显示当前界面所需要的数据
	 */

	private List<Object> mCurrentObjectList = new ArrayList<Object>();
	/**
	 * 顶部跳转按钮数据
	 */
	private LinkedList<OrganizationTree> mGuideList = new LinkedList<OrganizationTree>();

	/**
	 * 创建讨论组时选择的用户
	 */
	private List<User> mNewAddUserList = new ArrayList<User>();

	/**
	 * 组织机构列表适配器
	 */
	private DisCreateSelectListAdapter mDisCreateSelectListAdapter;
	/**
	 * 组织机构快速跳转的适配器
	 */
	private OrgPathAdapter mOrgPathAdapter;
	/**
	 * 底部已选中的横向列表
	 */
	private HorizontalListViewAdapter mHorizontalListViewAdapter;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case REFRESH_GALLARY_UI:
				mHorizontalListViewAdapter.setData(mNewAddUserList);
				mHorizontalListViewAdapter.notifyDataSetChanged();
				break;
			case REFRESH_LIST_UI:
				mDisCreateSelectListAdapter.setData(mCurrentObjectList);
				mDisCreateSelectListAdapter.notifyDataSetChanged();
				break;
			case REFRESH_GUID_UI:
				mOrgPathAdapter.setData(mGuideList);
				mOrgPathAdapter.notifyDataSetChanged();
				break;
			case CREATE_SUCCESS:
				finish();
				break;
			case CREATE_FAILED:
				break;
			case OVER_FLOW_MAX_MEMBERS:
				ToastUtil.toastAlerMessage(DisCreateActivity.this, "您的讨论组人数已达到"+mMaxMemberNum+"人上限！", Toast.LENGTH_SHORT);
				break;
			default:
				break;
			}
		}

	};
	private DisSessionBean mDisSessionBean;
	private int mMaxMemberNum = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_disgroup);
		initialize();
		initEvent();
	}

	/**
	 * 初始化界面和数据
	 */
	private void initialize() {
		mXmppConnectionManager = XmppConnectionManager.getInstance();
//		mXmppConnectionManager
//				.addReceiveReqIQCallBack("com:yineng:group", this);
		mContactOrgDao = new ContactOrgDao(this);
		mCreateBtn = (Button) findViewById(R.id.btn_create_disgroup_createbtn);
		mTitleTV = (TextView) findViewById(R.id.tv_create_disgroup_title);
		mSearchET = (EditText) findViewById(R.id.et_create_disgroup_search);
		mOrgListLV = (ListView) findViewById(R.id.lv_create_disgroup_listview);
		mHorizontalListView = (HorizontalListView) findViewById(R.id.gl_create_disgroup_horizontallistView);

		// mGroupId是上一级界面传过来的讨论组ID，如果是新建讨论组，则key为null
		mGroupId = (String) getIntent().getCharSequenceExtra(DIS_GROUP_ID_KEY);

		// 组织结构跳转适配器
		mOrgPathAdapter = new OrgPathAdapter(this);

		// 画廊适配器
		mHorizontalListViewAdapter = new HorizontalListViewAdapter(this);
		User existedUser = (User) getIntent().getParcelableExtra("disGroupAddedUser");
		if (existedUser != null) {
			mNewAddUserList.add(existedUser);
			mHorizontalListViewAdapter.setData(mNewAddUserList);
		}
		mHorizontalListView.setAdapter(mHorizontalListViewAdapter);

		// 人员或组织结构显示列表
		mDisCreateSelectListAdapter = new DisCreateSelectListAdapter(this);
		mOrgListLV.setAdapter(mDisCreateSelectListAdapter);
		showRootListUI();
		initMaxUser();
	}

	/**
	 * 初始化最大成员数
	 */
	private void initMaxUser() {
		ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
		if (mClientInitConfig != null) {
			mMaxMemberNum = Integer.parseInt(mClientInitConfig.getDisgroup_max_user());
		}		
	}
	
	/**
	 * 初始化界面监听器
	 */
	private void initEvent() {
		mCreateBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createGroupAction();
			}
		});

		mTitleTV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showWindow(v);
			}
		});

		mSearchET.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					mCurrentObjectList = mContactOrgDao
							.querySearchResultByKeyWords(s.toString());
					updateCurrentObjectList(mNewAddUserList, mCurrentObjectList);
					mHandler.sendEmptyMessage(REFRESH_LIST_UI);
				} else {
					if (getCurrentOrgNum() == null) {
						showRootListUI();
					} else {
						updateCurrentObjectList(getCurrentOrgNum());
						mHandler.sendEmptyMessage(REFRESH_LIST_UI);
					}
				}

			}
		});
	}

	/**
	 * 创建讨论组的操作
	 * 
	 * @throws IOException
	 */
	private void createGroupAction() {
		if (mNewAddUserList == null || mNewAddUserList.isEmpty()) {
			ToastUtil.toastAlerMessage(this, "未选择成员", Toast.LENGTH_SHORT);
			return;
		} else {
//			if (mNewAddUserList.size()<2) {
//				ToastUtil.toastAlerMessage(this, "成员数量不能少于2个", Toast.LENGTH_SHORT);
//				return;
//			} else {
//				ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
//				if (mClientInitConfig != null) {
//					int maxUsers = Integer.parseInt(mClientInitConfig.getDisgroup_max_user());
//					if (mNewAddUserList.size() > maxUsers) {
//						ToastUtil.toastAlerMessage(this, "成员数量超过上限", Toast.LENGTH_SHORT);
//						return;
//					}
//				}
//			}
			ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
			if (mClientInitConfig != null) {
				int maxUsers = Integer.parseInt(mClientInitConfig.getDisgroup_max_user());
				if (mNewAddUserList.size() > (maxUsers-1)) {
					ToastUtil.toastAlerMessage(this, "成员数量超过上限", Toast.LENGTH_SHORT);
					return;
				}
			}
		}
		mDisSessionBean = new DisSessionBean();
		String groupNameString = calculateGroupName(mNewAddUserList, null);
		mDisSessionBean.setDesc("");
		mDisSessionBean.setSubject(groupNameString);
		mDisSessionBean.setNaturalName(groupNameString);
		mDisSessionBean.setGroupType(DisSessionBean.DISSESSION);
		List<String> memberList = new ArrayList<String>();
		for (User user : mNewAddUserList) {
			memberList.add(user.getUserNo());
		}
		mDisSessionBean.setMemberList(memberList);
		String json = JSON.toJSONString(mDisSessionBean);
		ReqIQ iq = new ReqIQ();
		iq.setAction(1);
		iq.setType(Type.SET);
		iq.setNameSpace("com:yineng:group");
		iq.setFrom(JIDUtil.getJIDByAccount(LastLoginUserSP.getInstance(this)
				.getUserAccount()));
		iq.setTo("admin@" + mXmppConnectionManager.getServiceName());
		iq.setParamsJson(json);
		mCreatePacketId = iq.getPacketID();
		if (NetWorkUtil.isNetworkAvailable(DisCreateActivity.this)) {
			try {
				mXmppConnectionManager.sendPacket(iq);
				mHandler.sendEmptyMessage(CREATE_SUCCESS);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mHandler.sendEmptyMessage(CREATE_FAILED);
			}
		} else {
			ToastUtil.toastAlerMessage(DisCreateActivity.this, "没有网络",Toast.LENGTH_SHORT);
		}
	}

	/**
	 * 
	 * 通过讨论组成员信息或name参数，返回讨论组名称
	 * 
	 * @param list
	 * @return
	 */
	public static String calculateGroupName(List<User> list, String name) {
		if (list == null) {
			return "";
		}

		if (name != null) {
			return name;
		}
		StringBuilder builder = new StringBuilder();
//		if (list.size() > 3) {
//			for (int i = 0; i < 3; i++) {
//				builder.append(list.get(i).getUserName());
//				builder.append("、");
//			}
//		} else {
//			for (User user : list) {
//				builder.append(user.getUserName());
//				builder.append("、");
//			}
//		}
//		builder.append("(" + list.size() + "人)").toString();
		for (User user : list) {
			builder.append(user.getUserName());
//			builder.append("、");
			if (builder.length()>20) {
				builder.append("...");
				break;
			}
		}
		return builder.toString();
	}

	/**
	 * 
	 * 判断objectList中，User对象集合中，哪些存在在UserList中， 存在则将setSelected(boolean
	 * )传入true，否则传入false
	 * 
	 * @param UserList
	 * @param ObjectList
	 */
	private void updateCurrentObjectList(List<User> userList,
			List<Object> objectList) {
		List<User> list = new ArrayList<User>();
		if (userList != null && !userList.isEmpty()) {
			list.addAll(userList);
		}

		if (list.isEmpty()) {
			return;
		}

		for (Object obj : objectList) {
			if (obj instanceof User) {
				for (User user : list) {
					// 判断当前显示界面的数据有哪些是已选择的
					if (((User) obj).getUserNo().equals(user.getUserNo())) {
						((User) obj).setSelected(true);
						break;
					}

				}
			}
		}
	}

	/****************************************************************/
	// mGuideList的操作方法
	/***************************************************************/

	/**
	 * 删除最后一个
	 */
	private void removeGuidLastOne() {
		if (!mGuideList.isEmpty()) {
			mGuideList.removeLast();
		}
	}

	/**
	 * 
	 * 获得mGuideList中最后一个OrganizationTree的组织机构ID
	 * 
	 * @return
	 */
	private String getCurrentOrgNum() {
		if (mGuideList.size() > 0) {
			return mGuideList.getLast().getOrgNo();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * 删除mGuideList中指定对象之后的所有单元
	 * 
	 * @param entity
	 */
	private void removeGuideListTail(int position) {
		while ((mGuideList.size() - 1) > position) {
			mGuideList.removeLast();
		}
	}

	/**
	 * 
	 * 根据传入的组织机构ID，查询出当前组织机构下的所有子组织机构和用户， 并且将已选择的用户标识出来
	 * 
	 * @param orgNum
	 */
	private boolean updateCurrentObjectList(String orgNum) {
		if (orgNum == null) {
			return false;
		}
		// 获取用户列表
		ArrayList<User> tempUserList = (ArrayList<User>) mContactOrgDao
				.queryUsersByOrgNo(orgNum);
		// 获得组织列表
		ArrayList<OrganizationTree> tempOrgList = (ArrayList<OrganizationTree>) mContactOrgDao
				.queryOrgListByParentId(orgNum);
		mCurrentObjectList.clear();

		// 成员放在上面
		if (tempUserList != null) {
			for (User user : tempUserList) {
				mCurrentObjectList.add(user);
			}
		}

		// 组织机构放在下面
		if (tempOrgList != null) {
			for (OrganizationTree entity : tempOrgList) {
				mCurrentObjectList.add(entity);
			}
		}

		// 将已在讨论组中的用户标识出来
		updateCurrentObjectList(mNewAddUserList, mCurrentObjectList);

		return true;
	}

	/**
	 * 显示“组织机构，群，讨论组”
	 */
	private void showRootListUI() {
		mCurrentObjectList.clear();
		ContactCommonBean bean = new ContactCommonBean();
		bean.setName("组织机构");
		bean.setNum("0");
		mCurrentObjectList.add(bean);
		ContactCommonBean bean1 = new ContactCommonBean();
		bean1.setName("群");
		mCurrentObjectList.add(bean1);
		ContactCommonBean bean2 = new ContactCommonBean();
		bean2.setName("讨论组");
		mCurrentObjectList.add(bean2);
		mHandler.sendEmptyMessage(REFRESH_LIST_UI);
	}

	/**
	 * 
	 * 显示组织机构跳转按钮布局
	 * 
	 * @param parent
	 */
	private void showWindow(View parent) {
		TextView firstTitleTV;
		if (mOrgTitlePopWindow == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View view = layoutInflater.inflate(
					R.layout.contact_orgtitle_popwindow, null);
			firstTitleTV = (TextView) view
					.findViewById(R.id.tv_contact_title_jump_my_org);
			mOrgTitlePopwinList = (ListView) view
					.findViewById(R.id.lv_contact_title_current_path);

			mOrgTitlePopwinList.setAdapter(mOrgPathAdapter);
			// 创建一个PopuWidow对象
			mOrgTitlePopWindow = new PopupWindow(view, parent.getWidth(),
					LayoutParams.WRAP_CONTENT);
			firstTitleTV.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// 点击跳转到当前用户所在的组织机构
					OrganizationTree entity = mContactOrgDao
							.queryMyOrg(DisCreateActivity.this);
					updateCurrentObjectList(entity.getOrgNo());
					mGuideList.clear();
					closePopWindow();
					mHandler.sendEmptyMessage(REFRESH_LIST_UI);
				}
			});
		}
		mHandler.sendEmptyMessage(REFRESH_GUID_UI);
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
				closePopWindow();
				removeGuideListTail(position);
				if (updateCurrentObjectList(getCurrentOrgNum())) {
					// 点击跳转，更新listview
					mHandler.sendEmptyMessage(REFRESH_LIST_UI);
				}
			}
		});
	}

	/**
	 * 关闭组织机构跳转布局
	 */
	private void closePopWindow() {
		if (mOrgTitlePopWindow != null && mOrgTitlePopWindow.isShowing()) {
			mOrgTitlePopWindow.dismiss();
		}
	}

	/**
	 * 组织机构列表适配器
	 */
	class DisCreateSelectListAdapter extends BaseAdapter {
		private Context nContext;
		/**
		 * 显示当前界面所需要的数据
		 */
		private List<Object> nListObjects = new ArrayList<Object>();
		private ContactOrgDao mContactOrgDao;

		public DisCreateSelectListAdapter(Context context) {
			nContext = context;
			mContactOrgDao = new ContactOrgDao(nContext);
		}

		public void setData(List<Object> nListObjects) {
			this.nListObjects = nListObjects;
		}

		@Override
		public int getCount() {
			return nListObjects.size();
		}

		@Override
		public Object getItem(int position) {
			return nListObjects.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			Object tempResultObject = nListObjects.get(position);
			if (convertView == null) {
				convertView = LayoutInflater.from(nContext).inflate(
						R.layout.dis_create_select_list_item, null);
				viewHolder = new ViewHolder();
				viewHolder.tvContactItemTag = (TextView) convertView
						.findViewById(R.id.tv_dis_create_item_tag);

				viewHolder.llContactItemOrg = (LinearLayout) convertView
						.findViewById(R.id.ll_dis_create_item_org);
				viewHolder.tvContactItemOrgName = (TextView) convertView
						.findViewById(R.id.tv_dis_create_item_orgname);
				viewHolder.tvContactItemOrgCount = (TextView) convertView
						.findViewById(R.id.tv_dis_create_item_personcount);

				viewHolder.llContactItemPerson = (LinearLayout) convertView
						.findViewById(R.id.ll_dis_create_item_person);
				viewHolder.ivContactItemPersonIcon = (ImageView) convertView
						.findViewById(R.id.iv_dis_create_item_personicon);
				viewHolder.tvContactItemPersonName = (TextView) convertView
						.findViewById(R.id.tv_dis_create_item_personname);
				viewHolder.ivContactSelected = (ImageView) convertView
						.findViewById(R.id.cb_dis_create_item_checkBox);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.tvContactItemOrgCount.setVisibility(View.VISIBLE);
			viewHolder.tvContactItemTag.setVisibility(View.GONE);
			viewHolder.llContactItemOrg.setVisibility(View.GONE);
			viewHolder.llContactItemPerson.setVisibility(View.GONE);
			viewHolder.ivContactSelected.setEnabled(true);

			if (tempResultObject instanceof User) {// 用户
				Object temp = null;
				if (position > 0) {
					temp = nListObjects.get(position - 1);
				}
				// 当前的position是第一个或者前一个item是组织机构，则显示tag
				if (temp == null || temp instanceof OrganizationTree) {
					viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
					viewHolder.tvContactItemTag.setText("成员");
				}

				User tempUser = (User) nListObjects.get(position);
				if (tempUser.isExited()) {
					viewHolder.ivContactSelected.setImageResource(R.drawable.report_option_selelcted);
				} else {
					if (tempUser.isSelected()) {
						viewHolder.ivContactSelected.setImageResource(R.drawable.report_option_selelcting);
					} else {
						viewHolder.ivContactSelected.setImageResource(R.drawable.report_option_not_selelcted);
					}
				}

				viewHolder.llContactItemPerson.setVisibility(View.VISIBLE);
				viewHolder.tvContactItemPersonName.setText(((User) nListObjects
						.get(position)).getUserName());
				viewHolder.llContactItemPerson.setTag(position);
				viewHolder.llContactItemPerson.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						int p = (Integer) v.getTag();
						User user = (User) nListObjects.get(p);
						if (user.isExited()) {
							return;
						}
						int mSumUser = mNewAddUserList.size();                                                                                                                                          
						if (mSumUser > mMaxMemberNum-1) {
							mHandler.sendEmptyMessage(OVER_FLOW_MAX_MEMBERS);
							return;
						}
						if (user.isSelected()) {
							user.setSelected(false);
							removeFromUserList(user);
						} else {
							user.setSelected(true);
							addToUserList(user);
						}
						notifyDataSetChanged();
						mHandler.sendEmptyMessage(REFRESH_GALLARY_UI);
					}
				});
//				viewHolder.mCheckBox.setTag(position);
//				viewHolder.mCheckBox
//						.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//							@Override
//							public void onCheckedChanged(
//									CompoundButton buttonView, boolean isChecked) {
//								// TODO Auto-generated method stub
//								int position = (Integer) buttonView.getTag();
//								User user = (User) nListObjects.get(position);
//								if (isChecked) {
//									user.setSelected(true);
//
//									// 新添加的用户，则将该标识设置为false，方便刷新UI
//									user.setExited(false);
//									addToUserList(user);
//								} else {
//									user.setSelected(false);
//									removeFromUserList(user);
//								}
//								mHandler.sendEmptyMessage(REFRESH_GALLARY_UI);
//							}
//						});
			}

			if (tempResultObject instanceof OrganizationTree) {// 组织机构
				Object temp = null;
				if (position >= 1) {
					temp = nListObjects.get(position - 1);
				}
				// 当前的position是第一个或者前一个item是用户，则显示tag
				if (temp == null || temp instanceof User) {
					viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
					viewHolder.tvContactItemTag.setText("部门");
				}
				viewHolder.llContactItemOrg.setVisibility(View.VISIBLE);
				int num = mContactOrgDao.getOrgUsersCountByOrgIdFromDb(
						(OrganizationTree) nListObjects.get(position), 0);
				viewHolder.tvContactItemOrgCount.setText(num + "");
				viewHolder.tvContactItemOrgName
						.setText(((OrganizationTree) nListObjects.get(position))
								.getOrgName());
				viewHolder.llContactItemOrg
						.setTag((OrganizationTree) nListObjects.get(position));
				viewHolder.llContactItemOrg
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								OrganizationTree entity = (OrganizationTree) v
										.getTag();
								addGuideList(entity);
								if (updateCurrentObjectList(entity.getOrgNo())) {
									mHandler.sendEmptyMessage(REFRESH_LIST_UI);
								}
							}
						});
			}

			if (tempResultObject instanceof ContactCommonBean) {// 联系界面顶层分类（组织机构、群、讨论组）
				viewHolder.llContactItemOrg.setVisibility(View.VISIBLE);
				viewHolder.tvContactItemOrgName
						.setText(((ContactCommonBean) nListObjects
								.get(position)).getName());
				viewHolder.llContactItemOrg.setTag(position);
				viewHolder.tvContactItemOrgCount.setVisibility(View.GONE);
				viewHolder.llContactItemOrg
						.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								isRootList = false;
								switch ((Integer) v.getTag()) {
								case 0:
									OrganizationTree entity = new OrganizationTree();
									entity.setOrgNo("0");
									entity.setOrgName("组织机构");
									addGuideList(entity);
									ArrayList<OrganizationTree> list1 = (ArrayList<OrganizationTree>) mContactOrgDao
											.queryOrgListByParentId("0");
									if (list1 != null && !list1.isEmpty()) {
										mCurrentObjectList.clear();
										for (OrganizationTree entity1 : list1) {
											mCurrentObjectList.add(entity1);
										}
										notifyDataSetChanged();
									}
									break;
								case 1:// 群

									List<ContactGroup> list2 = mContactOrgDao
											.queryGroupList(8);
									if (list2 != null && !list2.isEmpty()) {
										mCurrentObjectList.clear();
										for (ContactGroup entity2 : list2) {
											mCurrentObjectList.add(entity2);
										}
										notifyDataSetChanged();
									}
									break;// 讨论组
								case 2:
									List<ContactGroup> list3 = mContactOrgDao
											.queryGroupList(9);
									if (list3 != null && !list3.isEmpty()) {
										mCurrentObjectList.clear();
										for (ContactGroup entity3 : list3) {
											mCurrentObjectList.add(entity3);
										}
										notifyDataSetChanged();
									}
									break;

								default:
									break;
								}
							}
						});
			}

			if (tempResultObject instanceof ContactGroup) {// 群组
				ContactGroup bean = (ContactGroup) tempResultObject;
				List<User> list = mContactOrgDao.queryUsersByGroupName(
						bean.getGroupName(), bean.getGroupType());
				if (list != null && !list.isEmpty()) {
					mCurrentObjectList.clear();
					for (User entity : list) {
						mCurrentObjectList.add(entity);
					}
					notifyDataSetChanged();
				}
			}
			return convertView;
		}

		/**
		 * 
		 * 添加entity到gallery的数据链表中
		 * 
		 * @param user
		 */
		private void addToUserList(User user) {
			user.setExited(false);
			mNewAddUserList.add(user);
		}

		/**
		 * 从gallery数据中删除entity
		 * 
		 * @param user
		 */
		private void removeFromUserList(User user) {
			for (User entity : mNewAddUserList) {
				if (entity.getUserNo().equals(user.getUserNo())) {
					mNewAddUserList.remove(entity);
					return;
				}
			}
		}

		/**
		 * 添加对象到mGuideList
		 * 
		 * @param entity
		 */
		private void addGuideList(OrganizationTree entity) {
			mGuideList.add(entity);
		}

		class ViewHolder {
			public TextView tvContactItemPersonName;
			public ImageView ivContactItemPersonIcon;
			public LinearLayout llContactItemPerson;
			public TextView tvContactItemOrgCount;
			public TextView tvContactItemOrgName;
			public LinearLayout llContactItemOrg;
			public TextView tvContactItemTag;
			public ImageView ivContactSelected;
		}
	}

	public void back(View view) {
		back();
	}

	private void back() {
		removeGuidLastOne();
		if (!mGuideList.isEmpty()) {
			updateCurrentObjectList(getCurrentOrgNum());
			mHandler.sendEmptyMessage(REFRESH_LIST_UI);
			isRootList = false;
		} else {
			if (!isRootList) {
				showRootListUI();
				isRootList = true;
			} else {
				finish();
			}
		}
	}

	@Override
	public void onBackPressed() {
		back();
	}

//	@Override
//	public void receivedReqIQResult(ReqIQResult packet) {
//
//		// 接收到回执信息，判断讨论组是否创建成功
//		if (mCreatePacketId.equals(packet.getPacketID())) {
//			if (packet.getCode() == 200) {
//				String resultJson = packet.getResp();
//				L.i("resultJson == "+resultJson);
//				String groupName = null;
//				try {
//					JSONObject mJsonObject = new JSONObject(resultJson);
//					groupName = mJsonObject.optString("groupName");
//				} catch (JSONException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//				if (groupName != null) {
//					insertContactGroupDataToDB(mDisSessionBean,groupName);
//					//发送创建讨论组成功的广播
//					Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_CREATE_GROUP);
//					sendBroadcast(updateViewIntent);
//				}
//				mHandler.sendEmptyMessage(CREATE_SUCCESS);
//			} else {
//				mHandler.sendEmptyMessage(CREATE_FAILED);
//			}
//		}
//	}
	
//	/**
//	 * 添加讨论组信息到数据库
//	 * @param bean 讨论组实例
//	 * @param groupName IQ回执的讨论组ID
//	 */
//	public void insertContactGroupDataToDB(DisSessionBean bean, String groupName) {
//		ContactGroup mContactGroup = new ContactGroup();
//		mContactGroup.setGroupName(groupName);
//		mContactGroup.setNaturalName(bean.getNaturalName());
//		mContactGroup.setDesc(bean.getDesc());
//		mContactGroup.setSubject(bean.getSubject());
//		ClientInitConfig mClientInitConfig = mContactOrgDao.getClientInitInfo();
//		if (mClientInitConfig != null) {
//			int maxUsers = Integer.parseInt(mClientInitConfig.getDisgroup_max_user());
//			mContactGroup.setMaxUsers(maxUsers);
//		}
//		mContactGroup.setGroupType(Const.CONTACT_DISGROUP_TYPE);
//		mContactGroup.setCreateTime(System.currentTimeMillis()+"");
//		mContactGroup.setCreateUser(AppController.getInstance().mMyUser.getUserNo());
//		
//		//插入一条讨论组记录
//		mContactOrgDao.insertOneContactGroupData(mContactGroup, Const.CONTACT_DISGROUP_TYPE);
//		
//		for (String userNo : bean.getMemberList()) {
//			ContactGroupUser tempGroupUser = new ContactGroupUser();
//			tempGroupUser.setGroupName(groupName);
//			tempGroupUser.setUserNo(userNo);
//			tempGroupUser.setJid(userNo);
//			tempGroupUser.setRole(50);
//			//插入一条讨论组成员信息
//			mContactOrgDao.insertOneGroupUserRelationData(tempGroupUser,Const.CONTACT_DISGROUP_TYPE);
//		}
//	}
	
//	@Override
//	protected void onDestroy() {
//		// TODO Auto-generated method stub
//		super.onDestroy();
//		mXmppConnectionManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP);
//	}
}
