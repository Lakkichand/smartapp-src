/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.DockItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.plugin.notification.NotificationControler;
import com.jiubang.ggheart.plugin.notification.NotificationType;

/**
 * @author ruxueqin
 * 
 */
public class DockIconView extends ImageView implements BroadCasterObserver {
	/**
	 * 在DOCK条区域位置
	 */
	private int mAreaPosition;

	/**
	 * dock图标移动前的坐标
	 */
	private int mOldPointX;

	/**
	 * dock图标移动后的坐标
	 */
	private int mNewPointX;

	/**
	 * 后台数据信息
	 */
	private DockItemInfo mInfo;

	/**
	 * 发光
	 */
	private static Bitmap sBgLight;

	/**
	 * 标记是否背景发光
	 */
	private boolean mIsBgShow;

	/**
	 * 通讯统计排版参数
	 */
	private static int sNotifyWidth;
	private static int sNotifyHeight;
	private static int sFontSize;

	/**
	 * 标记是否显示通讯统计
	 */
	private boolean mIsNotifyShow;

	/**
	 * 是什么类型的通讯统计程序，默认为0,不是通讯统计程序类型定义在NotificationType.java
	 */
	private int mNotificationType = NotificationType.IS_NOT_NOTIFICSTION;

	private int mNotifyCount;
	private String mNotifyCountString;

	private Drawable mNotifyDrawable;
	private Drawable mNewAppDrawable;

	private Paint mPaint = new Paint();

	private final Rect mCounterRect = new Rect();
	private final Rect mNewAppRect = new Rect();
	private static int sCounterPadding;
	private int mIconAlpha = 255;
	/**
	 * 发给handler的消息，setimagebitmap
	 */
	public static final int CHANGE_ICON_STRING = 1;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case CHANGE_ICON_STRING :
					if (null == mInfo || null == mInfo.getIcon()) {
						return;
					}
					mNewAppDrawable = null; // 将new图片置空，主题切换可以从主题包中重新获取图片
					
					if (mInfo.mItemInfo instanceof UserFolderInfo) {
						UserFolderInfo folder = (UserFolderInfo) mInfo.mItemInfo;
						setmIsNotifyShow(true);
						setmNotifyCount(folder.mTotleUnreadCount);
					}
					BitmapDrawable drawable = mInfo.getIcon();
					setImageBitmap(drawable.getBitmap());
					break;

