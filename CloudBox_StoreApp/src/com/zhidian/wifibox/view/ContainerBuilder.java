package com.zhidian.wifibox.view;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.PageDataBean;

/**
 * 工厂类，向TabManageView提供IContainer
 * 
 * 用url做key，保存IContainer的软引用
 * 
 * @author xiedezhi
 * 
 */
public class ContainerBuilder {
	private Context mContext = null;
	private LayoutInflater mInflater = null;
	/**
	 * 已经生成的container，以分类url为key
	 */
	private Map<String, SoftReference<? extends IContainer>> mBuildedContainer = new HashMap<String, SoftReference<? extends IContainer>>();

	public ContainerBuilder(Context context, LayoutInflater inflater) {
		mContext = context;
		mInflater = inflater;
	}

	/**
	 * 根据列表数据获取生成列表对象
	 */
	public IContainer getContainer(PageDataBean page) {
		if (page == null) {
			return null;
		}
		int type = page.mDataType;
		String url = page.mUrl;
		switch (type) {

		case PageDataBean.CATEGORIES_DATATYPE: {
			// 应用分类列表
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof CategoriesContainer)) {
					CategoriesContainer container = (CategoriesContainer) pointer
							.get();
					return container;
				}
			}
			CategoriesContainer container = (CategoriesContainer) mInflater
					.inflate(R.layout.categoriescontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		case PageDataBean.DOWNLOADPAGE_DATATYPE: {
			// 下载管理列表
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof DownloadManagerContainer)) {
					DownloadManagerContainer container = (DownloadManagerContainer) pointer
							.get();
					return container;
				}
			}
			DownloadManagerContainer container = (DownloadManagerContainer) mInflater
					.inflate(R.layout.downloadcontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		case PageDataBean.MANAGER_DATATYPE: {
			// 管理界面
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof ManagerContainer)) {
					ManagerContainer container = (ManagerContainer) pointer
							.get();
					return container;
				}
			}
			ManagerContainer container = (ManagerContainer) mInflater.inflate(
					R.layout.managercontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		case PageDataBean.TOPIC_DATATYPE: {
			// 专题列表
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof TopicContainer)) {
					TopicContainer container = (TopicContainer) pointer.get();
					return container;
				}
			}
			TopicContainer container = (TopicContainer) mInflater.inflate(
					R.layout.topiccontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		case PageDataBean.TOPICCONTENT_DATATYPE: {
			// 专题类容列表
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof TopicContentContainer)) {
					TopicContentContainer container = (TopicContentContainer) pointer
							.get();
					return container;
				}
			}
			TopicContentContainer container = (TopicContentContainer) mInflater
					.inflate(R.layout.topiccontentcontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		case PageDataBean.UPDATEPAGE_DATATYPE: {
			// 应用更新列表
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof UpdateContainer)) {
					UpdateContainer container = (UpdateContainer) pointer.get();
					return container;
				}
			}
			UpdateContainer container = (UpdateContainer) mInflater.inflate(
					R.layout.updatecontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		case PageDataBean.EXTRA_DATATYPE: {
			// 应用游戏列表
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof ExtraContainer)) {
					ExtraContainer container = (ExtraContainer) pointer.get();
					return container;
				}
			}
			ExtraContainer container = (ExtraContainer) mInflater.inflate(
					R.layout.extracontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		
		case PageDataBean.EXTRA_RANK:{
			//排行
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof RankingContainer)) {
					RankingContainer container = (RankingContainer) pointer.get();
					return container;
				}
			}
			
			RankingContainer container = (RankingContainer) mInflater.inflate(
					R.layout.rankingcontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		
		case PageDataBean.HOME_FEATURE_DATATYPE: {
			// 首页推荐列表
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof HomeFeatureContainer)) {
					HomeFeatureContainer container = (HomeFeatureContainer) pointer
							.get();
					return container;
				}
			}
			HomeFeatureContainer container = (HomeFeatureContainer) mInflater
					.inflate(R.layout.homefeaturecontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		case PageDataBean.EMPTY_DATATYPE: {
			// 空容器
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof EmptyContainer)) {
					EmptyContainer container = (EmptyContainer) pointer.get();
					return container;
				}
			}
			EmptyContainer container = (EmptyContainer) mInflater.inflate(
					R.layout.emptycontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		case PageDataBean.SPEEDINGDOWNLOAD_DATATYPE: {
			// 超速下载
			if (mBuildedContainer.containsKey(url)) {
				SoftReference<? extends IContainer> pointer = mBuildedContainer
						.get(url);
				if (pointer != null && pointer.get() != null
						&& (pointer.get() instanceof SpeedingDownloadContainer)) {
					SpeedingDownloadContainer container = (SpeedingDownloadContainer) pointer
							.get();
					return container;
				}
			}
			SpeedingDownloadContainer container = (SpeedingDownloadContainer) mInflater
					.inflate(R.layout.speedingdownloadcontainer, null);
			SoftReference<IContainer> pointer = new SoftReference<IContainer>(
					container);
			mBuildedContainer.put(url, pointer);
			return container;
		}
		default:
			break;
		}
		return null;
	}
}
