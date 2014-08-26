package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

/**
 * TabManageView里的ViewPager的适配器
 * 
 * @author xiedezhi
 * 
 */
public class TabPagerAdapter extends PagerAdapter {

	private List<View> mContainers = new ArrayList<View>();

	public TabPagerAdapter(Context context) {

	}

	@Override
	public int getCount() {
		return mContainers.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(mContainers.get(position));
		return mContainers.get(position);
	}

	@Override
	public Object instantiateItem(View collection, int position) {
		((ViewPager) collection).addView(mContainers.get(position));
		return mContainers.get(position);
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager) collection).removeView((View) view);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public void startUpdate(View arg0) {
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	/**
	 * 更新viewpager的container数据并调用notifyDataSetChanged
	 */
	public void update(List<IContainer> containers) {
		mContainers.clear();
		if (containers == null || containers.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		for (IContainer container : containers) {
			mContainers.add((View) container);
		}
		notifyDataSetChanged();
	}

}
