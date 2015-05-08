package com.yineng.ynmessager.activity.session;

import java.util.ArrayList;
import java.util.HashSet;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.BaseActivity;
import com.yineng.ynmessager.bean.BroadcastChat;
import com.yineng.ynmessager.db.dao.BroadcastChatDao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Yutang
 * 
 */
public class BroadcastChatContentActivity extends BaseActivity {
	private ArrayList<BroadcastChat> mBroadcastList;
	private int mCurrentIndex;
	private TextView mTitleTV;
	private TextView mDateTimeTV;
	private TextView mSenderNameTV;
	private WebView mWebView;
	private ImageView mPreviousIV;
	private ImageView mNextIV;
	private BroadcastChatDao mBroadcastChatDao;
	private HashSet<Integer> idSet = new HashSet<Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_session_broadcast_content_layout);
		init();
		findViews();
		updateView();
	}

	@SuppressWarnings("unchecked")
	private void init() {
		mBroadcastChatDao = new BroadcastChatDao(this);
		Intent intent = getIntent();
		if (intent != null) {
			mBroadcastList = (ArrayList<BroadcastChat>) intent
					.getSerializableExtra(BroadcastChatActivity.EXTRA_KEY_LIST);
			if (mBroadcastList == null || mBroadcastList.size() == 0) {
				finish();
				return;
			}
			mCurrentIndex = intent.getIntExtra(
					BroadcastChatActivity.EXTRA_KEY_INDEX, 0);

		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		backTo();
	}

	private void findViews() {
		mTitleTV = (TextView) findViewById(R.id.tv_broadcastchat_content_title);
		mSenderNameTV = (TextView) findViewById(R.id.tv_broadcastchat_content_sendername);
		mDateTimeTV = (TextView) findViewById(R.id.tv_broadcastchat_content_datetime);
		mWebView = (WebView) findViewById(R.id.wv_broadcastchat_content_detail);
		mPreviousIV = (ImageView) findViewById(R.id.iv_broadcastchat_content_pre_button);
		mNextIV = (ImageView) findViewById(R.id.iv_broadcastchat_content_next_button);
	}

	public void cancel(View view) {
		backTo();
	}

	public void backTo() {
		Intent resultIntent = new Intent();
		resultIntent.putExtra("set", idSet);
		setResult(0, resultIntent);
		this.finish();
	}

	private void updateView() {
		BroadcastChat bc = mBroadcastList.get(mCurrentIndex);
		if (bc != null) {
			mTitleTV.setText(bc.getTitle());
			mSenderNameTV.setText("发送者：" + bc.getUserName());
			mDateTimeTV.setText(bc.getDateTime());
			mWebView.loadData(bc.getMessage(), "text/html; charset=UTF-8", null);
			if (bc.getIsRead() == 0) {
				mBroadcastChatDao.updateIsReadById(bc.getId(), 1);// 标记已读
				idSet.add(bc.getId());
			}
		}
		if (mCurrentIndex <= 0) {
			mPreviousIV.setEnabled(false);
		} else {
			mPreviousIV.setEnabled(true);
		}

		if (mCurrentIndex >= mBroadcastList.size() - 1) {
			mNextIV.setEnabled(false);
		} else {
			mNextIV.setEnabled(true);
		}
	}

	public void previous(View view) {
		if (mCurrentIndex > 0) {
			mCurrentIndex--;
			updateView();
		}
	}

	public void next(View view) {
		if (mCurrentIndex < mBroadcastList.size() - 1) {
			mCurrentIndex++;
			updateView();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebView.destroy();
	}
}
