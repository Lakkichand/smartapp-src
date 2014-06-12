package com.jiubang.ggheart.appgame.gostore.base.component;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.bean.AppDetailInfoBean;
import com.jiubang.ggheart.apps.gowidget.gostore.bean.DetailItemBean;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  lijunye
 * @date  [2012-10-29]
 */
public class GoStoreThemeDetailItemView {
	private Context mContext = null;
	private TextView mAuthorName = null;
	private TextView mThemePkgSize = null;
	private TextView mThemeVersion = null;
	private TextView mUpdateTime = null;
	private TextView mDonwloadCount = null;
	private TextView mThemeDescriptionTittle = null;
	private TextView mThemeDescription = null;
	private LinearLayout mMoreThemeDescription = null;
	private TextView mMoreThemeDescriptionImg = null;
	private TextView mThemeUpdateTittle = null;
	private TextView mThemeUpdate = null;
	private LinearLayout mMoreThemeUpdate = null;
	private TextView mMoreThemeUpdateImg = null;
	private ImageView mSecondLine = null;
	private LinearLayout mLinearLayout = null;
	private int mLineCount = 2;
	private boolean mIsNeedIconsView = true;
	public int mTextLineCount = 0;

	public GoStoreThemeDetailItemView(Context context) {
		mContext = context;
		if (DrawUtils.sDensity < 1) {
			mLineCount = 2;
		} else if (DrawUtils.sDensity < 1.5) {
			mLineCount = 4;
		} else {
			mLineCount = 5;
		}
	}

	/**
	 * <br>功能简述:初始化界面
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initView() {
		LayoutInflater layoutInflater = LayoutInflater.from(mContext);
		if (mIsNeedIconsView) {
			mLinearLayout = (LinearLayout) layoutInflater.inflate(
					R.layout.gostore_theme_detail_gallery_item_new, null);
		} else {
			mLinearLayout = (LinearLayout) layoutInflater.inflate(
					R.layout.gostore_theme_detail_gallery_item_new_noneicons, null);
		}
		mAuthorName = (TextView) mLinearLayout.findViewById(R.id.contentDeveloperTextView);
		mThemePkgSize = (TextView) mLinearLayout.findViewById(R.id.contentPackageSizeTextView);
		mThemeVersion = (TextView) mLinearLayout.findViewById(R.id.contentVersionTextView);
		mUpdateTime = (TextView) mLinearLayout.findViewById(R.id.contentUpateTimeTextView);
		mDonwloadCount = (TextView) mLinearLayout.findViewById(R.id.contentDownloadCountTextView);
		mThemeUpdateTittle = (TextView) mLinearLayout.findViewById(R.id.contentUpdateTitleTextView);
		mThemeUpdate = (TextView) mLinearLayout.findViewById(R.id.contentUpdateTextView);
		mMoreThemeUpdate = (LinearLayout) mLinearLayout.findViewById(R.id.moreUpdateTextView);
		mMoreThemeUpdateImg = (TextView) mLinearLayout.findViewById(R.id.moreUpdateTextView_img);
		mSecondLine = (ImageView) mLinearLayout.findViewById(R.id.secondLineImageView);
		mThemeDescriptionTittle = (TextView) mLinearLayout
				.findViewById(R.id.contentDescriptionTitleTextView);
		mThemeDescription = (TextView) mLinearLayout.findViewById(R.id.contentDescriptionTextView);
		mMoreThemeDescription = (LinearLayout) mLinearLayout.findViewById(R.id.moreDescriptionTextView);
		mMoreThemeDescriptionImg = (TextView) mLinearLayout.findViewById(R.id.moreDescriptionTextView_img);

	}

	/**
	 * <br>功能简述:设置是否需要显示底部icon列表，是否需要会导致大小不同
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param isNeed
	 */
	public void setIsNeedIconsView(boolean isNeed) {
		mIsNeedIconsView = isNeed;
	}

