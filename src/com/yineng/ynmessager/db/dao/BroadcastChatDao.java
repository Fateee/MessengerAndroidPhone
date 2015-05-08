package com.yineng.ynmessager.db.dao;

import java.util.LinkedList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.yineng.ynmessager.bean.BroadcastChat;
import com.yineng.ynmessager.db.UserAccountDB;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;

/**
 * 广播会话实体
 * 
 * @author Yutang
 * 
 */
public class BroadcastChatDao {
	private final String mTable = "BroadcastChat";
	private SQLiteDatabase mDB;
	private String TAG = RecentChatDao.class.getName();

	public BroadcastChatDao(Context context) {
		mDB = UserAccountDB.getInstance(context,
				LastLoginUserSP.getInstance(context).getUserAccount())
				.getWritableDatabase();
	}

	/**
	 * 插入一条新记录
	 * 
	 * @param chat
	 */
	public void insertBroadcastChat(BroadcastChat chat) {
		L.v(TAG, TAG + ":insertBroadcastChat->");
		ContentValues contentValues = new ContentValues();
		contentValues.put("packetId", chat.getPacketId());
		contentValues.put("userNo", chat.getUserNo());
		contentValues.put("userName", chat.getUserName());
		contentValues.put("title", chat.getTitle());
		contentValues.put("message", chat.getMessage());
		contentValues.put("dateTime", chat.getDateTime());
		contentValues.put("messageType", chat.getMessageType());
		contentValues.put("isSend", chat.getIsSend());
		contentValues.put("isSendOk", chat.getIsSendOk());
		contentValues.put("isRead", chat.getIsRead());
		mDB.insert(mTable, null, contentValues);
	}

	/**
	 * @param chat
	 */
	public void updateBroadcastChat(BroadcastChat chat) {
		L.v(TAG, TAG + ":updateRecentChat->");
		ContentValues contentValues = new ContentValues();
		contentValues.put("userNo", chat.getUserNo());
		contentValues.put("userName", chat.getUserName());
		contentValues.put("title", chat.getTitle());
		contentValues.put("message", chat.getMessage());
		contentValues.put("dateTime", chat.getDateTime());
		contentValues.put("messageType", chat.getMessageType());
		contentValues.put("isSend", chat.getIsSend());
		contentValues.put("isSendOk", chat.getIsSendOk());
		contentValues.put("isRead", chat.getIsRead());
		mDB.update(mTable, contentValues, "id = ?", new String[] { chat.getId()
				+ "" });
	}

	/**
	 * 标记已读或未读
	 * 
	 * @param id
	 */
	public void updateIsReadById(int id, int isread) {
		L.v(TAG, TAG + ":updateRecentChat->");
		ContentValues contentValues = new ContentValues();
		contentValues.put("isRead", isread);
		mDB.update(mTable, contentValues, "id = ?", new String[] { id + "" });
	}

	/**
	 * 分页查询
	 * 
	 * @param pageindex
	 * @param count
	 * @return
	 */
	public LinkedList<BroadcastChat> queryBroadcastChatPage(int pageindex,
			int count) {
		LinkedList<BroadcastChat> list = new LinkedList<BroadcastChat>();
		StringBuilder sql = new StringBuilder(
				"select * from BroadcastChat Order By id desc ");

		sql.append(" limit " + pageindex * count + "," + count);
		Cursor cursor = mDB.rawQuery(sql.toString(), null);

		BroadcastChat chat = null;
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				chat = new BroadcastChat();
				chat.setId(cursor.getInt(cursor.getColumnIndex("id")));
				chat.setPacketId(cursor.getString(cursor
						.getColumnIndex("packetId")));
				chat.setUserNo(cursor.getString(cursor.getColumnIndex("userNo")));
				chat.setUserName(cursor.getString(cursor
						.getColumnIndex("userName")));
				chat.setTitle(cursor.getString(cursor.getColumnIndex("title")));
				chat.setMessage(cursor.getString(cursor
						.getColumnIndex("message")));
				chat.setDateTime(cursor.getString(cursor
						.getColumnIndex("dateTime")));
				chat.setMessageType(cursor.getInt(cursor
						.getColumnIndex("messageType")));
				chat.setIsSend(cursor.getInt(cursor.getColumnIndex("isSend")));
				chat.setIsSendOk(cursor.getInt(cursor
						.getColumnIndex("isSendOk")));
				chat.setIsRead(cursor.getInt(cursor.getColumnIndex("isRead")));
				if (pageindex == 0) {
					list.addFirst(chat);
				} else {
					list.add(chat);
				}
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		L.v(TAG, TAG + ":queryBroadcastChatPage:list.size->" + list.size());
		return list;
	}

	/**
	 * 删除广播
	 * 
	 * @param id
	 */
	public void deleteRecentChatById(int id) {
		String sql = "delete from RecentChat where id = " + id;
		mDB.execSQL(sql);
	}
}
