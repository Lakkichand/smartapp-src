package com.jiubang.ggheart.components;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.plugin.mediamanagement.AppInfo;
import com.jiubang.ggheart.plugin.mediamanagement.ChooserListViewIcon;


/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @date  [2013-3-5]
 */
public class TouchHelperChooser {
	
	private Context mContext = null;
	private View mRootView = null;
	private Dialog mDialog = null;
	private TextView mTitle = null;
	private ListView mList = null;
	private ImageView mCheckImg = null;
	private boolean mIsCheck;
	private MyAdapter mAdapter = null;
	private List<AppInfo> mApps = null;
	private MsgHanlder mMsgHanlder;

	private int mDialogOriginalWidth;
	private int mDialogOriginalHeight;
	private int mItemHeight;
	private int mTopHeight;
	private int mBottomHeight;
	
	public TouchHelperChooser(Context context) {
		mContext = context;
		mApps = new ArrayList<AppInfo>();
		mAdapter = new MyAdapter();
		initDimenSize();
		mApps = getAllTouchhelper(context);
		initViews();
		// 注册消息接收器，接受横竖屏切换事件
		mMsgHanlder = new MsgHanlder();
		GoLauncher.registMsgHandler(mMsgHanlder);
	}
	
	private void initDimenSize() {
		mDialogOriginalHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_height);
		mDialogOriginalWidth = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_width);
		mItemHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_listitem_height);
		mTopHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_top_height);
		mBottomHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_bottom_height);

	}
	
	private void initViews() {
		LayoutInflater factory = LayoutInflater.from(mContext);
		mRootView = factory.inflate(
				R.layout.appfunc_mediamanagement_activitychooser, null);
		mTitle = (TextView) mRootView
				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_title);
		mTitle.setText(R.string.touchhelper_dialog_title);
		mCheckImg = (ImageView) mRootView
				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_img);
		TextView defaultText = (TextView) mRootView
				.findViewById(R.id.set_as_default);
		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsCheck) {
					mCheckImg
							.setImageResource(R.drawable.apps_uninstall_not_selected);
					mIsCheck = false;
				} else {
					mCheckImg
							.setImageResource(R.drawable.apps_uninstall_selected);
					mIsCheck = true;
				}

			}

		};
		defaultText.setOnClickListener(listener);
		mCheckImg.setOnClickListener(listener);

		mList = (ListView) mRootView
				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_list);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {
				AppInfo info = (AppInfo) view.getTag();
				open(info);
				mDialog.dismiss();
			}

		});
	}
	
	private void open(AppInfo info) {
		Intent intent = new Intent();
		ComponentName component = new ComponentName(info.pkName, info.actName);
		intent.setComponent(component);
		try {
			AppUtils.safeStartActivity(mContext, intent);
		} catch (Exception e) {
			return;
		}
		
		if (mIsCheck) {
			//写入默认
			PreferencesManager preferences = new PreferencesManager(mContext,
					IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
			preferences.putString(IPreferencesIds.DEFAULT_TOUCHHELPER_PKG, info.pkName);
			preferences.commit();
		}
	}
	
	/**
	 * 
	 * 适配器
	 */
	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (mApps == null) {
				return 0;
			}
			return mApps.size();
		}

		@Override
		public Object getItem(int i) {
			if (mApps == null) {
				return null;
			}
			if (i < 0 || i >= mApps.size()) {
				return null;
			}
			return mApps.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewgroup) {
			ChooserListViewIcon cv = null;
			if (view != null && view instanceof ChooserListViewIcon) {
				cv = (ChooserListViewIcon) view;
			} else {
				cv = new ChooserListViewIcon(mContext);
			}

			AppInfo info = mApps.get(i);
			cv.setTitle(info.displayName);
			cv.setIcon(info.icon);

			cv.setTag(info);
			return cv;
		}
	}
	

	
	/**
	 * <br>功能简述:获取默认touchhelper
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return null:没有默认　string:默认那个touchhelper的包名
	 */
	public static String getDefaultTouchhelperPkg(Context context) {
		PreferencesManager preferences = new PreferencesManager(context,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		String defaultPkg = preferences.getString(IPreferencesIds.DEFAULT_TOUCHHELPER_PKG, null);
		if (defaultPkg != null) {
			PackageManager pm = context.getPackageManager();
			try {
				Intent intent = pm.getLaunchIntentForPackage(defaultPkg);
				if (intent != null) {
					return defaultPkg;
				}
			} catch (Throwable e) {
				//DO NOTHING
			}
		}
		return null;
	}
	
	/**
	 * <br>功能简述:获取所有安装的touchhelper
	 * <br>功能详细描述:
	 * <br>注意:1.0版本的touchhelper没加同类intentfilter,做特殊处理
	 * @param context
	 * @return
	 */
	public static List<AppInfo> getAllTouchhelper(Context context) {
		ArrayList<AppInfo> list = new ArrayList<AppInfo>();
		PackageManager pm = context.getPackageManager();
		
		try {
			Intent oldTouchhelper = new Intent();
			oldTouchhelper.setPackage(LauncherEnv.GO_TOUCHHELPER_PACKAGE_NAME);
			List<ResolveInfo> oldTouchhelperList = pm.queryIntentActivities(oldTouchhelper, 0);
			if (oldTouchhelperList != null && !oldTouchhelperList.isEmpty()) {
				//先找指定包名1.0版touchhelper
				ResolveInfo resolveInfo = oldTouchhelperList.get(0);
				AppInfo info = new AppInfo();
				info.pkName = resolveInfo.activityInfo.packageName;
				info.actName = resolveInfo.activityInfo.name;
				info.displayName = resolveInfo.activityInfo.loadLabel(pm)
						.toString();
				ComponentName c = new ComponentName(info.pkName, info.actName);
				oldTouchhelper.setComponent(c);
				info.icon = AppDataEngine.getInstance(context)
						.getAppItemIconByIntent(oldTouchhelper);
				list.add(info);
			}
		} catch (Throwable e) {
			//DO NOTHING
		} 
		
		Intent intent = new Intent(ICustomAction.ACTION_TOUCHHELPER);
		intent.addCategory("android.intent.category.DEFAULT");
		List<ResolveInfo> queryList = pm.queryIntentActivities(intent, 0);
		if (queryList != null && !queryList.isEmpty()) {
			for (ResolveInfo resolveInfo : queryList) {
				AppInfo info = new AppInfo();
				info.pkName = resolveInfo.activityInfo.packageName;
				if (LauncherEnv.GO_TOUCHHELPER_PACKAGE_NAME.equals(info.pkName)) {
					continue;
				}
				info.actName = resolveInfo.activityInfo.name;
				info.displayName = resolveInfo.activityInfo.loadLabel(pm)
						.toString();
				ComponentName c = new ComponentName(info.pkName, info.actName);
				intent.setComponent(c);
				info.icon = AppDataEngine.getInstance(context)
						.getAppItemIconByIntent(intent);
				list.add(info);
			}
		}
		
		return list;
	}
	
	/**
	 * 显示对话框
	 * 
	 */
	public void showDialog() {
		// 计算对话框高度
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			// 竖屏
			params.height = getDialogHeight(mDialogOriginalHeight);
			params.width = mDialogOriginalWidth;
		} else {
			// 横屏
			params.height = getDialogHeight(mDialogOriginalWidth);
			params.width = mDialogOriginalHeight;
		}
		if (mDialog == null) {
			mDialog = new Dialog(mContext, R.style.media_open_chooser_dialog);
			mDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					mApps.clear();
					if (mMsgHanlder != null) {
						GoLauncher.unRegistMsgHandler(mMsgHanlder);
					}
				}
			});
			mDialog.addContentView(mRootView, params);
		} else {
			mDialog.setContentView(mRootView, params);
		}
		mDialog.show();
	}
	
	/**
	 * 计算对话框的高度
	 */
	private int getDialogHeight(int maxHeight) {
		int height = mTopHeight + mBottomHeight + mItemHeight * mApps.size();
		return maxHeight > height ? height : maxHeight;
	}
	
	/**
	 * 
	 * 消息队列
	 */
	public class MsgHanlder implements IMessageHandler {

		@Override
		public int getId() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean handleMessage(Object arg0, int arg1, int arg2, int arg3,
				Object arg4, List arg5) {
			switch (arg2) {
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED: 
				configurationChange(arg3);
				break;
			default:
				break;
			}
			return false;
		}
	}
	
	public void configurationChange(int configurationOrientation) {
		if (mRootView != null) {
			// android.view.ViewGroup.LayoutParams layoutParams =
			// mRootView.getLayoutParams();
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			if (configurationOrientation == Configuration.ORIENTATION_PORTRAIT) {
				layoutParams.height = getDialogHeight(mDialogOriginalHeight);
				layoutParams.width = mDialogOriginalWidth;
			} else if (configurationOrientation == Configuration.ORIENTATION_LANDSCAPE) {
				layoutParams.height = getDialogHeight(mDialogOriginalWidth);
				layoutParams.width = mDialogOriginalHeight;

			}
			mRootView.setLayoutParams(layoutParams);
		}

	}
	
	public boolean isShowing() {
		return mDialog != null ? mDialog.isShowing() : false;
	}
}
