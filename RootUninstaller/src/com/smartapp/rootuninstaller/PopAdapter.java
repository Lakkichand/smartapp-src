package com.smartapp.rootuninstaller;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PopAdapter extends BaseAdapter {
	private List<String> mPopList = null;
	private LayoutInflater mInflater;

	public PopAdapter(Context context, List<String> list) {
		mPopList = list;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mPopList.size();
	}

	@Override
	public Object getItem(int position) {
		return mPopList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.help_popupwindow, parent,
				false);
		TextView title = (TextView) convertView
				.findViewById(R.id.listitem_title);
		title.setText(mPopList.get(position));
		return convertView;
	}

}
