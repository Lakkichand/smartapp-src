/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.Utilities;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.components.BubbleTextView;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.UserFolderInfo;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskFolderThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.FolderStyle;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * An icon that can appear on in the workspace representing an
 * {@link UserFolder}.
 */
public class FolderIcon extends BubbleTextView {
	public final static int FOLDER_ICON_OPEN = 0;
	public final static int FOLDER_ICON_CLOSE = 1;

	public final static int INNER_ICON_COLS = 2;
	public final static int INNER_ICON_SIZE = 4;

	private UserFolderInfo mInfo;

	private Drawable mRawIcon; // 原始图标
	private Drawable mCloseIcon; // 关闭图标，需要绘制缩略图
	private Drawable mOpenIcon; // 打开的图标

	// 注：默认主题里面的文件夹图标的名字与其他主题包的不统一
	public static final String DEFAULT_RAWICON_RES = "folder_back"; // 默认的原始文件夹图标名字
	public static final String DEFAULT_CLOSE_RES = "folder_top"; // 默认的原始文件夹关闭图标名字
	public static final String DEFAULT_OPEN_RES = "folder_open_top"; // 默认的原始文件夹打开图标名字

	public static final float FOLDER_INNER_ICONS_PADDING = 0.12f; // 文件夹内部放置图标时的左边距参数（左边距是参数与图标宽度相乘）
	public static final float FOLDER_SCREEN_INNER_ICONS_SPACE_BETWEEN = 0.03f; // 桌面文件夹内部图标间距参数
	public static final float FOLDER_DOCK_INNER_ICONS_SPACE_BETWEEN = 0.08f; // dock文件夹内部图标间距参数

	FolderRingAnimator mFolderRingAnimator = null;

	public FolderIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FolderIcon(Context context) {
		super(context);
	}

	public UserFolderInfo getInfo() {
		return mInfo;
	}

	public void setInfo(UserFolderInfo info) {
		mInfo = info;
	}

	public static FolderIcon fromXml(int resId, Context context, ViewGroup group,
			UserFolderInfo folderInfo, CharSequence text) {

		FolderIcon icon = (FolderIcon) LayoutInflater.from(context).inflate(resId, group, false);
		prepareIcon(icon, folderInfo);
		icon.setIcon(folderInfo.mIcon);
		icon.setText(text);
		icon.setTag(folderInfo);
		icon.setOnClickListener(null);
		icon.mInfo = folderInfo;
		icon.mFolderRingAnimator = new FolderRingAnimator(context, icon);
		icon.mFolderRingAnimator.setFolderBg(context);
		return icon;
	}

	/**
	 * @param context
	 * @param manager
	 * @param themePackage
	 * @param resourceName
	 * @return
	 */
	// static Drawable loadFolderFromTheme(Context context,
	// PackageManager manager, String themePackage, String resourceName) {
	// Drawable icon = null;
	// Resources themeResources = null;
	// try {
	// themeResources = manager.getResourcesForApplication(themePackage);
	// } catch (NameNotFoundException e) {
	// //e.printStackTrace();
	// }
	// if (themeResources != null)
	// {
	// int resource_id = themeResources.getIdentifier(resourceName, "drawable",
	// themePackage);
	// if (resource_id != 0)
	// {
	// icon = themeResources.getDrawable(resource_id);
	// }
	// }
	// return icon;
	// }

	public void open() {
		setIcon(mOpenIcon);
	}

	public void close() {
		setIcon(mCloseIcon);
	}
	
	/**
	 * <br>功能简述:获取文件夹底图
	 * @return
	 */
//	public Drawable getRawIcon() {
//	    return mRawIcon;
//	}
	
