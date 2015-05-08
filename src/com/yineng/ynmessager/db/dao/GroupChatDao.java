package com.yineng.ynmessager.db.dao;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.activity.p2psession.P2PChatMsgEntity;
import com.yineng.ynmessager.bean.groupsession.GroupChatMsgEntity;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.db.UserAccountDB;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;

public class GroupChatDao {
	
	private final String mTableName = "GroupChat";
	private SQLiteDatabase mDb = null;
	
	public GroupChatDao(Context context) {
		mDb = UserAccountDB.getInstance(context, LastLoginUserSP
				.getInstance(context).getUserAccount()).getReadableDatabase();
	}
	
	/**
	 * 根据消息id来进行更新或插入数据库的操作
	 * @param msg
	 */
	public synchronized void saveOrUpdate(GroupChatMsgEntity msg) {
		if (msg == null) {
			return;
		}
		if (IsExists(msg.getPacketId())) {
			updateGroupMsg(msg);
		} else {
			saveMsg(msg);
		}
	}

	/**
	 * 清空表数据
	 */
	public void deleteAll()
	{
		mDb.delete(mTableName,null,null);
	}
	
	/**
	 * 更新一条记录
	 * @param msg
	 */
	private void updateGroupMsg(GroupChatMsgEntity msg) {
		ContentValues values = new ContentValues();
		values.put("packetId", msg.getPacketId());
		values.put("groupId", msg.getGroupId());	// 群ID
		values.put("chatUserNo", msg.getChatUserNo());	// 发送：id为自己  接收：id为别人
		values.put("senderName", msg.getSenderName());//发送者名称
		values.put("fileId", msg.getFileId());
		values.put("messageType", msg.getMessageType());
		values.put("isSend", msg.getIsSend());  //0:是发送 1:不是发送（即接收）
		values.put("message", msg.getMessage());
		values.put("mTime", msg.getmTime());
		values.put("isReaded", msg.getIsReaded());	//// 是否已读：0是未读,1是已读
		values.put("isSuccess", msg.getIsSuccess());
		String[] args = { msg.getPacketId() };
		mDb.update(mTableName, values, "packetId = ?", args);
	}

	/**
	 * 保存一条消息记录
	 * @param msg
	 */
	private void saveMsg(GroupChatMsgEntity msg) {
		ContentValues values = new ContentValues();
		values.put("packetId", msg.getPacketId());
		values.put("groupId", msg.getGroupId());	// 群ID
		values.put("chatUserNo", msg.getChatUserNo());	// 发送：id为自己  接收：id为别人
		values.put("senderName", msg.getSenderName());//发送者名称
		values.put("fileId", msg.getFileId());
		values.put("messageType", msg.getMessageType());
		values.put("isSend", msg.getIsSend());  //0:是发送 1:不是发送（即接收）
		values.put("message", msg.getMessage());
		values.put("mTime", msg.getmTime());
		values.put("isReaded", msg.getIsReaded());	//// 是否已读：0是未读,1是已读
		values.put("isSuccess", msg.getIsSuccess());
		if (msg.getMessageType() == GroupChatMsgEntity.MESSAGE) {	//如果是普通消息
			MessageBodyEntity bodyEntity = JSON.parseObject(msg.getMessage(),
					MessageBodyEntity.class);
			values.put("content", bodyEntity.getContent());
		}
		mDb.insert(mTableName, null, values);
	}
	
	/**
	 * 保存一条消息记录
	 * @param msg
	 */
	public void saveGroupChatMsg(GroupChatMsgEntity msg) {
		if (msg != null && !IsExists(msg.getPacketId())) {
			ContentValues values = new ContentValues();
			values.put("packetId", msg.getPacketId());
			values.put("groupId", msg.getGroupId());	// 群ID
			values.put("chatUserNo", msg.getChatUserNo());	// 发送：id为自己  接收：id为别人
			values.put("senderName", msg.getSenderName());//发送者名称
			values.put("fileId", msg.getFileId());
			values.put("messageType", msg.getMessageType());
			values.put("isSend", msg.getIsSend());  //0:是发送 1:不是发送（即接收）
			values.put("message", msg.getMessage());
			values.put("mTime", msg.getmTime());
			values.put("isReaded", msg.getIsReaded());	//// 是否已读：0是未读,1是已读
			values.put("isSuccess", msg.getIsSuccess());
			if (msg.getMessageType() == GroupChatMsgEntity.MESSAGE) {	//如果是普通消息
				MessageBodyEntity bodyEntity = JSON.parseObject(msg.getMessage(),
						MessageBodyEntity.class);
				values.put("content", bodyEntity.getContent());
			}
			mDb.insert(mTableName, null, values);
		}
	}

	private boolean IsExists(String packetId) {
		boolean flag = false;
		Cursor cursor = mDb.query(mTableName, null, "packetId = ? ",
				new String[] {packetId + "" }, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			flag = true;
		} else {
			flag = false;
		}
		if (cursor != null) {
			cursor.close();
		}
		return flag;
	}
	
