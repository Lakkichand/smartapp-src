package com.jiubang.ggheart.appgame.base.component;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AdvancedManagementContainer;
import com.jiubang.ggheart.appgame.appcenter.component.AppsUpdateViewContainer;
import com.jiubang.ggheart.appgame.appcenter.component.MyAppsContainer;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.gostore.base.component.GridSortContainer;
import com.jiubang.ggheart.appgame.gostore.base.component.GridViewContainer;
import com.jiubang.ggheart.appgame.gostore.base.component.WallpaperGridContainer;

/**
 * Container生成器，为TabManageView提供Container。
 * 
 * 一个分类id对应一个container，container不再复用。
 * 
 * 首层的container用强引用，子分类的container用软引用，如果系统把container回收了就重新生成一个新的。
 * `
 * 
 * 我的应用，我的游戏，应用更新，搜索，高级管理暂时用软引用，避免内存溢出
 * 
 * @author xiedezhi
 * 
 */
public class ContainerBuiler {
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	/**
	 * 已经生成的container，以分类id为key
	 */
	private Map<Integer, SoftReference<? extends IContainer>> mBuildedContainer = new HashMap<Integer, SoftReference<? extends IContainer>>();
	/**
	 * 首层的container，用强引用，避免回收，加快tab切换速度
	 */
	private Map<Integer, IContainer> mBuildedTopContainer = new HashMap<Integer, IContainer>();

	public ContainerBuiler(Context context, LayoutInflater inflater) {
		mContext = context;
		mInflater = inflater;
	}

