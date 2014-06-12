package com.jiubang.ggheart.components.facebook;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingBackupActivity;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingFacebookActivity;
import com.jiubang.ggheart.apps.desks.diy.GoGuideActivity;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-11-30]
 */
public class SessionStatusCallback implements Session.StatusCallback {
	private Activity mActivity;
	private int mAccessType = GoFacebookUtil.TYPE_NONE;
	private Object mData;

	public SessionStatusCallback(Activity activity, int type, Object object) {
		super();

		mActivity = activity;
		mAccessType = type;
		mData = object;
	}

	@Override
	public void call(Session session, SessionState state, Exception exception) {
		onSessionStateChange(state, exception);
	}

	private void onSessionStateChange(SessionState state, Exception exception) {
		if (state.isOpened()) {
			GoFacebookUtil.log("facebook login success");

			getUserInfo();

			switch (mAccessType) {
				case GoFacebookUtil.TYPE_SHAREALINK :
//					GoFacebookUtil.showShareDialog(mActivity);
					
					if (mActivity instanceof DeskSettingFacebookActivity && !mActivity.isFinishing()) {
						((DeskSettingFacebookActivity) mActivity).updateFacebookView();
					} else if (mActivity instanceof GoGuideActivity && !mActivity.isFinishing()) {
						((GoGuideActivity) mActivity).updateFBView();
					}
					
					mAccessType = GoFacebookUtil.TYPE_NONE;
					break;
					
				case GoFacebookUtil.TYPE_SHAREALINK_RESOTRE:
					if (mData != null && mData instanceof String) {
						String msg = (String) mData;
						shareALink(GoFacebookUtil.sGOLAUNCHERPAGE_FACEBOOK, msg);
					}
					mAccessType = GoFacebookUtil.TYPE_NONE;
					break;
					
				case GoFacebookUtil.TYPE_POSTMSG:
					if (mData != null && mData instanceof String) {
						String msg = (String) mData;
						shareALink(null, msg);
					}
					
					mAccessType = GoFacebookUtil.TYPE_NONE;
					break;
					
				case GoFacebookUtil.TYPE_OPEN_GRAPH:
					if (mData != null && mData instanceof OpenGraphObject && mActivity != null && !mActivity.isFinishing()) {
						String pkgName = ((OpenGraphObject) mData).getPkgName();
						String name =  ((OpenGraphObject) mData).getThemeName();						
						FacebookOpenGraphUtil.requestFacebookOG(mActivity, pkgName, name, new OGRequestServerHandler());
					}
					mAccessType = GoFacebookUtil.TYPE_NONE;
					break;
				default :
					break;
			}

		} else {
			GoFacebookUtil.log("facebook login fail");
			
			UserInfo userInfo = GoFacebookUtil.getUserInfo();
			userInfo.setId(null);
			userInfo.setName(null);
			if (mActivity instanceof DeskSettingFacebookActivity && !mActivity.isFinishing()) {
				((DeskSettingFacebookActivity) mActivity).updateFacebookView();
			}
			if (mActivity instanceof DeskSettingBackupActivity && !mActivity.isFinishing()) {
				DeskSettingBackupActivity deskSettingBackupActivity = (DeskSettingBackupActivity) mActivity;
				deskSettingBackupActivity.updateFacebookView();
			}
		}
	}
	
	protected void setData(Object object) {
		mData = object;
	}

	protected void setAccessType(int type) {
		mAccessType = type;
	}

	public void shareALink(String link, String msg) {
		Session session = Session.getActiveSession();

		if (session != null) {

			Bundle postParams = new Bundle();
			postParams.putString("link", link);
			postParams.putString("message", msg);

			Request.Callback callback = new Request.Callback() {

				public void onCompleted(Response response) {
					try {
						JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
						String postId = null;
						try {
							postId = graphResponse.getString("id");
						} catch (JSONException e) {
							GoFacebookUtil.log("JSON error " + e.getMessage());
						}
						FacebookRequestError error = response.getError();
						if (error != null) {
							String failMSG = mActivity.getString(R.string.facebook_share_fail);
							Toast.makeText(mActivity, failMSG + error.getErrorMessage(),
									Toast.LENGTH_SHORT).show();
							GoFacebookUtil.log("fail to share");
						} else {
							String successMSG = mActivity
									.getString(R.string.facebook_share_success);
							Toast.makeText(mActivity.getApplicationContext(), successMSG,
									Toast.LENGTH_LONG).show();
							GoFacebookUtil.log("share success");
						}
					} catch (Throwable e) {
						String failMSG = mActivity.getString(R.string.facebook_share_fail);
						Toast.makeText(mActivity, failMSG + e.getMessage(), Toast.LENGTH_SHORT)
								.show();
						GoFacebookUtil.log("fail to share");
					}
				}
			};

			Request request = new Request(session, "me/feed", postParams, HttpMethod.POST, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}
	}

	protected void getUserInfo() {
		Session session = Session.getActiveSession();
		if (session != null) {
			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					try {
						JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
						String id = graphResponse.getString("id");
						String name = graphResponse.getString("name");

						UserInfo userInfo = GoFacebookUtil.getUserInfo();
						userInfo.setId(id);
						userInfo.setName(name);
						
						PreferencesManager preferencesManager = new PreferencesManager(mActivity,
								IPreferencesIds.FACEBOOK_RECORD, Context.MODE_PRIVATE);
						preferencesManager.putString(IPreferencesIds.FACEBOOK_LOGIN_AS_USER, name);
						preferencesManager.commit();
						if (mActivity instanceof DeskSettingFacebookActivity && !mActivity.isFinishing()) {
							((DeskSettingFacebookActivity) mActivity).updateFacebookView();
						}						
						if (mActivity instanceof DeskSettingBackupActivity && !mActivity.isFinishing()) {
							DeskSettingBackupActivity deskSettingBackupActivity = (DeskSettingBackupActivity) mActivity;
							deskSettingBackupActivity.updateFacebookView();
							
							switch (mAccessType) {
								case GoFacebookUtil.TYPE_BACKUP :
									FacebookBackupUtil.backupFacebookDB(deskSettingBackupActivity);
									mAccessType = GoFacebookUtil.TYPE_NONE;
									break;

								case GoFacebookUtil.TYPE_RESTORE :
									FacebookBackupUtil.restoreFacebookDB(deskSettingBackupActivity);
									mAccessType = GoFacebookUtil.TYPE_NONE;
									break;

								default :
									break;
							}
						}
					} catch (Throwable e) {
						String failMSG = mActivity.getString(R.string.facebook_share_fail);
						Toast.makeText(mActivity, failMSG + e.getMessage(), Toast.LENGTH_SHORT)
								.show();
						GoFacebookUtil.log("fail to get userinfo");
					}
				}
			};

			Request request = new Request(session, "me", null, HttpMethod.GET, callback);

			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}
	}
}
