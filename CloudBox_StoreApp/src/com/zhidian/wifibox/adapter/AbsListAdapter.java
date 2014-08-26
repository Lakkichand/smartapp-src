package com.zhidian.wifibox.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

/**
 * 自定义BaseAdapter
 * @author zhaoyl
 *
 */
public abstract class AbsListAdapter<E> extends BaseAdapter {
	protected Context mContext;
	protected LayoutInflater mInflater;
	protected List<E> mDataList;

	public AbsListAdapter(Context context, List<E> dataList) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDataList = dataList;
	}

	public int getCount() {
		return mDataList.size();
	}

	public Object getItem(int arg0) {
		return mDataList.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

}
