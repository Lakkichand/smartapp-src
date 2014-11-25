package com.escape.uninstaller.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.escape.uninstaller.ui.SystemAppFragment;
import com.escape.uninstaller.ui.TrashFragment;
import com.escape.uninstaller.ui.UserAppFragment;

public class MainAdapter extends FragmentPagerAdapter {

	public MainAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		if (position == 0) {
			return UserAppFragment.newInstance();
		} else if (position == 1) {
			return SystemAppFragment.newInstance();
		} else {
			return TrashFragment.newInstance();
		}
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (position == 0) {
			return "用户应用";
		} else if (position == 1) {
			return "系统应用";
		} else {
			return "回收站";
		}
	}

	@Override
	public int getCount() {
		return 3;
	}

}
