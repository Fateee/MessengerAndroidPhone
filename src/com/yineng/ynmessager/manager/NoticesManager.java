package com.yineng.ynmessager.manager;

import java.util.List;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.MainActivity;
import com.yineng.ynmessager.app.AppController;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.settings.Setting;
import com.yineng.ynmessager.db.dao.RecentChatDao;
import com.yineng.ynmessager.util.TimeUtil;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v4.app.NotificationCompat;

/**
 * @author Yutang
 * 
 */
public class NoticesManager
{
	private static final String TAG = "NoticesManager";
	private static NoticesManager noticesManager;
	private Context mContext;
	private NotificationManager mNM;
	private Bitmap mMSGLargeIcon;// 消息通知的大图标
	private RecentChatDao mRecentchatDao;
	private SoundPool mSoundPool;
	private int mStreamId;
	private ActivityManager mActivityManager;

	private NoticesManager(Context context)
	{
		mContext = context;
		mRecentchatDao = new RecentChatDao(mContext);
		mNM = (NotificationManager)mContext.getSystemService(Activity.NOTIFICATION_SERVICE);
		mMSGLargeIcon = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_launcher);
		mActivityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
		initSoundPool();
	}

	/**
	 * 懒汉式单例方法
	 * 
	 * @return
	 */
	public static synchronized NoticesManager getInstance(Context context)
	{
		if(noticesManager == null)
		{
			noticesManager = new NoticesManager(context);
		}
		return noticesManager;
	}

	public void clearMessageTypeNotice()
	{
		mNM.cancel(20150313);
	}

	/**
	 * 未读消息提醒
	 */
	public void sendMessageTypeNotice(String account, int chatType)
	{
		sendMessageTypeNotice(account,chatType,true);
	}

	/**
	 * 未读消息提醒
	 * 
	 * @param account
	 * @param chatType
	 * @param isSetAlarm
	 *            该联系人\群\讨论组是否本身有消息设置的提醒；true代表接收并提醒，false代表接收但不提醒
	 */
	public void sendMessageTypeNotice(String account, int chatType, boolean isSetAlarm)
	{
		updateRecentChatList(account,chatType);
		boolean alarm = shouldAlarm(chatType,isSetAlarm);
		if(alarm)
		{
			playSound(0);
			if(!isTheUIRunning())
			{
				showNotify();
			}
		}else
		{
			showNotify();
		}
	}

	private void showNotify()
	{
		mNM.cancel(20150313);
		mNM.notify(
				20150313,
				createMessageNotification("会话消息",
						"来自" + mRecentchatDao.getUnReadUserCount() + "个联系人的" + mRecentchatDao.getUnReadMsgCount()
								+ "条消息！"));
	}

	public boolean shouldAlarm(int chatType, boolean isSetAlarm)
	{
		boolean flag = false;
		// 获取当前时间是否在设置的免打扰时间段内
		Setting setting = AppController.getInstance().mUserSetting;
		if(setting != null)
		{
			boolean isInTimeScope = TimeUtil.isCurrentInTimeScope(setting.getDistractionFree_begin_h(),
					setting.getDistractionFree_begin_m(),setting.getDistractionFree_end_h(),
					setting.getDistractionFree_end_m());
			// 首先如果开了声音
			flag = setting.getAudio() != 0;
			// 如果免打扰没开，或者免打扰开了但不在时间段内
			flag = flag && (setting.getDistractionFree() == 0 || (setting.getDistractionFree() != 0 && !isInTimeScope));
			// 最后还要判断该联系人\群\讨论组是否本身开启了消息提醒
			flag = flag && isSetAlarm;
		}
		return flag;
	}

	/**
	 * 刷新最近会话
	 * 
	 * @param account
	 * @param chattype
	 */
	public void updateRecentChatList(String account, int chattype)
	{
		Intent intent = new Intent();
		intent.setAction(Const.ACTION_UPDATE_UNREAD_COUNT);
		intent.putExtra("userno",account);
		intent.putExtra("chattype",chattype);
		mContext.sendBroadcast(intent);
	}

	private Notification createMessageNotification(String title, String contenttext)
	{
		Intent intent = new Intent(mContext,MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra("tabIndex",1);

		PendingIntent pintent = PendingIntent.getActivity(mContext,0,intent,0);

		Notification notification = new NotificationCompat.Builder(mContext).setContentTitle(title)
				.setContentText(contenttext).setSmallIcon(R.drawable.ic_launcher).setLargeIcon(mMSGLargeIcon)
				.setAutoCancel(true).setContentIntent(pintent).build();
		// 消息栏通知没有声音和震动
		// notification.defaults |= Notification.DEFAULT_SOUND;
		// notification.defaults |= Notification.DEFAULT_VIBRATE;
		return notification;
	}

	// 初始化声音池的方法
	public void initSoundPool()
	{
		mSoundPool = new SoundPool(2,AudioManager.STREAM_MUSIC,0); // 创建SoundPool对象
		mStreamId = mSoundPool.load(mContext,R.raw.office,1);
	}

	// 播放声音的方法
	public void playSound(int loop)
	{ // 获取AudioManager引用
		/*
		 * AudioManager am = (AudioManager) mContext
		 * .getSystemService(Context.AUDIO_SERVICE); // 获取当前音量 float
		 * streamVolumeCurrent = am .getStreamVolume(AudioManager.STREAM_MUSIC);
		 * // 获取系统最大音量 float streamVolumeMax = am
		 * .getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 计算得到播放音量 float
		 * volume = streamVolumeCurrent / streamVolumeMax; //
		 * 调用SoundPool的play方法来播放声音文件 sp.play(currStreamId, volume, volume, 1,
		 * loop, 1.0f);
		 */
		mSoundPool.play(mStreamId,0.8f,0.8f,1,loop,1.0f);
	}

	/**
	 * 判断当前UI是否前台运行
	 * 
	 * @return
	 */
	public boolean isTheUIRunning()
	{
		boolean isAppRunning = false;
		List<RunningTaskInfo> runningTaskInfos = mActivityManager.getRunningTasks(10);
		String packageName = mContext.getApplicationContext().getPackageName();
		if(runningTaskInfos != null && runningTaskInfos.size() > 0)
		{
			for(RunningTaskInfo info : runningTaskInfos)
			{
				if(info.topActivity.getPackageName().equals(packageName))
				{
					isAppRunning = true;
					break;
				}
			}
		}
		return isAppRunning;
	}
}
