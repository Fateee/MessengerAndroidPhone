package com.yineng.ynmessager.activity.dissession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.IQ.Type;

import android.content.Context;
import android.content.IntentFilter;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.dissession.DisSessionBean;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.HorizontalListView;

/**
 * ClassName: DisAddActivity <br/>
 * Function: TODO ADD FUNCTION. <br/>
 * Reason: TODO ADD REASON(可选). <br/>
 * date: 2015年3月18日 上午9:54:21 <br/>
 * 
 * @author YINENG
 * @version
 * @since JDK 1.6
 */
public class DisAddActivity extends BaseActivity implements /*ReceiveReqIQCallBack,*/
		OnClickListener {
	public static final int REFRESH_GALLARY_UI = 0;// 刷新画廊UI
	public static final int REFRESH_LIST_UI = 1;// 刷新listview的UI
	public static final int REFRESH_GUID_UI = 2;// 刷新组织机构跳转UI
	public static final int ADD_SUCCESS = 3;// 成功创建讨论组
	public static final int ADD_FAILED = 4;// 创建讨论组失败
	public static final int OVER_FLOW_MAX_MEMBERS = 5;//成员超限
	public static final String DIS_GROUP_ID_KEY = "discussion_group_id";// 获得传过来的讨论组IDkey

	/**
	 * mOrgPathAdapter: 组织机构跳转适配器
	 */
	private OrgPathAdapter mOrgPathAdapter;
	/**
	 * mAddPacketId: 添加讨论组成员的PacketId
	 */
	private String mAddPacketId;
	/**
	 * isRootList: 判断mOrgListLV是否显示（组织机构、群、讨论组）
	 */
	private boolean isRootList = true;
	
	/**
	 * 搜索框
	 */
	private EditText mSearchET;
	
	/**
	 * mOrgListLV: 显示组织机构、人员、群、讨论组的UI载体
	 */
	private ListView mOrgListLV;

	/**
	 * mOrgListAdapter: mOrgListLV的适配器
	 */
	private OrgListAdapter mOrgListAdapter;
	/**
	 * mTitleTV:
	 */
	private TextView mTitleTV;
	/**
	 * mAddBtn: 向服务器发送添加请求的按钮
	 */
	private Button mAddBtn;
	/**
	 * mHorizontalListView: 显示已选择用户的画廊
	 */
	private HorizontalListView mHorizontalListView;

	/**
	 * mHorizontalListViewAdapter: 画廊适配器
	 */
	private HorizontalListViewAdapter mHorizontalListViewAdapter;
	/**
	 * mOrgTitlePopwinList: 组织机构跳转导航list
	 */
	private ListView mOrgTitlePopwinList;
	/**
	 * mOrgTitlePopWindow: 装载组织机构跳转导航list的PopWindow
	 */
	private PopupWindow mOrgTitlePopWindow;
	/**
	 * mContactOrgDao: 组织机构、群、讨论组所用到的数据库dao
	 */
	private ContactOrgDao mContactOrgDao;
	/**
	 * mGroupId: 当前组织机构的ID
	 */
	private String mGroupId;
	/**
	 * mXmppConnectionManager: ASMACK工具
	 */
	private XmppConnectionManager mXmppConnectionManager;
	/**
	 * mCurrentObjectList:显示当前mOrgListLV界面所需要的数据
	 * 
	 */
	private List<Object> mCurrentObjectList = new ArrayList<Object>();
	/**
	 * mGuideList:顶部跳转按钮数据
	 *
	 */
	private LinkedList<OrganizationTree> mGuideList = new LinkedList<OrganizationTree>();
	
	/**
	 * 用于判断群组、讨论组层次
	 */
	private LinkedList<ContactGroup> mGroupGuideList = new LinkedList<ContactGroup>();
	/**
	 * mNewAddUserList:创建讨论组时选择的用户
	 * 
	 */
	private ArrayList<User> mNewAddUserList = new ArrayList<User>();

	/**
	 * mOldUserList: 当前讨论组中已存在的用户
	 */
	private List<User> mOldUserList = new ArrayList<User>();

	private ArrayList<ContactGroupUser> mGroupUserRel = new ArrayList<ContactGroupUser>();
	
	/**
	 * mContactGroup: 当前组织机构信息
	 */
	private ContactGroup mContactGroup;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case REFRESH_GALLARY_UI:
				mHorizontalListViewAdapter.setData(mNewAddUserList);
				mHorizontalListViewAdapter.notifyDataSetChanged();
				break;
			case REFRESH_LIST_UI:
				mOrgListAdapter.setData(mCurrentObjectList);
				mOrgListAdapter.notifyDataSetChanged();
				break;
			case REFRESH_GUID_UI:
				mOrgPathAdapter.setData(mGuideList);
				mOrgPathAdapter.notifyDataSetChanged();
				break;
			case ADD_SUCCESS:
//				ArrayList<ContactGroupUser> mContactGroupUsers = castUserToContactGroupUser(mNewAddUserList);
//				Intent resultIntent = new Intent();
//				resultIntent.putExtra(Const.GROUP_ADD_USER, mContactGroupUsers);
//				setResult(Const.RESULT_CODE, resultIntent);
				break;
			case ADD_FAILED:
				break;
			case OVER_FLOW_MAX_MEMBERS:
				ToastUtil.toastAlerMessage(DisAddActivity.this, "您的讨论组人数已达到"+mMaxMemberNum+"人上限！", Toast.LENGTH_SHORT);
				break;	
			default:
				break;
			}
		}

	};
	private int mGroupType;
	private int mMaxMemberNum = 100;
	private CommonReceiver mCommonReceiver;
	protected boolean isFinishAcitivity = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_disgroup);
		init();
		initEvent();
	}

	/**
	 * init: 初始化操作
	 * 
	 * @author YINENG
	 */
	private void init() {
		mXmppConnectionManager = XmppConnectionManager.getInstance();
//		mXmppConnectionManager
//				.addReceiveReqIQCallBack("com:yineng:group", this);
		mContactOrgDao = new ContactOrgDao(this);

		mAddBtn = (Button) findViewById(R.id.btn_create_disgroup_createbtn);
		mTitleTV = (TextView) findViewById(R.id.tv_create_disgroup_title);
		mSearchET = (EditText) findViewById(R.id.et_create_disgroup_search);
		mOrgListLV = (ListView) findViewById(R.id.lv_create_disgroup_listview);
		mHorizontalListView = (HorizontalListView) findViewById(R.id.gl_create_disgroup_horizontallistView);

		// mGroupId是上一级界面传过来的讨论组ID，如果是新建讨论组，则key为null
		mGroupId = (String) getIntent().getCharSequenceExtra(DIS_GROUP_ID_KEY);
		mGroupType = getIntent().getIntExtra(DisGroupPersonList.GROUP_TYPE, 0);
		if (mGroupType != 0) {
			mOldUserList = mContactOrgDao.queryUsersByGroupName(mGroupId, mGroupType);
			mContactGroup = mContactOrgDao.getGroupBeanById(mGroupId, mGroupType);
		}
//		mOldUserList = mContactOrgDao.queryUsersByGroupName(mGroupId, 9);
//		mContactGroup = mContactOrgDao.getGroupBeanById(mGroupId, 9);

		// 组织结构跳转适配器
		mOrgPathAdapter = new OrgPathAdapter(this);

		// 画廊适配器
		mHorizontalListViewAdapter = new HorizontalListViewAdapter(this);
		mHorizontalListView.setAdapter(mHorizontalListViewAdapter);

		// 人员或组织结构显示列表
		mOrgListAdapter = new OrgListAdapter(this);
		mOrgListLV.setAdapter(mOrgListAdapter);
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


	private void initEvent() {
		mAddBtn.setOnClickListener(this);
		mTitleTV.setOnClickListener(this);
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
					updateCurrentObjectList(mNewAddUserList,mOldUserList,mCurrentObjectList);
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
				if (mGroupType == DisAddActivity.this.mGroupType) {
					if (!isFinishAcitivity) {
					} else {
						isFinishAcitivity  = false;
					}
				}
			}
		});
		mCommonReceiver.setIQuitGroupListener(new IQuitGroupListener() {

			@Override
			public void IQuitMyGroup(int mGroupType) {
				if (mGroupType == DisAddActivity.this.mGroupType){
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
	 * removeGuideListTail: 删除mGuideList中指定position对象之后的所有单元
	 * 
	 * @author YINENG
	 * @param position
	 */
	private void removeGuideListTail(int position) {
		while ((mGuideList.size() - 1) > position) {
			mGuideList.removeLast();
		}
	}

	/**
	 * getCurrentOrgNum: 获得mGuideList中最后一个OrganizationTree的组织机构ID
	 * 
	 * @author YINENG
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
							.queryMyOrg(DisAddActivity.this);
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
	 * addAction: 向服务器请求添加讨论组成员的操作
	 * 
	 * @author YINENG
	 * @param list
	 *            需要新添加的成员列表
	 */
	private void addAction(List<User> list,String groupId) {
		if (list == null || list.isEmpty()) {
			ToastUtil.toastAlerMessage(this, "未选择新的成员", Toast.LENGTH_SHORT);
			return;
		} 
		DisSessionBean bean = new DisSessionBean();
//		String groupNameString = DisCreateActivity.calculateGroupName(
//				mNewAddUserList, null);
//		bean.setDesc("");
//		bean.setSubject("");
//		bean.setNaturalName(groupNameString);
//		bean.setGroupType(DisSessionBean.DISSESSION);
		List<String> memberList = new ArrayList<String>();
		for (User user : list) {
			memberList.add(user.getUserNo());
		}
		bean.setMemberList(memberList);
		bean.setGroupName(groupId);
		String json = JSON.toJSONString(bean);
		ReqIQ iq = new ReqIQ();
		iq.setAction(3);
		iq.setType(Type.SET);
		iq.setNameSpace("com:yineng:group");
		iq.setFrom(JIDUtil.getJIDByAccount(LastLoginUserSP.getInstance(this)
				.getUserAccount()));
		iq.setTo("admin@" + mXmppConnectionManager.getServiceName());
		iq.setParamsJson(json);
		mAddPacketId = iq.getPacketID();
		if (NetWorkUtil.isNetworkAvailable(DisAddActivity.this)) {
			try {
				mXmppConnectionManager.sendPacket(iq);
				finish();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mHandler.sendEmptyMessage(ADD_FAILED);
			}
		} else {
			ToastUtil.toastAlerMessage(DisAddActivity.this, "没有网络",Toast.LENGTH_SHORT);
		}
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

//	@Override
//	public void receivedReqIQResult(ReqIQResult packet) {
//		// 接收到回执信息，判断讨论组是否成功添加人员
//		if (mAddPacketId.equals(packet.getPacketID())) {
//			switch (packet.getCode()) {
//			case 200:
//				mHandler.sendEmptyMessage(ADD_SUCCESS);
//				finish();
//				break;
//			case 604:
//				Toast.makeText(DisAddActivity.this, "没有添加人员的权限", 500).show();
//				break;
//			default:
//				mHandler.sendEmptyMessage(ADD_FAILED);
//				break;
//			}
//		}
//	}

	/**
	 * updateCurrentObjectList: 根据传入的组织机构ID，查询出当前组织机构下的所有子组织机构和用户， 并且将已选择的用户标识出来
	 * 
	 * @author YINENG
	 * @param orgNum
	 * @return
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
		updateCurrentObjectList(mNewAddUserList, mOldUserList,
				mCurrentObjectList);

		return true;
	}

	/**
	 * updateCurrentObjectList: 将已在群中的成员标识出来，已选择的成员标识出来
	 * 
	 * @author YINENG
	 * @param newUserList
	 *            已选择的成员
	 * @param oldUserList
	 *            已在群中的成员
	 * @param objectList
	 *            当前的显示列表
	 */
	private void updateCurrentObjectList(List<User> newUserList,
			List<User> oldUserList, List<Object> objectList) {
//		mSelectedUsersMap.clear();
		if (newUserList.size() > 0 || oldUserList.size() > 0) {
			for (Object obj : objectList) {
				if (obj instanceof User) {
					for (User user : newUserList) {
						// 判断当前显示界面的数据有哪些是已选择的
						if (((User) obj).getUserNo().equals(user.getUserNo())) {
							((User) obj).setSelected(true);
//							mSelectedUsersMap.put(user.getUserNo(), ((User) obj));
							break;
						}
					}
					// TODO Auto-generated method stub
					for (User user : oldUserList) {
						if (((User) obj).getUserNo().equals(user.getUserNo())) {
							((User) obj).setExited(true);
							break;
						}
					}

				}
			}
		}
	}
	private Map<String,User> mSelectedUsersMap = new HashMap<String,User>();
	class OrgListAdapter extends BaseAdapter {
		private Context nContext;
		private List<Object> nListObjects = new ArrayList<Object>();
		private ContactOrgDao mContactOrgDao;
		
		public OrgListAdapter(Context context) {
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
				viewHolder.tvContactItemPersonName.setText(tempUser.getUserName());
				viewHolder.llContactItemPerson.setTag(position);
				viewHolder.llContactItemPerson.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						int p = (Integer) v.getTag();
						User user = (User) nListObjects.get(p);
						if (user.isExited()) {
							return;
						}
						int mSumUser = mNewAddUserList.size()+ mOldUserList.size();                                                                                                                                          
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
									ContactGroup tempGroup = new ContactGroup();
									tempGroup.setGroupName("0");
									tempGroup.setNaturalName("群组");
									tempGroup.setGroupType(Const.CONTACT_GROUP_TYPE);
									addGroupGuideList(tempGroup);
									List<ContactGroup> list2 = mContactOrgDao
											.queryGroupList(8);
									if (list2 != null && !list2.isEmpty()) {
										mCurrentObjectList.clear();
										for (ContactGroup entity2 : list2) {
											mCurrentObjectList.add(entity2);
										}
										notifyDataSetChanged();
									}
									break;
								case 2:// 讨论组
									ContactGroup tempDisGroup = new ContactGroup();
									tempDisGroup.setGroupName("0");
									tempDisGroup.setNaturalName("讨论组");
									tempDisGroup.setGroupType(Const.CONTACT_DISGROUP_TYPE);
									addGroupGuideList(tempDisGroup);
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
				viewHolder.llContactItemOrg.setVisibility(View.VISIBLE);
				ContactGroup bean = (ContactGroup) tempResultObject;
				if (bean.getGroupType() == 8) { // 群组
					viewHolder.tvContactItemOrgName.setText(bean.getNaturalName());
				} else { // 讨论组
					if (bean.getSubject() != null && !bean.getSubject().isEmpty()) {
						viewHolder.tvContactItemOrgName.setText(bean.getSubject());
					} else {
						viewHolder.tvContactItemOrgName.setText(bean.getNaturalName());
					}
				}
				List<User> list = mContactOrgDao.queryUsersByGroupName(
						bean.getGroupName(), bean.getGroupType());
				int mUserCount = 0;
				if (list != null) {
					mUserCount = list.size();// 总人数
				}
				viewHolder.tvContactItemOrgCount.setText(mUserCount + "");
				viewHolder.llContactItemOrg.setTag(bean);
				viewHolder.llContactItemOrg.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						ContactGroup tempBean = (ContactGroup) v.getTag();
						addGroupGuideList(tempBean);
						List<User> list = mContactOrgDao.queryUsersByGroupName(
								tempBean.getGroupName(), tempBean.getGroupType());
						if (list != null && !list.isEmpty()) {
							mCurrentObjectList.clear();
							for (User entity : list) {
								mCurrentObjectList.add(entity);
							}
							updateCurrentObjectList(mNewAddUserList,mOldUserList,mCurrentObjectList);
							notifyDataSetChanged();
						}
					}
				});
			}
			return convertView;
		}

		/**
		 * 
		 * 
		 * @param user
		 */
		private void addToUserList(User user) {
			user.setExited(false);
			mNewAddUserList.add(user);
//			mSelectedUsersMap.put(user.getUserNo(), user);
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
//					mSelectedUsersMap.remove(entity.getUserNo());
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
		
		/**
		 * @param tempGroup
		 */
		protected void addGroupGuideList(ContactGroup tempGroup) {
			mGroupGuideList.add(tempGroup);
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

	private void removeGuidLastOne() {
		if (!mGuideList.isEmpty()) {
			mGuideList.removeLast();
		}
	}

	private int removeGroupGuideLastItem() {
		if (!mGroupGuideList.isEmpty()) {
			return mGroupGuideList.removeLast().getGroupType();
		}
		return 0;
	}
	
	/**
	 * 
	 */
	private void back() {
		if (isFinishAcitivity) {
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
		} else {
			int groupType = removeGroupGuideLastItem();
			if (!mGroupGuideList.isEmpty()) {
				if (groupType != 0) {
					List<ContactGroup> list3 = mContactOrgDao.queryGroupList(groupType);
					if (list3 != null && !list3.isEmpty()) {
						mCurrentObjectList.clear();
						for (ContactGroup entity3 : list3) {
							mCurrentObjectList.add(entity3);
						}
					}
					mHandler.sendEmptyMessage(REFRESH_LIST_UI);
					isRootList = false;
				}
			} else {
				if (!isRootList) {
					showRootListUI();
					isRootList = true;
				} else {
					finish();
				}
			}
		}
//		removeGuidLastOne();
//		if (!mGuideList.isEmpty()) {
//			updateCurrentObjectList(getCurrentOrgNum());
//			mHandler.sendEmptyMessage(REFRESH_LIST_UI);
//			isRootList = false;
//		} else {
//			if (!isRootList) {
//				showRootListUI();
//				isRootList = true;
//			} else {
//				finish();
//			}
//		}
	}

	@Override
	public void onBackPressed() {
		back();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_create_disgroup_createbtn:
			addAction(mNewAddUserList,mGroupId);
			break;

		case R.id.tv_create_disgroup_title:
			showWindow(v);
			break;
		default:
			break;
		}
	}
	
	protected ArrayList<ContactGroupUser> castUserToContactGroupUser(ArrayList<User> mNewAddUserList2) {
		ArrayList<ContactGroupUser> tempContactGroupUsers = new ArrayList<ContactGroupUser>();
		for (User user : mNewAddUserList2) {
			ContactGroupUser tempContactGroupUser = new ContactGroupUser();
			tempContactGroupUser.setGroupName(mGroupId);
			tempContactGroupUser.setRole(Const.GROUP_USER_TYPE);
			tempContactGroupUser.setJid(user.getUserNo());
			tempContactGroupUsers.add(tempContactGroupUser);
		}
		return tempContactGroupUsers;
	}

	/* (non-Javadoc)
	 * @see com.yineng.ynmessager.activity.BaseActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
//		mXmppConnectionManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP);
		unregisterReceiver(mCommonReceiver);
	}
}
