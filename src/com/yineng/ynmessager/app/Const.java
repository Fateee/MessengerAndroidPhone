package com.yineng.ynmessager.app;

import android.os.Environment;

/**
 * @author 贺毅柳
 *
 */
public interface Const
{
	public static final boolean DEBUG = true; // true
	public static final int SERVER_PORT = 5222;
	public static final String SERVICENAME = "messenger.yineng.com.cn";
	// public static final String SERVICENAME="m.com";
	// public static final String SERVICENAME="v8.ynedut.com";
	public static final String RESOURSE_NAME = "Msg_Phone";
	public static final String DES_KEY = "learning";
	public static final int REQUEST_CODE = 0;
	public static final int RESULT_CODE = 1;
	public static final int CHAT_TYPE_P2P = 1;// 两人会话
	public static final int CHAT_TYPE_GROUP = 2;// 群会话
	public static final int CHAT_TYPE_DIS = 3;// 讨论组会话
	public static final int CHAT_TYPE_BROADCAST = 4;// 广播
	public static final int CHAT_TYPE_NOTICE = 7;// 通知
	public static final int CHAT_TYPE_MOVE = 11;// 抖动
	public static final int CHAT_TYPE_FILE = 12;// 文件
	public static final int CHAT_TYPE_AUTO_SEND = 13;// 自动回复
	public static final String BROADCAST_NAME = "广播";// 消息列表显示的名称
	public static final String BROADCAST_ID = "com:yineng:broadcast";// 消息列表广播存放的uerno

	public static final int ORG_UPDATE_ALL = 0;// 全量更新
	public static final int ORG_UPDATE_SOME = 1;// 增量更新
	public static final int GET_OFFLINE_MSG = 2;// 离线消息
	public static final int CONTACT_GROUP_TYPE = 8;// 联系人-群组
	public static final int CONTACT_DISGROUP_TYPE = 9;// 联系人-讨论组
	public static final int GROUP_CREATER_TYPE = 10;// 群组、讨论组-创建人
	public static final int GROUP_MANAGER_TYPE = 20;// 群组、讨论组-管理员
	public static final int GROUP_USER_TYPE = 50;// 群组、讨论组-一般用户
	public static final int GET_OFFLINE_MSG_NUM = 0;// 获取离线消息条数

	/**
	 * 广播-更新群/讨论组信息的action
	 */
	public final String BROADCAST_ACTION_UPDATE_GROUP = "ynmsg.group.update";// 更新群/讨论组信息

	/**
	 * 广播-创建讨论组的action
	 */
	public final String BROADCAST_ACTION_CREATE_GROUP = "ynmsg.group.create";// 创建讨论组

	/**
	 * 广播-退出某讨论组的action
	 */
	public final String BROADCAST_ACTION_QUIT_GROUP = "ynmsg.group.quit";// 退出讨论组

	/**
	 * 广播-登录用户退出某讨论组的action
	 */
	public final String BROADCAST_ACTION_I_QUIT_GROUP = "ynmsg.group.I.quit";// 我退出讨论组

	/**
	 * 广播 - 清空会话列表
	 */
	public final String BROADCAST_ACTION_CLEAR_SESSION_LIST = "ynmsg.setting.clearSessionList";
	/**
	 * 广播 - 清除所有聊天记录
	 */
	public final String BROADCAST_ACTION_CLEAR_ALL_CHAT_MSG = "ynmsg.setting.clearAllChatMsg";
	/**
	 * 广播 - 清除缓存
	 */
	public final String BROADCAST_ACTION_CLEAR_CACHE = "ynmsg.setting.clearCache";
	
	/**
	 * 用户退出登陆
	 */
	public final String BROADCAST_ACTION_USER_LOGOUT= "ynmsg.user.logout";
	
	/**
	 * 服务器接口-重命名群/讨论组的action
	 */
	public final int INTERFACE_ACTION_GROUP_RENAME = 2;// 修改群/讨论组名称

	/** 添加人员到群组、讨论组 **/
	public final String GROUP_ADD_USER = "group_add_user";

	/** 联系人进入群或讨论组时传一个群/讨论组列表的Extra的key **/
	public final String INTENT_GROUP_LIST_EXTRA_NAME = "contactGroupList";

	/** 单人会话Intent传一个用户对象的Extra的key **/
	public final String INTENT_USER_EXTRA_NAME = "userObject";

	/** 群组Intent传一个群对象的Extra的key **/
	public final String INTENT_GROUP_EXTRA_NAME = "groupObject";

	/** 群组Intent传一个群ID的Extra的key **/
	public final String INTENT_GROUPID_EXTRA_NAME = "discussion_group_id";

	/** 群组Intent传一个群/讨论组类型的Extra的key **/
	public final String INTENT_GROUPTYPE_EXTRA_NAME = "GROUP_TYPE";

	/**
	 * 客户端初始化命名空间
	 */
	public static final String REQ_IQ_XMLNS_CLIENT_INIT = "com:yineng:clientinit";

	/**
	 * 获取组织机构的命名空间
	 */
	public static final String REQ_IQ_XMLNS_GET_ORG = "com:yineng:orgget";

	/**
	 * 获取用户状态的命名空间
	 */
	public final String REQ_IQ_XMLNS_GET_STATUS = "com:yineng:status";

	/**
	 * 获取群、讨论组的命名空间
	 */
	public static final String REQ_IQ_XMLNS_GET_GROUP = "com:yineng:group";

	/**
	 * 获取某个用户的详细信息的命名空间
	 */
	public static final String REQ_IQ_XMLNS_GET_PERSON_DETAIL = "com:yineng:querydetail";

	/**
	 * 获取某个用户的离线消息
	 */
	public static final String REQ_IQ_XMLNS_GET_OFFLINE_MSG = "com:yineng:offline";

	/**
	 * 存储到Preference中，标识是否是第一次启动程序
	 */
	public static final String IS_FIRST_LAUNCH = "isFirstLaunch";

	/**
	 * 用户下线
	 */
	public static final int USER_OFF_LINE = 0;

	/**
	 * 用户上线
	 */
	public static final int USER_ON_LINE = 1;

	/**
	 * 用户状态上下线状态改变
	 */
	public static final int USER_STATUS_CHANGED = 2;

	/**
	 * 常规文件保存地址
	 */
	public static final String COMMON_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
			+ "/YNMessenger/commonFile";// 其他临时图片保存途径
	/**
	 * 源图片保存地址
	 */
	public static final String IMAGE_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
			+ "/YNMessenger/imageFile";// 其他临时图片保存途径

	/**
	 * 缩略图存放位置
	 */
	public static final String THUMB_IMAGE_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
			+ "/YNMessenger/thumbImageFile";// 缩略图片存放地址

	/**
	 * 音频文件存储地址
	 */
	public static final String VOICE_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
			+ "/YNMessenger/voiceFile";// 缩略图片存放地址

	/**
	 * 视频文件存储地址
	 */
	public static final String VIDEO_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
			+ "/YNMessenger/videoFile";// 缩略图片存放地址

	/**
	 * office 文件存储地址
	 */
	public static final String office_FILE_PATH = Environment.getExternalStorageDirectory().getPath()
			+ "/YNMessenger/officeFile";// 缩略图片存放地址

	// actions
	/**
	 * 更新主界面未读消息条数的广播
	 */
	public static final String ACTION_UPDATE_UNREAD_COUNT = "UPDATE_UNREAD_COUNT_UI";

	/******************* IQ请求返回码 *******************/

	/**
	 * 处理成功
	 */
	public final int IQ_RESPONSE_CODE_SUCCESS = 200;

}
