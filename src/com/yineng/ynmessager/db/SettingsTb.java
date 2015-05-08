//***************************************************************
//*    2015-4-23  下午4:19:29
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.dao.SettingsTbDao;
import com.yineng.ynmessager.util.L;

/**
 * 用户数据库设置表的操作类
 * 
 * @author 贺毅柳
 * @see SettingsTbDao
 */
public class SettingsTb implements SettingsTbDao
{
	public static final String TAG = "SettingsTb";
	private SQLiteDatabase mdb;

	public SettingsTb(Context context, String account)
	{
		mdb = UserAccountDB.getInstance(context,account).getWritableDatabase();
	}

	@Override
	public long insert()
	{
		long rowId = 0;
		if(isEmpty())
		{
			ContentValues values = new ContentValues();
			values.put(COLUMN_DISTRACTION_FREE,VALUE_DISTRACTION_FREE);
			values.put(COLUMN_DISTRACTION_FREE_BEGIN_H,VALUE_DISTRACTION_FREE_BEGIN_H);
			values.put(COLUMN_DISTRACTION_FREE_BEGIN_M,VALUE_DISTRACTION_FREE_BEGIN_M);
			values.put(COLUMN_DISTRACTION_FREE_END_H,VALUE_DISTRACTION_FREE_END_H);
			values.put(COLUMN_DISTRACTION_FREE_END_M,VALUE_DISTRACTION_FREE_END_M);
			values.put(COLUMN_AUDIO,VALUE_AUDIO);
			values.put(COLUMN_AUDIO_GROUP,VALUE_AUDIO_GROUP);
			values.put(COLUMN_VIBRATE,VALUE_VIBRATE);
			values.put(COLUMN_VIBRATE_GROUP,VALUE_VIBRATE_GROUP);
			values.put(COLUMN_RECEIVE_WHEN_EXIT,VALUE_RECEIVE_WHEN_EXIT);
			values.put(COLUMN_FONT_SIZE,VALUE_FONT_SIZE);
			values.put(COLUMN_SKIN,VALUE_SKIN);
			values.put(COLUMN_ALWAYS_AUTO_RECEIVE_IMG,VALUE_ALWAYS_AUTO_RECEIVE_IMG);
			rowId = insert(values);
			L.d(TAG,"初始化设置表数据");
		}
		return rowId;
	}

	@Override
	public long insert(ContentValues values)
	{
		long rowId = mdb.insert(TABLE_NAME,null,values);
		return rowId;
	}

	public int update(Setting setting)
	{
		int affected = 0;
		ContentValues values = new ContentValues();
		values.put(COLUMN_DISTRACTION_FREE,setting.getDistractionFree());
		values.put(COLUMN_DISTRACTION_FREE_BEGIN_H,setting.getDistractionFree_begin_h());
		values.put(COLUMN_DISTRACTION_FREE_BEGIN_M,setting.getDistractionFree_begin_m());
		values.put(COLUMN_DISTRACTION_FREE_END_H,setting.getDistractionFree_end_h());
		values.put(COLUMN_DISTRACTION_FREE_END_M,setting.getDistractionFree_end_m());
		values.put(COLUMN_AUDIO,setting.getAudio());
		values.put(COLUMN_AUDIO_GROUP,setting.getAudio_group());
		values.put(COLUMN_VIBRATE,setting.getVibrate());
		values.put(COLUMN_VIBRATE_GROUP,setting.getVibrate_group());
		values.put(COLUMN_RECEIVE_WHEN_EXIT,setting.getReceiveWhenExit());
		values.put(COLUMN_FONT_SIZE,setting.getFontSize());
		values.put(COLUMN_SKIN,setting.getSkin());
		values.put(COLUMN_ALWAYS_AUTO_RECEIVE_IMG,setting.getAlwaysAutoReceiveImg());
		affected = mdb.update(TABLE_NAME,values,COLUMN_ID + "=?",new String[] {"1"}); // 只会更新ID为1的行
		return affected;
	}

	@Override
	public Cursor query(String[] columns)
	{
		Cursor cur = mdb.query(TABLE_NAME,columns,COLUMN_ID + "=?",new String[] {"1"},null,null,null); // 只在ID为1的行里查询
		return cur;
	}

	@Override
	public boolean isEmpty()
	{
		boolean isEmpty = false;
		StringBuffer sql = new StringBuffer();
		sql.append("select count(").append(COLUMN_ID).append(") from ").append(TABLE_NAME);
		Cursor cur = mdb.rawQuery(sql.toString(),null);
		if(cur.moveToFirst())
		{
			isEmpty = (cur.getInt(0) == 0);
		}
		cur.close();
		return isEmpty;
	}

	@Override
	public Setting obtainSettingFromDb()
	{
		Setting setting = new Setting();
		Cursor cur = query(null);
		for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
		{
			setting.setId(cur.getInt(cur.getColumnIndex(COLUMN_ID)));
			setting.setDistractionFree(cur.getInt(cur.getColumnIndex(COLUMN_DISTRACTION_FREE)));
			setting.setDistractionFree_begin_h(cur.getInt(cur.getColumnIndex(COLUMN_DISTRACTION_FREE_BEGIN_H)));
			setting.setDistractionFree_begin_m(cur.getInt(cur.getColumnIndex(COLUMN_DISTRACTION_FREE_BEGIN_M)));
			setting.setDistractionFree_end_h(cur.getInt(cur.getColumnIndex(COLUMN_DISTRACTION_FREE_END_H)));
			setting.setDistractionFree_end_m(cur.getInt(cur.getColumnIndex(COLUMN_DISTRACTION_FREE_END_M)));
			setting.setAudio(cur.getInt(cur.getColumnIndex(COLUMN_AUDIO)));
			setting.setAudio_group(cur.getInt(cur.getColumnIndex(COLUMN_AUDIO_GROUP)));
			setting.setVibrate(cur.getInt(cur.getColumnIndex(COLUMN_VIBRATE)));
			setting.setVibrate_group(cur.getInt(cur.getColumnIndex(COLUMN_VIBRATE_GROUP)));
			setting.setReceiveWhenExit(cur.getInt(cur.getColumnIndex(COLUMN_RECEIVE_WHEN_EXIT)));
			setting.setFontSize(cur.getInt(cur.getColumnIndex(COLUMN_FONT_SIZE)));
			setting.setSkin(cur.getInt(cur.getColumnIndex(COLUMN_SKIN)));
			setting.setAlwaysAutoReceiveImg(cur.getInt(cur.getColumnIndex(COLUMN_ALWAYS_AUTO_RECEIVE_IMG)));
		}
		cur.close();
		return setting;
	}

}
