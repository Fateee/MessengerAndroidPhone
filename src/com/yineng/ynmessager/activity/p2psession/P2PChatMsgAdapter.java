package com.yineng.ynmessager.activity.p2psession;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.text.SpannableString;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.p2psession.MessageBodyEntity;
import com.yineng.ynmessager.util.TimeUtil;
import com.yineng.ynmessager.view.face.FaceConversionUtil;

/**
 * 
 */
public class P2PChatMsgAdapter extends BaseAdapter
{

	// public static interface IMsgViewType {
	// int IMVT_COM_MSG = 0;
	// int IMVT_TO_MSG = 1;
	// }

	private List<P2PChatMsgEntity> coll = new ArrayList<P2PChatMsgEntity>();
	private LayoutInflater mInflater;
	private P2PChatActivity mContext;

	public P2PChatMsgAdapter(P2PChatActivity context)
	{
		mInflater = LayoutInflater.from(context);
		mContext = context;
	}

	public void setData(List<P2PChatMsgEntity> coll)
	{
		this.coll = coll;
	}

	public List<P2PChatMsgEntity> getData()
	{
		return coll;
	}

	@Override
	public int getCount()
	{
		return coll.size();
	}

	@Override
	public P2PChatMsgEntity getItem(int position)
	{
		return coll.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getItemViewType(int position)
	{
		P2PChatMsgEntity entity = coll.get(position);
		if(entity.getIsSend() == P2PChatMsgEntity.COM_MSG)
		{
			return P2PChatMsgEntity.COM_MSG;
		}else
		{
			return P2PChatMsgEntity.TO_MSG;
		}

	}

	@Override
	public int getViewTypeCount()
	{
		return 2;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final P2PChatMsgEntity entity = coll.get(position);
		ViewHolder viewHolder = null;
		if(convertView == null)
		{
			if(entity.getIsSend() == P2PChatMsgEntity.COM_MSG)
			{
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_left,null);
			}else
			{
				convertView = mInflater.inflate(R.layout.chatting_item_msg_text_right,null);
			}

			viewHolder = new ViewHolder();
			viewHolder.tvSendTime = (TextView)convertView.findViewById(R.id.tv_sendtime);
			viewHolder.tvContent = (TextView)convertView.findViewById(R.id.tv_chatcontent);
			viewHolder.tvSendStatus = (TextView)convertView.findViewById(R.id.tv_chat_tag);
			viewHolder.mLayout = (RelativeLayout)convertView.findViewById(R.id.chat_item_layout);

			convertView.setTag(viewHolder);
		}else
		{
			viewHolder = (ViewHolder)convertView.getTag();
		}

		if(entity.getIsSend() == P2PChatMsgEntity.COM_MSG)
		{
			viewHolder.tvSendStatus.setVisibility(View.INVISIBLE);
		}

		switch(entity.getIsSuccess())
		{
			case P2PChatMsgEntity.SEND_SUCCESS:
				viewHolder.tvSendStatus.setText("发送成功");
				break;
			case P2PChatMsgEntity.SEND_FAILED:
				viewHolder.tvSendStatus.setText("发送失败");
				break;
			case P2PChatMsgEntity.SEND_ING:
				viewHolder.tvSendStatus.setText("发送中");
				break;
			default:
				break;
		}
		viewHolder.tvSendTime.setVisibility(View.INVISIBLE);

		if(entity.isShowTime())
		{
			viewHolder.tvSendTime.setVisibility(View.VISIBLE);
			Date sendTime = new Date(Long.valueOf(entity.getmTime()));
			viewHolder.tvSendTime.setText(TimeUtil.getTimeRelationFromNow2(mContext,sendTime));
//			viewHolder.tvSendTime.setText(TimeUtil.getDateByMillisecond(entity.getmTime(),TimeUtil.FORMAT_DATETIME_24));
		}

		viewHolder.tvContent.setTag(entity);
		viewHolder.tvContent.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v)
			{
				P2PChatMsgEntity entity = (P2PChatMsgEntity)v.getTag();
				if(entity.getIsSuccess() == P2PChatMsgEntity.SEND_FAILED)
				{
					entity.setIsSuccess(P2PChatMsgEntity.SEND_ING);
					mContext.send(entity);
				}
			}
		});

		if(entity.getMessage() != null)
		{
			SpannableString spannableString;
			// L.e("getview == "+viewHolder.tvSendTime.getTag());
			if(entity.getSpannableString() != null)
			{
				spannableString = entity.getSpannableString();
			}else
			{
				MessageBodyEntity body = JSON.parseObject(entity.getMessage(),MessageBodyEntity.class);
				// SpannableString spannableString =
				// FaceConversionUtil.getInstace()
				// .getExpressionString(context, body.getContent());

				// 对内容做处理
				spannableString = FaceConversionUtil.getInstace().handlerContent(mContext,viewHolder.tvContent,
						body.getContent());
				entity.setSpannableString(spannableString);
			}
			viewHolder.tvContent.setText(spannableString);
		}
		return convertView;
	}

	class ViewHolder
	{
		public TextView tvSendTime;
		public TextView tvContent;
		public TextView tvSendStatus;
		public RelativeLayout mLayout;
	}

}
