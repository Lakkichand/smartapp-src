package com.zhidian.wifibox.adapter;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;


/***
 * 自定义滚动广告ViewPager的PagerAdapter
 * @author zhaoyl
 *
 */
public class AdvAdapter extends PagerAdapter {

	private List<View> advs;
	public AdvAdapter(Context context , List<View> list){
		advs = list;
	}
	@Override
	public int getCount() {
		return advs.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(advs.get(position));
	}

	@Override
	public Object instantiateItem(View container, int position) {
		((ViewPager) container).addView(advs.get(position));
		return advs.get(position);
	}

}