	// 解析文件夹
	private static boolean applyTheme(String themePackage, FolderIcon folderIcon) {
		boolean result = true;
		// String fileName = null;
		// InputStream inputStream = null;
		// XmlPullParser xmlPullParser = null;
		Context context = folderIcon.getContext().getApplicationContext();
		DeskFolderThemeBean themeBean = ThemeManager.getInstance(context).parserDeskFolderTheme(
				themePackage);
		// IParser parser = null;

		ImageExplorer imageExplorer = ImageExplorer.getInstance(context);

		// //解析桌面中相关主题信息
		// fileName = ThemeConfig.DESKTHEMEFILENAME;
		// inputStream = XmlParserFactory.createInputStream(context,
		// themePackage, fileName);
		// xmlPullParser = XmlParserFactory.createXmlParser(inputStream);
		// if (xmlPullParser != null) {
		// themeBean = new DeskFolderThemeBean(themePackage);
		// parser = new DeskFolderThemeParser();
		// parser.parseXml(xmlPullParser, themeBean);
		// parser = null;
		//
		if (themeBean != null && themeBean.mFolderStyle != null) {
			if (themeBean.mFolderStyle.mBackground != null) {
				// 换图标的时候需要获取设置图片路径，如果没有就选择默认文件夹图标
				String mResName = null;
				if (folderIcon.getInfo() != null) {
					mResName = folderIcon.getInfo().mFeatureIconPath;
				}
				if (mResName == null || mResName.equals("")) {
					mResName = themeBean.mFolderStyle.mBackground.mResName;
				}
				folderIcon.mRawIcon = imageExplorer.getDrawable(themePackage, mResName);
			}
			if (themeBean.mFolderStyle.mClosedFolder != null) {
				folderIcon.mCloseIcon = imageExplorer.getDrawable(themePackage,
						themeBean.mFolderStyle.mClosedFolder.mResName);
			}
			if (themeBean.mFolderStyle.mOpendFolder != null) {
				folderIcon.mOpenIcon = imageExplorer.getDrawable(themePackage,
						themeBean.mFolderStyle.mOpendFolder.mResName);
			}
		} else {
			result = false;
		}
		// //关闭inputStream
		// if (inputStream != null) {
		// try {
		// inputStream.close();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }

		return result;
	}

