package com.jiubang.go.backup.pro.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;

/**
 * @author jiangpeihe
 *
 */
public class PayUpdateHelpAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<PayItemContent> mItemConteList;
	private Context mContext;
	private int mSelectedPosition = -1;
	
	public PayUpdateHelpAdapter(Context context, List<PayItemContent> itemConteList) {
		mItemConteList = itemConteList;
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mItemConteList.size();
	}

	@Override
	public PayItemContent getItem(int position) {
		return mItemConteList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setSelection(int position) {
		mSelectedPosition = position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.layout_pay_update_help_view_item, null);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.pay_item);
		TextView titleTextView = (TextView) convertView.findViewById(R.id.pay_item_title);
		TextView titleTipsTextView = (TextView) convertView.findViewById(R.id.pay_item_title_tips);
		PayItemContent itemContent = getItem(position);
		if (itemContent != null) {
			imageView
					.setImageDrawable(mContext.getResources().getDrawable(itemContent.mDrawableId));
			titleTextView.setText(itemContent.mItemTitle);
			titleTipsTextView.setText(itemContent.mItemTitleTips);
		}
		if (mSelectedPosition == position) {
			titleTextView.setTextColor(mContext.getResources().getColor(R.color.pay_activity_title_highlight));
		} else {
			titleTextView.setTextColor(mContext.getResources().getColor(R.color.pay_activity_title_normal));
		}
		return convertView;
	}

}
