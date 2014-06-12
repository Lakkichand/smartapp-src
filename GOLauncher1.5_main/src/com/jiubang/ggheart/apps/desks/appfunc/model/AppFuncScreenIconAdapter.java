package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;

import android.app.Activity;

import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.component.AppFuncScreenIcon;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
/**
 * 
 * <br>类描述:功能表桌面屏幕预览组件对应的适配器
 * <br>功能详细描述:
 * 
 * @author  huangshaotao
 * @date  [2012-9-25]
 */
public class AppFuncScreenIconAdapter extends GBaseAdapter {

	private ArrayList<AppFuncScreenItemInfo> mScreenItems = null;
	private Activity mActivity = null;
	
	public AppFuncScreenIconAdapter(Activity activity) {
		mActivity = activity;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {

	}

	@Override
	public int getCount() {
		if (mScreenItems != null) {
			return mScreenItems.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (position < 0) {
			return null;
		}
		if (mScreenItems != null && position < mScreenItems.size()) {
			return mScreenItems.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public XComponent getComponent(int position, int x, int y, int width,
			int height, XComponent convertView, XPanel parent) {
		AppFuncScreenItemInfo info = (AppFuncScreenItemInfo) getItem(position);
		if (info != null) {
			AppFuncScreenIcon icon = new AppFuncScreenIcon(mActivity, 1, x, y, width, height);
			icon.setInfo(info);
			icon.setAttachPanel(parent);
			return icon;
		}

		return null;
	}

	/**
	 * 由于桌面横竖屏切换后会重新加载，加载使用的是MessageQueue.IdleHandler
	 * 其在UI线程空闲时才会工作，由于功能表编辑界面有抖动效果，会不断 的刷新，UI线程始终被其占有，因此桌面在横竖屏切换后始终的不到加载，
	 * 因此这里使用缓存
	 */
	@Override
	public void loadApp() {

		if (mScreenItems != null) {
			for (int i = 0; i < mScreenItems.size(); i++) {
				AppFuncScreenItemInfo info = mScreenItems.get(i);
				info.recycleBmp();
			}
			mScreenItems.clear();
		}
		mScreenItems = new ArrayList<AppFuncScreenItemInfo>();
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_GENERATE_PREVIEW_BMP, -1, null, mScreenItems);
	}

	public void reloadApp(AppFuncScreenItemInfo info) {
		GoLauncher.sendHandler(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_GENERATE_SCREEN_BMP, info.mIndex, info, null);
	}

	@Override
	public boolean switchPosition(int origPos, int newPos) {
		return false;
	}

	@Override
	public void reloadApps() {
		// TODO Auto-generated method stub

	}
}
