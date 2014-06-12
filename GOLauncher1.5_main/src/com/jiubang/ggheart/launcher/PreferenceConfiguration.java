package com.jiubang.ggheart.launcher;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.ISelfObject;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;

public class PreferenceConfiguration implements ISelfObject, BroadCasterObserver {
	// appearance
	// private int mTitleColor;
	// private int mTitleStyle;
	// private Drawable mSeparateLine;
	// private Drawable mScroll;
	// private Drawable mBackground;

	// item
	// private int mItemTitleColor;
	// private int mItemSummaryColor;
	// private Drawable mItemBackground;

	// category
	// private int mCategoryColor;
	// private Drawable mCategoryBackground;

	private DeskThemeBean.PreferenceAppearanceBean mAppearanceBean;

	private Context mContext;
	private static PreferenceConfiguration mInstance;

	public static synchronized PreferenceConfiguration getInstance() {
		return mInstance;
	}

	public static synchronized PreferenceConfiguration createInstance(Context context) {
		if (null == mInstance) {
			mInstance = new PreferenceConfiguration(context);
		}
		return mInstance;
	}

	public static synchronized void destroyInstance() {
		if (null != mInstance) {
			mInstance.selfDestruct();
			mInstance = null;
		}
	}

	private PreferenceConfiguration(Context context) {
		mContext = context;
		selfConstruct();
	}

	public boolean isPreferenceConfigurationValid() {
		return (mAppearanceBean != null);
	}

	public int getTitleColor() {
		int ret = 0;
		if (null != mAppearanceBean) {
			ret = mAppearanceBean.mTitleColor;
		}
		return ret;
	}

	public int getTitleStyle() {
		int ret = 0;
		if (null != mAppearanceBean) {
			ret = PreferenceStyleConfiguration.getStyle(mAppearanceBean.mTitleStyle);
		}
		return ret;
	}

	public Drawable getSeparateLine() {
		// if (null == mSeparateLine)
		// {
		// if (null != mAppearanceBean && null != mAppearanceBean.mSeparateLine)
		// {
		// mSeparateLine =
		// createDrawable(mAppearanceBean.mSeparateLine.mResName);
		// }
		// }
		// return mSeparateLine;
		Drawable ret = null;
		if (null != mAppearanceBean && null != mAppearanceBean.mSeparateLine) {
			ret = createDrawable(mAppearanceBean.mSeparateLine.mResName);
		}
		return ret;
	}

	public Drawable getScroll() {
		// if (null == mScroll)
		// {
		// if (null != mAppearanceBean && null != mAppearanceBean.mScroll)
		// {
		// mScroll = createDrawable(mAppearanceBean.mScroll.mResName);
		// }
		// }
		// return mScroll;
		Drawable ret = null;
		if (null != mAppearanceBean && null != mAppearanceBean.mScroll) {
			ret = createDrawable(mAppearanceBean.mScroll.mResName);
		}
		return ret;
	}

	public Drawable getBackground() {
		// if (null == mBackground)
		// {
		// if (null != mAppearanceBean && null != mAppearanceBean.mBackground)
		// {
		// mBackground = createDrawable(mAppearanceBean.mBackground.mResName);
		// }
		// }
		// return mBackground;
		Drawable ret = null;
		if (null != mAppearanceBean && null != mAppearanceBean.mBackground) {
			ret = createDrawable(mAppearanceBean.mBackground.mResName);
		}
		return ret;
	}

	public int getItemTitleColor() {
		int ret = 0;
		if (null != mAppearanceBean) {
			ret = mAppearanceBean.mItemTitleColor;
		}
		return ret;
	}

	public int getItemSummaryColor() {
		int ret = 0;
		if (null != mAppearanceBean) {
			ret = mAppearanceBean.mItemSummaryColor;
		}
		return ret;
	}

	public Drawable getItemBackground() {
		// // if (null == mItemBackground)
		// {
		// if (null != mAppearanceBean && null !=
		// mAppearanceBean.mItemBackground)
		// {
		// mItemBackground =
		// createDrawable(mAppearanceBean.mItemBackground.mResName);
		// }
		// }
		// return mItemBackground;
		Drawable ret = null;
		if (null != mAppearanceBean && null != mAppearanceBean.mItemBackground) {
			ret = createDrawable(mAppearanceBean.mItemBackground.mResName);
		}
		return ret;
	}

	public int getCategoryColor() {
		int ret = 0;
		if (null != mAppearanceBean) {
			ret = mAppearanceBean.mCategoryColor;
		}
		return ret;
	}

	public Drawable getCategoryBackground() {
		// // if (null == mCategoryBackground)
		// {
		// if (null != mAppearanceBean && null !=
		// mAppearanceBean.mCategoryBackground)
		// {
		// mItemBackground =
		// createDrawable(mAppearanceBean.mCategoryBackground.mResName);
		// }
		// }
		// return mCategoryBackground;
		Drawable ret = null;
		if (null != mAppearanceBean && null != mAppearanceBean.mCategoryBackground) {
			ret = createDrawable(mAppearanceBean.mCategoryBackground.mResName);
		}
		return ret;
	}

	@Override
	public void selfConstruct() {
		initData();
	}

	@Override
	public void selfDestruct() {
		cleanData();
		mContext = null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.EVENT_THEME_CHANGED :
				cleanData();
				initData();
				break;

			default :
				break;
		}
	}

	private void initData() {
		if (null != AppCore.getInstance()) {
			DeskThemeBean bean = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean();
			if (null != bean) {
				mAppearanceBean = bean.mPreferenceAppearanceBean;
			}
		}
	}

	private void cleanData() {
		// mTitleColor = 0;
		// mTitleStyle = 0;
		// mSeparateLine = null;
		// mScroll = null;
		// mBackground = null;
		//
		// mItemTitleColor = 0;
		// mItemSummaryColor = 0;
		// mItemBackground = null;
		//
		// mCategoryColor = 0;
		// mCategoryBackground = null;

		mAppearanceBean = null;
	}

	private Drawable createDrawable(String name) {
		return ImageExplorer.getInstance(mContext).getDrawable(name);
	}
}
