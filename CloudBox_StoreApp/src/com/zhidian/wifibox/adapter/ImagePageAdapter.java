/**
 * 
 */
package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

/**
 * @author wangsj
 * @time:2013-10-22 上午10:39:36
 *
 */
public class ImagePageAdapter  extends PagerAdapter {
	private List<ImageView> GestureImageViews=new ArrayList<ImageView>();
	private Context context;

	public ImagePageAdapter(Context context,List<ImageView> GestureImageViews) {
		this.context=context;
		this.GestureImageViews=GestureImageViews;
	}

		@Override
		public int getCount() {
			return GestureImageViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(GestureImageViews.get(arg1));
			return GestureImageViews.get(arg1);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			if(getCount()>0){
				((ViewPager) arg0).removeView((View) arg2);
			}
			
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
		
			return arg0 == arg1;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public void finishUpdate(View arg0) {

		}
	}