	/**
	 * <br>功能简述:设置主题版本
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text
	 */
	public void setmThemeVersion(String text) {
		String themeVersion = text;
		if (themeVersion == null || "".equals(themeVersion.trim())) {
			themeVersion = mContext.getResources().getString(R.string.themestore_infor_no_data);
		}
		if (mThemeVersion != null) {
			mThemeVersion.setText(mContext.getResources()
					.getString(R.string.app_detail_version_tip) + themeVersion);
		}
	}

	/**
	 * <br>功能简述:设置开发商
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text
	 */
	public void setmAuthorName(String text) {
		String authorName = text;
		if (authorName == null || "".equals(authorName.trim())) {
			authorName = mContext.getResources().getString(R.string.themestore_infor_no_data);
		}
		if (mAuthorName != null) {
			mAuthorName.setText(mContext.getResources().getString(
					R.string.themestore_detail_developer)
					+ authorName);
		}
	}

	/**
	 * <br>功能简述:设置主题包大小
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text
	 */
	public void setmThemePkgSize(String text) {
		String themePkgSize = text;
		if (themePkgSize == null || "".equals(themePkgSize.trim())) {
			themePkgSize = mContext.getResources().getString(R.string.themestore_infor_no_data);
		}
		if (mThemePkgSize != null) {
			mThemePkgSize.setText(mContext.getResources().getString(
					R.string.themestore_detail_pkgsize)
					+ themePkgSize);
		}
	}

	/**
	 * <br>功能简述:设置更新时间
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text
	 */
	public void setmUpdateTime(String text) {
		String updateTime = text;
		if (updateTime != null && !"".equals(updateTime.trim())) {
			int endIndex = updateTime.indexOf(" ");
			if (endIndex > 0) {
				updateTime = updateTime.substring(0, endIndex);
			}
			mUpdateTime.setText(mContext.getResources().getString(
					R.string.themestore_item_updatetime)
					+ " " + updateTime);
		} else {
			mUpdateTime.setText(mContext.getResources().getString(
					R.string.themestore_item_updatetime)
					+ " " + mContext.getResources().getString(R.string.themestore_unknow));
		}
	}

	/**
	 * <br>功能简述:设置下载量
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text
	 */
	public void setmDonwloadCount(String text) {
		String downloadCount = text;
		if (downloadCount == null || "".equals(downloadCount.trim())) {
			downloadCount = mContext.getResources().getString(R.string.themestore_infor_no_data);
		}
		mDonwloadCount.setText(mContext.getResources()
				.getString(R.string.themestore_download_count) + downloadCount);
	}

	/**
	 * <br>功能简述:设置主题简介
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text
	 */
	public void setmThemeDescription(String text) {
		if (!TextUtils.isEmpty(text)) {
			mThemeDescription.setText(text);
			mThemeDescription.setMaxLines(Integer.MAX_VALUE);
			mThemeDescription.setEllipsize(null);
//			if (descriptionLineCount > mLineCount) {
//				mMoreThemeDescription.setVisibility(View.VISIBLE);
//				mThemeDescription.setMaxLines(Integer.MAX_VALUE);
//				mThemeDescription.setEllipsize(null);
//				OnTouchListener onTouchListener = new OnTouchListener() {
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						int action = event.getAction();
//						switch (action) {
//							case MotionEvent.ACTION_MOVE :
//								return false;
//							case MotionEvent.ACTION_UP :
//								if (mThemeDescription.getEllipsize() == null) {
//									mThemeDescription.setMaxLines(mLineCount);
//									mThemeDescription.setEllipsize(TextUtils.TruncateAt.END);
//									// mScrollView.scrollTo(0, 0);
//									mMoreThemeDescription
//											.setBackgroundResource(R.drawable.gostore_theme_gallery_text_open);
//								} else {
//									mThemeDescription.setMaxLines(Integer.MAX_VALUE);
//									mThemeDescription.setEllipsize(null);
//									mMoreThemeDescription
//											.setBackgroundResource(R.drawable.gostore_theme_gallery_text_close);
//								}
//								break;
//							default :
//								break;
//						}
//						return true;
//					}
//				};
//				mMoreThemeDescription.setOnTouchListener(onTouchListener);
//				mThemeDescription.setOnTouchListener(onTouchListener);
//			}
		} else {
			mThemeDescriptionTittle.setVisibility(View.GONE);
			mThemeDescription.setVisibility(View.GONE);
			mMoreThemeDescription.setVisibility(View.GONE);
		}
	}

