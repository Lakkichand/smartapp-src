package com.jiubang.ggheart.apps.desks.Preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.gau.go.launcherex.R;
import com.gau.utils.net.util.NetUtil;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemBaseView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingItemCheckBoxView;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.facebook.GoFacebookUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;
/**
 * 
 * @author xiangliang
 *
 */
public class DeskSettingFacebookActivity extends DeskSettingBaseActivity {
	private DeskSettingItemBaseView mFacebookLogin;
	private DeskSettingItemCheckBoxView mThemeTrack;
	private DeskSettingItemBaseView mFacebookLogOut;
	PreferencesManager mPreferencesManager; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.desk_setting_facebook_set);
		init();
	}
	
	private void init() {
		mFacebookLogin = (DeskSettingItemBaseView) findViewById(R.id.login_facebook);
		mFacebookLogin.setOnClickListener(this);
		mThemeTrack = (DeskSettingItemCheckBoxView) findViewById(R.id.track_theme);
		mFacebookLogOut = (DeskSettingItemBaseView) findViewById(R.id.logout_facebook);		
		mThemeTrack.setOnValueChangeListener(this);
		mFacebookLogOut.setOnClickListener(this);
		mFacebookLogOut.setVisibility(View.GONE);
		mPreferencesManager = new PreferencesManager(this,
				IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
		boolean trackOn = mPreferencesManager.getBoolean(IPreferencesIds.FACEBOOK_OPEN_GRAPH_SWITCH, false);
		Session session = Session.getActiveSession();
		mThemeTrack.setIsCheck(trackOn && session != null && session.isOpened() && GoFacebookUtil.hasPermissions(session));
		
		updateFacebookView();
		
		// 如果没登录FB，判断是否自动连接
		if (session == null) {
			Session.setActiveSession(null);
			session = new Session.Builder(this).setApplicationId(
					GoFacebookUtil.APPID).build();
			Session.setActiveSession(session);
			if (session.getState() == SessionState.CREATED_TOKEN_LOADED) {
				mFacebookLogin.performClick();
			}
		}
	}
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch (v.getId()) {
		case R.id.login_facebook:
			GoFacebookUtil.shareALink(this);			
			break;
		case R.id.logout_facebook:
			Session session = Session.getActiveSession();
			if (session == null || !session.isOpened()) {
				Toast.makeText(this, R.string.facebook_user_notlogin_summary, Toast.LENGTH_SHORT).show();
			} else {
				//FB登出
				GoFacebookUtil.logout(this);
				updateFacebookView();
			}			
			StatisticsData.countStatData(this, StatisticsData.KEY_FACEBOOK_ACOUNT_CHECKOUT);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onValueChange(DeskSettingItemBaseView view, Object value) {
		if (NetUtil.getNetWorkType(this) == NetUtil.NETWORKTYPE_NULL) {
			mThemeTrack.setIsCheck(false);
		}
		if (mThemeTrack.getIsCheck()) {
			mPreferencesManager.putBoolean(IPreferencesIds.FACEBOOK_OPEN_GRAPH_SWITCH, true);
		} else {
			mPreferencesManager.putBoolean(IPreferencesIds.FACEBOOK_OPEN_GRAPH_SWITCH, false);
		}
		mPreferencesManager.commit();
		if (view == mThemeTrack && (Boolean) value) {
			Session session = Session.getActiveSession();
			if (session != null && session.isOpened() && !GoFacebookUtil.hasPermissions(session)) {
				GoFacebookUtil.requestNewPublishPermissions(session, this);
			} /*else if (session != null && !session.isOpened()) {
				if (!session.isClosed()) {
					session.closeAndClearTokenInformation();
				}
				GoFacebookUtil.shareALink((Activity) this);
			} */ else if (session == null || !session.isOpened()) {
				GoFacebookUtil.shareALink((Activity) this);
			}
		}
		return super.onValueChange(view, value);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	public void updateFacebookView() {
		Session session = Session.getActiveSession();
		String name = GoFacebookUtil.getUserInfo().getName();
		if (session != null && session.isOpened()) {
			mFacebookLogin.setTitleText(R.string.facebook_share_on_facebook);
			mFacebookLogOut.setVisibility(View.VISIBLE);
			if (name != null) {
				mFacebookLogOut.setSummaryText(getString(R.string.facebook_user_summary) + name);
			}
		} else {
			mFacebookLogin.setTitleText(R.string.facebook_connect_with);
			if (!(mFacebookLogOut.getVisibility() == View.GONE)) {
				mFacebookLogOut.setVisibility(View.GONE);
			}
		}

		boolean trackOn = mPreferencesManager.getBoolean(
				IPreferencesIds.FACEBOOK_OPEN_GRAPH_SWITCH, false);

		mThemeTrack.setIsCheck(trackOn && session != null && session.isOpened()
				&& GoFacebookUtil.hasPermissions(session));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session session = Session.getActiveSession();
		if (session != null) {
			session.onActivityResult(this, requestCode, resultCode, data);
		}
		if (session == null || !session.isOpened()) {
			mPreferencesManager.putBoolean(IPreferencesIds.FACEBOOK_OPEN_GRAPH_SWITCH, false);
			mPreferencesManager.commit();
			Toast.makeText(this, "Sorry,facebook login failed", Toast.LENGTH_SHORT).show();
		}
	}

}
