package com.youle.gamebox.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.youle.gamebox.ui.bean.GonglueBean;

/**
 * 新闻列表Adapter
 * @author zhaoyl
 *
 */
public class NewsListAdapter extends YouleBaseAdapter<GonglueBean>{

	public NewsListAdapter(Context mContext, List<GonglueBean> mList) {
		super(mContext, mList);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		GonglueBean gonglueBean = getItem(position);
		
		return convertView;
	}

}
