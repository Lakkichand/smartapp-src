package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.tabs;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.gau.go.launcherex.R;
import com.go.util.Utilities;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.IDataSetChangeListener;
import com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox.TabActionListener;
   /**
    * 
    * <br>类描述:所有tab的公共父类
    * 
    */
public abstract class BaseTab implements OnClickListener, OnLongClickListener {
	public static final int TAB_LEVEL_1 = 1; // Tab一级
	public static final int TAB_LEVEL_2 = 2; // Tab二级

	public static final int TAB_LEVEL_3 = 3; // Tab三级

	public static final String TAB_APP = "apps"; // 添加应用程序模块
	public static final String TAB_WALLPAPER = "wallpaper"; // 壁纸模块
	public static final String TAB_THEMELOCKER = "themelocker"; // 主题模块
	public static final String TAB_EFFECTS = "effects"; // 特效模块
	public static final String TAB_THEME = "theme"; // 主题模块（二级）
	public static final String TAB_LOCKER = "locker"; // 锁屏模块（二级）
	public static final String TAB_GOWALLPAPER = "gowallpaper"; // Go壁纸模块(二级)
	public static final String TAB_GOWIDGET = "gowidgets"; // Go小部件模块(二级)

	public static final String TAB_ADDAPPS = "add_apps"; // 应用程序添加(三级)
	public static final String TAB_ADDFOLDER = "add_folder"; // 文件夹添加(三级)
	public static final String TAB_ADDGOSHORTCUT = "add_goshortcut"; // Go快捷方式添加(三级)
	public static final String TAB_ADDGOWIDGET = "add_gowidget"; // Go小部件添加(三级)

	public Context mContext;
	public String mTag; // tab标识
	public LayoutInflater mInflater;
	public IDataSetChangeListener mListener;

	public abstract ArrayList<Object> getDtataList();

	public abstract int getItemCount();

	public abstract View getView(int position);

	public abstract void resetData();

	public int mTabLevel; // 所属的Tab级别
	private Paint mPaint;
	private PorterDuffXfermode mXfermode;
	private BitmapDrawable mIconBody;
	private BitmapDrawable mIconMask;
	public TabActionListener mTabActionListener; // 刷新
	private boolean mIsFirstConstruct; // 初次进入tab
	protected boolean mIsNeedAsyncLoadData; // 是否需要在构造函数中异步加载数据

	public BaseTab(Context context, String tag, int tabLevel) {
		mContext = context;
		mTag = tag;
		mTabLevel = tabLevel;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPaint = new Paint();
		mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
		mIconBody = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.screen_edit_icon_body);
		mIconMask = (BitmapDrawable) context.getResources().getDrawable(
				R.drawable.screen_edit_icon_mask);
		setFirstConstruct(true);
	}
  // 设置tab动作监听
	public void setTabActionListener(TabActionListener listener) {
		mTabActionListener = listener;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onLongClick(View v) {
		return false;
	}

	/***
	 * 查看桌面是否还能放下指定大小的组件
	 * 
	 * @param spanX
	 *            行
	 * @param spanY
	 *            列
	 * @return
	 */
	public boolean checkScreenVacant(int spanX, int spanY) {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(spanX);
		list.add(spanY);
		return GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_EDIT_PRE_ADD, 0, null, list);
	}

	/***
	 * 当前是否显示了拖拽层
	 * 
	 * @return
	 */
	public boolean showDragFrame() {
		AbstractFrame topFrame = GoLauncher.getTopFrame();
		if (topFrame != null && topFrame.getId() == IDiyFrameIds.DRAG_FRAME) {
			return true;
		}
		return false;
	}

	public void clearData() {
		mTabActionListener = null;
		mTag = null;
		mInflater = null;
		//		mContext = null;
		mListener = null;
		mPaint = null;
		mXfermode = null;
		mIconBody = null;
		mIconMask = null;
		return;
	}
	// 获取tab标签
	public String getTag() {
		return mTag;
	}
	// 设置数据变更监听
	public void setDataSetListener(IDataSetChangeListener listener) {
		mListener = listener;
	}

	/**
	 * 返回合适添加模块的icon的图片
	 * 
	 * @param drawable
	 *            原图
	 * @param mask
	 *            是否需要勾图
	 * @return
	 */
	public BitmapDrawable getFitIcon(Drawable drawable, boolean mask) {
		try {
			int maskH = mIconBody.getIntrinsicHeight();
			int maskW = mIconBody.getIntrinsicWidth();
			Bitmap temp = Bitmap.createBitmap(maskW, maskH, Bitmap.Config.ARGB_8888);
			Canvas canvasTemp = new Canvas(temp);
			if (mask) {
				canvasTemp.drawColor(Color.WHITE);
			}
			if (drawable != null) {
				Bitmap oldbmp = null;
				// drawable 转换成 bitmap
				if (drawable instanceof BitmapDrawable) {
					// 如果传入的drawable是BitmapDrawable,就不必要生成新的bitmap
					oldbmp = ((BitmapDrawable) drawable).getBitmap();
				} else {
					oldbmp = Utilities.createBitmapFromDrawable(drawable);
				}

				int width = oldbmp.getWidth();
				int height = oldbmp.getHeight();

				Matrix matrix = new Matrix(); // 创建操作图片用的 Matrix 对象
				float scale;
				int offsetCutX = 0;
				int offsetCutY = 0;
				if (width > height) {
					offsetCutX = (width - height) / 2;
					scale = (float) maskH / height;
				} else {
					offsetCutY = (height - width) / 2;
					scale = (float) maskW / width;
				}
				matrix.postScale(scale, scale); // 设置缩放比例
				Bitmap newbmp = Bitmap.createBitmap(oldbmp, offsetCutX, offsetCutY,
						width - 2 * offsetCutX, height - 2 * offsetCutY, matrix, true); // 建立新的
																							// bitmap
																							// ，其内容是对原
																							// bitmap的缩放后的图
				final int drawTop = Math.max(0, maskH - newbmp.getHeight());
				final int drawLeft = Math.max(0, (maskW - newbmp.getWidth()) / 2);
				canvasTemp.drawBitmap(newbmp, drawLeft, drawTop, mPaint);
				matrix = null;
			}

			if (mask) {
				Xfermode xf = mPaint.getXfermode();
				mPaint.setXfermode(mXfermode);
				// 画勾图的形状
				canvasTemp.drawBitmap(mIconMask.getBitmap(), 0, 0, mPaint);
				mPaint.setXfermode(xf);
				// 画上面的罩子
				canvasTemp.drawBitmap(mIconBody.getBitmap(), 0, 0, mPaint);
			}

			return new BitmapDrawable(mContext.getResources(), temp); // 把
																		// bitmap
																		// 转换成
																		// drawable
																		// 并返回
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	public void handleAppChanged(int msgId, String pkgName) {

	}

	public boolean isFirstConstruct() {
		return mIsFirstConstruct;
	}

	public void setFirstConstruct(boolean mIsFirstConstruct) {
		this.mIsFirstConstruct = mIsFirstConstruct;
	}

	public boolean isNeedAsyncLoadData() {
		return mIsNeedAsyncLoadData;
	}
}
