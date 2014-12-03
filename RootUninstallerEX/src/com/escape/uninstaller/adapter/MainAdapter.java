package com.escape.uninstaller.adapter;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.escape.uninstaller.ui.IFragment;

public class MainAdapter extends FragmentPagerAdapter {

	private List<IFragment> mList = new ArrayList<IFragment>();

	public MainAdapter(FragmentManager fm) {
		super(fm);
	}

	public void setList(List<IFragment> list) {
		mList.clear();
		if (list != null) {
			mList.addAll(list);
		}
	}

	@Override
	public Fragment getItem(int position) {
		return (Fragment) mList.get(position);
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
		return mList.size();
	}

}
