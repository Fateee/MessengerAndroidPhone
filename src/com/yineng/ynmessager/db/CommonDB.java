package com.yineng.ynmessager.db;

import com.yineng.ynmessager.util.L;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;

/**
 * 公共信息 数据库，与登陆账号没关系
 * 
 * @author Yutang
 * 
 */
public class CommonDB extends SQLiteOpenHelper {
	private static final int VERSION = 1;
	private static CommonDB mCommonDB;
	private static final String DATABASE_NAME = "common.db";// 创建数据库名

	/**
	 * 懒汉式单例方法
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized CommonDB getInstance(Context context) {
		if (mCommonDB == null) {
			mCommonDB = new CommonDB(context.getApplicationContext());
		}
		return mCommonDB;
	}

	/**
	 * 带参构造函数
	 * 
	 * @param context
	 */
	private CommonDB(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 登录用户表
		L.d(getLoginUserTableSql());
		db.execSQL(getLoginUserTableSql());

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * @return 登录用户 记录表
	 */
	public String getLoginUserTableSql() {
		StringBuilder sb_LoginUser = new StringBuilder();
		sb_LoginUser.append("CREATE TABLE [LoginUser]("); // 表名
		sb_LoginUser.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT,");// 主键,自增
		sb_LoginUser.append("[account] varchar(20),"); // 登录帐号
		sb_LoginUser.append("[userNo] varchar(36),"); // 用户ID
		sb_LoginUser.append("[passWord] varchar(32),"); // 密码
		sb_LoginUser.append("[theme] INTEGER,"); // 主题
		sb_LoginUser.append("[fileSavePath] text,"); //用户接收文件存储路径
		sb_LoginUser.append("[firstLoginDate] varchar(30),"); // 第一次登录时间
		sb_LoginUser.append("[lastLoginDate] varchar(30))"); // 最近登录时间
		return sb_LoginUser.toString();
	}
	
}
