package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.DrawableCacheManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.IDrawableLoader;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditBoxFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.ScreenEditTabView;
import com.jiubang.ggheart.apps.desks.diy.themescan.NewThemeAdapter;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemePurchaseManager;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.broadcastReceiver.MyThemeReceiver;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 桌面主题TAB区域
 * 
 * @author yangbing
 * 
 */
public class DesktopThemeTab extends BaseTab implements IDrawableLoader {
//	private static final String ACTION_SEND_TO_GOLOCK = "com.gau.go.launcherex_action_send_to_golock";
	private static final String NEW_THEME_KEY = "newtheme";
	private ArrayList<ThemeInfoBean> mThemeInfoDatas; // 主题包数据
	private String mCurThemePackageName; // 当前应用主题包名称
	private AlertDialog mDialog = null; // 大主题对话框
	private String mGolauncher;
	private String mGowidget;
	private String mGolock;
	private int mOldselect;
	private View mSelectView; // 当前主题的view
	private View mLastSelectView; // 上一个选中的主题的view
	private DrawableCacheManager mCacheManager;
	private GoProgressBar mGoProgressBar;
	
	public DesktopThemeTab(Context context, String tag, int level) {
		super(context, tag, level);
		mThemeInfoDatas = new ArrayList<ThemeInfoBean>();
		mMutex = new Object();
		mCacheManager = DrawableCacheManager.getInstance();
		mIsNeedAsyncLoadData = true;
		initListByLoading();
	}

	private void initData() {
		mThemeInfoDatas.clear();
		mCurThemePackageName = ThemeManager.getInstance(mContext).getCurThemePackage();
		mThemeInfoDatas = ThemeManager.getInstance(mContext).getAllInstalledThemeInfos();

	}

	@Override
	public ArrayList<Object> getDtataList() {
		// for
		return null;
	}

	@Override
	public int getItemCount() {
		if (mThemeInfoDatas != null) {
			return mThemeInfoDatas.size() + 1;
		}
		return 0;
	}

	// 根据当前点击的item，改变选中状态
	public void changeSelectedView(View v) {
		if (mSelectView != null && v != null) {
			ImageView image = (ImageView) mSelectView.findViewById(R.id.thumb_select);
			image.setVisibility(View.GONE);
			ImageView image2 = (ImageView) v.findViewById(R.id.thumb_select);
			image2.setVisibility(View.VISIBLE);
			mLastSelectView = mSelectView;
			mSelectView = v;
		}
	}

	@Override
	public View getView(int position) {
		View view = mInflater.inflate(R.layout.screen_edit_item_theme, null);
		ImageView image_select = (ImageView) view.findViewById(R.id.thumb_select);
		TextView mText = (TextView) view.findViewById(R.id.title);
		if (mThemeInfoDatas != null && mThemeInfoDatas.size() != 0) {
			if (mThemeInfoDatas.size() == position) {
				mText.setText(mContext.getString(R.string.themestore_mainlistview_btmbutton));
				view.setTag("Download");
				return view;
			}
			ThemeInfoBean themeInfoBean = mThemeInfoDatas.get(position);
			if (themeInfoBean.getPackageName().equals(mCurThemePackageName)) {
				image_select.setVisibility(View.VISIBLE);
				mSelectView = view;
			}
			String name = themeInfoBean.getThemeName();
			mText.setText(name);
			view.setTag(themeInfoBean);
		} else {
			return null;
		}
		return view;
	}

