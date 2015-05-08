package com.yineng.ynmessager.receiver;

import com.yineng.ynmessager.app.Const;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CommonReceiver extends BroadcastReceiver {
	private final String TAG = "CommonReceiver";
	private updateGroupDataListener mUpdateGroupDataListener;
	private groupCreatedListener mGroupCreatedListener;
	private IQuitGroupListener mIQuitGroupListener;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		int mGroupType = intent.getIntExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, 0);
		//用户重命名讨论组，添加成员到讨论组，被添加人员自动创建该讨论组的广播
		if (action.equals(Const.BROADCAST_ACTION_UPDATE_GROUP)) {
			mUpdateGroupDataListener.updateGroupData(mGroupType);
		} 
		//用户创建讨论组,群组
		else if (action.equals(Const.BROADCAST_ACTION_CREATE_GROUP)) {
			mGroupCreatedListener.groupCreated();
		} 
		//其他用户退出讨论组,群组
		else if (action.equals(Const.BROADCAST_ACTION_QUIT_GROUP)) {
			mUpdateGroupDataListener.updateGroupData(mGroupType);
		} 
		//我退出该讨论组;别人把我T出讨论组或群组
		else if (action.equals(Const.BROADCAST_ACTION_I_QUIT_GROUP)) {
			if (mIQuitGroupListener != null) {
				mIQuitGroupListener.IQuitMyGroup(mGroupType);
			}
			if (mUpdateGroupDataListener != null) {
				mUpdateGroupDataListener.updateGroupData(mGroupType);
			}
		}
	}
	
	public interface updateGroupDataListener{
		public void updateGroupData(int mGroupType);
	}
	
	public interface groupCreatedListener{
		public void groupCreated();
	}
	
	public interface IQuitGroupListener{
		public void IQuitMyGroup(int mGroupType);
	}
	
	public void setUpdateGroupDataListener(
			updateGroupDataListener updateGroupDataListener) {
		this.mUpdateGroupDataListener = updateGroupDataListener;
	}

	public void setGroupCreatedListener(groupCreatedListener mGroupCreatedListener) {
		this.mGroupCreatedListener = mGroupCreatedListener;
	}

	public void setIQuitGroupListener(IQuitGroupListener mIQuitGroupListener) {
		this.mIQuitGroupListener = mIQuitGroupListener;
	}


}
