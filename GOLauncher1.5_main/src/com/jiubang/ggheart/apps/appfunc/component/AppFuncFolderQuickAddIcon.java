package com.jiubang.ggheart.apps.appfunc.component;

import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncMainView;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncModifyFolderActivity;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.appfunc.handler.AppFuncHandler;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.FunItemInfo;
	
/**
 * 文件夹快捷添加栏内图标基类
 * @author yangguanxiang
 *
 */
public class AppFuncFolderQuickAddIcon extends ApplicationIcon {

	private static final String FOLDER_BG_PRIMARY_KEY = "app_func_folder_quick_add_icon_bg_key";
	
	public AppFuncFolderQuickAddIcon(Activity activity, int tickCount, int x, int y, int width,
			int height, FunItemInfo info, BitmapDrawable appPic, BitmapDrawable editPic,
			BitmapDrawable editLightPic, String title, boolean isDrawText) {
		super(activity, tickCount, x, y, width, height, info, appPic, editPic, editLightPic, title,
				isDrawText);
	}

	@Override
	protected void registerHandler() {
		// do nothing
	}

	protected void setAppItemInfoListener() {
		// do nothing
	}

	@Override
	protected void setFunFolderItemInfoListener() {
		// do nothing
	}

	@Override
	public void unRegister() {
		if (mImageList != null) {
			for (FolderItem item : mImageList) {
				item.clearItemInfo();
			}
		}
		mFolderBg = null;
	}

