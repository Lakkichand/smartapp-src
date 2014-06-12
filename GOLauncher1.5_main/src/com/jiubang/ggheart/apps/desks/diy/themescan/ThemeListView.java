package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.GoLockerThemeManager;
import com.jiubang.ggheart.data.theme.LockerManager;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeBannerBean;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 主题列表
 * 
 * @author yangbing
 * */
public class ThemeListView extends ListView {

	private Context mContext;
	private ThemeListAdapter mListAdapter = null;
	private ArrayList<ThemeInfoBean> mThemeDatas; // 主题数据
	private ThemeBannerBean mBannerBean;
	private View.OnClickListener mItemClickListener; // 点击事件监听器
	public ThemeListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public ThemeListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public ThemeListView(Context context) {
		super(context);
		mContext = context;
	}

	/**
	 * 设置显示的数据
	 * */
	public void setThemeDatas(ArrayList<ThemeInfoBean> themeDatas, ThemeBannerBean bannerBean) {
		if (themeDatas != null) {
			mThemeDatas = (ArrayList<ThemeInfoBean>) themeDatas.clone();
			mBannerBean = bannerBean;
			if (mThemeDatas.get(0).getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID) {
				setDivider(null);
			} else if (mThemeDatas.get(0).getBeanType() == ThemeConstants.LOCKER_INSTALLED_THEME_ID) {
				String curPackageName = ThemeManager.getInstance(mContext).getCurLockerTheme();
				updateCurLockerTheme(curPackageName);
			}
			setVisibility(View.GONE);
			mListAdapter.setThemeDatas(mThemeDatas, mBannerBean);
			setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 初始化item时间监听器
	 * */
	private void initItemClickListener() {
		mItemClickListener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v instanceof ItemThemeView) {
					ThemeInfoBean infoBean = ((ItemThemeView) v).getThemeData();
					if (infoBean == null) {
						return;
					}
					String packageName = infoBean.getPackageName();
					if (packageName == null || "".equals(packageName)) {
						return;
					}
					switch (infoBean.getBeanType()) {
						case ThemeConstants.LAUNCHER_INSTALLED_THEME_ID :
							deskInstalledClickEvent(packageName);
							StatisticsData
									.countThemeTabData(StatisticsData.THEME_TAB_ID_LOCAL_THEME_DETAIL);
							break;
						case ThemeConstants.LOCKER_INSTALLED_THEME_ID :
							lockerInstalledClickEvent(packageName);
							break;
						default :
							break;
					}

				}

			}
		};
	}

	/**
	 * 锁屏本地主题点击事件
	 * */
	protected void lockerInstalledClickEvent(String packageName) {
		if (isGoLockerVersionLow()) {
			// 版本过低
			showGoLockerUpdateTips(packageName);
		} else {
			Intent intent = new Intent();
			intent.setAction(ICustomAction.ACTION_LOCKER_DETAIL);
			boolean isZip = LockerManager.getInstance(mContext).isZipTheme(mContext, packageName);
			if (isZip && !isGoLockerSuppertGoTheme()) {
				// 版本过低
				updateGoLockerTips();
				return;
			}
			intent.putExtra("IS_ZIP_THEME", isZip);
			if (isZip) {
				String apkPath = LockerManager.getInstance(mContext).getZipThemeFileName(
						packageName);
				intent.putExtra("ZIP_FILE_NAME", apkPath);
			}

			intent.putExtra(ThemeConstants.LOCKER_PACKAGE_NAME_EXTRA_KEY, packageName);
			try {
				getContext().startActivity(intent);
				ThemeManageActivity.sRefreshFlag = true;
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 桌面本地主题点击事件
	 * */
	protected void deskInstalledClickEvent(String packageName) {
		Intent intent = new Intent(getContext(), ThemeDetailActivity.class);
		intent.putExtra(ThemeConstants.PACKAGE_NAME_EXTRA_KEY, packageName);
		getContext().startActivity(intent);

	}

	/**
	 * 初始化列表
	 */
	public void initThemeListView() {
		if (mListAdapter == null) {
			mListAdapter = new ThemeListAdapter(getContext());
		}
		if (mItemClickListener == null) {
			initItemClickListener();
		}
		mListAdapter.setmItemClickListener(mItemClickListener);
		setAdapter(mListAdapter);
//		setDivider(mContext.getResources().getDrawable(R.drawable.theme_list_divider));
	}

	/**
	 * 判断本机安装的go锁屏软件版本是否过低
	 * 
	 */
	private boolean isGoLockerVersionLow() {
		PackageManager manager = getContext().getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(LauncherEnv.Plugin.LOCKER_PACKAGE, 0);
			int appVersionCode = info.versionCode; // 版本号
			if (appVersionCode < ThemeConstants.REQUEST_LOCKER_VERSION_NUM) {
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 判断本机安装的go锁屏软件版本是否过低
	 * 
	 */
	private boolean isGoLockerSuppertGoTheme() {
		PackageManager manager = getContext().getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(LauncherEnv.Plugin.LOCKER_PACKAGE, 0);
			int appVersionCode = info.versionCode; // 版本号
			if (appVersionCode < ThemeConstants.REQUEST_LOCKER_GO_THEME_VERSION_NUM) {
				return false;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return true;
	}

	/**
	 * 刷新界面
	 * 
	 */

	public void refreshView() {
		if (mListAdapter != null) {
			setVisibility(View.GONE);
			mListAdapter.notifyDataSetChanged();
			setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 清理
	 * 
	 */
	public void cleanup() {
		if (mThemeDatas != null) {
			mThemeDatas = null;
		}
		if (mListAdapter != null) {
			setVisibility(View.GONE);
			mListAdapter.clearThemeDatas();
			setVisibility(View.VISIBLE);
		}

	}

	/**
	 * 锁屏版本过低 提示
	 */
	private void showGoLockerUpdateTips(final String mPackageName) {
		AlertDialog.Builder builder = new Builder(getContext());
		builder.setMessage(R.string.locker_low_verson_tips_content);
		builder.setTitle(R.string.locker_low_verson_tips_title);
		builder.setPositiveButton(R.string.locker_low_verson_update,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!AppUtils.gotoMarket(mContext, LauncherEnv.Market.APP_DETAIL
								+ LauncherEnv.Plugin.LOCKER_PACKAGE)) {
							AppUtils.gotoBrowser(mContext, LauncherEnv.Url.GOLOCKER_DOWNLOAD_URL);
						}

					}

				});

		builder.setNegativeButton(R.string.locker_low_verson_use,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String oldPackageName = ThemeManager.getInstance(mContext)
								.getCurLockerTheme();
						new GoLockerThemeManager(mContext).changeLockTheme(mPackageName);
						mListAdapter.updateInstalledLockerList(mPackageName, oldPackageName);
						if (mThemeDatas != null && mThemeDatas.size() > 0) {
							setVisibility(View.GONE);
							mListAdapter.setThemeDatas(mThemeDatas, mBannerBean);
							setVisibility(View.VISIBLE);
						}

					}
				});

		builder.create().show();
	}

	/**
	 * 锁屏版本过低 提示
	 */
	private void updateGoLockerTips() {
		AlertDialog.Builder builder = new Builder(getContext());
		builder.setMessage(R.string.locker_vip_low_verson_tips_content);
		builder.setTitle(R.string.locker_low_verson_tips_title);
		builder.setPositiveButton(R.string.locker_low_verson_update,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!AppUtils.gotoMarket(mContext, LauncherEnv.Market.APP_DETAIL
								+ LauncherEnv.Plugin.LOCKER_PACKAGE)) {
							AppUtils.gotoBrowser(mContext, LauncherEnv.Url.GOLOCKER_DOWNLOAD_URL);
						}

					}

				});

		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		builder.create().show();
	}

	/**
	 * 应用锁屏主题后，更改数据状态
	 * 
	 * */
	protected void updateCurLockerTheme(String curPackageName) {
		mThemeDatas = ThemeDataManager.getInstance(mContext).getThemeData(
				ThemeConstants.LOCKER_INSTALLED_THEME_ID);
		if (mThemeDatas == null || mThemeDatas.size() <= 0 || curPackageName == null) {
			return;
		}
		for (ThemeInfoBean bean : mThemeDatas) {
			if (curPackageName.equals(bean.getPackageName())) {
				bean.setIsCurTheme(true);
			} else {
				bean.setIsCurTheme(false);
			}
		}

	}

	/**
	 * 应用锁屏主题后，更改数据状态
	 * 
	 * */
//	protected void updateInstalledLockerList(String packageName, String oldPackageName) {
//		mThemeDatas = ThemeDataManager.getInstance(mContext).getThemeData(
//				ThemeConstants.LOCKER_INSTALLED_THEME_ID);
//		if (mThemeDatas == null || mThemeDatas.size() <= 0 || oldPackageName == null) {
//			return;
//		}
//		for (ThemeInfoBean bean : mThemeDatas) {
//			if (oldPackageName.equals(bean.getPackageName())) {
//				bean.setIsCurTheme(false);
//			}
//			if (packageName.equals(bean.getPackageName())) {
//				bean.setIsCurTheme(true);
//			}
//		}
//
//	}

	/**
	 * 横竖屏切换
	 * 
	 */
	public void changeOrientation(ArrayList<ThemeInfoBean> themeInfoBeans, ThemeBannerBean banner) {
		this.mThemeDatas = themeInfoBeans;
		mBannerBean = banner;
		setVisibility(View.GONE);
		mListAdapter.changeOrientation();
		mListAdapter.setThemeDatas(mThemeDatas, mBannerBean);
		setVisibility(View.VISIBLE);
	}

	public void onDestroy() {
		mListAdapter.recyle();
	}
}
