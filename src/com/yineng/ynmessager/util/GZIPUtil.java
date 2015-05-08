package com.yineng.ynmessager.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.util.Base64;

public class GZIPUtil {
	
	// 压缩
	public static String compress(String str) {
		if (str == null || str.length() == 0) {
			return "";
		}
		byte[] tArray = null;
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes("UTF-8"));
			gzip.flush();
			gzip.close();
			tArray = out.toByteArray();
			out.close();
			return Base64.encodeToString(tArray, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// 解压缩
	public static String uncompress(String str){
		if (str == null || str.length() == 0) {
			return "";
		}
		try {
			byte[] t = Base64.decode(str, 0);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayInputStream in = new ByteArrayInputStream(t);
			GZIPInputStream gunzip = new GZIPInputStream(in);
			byte[] buffer = new byte[256];
			int n;
			while ((n = gunzip.read(buffer)) >= 0) {
				out.write(buffer, 0, n);
			}
			gunzip.close();
			in.close();
			out.close();
			return out.toString("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
