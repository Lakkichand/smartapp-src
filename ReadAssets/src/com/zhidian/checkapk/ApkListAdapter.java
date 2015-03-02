package com.zhidian.checkapk;

import java.util.ArrayList;
import java.util.List;
import com.zhidian.bean.APKBean;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ApkListAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater inflater;
	private List<APKBean> mList = new ArrayList<APKBean>();
	
	public ApkListAdapter(Context context){
		mContext = context;
		inflater = LayoutInflater.from(context);
	}
	
	/**
	 * ������ݣ�������notifyDataSetChanged
	 */
	public void update(List<APKBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}
	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
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
			convertView = inflater.inflate(R.layout.listview_item, null);
			holder.tvPath = (TextView) convertView.findViewById(R.id.path);
			holder.tvStatus = (TextView) convertView.findViewById(R.id.status);
			
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		APKBean bean = mList.get(position);
		String path = bean.path;
		String status = bean.status;
		int rank = position + 1;
		holder.tvPath.setText(rank + "." + path);
		holder.tvStatus.setText(status);
		
		return convertView;
	}
	
	static class ViewHolder{
		TextView tvPath;
		TextView tvStatus;
	}

}
