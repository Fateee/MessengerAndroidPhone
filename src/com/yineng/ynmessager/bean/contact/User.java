package com.yineng.ynmessager.bean.contact;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable
{
	private String userNo;// 用户ID
	private String userName;// 用户姓名
	private int gender;// 1-男 2-女
	private String dayOfBirth;// 出生日期
	private String telephone;// 联系电话
	private String email;// 邮箱地址
	private String post;// 职务
	private String headUrl;// 头像图标
	private String signature;// 签名
	private int userType;// 用户类型
	private int removeTag;// 删除标志 0-有效 1-无效
	private int userStatus = 0; // 默认0：离线 1：在线  2:android手机在线 3：ios手机在线  4：pc在线 5：pc手机都在线
	private boolean isSelected = false;// 是否选中
	private boolean isExited = false;// 用户是否已存在讨论组中
	private String sortLetters; // 显示数据拼音的首字母
	private String orgName;// 所属组织机构名称
	private int createTime;
	private String joinTime;

	public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in)
		{
			return new User(in);
		}

		public User[] newArray(int size)
		{
			return new User[size];
		}
	};

	private User(Parcel in)
	{
		this.userNo = in.readString();
		this.userName = in.readString();
		this.gender = in.readInt();
		this.dayOfBirth = in.readString();
		this.telephone = in.readString();
		this.email = in.readString();
		this.post = in.readString();
		this.headUrl = in.readString();
		this.signature = in.readString();
		this.userType = in.readInt();
		this.removeTag = in.readInt();
		this.userStatus = in.readInt();
		this.isSelected = in.readByte() != 0;
		this.isExited = in.readByte() != 0;
		this.sortLetters = in.readString();
		this.orgName = in.readString();
		this.createTime = in.readInt();
		this.joinTime = in.readString();
	}

	public User()
	{
		
	}
	
	public User(String userNo, String userName, int gender, String dayOfBirth, String telephone, String email,
			String post, String headUrl, String sigature, int userType, int removeTag, int userStatus)
	{
		this.userNo = userNo;
		this.userName = userName;
		this.gender = gender;
		this.dayOfBirth = dayOfBirth;
		this.telephone = telephone;
		this.email = email;
		this.post = post;
		this.headUrl = headUrl;
		this.signature = sigature;
		this.userType = userType;
		this.removeTag = removeTag;
		this.userStatus = userStatus;
	}

	public String getUserNo()
	{
		return userNo;
	}

	public void setUserNo(String userNo)
	{
		this.userNo = userNo;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public int getGender()
	{
		return gender;
	}

	public void setGender(int gender)
	{
		this.gender = gender;
	}

	public String getDayOfBirth()
	{
		return dayOfBirth;
	}

	public void setDayOfBirth(String dayOfBirth)
	{
		this.dayOfBirth = dayOfBirth;
	}

	public String getTelephone()
	{
		return telephone;
	}

	public void setTelephone(String telephone)
	{
		this.telephone = telephone;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPost()
	{
		return post;
	}

	public void setPost(String post)
	{
		this.post = post;
	}

	public String getHeadUrl()
	{
		return headUrl;
	}

	public void setHeadUrl(String headUrl)
	{
		this.headUrl = headUrl;
	}

	public String getSigature()
	{
		return signature;
	}

	public void setSigature(String sigature)
	{
		this.signature = sigature;
	}

	public int getUserType()
	{
		return userType;
	}

	public void setUserType(int userType)
	{
		this.userType = userType;
	}

	public int getRemoveTag()
	{
		return removeTag;
	}

	public void setRemoveTag(int removeTag)
	{
		this.removeTag = removeTag;
	}

	public int getUserStatus()
	{
		return userStatus;
	}

	public void setUserStatus(int userStatus)
	{
		this.userStatus = userStatus;
	}

	public boolean isSelected()
	{
		return isSelected;
	}

	public void setSelected(boolean isSelected)
	{
		this.isSelected = isSelected;
	}

	public boolean isExited()
	{
		return isExited;
	}

	public void setExited(boolean isExited)
	{
		this.isExited = isExited;
	}

	public String getSortLetters()
	{
		return sortLetters;
	}

	public void setSortLetters(String sortLetters)
	{
		this.sortLetters = sortLetters;
	}

	public String getOrgName()
	{
		return orgName;
	}

	public void setOrgName(String orgName)
	{
		this.orgName = orgName;
	}

	public int getCreateTime()
	{
		return createTime;
	}

	public void setCreateTime(int createTime)
	{
		this.createTime = createTime;
	}

	public String getJoinTime()
	{
		return joinTime;
	}

	public void setJoinTime(String joinTime)
	{
		this.joinTime = joinTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents()
	{
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(this.userNo);
		dest.writeString(this.userName);
		dest.writeInt(this.gender);
		dest.writeString(this.dayOfBirth);
		dest.writeString(this.telephone);
		dest.writeString(this.email);
		dest.writeString(this.post);
		dest.writeString(this.headUrl);
		dest.writeString(this.signature);
		dest.writeInt(this.userType);
		dest.writeInt(this.removeTag);
		dest.writeInt(this.userStatus);
		dest.writeByte((byte)(this.isSelected ? 1 : 0));
		dest.writeByte((byte)(this.isExited ? 1 : 0));
		dest.writeString(this.sortLetters);
		dest.writeString(this.orgName);
		dest.writeInt(this.createTime);
		dest.writeString(this.joinTime);
	}
}
