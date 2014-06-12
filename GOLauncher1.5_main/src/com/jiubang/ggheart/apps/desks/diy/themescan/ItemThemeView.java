package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;

/**
 * 主题item视图
 * 
 * @author yangbing
 * 
 */
public class ItemThemeView extends LinearLayout implements BroadCasterObserver {

	private ImageView mImageView; // 图片
	private TextView mTextView; // 主题应用类型（如应用、下载、免费、付费等）
	private TextView mInsideTextView; // 名称
	private ThemeInfoBean mThemeData = null; // 主题数据结构体
	private RelativeLayout mImageContainer;
	private ImageView mCurSignImageView; // 当前主题标识
	private ImageView mNewSignImageView; // 大主题标识
	private ImageView mUpdateSignImageView; // Update主题标识
	private ImageView mGetjarImageView; // getjar主题标识
	private ImageView mNewPushView; // getjar主题标识
	private TextView mTitle; // getjar主题标识
	private int mPosition; // 位置
	private int mInsideTextViewPadding; //主题名称与图框的边距

	/**
	 * @return the mThemeData
	 */
	public ThemeInfoBean getThemeData() {
		return mThemeData;
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public ItemThemeView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void setText(String text) {
		mTextView.setText(text);
	}

	public void setThemeData(ThemeInfoBean bean, int position) {
		if (null == bean) {
			return;
		}
		mPosition = position;
		mThemeData = bean;
//		if (mThemeData.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
//				|| mThemeData.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID
//				|| mThemeData.getBeanType() == ThemeConstants.LAUNCHER_SPEC_THEME_ID
//				|| mThemeData.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID) {
//			mInsideTextView.setVisibility(View.VISIBLE);
//			mInsideTextView.setText(mThemeData.getThemeName());
//			mTextView.setVisibility(View.GONE);
//		} else {
//			mTextView.setText(mThemeData.getThemeName());
//			if (mTextView.getVisibility() != View.VISIBLE) {
//				mTextView.setVisibility(View.VISIBLE);
//			}
//			mInsideTextView.setVisibility(View.GONE);
//		}
		mInsideTextView.setVisibility(View.VISIBLE);
		mInsideTextView.setText(mThemeData.getThemeName());

		ThemeImageManager themeImageManager = ThemeImageManager.getInstance(getContext());
		Drawable drawable = themeImageManager.getImageByThemeInfo(mThemeData, this);
		if (drawable != null) {
			mImageView.setImageDrawable(drawable);
		} else {
			mImageView.setImageResource(R.drawable.theme_default_bg);
		}
		if (mThemeData.isCurTheme()) {
			addLogoForCurrentTheme();
		}
		if (mThemeData.isNewTheme()) {
			addLogoForNewTheme();
		}
		//去除掉getjar标签
//		String type = mThemeData.getThemeType();
//		int level = ThemePurchaseManager.getCustomerLevel(getContext());
//		if (level == ThemeConstants.CUSTOMER_LEVEL0) {
//			if (type != null && type.equals(ThemeInfoBean.THEMETYPE_GETJAR)
//					&& !mThemeData.isZipTheme()) {
//				boolean paid = false;
//				String packageName = mThemeData.getPackageName();
//				if (packageName != null) {
//					try {
//						Context context = getContext().createPackageContext(packageName,
//								Context.CONTEXT_IGNORE_SECURITY);
//						PreferencesManager preferences = new PreferencesManager(context,
//								packageName, Context.MODE_PRIVATE);
//						if (preferences != null) {
//							paid = preferences.getBoolean("key_paid_status", false);
//						}
//					} catch (NameNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//				if (!paid) {
//					addLogoForGetJarTheme(mThemeData.getBeanType());
//				}
//			} else if ((mThemeData.getBeanType() == ThemeConstants.LAUNCHER_FEATURED_THEME_ID
//					|| mThemeData.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID || mThemeData
//					.getBeanType() == ThemeConstants.LAUNCHER_HOT_THEME_ID)
//					&& mThemeData.getFeeType() == ThemeInfoBean.FEETYPE_GETJAR) {
//				addLogoForGetJarTheme(mThemeData.getBeanType());
//			}
//		}

		/**
		 * 添加判断当前的主题是否有更新 通过包名和版本号来判断UI3.0主题是不是最新的
		 */
		if (mThemeData.getPackageName() != null
				&& (mThemeData.getPackageName().equals(ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER) || mThemeData
						.getPackageName().equals(ThemeManager.DEFAULT_THEME_PACKAGE_3))) {
			if (mThemeData.getVerId() < ThemeManager.NEW_UI3_THEME_VERSION) {
				// 为旧版本的UI3.0贴上标签
				addLogoForUpdateTheme();
			}
		}

		if (mThemeData.getIsNew()) {
			addLogoForNew();
		}
	}

	/**
	 * 若主题是有更新的，则为主题添加标识图片（Update）
	 */
	private void addLogoForUpdateTheme() {
		try {
			mUpdateSignImageView = new ImageView(getContext());
			Drawable signDrawable = getContext().getResources()
					.getDrawable(R.drawable.theme_update);
			mUpdateSignImageView.setImageDrawable(signDrawable);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			mImageContainer.addView(mUpdateSignImageView, params);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Throwable e) {

		}
	}
	/**
	 * 若主题是有更新的，则为主题添加标识图片（Update）
	 */
	private void addThemeName() {
		try {
			mTitle = new TextView(getContext());
			mTitle.setBackgroundResource(R.drawable.theme_title_bg);
			mTitle.setText(mThemeData.getThemeName());
			mTitle.setTextColor(0xffffffff);
			mTitle.setTextSize(12);
			mTitle.setSingleLine();
			mTitle.setGravity(Gravity.CENTER);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) getContext()
					.getResources().getDimension(R.dimen.mytheme_pic_width) - 6,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(10, 0, 10, 18);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			mImageContainer.addView(mTitle, params);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Throwable e) {

		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mImageContainer = (RelativeLayout) findViewById(R.id.imagecontainer);
		mImageView = (ImageView) findViewById(R.id.image);
		mTextView = (TextView) findViewById(R.id.imgbtn_get);
		mInsideTextView = (TextView) findViewById(R.id.insidename);
		mInsideTextViewPadding = getContext().getResources().getDimensionPixelSize(R.dimen.mytheme_pic_padding);
		setItemThemeViewProperties();
	}
	
	/**
	 * added by liulixia
	 * 功能描述：设置主题预览项属性
	 */
	public void setItemThemeViewProperties() {
		if (SpaceCalculator.sPortrait) {
			int imageWidth = SpaceCalculator.getInstance(getContext()).getImageWidth();
			if (mImageContainer.getWidth() != imageWidth) {
				ViewGroup.LayoutParams params = mImageContainer.getLayoutParams();
				params.width = imageWidth;
				params.height = SpaceCalculator.getInstance(getContext()).getImageHeight();
				mImageContainer.setLayoutParams(params);
				
				params = mImageView.getLayoutParams();
				params.width = imageWidth;
				params.height = SpaceCalculator.getInstance(getContext()).getImageHeight();
				mImageView.setLayoutParams(params);
				
				params = mTextView.getLayoutParams();
				params.width = imageWidth;
				mTextView.setLayoutParams(params);
				
				params = mInsideTextView.getLayoutParams();
				params.width = imageWidth - mInsideTextViewPadding;
				mInsideTextView.setLayoutParams(params);
				
				this.setGravity(Gravity.LEFT);
			}
		} else {
			int imageWidth = getContext().getResources().getDimensionPixelSize(R.dimen.mytheme_pic_width);
			if (mImageContainer.getWidth() != imageWidth) {
				ViewGroup.LayoutParams params = mImageContainer.getLayoutParams();
				params.width = imageWidth;
				params.height = getContext().getResources().getDimensionPixelSize(R.dimen.mytheme_pic_height);
				mImageContainer.setLayoutParams(params);
				
				params = mImageView.getLayoutParams();
				params.width = imageWidth;
				params.height = getContext().getResources().getDimensionPixelSize(R.dimen.mytheme_pic_height);
				mImageView.setLayoutParams(params);
				
				params = mTextView.getLayoutParams();
				params.width = imageWidth;
				mTextView.setLayoutParams(params);
				
				params = mInsideTextView.getLayoutParams();
				params.width = getContext().getResources().getDimensionPixelSize(R.dimen.mytheme_name_width);
				mInsideTextView.setLayoutParams(params);
				this.setGravity(Gravity.CENTER);
			}
		}
	}

	/**
	 * 为当前主题加标志图片
	 */
	private void addLogoForCurrentTheme() {
		try {

			mCurSignImageView = new ImageView(getContext());
			Drawable signDrawable = getContext().getResources().getDrawable(R.drawable.theme_using);
			mCurSignImageView.setImageDrawable(signDrawable);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 0, 6);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			mImageContainer.addView(mCurSignImageView, params);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Throwable e) {
		}
	}

