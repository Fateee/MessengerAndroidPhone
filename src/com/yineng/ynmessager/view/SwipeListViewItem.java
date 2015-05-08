package com.yineng.ynmessager.view;

import com.yineng.ynmessager.activity.MainActivity;
import com.yineng.ynmessager.activity.dissession.DisChatActivity;
import com.yineng.ynmessager.activity.groupsession.GroupChatActivity;
import com.yineng.ynmessager.activity.p2psession.P2PChatActivity;
import com.yineng.ynmessager.activity.session.BroadcastChatActivity;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.bean.RecentChat;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.ToastUtil;
import com.yineng.ynmessager.view.slidingmenu.SlidingMenu;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

//显示设置初始化位置
//响应滑动
//响应点击删除按钮事件，响应点击进入会话，响应各部分显示的更新，响应点击或滑动取消未读消息数
public class SwipeListViewItem extends HorizontalScrollView {
	private String TAG = SwipeListViewItem.class.getName();
	// 当前消息项滑动状态
	private boolean mIsOpen = false;
	private Context mContext;
	private float mCurrentX;
	private float mFirstX;
	// 防没有触发侧边栏打开，并且回到了左边缘的拖
	private boolean mIsClick;
	private SlidingMenu mSlidingMenu;
	private RecentChat mRecentChat;
	private SwipeViewItemOpendListener mOpendListener;

	/**
	 * 设置最近会话数据
	 * 
	 * @param c
	 */
	public void setRecentChat(RecentChat c) {
		mRecentChat = c;
	}

	/**
	 * 是否打开
	 * 
	 * @return
	 */
	public boolean isOpen() {
		return mIsOpen;
	}

	public void setItemOpendListener(SwipeViewItemOpendListener listener) {

		mOpendListener = listener;
	}

	/**
	 * 构造函数，布局文件自动调用
	 * 
	 * @param context
	 * @param attrs
	 */
	public SwipeListViewItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		mSlidingMenu = ((MainActivity) context).getSlidingMenu();
	}

	/**
	 * 做点击效果
	 */
	private void doClick() {
		if (mIsClick && mRecentChat != null) {
			//mRecentChat.setUnReadCount(0);
			switch (mRecentChat.getChatType()) {

			case Const.CHAT_TYPE_P2P:

				Intent intent = new Intent(mContext, P2PChatActivity.class);
				intent.putExtra(P2PChatActivity.ACCOUNT,
						mRecentChat.getUserNo());
				mContext.startActivity(intent);
				break;
			case Const.CHAT_TYPE_DIS:
				Intent disintent = new Intent(mContext, DisChatActivity.class);
				disintent.putExtra(P2PChatActivity.ACCOUNT,
						mRecentChat.getUserNo());
				mContext.startActivity(disintent);
				//ToastUtil.toastAlerMessageCenter(mContext, "打开讨论组", 1000);
				break;
			case Const.CHAT_TYPE_GROUP:
				Intent groupintent = new Intent(mContext, GroupChatActivity.class);
				groupintent.putExtra(P2PChatActivity.ACCOUNT,
						mRecentChat.getUserNo());
//				groupintent.putExtra(P2PChatActivity.ACCOUNT,mRecentChat.getUserNo());
				mContext.startActivity(groupintent);
				//ToastUtil.toastAlerMessageCenter(mContext, "打开群组", 1000);
				break;
			case Const.CHAT_TYPE_NOTICE:

				ToastUtil.toastAlerMessageCenter(mContext, "打开系统通知", 1000);
				break;
			case Const.CHAT_TYPE_BROADCAST:
				Intent broadcast = new Intent(mContext,
						BroadcastChatActivity.class);
				mContext.startActivity(broadcast);
				break;
			}
		}
	}

	/**
	 * 做滑动操作
	 * 
	 * @param scrollX
	 */
	private void handleScrollResult(int scrollX) {
		if (mIsOpen) {
			// 把里面的布局填充到这个空间里面就可以方便的获取back view的宽度
			if (scrollX < 200) {
				this.smoothScrollTo(0, 0);
				mIsOpen = false;
			} else {
				this.smoothScrollTo(getWidth(), 0);
				// mOpendListener.onSwipeViewItemOpend(this);
			}
		} else {
			if (scrollX > 30) {
				this.smoothScrollTo(getWidth(), 0);
				mIsOpen = true;
				mOpendListener.onSwipeViewItemOpend(this);
			} else {
				this.smoothScrollTo(0, 0);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mIsClick = true;
			mFirstX = ev.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			L.v(TAG, "MotionEvent.ACTION_MOVE -> getScrollX() = "
					+ getScrollX());

			mCurrentX = ev.getX();
			if (Math.abs(mCurrentX - mFirstX) > 5) {// 如果滑动5
				mIsClick = false;
			}
			// 右滑
			if ((mCurrentX - mFirstX) > 0) {
				// 当前已经展开
				if (mIsOpen) {
					mSlidingMenu.setSlidingEnabled(false);
				} else {

					return false;
				}
			} else if (mFirstX - mCurrentX > 200) {
				mOpendListener.onSwipeViewItemOpend(this);
			}
			break;
		case MotionEvent.ACTION_UP:
			// 处理手势后的自动滑动
			int scrollX = getScrollX();
			if (scrollX > 0) {
				handleScrollResult(scrollX);
			} else {
				mIsOpen = false;
				doClick();
			}
			mSlidingMenu.setSlidingEnabled(true);
			return true;
		}
		return super.onTouchEvent(ev);
	}

	public interface SwipeViewItemOpendListener {
		void onSwipeViewItemOpend(SwipeListViewItem tiem);
	}
}
