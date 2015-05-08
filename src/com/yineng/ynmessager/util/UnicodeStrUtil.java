package com.yineng.ynmessager.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Html;
import android.util.Log;

public class UnicodeStrUtil {
	
	public void test() {
		String test = "{&amp;quot;Content&amp;quot;:&amp;quot;4444&amp;quot;,&amp;quot;SenderName&amp;quot;:&amp;quot;teacher_00086&amp;quot;,&amp;" +
				"quot;MsgType&amp;quot;:&amp;quot;1&amp;quot;,&amp;quot;Images&amp;quot;:[],&amp;quot;CustomAvatars&amp;quot;:[],&amp;quot;Files&amp;quot;:[],&amp;" +
				"quot;Style&amp;quot;:{&amp;quot;Foreground&amp;quot;:&amp;quot;#FF000000&amp;quot;,&amp;quot;IsUnderline&amp;quot;:false,&amp;quot;IsItalic&amp;quot;" +
				":false,&amp;quot;IsBold&amp;quot;:false,&amp;quot;FontSize&amp;quot;:12,&amp;quot;FontFamily&amp;quot;:&amp;quot;宋体&amp;quot;}}";
		String test1 = String.format(test, null);
		String text = Html.fromHtml(test1).toString();
		
		String inp = "http://10.50.74.222/TV/0/9?Token=9$10.63.253.198$123&amp;StartTime=1344002400&amp;EndTime=1344006000";
		 
		try{
		    String text2 = Html.fromHtml(inp).toString();
		    String text3 = Html.fromHtml(test).toString();
		    Log.d("aaa",text2);//http://10.50.74.222/TV/0/9?Token=9$10.63.253.198$123&StartTime=1344002400&EndTime=1344006000
		    Log.e("bbb",text3);
		     
		}catch(Exception ex){
		    ex.printStackTrace();
		}
	}
	
	public static String URLEncoder(String tempStr) {
		String utf8_Str = "";
		try {
			Log.i("tempStr", tempStr);
			utf8_Str= URLEncoder.encode(tempStr, "gb2312");
			Log.i("ChangeUtf8", utf8_Str);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return utf8_Str;
	}
	
	public static String URLDecoder(String tempStr) {
		String utf8_Str = "";
		try {
//			utf8_Str= URLEncoder.encode(tempStr, "utf-8");
			Log.e("tempStr", tempStr);
			utf8_Str = URLDecoder.decode(tempStr, "gb2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return utf8_Str;
	}

	 /**
     * 汉字转Unicode
     * @param s
     * @return
     */
    public static String gbEncoding(final String s){
        String str = "";
        for (int i = 0; i < s.length(); i++) {
        int ch = (int) s.charAt(i);
        str += "\\u" + Integer.toHexString(ch);
        }
        return str;
    }
    
    /**
     * Unicode转汉字
     * @param str
     * @return
     */
    public static String encodingtoStr(String str){
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
        ch = (char) Integer.parseInt(matcher.group(2), 16);
        str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }
}
