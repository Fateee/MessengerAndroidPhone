package com.yineng.ynmessager.db;

import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.util.L;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class UserAccountDB extends SQLiteOpenHelper {
	public static final String TAG = "UserAccountDB";
	private static final int VERSION = 1;
	private static UserAccountDB UserAccountDB;
	private String mCurrentAccount;
	
	public static synchronized UserAccountDB getInstance(Context context,
			String accountDb) {
		if (UserAccountDB == null) {
			UserAccountDB = new UserAccountDB(context.getApplicationContext(),
					accountDb);
		} else {
			if (UserAccountDB.getCurrentAccount() != null
					&& !UserAccountDB.getCurrentAccount().equals(accountDb)) {
				UserAccountDB = new UserAccountDB(
						context.getApplicationContext(), accountDb);
			}
		}
		return UserAccountDB;
	}

	public static void setNullInstance() {
		if (UserAccountDB != null) {
			UserAccountDB = null;
		}
	}

	private UserAccountDB(Context context, String accountDb) {
		this(context, accountDb + ".db", null, VERSION);
		this.setCurrentAccount(accountDb);
	}

	private UserAccountDB(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql;
		/*
		 * // 登录用户表 L.d(getLoginUserTableSql());
		 * db.execSQL(getLoginUserTableSql());
		 */
		// 初始化信息
		sql=getClientInitTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 组织机构树
		sql = getOrganizationTreeTableSql(); 
		L.d(sql);
		db.execSQL(sql);
		// 用户列表
		sql = getUserTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 用户与机构关系
		sql = getUserTreeRelationTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 用户状态
		sql = getUserStatusTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 群组信息
		sql = getUserGroupTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 讨论组信息
		sql = getDiscussGroupTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 用户与群组关系
		sql = getUserGroupRelationsTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 用户与讨论组关系
		sql = getUserDisGroupRelationTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 共享文件
		sql = getUserGroupFileShareTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 两人会话
		sql = getTwoChatTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 群会话
		sql = getGroupChatTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 讨论组会话
		sql = getDiscussionChatTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 广播
		sql = getDroadcastChatTableSql();
		L.d(sql);
		db.execSQL(sql);
		// 最近会话
		sql = getRecentChatTableSql();
		L.d(sql);
		db.execSQL(sql);
		// // 未读会话
		// L.d(getUnreadChatTableSql());
		// db.execSQL(getUnreadChatTableSql());

		// 文件属性表
		sql = getFileAttrTableSql();
		L.d(sql);
		db.execSQL(sql);
		
		//用户设置表
		sql = getSettingsTableSql();
		L.d(sql);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * @return 登录用户表
	 */
	/*
	 * public String getLoginUserTableSql() { StringBuffer sb_LoginUser = new
	 * StringBuffer(); sb_LoginUser.append("CREATE TABLE [LoginUser]("); // 表名
	 * sb_LoginUser.append("[userNo] varchar(36) PRIMARY KEY,"); // 用户ID
	 * sb_LoginUser.append("[loginAccount] varchar(20),"); // 登录帐号
	 * sb_LoginUser.append("[passWord] varchar(32),"); // 密码
	 * sb_LoginUser.append("[loginDate] varchar(30),"); // 第一次登录时间
	 * sb_LoginUser.append("[theme] text,"); // 主题
	 * sb_LoginUser.append("[fileSavePath] varchar(512),"); // 用户接收文件存储路径
	 * sb_LoginUser.append("[lastLoginDate] varchar(30),"); // 最近登录时间
	 * sb_LoginUser.append("[serverTime] varchar(30))");// 服务器时间 return
	 * sb_LoginUser.toString(); }
	 */

	/**
	 * @return 初始化信息
	 */
	private String getClientInitTableSql() {
		StringBuffer sb_ClientInit = new StringBuffer();
		sb_ClientInit.append("CREATE TABLE [ClientInit]("); // 表名
		sb_ClientInit.append("[disgroup_max_user] varchar(30),"); // 讨论组的最大人数
		sb_ClientInit.append("[group_max_user] varchar(30),"); // 群的最大人数
		sb_ClientInit.append("[max_disdisgroup_can_create] varchar(30),"); // 可创建的最大讨论组个数
		sb_ClientInit.append("[max_group_can_create] varchar(30),"); // 可创建的最大群个数
		sb_ClientInit.append("[servertime] varchar(30),"); // 服务器时间
		sb_ClientInit.append("[org_update_type] varchar(30))"); // 组织机构数据更新方式，如果为0则表示全量更新，1则表示增量更新
		return sb_ClientInit.toString();
	}

	/**
	 * @return 组织机构树
	 */
	private String getOrganizationTreeTableSql() {
		StringBuffer sb_OrgTree = new StringBuffer();
		sb_OrgTree.append("CREATE TABLE [OrganizationTree]("); // 表名
		sb_OrgTree.append("[orgNo] varchar(36) PRIMARY KEY,"); // 机构Id
		sb_OrgTree.append("[parentOrgNo] varchar(36),"); // 父ID
		sb_OrgTree.append("[orgName] varchar(100),"); // 名称
		sb_OrgTree.append("[orgType] INTEGER,"); // 组织类型
		sb_OrgTree.append("[ordId] INTEGER,"); // 排序
		sb_OrgTree.append("[removeTag] INTEGER)"); // 删除标志
		return sb_OrgTree.toString();
	}

	/**
	 * @return 用户列表
	 */
	private String getUserTableSql() {
		StringBuffer sb_User = new StringBuffer();
		sb_User.append("CREATE TABLE [User]("); // 表名
		sb_User.append("[userNo] varchar(36) PRIMARY KEY,"); // 用户ID
		sb_User.append("[userName] varchar(50),"); // 用户姓名
		sb_User.append("[gender] INTEGER,"); // 性别
		sb_User.append("[dayOfBirth] varchar(30),"); // 出生日期
		sb_User.append("[telephone] varchar(16),"); // 联系电话
		sb_User.append("[email] varchar(200),"); // 邮箱地址
		sb_User.append("[post] varchar(200),"); // 职务
		sb_User.append("[headUrl] varchar(200),"); // 头像图标
		sb_User.append("[sigature] varchar(200),"); // 签名
		sb_User.append("[userType] INTEGER,"); // 用户类型
		sb_User.append("[userStatus] INTEGER,"); // 用户在线状态
		sb_User.append("[removeTag] INTEGER)"); // 删除标志
		return sb_User.toString();
	}

	/**
	 * @return 用户与机构关系
	 */
	private String getUserTreeRelationTableSql() {
		StringBuffer sb_UserTreeRelation = new StringBuffer();
		sb_UserTreeRelation.append("CREATE TABLE [UserTreeRelation]("); // 表名
		sb_UserTreeRelation.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT,"); // Id
		sb_UserTreeRelation.append("[userNo] varchar(36),"); // 用户ID
		sb_UserTreeRelation.append("[orgNo] varchar(36),"); // 机构ID
		sb_UserTreeRelation.append("[ordId] INTEGER,"); // 排序
		sb_UserTreeRelation.append("[relationType] varchar(20),"); // 排序
		sb_UserTreeRelation.append("[removeTag] INTEGER)"); // 排序
		// sb_UserTreeRelation
		// .append("FOREIGN KEY [userNo] REFERENCES User [userNo],"); // 用户ID外键
		// sb_UserTreeRelation
		// .append("FOREIGN KEY [orgNo] REFERENCES OrganizationTree [orgNo])");
		// // 组织机构ID外键
		return sb_UserTreeRelation.toString();
	}

	/**
	 * @return 用户状态
	 */
	private String getUserStatusTableSql() {
		StringBuffer sb_UserStatus = new StringBuffer();
		sb_UserStatus.append("CREATE TABLE [UserStatus]("); // 表名
		sb_UserStatus.append("[userNo] varchar(36) PRIMARY KEY,"); // 用户ID
		sb_UserStatus.append("[status] varchar(20),"); // 状态名称
		sb_UserStatus.append("[statusID] INTEGER)"); // 状态ID
		return sb_UserStatus.toString();
	}

	/**
	 * @return 群组信息
	 */
	private String getUserGroupTableSql() {
		StringBuffer sb_userGroup = new StringBuffer();
		sb_userGroup.append("CREATE TABLE [UserGroup]("); // 表名
		sb_userGroup.append("[groupName] varchar(36) PRIMARY KEY,"); // 群ID
		sb_userGroup.append("[createUser] varchar(36),"); // 创建者
		sb_userGroup.append("[createTime] varchar(30),"); // 创建时间
		sb_userGroup.append("[naturalName] varchar(36),"); // 群名称
		sb_userGroup.append("[subject] varchar(100),"); // PC群会话窗口顶部标题下方的描述
		sb_userGroup.append("[maxUsers] INTEGER,"); // 最大成员数
		sb_userGroup.append("[desc] varchar(100),"); // 群公告（描述简介）
		sb_userGroup.append("[notifyMode] INTEGER default "+ContactGroup.NOTIFYMODE_YES+")");  //群的消息提醒方式，默认接收提醒
		return sb_userGroup.toString();
	}

	/**
	 * @return 讨论组信息
	 */
	private String getDiscussGroupTableSql() {
		StringBuffer sb_discussGroup = new StringBuffer();
		sb_discussGroup.append("CREATE TABLE [DiscussGroup]("); // 表名
		sb_discussGroup.append("[groupName] varchar(36) PRIMARY KEY,"); // 讨论组ID
		sb_discussGroup.append("[createUser] varchar(36),"); // 创建者
		sb_discussGroup.append("[createTime] varchar(30),"); // 创建时间
		sb_discussGroup.append("[naturalName] varchar(36),"); // （讨论组名称）
		sb_discussGroup.append("[subject] varchar(100),"); // 讨论组名称
		sb_discussGroup.append("[maxUsers] INTEGER,"); // 最大成员数
		sb_discussGroup.append("[desc] varchar(100),"); // PC讨论组会话窗口顶部标题下方的描述
		sb_discussGroup.append("[notifyMode] INTEGER default "+ContactGroup.NOTIFYMODE_YES+")");  //讨论组消息提醒方式，默认接收提醒
		return sb_discussGroup.toString();
	}

	/**
	 * @return 用户与群组关系
	 */
	private String getUserGroupRelationsTableSql() {
		StringBuffer sb_userGroupRel = new StringBuffer();
		sb_userGroupRel.append("CREATE TABLE [UserGroupRelations]("); // 表名
		sb_userGroupRel.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT,"); // 主键自增
		sb_userGroupRel.append("[userNo] varchar(36),"); // 用户ID
		sb_userGroupRel.append("[groupName] varchar(36) ,"); // 群ID
		sb_userGroupRel.append("[role] varchar(16))"); // 群成员类型 10-创建人 20-管理员
														// 50-一般用户
		return sb_userGroupRel.toString();
	}

	/**
	 * @return 用户与讨论组关系
	 */
	private String getUserDisGroupRelationTableSql() {
		StringBuffer sb_userDisGroupRel = new StringBuffer();
		sb_userDisGroupRel
				.append("CREATE TABLE [UserDiscussionGroupRelations]("); // 表名
		sb_userDisGroupRel.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT,"); // 主键自增
		sb_userDisGroupRel.append("[userNo] varchar(36),"); // 用户ID
		sb_userDisGroupRel.append("[groupName] varchar(36) ,"); // 群ID
		sb_userDisGroupRel.append("[role] varchar(16))"); // 群成员类型 10-创建人 20-管理员
															// 50-一般用户
		return sb_userDisGroupRel.toString();
	}

	/**
	 * @return 共享文件
	 */
	private String getUserGroupFileShareTableSql() {
		StringBuffer sb_userFileShare = new StringBuffer();
		sb_userFileShare.append("CREATE TABLE [UserGroupFileShare]("); // 表名
		sb_userFileShare.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT,"); // 主键自增
		sb_userFileShare.append("[userNo] varchar(36),"); // 用户ID
		sb_userFileShare.append("[groupName] varchar(36) ,"); // 群ID
		sb_userFileShare.append("[name] varchar(100) ,"); // 文件名称
		sb_userFileShare.append("[downPath] varchar(1024) ,"); // 文件存储路径
		sb_userFileShare.append("[upDate] varchar(30) ,"); // 上传时间
		sb_userFileShare.append("[removeTag] INTEGER ,"); // 删除标志
		sb_userFileShare.append("[removeUserId] varchar(36))"); // 删除人Id
		return sb_userFileShare.toString();
	}

	/**
	 * @return 两人会话
	 */
	private String getTwoChatTableSql() {
		StringBuffer sb_twoChat = new StringBuffer();
		sb_twoChat.append("CREATE TABLE [Chat]("); // 表名
		sb_twoChat.append("[packetId] varchar(30) PRIMARY KEY ,"); // 信息包id
		sb_twoChat.append("[chatUserNo] varchar(20),"); // 聊天对方的帐号
		// sb_twoChat.append("[myUserNo] varchar(20),"); // 当前登录帐号
		sb_twoChat.append("[fileId] varchar(100),"); // 文件id,映射FileAttr表中的主键
		sb_twoChat.append("[messageType] INTEGER,"); // 0:普通消息 1:图片 2：文件
		sb_twoChat.append("[isSendMsg] INTEGER,"); // 0:是发送 1:不是发送（即接收）
		sb_twoChat.append("[message] text,"); // 消息内容
		sb_twoChat.append("[content] text,"); // 普通消息的内容,用于记录搜索
		sb_twoChat.append("[mTime] varchar(30),"); // 接收时间
		sb_twoChat.append("[isReaded] INTEGER,"); // 是否已读：0是未读,1是已读
		sb_twoChat.append("[isSuccess] INTEGER)"); // 是否发送成功 0:失败 1成功 默认0
		return sb_twoChat.toString();
	}

	/**
	 * @return 群会话
	 */
	private String getGroupChatTableSql() {
		StringBuffer sb_groupChat = new StringBuffer();
		sb_groupChat.append("CREATE TABLE [GroupChat]("); // 表名
		sb_groupChat.append("[packetId] varchar(30) PRIMARY KEY,"); // Id
		sb_groupChat.append("[groupId] varchar(50),"); // 群ID
		sb_groupChat.append("[chatUserNo] varchar(50),"); // 发送：id为自己 接收：id为别人
		sb_groupChat.append("[senderName] varchar(50),"); // 发送者名称
		sb_groupChat.append("[fileId] varchar(100),"); // 文件id,映射FileAttr表中的主键
		sb_groupChat.append("[messageType] INTEGER,"); // 0:普通消息 1:图片 2：文件
		sb_groupChat.append("[isSend] INTEGER,"); // 0:是发送 1:不是发送（即接收）
		sb_groupChat.append("[message] text,"); // 消息内容
		sb_groupChat.append("[content] text,"); // 普通消息的内容
		sb_groupChat.append("[mTime] varchar(30),"); // 接收时间
		sb_groupChat.append("[isReaded] INTEGER,"); // 是否已读：0是未读,1是已读
		sb_groupChat.append("[isSuccess] INTEGER)"); // 是否发送成功 0:失败 1成功 默认0
		return sb_groupChat.toString();
	}

	/**
	 * @return 讨论组会话
	 */
	private String getDiscussionChatTableSql() {
		StringBuffer sb_disChat = new StringBuffer();
		sb_disChat.append("CREATE TABLE [DiscussionChat]("); // 表名
		sb_disChat.append("[packetId] varchar(30) PRIMARY KEY,"); // Id
		sb_disChat.append("[groupId] varchar(50),"); // 讨论组ID
		sb_disChat.append("[chatUserNo] varchar(50),"); // 发送：id为自己 接收：id为别人
		sb_disChat.append("[senderName] varchar(50),"); // 发送者名称
		sb_disChat.append("[fileId] varchar(100),"); // 文件id,映射FileAttr表中的主键
		sb_disChat.append("[messageType] INTEGER,"); // 0:普通消息 1:图片 2：文件
		sb_disChat.append("[isSend] INTEGER,"); // 0:是发送 1:不是发送（即接收）
		sb_disChat.append("[message] text,"); // 消息内容
		sb_disChat.append("[content] text,"); // 普通消息的内容
		sb_disChat.append("[mTime] varchar(30),"); // 接收时间
		sb_disChat.append("[isReaded] INTEGER,"); // 是否已读：0是未读,1是已读
		sb_disChat.append("[isSuccess] INTEGER)"); // 是否发送成功 0:失败 1成功 默认0
		return sb_disChat.toString();
	}

	/**
	 * @return 广播
	 */
	private String getDroadcastChatTableSql() {
		StringBuffer sb_droadcastChat = new StringBuffer();
		sb_droadcastChat.append("CREATE TABLE [BroadcastChat]("); // 表名
		sb_droadcastChat.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT,");// 主键,自增Id
		sb_droadcastChat.append("[packetId] varchar(30),"); // Id
		sb_droadcastChat.append("[userNo] varchar(20),"); // 发送者ID
		sb_droadcastChat.append("[userName] varchar(40),"); // 发送者昵称
		sb_droadcastChat.append("[title] varchar(100),"); // 主题
		sb_droadcastChat.append("[message] text,"); // 消息内容
		sb_droadcastChat.append("[messageType] INTEGER,"); // 0:普通消息 1:图片 2：文件
		sb_droadcastChat.append("[isSend] INTEGER,"); // 0:发送 1:接收
		sb_droadcastChat.append("[isSendOk] INTEGER DEFAULT 0,"); // 0:发送失败;1成功
		sb_droadcastChat.append("[isRead] INTEGER,"); // 是否已读：0是未读,1是已读
		sb_droadcastChat.append("[dateTime] timestamp)"); // 接收时间
		return sb_droadcastChat.toString();
	}

	/**
	 * @return 最近会话老版本
	 */
	/*
	 * public String getRecentChatTableSql_old() { StringBuffer sb_recentChat =
	 * new StringBuffer(); sb_recentChat.append("CREATE TABLE [RecentChat](");
	 * // 表名 sb_recentChat.append("[packetId] varchar(30) PRIMARY KEY ,"); // Id
	 * sb_recentChat.append("[subject] text,"); // 主题
	 * sb_recentChat.append("[fromUserNo] varchar(20),"); // 发送者ID
	 * sb_recentChat.append("[toUserNo] varchar(20),"); // 接收者ID
	 * sb_recentChat.append("[messageType] INTEGER,"); // 0:发送 1:接收
	 * sb_recentChat.append("[message] text,"); // 消息内容
	 * sb_recentChat.append("[mTime] timestamp,"); // 接收时间
	 * sb_recentChat.append("[sessionTypeId] INTEGER,"); // 0:两人 1:讨论组 2：群组 3:广播
	 * 4:通知 sb_recentChat.append("[resourceName] varchar(20),"); // 资源名称
	 * sb_recentChat.append("[fromUserName] varchar(20),"); // 发送者名称
	 * sb_recentChat.append("[unReadSum] INTEGER)"); // 默认为0 return
	 * sb_recentChat.toString(); }
	 */

	/**
	 * @return 最近会话
	 */
	private String getRecentChatTableSql() {
		StringBuffer sb_recentChat = new StringBuffer();
		sb_recentChat.append("CREATE TABLE [RecentChat]("); // 表名
		sb_recentChat.append("[id] INTEGER PRIMARY KEY AUTOINCREMENT,");// 主键,自增Id
		sb_recentChat.append("[userNo] varchar(30),"); // 对方no，群，个人，讨论组，广播 ，通知no
		sb_recentChat.append("[title] varchar(100),"); // 主题：群，个人，讨论组的名称
		sb_recentChat.append("[content] text,"); // 消息内容
		sb_recentChat.append("[chatType] INTEGER,"); // 0:两人 1:讨论组 2：群组 3:广播
														// 4:通知
		sb_recentChat.append("[senderName] varchar(30),"); // 发送者名称
		sb_recentChat.append("[senderNo] varchar(30),"); // 发送者账号：自己或对方（个人对话时，senderNo==fromUserNo）
		sb_recentChat.append("[dateTime] timestamp,"); // 收发时间
		sb_recentChat.append("[topTime] timestamp,"); // 置顶时间
		sb_recentChat.append("[headUrl] varchar(100),"); // 头像url
		sb_recentChat.append("[headLocalPath] varchar(100),"); // 头像本地路径
		sb_recentChat.append("[isTop] INTEGER DEFAULT 0,"); // 是否置顶 1：置顶，0：不置顶
		sb_recentChat.append("[unReadCount] INTEGER DEFAULT 0,"); // 未读条数， 默认为0
		sb_recentChat.append("[draft] TEXT)");
		return sb_recentChat.toString();
	}

	// /**
	// * @return 未读会话
	// */
	// public String getUnreadChatTableSql() {
	// StringBuffer sb_unreadChat = new StringBuffer();
	// sb_unreadChat.append("CREATE TABLE [UnreadChat]("); // 表名
	// sb_unreadChat.append("[packetId] varchar(30) PRIMARY KEY ,"); // Id
	// sb_unreadChat.append("[subject] text,"); // 主题
	// sb_unreadChat.append("[fromUserNo] varchar(20),"); // 发送者ID
	// sb_unreadChat.append("[toUserNo] varchar(20),"); // 接收者ID
	// sb_unreadChat.append("[messageType] varchar(20),"); // 消息类型
	// sb_unreadChat.append("[message] text,"); // 消息内容
	// sb_unreadChat.append("[mTime] varchar(30),"); // 接收时间
	// sb_unreadChat.append("[sessionTypeId] varchar(16),"); // 会话类型
	// sb_unreadChat.append("[resourceName] varchar(20),"); // 资源名称
	// sb_unreadChat.append("[fromUserName] varchar(20))"); // 发送者名称
	// return sb_unreadChat.toString();
	// }

	/**
	 * @return 文件属性表,所有属性都只针对源文件
	 */
	private String getFileAttrTableSql() {
		StringBuffer sb_fileAttr = new StringBuffer();
		sb_fileAttr.append("CREATE TABLE [FileAttr]("); // 表名
		sb_fileAttr.append("[fileId] varchar(100) PRIMARY KEY ,"); // 文件ID
		sb_fileAttr.append("[fileType] INTEGER DEFAULT 0,");// 0：常规文件 1：图片 2：音频
															// 3：视频// 4：office文档
		// sb_fileAttr.append("[key] varchar(20),"); // img：消息体中图片代码 //
		// file：消息体中文件编码，默认以file0,file1如此命令
		sb_fileAttr.append("[sendUserNo] varchar(36),"); // 发送人ID
		sb_fileAttr.append("[senduserName] varchar(50),"); // 发送人名称
		sb_fileAttr.append("[name] varchar(20),");// img：图片名称 file：文件名称
		sb_fileAttr.append("[size] varchar(20),");// img：原图片大小，以字节为单位
													// file：原文件大小，以字节为单位
		sb_fileAttr.append("[thumbUrl] text,");// img：缩略图地址 file：无
		sb_fileAttr.append("[sourceUrl] text,");// img：原图地址 file：源文件存储地址
		sb_fileAttr.append("[width] varchar(20),");// img：图片源宽度（像素） file：无
		sb_fileAttr.append("[height] varchar(20))");// img：图片源高度（像素） file：无
		return sb_fileAttr.toString();
	}

	/**
	 * 用户设置表
	 * @return 用户设置表建表SQL
	 */
	private String getSettingsTableSql()
	{
		StringBuffer sql = new StringBuffer("CREATE TABLE ")
		.append(SettingsTb.TABLE_NAME).append("(")
		.append(SettingsTb.COLUMN_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
		.append(SettingsTb.COLUMN_DISTRACTION_FREE).append(" INTEGER,")
		.append(SettingsTb.COLUMN_DISTRACTION_FREE_BEGIN_H).append(" INTEGER,")
		.append(SettingsTb.COLUMN_DISTRACTION_FREE_BEGIN_M).append(" INTEGER,")
		.append(SettingsTb.COLUMN_DISTRACTION_FREE_END_H).append(" INTEGER,")
		.append(SettingsTb.COLUMN_DISTRACTION_FREE_END_M).append(" INTEGER,")
		.append(SettingsTb.COLUMN_AUDIO).append(" INTEGER,")
		.append(SettingsTb.COLUMN_AUDIO_GROUP).append(" INTEGER,")
		.append(SettingsTb.COLUMN_VIBRATE).append(" INTEGER,")
		.append(SettingsTb.COLUMN_VIBRATE_GROUP).append(" INTEGER,")
		.append(SettingsTb.COLUMN_RECEIVE_WHEN_EXIT).append(" INTEGER,")
		.append(SettingsTb.COLUMN_FONT_SIZE).append(" INTEGER,")
		.append(SettingsTb.COLUMN_SKIN).append(" INTEGER,")
		.append(SettingsTb.COLUMN_ALWAYS_AUTO_RECEIVE_IMG).append(" INTEGER);");
		return sql.toString();
	}
	
	
	public String getCurrentAccount() {
		return mCurrentAccount;
	}

	public void setCurrentAccount(String currentAccount) {
		this.mCurrentAccount = currentAccount;
	}
}