	/**
	 * 根据类型获取container，类型详情请看{@link ClassificationDataBean}
	 * 
	 * @param type
	 *            类型
	 * @return container
	 */
	public IContainer getContainer(ClassificationDataBean bean) {
		if (bean == null) {
			return null;
		}
		int type = bean.dataType;
		int typeId = bean.typeId;
		
		switch (type) {
		case ClassificationDataBean.MY_APP_TYPE : {
			// 我的应用
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof MyAppsContainer)) {
					MyAppsContainer container = (MyAppsContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "MyAppsContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			MyAppsContainer container = (MyAppsContainer) mInflater.inflate(
					R.layout.recomm_apps_management_myapps_layout, null);
//			Log.e("ContainerBuiler", "inflate MyAppsContainer");
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(typeId, pointer);
			return container;
		}
		case ClassificationDataBean.UPDATE_APP_TYPE : {
			// 应用更新
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof AppsUpdateViewContainer)) {
					AppsUpdateViewContainer container = (AppsUpdateViewContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "AppsUpdateViewContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			AppsUpdateViewContainer container = (AppsUpdateViewContainer) mInflater.inflate(
					R.layout.recomm_appsmanagement_update_list_container, null);
//			Log.e("ContainerBuiler", "inflate AppsUpdateViewContainer");
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(typeId, pointer);
			return container;
		}
		case ClassificationDataBean.SEARCH_TYPE : {
			// 搜索
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof SearchContainer)) {
					SearchContainer container = (SearchContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "SearchContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			SearchContainer container = (SearchContainer) mInflater.inflate(
					R.layout.appgame_search_container, null);
//			Log.e("ContainerBuiler", "inflate SearchContainer");
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(typeId, pointer);
			return container;
		}
		case ClassificationDataBean.FEATURE_TYPE: {
			// 精品推荐
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof FeaturedContainer) {
					if (((FeaturedContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"FeaturedContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof FeaturedContainer)) {
					FeaturedContainer container = (FeaturedContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "FeaturedContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			FeaturedContainer container = (FeaturedContainer) mInflater.inflate(
					R.layout.apps_mgr_game_feature, null);
//			Log.e("ContainerBuiler", "inflate FeaturedContainer");
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		case ClassificationDataBean.CATEGORIES_TYPE: {
			// 分类推荐
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof CategoriesContainer) {
					if (((CategoriesContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"CategoriesContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof CategoriesContainer)) {
					CategoriesContainer container = (CategoriesContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "CategoriesContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			CategoriesContainer container = (CategoriesContainer) mInflater.inflate(
					R.layout.apps_mgr_game_categories, null);
//			Log.e("ContainerBuiler", "inflate CategoriesContainer");
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		case ClassificationDataBean.SPECIALSUBJECT_TYPE: {
			// 专题推荐
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof ExtraContainer) {
					if (((ExtraContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"ExtraContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof ExtraContainer)) {
					ExtraContainer container = (ExtraContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "ExtraContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			ExtraContainer container = (ExtraContainer) mInflater.inflate(
					R.layout.apps_mgr_game_extra, null);
//			Log.e("ContainerBuiler", "inflate ExtraContainer");
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		case ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE: {
			// 一栏一列专题推荐
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof NewExtraContainer) {
					if (((NewExtraContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"NewExtraContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof NewExtraContainer)) {
					NewExtraContainer container = (NewExtraContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "NewExtraContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			NewExtraContainer container = (NewExtraContainer) mInflater.inflate(
					R.layout.appgame_new_extra_container, null);
//			Log.e("ContainerBuiler", "inflate NewExtraContainer");
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		case ClassificationDataBean.EDITOR_RECOMM_TYPE : {
			// 编辑推荐列表
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof EditorFavoriteContainer) {
					if (((EditorFavoriteContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"EditorFavoriteContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof EditorFavoriteContainer)) {
					EditorFavoriteContainer container = (EditorFavoriteContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "EditorFavoriteContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			EditorFavoriteContainer container = (EditorFavoriteContainer) mInflater.inflate(
					R.layout.appgame_editor_favorite, null);
//			Log.e("ContainerBuiler", "inflate EditorFavoriteContainer");
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		case ClassificationDataBean.ADVANCED_MANAGEMENT: {
			// 高级管理
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof AdvancedManagementContainer)) {
					AdvancedManagementContainer container = (AdvancedManagementContainer) pointer
							.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "AdvancedManagementContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			AdvancedManagementContainer container = (AdvancedManagementContainer) mInflater
					.inflate(R.layout.apps_management_advancedmanagement_layout, null);
			SoftReference<AdvancedManagementContainer> pointer = new SoftReference<AdvancedManagementContainer>(
					container);
			mBuildedContainer.put(typeId, pointer);
			return container;
		}
		case ClassificationDataBean.PRICE_ALERT: {
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof PriceAlertContainer) {
					if (((PriceAlertContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"PriceAlertContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof PriceAlertContainer)) {
					PriceAlertContainer container = (PriceAlertContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "PriceAlertContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			PriceAlertContainer container = (PriceAlertContainer) mInflater.inflate(
					R.layout.appgame_price_alert_container, null);
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		//这两种类型都以MultiContainer展示
		case ClassificationDataBean.TAB_TYPE :
		case ClassificationDataBean.BUTTON_TAB : {
			//按钮tab栏展示
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof MultiContainer) {
					if (((MultiContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"MultiContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof MultiContainer)) {
					MultiContainer container = (MultiContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "MultiContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			MultiContainer container = new MultiContainer(mContext);
			//			Log.e("ContainerBuiler", "inflate MultiContainer");
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		case ClassificationDataBean.GRID_SORT: {
			// 九格宫分类推荐
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof GridSortContainer) {
					if (((GridSortContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"GridSortContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof GridSortContainer)) {
					GridSortContainer container = (GridSortContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "GridSortContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			GridSortContainer container = (GridSortContainer) mInflater.inflate(
					R.layout.apps_mgr_game_gridsort, null);
//			Log.e("ContainerBuiler", "inflate CategoriesContainer");
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		case ClassificationDataBean.GRID_TYPE: {
			// 九宫格类型
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof GridViewContainer) {
					if (((GridViewContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"GridViewContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof GridViewContainer)) {
					GridViewContainer container = (GridViewContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler", "GridViewContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			GridViewContainer container = (GridViewContainer) mInflater.inflate(
					R.layout.apps_mgr_game_grid, null);
//			Log.e("ContainerBuiler", "inflate GridViewContainer");
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		case ClassificationDataBean.WALLPAPER_GRID: {
			// 壁纸九宫格类型
			if (mBuildedTopContainer.containsKey(typeId)) {
				IContainer container = mBuildedTopContainer.get(typeId);
				if (container != null && container instanceof WallpaperGridContainer) {
					if (((WallpaperGridContainer) container).getParent() != null) {
						// container没有被移除，不能使用
						Log.e("ContainerBuiler",
								"WallpaperGridContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			if (mBuildedContainer.containsKey(typeId)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer.get(typeId);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof WallpaperGridContainer)) {
					WallpaperGridContainer container = (WallpaperGridContainer) pointer.get();
					if (container != null && container.getParent() != null) {
						// container没有被移除，不能使用
						Log.e("WallpaperGridContainer", "WallpaperGridContainer.getParent() != null");
						return null;
					}
					return container;
				}
			}
			// 初始化container
			WallpaperGridContainer container = (WallpaperGridContainer) mInflater.inflate(
					R.layout.apps_mgr_wallpaper_grid, null);
//			Log.e("ContainerBuiler", "inflate WallpaperGridContainer");
			if (TabDataManager.getInstance().isTopTabId(typeId)) {
				mBuildedTopContainer.put(typeId, container);
			} else {
				SoftReference<IContainer> pointer = new SoftReference<IContainer>(
						container);
				mBuildedContainer.put(typeId, pointer);
			}
			return container;
		}
		case ClassificationDataBean.EMPTY_TYPE : {
			// 空container，不用放到缓存中，等真正的container生成后才放到缓存中
			EmptyContainer container = new EmptyContainer(mContext);
			return container;
		}
		default:
			Log.e("ContainerBuiler", "getContainer bad container type = "
					+ type);
			break;
		}
		return null;
	}

	/**
	 * 获取所有已经生成的container
	 */
	public List<IContainer> getBuildedContainers() {
		List<IContainer> ret = new ArrayList<IContainer>();
		//弱引用container
		Collection<SoftReference<? extends IContainer>> collection = mBuildedContainer
				.values();
		if (collection != null) {
			for (SoftReference<? extends IContainer> pointer : collection) {
				if (pointer != null && pointer.get() != null) {
					IContainer container = pointer.get();
					ret.add(container);
				}
			}
		}
		//强引用container
		for (IContainer container : mBuildedTopContainer.values()) {
			if (container != null) {
				ret.add(container);
			}
		}
		return ret;
	}
	
	/**
	 * 获取闲置的container，也就是被移除不在View树中的container
	 * 
	 * 包括MultiContainer中非正在显示的container
	 */
	public List<IContainer> getIdleContainers() {
		List<IContainer> ret = new ArrayList<IContainer>();
		List<IContainer> allContainers = getBuildedContainers();
		if (allContainers == null || allContainers.size() <= 0) {
			return ret;
		}
		for (IContainer container : allContainers) {
			if (container instanceof View) {
				View view = (View) container;
				if (view.getParent() == null) {
					ret.add(container);
				}
			}
		}
		return ret;
	}
}