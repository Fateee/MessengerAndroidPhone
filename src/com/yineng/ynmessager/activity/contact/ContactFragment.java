package com.yineng.ynmessager.activity.contact;

import java.util.ArrayList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.view.SearchContactEditText;
import com.yineng.ynmessager.view.SearchContactEditText.onCancelSearchAnimationListener;

public class ContactFragment extends Fragment implements onCancelSearchAnimationListener{
	private ListView mContactOrgLV;
	private Context mContext;
	private ContactOrgDao mContactOrgDao;
//	private ArrayList<OrganizationTree> mRootOrg;
	private String[] root = {"组织机构","群组","讨论组"};
	private RelativeLayout mContactRelativeLayout;
	/***搜索联系人功能***/
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this.getActivity();
		mContactOrgDao = new ContactOrgDao(mContext);
////		mRootOrg = mContactOrgDao.queryOrgListByParentId("0");
//		mContactOrgDao.queryAllOrgListByParentId(OrganizationTree.getInstance(),"0");
//		mRootOrg = (ArrayList<OrganizationTree>) OrganizationTree.mOrganizationTree.getChildOrgTreeMap().get("0");
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main_contact_layout, null);	
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		findViews(view);
		initListener();
	}
	
	private void findViews(View view) {
		mContactOrgLV = (ListView) view.findViewById(R.id.contact_org_listview);
		findSearchContactView(view);
	}
	
	private void findSearchContactView(View view) {
		mSearchContactEditText = new SearchContactEditText(mContext);
		mContactEditView =  (EditText) view.findViewById(R.id.se_contact_org_search_dis);
		mContactRelativeLayout= (RelativeLayout) view.findViewById(R.id.ll_contact_org_frame);
	}
	
	private void initListener() {
		initSearchContactViewListener();
		mContactOrgLV.setAdapter(new SampleAdapter(mContext));
		mContactOrgLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				/****************通过数据库去获取组织机构*********************/
//				Intent childOrgIntent = new Intent(mContext, ContactChildOrgActivity.class);
//				childOrgIntent.putExtra("childOrgTitle", mRootOrg.get(arg2).getOrgName());
//				//必须转为ArrayList 才能通过PutExtra把list传到子activity
//				ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>) mContactOrgDao.queryOrgListByParentId(mRootOrg.get(arg2).getOrgNo());
//				if (tempList != null) {
//					childOrgIntent.putExtra("childOrg", tempList);
//				}
//				ArrayList<User> tempUsers = (ArrayList<User>) mContactOrgDao.queryUsersByOrgNo(mRootOrg.get(arg2).getOrgNo());
//				if (tempUsers != null) {
//					childOrgIntent.putExtra("childOrgUser", tempUsers);
//				}
//				startActivity(childOrgIntent);
				
				switch (arg2) {
				case 0:
					/****************通过对象去获取组织机构*********************/
					Intent childOrgIntent = new Intent(mContext, ContactChildOrgActivity.class);
//					childOrgIntent.putExtra("childOrgTitle", root[arg2]);
					//必须转为ArrayList 才能通过PutExtra把list传到子activity
					ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>) mContactOrgDao.queryOrgListByParentId("0");
					if (tempList != null) {
						childOrgIntent.putExtra("childOrgList", tempList);
					}
					// 表示进入子组织机构界面
					OrganizationTree firstOrganizationTree = new OrganizationTree("0", "-1", root[arg2], 0, "0", 0);
					childOrgIntent.putExtra("parentOrg",firstOrganizationTree);
					startActivity(childOrgIntent);
					break;
				case 1:
				case 2:
					ArrayList<ContactGroup> mContactGroupList;
					Intent groupIntent = new Intent(mContext, ContactGroupOrgActivity.class);
					groupIntent.putExtra("childGroupTitle", root[arg2]);
					if (arg2 == 1) {
						mContactGroupList = (ArrayList<ContactGroup>) mContactOrgDao.queryGroupList(8);
						groupIntent.putExtra("groupType", 8);
					} else {
						mContactGroupList = (ArrayList<ContactGroup>) mContactOrgDao.queryGroupList(9);
						groupIntent.putExtra("groupType", 9);
					}
					if (mContactGroupList != null) {
						groupIntent.putExtra(Const.INTENT_GROUP_LIST_EXTRA_NAME, mContactGroupList);
					}
					startActivity(groupIntent);
					break;
				default:
					break;
				}
