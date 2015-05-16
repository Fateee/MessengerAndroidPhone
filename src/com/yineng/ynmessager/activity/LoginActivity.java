package com.yineng.ynmessager.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.p2psession.P2PChatActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.login.LoginThread;
import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.db.UserAccountDB;
import com.yineng.ynmessager.db.dao.LoginUserDao;
import com.yineng.ynmessager.manager.XmppConnectionManager;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.NetWorkUtil;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.util.ToastUtil;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
/**
 * @author YINENG 用户登陆界面
 * 
 */
public class LoginActivity extends BaseActivity {
	private String tag = LoginActivity.class.getSimpleName();
	private Context mContext;// 全局上下文
	private ImageButton mSpinnerIB;// 下拉选项
	private String mUserAccount;// 用户账号
	// private String mUserId;
	private String mUserPassword;// 用户密码
	private String mServiceAddress;// 用户地址
	// UI references.
	private EditText mUserAccountEV;// 用户账号
	private EditText mPasswordEV;// 用户密码
	private EditText mServiceAddressEV;// 用户地址
	private View mServerstatusLL;// 服务器显示按钮
	private Button mLoginBT;// 登陆按钮
	private TextView mAddressStatusTV;// 服务器显示状态
	private ImageView mAddressStautsIV;// 服务器图标
	private LinearLayout mServiceAddressLL;// 服务器区域
	private ProgressDialog mProgressDialog;// 进度框
	private LoginUserDao mLoginUserDao;// 用户登陆账号列表
	private boolean mIsHid;// 服务器地址是否隐藏
	private List<LoginUser> mUserList;
	private XmppConnectionManager mXmppConnectionManager;//
	private LastLoginUserSP mLastUser;// 最近一次登陆账号信息
	private MyAdapter mLoginUserAdapter;
	private PopupWindow pop;
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			int x = msg.arg1;
			switch (x) {

			case LoginThread.LOGIN_START:// 登陆中，登陆按钮不可用
				showProgressD();
				break;
			case LoginThread.LOGIN_FAIL:// 登陆失败
				L.i(tag, "LOGIN_FAIL");
				hidProgessD();
				ToastUtil.toastAlerMessageiconTop(mContext,
						LoginActivity.this.getLayoutInflater(), "登录失败", 1000);
				break;
			case LoginThread.LOGIN_SUCCESS:// 登陆成功
				// hidProgessD();
				L.i(tag, "LOGIN_SUCCESS");
				saveLastUser();// 保存登陆信息
				Intent serviceIntent = new Intent(mContext,
						XmppConnService.class);
				LoginActivity.this.startService(serviceIntent);

				hidProgessD();
				ToastUtil.toastAlerMessageCenter(mContext, "登录成功！", 1000);
				startMainUIAndService();
				break;
			case LoginThread.LOGIN_TIMEOUT:// 建立连接失败
				hidProgessD();
				ToastUtil.toastAlerMessageCenter(mContext, "建立连接失败！", 1000);
				break;
			default:
				hidProgessD();
				break;
			}
		}
	};
	private PopupWindow mPopupWindow;
	
	private String mDeleteUserAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_layout);
		initViews();
		initUserInfo();
		initViewListener();
	}

	/**
	 * 初始化views
	 */
	private void initViews() {
		mContext = this;
		mSpinnerIB = (ImageButton) findViewById(R.id.ib_login_select_account);
		mUserAccountEV = (EditText) findViewById(R.id.ev_login_useraccount);
		mPasswordEV = (EditText) findViewById(R.id.ev_login_userpassword);
		mServiceAddressEV = (EditText) findViewById(R.id.ev_login_serviceaddress);
		mAddressStatusTV = (TextView) findViewById(R.id.tv_login_addressstatus);
		mAddressStautsIV = (ImageView) findViewById(R.id.iv_login_addressstatus);
		mServiceAddressLL = (LinearLayout) findViewById(R.id.ll_login_servers);
		mLoginBT = (Button) findViewById(R.id.bt_login_login);
		mServerstatusLL = (LinearLayout) findViewById(R.id.ll_login_serverstatus);
		View mPhonePopView = LayoutInflater.from(mContext).inflate(R.layout.contact_personinfo_phone_popview, null);
		TextView mDeleteTitleTV = (TextView) mPhonePopView.findViewById(R.id.tv_personinfo_pop_add_contact);
		TextView mOnlyDelAccountTV = (TextView) mPhonePopView.findViewById(R.id.tv_personinfo_pop_call_contact);
		TextView mDeleteAllTV= (TextView) mPhonePopView.findViewById(R.id.tv_personinfo_pop_sent_sms);
		mDeleteTitleTV.setText("确定要删除此账号?");
		mDeleteTitleTV.setTextColor(Color.RED);
		mOnlyDelAccountTV.setText("仅删除账号");
		mDeleteAllTV.setText("删除账号和本地记录");
		mPopupWindow = new PopupWindow(mPhonePopView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
	}

	/**
	 * 初始化用户界面
	 */
	private void initUserInfo() {
		mProgressDialog = new ProgressDialog(mContext);
		mLastUser = LastLoginUserSP.getInstance(mContext);
		mLoginUserDao = new LoginUserDao(mContext);
		String address = mLastUser.getUserServicesAddress();
		mUserAccountEV.setText(mLastUser.getUserAccount());
		mPasswordEV.setText(mLastUser.getUserPassword());
		// 如果本地没有服务器地址则显示，否则隐藏
		if (TextUtils.isEmpty(address)) {
			if (mIsHid) {
				showServerAddress();
			}
		} else {
			mServiceAddressEV.setText(address);
			if (!mIsHid) {
				hidServerAddress();
			}
		}
	}

	/**
	 * 为view设置监听器
	 */
	private void initViewListener() {
		// 单击隐藏/显示服务器地址
		mServerstatusLL.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				ShowOrHidAddress();
			}
		});
		// 如果有两个以上账号，则显示账号选择框
		mUserList = mLoginUserDao.getLoginUsers();
		if (mUserList != null && mUserList.size() > 0) {
			mSpinnerIB.setVisibility(View.VISIBLE);
			List<String> accounts = new ArrayList<String>();
			for (LoginUser user : mUserList) {
				accounts.add(user.getAccount());
			}
			/*
			 * ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
			 * android.R.layout.simple_spinner_item, accounts);
			 * adapter.setDropDownViewResource
			 * (android.R.layout.simple_spinner_dropdown_item);
			 * mSpinner.setAdapter(adapter);
			 * 
			 * mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			 * public void onItemSelected(AdapterView<?> parent, View view, int
			 * position, long id) { ((TextView) view).setText(""); LoginUser
			 * user = mUserList.get(position); if (user != null) {
			 * mUserAccountEV.setText(user.getAccount());
			 * mPasswordEV.setText(user.getPassWord()); } }
			 * 
			 * public void onNothingSelected(AdapterView<?> parent) {
			 * 
			 * } });
			 */
			mSpinnerIB.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					ListView listView = new ListView(mContext);
					listView.setCacheColorHint(R.color.common_blue);
					listView.setVerticalScrollBarEnabled(false);

					mLoginUserAdapter = new MyAdapter();
					listView.setAdapter(mLoginUserAdapter);

					pop = new PopupWindow(listView, mUserAccountEV.getWidth(),
							LayoutParams.WRAP_CONTENT, true);

					pop.setBackgroundDrawable(new ColorDrawable(
							R.color.common_blue));
					pop.showAsDropDown(mUserAccountEV, 0, -5);
				}
			});

		} else {
			// mSpinner.setVisibility(View.GONE);
			mSpinnerIB.setVisibility(View.GONE);
		}

		// 单击登陆按钮触发事件
		mLoginBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mUserAccount = mUserAccountEV.getText().toString().trim();
				mUserPassword = mPasswordEV.getText().toString().trim();
				mServiceAddress = mServiceAddressEV.getText().toString().trim();
				// 验证用户输入及提示用输入内容
				if (TextUtils.isEmpty(mUserAccount)) {
					mUserAccountEV.requestFocus();
					ToastUtil.toastAlerMessageiconTop(mContext,
							LoginActivity.this.getLayoutInflater(),
							getString(R.string.login_errortext_emptyusername),
							2000);
					return;
				}

				if (TextUtils.isEmpty(mUserPassword)) {
					mPasswordEV.requestFocus();
					ToastUtil.toastAlerMessageiconTop(mContext,
							LoginActivity.this.getLayoutInflater(),
							getString(R.string.login_errortext_emptypassowrd),
							2000);
					return;
				}
				if (TextUtils.isEmpty(mServiceAddress)) {
					if (mIsHid) {
						showServerAddress();
					}
					mServiceAddressEV.requestFocus();
					ToastUtil
							.toastAlerMessageiconTop(
									mContext,
									LoginActivity.this.getLayoutInflater(),
									getString(R.string.login_errortext_emptyserviceaddress),
									2000);
					return;
				} else {
					if (!checkServerAddressIsURL(mServiceAddress)) {// 如果用户输入格式不正确
						if (mIsHid) {
							showServerAddress();
						}
						mServiceAddressEV.requestFocus();
						ToastUtil
								.toastAlerMessageiconTop(
										mContext,
										LoginActivity.this.getLayoutInflater(),
										getString(R.string.login_errorformat_serviceaddress),
										2000);
						return;
					}
				}
				// 如果网络不可用，建议用户开启网络连接
				if (!NetWorkUtil.isNetworkAvailable(mContext)) {

					if (mLastUser.isExistsUser())// 如果存在本地账户，进入主页
					{
						Intent mainActivityIntent = new Intent(mContext,
								MainActivity.class);
						LoginActivity.this.startActivity(mainActivityIntent);

					} else {// 无网络无登录记录
						ToastUtil.toastAlerMessageCenter(mContext, "网络连接已断开",
								1000);
						return;
					}
				} else {// 如果网络可用，开启登陆线程
					mXmppConnectionManager = XmppConnectionManager
							.getInstance();
					//如果密码不同，则恢复状态，重新初始化
					if (!mUserPassword.equals(mLastUser.getUserPassword())) {
						XmppConnectionManager.getInstance().setXmppConnectionConfigNull();
					}
					mXmppConnectionManager.init(
							LoginThread.getHostFromAddress(mServiceAddress),
							LoginThread.getPortFromAddress(mServiceAddress),
							Const.SERVICENAME);
					mXmppConnectionManager.doLoginThread(mUserAccount,
							mUserPassword, Const.RESOURSE_NAME, mHandler);
				}
			}
		});
	}

	/**
	 * 保存用户登陆信息
	 */
	private void saveLastUser() {
		// save to sharedpreference
		mLastUser.saveUserAccount(mUserAccount);
		mLastUser.saveUserPassword(mUserPassword);
		mLastUser.saveUserServicesAddress(mServiceAddress);
		mLastUser.setIsFirstLogin(mUserAccount, false);// 是否首次登陆为false
		// save to sqlite
		LoginUser user = null;
		if (mLoginUserDao.isExists(mUserAccount)) {
			user = mLoginUserDao.getLoginUserByAccount(mUserAccount);
		} else {
			user = new LoginUser(mUserAccount);
			user.setFirstLoginDate(TimeUtil
					.getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24));
		}
		user.setPassWord(mUserPassword);
		user.setLastLoginDate(TimeUtil
				.getCurrenDateTime(TimeUtil.FORMAT_DATETIME_24));
		mLoginUserDao.saveLoginUser(user);
	}

	/**
	 * 开启主界面和服务
	 */
	private void startMainUIAndService() {
		Intent mainActivityIntent = new Intent(mContext, MainActivity.class);
		LoginActivity.this.startActivity(mainActivityIntent);
		finishActivity();
	}

	private void showProgressD() {
		if (mProgressDialog != null && !mProgressDialog.isShowing()) {
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mProgressDialog.setMessage("正在登陆。。。");
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
			mProgressDialog.setCanceledOnTouchOutside(false);
		}
	}

	private void hidProgessD() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.cancel();
		}
	}

	private void ShowOrHidAddress() {
		String mAddressStatusMessage = mAddressStatusTV.getText().toString()
				.trim();
		//
		if (mAddressStatusMessage.equals(LoginActivity.this
				.getString(R.string.login_tvtext_hidserviceaddress))) {
			if (!mIsHid) {
				hidServerAddress();
			}

		} else {
			if (mIsHid) {
				showServerAddress();
			}
		}
	}

	private void hidServerAddress() {
		mIsHid = true;
		Animation animation = AnimationUtils.loadAnimation(this,
				R.anim.login_slide_out_to_top_translate);
		animation.setFillAfter(true);
		mServiceAddressLL.startAnimation(animation);
		//
		mServiceAddressEV.setEnabled(false);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				Animation animation1 = AnimationUtils.loadAnimation(
						getApplicationContext(),
						R.anim.login_slide_out_to_top_translate);
				animation1.setFillAfter(true);
				mLoginBT.startAnimation(animation1);

				animation1.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						mLoginBT.clearAnimation();
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
								android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
								getResources().getDimensionPixelSize(
										R.dimen.login_editer_height));

						params.leftMargin = getResources()
								.getDimensionPixelSize(
										R.dimen.login_right_left_padding);

						params.rightMargin = getResources()
								.getDimensionPixelSize(
										R.dimen.login_right_left_padding);

						params.topMargin = getResources()
								.getDimensionPixelSize(
										R.dimen.login_loginbt_paddingtop);
						params.addRule(RelativeLayout.BELOW,
								R.id.ll_login_serverstatus);

						mLoginBT.setLayoutParams(params);
					}
				});
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

				mAddressStautsIV.setImageResource(R.drawable.login_server_down);
				mAddressStatusTV.setText(LoginActivity.this
						.getString(R.string.login_tvtext_showserviceaddress));
			}
		});
	}

	private void showServerAddress() {
		mIsHid = false;
		Animation animation = AnimationUtils.loadAnimation(this,
				R.anim.login_slide_in_from_top_translate);
		animation.setFillAfter(true);
		mServiceAddressLL.startAnimation(animation);
		//
		mServiceAddressEV.setEnabled(true);
		// mySpinner.setEnabled(true);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

				Animation animation1 = AnimationUtils.loadAnimation(
						getApplicationContext(),
						R.anim.login_slide_in_from_top_translate2);
				mLoginBT.startAnimation(animation1);

				animation1.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						mLoginBT.clearAnimation();
						RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
								android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
								getResources().getDimensionPixelSize(
										R.dimen.login_editer_height));

						params.leftMargin = getResources()
								.getDimensionPixelOffset(
										R.dimen.login_right_left_padding);
						params.rightMargin = getResources()
								.getDimensionPixelOffset(
										R.dimen.login_right_left_padding);
						params.topMargin = getResources()
								.getDimensionPixelSize(
										R.dimen.login_loginbt_paddingtop);

						params.addRule(RelativeLayout.BELOW,
								R.id.ll_server_address);
						mLoginBT.setLayoutParams(params);
					}
				});
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mAddressStautsIV.setImageResource(R.drawable.login_server_up);
				mAddressStatusTV.setText(LoginActivity.this
						.getString(R.string.login_tvtext_hidserviceaddress));
			}
		});

	}

	/**
	 * 关闭界面
	 */
	private void finishActivity() {
		this.finish();
	}

	/**
	 * @param adress
	 * @return
	 */
	private boolean checkServerAddressIsURL(String adress) {
		//
		String urlstr = "";
		String regex = "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP
				+ "|"
				+ "([0-9a-z_!~*'()-]+\\.)*"
				+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." + "[a-z]{2,6})" // domain

				+ "(:[0-9]{1,4})?";

		if (adress.contains(":")) {

			urlstr = adress.toLowerCase();

		} else {

			urlstr = adress.toLowerCase() + ":" + Const.SERVER_PORT;
		}
		return urlstr.matches(regex);
	}

	public void onClickListener(View v) {
		switch (v.getId()) {
		case R.id.tv_personinfo_pop_call_contact://仅删除账号
			deleteSpAndDao(false);
			break;
		case R.id.tv_personinfo_pop_sent_sms://删除账号和本地记录
			deleteSpAndDao(true);
			break;
		default:
			break;
		}
		mPopupWindow.dismiss();
	}
	
	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
		// if (mXmppConnectionManager != null) {
		// mXmppConnectionManager
		// .removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_CLIENT_INIT);
		// mXmppConnectionManager
		// .removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_ORG);
		// mXmppConnectionManager
		// .removeReceiveReqIQCallBack(Const.REQ_IQ_XMLNS_GET_GROUP);
		// }
	}

	class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mUserList.size();
		}

		@Override
		public Object getItem(int position) {
			return mUserList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			LayoutInflater inflater = LayoutInflater
					.from(getApplicationContext());
			View view = inflater.inflate(
					R.layout.item_login_deluser_popwindow_list, parent, false);

			TextView tv_name = (TextView) view.findViewById(R.id.tv_login_users_name);
			ImageButton delete = (ImageButton) view.findViewById(R.id.ib_login_users_delete);

			tv_name.setText(mUserList.get(position).getAccount());

			tv_name.setTag(position);
			delete.setTag(position);
			tv_name.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int index = (Integer) v.getTag();
					mUserAccountEV.setText(mUserList.get(index).getAccount());
					mPasswordEV.setText(mUserList.get(index).getPassWord());
					pop.dismiss();
				}
			});

			delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int index = (Integer) v.getTag();
					/*
					 * mLoginUserDao.deleteByAccount(mUserList.get(index)
					 * .getAccount()); mUserList.remove(index);
					 * adapter.notifyDataSetChanged();
					 */
