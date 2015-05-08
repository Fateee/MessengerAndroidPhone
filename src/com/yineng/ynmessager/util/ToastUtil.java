package com.yineng.ynmessager.util;

import com.yineng.ynmessager.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil {
	// private static final int NETWORK_SHOW_TIME = 2000;
	private static Toast mToast = null;

	/**
	 * @param context
	 * @param message
	 * 
	 * @param duration
	 * 
	 */
	public static void toastAlerMessage(Context context, String message,
			int duration) {
		try {
			Toast toast = getInstanceToast(context);
			toast.setText(message);
			toast.setDuration(duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param message
	 * 
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageTop(Context context, String message,
			int duration) {
		try {
			Toast toast = getInstanceToast(context);
			toast.setText(message);
			toast.setDuration(duration);
			toast.setGravity(Gravity.TOP, 0, 0);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param message
	 * 
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageCenter(Context context, String message,
			int duration) {
		try {
			Toast toast = getInstanceToast(context);
			toast.setText(message);
			toast.setDuration(duration);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param context
	 * @param message
	 * 
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageBottom(Context context, String message,
			int duration) {
		try {
			Toast toast = getInstanceToast(context);
			toast.setText(message);
			toast.setDuration(duration);
			toast.setGravity(Gravity.BOTTOM, 0, 0);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param inflater
	 * @param message
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageiconTop(Context context,
			LayoutInflater inflater, String message, int duration) {

		try {
			View layout = inflater.inflate(R.layout.view_toast, null);
			TextView text = (TextView) layout.findViewById(R.id.tv_toast_text);
			text.setText(message);
			Toast toast = getInstanceToast(context);

			toast.setGravity(Gravity.TOP, 0, 8);
			// toast.setMargin(0.0F, 0.83F);
			toast.setDuration(duration);
			toast.setView(layout);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @param inflater
	 * @param message
	 * @param duration
	 * 
	 */
	public static void toastAlerMessageiconCenter(Context context,
			LayoutInflater inflater, String message, int duration) {

		try {
			View layout = inflater.inflate(R.layout.view_toast, null);
			TextView text = (TextView) layout.findViewById(R.id.tv_toast_text);
			text.setText(message);
			Toast toast = getInstanceToast(context);
			//
			toast.setGravity(Gravity.CENTER, 0, 0);
			// toast.setMargin(0.0F, 0.83F);
			toast.setDuration(duration);
			//
			toast.setView(layout);
			toast.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param context
	 * @return
	 */
	@SuppressLint("ShowToast")
	public static Toast getInstanceToast(Context context) {
		if (mToast == null) {
			mToast = Toast.makeText(context.getApplicationContext(), "warning",
					Toast.LENGTH_SHORT);
		} else {
			mToast.cancel();
			mToast = Toast.makeText(context.getApplicationContext(), "warning",
					Toast.LENGTH_SHORT);
		}
		return mToast;
	}
}
