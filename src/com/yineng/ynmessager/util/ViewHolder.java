//***************************************************************
//*    2015-5-14  上午10:18:14
//*    桌面产品部  贺毅柳
//*    TEL：18608044899
//*    Email：sumknot@foxmail.com
//*    成都依能科技有限公司
//*    Copyright© 2004-2015 All Rights Reserved
//*    version 1.0.0.0
//***************************************************************
package com.yineng.ynmessager.util;

import android.util.SparseArray;
import android.view.View;

/**
 * 通用ViewHolder类，方便在BaseAdapter.getView()中直接调用，不必再在每个adapter中重复实现ViewHolder
 * 
 * @author 贺毅柳
 * 
 */
public class ViewHolder
{

	/**
	 * 查找获取convertView中对应id的控件
	 * 
	 * @param view
	 *            convertView
	 * @param id
	 *            要获取的控件的id
	 * @return 查找到的控件
	 */
	public static <T extends View> T get(View view, int id)
	{
		SparseArray<View> viewHolder = (SparseArray<View>)view.getTag();
		if(viewHolder == null)
		{
			viewHolder = new SparseArray<View>();
			view.setTag(viewHolder);
		}
		View childView = viewHolder.get(id);
		if(childView == null)
		{
			childView = view.findViewById(id);
			viewHolder.put(id,childView);
		}
		return (T)childView;
	}
}
