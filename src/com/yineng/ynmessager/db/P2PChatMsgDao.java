package com.yineng.ynmessager.db;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.SpannableString;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.activity.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

public class P2PChatMsgDao
{
	private SQLiteOpenHelper mDBHelper;
	private String mTable = "Chat";

	public P2PChatMsgDao(Context context)
	{
		mDBHelper = UserAccountDB.getInstance(context,LastLoginUserSP.getInstance(context).getUserAccount());
	}

	// public boolean IsExists(String id) {
	// SQLiteDatabase db = mDBHelper.getReadableDatabase();
	// boolean flag = IsExists(id, db);
	// // db.close();
	// return flag;
	// }

	/**
	 * 清空表数据
	 */
	public int deleteAll()
	{
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		return db.delete(mTable,null,null);
	}

	public int deleteByPacketId(String packetId)
	{
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		return db.delete(mTable,"packetId=?",new String[] {packetId});
	}

	private boolean IsExists(String id, SQLiteDatabase db)
	{
		boolean flag = false;
		Cursor cursor = db.query(mTable,null,"packetId = ? ",new String[] {id + ""},null,null,null);
		if(cursor != null && cursor.getCount() > 0)
		{
			flag = true;
		}else
		{
			flag = false;
		}
		if(cursor != null)
		{
			cursor.close();
		}
		return flag;
	}

	public synchronized void saveOrUpdate(P2PChatMsgEntity msg)
	{
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		if(msg == null)
		{
			return;
		}
		if(IsExists(msg.getPacketId(),db))
		{
			update(msg,db);
		}else
		{
			saveMsg(msg,db);
		}
	}

	private void update(P2PChatMsgEntity msg, SQLiteDatabase db)
	{
		ContentValues values = new ContentValues();
		values.put("packetId",msg.getPacketId());
		values.put("chatUserNo",msg.getChatUserNo());
		values.put("messageType",msg.getMessageType());
		values.put("message",msg.getMessage());
		values.put("mTime",msg.getmTime());
		values.put("isReaded",msg.getIsReaded());
		values.put("fileId",msg.getFileId());
		values.put("isSendMsg",msg.getIsSend());
		values.put("isSuccess",msg.getIsSuccess());
		String[] args = {msg.getPacketId()};
		db.update(mTable,values,"packetId = ?",args);
	}

	private void saveMsg(P2PChatMsgEntity msg, SQLiteDatabase db)
	{
		ContentValues values = new ContentValues();
		values.put("packetId",msg.getPacketId());
		values.put("chatUserNo",msg.getChatUserNo());
		values.put("messageType",msg.getMessageType());
		values.put("message",msg.getMessage());
		values.put("mTime",msg.getmTime());
		values.put("isReaded",msg.getIsReaded());
		values.put("fileId",msg.getFileId());
		values.put("isSendMsg",msg.getIsSend());
		values.put("isSuccess",msg.getIsSuccess());
		if(msg.getMessageType() == GroupChatMsgEntity.MESSAGE)
		{ // 如果是普通消息
			MessageBodyEntity bodyEntity = JSON.parseObject(msg.getMessage(),MessageBodyEntity.class);
			values.put("content",bodyEntity.getContent());
		}
		db.insert(mTable,null,values);
	}

	public synchronized void saveMsg(P2PChatMsgEntity msg)
	{
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		if(msg != null && !IsExists(msg.getPacketId(),db))
		{
			ContentValues values = new ContentValues();
			values.put("packetId",msg.getPacketId());
			values.put("chatUserNo",msg.getChatUserNo());
			values.put("messageType",msg.getMessageType());
			values.put("message",msg.getMessage());
			values.put("mTime",msg.getmTime());
			values.put("isReaded",msg.getIsReaded());
			values.put("isSendMsg",msg.getIsSend());
			values.put("isSuccess",msg.getIsSuccess());
			if(msg.getMessageType() == GroupChatMsgEntity.MESSAGE)
			{ // 如果是普通消息
				MessageBodyEntity bodyEntity = JSON.parseObject(msg.getMessage(),MessageBodyEntity.class);
				values.put("content",bodyEntity.getContent());
			}
			db.insert(mTable,null,values);
		}
		// db.close();
	}

	/**
	 * 更新已读/未读字段
	 * 
	 * @param msg
	 * @param status
	 */
	public synchronized void updateMsgReadStatus(String id, int status)
	{
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		if(IsExists(id,db))
		{
			ContentValues values = new ContentValues();
			values.put("isReaded",status);
			String[] args = {id + ""};
			db.update(mTable,values,"packetId = ?",args);
		}
		// db.close();
	}

