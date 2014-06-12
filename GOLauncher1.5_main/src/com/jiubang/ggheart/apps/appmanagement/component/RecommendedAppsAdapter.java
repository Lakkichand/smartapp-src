/**
 * 
 */
package com.jiubang.ggheart.apps.appmanagement.component;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.appmanagement.bean.RecommendedApp;
import com.jiubang.ggheart.apps.appmanagement.bean.RecommendedAppCategory;
import com.jiubang.ggheart.apps.appmanagement.help.RecommAppsUtils;
import com.jiubang.ggheart.apps.gowidget.gostore.ThreadPoolManager;

/**
 * 应用推荐
 * 
 * @author zhoujun
 * 
 */
public class RecommendedAppsAdapter extends BaseExpandableListAdapter {

	private ArrayList<RecommendedAppCategory> mRecommAppCtgList = null;
	private LayoutInflater mLayoutInflater = null;
	private Context mContext = null;
	private int position = -1;
	private HashMap<String, SoftReference<BitmapDrawable>> mBitmapHashMap = null; // 图片存储
	private HashMap<String, Runnable> mLoadingImgRunableHashMap = new HashMap<String, Runnable>();
	private final String BUNDLE_IMAGE_URL = "imageUrl";
	private final String THREAD_NAME_FOR_LOAD_IMAGE = "thread_load_image";

