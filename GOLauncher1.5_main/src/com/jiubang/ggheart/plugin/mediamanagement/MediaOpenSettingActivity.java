package com.jiubang.ggheart.plugin.mediamanagement;

import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.file.media.FileEngine;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingBaseActivity;
import com.jiubang.ggheart.apps.desks.Preferences.info.DeskSettingSingleInfo;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemListView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingPageTitleView;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 图片/音乐播放设置
 * 
 * @author yangbing
 * */
public class MediaOpenSettingActivity extends DeskSettingBaseActivity {

	public static final String SETTING_EXTRA_KEY = "setting_type";
//	private static final int SHOW_DIALOG = 1;
	private int mCurrentType;
	private FunAppSetting mFunAppSetting;
	private PackageManager mPackageManager;
	private List<ResolveInfo> mResolveInfos;
	private String[] mOpenAppNames;
	private int mCurrentOpenAppIndex = -1; // 默认打开方式在选择对话框中的顺序号
	private String mCurrentOpenAppName; // 默认打开方式的程序名
//	private SimpleAdapter mAdapter;

	/**
	 * 媒体播放器单选框
	 */
	private DeskSettingItemListView mMediaOpener;
	/**
	 * 顶部栏控件
	 */
	private DeskSettingPageTitleView mTitleView;
	/**
	 * 媒体播放器的图标数组
	 */
	private Drawable[] mOpenAppIcon;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mediamanagement_media_open_setting);
		mCurrentType = getIntent().getIntExtra(SETTING_EXTRA_KEY, 0);
		mFunAppSetting = GoSettingControler.getInstance(this).getFunAppSetting();
		initData();
//		loadAllOPenWays();
//		if (mCurrentType == FileEngine.TYPE_IMAGE) {
//			setTitle(R.string.gallery_settings);
//		} else if (mCurrentType == FileEngine.TYPE_AUDIO) {
//			setTitle(R.string.music_settings);
//		}
//		setAdapter();
//		setListAdapter(mAdapter);
	}
	
	private void initData() {
		loadAllOPenWays();
		mMediaOpener = (DeskSettingItemListView) findViewById(R.id.media_default_opener);
		mMediaOpener.setOnValueChangeListener(this);
		mTitleView = (DeskSettingPageTitleView) findViewById(R.id.media_open_setting_title);
		mTitleView.getBackLayout().setVisibility(View.GONE);
		mTitleView.getTitleTextView().setPadding(
				getResources().getDimensionPixelSize(
						R.dimen.media_open_setting_title_paddingleft), 0, 0, 0);

		DeskSettingSingleInfo settingSingleInfo = mMediaOpener
				.getDeskSettingInfo().getSingleInfo();

		if (settingSingleInfo != null) {
			if (mCurrentType == FileEngine.TYPE_IMAGE) { // 图片设置
				mTitleView.setTitleText(R.string.gallery_settings);
				mMediaOpener.updateSumarryText();
				mMediaOpener.setSummaryText(mCurrentOpenAppName);
				mMediaOpener.setTitleText(R.string.picture_browser);
				settingSingleInfo.setTitle(getResources().getString(
						R.string.picture_browser));
			} else if (mCurrentType == FileEngine.TYPE_AUDIO) { // 音乐设置
				mTitleView.setTitleText(R.string.music_settings);
				mMediaOpener.updateSumarryText();
				mMediaOpener.setSummaryText(mCurrentOpenAppName);
				mMediaOpener.setTitleText(R.string.music_player);
				settingSingleInfo.setTitle(getResources().getString(
						R.string.music_player));
			}
			settingSingleInfo.setEntries(mOpenAppNames);
			settingSingleInfo.setEntryValues(mOpenAppNames);
			settingSingleInfo.setSelectValue(mCurrentOpenAppName);
			settingSingleInfo.setImageDrawable(mOpenAppIcon);
		}
	}

//	private void setAdapter() {
//		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//		Map<String, Object> map = new HashMap<String, Object>();
//		if (mCurrentType == FileEngine.TYPE_IMAGE) {
//			map.put("title", getResources().getString(R.string.picture_browser));
//		} else if (mCurrentType == FileEngine.TYPE_AUDIO) {
//			map.put("title", getResources().getString(R.string.music_player));
//		}
//		map.put("defaultOpen", mCurrentOpenAppName);
//		list.add(map);
//		mAdapter = new SimpleAdapter(this, list, R.layout.app_func_media_open_setting,
//				new String[] { "title", "defaultOpen" }, new int[] { R.id.setting_title,
//						R.id.default_open });
//	}

//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		if (mOpenAppNames == null || mOpenAppNames.length <= 0) {
//			DeskToast.makeText(this, R.string.no_way_to_open_file, Toast.LENGTH_SHORT).show();
//		} else {
//			showDialog(SHOW_DIALOG);
//		}
//	}

