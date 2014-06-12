package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.TabDataGroup;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * UI2.0的标题栏，图标加文字线性排序，点击切换侧面tab栏
 * 
 * @author xiedezhi
 * 
 */
public class GridTitleBar extends LinearLayout {
	//TODO:XIEDEZHI 版本换图标时会不会有问题
	/**
	 * 图标缓存池，读图标时先判断缓存池是否已经有图标了
	 */
	Map<String, Bitmap> mIconCache = null;

	private LayoutInflater mInflater = null;
	/**
	 * item的布局属性
	 */
	private LinearLayout.LayoutParams mLayoutParams = null;

	private Context mContext = null;
	/**
	 * 当前选中的index
	 */
	private int mCurrentIndex = 0;
	/**
	 * 当前tab栏的数据列表，该对象与TabManageView的mTabDataGroup，
	 * TabDataManager的tabDataStack里的是同一个对象
	 */
	protected TabDataGroup mTabDataGroup = null;
	/**
	 * 当前显示的图标组件，点击切换侧面tab栏
	 */
	private List<View> mItems = null;
	/**
	 * 图片管理器
	 */
	private AsyncImageManager mImageManager = null;
	/**
	 * item的点击监听器
	 */
	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() == null || (!(v.getTag() instanceof Integer))) {
				return;
			}
			Integer tag = (Integer) v.getTag();
			if (tag != mCurrentIndex) {
				// 跳转到侧面的tab
				showSideTab(tag, -1);
			}
		}
	};
	
	/**
	 * 加载图标的线程池
	 */
	private ExecutorService mPool;

	public GridTitleBar(Context context) {
		super(context);
		init(context);
	}

	public GridTitleBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/**
	 * 初始化
	 */
	private void init(Context context) {
		mContext = context;
		mIconCache = new HashMap<String, Bitmap>();
		mInflater = LayoutInflater.from(context);
		this.setOrientation(HORIZONTAL);

		// item布局参数
		mLayoutParams = new LinearLayout.LayoutParams(0,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		mLayoutParams.weight = 1.0f;
		mImageManager = AsyncImageManager.getInstance();
		mItems = new ArrayList<View>();
	}

	/**
	 * 设置图标和文字
	 * 
	 * @param index
	 *            需要设置的图标的组件的下标
	 * @param isSelect
	 *            是否被选中
	 */
	private void setIcon(final int index, boolean isSelect) {
		if (index < 0 || index > mItems.size() - 1) {
			return;
		}
		View item = mItems.get(index);
		final ImageView image = (ImageView) item.findViewById(R.id.grid_titlebar_image);
		// 把选中的文字设为高亮，把未选中的文字设为普通颜色
		TextView text = (TextView) item.findViewById(R.id.grid_titlebar_text);
		if (isSelect) {
			item.setBackgroundDrawable(null);
			text.setTextColor(0xFF588500);
		} else {
			item.setBackgroundResource(R.drawable.appgame_tabbar_selector);
			text.setTextColor(0xFF626262);
		}
		CategoriesDataBean category = mTabDataGroup.categoryData.get(index);
		if (isSelect) {
			String cicon = category.cicon;
			if (cicon == null || cicon.equals("")) {
				// 设置默认图标
				image.setImageDrawable(getDefaultHitIcon(index));
				return;
			}
			// 去缓存找是否已经加载图标
			Bitmap bm = mIconCache.get(cicon);
			if (bm != null) {
				image.setImageBitmap(bm);
				return;
			} 
			loadImage(LauncherEnv.Path.APP_MANAGER_ICON_PATH, String.valueOf(cicon.hashCode()),
					cicon, new AsyncImageLoadedCallBack() {

						@Override
						public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
							// 保存图标
							mIconCache.put(imgUrl, imageBitmap);
							if (index == mCurrentIndex) {
								// 如果当前的item是为选中的item
								image.setImageBitmap(imageBitmap);
							}
						}
					});
			//找不到图标则用默认图标
			image.setImageDrawable(getDefaultHitIcon(index));
		} else {
			String icon = category.icon;
			if (icon == null || icon.equals("")) {
				// 设置默认图标
				image.setImageDrawable(getDefaultIcon(index));
				return;
			}
			// 去缓存找是否已经加载图标
			Bitmap bm = mIconCache.get(icon);
			if (bm != null) {
				image.setImageBitmap(bm);
				return;
			}
			loadImage(LauncherEnv.Path.APP_MANAGER_ICON_PATH, String.valueOf(icon.hashCode()),
					icon, new AsyncImageLoadedCallBack() {

						@Override
						public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
							// 保存图标
							mIconCache.put(imgUrl, imageBitmap);
							if (index != mCurrentIndex) {
								// 如果当前的item不是为选中的item
								image.setImageBitmap(imageBitmap);
							}
						}
					});
			//找不到图标则用默认图标
			image.setImageDrawable(getDefaultIcon(index));
		}
	}

	/**
	 * 根据分类数据初始化标题栏
	 * 
	 * @param group
	 *            新层级tab栏数据
	 * 
	 * @param targetSubIndex
	 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
	 */
	public void fillUp(TabDataGroup group, final int targetSubIndex) {
		if (group == null || !group.isIconTab || group.categoryData == null
				|| group.categoryData.size() == 0) {
			return;
		}
		this.removeAllViews();
		mTabDataGroup = group;
		mItems.clear();
		List<CategoriesDataBean> cList = group.categoryData;
		// 首页的下标
		int homeIndex = group.position;
		if (homeIndex < 0 || homeIndex > cList.size() - 1) {
			homeIndex = 0;
		}
		for (int i = 0; i < cList.size(); i++) {
			CategoriesDataBean category = cList.get(i);
//			if (category.feature == CategoriesDataBean.FEATURE_FOR_SEARCH) {
//				AppsManagementActivity.sendHandler(this,
//						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
//						IDiyMsgIds.SHOW_SEARCH_BUTTON, 1,
//						null, null);
//			}
			final RelativeLayout item = (RelativeLayout) mInflater.inflate(
					R.layout.apps_mgr_grid_titlebar_item, null);
			this.addView(item, mLayoutParams);
			mItems.add(item);
			// 下标作为每个item的tag
			item.setTag(i);
			if (i == homeIndex) {
				// 初始化分类图标
				setIcon(i, true);
				if (!TextUtils.isEmpty(category.icon)) {
					if (mIconCache.get(category.icon) == null) {
						final int index = i;
						// 加载分类未选中的图标
						loadImage(LauncherEnv.Path.APP_MANAGER_ICON_PATH,
								String.valueOf(category.icon.hashCode()), category.icon,
								new AsyncImageLoadedCallBack() {

									@Override
									public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
										mIconCache.put(imgUrl, imageBitmap);
										if (index != mCurrentIndex) {
											final ImageView image = (ImageView) item
													.findViewById(R.id.grid_titlebar_image);
											// 如果当前的item不是为选中的item
											image.setImageBitmap(imageBitmap);
										}
									}
								});
					}
				}
			} else {
				// 初始化分类图标
				setIcon(i, false);
				if (!TextUtils.isEmpty(category.cicon)) {
					if (mIconCache.get(category.cicon) == null) {
						final int index = i;
						// 加载分类选中的图标
						loadImage(LauncherEnv.Path.APP_MANAGER_ICON_PATH,
								String.valueOf(category.cicon.hashCode()), category.cicon,
								new AsyncImageLoadedCallBack() {

									@Override
									public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
										mIconCache.put(imgUrl, imageBitmap);
										if (index == mCurrentIndex) {
											final ImageView image = (ImageView) item
													.findViewById(R.id.grid_titlebar_image);
											// 如果当前的item不是为选中的item
											image.setImageBitmap(imageBitmap);
										}
									}
								});
					}
				}
			}
			// 初始化分类名
			TextView text = (TextView) item.findViewById(R.id.grid_titlebar_text);
			text.setText(category.name);
			// 为每个item加上监听器
			item.setTag(i);
			item.setOnClickListener(mItemClickListener);
		}
		showSideTab(homeIndex, targetSubIndex);
		// 展示可更新数字
		setUpdateCount(getUpdateCount());
		if (mPool != null && !mPool.isShutdown()) {
			mPool.shutdown();
			mPool = null;
		}
	}

	/**
	 * 在管理的图标上设置更新数字，只限于应用中心
	 * 
	 * @param count
	 *            更新数字
	 */
	public void setUpdateCount(int count) {
		if (mTabDataGroup == null || mTabDataGroup.categoryData == null) {
			return;
		}
		try {
			List<CategoriesDataBean> cList = mTabDataGroup.categoryData;
			for (int i = 0; i < cList.size(); i++) {
				CategoriesDataBean category = cList.get(i);
				if (category.feature == CategoriesDataBean.FEATURE_FOR_MANAGEMENT) {
					View item = mItems.get(i);
					TextView cTx = (TextView) item.findViewById(R.id.grid_titlebar_updatecount);
					if (cTx == null) {
						ViewStub stub = (ViewStub) item
								.findViewById(R.id.grid_titlebar_updatecount_stub);
						if (stub != null) {
							stub.inflate();
							cTx = (TextView) item.findViewById(R.id.grid_titlebar_updatecount);
						}
					}
					if (cTx != null) {
						cTx.setText(count + "");
						if (count > 0) {
							cTx.setVisibility(View.VISIBLE);
						} else {
							cTx.setVisibility(View.GONE);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从SharedPreferences里读取更新数字的个数
	 */
	public int getUpdateCount() {
		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		int mBeancount = preferences.getInt(FunControler.GOSTORECOUNT, 0);
		return mBeancount;
	}

	/**
	 * 展示侧面的tab
	 * 
	 * @param index
	 *            被选中的item的下标
	 * @param targetSubIndex
	 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
	 */
	public void showSideTab(int index, int targetSubIndex) {
		if (index < 0 || index > mTabDataGroup.categoryData.size() - 1) {
			return;
		}
		TabDataGroup subGroup = mTabDataGroup.subGroupList.get(index);
		if (subGroup == null) {
			// 把之前选中的图标设回未选中
			setIcon(mCurrentIndex, false);
			mCurrentIndex = index;
			// 更新titlebar界面
			setIcon(mCurrentIndex, true);
		}
		// 通知controller跳转到侧面tab
		TabController.skipToTheSideTab(mTabDataGroup, index, targetSubIndex);
	}

	/**
	 * 更改图标选择状态
	 * 
	 * @param index
	 *            被选中的图标
	 */
	public void setSelection(int index) {
		if (mItems == null || index < 0 || index >= mItems.size() || mCurrentIndex == index) {
			return;
		}
		// 把之前选中的图标设回未选中
		int oIndex = mCurrentIndex;
		// 更改选择状态
		setIcon(oIndex, false);
		mCurrentIndex = index;
		// 更新titlebar界面
		final int nIndex = mCurrentIndex;
		// 更改选择状态
		setIcon(nIndex, true);
	}

	/**
	 * 当本地没有顶级tab栏数据时，为了避免一进去是空的，先显示一个本地写死的顶级标题栏，但不加入点击事件。这个方法同样用在显示顶级tab栏的错误页上。
	 */
	public void showDefaultBar() {
		// 清空数据
		this.removeAllViews();
		for (int i = 0; i < 6; i++) {
			View item = mInflater.inflate(R.layout.apps_mgr_grid_titlebar_item, null);
			ImageView image = (ImageView) item.findViewById(R.id.grid_titlebar_image);
			TextView text = (TextView) item.findViewById(R.id.grid_titlebar_text);
			image.setImageDrawable(getDefaultIcon(i));
			text.setText(getDefaultTitle(i));
			this.addView(item, mLayoutParams);
		}
	}

	/**
	 * SD卡事件通知接口
	 */
	public void onSDCardStateChange(boolean turnon) {
		if (turnon) {
			for (int i = 0; i < mItems.size(); i++) {
				if (i == mCurrentIndex) {
					setIcon(i, true);
				} else {
					setIcon(i, false);
				}
			}
		}
	}

	/**
	 * 清空item点击事件
	 */
	public void clearClickListener() {
		if (mItems != null) {
			for (View item : mItems) {
				item.setOnClickListener(null);
			}
		}
	}

	/**
	 * 根据下标获取默认的未选中icon
	 */
	private Drawable getDefaultIcon(int index) {
		Resources res = getResources();
		switch (index) {
			case 0 :
				return res.getDrawable(R.drawable.appgame_toptab_apps);
			case 1 :
				return res.getDrawable(R.drawable.appgame_toptab_app);
			case 2 :
				return res.getDrawable(R.drawable.appgame_toptab_game);
			case 3 :
				return res.getDrawable(R.drawable.appgame_toptab_theme);
			case 4 :
				return res.getDrawable(R.drawable.appgame_toptab_locker);
			case 5 :
				return res.getDrawable(R.drawable.appgame_toptab_manage);
			default :
				return null;
		}
	}

	/**
	 * 根据下标获取默认的选中icon
	 */
	private Drawable getDefaultHitIcon(int index) {
		Resources res = getResources();
		switch (index) {
			case 0 :
				return res.getDrawable(R.drawable.appgame_toptab_apps_light);
			case 1 :
				return res.getDrawable(R.drawable.appgame_toptab_app_light);
			case 2 :
				return res.getDrawable(R.drawable.appgame_toptab_game_light);
			case 3 :
				return res.getDrawable(R.drawable.appgame_toptab_theme_light);
			case 4 :
				return res.getDrawable(R.drawable.appgame_toptab_locker_light);
			case 5 :
				return res.getDrawable(R.drawable.appgame_toptab_manage_light);
			default :
				return null;
		}
	}

	/**
	 * 根据标题获取默认的名字
	 */
	private String getDefaultTitle(int index) {
		Resources res = getResources();
		switch (index) {
			case 0 :
				return res.getString(R.string.appgame_toptitle_apps);
			case 1 :
				return res.getString(R.string.appgame_toptitle_app);
			case 2 :
				return res.getString(R.string.appgame_toptitle_game);
			case 3 :
				return res.getString(R.string.gostore_theme);
			case 4 :
				return res.getString(R.string.gostore_locker);
			case 5 :
				return res.getString(R.string.appgame_toptitle_manage);
			default :
				return null;
		}
	}
	
	/**
	 * 从SD卡或者网络加载图标
	 */
	private void loadImage(final String filePath, final String fileName, final String url,
			final AsyncImageLoadedCallBack callback) {
		if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(fileName) || TextUtils.isEmpty(url)) {
			return;
		}
		if (mPool == null || mPool.isShutdown()) {
//			Log.e("XIEDEZHI", "mPool init");
			mPool = Executors.newSingleThreadExecutor();
		}
		Runnable task = new Runnable() {

			@Override
			public void run() {
				Bitmap bm = null;
				try {
					bm = mImageManager.loadImgFromSD(filePath, fileName, url, false);
					if (bm == null) {
						bm = mImageManager.loadImgFromNetwork(url);
						if (bm != null) {
							if (FileUtil.isSDCardAvaiable()) {
								FileUtil.saveBitmapToSDFile(bm, filePath + fileName,
										Bitmap.CompressFormat.PNG);
							}
						}
					}
				} catch (OutOfMemoryError error) {
					//爆内存
					error.printStackTrace();
				}
				if (bm != null && callback != null) {
					final Bitmap ret = bm;
					// 主线程显示图片
					post(new Runnable() {

						@Override
						public void run() {
							callback.imageLoaded(ret, url);
						}
					});
				}
			}
		};
		mPool.execute(task);
	}
}
