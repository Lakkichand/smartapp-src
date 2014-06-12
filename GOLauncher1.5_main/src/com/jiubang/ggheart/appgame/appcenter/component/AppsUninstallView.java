package com.jiubang.ggheart.appgame.appcenter.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.go.root.Commander;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author rongjinsong
 * @date [2012-9-27]
 */
public class AppsUninstallView extends LinearLayout {
	private LayoutInflater mInflater;
	private AppsUninstallContainer mMyAppsListContainer = null; // 卸载列表

	// public boolean mIsRoot = false; //是否以ROOT卸载

	/**
	 * 是否在打开批量卸载页面,防止重复点击
	 */
	private boolean mIsOpenUninstallView = false;
	/**
	 * 用户未选择是否使用ROOT权限
	 */
	public final static int ROOT_NOT_SELECT = 0;
	/**
	 * 用户允许使用ROOT权限
	 */
	public final static int ROOT_ALLOW = 1;
	/**
	 * 用户不允许使用ROOT权限
	 */
	public final static int ROOT_NOT_ALLOW = 2;

	// public static int mIsAllowRoot = ROOT_NOT_SELECT;

	private Context mContext;

	public AppsUninstallView(Context context) {
		super(context);
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		initView();
	}

	// public void setHandler(Handler handler) {
	// mMyAppsListContainer.setHandler(handler);
	// }

	/**
	 * 初始化界面的方法
	 */
	private void initView() {
		setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(Color.parseColor("#faf9f9"));
		// 初始化列表
		initListView();
		showUninstallAppView();
	}

	/**
	 * 初始化列表的方法
	 */
	private void initListView() {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		mMyAppsListContainer = (AppsUninstallContainer) mInflater.inflate(
				R.layout.recomm_apps_management_appsuninstall_layout, null);
		addView(mMyAppsListContainer, params);
		// if (mIsAllowRoot == ROOT_ALLOW) {
		// mMyAppsListContainer.setIsRootUninstall(true);
		// } else {
		// mMyAppsListContainer.setIsRootUninstall(false);
		// }
		this.setFocusable(true);
		this.requestFocus();
	}

	// /**
	// * 设置用户是否允许ROOT
	// * @param isRoot
	// */
	// public void setRootStatus (boolean isRoot) {
	// mIsRoot = isRoot;
	// mMyAppsListContainer.setIsRootUninstall(mIsRoot);
	// }

	public void updateList() {
		if (mMyAppsListContainer != null) {
			mMyAppsListContainer.updateList(null);
		}
	}

	public void cleanup() {
		if (mMyAppsListContainer != null) {
			mMyAppsListContainer.cleanup();
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			if (mMyAppsListContainer != null) {
				mMyAppsListContainer.back();
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (mMyAppsListContainer != null) {
				mMyAppsListContainer.back();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showUninstallAppView() {
		if (mIsOpenUninstallView) {
			return;
		}
		mIsOpenUninstallView = true;
		if (!hasShowedDialog()) {
			// 显示弹出框
			showDialog();
		} else {
			getRoot();
		}
		// else {
		// initListView();
		// // getRoot();
		// }
	}

	// private void getRoot() {
	// //直接拿root权限
	// if (RootUtils.isAccessGiven()) {
	// mMyAppsListContainer.setIsRootUninstall(true);
	// } else {
	// if (RootUtils.isRootAvailable()) {
	// try {
	// List<String> commandsList = RootUtils.sendShell(new String[] { "su" }, 0,
	// 10000);
	// if (commandsList != null && commandsList.size()>0) {
	// for (String commands : commandsList) {
	// if ("Permission denied".equals(commands)) {
	// mMyAppsListContainer.setIsRootUninstall(false);
	// return ;
	// }
	// }
	// //mMyAppsListContainer.setIsRootUninstall(true);
	// }
	//
	// } catch (Exception e) {
	// // e.printStackTrace();
	// mMyAppsListContainer.setIsRootUninstall(true);
	// // mMyAppsListContainer.setIsRootUninstall(false);
	// }
	// }
	// }
	// }

	private boolean hasShowedDialog() {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.APPS_UNINSTALL_SHOW_DIALOG,
				Context.MODE_PRIVATE);
		return preferences.getBoolean("hasShow", false);
	}

	private void savePreferences() {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.APPS_UNINSTALL_SHOW_DIALOG,
				Context.MODE_PRIVATE);
		preferences.putBoolean("hasShow", true);
		preferences.commit();
	}

	private void showDialog() {
		new AlertDialog.Builder(mContext)
				.setTitle(
						mContext.getString(R.string.appsuninstall_root_alert_title))
				.setMessage(
						mContext.getString(R.string.appsuninstall_root_alert_text))
				.setPositiveButton(mContext.getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								savePreferences();
								getRoot();
							}
						})
				.setNegativeButton(mContext.getString(R.string.cancle),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mMyAppsListContainer.setIsRootUninstall(false);
							}
						}).show();
	}

	private void getRoot() {
		boolean isRoot = false;
		int rootType = StatisticsData.ROOT_INFO_NO_ROOT;
		if (CommandManager.findSu()) {
			// rootType = StatisticsData.ROOT_INFO_HAS_ROOT;
			isRoot = Commander.getInstance().requireRootAccess();
			if (!isRoot) {
				rootType = StatisticsData.ROOT_INFO_REFUSE_ROOT;
			} else {
				rootType = StatisticsData.ROOT_INFO_ACCEPT_ROOT;
			}
		}
		StatisticsData.saveRootPreferences(mContext, rootType);
		mMyAppsListContainer.setIsRootUninstall(isRoot);
	}

	//
	// private void getRoot() {
	// new Thread(new Runnable() {
	// public void run() {
	// boolean hasRoot = CommandManager.getInstance().getRoot();
	// if (hasRoot) {
	// mIsAllowRoot = ROOT_ALLOW;
	// mMyAppsListContainer.setIsRootUninstall(true);
	// } else {
	// // Toast.makeText(
	// // mContext,
	// // mContext.getString(R.string.getroot_failed),
	// // Toast.LENGTH_LONG).show();
	// mIsAllowRoot = ROOT_NOT_ALLOW;
	// }
	// }
	// }).start();
	// }
}
