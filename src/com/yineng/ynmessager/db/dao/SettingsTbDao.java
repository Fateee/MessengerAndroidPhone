//***************************************************************
//*    2015-4-23  下午4:23:44
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.db.dao;

import com.yineng.ynmessager.bean.settings.Setting;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * 用户数据库设置表的操作接口
 * 
 * @author 贺毅柳
 * 
 */
public interface SettingsTbDao
{
	/**
	 * 表名
	 */
	public final String TABLE_NAME = "Settings";
	// 列名
	public final String COLUMN_ID = "id";
	public final String COLUMN_DISTRACTION_FREE = "DistractionFree";
	public final String COLUMN_DISTRACTION_FREE_BEGIN_H = "DistractionFree_Begin_H";
	public final String COLUMN_DISTRACTION_FREE_BEGIN_M = "DistractionFree_Begin_M";
	public final String COLUMN_DISTRACTION_FREE_END_H = "DistractionFree_End_H";
	public final String COLUMN_DISTRACTION_FREE_END_M = "DistractionFree_End_M";
	public final String COLUMN_AUDIO = "Audio";
	public final String COLUMN_AUDIO_GROUP = "Audio_Group";
	public final String COLUMN_VIBRATE = "Vibrate";
	public final String COLUMN_VIBRATE_GROUP = "Vibrate_Group";
	public final String COLUMN_RECEIVE_WHEN_EXIT = "ReceiveWhenExit";
	public final String COLUMN_FONT_SIZE = "FontSize";
	public final String COLUMN_SKIN = "Skin";
	public final String COLUMN_ALWAYS_AUTO_RECEIVE_IMG = "AlwaysAutoReceiveImg";
	// 每个列的默认值
	public final int VALUE_DISTRACTION_FREE = 0;
	public final int VALUE_DISTRACTION_FREE_BEGIN_H = 23;
	public final int VALUE_DISTRACTION_FREE_BEGIN_M = 18;
	public final int VALUE_DISTRACTION_FREE_END_H = 7;
	public final int VALUE_DISTRACTION_FREE_END_M = 5;
	public final int VALUE_AUDIO = 1;
	public final int VALUE_AUDIO_GROUP = 1;
	public final int VALUE_VIBRATE = 1;
	public final int VALUE_VIBRATE_GROUP = 1;
	public final int VALUE_RECEIVE_WHEN_EXIT = 1;
	public final int VALUE_FONT_SIZE = 1;
	public final int VALUE_SKIN = 0;
	public final int VALUE_ALWAYS_AUTO_RECEIVE_IMG = 1;

	/**
	 * 插入默认数据，也就是默认的设置。会先检查表是否为空，为空则执行。
	 * 
	 * @return 最后插入的数据的行号。返回0表示为执行插入默认数据；-1表示出错
	 */
	public long insert();

	/**
	 * 插入指定的数据到Settings表中
	 * 
	 * @param values
	 *            ContentValues对象，要插入的数据
	 * @return 最后插入的数据的行号。返回-1表示出错
	 */
	public long insert(ContentValues values);

	/**
	 * 更新Setting表中ID为1的行的数据
	 * 
	 * @param setting
	 *            一个Setting对象
	 * @return 更新操作后受影响的行数
	 */
	public int update(Setting setting);

	/**
	 * 查询Setting表中的数据
	 * 
	 * @param columns
	 *            要查询的列。传入null返回所有列
	 * @return 返回ID为1的行所查询的列的数据（Settings表本来只需要一条数据）
	 */
	public Cursor query(String[] columns);

	/**
	 * 检查表是否为空
	 * 
	 * @return true表示为空，否则false
	 */
	public boolean isEmpty();

	/**
	 * 通过查询数据库来生成对应的Setting实体类对象
	 * 
	 * @return Setting VO
	 */
	public Setting obtainSettingFromDb();
}
