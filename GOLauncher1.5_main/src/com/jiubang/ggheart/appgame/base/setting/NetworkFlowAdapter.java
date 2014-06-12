/*
 * 文 件 名:  NetworkFlowAdapter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-7-24
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-7-24]
 */
public class NetworkFlowAdapter extends BaseAdapter {

	private String[] mStrArray = null;

	private LayoutInflater mInflater = null;

	private AppGameSettingData mSettingData = null;

	public NetworkFlowAdapter(Context context, String[] array) {
		// TODO Auto-generated constructor stub
		mInflater = LayoutInflater.from(context);
		mStrArray = array;
		mSettingData = AppGameSettingData.getInstance(context);
	}

	/** {@inheritDoc} */

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mStrArray.length;
	}

	/** {@inheritDoc} */

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mStrArray[position];
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
			convertView = mInflater.inflate(R.layout.appgame_network_flow_item, null);
			viewholder = new Viewholder();
			viewholder.mTv = (TextView) convertView
					.findViewById(R.id.appgame_network_flow_item_textview);
			viewholder.mImg = (ImageView) convertView
					.findViewById(R.id.appgame_network_flow_item_checkbox);
			convertView.setTag(viewholder);
		} else {
			viewholder = (Viewholder) convertView.getTag();
		}
		viewholder.mTv.setText(mStrArray[position]);
		if (position == mSettingData.getTrafficSavingMode()) {
			viewholder.mImg.setImageResource(R.drawable.radio_select);
		} else {
			viewholder.mImg.setImageResource(R.drawable.radio_unselect);
		}
		convertView.setId(position);
		return convertView;
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 */
	private class Viewholder {
		TextView mTv;
		ImageView mImg;
	}
}
