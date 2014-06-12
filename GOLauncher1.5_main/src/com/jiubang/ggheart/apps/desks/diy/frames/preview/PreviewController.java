/**
 * 
 */
package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.theme.DeskThemeControler;

/**
 * 预览层控制类，管理资源
 * 
 * @author ruxueqin
 * 
 */
public class PreviewController implements ICleanable {

	private DeskThemeControler mDeskThemeControler; //指针，方便显资源

	/**
	 * 屏幕显示模式，有些资源需要区别横竖屏来取资源图片, 在SensePreviewFrame里设置此值
	 */
	public static int sDisplayMode;
	public final static int PORT = 0; // 竖屏
	public final static int LAND = 1; // 横屏

	/**
	 * @param context
	 */
	public PreviewController(Context context) {
		mDeskThemeControler = AppCore.getInstance().getDeskThemeControler();
	}

	/**
	 * 返回发光边框图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	public Drawable getLightBorderImg() {
		Drawable drawable = null;

		String lightBorderImgResName = null;
		try {
			lightBorderImgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mCurrScreen.mItem.mBackground.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(lightBorderImgResName,
					R.drawable.preview_border_light);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	/**
	 * 返回不发光边框图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	public Drawable getBorderImg() {
		Drawable drawable = null;

		String imgResName = null;
		try {
			imgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mScreen.mItem.mBackground.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(imgResName, R.drawable.preview_border);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	public Drawable getFocusBorderImg() {
		Drawable drawable = null;

		String imgResName = null;
		try {
			imgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mFucosScreen.mItem.mBackground.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(imgResName, R.drawable.preview_border_drag);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	public Drawable getFocusAddBorderImg() {
		Drawable drawable = null;

		String imgResName = null;
		try {
			imgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mFocusAddScreen.mItem.mBackground.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(imgResName,
					R.drawable.preview_addscreen_light);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	// public Drawable getDeleteBorderImg()
	// {
	// Drawable drawable = null;
	//
	// String ImgResName = null;
	// try
	// {
	// ImgResName =
	// mDeskThemeControler.getDeskThemeBean().mPreview.mDeleteScreen.mItem.mBackground.mResName;
	// }
	// catch (Exception e)
	// {
	// // 取不到，空指针或其他情况
	// e.printStackTrace();
	// }
	//
	// try
	// {
	// drawable = mDeskThemeControler.getDrawable(ImgResName,
	// R.drawable.preview_border_del);
	// }
	// catch (Exception e)
	// {
	// // 防空指针
	// e.printStackTrace();
	// }
	// catch (OutOfMemoryError e)
	// {
	// OutOfMemoryHandler.handle();
	// e.printStackTrace();
	// }
	//
	// return drawable;
	// }

	/**
	 * 返回发光主屏Home图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	public Drawable getLightHomeImg() {
		Drawable drawable = null;

		String imgResName = null;
		try {
			imgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mHome.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(imgResName,
					R.drawable.preview_home_btn_light);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	/**
	 * 返回不发光非主屏Home图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	public Drawable getHomeImg() {
		Drawable drawable = null;

		String imgResName = null;
		try {
			imgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mNotHome.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(imgResName, R.drawable.preview_home_btn);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	/**
	 * 返回发光没有空间背景图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	public Drawable getLightNoRoom() {
		Drawable drawable = null;

		String imgResName = null;
		try {
			imgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mAddScreen.mItem.mBackground.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(imgResName, R.drawable.lightnoroom);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	/**
	 * 返回不发光非没有空间背景图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	public Drawable getNoRoom() {
		Drawable drawable = null;

		String imgResName = null;
		try {
			imgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mAddScreen.mItem.mBackground.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(imgResName, R.drawable.noroom);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	/**
	 * 返回Del点击状态图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	// public Drawable getLightDelImg()
	// {
	// Drawable drawable = null;
	//
	// String ImgResName = null;
	// try
	// {
	// ImgResName =
	// mDeskThemeControler.getDeskThemeBean().mPreview.mColsing.mResName;
	// } catch (Exception e)
	// {
	// // 取不到，空指针或其他情况
	// e.printStackTrace();
	// }
	//
	// try
	// {
	// drawable = mDeskThemeControler.getDrawable(ImgResName,
	// R.drawable.preview_del_btn_light);
	// } catch (Exception e)
	// {
	// // 防空指针
	// e.printStackTrace();
	// } catch (OutOfMemoryError e) {
	// OutOfMemoryHandler.handle();
	// e.printStackTrace();
	// }
	//
	// return drawable;
	// }

	/**
	 * 返回Del非点击状态图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	// public Drawable getDelImg()
	// {
	// Drawable drawable = null;
	//
	// String ImgResName = null;
	// try
	// {
	// ImgResName =
	// mDeskThemeControler.getDeskThemeBean().mPreview.mColsed.mResName;
	// } catch (Exception e)
	// {
	// // 取不到，空指针或其他情况
	// e.printStackTrace();
	// }
	//
	// try
	// {
	// drawable = mDeskThemeControler.getDrawable(ImgResName,
	// R.drawable.preview_del_btn);
	// } catch (Exception e)
	// {
	// // 防空指针
	// e.printStackTrace();
	// } catch (OutOfMemoryError e) {
	// OutOfMemoryHandler.handle();
	// e.printStackTrace();
	// }
	//
	// return drawable;
	// }

	/**
	 * 返回+号点击状态图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	public Drawable getLightAddImg() {
		Drawable drawable = null;

		String imgResName = null;
		try {
			imgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mFocusAddScreen.mItem.mBackground.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(imgResName,
					R.drawable.preview_addscreen_light);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	/**
	 * 返回Del非点击状态图 优先取主题指定图片，取不到再取默认图片
	 * 
	 * @return
	 */
	public Drawable getAddImg() {
		Drawable drawable = null;

		String imgResName = null;
		try {
			imgResName = mDeskThemeControler.getDeskThemeBean().mPreview.mAddScreen.mItem.mBackground.mResName;
		} catch (Exception e) {
			// 取不到，空指针或其他情况
			e.printStackTrace();
		}

		try {
			drawable = mDeskThemeControler.getDrawable(imgResName, R.drawable.preview_addscreen);
		} catch (Exception e) {
			// 防空指针
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			e.printStackTrace();
		}

		return drawable;
	}

	/**
	 * 读取ＨＯＭＥ排版锚点
	 * 
	 * @return
	 */
	public Point getmHomeTopAndLeftPoint() {
		Point point = new Point();
		try {
			if (sDisplayMode == PORT) {
				point.x = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean().mPreview.mHome.mPortMargins.mLeft;
				point.y = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean().mPreview.mHome.mPortMargins.mTop;
			} else {
				point.x = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean().mPreview.mHome.mLandMargins.mLeft;
				point.y = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean().mPreview.mHome.mLandMargins.mTop;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return point;
	}

	/**
	 * 读取ＤＥＬ排版锚点
	 * 
	 * @return
	 */
	public Point getmDelTopAndLeftPoint() {
		Point point = new Point();
		try {
			if (sDisplayMode == PORT) {
				point.x = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean().mPreview.mColsed.mPortMargins.mLeft;
				point.y = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean().mPreview.mColsed.mPortMargins.mTop;
			} else {
				point.x = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean().mPreview.mColsed.mLandMargins.mLeft;
				point.y = AppCore.getInstance().getDeskThemeControler().getDeskThemeBean().mPreview.mColsed.mLandMargins.mTop;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return point;
	}

	@Override
	public void cleanup() {
	}

}
