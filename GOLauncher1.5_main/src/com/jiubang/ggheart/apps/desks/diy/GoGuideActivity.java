package com.jiubang.ggheart.apps.desks.diy;

import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DesktopIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IndicatorListner;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicatorItem;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.components.DeskBuilder;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.components.facebook.GoFacebookUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * Go桌面引导页面
 *
 */
public class GoGuideActivity extends Activity
		implements
			IndicatorListner,
			OnClickListener,
			OnTouchListener {
	private PackageReceiver mReceiver;
	private boolean mNeedPop = false;
	public static final String QUICK_MODE = "qucikmode";
	public static final String NORMAL_MODE = "normalmode";
	public static final int TYPE_ID = 501;
	public boolean mMigrateClicked = false;
	public boolean mLanguageClicked = false;
	public boolean mClearClicked = false;
	public boolean mRestart = false;
	private DesktopIndicator mIndicator;
	public static final int AGREEMENT_REQUESTCODE = 0x1001;
	public static final int AGREEMENT_RESULTCODE_EXIT = 100;
	private GuidePageScroller mScreenScroller;
	private GuideImageScroller mImageScroller;

	private TextView mContent_msg2;
	//精品引导页中间的图片
	private Drawable mDrawable;
	private Bitmap mResizedBitmap;
	
	private Button mFBView;
	
	private final int mHandlerClickEnable = 1;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case mHandlerClickEnable :
					View view = (View) msg.obj;
					view.setClickable(true);
					break;

				default :
					break;
			}
		};
	};
	
	public GuideImageScroller getmImageScroller() {
		return mImageScroller;
	}

	public GuidePageScroller getmScreenScroller() {
		return mScreenScroller;
	}

	public GoGuideActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		LayoutInflater inflater = getLayoutInflater();
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mScreenScroller = new GuidePageScroller(this);
		View firstPage = null;
		if (Machine.isMeizu()) {
			firstPage = inflater.inflate(R.layout.guide_layout_m9, null);
		} else {
			firstPage = inflater.inflate(R.layout.guide_layout, null);
		}

		mImageScroller = (GuideImageScroller) firstPage.findViewById(R.id.giudeview);
		View transparentPage = inflater.inflate(R.layout.guide_last_layout, null);
		ImageView themepage = (ImageView) transparentPage.findViewById(R.id.theme_page);
		themepage.setOnClickListener(this);
		themepage.setOnTouchListener(this);
		Button gotoMain = (Button) transparentPage.findViewById(R.id.gotomain);
		gotoMain.setOnClickListener(this);
		Button gotoTheme = (Button) transparentPage.findViewById(R.id.gototheme);
		gotoTheme.setOnClickListener(this);
		mContent_msg2 = (TextView) transparentPage.findViewById(R.id.content_msg2);
		mContent_msg2.setOnClickListener(this);
		mContent_msg2.setOnTouchListener(this);
		View guideImage0 = inflater.inflate(R.layout.guidelayoutitem, null);
		View guideImage1 = inflater.inflate(R.layout.guidelayoutitem, null);
		View guideImage2 = inflater.inflate(R.layout.guidelayoutitem, null);
		mImageScroller.addView(guideImage0);
		mImageScroller.addView(guideImage1);
		mImageScroller.addView(guideImage2);
		mImageScroller.setView();
		
		// add by zhouxuewe 增加用户是新安装还是覆盖安装的标记
		String isCover = "1";
		if (null != GoLauncher.getContext() && GoLauncher.getContext().getFirstRun()) {
			mScreenScroller.addView(firstPage);
			mScreenScroller.setImageScroll(mImageScroller);
			isCover = "0";
		}
		
		View lastPage = null;
		View leftBtn = null;
		ImageButton middleBtn = null;
		ImageButton rightBtn = null;
		if (getLanguage().contains("zh")) {
			lastPage = inflater.inflate(R.layout.guide_cn_lastpage, null);
			leftBtn = (View) lastPage.findViewById(R.id.guide_left_btn);
			middleBtn = (ImageButton) lastPage.findViewById(R.id.guide_middle_btn);
			mScreenScroller.addView(lastPage);
		} else {
			lastPage = inflater.inflate(R.layout.guide_lastpage, null);
			leftBtn = lastPage.findViewById(R.id.guide_left_btn);
			middleBtn = (ImageButton) lastPage.findViewById(R.id.guide_middle_btn);
			rightBtn = (ImageButton) lastPage.findViewById(R.id.guide_right_btn);
			rightBtn.setOnClickListener(this);
			mScreenScroller.addView(lastPage);
			
			//facebook引导页
			if (GoFacebookUtil.isEnable()) {
				View facebookGuideView = inflater.inflate(R.layout.guide_facebook, null);
				mFBView = (Button) facebookGuideView.findViewById(R.id.connectbutton);
				mFBView.setOnClickListener(this);
				mScreenScroller.addView(facebookGuideView);
			}
		}
		Statistics.setUserCover(this, isCover);
		mScreenScroller.addView(transparentPage);
		setContentView(mScreenScroller);
		// 设置背景图片为平铺
		RelativeLayout layout = (RelativeLayout) firstPage.findViewById(R.id.guide_act_layout);
		BitmapDrawable drawable = (BitmapDrawable) layout.getBackground();
		drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);

		RelativeLayout layoutLast = (RelativeLayout) lastPage.findViewById(R.id.guide_last_lay);
		BitmapDrawable drawableLast = (BitmapDrawable) layoutLast.getBackground();
		drawableLast.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);

		leftBtn.setOnClickListener(this);
		middleBtn.setOnClickListener(this);
		setUpChangeLogPage(lastPage);

		View tmpIndicator = firstPage.findViewById(R.id.indicator);
		try {
			mIndicator = (DesktopIndicator) tmpIndicator;
			mIndicator.setVisibility(View.VISIBLE);
			initIndicator();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mReceiver = new PackageReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
		registerReceiver(mReceiver, filter);

		// SharedPreferences sharedPreferences
		// =getSharedPreferences(IPreferencesIds.USERTUTORIALCONFIG,Context.MODE_PRIVATE);
		// String mode= sharedPreferences.getString("mode",
		// GoGuideActivity.NORMALMODE);
		initDrawable();
	}

	@Override
	protected void onResume() {

		super.onResume();
		if (mNeedPop) {
			mNeedPop = false;
			showMigrateTip();
		}

	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		if (mRestart == true) {
			if ((!mClearClicked && hasSetThirdDefaultLauncher())
					|| (!mLanguageClicked && !hasInstallLanguagePackage()) || !mMigrateClicked) {
				PreferencesManager sharedPreferences = new PreferencesManager(this,
						IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
				sharedPreferences.putString("mode", QUICK_MODE);
				// editor.putInt("precount", mGuidView.getCount());
				sharedPreferences.commit();
			}
		} else {
			PreferencesManager sharedPreferences = new PreferencesManager(this,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			sharedPreferences.putString("mode", NORMAL_MODE);
			sharedPreferences.commit();
		}
		// mGuidView = null;
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			mRestart = false;
			if (mScreenScroller.getmCurrentScreen() == mScreenScroller.getChildCount() - 1) {
				Toast.makeText(getApplicationContext(), R.string.gostore_guide_content,
						Toast.LENGTH_SHORT).show();
			}
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 
	 * 包状态接受者
	 *
	 */
	public class PackageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
				String packageName = intent.getDataString();
				if (packageName.contains(LauncherEnv.Plugin.DESKMIGRATE_PACKAGE_NAME)) {
					mNeedPop = true;
				}

			}
		}
	}

	private void showMigrateTip() {
		DeskBuilder builder = new DeskBuilder(this);
		builder.setTitle(R.string.migrate_tip_title);
		builder.setMessage(R.string.migrate_tip_message);
		builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(ICustomAction.ACTION_DESK_MIGRATE);
				Bundle bundle = new Bundle();
				bundle.putInt("code", IRequestCodeIds.REQUEST_MIGRATE_DESK);
				intent.putExtras(bundle);
				try {
					mRestart = true;
					if ((!mClearClicked && hasSetThirdDefaultLauncher())
							|| (!mLanguageClicked && !hasInstallLanguagePackage())) {
						PreferencesManager sharedPreferences = new PreferencesManager(
								GoGuideActivity.this, IPreferencesIds.USERTUTORIALCONFIG,
								Context.MODE_PRIVATE);
						sharedPreferences.putString("mode", QUICK_MODE);
						sharedPreferences.commit();
					}
					GoGuideActivity.this.startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		builder.setNegativeButton(GoGuideActivity.this.getString(R.string.cancel), null);
		builder.create().show();
	}

	public boolean hasInstallLanguagePackage() {
		boolean bRet = true;
		DeskResourcesConfiguration configuration = DeskResourcesConfiguration.getInstance();
		if (configuration != null) {
			int error = configuration.getErrorCode();
			if (DeskResourcesConfiguration.ERROR_LANGUAGE_NO_INSTALL == error
					|| DeskResourcesConfiguration.ERROR_LANGUAGE_NEED_UPDATE == error) {
				bRet = false;
			}
		}
		return bRet;
	}

	public boolean hasSetThirdDefaultLauncher() {
		boolean bRet = false;
		final String packageStr = AppUtils.getDefaultLauncherPackage(this);
		// 没有设置
		// 设置为Go桌面
		if (null != packageStr && !packageStr.equals(LauncherEnv.PACKAGE_NAME)) {
			bRet = true;
		}
		return bRet;
	}

	public boolean hasSetAsDefaultLauncher() {
		boolean bRet = false;
		final String packageStr = AppUtils.getDefaultLauncherPackage(this);
		// 没有设置
		// 设置为Go桌面
		if (null != packageStr && packageStr.equals(LauncherEnv.PACKAGE_NAME)) {
			bRet = true;
		}
		return bRet;
	}

	@Override
	public void clickIndicatorItem(int index) {

	}

	@Override
	public void sliding(float percent) {

	}

	public void updateIndicator(int position) {
		if (mIndicator != null && mImageScroller != null && position >= 0 && position < mImageScroller.getChildCount()) {
			mIndicator.setCurrent(position);
		}
	}

	private void initIndicator() {
		mIndicator.setIndicatorListner(this);
		mIndicator.setDefaultDotsIndicatorImage(R.drawable.guide_indicator_cur,
				R.drawable.guide_indicator_other);
		mIndicator.setDotIndicatorLayoutMode(ScreenIndicator.LAYOUT_MODE_ADJUST_PICSIZE);
		mIndicator.setDotIndicatorDrawMode(ScreenIndicatorItem.DRAW_MODE_INDIVIDUAL);
		mIndicator.setTotal(mImageScroller.getChildCount());
		mIndicator.setCurrent(0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case AGREEMENT_REQUESTCODE :
				if (resultCode == AGREEMENT_RESULTCODE_EXIT) {

					finish();
				}
				break;

			default :
				//facebook
				Session session = Session.getActiveSession();
				if (session != null) {
					session.onActivityResult(this, requestCode, resultCode, data);
				}
				break;
		}
	}

	private void setUpChangeLogPage(View convertView) {
		String urlLicense = null;
		if (getLanguage().contains("zh")) {
			urlLicense = LauncherEnv.Url.LICENSE_AGREEMENT_CN;
		} else {
			urlLicense = LauncherEnv.Url.LICENSE_AGREEMENT_EN;
		}
		TextView gotoLicense = (TextView) convertView.findViewById(R.id.gotolicense);
		gotoLicense.setVisibility(View.VISIBLE);
		String content = getResources().getString(R.string.user_license);
		int start = 0;
		int end = 0;
		if (getLanguage().contains("zh")) {
			start = content.indexOf(getString(R.string.french_quotes_left));
			end = content.indexOf(getString(R.string.french_quotes_right)) + 1;
			if (start == -1)// 针对内侧包去掉中文
			{
				start = content.indexOf('<');
				end = content.indexOf('>') + 1;
			}
		} else {
			start = content.indexOf('<');
			end = content.indexOf('>') + 1;
		}
		if ((start == 0 && end == 0) || start == -1 || end == -1) {
			return;
		}
		SpannableString s = new SpannableString(content);
		s.setSpan(new TextUrlSpan(urlLicense), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		s.setSpan(new ForegroundColorSpan(0xFF55AB34), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		gotoLicense.setText(s);
		gotoLicense.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private String getLanguage() {
		Locale l = Locale.getDefault();
		return String.format("%s-%s", l.getLanguage(), l.getCountry());
	}

	/**
	 * 
	 * 文字URL封装类
	 *
	 */
	private class TextUrlSpan extends ClickableSpan {
		private String mUrl;

		public TextUrlSpan(String url) {
			super();
			mUrl = url;
		}

		@Override
		public void onClick(View widget) {

			AppUtils.gotoBrowser(GoGuideActivity.this, mUrl);
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			ds.setUnderlineText(false); // 去掉下划线
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.connectbutton:
			//share FB
			GoFacebookUtil.shareALink(this);
			
			//防止连续双击
			v.setClickable(false);
			Message msg = new Message();
			msg.what = mHandlerClickEnable;
			msg.obj = v;
			mHandler.sendMessageDelayed(msg, 1000);
			break;
			
		case R.id.guide_left_btn:
			if (!GoFacebookUtil.isEnable()) {
				AppUtils.gotoBrowser(GoGuideActivity.this,
						getString(R.string.notice_url));
			} else {
				AppUtils.gotoBrowser(GoGuideActivity.this,
						getString(R.string.twitter_url));
			}
			break;
		case R.id.guide_right_btn:
			AppUtils.gotoBrowser(GoGuideActivity.this,
					getString(R.string.translate_url));
			break;
		case R.id.guide_middle_btn:
			if (getLanguage().contains("zh")) {
				AppUtils.gotoBrowser(GoGuideActivity.this,
						getString(R.string.bolg_url));
			} else {
				AppUtils.gotoBrowser(GoGuideActivity.this,
						getString(R.string.facebook_url));
			}
			break;
		case R.id.theme_page:
//			GoStoreStatisticsUtil.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_UPDATA_GUIDE, this);
			//　更改为应用中心的入口
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(this, AppRecommendedStatisticsUtil.ENTRY_TYPE_UPDATA_GUIDE);
			StatisticsData.saveStatData(this, StatisticsData.GUIDE_PAGE_FOR_GOSTORE,
					StatisticsData.CLICK_BANNER_ENTER_GOSTORE_TOPIC);
//			bundle.putString(GoStore.GOSTORE_SORT_ID, "266");
//			intent.setClass(this, GoStore.class);
//			intent.putExtras(bundle);
//			this.startActivity(intent);
			AppsManagementActivity.startTopic(GoLauncher.getContext(), TYPE_ID, true);
			this.finish();
			break;
		case R.id.gotomain:
			StatisticsData.saveStatData(this, StatisticsData.GUIDE_PAGE_FOR_GOSTORE,
					StatisticsData.CLICK_HOME_SCREEN_BUTTON);
			this.finish();
			break;
		case R.id.gototheme:
//			GoStoreStatisticsUtil.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_UPDATA_GUIDE, this);
			StatisticsData.saveStatData(this, StatisticsData.GUIDE_PAGE_FOR_GOSTORE,
					StatisticsData.CLICK_MORE_THEME_BUTTON);
//			bundle.putString(GoStore.GOSTORE_SORT_ID, "230");
//			intent.setClass(this, GoStore.class);
//			intent.putExtras(bundle);
//			this.startActivity(intent);
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(GoLauncher.getContext(), 
					AppRecommendedStatisticsUtil.ENTRY_TYPE_UPDATA_GUIDE);
			AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
					MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
			this.finish();
			break;
		case R.id.content_msg2:
			GoStoreStatisticsUtil.setCurrentEntry(GoStoreStatisticsUtil.ENTRY_TYPE_UPDATA_GUIDE, this);
			StatisticsData.saveStatData(this, StatisticsData.GUIDE_PAGE_FOR_GOSTORE,
					StatisticsData.CLICK_BANNER_ENTER_GOSTORE_TOPIC);
//			bundle.putString(GoStore.GOSTORE_SORT_ID, "266");
//			intent.setClass(this, GoStore.class);
//			intent.putExtras(bundle);
//			this.startActivity(intent);
			AppsManagementActivity.startTopic(GoLauncher.getContext(), TYPE_ID, true);
			this.finish();
			break;

			default :
				break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		switch (v.getId()) {
		case R.id.content_msg2:
			if (action == MotionEvent.ACTION_DOWN) {
				mContent_msg2.setTextColor(getResources().getColor(
						R.color.gesture_draw_color));
			} else if (action == MotionEvent.ACTION_UP 
					|| action == MotionEvent.ACTION_CANCEL) {
				mContent_msg2.setTextColor(getResources().getColor(
						R.color.gostore_guide));
			}
			break;
			case R.id.theme_page :
				if (action == MotionEvent.ACTION_DOWN) {
					if (mResizedBitmap == null || mDrawable == null) {
						initDrawable();
					}
					((ImageView) v).setImageBitmap(mResizedBitmap);
				} else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
					if (mResizedBitmap == null || mDrawable == null) {
						initDrawable();
					}
					((ImageView) v).setImageDrawable(mDrawable);
				}
				break;
			default :
				break;
		}
		return false;
	}

	private void initDrawable() {
		try {
			mDrawable = this.getResources().getDrawable(R.drawable.themepage);
			Bitmap bitmapOrg = BitmapFactory.decodeResource(getResources(), R.drawable.themepage);
			int width = bitmapOrg.getWidth();
			int height = bitmapOrg.getHeight();
			float scaleWidth = (float) 0.93;
			float scaleHeight = (float) 0.93;
			Matrix matrix = new Matrix();
			matrix.postScale(scaleWidth, scaleHeight);
			mResizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
		} catch (OutOfMemoryError e) {
		}
	}
	
	public void updateFBView() {
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened() && mFBView != null) {
			mFBView.setText(R.string.facebook_share_on_facebook);
		} else {
			mFBView.setText(R.string.facebook_connect_with);
		}
	}
}
