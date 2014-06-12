package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.SortUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditLargeTabView;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 添加应用程序
 */
public class AddAppTab extends BaseTab {

	private static final long CLICK_TIME = 300;
	private long mLastTime; // 上次的点击时间
	private GoProgressBar mGoProgressBar;
	private final static int LIST_INIT_OK = 1000;

	public AddAppTab(Context context, String tag, int level) {
		super(context, tag, level);
		initListByLoading();
		mLastTime = System.currentTimeMillis();
		mIsNeedAsyncLoadData = true;
	}

	@Override
	public ArrayList<Object> getDtataList() {
		return mAllList;
	}

	@Override
	public int getItemCount() {
		if (mAllList != null) {
			return mAllList.size();
		}
		return 0;
	}

	@Override
	public View getView(int position) {
		AppItemInfo itemInfo = (AppItemInfo) mAllList.get(position);
		View view = mInflater.inflate(R.layout.screen_edit_item_theme, null);
		ImageView image = (ImageView) view.findViewById(R.id.thumb);
		image.setImageDrawable(itemInfo.mIcon);
		TextView mText = (TextView) view.findViewById(R.id.title);
		mText.setText(itemInfo.mTitle);
		view.setTag(itemInfo);
		return view;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		long curTime = System.currentTimeMillis();
		if (curTime - mLastTime < CLICK_TIME) {
			return;
		}
		mLastTime = curTime;
		if (showDragFrame() || !resetTag(v)) {
			return;
		}
		if (!checkScreenVacant(1, 1)) {
			return;
		}
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.SCREEN_AUTO_FLY,
				DragFrame.TYPE_ADD_APP_DRAG, v, null);
	}

	@Override
	public boolean onLongClick(View v) {
		super.onLongClick(v);
		// if(!resetTag(v)){
		// return false;
		// }
		//
		// ArrayList<Object> list = new ArrayList<Object>();
		// list.add(Workspace.getLayoutScale());
		// list.add(0);
		// list.add(0);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
		// IDiyMsgIds.DRAG_START, DragFrame.TYPE_ADD_APP_DRAG, v, list);

		return false;
	}
	// 重置view的tag
	private boolean resetTag(View v) {
		if (null == v || null == v.getTag()) {
			return false;
		}
		if (v.getTag() instanceof AppItemInfo) {
			ShortCutInfo ret = new ShortCutInfo();
			AppItemInfo info = (AppItemInfo) v.getTag();
			if (null == info) {
				return false;
			}
			ret.mIcon = info.mIcon;
			ret.mIntent = info.mIntent;
			ret.mItemType = IItemType.ITEM_TYPE_APPLICATION;
			ret.mSpanX = 1;
			ret.mSpanY = 1;
			ret.mTitle = info.mTitle;
			ret.mInScreenId = -1;
			v.setTag(ret);
		} else if (v.getTag() instanceof ShortCutInfo) {
			ShortCutInfo ret = new ShortCutInfo();
			ShortCutInfo info = (ShortCutInfo) v.getTag();
			if (null == info) {
				return false;
			}
			ret.mIcon = info.mIcon;
			ret.mIntent = info.mIntent;
			ret.mItemType = IItemType.ITEM_TYPE_APPLICATION;
			ret.mSpanX = 1;
			ret.mSpanY = 1;
			ret.mTitle = info.mTitle;
			ret.mInScreenId = -1;
			v.setTag(ret);
		} else {
			return false;
		}
		return true;
	}
	// 清空数据
	@Override
	public void clearData() {
		this.setDataSetListener(null);
		this.setTabActionListener(null);
		if (mHandler != null) {
			mHandler.removeMessages(LIST_INIT_OK);
		}

		if (mAllList != null) {
			mAllList.clear();
			mAllList = null;
		}
		if (mGoProgressBar != null) {
			mGoProgressBar = null;
		}
		super.clearData();
	}

	@Override
	public void resetData() {

	}

	// 列表的所有元素
	private ArrayList<Object> mAllList = new ArrayList<Object>();

	public ArrayList<Object> getAllList() {
		return mAllList;
	}
	// 初始化数据
	private void initData() {
		try {
			if (mAllList == null) {
				mAllList = new ArrayList<Object>();
			}
			// 先清空
			mAllList.clear();
			final AppDataEngine engine = GOLauncherApp.getAppDataEngine();
			ArrayList<AppItemInfo> list2 = engine.getCompletedAppItemInfosExceptHide();
			if (list2.size() > 0) {
				try {
					SortUtils.sort(list2, "getTitle", null, null, null);
				} catch (IllegalArgumentException e) {
					// 可能因为用户手机Java运行时环境的问题出错
					e.printStackTrace();
				}
				for (AppItemInfo info : list2) {
					if (info.mIntent != null && info.mIntent.getComponent() != null) {
						mAllList.add(info);
					}
				}
			}
		} catch (Exception e) {
		}
	}
	// 异步加载数据
	private void initListByLoading() {
		// 显示提示框
		showProgressDialog();
		new Thread(ThreadName.SCREEN_EDIT_THEMETAB) {
			@Override
			public void run() {
				// 初始化数据
				initData();
				// 对外通知
				Message msg = new Message();
				msg.what = LIST_INIT_OK;
				mHandler.sendMessage(msg);
			};
		}.start();
	}
	// 处理异步线程发过来的消息
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LIST_INIT_OK :
					// 刷新
					if (mTabActionListener != null) {
						mTabActionListener.onRefreshTab(BaseTab.TAB_ADDAPPS, 0);
					}
					dismissProgressDialog();
					break;

				default :
					break;
			}
		};
	};
	// 显示进度条
	private void showProgressDialog() {
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		if (screenEditBoxFrame != null) {
			ScreenEditLargeTabView mLayOutView = screenEditBoxFrame.getLargeTabView();
			mGoProgressBar = (GoProgressBar) mLayOutView.findViewById(R.id.edit_tab_progress);
			if (mGoProgressBar != null) {
				mGoProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}
	// 隐藏进度条
	private void dismissProgressDialog() {
		if (mGoProgressBar != null) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

}