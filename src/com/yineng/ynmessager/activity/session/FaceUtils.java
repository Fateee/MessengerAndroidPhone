package com.yineng.ynmessager.activity.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * 
 */
public class FaceUtils {
	/**
	 * 读取表情配置文件
	 * 
	 * @param context
	 * @return
	 */
	public static List<String> getEmojiFile(Context context) {
		try {
			List<String> list = new ArrayList<String>();
//			InputStream in = context.getResources().getAssets().open("emoji");// 读取文件emoji中的数据
//			BufferedReader br = new BufferedReader(new InputStreamReader(in,
//					"UTF-8"));
//			String str = null;
//			while ((str = br.readLine()) != null) {
//				list.add(str);
//			}
			
			String[] faces = context.getAssets().list("face/gif");
			//将Assets中的表情名称转为字符串一一添加进list
			for (int i = 0; i < faces.length; i++) {
				list.add(faces[i]);
			}
			
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	/**
//	 * 读取表情配置文件
//	 * 
//	 * @param context
//	 * @return
//	 */
//	public static List<String> getEmojiFile(Context context) {
//		List<String> list = new ArrayList<String>();
//		for (int i = 0; i <= 80; i++) {
//			list.add(i+"");
//		}
//		return list;
//	}
}