	@Override
	public Drawable loadDrawable(int position, Object arg) {
		Drawable icon = null;
		try {
			if (mThemeInfoDatas != null && mThemeInfoDatas.size() != 0) {
				ThemeInfoBean info = null;
				if (position != mThemeInfoDatas.size()) {
					info = mThemeInfoDatas.get(position);
					icon = mCacheManager
							.getDrawableFromCache(DrawableCacheManager.CACHE_DESKTOPTHEMETAB
									+ info.getPackageName());
					if (icon != null) {
						return icon;
					}
				}
				if (mThemeInfoDatas.size() == position) {
					icon = getFitIcon(mContext.getResources()
							.getDrawable(R.drawable.gostore_4_def3), false);
					if (icon == null) {
						return mContext.getResources().getDrawable(R.drawable.gostore_4_def3);
					}
					return icon;
				} else {
					icon = cutThemeIcon(info);
					mCacheManager.saveToCache(
							DrawableCacheManager.CACHE_DESKTOPTHEMETAB + info.getPackageName(),
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
			ImageView thumb = (ImageView) view;
			thumb.setImageDrawable(drawable);
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getTag() instanceof ThemeInfoBean) {
			ThemeInfoBean theme = (ThemeInfoBean) v.getTag();
			String type = theme.getThemeType();
			if (null != type && type.equals(ThemeInfoBean.THEMETYPE_GETJAR) && !theme.isZipTheme()) {
				try {
					// getjar主题
					Intent intent = new Intent();
					intent = mContext.getPackageManager().getLaunchIntentForPackage(
							theme.getPackageName());
					int level = ThemePurchaseManager.getCustomerLevel(mContext);
					if (level != ThemeConstants.CUSTOMER_LEVEL0) {

						intent.putExtra("viplevel", level);
					}
					mContext.startActivity(intent);
					/*if (level != ThemeConstants.CUSTOMER_LEVEL0
							|| ThemeManager.canBeUsedTheme(mContext,
									theme.getPackageName())) {
						changeSelectedView(v);
					}*/
				} catch (Exception e) {
				}
			} else {
				// 一般主题
				applyTheme(theme, v);
			}

		} else {
			String tag = (String) v.getTag();
			if ("Download".equals(tag)) {
				// 下载主题
//				Intent intent = new Intent();
//				intent.setClass(mContext, GoStore.class);
//				Bundle b = new Bundle();
//				b.putString("sort", "1"); // 1为跳到主题部分
//				intent.putExtras(b);
//				mContext.startActivity(intent);
				AppsManagementActivity.startAppCenter(mContext,
						MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
			}
		}

	}

	@Override
	public void clearData() {
		if (mThemeInfoDatas != null) {
			mThemeInfoDatas.clear();
			mThemeInfoDatas = null;
		}
		if (mGoProgressBar != null) {
			mGoProgressBar = null;
		}
		mSelectView = null;
		mLastSelectView = null;
		super.clearData();

	}

	/**
	 * 应用主题
	 */
	private void applyTheme(ThemeInfoBean themeInfoBean, View view) {
		if (themeInfoBean == null || mThemeInfoDatas == null) {
			return;
		}
		if (!themeInfoBean.isNewTheme()) {
			// 不是大主题
			mOldselect = mThemeInfoDatas.indexOf(themeInfoBean);
			String pkgName = themeInfoBean.getPackageName();
			mCurThemePackageName = pkgName;
			// 单独发动态广播,防止广播接收延迟
			Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST_SCREENEDIT);
			intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING, MyThemeReceiver.CHANGE_THEME);
			intent.putExtra(MyThemeReceiver.PKGNAME_STRING, themeInfoBean.getPackageName());
			mContext.sendBroadcast(intent);
			changeSelectedView(view);
		} else {
			// 大主题
			Resources resources = mContext.getResources();
			mGolauncher = resources.getString(R.string.new_theme_golauncher);
			mGowidget = resources.getString(R.string.new_theme_gowidget);
			mGolock = resources.getString(R.string.new_theme_golock);
			showDialog(themeInfoBean, view);
		}
	}

	private void showDialog(final ThemeInfoBean mInfoBean, final View view) {
		final NewThemeAdapter myAdapter = new NewThemeAdapter(mContext, mInfoBean, mGolauncher,
				mGowidget, mGolock);
		myAdapter.filterNotExistTheme();
		mDialog = new AlertDialog.Builder(mContext).setTitle(R.string.new_theme_title)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mInfoBean != null && mContext != null) {
							// 存在桌面主题且被选中
							if (mInfoBean.ismExistGolauncher()
									&& myAdapter.getmCheckBoxState().get(mGolauncher)) {
								mOldselect = mThemeInfoDatas.indexOf(mInfoBean) + 1;
								String pkgName = mInfoBean.getPackageName();
								mCurThemePackageName = pkgName;
								Intent intent = new Intent(ICustomAction.ACTION_THEME_BROADCAST);
								intent.putExtra(MyThemeReceiver.ACTION_TYPE_STRING,
										MyThemeReceiver.CHANGE_THEME);
								intent.putExtra(MyThemeReceiver.PKGNAME_STRING,
										mInfoBean.getPackageName());
								mContext.sendBroadcast(intent);
								changeSelectedView(view);
							} else if (mInfoBean.getGoWidgetPkgName() != null
									&& myAdapter.getmCheckBoxState().get(mGowidget)) {
								// 存在widget主题且被选中
								mOldselect = mThemeInfoDatas.indexOf(mInfoBean) + 1;
								String pkgName = mInfoBean.getPackageName();
								mCurThemePackageName = pkgName;
								Intent intent_GW = new Intent(
										ICustomAction.ACTION_CHANGE_WIDGETS_THEME);
								intent_GW.putExtra(ICustomAction.WIDGET_THEME_KEY,
										mInfoBean.getPackageName());
								mContext.sendBroadcast(intent_GW);
							} else
							// 存在GO锁屏主题且被选中
							if (mInfoBean.ismExistGolock()
									&& myAdapter.getmCheckBoxState().get(mGolock)) {
								if (AppUtils.isGoLockerExist(mContext)) {
									try {
										String newThemePkgName = mInfoBean.getPackageName();
										if (newThemePkgName != null) {
											Intent intent_GL = new Intent(ICustomAction.ACTION_SEND_TO_GOLOCK);
											intent_GL.putExtra(NEW_THEME_KEY, newThemePkgName);
											mContext.sendBroadcast(intent_GL);
										}
									} catch (Exception e) {
									}
								}
								return;
							} else {
								dialog.dismiss();
								return;
							}
							// 刷新
							// mTabActionListener.onRefreshTab(BaseTab.TAB_THEME,0);
						}

					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).setAdapter(myAdapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).create();
		mDialog.show();
	}

