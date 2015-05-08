package com.yineng.ynmessager.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.session.FaceConversionUtil;
import com.yineng.ynmessager.app.Const;
import com.yineng.ynmessager.manager.NoticesManager;
import com.yineng.ynmessager.service.XmppConnService;
import com.yineng.ynmessager.sharedpreference.LastLoginUserSP;
import com.yineng.ynmessager.util.L;
import com.yineng.ynmessager.util.PreferenceUtils;
import com.yineng.ynmessager.view.ScrollLayout;

public class SplashActivity extends BaseActivity implements
		OnSplashViewChangeListener {
	private String tag = SplashActivity.class.getSimpleName();
	private boolean isFirstLaunch;
	private boolean isAutoLogin;
	private Handler mHandler = new Handler();

	private ScrollLayout mScrollLayout;
	private ImageView[] imgs;
	private int count;
	private int currentItem;
	private Button startBtn;
	private RelativeLayout mainRLayout;
	private LinearLayout pointLLayout;
	private LinearLayout leftLayout;
	private LinearLayout rightLayout;
	private LinearLayout animLayout;

	/**
	 * 第一期启动程序的操作
	 */
	private Runnable mFirstLaunchAction = new Runnable() {
		@Override
		public void run() {

		}
	};

	/**
	 * 启动程序的操作
	 */
	private Runnable mCommonLaunchAction = new Runnable() {
		@Override
		public void run() {
			LastLoginUserSP lastUser = LastLoginUserSP
					.getInstance(SplashActivity.this);

			if (lastUser.isExistsUser()) {// 如果本机已有登陆记录,不管网络状况，直接到主页。
				Intent serviceIntent = new Intent(SplashActivity.this,
						XmppConnService.class);
				SplashActivity.this.startService(serviceIntent);// 开启服务，自动登陆

				Intent mainActivityIntent = new Intent(SplashActivity.this,
						MainActivity.class);
				SplashActivity.this.startActivity(mainActivityIntent);
				NoticesManager.getInstance(SplashActivity.this).clearMessageTypeNotice();
				SplashActivity.this.finish();

			} else {// 如果没有登陆账号记录
				Intent intent = new Intent(SplashActivity.this,
						LoginActivity.class);
				SplashActivity.this.startActivity(intent);

				SplashActivity.this.finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		L.i(tag, "启动");
		new Thread(new Runnable() {
			@Override
			public void run() {
				FaceConversionUtil.getInstace().getFileText(getApplication());
			}
		}).start();
		isFirstLaunch = PreferenceUtils.getPrefBoolean(this,
				Const.IS_FIRST_LAUNCH, true);
		if (isFirstLaunch) {//
			setContentView(R.layout.activity_splash_fisrt_launch_layout);
			PreferenceUtils.setPrefBoolean(this, Const.IS_FIRST_LAUNCH, false);
			L.v("SplashActivity", "isFirstLaunch ->" + isFirstLaunch);
			initView();

		} else {
			setContentView(R.layout.activity_splash_layout);
			// mHandler.post(mFirstLaunchAction);
			L.v("SplashActivity", "isFirstLaunch ->" + isFirstLaunch);
			mHandler.postDelayed(mCommonLaunchAction, 1000);
		}
	}

	private void initView() {
		if (isFirstLaunch) {
			mScrollLayout = (ScrollLayout) findViewById(R.id.ScrollLayout);
			pointLLayout = (LinearLayout) findViewById(R.id.llayout);
			mainRLayout = (RelativeLayout) findViewById(R.id.mainRLayout);
			startBtn = (Button) findViewById(R.id.startBtn);
			startBtn.setOnClickListener(onClick);
			animLayout = (LinearLayout) findViewById(R.id.animLayout);
			leftLayout = (LinearLayout) findViewById(R.id.leftLayout);
			rightLayout = (LinearLayout) findViewById(R.id.rightLayout);
			count = mScrollLayout.getChildCount();
			imgs = new ImageView[count];
			for (int i = 0; i < count; i++) {
				imgs[i] = (ImageView) pointLLayout.getChildAt(i);
				imgs[i].setEnabled(true);
				imgs[i].setTag(i);
			}
			currentItem = 0;
			imgs[currentItem].setEnabled(false);
			mScrollLayout.SetOnViewChangeListener(this);
		}
	}

	private View.OnClickListener onClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.startBtn:
				mScrollLayout.setVisibility(View.GONE);
				pointLLayout.setVisibility(View.GONE);
				animLayout.setVisibility(View.VISIBLE);
				mainRLayout.setBackgroundResource(R.drawable.whatsnew_bg);
				Animation leftOutAnimation = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.translate_left);
				Animation rightOutAnimation = AnimationUtils.loadAnimation(
						getApplicationContext(), R.anim.translate_right);
				leftLayout.setAnimation(leftOutAnimation);
				rightLayout.setAnimation(rightOutAnimation);
				leftOutAnimation.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
						mainRLayout.setBackgroundColor(Color.BLACK);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						leftLayout.setVisibility(View.GONE);
						rightLayout.setVisibility(View.GONE);
						Intent intent = new Intent(SplashActivity.this,
								LoginActivity.class);
						SplashActivity.this.startActivity(intent);

						SplashActivity.this.finish();
						// 结束老Activity启动新Activity之前的一个过度动画
						overridePendingTransition(R.anim.zoom_out_enter,
								R.anim.zoom_out_exit);
					}
				});
				break;
			}
		}
	};

	@Override
	public void OnViewChange(int position) {
		setcurrentPoint(position);
	}

	private void setcurrentPoint(int position) {
		if (position < 0 || position > count - 1 || currentItem == position) {
			return;
		}
		imgs[currentItem].setEnabled(true);
		imgs[position].setEnabled(false);
		currentItem = position;
	}
}
