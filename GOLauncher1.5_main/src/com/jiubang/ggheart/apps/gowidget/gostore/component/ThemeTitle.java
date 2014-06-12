package com.jiubang.ggheart.apps.gowidget.gostore.component;


import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.appmanagement.component.AppsManageView;
import com.jiubang.ggheart.apps.appmanagement.controler.ApplicationManager;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreCore;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;

/**
 * 统一的标题栏组件
 * 
 * @author huyong
 * 
 */
public class ThemeTitle extends LinearLayout {

	private ImageView mLineImageViewOne = null;
	private ImageView mLineImageViewTwo = null;
	private ImageView mLineImageView3 = null;
//	private LinearLayout mAppCenterImageView = null;
//	private LinearLayout mGameCenterImageView = null;
	
	private TextView mGameCenterImageView = null;
	private TextView mAppCenterImageView = null;
	private TextView mImageView = null;
	private TextView mUpdateImageView = null;
	private TextView mBackView = null;

	
	public ThemeTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ThemeTitle(Context context) {
		super(context);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initElements();
	}

	private void initElements() {

		
		mBackView = (TextView) this.findViewById(R.id.themestore_top_title_back);
		
		mBackView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 需重写方法
			}
		});
		if (mBackView != null)
		{
			mBackView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.themestore_top_title_back, 0, 0, 0);
			mBackView.setCompoundDrawablePadding(10);
			mBackView.setPadding(10, 0, 0, 0);
		}
		mLineImageViewOne = (ImageView) this
				.findViewById(R.id.themestore_top_title_line_imageView1);
		mLineImageViewTwo = (ImageView) this
				.findViewById(R.id.themestore_top_title_line_imageView2);
		mLineImageView3 = (ImageView) this.findViewById(R.id.themestore_top_title_line_imageView3);
		
		mAppCenterImageView = (TextView) this.findViewById(R.id.themestore_top_title_appcenter);
		mGameCenterImageView = (TextView) this.findViewById(R.id.themestore_top_title_gamecenter);
		mImageView = (TextView) this.findViewById(R.id.themestore_top_title_imageView);
		mUpdateImageView = (TextView) this.findViewById(R.id.themestore_top_title_update);
		
		// 根据渠道配置信息，添加应用游戏中心入口
		ChannelConfig channelConfig = GoStoreCore.getChannelConfig(getContext());
		if (channelConfig != null) {
			if (channelConfig.isAddAppGoStoreTitleEntrance()) {
				showAppCenterIcon();
			}
//			if (channelConfig.isAddGameGoStoreTitleEntrance()) {
//				showGameCenterIcon();
//			}
		}
	}

	public void setBackViewImageVisable(int visibility) {
		if (mBackView != null)
		{
			if (visibility == VISIBLE)
			{
				mBackView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.themestore_top_title_back, 0, 0, 0);
				mBackView.setOnClickListener(null);
			}
			else
			{
				mBackView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}
		}
		
	}

	public void setBackViewOnClickListener(OnClickListener listener) {
		if (mBackView != null)
		{
			mBackView.setOnClickListener(listener);
		}
	}

	public void setBackViewImageResource(int resId) {
		if (mBackView != null)
		{
			mBackView.setBackgroundResource(resId);
		}
	}

	/**
	 * 显示搜索按钮的方法
	 */
	public void showSearchIcon() {
		if (mImageView != null) {
			mImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.themestore_seachicon_no, 0, 0);
			mImageView.setVisibility(VISIBLE);
			mImageView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.themestore_seachicon_light, 0, 0);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						mImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.themestore_seachicon_no, 0, 0);
						Context context = getContext();
						if (context == null) {
							return false;
						}
//						Intent intent = new Intent(context, BaseViewActivity.class);
//						Bundle bundle = new Bundle();
//						bundle.putString("title",
//								context.getString(R.string.themestore_search_input));
//						bundle.putLong("type", GoStorePublicDefine.VIEW_TYPE_SEARCH_INPUT);
//						intent.putExtras(bundle);
//						context.startActivity(intent);
					}
					return true;
				}
			});	
								
		}
		if (mLineImageViewTwo != null) {
			mLineImageViewTwo.setVisibility(VISIBLE);
		}
	}

	/**
	 * 不显示搜索按钮的方法
	 */
	public void dismissSearchIcon() {
		if (mImageView != null) {
			mImageView.setVisibility(GONE);
			mImageView.setOnTouchListener(null);
		}
		if (mLineImageViewTwo != null) {
			mLineImageViewTwo.setVisibility(GONE);
		}
	}

	public void setSearchIconListener(OnTouchListener listener) {
		mImageView.setOnTouchListener(listener);
	}

	/**
	 * 显示更新按钮
	 */
	public void showUpdateIcon() {
		if (mUpdateImageView != null) {
			mUpdateImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.apps_manager_update_no, 0, 0);
			mUpdateImageView.setVisibility(VISIBLE);
			mUpdateImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AppManagementStatisticsUtil.getInstance().saveCurrentEnter(getContext(),
							AppManagementStatisticsUtil.ENTRY_TYPE_GOSTORE);
					ApplicationManager.getInstance(getContext()).show(IDiyFrameIds.GO_STORE_FRAME,
							AppsManageView.APPS_UPDATE_VIEW_ID);
				}
			});
		}
	}

	/**
	 * 隐藏更新按钮
	 */
	public void dismissUpdateIcon() {
		if (mUpdateImageView != null) {
			mUpdateImageView.setVisibility(GONE);
			mUpdateImageView.setOnTouchListener(null);
		}
		if (mLineImageViewOne != null) {
			mLineImageViewOne.setVisibility(GONE);
		}
	}

	/**
	 * 显示应用中心按钮
	 */
	public void showAppCenterIcon() {
		if (mAppCenterImageView != null)
		{
			mAppCenterImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.gostore_title_apps_center_icon_no, 0, 0);
			mAppCenterImageView.setVisibility(VISIBLE);
			mAppCenterImageView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mAppCenterImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.gostore_title_apps_center_icon_light, 0, 0);
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						mAppCenterImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.gostore_title_apps_center_icon_no, 0, 0);
						AppManagementStatisticsUtil.getInstance().saveCurrentEnter(getContext(),
								AppManagementStatisticsUtil.ENTRY_TYPE_GOSTORE);
						AppsManagementActivity.startAppCenter(getContext(),
								MainViewGroup.ACCESS_FOR_GOSTORE, false);
					}
					return true;
				}
			});
		}
		if (mLineImageView3 != null) {
			mLineImageView3.setVisibility(VISIBLE);
		}
	}

	/**
	 * 隐藏应用中心按钮
	 */
	public void dismissAppCenterIcon() {
		if (mAppCenterImageView != null) {
			mAppCenterImageView.setVisibility(GONE);
			mAppCenterImageView.setOnTouchListener(null);
		}
		if (mLineImageView3 != null) {
			mLineImageView3.setVisibility(GONE);
		}
	}

