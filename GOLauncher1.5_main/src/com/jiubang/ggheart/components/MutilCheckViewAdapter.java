package com.jiubang.ggheart.components;

import java.util.ArrayList;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MutilCheckViewAdapter extends BaseAdapter {

	public ArrayList<Object> mItemList;
	public int mScreen;

	public MutilCheckViewAdapter(ArrayList<Object> list, int screenIndex) {
		mItemList = list;
		mScreen = screenIndex;
	}

	@Override
	public int getCount() {
		if (mItemList != null) {
			return mItemList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mItemList != null && position >= 0 && position < mItemList.size()) {
			return mItemList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
