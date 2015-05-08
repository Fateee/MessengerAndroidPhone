package com.yineng.ynmessager.bean.login;

import com.yineng.ynmessager.R;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author YINENG
 * 
 *         sb_LoginUser.append("[account] varchar(20),"); // 登录帐号
 *         sb_LoginUser.append("[userNo] varchar(36),"); // 用户ID
 *         sb_LoginUser.append("[passWord] varchar(32),"); // 密码
 *         sb_LoginUser.append("[theme] text,"); // 主题
 *         sb_LoginUser.append("[fileSavePath] text,"); //用户接收文件存储路径
 *         sb_LoginUser.append("[firstLoginDate] varchar(30),"); // 第一次登录时间
 *         sb_LoginUser.append("[lastLoginDate] varchar(30))"); // 最近登录时间
 */
public class LoginUser implements Parcelable {
	private int id;
	private String userNo;
	private String account;
	private String passWord;
	private int theme=R.style.AppTheme_Light;
	private String fileSavePath;
	private String firstLoginDate;
	private String lastLoginDate;

	public static final Parcelable.Creator<LoginUser> CREATOR = new Parcelable.Creator<LoginUser>() {

		@Override
		public LoginUser createFromParcel(Parcel source) {
			return new LoginUser(source);
		}

		@Override
		public LoginUser[] newArray(int size) {
			return new LoginUser[size];
		}

	};

	public LoginUser() {
	}

	private LoginUser(Parcel source) {
		this.id = source.readInt();
		this.userNo = source.readString();
		this.account = source.readString();
		this.passWord = source.readString();
		this.theme = source.readInt();
		this.fileSavePath = source.readString();
		this.firstLoginDate = source.readString();
		this.lastLoginDate = source.readString();
	}

	public LoginUser(String loginAccount) {
		this.setAccount(loginAccount);
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(String lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public String getFirstLoginDate() {
		return firstLoginDate;
	}

	public void setFirstLoginDate(String firstLoginDate) {
		this.firstLoginDate = firstLoginDate;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTheme() {
		return theme;
	}

	public void setTheme(int theme) {
		this.theme = theme;
	}

	public String getFileSavePath() {
		return fileSavePath;
	}

	public void setFileSavePath(String fileSavePath) {
		this.fileSavePath = fileSavePath;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(this.id);
		dest.writeString(this.userNo);
		dest.writeString(this.account);
		dest.writeString(this.passWord);
		dest.writeInt(this.theme);
		dest.writeString(this.fileSavePath);
		dest.writeString(this.firstLoginDate);
		dest.writeString(this.lastLoginDate);
	}
}