	@Override
	protected void setFontSize() {
		mTextHeight = mUtils.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_text_size);
	}

	@Override
	protected void setIconTextDst() {
		mIconTextDst = mUtils.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_icon_text_dst);
	}

	@Override
	protected void initIconSize() {
		if (mAppPic != null) {
			mIconHeight = mUtils
					.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_padding_top)
					+ mAppPic.getHeight() + mIconTextDst;
			mIconWidth = (int) ((mAppPic.getWidth() * (1 + 2 * 0.194)));
		} else {
			mIconHeight = mUtils
					.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_padding_top)
					+ mIntrinSize + mIconTextDst;
			mIconWidth = (int) (mIntrinSize * (1 + 2 * 0.194));
		}
	}
	@Override
	protected void constructAppIcon() {
		int startX = (mWidth - mIconWidth) / 2;
		int startY = mMargin_v;
		if (mIconImage == null) {
			mIconImage = new FolderIconImage(mTickCount, startX, startY, mIconWidth, mIconHeight);
		} else {
			mIconImage.setXY(startX, startY);
			mIconImage.setSize(mIconWidth, mIconHeight);
		}
	}

	@Override
	protected int getTextTop() {
		return mUtils.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_padding_top)
				+ mIntrinSize + mIconTextDst;
	}

	@Override
	protected void loadResource() {
		//		if(!mIsResLoaded){
		reloadFolderResource();
		//		}
		if (AppFuncFrame.sVisible) {
			readyData();
		}
	}

	@Override
	protected void reloadFolderResource() {
		Resources res = mContext.getResources();
		if (mInfo != null && mInfo instanceof FunFolderItemInfo) {
			int folderType = ((FunFolderItemInfo) mInfo).getFolderType();
			if (!sFolderBgMap.containsKey(FOLDER_BG_PRIMARY_KEY)) {
				ConcurrentHashMap<String, BitmapDrawable> map = new ConcurrentHashMap<String, BitmapDrawable>();
				if (folderType == FunFolderItemInfo.TYPE_NEW_FOLDER) {
					mFolderBg = (BitmapDrawable) res
							.getDrawable(R.drawable.appfunc_quick_add_new_folder_bg);
					if (mFolderBg != null) {
						map.put(Integer.toString(R.drawable.appfunc_quick_add_new_folder_bg),
								mFolderBg);
					}
				} else {
					mFolderBg = (BitmapDrawable) res
							.getDrawable(R.drawable.appfunc_quick_add_folder_bg);
					if (mFolderBg != null) {
						map.put(Integer.toString(R.drawable.appfunc_quick_add_folder_bg), mFolderBg);
					}
				}
				sFolderBgMap.put(FOLDER_BG_PRIMARY_KEY, map);
			} else {
				ConcurrentHashMap<String, BitmapDrawable> map = sFolderBgMap
						.get(FOLDER_BG_PRIMARY_KEY);
				if (folderType == FunFolderItemInfo.TYPE_NEW_FOLDER) {
					String key = Integer.toString(R.drawable.appfunc_quick_add_new_folder_bg);
					if (map.containsKey(key)) {
						mFolderBg = map.get(key);
					} else {
						mFolderBg = (BitmapDrawable) res
								.getDrawable(R.drawable.appfunc_quick_add_new_folder_bg);
						if (mFolderBg != null) {
							map.put(Integer.toString(R.drawable.appfunc_quick_add_new_folder_bg),
									mFolderBg);
						}
					}
				} else {
					String key = Integer.toString(R.drawable.appfunc_quick_add_folder_bg);
					if (map.containsKey(key)) {
						mFolderBg = map.get(key);
					} else {
						mFolderBg = (BitmapDrawable) res
								.getDrawable(R.drawable.appfunc_quick_add_folder_bg);
						if (mFolderBg != null) {
							map.put(Integer.toString(R.drawable.appfunc_quick_add_folder_bg),
									mFolderBg);
						}
					}
				}
			}
		}
		//		mIsResLoaded = true;
	}

	@Override
	protected void initIntrinSize() {
		mIntrinSize = mUtils.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_size);
	}

	//	protected int getFitInnerSize() {
	//		return (int) (mIntrinSize * 0.265f);
	//	}

	public PointF getNextFolderItemPoint() {

		PointF point = new PointF();
		boolean isAction = false;

		mIsInMid = true;
		int iConsize = getFitInnerSize();
		int width = mIntrinSize;
		int height = mIntrinSize;
		if (mAppPic != null) {
			width = mAppPic.getWidth();
			height = mAppPic.getHeight();
		}
		if (mImageList != null) {
			int colunm = 2;
			int curColunm = 0;
			int row = 0;

			for (FolderItem folderItem : mImageList) {

				if (folderItem.mItemInfo != null && folderItem.mBitmap != null) {
					curColunm++;
					if (curColunm >= colunm) {
						curColunm = 0;
						row++;
					}
				} else {
					isAction = true;
					mIsInMid = false;
					point.x = (width - iConsize * 2 - width * 0.03f) / 2 + curColunm
							* (iConsize + width * 0.03f) + width * 0.194f + mIconImage.mX;
					point.y = height * 0.12f + row * (iConsize + width * 0.03f) + mIconImage.mY
							+ ((FolderIconImage) mIconImage).mTopPadding;
				}
			}
		}

		if (!isAction) {
			point.x = (width - iConsize) / 2 + width * 0.194f + mIconImage.mX;;
			point.y = (height - iConsize) / 2 + mIconImage.mX
					+ ((FolderIconImage) mIconImage).mTopPadding;
		}

		return point;
	}

	/**
	 * 特殊文件夹封装类
	 * @author yangguanxiang
	 *
	 */
	public static class SpecialFolderItem {
		public int mType;
		public FunFolderItemInfo mInfo;

		public SpecialFolderItem(int type, FunFolderItemInfo info) {
			mType = type;
			mInfo = info;
		}
	}

	/**
	 * 文件夹图标
	 * @author yangguanxiang
	 *
	 */
	private class FolderIconImage extends IconImage {
		public int mTopPadding;
		public FolderIconImage(int tickCount, int x, int y, int width, int height) {
			super(tickCount, x, y, width, height);
			mTopPadding = mUtils
					.getDimensionPixelSize(R.dimen.appfunc_quick_add_folder_padding_top);
		}

		@Override
		protected void drawCurrentFrame(Canvas canvas) {
			int picWidth = mIntrinSize;
			if (mAppPic != null) {
				picWidth = mAppPic.getWidth();
			}
			if (mAppPic != null) {
				if (mIsShrink) {
					mAppPic.draw(canvas, picWidth * 0.194f, picWidth * 0.194f);
				} else {
					mAppPic.draw(canvas, picWidth * 0.194f, mTopPadding);
				}
			}
		}
	}
	
	@Override
	public boolean onTouch(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN :
				setIsFolderReady(true);
				return true;
			case MotionEvent.ACTION_CANCEL :
				setIsFolderReady(false);
				break;
			case MotionEvent.ACTION_UP :
				setIsFolderReady(false);
				onTouchUp();
				break;
			default :
				break;
		}
		return super.onTouch(event);
	}

	private void onTouchUp() {
		if (mInfo instanceof FunFolderItemInfo && FunItemInfo.TYPE_FOLDER == mInfo.getType()) {
			FunFolderItemInfo folderInfo = (FunFolderItemInfo) mInfo;
			int folderType = folderInfo.getFolderType();
			XBaseGrid curGrid = AppFuncHandler.getInstance().getCurrentGrid();
			AppFuncMainView mainView = XViewFrame.getInstance().getAppFuncMainView();
			if (mainView.isReadyShowFolder() ||mainView.isMotion()) {
				return;
			}
			switch (folderType) {
				case FunFolderItemInfo.TYPE_NEW_FOLDER : {
//					curGrid.onCreateNewFolder(null);
					Intent newFolderIntent = new Intent(mActivity, AppFuncModifyFolderActivity.class);
					newFolderIntent.putExtra(AppFuncConstants.CREATEFOLDER, true);
					if (newFolderIntent != null) {
						mActivity.startActivity(newFolderIntent);
					}
				}
					break;
				case FunFolderItemInfo.TYPE_NORMAL :
					AppFuncFolder folder = AppFuncFolder.getInstance();
					if (folder != null) {
						FunFolderItemInfo oldInfo = folder.getFolderInfo();
						if (mainView.isFolderShow()) {
							if (oldInfo.getFolderId() == folderInfo.getFolderId()) {
								break;
							} else {
								// 有点扯淡的做法，但就先这样
								mainView.setIsCloseAndOpenFolder(true);
								mainView.setFolderToShow(folderInfo);
								AppFuncHandler.getInstance().hideFolder();
							}
						} else {
							mainView.scrollAndOpenFolder(folderInfo, curGrid);
						}
					} else {
						mainView.scrollAndOpenFolder(folderInfo, curGrid);
					}
					break;
				case FunFolderItemInfo.TYPE_GAME :
				case FunFolderItemInfo.TYPE_SOCIAL :
				case FunFolderItemInfo.TYPE_SYSTEM :
				case FunFolderItemInfo.TYPE_TOOL : {
//					curGrid.onCreateSpecialFolder(folderInfo);
					Intent newFolderIntent = new Intent(mActivity, AppFuncModifyFolderActivity.class);
					newFolderIntent.putExtra(AppFuncConstants.CREATEFOLDER, true);
					newFolderIntent.putExtra(AppFuncConstants.NEW_FOLDER_NAME, folderInfo.getTitle());
					newFolderIntent.putExtra(AppFuncConstants.NEW_FOLDER_TYPE, folderInfo.getFolderType());
					if (newFolderIntent != null) {
						mActivity.startActivity(newFolderIntent);
					}
				}
					break;
				default :
					break;
			
			}
		}
	}

}