	/**
	 * 
	 * 查找指定页数据(从最新的开始查)
	 * 
	 * @param account
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public synchronized LinkedList<P2PChatMsgEntity> getChatMsgEntitiesByPage(String account, int page, int pageSize)
	{
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		LinkedList<P2PChatMsgEntity> list = new LinkedList<P2PChatMsgEntity>();
		Cursor cursor = db.query(mTable,null,"chatUserNo = ? ",new String[] {account + ""},null,null,"mTime DESC",
				String.valueOf(page * pageSize) + "," + String.valueOf(pageSize));
		if(cursor != null && cursor.getCount() > 0)
		{
			while(cursor.moveToNext())
			{
				P2PChatMsgEntity msg = new P2PChatMsgEntity();
				msg.setChatUserNo(cursor.getString(cursor.getColumnIndex("chatUserNo")));
				msg.setIsReaded(cursor.getInt(cursor.getColumnIndex("isReaded")));
				msg.setIsSuccess(cursor.getInt(cursor.getColumnIndex("isSuccess")));
				msg.setMessage(cursor.getString(cursor.getColumnIndex("message")));
				msg.setMessageType(cursor.getInt(cursor.getColumnIndex("messageType")));
				msg.setmTime(cursor.getString(cursor.getColumnIndex("mTime")));
				msg.setPacketId(cursor.getString(cursor.getColumnIndex("packetId")));
				msg.setFileId(cursor.getString(cursor.getColumnIndex("fileId")));
				msg.setIsSend(cursor.getInt(cursor.getColumnIndex("isSendMsg")));
				Log.i("message","message:   " + msg.getMessage());
				list.add(msg);
			}

		}
		if(cursor != null)
		{
			cursor.close();
		}
		// db.close();
		return list;
	}

	public synchronized LinkedList<P2PChatMsgEntity> getChatMsgEntities(String account)
	{
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		LinkedList<P2PChatMsgEntity> list = new LinkedList<P2PChatMsgEntity>();
		Cursor cursor = db.query(mTable,null,"chatUserNo = ? ",new String[] {account + ""},null,null,null);
		if(cursor != null && cursor.getCount() > 0)
		{
			while(cursor.moveToNext())
			{
				P2PChatMsgEntity msg = new P2PChatMsgEntity();
				msg.setChatUserNo(cursor.getString(cursor.getColumnIndex("chatUserNo")));
				msg.setIsReaded(cursor.getInt(cursor.getColumnIndex("isReaded")));
				msg.setIsSuccess(cursor.getInt(cursor.getColumnIndex("isSuccess")));
				msg.setMessage(cursor.getString(cursor.getColumnIndex("message")));
				msg.setMessageType(cursor.getInt(cursor.getColumnIndex("messageType")));
				msg.setmTime(cursor.getString(cursor.getColumnIndex("mTime")));
				msg.setPacketId(cursor.getString(cursor.getColumnIndex("packetId")));
				msg.setFileId(cursor.getString(cursor.getColumnIndex("fileId")));
				msg.setIsSend(cursor.getInt(cursor.getColumnIndex("isSendMsg")));
				Log.i("message","message:   " + msg.getMessage());
				list.addFirst(msg);
			}
		}
		if(cursor != null)
		{
			cursor.close();
		}
		// db.close();
		return list;
	}

	public synchronized ArrayList<GroupChatMsgEntity> getChatMsgEntitiesToFindRecord(String account)
	{
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		ArrayList<GroupChatMsgEntity> list = new ArrayList<GroupChatMsgEntity>();
		Cursor cursor = db.query(mTable,null,"chatUserNo = ? ",new String[] {account + ""},null,null,"mTime DESC");
		if(cursor != null && cursor.getCount() > 0)
		{
			while(cursor.moveToNext())
			{
				GroupChatMsgEntity msg = new GroupChatMsgEntity();
				msg.setChatUserNo(cursor.getString(cursor.getColumnIndex("chatUserNo")));
				msg.setIsReaded(cursor.getInt(cursor.getColumnIndex("isReaded")));
				msg.setIsSuccess(cursor.getInt(cursor.getColumnIndex("isSuccess")));
				msg.setMessage(cursor.getString(cursor.getColumnIndex("message")));
				msg.setMessageType(cursor.getInt(cursor.getColumnIndex("messageType")));
				msg.setmTime(cursor.getString(cursor.getColumnIndex("mTime")));
				msg.setPacketId(cursor.getString(cursor.getColumnIndex("packetId")));
				msg.setFileId(cursor.getString(cursor.getColumnIndex("fileId")));
				msg.setIsSend(cursor.getInt(cursor.getColumnIndex("isSendMsg")));
				Log.i("message","message:   " + msg.getMessage());
				// list.addFirst(msg);
				list.add(msg);
			}
		}
		if(cursor != null)
		{
			cursor.close();
		}
		// db.close();
		return list;
	}

	/**
	 * 
	 * 更新发送状态
	 * 
	 * @param msg
	 * @param status
	 */
	public synchronized void updateMsgSendStatus(String id, int status)
	{
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		if(IsExists(id,db))
		{
			ContentValues values = new ContentValues();
			values.put("isSuccess",status);
			String[] args = {id + ""};
			db.update(mTable,values,"packetId = ?",args);
		}
		// db.close();
	}