	public RecommendedAppsAdapter(Context context) {
		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);
	}

	public void refreshData(ArrayList<RecommendedAppCategory> recommAppCtgList) {
		mRecommAppCtgList = recommAppCtgList;
		if (mRecommAppCtgList != null && !mRecommAppCtgList.isEmpty()) {
			int allAppCount = 0;
			for (RecommendedAppCategory recommAppCtg : mRecommAppCtgList) {
				allAppCount = allAppCount + recommAppCtg.mCount;
			}
			mBitmapHashMap = new HashMap<String, SoftReference<BitmapDrawable>>(allAppCount);
		}
	}

	public ArrayList<RecommendedAppCategory> getRecommAppList() {
		return mRecommAppCtgList;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return mRecommAppCtgList == null ? 0 : mRecommAppCtgList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {

		return mRecommAppCtgList.get(groupPosition) == null ? 0 : mRecommAppCtgList
				.get(groupPosition).mCount;
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return mRecommAppCtgList.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return mRecommAppCtgList.get(groupPosition).mCount > 0 ? mRecommAppCtgList
				.get(groupPosition).mRecommendedAppList.get(childPosition) : 0;
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
			ViewGroup parent) {
		View view = convertView;
		if (mRecommAppCtgList != null && groupPosition < mRecommAppCtgList.size()) {
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.appsmanagement_recomm_list_group_item, null);
			}
			RecommendedAppCategory recommAppCtg = mRecommAppCtgList.get(groupPosition);

			TextView title = (TextView) view.findViewById(R.id.recomm_app_nametext);
			ImageView imageView = (ImageView) view.findViewById(R.id.recomm_app_group_image);
			title.setText(recommAppCtg.mName + "(" + recommAppCtg.mCount + ")");
			if (isExpanded) {
				imageView.setImageResource(R.drawable.recomm_app_group_expand);
			} else {
				imageView.setImageResource(R.drawable.recomm_app_group_collapse);
			}

			if (recommAppCtg.mFirstShow) {
				if (recommAppCtg.mViewtype == 1) {
					((ExpandableListView) parent).expandGroup(groupPosition);
					recommAppCtg.mFirstShow = false;
				}
			}

			if (position == groupPosition) {
				((ExpandableListView) parent).expandGroup(groupPosition);
				position = -1;
			}
		}
		return view;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		RecommendedAppsUpdateListItem recommAppInfoListItem = null;
		RecommendedAppCategory recommAppCtg = mRecommAppCtgList.get(groupPosition);
		if (recommAppCtg != null && childPosition < recommAppCtg.mCount) {
			RecommendedApp recommApp = recommAppCtg.mRecommendedAppList.get(childPosition);
			if (convertView != null && convertView instanceof RecommendedAppsUpdateListItem) {
				recommAppInfoListItem = (RecommendedAppsUpdateListItem) convertView;
				recommAppInfoListItem.resetDefaultStatus();
			}
			if (recommAppInfoListItem == null) {
				recommAppInfoListItem = (RecommendedAppsUpdateListItem) mLayoutInflater.inflate(
						R.layout.appsmanagement_recomm_list_item, null);
			}
			//			BitmapDrawable bitmap = getBitmap(recommApp.mIconLocalPath);
			//			recommAppInfoListItem.bindAppInfo(mContext, recommApp, bitmap);
			recommAppInfoListItem.bindAppInfo(mContext, recommApp);
		}

		return recommAppInfoListItem;

	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	private BitmapDrawable getBitmap(final String imgUrl) {
		if (imgUrl == null || mBitmapHashMap == null) {
			return null;
		}
		BitmapDrawable bmp = null;
		// 先从内存里面取
		SoftReference<BitmapDrawable> image = mBitmapHashMap.get(imgUrl);
		if (image != null) {
			// 图片先从内存里面取
			bmp = image.get();
		}

		if (bmp == null) {
			// Log.d("RecommmendedAdapter",
			// imgUrl+" is not esxist ,need to load");
			// 如果内存取到的图片为空
			if (mLoadingImgRunableHashMap != null && !mLoadingImgRunableHashMap.containsKey(imgUrl)) {
				mLoadingImgRunableHashMap.put(imgUrl, new Runnable() {
					@Override
					public void run() {
						loadImgInBackgroud(imgUrl);
					}
				});
				ThreadPoolManager.getInstance(THREAD_NAME_FOR_LOAD_IMAGE).execute(
						mLoadingImgRunableHashMap.get(imgUrl));
			}
		}

		return bmp;
	}

	private void loadImgInBackgroud(String imgUrl) {
		try {
			BitmapDrawable drawable = null;
			File file = new File(imgUrl);
			if (file.exists()) {
				drawable = RecommAppsUtils.loadAppIcon(imgUrl, mContext);
				if (drawable != null) {
					// 如果本地图片取得到
					if (mHandler != null) {
						Message message = mHandler.obtainMessage(1);
						Bundle bundle = new Bundle();
						bundle.putString(BUNDLE_IMAGE_URL, imgUrl);

						message.obj = drawable;
						message.setData(bundle);
						mHandler.sendMessage(message);
						return;
					}
				}
			}

			// 当图片不存在时，取消加载任务,避免再次进入的时候，不执行加载任务
			if (mLoadingImgRunableHashMap != null) {
				Runnable runnable = mLoadingImgRunableHashMap.get(imgUrl);
				if (runnable != null) {
					ThreadPoolManager.getInstance(THREAD_NAME_FOR_LOAD_IMAGE).cancel(runnable);
					mLoadingImgRunableHashMap.remove(imgUrl);
				}
			}
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				BitmapDrawable bitmap = (BitmapDrawable) msg.obj;
				Bundle bundle = msg.getData();
				String imageUrl = bundle.getString(BUNDLE_IMAGE_URL);
				mBitmapHashMap.put(imageUrl, new SoftReference<BitmapDrawable>(bitmap));
				if (imageUrl != null) {
					if (mLoadingImgRunableHashMap != null) {
						mLoadingImgRunableHashMap.remove(imageUrl);
					}
				}
				RecommendedAppsAdapter.this.notifyDataSetChanged();
			}
			super.handleMessage(msg);
		}
	};

	public void clean() {
		if (mLoadingImgRunableHashMap != null && mLoadingImgRunableHashMap.size() > 0) {
			Iterator<String> keyStrIter = mLoadingImgRunableHashMap.keySet().iterator();
			while (keyStrIter.hasNext()) {
				String key = keyStrIter.next();
				Runnable runnable = mLoadingImgRunableHashMap.get(key);
				if (runnable != null) {
					ThreadPoolManager.getInstance(THREAD_NAME_FOR_LOAD_IMAGE).cancel(runnable);
				}
			}
			mLoadingImgRunableHashMap.clear();
		}

		if (mBitmapHashMap != null && mBitmapHashMap.size() > 0) {
			Iterator<String> iter = mBitmapHashMap.keySet().iterator();
			while (iter.hasNext()) {
				String key = iter.next();
				SoftReference<BitmapDrawable> refer = mBitmapHashMap.get(key);
				if (refer != null) {
					BitmapDrawable drawable = refer.get();
					if (drawable != null) {
						drawable = null;
					}
					// refer.get().getBitmap().recycle();
					refer = null;
				}
			}
			mBitmapHashMap.clear();
		}
	}
}
