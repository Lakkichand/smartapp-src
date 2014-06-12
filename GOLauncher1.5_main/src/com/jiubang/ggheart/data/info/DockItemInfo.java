package com.jiubang.ggheart.data.info;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.Utilities;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.FolderIcon;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.tables.ShortcutTable;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeConfig;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.XmlParserFactory;
import com.jiubang.ggheart.data.theme.bean.DeskFolderThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.FolderStyle;
import com.jiubang.ggheart.data.theme.parser.DeskFolderThemeParser;
import com.jiubang.ggheart.data.theme.parser.IParser;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 后台数据信息
 * 
 * @author ruxueqin
 */
public class DockItemInfo extends BaseItemInfo implements IDatabaseObject {
	public static final int ICONCHANGED = 5; // 图标改变
	public static final int INTENTCHANGED = 6; // Intent改变

	public GestureInfo mGestureInfo;

	public FeatureItemInfo mItemInfo;

	// 排序索引值
	public int mIndex;

	// 在第几行
	private int mRowId = -1;

	// 所在行的第几个图标
	private int mIndexInRow = -1;

	// bitmap　size用于在setIcon时进行对原图片缩放
	private int mBmpSize;

	/*************************************************************************/

	// 源应用数据
	// private AppItemInfo mAppItemInfo;

	// 关联应用源的标记
	// public Intent mIntent;

	// 特色图标
	// public BitmapDrawable mUserIcon; //经过主题、风格变换后的图标
	// 不直接存储图片
	// 根据类型来区分ICON类型
	// 0 : 来自资源
	// 1 : 来自文件
	// 2 : 来自系统默认
	// public int mIconType;
	// public int mIconId;
	// public String mIconPackage; //目标图标的所在主题
	public String mUsePackage; // 设置图标时的当前主题
	// public String mIconPath; //图片名称
	private BitmapDrawable mFolderIcon; // 用来保存folder合成图

	/**
	 * DockItemInfo构造函数
	 * 
	 * @param type
	 *            　IItemType类型
	 * @param bmpSize
	 *            　图标图片size
	 */
	public DockItemInfo(int type, int bmpSize) {
		mGestureInfo = new GestureInfo();

		mItemInfo = (FeatureItemInfo) ItemInfoFactory.createItemInfo(type);
		mItemInfo.registerObserver(this);

		mBmpSize = bmpSize;
	}

	public void setInfo(FeatureItemInfo info) {
		if (null != mItemInfo) {
			mItemInfo.unRegisterObserver(this);
		}

		mItemInfo = info;
		if (null != mItemInfo) {
			mItemInfo.registerObserver(this);
		}
	}

	public int getDockIndex() {
		return mIndex;
	}

	public void setIcon(BitmapDrawable icon) {
		// 处理图片
		if (null != icon && null != icon.getBitmap()) {
			// BitmapDrawable tempicon = scaleBitmap(icon.getBitmap(), sSize,
			// sSize);
			final Resources resources = GOLauncherApp.getContext().getResources();
			BitmapDrawable tempicon = new BitmapDrawable(resources,
					BitmapUtility.createScaledBitmap(icon.getBitmap(), mBmpSize, mBmpSize));
			if (null != tempicon) {
				tempicon.setTargetDensity(resources.getDisplayMetrics());
				icon = tempicon;
			}
		}
		if (mItemInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION
				|| mItemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT) {
			if (null != ((ShortCutInfo) mItemInfo).mIcon) {
				((ShortCutInfo) mItemInfo).mIcon.setCallback(null);
			}
			((ShortCutInfo) mItemInfo).mIcon = icon;
		} else if (mItemInfo.mItemType == IItemType.ITEM_TYPE_USER_FOLDER) {
			mFolderIcon = icon;
		}

		// 通知图标更换
		broadCast(ICONCHANGED, 0, icon, null);
	}