//				/****************通过对象去获取组织机构*********************/
//				OrganizationTree mOrgTree = mRootOrg.get(arg2);
//				Intent childOrgIntent = new Intent(mContext, ContactChildOrgActivity.class);
//				childOrgIntent.putExtra("childOrgTitle", mOrgTree.getOrgName());
//				//必须转为ArrayList 才能通过PutExtra把list传到子activity
//				ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>) mOrgTree.getChildOrgTreeMap().get(mOrgTree.getOrgNo());//OrganizationTree.mOrganizationTree.childOrgTreeMap.get(mRootOrg.get(arg2).getOrgNo());
//				if (tempList != null) {
//					childOrgIntent.putExtra("childOrg", tempList);
//				}
////				ArrayList<User> tempUsers = (ArrayList<User>) mContactOrgDao.queryUsersByOrgNo(mRootOrg.get(arg2).getOrgNo());
//				ArrayList<User> tempUsers = (ArrayList<User>) mOrgTree.getmOrgUsers();
//				if (tempUsers != null && tempUsers.size() > 0) {
//					childOrgIntent.putExtra("childOrgUser", tempUsers);
//				}
//				startActivity(childOrgIntent);
			}
		});		
	}

	// public void startPersonInfoActivity(User mUser) {
	// Intent infoIntent = new Intent(mContext,
	// ContactPersonInfoActivity.class);
	// infoIntent.putExtra("contactInfo", mUser);
	// startActivity(infoIntent);
	// }

	// protected void startChildOrgActivity(OrganizationTree mOrgTree) {
	// // 表示进入子组织机构界面
	// Intent childOrgIntent = new
	// Intent(mContext,ContactChildOrgActivity.class);
	// childOrgIntent.putExtra("childOrgTitle",mOrgTree.getOrgName());
	// //必须转为ArrayList 才能通过PutExtra把list传到子activity
	// ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>)
	// mContactOrgDao.queryOrgListByParentId(mOrgTree.getOrgNo());
	// // ArrayList<OrganizationTree> tempList = (ArrayList<OrganizationTree>)
	// mOrgTree
	// // .getChildOrgTreeMap().get(mOrgTree.getOrgNo());
	// if (tempList != null) {
	// childOrgIntent.putExtra("childOrg", tempList);
	// }
	// //通过数据库获取用户列表，主要是获取用户当前状态
	// ArrayList<User> tempUsers =
	// (ArrayList<User>)mContactOrgDao.queryUsersByOrgNo(mOrgTree.getOrgNo());
	// if (tempUsers != null) {
	// childOrgIntent.putExtra("childOrgUser", tempUsers);
	// }
	// childOrgIntent.putExtra("parentOrgNo", mOrgTree.getOrgNo());
	// startActivity(childOrgIntent);
	// }

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
		RelativeLayout.LayoutParams etParamTest = (RelativeLayout.LayoutParams) mContactEditView.getLayoutParams();
		searchViewY = mContactEditView.getY()-etParamTest.topMargin;
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
	
	public class SampleAdapter extends BaseAdapter {

		private Context nContext;
		public SampleAdapter(Context context) {
			nContext = context;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(nContext).inflate(R.layout.contact_org_orglist_item, null);
				TextView mOrgUserCountTV = (TextView) convertView.findViewById(R.id.tv_contact_orglist_item_count);
				mOrgUserCountTV.setText("");
			}
//			TextView tag = (TextView) convertView.findViewById(R.id.tv_contact_orglist_item_tag);
//			if (position == 0) {
//				tag.setVisibility(View.VISIBLE);
//			} else {
//				tag.setVisibility(View.GONE);
//			}
			TextView title = (TextView) convertView.findViewById(R.id.tv_contact_orglist_item_name);
			
//			OrganizationTree tempOrg = mRootOrg.get(position);
			title.setText(root[position]);
//			int mOrgCount = mContactOrgDao.getOrgUsersCountByOrgId(tempOrg,0);
//			mOrgUserCountTV.setText(mOrgCount+"");
			
			return convertView;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return root.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
	}
	
}
