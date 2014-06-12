/*
 * 文 件 名:  AppGameMenuAdapter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-7-23
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.menu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-23]
 */
public class AppGameMenuAdapter extends BaseAdapter {

	/**
	 * 字符串资源值的数组
	 */
	private int[] resId = new int[] {};

	private LayoutInflater mInflater = null;

	public AppGameMenuAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	/** {@inheritDoc} */

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return resId.length;
	}

	/** {@inheritDoc} */

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return resId[position];
	}

	/** {@inheritDoc} */

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/** {@inheritDoc} */

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Viewholder viewholder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.appgame_menu_item, null);
			viewholder = new Viewholder();
			viewholder.tv = (TextView) convertView.findViewById(R.id.appgame_menu_item_textview);
			convertView.setTag(viewholder);
		} else {
			viewholder = (Viewholder) convertView.getTag();
		}
		int id = resId[position];
		viewholder.tv.setText(id);
		convertView.setId(id);
		return convertView;
	}

	public void setResourceData(int[] id) {
		if (id != null) {
			resId = id;
		}
	}

	private class Viewholder {
		TextView tv;
	}

	/**
	 * 功能简述:判断Adapter的基本数据是否为Null 功能详细描述: 注意:
	 * 
	 * @return
	 */
	public boolean isDataEmpty() {
		if (resId == null) {
			return true;
		}
		return false;
	}
}
