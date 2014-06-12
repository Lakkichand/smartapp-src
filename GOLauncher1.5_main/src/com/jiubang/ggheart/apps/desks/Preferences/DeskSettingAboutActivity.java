package com.jiubang.ggheart.apps.desks.Preferences;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.share.ShareLayout;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.data.theme.ThemeManager;

/**
 * 
 * <br>类描述:桌面设置-关于GO桌面Activity
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-25]
 */
public class DeskSettingAboutActivity extends DeskSettingBaseActivity implements OnClickListener {
	/**
	 * 版本更新
	 */
	private DeskSettingItemBaseView mSettingCheckVersion;

	/**
	 * 试用帮助
	 */
	private DeskSettingItemBaseView mSettingHelpUse;

	/**
	 * 分享软件
	 */
	private DeskSettingItemBaseView mSettingShareAPP;

	/**
	 * 软件评分
	 */
	private DeskSettingItemBaseView mSettingRateGo;

	/**
	 * 意见反馈
	 */
	private DeskSettingItemBaseView mSettingFeedBack;

	/**
	 * 加入我们
	 */
	private DeskSettingItemBaseView mSettingJoinUSetting;

	/**
	 * 版权信息
	 */
	private DeskSettingItemBaseView mSettingCopyRight;

	/**
	 * Check for beta updates
	 */
	private DeskSettingItemBaseView mSettingBetaUpfate;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.desk_setting_about);
		initViews();
	}

	/**
	 * <br>功能简述:初始化View
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initViews() {
		mSettingCheckVersion = (DeskSettingItemBaseView) findViewById(R.id.check_version_item);
		mSettingCheckVersion.setOnClickListener(this);

		mSettingHelpUse = (DeskSettingItemBaseView) findViewById(R.id.help_use_item);
		mSettingHelpUse.setOnClickListener(this);

		mSettingShareAPP = (DeskSettingItemBaseView) findViewById(R.id.share_app_item);
		mSettingShareAPP.setOnClickListener(this);

		mSettingRateGo = (DeskSettingItemBaseView) findViewById(R.id.rate_go_item);
		mSettingRateGo.setOnClickListener(this);

		mSettingFeedBack = (DeskSettingItemBaseView) findViewById(R.id.feedback_item);
		mSettingFeedBack.setOnClickListener(this);

		mSettingJoinUSetting = (DeskSettingItemBaseView) findViewById(R.id.joinus_info_item);
		mSettingJoinUSetting.setOnClickListener(this);

		mSettingCopyRight = (DeskSettingItemBaseView) findViewById(R.id.copyright_info_item);
		mSettingCopyRight.setOnClickListener(this);

		mSettingBetaUpfate = (DeskSettingItemBaseView) findViewById(R.id.beta_updates_info_item);
		mSettingBetaUpfate.setOnClickListener(this);

		//判断是否CN地区。是就显示加入我们，否则显示Check for beta updates
		boolean isCn = Machine.isCnUser(this);
		if (isCn) {
			mSettingBetaUpfate.setVisibility(View.GONE);
			mSettingCopyRight.setBottomLineVisible(View.GONE);
		} else {
			mSettingJoinUSetting.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.check_version_item :
				// 点击跳转GOStore详情界面
				AppsDetail.gotoDetailDirectly(this, 
						AppsDetail.START_TYPE_APPRECOMMENDED, getPackageName());
//				GoStoreOperatorUtil.gotoStoreDetailDirectly(this, getPackageName());
				break;

			case R.id.help_use_item :
				saveStartActivity(new Intent(this, DeskSettingQaTutorialActivity.class));
				break;

			case R.id.share_app_item :
				startShareIntent();
				break;

			case R.id.rate_go_item :
				AppUtils.viewAppDetail(this, ThemeManager.DEFAULT_THEME_PACKAGE);
				break;

			case R.id.feedback_item :
				startFeedbackIntent();
				break;

			case R.id.joinus_info_item :
				startJoinus();
				break;

			case R.id.copyright_info_item :
				startCopyrightIntent();
				break;

			case R.id.beta_updates_info_item :
				startBetaUpdates();
				break;

			default :
				break;
		}
	}

	/**
	 * <br>功能简述:软件分享
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void startShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_title));

		String shareContent = getString(R.string.share_content_text);
		if (GoStorePhoneStateUtil.is200ChannelUid(this)) {
			shareContent += ShareLayout.DOWNLOAD_200;
		} else {
			shareContent += ShareLayout.DOWNLOAD_UN_200;
		}
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareContent);

		Intent chooser = Intent.createChooser(shareIntent, getString(R.string.choose_share_way));
		saveStartActivity(chooser);
	}

	/**
	 * <br>功能简述:意见反馈
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void startFeedbackIntent() {
		DeskSettingQaTutorialActivity.startFeedbackIntent(this);
	}

	/**
	 * <br>功能简述:加入我们
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void startJoinus() {
		Uri uri = Uri.parse("http://www.3g.cn/recruit/index.aspx");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		saveStartActivity(intent);
	}

	/**
	 * <br>功能简述:加入我们
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void startCopyrightIntent() {
		Uri uri = Uri.parse("http://3g.cn");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		saveStartActivity(intent);
	}

	/**
	 * <br>功能简述:Check for beta updates
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void startBetaUpdates() {
		Uri uri = Uri.parse("http://golauncher.goforandroid.com/tag/go-launcher-beta/");
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		saveStartActivity(intent);
	}

	public void saveStartActivity(Intent intent) {
		try {
			super.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