	/**
	 * <br>功能简述:设置主题更新内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param text
	 */
	public void setmThemeUpdate(String text) {
		if (!TextUtils.isEmpty(text)) {
			mThemeUpdate.setText(text);
			mThemeUpdate.setMaxLines(Integer.MAX_VALUE);
			mThemeUpdate.setEllipsize(null);
		} else {
			mThemeUpdateTittle.setVisibility(View.GONE);
			mThemeUpdate.setVisibility(View.GONE);
			mMoreThemeUpdate.setVisibility(View.GONE);
			mSecondLine.setVisibility(View.GONE);
		}

	}

	public View getView() {
		initView();
		return mLinearLayout;
	}
	/**
	 * <br>功能简述:获取详情文字页
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param detailItemBean 应用中心的详情bean
	 * @return
	 */
	public void getmScrollView(AppDetailInfoBean detailItemBean) {
		if (detailItemBean != null) {
			setmAuthorName(detailItemBean.mDeveloper);
			setmThemePkgSize(detailItemBean.mSize);
			setmThemeVersion(detailItemBean.mVersion);
			setmUpdateTime(detailItemBean.mUpdateTime);
			setmDonwloadCount(detailItemBean.mDownloadCount);
			setmThemeUpdate(detailItemBean.mUpdateLog);
			setmThemeDescription(detailItemBean.mDetail);
		}
//		return mLinearLayout;
	}

	/**
	 * <br>功能简述:获取详情文字页
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param detailItemBean Go精品的详情bean
	 * @return
	 */
	public View getmScrollView(DetailItemBean detailItemBean) {
		initView();
		if (detailItemBean != null) {
			setmAuthorName(detailItemBean.getDeveloper());
			setmThemePkgSize(detailItemBean.getInstallPackageSize());
			setmThemeVersion(detailItemBean.getVerString());
			setmUpdateTime(detailItemBean.getUpateTime());
			setmDonwloadCount(detailItemBean.getDownloadCount());
			setmThemeUpdate(detailItemBean.getUpdateContent());
			setmThemeDescription(detailItemBean.getDetailDescriptionString());
		}
		return mLinearLayout;
	}

