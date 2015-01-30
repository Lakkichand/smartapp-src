package com.zhidian.wifibox.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zhidian.wifibox.view.SystemAppUninstallFragment;
import com.zhidian.wifibox.view.UserAppUninstallFragment;

/**
 * 应用卸载界面适配器
 * 
 * @author xiedezhi
 * 
 */
public class AppUninstallAdapter extends FragmentPagerAdapter {

	private int mCount = 0;

	public AppUninstallAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int arg0) {
		if (arg0 == 0) {
			return new UserAppUninstallFragment();
		} else {
			return new SystemAppUninstallFragment();
		}
	}

	@Override
	public int getCount() {
		return mCount;
	}

	/**
	 * 更新数据
	 */
	public void update(int count) {
		mCount = count;
		notifyDataSetChanged();
	}

}
