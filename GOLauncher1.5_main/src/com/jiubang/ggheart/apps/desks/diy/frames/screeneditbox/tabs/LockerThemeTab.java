package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.DrawableCacheManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.IDrawableLoader;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditTabView;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.components.DeskBuilder;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.theme.GoLockerThemeManager;
import com.jiubang.ggheart.data.theme.LockerManager;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * Go锁屏TAB区域
 * 
 * @author dingzijian
 * 
 */
public class LockerThemeTab extends BaseTab implements IDrawableLoader {
	private static final String STATISTICS_GOLOCKER_URI = "content://com.jiubang.goscreenlock/theme"; // 保存网络请求成功使用时间所使用的KEY
	private static final String STATISTICS_GOLOCKER_USING_THEME_PKG_NAME = "usingThemePackageName"; // 保存网络请求成功使用时间所使用的KEY

	private static final String RANDOM_PKGNAME = "com.jiubang.goscreenlock.theme.random";
	private static final String DEFAULT_PKGNAME = "com.jiubang.goscreenlock.theme.classic.default"; // 设置默认主题后的包名
	public static boolean sCHANG_LOCKER_THEME = false;

	private GoLockerThemeManager mLockerThemeManager;
	private String mCurLockerTheme = ""; // 当前已应用的锁屏主题
	private ThemeManager mThemeManager;
	private ArrayList<LockerDataBean> mLockDataListBean; // 当前锁屏数据bean
	private View mSelectView; // 显示当前锁屏的view
	private boolean mRandomLocker = false; // 当前go锁屏是否含有随机锁屏
	private DrawableCacheManager mCacheManager;
	private GoProgressBar mGoProgressBar;
	private String mGoLockerPkgName; //当前安装的go锁屏的包名

	public LockerThemeTab(Context context, String tag, int level) {
		super(context, tag, level);
		mMutex = new Object();
		mCacheManager = DrawableCacheManager.getInstance();
		mIsNeedAsyncLoadData = true;
		mGoLockerPkgName = AppUtils.getCurLockerPkgName(mContext);
		mThemeManager = ThemeManager.getInstance(mContext);
		initListByLoading();
	}

	private void initDataBean() {
		// 获取已选主题
		mCurLockerTheme = mThemeManager.getCurLockerTheme();
		mLockDataListBean = new ArrayList<LockerThemeTab.LockerDataBean>();
		// 默认锁屏
		if (AppUtils.isGoLockerExist(mContext)) {
			LockerDataBean defaultBean = new LockerDataBean();
			defaultBean.mAppName = mContext.getString(R.string.locker_default_theme);
			defaultBean.mPkgName = mGoLockerPkgName;
			defaultBean.mPositon = 0;
			mLockDataListBean.add(defaultBean);
			mLockerThemeManager = new GoLockerThemeManager(mContext);
			// 获取随机锁屏
			BitmapDrawable icon = mLockerThemeManager.getRandomPreView(defaultBean.mPkgName);
			defaultBean = null;

			if (icon != null) {
				LockerDataBean random_Bean = new LockerDataBean();
				random_Bean.mAppName = mContext.getString(R.string.random_locker_theme); // 随机锁屏
				random_Bean.mPkgName = RANDOM_PKGNAME;
				random_Bean.mPositon = 1;
				mLockDataListBean.add(random_Bean);
				random_Bean = null;
				mRandomLocker = true;
			}

			// 已安装
			Map<CharSequence, CharSequence> mInstalledThemePackage = mLockerThemeManager
					.queryInstalledTheme();
			LockerDataBean bean = null;
			int startPostion = 1;
			Iterator<CharSequence> iterator = mInstalledThemePackage.keySet().iterator();
			for (; iterator.hasNext();) {
				CharSequence packageName = iterator.next();
				bean = new LockerDataBean();
				bean.mAppName = packageName.toString();
				bean.mPkgName = packageName.toString();
				bean.mAppName = mInstalledThemePackage.get(packageName).toString();
				bean.mPositon = startPostion;
				++startPostion;
				mLockDataListBean.add(bean);
			}
		}

		// 获取更多
		LockerDataBean downloadBean = new LockerDataBean();
		downloadBean.mAppName = mContext.getString(R.string.themestore_mainlistview_btmbutton);
		downloadBean.mPositon = mLockDataListBean.size();
		mLockDataListBean.add(downloadBean);
	}

