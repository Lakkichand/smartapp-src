/*
 * 文 件 名:  AppMigrationContainer.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-16
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.appcenter.appmigration;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-11-16]
 */
public class AppMigrationContainer extends FrameLayout implements OnScrollListener {

	private PinnedHeaderListView mListView = null;

	private NetworkTipsTool mNetworkTip = null;

	private AppMigrationAdapter mAdapter = null;
	
	/**
	 * 显示的数据的类型（手机内存，SD卡，系统程序）
	 */
	private int mType = -1;

	public AppMigrationContainer(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AppMigrationContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public AppMigrationContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ViewGroup view = (ViewGroup) findViewById(R.id.app_migration_tips_view);
		mNetworkTip = new NetworkTipsTool(view);
		mNetworkTip.showNothing();
		mListView = (PinnedHeaderListView) findViewById(R.id.app_migration_listview);
		mListView.setOnScrollListener(this);
	}

	public void setData(ArrayList<AppMigrationBean> list, int type) {
		if (list == null || list.size() == 0) {
			mListView.setVisibility(View.GONE);
			if (type == AppMigrationBean.sTYPE_INTERNAL_STORAGE) {
				mType = AppMigrationBean.sTYPE_INTERNAL_STORAGE;
				mNetworkTip.showRetryErrorTip(null, NetworkTipsTool.TYPE_NO_APPS_ON_PHONE);
			} else if (type == AppMigrationBean.sTYPE_SD) {
				mType = AppMigrationBean.sTYPE_SD;
				mNetworkTip.showRetryErrorTip(null, NetworkTipsTool.TYPE_NO_APPS_ON_SDCARD);
			} else if (type == AppMigrationBean.sTYPE_SYSTEM) {
				mType = AppMigrationBean.sTYPE_SYSTEM;
				mNetworkTip.showRetryErrorTip(null, NetworkTipsTool.TYPE_NO_APPS_UNMOVABLE);
			}
		} else {
			if (type == AppMigrationBean.sTYPE_INTERNAL_STORAGE) {
				mType = AppMigrationBean.sTYPE_INTERNAL_STORAGE;
			} else if (type == AppMigrationBean.sTYPE_SD) {
				mType = AppMigrationBean.sTYPE_SD;
			} else if (type == AppMigrationBean.sTYPE_SYSTEM) {
				mType = AppMigrationBean.sTYPE_SYSTEM;
			}
			mListView.setVisibility(View.VISIBLE);
			mAdapter = new AppMigrationAdapter(getContext(), list);
			mNetworkTip.removeProgress();
			mListView.setAdapter(mAdapter);
			initListHeaderView();
		}
	}
	
	public int getShowType() {
		return mType;
	}
	
	public void updateAdapterList(ArrayList<AppMigrationBean> list) {
		if (list != null && list.size() > 0) {
			if (mAdapter != null) {
				mListView.setVisibility(View.VISIBLE);
				mNetworkTip.removeProgress();
				mAdapter.updateList(list);
				mAdapter.notifyDataSetChanged();
			} else {
				mListView.setVisibility(View.VISIBLE);
				mAdapter = new AppMigrationAdapter(getContext(), list);
				mNetworkTip.removeProgress();
				mListView.setAdapter(mAdapter);
				initListHeaderView();
			}
		}
		if (list == null || list.size() <= 0) {
			mListView.setVisibility(View.GONE);
			if (mType == AppMigrationBean.sTYPE_INTERNAL_STORAGE) {
				mNetworkTip.showRetryErrorTip(null, NetworkTipsTool.TYPE_NO_APPS_ON_PHONE);
			} else if (mType == AppMigrationBean.sTYPE_SD) {
				mNetworkTip.showRetryErrorTip(null, NetworkTipsTool.TYPE_NO_APPS_ON_SDCARD);
			} else if (mType == AppMigrationBean.sTYPE_SYSTEM) {
				mNetworkTip.showRetryErrorTip(null, NetworkTipsTool.TYPE_NO_APPS_UNMOVABLE);
			}
		}
	}
	
	public void doRefresh() {
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	private void initListHeaderView() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.recomm_appsmanagement_list_head, mListView, false);
		TextView tv = (TextView) view.findViewById(R.id.nametext);
		//对显示的文字做margin的设置
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		tv.setPadding(
				getContext().getResources().getDimensionPixelSize(
						R.dimen.download_manager_text_padding) * 2,
				getContext().getResources().getDimensionPixelSize(
						R.dimen.download_manager_text_padding),
				0,
				getContext().getResources().getDimensionPixelSize(
						R.dimen.download_manager_text_padding));
		tv.setLayoutParams(lp);
		mListView.setPinnedHeaderView(view);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE : {
				//列表停止滚动时
				//找出列表可见的第一项和最后一项
				int start = view.getFirstVisiblePosition();
				int end = view.getLastVisiblePosition();
				//如果有添加HeaderView，要减去
				ListView lisView = null;
				if (view instanceof ListView) {
					lisView = (ListView) view;
				}
				if (lisView != null) {
					int headViewCount = lisView.getHeaderViewsCount();
					start -= headViewCount;
					end -= headViewCount;
				}
				if (end >= view.getCount()) {
					end = view.getCount() - 1;
				}
				//对图片控制器进行位置限制设置
				AsyncImageManager.getInstance().setLimitPosition(start, end);
				//然后解锁通知加载
				AsyncImageManager.getInstance().unlock();
			}
				break;
			case OnScrollListener.SCROLL_STATE_FLING : {
				//列表在滚动，图片控制器加锁
				AsyncImageManager.getInstance().lock();
			}
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL : {
				//列表在滚动，图片控制器加锁
				AsyncImageManager.getInstance().lock();
			}
				break;
			default :
				break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		// AndroidDevice.hideInputMethod(ContactListActivity.this);
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
	}
}