	public BitmapDrawable getIcon() {
		if ((mItemInfo.mItemType == IItemType.ITEM_TYPE_APPLICATION || mItemInfo.mItemType == IItemType.ITEM_TYPE_SHORTCUT)
				&& null != ((ShortCutInfo) mItemInfo).mIcon) {
			if (((ShortCutInfo) mItemInfo).mIcon instanceof BitmapDrawable) {
				BitmapDrawable bitmapDrawable = (BitmapDrawable) ((ShortCutInfo) mItemInfo).mIcon;
				if (bitmapDrawable.getIntrinsicWidth() != mBmpSize
						&& bitmapDrawable.getIntrinsicWidth() > 0) {
					Resources resources = GOLauncherApp.getContext().getResources();
					// 解决主题装在SD卡上， 浏览器图标变大问题
					BitmapDrawable bitmapDrawable2 = new BitmapDrawable(GOLauncherApp.getContext()
							.getResources(), BitmapUtility.createScaledBitmap(
							bitmapDrawable.getBitmap(), mBmpSize, mBmpSize));
					if (null != bitmapDrawable2) {
						bitmapDrawable2.setTargetDensity(resources.getDisplayMetrics());
						((ShortCutInfo) mItemInfo).mIcon = bitmapDrawable2;
					}
				}
				return (BitmapDrawable) ((ShortCutInfo) mItemInfo).mIcon;
			} else {
				final Resources resources = GOLauncherApp.getContext().getResources();
				return new BitmapDrawable(resources,
						Utilities.createBitmapFromDrawable(((ShortCutInfo) mItemInfo).mIcon));
			}
		}

		if (mItemInfo instanceof UserFolderInfo) {
			return mFolderIcon;
		}
		return null;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public void onBCChange(int msgId, int param, Object object, List objects) {
		super.onBCChange(msgId, param, object, objects);

		if (AppItemInfo.INCONCHANGE == msgId) {
			if (null != mItemInfo && null != object && object instanceof BitmapDrawable) {
				if (mItemInfo instanceof UserFolderInfo) {
					BitmapDrawable drawable = getFolderBackIcon();
					if (null != drawable) {
						final Resources resources = GOLauncherApp.getContext().getResources();
						setIcon(new BitmapDrawable(resources, prepareOpenFolderIcon(drawable)));
					}
				} else if (ImagePreviewResultType.TYPE_DEFAULT == mItemInfo.mFeatureIconType) {
					BitmapDrawable icon = (BitmapDrawable) object;
					setIcon(icon);
				}
			}
		}
	}

	@Override
	public void readObject(Cursor cursor, String table) {
		mGestureInfo.readObject(cursor, table);
		mItemInfo.readObject(cursor, table);
	}

	@Override
	public void writeObject(ContentValues values, String table) {
		if (ShortcutTable.TABLENAME.equals(table)) {
			mGestureInfo.writeObject(values, table);
			mItemInfo.writeObject(values, table);

			// dock3.0:加入rowid和indexinrow
			values.put(ShortcutTable.ROWSID, mRowId);
			values.put(ShortcutTable.MINDEX, mIndexInRow);
			values.put(ShortcutTable.THEMENAME, ThemeManager.DEFAULT_THEME_PACKAGE);
		}
	}

	public Bitmap prepareOpenFolderIcon(Drawable drawable) {
		Drawable openIcon = null;
		Drawable closeIcon = null;
		Canvas canvas = null;
		Paint paint = null;
		ArrayList<Drawable> folderIcons = new ArrayList<Drawable>();
		getFolderIcon(folderIcons);
		openIcon = folderIcons.get(0);
		closeIcon = folderIcons.get(1);
		paint = new Paint();
		paint.setDither(false);
		paint.setFilterBitmap(true);
		final Context context = GOLauncherApp.getContext();
		// final boolean drawInnerPic = ImagePreviewResultType.TYPE_IMAGE_FILE
		// != ((UserFolderInfo) mItemInfo).mFeatureIconType;
		boolean drawInnerPic = false;
		int tempType = ((UserFolderInfo) mItemInfo).mFeatureIconType;
		if (tempType != ImagePreviewResultType.TYPE_IMAGE_FILE
				&& tempType != ImagePreviewResultType.TYPE_APP_ICON) {
			drawInnerPic = true;
		}
		final int iconSize = Utilities.getStandardIconSize(context);
		Bitmap newBitmap = FolderIcon.combinDraw(drawable, (UserFolderInfo) mItemInfo, iconSize,
				drawInnerPic);

		if (null == newBitmap) {
			return null;
		}

		Bitmap bitmap = null;
		if (newBitmap.isMutable()) {
			bitmap = newBitmap;
		} else {
			try {
				// 防止出现Immutable bitmap passed to Canvas constructor错误,所以用copy
				bitmap = Bitmap.createBitmap(newBitmap.copy(Bitmap.Config.ARGB_8888, true));
			} catch (Exception e) {
				OutOfMemoryHandler.handle();
			}
			// 将newBitmap置空，以便系统加快回收资源
			if (newBitmap != null) {
				newBitmap.recycle();
				newBitmap = null;
			}
		}

		if (null == openIcon /* || null == closeIcon */ || null == bitmap) {
			folderIcons.clear();
			folderIcons = null;
			return null;
		}

		Matrix matrix = new Matrix();
		if (!((UserFolderInfo) mItemInfo).mOpened) {
			canvas = new Canvas(bitmap);
			canvas.drawBitmap(bitmap, 0, 0, paint);
			if (null != closeIcon && drawInnerPic) {
				Bitmap closeBitmap = ((BitmapDrawable) closeIcon).getBitmap();
				if (closeBitmap != null && !closeBitmap.isRecycled()) {
					final float scale = (iconSize + 0.1f) / closeBitmap.getWidth();
					matrix.setScale(scale, scale);
					canvas.drawBitmap(closeBitmap, matrix, paint);
				}
			}
			folderIcons.clear();
			folderIcons = null;
			return bitmap;
		}

		canvas = new Canvas(bitmap);
		if (bitmap != null && !bitmap.isRecycled()) {
			// 画合成图
			canvas.drawBitmap(bitmap, 0, 0, paint);
		}

		final Bitmap tempBitmap = ((BitmapDrawable) openIcon).getBitmap();
		if (tempBitmap != null && !tempBitmap.isRecycled()) {
			final float scale = (iconSize + 0.1f) / tempBitmap.getWidth();
			matrix.setScale(scale, scale);
			canvas.drawBitmap(tempBitmap, matrix, paint);
		}
		folderIcons.clear();
		folderIcons = null;
		return bitmap;

	}

	public BitmapDrawable getFolderBackIcon() {
		if (mItemInfo.mFeatureIconType == ImagePreviewResultType.TYPE_DEFAULT) {
			DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
			FolderStyle folderStyle = null;
			// GO主题类型
			ImageExplorer imageExplorer = ImageExplorer.getInstance(GOLauncherApp.getContext());
			if (themeControler != null /* && themeControler.isUesdTheme() */) {
				DeskThemeBean themeBean = themeControler.getDeskThemeBean();
				if (themeBean != null && themeBean.mScreen != null) {
					folderStyle = themeBean.mScreen.mFolderStyle;
				}
			}
			if (folderStyle != null && folderStyle.mBackground != null) {
				Drawable icon = imageExplorer.getDrawable(folderStyle.mPackageName,
						folderStyle.mBackground.mResName);
				if (icon != null) {
					return (BitmapDrawable) icon;
				}
			}
			return (BitmapDrawable) GOLauncherApp.getContext().getResources()
					.getDrawable(R.drawable.folder_back);
		}
		if (null != mItemInfo.getFeatureIcon()) {
			return (BitmapDrawable) mItemInfo.getFeatureIcon();
		}
		return null;
	}

	/**
	 * 获得文件夹发开始的开口图片 获得文件夹最上面的罩子图片
	 * 
	 * @param icons
	 */
	private void getFolderIcon(ArrayList<Drawable> icons) {
		Drawable openIcon = null;
		Drawable closeIcon = null;
		// 文件夹样式
		FolderStyle folderStyle = null;
		DeskThemeControler themeControler = null;

		// 获取图标类型
		int type = ((UserFolderInfo) mItemInfo).getmFeatureIconType();
		// 图标的主题包
		String packageName = ((UserFolderInfo) mItemInfo).getmFeatureIconPackage();
		// 判断改主题是否有安装
		boolean isInstall = AppUtils.isAppExist(GOLauncherApp.getContext(), packageName);
		ImageExplorer imageExplorer = ImageExplorer.getInstance(GOLauncherApp.getContext());
		// GO主题类型
		if ((type == ImagePreviewResultType.TYPE_PACKAGE_RESOURCE || type == ImagePreviewResultType.TYPE_APP_ICON)
				&& isInstall) {
			// 如果使用的是默认主题的GO样式
			if (packageName.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
				openIcon = imageExplorer.getDrawable(packageName, FolderIcon.DEFAULT_OPEN_RES);
				closeIcon = imageExplorer.getDrawable(packageName, FolderIcon.DEFAULT_CLOSE_RES);
			} else {
				InputStream inputStream = null;
				XmlPullParser xmlPullParser = null;
				DeskFolderThemeBean themeBean = null;
				IParser parser = null;

				// 解析桌面中相关主题信息
				inputStream = ThemeManager.getInstance(GOLauncherApp.getApplication())
						.createParserInputStream(packageName, ThemeConfig.DESKTHEMEFILENAME);
				if (inputStream != null) {
					xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
				} else {
					xmlPullParser = XmlParserFactory.createXmlParser(GOLauncherApp.getApplication(),
							ThemeConfig.DESKTHEMEFILENAME, packageName);
				}
				if (xmlPullParser != null) {
					themeBean = new DeskFolderThemeBean(packageName);
					parser = new DeskFolderThemeParser();
					parser.parseXml(xmlPullParser, themeBean);
					parser = null;

					if (themeBean != null && themeBean.mFolderStyle != null) {
						if (themeBean.mFolderStyle.mOpendFolder != null) {
							openIcon = imageExplorer.getDrawable(packageName,
									themeBean.mFolderStyle.mOpendFolder.mResName);
							if (null == openIcon) {
								openIcon = imageExplorer.getDrawable(
										ThemeManager.DEFAULT_THEME_PACKAGE,
										FolderIcon.DEFAULT_OPEN_RES);
							}
						}
						if (themeBean.mFolderStyle.mClosedFolder != null) {
							closeIcon = imageExplorer.getDrawable(packageName,
									themeBean.mFolderStyle.mClosedFolder.mResName);
							if (null == closeIcon) {
								closeIcon = imageExplorer.getDrawable(
										ThemeManager.DEFAULT_THEME_PACKAGE,
										FolderIcon.DEFAULT_CLOSE_RES);
							}
						}
					}
				}
				// 关闭inputStream
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		} else {
			themeControler = AppCore.getInstance().getDeskThemeControler();
			if (themeControler != null /* && themeControler.isUesdTheme() */) {
				DeskThemeBean themeBean = themeControler.getDeskThemeBean();
				if (themeBean != null && themeBean.mScreen != null) {
					folderStyle = themeBean.mScreen.mFolderStyle;
				}
			}
			if (folderStyle != null && folderStyle.mOpendFolder != null) {
				Drawable tempDrawable = imageExplorer.getDrawable(folderStyle.mPackageName,
						folderStyle.mOpendFolder.mResName);
				if (tempDrawable != null && tempDrawable instanceof BitmapDrawable) {
					openIcon = tempDrawable;
				}
			}
			if (openIcon == null) {
				openIcon = GOLauncherApp.getContext().getResources()
						.getDrawable(R.drawable.folder_open_top);
			}

			if (folderStyle != null && folderStyle.mClosedFolder != null
					&& type != ImagePreviewResultType.TYPE_IMAGE_FILE) {
				Drawable tempDrawable = imageExplorer.getDrawable(folderStyle.mPackageName,
						folderStyle.mClosedFolder.mResName);
				if (tempDrawable != null && tempDrawable instanceof BitmapDrawable) {
					closeIcon = tempDrawable;
				}
			}
			if (closeIcon == null && type != ImagePreviewResultType.TYPE_IMAGE_FILE) {
				closeIcon = GOLauncherApp.getContext().getResources()
						.getDrawable(R.drawable.folder_top);
			}
		}

		icons.add(openIcon);
		icons.add(closeIcon);
	}

	/**
	 * @return the mRowId
	 */
	public int getmRowId() {
		return mRowId;
	}

	/**
	 * @param mRowId
	 *            the mRowId to set
	 */
	public void setmRowId(int mRowId) {
		this.mRowId = mRowId;
	}

	/**
	 * @return the mIndexInRow
	 */
	public int getmIndexInRow() {
		return mIndexInRow;
	}

	/**
	 * @param mIndexInRow
	 *            the mIndexInRow to set
	 */
	public void setmIndexInRow(int mIndexInRow) {
		this.mIndexInRow = mIndexInRow;
	}

	public void setBmpSize(int size) {
		mBmpSize = size;
	}

	public int getBmpSize() {
		return mBmpSize;
	}

	/**
	 * <br>
	 * 功能简述:释放info <br>
	 * 功能详细描述: <br>
	 * 注意:在删除dock图标或减少dock行数时调用
	 */
	public void selfDestruct() {
		if (mItemInfo != null
				&& (mItemInfo.getObserver() == null || mItemInfo.getObserver().size() <= 1)) {
			/*
			 * 如果mItemInfo.getObserver().size() > 1,
			 * 说明这个mItemInfo除了DockItemInfo还有其他监听者，此时不释放mItemInfo
			 */
			mItemInfo.selfDestruct();
		}
		clearAllObserver();
	}
}