	/**
	 * 解析文件夹 add by Ryan 2012.07.28
	 * 
	 * @param themePackage
	 * @param folderIcon
	 * @param folderInfo
	 * @return
	 */
	private static boolean applyTheme(String themePackage, FolderIcon folderIcon,
			UserFolderInfo folderInfo) {
		boolean result = true;
		Context context = folderIcon.getContext().getApplicationContext();
		DeskFolderThemeBean themeBean = ThemeManager.getInstance(context).parserDeskFolderTheme(
				themePackage);

		ImageExplorer imageExplorer = ImageExplorer.getInstance(context);

		if (themeBean != null && themeBean.mFolderStyle != null) {
			if (themeBean.mFolderStyle.mBackground != null) {
				if (folderInfo.getmFeatureIconType() == ImagePreviewResultType.TYPE_APP_ICON) {
					folderIcon.mRawIcon = imageExplorer.getDrawable(folderInfo.mFeatureIconPackage,
							folderInfo.mFeatureIconPath);
				} else {
					// 换图标的时候需要获取设置图片路径，如果没有就选择默认文件夹图标
					String mResName = null;
					if (folderIcon.getInfo() != null) {
						mResName = folderIcon.getInfo().mFeatureIconPath;
					}
					if (mResName == null || mResName.equals("")) {
						mResName = themeBean.mFolderStyle.mBackground.mResName;
					}
					folderIcon.mRawIcon = imageExplorer.getDrawable(themePackage, mResName);
				}
			}
			if (themeBean.mFolderStyle.mClosedFolder != null) {
				folderIcon.mCloseIcon = imageExplorer.getDrawable(themePackage,
						themeBean.mFolderStyle.mClosedFolder.mResName);
			}
			if (themeBean.mFolderStyle.mOpendFolder != null) {
				folderIcon.mOpenIcon = imageExplorer.getDrawable(themePackage,
						themeBean.mFolderStyle.mOpendFolder.mResName);
			}
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * 文件夹底图与预览图的合并
	 * 
	 * @param drawable
	 *            源图
	 * @param contents
	 *            文件夹的内容
	 * @param drawInnerPic
	 *            是否画缩略图的标识
	 * @param size
	 *            桌面图标大小
	 * @param return Bitmap 返回如果是null的话，调用的地方要再做相应的处理
	 */
	public static Bitmap combinDraw(Drawable drawable, UserFolderInfo folderInfo, int size,
			boolean drawInnerPic) {
		if (drawable == null || folderInfo == null) {
			return null;
		}
		Paint paint = new Paint();
		paint.setDither(false);
		paint.setFilterBitmap(true);
		final int iconSize = size;

		Bitmap bitmap = null;
		try {
			bitmap = Bitmap.createBitmap(iconSize, iconSize, Config.ARGB_8888);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}

		if (bitmap == null) {
			return null;
		}

		Canvas canvas = new Canvas(bitmap);
		// 画文件夹底图
		final Bitmap folderbackBitmap = ((BitmapDrawable) drawable).getBitmap();

		if (folderbackBitmap != null && !folderbackBitmap.isRecycled()) {
			// canvas.drawBitmap(folderbackBitmap, 0, 0, paint);
			Rect rect = new Rect(0, 0, folderbackBitmap.getWidth(), folderbackBitmap.getWidth());
			RectF rectF = new RectF(0, 0, iconSize, iconSize);
			canvas.drawBitmap(folderbackBitmap, rect, rectF, paint);
			rect = null;
			rectF = null;
			// 如果不画预览图，直接返回
			if (!drawInnerPic) {
				return bitmap;
			}
		}

		Matrix matrix = new Matrix();
		int curColunm = 0;
		int row = 0;

		final int childCount = folderInfo.getChildCount();
		final int count = Math.min(INNER_ICON_SIZE, childCount);
		for (int i = 0; i < count; i++) {
			ShortCutInfo item = folderInfo.getChildInfo(i);
			if (item != null && item.mIcon != null && item.mIcon instanceof BitmapDrawable) {
				Drawable innerIcon = item.mIcon;
				if (innerIcon != null && innerIcon instanceof BitmapDrawable) {
					Bitmap innerBitmap = ((BitmapDrawable) innerIcon).getBitmap();
					if (innerBitmap == null || innerBitmap.isRecycled()) {
						continue;
					}

					final float first = iconSize * 0.12f;
					final float grap = iconSize * 0.015f;
					final int innerIconSize = (int) (iconSize - first * 2 - grap * 2) / 2;
					final float left = first + curColunm * (innerIconSize + grap * 2);
					final float top = first + row * (innerIconSize + grap * 2);
					final float scaleX = (float) innerIconSize / (float) innerBitmap.getWidth();
					final float scaleY = (float) innerIconSize / (float) innerBitmap.getHeight();

					matrix.reset();
					matrix.postScale(scaleX, scaleY);
					matrix.postTranslate(left, top);
					canvas.drawBitmap(innerBitmap, matrix, paint);

					curColunm++;
					if (curColunm >= INNER_ICON_COLS) {
						curColunm = 0;
						row++;
					}
				}// end if inner
			}
		}
		return bitmap;
	}

	public synchronized static void prepareIcon(FolderIcon folderIcon, UserFolderInfo folderInfo) {
		if (folderInfo == null || folderIcon == null) {
			return;
		}

		final Context context = folderIcon.getContext();
		final Resources resources = context.getResources();

		// 文件夹样式
		FolderStyle folderStyle = null;
		DeskThemeControler themeControler = null;

		// 清空底图
		folderIcon.mRawIcon = null;
		// 获取图标类型
		int type = folderInfo.getmFeatureIconType();
		// 图标的主题包
		String packageName = folderInfo.getmFeatureIconPackage();
		// 判断改主题是否有安装
		boolean isInstall = AppUtils.isAppExist(context, packageName);
		// GO主题类型
		ImageExplorer imageExplorer = ImageExplorer.getInstance(GOLauncherApp.getContext());

		if ((type == ImagePreviewResultType.TYPE_PACKAGE_RESOURCE || type == ImagePreviewResultType.TYPE_APP_ICON)
				&& isInstall) {
			// 如果使用的是默认主题的GO样式
		    applyThemeIcon(folderIcon, folderInfo);
		}
		// 自定义类型
		else if (type == ImagePreviewResultType.TYPE_IMAGE_FILE) {
			folderIcon.mRawIcon = folderInfo.mIcon;
		} else {
			themeControler = AppCore.getInstance().getDeskThemeControler();
			if (themeControler != null) {
				DeskThemeBean themeBean = themeControler.getDeskThemeBean();
				if (themeBean != null && themeBean.mScreen != null) {
					folderStyle = themeBean.mScreen.mFolderStyle;
				}
			}
			if (folderStyle != null && folderStyle.mBackground != null) {
				folderIcon.mRawIcon = imageExplorer.getDrawable(folderStyle.mPackageName,
						folderStyle.mBackground.mResName);
			}
		}
		
		// 读取默认主题图标
		if (folderIcon.mRawIcon == null) {
			// 失败就没有默认图了
			// 像这种默认图，是否应该全局缓存，应对异常时处理？
			try {
				folderIcon.mRawIcon = imageExplorer.getDefaultDrawable(R.drawable.folder_back);
				if (null == folderIcon.mRawIcon) {
					folderIcon.mRawIcon = resources.getDrawable(R.drawable.folder_back);
				}
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
				return;
			} catch (Exception e) {
				Log.i("FolderIcon", "prepareIcon() has exception " + e.getMessage());
				return;
			}

			folderInfo.mIcon = folderIcon.mRawIcon;
		}

		BitmapDrawable folderBottom = (BitmapDrawable) folderIcon.mRawIcon;
		if (folderBottom == null) {
			return;
		}

		Bitmap bitmap = null;
		Canvas canvas = null;
		Paint paint = null;

		// 如果不是用户自定义图标返回true
		boolean drawInnerPic = false;
		if (type != ImagePreviewResultType.TYPE_IMAGE_FILE
				&& type != ImagePreviewResultType.TYPE_APP_ICON) {
			drawInnerPic = true;
		}
		
		bitmap = combinFolderIconBitmap(context, folderIcon, folderInfo, drawInnerPic);

		if (bitmap == null) {
			return;
		}
		
		try {
            canvas = new Canvas(bitmap);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        paint = new Paint();
        paint.setDither(false);
        paint.setFilterBitmap(true);

        if (bitmap != null && !bitmap.isRecycled()) {
            // 画合成图
            canvas.drawBitmap(bitmap, 0, 0, paint);
        }

		
		Bitmap openBitmap = null;
		try {
			openBitmap = Bitmap.createBitmap(bitmap);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		}

		compositeCloseBitmap(context, drawInnerPic, folderIcon, folderInfo, canvas, paint);
		folderIcon.mCloseIcon = new BitmapDrawable(resources, bitmap);

        compositeOpenBitmap(context, openBitmap, folderIcon, folderInfo, canvas, paint);
		folderIcon.setIcon(folderIcon.mCloseIcon);
	}
	
	/**
	 * <br>功能简述:
	 * @param folderIcon
	 * @param folderInfo
	 */
    private static void applyThemeIcon(FolderIcon folderIcon, UserFolderInfo folderInfo) {
        if (folderIcon == null || folderInfo == null) {
            return;
        }
        
        String packageName = folderInfo.getmFeatureIconPackage();
        int type = folderInfo.getmFeatureIconType();
        // 如果使用的是默认主题的GO样式
        if (packageName.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
            
            ImageExplorer imageExplorer = ImageExplorer.getInstance(GOLauncherApp.getContext());
            // 文件夹自定义了app的图标
            if (type == ImagePreviewResultType.TYPE_APP_ICON) {
                folderIcon.mRawIcon =
                    imageExplorer.getDrawable(folderInfo.mFeatureIconPackage, folderInfo.mFeatureIconPath);
            }
            else {
                // UI3.0的文件夹背景图片名称换了，需要自动获取，如果没有就选择默认文件夹图标
                String mResName = null;
                if (folderIcon.getInfo() != null) {
                    mResName = folderIcon.getInfo().mFeatureIconPath;
                }
                if (mResName == null || mResName.equals("")) {
                    mResName = DEFAULT_RAWICON_RES;
                }
                folderIcon.mRawIcon = imageExplorer.getDrawable(packageName, mResName);
            }
            folderIcon.mCloseIcon = imageExplorer.getDrawable(packageName, DEFAULT_CLOSE_RES);
            folderIcon.mOpenIcon = imageExplorer.getDrawable(packageName, DEFAULT_OPEN_RES);
        }
        else {
            if (type == ImagePreviewResultType.TYPE_APP_ICON) {
                // add by Ryan 2012.07.28
                applyTheme(packageName, folderIcon, folderInfo);
            }
            else {
                // 设置3个图标
                applyTheme(packageName, folderIcon);
            }
        }
    }
	
    /**
     * <br>功能简述:
     * @param context
     * @param folderIcon
     * @param folderInfo
     * @param needDrawInnerPic 需不需要画文件夹里面的缩略图
     * @return
     */
    private static Bitmap combinFolderIconBitmap(Context context, FolderIcon folderIcon, UserFolderInfo folderInfo,
        boolean needDrawInnerPic) {
        // 如果不是用户自定义图标返回true
        BitmapDrawable folderBottom = (BitmapDrawable) folderIcon.mRawIcon;
        // 获取桌面图标大小
        final int size = Utilities.getStandardIconSize(context);
        // 获取文件夹底图与预览图的合成图
        Bitmap newBitmap = combinDraw(folderBottom, folderInfo, size, needDrawInnerPic);
        
        Bitmap bitmap = null;
        if (newBitmap != null) {
            if (newBitmap.isMutable()) {
                bitmap = newBitmap;
            }
            else {
                try {
                    // 防止出现Immutable bitmap passed to Canvas constructor错误,所以用copy
                    bitmap = Bitmap.createBitmap(newBitmap.copy(Bitmap.Config.ARGB_8888, true));
                }
                catch (OutOfMemoryError e) {
                    OutOfMemoryHandler.handle();
                }
                // 将newBitmap置空，以便系统加快回收资源
                if (newBitmap != null) {
                    newBitmap.recycle();
                    newBitmap = null;
                }
            }
        }
        
        return bitmap;
    }
    
	/**
	 * <br>功能简述:合成带罩子的文件夹图片
	 * @param context
	 * @param drawInnerPic 是否需要话文件夹内的缩略图
	 * @param folderIcon
	 * @param folderInfo
	 * @param canvas
	 * @param paint
	 * @return
	 */
    public static Bitmap compositeCloseBitmap(Context context, boolean drawInnerPic, FolderIcon folderIcon,
        UserFolderInfo folderInfo, Canvas canvas, Paint paint) {
        // 关闭图标的设置
        BitmapDrawable closeTop = null;
        ImageExplorer imageExplorer = ImageExplorer.getInstance(GOLauncherApp.getContext());
        int type = folderInfo.getmFeatureIconType();
        FolderStyle folderStyle = setFolderStyle(type);
        boolean isInstall = AppUtils.isAppExist(context, folderInfo.getmFeatureIconPackage());
        final Resources resources = context.getResources();
        if (drawInnerPic) // 如果不是自定义图片才画罩子
        {
            // 如果定义的是GO主题样式
            if (type == ImagePreviewResultType.TYPE_PACKAGE_RESOURCE && isInstall) {
                if (folderIcon.mCloseIcon != null
                        && folderIcon.mCloseIcon instanceof BitmapDrawable) {
                    closeTop = (BitmapDrawable) folderIcon.mCloseIcon;
                }
            } else {
                if (folderStyle != null && folderStyle.mClosedFolder != null) {
                    Drawable tempDrawable = imageExplorer.getDrawable(folderStyle.mPackageName,
                            folderStyle.mClosedFolder.mResName);
                    if (tempDrawable != null && tempDrawable instanceof BitmapDrawable) {
                        closeTop = (BitmapDrawable) tempDrawable;
                    }
                }
            }

            if (closeTop == null) {
                try {
                    closeTop = (BitmapDrawable) imageExplorer
                            .getDefaultDrawable(R.drawable.folder_top);
                    if (null == closeTop) {
                        closeTop = (BitmapDrawable) resources.getDrawable(R.drawable.folder_top);
                    }
                } catch (OutOfMemoryError e) {
                    OutOfMemoryHandler.handle();
                }
            }
        }// end getCloseTop

        Bitmap tempBitmap = null;
        if (closeTop != null) {
            tempBitmap = closeTop.getBitmap();
            if (tempBitmap != null && !tempBitmap.isRecycled()) {
                Matrix matrix = new Matrix();
                final float scale = (Utilities.getStandardIconSize(context) + 0.1f)
                        / tempBitmap.getWidth();
                matrix.setScale(scale, scale);
                canvas.drawBitmap(tempBitmap, matrix, paint);
            }
        }
        return tempBitmap;
	}
    
    /**
     * <br>功能简述:合成不带罩子的文件夹图片
     * @param context
     * @param openBitmap 
     * @param folderIcon
     * @param folderInfo
     * @param canvas
     * @param paint
     * @return
     */
    public static Bitmap compositeOpenBitmap(Context context, Bitmap openBitmap, FolderIcon folderIcon,
        UserFolderInfo folderInfo, Canvas canvas, Paint paint) {
        // 打开图标的设置
        if (openBitmap != null && !openBitmap.isRecycled()) {
            canvas.setBitmap(openBitmap);
            BitmapDrawable openTop = null;
            final Resources resources = context.getResources();
            ImageExplorer imageExplorer = ImageExplorer.getInstance(GOLauncherApp.getContext());
            // 如果定义的是GO主题样式
            int type = folderInfo.getmFeatureIconType();
            FolderStyle folderStyle = setFolderStyle(type);
            boolean isInstall = AppUtils.isAppExist(context, folderInfo.getmFeatureIconPackage());
            if ((type == ImagePreviewResultType.TYPE_PACKAGE_RESOURCE || type == ImagePreviewResultType.TYPE_APP_ICON)
                    && isInstall) {
                if (folderIcon.mOpenIcon != null && folderIcon.mOpenIcon instanceof BitmapDrawable) {
                    openTop = (BitmapDrawable) folderIcon.mOpenIcon;
                }
            } else {
                if (folderStyle != null && folderStyle.mOpendFolder != null) {
                    Drawable tempDrawable = imageExplorer.getDrawable(folderStyle.mPackageName,
                            folderStyle.mOpendFolder.mResName);
                    if (tempDrawable != null && tempDrawable instanceof BitmapDrawable) {
                        openTop = (BitmapDrawable) tempDrawable;
                    }
                }
            }

            if (openTop == null) {
                openTop = (BitmapDrawable) imageExplorer
                        .getDefaultDrawable(R.drawable.folder_open_top);
                if (null == openTop) {
                    openTop = (BitmapDrawable) resources.getDrawable(R.drawable.folder_open_top);
                }
            }

            if (openTop != null) {
                final Bitmap tempBitmap = openTop.getBitmap();
                if (tempBitmap != null && !tempBitmap.isRecycled()) {
                    // 获取桌面图标大小
                    final int size = Utilities.getStandardIconSize(context);
                    final float scale = size / (tempBitmap.getWidth() + 0.1f);
                    // canvas.drawBitmap(tempBitmap, 0, 0, paint);
                    Matrix matrix = new Matrix();
                    if (scale != 1.0f) {
                        matrix.setScale(scale, scale);
                    }
                    canvas.drawBitmap(tempBitmap, matrix, paint);
                }
            }
            folderIcon.mOpenIcon = new BitmapDrawable(resources, openBitmap);
        }
        return openBitmap;
    }
    
    private static FolderStyle setFolderStyle(int type) {
        FolderStyle folderStyle = null;
        if (type != ImagePreviewResultType.TYPE_PACKAGE_RESOURCE && type != ImagePreviewResultType.TYPE_APP_ICON
            && type != ImagePreviewResultType.TYPE_IMAGE_FILE) {
            DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
            DeskThemeBean themeBean = themeControler.getDeskThemeBean();
            if (themeBean != null && themeBean.mScreen != null) {
                folderStyle = themeBean.mScreen.mFolderStyle;
            }
        }
        return folderStyle;
    }

	@SuppressWarnings({ "rawtypes" })
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case AppItemInfo.INCONCHANGE : {
				post(new Runnable() {
					@Override
					public void run() {
						prepareIcon(FolderIcon.this, getInfo());
					}
				});
				break;
			}

			default :
				break;
		}
	}

	private boolean willAcceptItem(ItemInfo item) {
		if (item == mInfo) {
			return false;
		}
		final int itemType = item.mItemType;
		boolean isAccept = itemType == IItemType.ITEM_TYPE_APPLICATION
				|| itemType == IItemType.ITEM_TYPE_SHORTCUT
				|| itemType == IItemType.ITEM_TYPE_USER_FOLDER;
		return isAccept;
	}

	public void onDragEnter(Object dragInfo) {
		if (!willAcceptItem((ItemInfo) dragInfo)) {
			return;
		}
		open();
	}

	public void onDragExit() {
		close();
	}

	private static final int CONSUMPTION_ANIMATION_DURATION = 100;

	// The degree to which the inner ring grows when accepting drop
	private static final float INNER_RING_GROWTH_FACTOR = 0.15f;

	// The degree to which the outer ring is scaled in its natural state
	private static final float OUTER_RING_GROWTH_FACTOR = 0.35f;

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  
	 * @date  [2012-10-18]
	 */
	public static class FolderRingAnimator {
		public int mCellX;
		public int mCellY;
		private CellLayout mCellLayout;
		public float mOuterRingSize;
		public float mInnerRingSize;
		public FolderIcon mFolderIcon = null;
		public static Drawable sSharedInnerRingDrawable = null;
		public static int sPreviewSize = -1;
		public static int sPreviewPadding = -1;

		private ValueAnimator mAcceptAnimator;
		private ValueAnimator mNeutralAnimator;

		public FolderRingAnimator(Context context, FolderIcon folderIcon) {
			mFolderIcon = folderIcon;
			sPreviewSize = Utilities.getStandardIconSize(context);
		}

		public void setFolderBg(Context context) {
			try {
				// 获取当前主题包名
				String curPackageName = ThemeManager.getInstance(context).getCurThemePackage();
				DeskThemeControler themeControler = AppCore.getInstance().getDeskThemeControler();
				if (themeControler != null && curPackageName != null) {
					DeskThemeBean themeBean = themeControler.getDeskThemeBean();
					if (themeBean != null) {
						FolderStyle folderStyle = themeBean.mScreen.mFolderStyle;
						if (folderStyle != null) {
							sSharedInnerRingDrawable = ImageExplorer.getInstance(context)
									.getDrawable(curPackageName, folderStyle.mBackground.mResName);
						}
					}
				}
				if (sSharedInnerRingDrawable == null) {
					Resources res = context.getResources();
					sSharedInnerRingDrawable = res.getDrawable(R.drawable.folder_back);
				}
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
			}
		}

		public void animateToAcceptState() {
			if (mNeutralAnimator != null) {
				mNeutralAnimator.cancel();
			}
			mAcceptAnimator = ValueAnimator.ofFloat(0f, 1f);
			mAcceptAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

			final int previewSize = sPreviewSize;
			mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float percent = (Float) animation.getAnimatedValue();
					mOuterRingSize = (1 + percent * OUTER_RING_GROWTH_FACTOR) * previewSize;
					mInnerRingSize = (1 + percent * INNER_RING_GROWTH_FACTOR) * previewSize;
					if (mCellLayout != null) {
						mCellLayout.invalidate();
					}
				}
			});
			mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					if (mFolderIcon != null) {
						// mFolderIcon.mPreviewBackground.setVisibility(INVISIBLE);
						mFolderIcon.setVisibility(INVISIBLE);
					}
				}
			});
			mAcceptAnimator.start();
		}

		public void animateToNaturalState() {
			if (mAcceptAnimator != null) {
				mAcceptAnimator.cancel();
			}
			mNeutralAnimator = ValueAnimator.ofFloat(0f, 1f);
			mNeutralAnimator.setDuration(CONSUMPTION_ANIMATION_DURATION);

			final int previewSize = sPreviewSize;
			mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float percent = (Float) animation.getAnimatedValue();
					mOuterRingSize = (1 + (1 - percent) * OUTER_RING_GROWTH_FACTOR) * previewSize;
					mInnerRingSize = (1 + (1 - percent) * INNER_RING_GROWTH_FACTOR) * previewSize;
					if (mCellLayout != null) {
						mCellLayout.invalidate();
					}
				}
			});
			mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if (mCellLayout != null) {
						mCellLayout.hideFolderAccept(FolderRingAnimator.this);
					}
					if (mFolderIcon != null) {
						mFolderIcon.setVisibility(VISIBLE);
					}
				}
			});
			mNeutralAnimator.start();
		}

		// Location is expressed in window coordinates
		public void setCell(int x, int y) {
			mCellX = x;
			mCellY = y;
		}

		public void setCellLayout(CellLayout layout) {
			mCellLayout = layout;
		}

		public float getOuterRingSize() {
			return mOuterRingSize;
		}
	}
}