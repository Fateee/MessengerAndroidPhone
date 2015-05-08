package com.yineng.ynmessager.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.yineng.ynmessager.bean.ClientInitConfig;
import com.yineng.ynmessager.bean.contact.ContactGroup;
import com.yineng.ynmessager.bean.contact.ContactGroupUser;
import com.yineng.ynmessager.bean.contact.OrganizationTree;
import com.yineng.ynmessager.bean.contact.SelfUser;
import com.yineng.ynmessager.bean.contact.User;
import com.yineng.ynmessager.bean.contact.UserStatus;
import com.yineng.ynmessager.bean.contact.UserTreeRelation;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;

/**
 * 联系人数据库dao,包含组织机构、群、讨论组的获取；用户上下线状态的更改；搜索联系人接口
 * 
 * @author huyi
 * 
 */
public class ContactOrgDao
{
	private SQLiteDatabase mDB;
	private String CLIENT_INIT_TABLE = "ClientInit";
	private String ORG_TREE_TABLE = "OrganizationTree";
	private String USER_TABLE = "User";
	private String USER_TREE_RELATION_TABLE = "UserTreeRelation";
	private String USER_STATUS_TABLE = "UserStatus";
	private String CONTACT_GROUP = "UserGroup";
	private String CONTACT_GROUP_USER_REL = "UserGroupRelations";
	private String CONTACT_DIS_GROUP = "DiscussGroup";
	private String CONTACT_DIS_GROUP_USER_REL = "UserDiscussionGroupRelations";

	public ContactOrgDao(Context mContext)
	{
		String userAccount = LastLoginUserSP.getInstance(mContext).getUserAccount();
		if(!userAccount.isEmpty())
		{
			mDB = (UserAccountDB.getInstance(mContext,userAccount)).getWritableDatabase();
		}
	}

	public ContactOrgDao(Context mContext, String Account)
	{
		String userAccount = LastLoginUserSP.getInstance(mContext).getUserAccount();
		if(!userAccount.isEmpty())
		{
			mDB = (UserAccountDB.getInstance(mContext,userAccount)).getWritableDatabase();
		}else
		{
			mDB = (UserAccountDB.getInstance(mContext,Account)).getWritableDatabase();
		}
	}

	// TODO Auto-generated method stub
	/********************************************* 初始化信息 *********************************************************/

	/**
	 * 保存客户端初始化信息
	 * 
	 * @param tempClientInitConfig
	 */
	public synchronized void saveClientInitInfo(ClientInitConfig tempClientInitConfig)
	{
		ContentValues clientInitValues = new ContentValues();
		clientInitValues.put("disgroup_max_user",tempClientInitConfig.getDisgroup_max_user());
		clientInitValues.put("group_max_user",tempClientInitConfig.getGroup_max_user());
		clientInitValues.put("max_disdisgroup_can_create",tempClientInitConfig.getMax_disdisgroup_can_create());
		clientInitValues.put("max_group_can_create",tempClientInitConfig.getMax_group_can_create());
		clientInitValues.put("org_update_type",tempClientInitConfig.getOrg_update_type());
		ClientInitConfig mClientInitConfig = getClientInitInfo();
		if(mClientInitConfig != null)
		{
			mDB.update(CLIENT_INIT_TABLE,clientInitValues,null,null);
		}else
		{
			mDB.insert(CLIENT_INIT_TABLE,null,clientInitValues);
		}
	}

