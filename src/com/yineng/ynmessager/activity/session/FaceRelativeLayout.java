package com.yineng.ynmessager.activity.session;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.activity.p2psession.ViewPagerAdapter;
import com.yineng.ynmessager.bean.ChatEmoji;

/**
 * 
 */
public class FaceRelativeLayout extends RelativeLayout implements
		OnItemClickListener, OnClickListener {

	private Context mContext;

	/** 表情页的监听事件 */
	private OnCorpusSelectedListener mListener;

	/** 显示表情页的viewpager */
	private ViewPager mFaceVP;

	/** 表情页界面集合 */
	private ArrayList<View> mPageViewList;

	/** 游标显示布局 */
	private LinearLayout mLayoutPoint;

	/** 游标点集合 */
	private ArrayList<ImageView> mPointViewList;

	/** 表情集合 */
	private List<List<ChatEmoji>> mEmojiLists;

	/** 表情区域 */
	private View mView;

	/** 输入框 */
	private EditText mSendMessageET;

	/** 表情数据填充器 */
	private List<FaceAdapter> mFaceAdapters;

	/** 当前表情页 */
	private int current = 0;

	public FaceRelativeLayout(Context mContext) {
		super(mContext);
		this.mContext = mContext;
	}

	public FaceRelativeLayout(Context mContext, AttributeSet attrs) {
		super(mContext, attrs);
		this.mContext = mContext;
	}

	public FaceRelativeLayout(Context mContext, AttributeSet attrs, int defStyle) {
		super(mContext, attrs, defStyle);
		this.mContext = mContext;
	}

	public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
		mListener = listener;
	}

	/**
	 * 表情选择监听
	 * 
	 * @时间： 2013-1-15下午04:32:54
	 */
	public interface OnCorpusSelectedListener {

		void onCorpusSelected(ChatEmoji emoji);

		void onCorpusDeleted();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mEmojiLists = FaceConversionUtil.getInstace().mEmojiLists;
		onCreate();
	}

	private void onCreate() {
		initView();
		initViewPager();
		initPoint();
		initData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_face:
			// 隐藏表情选择框
			if (mView.getVisibility() == View.VISIBLE) {
				mView.setVisibility(View.GONE);
			} else {
				mView.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.et_sendmessage:
			// 隐藏表情选择框
			if (mView.getVisibility() == View.VISIBLE) {
				mView.setVisibility(View.GONE);
			}
			break;

		}
	}

	/**
	 * 隐藏表情选择框
	 */
	public boolean hideFaceView() {
		// 隐藏表情选择框
		if (mView.getVisibility() == View.VISIBLE) {
			mView.setVisibility(View.GONE);
			return true;
		}
		return false;
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		mFaceVP = (ViewPager) findViewById(R.id.vp_contains);
		mSendMessageET = (EditText) findViewById(R.id.et_sendmessage);
		mLayoutPoint = (LinearLayout) findViewById(R.id.iv_image);
		mSendMessageET.setOnClickListener(this);
		findViewById(R.id.btn_face).setOnClickListener(this);
		mView = findViewById(R.id.ll_facechoose);

	}

	/**
	 * 初始化显示表情的viewpager
	 */
	private void initViewPager() {
		mPageViewList = new ArrayList<View>();
		// 左侧添加空页
		View nullView1 = new View(mContext);
		// 设置透明背景
		nullView1.setBackgroundColor(Color.TRANSPARENT);
		mPageViewList.add(nullView1);

		// 中间添加表情页

		mFaceAdapters = new ArrayList<FaceAdapter>();
		for (int i = 0; i < mEmojiLists.size(); i++) {
			GridView view = new GridView(mContext);
			FaceAdapter adapter = new FaceAdapter(mContext, mEmojiLists.get(i));
			view.setAdapter(adapter);
			mFaceAdapters.add(adapter);
			view.setOnItemClickListener(this);
			view.setNumColumns(7);
			view.setBackgroundColor(Color.TRANSPARENT);
			view.setHorizontalSpacing(1);
			view.setVerticalSpacing(1);
			view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
			view.setCacheColorHint(0);
			view.setPadding(5, 0, 5, 0);
			view.setSelector(new ColorDrawable(Color.TRANSPARENT));
			view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT));
			view.setGravity(Gravity.CENTER);
			mPageViewList.add(view);
		}

		// 右侧添加空页面
		View nullView2 = new View(mContext);
		// 设置透明背景
		nullView2.setBackgroundColor(Color.TRANSPARENT);
		mPageViewList.add(nullView2);
	}

	/**
	 * 初始化游标
	 */
	private void initPoint() {

		mPointViewList = new ArrayList<ImageView>();
		ImageView imageView;
		for (int i = 0; i < mPageViewList.size(); i++) {
			imageView = new ImageView(mContext);
			imageView.setBackgroundResource(R.drawable.d1);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
			layoutParams.leftMargin = 10;
			layoutParams.rightMargin = 10;
			layoutParams.width = 8;
			layoutParams.height = 8;
			mLayoutPoint.addView(imageView, layoutParams);
			if (i == 0 || i == mPageViewList.size() - 1) {
				imageView.setVisibility(View.GONE);
			}
			if (i == 1) {
				imageView.setBackgroundResource(R.drawable.d2);
			}
			mPointViewList.add(imageView);

		}
	}

	/**
	 * 填充数据
	 */
	private void initData() {
		mFaceVP.setAdapter(new ViewPagerAdapter(mPageViewList));

		mFaceVP.setCurrentItem(1);
		current = 0;
		mFaceVP.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				current = arg0 - 1;
				// 描绘分页点
				mDrawPoint(arg0);
				// 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，如果是最后一屏则跳转到倒数第二屏.
				if (arg0 == mPointViewList.size() - 1 || arg0 == 0) {
					if (arg0 == 0) {
						mFaceVP.setCurrentItem(arg0 + 1);// 第二屏 会再次实现该回调方法实现跳转.
						mPointViewList.get(1).setBackgroundResource(R.drawable.d2);
					} else {
						mFaceVP.setCurrentItem(arg0 - 1);// 倒数第二屏
						mPointViewList.get(arg0 - 1).setBackgroundResource(
								R.drawable.d2);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});

	}

	/**
	 * 绘制游标背景
	 */
	public void mDrawPoint(int index) {
		for (int i = 1; i < mPointViewList.size(); i++) {
			if (index == i) {
				mPointViewList.get(i).setBackgroundResource(R.drawable.d2);
			} else {
				mPointViewList.get(i).setBackgroundResource(R.drawable.d1);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		ChatEmoji emoji = (ChatEmoji) mFaceAdapters.get(current).getItem(arg2);
		if (emoji.getId() == R.drawable.face_del_icon) {
			int selection = mSendMessageET.getSelectionStart();
			String text = mSendMessageET.getText().toString();
			if (selection > 0) {
				String text2 = text.substring(selection - 1);
				if ("]".equals(text2)) {
					int start = text.lastIndexOf("[");
					int end = selection;
					mSendMessageET.getText().delete(start, end);
					return;
				}
				mSendMessageET.getText().delete(selection - 1, selection);
			}
		}
		if (!TextUtils.isEmpty(emoji.getCharacter())) {
			if (mListener != null)
				mListener.onCorpusSelected(emoji);
			SpannableString spannableString = FaceConversionUtil.getInstace()
					.addFace(getContext(), emoji.getId(), emoji.getCharacter());
			mSendMessageET.append(spannableString);
		}

	}
}
