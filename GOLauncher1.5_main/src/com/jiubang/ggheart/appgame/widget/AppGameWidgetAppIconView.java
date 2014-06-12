package com.jiubang.ggheart.appgame.widget;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.utils.MD5;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreDisplayUtil;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>
 * 类描述: 显示一组应用icon或游戏的banner <br>
 * 功能详细描述:
 * 
 * @author zhoujun
 * @date [2012-9-10]
 */
public class AppGameWidgetAppIconView extends LinearLayout {

	private Context mContext;
	/**
	 * 显示游戏banner
	 */
	private static final int SHOW_TYPE_FOR_GAME = 1;
	/**
	 * 显示应用的icon
	 */
	private static final int SHOW_TYPE_FOR_APP = 2;

	/**
	 * 数据类型-应用
	 */
	private static final int DATA_TYPE_FOR_APP = 1;
	/**
	 * 数据类型-游戏
	 */

	private static final int DATA_TYPE_FOR_GAME = 2;
	// private static final int DEFAULT_APP_ICON_COUNT = 5;
	private static final int DEFAULT_GAME_ICON_COUNT = 2;

	private static final int APP_BASE_IMAGE_WIDTH = 72;
	private static final int APP_BASE_IMAGE_HEIGHT = 76;

	private static final int APP_ICON_WIDTH = 56;
	private static final int APP_ICON_HEIGHT = 56;

	private int mAppBaseWidth = 0;
	private int mAppBaseHeight = 0;

	private int mAppIconWidth = 0;
	private int mAppIconHeight = 0;

	private static final int GANE_BASE_IMAGE_WIDTH = 214;
	private static final int GAME_BASE_IMAGE_HEIGHT = 105;

	private static final int GAME_ICON_WIDTH = 200;
	private static final int GAME_ICON_HEIGHT = 85;

	private int mGameBaseWidth = 0;
	private int mGameBaseHeight = 0;

	private int mGameIconWidth = 0;
	private int mGameIconHeight = 0;

	private Bitmap mAppBaseBitmap;
	private Bitmap mGameBaseBitmap;

	private Bitmap mAppBaseWithReflectionBitmap;
	private Bitmap mGameBaseWithReflectionBitmap;

	private int mCurrType = 0;

	/**
	 * 默认返回应用中心
	 */
	private int mDataType = DATA_TYPE_FOR_APP;

	private float mScale = 1.0f;
	/**
	 * 应用中心图标左边距
	 */
	private static final int APP_ICON_LEFT_MARGIN = 25;
	/**
	 * 游戏banner图标左边距
	 */
	private static final int GAME_BANNER_LEFT_MARGIN = 13;

	/**
	 * 倒影图片的高度
	 */
	private static final int REFLECTION_IMAGE_HEIGHT = 12;

	private int mReflectionHeight = 0;

	/**
	 * 上次点击button时间，为了防止重复点击
	 */
	private long mLastClickTime = 0;

	/**
	 * 两次点击之间 默认最小的时间间隔
	 */
	private static final int MIN_TIME_INNTERVAL = 1000;

	public AppGameWidgetAppIconView(Context context,
			OnLongClickListener onLongClickLister) {
		super(context);
		mContext = context;
		this.mOnLongClickLister = onLongClickLister;
		init();
	}

	public AppGameWidgetAppIconView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	private void init() {
		initView();
		initWidthAndHeight();

		// 创建基本的底图
		mAppBaseBitmap = createBaseBitmap(mContext, mAppBaseWidth,
				mAppBaseHeight);
		mGameBaseBitmap = createBitmap(mContext);

		mAppBaseWithReflectionBitmap = createReflectionImageWithOrigin(
				mAppBaseBitmap, mReflectionHeight);
		mGameBaseWithReflectionBitmap = createReflectionImageWithOrigin(
				mGameBaseBitmap, mReflectionHeight);

		// 生成图片icon view
		createAppView(DEFAULT_GAME_ICON_COUNT);
	}

	private void initView() {
		this.setOrientation(LinearLayout.HORIZONTAL);
		// this.setBackgroundColor(Color.GREEN);
		this.setGravity(Gravity.BOTTOM);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		int baseMargin = GAME_BANNER_LEFT_MARGIN;
		if (mCurrType == SHOW_TYPE_FOR_APP) {
			baseMargin = APP_ICON_LEFT_MARGIN;
		}
		int margin = GoStoreDisplayUtil.scalePxToMachine(mContext, baseMargin);
		params.leftMargin = margin;
		params.rightMargin = margin;
		// params.setMargins(margin, 0, margin, 0);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		this.setLayoutParams(params);
	}

