package com.yineng.ynmessager.activity.dissession;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.util.L;

public class HorizontalListViewAdapter extends BaseAdapter {
	private List<User> mMemberList = new ArrayList<User>();
	private LayoutInflater inflater;

	public HorizontalListViewAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	public void setData(List<User> list) {
		this.mMemberList = list;
	}

	@Override
	public int getCount() {
		return mMemberList.size();
	}

	@Override
	public Object getItem(int position) {
		return mMemberList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHold viewHold = null;
		User mUser = mMemberList.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.dis_create_gallary_item, null);
			viewHold = new ViewHold();
			viewHold.mMemberIconIV = (ImageView) convertView
					.findViewById(R.id.iv_dis_create_gallary_item_head);
			viewHold.mMemberNameTV = (TextView) convertView
					.findViewById(R.id.tv_dis_create_gallary_item_name);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHold) convertView.getTag();
		}
		L.e("adapter user name == "+mUser.getUserName());
		viewHold.mMemberNameTV.setText(mUser.getUserName());
		return convertView;
	}

	class ViewHold {
		ImageView mMemberIconIV;
		TextView mMemberNameTV;
	}
}
