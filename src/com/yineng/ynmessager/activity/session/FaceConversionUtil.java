package com.yineng.ynmessager.activity.session;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.ChatEmoji;
import com.yineng.ynmessager.view.face.gif.AnimatedGifDrawable;
import com.yineng.ynmessager.view.face.gif.AnimatedImageSpan;

/**
 * 
 */
public final class FaceConversionUtil {

	/** 每一页表情的个数 */
	private final int mPageSize = 20;

	private static FaceConversionUtil mFaceConversionUtil;

	/** 保存于内存中的表情HashMap */
	private HashMap<String, String> mEmojiMap = new HashMap<String, String>();

	/** 保存于内存中的表情集合 */
	private List<ChatEmoji> mEmojiList = new ArrayList<ChatEmoji>();

	/** 表情分页的结果集合 */
	public List<List<ChatEmoji>> mEmojiLists = new ArrayList<List<ChatEmoji>>();

	private FaceConversionUtil() {

	}

	public static FaceConversionUtil getInstace() {
		if (mFaceConversionUtil == null) {
			mFaceConversionUtil = new FaceConversionUtil();
		}
		return mFaceConversionUtil;
	}

	/**
	 * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
	 * 
	 * @param context
	 * @param str
	 * @return
	 */
	public SpannableString getExpressionString(Context context, String str) {
		
		SpannableString spannableString = new SpannableString(str);
		// 正则表达式比配字符串里是否含有表情，如： 我好[开心]啊
		String zhengze = "\\[[^\\]]+\\]";
		// 通过传入的正则表达式来生成一个pattern
		Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
		try {
			dealExpression(context, spannableString, sinaPatten, 0);
		} catch (Exception e) {
			Log.e("dealExpression", e.getMessage());
		}
		return spannableString;
	}

