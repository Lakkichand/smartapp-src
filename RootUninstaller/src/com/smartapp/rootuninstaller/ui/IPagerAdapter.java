package com.smartapp.rootuninstaller.ui;

import java.util.ArrayList;
import java.util.List;

import com.smartapp.rootuninstaller.R;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

/**
 * 主页面viewpager适配器
 * 
 * @author xiedezhi
 * 
 */
public class IPagerAdapter extends PagerAdapter {
	/**
	 * viewpager的子view
	 */
	private List<View> mChildViews;

	private Context mContext;

	private List<String> mTitles = new ArrayList<String>();

	public IPagerAdapter(Context context, List<View> views) {
		mContext = context;
		mChildViews = views;

		mTitles.add(mContext.getString(R.string.userapp));
		mTitles.add(mContext.getString(R.string.systemapp));
		mTitles.add(mContext.getString(R.string.recyclebin));
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView(mChildViews.get(position));
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(mChildViews.get(position), 0);
		return mChildViews.get(position);
	}

	@Override
	public int getCount() {
		return mChildViews.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (position >= 0 && position < mTitles.size()) {
			return mTitles.get(position);
		}
		return "NULL";
	}
}