	/**
	 * 为大主题加标志图片
	 */
	private void addLogoForNewTheme() {
		try {
			mNewSignImageView = new ImageView(getContext());
			Drawable signDrawable = getContext().getResources().getDrawable(R.drawable.theme_new);
			mNewSignImageView.setImageDrawable(signDrawable);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			mImageContainer.addView(mNewSignImageView, params);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Throwable e) {
		}
	}

	private void addLogoForGetJarTheme(int type) {
		try {
			mGetjarImageView = new ImageView(getContext());
			Drawable getjarDrawable = null;
			getjarDrawable = getContext().getResources().getDrawable(R.drawable.theme_getfree);

			mGetjarImageView.setImageDrawable(getjarDrawable);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(6, 0, 0, 8);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			mImageContainer.addView(mGetjarImageView, params);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Throwable e) {
		}
	}

	/**
	 * <br>功能简述:是否是新推主题
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void addLogoForNew() {
		try {
			mNewPushView = new ImageView(getContext());
			mNewPushView.setImageResource(R.drawable.themestore_new);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			mImageContainer.addView(mNewPushView, params);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Throwable e) {
		}
	}

	public void cleanup() {
		if (mImageContainer != null) {
			if (mCurSignImageView != null) {
				mImageContainer.removeView(mCurSignImageView);
			}
			if (mNewSignImageView != null) {
				mImageContainer.removeView(mNewSignImageView);
			}
			if (mUpdateSignImageView != null) {
				mImageContainer.removeView(mUpdateSignImageView);
			}
			if (mGetjarImageView != null) {
				mImageContainer.removeView(mGetjarImageView);
			}
			if (mNewPushView != null) {
				mImageContainer.removeView(mNewPushView);
			}
			if (mTitle != null) {
				mImageContainer.removeView(mTitle);
			}
		}
		if (mImageView != null) {
			mImageView.setImageDrawable(null);
		}
		ThemeImageManager.getInstance(getContext()).unregisterImageObverser(mThemeData, this);
	}

	public int getmPosition() {
		return mPosition;
	}

	//	private static volatile int TCound = 0;
	@Override
	public void onBCChange(int msgId, int param, final Object object, Object object2) {
		switch (msgId) {
			case ThemeImageManager.FINISH_LOAD_IMAGE : {
				if (mImageView != null && object != null && object instanceof Drawable) {
					mImageView.setImageDrawable((Drawable) object);
				}
			}
				break;
			case ThemeImageManager.EVENT_LOCAL_ICON_EXIT :
			case ThemeImageManager.EVENT_NETWORK_ICON_CHANGE : {
				if (object == null) {
					break;
				}
				if (mImageView != null
						&& object != null
						&& object instanceof BitmapDrawable
						&& (object2 != null && mThemeData.getFirstPreViewDrawableName() != null && mThemeData
								.getFirstPreViewDrawableName().endsWith((String) object2))) {
					mImageView.setImageDrawable((BitmapDrawable) object);
				}
			}
				break;
			case ThemeImageManager.EVENT_NETWORK_EXCEPTION : {
				// Log.e("jason", "EVENT_NETWORK_EXCEPTION");
			}
			default :
				break;
		}
	}
}
