/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * @author ruxueqin
 * 
 */
public class SingleThemeView extends LinearLayout implements BroadCasterObserver {
	private ImageView mImageView;
	private String mImageId;
	private boolean mHasVideo = false;;
	private ImageView mVideoFlagView;
	private int mDefaultWidth = 0;
	
	/**
	 * @param context
	 */
	public SingleThemeView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public SingleThemeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 设置主题详情的预览图
	 * 
	 * @param bean
	 */
	public void setmThemeDetailData(ThemeInfoBean bean, String previewName, boolean isModify,
			boolean isFeaturedTheme) {
		try {
			mImageId = previewName;
			String path = null;
			if (bean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
				path = LauncherEnv.Path.GOTHEMES_PATH + "lockericon/";
			} else {
				path = LauncherEnv.Path.GOTHEMES_PATH + "icon/";
			}
			Drawable drawable = null;
			if (mHasVideo) { //如果有视频
				String vimagurl = bean.getVimgUrl();
				drawable = ThemeImageManager.getInstance(getContext()).getImageByUrl(vimagurl, this,
						path, previewName);
				if (drawable != null) {
					mImageView.setImageDrawable(drawable);
				} else {
					mImageView
					.setImageResource(R.drawable.themestore_detail_thumbnail_default_icon);
				}
				setImageSize();
			} else {
				if (isFeaturedTheme) {
//				String path = null;
//				if (bean.getBeanType() == ThemeConstants.LOCKER_FEATURED_THEME_ID) {
//					path = LauncherEnv.Path.GOTHEMES_PATH + "lockericon/";
//				} else {
//					path = LauncherEnv.Path.GOTHEMES_PATH + "icon/";
//				}
//				Drawable drawable = null;
					
					if (bean.getImgSource() == 0 || bean.getImgUrls() == null
							|| bean.getImgUrls().isEmpty()) { //如果服务器没下发url也拿ID取图片
						drawable = ThemeImageManager.getInstance(getContext().getApplicationContext())
								.getImageById(previewName, path, this);
						
					} else if (bean.getImgUrls() != null && !bean.getImgUrls().isEmpty()) {
						List<String> ids = bean.getImgIds();
						int index = -1;
						String url = null;
						if (ids != null && (index = ids.indexOf(previewName)) != -1) {
							if (index > -1 && index < bean.getImgUrls().size()) {
								url = bean.getImgUrls().get(index);
							}
						}
						if (url == null) {
							for (int i = 0; i < bean.getImgUrls().size(); i++) {
								if (bean.getImgUrls().get(i) != null
										&& bean.getImgUrls().get(i).contains(previewName)) {
									url = bean.getImgUrls().get(i);
									break;
								}
							}
						}
						drawable = ThemeImageManager.getInstance(getContext()).getImageByUrl(url, this,
								path, previewName);
					}
					if (drawable != null) {
						mImageView.setImageDrawable(drawable);
					} else {
						mImageView
						.setImageResource(R.drawable.themestore_detail_thumbnail_default_icon);
					}
					setImageSize();
				} else {
					drawable = ImageExplorer.getInstance(getContext()).getDrawable(
							bean.getPackageName(), previewName);
					mImageView.setImageDrawable(drawable);
					setImageSize();
					if (isModify) {
						setThemeOn();
					}
					findViewById(R.id.image_loading).setVisibility(GONE);
				}
			}
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}
	}
	
	public void setHasVideo(boolean has) {
		mHasVideo = has;
	}
	
	/**
	 * added by liulixia
	 * 设置默认高度值
	 * @param defaultWidth
	 */
	public void setDefaultWidth(int defaultWidth) {
		mDefaultWidth = defaultWidth;
	}
	
