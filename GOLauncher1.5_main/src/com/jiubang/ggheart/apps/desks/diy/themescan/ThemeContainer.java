package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.data.theme.bean.ThemeBannerBean;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;

/**
 * 
 * 主题数据主界面
 * 
 * @author yangbing
 * */
public class ThemeContainer extends RelativeLayout implements ICleanable {

	private int mThemeDataType = 0; // 主题数据类型
	private LinearLayout mLoadingLayout; // 加载等待界面
	private RelativeLayout mGoStoreLayout; // 去gostore下载更多 界面
	private ThemeListView mThemeListView = null; // 主题内容列表
	private ThemeDataManager mThemeDataManager = null; // 数据管理
	private ThemeImageManager mThemeImageManager = null; // 图片管理
	private LoadThemeDataTask mLoadDataTask; // 异步加载数据Task
	private LoadThemeImageTask mLoadImageTask; // 异步加载图片Task
	private ArrayList<ThemeInfoBean> mThemeInfoBeans; // 主题数据
	private ThemeBannerBean mBannerData;
	private ThemeBannerBean mLockerBannerData;
	private TextView mNoThemes;
	private int mSpecId; //分类主题ID
	public ThemeContainer(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public ThemeContainer(Context context) {
		super(context);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init();
	}

	/**
	 * 初始化
	 * */
	private void init() {
		mThemeListView = (ThemeListView) this.findViewById(R.id.theme_list);
		mThemeListView.initThemeListView();
		mThemeListView.setVisibility(GONE);
		mGoStoreLayout = (RelativeLayout) this.findViewById(R.id.theme_goto_store);
		mGoStoreLayout.setVisibility(GONE);
		mLoadingLayout = (LinearLayout) this.findViewById(R.id.theme_loading);
		mNoThemes = (TextView) findViewById(R.id.nothemes);
		mNoThemes.setVisibility(View.GONE);
		mThemeDataManager = ThemeDataManager.getInstance(getContext());
		mThemeImageManager = ThemeImageManager.getInstance(getContext());
	}

	/**
	 * 加载主题数据
	 * */
	public void loadThemeData(int mThemeDataType) {
		this.mThemeDataType = mThemeDataType;
		// 判断主题数据缓存里是否存在数据，如果不存在再去开启异步线程加载数据
		mThemeInfoBeans = mThemeDataManager.getThemeData(mThemeDataType);
		if (mThemeInfoBeans == null) {
			execLoadThemeDataTask();
		} else {
			if (mThemeImageManager.isExsitImageCache(mThemeDataType)) {
				refreshListView();
			} else {
				execLoadThemeImageTask();
			}

		}

	}

	/**
	 * <br>功能简述:获取专题数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param id 分类ID
	 */
	public void loadSpecThemeData(int id) {
		mSpecId = id;
		mThemeDataType = ThemeConstants.LAUNCHER_SPEC_THEME_ID;
		// 判断主题数据缓存里是否存在数据，如果不存在再去开启异步线程加载数据
		mThemeInfoBeans = mThemeDataManager.getThemeData(mThemeDataType);
		if (mThemeInfoBeans == null) {
			execLoadThemeDataTask();
		} else {
			if (mThemeImageManager.isExsitImageCache(mThemeDataType)) {
				refreshListView();
			} else {
				execLoadThemeImageTask();
			}

		}

	}

	/**
	 * 刷新数据列表
	 * */
	private void refreshListView() {
		if (mLoadingLayout.getVisibility() == View.VISIBLE) {
			mLoadingLayout.setVisibility(View.GONE);
		}
		if (mThemeListView.getVisibility() == View.GONE) {
			mThemeListView.setVisibility(View.VISIBLE);
		}
		if (mGoStoreLayout.getVisibility() == View.GONE && mSpecId == 0) {
			mGoStoreLayout.setVisibility(View.VISIBLE);
			mGoStoreLayout.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Context context = getContext();
					if (context instanceof ThemeManageActivity) {
						((ThemeManageActivity) context).gotoGoStore();
					} else if (context instanceof BannerDetailActivity) {
						((BannerDetailActivity) context).gotoGoStore();
					}
				}
			});
		}
		if (mThemeDataType == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			mThemeListView.setThemeDatas(mThemeInfoBeans, mBannerData);
		} else {
			mThemeListView.setThemeDatas(mThemeInfoBeans, mLockerBannerData);
		}
		mThemeListView.refreshView();
		if (mSpecId == 0) {
			switchNothemetips();
		}
	}

	/**
	 * 缓存主题图片
	 * */
	private void execLoadThemeImageTask() {
		if (mLoadImageTask != null && mLoadImageTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		if (mThemeInfoBeans == null || mThemeInfoBeans.size() <= 0) {
			return;
		}
		mLoadImageTask = new LoadThemeImageTask();
		mLoadImageTask.execute();
	}

	/**
	 * 异步加载主题数据
	 * */
	private void execLoadThemeDataTask() {
		// 加载数据
		if (mLoadDataTask != null && mLoadDataTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		}
		mLoadDataTask = new LoadThemeDataTask();
		mLoadDataTask.execute();
	}

	/**
	 * 异步任务，加载主题数据
	 */
	private class LoadThemeDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if (mThemeListView.getVisibility() == View.VISIBLE) {
				mThemeListView.setVisibility(View.GONE);
			}
			if (mLoadingLayout.getVisibility() == View.GONE) {
				mLoadingLayout.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mThemeDataType == ThemeConstants.LAUNCHER_SPEC_THEME_ID) {
				mThemeDataManager.setSpecThemeId(mSpecId);
			}
			mThemeDataManager.loadThemeData(mThemeDataType);
			mThemeInfoBeans = mThemeDataManager.getThemeData(mThemeDataType);
			if (mThemeDataType == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
				mBannerData = mThemeDataManager.getBannerData();
			} else if (mThemeDataType == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
				mLockerBannerData = mThemeDataManager.getLockerBannerData();
			}
			loadImageCache();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			refreshListView();
		}
	}

	/**
	 * 异步任务，加载主题图片
	 */
	private class LoadThemeImageTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if (mThemeListView.getVisibility() == View.VISIBLE) {
				mThemeListView.setVisibility(View.GONE);
			}
			if (mLoadingLayout.getVisibility() == View.GONE) {
				mLoadingLayout.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			loadImageCache();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			refreshListView();
		}
	}

	@Override
	public void cleanup() {
		mLoadDataTask = null;
		mLoadImageTask = null;
		mThemeInfoBeans = null;
		if (mThemeListView != null) {
			mThemeListView.cleanup();
		}

	}

	/**
	 * 预先加载前9张图片到缓存
	 * */
	private void loadImageCache() {
		if (mThemeInfoBeans == null) {
			return;
		}
		if (mThemeInfoBeans.size() > 9) {
			mThemeImageManager.putImageCache(mThemeInfoBeans.subList(0, 9));
		} else {
			mThemeImageManager.putImageCache(mThemeInfoBeans);
		}
	}

	/**
	 * 横竖屏切换
	 * */
	public void changeOrientation() {
		if (mThemeListView != null && mThemeInfoBeans != null) {
			if (mThemeDataType == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
				mThemeListView.changeOrientation(mThemeInfoBeans, mLockerBannerData);
			} else {
				mThemeListView.changeOrientation(mThemeInfoBeans, mBannerData);
			}
		}
	}

	public void hideNoThemesTips() {
		if (mNoThemes == null) {
			return;
		}
		if (mNoThemes.getVisibility() == View.VISIBLE) {
			mNoThemes.setVisibility(View.GONE);
		}
	}

	public void switchNothemetips() {
		if (mNoThemes == null) {
			return;
		}
		if ((mThemeDataType == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
				|| mThemeDataType == ThemeConstants.LOCKER_FEATURED_THEME_ID
				|| mThemeDataType == ThemeConstants.LAUNCHER_HOT_THEME_ID || mThemeDataType == ThemeConstants.LAUNCHER_SPEC_THEME_ID)
				&& (mThemeInfoBeans == null || mThemeInfoBeans.size() == 0)) {
			if (mNoThemes.getVisibility() == View.GONE) {
				mNoThemes.setVisibility(View.VISIBLE);
			}
			return;
		} else {
			if (mNoThemes.getVisibility() == View.VISIBLE) {
				mNoThemes.setVisibility(View.GONE);
			}
		}
	}

	public TextView getNoThemeTextView() {
		return mNoThemes;
	}

	public void onDestroy() {
		mThemeListView.onDestroy();
	}

	public void reLoadBannerData(int type) {
		if (type == ThemeConstants.LAUNCHER_FEATURED_THEME_ID) {
			mBannerData = mThemeDataManager.getBannerData();
			mThemeListView.setThemeDatas(mThemeInfoBeans, mBannerData);
		} else {
			mLockerBannerData = mThemeDataManager.getLockerBannerData();
			mThemeListView.setThemeDatas(mThemeInfoBeans, mLockerBannerData);
		}
	}

	@Override
	public void setBackgroundDrawable(Drawable d) {
		// TODO Auto-generated method stub
		super.setBackgroundDrawable(d);
		if (d != null && mThemeListView != null) {
			mThemeListView.setBackgroundDrawable(d);
		}
		if (d != null && mLoadingLayout != null) {
			mLoadingLayout.setBackgroundDrawable(d);
		}
	}

	@Override
	public void setBackgroundColor(int color) {
		// TODO Auto-generated method stub
		super.setBackgroundColor(color);
		if (mThemeListView != null) {
			mThemeListView.setBackgroundColor(color);
		}
		if (mLoadingLayout != null) {
			mLoadingLayout.setBackgroundColor(color);
		}
	}

}
