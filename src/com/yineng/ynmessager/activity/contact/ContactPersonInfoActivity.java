package com.yineng.ynmessager.activity.contact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Intents;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.activity.p2psession.P2PChatActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.db.ContactOrgDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.smack.ReceiveReqIQCallBack;
import com.yineng.ynmessager.smack.ReqIQ;
import com.yineng.ynmessager.smack.ReqIQResult;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;

/**
 * 个人详情页面
 * @author 胡毅
 *
 */
public class ContactPersonInfoActivity extends BaseActivity implements ReceiveReqIQCallBack{

	private User mContactInfo;
	private TextView mPersonInfoNameTV;
	private TextView mPersonInfoGenderTV;
	private Context mContext;
	private XmppConnectionManager mXmppConnectionManager;
	private ContactOrgDao mContactOrgDao;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if (mContactOrgInfo != null) {
					mPersonInfoDepartmentTV.setText(mContactOrgInfo.getOrgName());
				}
				if (mContactInfo != null) {
					mPersonInfoNameTV.setText(mContactInfo.getUserName());
					mPersonInfoPhoneNumTV.setText(mContactInfo.getTelephone());
					mPersonInfoEmailTV.setText(mContactInfo.getEmail());
					if (mContactInfo.getGender() == 1) {
						mPersonInfoGenderTV.setText("男");
					} else {
						mPersonInfoGenderTV.setText("女");
					}
					mPersonInfoPostTV.setText(mContactInfo.getPost());
				}
				break;
			default:
				break;
			}
		};
	};
	private TextView mPersonInfoDepartmentTV;
	private TextView mPersonInfoPhoneNumTV;
	private TextView mPersonInfoTeleNumTV;
	private TextView mPersonInfoEmailTV;
	private OrganizationTree mContactOrgInfo;
	private TextView mPersonInfoPostTV;
	private PopupWindow mPopupWindow;
	private LinearLayout mPersonInfoLayoutLL;
	private View mPhonePopView;
	/**
	 * 已更新个人信息的用户
	 */
	private List<User> mUsersList = new ArrayList<User>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = ContactPersonInfoActivity.this;
		setContentView(R.layout.activity_contact_personinfo);
		initData();
		findViews();
	}

	private void initData() {
		mXmppConnectionManager = XmppConnectionManager.getInstance();
		mXmppConnectionManager.addReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL,ContactPersonInfoActivity.this);
		mContactOrgDao = new ContactOrgDao(mContext);
		Intent dataIntent = getIntent();