//	/**
//	 * 显示应用中心按钮
//	 */
//	public void showGameCenterIcon() {
//		if (mGameCenterImageView != null) {
//			mGameCenterImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.gostore_title_game_zone_icon_no, 0, 0);
//			mGameCenterImageView.setVisibility(VISIBLE);
//			mGameCenterImageView.setOnTouchListener(new OnTouchListener() {
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					if (event.getAction() == MotionEvent.ACTION_DOWN) {
//						mGameCenterImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.gostore_title_game_zone_icon_light, 0, 0);
//					} else if (event.getAction() == MotionEvent.ACTION_UP) {
//						mGameCenterImageView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.gostore_title_game_zone_icon_no, 0, 0);
//						GameCenterStatisticsUtil.getInstance().saveCurrentEnter(getContext(),
//								GameCenterStatisticsUtil.ENTRY_TYPE_GOSTORE);
//						GameCenterActivity.startGameZone(getContext(),
//								MainViewGroup.ACCESS_FOR_GOSTORE);
//					}
//					return true;
//				}
//			});			
//		}
//		if (mGameCenterImageView != null) {
//			mGameCenterImageView.setVisibility(VISIBLE);
//			mGameCenterImageView.setOnTouchListener(new OnTouchListener() {
//
//				@Override
//				public boolean onTouch(View v, MotionEvent event) {
//					ImageView img = (ImageView) v.findViewById(R.id.image);
//					if (event.getAction() == MotionEvent.ACTION_DOWN) {
//						img.setBackgroundResource(R.drawable.gostore_title_game_zone_icon_light);
//					} else if (event.getAction() == MotionEvent.ACTION_UP) {
//						img.setBackgroundResource(R.drawable.gostore_title_game_zone_icon_no);
//						GameCenterStatisticsUtil.getInstance().saveCurrentEnter(getContext(),
//								GameCenterStatisticsUtil.ENTRY_TYPE_GOSTORE);
//						GameCenterActivity.startGameZone(getContext(),
//								MainViewGroup.ACCESS_FOR_GOSTORE);
//					}
//					return true;
//				}
//			});
//		}
//	}

	/**
	 * 隐藏应用中心按钮
	 */
	public void dismissGameCenterIcon() {
		if (mAppCenterImageView != null) {
			mAppCenterImageView.setVisibility(GONE);
			mAppCenterImageView.setOnTouchListener(null);
		}
	}

//	public TextView getTitleTextView() {
//		return mTitleTextView;
//	}
	public TextView getTitleTextView() {
		return mBackView;
	}

	public void setTitleText(String text) {
		if (text != null && mBackView != null) {
			mBackView.setTextColor(Color.parseColor("#ffffff"));
			mBackView.setText(text);
		}
	}
	public void setTitleText(String text, String color) {
		if (text != null && mBackView != null) {
			mBackView.setTextColor(Color.parseColor(color));
			mBackView.setText(text);
			mBackView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.app_center_back, 0, 0, 0);
			mBackView.setCompoundDrawablePadding(10);
			mBackView.setPadding(10, 0, 0, 0);
		}
	}

	public TextView getImageView() {
		return mImageView;
	}

	public ImageView getLineImageView() {
		return mLineImageViewTwo;
	}


	public TextView getUpdateImageView() {
		return mUpdateImageView;
	}
	
	public void recycle() {
		if (mImageView != null) {
			mImageView = null;
		}
		if (mUpdateImageView != null) {
			mUpdateImageView = null;
		}
		if (mLineImageViewOne != null) {
			mLineImageViewOne.setImageDrawable(null);
			mLineImageViewOne = null;
		}
		if (mLineImageViewTwo != null) {
			mLineImageViewTwo.setImageDrawable(null);
			mLineImageViewTwo = null;
		}
		if (mLineImageView3 != null) {
			mLineImageView3.setImageDrawable(null);
			mLineImageView3 = null;
		}
		if (mAppCenterImageView != null) {
			mAppCenterImageView = null;
		}
		if (mGameCenterImageView != null) {
			mGameCenterImageView = null;
		}
		if (mImageView != null)
		{
			mGameCenterImageView = null;
		}

	}
}