				default :
					break;
			}
		};
	};

	public int getAreaPosition() {
		return mAreaPosition;
	}

	public void setAreaPosition(int areaPosition) {
		mAreaPosition = areaPosition;
	}

	public int getOldPointX() {
		return mOldPointX;
	}

	public void setOldPointX(int oldPoint_x) {
		mOldPointX = oldPoint_x;
	}

	public int getNewPointX() {
		return mNewPointX;
	}

	public void setNewPointX(int newPoint_x) {
		mNewPointX = newPoint_x;
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		super.setAlpha(alpha);
		mIconAlpha = alpha;
		invalidate();
	}

	/**
	 * @param mIsNotifyShow
	 *            the mIsNotifyShow to set
	 */
	public void setmIsNotifyShow(boolean mIsNotifyShow) {
		this.mIsNotifyShow = mIsNotifyShow;
	}

	/**
	 * @return the mIsBgShow
	 */
	public boolean ismIsBgShow() {
		return mIsBgShow;
	}

	/**
	 * @param mIsBgShow
	 *            the mIsBgShow to set
	 */
	public void setmIsBgShow(boolean mIsBgShow) {
		this.mIsBgShow = mIsBgShow;
		if (mIsBgShow == true) {
			try {
				initBg();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				// 内存回收
				OutOfMemoryHandler.handle();
			}
		}
		postInvalidate();
	}

	/**
	 * @param context
	 */
	public DockIconView(Context context) {
		this(context, null);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public DockIconView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public DockIconView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint.setStyle(Style.FILL_AND_STROKE);
		mPaint.setAntiAlias(true); // 抗锯齿
		mPaint.setColor(android.graphics.Color.WHITE);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 程序图标内容绘制
		Drawable drawable = getDrawable();
		if (drawable != null && drawable instanceof BitmapDrawable) {
			Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();

			if (bmp != null && !bmp.isRecycled()) {
				if (mIconAlpha != 255) {
					Style style = mPaint.getStyle();
					int alpha = mPaint.getAlpha();
					mPaint.setStyle(Paint.Style.STROKE); // 空心
					mPaint.setAlpha(mIconAlpha); // Bitmap透明度(0 ~ 100)
					canvas.drawBitmap(bmp, getPaddingLeft(), getPaddingTop(), mPaint);
					mPaint.setStyle(style);
					mPaint.setAlpha(alpha);
				} else {
					canvas.drawBitmap(bmp, getPaddingLeft(), getPaddingTop(), mPaint);
				}
			}
		}
		// 发光图片绘制
		if (mIsBgShow && null != sBgLight && null != getInfo()) {
			// canvas.drawBitmap(mBgLight, mBgLeft, mBgTop, mPaint);
			canvas.drawBitmap(sBgLight, getPaddingLeft()
					- (sBgLight.getWidth() - getInfo().getBmpSize()) / 2, getPaddingTop()
					- (sBgLight.getHeight() - getInfo().getBmpSize()) / 2, mPaint);
		}
		
		AppItemInfo itemInfo = (AppItemInfo) mInfo.mItemInfo.getRelativeItemInfo();
		if (itemInfo != null && itemInfo.mIsNewRecommendApp) {
			if (mNewAppDrawable == null) {
				initNewAppDrawable();
			}
			mNewAppDrawable.draw(canvas);
		}
		// TODO:把通讯统计的底座图片与文字内容写在一个ＸＭＬ里
		// 绘制通讯统计
		else if (mIsNotifyShow && mNotifyCount > 0) {
			if (mNotifyDrawable == null) {
				initNotifyDrawable();
			}
			mNotifyDrawable.draw(canvas);
			mPaint.setTextSize(sFontSize);
			canvas.drawText(mNotifyCountString, mCounterRect.centerX(), mCounterRect.centerY()
					+ sCounterPadding, mPaint);
		}
	}

	private void initBg() {
		if (sBgLight != null) {
			return;
		}
		Drawable aDrawable = getDrawable();
		if (aDrawable instanceof BitmapDrawable) {
			Bitmap bmp = ((BitmapDrawable) getDrawable()).getBitmap();
			if (bmp != null) {
				try {
					sBgLight = BitmapFactory.decodeResource(getResources(), R.drawable.dock_light);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					OutOfMemoryHandler.handle();
					sBgLight = null;
				} catch (Error e) {
					e.printStackTrace();
					sBgLight = null;
				} catch (Exception e) {
					e.printStackTrace();
					sBgLight = null;
				}
			}
		}
	}

	/**
	 * @param mNotifyCount
	 *            the mNotifyCount to set
	 */
	public void setmNotifyCount(int mNotifyCount) {
		this.mNotifyCount = mNotifyCount;
		if (mInfo.mItemInfo instanceof ShortCutInfo) {
			ShortCutInfo info = (ShortCutInfo) mInfo.mItemInfo;
			info.mCounter = mNotifyCount;
		}
		mNotifyCountString = String.valueOf(mNotifyCount);
		updateNotifyDrawableBound();
	}

	private void initNotifyDrawable() {
		mNotifyDrawable = getContext().getResources().getDrawable(R.drawable.stat_notify);
		updateNotifyDrawableBound();
	}
	
	private void initNewAppDrawable() {
		mNewAppDrawable = getDrawableById(AppFuncFrame.getThemeController().getThemeBean().mAppIconBean.mNewApp);
		if (mNewAppDrawable == null) {
			mNewAppDrawable = getResources().getDrawable(R.drawable.new_install_app);
		}
		updateNewAppDrawableBound();
	}
	
	private Drawable getDrawableById(String id) {
		Drawable drawable = AppFuncFrame.getThemeController().getDrawable(id, false);
		if ((drawable instanceof BitmapDrawable) == false) {
			drawable = null;
		}
		return drawable;
	}

	private void updateNotifyDrawableBound() {
		getNotifyDimens();

		if (mNotifyCountString != null) {
			int stringLength = (int) mPaint.measureText(mNotifyCountString, 0,
					mNotifyCountString.length() - 1);
			stringLength = Math.max(stringLength, 0);
			if (mNotifyDrawable != null) {
				mPaint.setTextAlign(Paint.Align.CENTER);

				mCounterRect.top = getPaddingTop();
				mCounterRect.bottom = mCounterRect.top + sNotifyHeight;
				mCounterRect.right = getWidth() - getPaddingRight();
				mCounterRect.left = mCounterRect.right - stringLength - sNotifyWidth;

				mNotifyDrawable.setBounds(mCounterRect.left, mCounterRect.top, mCounterRect.right,
						mCounterRect.bottom);
			}
		}
	}
	
	private void updateNewAppDrawableBound() {
		getNotifyDimens();
		if (mNewAppDrawable != null) {
			mNewAppRect.top = 0;
			mNewAppRect.bottom = mNewAppRect.top + mNewAppDrawable.getIntrinsicHeight();
			mNewAppRect.right = (int) (getWidth() * 1.1f);
			mNewAppRect.left = mNewAppRect.right - mNewAppDrawable.getIntrinsicWidth();
			mNewAppDrawable.setBounds(mNewAppRect.left, mNewAppRect.top, 
					mNewAppRect.right, mNewAppRect.bottom);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		updateNotifyDrawableBound();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	/**
	 * 内存回收
	 */
	public void clearSelf() {
		if (mInfo != null) {
			mInfo.selfDestruct();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case DockItemInfo.ICONCHANGED :
				Message msg = new Message();
				msg.what = CHANGE_ICON_STRING;
				// 必须放在msg queue首，不然会出现拖动后4个变5个，闪一下大图标才显示小图标
				mHandler.sendMessageAtFrontOfQueue(msg);
				break;

			case DockItemInfo.INTENTCHANGED : {
				NotificationControler controler = AppCore.getInstance().getNotificationControler();
				// 更新通讯统计程序标记与统计数字
				mNotificationType = whichNotificationType(mInfo);
				switch (mNotificationType) {
					case NotificationType.NOTIFICATIONTYPE_CALL :
						mIsNotifyShow = ShortCutSettingInfo.mAutoMisscallStatistic;
						setmNotifyCount(null == controler ? 0 : controler.getUnreadCallCount());
						break;

					case NotificationType.NOTIFICATIONTYPE_SMS :
						mIsNotifyShow = ShortCutSettingInfo.mAutoMessageStatistic;
						setmNotifyCount(null == controler ? 0 : controler.getUnreadSMSCount());
						break;

					case NotificationType.NOTIFICATIONTYPE_GMAIL :
						mIsNotifyShow = ShortCutSettingInfo.mAutoMissmailStatistic;
						setmNotifyCount(null == controler ? 0 : controler.getUnreadGmailCount());
						break;

					case NotificationType.NOTIFICATIONTYPE_K9MAIL :
						mIsNotifyShow = ShortCutSettingInfo.mAutoMissk9mailStatistic;
						setmNotifyCount(null == controler ? 0 : controler.getUnreadK9mailCount());
						break;

					case NotificationType.NOTIFICATIONTYPE_FACEBOOK :
						mIsNotifyShow = ShortCutSettingInfo.mAutoMissfacebookStatistic;
						setmNotifyCount(null == controler ? 0 : controler.getUnreadFacebookCount());
						break;

					case NotificationType.NOTIFICATIONTYPE_SinaWeibo :
						mIsNotifyShow = ShortCutSettingInfo.mAutoMissSinaWeiboStatistic;
						setmNotifyCount(null == controler ? 0 : controler.getUnreadSinaWeiboCount());
						break;

					case NotificationType.NOTIFICATIONTYPE_MORE_APP :
						mIsNotifyShow = true;
						setmNotifyCount(mInfo.mItemInfo.getRelativeItemInfo().getUnreadCount());
						break;

					case NotificationType.IS_NOT_NOTIFICSTION :
						mIsNotifyShow = false;
						break;

					default :
						mIsNotifyShow = false;
						break;
				}
			}
				break;
				
			case AppItemInfo.IS_RECOMMEND_APP_CHANGE : {
				invalidate();
				break;
			}

			default :
				break;
		}
	}

	/**
	 * @return the info
	 */
	public DockItemInfo getInfo() {
		return mInfo;
	}

	/**
	 * @param info
	 *            the info to set
	 */
	public void setInfo(DockItemInfo info) {
		if (null != mInfo) {
			mInfo.unRegisterObserver(this);
		}
		mInfo = info;
		mInfo.registerObserver(this);
		// 判断是不是、是什么通讯统计程序
		mNotificationType = whichNotificationType(info);
	}

	/**
	 * 判断是不是、是什么通讯统计程序
	 * 
	 * @param info
	 *            被判断对象
	 * @return 什么类型
	 */
	private int whichNotificationType(DockItemInfo info) {
		int type = NotificationType.IS_NOT_NOTIFICSTION;
		if (info == null) {
			return type;
		}

		// 判断内容：
		// 1:info.mIntent 内部Intent
		// 2:info.getAppItemInfo().mIntent 外部Intent
		if (info.mItemInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION
				|| info.mItemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			type = AppIdentifier.whichTypeOfNotification(getContext(),
					((ShortCutInfo) info.mItemInfo).getRelativeItemInfo());
		}
		if (info.mItemInfo.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
			type = NotificationType.NOTIFICATIONTYPE_DESKFOLDER;
		}

		if (type == NotificationType.IS_NOT_NOTIFICSTION
				&& info.mItemInfo.getRelativeItemInfo() != null) {
			type = AppIdentifier.whichTypeOfNotification(getContext(),
					info.mItemInfo.getRelativeItemInfo());
		}

		return type;
	}

	/**
	 * 计算通讯统计排版参数
	 */
	private void getNotifyDimens() {
		sNotifyWidth = getContext().getResources().getDimensionPixelSize(R.dimen.dock_notify_width);
		sNotifyHeight = getContext().getResources().getDimensionPixelSize(
				R.dimen.dock_notify_height);
		sFontSize = getContext().getResources()
				.getDimensionPixelSize(R.dimen.dock_notify_font_size);

		sCounterPadding = getResources().getDimensionPixelSize(R.dimen.counter_circle_padding);
	}

	/**
	 * @return the mNotificationType
	 */
	public int getmNotificationType() {
		return mNotificationType;
	}

	public void reset() {
		mIsNotifyShow = false;
		mNotificationType = NotificationType.IS_NOT_NOTIFICSTION;
		mNotifyCount = 0;
		mNotifyCountString = null;
	}
}
