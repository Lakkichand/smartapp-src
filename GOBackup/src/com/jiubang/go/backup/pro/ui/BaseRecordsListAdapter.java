package com.jiubang.go.backup.pro.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.jiubang.go.backup.pro.data.IRecord;

/**
 * 备份列表页面的Adapter
 * 
 * @author maiyongshen
 */
public abstract class BaseRecordsListAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mInflater;

	public BaseRecordsListAdapter(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("invalid argument");
		}
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
	}

	public Context getContext() {
		return mContext;
	}

	public LayoutInflater getLayoutInflater() {
		return mInflater;
	}

	public abstract boolean add(IRecord record);

	public abstract boolean addAll(List<IRecord> records);

	public abstract IRecord remove(int position);

	public abstract void clear();

	protected View newView(Context context, int position, ViewGroup parent) {
		return null;
	}

	protected void bindView(View view, Context context, int position) {

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = newView(mContext, position, parent);
		} else {
			view = convertView;
		}
		bindView(view, mContext, position);
		return view;
	}

}
