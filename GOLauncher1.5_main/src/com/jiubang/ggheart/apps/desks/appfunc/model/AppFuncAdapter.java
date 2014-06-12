package com.jiubang.ggheart.apps.desks.appfunc.model;

import android.app.Activity;

import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants.MessageID;

/**
 * ���ܱ�������ͼ������
 * 
 * @author tanshu
 * 
 */
public abstract class AppFuncAdapter extends GBaseAdapter implements IBackgroundInfoChangedObserver {
	protected Activity mActivity;
	/**
	 * 是否显示名称
	 */
	protected boolean mDrawText;
	/**
	 * 前台数据监听器
	 */
	protected DataSetObserver dataObserver;

	public AppFuncAdapter(Activity activity, boolean drawText) {
		mActivity = activity;
		mDrawText = drawText;
		try {
			AppFuncFrame.getDataHandler().registerBgInfoChangeObserver(this);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public XComponent getComponent(int position, int x, int y, int width, int height,
			XComponent convertView, XPanel parent) {
		return null;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		dataObserver = observer;
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// TODO Auto-generated method stub
		dataObserver = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean handleChanges(MessageID msgId, Object obj1, Object obj2) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * 图标位置被改变，需要更新刷新内存数据和数据库数据
	 */
	@Override
	public boolean switchPosition(int origPos, int newPos) {
		// 由子类实现
		return false;
	}

	/**
	 * 通知observer刷新
	 */
	public void notifyObserver() {
		if (dataObserver != null) {
			dataObserver.onInvalidated();
		}
	}

	/**
	 * 是否已经从后台加载过数据
	 * 
	 * @return
	 */
	public abstract boolean dataSourceLoaded();
}