	/**
	 * added by liulixia
	 * 等比例缩放图片
	 * @param drawable
	 * @return
	 */
	private int[] getHeightAndWidth(Drawable drawable) {
		if (drawable != null) {
			int viewHeight = drawable.getIntrinsicHeight();
			int viewWidth = drawable.getIntrinsicWidth();
			int[] prop = new int[2];
			prop[0] = mDefaultWidth;
			int screenheight = getContext().getResources()
					.getDisplayMetrics().heightPixels;
			int top = DrawUtils.dip2px(48); //主题详情页title高度
			int bottom = SpaceCalculator.sPortrait ? DrawUtils.dip2px(53) : 0; //主题详情页应用按钮高度
			int indicator = SpaceCalculator.sPortrait ? DrawUtils.dip2px(52) : DrawUtils.dip2px(13);
			int maxHeight = screenheight - top - bottom - indicator;
			int layoutHeight = viewHeight * mDefaultWidth / viewWidth;
			if (maxHeight < layoutHeight) {
				layoutHeight = maxHeight;
				prop[0] = viewWidth * layoutHeight / viewHeight;
			} 
			prop[1] = layoutHeight;
			return prop;
		}
		return null;
	}
	
	/**
	 * added by liulixia
	 * 根据图片大小缩放图片
	 */
	private void setImageSize() {
		int size[] = getHeightAndWidth(mImageView.getDrawable());
		if (size != null) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(size[0], size[1]);
			mImageView.setLayoutParams(params); 
			//如果有视频，设置视频宽高
			if (mHasVideo) {
				int padding = DrawUtils.dip2px(14); //图像不可画区域
				int videoViewWidth = size[0] - padding;
				int videoViewHeight = size[1] - padding;
				params = new RelativeLayout.LayoutParams(videoViewWidth, videoViewHeight);
				params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
				mVideoFlagView.setLayoutParams(params);
			}
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mImageView = (ImageView) findViewById(R.id.image);
		mVideoFlagView = (ImageView) findViewById(R.id.videoFlag);
	}
	
//	public boolean isLoadedFinished() {
//		View view = findViewById(R.id.image_loading);
//		if (view.getVisibility() == GONE) {	//图片已加载完
//			return true;
//		}
//		return false;
//	}
	
	public void setOnlyImageBackgroud() {
		if (mImageView != null) {
			mImageView.setImageResource(R.drawable.themestore_detail_thumbnail_default_icon);
			setImageSize();
		}
	}

	public ImageView getImageView() {
		return mImageView;
	}
	
	public ImageView getVideoFlagView() {
		return mVideoFlagView;
	}
	/**
	 * 设置为当前主题，加标志图片
	 */
	private void setThemeOn() {
		try {
			RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.imagecontainer);
			ModifyImageView signImageView = new ModifyImageView(getContext());
			Drawable signDrawable = getContext().getResources().getDrawable(
					R.drawable.theme_modify_logo);
			signImageView.setImageDrawable(signDrawable);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			relativeLayout.addView(signImageView, params);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Throwable e) {
		}
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, Object object2) {
		// TODO Auto-generated method stub
		switch (msgId) {
			case ThemeImageManager.EVENT_LOCAL_ICON_EXIT :
			case ThemeImageManager.EVENT_NETWORK_ICON_CHANGE :
			case ThemeImageManager.EVENT_NETWORK_ICON_URL_CHANGE :
				if (object != null && object instanceof BitmapDrawable && object2 != null
						&& mImageId.endsWith((String) object2)) {
					mImageView.setImageDrawable((BitmapDrawable) object);
				} else if (object instanceof Bitmap) {
					mImageView.setImageBitmap((Bitmap) object);
				}
				setImageSize();
				findViewById(R.id.image_loading).setVisibility(GONE);
				if (msgId != ThemeImageManager.EVENT_LOCAL_ICON_EXIT) {
					Intent intent = new Intent(ICustomAction.ACTION_CHANGE_FULLSCREEN_IMAGE);
					intent.putExtra(ThemeConstants.CHANGE_FULLSCREEN_IMAGE_ID, mImageId);
					getContext().sendBroadcast(intent);
				}
				break;
			default :
				break;
		}
	}
}