	/**
	 * <br>功能简述: 设置更新内容过长时的点击效果
	 * <br>功能详细描述:
	 * <br>注意: 因为getLineCount()必须要在绘制完成后才能获得，所以使用异步线程等ui绘制完后执行
	 */
	public void setUpdateListener() {
		if (mMoreThemeUpdate != null && mThemeUpdate != null) {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					if (mThemeUpdate != null && mMoreThemeUpdate != null) {
						int descriptionLineCount = mThemeUpdate.getLineCount();
						if (descriptionLineCount > mLineCount) {
							mMoreThemeUpdate.setVisibility(View.VISIBLE);
							mThemeUpdate.setMaxLines(Integer.MAX_VALUE);
							mThemeUpdate.setEllipsize(null);
							OnTouchListener onTouchListener = new OnTouchListener() {
								@Override
								public boolean onTouch(View v, MotionEvent event) {
									int action = event.getAction();
									switch (action) {
										case MotionEvent.ACTION_MOVE :
											return false;
										case MotionEvent.ACTION_UP :
											if (mThemeUpdate.getEllipsize() == null) {
												mThemeUpdate.setMaxLines(mLineCount);
												mThemeUpdate.setEllipsize(TextUtils.TruncateAt.END);
												mMoreThemeUpdateImg
														.setBackgroundResource(R.drawable.gostore_theme_gallery_text_open);
											} else {
												mThemeUpdate.setMaxLines(Integer.MAX_VALUE);
												mThemeUpdate.setEllipsize(null);
												mMoreThemeUpdateImg
														.setBackgroundResource(R.drawable.gostore_theme_gallery_text_close);
											}
											break;
										default :
											break;
									}
									return true;
								}
							};
							mMoreThemeUpdate.setOnTouchListener(onTouchListener);
							mThemeUpdate.setOnTouchListener(onTouchListener);
						}
					}
				}
			}.execute();
		}
	}
	
	/**
	 * <br>功能简述: 设置简介内容过长时的点击效果
	 * <br>功能详细描述:
	 * <br>注意: 因为getLineCount()必须要在绘制完成后才能获得，所以使用异步线程等ui绘制完后执行
	 */
	public void setDescriptionListener() {
		if (mThemeDescription != null && mMoreThemeDescription != null) {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					if (mThemeDescription != null && mMoreThemeDescription != null) {
						int descriptionLineCount = mThemeDescription.getLineCount();
						if (descriptionLineCount > mLineCount) {
							mMoreThemeDescription.setVisibility(View.VISIBLE);
							mThemeDescription.setMaxLines(Integer.MAX_VALUE);
							mThemeDescription.setEllipsize(null);
							OnTouchListener onTouchListener = new OnTouchListener() {
								@Override
								public boolean onTouch(View v, MotionEvent event) {
									int action = event.getAction();
									switch (action) {
										case MotionEvent.ACTION_MOVE :
											return false;
										case MotionEvent.ACTION_UP :
											if (mThemeDescription.getEllipsize() == null) {
												mThemeDescription.setMaxLines(mLineCount);
												mThemeDescription.setEllipsize(TextUtils.TruncateAt.END);
												mMoreThemeDescriptionImg
														.setBackgroundResource(R.drawable.gostore_theme_gallery_text_open);
											} else {
												mThemeDescription.setMaxLines(Integer.MAX_VALUE);
												mThemeDescription.setEllipsize(null);
												mMoreThemeDescriptionImg
														.setBackgroundResource(R.drawable.gostore_theme_gallery_text_close);
											}
											break;
										default :
											break;
									}
									return true;
								}
							};
							mMoreThemeDescription.setOnTouchListener(onTouchListener);
							mThemeDescription.setOnTouchListener(onTouchListener);
						}
					}
				}
			}.execute();
		}
	}
	/**
	 * <br>功能简述:资源回收
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void recycle() {
		if (mContext != null) {
			mContext = null;
		}
		if (mAuthorName != null) {
			mAuthorName.setText(null);
			mAuthorName = null;
		}
		if (mThemePkgSize != null) {
			mThemePkgSize.setText(null);
			mThemePkgSize = null;
		}
		if (mThemeVersion != null) {
			mThemeVersion.setText(null);
			mThemeVersion = null;
		}
		if (mUpdateTime != null) {
			mUpdateTime.setText(null);
			mUpdateTime = null;
		}
		if (mDonwloadCount != null) {
			mDonwloadCount.setText(null);
			mDonwloadCount = null;
		}
		if (mThemeDescriptionTittle != null) {
			mThemeDescriptionTittle.setText(null);
			mThemeDescriptionTittle = null;
		}
		if (mThemeDescription != null) {
			mThemeDescription.setText(null);
			mThemeDescription = null;
		}
		if (mMoreThemeDescription != null) {
			mMoreThemeDescription.setOnTouchListener(null);
			mMoreThemeDescription = null;
		}
		if (mMoreThemeDescriptionImg != null) {
			mMoreThemeDescriptionImg.setBackgroundDrawable(null);
			mMoreThemeDescriptionImg = null;
		}
		if (mThemeUpdateTittle != null) {
			mThemeUpdateTittle.setText(null);
			mThemeUpdateTittle = null;
		}
		if (mThemeUpdate != null) {
			mThemeUpdate.setText(null);
			mThemeUpdate = null;
		}
		if (mMoreThemeUpdate != null) {
			mMoreThemeUpdate.setOnTouchListener(null);
			mMoreThemeUpdate = null;
		}
		if (mMoreThemeUpdateImg != null) {
			mMoreThemeUpdateImg.setBackgroundDrawable(null);
			mMoreThemeUpdateImg = null;
		}
		if (mSecondLine != null) {
			mSecondLine.setBackgroundDrawable(null);
			mSecondLine = null;
		}
		if (mLinearLayout != null) {
			mLinearLayout.setBackgroundDrawable(null);
			mLinearLayout = null;
		}
	}
}
