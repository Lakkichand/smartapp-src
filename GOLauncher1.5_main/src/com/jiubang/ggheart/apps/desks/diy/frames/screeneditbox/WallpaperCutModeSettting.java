package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.WallpaperDensityUtil;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:选择壁纸横竖屏模式的对话框
 * <br>功能详细描述:
 * 
 */
public class WallpaperCutModeSettting extends Activity implements OnClickListener {

	private RadioButton mDefaultRB = null;
	private RadioButton mVertivalRB = null;
	private DeskButton mCancelBT = null;
	private DeskButton mOKBT = null;
	private ScreenSettingInfo mScreenInfo = null;
	private boolean mIsDefaultStyle = false;
	private RelativeLayout mWallPaperDefaultLayout = null;
	private RelativeLayout mWallPaperVerticalLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.screen_edit_wallpaper_cut_setting);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		init();
		addListeners();
		super.onStart();
	}

	private void init() {
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		mScreenInfo = controler.getScreenSettingInfo();
		mIsDefaultStyle = mScreenInfo.mWallpaperScroll;
		mDefaultRB = (RadioButton) findViewById(R.id.wallpaper_setting_default_RB);
		mVertivalRB = (RadioButton) findViewById(R.id.wallpaper_setting_vertical_RB);
		mCancelBT = (DeskButton) findViewById(R.id.cancel_btn);
		mOKBT = (DeskButton) findViewById(R.id.finish_btn);
		mWallPaperDefaultLayout = (RelativeLayout) findViewById(R.id.wall_paper_default_mode_layout);
		mWallPaperVerticalLayout = (RelativeLayout) findViewById(R.id.wall_paper_vertical_mode_layout);
		mDefaultRB.setChecked(mIsDefaultStyle);
		mVertivalRB.setChecked(!mIsDefaultStyle);
	}

	private void addListeners() {
		mCancelBT.setOnClickListener(this);
		mOKBT.setOnClickListener(this);
		mWallPaperDefaultLayout.setOnClickListener(this);
		mWallPaperVerticalLayout.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.cancel_btn :
				finish();
				break;
			case R.id.finish_btn :
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.GUIDE_GL_FRAME, null, null);
				GoSettingControler controler = GOLauncherApp.getSettingControler();
				mScreenInfo.mWallpaperScroll = mIsDefaultStyle;
				controler.updateScreenSettingInfo(mScreenInfo);
				WallpaperDensityUtil.setWallpaperDimension(GoLauncher.getContext());
				// 刷新壁纸列表
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
						IDiyMsgIds.SCREEN_EDIT_UPDATE_WALLPAPER_ITEMS, 0, null, null);
				finish();
				break;
			case R.id.wall_paper_default_mode_layout :
				mIsDefaultStyle = true;
				mVertivalRB.setChecked(false);
				mDefaultRB.setChecked(true);
				break;
			case R.id.wall_paper_vertical_mode_layout :
				mIsDefaultStyle = false;
				mVertivalRB.setChecked(true);
				mDefaultRB.setChecked(false);
				break;
			default :
				break;
		}

	}

	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
