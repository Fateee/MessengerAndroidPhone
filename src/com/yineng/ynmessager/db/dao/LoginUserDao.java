package com.yineng.ynmessager.db.dao;

import java.util.ArrayList;
import java.util.List;

import com.yineng.ynmessager.bean.login.LoginUser;
import com.yineng.ynmessager.db.CommonDB;
import com.yineng.ynmessager.util.L;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * @author Yutang date 2014-12-28 保存用户的登陆信息
 */
public class LoginUserDao {
	private SQLiteDatabase mDB;
	private final String TABLE_NAME = "LoginUser";

	/**
	 * @param context
	 * @param account
	 *            构造函数，
	 */
	public LoginUserDao(Context context) {
		mDB = (CommonDB.getInstance(context)).getWritableDatabase();
	}

	/**
	 * 保存登陆信息
	 * 
	 * @param loginUser
	 */
	public void saveLoginUser(LoginUser loginUser) {
		if (isExists(loginUser.getAccount())) {

			updateLoginUser(loginUser);

		} else {
			insertNewLoginUser(loginUser);
		}
	}

	/**
	 * 获取最近登陆的账号信息
	 * 
	 * @return
	 */
	public List<LoginUser> getLoginUsers() {
		List<LoginUser> list = new ArrayList<LoginUser>();
		LoginUser user = null;
		String sql = "select * from LoginUser Order By lastLoginDate desc";
		Cursor cursor = mDB.rawQuery(sql.toString(), null);
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				user = new LoginUser();
				user.setId(cursor.getInt(cursor.getColumnIndex("id")));
				user.setAccount(cursor.getString(cursor
						.getColumnIndex("account")));
				user.setPassWord(cursor.getString(cursor
						.getColumnIndex("passWord")));
				user.setLastLoginDate(cursor.getString(cursor
						.getColumnIndex("lastLoginDate")));
				user.setTheme(cursor.getInt(cursor.getColumnIndex("theme")));
				user.setFileSavePath(cursor.getString(cursor.getColumnIndex("fileSavePath")));
				user.setFirstLoginDate(cursor.getString(cursor.getColumnIndex("firstLoginDate")));
				list.add(user);
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	/**
	 * 插入一条用户登录新记录
	 * 
	 * @param mLoginUser
	 *            登录用户
	 */
	private void insertNewLoginUser(LoginUser loginUser) {
		L.v("LoginUserDao", "LoginUserDao:insertNewLoginUser->");
		ContentValues userContentValues = new ContentValues();
		userContentValues.put("userNo", loginUser.getUserNo());
		userContentValues.put("account", loginUser.getAccount());
		userContentValues.put("passWord", loginUser.getPassWord());
		userContentValues.put("firstLoginDate", loginUser.getFirstLoginDate());
		userContentValues.put("lastLoginDate", loginUser.getLastLoginDate());
		userContentValues.put("theme", loginUser.getTheme());
		userContentValues.put("fileSavePath", loginUser.getFileSavePath());
		mDB.insert(TABLE_NAME, null, userContentValues);
	}

	/**
	 * 更新登陆信息
	 * 
	 * @param loginUser
	 */
	private void updateLoginUser(LoginUser loginUser) {
		L.v("LoginUserDao", "LoginUserDao:updateLoginUser->");
		ContentValues userContentValues = new ContentValues();
		userContentValues.put("userNo", loginUser.getUserNo());
		userContentValues.put("account", loginUser.getAccount());
		userContentValues.put("passWord", loginUser.getPassWord());
		userContentValues.put("firstLoginDate", loginUser.getFirstLoginDate());
		userContentValues.put("lastLoginDate", loginUser.getLastLoginDate());
		userContentValues.put("theme", loginUser.getTheme());
		userContentValues.put("fileSavePath", loginUser.getFileSavePath());

		mDB.update(TABLE_NAME, userContentValues, "account = ?",
				new String[] { loginUser.getAccount() });
	}

	/**
	 * 判断账号是否存在
	 * 
	 * @param loginAccount
	 * @return
	 */
	public boolean isExists(String account) {
		boolean isExists = false;
		String sql = "select * from LoginUser where account ='" + account + "'";

		L.v("LoginUserDao", "LoginUserDao:isExists:sql->" + sql);
		Cursor cursor = mDB.rawQuery(sql.toString(), null);
		if (cursor != null && cursor.getCount() > 0) {

			isExists = true;
		}
		if (cursor != null) {
			cursor.close();
		}

		return isExists;
	}

	/**
	 * 根据用户ID删除用户登陆记录
	 * 
	 * @param id
	 */
	public void deleteByAccount(String account) {
		mDB.delete("LoginUser", "account = ?", new String[] { account });
	}

	/**
	 * 根据账号id找到该user对象
	 * @param mUserAccount 登陆账号
	 */
	public LoginUser getLoginUserByAccount(String mUserAccount) {
		LoginUser user = null;
		String sql = "select * from LoginUser where account = ?";
		Cursor cursor = mDB.rawQuery(sql.toString(), new String[]{mUserAccount});
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				user = new LoginUser();
				user.setId(cursor.getInt(cursor.getColumnIndex("id")));
				user.setAccount(cursor.getString(cursor
						.getColumnIndex("account")));
				user.setPassWord(cursor.getString(cursor
						.getColumnIndex("passWord")));
				user.setLastLoginDate(cursor.getString(cursor
						.getColumnIndex("lastLoginDate")));
				user.setTheme(cursor.getInt(cursor.getColumnIndex("theme")));
				user.setFileSavePath(cursor.getString(cursor.getColumnIndex("fileSavePath")));
				user.setFirstLoginDate(cursor.getString(cursor.getColumnIndex("firstLoginDate")));
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return user;
	}
	
	/**
	 * 通过用户登陆账号修改其主体
	 * @param mThemeId
	 * @param userAccount
	 */
	public void updateUserThemeByUserAccount(LoginUser loginUser) {
		ContentValues userContentValues = new ContentValues();
		userContentValues.put("theme", loginUser.getTheme());
		mDB.update(TABLE_NAME, userContentValues, "account = ?",
				new String[] { loginUser.getAccount() });
	}
}
