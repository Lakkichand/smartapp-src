package com.zhidian.wifibox.adapter;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.CommentBean;

/**
 * 用户评价 Adapter
 * 
 * @author zhaoyl
 * 
 */
public class CommentAdapter extends BaseAdapter {

	private List<CommentBean> mDataList;
	private Context context;
	private LayoutInflater inflater;
	public CommentAdapter(Context context, List<CommentBean> dataList) {
		mDataList = dataList;
		this.context = context;
		inflater = LayoutInflater.from(context);
	}
	
	public void addItems(List<CommentBean> nlist) {
		for (CommentBean bean : nlist) {
			mDataList.add(bean);
		}
		notifyDataSetChanged();
	}

	public void clear() {
		mDataList.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (mDataList == null || mDataList.size() <= 0)
			return 0;
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.item_user_comment, null);
			holder.ratingBar = (RatingBar) convertView
					.findViewById(R.id.item_comment_rating);
			holder.tvUserName = (TextView) convertView
					.findViewById(R.id.item_comment_username);
			holder.tvDate = (TextView) convertView
					.findViewById(R.id.item_comment_date);
			holder.tvContent = (TextView) convertView
					.findViewById(R.id.item_comment_content);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		CommentBean bean = mDataList.get(position);
		
		holder.tvContent.setText(bean.content);
		holder.tvDate.setText(bean.createTime);
		String userName="";
		if ("".equals(bean.nickname)) {
			userName = "匿名评论";
		}else {
			userName = bean.nickname;
		}
		
		holder.tvUserName.setText(Html.fromHtml("<u>" + userName + "</u>"));
		holder.ratingBar.setRating(bean.score / 2.0f);
		return convertView;
	}

	static class ViewHolder {
		TextView tvUserName; // 用户名称
		TextView tvDate; // 评论时间
		RatingBar ratingBar; // 应用评价等级
		TextView tvContent; // 评论内容

	}

}
