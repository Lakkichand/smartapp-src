package com.zhidian.wifibox.adapter;

import com.zhidian.wifibox.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 详情图片默认Adapter(用于刚进来时显示)
 * @author zhaoyl
 *
 */
public class DefaultAdapter extends BaseAdapter {

	private LayoutInflater mInflater;

	public DefaultAdapter(Context mContext) {
		mInflater = LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.gridview_item, null);
			holder.mImg = (ImageView) convertView.findViewById(R.id.mImage);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.mImg.setScaleType(ScaleType.CENTER);
		holder.mImg.setImageResource(R.drawable.loading_s);

		return convertView;
	}

	static class ViewHolder {
		ImageView mImg;
	}

}