	private static void gotoDownloadGolocker(final Context context) {
		DeskBuilder builder = new DeskBuilder(context);
		builder.setTitle(context.getString(R.string.locker_tip_title));
		builder.setMessage(context.getString(R.string.locker_tip_message));
		builder.setPositiveButton(context.getString(R.string.ok), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (Statistics.getUid(context).equals("311")) {
					// 如果是311渠道直接下载,不跳google play
					AppUtils.gotoBrowser(context, LauncherEnv.Url.GOLOCKER_DOWNLOAD_URL);
					dialog.cancel();
				} else if (!AppUtils.gotoMarket(context, LauncherEnv.Market.APP_DETAIL
						+ LauncherEnv.Plugin.LOCKER_PACKAGE)) {
					AppUtils.gotoBrowser(context, LauncherEnv.Url.GOLOCKER_DOWNLOAD_URL);
					dialog.cancel();
				}
			}
		});
		builder.setNegativeButton(context.getString(R.string.cancel), null);
		builder.create().show();
	}

	@Override
	public ArrayList<Object> getDtataList() {
		return null;
	}

	@Override
	public int getItemCount() {
		return mLockDataListBean != null ? mLockDataListBean.size() : 0;
	}
	/**
	 * 
	 * <br>类描述:锁屏bean
	 */
	public class LockerDataBean {
		public String mAppName = null;
		public String mPkgName = null;
		public int mPositon = 0;
	}

	/**
	 * 是否安装go锁屏
	 * */
	private boolean isInstallGoLocker() {
		return AppUtils.isGoLockerExist(mContext);
	}

	@Override
	public View getView(int position) {
		if (position == 0) {
			if (!isInstallGoLocker()) {
				Message msg = new Message();
				msg.what = LIST_INIT_FAIL;
				mHandler.sendMessage(msg);
			}
		}
		View view = mInflater.inflate(R.layout.screen_edit_item_theme, null);
		ImageView image_select = (ImageView) view.findViewById(R.id.thumb_select);
		TextView mText = (TextView) view.findViewById(R.id.title);
		if (position >= mLockDataListBean.size()) {
			return null;
		}

		LockerDataBean dataBean = mLockDataListBean.get(position);
		if (dataBean == null) {
			return null;
		}
		mText.setText(dataBean.mAppName);
		if (!(mLockDataListBean.size() - 1 == position)) {
			String pkgName = dataBean.mPkgName;
			if (null != pkgName) {
				if (mCurLockerTheme != null) {
					if (pkgName.equals(mCurLockerTheme) || pkgName.equals(mGoLockerPkgName)
							&& mCurLockerTheme.equals(DEFAULT_PKGNAME)) {
						// 选中打钩
						image_select.setVisibility(View.VISIBLE);
						mSelectView = view;
					}
				}
			}
		}
		view.setTag(dataBean);
		return view;
	}

	@Override
	public Drawable loadDrawable(int position, Object arg) {
		Drawable icon = null;
		try {
			LockerDataBean dataBean = mLockDataListBean.get(position);
			if (dataBean == null) {
				return null;
			}
			if (mLockDataListBean.size() - 1 == position) {
				// 下载
				icon = getFitIcon(mContext.getResources().getDrawable(R.drawable.gostore_4_def3),
						false);
				if (icon == null) {
					icon = mContext.getResources().getDrawable(R.drawable.gostore_4_def3);
				}
				return icon;
			} else {
				String pkgName = dataBean.mPkgName;
				icon = mCacheManager.getDrawableFromCache(DrawableCacheManager.CACHE_LOCKERTHEMETAB
						+ pkgName);
				if (icon != null) {
					return icon;
				}
				if (null != pkgName) {
					if (position == 1 && mRandomLocker) {
						icon = mLockerThemeManager.getRandomPreView(mGoLockerPkgName);
					} else {
						icon = mLockerThemeManager.getPreViewForScreenEdit(pkgName);
					}
					if (icon == null) {
						return icon;
					}
					icon = getFitIcon(icon, true);
					mCacheManager.saveToCache(DrawableCacheManager.CACHE_LOCKERTHEMETAB + pkgName,
							icon);
					return icon;

				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return icon;
	}

	@Override
	public void displayResult(View view, Drawable drawable) {
		if (drawable != null) {
			ImageView thumb = (ImageView) view.findViewById(R.id.thumb);
			thumb.setImageDrawable(drawable);
		}
	}

	@Override
	public void onClick(View v) {
		LockerDataBean dataBean = (LockerDataBean) v.getTag();
		if (mLockDataListBean.size() - 1 == dataBean.mPositon) {
			// 下载
			boolean isAppExist = AppUtils.isGoLockerExist(mContext);
			if (isAppExist) {
				//				Intent it = new Intent(mContext, GoStore.class);
				//				Bundle b = new Bundle();
				//				b.putString("sort", "2"); // 2为跳到锁屏部分
				//				it.putExtras(b);
				//				mContext.startActivity(it);
				AppsManagementActivity.startAppCenter(mContext,
						MainViewGroup.ACCESS_FOR_APPCENTER_LOCKER, false);
			} else {
				AppsDetail.gotoDetailDirectly(mContext, AppsDetail.START_TYPE_APPRECOMMENDED,
						LauncherEnv.GO_LOCK_PACKAGE_NAME);
				//				GoStoreOperatorUtil.gotoStoreDetailDirectly(mContext,
				//						LauncherEnv.GO_LOCK_PACKAGE_NAME);
			}
		} else {
			// 已安装locker主题列表
			String pkgName = dataBean.mPkgName;
			boolean isZip = LockerManager.getInstance(mContext).isZipTheme(mContext, pkgName);
			if (isZip && !isGoLockerSuppertGoTheme()) {
				updateGoLockerTips();
				return;
			}
			if (pkgName != null && pkgName.equals(mCurLockerTheme)) {
				return;
			}

			if (mSelectView != null) {
				ImageView image = (ImageView) mSelectView.findViewById(R.id.thumb_select);
				image.setVisibility(View.GONE);
				ImageView image2 = (ImageView) v.findViewById(R.id.thumb_select);
				image2.setVisibility(View.VISIBLE);
				mSelectView = v;
			}
			mLockerThemeManager.changeLockTheme(pkgName);
			mCurLockerTheme = pkgName;
		}

	}

	/**
	 * 锁屏版本过低 提示
	 */
	private void updateGoLockerTips() {
		AlertDialog.Builder builder = new Builder(mContext);
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
	 * 判断本机安装的go锁屏软件版本是否过低
	 * 
	 */
	private boolean isGoLockerSuppertGoTheme() {
		PackageManager manager = mContext.getPackageManager();
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

	@Override
	public void resetData() {
		// initDataBean();
	}

	@Override
	public void handleAppChanged(int msgId, String pkgName) {
		super.handleAppChanged(msgId, pkgName);
		if (pkgName != null && pkgName.startsWith(ICustomAction.ACTION_GOLOCK_THEME)) {
			// 验证是否是主题
			initDataBean();
			// 刷新
			if (mTabActionListener != null) {
				mTabActionListener.onRefreshTab(BaseTab.TAB_LOCKER, 0);
			}
		}
	}

	@Override
	public void clearData() {
		super.clearData();
		if (mLockDataListBean != null) {
			mLockDataListBean.clear();
			mLockDataListBean = null;
		}
		if (mGoProgressBar != null) {
			mGoProgressBar = null;
		}
		mLockerThemeManager = null;
	}

	private Object mMutex;
	private final static int LIST_INIT_OK = 1000;
	private final static int LIST_INIT_FAIL = 2000;

	private void initListByLoading() {
		// 显示提示框
		showProgressDialog();
		new Thread(ThreadName.SCREEN_EDIT_LOCKERTAB) {
			@Override
			public void run() {
				synchronized (mMutex) {
					initDataBean();
					Message msg = new Message();
					msg.what = LIST_INIT_OK;
					mHandler.sendMessage(msg);
				}
			};
		}.start();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case LIST_INIT_OK :
					if (mTabActionListener != null) {
						mTabActionListener.onRefreshTab(BaseTab.TAB_LOCKER, 0);
					}
					dismissProgressDialog();
					break;
				case LIST_INIT_FAIL :
					dismissProgressDialog();
					gotoDownloadGolocker(mContext);
				default :
					break;
			}
		};
	};

	private void showProgressDialog() {
		ScreenEditBoxFrame screenEditBoxFrame = (ScreenEditBoxFrame) GoLauncher
				.getFrame(IDiyFrameIds.SCREEN_EDIT_BOX_FRAME);
		if (screenEditBoxFrame != null) {
			ScreenEditTabView mLayOutView = screenEditBoxFrame.getTabView();
			mGoProgressBar = (GoProgressBar) mLayOutView.findViewById(R.id.edit_tab_progress);
			if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.INVISIBLE) {
				mGoProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void dismissProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.VISIBLE) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}

}