	/**
	 * 查询数据库中与指定用户的最新（最后）一条消息记录
	 * 
	 * @param chatUserNo 用户名
	 * @return 返回指定用户的最新（最后）的一条消息记录对象；未查询到返回null
	 */
	public P2PChatMsgEntity queryLastestChatMsg(String chatUserNo)
	{
		P2PChatMsgEntity chat = null;
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		Cursor cur = db.query(mTable,null,"chatUserNo=?",new String[] {chatUserNo},"chatUserNo","max(mTime)",null);
		if(cur.moveToFirst())
		{
			chat = new P2PChatMsgEntity();
			chat.setPacketId(cur.getString(cur.getColumnIndex("packetId")));
			chat.setChatUserNo(cur.getString(cur.getColumnIndex("chatUserNo")));
			chat.setFileId(cur.getString(cur.getColumnIndex("fileId")));
			chat.setMessageType(cur.getInt(cur.getColumnIndex("messageType")));
			chat.setIsSend(cur.getInt(cur.getColumnIndex("isSendMsg")));
			chat.setMessage(cur.getString(cur.getColumnIndex("message")));
			chat.setSpannableString(new SpannableString(cur.getString(cur.getColumnIndex("content"))));
			chat.setmTime(cur.getString(cur.getColumnIndex("mTime")));
			chat.setIsReaded(cur.getInt(cur.getColumnIndex("isReaded")));
			chat.setIsSuccess(cur.getInt(cur.getColumnIndex("isSuccess")));
		}
		cur.close();
		return chat;
	}

	/**
	 * 根据关键字查询聊天记录
	 * 
	 * @param string
	 * @param string
	 * @return
	 */
	public List<Object> querySearchResultByKeyWords(String mChatObjectId, String keyStr)
	{
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		LinkedList<Object> list = new LinkedList<Object>();
		Cursor cursor = db.query(mTable,
				null,
				// "chatUserNo = ? and message like ? ",
				// new String[] {mChatObjectId, "%" + keyStr + "%"},
				"chatUserNo = ? and content like ?",new String[] {mChatObjectId, "%" + keyStr + "%"},null,null,
				"mTime DESC",null);
		if(cursor != null && cursor.getCount() > 0)
		{
			while(cursor.moveToNext())
			{
				GroupChatMsgEntity msg = new GroupChatMsgEntity();
				msg.setPacketId(cursor.getString(cursor.getColumnIndex("packetId")));
				msg.setChatUserNo(cursor.getString(cursor.getColumnIndex("chatUserNo")));
				msg.setFileId(cursor.getString(cursor.getColumnIndex("fileId")));
				msg.setMessageType(cursor.getInt(cursor.getColumnIndex("messageType")));
				msg.setIsSend(cursor.getInt(cursor.getColumnIndex("isSendMsg")));
				msg.setMessage(cursor.getString(cursor.getColumnIndex("message")));
				msg.setmTime(cursor.getString(cursor.getColumnIndex("mTime")));
				msg.setIsReaded(cursor.getInt(cursor.getColumnIndex("isReaded")));
				msg.setIsSuccess(cursor.getInt(cursor.getColumnIndex("isSuccess")));
				Log.i("message","message:   " + msg.getMessage());
				list.addFirst(msg);
			}

		}
		if(cursor != null)
		{
			cursor.close();
		}
		return list;
	}
}
