package com.yineng.ynmessager.activity;

import java.io.File;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.db.UserAccountDB;
import com.yineng.ynmessager.db.dao.LoginUserDao;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DeleteUserPopupActivity extends BaseActivity implements
		OnClickListener {

	private Button btn_del_account, btn_del_all, btn_cancel;
	private Button mTitleTV;// 服务器显示状态
	private LoginUserDao mLoginUserDao;// 用户登陆账号列表
	private String mUserAccount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_delete_user_alert_dialog);
		mUserAccount = this.getIntent().getStringExtra("Account");
		mTitleTV = (Button) (Button) this
				.findViewById(R.id.tv_login_deluser_title);
		btn_del_account = (Button) this
				.findViewById(R.id.btn_login_deluser_account);
		btn_del_all = (Button) this.findViewById(R.id.btn_login_deluser_all);
		btn_cancel = (Button) this.findViewById(R.id.btn_login_deluser_cancel);
		mTitleTV.setText("确定要删除" + mUserAccount + "吗？");
		mLoginUserDao = new LoginUserDao(this);
		btn_cancel.setOnClickListener(this);
		btn_del_account.setOnClickListener(this);
		btn_del_all.setOnClickListener(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login_deluser_account:// 只删除账号
			deleteSpAndDao();
			gotoLogin();
			break;
		case R.id.btn_login_deluser_all:// 删除账号及本地数据
			deleteSpAndDao();
			String dbPath = (UserAccountDB.getInstance(this, mUserAccount))
					.getWritableDatabase().getPath();
			UserAccountDB.setNullInstance();// 释放数据库文件句柄

			if (dbPath != null) {// 删除数据库文件
				new File(dbPath).delete();
			}
			gotoLogin();
			break;
		case R.id.btn_login_deluser_cancel:// 取消
			finish();
			break;
		default:
			break;
		}
	}

	private void deleteSpAndDao() {
		mLoginUserDao.deleteByAccount(mUserAccount);
		LastLoginUserSP sp = LastLoginUserSP.getInstance(this);
		if (sp.getUserAccount().equals(mUserAccount)) {// 如果删除的是当前账号，清空当前账号
			sp.saveUserAccount("");
			sp.saveUserPassword("");
		}
	}

	private void gotoLogin() {
//		Intent intent = new Intent(this, LoginActivity.class);
//		startActivity(intent);
		finish();
	}
}
