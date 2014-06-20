package com.smartapp.autostartmanager;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.ta.TAApplication;

public class MainAdapter extends BaseAdapter {

	private List<DataBean> mList = new ArrayList<>();

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());

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
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.process_item, null);
		}
		DataBean bean = mList.get(position);
		return convertView;
	}

	/**
	 * 更新数据
	 */
	public void update(List<DataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		boolean hasAddFirst = false;
		// 排序
		for (DataBean bean : list) {
			if (!bean.mIsForbid && !bean.mIsSysApp) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
		for (DataBean bean : list) {
			if (!bean.mIsForbid && bean.mIsSysApp) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
		hasAddFirst = false;
		for (DataBean bean : list) {
			if (bean.mIsForbid && !bean.mIsSysApp) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
		for (DataBean bean : list) {
			if (bean.mIsForbid && bean.mIsSysApp) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
	}
}
