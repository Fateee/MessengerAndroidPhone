package com.yineng.ynmessager.app;

import java.util.ArrayList;

import com.yineng.ynmessager.bean.contact.OrganizationTree;

import android.app.Activity;

public class ContactActivityManager {
	public static ArrayList<Activity> activityList = new ArrayList<Activity>();
	public static ArrayList<OrganizationTree> mTitleOrgList = new ArrayList<OrganizationTree>();

	public ContactActivityManager() {

	}

	/**
	 * 添加到Activity容器中
	 */
	public static void addActivity(Activity activity) {
		if (!activityList.contains(activity)) {
			activityList.add(activity);
		}
	}

	/**
	 * 遍历所有Activigty并finish
	 */
	public static void finishAllActivity() {
		for (Activity activity : activityList) {
			activity.finish();
		}
		activityList.clear();
	}

	/**
	 * 清空所有组织机构历史信息
	 */
	public static void finishAllOrg() {
		if (mTitleOrgList.size() > 0) {
			mTitleOrgList.clear();
		}
	}
	
	/**
	 * 清空所有activity和org历史
	 */
	public static void finishAllActivityAndOrg() {
		finishAllOrg();
		finishAllActivity();
	}
	
	/**
	 * 结束指定的Activity
	 */
	public static void finishSingleActivity(Activity activity) {
		if (activity != null) {
			if (activityList.contains(activity)) {
				activityList.remove(activity);
			}
			activity.finish();
			activity = null;
		}
	}

	/**
	 * 结束指定类名的Activity 在遍历一个列表的时候不能执行删除操作，所有我们先记住要删除的对象，遍历之后才去删除。
	 */
	public static void finishSingleActivityByClass(Class<?> cls) {
		Activity tempActivity = null;
		for (Activity activity : activityList) {
			if (activity.getClass().equals(cls)) {
				tempActivity = activity;
			}
		}
		finishSingleActivity(tempActivity);
	}
	
	/**
	 * 根据索引位置来结束该位置之后的activity;
	 * @param index
	 */
	public static void finishAllActivityFromIndex(int index) {
		for (int i = index; i < activityList.size();) {
			Activity tempActivity = activityList.get(i);
			finishSingleActivity(tempActivity);
		}
	}

	/**
	 * 根据索引删除该索引之后的数据
	 * @param position
	 */
	public static void finishAllOrgFromIndex(int position) {
		for (int i = position; i < ContactActivityManager.mTitleOrgList.size();) {
			ContactActivityManager.mTitleOrgList.remove(i);
		}
	}
	
	/**
	 * 删除该索引及之后的activity和org实例
	 * @param index
	 */
	public static void finishAllActivityAndOrgFromIndex(int index) {
		finishAllOrgFromIndex(index);
		finishAllActivityFromIndex(index);
	}
	
	/**
	 * 删除最上层的activity
	 */
	public static void finishTopActivity() {
		if (activityList.size() > 0) {
			Activity tempActivity = activityList.get(activityList.size()-1);
			finishSingleActivity(tempActivity);
		}
	}

	/**
	 * 删除最顶部的组织机构实例
	 */
	public static void finishTopOrg() {
		if (mTitleOrgList.size() > 0) {
			ContactActivityManager.mTitleOrgList.remove(mTitleOrgList.size()-1);
		}
	}
	
	/**
	 * 删除最顶部的activity和org实例
	 */
	public static void finishTopActivityAndOrg() {
		finishTopOrg();
		finishTopActivity();
	}
}
