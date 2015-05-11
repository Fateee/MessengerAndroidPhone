package com.yineng.ynmessager.view.face;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yineng.ynmessager.R;
import com.yineng.ynmessager.bean.ChatEmoji;

/**
 * 
 */
public class FaceAdapter extends BaseAdapter {

    private List<ChatEmoji> mData;

    private LayoutInflater mInflater;

    private int mSize=0;

	private Context mContext;

    public FaceAdapter(Context context, List<ChatEmoji> list) {
        this.mInflater=LayoutInflater.from(context);
        this.mData=list;
        this.mSize=list.size();
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return this.mSize;
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatEmoji emoji=mData.get(position);
        ViewHolder viewHolder=null;
        if(convertView == null) {
            viewHolder=new ViewHolder();
            convertView=mInflater.inflate(R.layout.item_face, null);
            viewHolder.iv_face=(ImageView)convertView.findViewById(R.id.item_iv_face);
            convertView.setTag(viewHolder);
        } else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        if(emoji.getId() == R.drawable.face_del_icon) {
            convertView.setBackgroundDrawable(null);
            viewHolder.iv_face.setImageResource(emoji.getId());
        } else if(TextUtils.isEmpty(emoji.getCharacter())) {
            convertView.setBackgroundDrawable(null);
            viewHolder.iv_face.setImageDrawable(null);
        } else {
            viewHolder.iv_face.setTag(emoji);
            try {
				Bitmap mBitmap = BitmapFactory.decodeStream(mContext.getAssets().open("face/gif/" + emoji.getFaceName()+".gif"));
				viewHolder.iv_face.setImageBitmap(mBitmap);
			} catch (IOException e) {
				e.printStackTrace();
			}
//            viewHolder.iv_face.setImageResource(emoji.getId());
        }

        return convertView;
    }

    class ViewHolder {

        public ImageView iv_face;
    }
}