	/**
	 * 更新已读/未读字段
	 * 
	 * @param msg
	 * @param status
	 */
	public synchronized void updateMsgReadStatus(String id, int status) {
		if (IsExists(id)) {
			ContentValues values = new ContentValues();
			values.put("isReaded", status);
			String[] args = { id + "" };
			mDb.update(mTableName, values, "packetId = ?", args);
		}
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
	public synchronized LinkedList<GroupChatMsgEntity> getChatMsgEntitiesByPage(
			String account, int page, int pageSize) {
		LinkedList<GroupChatMsgEntity> list = new LinkedList<GroupChatMsgEntity>();
		Cursor cursor = mDb.query(mTableName,
				null,
				"groupId = ? ",
				new String[] { account + "" },
				null,
				null,
				"mTime DESC",
				String.valueOf(page * pageSize) + ","
						+ String.valueOf(pageSize));
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				GroupChatMsgEntity msg = new GroupChatMsgEntity();
				msg.setPacketId(cursor.getString(cursor
						.getColumnIndex("packetId")));
				msg.setGroupId(cursor.getString(cursor
						.getColumnIndex("groupId")));
				msg.setChatUserNo(cursor.getString(cursor
						.getColumnIndex("chatUserNo")));
				msg.setSenderName(cursor.getString(cursor.getColumnIndex("senderName")));
				msg.setFileId(cursor.getString(cursor.getColumnIndex("fileId")));
				msg.setMessageType(cursor.getInt(cursor
						.getColumnIndex("messageType")));
				msg.setIsSend(cursor.getInt(cursor.getColumnIndex("isSend")));
				msg.setMessage(cursor.getString(cursor
						.getColumnIndex("message")));
				msg.setmTime(cursor.getString(cursor.getColumnIndex("mTime")));
				msg.setIsReaded(cursor.getInt(cursor.getColumnIndex("isReaded")));
				msg.setIsSuccess(cursor.getInt(cursor
						.getColumnIndex("isSuccess")));
				Log.i("groupMessage", "message:   " + msg.getMessage());
				list.add(msg);
			}

		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	
	public synchronized ArrayList<GroupChatMsgEntity> getChatMsgEntities(
			String account) {
		ArrayList<GroupChatMsgEntity> list = new ArrayList<GroupChatMsgEntity>();
		Cursor cursor = mDb.query(mTableName, null, "groupId = ?",
				new String[] { account + "" }, null, null, "mTime DESC");
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				GroupChatMsgEntity msg = new GroupChatMsgEntity();
				msg.setPacketId(cursor.getString(cursor
						.getColumnIndex("packetId")));
				msg.setGroupId(cursor.getString(cursor
						.getColumnIndex("groupId")));
				msg.setChatUserNo(cursor.getString(cursor
						.getColumnIndex("chatUserNo")));
				msg.setSenderName(cursor.getString(cursor.getColumnIndex("senderName")));
				msg.setFileId(cursor.getString(cursor.getColumnIndex("fileId")));
				msg.setMessageType(cursor.getInt(cursor
						.getColumnIndex("messageType")));
				msg.setIsSend(cursor.getInt(cursor.getColumnIndex("isSend")));
				msg.setMessage(cursor.getString(cursor
						.getColumnIndex("message")));
				msg.setmTime(cursor.getString(cursor.getColumnIndex("mTime")));
				msg.setIsReaded(cursor.getInt(cursor.getColumnIndex("isReaded")));
				msg.setIsSuccess(cursor.getInt(cursor
						.getColumnIndex("isSuccess")));
				Log.i("groupMessage", "message:   " + msg.getMessage());
//				list.addFirst(msg);
				list.add(msg);
			}
		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}

	/**
	 * 
	 * 更新发送状态
	 * 
	 * @param msg
	 * @param status
	 */
	public synchronized void updateMsgSendStatus(String id, int status) {
		if (IsExists(id)) {
			ContentValues values = new ContentValues();
			values.put("isSuccess", status);
			String[] args = { id + "" };
			mDb.update(mTableName, values, "packetId = ?", args);
		}
	}
	
	/**
	 * 根据关键字查询聊天记录
	 * @param string
	 * @return
	 */
	public List<Object> querySearchResultByKeyWords(String mChatObjectId, String keyStr) {
		LinkedList<Object> list = new LinkedList<Object>();
		keyStr = keyStr.replaceAll("_", "/_");
		keyStr = keyStr.replaceAll("%", "/%");
		Cursor cursor = mDb.query(
				mTableName,
				null,
				"groupId = ? and content like ? ESCAPE '/'",
				new String[] {mChatObjectId,"%"+keyStr+"%"},
				null,
				null,
				"mTime DESC", null);
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				GroupChatMsgEntity msg = new GroupChatMsgEntity();
				msg.setPacketId(cursor.getString(cursor
						.getColumnIndex("packetId")));
				msg.setGroupId(cursor.getString(cursor
						.getColumnIndex("groupId")));
				msg.setChatUserNo(cursor.getString(cursor
						.getColumnIndex("chatUserNo")));
				msg.setSenderName(cursor.getString(cursor.getColumnIndex("senderName")));
				msg.setFileId(cursor.getString(cursor.getColumnIndex("fileId")));
				msg.setMessageType(cursor.getInt(cursor
						.getColumnIndex("messageType")));
				msg.setIsSend(cursor.getInt(cursor.getColumnIndex("isSend")));
				msg.setMessage(cursor.getString(cursor
						.getColumnIndex("message")));
				msg.setmTime(cursor.getString(cursor.getColumnIndex("mTime")));
				msg.setIsReaded(cursor.getInt(cursor.getColumnIndex("isReaded")));
				msg.setIsSuccess(cursor.getInt(cursor
						.getColumnIndex("isSuccess")));
				Log.i("groupMessage", "message:   " + msg.getMessage());
				list.addFirst(msg);
			}

		}
		if (cursor != null) {
			cursor.close();
		}
		return list;
	}
	
}
