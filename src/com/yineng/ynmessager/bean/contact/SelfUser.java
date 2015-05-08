//***************************************************************
//*    2015-4-24  上午9:32:15
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.bean.contact;

/**
 * @author 贺毅柳
 *
 */
public class SelfUser extends User
{
	public SelfUser()
	{
		super();
	}

	public SelfUser(String userNo, String userName, int gender, String dayOfBirth, String telephone, String email,
			String post, String headUrl, String sigature, int userType, int removeTag, int userStatus)
	{
		super(userNo, userName, gender, dayOfBirth, telephone, email, post, headUrl, sigature, userType, removeTag, userStatus);
	}


}
