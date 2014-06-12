/*
 * 文 件 名:  FeatureTwoCellAdapter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-10-26
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.net.InstallCallbackManager;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.AppGameInstallingValidator;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-10-26]
 */
public class FeatureTwoCellAdapter extends BaseAdapter {
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	/**
	 * 火焰动画帧集合
	 */
	private List<Bitmap> mBurningFrame = null;
	/**
	 * 该adapter对应的精品推荐页是否在激活状态
	 */
	private boolean mIsActive;
	/**
	 * 数据源，与container的数据不是同一个对象
	 */
	private List<BoutiqueApp> mDataSource = new ArrayList<BoutiqueApp>();
	/**
	 * 进入应用游戏中心时的DownloadManager里的下载任务列表，每次更新数据时都根据列表初始化应用的下载信息
	 */
	private List<DownloadTask> mDownloadTaskList = null;
	/**
	 * 默认图标
	 */
	private Bitmap mDefaultBitmap = null;
	/**
	 * 图标有火焰时要图标设置padding
	 */
	private int mBurningPadding = DrawUtils.dip2px(1);

	private AsyncImageManager mImgManager = null;

	private FeatureController mFeatureController = null;

	public FeatureTwoCellAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mImgManager = AsyncImageManager.getInstance();
		initBurningFrame();
	}

	/**
	 * 设置列表展现的默认图标
	 */
	public void setDefaultIcon(Drawable drawable) {
		if (drawable != null && drawable instanceof BitmapDrawable) {
			mDefaultBitmap = ((BitmapDrawable) drawable).getBitmap();
		}
	}

	/**
	 * 设置controller,用于跳转详情
	 */
	public void setFeatureController(FeatureController featureController) {
		mFeatureController = featureController;
	}
	/**
	 * 初始化火焰动画帧
	 */
	private void initBurningFrame() {
		mBurningFrame = new ArrayList<Bitmap>();
		Resources res = mContext.getResources();
		Bitmap bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_1)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_2)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_3)).getBitmap();
		mBurningFrame.add(bm);
		bm = ((BitmapDrawable) res.getDrawable(R.drawable.appgame_fire_4)).getBitmap();
		mBurningFrame.add(bm);
	}

	private OnClickListener mClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object tag = v.getTag(R.id.appgame);
			if (tag != null && tag instanceof BoutiqueApp) {
				BoutiqueApp app = (BoutiqueApp) tag;
				if (app != null) {
					// 判断treatment的值
					if (app.info.treatment > 0) {
						InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
					}
					// 判断是否需要安装成功之后回调
					if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
						InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
					}
					mFeatureController.onItemClick(v.getContext(), app);
				}
			}
		}
	};

	@Override
	public int getCount() {
		if (mDataSource == null) {
			return 0;
		}
		if (mDataSource.size() % 2 != 0) {
			return mDataSource.size() / 2 + 1;
		} else {
			return mDataSource.size() / 2;
		}
	}

	@Override
	public Object getItem(int position) {
		try {
			return mDataSource.get(position / 2);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//		Log.e("XIEDEZHI", "FeatureAdapter getview position = " + position);
		// TODO:XIEDEZHI getview是不要生成对象
		if (position < 0 || position >= mDataSource.size()) {
			return convertView;
		}
		FeatureViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.apps_mgr_feature_twocell, null);
			holder = new FeatureViewHolder();
			holder.mLeftView = convertView.findViewById(R.id.container_left);
			holder.mFeatureIconLeftStub = (ViewStub) holder.mLeftView
					.findViewById(R.id.feature_icon_stub);
			holder.mAppIconLeft = (BurningIcon) holder.mLeftView
					.findViewById(R.id.feature_app_icon);
			holder.mAppIconLeftAnother = (BurningIcon) holder.mLeftView
					.findViewById(R.id.feature_app_another_icon);
			holder.mImageSwitcherLeft = (ImageSwitcher) holder.mLeftView
					.findViewById(R.id.feature_image_switcher);
			holder.mAppNameLeft = (TextView) holder.mLeftView.findViewById(R.id.feature_name);
			holder.mRatingBarLeft = (RatingBar) holder.mLeftView.findViewById(R.id.feature_rating);
			holder.mProgressTextLeftStub = (ViewStub) holder.mLeftView
					.findViewById(R.id.feature_progress_stub);

			holder.mRightView = convertView.findViewById(R.id.container_right);
			holder.mFeatureIconRightStub = (ViewStub) holder.mRightView
					.findViewById(R.id.feature_icon_stub);
			holder.mAppIconRight = (BurningIcon) holder.mRightView
					.findViewById(R.id.feature_app_icon);
			holder.mAppIconRightAnother = (BurningIcon) holder.mRightView
					.findViewById(R.id.feature_app_another_icon);
			holder.mImageSwitcherRight = (ImageSwitcher) holder.mRightView
					.findViewById(R.id.feature_image_switcher);
			holder.mAppNameRight = (TextView) holder.mRightView.findViewById(R.id.feature_name);
			holder.mRatingBarRight = (RatingBar) holder.mRightView
					.findViewById(R.id.feature_rating);
			holder.mProgressTextRightStub = (ViewStub) holder.mRightView
					.findViewById(R.id.feature_progress_stub);
			convertView.setTag(holder);
		} else {
			holder = (FeatureViewHolder) convertView.getTag();
		}
		//恢复内部子view的可见性
		holder.setVisibility();
		for (int i = 0; i < 2; i++) {
			final int nPosition = position * 2 + i;
			BoutiqueApp app = null;
			if (nPosition < mDataSource.size()) {
				app = mDataSource.get(nPosition);
			}
			// 左边
			if (i == 0) {
				if (app == null) {
					holder.mLeftView.setBackgroundDrawable(new ColorDrawable(0));
					holder.mLeftView.setOnClickListener(null);
					if (holder.mFeatureIconLeft != null) {
						holder.mFeatureIconLeft.setVisibility(View.INVISIBLE);
					}
					holder.mImageSwitcherLeft.setVisibility(View.INVISIBLE);
					holder.mAppNameLeft.setVisibility(View.INVISIBLE);
					holder.mRatingBarLeft.setVisibility(View.INVISIBLE);
					if (holder.mProgressTextLeft != null) {
						holder.mProgressTextLeft.setVisibility(View.INVISIBLE);
					}
				} else {
					if (mIsActive) {
						int effect = app.info.effect;
						if (effect == 1) {
							boolean isInstall = AppGameInstallingValidator.getInstance()
									.isAppExist(mContext, app.info.packname);
							if (isInstall) {
								effect = 0;
							} else {
								if (DownloadUtil.checkViewedEffectApp(mContext, app.info.packname)) {
									effect = 0;
								}
							}
						}
						setIcon(position, holder.mImageSwitcherLeft, app.info.icon, app.picLocalPath,
								app.picLocalFileName, mDefaultBitmap, effect, true);
						if (holder.mFeatureIconLeft != null) {
							holder.mFeatureIconLeft.setImageDrawable(null);
						}
						// 设置特性图标，“必备”，“首发”，“最新”等等
						if (!TextUtils.isEmpty(app.info.ficon)) {
							if (holder.mFeatureIconLeft == null) {
								holder.mFeatureIconLeftStub.inflate();
								holder.mFeatureIconLeft = (ImageView) holder.mLeftView
										.findViewById(R.id.feature_icon);
							}
							if (holder.mFeatureIconLeft != null) {
								holder.mFeatureIconLeft.setVisibility(View.VISIBLE);
								setFeatureIcon(position, holder.mFeatureIconLeft, app.info.ficon,
										app.picLocalPath, app.localFeatureFileName);
							}
						}
					} else {
						((ImageView) holder.mImageSwitcherLeft.getCurrentView())
								.setImageBitmap(mDefaultBitmap);
						if (holder.mFeatureIconLeft != null) {
							holder.mFeatureIconLeft.setImageDrawable(null);
						}
					}
					// 名字
					holder.mAppNameLeft.setText(app.info.name);
					// 如果正在下载，则显示状态
					if (hasDownloadState(app)) {
						int state = app.downloadState.state;
						String progress = "";
						if (state == DownloadTask.STATE_WAIT || state == DownloadTask.STATE_START
								|| state == DownloadTask.STATE_RESTART) {
							// 等待下载
							progress = mContext.getString(R.string.download_manager_wait);
						} else if (state == DownloadTask.STATE_DOWNLOADING) {
							progress = app.downloadState.alreadyDownloadPercent + "%";
						} else if (state == DownloadTask.STATE_STOP) {
							progress = mContext.getString(R.string.download_manager_pause);
						}
						if (holder.mProgressTextLeft == null) {
							holder.mProgressTextLeftStub.inflate();
							holder.mProgressTextLeft = (TextView) holder.mLeftView
									.findViewById(R.id.feature_progress);
						}
						if (holder.mProgressTextLeft != null) {
							holder.mProgressTextLeft.setVisibility(View.VISIBLE);
							holder.mProgressTextLeft.setText(progress);
						}
						holder.mRatingBarLeft.setVisibility(View.GONE);
					} else {
						float grade = app.info.grade / 2.0f;
						// 没有下载，则显示星级
						holder.mRatingBarLeft.setRating(grade);
					}
					holder.mLeftView.setTag(R.id.appgame, app);
					holder.mLeftView.setOnClickListener(mClickListener);
				}
			} else if (i == 1) {
				if (app == null) {
					holder.mRightView.setBackgroundDrawable(new ColorDrawable(0));
					holder.mRightView.setOnClickListener(null);
					if (holder.mFeatureIconRight != null) {
						holder.mFeatureIconRight.setVisibility(View.INVISIBLE);
					}
					holder.mImageSwitcherRight.setVisibility(View.INVISIBLE);
					holder.mAppNameRight.setVisibility(View.INVISIBLE);
					holder.mRatingBarRight.setVisibility(View.INVISIBLE);
					if (holder.mProgressTextRight != null) {
						holder.mProgressTextRight.setVisibility(View.INVISIBLE);
					}
				} else {
					if (mIsActive) {
						int effect = app.info.effect;
						if (effect == 1) {
							boolean isInstall = AppGameInstallingValidator.getInstance()
									.isAppExist(mContext, app.info.packname);
							if (isInstall) {
								effect = 0;
							} else {
								if (DownloadUtil.checkViewedEffectApp(mContext, app.info.packname)) {
									effect = 0;
								}
							}
						}
						setIcon(position, holder.mImageSwitcherRight, app.info.icon, app.picLocalPath,
								app.picLocalFileName, mDefaultBitmap, effect, true);
						if (holder.mFeatureIconRight != null) {
							holder.mFeatureIconRight.setImageDrawable(null);
						}
						// 设置特性图标，“必备”，“首发”，“最新”等等
						if (!TextUtils.isEmpty(app.info.ficon)) {
							if (holder.mFeatureIconRight == null) {
								holder.mFeatureIconRightStub.inflate();
								holder.mFeatureIconRight = (ImageView) holder.mRightView
										.findViewById(R.id.feature_icon);
							}
							if (holder.mFeatureIconRight != null) {
								holder.mFeatureIconRight.setVisibility(View.VISIBLE);
								setFeatureIcon(position, holder.mFeatureIconRight, app.info.ficon,
										app.picLocalPath, app.localFeatureFileName);
							}
						}
					} else {
						((ImageView) (holder.mImageSwitcherRight.getCurrentView()))
								.setImageBitmap(mDefaultBitmap);
						if (holder.mFeatureIconRight != null) {
							holder.mFeatureIconRight.setImageDrawable(null);
						}
					}
					// 名字
					holder.mAppNameRight.setText(app.info.name);
					// 如果正在下载，则显示状态
					if (hasDownloadState(app)) {
						int state = app.downloadState.state;
						String progress = "";
						if (state == DownloadTask.STATE_WAIT || state == DownloadTask.STATE_START
								|| state == DownloadTask.STATE_RESTART) {
							// 等待下载
							progress = mContext.getString(R.string.download_manager_wait);
						} else if (state == DownloadTask.STATE_DOWNLOADING) {
							progress = app.downloadState.alreadyDownloadPercent + "%";
						} else if (state == DownloadTask.STATE_STOP) {
							progress = mContext.getString(R.string.download_manager_pause);
						}
						if (holder.mProgressTextRight == null) {
							holder.mProgressTextRightStub.inflate();
							holder.mProgressTextRight = (TextView) holder.mRightView
									.findViewById(R.id.feature_progress);
						}
						if (holder.mProgressTextRight != null) {
							holder.mProgressTextRight.setVisibility(View.VISIBLE);
							holder.mProgressTextRight.setText(progress);
						}
						holder.mRatingBarRight.setVisibility(View.GONE);
					} else {
						float grade = app.info.grade / 2.0f;
						if (Math.abs(grade - holder.mRatingBarRight.getRating()) >= 0.0000001f) {
							holder.mRatingBarRight.setRating(grade);
						}
					}
					holder.mRightView.setTag(R.id.appgame, app);
					holder.mRightView.setOnClickListener(mClickListener);
				}
			}
		}
		return convertView;
	}

	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final int position, final ImageSwitcher switcher, String imgUrl,
			String imgPath, String imgName, final Bitmap defaultBitmap, final int effect, boolean isMask) {
		// TODO:XIEDEZHI 修改接口，不要每次setIcon都要生成一个回调
		final int padding = mBurningPadding;
		if (switcher.getTag() != null && switcher.getTag().equals(imgUrl)) {
			ImageView image = (ImageView) switcher.getCurrentView();
			Drawable drawable = image.getDrawable();
			if (drawable != null && drawable instanceof BitmapDrawable) {
				BitmapDrawable bDrawable = (BitmapDrawable) drawable;
				if (bDrawable.getBitmap() != null && bDrawable.getBitmap() != mDefaultBitmap) {
					if (effect == 1 && image instanceof BurningIcon) {
						((BurningIcon) image).setBurningFrame(mBurningFrame);
						((BurningIcon) image).setBurning(true);
						image.setPadding(padding, padding, padding, padding);
					}
					return;
				}
			}
		}
		switcher.setTag(imgUrl);
		switcher.getCurrentView().clearAnimation();
		switcher.getNextView().clearAnimation();
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false,
				isMask ? AppGameDrawUtils.getInstance().mMaskIconOperator : null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (switcher.getTag().equals(imgUrl)) {
							Drawable drawable = ((ImageView) switcher
									.getCurrentView()).getDrawable();
							if (drawable instanceof BitmapDrawable) {
								Bitmap bm = ((BitmapDrawable) drawable)
										.getBitmap();
								if (bm == defaultBitmap) {
									switcher.setImageDrawable(new BitmapDrawable(imageBitmap));
								}
								ImageView imageView = (ImageView) switcher.getCurrentView();
								if (effect == 1 && imageView instanceof BurningIcon) {
									((BurningIcon) imageView).setBurningFrame(mBurningFrame);
									((BurningIcon) imageView).setBurning(true);
									imageView.setPadding(padding, padding, padding, padding);
								}
							}
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		ImageView imageView = (ImageView) switcher.getCurrentView();
		if (bm != null && mIsActive) {
			imageView.setImageBitmap(bm);
			if (effect == 1 && imageView instanceof BurningIcon) {
				((BurningIcon) imageView).setBurningFrame(mBurningFrame);
				((BurningIcon) imageView).setBurning(true);
				imageView.setPadding(padding, padding, padding, padding);
			}
		} else {
			imageView.setImageBitmap(defaultBitmap);
		}
	}
	
	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setFeatureIcon(final int position, final ImageView imageView, String imgUrl,
			String imgPath, String imgName) {
		// TODO:XIEDEZHI 修改接口，不要每次setIcon都要生成一个回调
		imageView.setTag(imgUrl);
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false,
				null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl) && mIsActive) {
							imageView.setImageBitmap(imageBitmap);
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		if (bm != null && mIsActive) {
			imageView.setImageBitmap(bm);
		} else {
			imageView.setImageBitmap(null);
		}
	}

	/**
	 * 判断应用是否正在下载
	 * 
	 * @return 正在下载返回true，否则返回false
	 */
	private boolean hasDownloadState(BoutiqueApp app) {
		int state = app.downloadState.state;
		switch (state) {
			case DownloadTask.STATE_WAIT :
			case DownloadTask.STATE_START :
			case DownloadTask.STATE_DOWNLOADING :
			case DownloadTask.STATE_STOP :
			case DownloadTask.STATE_RESTART :
				return true;
			case DownloadTask.STATE_NEW :
			case DownloadTask.STATE_FAIL :
			case DownloadTask.STATE_FINISH :
			case DownloadTask.STATE_DELETE :
				return false;
			default :
				return false;
		}
	}

	/**
	 * 更新adapter数据源，并调用notifyDataSetChanged
	 * 
	 * @param data
	 *            新数据
	 */
	public void update(List<BoutiqueApp> data) {
		mDataSource.clear();
		if (data != null) {
			Map<String, DownloadTask> map = new HashMap<String, DownloadTask>();
			// 根据mDownloadTaskList初始化应用的下载状态
			if (mDownloadTaskList != null && mDownloadTaskList.size() > 0) {
				for (DownloadTask task : mDownloadTaskList) {
					if (task != null) {
						map.put(String.valueOf(task.getId()), task);
					}
				}
			}
			for (BoutiqueApp app : data) {
				// 初始化应用的图片路径
				String icon = app.info.icon;
				if (!TextUtils.isEmpty(icon)) {
					String fileName = String.valueOf(icon.hashCode());
					app.picLocalPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;
					app.picLocalFileName = fileName;
				}
				if (!TextUtils.isEmpty(app.info.ficon)) {
					app.localFeatureFileName = String.valueOf(app.info.ficon.hashCode());
				}
				if (map.containsKey(app.info.appid)) {
					DownloadTask task = map.get(app.info.appid);
					app.downloadState.state = task.getState();
					app.downloadState.alreadyDownloadPercent = task.getAlreadyDownloadPercent();
				}
				mDataSource.add(app);
			}
		}
		// notifyDataSetChanged();
	}

	/**
	 * @return 如果更新了列表可视范围的下载进度，返回true
	 */
	public boolean updateDownloadTask(int firstIndex, int lastIndex, int headviewcount,
			DownloadTask downloadTask) {
		boolean ret = false;
		for (int i = 0; i < mDataSource.size(); i++) {
			BoutiqueApp app = mDataSource.get(i);
			if (app == null || app.info == null || app.info.appid == null) {
				continue;
			}
			if (app.info.appid.equals(downloadTask.getId() + "")) {
				app.downloadState.state = downloadTask.getState();
				app.downloadState.alreadyDownloadPercent = downloadTask.getAlreadyDownloadPercent();
				int truePosition = i / 2;
				if ((truePosition + headviewcount) >= firstIndex
						&& (truePosition + headviewcount) <= lastIndex) {
					ret = true;
					if (mIsActive) {
						notifyDataSetChanged();
					}
				}
				break;
			}
		}
		return ret;
	}

	/**
	 * 更改激活状态，如果是true则getview时会加载图标，否则不加载图标
	 * 
	 * @param isActive
	 *            是否为激活状态
	 */
	public void onActiveChange(boolean isActive) {
		mIsActive = isActive;
	}

	/**
	 * 通知adapter有应用状态更新
	 * 
	 * @param packName
	 *            安装/卸载/更新的包名
	 * 
	 * @param appAction
	 *            代表应用的操作码，详情看{@link MainViewGroup}
	 * @return 如果列表可视范围的应用发生了安装卸载，返回true
	 */
	public boolean onAppAction(int firstIndex, int lastIndex, int headviewcount, String packName,
			int appAction) {
		if (mDataSource == null) {
			return false;
		}
		boolean ret = false;
		for (int i = 0; i < mDataSource.size(); i++) {
			BoutiqueApp app = mDataSource.get(i);
			if (app == null || app.info == null) {
				continue;
			}
			if (packName.equals(app.info.packname)) {
				int truePosition = i / 2;
				if ((truePosition + headviewcount) >= firstIndex
						&& (truePosition + headviewcount) <= lastIndex) {
					ret = true;
					if (mIsActive) {
						notifyDataSetChanged();
					}
				}
				break;
			}
		}
		return ret;
	}

	/**
	 * 设置downloadTask列表
	 */
	public void setDownloadTaskList(List<DownloadTask> downloadList) {
		mDownloadTaskList = downloadList;
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 * 
	 */
	public class FeatureViewHolder {
		public View mLeftView;
		public ViewStub mFeatureIconLeftStub;
		public ImageView mFeatureIconLeft;
		public BurningIcon mAppIconLeft;
		public BurningIcon mAppIconLeftAnother;
		public ImageSwitcher mImageSwitcherLeft;
		public TextView mAppNameLeft;
		public RatingBar mRatingBarLeft;
		public ViewStub mProgressTextLeftStub;
		public TextView mProgressTextLeft;

		public View mRightView;
		public ViewStub mFeatureIconRightStub;
		public ImageView mFeatureIconRight;
		public BurningIcon mAppIconRight;
		public BurningIcon mAppIconRightAnother;
		public ImageSwitcher mImageSwitcherRight;
		public TextView mAppNameRight;
		public RatingBar mRatingBarRight;
		public ViewStub mProgressTextRightStub;
		public TextView mProgressTextRight;

		public void setVisibility() {

			if (mAppNameLeft.getVisibility() != View.VISIBLE) {
				mLeftView.setBackgroundResource(R.drawable.recomm_app_list_item_selector);
			}
			if (mFeatureIconLeft != null) {
				mFeatureIconLeft.setVisibility(View.VISIBLE);
			}
			mAppIconLeft.setBurning(false);
			mAppIconLeft.setPadding(0, 0, 0, 0);
			mAppIconLeftAnother.setBurning(false);
			mAppIconLeftAnother.setPadding(0, 0, 0, 0);
			mImageSwitcherLeft.setVisibility(View.VISIBLE);
			mAppNameLeft.setVisibility(View.VISIBLE);
			mRatingBarLeft.setVisibility(View.VISIBLE);
			if (mProgressTextLeft != null) {
				mProgressTextLeft.setVisibility(View.GONE);
			}

			if (mAppNameRight.getVisibility() != View.VISIBLE) {
				mRightView.setBackgroundResource(R.drawable.recomm_app_list_item_selector);
			}
			if (mFeatureIconRight != null) {
				mFeatureIconRight.setVisibility(View.VISIBLE);
			}
			mAppIconRight.setBurning(false);
			mAppIconRight.setPadding(0, 0, 0, 0);
			mAppIconRightAnother.setBurning(false);
			mAppIconRightAnother.setPadding(0, 0, 0, 0);
			mImageSwitcherRight.setVisibility(View.VISIBLE);
			mAppNameRight.setVisibility(View.VISIBLE);
			mRatingBarRight.setVisibility(View.VISIBLE);
			if (mProgressTextRight != null) {
				mProgressTextRight.setVisibility(View.GONE);
			}
		}
	}
}