	public void dismissDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	/**
	 * 切取主题小图标
	 * 
	 * @return
	 */
	private Drawable cutThemeIcon(ThemeInfoBean themeInfoBean) {
		Drawable drawable;
		try {
			if (themeInfoBean.getPreViewDrawableNames().size() == 0) {
				// 处理没有预览图的主题
				drawable = mContext.getResources().getDrawable(R.drawable.screen_edit_icon_body);
				return getFitIcon(drawable, true);
			}
			drawable = ImageExplorer.getInstance(mContext).getDrawable(
					themeInfoBean.getPackageName(), themeInfoBean.getPreViewDrawableNames().get(0));
			return getFitIcon(drawable, true);
		} catch (Throwable e) {
		}
		return null;
	}

	@Override
	public void resetData() {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleAppChanged(int msgId, String pkgName) {
		super.handleAppChanged(msgId, pkgName);
		// resetData();
		if (pkgName != null && pkgName.startsWith(ThemeManager.MAIN_THEME_PACKAGE)) {
			initData();
			// 刷新
			if (mTabActionListener != null) {
				mTabActionListener.onRefreshTab(BaseTab.TAB_THEME, 0);
			}

		}

	}

	private Object mMutex;
	private final static int LIST_INIT_OK = 1000;

	private void initListByLoading() {
		// 显示提示框
		showProgressDialog();
		new Thread(ThreadName.SCREEN_EDIT_THEMETAB) {
			@Override
			public void run() {
				// 初始化数据
				synchronized (mMutex) {
					initData();
					// 对外通知
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
					// 刷新
					if (mTabActionListener != null) {
						mTabActionListener.onRefreshTab(BaseTab.TAB_THEME, 0);
					}
					dismissProgressDialog();
					break;

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
			if (mGoProgressBar != null) {
				mGoProgressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void dismissProgressDialog() {
		if (mGoProgressBar != null) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}
	
	//用于带激活码的主题在弹框取消后，选中之前的主题
	public void changeLastSelectView() {
		if (mLastSelectView != null) {
			changeSelectedView(mLastSelectView);
		}
	}
}
