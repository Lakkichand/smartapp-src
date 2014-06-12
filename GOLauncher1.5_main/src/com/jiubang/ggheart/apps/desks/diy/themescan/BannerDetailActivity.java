package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.HttpUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageHttp;
import com.jiubang.ggheart.components.DeskResources;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.data.theme.bean.SpecThemeViewConfig;
import com.jiubang.ggheart.data.theme.parser.ThemeInfoParser;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-10-18]
 */
public class BannerDetailActivity extends Activity {

	private BannerDetailView mBannerDetailView;
	private RelativeLayout mTitleGroup;
	private LinearLayout mBackbtnArea;
	private SpecThemelistViewLayout mlistViewLayout;
	private BroadcastReceiver mThemeChangeReceiver;
	private TextView mTitleView;
	private String mTitle;
	private boolean mFromMessageCenter = false;
	private ImageView mBackBtn;

	public static final String VIEW_CONFIG_POSTFIX = ".config";
	public static final int MSG_LOAD_DATA_FINISHED = 1;
	public static final int MSG_PARSE_VIEW_CONFIG_FINISHED = 2;
	public static final String SPEC_RES_PATH = LauncherEnv.Path.GOTHEMES_PATH + "spec/";
	private SpecThemeViewConfig mViewConfig;
	private int mSpecId = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.theme_banner_detailview);
		mlistViewLayout = (SpecThemelistViewLayout) findViewById(R.id.banner_layout);
		mTitleGroup = (RelativeLayout) findViewById(R.id.banner_detail_topbar);
		mBackBtn = (ImageView) mTitleGroup.findViewById(R.id.back_btn);
		confirmOrientation();
		Intent it = getIntent();
		mSpecId = it.getIntExtra("ty", -1);
		if (mSpecId < 0) {
			finish();
		}
		mFromMessageCenter = it.getBooleanExtra("entrance", false);
		mTitle = it.getStringExtra("title");
		mTitleView = (TextView) findViewById(R.id.banner_title);
		mTitleView.setText(mTitle);
		registerThemeChangedReceiver();
		mBannerDetailView = new BannerDetailView(this, mSpecId);
		RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		mlistViewLayout.addView(mBannerDetailView, relativeLayoutParams);

		mBackbtnArea = (LinearLayout) findViewById(R.id.back_btnArea);
		mBackbtnArea.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		applyViewConfig();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mBannerDetailView.onDestory();
		unregisterReceiver(mThemeChangeReceiver);
		if (mFromMessageCenter) {
			ThemePurchaseManager.getInstance(getApplicationContext()).destory();
			AppUtils.killProcess();
		}
		if (mTitleView != null && mTitleView instanceof DeskTextView) {
			((DeskTextView) mTitleView).selfDestruct();
			mTitleView = null;
		}
	}

	/**
	 * 判断是横屏还是竖屏
	 * */
	public void confirmOrientation() {
		DisplayMetrics mMetrics = getResources().getDisplayMetrics();
		if (mMetrics.widthPixels <= mMetrics.heightPixels) {
			SpaceCalculator.setIsPortrait(true);
			SpaceCalculator.getInstance(this).calculateItemViewInfo();
		} else {
			SpaceCalculator.setIsPortrait(false);
			SpaceCalculator.getInstance(this).calculateThemeListItemCount();
		}
//		SpaceCalculator.getInstance(this).calculateThemeListItemCount();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Resources res = getResources();
		if (res instanceof DeskResources) {
			res.updateConfiguration(super.getResources().getConfiguration(), super.getResources()
					.getDisplayMetrics());

			try {
				Configuration config = res.getConfiguration(); //获得设置对象
				DisplayMetrics dm = res.getDisplayMetrics(); //获得屏幕参数：主要是分辨率，像素等。
				PreferencesManager preferences = new PreferencesManager(this,
						IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
				String currentlanguage = preferences.getString(
						IPreferencesIds.CURRENTSELETELANGUAGE, "");
				if (currentlanguage != null && !currentlanguage.equals("")) {
					if (currentlanguage.length() == 5) {
						String language = currentlanguage.substring(0, 2);
						String country = currentlanguage.substring(3, 5);
						config.locale = new Locale(language, country);
					} else {
						config.locale = new Locale(currentlanguage);
					}
					res.updateConfiguration(config, dm);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		DisplayMetrics mMetrics = getResources().getDisplayMetrics();
//		if (mMetrics.widthPixels <= mMetrics.heightPixels) {
//			SpaceCalculator.setIsPortrait(true);
//		} else {
//			SpaceCalculator.setIsPortrait(false);
//		}
		confirmOrientation();
		if (null != mBannerDetailView) {
			mBannerDetailView.changeOrientation();
		}
	}
	private void registerThemeChangedReceiver() {

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addAction(ICustomAction.ACTION_SPEC_THEME_CHANGED);
		intentFilter.addAction(ICustomAction.ACTION_ZIP_THEME_REMOVED);
		intentFilter.addAction(ICustomAction.ACTION_NEW_THEME_INSTALLED);
		intentFilter.addAction(ICustomAction.ACTION_SPEC_THEME_TITLE);
		intentFilter.addDataScheme("package");
		intentFilter.setPriority(Integer.MAX_VALUE);
		mThemeChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (ICustomAction.ACTION_SPEC_THEME_CHANGED.equals(intent.getAction())) {
					if (mBannerDetailView != null) {
						mBannerDetailView.startLoadThemeData();
					}
				} else if (ICustomAction.ACTION_SPEC_THEME_TITLE.equals(intent.getAction())) {
					mTitle = intent.getStringExtra("title");
					((TextView) findViewById(R.id.banner_title)).setText(mTitle);
					String url = intent.getStringExtra("config");
					if (url != null) {
						downloadConfigXml(url);
					}
				}
			}
		};
		try {
			registerReceiver(mThemeChangeReceiver, intentFilter);
		} catch (Throwable e) {
			try {
				unregisterReceiver(mThemeChangeReceiver);
				registerReceiver(mThemeChangeReceiver, intentFilter);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		}

	}

	public void gotoGoStore() {
		StatisticsData.countStatData(this, StatisticsData.ENTRY_KEY_THEMEMANAGE);
		//		GoStoreStatisticsUtil.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_THEMEMANAGE, this);
		AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(this,
				AppRecommendedStatisticsUtil.ENTRY_TYPE_THEMEMANAGE);
		AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
				MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
		StatisticsData.countThemeTabData(StatisticsData.THEME_TAB_ID_GET_MORE_THEME);
		//		Intent intent = new Intent();
		//		intent.setClass(this, GoStore.class);
		//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//		intent.putExtra("sort", SortsBean.SORT_THEME + "");
		//		startActivity(intent);

	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
				case MSG_PARSE_VIEW_CONFIG_FINISHED :
					if (msg.obj != null && msg.obj instanceof SpecThemeViewConfig) {
						mViewConfig = (SpecThemeViewConfig) msg.obj;
						configView();
					}
					break;

				default :
					break;
			}
		}

	};

	private void configView() {

		if (mViewConfig != null) {
			if (mTitleView != null) {
				mTitleView.setTextColor(mViewConfig.mTitleColor);
			}
			String path = mViewConfig.mDataPath;
			if (mViewConfig.mBackBtnBgImage != null) {
				Bitmap bmp = decodeBitmap(path + mViewConfig.mBackBtnBgImage);
				if (bmp != null) {
					mBackBtn.setImageBitmap(bmp);
				}
			}
			if (mViewConfig.mTileGroupBgImage != null) {
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
						android.view.ViewGroup.LayoutParams.FILL_PARENT, (int) getResources()
								.getDimension(R.dimen.theme_spec_page_title_group_height));
				mTitleGroup.setLayoutParams(params);
				Bitmap bmp = decodeBitmap(path + mViewConfig.mTileGroupBgImage);
				if (bmp != null) {
					mTitleGroup.setBackgroundDrawable(new BitmapDrawable(bmp));
				}
			}
			if (mViewConfig.mListViewBgImage != null) {
				Bitmap bmp = decodeBitmap(path + mViewConfig.mListViewBgImage);
				if (bmp != null) {
					mViewConfig.mListViewBgImgDrawable = new BitmapDrawable(bmp);
				}
			}
			if (mBannerDetailView != null) {

				mBannerDetailView.configView(mViewConfig);
			}
			mlistViewLayout.configView(mViewConfig);
		}
	}
	private void applyViewConfig() {
		new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				String fileName = SPEC_RES_PATH + mSpecId + VIEW_CONFIG_POSTFIX;
				HttpUtil.unZipFolder(fileName, SPEC_RES_PATH + mSpecId, false);
				SpecThemeViewConfig config = new ThemeInfoParser()
						.parseSpecThemeViewConfig(SPEC_RES_PATH + mSpecId + "/"
								+ getConfigFileName(SPEC_RES_PATH + mSpecId));
				if (config != null) {
					config.mDataPath = SPEC_RES_PATH + mSpecId + "/";
					Message msg = mHandler.obtainMessage();
					msg.obj = config;
					msg.what = MSG_PARSE_VIEW_CONFIG_FINISHED;
					mHandler.sendMessage(msg);
				}
			}

		}.start();
	}

	public void downloadConfigXml(final String url) {
		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				String fileName = SPEC_RES_PATH + mSpecId + VIEW_CONFIG_POSTFIX;
				if (!new File(fileName).exists()) {
					if (MessageHttp.downloadZipRes(url, SPEC_RES_PATH, mSpecId
							+ VIEW_CONFIG_POSTFIX)) {
					}
				}
				HttpUtil.unZipFolder(fileName, SPEC_RES_PATH + mSpecId, false);
				SpecThemeViewConfig config = new ThemeInfoParser()
						.parseSpecThemeViewConfig(SPEC_RES_PATH + mSpecId + "/"
								+ getConfigFileName(SPEC_RES_PATH + mSpecId));
				if (config != null) {
					config.mDataPath = SPEC_RES_PATH + mSpecId + "/";
					Message msg = mHandler.obtainMessage();
					msg.what = MSG_PARSE_VIEW_CONFIG_FINISHED;
					msg.obj = config;
					mHandler.sendMessage(msg);
				}
			}

		}.start();
	}

	private String getConfigFileName(String dirPath) {
		File dir = new File(dirPath);
		if (dir.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				private Pattern mPattern = Pattern.compile("xml");

				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					String nameString = new File(filename).getName();
					String postfix = nameString.substring(nameString.lastIndexOf(".") + 1);
					return mPattern.matcher(postfix).matches();
				}
			};
			String[] xmls = dir.list(filter);
			if (xmls != null && xmls.length != 0) {
				return xmls[0];
			}
		}
		return null;
	}

	public SpecThemeViewConfig getViewConfig() {
		return mViewConfig;
	}

	private Bitmap decodeBitmap(String path) {
		try {
			return BitmapFactory.decodeFile(path);

		} catch (OutOfMemoryError e) {

		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}
}
