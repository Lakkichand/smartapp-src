package com.jiubang.ggheart.appgame.widget;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;

/**
 * 应用游戏中心的widget
 * 
 * @author zhoujun
 * 
 */
public class AppGameWidget extends LinearLayout implements OnLongClickListener {
	public static final boolean DEBUG = false;
	public static final String TAG = "zj";
	private TextView mGameView;
	private TextView mAppView;
	private TextView mManagerView;
	private TextView mSearchView;
	private TextView mThemeView;
	private TextView mRecommendView;
	private TextView mLockerView;
	private TextView mSoftiew;
	private Context mContext;
	private AppGameWidgetIconContainer mAppwidgeIcon;
	private LinearLayout mAppGameWidget;
	//	private ImageView mBaseImage;
	/**
	 * 上次点击button时间，为了防止重复点击
	 */
	private long mLastClickTime = 0;

	/**
	 * 两次点击之间 默认最小的时间间隔
	 */
	private static final int MIN_TIME_INNTERVAL = 1000;

	public AppGameWidget(Context context) {
		super(context);
		mContext = context;
	}

	public AppGameWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void onStart(Bundle bundle) {
		//		int widgetId = bundle.getInt(GoWidgetConstant.GOWIDGET_ID);
		// Log.d("zj", "onStart is running : "+widgetId);
	}

	public void onDelete(int widgetId) {
		// Log.d("zj", "onDelete is running : "+widgetId);
		// 回收资源
		recycle();
	}

	public void onRemove(int widgetId) {
		// 回收资源
		// Log.d("zj", "onRemove is running : "+widgetId);
		recycle();

	}

	public void onEnter(int widgetId) {
		if (DEBUG) {
			Log.d(TAG, "onEnter widgetId:" + widgetId);
		}
		//		Toast.makeText(this.getContext(), "onEnter widgetId:", Toast.LENGTH_SHORT).show();
		if (mAppwidgeIcon != null) {
			mAppwidgeIcon.refreshView();
		}
	}

	public void onLeave(int widgetId) {
		if (DEBUG) {
			Log.d(TAG, "onLeave widgetId:" + widgetId);
		}
	}

	/**
	 * 回收资源的方法
	 */
	private void recycle() {
		if (mAppwidgeIcon != null) {
			mAppwidgeIcon.recycle();
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mAppGameWidget = (LinearLayout) this.findViewById(R.id.appgame_widget_41);
		mAppGameWidget.setOnLongClickListener(this);

		//		mBaseImage = ( ImageView ) this.findViewById(R.id.appgame_widget_bg);

		mAppwidgeIcon = (AppGameWidgetIconContainer) this
				.findViewById(R.id.appgame_widget_icon_grid);
		mAppwidgeIcon.showView(this);

		mGameView = (TextView) this.findViewById(R.id.appgame_widget_button_game);

		mAppView = (TextView) this.findViewById(R.id.appgame_widget_button_app);
		mManagerView = (TextView) this.findViewById(R.id.appgame_widget_button_manager);
		mManagerView.setVisibility(View.GONE);
		mSearchView = (TextView) this.findViewById(R.id.appgame_widget_button_search);
		mThemeView = (TextView) this.findViewById(R.id.appgame_widget_button_theme);
		
		mRecommendView = (TextView) this.findViewById(R.id.appgame_widget_button_recommend);
		mLockerView = (TextView) this.findViewById(R.id.appgame_widget_button_locker);
		mSoftiew = (TextView) this.findViewById(R.id.appgame_widget_button_soft);
		
		mGameView.setVisibility(View.GONE);
		mRecommendView.setVisibility(View.GONE);
		mSoftiew.setVisibility(View.GONE);
//		if (!ChannelConfig.getInstance(mContext).isNeedAppCenter()) {
//			mAppView.setVisibility(View.GONE);
//			mGameView.setVisibility(View.GONE);
//			mSearchView.setVisibility(View.GONE);
//		} else {
//			mRecommendView.setVisibility(View.GONE);
//			mLockerView.setVisibility(View.GONE);
//			mSoftiew.setVisibility(View.GONE);
//		}

		mGameView.setOnClickListener(mOnClickLister);
		mAppView.setOnClickListener(mOnClickLister);
		mManagerView.setOnClickListener(mOnClickLister);
		mSearchView.setOnClickListener(mOnClickLister);
		mThemeView.setOnClickListener(mOnClickLister);
		mRecommendView.setOnClickListener(mOnClickLister);
		mLockerView.setOnClickListener(mOnClickLister);
		mSoftiew.setOnClickListener(mOnClickLister);

	}

	private OnClickListener mOnClickLister = new OnClickListener() {
		@Override
		public void onClick(View v) {

			//在1s内多次点击，被视为重复点击，不做处理。
			long newClickTime = System.currentTimeMillis();
			if (newClickTime - mLastClickTime < MIN_TIME_INNTERVAL) {
				if (AppGameWidget.DEBUG) {
					Log.d(AppGameWidget.TAG, "AppGameWidget time is in :" + (newClickTime - mLastClickTime));
				}
				return;
			}
			mLastClickTime = newClickTime;
			// Home键跳转标识
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 0, null, null);
			if (v.getId() == mAppView.getId()) {
				// 跳转到应用中心
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_APPCENTER_APPS, false);
			} else if (v.getId() == mManagerView.getId()) {
				// 跳转到应用中心管理页面
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_WIDGET_MANAGER, false);
			} else if (v.getId() == mSearchView.getId()) {
				// 跳转到应用中心的搜索页面
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_WIDGET_SEARCH, false);
			} else if (v.getId() == mThemeView.getId()) {
				// 跳转到应用中心的主题页面
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
			} else if (v.getId() == mRecommendView.getId()) {
				// 跳转到应用中心的推荐页面
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_APPCENTER_RECOMMEND, false);
			} else if (v.getId() == mLockerView.getId()) {
				// 跳转到应用中心的锁屏页面
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_APPCENTER_LOCKER, false);
			} else if (v.getId() == mSoftiew.getId()) {
				// 跳转到应用中心的软件页面
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_APPCENTER_SOFTWARE, false);
			} else if (v.getId() == mGameView.getId()) {
				// 跳转到应用中心的游戏页面
				AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
						AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
						MainViewGroup.ACCESS_FOR_APPCENTER_GAME, false);
			}
			
		}
	};

	@Override
	public boolean onLongClick(View v) {
		return performLongClick();
	}

	/**
	 * 桌面更换Widget皮肤时回调此函数
	 * 
	 * @param bundle
	 */
	public boolean onApplyTheme(Bundle bundle) {
		boolean result = false;
		return result;
	}
}