	/**
	 * 获取初始化信息
	 * 
	 * @return
	 */
	public ClientInitConfig getClientInitInfo()
	{
		ClientInitConfig mClientInitConfig = null;
		Cursor mCursor = mDB.query(CLIENT_INIT_TABLE,null,null,null,null,null,null);
		if(mCursor != null)
		{
			if(mCursor.getCount() == 1)
			{
				mCursor.moveToNext();
				String disgroup_max_user = mCursor.getString(mCursor.getColumnIndex("disgroup_max_user"));
				String group_max_user = mCursor.getString(mCursor.getColumnIndex("group_max_user"));
				String max_disdisgroup_can_create = mCursor.getString(mCursor
						.getColumnIndex("max_disdisgroup_can_create"));
				String max_group_can_create = mCursor.getString(mCursor.getColumnIndex("max_group_can_create"));
				String org_update_type = mCursor.getString(mCursor.getColumnIndex("org_update_type"));
				String serverTime = mCursor.getString(mCursor.getColumnIndex("servertime"));
				mClientInitConfig = new ClientInitConfig(disgroup_max_user,group_max_user,max_disdisgroup_can_create,
						max_group_can_create,org_update_type);
				mClientInitConfig.setServertime(serverTime);
			}else
			{
				mDB.delete(CLIENT_INIT_TABLE,null,null);
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mClientInitConfig;
	}

	/**
	 * 保存初始化服务器时间
	 * 
	 * @param mServerTime
	 */
	public synchronized void saveInitServerTime(String mServerTime)
	{
		ContentValues clientInitValues = new ContentValues();
		clientInitValues.put("servertime",mServerTime);
		ClientInitConfig mClientInitConfig = getClientInitInfo();
		if(mClientInitConfig != null)
		{
			mDB.update(CLIENT_INIT_TABLE,clientInitValues,null,null);
		}
	}

	/**
	 * 得到初始化服务器时间
	 * 
	 * @return
	 */
	public synchronized String getInitServerTime()
	{
		String serverTime = "0";
		Cursor mCursor = mDB.query(CLIENT_INIT_TABLE,null,null,null,null,null,null);
		if(mCursor != null && mCursor.getCount() == 1)
		{
			mCursor.moveToNext();
			serverTime = mCursor.getString(mCursor.getColumnIndex("servertime"));
		}
		if(serverTime == null)
		{
			serverTime = "0";
		}
		return serverTime;
	}

	// TODO Auto-generated method stub
	/********************************************* 保存所有组织机构树 *********************************************************/

	/**
	 * 保存所有组织机构树
	 * 
	 * @param mListObject
	 */
	public synchronized void saveAllOrgData(List<OrganizationTree> mList)
	{
		mDB.beginTransaction();
		for(OrganizationTree organizationTree : mList)
		{
			insertUpdateOneOrgData(organizationTree);
		}
		mDB.setTransactionSuccessful();
		mDB.endTransaction();
	}

	/**
	 * 插入或更新一条组织机构记录
	 * 
	 * @param organizationTree
	 *            组织机构
	 * @param isExist
	 *            该记录是否存在
	 */
	private void insertUpdateOneOrgData(OrganizationTree organizationTree)
	{
		boolean isOrgExist = isOrgExist(organizationTree.getOrgNo());
		ContentValues orgTreeContentValues = new ContentValues();
		orgTreeContentValues.put("orgNo",organizationTree.getOrgNo());
		orgTreeContentValues.put("parentOrgNo",organizationTree.getParentOrgNo());
		orgTreeContentValues.put("orgName",organizationTree.getOrgName());
		orgTreeContentValues.put("orgType",organizationTree.getOrgType());
		orgTreeContentValues.put("ordId",organizationTree.getOrdId());
		orgTreeContentValues.put("removeTag",organizationTree.getRemoveTag());
		if(isOrgExist)
		{
			mDB.update(ORG_TREE_TABLE,orgTreeContentValues,"orgNo = ?",new String[] {organizationTree.getOrgNo()});
		}else
		{
			mDB.insert(ORG_TREE_TABLE,null,orgTreeContentValues);
		}
	}

	/**
	 * 组织机构是否存在
	 * 
	 * @param orgId
	 *            组织机构id
	 * @return true 存在 fasle 不存在
	 */
	public boolean isOrgExist(String orgId)
	{
		Cursor mCursor = mDB.query(ORG_TREE_TABLE,null,"orgNo = ?",new String[] {orgId},null,null,null);
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mCursor.close();
			return true;
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return false;
	}

	/**
	 * 根据父id获取子组织机构信息
	 * 
	 * @param parentOrgId
	 *            父节点
	 * @return 结果按照ordId降序排列
	 */
	public List<OrganizationTree> queryOrgListByParentId(String parentOrgId)
	{
		List<OrganizationTree> mList = null;
		Cursor mCursor = mDB.query(ORG_TREE_TABLE,null,"parentOrgNo = ?",new String[] {parentOrgId},null,null,
				"ordId desc");
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mList = new ArrayList<OrganizationTree>();
			while(mCursor.moveToNext())
			{
				String orgNo = mCursor.getString(mCursor.getColumnIndex("orgNo"));
				String parentOrgNo = mCursor.getString(mCursor.getColumnIndex("parentOrgNo"));
				String orgName = mCursor.getString(mCursor.getColumnIndex("orgName"));
				int orgType = mCursor.getInt(mCursor.getColumnIndex("orgType"));
				// int ordId = mCursor.getInt(mCursor.getColumnIndex("ordId"));
				String ordId = mCursor.getString(mCursor.getColumnIndex("ordId"));
				int removeTag = mCursor.getInt(mCursor.getColumnIndex("removeTag"));
				OrganizationTree tempOrgTree = new OrganizationTree(orgNo,parentOrgNo,orgName,orgType,ordId,removeTag);
				mList.add(tempOrgTree);
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mList;
	}

	/**
	 * 递归方式保存组织机构到对象
	 * 
	 * @param mOrganizationTree
	 *            某组织机构
	 * @param parentOrgId
	 *            父ID
	 */
	// public void queryAllOrgListByParentId(OrganizationTree mOrganizationTree,
	// String parentOrgId) {
	// List<OrganizationTree> mList = queryOrgListByParentId(parentOrgId);
	// List<User> mUsers = queryUsersByOrgNo(parentOrgId);
	// mOrganizationTree.getChildOrgTreeMap().put(parentOrgId, mList);//
	// 以父id为key，存入组织机构list
	// if (mUsers != null) {
	// mOrganizationTree.getmOrgUsers().addAll(mUsers);// 保存该组织机构下的用户
	// }
	// if (mList != null && mList.size() != 0) {
	// for (OrganizationTree organizationTree : mList) {
	// queryAllOrgListByParentId(organizationTree,
	// organizationTree.getOrgNo());
	// }
	// }
	// }

	/**
	 * 通过递归变量来统计某组织机构的总人数
	 * 
	 * @param mOrganizationTree
	 *            某组织机构
	 * @param count
	 *            人数
	 * @return 总人数
	 */
	// public int getOrgUsersCountByOrgId(OrganizationTree mOrganizationTree,
	// int count) {
	// count = count + mOrganizationTree.getmOrgUsers().size();
	// List<OrganizationTree> mOrgList = mOrganizationTree
	// .getChildOrgTreeMap().get(mOrganizationTree.getOrgNo());
	// if (mOrgList != null && mOrgList.size() != 0) {
	// for (OrganizationTree organizationTree : mOrgList) {
	// count = getOrgUsersCountByOrgId(organizationTree, count);
	// }
	// }
	// return count;
	// }

	/**
	 * 通过递归查数据库来统计某组织机构的总人数
	 * 
	 * @param mOrganizationTree
	 *            某组织机构
	 * @param count
	 *            人数
	 * @return 总人数
	 */
	public int getOrgUsersCountByOrgIdFromDb(OrganizationTree mOrganizationTree, int count)
	{
		List<User> mUsers = queryUsersByOrgNo(mOrganizationTree.getOrgNo());
		if(mUsers != null)
		{
			count = count + mUsers.size();
		}
		List<OrganizationTree> mList = queryOrgListByParentId(mOrganizationTree.getOrgNo());
		if(mList != null && mList.size() != 0)
		{
			for(OrganizationTree organizationTree : mList)
			{
				count = getOrgUsersCountByOrgIdFromDb(organizationTree,count);
			}
		}
		return count;
	}

	/**
	 * 根据某机构id，找到它所属的机构列表
	 * 
	 * @param mMyOrgPath
	 * @param myOrganizationTree
	 * @return
	 * @return
	 */
	public ArrayList<OrganizationTree> queryOrgBelongListByOrgNo(OrganizationTree mOrganizationTree,
			ArrayList<OrganizationTree> mMyOrgPathList)
	{
		OrganizationTree tempOrganizationTree = null;
		if(mDB != null && mOrganizationTree != null)
		{
			Cursor mCursor = mDB.query(ORG_TREE_TABLE,null,"orgNo = ?",
					new String[] {mOrganizationTree.getParentOrgNo()},null,null,null);
			if(mCursor != null && mCursor.getCount() > 0)
			{
				while(mCursor.moveToNext())
				{
					String orgNo = mCursor.getString(mCursor.getColumnIndex("orgNo"));
					String parentOrgNo = mCursor.getString(mCursor.getColumnIndex("parentOrgNo"));
					String orgName = mCursor.getString(mCursor.getColumnIndex("orgName"));
					int orgType = mCursor.getInt(mCursor.getColumnIndex("orgType"));
					// int ordId =
					// mCursor.getInt(mCursor.getColumnIndex("ordId"));
					String ordId = mCursor.getString(mCursor.getColumnIndex("ordId"));
					int removeTag = mCursor.getInt(mCursor.getColumnIndex("removeTag"));
					tempOrganizationTree = new OrganizationTree(orgNo,parentOrgNo,orgName,orgType,ordId,removeTag);
					mMyOrgPathList.add(tempOrganizationTree);
					queryOrgBelongListByOrgNo(tempOrganizationTree,mMyOrgPathList);
				}
			}
			if(mCursor != null)
			{
				mCursor.close();
			}
		}
		return mMyOrgPathList;
	}

	/**
	 * 获取我所在的组织机构信息
	 * 
	 * @param mContext
	 * @return
	 */
	public OrganizationTree queryMyOrg(Context mContext)
	{
		String userAccount = LastLoginUserSP.getInstance(mContext).getUserAccount();
		OrganizationTree myOrganizationTree = queryUserRelationByUserNo(userAccount);
		return myOrganizationTree;
	}

	/**
	 * 根据用户id找到他所属的组织机构(跳转到我的组织机构)
	 * 
	 * @param mUserNo
	 * @return
	 */
	public OrganizationTree queryUserRelationByUserNo(String mUserNo)
	{
		OrganizationTree mOrganizationTree = null;
		Cursor mCursor = mDB.rawQuery(
				"SELECT * FROM OrganizationTree WHERE orgNo = (SELECT orgNo FROM UserTreeRelation WHERE userNo = ?)",
				new String[] {mUserNo});
		if(mCursor != null && mCursor.getCount() > 0)
		{
			while(mCursor.moveToNext())
			{
				String orgNo = mCursor.getString(mCursor.getColumnIndex("orgNo"));
				String parentOrgNo = mCursor.getString(mCursor.getColumnIndex("parentOrgNo"));
				String orgName = mCursor.getString(mCursor.getColumnIndex("orgName"));
				int orgType = mCursor.getInt(mCursor.getColumnIndex("orgType"));
				// int ordId = mCursor.getInt(mCursor.getColumnIndex("ordId"));
				String ordId = mCursor.getString(mCursor.getColumnIndex("ordId"));
				int removeTag = mCursor.getInt(mCursor.getColumnIndex("removeTag"));
				mOrganizationTree = new OrganizationTree(orgNo,parentOrgNo,orgName,orgType,ordId,removeTag);
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mOrganizationTree;
	}

	// TODO Auto-generated method stub
	/******************************************** 用户 **********************************************************/

	/**
	 * 保存所有用户
	 * 
	 * @param mListObject
	 */
	public synchronized void saveAllUserData(List<User> mList)
	{
		mDB.beginTransaction();
		for(User mUser : mList)
		{
			insertUpdateOneUserData(mUser);
		}
		mDB.setTransactionSuccessful();
		mDB.endTransaction();
	}

	/**
	 * 插入或更新一个用户信息
	 * 
	 * @param mUser
	 *            用户信息
	 */
	public void insertUpdateOneUserData(User mUser)
	{
		boolean isUserExist = isUserExist(mUser.getUserNo());
		ContentValues userContentValues = new ContentValues();
		userContentValues.put("userNo",mUser.getUserNo());
		userContentValues.put("userName",mUser.getUserName());
		userContentValues.put("gender",mUser.getGender());
		userContentValues.put("dayOfBirth",mUser.getDayOfBirth());
		userContentValues.put("telephone",mUser.getTelephone());
		userContentValues.put("email",mUser.getEmail());
		userContentValues.put("post",mUser.getPost());
		userContentValues.put("headUrl",mUser.getHeadUrl());
		userContentValues.put("sigature",mUser.getSigature());
		userContentValues.put("userType",mUser.getUserType());
		userContentValues.put("removeTag",mUser.getRemoveTag());
		userContentValues.put("userStatus",mUser.getUserStatus());
		if(isUserExist)
		{
			mDB.update(USER_TABLE,userContentValues,"userNo = ?",new String[] {mUser.getUserNo()});
		}else
		{
			mDB.insert(USER_TABLE,null,userContentValues);
		}
	}

	/**
	 * 用户是否存在
	 * 
	 * @param userNo
	 * @return
	 */
	public boolean isUserExist(String userNo)
	{
		Cursor mCursor = mDB.query(USER_TABLE,null,"userNo = ?",new String[] {userNo},null,null,null);
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mCursor.close();
			return true;
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return false;
	}

	/**
	 * 获取所有用户
	 * 
	 * @return
	 */
	public List<User> queryAllUser()
	{
		List<User> mList = null;
		Cursor mCursor = mDB.query(USER_TABLE,null,null,null,null,null,null);
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mList = new ArrayList<User>();
			while(mCursor.moveToNext())
			{
				String userNo = mCursor.getString(mCursor.getColumnIndex("userNo"));
				String userName = mCursor.getString(mCursor.getColumnIndex("userName"));
				int gender = mCursor.getInt(mCursor.getColumnIndex("gender"));
				String dayOfBirth = mCursor.getString(mCursor.getColumnIndex("dayOfBirth"));
				String telephone = mCursor.getString(mCursor.getColumnIndex("telephone"));
				String email = mCursor.getString(mCursor.getColumnIndex("email"));
				String post = mCursor.getString(mCursor.getColumnIndex("post"));
				String headUrl = mCursor.getString(mCursor.getColumnIndex("headUrl"));
				String sigature = mCursor.getString(mCursor.getColumnIndex("sigature"));
				int userType = mCursor.getInt(mCursor.getColumnIndex("userType"));
				int removeTag = mCursor.getInt(mCursor.getColumnIndex("removeTag"));
				int userStatus = mCursor.getInt(mCursor.getColumnIndex("userStatus"));
				User tempUser = new User(userNo,userName,gender,dayOfBirth,telephone,email,post,headUrl,sigature,
						userType,removeTag,userStatus);
				mList.add(tempUser);
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mList;
	}

	/**
	 * 根据用户ID得到用户信息
	 * 
	 * @param mUserNo
	 * @return
	 */
	public SelfUser queryUserInfoByUserNo(String mUserNo)
	{
		SelfUser tempUser = null;
		Cursor mCursor = mDB.query(USER_TABLE,null,"userNo = ?",new String[] {mUserNo},null,null,null);
		if(mCursor != null && mCursor.getCount() > 0)
		{
			while(mCursor.moveToNext())
			{
				String userNo = mCursor.getString(mCursor.getColumnIndex("userNo"));
				String userName = mCursor.getString(mCursor.getColumnIndex("userName"));
				int gender = mCursor.getInt(mCursor.getColumnIndex("gender"));
				String dayOfBirth = mCursor.getString(mCursor.getColumnIndex("dayOfBirth"));
				String telephone = mCursor.getString(mCursor.getColumnIndex("telephone"));
				String email = mCursor.getString(mCursor.getColumnIndex("email"));
				String post = mCursor.getString(mCursor.getColumnIndex("post"));
				String headUrl = mCursor.getString(mCursor.getColumnIndex("headUrl"));
				String sigature = mCursor.getString(mCursor.getColumnIndex("sigature"));
				int userType = mCursor.getInt(mCursor.getColumnIndex("userType"));
				int removeTag = mCursor.getInt(mCursor.getColumnIndex("removeTag"));
				int userStatus = mCursor.getInt(mCursor.getColumnIndex("userStatus"));
				tempUser = new SelfUser(userNo,userName,gender,dayOfBirth,telephone,email,post,headUrl,sigature,
						userType,removeTag,userStatus);
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return tempUser;
	}

	/**
	 * 应用初始化时调用，根据状态实例更新用户在线状态
	 * 
	 * @param mUserStatus
	 *            对象状态
	 */
	public synchronized void updateOneUserStatus(UserStatus mUserStatus)
	{
		if(isUserExist(mUserStatus.getUserNo()))
		{
			ContentValues userContentValues = new ContentValues();
			userContentValues.put("userStatus",1);
			mDB.update(USER_TABLE,userContentValues,"userNo = ?",new String[] {mUserStatus.getUserNo()});
		}
	}

	/**
	 * 用户上线或更新状态时，根据用户对象来更新用户在线状态
	 * 
	 * @param mUser用户对象
	 */
	public synchronized void updateOneUserStatus(User mUser)
	{
		if(isUserExist(mUser.getUserNo()))
		{
			ContentValues userContentValues = new ContentValues();
			userContentValues.put("userStatus",mUser.getUserStatus());
			mDB.update(USER_TABLE,userContentValues,"userNo = ?",new String[] {mUser.getUserNo()});
		}
	}

	/**
	 * 用户上线或更新状态时，根据用户ID和 0、1 来更新用户在线状态
	 * 
	 * @param mUserNo
	 * @param onlineStatus
	 *            0: offline 1:online
	 */
	public synchronized void updateOneUserStatusByAble(String mUserNo, int onlineStatus)
	{
		if(isUserExist(mUserNo))
		{
			ContentValues userContentValues = new ContentValues();
			userContentValues.put("userStatus",onlineStatus);
			mDB.update(USER_TABLE,userContentValues,"userNo = ?",new String[] {mUserNo});
		}
	}

	// TODO Auto-generated method stub
	/********************************************* 用户与机构关系 *********************************************************/

	/**
	 * 用户与机构关系
	 * 
	 * @param mList
	 */
	public synchronized void saveAllUserRelationData(List<UserTreeRelation> mList)
	{
		mDB.beginTransaction();
		for(UserTreeRelation mUserRelation : mList)
		{
			insertUpdateOneUserRelationData(mUserRelation);
		}
		mDB.setTransactionSuccessful();
		mDB.endTransaction();
	}

	/**
	 * 插入或更新一个用户机构关系信息
	 * 
	 * @param mUserRelation
	 *            用户机构关系信息
	 */
	public void insertUpdateOneUserRelationData(UserTreeRelation mUserRelation)
	{
		boolean isUserExist = isUserRelationExist(mUserRelation.getUserNo(),mUserRelation.getOrgNo());
		ContentValues userContentValues = new ContentValues();
		userContentValues.put("userNo",mUserRelation.getUserNo());
		userContentValues.put("orgNo",mUserRelation.getOrgNo());
		userContentValues.put("ordId",mUserRelation.getOrdId());
		userContentValues.put("relationType",mUserRelation.getRelationType());
		userContentValues.put("removeTag",mUserRelation.getRemoveTag());
		if(isUserExist)
		{
			mDB.update(USER_TREE_RELATION_TABLE,userContentValues,"userNo = ? and orgNo = ?",new String[] {
					mUserRelation.getUserNo(), mUserRelation.getOrgNo()});
		}else
		{
			mDB.insert(USER_TREE_RELATION_TABLE,null,userContentValues);
		}
	}

	/**
	 * 用户机构关系是否存在
	 * 
	 * @param userNo
	 * @param orgNo
	 * @return
	 */
	public boolean isUserRelationExist(String userNo, String orgNo)
	{
		Cursor mCursor = mDB.query(USER_TREE_RELATION_TABLE,null,"userNo = ? and orgNo = ?",
				new String[] {userNo, orgNo},null,null,null);
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mCursor.close();
			return true;
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return false;
	}

	/**
	 * 根据组织机构ID获取组织机构下的用户
	 * 
	 * @param orgNo
	 *            组织机构id
	 * @return 组织机构下的人
	 */
	public List<User> queryUsersByOrgNo(String orgNo)
	{
		List<User> mUsers = null;
		Cursor mCursor = mDB.rawQuery("SELECT u.* FROM " + USER_TREE_RELATION_TABLE + " m," + USER_TABLE
				+ " u WHERE m.userNo = u.userNo AND m.orgNo = '" + orgNo + "'" + " order by userStatus desc",null);
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mUsers = new ArrayList<User>();
			while(mCursor.moveToNext())
			{
				String mUserNo = mCursor.getString(mCursor.getColumnIndex("userNo"));
				String mUserName = mCursor.getString(mCursor.getColumnIndex("userName"));
				int mGender = mCursor.getInt(mCursor.getColumnIndex("gender"));
				String mDayOfBirth = mCursor.getString(mCursor.getColumnIndex("dayOfBirth"));
				String mTelephone = mCursor.getString(mCursor.getColumnIndex("telephone"));
				String mEmail = mCursor.getString(mCursor.getColumnIndex("email"));
				String mPost = mCursor.getString(mCursor.getColumnIndex("post"));
				String mHeadUrl = mCursor.getString(mCursor.getColumnIndex("headUrl"));
				String mSigature = mCursor.getString(mCursor.getColumnIndex("sigature"));
				int mUserType = mCursor.getInt(mCursor.getColumnIndex("userType"));
				int mRemoveTag = mCursor.getInt(mCursor.getColumnIndex("removeTag"));
				int userStatus = mCursor.getInt(mCursor.getColumnIndex("userStatus"));
				mUsers.add(new User(mUserNo,mUserName,mGender,mDayOfBirth,mTelephone,mEmail,mPost,mHeadUrl,mSigature,
						mUserType,mRemoveTag,userStatus));
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mUsers;
	}

	// TODO Auto-generated method stub
	/********************************************* 用户状态 *********************************************************/

	/**
	 * 保存所有用户在线信息
	 * 
	 * @param mList
	 */
	public synchronized void saveAllUserStatusData(List<UserStatus> mList)
	{
		clearAllUserStatus();
		for(UserStatus mUserStatus : mList)
		{
			// insertUpdateOneUserStatusData(mUserStatus);
			updateOneUserStatus(mUserStatus);
		}
	}

	/**
	 * 用户上线或更新状态时，插入或更新一个用户在线信息
	 * 
	 * @param mUserStatus
	 *            用户在线信息
	 */
	public void insertUpdateOneUserStatusData(UserStatus mUserStatus)
	{
		boolean isUserStatusExist = isUserStatusExist(mUserStatus.getUserNo());
		ContentValues userContentValues = new ContentValues();
		userContentValues.put("userNo",mUserStatus.getUserNo());
		userContentValues.put("status",mUserStatus.getStatus());
		userContentValues.put("statusID",mUserStatus.getStatusID());
		if(isUserStatusExist)
		{
			mDB.update(USER_STATUS_TABLE,userContentValues,"userNo = ?",new String[] {mUserStatus.getUserNo()});
		}else
		{
			mDB.insert(USER_STATUS_TABLE,null,userContentValues);
		}
	}

	/**
	 * 用户在线信息是否存在
	 * 
	 * @param userNo
	 * @return
	 */
	public boolean isUserStatusExist(String userNo)
	{
		Cursor mCursor = mDB.query(USER_STATUS_TABLE,null,"userNo = ?",new String[] {userNo},null,null,null);
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mCursor.close();
			return true;
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return false;
	}

	/**
	 * 用户离线后，删除该用户状态的记录
	 * 
	 * @param userNo
	 */
	public synchronized void removeUserOnlineStatus(String userNo)
	{
		if(isUserStatusExist(userNo))
		{
			mDB.delete(USER_STATUS_TABLE,"userNo = ?",new String[] {userNo});
		}
	}

	/**
	 * 清空用户状态表
	 */
	public void clearAllUserStatus()
	{
		mDB.delete(USER_STATUS_TABLE,null,null);
	}

	/**
	 * 获取所有在线用户
	 */
	public List<UserStatus> queryUserStatus()
	{
		List<UserStatus> mUsersStatus = null;
		Cursor mCursor = mDB.query(USER_STATUS_TABLE,null,null,null,null,null,null);
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mUsersStatus = new ArrayList<UserStatus>();
			while(mCursor.moveToNext())
			{
				String mUserNo = mCursor.getString(mCursor.getColumnIndex("userNo"));
				String mStatus = mCursor.getString(mCursor.getColumnIndex("status"));
				int mStatusID = mCursor.getInt(mCursor.getColumnIndex("statusID"));
				mUsersStatus.add(new UserStatus(mUserNo,mStatus,mStatusID));
			}
			mCursor.close();
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mUsersStatus;
	}

	// TODO Auto-generated method stub
	/********************************************* 群组 *********************************************************/

	/**
	 * 保存所有群组/讨论组信息
	 * 
	 * @param mList
	 *            群/讨论组数据
	 * @param groupType
	 *            8：群组 9：讨论组
	 */
	public synchronized void saveAllContactGroupData(List<ContactGroup> mList, int groupType)
	{
		removeAllContactGroupData(groupType);
		mDB.beginTransaction();
		for(ContactGroup contactGroup : mList)
		{
			insertOneContactGroupData(contactGroup,groupType);
			// insertUpdateOneContactGroupData(contactGroup,groupType);
		}
		mDB.setTransactionSuccessful();
		mDB.endTransaction();
	}

	/**
	 * 删除所有群组/讨论组信息
	 * 
	 * @param groupType
	 *            8：群组 9：讨论组
	 */
	private synchronized void removeAllContactGroupData(int groupType)
	{
		if(groupType == 8)
		{
			mDB.delete(CONTACT_GROUP,null,null);
		}else
		{
			mDB.delete(CONTACT_DIS_GROUP,null,null);
		}
	}

	/**
	 * 插入或更新一条群组/讨论组记录
	 * 
	 * @param groupType
	 *            8：群组 9：讨论组
	 * @param gontactGroup
	 *            群/讨论组数据
	 */
	public void insertOneContactGroupData(ContactGroup contactGroup, int groupType)
	{
		int existType = isContactGroupExist(contactGroup.getGroupName(),groupType);
		ContentValues contactGroupCV = new ContentValues();
		contactGroupCV.put("groupName",contactGroup.getGroupName());
		contactGroupCV.put("createUser",contactGroup.getCreateUser());
		contactGroupCV.put("createTime",contactGroup.getCreateTime());
		contactGroupCV.put("naturalName",contactGroup.getNaturalName());
		contactGroupCV.put("subject",contactGroup.getSubject());
		contactGroupCV.put("maxUsers",contactGroup.getMaxUsers());
		contactGroupCV.put("desc",contactGroup.getDesc());
		// if (groupType == 8) {
		// mDB.insert(CONTACT_GROUP, null, contactGroupCV);
		// } else {
		// mDB.insert(CONTACT_DIS_GROUP, null, contactGroupCV);
		// }
		switch(existType)
		{
			case 0:
				mDB.insert(CONTACT_GROUP,null,contactGroupCV);
				break;
			case 1:
				mDB.update(CONTACT_GROUP,contactGroupCV,"groupName = ?",new String[] {contactGroup.getGroupName()});
				break;
			case 2:
				mDB.insert(CONTACT_DIS_GROUP,null,contactGroupCV);
				break;
			case 3:
				mDB.update(CONTACT_DIS_GROUP,contactGroupCV,"groupName = ?",new String[] {contactGroup.getGroupName()});
				break;
			default:
				break;
		}
	}

	/**
	 * 群组/讨论组是否存在
	 * 
	 * @param groupType
	 *            8：群组 9：讨论组
	 * @param groupId
	 *            群组/讨论组id
	 * @return 0:群组不存在 1：群组存在 2：讨论组不存在 3 讨论组存在
	 */
	public int isContactGroupExist(String groupId, int groupType)
	{
		Cursor mCursor;
		int ret = -1;
		if(groupType == 8)
		{// 群组
			mCursor = mDB.query(CONTACT_GROUP,null,"groupName = ?",new String[] {groupId},null,null,null);
			if(mCursor != null && mCursor.getCount() > 0)
			{
				ret = 1;
			}else
			{
				ret = 0;
			}
		}else
		{// 讨论组
			mCursor = mDB.query(CONTACT_DIS_GROUP,null,"groupName = ?",new String[] {groupId},null,null,null);
			if(mCursor != null && mCursor.getCount() > 0)
			{
				ret = 3;
			}else
			{
				ret = 2;
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return ret;
	}

	/**
	 * 获取用户的群组/讨论组列表信息
	 * 
	 * @param groupType
	 *            8：群组 9：讨论组
	 * @return 结果按照ordId降序排列
	 */
	public List<ContactGroup> queryGroupList(int groupType)
	{
		List<ContactGroup> mList = new ArrayList<ContactGroup>();
		Cursor mCursor;
		if(groupType == 8)
		{
			mCursor = mDB.query(CONTACT_GROUP,null,null,null,null,null,null);
		}else
		{
			mCursor = mDB.query(CONTACT_DIS_GROUP,null,null,null,null,null,null);
		}
		if(mCursor != null && mCursor.getCount() > 0)
		{
			while(mCursor.moveToNext())
			{
				String groupName = mCursor.getString(mCursor.getColumnIndex("groupName"));
				String createUser = mCursor.getString(mCursor.getColumnIndex("createUser"));
				String createTime = mCursor.getString(mCursor.getColumnIndex("createTime"));
				String naturalName = mCursor.getString(mCursor.getColumnIndex("naturalName"));
				String subject = mCursor.getString(mCursor.getColumnIndex("subject"));
				int maxUsers = mCursor.getInt(mCursor.getColumnIndex("maxUsers"));
				String desc = mCursor.getString(mCursor.getColumnIndex("desc"));
				int notifyMode = mCursor.getInt(mCursor.getColumnIndex("notifyMode"));
				ContactGroup tempContactGroup = new ContactGroup(groupName,createUser,createTime,naturalName,subject,
						maxUsers,desc,notifyMode);
				mList.add(tempContactGroup);
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mList;
	}

	/**
	 * 根据GroupName来查询一个指定的群讨论组
	 * @param groupName 要查询的群、讨论组的GroupName
	 * @return 查询到的结果；返回null代表未在表中查询到
	 */
	public ContactGroup queryGroupOrDiscussByGroupName(String groupName)
	{
		ContactGroup group = null;
		if(TextUtils.isEmpty(groupName))
		{
			return null;
		}
		String sql = "select * from (select * from " + CONTACT_GROUP + " union all select * from " + CONTACT_DIS_GROUP
				+ ") where groupName='" + groupName + "'";
		Cursor cur = mDB.rawQuery(sql,null);
		if(cur.moveToFirst())
		{
			String _groupName = cur.getString(cur.getColumnIndex("groupName"));
			String _createUser = cur.getString(cur.getColumnIndex("createUser"));
			String _createTime = cur.getString(cur.getColumnIndex("createTime"));
			String _naturalName = cur.getString(cur.getColumnIndex("naturalName"));
			String _subject = cur.getString(cur.getColumnIndex("subject"));
			int _maxUsers = cur.getInt(cur.getColumnIndex("maxUsers"));
			String _desc = cur.getString(cur.getColumnIndex("desc"));
			int _notifyMode = cur.getInt(cur.getColumnIndex("notifyMode"));
			group = new ContactGroup(_groupName,_createUser,_createTime,_naturalName,_subject,_maxUsers,_desc,
					_notifyMode);
		}
		cur.close();
		return group;

	}

	/**
	 * 联合查询群和讨论组表，并按照某一列来排序
	 * 
	 * @param orderBy
	 *            要用来排序的列名；为null或者是长度为0字符串，则默认为"groupName"
	 * @return 联合的查询结果集
	 */
	public List<ContactGroup> queryGroupAndDiscussList(String orderBy)
	{
		if(TextUtils.isEmpty(orderBy))
		{
			orderBy = "groupName";
		}
		List<ContactGroup> list = new ArrayList<ContactGroup>();
		String sql = "select * from (select * from " + CONTACT_GROUP + " union all select * from " + CONTACT_DIS_GROUP
				+ ") order by " + orderBy;
		Cursor cur = mDB.rawQuery(sql,null);
		for(cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext())
		{
			String groupName = cur.getString(cur.getColumnIndex("groupName"));
			String createUser = cur.getString(cur.getColumnIndex("createUser"));
			String createTime = cur.getString(cur.getColumnIndex("createTime"));
			String naturalName = cur.getString(cur.getColumnIndex("naturalName"));
			String subject = cur.getString(cur.getColumnIndex("subject"));
			int maxUsers = cur.getInt(cur.getColumnIndex("maxUsers"));
			String desc = cur.getString(cur.getColumnIndex("desc"));
			int notifyMode = cur.getInt(cur.getColumnIndex("notifyMode"));
			ContactGroup tempContactGroup = new ContactGroup(groupName,createUser,createTime,naturalName,subject,
					maxUsers,desc,notifyMode);
			list.add(tempContactGroup);
		}
		cur.close();
		return list;
	}

	/**
	 * 会根据ContactGroup.groupName来更新所在数据库表中对应的行的数据（自动识别并更新到其所在群或讨论组表）
	 * @param group 除了groupName、groupType属性，其他属性都将被更新到数据库对应列
	 * @return 更新操作所影响的行数
	 */
	public int updateGroupOrDiscuss(ContactGroup group)
	{
		int affected = 0;
		String groupName = group.getGroupName();
		ContentValues values = new ContentValues();
		values.put("createUser",group.getCreateUser());
		values.put("createTime",group.getCreateTime());
		values.put("naturalName",group.getNaturalName());
		values.put("subject",group.getSubject());
		values.put("maxUsers",group.getMaxUsers());
		values.put("desc",group.getDesc());
		values.put("notifyMode",group.getNotifyMode());
		String table = CONTACT_GROUP;
		Cursor cur = mDB.query(CONTACT_GROUP,new String[] {"groupName"},"groupName=?",new String[] {groupName},null,
				null,null);
		if(cur.getCount() > 0)
		{
			table = CONTACT_GROUP;
		}else
		{
			cur = mDB.query(CONTACT_DIS_GROUP,new String[] {"groupName"},"groupName=?",new String[] {groupName},null,
					null,null);
			if(cur.getCount() > 0)
			{
				table = CONTACT_DIS_GROUP;
			}
		}
		cur.close();
		affected = mDB.update(table,values,"groupName=?",new String[] {group.getGroupName()});
		return affected;
	}

	/**
	 * 更改群组、讨论组名称
	 * 
	 * @param mGroupId
	 *            群、讨论组id
	 * @param string
	 *            新名称
	 * @param contactGroupType
	 *            类型
	 */
	public void updateGroupSubject(String mGroupId, String string, int contactGroupType)
	{
		int ret = isContactGroupExist(mGroupId,contactGroupType);
		ContentValues contactGroupCV = new ContentValues();
		switch(ret)
		{
			case 1:
				contactGroupCV.put("naturalName",string);
				mDB.update(CONTACT_GROUP,contactGroupCV,"groupName = ?",new String[] {mGroupId});
				break;
			case 3:
				contactGroupCV.put("subject",string);
				mDB.update(CONTACT_DIS_GROUP,contactGroupCV,"groupName = ?",new String[] {mGroupId});
				break;
			default:
				break;
		}
	}

	/**
	 * 根据群组、讨论组id删除数据库该条记录
	 * 
	 * @param mGroupId
	 * @param contactGroupType
	 */
	public void deleteOneContactGroup(String mGroupId, int contactGroupType)
	{
		int ret = isContactGroupExist(mGroupId,contactGroupType);
		switch(ret)
		{
			case 1:// 群组存在
				mDB.delete(CONTACT_GROUP,"groupName = ?",new String[] {mGroupId});
				break;
			case 3:// 讨论组存在
				mDB.delete(CONTACT_DIS_GROUP,"groupName = ?",new String[] {mGroupId});
				break;
			default:
				break;
		}
	}

	/**
	 * 退出某个群\讨论组
	 * 
	 * @param mGroupId
	 * @param mUserNo
	 * @param contactGroupType
	 */
	public void quitContactGroup(String mGroupId, String mUserNo, int contactGroupType)
	{
		deleteOneContactGroup(mGroupId,contactGroupType);
		deleteOneGroupUserRelation(mGroupId,mUserNo,contactGroupType);
	}

	// TODO Auto-generated method stub
	/********************************************* 用户与群/讨论组关系 *********************************************************/

	/**
	 * 保存所有用户与群组/讨论组关系
	 * 
	 * @param mList
	 * @param groupType
	 */
	public synchronized void saveAllGroupUserRelationData(List<ContactGroupUser> mList, int groupType)
	{
		removeAllGroupUserRelationData(groupType);
		mDB.beginTransaction();
		for(ContactGroupUser mContactGroupUser : mList)
		{
			insertOneGroupUserRelationData(mContactGroupUser,groupType);
			// insertUpdateOneGroupUserRelationData(mContactGroupUser,groupType);
		}
		mDB.setTransactionSuccessful();
		mDB.endTransaction();
	}

	/**
	 * 根据群组/讨论组id找到群组、讨论组信息
	 * 
	 * @param id
	 *            群组/讨论组id
	 * @param type
	 *            8：群组 9：讨论组
	 * @return
	 */
	public ContactGroup getGroupBeanById(String id, int type)
	{
		ContactGroup bean = null;
		Cursor mCursor = null;
		String[] args = {id};
		switch(type)
		{
			case 8:
				mCursor = mDB.query(CONTACT_GROUP,null,"groupName = ?",args,null,null,null);
				break;
			case 9:
				mCursor = mDB.query(CONTACT_DIS_GROUP,null,"groupName = ?",args,null,null,null);
				break;
			default:
				break;
		}
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mCursor.moveToNext();
			String groupName = mCursor.getString(mCursor.getColumnIndex("groupName"));
			String createUser = mCursor.getString(mCursor.getColumnIndex("createUser"));
			String createTime = mCursor.getString(mCursor.getColumnIndex("createTime"));
			String naturalName = mCursor.getString(mCursor.getColumnIndex("naturalName"));
			String subject = mCursor.getString(mCursor.getColumnIndex("subject"));
			int maxUsers = mCursor.getInt(mCursor.getColumnIndex("maxUsers"));
			String desc = mCursor.getString(mCursor.getColumnIndex("desc"));
			int notifyMode = mCursor.getInt(mCursor.getColumnIndex("notifyMode"));
			bean = new ContactGroup(groupName,createUser,createTime,naturalName,subject,maxUsers,desc,notifyMode);
		}

		return bean;
	}

	/**
	 * 删除所有用户与群组/讨论组关系
	 * 
	 * @param groupType
	 */
	private synchronized void removeAllGroupUserRelationData(int groupType)
	{
		if(groupType == 8)
		{
			mDB.delete(CONTACT_GROUP_USER_REL,null,null);
		}else
		{
			mDB.delete(CONTACT_DIS_GROUP_USER_REL,null,null);
		}
	}

	/**
	 * 插入或更新一个群/讨论组成员关系信息
	 * 
	 * @param mContactGroupUser
	 *            一个用户和群关系的实例
	 * @param groupType
	 *            8：群组 9：讨论组
	 */
	public void insertOneGroupUserRelationData(ContactGroupUser mContactGroupUser, int groupType)
	{
		String mUserNO;
		if(mContactGroupUser.getJid().contains("@"))
		{
			mUserNO = mContactGroupUser.getJid().split("@")[0];
		}else
		{
			mUserNO = mContactGroupUser.getJid();
		}
		// L.e("mUserNO == "+mUserNO);
		int existType = isGroupUserRelationExist(mContactGroupUser.getGroupName(),mUserNO,groupType);
		ContentValues groupUserCV = new ContentValues();
		groupUserCV.put("userNo",mUserNO);
		groupUserCV.put("groupName",mContactGroupUser.getGroupName());
		groupUserCV.put("role",mContactGroupUser.getRole()); // 群成员类型 10-创建人
																// 20-管理员
																// 50-一般用户
																// if (groupType
																// == 8) {
		// mDB.insert(CONTACT_GROUP_USER_REL, null, groupUserCV);
		// } else {
		// mDB.insert(CONTACT_DIS_GROUP_USER_REL, null, groupUserCV);
		// }
		switch(existType)
		{
			case 0:
				mDB.insert(CONTACT_GROUP_USER_REL,null,groupUserCV);
				break;
			case 1:
				mDB.update(CONTACT_GROUP_USER_REL,groupUserCV,"groupName = ? and userNo = ?",new String[] {
						mContactGroupUser.getGroupName(), mUserNO});
				break;
			case 2:
				mDB.insert(CONTACT_DIS_GROUP_USER_REL,null,groupUserCV);
				break;
			case 3:
				mDB.update(CONTACT_DIS_GROUP_USER_REL,groupUserCV,"groupName = ? and userNo = ?",new String[] {
						mContactGroupUser.getGroupName(), mUserNO});
				break;
			default:
				break;
		}
	}

	/**
	 * 得到某群/讨论组的某一个用户与群/讨论组关系
	 * 
	 * @param groupName
	 * @param userNo
	 * @param groupType
	 * @return
	 */
	public ContactGroupUser getContactGroupUserById(String groupName, String userNo, int groupType)
	{
		ContactGroupUser mContactGroupUser = null;
		Cursor mCursor = null;
		int existType = isGroupUserRelationExist(groupName,userNo,groupType);
		if(existType == 1)
		{
			mCursor = mDB.query(CONTACT_GROUP_USER_REL,null,"groupName = ? and userNo = ?",new String[] {groupName,
					userNo},null,null,null);
		}else if(existType == 3)
		{
			mCursor = mDB.query(CONTACT_DIS_GROUP_USER_REL,null,"groupName = ? and userNo = ?",new String[] {groupName,
					userNo},null,null,null);
		}
		if(mCursor != null && mCursor.getCount() > 0)
		{
			while(mCursor.moveToNext())
			{
				String mGroupName = mCursor.getString(mCursor.getColumnIndex("groupName"));
				String mUserNo = mCursor.getString(mCursor.getColumnIndex("userNo"));
				int role = mCursor.getInt(mCursor.getColumnIndex("role"));
				mContactGroupUser = new ContactGroupUser();
				mContactGroupUser.setGroupName(mGroupName);
				mContactGroupUser.setRole(role);
				mContactGroupUser.setUserNo(mUserNo);
			}
		}
		return mContactGroupUser;
	}

	/**
	 * 用户与群/讨论组关系是否存在
	 * 
	 * @param groupName
	 * @param orgNo
	 * @param groupType
	 *            8：群组 9：讨论组
	 * @return 0:群组与用户关系不存在 1：群组与用户关系存在 2：讨论组与用户关系不存在 3 讨论组与用户关系存在
	 */
	public int isGroupUserRelationExist(String groupName, String userNo, int groupType)
	{
		Cursor mCursor;
		int ret = -1;
		if(groupType == 8)
		{// 群组
			mCursor = mDB.query(CONTACT_GROUP_USER_REL,null,"groupName = ? and userNo = ?",new String[] {groupName,
					userNo},null,null,null);
			if(mCursor != null && mCursor.getCount() > 0)
			{
				ret = 1;
			}else
			{
				ret = 0;
			}
		}else
		{// 讨论组
			mCursor = mDB.query(CONTACT_DIS_GROUP_USER_REL,null,"groupName = ? and userNo = ?",new String[] {groupName,
					userNo},null,null,null);
			if(mCursor != null && mCursor.getCount() > 0)
			{
				ret = 3;
			}else
			{
				ret = 2;
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return ret;
	}

	/**
	 * 删除某用户与群、讨论组的关系
	 * 
	 * @param mGroupId
	 * @param mUserNo
	 * @param mContactGroupType
	 */
	public void deleteOneGroupUserRelation(String mGroupId, String mUserNo, int mContactGroupType)
	{
		int ret = isGroupUserRelationExist(mGroupId,mUserNo,mContactGroupType);
		switch(ret)
		{
			case 1:
				mDB.delete(CONTACT_GROUP_USER_REL,"groupName = ? and userNo = ?",new String[] {mGroupId, mUserNo});
				break;
			case 3:
				mDB.delete(CONTACT_DIS_GROUP_USER_REL,"groupName = ? and userNo = ?",new String[] {mGroupId, mUserNo});
				break;
			default:
				break;
		}
	}

	/**
	 * searchUserFromGroup: 根据关键字搜索讨论组或群中的成员
	 * 
	 * @author YINENG
	 * @param groupName
	 * @param groupType
	 * @param key
	 * @return
	 */
	public List<User> searchUserFromGroup(String groupName, int groupType, String key)
	{
		List<User> mUserList1 = queryUserListByKeyWords(key);
		List<User> mUserList2 = queryUsersByGroupName(groupName,groupType);
		List<User> mUserList3 = new ArrayList<User>();
		if(mUserList1 != null && mUserList2 != null)
		{
			for(User user1 : mUserList1)
			{
				for(User user2 : mUserList2)
				{
					if(user1.getUserNo().equals(user2.getUserNo()))
					{
						mUserList3.add(user1);
					}
				}
			}
		}

		return mUserList3;
	}

	/**
	 * 根据群和讨论组ID获取其下的用户
	 * 
	 * @param groupName
	 *            群组/讨论组ID
	 * @param groupType
	 *            8：群组 9：讨论组
	 * @return群/讨论组下的人
	 */
	public List<User> queryUsersByGroupName(String groupName, int groupType)
	{
		List<User> mUsers = null;
		Cursor mCursor;
		if(groupType == 8)
		{
			mCursor = mDB.rawQuery("SELECT u.* FROM " + CONTACT_GROUP_USER_REL + " m," + USER_TABLE
					+ " u WHERE m.userNo = u.userNo AND m.groupName = '" + groupName + "'",null);
		}else
		{
			mCursor = mDB.rawQuery("SELECT u.* FROM " + CONTACT_DIS_GROUP_USER_REL + " m," + USER_TABLE
					+ " u WHERE m.userNo = u.userNo AND m.groupName = '" + groupName + "'",null);
		}
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mUsers = new ArrayList<User>();
			while(mCursor.moveToNext())
			{
				String mUserNo = mCursor.getString(mCursor.getColumnIndex("userNo"));
				String mUserName = mCursor.getString(mCursor.getColumnIndex("userName"));
				int mGender = mCursor.getInt(mCursor.getColumnIndex("gender"));
				String mDayOfBirth = mCursor.getString(mCursor.getColumnIndex("dayOfBirth"));
				String mTelephone = mCursor.getString(mCursor.getColumnIndex("telephone"));
				String mEmail = mCursor.getString(mCursor.getColumnIndex("email"));
				String mPost = mCursor.getString(mCursor.getColumnIndex("post"));
				String mHeadUrl = mCursor.getString(mCursor.getColumnIndex("headUrl"));
				String mSigature = mCursor.getString(mCursor.getColumnIndex("sigature"));
				int mUserType = mCursor.getInt(mCursor.getColumnIndex("userType"));
				int mRemoveTag = mCursor.getInt(mCursor.getColumnIndex("removeTag"));
				int userStatus = mCursor.getInt(mCursor.getColumnIndex("userStatus"));
				mUsers.add(new User(mUserNo,mUserName,mGender,mDayOfBirth,mTelephone,mEmail,mPost,mHeadUrl,mSigature,
						mUserType,mRemoveTag,userStatus));
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mUsers;
	}

	// TODO Auto-generated method stub
	/********************************************* 搜索联系人接口 *********************************************************/

	/**
	 * 搜索联系人接口
	 * 
	 * @param keyStr
	 *            关键字
	 * @return 搜索结果
	 */
	public List<Object> querySearchResultByKeyWords(String keyStr)
	{
		List<Object> mList = new ArrayList<Object>();
		List<User> tempResultUserList = queryUserListByKeyWords(keyStr);
		List<OrganizationTree> tempResultOrgList = queryOrgListByKeyWords(keyStr);
		List<ContactGroup> tempResultGroupList = queryGroupListByKeyWords(keyStr);
		if(tempResultOrgList != null && tempResultOrgList.size() > 0)
		{// 组织机构排前面
			mList.addAll(tempResultOrgList);
		}
		if(tempResultUserList != null && tempResultUserList.size() > 0)
		{
			mList.addAll(tempResultUserList);
		}
		if(tempResultGroupList.size() > 0)
		{
			mList.addAll(tempResultGroupList);
		}
		return mList;
	}

	/**
	 * 根据关键字来获取组织机构
	 * 
	 * @param keyStr
	 *            关键字
	 * @return 组织机构列表
	 */
	public List<OrganizationTree> queryOrgListByKeyWords(String keyStr)
	{
		List<OrganizationTree> mList = null;
		keyStr = keyStr.replaceAll("_","/_");
		keyStr = keyStr.replaceAll("%","/%");
		Cursor mCursor = mDB.query(ORG_TREE_TABLE,null,"orgName like ? ESCAPE '/'",new String[] {"%" + keyStr + "%"},
				null,null,"ordId desc");
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mList = new ArrayList<OrganizationTree>();
			while(mCursor.moveToNext())
			{
				String orgNo = mCursor.getString(mCursor.getColumnIndex("orgNo"));
				String parentOrgNo = mCursor.getString(mCursor.getColumnIndex("parentOrgNo"));
				String orgName = mCursor.getString(mCursor.getColumnIndex("orgName"));
				int orgType = mCursor.getInt(mCursor.getColumnIndex("orgType"));
				// int ordId = mCursor.getInt(mCursor.getColumnIndex("ordId"));
				String ordId = mCursor.getString(mCursor.getColumnIndex("ordId"));
				int removeTag = mCursor.getInt(mCursor.getColumnIndex("removeTag"));
				OrganizationTree tempOrgTree = new OrganizationTree(orgNo,parentOrgNo,orgName,orgType,ordId,removeTag);
				mList.add(tempOrgTree);
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mList;
	}

	/**
	 * 根据关键字来获取用户
	 * 
	 * @param keyStr
	 *            关键字
	 * @return 用户列表
	 */
	public List<User> queryUserListByKeyWords(String keyStr)
	{
		List<User> mList = null;
		keyStr = keyStr.replaceAll("_","/_");
		keyStr = keyStr.replaceAll("%","/%");
		Cursor mCursor = mDB.query(USER_TABLE,null,"userName like ? ESCAPE '/'",new String[] {"%" + keyStr + "%"},null,
				null,"userStatus desc");
		if(mCursor != null && mCursor.getCount() > 0)
		{
			mList = new ArrayList<User>();
			while(mCursor.moveToNext())
			{
				String userNo = mCursor.getString(mCursor.getColumnIndex("userNo"));
				String userName = mCursor.getString(mCursor.getColumnIndex("userName"));
				int gender = mCursor.getInt(mCursor.getColumnIndex("gender"));
				String dayOfBirth = mCursor.getString(mCursor.getColumnIndex("dayOfBirth"));
				String telephone = mCursor.getString(mCursor.getColumnIndex("telephone"));
				String email = mCursor.getString(mCursor.getColumnIndex("email"));
				String post = mCursor.getString(mCursor.getColumnIndex("post"));
				String headUrl = mCursor.getString(mCursor.getColumnIndex("headUrl"));
				String sigature = mCursor.getString(mCursor.getColumnIndex("sigature"));
				int userType = mCursor.getInt(mCursor.getColumnIndex("userType"));
				int removeTag = mCursor.getInt(mCursor.getColumnIndex("removeTag"));
				int userStatus = mCursor.getInt(mCursor.getColumnIndex("userStatus"));
				User tempUser = new User(userNo,userName,gender,dayOfBirth,telephone,email,post,headUrl,sigature,
						userType,removeTag,userStatus);
				if(tempUser != null && tempUser.getUserNo() != null)
				{
					OrganizationTree tempOrg = queryUserRelationByUserNo(tempUser.getUserNo());
					if(tempOrg != null)
					{
						tempUser.setOrgName(tempOrg.getOrgName());
					}
				}
				mList.add(tempUser);
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mList;
	}

	/**
	 * 根据关键字来获取群组\讨论组
	 * 
	 * @param keyStr
	 *            关键字
	 * @return 群组\讨论组列表
	 */
	public List<ContactGroup> queryGroupListByKeyWords(String keyStr)
	{
		Cursor mCursor = null;
		keyStr = keyStr.replaceAll("_","/_");
		keyStr = keyStr.replaceAll("%","/%");
		List<ContactGroup> mList = new ArrayList<ContactGroup>();
		for(int groupType = 8; groupType < 10; groupType++)
		{
			if(groupType == 8)
			{
				mCursor = mDB.query(CONTACT_GROUP,null,"naturalName like ? ESCAPE '/'",
						new String[] {"%" + keyStr + "%"},null,null,null);
			}else
			{
				mCursor = mDB.query(CONTACT_DIS_GROUP,null,"naturalName like ? ESCAPE '/'",new String[] {"%" + keyStr
						+ "%"},null,null,null);
			}
			if(mCursor != null && mCursor.getCount() > 0)
			{
				while(mCursor.moveToNext())
				{
					String groupName = mCursor.getString(mCursor.getColumnIndex("groupName"));
					String createUser = mCursor.getString(mCursor.getColumnIndex("createUser"));
					String createTime = mCursor.getString(mCursor.getColumnIndex("createTime"));
					String naturalName = mCursor.getString(mCursor.getColumnIndex("naturalName"));
					String subject = mCursor.getString(mCursor.getColumnIndex("subject"));
					int maxUsers = mCursor.getInt(mCursor.getColumnIndex("maxUsers"));
					String desc = mCursor.getString(mCursor.getColumnIndex("desc"));
					int notifyMode = mCursor.getInt(mCursor.getColumnIndex("notifyMode"));
					ContactGroup tempContactGroup = new ContactGroup(groupName,createUser,createTime,naturalName,
							subject,maxUsers,desc,notifyMode);
					tempContactGroup.setGroupType(groupType);
					mList.add(tempContactGroup);
				}
			}
		}
		if(mCursor != null)
		{
			mCursor.close();
		}
		return mList;
	}
}
