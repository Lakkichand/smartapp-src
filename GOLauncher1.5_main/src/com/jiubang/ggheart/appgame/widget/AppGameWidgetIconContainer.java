package com.jiubang.ggheart.appgame.widget;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.appcenter.help.AppCacheManager;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;

/**
 * 显示应用游戏图标和网络异常界面
 * 
 * @author zhoujun
 * 
 */
public class AppGameWidgetIconContainer extends LinearLayout implements Observer {

	private Context mContext;
	private AppGameWidgetExceptionView mExceptionView;
	private AppGameWidgetIconView mViewSwitcher;
	private OnLongClickListener mLongClickListener;
	private AppGameWidgetDataManager mAppGameWidgetData;

	public AppGameWidgetIconContainer(Context context) {
		super(context);
		init(context);
	}

	public AppGameWidgetIconContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		mAppGameWidgetData = AppGameWidgetDataManager.getInstance(mContext);
		mAppGameWidgetData.addObserver(this);
	}

	public void showView(OnLongClickListener onLongClickListener) {
		mLongClickListener = onLongClickListener;
		// 没有网络并且本地也没有缓存数据时，显示网络异常，请求重试界面
		//		if (!Machine.isNetworkOK(getContext())
		//				&& !FileUtil.isFileExist(AppGameWidgetDataProvider.APPGAME_WIDGET_DATA_LOCAL_PATH)) {
		//			showExceptionView();
		//
		//			//清理掉内存里面的数据
		//			mAppGameWidgetData.cleanData();
		//		} else {
		//			showAppIconView();
		//		}

		// TODO:LIGUOLIANG 修改缓存管理方式
		if (!Machine.isNetworkOK(getContext())
				&& !AppCacheManager.getInstance().isCacheExist(
						AppGameWidgetDataProvider.KEY_CACHE_WIDGET)) {
			showExceptionView();

			//清理掉内存里面的数据
			mAppGameWidgetData.cleanData();
		} else {
			showAppIconView();
		}
	}

	/**
	 * 显示网络异常界面
	 */
	private void showExceptionView() {
		mExceptionView = new AppGameWidgetExceptionView(mContext);
		this.addView(mExceptionView);
	}

	/**
	 * 显示应用游戏图标的界面
	 */
	private void showAppIconView() {
		mViewSwitcher = new AppGameWidgetIconView(mContext, mLongClickListener, mHandler);
		this.addView(mViewSwitcher, new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));

		ArrayList<ClassificationDataBean> widgetDataList = mAppGameWidgetData.getWidgetData();
		if (widgetDataList != null) {
			if (mViewSwitcher != null) {
				mViewSwitcher.updateData(widgetDataList);
			}
		}

	}

	/**
	 * <br>功能简述:刷新当前显示的widget内容
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void refreshView() {
		if (mViewSwitcher != null) {
			if (mViewSwitcher.getParent() != null) {
				//当前正在展示数据页面,此时要切换内容
				if (mHandler != null) {
					mHandler.sendEmptyMessage(0);
				}
				return;
			}
		}

		//当前显示的是网络异常界面,如果有网络，要刷新数据
		if (Machine.isNetworkOK(getContext())) {
			mAppGameWidgetData.getWidgetData();
		}
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data != null) {
			ArrayList<ClassificationDataBean> widgetDataList = (ArrayList<ClassificationDataBean>) data;
			doOnHasWidgetData(widgetDataList);
		} else {
			//显示异常界面
			doOnNoWidgetData();
		}
	}

	/**
	 * <br>功能简述: 当获取到widget数据时，显示数据页面
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param widgetDataList
	 */
	private void doOnHasWidgetData(ArrayList<ClassificationDataBean> widgetDataList) {
		if (mViewSwitcher != null) {
			if (mViewSwitcher.getParent() == null) {
				this.addView(mViewSwitcher);
			}
			mViewSwitcher.updateData(widgetDataList);
		} else {
			showAppIconView();
		}
		if (mExceptionView != null) {
			this.removeView(mExceptionView);
			mExceptionView = null;
		}
	}

	/**
	 * <br>功能简述: 当获取不到widget数据时，显示网络异常界面
	 * <br>功能详细描述:
	 * <br>注意: 当本地没有缓存的widget数据， 并且没有网络时，才会出现获取不到widget的情况
	 */
	private void doOnNoWidgetData() {
		//显示异常界面
		if (mViewSwitcher != null) {
			this.removeView(mViewSwitcher);
		}
		if (mExceptionView == null) {
			showExceptionView();
		}
	}

	public void recycle() {
		if (mAppGameWidgetData != null) {
			mAppGameWidgetData.deleteObserver(this);
			int count = mAppGameWidgetData.countObservers();
			if (AppGameWidget.DEBUG) {
				Log.d(AppGameWidget.TAG, " recycle countObservers : " + count);
			}
			if (count == 0) {
				mAppGameWidgetData.cancel();
			}
			mAppGameWidgetData = null;
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			if (what == 0) {
				//每次滑动到widget所在的屏幕时，切换widget显示的内容
				if (mViewSwitcher != null) {
					mViewSwitcher.refreshView();
				}
				// 如果是初次安装滑动到widget所在屏幕，提示用户 上下滑动 可以切换widget内容
				if (!hasShowMessage()) {
					if (ChannelConfig.getInstance(mContext).isNeedAppCenter()) {
						Toast.makeText(mContext,
								mContext.getString(R.string.appgame_widget_scroll_message),
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(mContext,
								mContext.getString(R.string.appgame_widget_scroll_message_gostore),
								Toast.LENGTH_LONG).show();
					}
					saveShowMessage();
				}
			} else if (what == 1) {
				//清理掉缓存中的数据
				mAppGameWidgetData.cleanData();
				doOnNoWidgetData();
			}
		}

	};

	private boolean hasShowMessage() {
		if (mContext != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.APPGAME_WIDGET_SHOW_MESSAGE, Context.MODE_PRIVATE);
			return sharedPreferences.getBoolean("has_show_message", false);
		}
		return false;
	}

	private void saveShowMessage() {
		if (mContext != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.APPGAME_WIDGET_SHOW_MESSAGE, Context.MODE_PRIVATE);
			sharedPreferences.putBoolean("has_show_message", true);
			sharedPreferences.commit();
		}
	}
}