	private void initWidthAndHeight() {
		if (!GoLauncher.isPortait()) {
			mScale = mScale * DrawUtils.sHeightPixels / DrawUtils.sWidthPixels;
		}
		mAppBaseWidth = GoStoreDisplayUtil.scalePxToMachine(mContext,
				(int) (APP_BASE_IMAGE_WIDTH * mScale));
		mAppBaseHeight = GoStoreDisplayUtil.scalePxToMachine(mContext,
				(int) (APP_BASE_IMAGE_HEIGHT * mScale));

		mAppIconWidth = GoStoreDisplayUtil.scalePxToMachine(mContext,
				(int) (APP_ICON_WIDTH * mScale));
		mAppIconHeight = GoStoreDisplayUtil.scalePxToMachine(mContext,
				(int) (APP_ICON_HEIGHT * mScale));

		mGameBaseWidth = GoStoreDisplayUtil.scalePxToMachine(mContext,
				(int) (GANE_BASE_IMAGE_WIDTH * mScale));
		mGameBaseHeight = GoStoreDisplayUtil.scalePxToMachine(mContext,
				(int) (GAME_BASE_IMAGE_HEIGHT * mScale));

		mGameIconWidth = GoStoreDisplayUtil.scalePxToMachine(mContext,
				(int) (GAME_ICON_WIDTH * mScale));
		mGameIconHeight = GoStoreDisplayUtil.scalePxToMachine(mContext,
				(int) (GAME_ICON_HEIGHT * mScale));

		mReflectionHeight = GoStoreDisplayUtil.scalePxToMachine(mContext,
				REFLECTION_IMAGE_HEIGHT);
	}

	private OnLongClickListener mOnLongClickLister;

	@Override
	public void setOnLongClickListener(OnLongClickListener onLongClickLister) {
		this.mOnLongClickLister = onLongClickLister;
	}

	private void createAppView(int count) {
		FrameLayout.LayoutParams parentParams = (FrameLayout.LayoutParams) this
				.getLayoutParams();
		if (mCurrType == SHOW_TYPE_FOR_APP) {
			int margin = GoStoreDisplayUtil.scalePxToMachine(mContext,
					APP_ICON_LEFT_MARGIN);
			parentParams.leftMargin = margin;
			parentParams.rightMargin = margin;
		}
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.weight = 1;

		Bitmap bitmap = mGameBaseWithReflectionBitmap;
		if (mCurrType == SHOW_TYPE_FOR_APP) {
			bitmap = mAppBaseWithReflectionBitmap;
		}
		ImageView imageView = null;
		for (int i = 0; i < count; i++) {
			imageView = new ImageView(mContext);
			imageView.setScaleType(ScaleType.CENTER);
			imageView.setImageBitmap(bitmap);
			// imageView.setBackgroundColor(Color.RED);
			imageView.setOnClickListener(mOnClickListenter);
			imageView.setOnLongClickListener(mOnLongClickLister);
			this.addView(imageView, params);
		}
	}

	public void updateData(ClassificationDataBean dataBean) {
		List<BoutiqueApp> featureList = null;
		if (dataBean != null) {
			featureList = dataBean.featureList;
		}
		if (featureList != null && featureList.size() > 0) {
			mCurrType = dataBean.mViewType;
			mDataType = dataBean.mWidgetDataType;
			int childCount = this.getChildCount();
			int size = featureList.size();
			if (childCount != size) {
				this.removeAllViews();
				createAppView(size);
			}
			BoutiqueApp boutiqueApp = null;
			ImageView imageView = null;
			for (int i = 0; i < size; i++) {
				boutiqueApp = featureList.get(i);
				imageView = (ImageView) this.getChildAt(i);
				if (boutiqueApp != null && imageView != null) {
					imageView.setTag(boutiqueApp);
					String url = (mCurrType == SHOW_TYPE_FOR_GAME)
							? boutiqueApp.pic
							: boutiqueApp.info.icon;
					if (url != null) {
						setIcon(imageView, url, LauncherEnv.Path.APP_MANAGER_ICON_PATH,
								boutiqueApp.picLocalFileName, true);
					}
				}
			}
		}
	}

