package com.zhidian.wifibox.adapter;

import java.util.List;

import com.zhidian.wifibox.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SearchKeyAdapter extends AbsListAdapter<String> {

	public SearchKeyAdapter(Context context, List<String> dataList) {
		super(context, dataList);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.gridview_item_keyword, null);
			holder.tvKey = (TextView) convertView.findViewById(R.id.keyword_tv);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String key = mDataList.get(position);
		holder.tvKey.setText(key);
		if (isOdd(position)) {//为奇数
			holder.tvKey.setBackgroundColor(mContext.getResources().getColor(R.color.search_bj));
		}else {
			holder.tvKey.setBackgroundColor(mContext.getResources().getColor(R.color.white));
		}
		return convertView;
	}	
	static class ViewHolder {
		TextView tvKey;//关键字
	}
	
	private boolean isOdd(int i){
		return (i & 1) != 0;
		
	}
        

}
