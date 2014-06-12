package com.jiubang.ggheart.components.facebook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.Session;
import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingPageTitleView;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
/**
 * 
 * @author xiangliang
 *
 */
public class FacebookConnectActivity extends Activity {
	
	Button mAddLineBt;
	Context mContext;
	Session mSession;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.facebook_conncet);
		mContext = this;
		initViews();

	}

	private void initViews() {
		DeskSettingPageTitleView pageTitle = (DeskSettingPageTitleView) findViewById(R.id.main_title);
		pageTitle.getBackLayout().setVisibility(View.GONE);
		mAddLineBt = (Button) findViewById(R.id.addLineBt);
		mAddLineBt.setOnClickListener(mBtListener);
	}
	OnClickListener mBtListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Session session = Session.getActiveSession();
//			Bundle bundle = getIntent().getExtras();
//			String themeName = bundle.getString("name");
//			String pkgName = bundle.getString("pkgName");
//			if (session != null && session.isOpened() && GoFacebookUtil.hasPermissions(session)) {
//				FacebookOpenGraphUtil.requestFacebookOG(GOLauncherApp.getContext(), pkgName, themeName, new OGRequestServerHandler());
//			} else if (session != null && session.isOpened()) {
//				GoFacebookUtil.requestNewPublishPermissions(session, mContext);
//			} else {
//				OpenGraphObject object = new OpenGraphObject();
//				object.setPkgName(pkgName);mc
//				object.setThemeName(themeName);
//				GoFacebookUtil.sendOpenGraphByLogin((Activity)mContext, object);
//			}
			PreferencesManager sp = new PreferencesManager(mContext,
					IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
			sp.putBoolean(IPreferencesIds.FACEBOOK_OPEN_GRAPH_SWITCH, true);
			sp.commit();
			if (session != null && session.isOpened() && !GoFacebookUtil.hasPermissions(session)) {
				GoFacebookUtil.requestNewPublishPermissions(session, mContext);
			} else if (session == null || !session.isOpened()) {
				GoFacebookUtil.shareALink((Activity) mContext);
			} else {
				finish();
			}
		}
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		mSession = Session.getActiveSession();
		if (mSession != null) {
			mSession.onActivityResult(this, requestCode, resultCode, data);
		}
		if (mSession == null || !mSession.isOpened()) {
			Toast.makeText(this, "Sorry,facebook login failed", Toast.LENGTH_SHORT).show();
			PreferencesManager sp = new PreferencesManager(mContext,
					IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
			sp.putBoolean(IPreferencesIds.FACEBOOK_OPEN_GRAPH_SWITCH, false);
			sp.commit();
		}
		finish();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