	/**
	 * 读取图标，然后设到imageview里
	 * 
	 */
	private void setIcon(final ImageView imageView, String imgUrl,
			String imgPath, String imgName, boolean setDefaultIcon) {
		imgName = MD5.encode(imgUrl);
		// TODO 这里能不能不要每次load图片都生成一个回调对象？
		Bitmap bm = AsyncImageManager.getInstance().loadImage(imgPath, imgName,
				imgUrl, true, true, null, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						BoutiqueApp boutiqueApp = (BoutiqueApp) imageView
								.getTag();
						if (boutiqueApp != null) {
							String url = (mCurrType == SHOW_TYPE_FOR_GAME) ? boutiqueApp.pic
									: boutiqueApp.info.icon;
							if (url != null && url.equals(imgUrl)) {
								if (mCurrType == SHOW_TYPE_FOR_GAME) {
									imageBitmap = createGameBitmap(imageBitmap);
								} else {
									imageBitmap = createAppBitmap(imageBitmap);
								}
								if (imageBitmap != null) {
									imageView
											.setImageBitmap(createReflectionImageWithOrigin(
													imageBitmap,
													mReflectionHeight));
								}

							} else {
								imageBitmap = null;
								imgUrl = null;
							}
						}
					}
				});
		if (bm != null) {
			if (mCurrType == SHOW_TYPE_FOR_GAME) {
				bm = createGameBitmap(bm);
			} else {
				bm = createAppBitmap(bm);
			}
			if (bm != null) {
				imageView.setImageBitmap(createReflectionImageWithOrigin(bm,
						mReflectionHeight));
			}
		} else {
			if (setDefaultIcon) {
				Bitmap bitmap = mAppBaseWithReflectionBitmap;
				if (mCurrType == SHOW_TYPE_FOR_GAME) {
					bitmap = mGameBaseWithReflectionBitmap;
				}
				imageView.setImageBitmap(bitmap);
			} else {
				imageView.setImageDrawable(null);
			}
		}
	}

	private OnClickListener mOnClickListenter = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// 在1s内多次点击，被视为重复点击，不做处理。
			long newClickTime = System.currentTimeMillis();
			if (newClickTime - mLastClickTime < MIN_TIME_INNTERVAL) {
				if (AppGameWidget.DEBUG) {
					Log.d(AppGameWidget.TAG, "time is in :"
							+ (newClickTime - mLastClickTime));
				}
				return;
			}
			mLastClickTime = newClickTime;

			BoutiqueApp boutiqueApp = (BoutiqueApp) view.getTag();
			if (boutiqueApp == null) {
				return;
			}

			// 获取统计widget使用的typeId
			String typeId = DownloadUtil.getMark(mContext,
					AppGameWidgetDataProvider.KEY_APPCENER_WIDGET_TYPEID);
			if (typeId != null && !"".equals(typeId)) {
				boutiqueApp.typeid = Integer.parseInt(typeId);
			}

			// 统计
			int access = AppsDetail.START_TYPE_WIDGET_APP;
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mContext,
					AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET);
			AppManagementStatisticsUtil.saveTabClickData(getContext(), boutiqueApp.typeid,
					String.valueOf(AppRecommendedStatisticsUtil.ENTRY_TYPE_WIDGET));

			// 跳转到详情界面
			int position = getViewPosition(view) + 1;
			AppsDetail.jumpToDetail(mContext, boutiqueApp, access, position,
					true);
			// Home键跳转标识
			GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
					IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 0, null, null);

		}
	};

	/**
	 * 获取当前view的位置
	 * 
	 * @param view
	 * @return
	 */
	private int getViewPosition(View view) {
		int count = this.getChildCount();
		View childView = null;
		for (int i = 0; i < count; i++) {
			if (childView == view) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * 获得带倒影的图片方法
	 * 
	 * @param bitmap
	 * @param scaleH
	 *            获得倒影高度比例值;
	 * @return
	 * 
	 * 
	 */
	public static Bitmap createReflectionImageWithOrigin(Bitmap bitmap,
			int reflectHeight) {
		final int reflectionGap = 0;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);

		Bitmap bitmapWithReflection = Bitmap.createBitmap(width, height
				+ reflectHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
				| Paint.FILTER_BITMAP_FLAG));
		canvas.drawBitmap(bitmap, 0, 0, null);
		// canvas.drawRect(0, height, width, (height + height * scaleH),
		// deafalutPaint);
		Paint deafalutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,
				bitmapWithReflection.getHeight() + reflectionGap, 0x79ffffff,
				0x30ffffff, TileMode.CLAMP);
		deafalutPaint.setShader(shader);
		deafalutPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height
				- reflectHeight, width, reflectHeight, matrix, false);
		canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);
		canvas.drawRect(0, height, width, bitmapWithReflection.getHeight()
				+ reflectionGap, deafalutPaint);

		return bitmapWithReflection;
	}

	private Bitmap createAppBitmap(Bitmap bitmap) {
		try {
			Bitmap newBitmap = GoStoreDisplayUtil.scaleBitmapToSize(bitmap,
					mAppIconWidth, mAppIconHeight);
			Bitmap bitmapWithReflection = Bitmap.createBitmap(mAppBaseWidth,
					mAppBaseHeight, Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmapWithReflection);
	
			canvas.drawBitmap(mAppBaseBitmap, 0, 0, null);
			canvas.drawBitmap(newBitmap, scale(8), scale(15), null);
			return bitmapWithReflection;
		} catch (OutOfMemoryError error) {
//			error.printStackTrace();
		} 
		return null;
	}

	private Bitmap createGameBitmap(Bitmap bitmap) {
		Bitmap newBitmap = GoStoreDisplayUtil.scaleBitmapToSize(bitmap,
				mGameIconWidth, mGameIconHeight);
		Bitmap bitmapWithReflection = Bitmap.createBitmap(mGameBaseWidth,
				mGameBaseHeight, Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(mGameBaseBitmap, 0, 0, null);
		canvas.drawBitmap(newBitmap, scale(7), scale(18), null);
		return bitmapWithReflection;
	}

	/**
	 * 创建底座的bitmap
	 * 
	 * @param context
	 * @param width
	 * @param height
	 * @return
	 */
	private Bitmap createBaseBitmap(Context context, int width, int height) {

		Bitmap baseBitmap = Bitmap
				.createBitmap(width, height, Config.ARGB_8888);

		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.appgame_widget_icon_backpages);

		Bitmap bmpBg = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.appgame_widget_icon_bg);

		bmp = scaleBitmap(bmp);
		bmpBg = scaleBitmap(bmpBg);

		Canvas canvas = new Canvas(baseBitmap);
		canvas.drawBitmap(bmp, 0, 0, null);
		canvas.drawBitmap(bmpBg, 0, scale(8), null);
		return baseBitmap;
	}

	private Bitmap scaleBitmap(Bitmap bitmap) {
		int bmpWidth = bitmap.getWidth();
		int bmpHeiht = bitmap.getHeight();

		// int scaleWidth = scale(bmpWidth);
		// int scaleHeight = scale(bmpHeiht);

		int scaleWidth = (int) (bmpWidth * mScale);
		int scaleHeight = (int) (bmpHeiht * mScale);

		if (scaleWidth != bmpWidth) {
			bitmap = GoStoreDisplayUtil.scaleBitmapToSize(bitmap, scaleWidth,
					scaleHeight);
		}
		return bitmap;
	}

	private int scale(int widthOrHeight) {
		widthOrHeight = GoStoreDisplayUtil.scalePxToMachine(mContext,
				widthOrHeight);
		return (int) (widthOrHeight * mScale);
	}

	private Bitmap createBitmap(Context context) {
		Bitmap bitmapWithReflection = Bitmap.createBitmap(mGameBaseWidth,
				mGameBaseHeight, Config.ARGB_8888);

		Bitmap bmp = createNineBitmap(
				R.drawable.appgame_widget_banner_backpages_bg, scale(202),
				scale(94));
		Bitmap bmpBg = createNineBitmap(R.drawable.appgame_widget_banner_bg,
				mGameBaseWidth, scale(96));
		Canvas canvas = new Canvas(bitmapWithReflection);
		canvas.drawBitmap(bmp, scale(6), 0, null);
		canvas.drawBitmap(bmpBg, 0, scale(9), null);

		return bitmapWithReflection;
	}

	private Bitmap createNineBitmap(int resourceId, int width, int height) {
		NinePatchDrawable bgNine = (NinePatchDrawable) getContext()
				.getResources().getDrawable(resourceId);
		bgNine.setBounds(new Rect(0, 0, width, height));
		int bitWidth = bgNine.getBounds().width();
		int bitHeight = bgNine.getBounds().height();
		Bitmap bgBit = null;
		if (bitWidth > 0 && bitHeight > 0) {
			bgBit = Bitmap.createBitmap(bitWidth, bitHeight, Config.ARGB_8888);
			Canvas iconCanvas = new Canvas(bgBit);
			bgNine.draw(iconCanvas);
		}
		return bgBit;
	}
}
