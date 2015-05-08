package com.yineng.ynmessager.activity.dissession;

import java.io.IOException;
import java.util.List;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.receiver.CommonReceiver;
import com.yineng.ynmessager.receiver.CommonReceiver.IQuitGroupListener;
import com.yineng.ynmessager.receiver.CommonReceiver.updateGroupDataListener;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.JIDUtil;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.ToastUtil;

/**
 * 重命名讨论组
 * @author 胡毅
 *
 */
public class DisGroupRenameActivity extends BaseActivity {

	/**
	 * 讨论组实例
	 */
	private ContactGroup mContactGroup;
	/**
	 * 讨论组id
	 */
	private String mGroupId;
	/**
	 * 联系人数据库操作对象
	 */
	private ContactOrgDao mContactOrgDao;
	/**
	 * xmpp管理类
	 */
	private XmppConnectionManager mXmppConnectionManager;
	/**
	 * asmack包id
	 */
	private String mPacketId;
	/**
	 * 编辑框
	 */
	private EditText mRenameDisgroupET;
	/**
	 * 新名称
	 */
	private String mNewName = "";
	/**
	 * 默认讨论组名称
	 */
	private String mGroupName;

	private Handler mHandler = new Handler(){
		public void dispatchMessage(android.os.Message msg) {
			finish();
			overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
		};
	};
	private int mGroupType;
	private CommonReceiver mCommonReceiver;
	protected boolean isFinishAcitivity = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initData();
		setContentView(R.layout.activity_dissession_rename_layout);
		findViews();
		addGroupUpdatedListener();
	}

	private void initData() {
		mXmppConnectionManager = XmppConnectionManager.getInstance();
//		mXmppConnectionManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP, this);
		initDisGroupObject();
	}
	
	/**
	 * 初始化该讨论组对象数据
	 */
	private void initDisGroupObject() {
		mContactOrgDao = new ContactOrgDao(this);
		mContactGroup = (ContactGroup) getIntent().getSerializableExtra(
				Const.INTENT_GROUP_EXTRA_NAME);
		mGroupType = getIntent().getIntExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, 0);
		if (mContactGroup != null) {
			mGroupId = mContactGroup.getGroupName();
		} else {
			mGroupId = (String) getIntent().getCharSequenceExtra(Const.INTENT_GROUPID_EXTRA_NAME);
			mContactGroup = mContactOrgDao.getGroupBeanById(mGroupId, mGroupType);
		}
		if (mContactGroup != null) {
			if (mContactGroup.getSubject() != null && !mContactGroup.getSubject().isEmpty()) {
				mGroupName = mContactGroup.getSubject();
			} else {
				mGroupName = mContactGroup.getNaturalName();
			}
		} else {
			List<User> mUserList = mContactOrgDao.queryUsersByGroupName(mGroupId, Const.CONTACT_DISGROUP_TYPE);
			mGroupName = DisCreateActivity.calculateGroupName(mUserList,null);
		}
	}
	
	private void findViews() {
		mRenameDisgroupET = (EditText) findViewById(R.id.et_disgroup_rename_edit_text);
		mRenameDisgroupET.setText(mGroupName);
	}
	
	/**
	 * 添加讨论组信息更改监听器
	 */
	private void addGroupUpdatedListener() {
		mCommonReceiver = new CommonReceiver();
		mCommonReceiver.setUpdateGroupDataListener(new updateGroupDataListener() {
			
			@Override
			public void updateGroupData(int mGroupType) {
				if (mGroupType == DisGroupRenameActivity.this.mGroupType) {
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
				if (mGroupType == DisGroupRenameActivity.this.mGroupType) {
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
	
	public void onTitleViewClickListener(View v) throws IOException {
		switch (v.getId()) {
		case R.id.tv_disgroup_rename_title_back:
			mNewName = mRenameDisgroupET.getText().toString().trim();
			if (mNewName.isEmpty()) {
//				ToastUtil.toastAlerMessage(DisGroupRenameActivity.this, "网络异常，请检查网络",Toast.LENGTH_SHORT);
				return;
			} else {
				//转码
				mNewName = TextUtils.htmlEncode(mNewName);
			}
			if (NetWorkUtil.isNetworkAvailable(DisGroupRenameActivity.this)) {
				sendRequestIQPacket(Const.REQ_IQ_XMLNS_GET_GROUP);
				mHandler.sendEmptyMessageDelayed(0, 500);
			} else {
				ToastUtil.toastAlerMessage(DisGroupRenameActivity.this, "网络异常，请检查网络",Toast.LENGTH_SHORT);
			}
			break;

		default:
			break;
		}
	}

//	@Override
//	public void receivedReqIQResult(ReqIQResult packet) {
//		// 接收到回执信息，判断退出操作是否成功
//		L.i(TAG, "收到回执信息");
//		if (mPacketId.equals(packet.getPacketID())) {
//			if (packet.getCode() == 200) {
//				mContactGroup.setSubject(mNewName);
////				mContactOrgDao.insertOneContactGroupData(mContactGroup, Const.CONTACT_DISGROUP_TYPE);
//				Intent updateViewIntent = new Intent(Const.BROADCAST_ACTION_UPDATE_GROUP);
//				updateViewIntent.putExtra(Const.INTENT_GROUPTYPE_EXTRA_NAME, mGroupType);
//				sendBroadcast(updateViewIntent);
//			} else {
//			}
//		}
//	}
	
	/**
	 * 发送IQ请求
	 * @param nameSpace
	 *            命名空间
	 */
	private void sendRequestIQPacket(String nameSpace) {
		String paramStr = getParamsJson();
		ReqIQ iq = new ReqIQ();
		mPacketId = iq.getPacketID();
		iq.setNameSpace(nameSpace);
		iq.setParamsJson(paramStr);
		iq.setType(org.jivesoftware.smack.packet.IQ.Type.SET);
		iq.setAction(Const.INTERFACE_ACTION_GROUP_RENAME);
		iq.setFrom(JIDUtil.getJIDByAccount(AppController.getInstance().mSelfUser.getUserNo()));
		iq.setTo("admin@"+mXmppConnectionManager.getServiceName());
		L.i("RenameDisGroup", "iq xml ->" + iq.toXML());
		try {
			mXmppConnectionManager.sendPacket(iq);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 拼接字符串 
	 * @return  eg： {"subject":"请大家下午2点半开会","groupName":"leklddkdfd"}
	 */
	private String getParamsJson() {
//		mNewName = mRenameDisgroupET.getText().toString().trim();
		StringBuilder mBuilder = new StringBuilder();
		if (mGroupType == Const.CONTACT_DISGROUP_TYPE) {
			mBuilder.append("{\"subject\":\"");
		} else {
			mBuilder.append("{\"subject\":\"");
		}
		
		mBuilder.append(mNewName);
		mBuilder.append("\",\"groupName\":\"");
		mBuilder.append(mContactGroup.getGroupName());
		mBuilder.append("\"}");
		return mBuilder.toString();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mCommonReceiver);
	}

}
