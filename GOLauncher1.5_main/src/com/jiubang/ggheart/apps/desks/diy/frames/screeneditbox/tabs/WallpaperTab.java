package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;
import java.util.List;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.WallpaperControler;
import com.jiubang.ggheart.apps.desks.diy.WallpaperDensityUtil;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.bean.WallpaperItemInfo;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.CheckApplication;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * @author licanhui 壁纸TAB
 */
public class WallpaperTab extends BaseTab {
	private static final long CLICK_TIME = 500;
	private long mLastTime; // 上次的点击时间
	private List<WallpaperItemInfo> mList; // 所有一级数据及图标

	private static final String LIVE_WALLPAPER_CLASS_NAME = "com.android.wallpaper.livepicker.LiveWallpaperListActivity";
	WallPaperTabManager mWallPaperTabManager;
	WallpaperItemInfo mDto;

	public WallpaperTab(Context context, String tag, int level) {
		super(context, tag, level);

		mWallPaperTabManager = new WallPaperTabManager(context, this);
		mList = mWallPaperTabManager.findAll();
		mLastTime = System.currentTimeMillis();
	}

	@Override
	public ArrayList<Object> getDtataList() {
		return null;
	}

	@Override
	public int getItemCount() {
		return mList.size();
	}

	@Override
	public View getView(int position) {
		View view = mInflater.inflate(R.layout.screen_edit_item, null);
		ImageView image = (ImageView) view.findViewById(R.id.thumb);
		TextView mText = (TextView) view.findViewById(R.id.title);

		WallpaperItemInfo dto = mList.get(position);
		if (position == 0) {
			image.setImageDrawable(getFitIcon(dto.getAppIcon(), true));
//			mText.setText(dto.getmAppLabel());
			GoSettingControler controler = GOLauncherApp.getSettingControler();
			ScreenSettingInfo screenInfo = controler.getScreenSettingInfo();
			int titleRes = screenInfo.mWallpaperScroll
					? R.string.guide_wallpapersetting_defaultstyle
					: R.string.guide_wallpapersetting_verticalstyle;
			mText.setText(titleRes);
		} else if (position == mList.size()) {
			Drawable icon = getFitIcon(dto.getAppIcon(), false);
			image.setImageDrawable(icon == null ? dto.getAppIcon() : icon);
			mText.setText(dto.getmAppLabel());
			view.setTag(dto);
		} else {
			image.setImageDrawable(dto.getAppIcon());
			mText.setText(dto.getmAppLabel());
			view.setTag(dto);
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		long current = System.currentTimeMillis();
		if (current - mLastTime < CLICK_TIME) {
			return;
		}
		mLastTime = current;
		if (v.getTag() == null) {
			changeCutMode(v);
//			mContext.startActivity(new Intent(mContext, WallpaperCutModeSettting.class));
			//用户行为统计
			StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
					StatisticsData.USER_ACTION_TWELVE, IPreferencesIds.DESK_ACTION_DATA);
			return;
		}
		WallpaperItemInfo dto = (WallpaperItemInfo) v.getTag();
		if (LauncherEnv.PACKAGE_NAME.equals(dto.getPkgName())) {
			if (mTabActionListener != null) {
				mTabActionListener.setCurrentTab(BaseTab.TAB_GOWALLPAPER);
				mTabActionListener.onRefreshTopBack(BaseTab.TAB_GOWALLPAPER);
			}
			//用户行为统计
			StatisticsData.countUserActionData(StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
					StatisticsData.USER_ACTION_FOUTEEN, IPreferencesIds.DESK_ACTION_DATA);
		} else {
			
			if (dto.getPkgName() != null
					&& dto.getPkgName().equals(
							LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
				// TODO 这里是否需要用户行为统计？

				// 判断设备是否支持动态壁纸
				if (!isSupportedLiveWallpaper()) {
					// 不支持动态壁纸
					Toast.makeText(
							mContext,
							mContext.getText(R.string.not_support_live_wallpaper_toast),
							Toast.LENGTH_LONG).show();
				} else {
					if (!AppUtils.isAppExist(mContext,
							LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME)) {
						
						// 这里判断有没有安装多屏多壁纸，没有则跳转下载
						gotoDownloadMultipleWallpaper();
						// 先让桌面退出添加界面，恢复正常
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_SMALL_TO_NORMAL, -1, null,
								null);

					} else {

//						if (!GOLauncherApp.getSettingControler()
//								.getScreenSettingInfo().mWallpaperScroll) {
							// 如果壁纸设置为竖屏模式，则弹Toast提示不可滚动
//							Toast.makeText(
//									mContext,
//									mContext.getText(R.string.go_multiple_wallpaper_toast),
//									Toast.LENGTH_LONG).show();
//						}
						Intent intent = dto.getIntent();
						// 把壁纸模式传递给多屏多壁纸
						intent.putExtra("wallpaper_scroll", GOLauncherApp.getSettingControler().getScreenSettingInfo().mWallpaperScroll);

						try {
							mContext.startActivity(intent);
							WallpaperControler.setWallpaperSetting(true);
						} catch (Exception e) {
						}
					}
				}
				
			} else {

				Intent intent = dto.getIntent();
				// 统计动态壁纸
				if (intent.getComponent().getClassName()
						.equals(LIVE_WALLPAPER_CLASS_NAME)) {
					// 用户行为统计
					StatisticsData.countUserActionData(
							StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
							StatisticsData.USER_ACTION_THIRTEEN,
							IPreferencesIds.DESK_ACTION_DATA);
				} else {
					// 用户行为统计
					StatisticsData.countUserActionData(
							StatisticsData.DESK_ACTION_ID_SCREEN_EDIT,
							StatisticsData.USER_ACTION_FIFTEEN,
							IPreferencesIds.DESK_ACTION_DATA);
				}
				try {
					mContext.startActivity(intent);
					WallpaperControler.setWallpaperSetting(true);
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public void clearData() {
		super.clearData();
		if (mWallPaperTabManager != null) {
			mWallPaperTabManager.clear();
			mWallPaperTabManager = null;
		}
		if (mList != null) {
			mList.clear();
			mList = null;
		}
	}

	@Override
	public void resetData() {
		if (mList != null) {
			mList.clear();
			mList = mWallPaperTabManager.findAll();
			// 刷新
			if (mTabActionListener != null) {
				mTabActionListener.onRefreshTab(BaseTab.TAB_WALLPAPER, 0);
			}
		}
	}

	// 以后做动态壁纸的设置用，勿删
	// ResolveInfo settingInfo = null;
	// WallpaperManager wm = (WallpaperManager)
	// mContext.getSystemService(Context.WALLPAPER_SERVICE);
	// WallpaperInfo wi = wm.getWallpaperInfo();
	//
	// if (wi != null && wi.getSettingsActivity() != null)
	// {
	// LabeledIntent li = new
	// LabeledIntent(mContext.getPackageName(),R.string.configure_wallpaper, 0);
	// li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
	// settingInfo = pm.resolveActivity(li, 0);
	// }

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * 跳转下载多屏多壁纸
	 */
	private void gotoDownloadMultipleWallpaper() {
		// String packageName = LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME;
		// TODO 由于Google电子市场还没有多屏多壁纸的包，这里用通讯统计的包测试，等有多屏多壁纸的包再修改
		String packageName = LauncherEnv.Plugin.MULTIPLEWALLPAPER_PKG_NAME;
		String url = LauncherEnv.Url.MULTIPLEWALLPAPER_URL;
		String linkArray[] = { packageName, url };
		String title = mContext
				.getString(R.string.go_multiple_wallpaper_title);
		String content = mContext
				.getString(R.string.go_multiple_wallpaper_tip_content);
		boolean isCnUser = Machine.isCnUser(mContext);

		CheckApplication.downloadAppFromMarketFTPGostore(mContext, content,
				linkArray, LauncherEnv.MULTIPLEWALLPAPER_GOOGLE_REFERRAL_LINK, title,
				System.currentTimeMillis(), isCnUser,
				CheckApplication.FROM_GO_FOLDER);
	}
	
	public void changeCutMode(View v) {
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.REMOVE_FRAME,
				IDiyFrameIds.GUIDE_GL_FRAME, null, null);
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		ScreenSettingInfo screenInfo = controler.getScreenSettingInfo();
		screenInfo.mWallpaperScroll = !screenInfo.mWallpaperScroll;
		controler.updateScreenSettingInfo(screenInfo);
		if (v != null) {
			TextView title = (TextView) v.findViewById(R.id.title);
			int titleRes = screenInfo.mWallpaperScroll
					? R.string.guide_wallpapersetting_defaultstyle
					: R.string.guide_wallpapersetting_verticalstyle;
			int toastRes = screenInfo.mWallpaperScroll
					? R.string.guide_wallpapersetting_scrollable
					: R.string.guide_wallpapersetting_locked;
			Toast.makeText(mContext, toastRes, Toast.LENGTH_SHORT).show();
			title.setText(titleRes);
		}
		WallpaperDensityUtil.setWallpaperDimension(GoLauncher.getContext());
		// 刷新壁纸列表
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
				IDiyMsgIds.SCREEN_EDIT_UPDATE_WALLPAPER_ITEMS, 0, null, null);
	}
	/**
	 * 判断是否支持动态壁纸
	 * 
	 * @return
	 */
	private boolean isSupportedLiveWallpaper() {
		Intent i = new Intent();
		i.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
		return AppUtils.isAppExist(mContext, i);
	}
}