//					Intent delUserIntent = new Intent(LoginActivity.this,
//							DeleteUserPopupActivity.class);
//					delUserIntent.putExtra("Account", mUserList.get(index)
//							.getAccount());
//					startActivity(delUserIntent);
					mDeleteUserAccount = mUserList.get(index).getAccount();
					mDeleteUserIndex = index;
					pop.dismiss();
					showPhonePopWindow();
				}
			});

			return view;
		}
	}
	
	public void showPhonePopWindow(){
		mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#b0000000")));
		mPopupWindow.showAtLocation(findViewById(R.id.sv_login_view), Gravity.BOTTOM, 0, 0);
		mPopupWindow.setAnimationStyle(R.style.AnimBottom);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.update();
	}
	private int mDeleteUserIndex = 0;
	private void deleteSpAndDao(boolean deleteAll) {
		mLoginUserDao.deleteByAccount(mDeleteUserAccount);
		if (mUserList != null && mDeleteUserIndex < mUserList.size()) {
			mUserList.remove(mDeleteUserIndex);
			mLoginUserAdapter.notifyDataSetChanged();
			if (mUserList.size() == 0) {
				mSpinnerIB.setVisibility(View.GONE);
				mUserAccountEV.setText("");
				mPasswordEV.setText("");
			} else {
				mDeleteUserIndex = 0;
				LoginUser tempLoginUser = mUserList.get(mDeleteUserIndex);
				mUserAccountEV.setText(tempLoginUser.getAccount());
				mPasswordEV.setText(tempLoginUser.getPassWord());
			}
		}
		LastLoginUserSP sp = LastLoginUserSP.getInstance(this);
		if (sp.getUserAccount().equals(mDeleteUserAccount)) {// 如果删除的是当前账号，清空当前账号
			sp.saveUserAccount("");
			sp.saveUserPassword("");
		}
		//删除手机中该账号的文件
		if (deleteAll) {
			String dbPath = (UserAccountDB.getInstance(this, mDeleteUserAccount))
					.getWritableDatabase().getPath();
			UserAccountDB.setNullInstance();// 释放数据库文件句柄

			if (dbPath != null) {// 删除数据库文件
				new File(dbPath).delete();
			}
		}
	}
}
