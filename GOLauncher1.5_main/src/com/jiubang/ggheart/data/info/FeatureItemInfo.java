package com.jiubang.ggheart.data.info;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.tables.FolderTable;
import com.jiubang.ggheart.data.tables.PartToScreenTable;
import com.jiubang.ggheart.data.tables.ShortcutTable;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * @author 
 *
 */
public class FeatureItemInfo extends RelativeItemInfo {
	// icon
	// 1. resource
	// 3. file
	private Drawable mFeatureIcon;
	public int mFeatureIconId;
	public String mFeatureIconPath;
	public int mFeatureIconType;
	public String mFeatureIconPackage;

	// title
	public String mFeatureTitle;

	public FeatureItemInfo() {
		super();
		resetFeature();
	}

	public FeatureItemInfo(FeatureItemInfo info) {
		super(info);

		mFeatureIconType = info.mFeatureIconType;
		mFeatureIconPackage = info.mFeatureIconPackage;
		mFeatureIconId = info.mFeatureIconId;
		mFeatureIconPath = info.mFeatureIconPath;

		mFeatureTitle = info.mFeatureTitle;
	}

	public void resetFeature() {
		mFeatureIcon = null;
		mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
		mFeatureIconPackage = null;
		mFeatureIconId = 0;
		mFeatureIconPath = null;

		mFeatureTitle = null;
	}

	public void setFeatureIcon(Drawable icon, int iconType, String iconPackage, int iconId,
			String iconPath) {
		mFeatureIcon = icon;
		mFeatureIconType = iconType;
		mFeatureIconPackage = iconPackage;
		mFeatureIconId = iconId;
		mFeatureIconPath = iconPath;
	}

	public Drawable getFeatureIcon() {
		return mFeatureIcon;
	}

