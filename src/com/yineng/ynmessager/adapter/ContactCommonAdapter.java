package com.yineng.ynmessager.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.contact.ContactPersonInfoActivity;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;

public class ContactCommonAdapter extends BaseAdapter {
	private Context nContext;
	private List<Object> nListObjects;
	private int mPosition = -1;
	private ContactOrgDao mContactOrgDao = null;
	private int mShowUserTag = -1;
	private int mShowOrgTag = -1;
	private int mShowGroupTag = -1;
	private int mShowDisGroupTag = -1;
	
	public ContactCommonAdapter(Context context, List<Object> mListObjects) {
		nContext = context;
		this.nListObjects = mListObjects;
		mContactOrgDao = new ContactOrgDao(nContext);
	}

	public void resetViewTag() {
		mShowUserTag = -1;
		mShowOrgTag = -1;
		mShowGroupTag = -1;
		mShowDisGroupTag = -1;
	}
	
	public void setnListObjects(List<Object> nListObjects) {
		mPosition = -1;
		this.nListObjects = nListObjects;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		Object tempResultObject = nListObjects.get(position);
		if (convertView == null) {
			

			
			
			
			convertView = LayoutInflater.from(nContext).inflate(
					R.layout.contact_orglist_common_item, null);
			viewHolder = new ViewHolder();
			viewHolder.tvContactItemTag = (TextView) convertView
					.findViewById(R.id.tv_contactlist_common_item_tag);

			viewHolder.llContactItemOrg = (LinearLayout) convertView
					.findViewById(R.id.ll_contactlist_common_item_org);
			viewHolder.tvContactItemOrgName = (TextView) convertView
					.findViewById(R.id.tv_contactlist_common_item_orgname);
			viewHolder.tvContactItemOrgCount = (TextView) convertView
					.findViewById(R.id.tv_contactlist_common_item_personcount);

			viewHolder.llContactItemPerson = (LinearLayout) convertView
					.findViewById(R.id.ll_contactlist_common_item_person);
			viewHolder.ivContactItemPersonIcon = (ImageView) convertView
					.findViewById(R.id.iv_contactlist_common_item_personicon);
			viewHolder.tvContactItemPersonName = (TextView) convertView
					.findViewById(R.id.tv_contactlist_common_item_personname);
			viewHolder.tvContactItemPostName = (TextView) convertView
					.findViewById(R.id.tv_contactlist_common_item_postname);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		if (mPosition != position) {
			mPosition = position;
			if (tempResultObject instanceof User) {
				if (mShowUserTag == -1 || mShowUserTag == position) {
					viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
					viewHolder.tvContactItemTag.setText("成员");
					mShowUserTag = position;
				} else {
					viewHolder.tvContactItemTag.setVisibility(View.GONE);
				}
				showUserListItem(viewHolder);
				User mUser = (User) tempResultObject;
				viewHolder.tvContactItemPersonName.setText(mUser.getUserName());
				if (mUser.getOrgName() != null && !(mUser.getOrgName().isEmpty())) {
					viewHolder.tvContactItemPostName.setVisibility(View.VISIBLE);
					viewHolder.tvContactItemPostName.setText(mUser.getOrgName());
				} else {
					viewHolder.tvContactItemPostName.setVisibility(View.GONE);
				}
				if (mUser.getUserStatus() == 1) {
					viewHolder.ivContactItemPersonIcon
							.setImageResource(R.drawable.main_contact_unselect_online);
				} else {
					viewHolder.ivContactItemPersonIcon
							.setImageResource(R.drawable.main_contact_unselect);
				}
			} else if (tempResultObject instanceof OrganizationTree) {// 组织结构
				if (mShowOrgTag == -1 || mShowOrgTag == position) {
					viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
					viewHolder.tvContactItemTag.setText("部门");
					mShowOrgTag = position;
				} else {
					viewHolder.tvContactItemTag.setVisibility(View.GONE);
				}
				showOrgListItem(viewHolder);
				OrganizationTree tempOrg = (OrganizationTree) tempResultObject;
				viewHolder.tvContactItemOrgName.setText(tempOrg.getOrgName());
				//某组织机构节点总人数统计
				int mOrgCount = mContactOrgDao.getOrgUsersCountByOrgIdFromDb(tempOrg,0);
				viewHolder.tvContactItemOrgCount.setText(mOrgCount + "");
			} else if (tempResultObject instanceof ContactGroup) {
				showOrgListItem(viewHolder);
				List<User> mTempList;
				ContactGroup tempContactGroup = (ContactGroup) tempResultObject;
				if (tempContactGroup.getGroupType() == 8) { // 群组
					if (mShowGroupTag == -1 || mShowGroupTag == position) {
						viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
						viewHolder.tvContactItemTag.setText("群组");
						mShowGroupTag = position;
					} else {
						viewHolder.tvContactItemTag.setVisibility(View.GONE);
					}
					mTempList = mContactOrgDao.queryUsersByGroupName(tempContactGroup.getGroupName(), 8);
					viewHolder.tvContactItemOrgName.setText(tempContactGroup
							.getNaturalName());
				} else { // 讨论组
					if (mShowDisGroupTag == -1 || mShowDisGroupTag == position) {
						viewHolder.tvContactItemTag.setVisibility(View.VISIBLE);
						viewHolder.tvContactItemTag.setText("讨论组");
						mShowDisGroupTag = position;
					} else {
						viewHolder.tvContactItemTag.setVisibility(View.GONE);
					}
					mTempList = mContactOrgDao.queryUsersByGroupName(tempContactGroup.getGroupName(), 9);
					if (tempContactGroup.getSubject() != null && !tempContactGroup.getSubject().isEmpty()) {
						viewHolder.tvContactItemOrgName.setText(tempContactGroup.getSubject());
					} else {
						viewHolder.tvContactItemOrgName.setText(tempContactGroup
								.getNaturalName());
					}
				}

				int mOrgCount = 0;
				if (mTempList != null) {
					mOrgCount = mTempList.size();// 总人数
				}
				viewHolder.tvContactItemOrgCount.setText(mOrgCount + "");
			}
		}
		return convertView;
	}

	public void showUserListItem(ViewHolder viewHolder) {
		viewHolder.llContactItemOrg.setVisibility(View.GONE);
		viewHolder.llContactItemPerson.setVisibility(View.VISIBLE);
	}

	public void showOrgListItem(ViewHolder viewHolder) {
		viewHolder.llContactItemOrg.setVisibility(View.VISIBLE);
		viewHolder.llContactItemPerson.setVisibility(View.GONE);
	}

	@Override
	public int getCount() {
		if (nListObjects == null) {
			return 0;
		}
		return nListObjects.size();
	}

	@Override
	public Object getItem(int arg0) {
		if (nListObjects == null) {
			return null;
		}
		return nListObjects.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	class ViewHolder {
		public TextView tvContactItemPostName;
		public TextView tvContactItemPersonName;
		public ImageView ivContactItemPersonIcon;
		public LinearLayout llContactItemPerson;
		public TextView tvContactItemOrgCount;
		public TextView tvContactItemOrgName;
		public LinearLayout llContactItemOrg;
		public TextView tvContactItemTag;
	}
	
	public void startPersonInfoActivity(User mUser) {
		Intent infoIntent = new Intent(nContext,
				ContactPersonInfoActivity.class);
		infoIntent.putExtra("contactInfo", mUser);
		nContext.startActivity(infoIntent);
	}
	
	/**
	 * 打开时，界面右进左出
	 */
	public void enterMenuAnimation() {
		((Activity) nContext).overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
	}
	
	/**
	 * 返回是，界面左进右出
	 */
	public void backMenuAnimation() {
		((Activity) nContext).overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
	}
}