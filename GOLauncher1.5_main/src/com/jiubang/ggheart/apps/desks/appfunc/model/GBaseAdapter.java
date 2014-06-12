package com.jiubang.ggheart.apps.desks.appfunc.model;

public abstract class GBaseAdapter implements GAdapter {

	@Override
	public boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	/**
	 * 从后台加载数据，由子类实现
	 */
	public abstract void loadApp();

	public abstract boolean switchPosition(int origPos, int newPos);

	public abstract void reloadApps();
}