//		mContactInfo = (User) dataIntent.getSerializableExtra("contactInfo");
		mContactInfo = (User) dataIntent.getParcelableExtra("contactInfo");
		mContactOrgInfo = (OrganizationTree) dataIntent.getSerializableExtra("parentOrg");
		connectSendPacket(0, Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL);
	}

	private void findViews() {
		mPersonInfoLayoutLL= (LinearLayout) findViewById(R.id.ll_contact_personinfo_layout);
		mPersonInfoNameTV = (TextView) findViewById(R.id.tv_contact_personinfo_name);
		mPersonInfoGenderTV = (TextView) findViewById(R.id.tv_contact_personinfo_gender);
		mPersonInfoPostTV = (TextView) findViewById(R.id.tv_contact_personinfo_post);
		mPersonInfoDepartmentTV = (TextView) findViewById(R.id.tv_contact_personinfo_department);
		mPersonInfoPhoneNumTV = (TextView) findViewById(R.id.tv_contact_personinfo_phonenumber);
		mPersonInfoTeleNumTV = (TextView) findViewById(R.id.tv_contact_personinfo_telenumber);
		mPersonInfoEmailTV = (TextView) findViewById(R.id.tv_contact_personinfo_email);
		mPhonePopView = LayoutInflater.from(mContext).inflate(R.layout.contact_personinfo_phone_popview, null);
		mPopupWindow = new PopupWindow(mPhonePopView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
//		mPersonInfoNumTV.setText(mContactInfo.getUserName());
	}
	
	public void onClickListener(View v) {
		switch (v.getId()) {
		case R.id.ll_contact_personinfo_creatChat:///进入会话界面
			Intent chatIntent = new Intent(mContext, P2PChatActivity.class);
			chatIntent.putExtra(Const.INTENT_USER_EXTRA_NAME, mContactInfo);
			startActivity(chatIntent);
			break;
		case R.id.ll_contact_personinfo_sendcard://发送名片
			sendContactCard();
			break;
		case R.id.ll_contact_personinfo_phonenumber://点击电话，打开弹出框
			if (!mPersonInfoPhoneNumTV.getText().toString().isEmpty()) {
				showPhonePopWindow();
			}
			break;
		case R.id.ll_contact_personinfo_telenumber://点击手机，打开弹出框
			if (!mPersonInfoTeleNumTV.getText().toString().isEmpty()) {
				showPhonePopWindow();
			}
			break;
		case R.id.tv_personinfo_pop_add_contact://弹出框之添加号码到联系人
			openContactAppForAddContact();
			mPopupWindow.dismiss();
			break;
		case R.id.tv_personinfo_pop_call_contact://弹出框之打电话
			callContactPhone();
			mPopupWindow.dismiss();
			break;
		case R.id.tv_personinfo_pop_sent_sms://弹出框之发短信
			sendContactMsg();
			mPopupWindow.dismiss();
			break;
		case R.id.tv_personinfo_pop_cancle_pop://取消
			mPopupWindow.dismiss();
			break;
		case R.id.bt_contact_personinfo_refresh:
			mUsersList.clear();
			connectSendPacket(0, Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL);
			ToastUtil.toastAlerMessageCenter(mContext, "已更新个人信息", 1000);
			break;
		default:
			break;
		}
	}
	
	public void back(View view) {
		finish();
		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
	}
	
	/**
	 * 发送IQ请求获取该联系人详细信息
	 * 
	 * @param action
	 * @param nameSpace
	 */
	private void connectSendPacket(int action, String nameSpace) {
		ReqIQ iq = new ReqIQ();
		iq.setNameSpace(nameSpace);
		iq.setParamsJson("{\"userNoList\":[\""+mContactInfo.getUserNo()+"\"]}");
//		iq.setFrom("admin" + "@" + mXmppConnectionManager.getServiceName());
		L.d("ContactPersonInfoActivity", "iq xml ->" + iq.toXML());
		try {
			mXmppConnectionManager.sendPacket(iq);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * IQ消息回执
	 * 
	 */
	@Override
	public void receivedReqIQResult(ReqIQResult packet) {
		String nameSpace = packet.getNameSpace();
		if (nameSpace.equals(Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL)) {
			L.v("ContactPersonInfoActivity", "iq xml ->" + packet.toXML());
			if (packet.getCode() == 200) {
//				mContactInfo = JSON.parseObject(packet.getResp(),User.class);
				try {
					JSONArray tempJsonArray = new JSONArray(packet.getResp());
					if (tempJsonArray != null && tempJsonArray.length() > 0) {
						for (int i = 0; i < tempJsonArray.length(); i++) {
							User tempUser = new User();
							JSONObject mUserObject = tempJsonArray.optJSONObject(i);
							tempUser.setCreateTime(mUserObject.optInt("createTime"));
							tempUser.setGender(mUserObject.optInt("gender"));
							tempUser.setUserType(mUserObject.optInt("userType"));
							tempUser.setDayOfBirth(mUserObject.optString("dayOfBirth"));
							tempUser.setEmail(mUserObject.optString("email"));
							tempUser.setHeadUrl(mUserObject.optString("headUrl"));
							tempUser.setJoinTime(mUserObject.optString("joinTime"));
							tempUser.setPost(mUserObject.optString("post"));
							tempUser.setSigature(mUserObject.optString("signature"));
							tempUser.setTelephone(mUserObject.optString("telephone"));
							tempUser.setUserName(mUserObject.optString("userName"));
							tempUser.setUserNo(mUserObject.optString("userNo"));
							mUsersList.add(tempUser);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (mUsersList.size() > 0) {
					mContactInfo = mUsersList.get(0);
					mContactOrgDao.insertUpdateOneUserData(mContactInfo);
				}
				mHandler.sendEmptyMessage(0);
			}

		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mXmppConnectionManager != null) {
			mXmppConnectionManager.removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_PERSON_DETAIL);
		}
	}
	
	/**
	 * 打开弹出框
	 */
	public void showPhonePopWindow(){
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
		mPopupWindow.showAtLocation(mPersonInfoLayoutLL, Gravity.BOTTOM, 0, 0);
		mPopupWindow.setAnimationStyle(R.style.AnimBottom);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.update();
	}
	
	/**
	 * 添加号码到联系人
	 */
	public void openContactAppForAddContact() {
		if (mContactInfo != null && !mContactInfo.getTelephone().isEmpty()) {
			Intent intent = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(
					Uri.parse("content://com.android.contacts"), "contacts"));
			intent.putExtra(Intents.Insert.NAME, mContactInfo.getUserName());
			intent.putExtra(Intents.Insert.PHONE, mContactInfo.getTelephone());
			startActivity(intent);
		}
	}
	
	/**
	 * 拨打号码
	 */
	public void callContactPhone() {
		if (mContactInfo != null && !mContactInfo.getTelephone().isEmpty()) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:"+mContactInfo.getTelephone()));
			startActivity(intent);
		}
	}
	
	/**
	 * 发送消息
	 */
	public void sendContactMsg() {
		if (mContactInfo != null && !mContactInfo.getTelephone().isEmpty()) {
			Uri smsToUri = Uri.parse("smsto:"+mContactInfo.getTelephone());
			Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
			startActivity(intent);
		}
	}
	
	/**
	 * 发送名片
	 */
	public void sendContactCard() {
		if (mContactInfo != null) {
			mPersonInfoPostTV.setText(mContactInfo.getPost());
			Uri smsToUri = Uri.parse("smsto:");
			Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
			intent.putExtra("sms_body", "姓名: "+mContactInfo.getUserName()+", 手机: "+mContactInfo.getTelephone()
					+", 邮箱: "+mContactInfo.getEmail());
			startActivity(intent);
		} else {
			ToastUtil.toastAlerMessage(mContext, "没有该用户信息", 1000);
		}
	}
}
