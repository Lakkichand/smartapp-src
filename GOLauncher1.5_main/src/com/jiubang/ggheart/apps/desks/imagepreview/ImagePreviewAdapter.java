package com.jiubang.ggheart.apps.desks.imagepreview;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.MyThread;

public class ImagePreviewAdapter extends BaseAdapter {
	private Object mMutex;

	private ArrayList<ImageView> mImageViews;

	private ArrayList<Drawable> mDrawables;

	private ConcurrentHashMap<Integer, Integer> mPositionMap;

	/**
	 * 当前更新到图片索引
	 */
	private int mCurrentedUpdateViewIndex = 0;

	/**
	 * 一共doUpdateUI了几次，用于判断最后一次update后通知UI notifyDataInvalidate
	 */
	private int mTotalUpdateUICount = 0;

	public interface IImageNode {
		public void setDrawable(Drawable drawable);

		public Drawable getDrawable();

		public void loadDrawable();

		public void freeDrawable();
	}

	public class NoNeedFreeImageNode implements IImageNode {
		protected Drawable mDrawable;

		public NoNeedFreeImageNode() {

		}

		@Override
		public void setDrawable(Drawable drawable) {
			mDrawable = drawable;
		}

		@Override
		public Drawable getDrawable() {
			return mDrawable;
		}

		@Override
		public void loadDrawable() {

		}

		@Override
		public void freeDrawable() {
			mDrawable = null;
		}
	}

	public class NeedFreeImageNode extends NoNeedFreeImageNode {
		public NeedFreeImageNode() {

		}

		@Override
		public void freeDrawable() {
			if (null != mDrawable) {
				BitmapDrawable bmpDrawable = (BitmapDrawable) mDrawable;
				bmpDrawable.getBitmap().recycle();
				// bmpDrawable.setCallback(null);
				mDrawable = null;
			}
		}
	}

	public class FileImageNode extends NeedFreeImageNode {
		private String mFilePath;

		public FileImageNode(String path) {
			mFilePath = path;
		}

		public String getFilePath() {
			return mFilePath;
		}

		@Override
		public void loadDrawable() {
			try {
				mDrawable = Drawable.createFromPath(mFilePath);

				if (null != mDrawable) {
					mDrawables.add(mDrawable);
					int indexInmImageNodeList = mImageNodeList.indexOf(this);
					int indexInmDrawables = mDrawables.size() - 1;
					mPositionMap.put(indexInmDrawables, indexInmImageNodeList);
				}
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
			} catch (Exception e) {

			}
		}
	}

	public class PackageImageNode extends NoNeedFreeImageNode {
		private String mPackageName;
		private String mPackageResName;

		public PackageImageNode(String packageName, String packageResName) {
			mPackageName = packageName;
			mPackageResName = packageResName;
		}

		public String getPackageName() {
			return mPackageName;
		}

		public String getPackageResName() {
			return mPackageResName;
		}

		@Override
		public void loadDrawable() {
			mDrawable = mImageExplorer.getDrawable(mPackageName, mPackageResName);

			if (null != mDrawable) {
				mDrawables.add(mDrawable);
				int indexInmImageNodeList = mImageNodeList.indexOf(this);
				int indexInmDrawables = mDrawables.size() - 1;
				mPositionMap.put(indexInmDrawables, indexInmImageNodeList);
			}
		}
	}

	public class ResourceImageNode extends NoNeedFreeImageNode {
		private String mResourceName;

		public ResourceImageNode(String resourceName) {
			mResourceName = resourceName;
		}

		public String getResourceName() {
			return mResourceName;
		}

		@Override
		public void loadDrawable() {
			try {
				ImageExplorer imageExplorer = ImageExplorer.getInstance(mContext);
				mDrawable = imageExplorer.getDrawable(ThemeManager.DEFAULT_THEME_PACKAGE,
						mResourceName);

				if (null != mDrawable) {
					mDrawables.add(mDrawable);
					int indexInmImageNodeList = mImageNodeList.indexOf(this);
					int indexInmDrawables = mDrawables.size() - 1;
					mPositionMap.put(indexInmDrawables, indexInmImageNodeList);
				}
			} catch (OutOfMemoryError e) {
				OutOfMemoryHandler.handle();
			} catch (Throwable e) {

			}
		}
	}

