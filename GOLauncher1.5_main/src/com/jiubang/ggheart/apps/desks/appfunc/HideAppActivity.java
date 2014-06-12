package com.jiubang.ggheart.apps.desks.appfunc;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.apps.appfunc.controler.AppConfigControler;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.components.DeskButton;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.GoProgressBar;
import com.jiubang.ggheart.components.ModifySuccessTipView;
import com.jiubang.ggheart.components.MultiCheckViewGroup;
import com.jiubang.ggheart.components.OnMultiItemClickedListener;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.launcher.ThreadName;

/**
 * 选择隐藏应用的Activity
 * 
 * @author yangguanxiang
 * 
 */
public class HideAppActivity extends Activity
		implements
			OnClickListener,
			OnMultiItemClickedListener {

	public static final int UPDATEINDICATOR = 1;
	private static final int INITFINISH = 2;
	/**
	 * 提示页面的显示次数
	 */
	private static final int TIP_SHOW_TIMES = 3;
	private static final int DEFAULT = 0;

	private volatile ArrayList<Object> mList;
	/**
	 * 选钩列表
	 */
	private ArrayList<Boolean> mBooleanList;
	private ArrayList<Boolean> mBooleanCopyList;
	private AppConfigControler mAppConfigControler;
	private Object mMutex;
	private DeskButton mHideOK, mHideCancle;
	private RelativeLayout mContentLayout;
	private GoProgressBar mGoProgressBar;
	private MultiCheckViewGroup mMultiCheckViewGroup;
	private ModifySuccessTipView mSuccessTipView;
	private boolean mIsNeedShowTip = false;
	private int mShowTipCount;
	private boolean mIsModify;
	private int mModifyNum;
	private DeskTextView mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.app_func_hide_app_list);
		mGoProgressBar = (GoProgressBar) findViewById(R.id.appfunc_hide_app_progress);
		mMultiCheckViewGroup = (MultiCheckViewGroup) findViewById(R.id.multi_check_viewgroup);
		mAppConfigControler = AppFuncFrame.getAppConfigControler();
		mMutex = new Object();
		mContentLayout = (RelativeLayout) findViewById(R.id.contentview);
		mTitle = (DeskTextView) findViewById(R.id.title);
		mTitle.setText(R.string.app_fun_hide_app_title);
		mSuccessTipView = (ModifySuccessTipView) findViewById(R.id.modify_success_tip);
		mHideOK = (DeskButton) findViewById(R.id.hideok);
		mHideCancle = (DeskButton) findViewById(R.id.hidecancle);
		mHideOK.setOnClickListener(this);
		mHideCancle.setOnClickListener(this);
		mMultiCheckViewGroup.setMultiItemClickedListener(this);
		mSuccessTipView.setResource(R.drawable.appfunc_search_info,
				R.string.app_fun_hide_app_success_tip, R.string.app_fun_hide_app_success_title);
		mSuccessTipView.setHightLightText(getString(R.string.app_fun_hide_app_tip_hightlight));
		mSuccessTipView.setVisibility(View.INVISIBLE);
		checkShowTipCount();
		initList();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case INITFINISH :
					if (mList != null && mBooleanList != null) {
						mMultiCheckViewGroup.setContentList(mList, mBooleanList);
					}
					dismissProgressDialog();
					break;
				default :
					break;
			}
			// 取消加载框
			dismissProgressDialog();
		}
	};

	@Override
	protected void onDestroy() {
		synchronized (mMutex) {
			if (mList != null) {
				mList.clear();
			}
			if (mBooleanList != null) {
				mBooleanList.clear();
			}
			if (mBooleanCopyList != null) {
				mBooleanCopyList.clear();
			}
			if (mMultiCheckViewGroup != null) {
				mMultiCheckViewGroup.recyle();
			}
			if (mHideOK != null && mHideOK instanceof DeskButton) {
				((DeskButton) mHideOK).selfDestruct();
			}
			if (mHideCancle != null && mHideCancle instanceof DeskButton) {
				((DeskButton) mHideCancle).selfDestruct();
			}
			DeskSettingConstants.selfDestruct(mSuccessTipView);
			dismissProgressDialog();
			super.onDestroy();
		}
	}

	private void initList() {
		// 显示提示框
		showProgressDialog();

		new Thread(ThreadName.INIT_HIDE_APP_LIST) {
			@Override
			public void run() {
				synchronized (mMutex) {
					if (mAppConfigControler != null) {
						ArrayList<AppItemInfo> tmpList = mAppConfigControler.getAllAppItemInfos();
						if (tmpList != null) {
							mList = (ArrayList<Object>) tmpList.clone();
						}
					}
					if (mList == null) {
						mList = new ArrayList<Object>();
					}
					if (mBooleanList == null) {
						mBooleanList = new ArrayList<Boolean>();
					}
					if (mBooleanCopyList == null) {
						mBooleanCopyList = new ArrayList<Boolean>();
					}
					if (mList != null && null != mAppConfigControler) {
						int size = mList.size();
						for (int i = 0; i < size; i++) {
							boolean b = mAppConfigControler
									.isHideApp(((AppItemInfo) mList.get(i)).mIntent);
							mBooleanList.add(i, b);
							mBooleanCopyList.add(i, b);
						}
					}
					Message message = mHandler.obtainMessage();
					message.what = INITFINISH;
					mHandler.sendMessage(message);
				}
			}

		}.start();

	}

	private void showProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.INVISIBLE) {
			mGoProgressBar.setVisibility(View.VISIBLE);
		}
	}

	private void dismissProgressDialog() {
		if (mGoProgressBar != null && mGoProgressBar.getVisibility() == View.VISIBLE) {
			mGoProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.hideok :
				if (mModifyNum != 0 && mIsModify) {
					Boolean b = null;
					if (null != mList && null != mBooleanList && null != mAppConfigControler) {
						try {
							ArrayList<Intent> intentList = new ArrayList<Intent>();
							for (int i = 0; i < mBooleanList.size(); i++) {
								b = mBooleanList.get(i);
								if (b == Boolean.TRUE) {
									intentList.add(((AppItemInfo) mList.get(i)).mIntent);
								}
							}
							mAppConfigControler.addHideAppItems(intentList, true);
						} catch (DatabaseException e) {
							AppFuncExceptionHandler.handle(e);
						}
						mIsModify = false;
					}
					if (mIsNeedShowTip) {
						mMultiCheckViewGroup.setVisibility(View.INVISIBLE);
						mHideCancle.setVisibility(View.GONE);
						mSuccessTipView.setVisibility(View.VISIBLE);
						PreferencesManager sharedPreferences = new PreferencesManager(this,
								IPreferencesIds.HIDE_APP_SHOW_TIP, Context.MODE_PRIVATE);
						mShowTipCount++;
						sharedPreferences.putInt(IPreferencesIds.HIDE_APP_SHOW_TIP_COUNT,
								mShowTipCount);
						sharedPreferences.commit();
						mIsNeedShowTip = false;
					} else {
						DeskToast.makeText(this, R.string.app_fun_hide_app_success_toast,
								Toast.LENGTH_SHORT).show();
						finish();
					}
				} else {
					finish();
				}
				break;
			case R.id.hidecancle :
				finish();
				break;
			default :
				break;
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		android.view.ViewGroup.LayoutParams layoutParams = mContentLayout.getLayoutParams();
		layoutParams.height = (int) getResources().getDimension(R.dimen.folder_edit_view_height);
		layoutParams.width = (int) getResources().getDimension(R.dimen.folder_edit_view_width);
		mContentLayout.setLayoutParams(layoutParams);
		mMultiCheckViewGroup.onConfigurationChanged();
	}

	@Override
	public void onMultiItemClicked(int position, boolean isSelected) {
		// 点击图标的响应回调事件
		mIsModify = true;
		if (isSelected == mBooleanCopyList.get(position)) {
			mModifyNum--;
		} else {
			mModifyNum++;
		}

	}

	private void checkShowTipCount() {
		PreferencesManager sharedPreferences = new PreferencesManager(this,
				IPreferencesIds.HIDE_APP_SHOW_TIP, Context.MODE_PRIVATE);
		mShowTipCount = sharedPreferences.getInt(IPreferencesIds.HIDE_APP_SHOW_TIP_COUNT, DEFAULT);
		if (mShowTipCount >= TIP_SHOW_TIMES) {
			mIsNeedShowTip = false;
		} else {
			mIsNeedShowTip = true;
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