	public boolean prepareFeatureIcon() {
		if (ImagePreviewResultType.TYPE_IMAGE_FILE == mFeatureIconType) {
			try {
				BitmapDrawable icon = (BitmapDrawable) Drawable.createFromPath(mFeatureIconPath);
				icon.setTargetDensity(GOLauncherApp.getContext().getResources().getDisplayMetrics());
				mFeatureIcon = icon;
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (ImagePreviewResultType.TYPE_IMAGE_URI == mFeatureIconType) {
			try {
				Uri uri = ConvertUtils.stringToUri(mFeatureIconPath);
				Bitmap bitmap = BitmapUtility.loadBitmap(GOLauncherApp.getContext(), uri, 1);
				if (null != bitmap) {
					int sz = 0;
					if (Machine.isLephone()) {
						sz = Machine.LEPHONE_ICON_SIZE;
					} else {
						final Resources resources = GOLauncherApp.getContext().getResources();
						// sz = (int)
						// resources.getDimension(android.R.dimen.app_icon_size);
						sz = (int) resources.getDimension(R.dimen.screen_icon_size);
					}
					int bmpW = bitmap.getWidth();
					int bmpH = bitmap.getHeight();
					float wScale = (float) sz / (float) bmpW;
					float hScale = (float) sz / (float) bmpH;
					float scale = wScale < hScale ? wScale : hScale;
					boolean needScale = scale < 1;
					boolean needYFit = false;
					if (scale < 1) {
						needYFit = (int) (bmpH * scale) < sz - 1; // 由于缩放误差1个单位
					} else {
						needYFit = bmpH < sz;
					}
					boolean needXFit = false; // for dock
					if (scale < 1) {
						needXFit = (int) (bmpW * scale) < sz - 1; // 由于缩放误差1个单位
					} else {
						needXFit = bmpW < sz;
					}
					if (needScale) {
						Bitmap tempbitmap = BitmapUtility.createScaledBitmap(bitmap,
								(int) (bmpW * scale), (int) (bmpH * scale));
						bitmap.recycle();
						bitmap = tempbitmap;
					}
					if (needYFit || needXFit) {
						Bitmap tempbitmap = BitmapUtility.createBitmap(bitmap, sz, sz);
						bitmap.recycle();
						bitmap = tempbitmap;
					}
					BitmapDrawable icon = new BitmapDrawable(GOLauncherApp.getContext()
							.getResources(), bitmap);
					mFeatureIcon = icon;
				}
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (ImagePreviewResultType.TYPE_PACKAGE_RESOURCE == mFeatureIconType
				|| ImagePreviewResultType.TYPE_APP_ICON == mFeatureIconType) {
			mFeatureIcon = ImageExplorer.getInstance(GOLauncherApp.getContext()).getDrawable(
					mFeatureIconPackage, mFeatureIconPath);
		}
		return null != mFeatureIcon;
	}

	public void setFeatureTitle(String title) {
		mFeatureTitle = title;
	}

	public String getFeatureTitle() {
		return mFeatureTitle;
	}

	public void copyFeatureItemInfo(FeatureItemInfo info) {
		if (null == info) {
			return;
		}

		mFeatureIconId = info.mFeatureIconId;
		mFeatureIconPackage = info.mFeatureIconPackage;
		mFeatureIconPath = info.mFeatureIconPath;
		mFeatureIconType = info.mFeatureIconType;
		mFeatureTitle = info.mFeatureTitle;
	}

	@Override
	public void writeObject(ContentValues values, String table) {
		super.writeObject(values, table);

		if (table.equals(PartToScreenTable.TABLENAME)) {
			values.put(PartToScreenTable.USERICONTYPE, mFeatureIconType);
			values.put(PartToScreenTable.USERICONPACKAGE, mFeatureIconPackage);
			values.put(PartToScreenTable.USERICONID, mFeatureIconId);
			values.put(PartToScreenTable.USERICONPATH, mFeatureIconPath);
			values.put(PartToScreenTable.USERTITLE, mFeatureTitle);
		} else if (table.equals(FolderTable.TABLENAME)) {
			values.put(FolderTable.USERICONTYPE, mFeatureIconType);
			values.put(FolderTable.USERICONPACKAGE, mFeatureIconPackage);
			values.put(FolderTable.USERICONID, mFeatureIconId);
			values.put(FolderTable.USERICONPATH, mFeatureIconPath);
			values.put(FolderTable.USERTITLE, mFeatureTitle);
		} else if (table.equals(ShortcutTable.TABLENAME)) {
			values.put(ShortcutTable.USERICONTYPE, mFeatureIconType);
			values.put(ShortcutTable.ICONTYPE, mFeatureIconType);
			values.put(ShortcutTable.USERICONPACKAGE, mFeatureIconPackage);
			values.put(ShortcutTable.USERICONID, mFeatureIconId);
			values.put(ShortcutTable.USERICONPATH, mFeatureIconPath);
			values.put(ShortcutTable.USERTITLE, mFeatureTitle);
		}
	}

	@Override
	public void readObject(Cursor cursor, String table) {
		super.readObject(cursor, table);

		if (table.equals(PartToScreenTable.TABLENAME)) {
			mFeatureIconType = cursor.getInt(cursor.getColumnIndex(PartToScreenTable.USERICONTYPE));
			mFeatureIconPackage = cursor.getString(cursor
					.getColumnIndex(PartToScreenTable.USERICONPACKAGE));
			mFeatureIconId = cursor.getInt(cursor.getColumnIndex(PartToScreenTable.USERICONID));
			mFeatureIconPath = cursor.getString(cursor
					.getColumnIndex(PartToScreenTable.USERICONPATH));
			mFeatureTitle = cursor.getString(cursor.getColumnIndex(PartToScreenTable.USERTITLE));
		} else if (table.equals(FolderTable.TABLENAME)) {
			mFeatureIconType = cursor.getInt(cursor.getColumnIndex(FolderTable.USERICONTYPE));
			mFeatureIconPackage = cursor.getString(cursor
					.getColumnIndex(FolderTable.USERICONPACKAGE));
			mFeatureIconId = cursor.getInt(cursor.getColumnIndex(FolderTable.USERICONID));
			mFeatureIconPath = cursor.getString(cursor.getColumnIndex(FolderTable.USERICONPATH));
			mFeatureTitle = cursor.getString(cursor.getColumnIndex(FolderTable.USERTITLE));
		} else if (table.equals(ShortcutTable.TABLENAME)) {
			mFeatureIconType = cursor.getInt(cursor.getColumnIndex(ShortcutTable.ICONTYPE));
			mFeatureIconPackage = cursor.getString(cursor
					.getColumnIndex(ShortcutTable.USERICONPACKAGE));
			mFeatureIconId = cursor.getInt(cursor.getColumnIndex(ShortcutTable.USERICONID));
			mFeatureIconPath = cursor.getString(cursor.getColumnIndex(ShortcutTable.USERICONPATH));
			mFeatureTitle = cursor.getString(cursor.getColumnIndex(ShortcutTable.USERTITLE));

			// TODO:处理public String mUsePackage; //设置图标时的当前主题
		}
	}
}