	public class DrawableImageNode extends NoNeedFreeImageNode {
		public DrawableImageNode(Drawable drawable) {
			mDrawable = drawable;
		}
	}

	protected Context mContext;
	protected ImageGridParam mItemParam;
	protected List<IImageNode> mImageNodeList;

	private ImageExplorer mImageExplorer;

	public ImagePreviewAdapter(Context context, ImageGridParam param) {
		mContext = context;
		mItemParam = param;
		mMutex = new Object();
		mImageNodeList = new ArrayList<IImageNode>();

		mImageExplorer = ImageExplorer.getInstance(context);

		mImageViews = new ArrayList<ImageView>();
		mDrawables = new ArrayList<Drawable>();
		mPositionMap = new ConcurrentHashMap<Integer, Integer>();
	}

	public void initDrawable(Drawable drawable) {
		if (null != drawable) {
			initImageNode(mImageNodeList, drawable);
		}
	}

	public void initResourceStringArray(String[] resourceStringArray) {
		if (null != resourceStringArray) {
			initImageNode(mImageNodeList, resourceStringArray);
		}
	}

	public void initPackageResourceArray(String packageName, ArrayList<String> packageResourceArray) {
		if (null != packageName && null != packageResourceArray) {
			initImageNode(mImageNodeList, packageName, packageResourceArray);
		}
	}

	public void initFolder(String scanFolder) {
		if (null != scanFolder) {
			File file = new File(scanFolder);
			if (file.exists()) {
				initImageNode(mImageNodeList, file);
			}
		}
	}

	public void initPackageResourceArrayInAllPacksges(ArrayList<String> packageNames,
			ArrayList<String> resName) {
		if (null != packageNames && null != resName) {
			initImageNode(mImageNodeList, packageNames, resName);
		}
	}

	private MyThread mLoadThread;

	public void start() {
		mLoadThread = new MyThread() {
			@Override
			protected void doBackground() {
				synchronized (mMutex) {
					if (null != mImageNodeList) {
						int sz = mImageNodeList.size();
						for (int i = 0; i < sz; i++) {
							if (!getRunFlag()) {
								break;
							}

							IImageNode node = mImageNodeList.get(i);
							if (null == node) {
								continue;
							}
							node.loadDrawable();

							updateUI(node);
						}
					}
				}
			}

			@Override
			protected void doUpdateUI(Object obj) {
				if (mDrawables != null && mCurrentedUpdateViewIndex < mDrawables.size()) {
					Drawable drawable = mDrawables.get(mCurrentedUpdateViewIndex);
					if (mCurrentedUpdateViewIndex < mImageViews.size()) {
						mImageViews.get(mCurrentedUpdateViewIndex).setImageDrawable(drawable);
						Log.i("jiang", "doUpdateUI index=" + mCurrentedUpdateViewIndex);
						if (null == drawable) {
							Log.i("jiang", "doUpdateUI drawable=null index="
									+ mCurrentedUpdateViewIndex);
						}
					}

					mCurrentedUpdateViewIndex++;
					if (mTotalUpdateUICount < 100 && mCurrentedUpdateViewIndex == mDrawables.size()) {
						notifyDataSetInvalidated();
					}
				}
				mTotalUpdateUICount++;
				if (null != mImageNodeList && mTotalUpdateUICount == mImageNodeList.size()) {
					notifyDataSetInvalidated();
				}
			}
		};
		mLoadThread.start();
	}

	public void cancel() {
		if (null != mLoadThread) {
			mLoadThread.setRunFlag(false);
			mLoadThread = null;
		}
	}

	public void freePictures() {
		synchronized (mMutex) {
			if (null != mImageNodeList) {
				int sz = mImageNodeList.size();
				for (int i = 0; i < sz; i++) {
					IImageNode node = mImageNodeList.get(i);
					if (null != node) {
						node.freeDrawable();
					}
				}
				System.gc();
				mImageNodeList.clear();
			}
			if (null != mImageViews) {
				mImageViews.clear();
			}
			if (null != mDrawables) {
				mDrawables.clear();
			}
			if (null != mPositionMap) {
				mPositionMap.clear();
			}
			mCurrentedUpdateViewIndex = 0;
			mTotalUpdateUICount = 0;
		}
	}