	/**
	 * 添加表情
	 * 
	 * @param context
	 * @param imgId
	 * @param spannableString
	 * @return
	 */
	public SpannableString addFace(Context context, int imgId,
			String spannableString) {
		if (TextUtils.isEmpty(spannableString)) {
			return null;
		}
//		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
//				imgId);
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(context.getAssets().open("face/gif/" + imgId+".gif"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bitmap = Bitmap.createScaledBitmap(bitmap, 35, 35, true);
		ImageSpan imageSpan = new ImageSpan(context, bitmap);
		SpannableString spannable = new SpannableString(spannableString);
		spannable.setSpan(imageSpan, 0, spannableString.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
	 * 
	 * @param context
	 * @param spannableString
	 * @param patten
	 * @param start
	 * @throws Exception
	 */
	private void dealExpression(Context context,
			SpannableString spannableString, Pattern patten, int start)
			throws Exception {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			String key = matcher.group();
			// 返回第一个字符的索引的文本匹配整个正则表达式,ture 则继续递归
			if (matcher.start() < start) {
				continue;
			}
			String value = mEmojiMap.get(key);
			if (TextUtils.isEmpty(value)) {
				continue;
			}
			Bitmap mBitmap = BitmapFactory.decodeStream(context.getAssets().open("face/gif/" + value));
			// 通过图片资源id来得到bitmap，用一个ImageSpan来包装
			ImageSpan imageSpan = new ImageSpan(mBitmap);
			// 计算该图片名字的长度，也就是要替换的字符串的长度
			int end = matcher.start() + key.length();
			// 将该图片替换字符串中规定的位置中
			spannableString.setSpan(imageSpan, matcher.start(), end,
					Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			if (end < spannableString.length()) {
				// 如果整个字符串还未验证完，则继续。。
				dealExpression(context, spannableString, patten, end);
			}
			break;
			
//			int resId = context.getResources().getIdentifier(value, "drawable",
//					context.getPackageName());
//			// 通过上面匹配得到的字符串来生成图片资源id
//			// Field field=R.drawable.class.getDeclaredField(value);
//			// int resId=Integer.parseInt(field.get(null).toString());
//			if (resId != 0) {
//				Bitmap bitmap = BitmapFactory.decodeResource(
//						context.getResources(), resId);
//				bitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, true);
//				// 通过图片资源id来得到bitmap，用一个ImageSpan来包装
//				ImageSpan imageSpan = new ImageSpan(bitmap);
//				// 计算该图片名字的长度，也就是要替换的字符串的长度
//				int end = matcher.start() + key.length();
//				// 将该图片替换字符串中规定的位置中
//				spannableString.setSpan(imageSpan, matcher.start(), end,
//						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//				if (end < spannableString.length()) {
//					// 如果整个字符串还未验证完，则继续。。
//					dealExpression(context, spannableString, patten, end);
//				}
//				break;
//			}
		}
	}

	public void getFileText(Context context) {
		parseData(FaceUtils.getEmojiFile(context), context);
	}

	/**
	 * 解析字符
	 * 
	 * @param data
	 */
	private void parseData(List<String> data, Context context) {
		if (data == null) {
			return;
		}
		ChatEmoji emojEentry;
		try {
			for (String str : data) {
//				String[] text = str.split(",");
//				String fileName = text[0]
//						.substring(0, text[0].lastIndexOf("."));
//				mEmojiMap.put(text[1], fileName);
//				
//				//根据文件名获得资源ID
//				int resID = context.getResources().getIdentifier(fileName,
//						"drawable", context.getPackageName());
//
//				if (resID != 0) {
//					emojEentry = new ChatEmoji();
//					emojEentry.setId(resID);
//					emojEentry.setCharacter(text[1]);
//					emojEentry.setFaceName(fileName);
//					mEmojiList.add(emojEentry);
//				}
				String[] fileTexts = str.split("\\.");
				String faceId = "[/"+fileTexts[0]+"]";//eg:[/18]
				String fileName = fileTexts[0];//eg:18
				mEmojiMap.put(faceId, str); //key:[/18] value:18.gif
				
//				//根据文件名获得资源ID
//				int resID = context.getResources().getIdentifier(fileName,
//						"drawable", context.getPackageName());

				emojEentry = new ChatEmoji();
				emojEentry.setId(Integer.parseInt(fileName));
				emojEentry.setCharacter(faceId);
				emojEentry.setFaceName(fileName);
				mEmojiList.add(emojEentry);
			}
			int pageCount = (int) Math.ceil(mEmojiList.size() / 20 + 0.1);

			for (int i = 0; i < pageCount; i++) {
				mEmojiLists.add(getData(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取分页数据
	 * 
	 * @param page
	 * @return
	 */
	private List<ChatEmoji> getData(int page) {
		int startIndex = page * mPageSize;
		int endIndex = startIndex + mPageSize;

		if (endIndex > mEmojiList.size()) {
			endIndex = mEmojiList.size();
		}
		// 不这么写，会在viewpager加载中报集合操作异常，我也不知道为什么
		List<ChatEmoji> list = new ArrayList<ChatEmoji>();
		list.addAll(mEmojiList.subList(startIndex, endIndex));
		if (list.size() < mPageSize) {
			for (int i = list.size(); i < mPageSize; i++) {
				ChatEmoji object = new ChatEmoji();
				list.add(object);
			}
		}
		if (list.size() == mPageSize) {
			ChatEmoji object = new ChatEmoji();
			object.setId(R.drawable.face_del_icon);
			list.add(object);
		}
		return list;
	}

	/**
	 * 处理消息文本
	 * @param tvContent
	 * @param content
	 * @return
	 */
	public SpannableString handlerContent(Context mContext,TextView gifTextView, String content) {
		SpannableString sb = new SpannableString(content);
		String regex = "(\\[\\/\\d{1,2}\\])";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		if (handlerCount(content) < 10) {
			showGifFace(true,m,sb,gifTextView,mContext);
			
		} else {
			showGifFace(false,m,sb,gifTextView,mContext);
		}
		
		return sb;
	}
	
	/**
	 * 处理消息文本
	 * @param gifTextView 
	 * @param sb 
	 * @param m 
	 * @param mContext 
	 * @param b
	 */
	private void showGifFace(boolean show, Matcher m, SpannableString sb, final TextView gifTextView, Context mContext) {
		InputStream is = null;
		while (m.find()) {
			String tempText = m.group();
			try {
				String num = tempText.substring("[/".length(), tempText.length()- "]".length());
				String gif = "face/gif/" + num + ".gif";
				/**
				 * 如果open这里不抛异常说明存在gif，则显示对应的gif
				 * 否则说明gif找不到，则显示png
				 * */
				is = mContext.getAssets().open(gif);
				
				if (show) {/**显示gif**/
					sb.setSpan(new AnimatedImageSpan(new AnimatedGifDrawable(is,new AnimatedGifDrawable.UpdateListener() {
						@Override
						public void update() {
							gifTextView.postInvalidate();
						}
					})), m.start(), m.end(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} else {/**显示静态pic**/
					Bitmap mBitmap = BitmapFactory.decodeStream(is);
					// 通过图片资源id来得到bitmap，用一个ImageSpan来包装
					ImageSpan imageSpan = new ImageSpan(mBitmap);
					sb.setSpan(imageSpan, m.start(), m.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}

			} catch (Exception e) {
//				String png = tempText.substring("#[".length(),tempText.length() - "]#".length());
//				try {
//					sb.setSpan(new ImageSpan(mContext, BitmapFactory.decodeStream(mContext.getAssets().open(png))), m.start(), m.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//				} catch (IOException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				e.printStackTrace();
			}
		}
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// 统计gif个数
	private int handlerCount(String content) {
		int number = 0;
		String regex = "(\\[\\/\\d{1,2}\\])";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		while (m.find()) {
			number++;
		}
		return number;
	}
}