//	@Override
//	protected Dialog onCreateDialog(int id) {
//		switch (id) {
//			case SHOW_DIALOG : {
//				return new AlertDialog.Builder(this)
//						.setIcon(android.R.drawable.ic_menu_more)
//						.setTitle(R.string.open_with)
//						.setSingleChoiceItems(mOpenAppNames, mCurrentOpenAppIndex,
//								new DialogInterface.OnClickListener() {
//									@Override
//									public void onClick(DialogInterface dialog, int whichButton) {
//										mCurrentOpenAppName = mOpenAppNames[whichButton];
//										saveDefault();
//										dismissDialog(SHOW_DIALOG);
//										setAdapter();
////										setListAdapter(mAdapter);
//
//									}
//								})
//						.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int whichButton) {
//
//							}
//						}).create();
//			}
//
//			default :
//				break;
//		}
//		return null;
//	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	/**
	 * 获取所有打开方式
	 * */
	public void loadAllOPenWays() {
		String defaultOpen = mFunAppSetting.getMediaOpenWay(mCurrentType);
//		String defaultOpen = null;
//		if (mCurrentType == FileEngine.TYPE_AUDIO) {
//			defaultOpen = MediaManagerFactory.getMediaManager().getMusicDefaultOpenWay();
//		} else if (mCurrentType == FileEngine.TYPE_IMAGE) {
//			defaultOpen = MediaManagerFactory.getMediaManager().getImageDefaultOpenWay();
//		}
		AppFuncAutoFitManager appFuncAutoFitManager = AppFuncAutoFitManager
				.getInstance(GOLauncherApp.getContext());

		// 无默认
		if (MediaManagementOpenChooser.APP_NONE.equals(defaultOpen)) {
			// if (mCurrentType == FileEngine.TYPE_IMAGE) {
			// mCurrentOpenAppName =
			// getResources().getString(R.string.go_picture_browser);
			// } else if (mCurrentType == FileEngine.TYPE_AUDIO) {
			// mCurrentOpenAppName =
			// getResources().getString(R.string.go_music_player);
			// }
			mCurrentOpenAppName = getResources().getString(R.string.open_with_none);
		}
		if (MediaManagementOpenChooser.APP_GO_PIC_VIEWER.equals(defaultOpen)) {
			mCurrentOpenAppName = getResources().getString(R.string.go_picture_browser);
			mCurrentOpenAppIndex = 0;
		}
		if (MediaManagementOpenChooser.APP_GO_MUSIC_PLAYER.equals(defaultOpen)) {
			mCurrentOpenAppName = getResources().getString(R.string.go_music_player);
			mCurrentOpenAppIndex = 0;
		}

		Intent intent = new Intent(Intent.ACTION_VIEW);
		if (mCurrentType == FileEngine.TYPE_IMAGE) {
			intent.setDataAndType(Uri.parse("file://" + MediaOpenSettingConstants.sImagePath),
					"image/*");
		} else if (mCurrentType == FileEngine.TYPE_AUDIO) {
			intent.setDataAndType(Uri.parse("file://" + MediaOpenSettingConstants.sMusicPath),
					"audio/*");
		}
		mPackageManager = getPackageManager();
		mResolveInfos = mPackageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		if (mResolveInfos != null && !mResolveInfos.isEmpty()) {
			int size = mResolveInfos.size();
			Intent in = new Intent(Intent.ACTION_VIEW);
			if ((mCurrentType == FileEngine.TYPE_IMAGE && !appFuncAutoFitManager
					.needHideImageBrowser())
					|| (mCurrentType == FileEngine.TYPE_AUDIO && !appFuncAutoFitManager
							.needHideMusicPlayer())) {
				mOpenAppNames = new String[size + 2];
				mOpenAppIcon = new Drawable[mOpenAppNames.length];
				
				if (mCurrentType == FileEngine.TYPE_IMAGE) {
					// GO图片查看器放在第一个位置
					mOpenAppNames[0] = getResources().getString(
							R.string.go_picture_browser);
					mOpenAppIcon[0] = getResources().getDrawable(
							R.drawable.go_picture_browser_icon);
				} else if (mCurrentType == FileEngine.TYPE_AUDIO) {
					// GO音乐播放器放在第一个位置
					mOpenAppNames[0] = getResources().getString(
							R.string.go_music_player);
					mOpenAppIcon[0] = getResources().getDrawable(
							R.drawable.go_music_player_icon);
				}
				mOpenAppNames[size + 1] = getResources().getString(R.string.open_with_none);
				mOpenAppIcon[size + 1] = getResources().getDrawable(R.drawable.media_open_setting_none_icon);
				for (int i = 0; i < size; i++) {
					mOpenAppNames[i + 1] = mResolveInfos.get(i).activityInfo.loadLabel(
							mPackageManager).toString();
					in.setClassName(mResolveInfos.get(i).activityInfo.packageName,
							mResolveInfos.get(i).activityInfo.name);
					if (in.toURI().equals(defaultOpen)) {
						mCurrentOpenAppIndex = i + 1;
						mCurrentOpenAppName = mOpenAppNames[i + 1];
					}
					mOpenAppIcon[i + 1] = getOpenAppIcon(mResolveInfos.get(i));
				}
				if (mCurrentOpenAppIndex == -1) {
					mCurrentOpenAppIndex = mOpenAppNames.length - 1;
				}
			} else {
				mOpenAppNames = new String[size + 1];
				mOpenAppIcon = new Drawable[mOpenAppNames.length];
				mOpenAppNames[size] = getResources().getString(R.string.open_with_none);
				mOpenAppIcon[size] = getResources().getDrawable(R.drawable.media_open_setting_none_icon);
				for (int i = 0; i < size; i++) {
					mOpenAppNames[i] = mResolveInfos.get(i).activityInfo.loadLabel(mPackageManager)
							.toString();
					in.setClassName(mResolveInfos.get(i).activityInfo.packageName,
							mResolveInfos.get(i).activityInfo.name);
					if (in.toURI().equals(defaultOpen)) {
						mCurrentOpenAppIndex = i;
						mCurrentOpenAppName = mOpenAppNames[i];
					}
					mOpenAppIcon[i] = getOpenAppIcon(mResolveInfos.get(i));
				}
				if (mCurrentOpenAppIndex == -1) {
					mCurrentOpenAppIndex = mOpenAppNames.length - 1;
				}
			}
		}
	}
	
	/**
	 * 
	 * 获取可打开程序的图标
	 * @param resolveInfo
	 * @return
	 */
	private Drawable getOpenAppIcon(ResolveInfo resolveInfo) {
		if (resolveInfo != null) {
			Intent newIntent = new Intent(Intent.ACTION_MAIN);
			ComponentName c = new ComponentName(
					resolveInfo.activityInfo.packageName,
					resolveInfo.activityInfo.name);
			newIntent.setComponent(c);
			return AppDataEngine.getInstance(GOLauncherApp.getContext())
					.getAppItemIconByIntent(newIntent);
		}
		return null;
	}

	/**
	 * 保存默认打开方式
	 * */
	private void saveDefault() {
		String uri = null;
		if (mCurrentOpenAppName.equals(getResources().getString(R.string.open_with_none))) {
			mFunAppSetting.saveMediaOpenWay(mCurrentType, MediaManagementOpenChooser.APP_NONE);
			return;
//			uri = MediaManagementOpenChooser.APP_NONE;
		}
		else if (mCurrentType == FileEngine.TYPE_IMAGE
				&& mCurrentOpenAppName
						.equals(getResources().getString(R.string.go_picture_browser))) {
			mFunAppSetting.saveMediaOpenWay(mCurrentType,
					MediaManagementOpenChooser.APP_GO_PIC_VIEWER);
			return;
//			uri = MediaManagementOpenChooser.APP_GO_PIC_VIEWER;
		}
		else if (mCurrentType == FileEngine.TYPE_AUDIO
				&& mCurrentOpenAppName.equals(getResources().getString(R.string.go_music_player))) {
			mFunAppSetting.saveMediaOpenWay(mCurrentType,
					MediaManagementOpenChooser.APP_GO_MUSIC_PLAYER);
			return;
//			uri = MediaManagementOpenChooser.APP_GO_MUSIC_PLAYER;
		}
		else {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			for (ResolveInfo resolveInfo : mResolveInfos) {
				if (mCurrentOpenAppName.equals(resolveInfo.activityInfo
						.loadLabel(mPackageManager).toString())) {
					intent.setClassName(resolveInfo.activityInfo.packageName,
							resolveInfo.activityInfo.name);
					break;
				}
			}
			uri = intent.toURI();
		}
		mFunAppSetting.saveMediaOpenWay(mCurrentType, uri);
//		if (mCurrentType == FileEngine.TYPE_AUDIO) {
//			MediaManagerFactory.getMediaManager().setMusicDefaultOpenWay(uri);
//		} else if (mCurrentType == FileEngine.TYPE_IMAGE) {
//			.getMediaManager().setImageDefaultOpenWay(uri);
//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mResolveInfos != null) {
			mResolveInfos.clear();
			mResolveInfos = null;
		}
	}
	
	@Override
	public boolean onValueChange(DeskSettingItemBaseView view, Object value) {
		if (view != null && view == mMediaOpener && value instanceof String) {
			mCurrentOpenAppName = (String) value;
			saveDefault();
			mMediaOpener.updateSumarryText();
			mMediaOpener.setSummaryText(mCurrentOpenAppName);
		}
		return true;
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (mCurrentType == FileEngine.TYPE_AUDIO) { 
			// 通知默认音乐设置界面onStop事件
			sendBroadcast(new Intent(ICustomAction.ACTION_MEDIA_OPEN_SETTING_ACTIVITY_ON_STOP));
		}
	}

}