	public void free() {
		freePictures();
		if (null != mImageNodeList) {
			mImageNodeList.clear();
			mImageNodeList = null;
		}
		if (null != mImageViews) {
			mImageViews.clear();
			mImageViews = null;
		}
		if (null != mDrawables) {
			mDrawables.clear();
			mDrawables = null;
		}
		if (null != mPositionMap) {
			mPositionMap.clear();
			mPositionMap = null;
		}
	}

	private void initImageNode(List<IImageNode> imageNodeList, Drawable drawable) {
		DrawableImageNode node = new DrawableImageNode(drawable);
		if (null == imageNodeList) {
			imageNodeList = new ArrayList<IImageNode>();
		}
		imageNodeList.add(node);

		if (null != drawable) {
			mDrawables.add(drawable);
			int indexInmImageNodeList = mImageNodeList.indexOf(node);
			int indexInmDrawables = mDrawables.size() - 1;
			mPositionMap.put(indexInmDrawables, indexInmImageNodeList);
		}
	}

	private void initImageNode(List<IImageNode> imageNodeList, String[] resourceIdArray) {
		int len = resourceIdArray.length;
		for (int i = 0; i < len; i++) {
			ResourceImageNode node = new ResourceImageNode(resourceIdArray[i]);
			if (null == imageNodeList) {
				imageNodeList = new ArrayList<IImageNode>();
			}
			imageNodeList.add(node);
		}
	}

	private void initImageNode(List<IImageNode> imageNodeList, String packageName,
			ArrayList<String> packageResourceArray) {
		int sz = packageResourceArray.size();
		for (int i = 0; i < sz; i++) {
			PackageImageNode node = new PackageImageNode(packageName, packageResourceArray.get(i));
			if (null == imageNodeList) {
				imageNodeList = new ArrayList<IImageNode>();
			}
			imageNodeList.add(node);
		}
	}

	/**
	 * 
	 * @param imageNodeList
	 * @param packageNames
	 *            所有的包名
	 * @param resName
	 *            要获取的图片的名字
	 */
	private void initImageNode(List<IImageNode> imageNodeList, ArrayList<String> packageNames,
			ArrayList<String> resNameList) {
		int count = packageNames.size();
		for (int i = 0; i < count; i++) {
			PackageImageNode node = new PackageImageNode(packageNames.get(i), resNameList.get(i));
			if (null == imageNodeList) {
				imageNodeList = new ArrayList<IImageNode>();
			}
			imageNodeList.add(node);
		}
	}

	private void initImageNode(List<IImageNode> imageNodeList, File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				initImageNode(imageNodeList, files[i]);
			}
		} else {
			if (isImageFile(file)) {
				FileImageNode node = new FileImageNode(file.getPath());
				if (null == imageNodeList) {
					imageNodeList = new ArrayList<IImageNode>();
				}
				imageNodeList.add(node);
			}
		}
	}

	private boolean isImageFile(File file) {
		return true;
	}

	@Override
	public int getCount() {
		if (mImageNodeList == null) {
			return 0;
		}

		return mImageNodeList.size();
	}

	@Override
	public Object getItem(int position) {
		return mImageNodeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public int getNodeIdFromPosition(int position) {
		return mPositionMap.get(position);
	}

	public int getmDrawablesSize() {
		return mDrawables.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = null;
		if (null == convertView) {
			imageView = new ImageView(mContext);
			if (null != mItemParam) {
				imageView.setLayoutParams(new GridView.LayoutParams(mItemParam.mWidth,
						mItemParam.mHeight));
				imageView.setPadding(mItemParam.mLeftPadding, mItemParam.mTopPadding,
						mItemParam.mRightPadding, mItemParam.mBottomPadding);
			}
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		} else {
			imageView = (ImageView) convertView;
		}

		if (position < mDrawables.size()) {
			Drawable mDrawable = mDrawables.get(position);
			imageView.setImageDrawable(mDrawable);
		} else {
			imageView.setImageDrawable(null);
		}
		if (!(position < mImageViews.size())) {
			mImageViews.add(imageView);
		}

		return imageView;
	}
}
