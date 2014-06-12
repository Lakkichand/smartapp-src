package com.jiubang.ggheart.apps.appmanagement.component;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.SortUtils;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager;
import com.jiubang.ggheart.apps.appmanagement.bean.AppInfo;
import com.jiubang.ggheart.apps.appmanagement.component.PinnedHeaderListView.PinnedHeaderAdapter;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.ThreadPoolManager;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 */
public class MyAppsListAdapter extends BaseAdapter implements PinnedHeaderAdapter {

	private Context mContext = null;
	// private ArrayList<AppItemInfo> mMyAppsData = null;
	private ArrayList<AppInfo> mMyAppsData = null;

	private LayoutInflater mLayoutInflater = null;

	private Drawable mInternalDrawable = null;
	private Drawable mSDCardDrawable = null;
	/**
	 * 安装在内存里面的应用
	 */
	// private ArrayList<AppItemInfo> internalAppsData = null;
	/**
	 * 安装在sdcard上的应用
	 */
	private AppsSectionIndexer mIndexer;

	/**
	 * list中显示group
	 */
	private static final int ITEMTYPE_GROUP = 1;

	private Handler mHandler = null;
	private Handler mContainerHandler = null;
	ArrayList<AppItemInfo> appItemInfos = null;

	HashMap<String, SoftReference<BitmapDrawable>> mBitmapHashMap = new HashMap<String, SoftReference<BitmapDrawable>>(
			100); // 图片存储
	HashMap<String, Runnable> mLoadingImgRunableHashMap = new HashMap<String, Runnable>();

	private int mViewType = MyAppsView.VIEW_TYPE_APPS;

	public MyAppsListAdapter(Context context, int state) {
		mContext = context;
		// mMyAppsData = uninstallData;
		mLayoutInflater = LayoutInflater.from(mContext);
		mInternalDrawable = mContext.getResources().getDrawable(
				R.drawable.appsmanagement_phone_icon);
		mSDCardDrawable = mContext.getResources().getDrawable(R.drawable.appsmanagement_sd_icon);

		initHandler();
		appItemInfos = GOLauncherApp.getAppDataEngine().getAllAppItemInfos();
		// loadListData();
	}

	public MyAppsListAdapter(Context context, int state, int viewtype) {
		this(context, state);
		mViewType = viewtype;
	}

	/**
	 * 传入ContainerHandler
	 * 
	 * @param h
	 */
	public void setContainerHandler(Handler h) {
		mContainerHandler = h;
	}

	private void initHandler() {

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg != null) {
					switch (msg.what) {
						case 0 :
							mMyAppsData = (ArrayList<AppInfo>) msg.obj;
							int internalAppSize = msg.arg1;
							int externalAppSize = msg.arg2;

							Resources resource = mContext.getResources();
							String phoneMemory = resource.getString(R.string.phone_memory) + "("
									+ internalAppSize + ")";
							String sdMemory = resource.getString(R.string.sd_memory) + "("
									+ externalAppSize + ")";

							String[] sections = new String[] { phoneMemory, sdMemory };
							int[] counts = new int[] { internalAppSize + 1, externalAppSize + 1 };
							mIndexer = new AppsSectionIndexer(sections, counts);

							break;
						case 1 :
							mMyAppsData = (ArrayList<AppInfo>) msg.obj;
							break;
						case 2 :
							BitmapDrawable bitmap = (BitmapDrawable) msg.obj;
							Bundle bundle = msg.getData();
							String imageUrl = bundle.getString("packageName");
							mBitmapHashMap.put(imageUrl, new SoftReference<BitmapDrawable>(bitmap));
							if (imageUrl != null) {
								if (mLoadingImgRunableHashMap != null) {
									mLoadingImgRunableHashMap.remove(imageUrl);
								}
							}
							MyAppsListAdapter.this.notifyDataSetChanged();
							break;
					}
				}
			}
		};

	}

	public void refreshData(List<String> appSizeList) {
		// getListData();
		refreshData(appSizeList, true);
	}

	public void refreshData(List<String> appSizeList, boolean isRefresh) {
		if (isRefresh) {
			PreferencesManager preferences = new PreferencesManager(mContext,
					IPreferencesIds.APPS_ORDER_TYPE, Context.MODE_PRIVATE);
			int orderType = preferences.getInt("orderType", 0);
			if (orderType == 0) {
				getListData();
			} else {
				getAppDataOrderByType(orderType);
			}
		}
		// if (appSizeList != null) {
		// for (String appSizeStr : appSizeList) {
		// String[] appInfo = appSizeStr.split("/");
		// if (mMyAppsData != null) {
		// for (AppItemInfo appItemInfo : mMyAppsData) {
		// if (appItemInfo.mType != ITEMTYPE_GROUP
		// && appInfo[0].equals(appItemInfo.mPackageName)) {
		// appItemInfo.mAppSize = Integer.parseInt(appInfo[1]);
		// break;
		// }
		// }
		// }
		// }
		// }
		// this.notifyDataSetChanged();
	}

	/**
	 * 加载应用程序信息
	 */
	private synchronized void getListData() {

		ArrayList<AppInfo> internalAppsData = new ArrayList<AppInfo>();
		ArrayList<AppInfo> externalAppsData = new ArrayList<AppInfo>();

		AppsManagementActivity.getApplicationManager().getAllInstalledApp(internalAppsData,
				externalAppsData);

		int internalAppSize = 0;
		int externalAppSize = 0;

		if (!internalAppsData.isEmpty()) {
			SortUtils.sort(internalAppsData, "getTitle", null, null, "ASC");
			internalAppSize = internalAppsData.size();
		}

		if (!externalAppsData.isEmpty()) {
			SortUtils.sort(externalAppsData, "getTitle", null, null, "ASC");
			externalAppSize = externalAppsData.size();
		}

		Resources resource = mContext.getResources();
		String phoneMemory = resource.getString(R.string.phone_memory) + "(" + internalAppSize
				+ ")";
		String sdMemory = resource.getString(R.string.sd_memory) + "(" + externalAppSize + ")";

		ArrayList<AppInfo> myAppsData = new ArrayList<AppInfo>(internalAppSize + externalAppSize
				+ 2);

		AppInfo appInfo = new AppInfo();
		appInfo.mTitle = phoneMemory;
		appInfo.mType = ITEMTYPE_GROUP;
		myAppsData.add(appInfo);

		if (internalAppSize > 0) {
			myAppsData.addAll(internalAppsData);
		}

		appInfo = new AppInfo();
		appInfo.mTitle = sdMemory;
		appInfo.mType = ITEMTYPE_GROUP;
		myAppsData.add(appInfo);

		if (externalAppSize > 0) {
			myAppsData.addAll(externalAppsData);
		}

		Message msg = new Message();
		msg.arg1 = internalAppSize;
		msg.arg2 = externalAppSize;
		msg.obj = myAppsData;
		if (mHandler != null) {
			mHandler.sendMessage(msg);
		}
	}

	private void getAppDataOrderByType(int type) {
		appItemInfos = GOLauncherApp.getAppDataEngine().getAllAppItemInfos();
		ArrayList<AppInfo> allAppsDataList = new ArrayList<AppInfo>();
		AppsManagementActivity.getApplicationManager().getAllInstalledApp(allAppsDataList);
		sortItem(allAppsDataList, type);

		if (mHandler != null) {
			Message message = mHandler.obtainMessage(1);
			message.obj = allAppsDataList;
			mHandler.sendMessage(message);
		}
	}

	private void sortItem(ArrayList<AppInfo> allAppsDataList, int type) {
		switch (type) {
			case 1 :
				SortUtils.sortByLong(allAppsDataList, "getInstallTime", null, null, "DESC");
				break;
			case 2 :
				SortUtils.sort(allAppsDataList, "getTitle", null, null, "ASC");
				break;
			case 3 :
				SortUtils.sortByLong(allAppsDataList, "getSize", null, null, "DESC");
				break;
			default :
				break;
		}
	}

	@Override
	public int getCount() {
		return mMyAppsData == null ? 0 : mMyAppsData.size();
	}

	@Override
	public Object getItem(int position) {
		if (mMyAppsData != null && position < mMyAppsData.size()) {
			return mMyAppsData.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (mMyAppsData == null || position >= mMyAppsData.size()) {
			return view;
		}
		final AppInfo info = mMyAppsData.get(position);
		if (info != null) {
			if (convertView == null) {
				AppstListItemViews itemviews = new AppstListItemViews();
				if (info.mType == ITEMTYPE_GROUP) {
					view = inflateGroupRes(itemviews, parent, info);
				} else {
					view = inflateItemRes(itemviews, parent);
					setAppValue(info, itemviews, view);
				}
			} else {
				AppstListItemViews items = (AppstListItemViews) convertView.getTag();
				if (items.mType == info.mType) {
					view = convertView;
					if (items.mType == ITEMTYPE_GROUP) {
						// 显示group
						items.mSoftNameTextView.setText(info.mTitle);
					} else {
						// 显示应用
						setAppValue(info, items, view);
					}
				} else {
					AppstListItemViews itemviews = new AppstListItemViews();
					if (info.mType == ITEMTYPE_GROUP) {
						view = inflateGroupRes(itemviews, parent, info);
					} else {
						view = inflateItemRes(itemviews, parent);
						setAppValue(info, itemviews, view);
					}
				}
			}
		}
		return view;
	}

	/**
	 * 加载应用的资源文件
	 * 
	 * @param itemviews
	 * @param view
	 * @param parent
	 */
	private View inflateItemRes(AppstListItemViews itemviews, ViewGroup parent) {
		itemviews.mType = 0;
		View view;
		if (mViewType == MyAppsView.VIEW_TYPE_APPS_UNINSTALL) {
			view = mLayoutInflater.inflate(R.layout.appsuninstall_list_item, parent, false);
		} else {
			view = mLayoutInflater.inflate(R.layout.appsmanagement_soft_list_item, parent, false);
		}

		// view绑定数据

		itemviews.mSoftImgView = (ImageView) view.findViewById(R.id.softImageView);
		itemviews.mSoftNameTextView = (TextView) view.findViewById(R.id.softNameTextView);
		itemviews.mVerTextView = (TextView) view.findViewById(R.id.verTextView);
		itemviews.mMoveImageView = (ImageView) view.findViewById(R.id.moveImageView);
		itemviews.mOperatorButton = (Button) view.findViewById(R.id.operatorbutton);

		view.setTag(itemviews);
		return view;
	}

	/**
	 * 加载group显示的资源文件
	 * 
	 * @param itemviews
	 * @param view
	 * @param parent
	 * @param info
	 */
	private View inflateGroupRes(AppstListItemViews itemviews, ViewGroup parent, AppInfo info) {
		itemviews.mType = MyAppsDataManager.ITEMTYPE_GROUP;

		View view = mLayoutInflater.inflate(R.layout.recomm_appsmanagement_list_group_item, parent,
				false);
		// view绑定数据
		itemviews.mSoftNameTextView = (TextView) view.findViewById(R.id.nametext);
		itemviews.mSoftNameTextView.setPadding(0,
				mContext.getResources()
						.getDimensionPixelSize(R.dimen.download_manager_text_padding), 0, mContext
						.getResources()
						.getDimensionPixelSize(R.dimen.download_manager_text_padding));
		setTextShowInfo(itemviews.mSoftNameTextView, info.mTitle);
		view.setTag(itemviews);
		return view;
	}

	/**
	 * 为textView设置Drawables和显示的值
	 * 
	 * @param textView
	 * @param textValue
	 */
	private void setTextShowInfo(TextView textView, String textValue) {

		textView.setText(textValue);

		if (textValue.startsWith(mContext.getResources().getString(R.string.phone_memory))) {
			textView.setCompoundDrawablesWithIntrinsicBounds(mInternalDrawable, null, null, null);
		} else {
			textView.setCompoundDrawablesWithIntrinsicBounds(mSDCardDrawable, null, null, null);
		}
	}

	/**
	 * 为app设置value
	 * 
	 * @param info
	 * @param itemviews
	 * @param view
	 */
	private void setAppValue(final AppInfo info, AppstListItemViews itemviews, View view) {
		String appName = null;
		appName = info.mTitle.trim();

		long appSize = info.mAppSize;
		// if (appSize == 0) {
		// appSize = info.getAppSize(pm);
		// }
		// long appSize = info.getAppSize(pm);
		itemviews.mVerTextView.setText(ConvertUtils.convertSizeToString(appSize,
				ConvertUtils.FORM_DECIMAL_WITH_TWO));

		itemviews.mSoftNameTextView.setText(appName);
		// itemviews.mSoftImgView.setImageDrawable(info.mIcon);
		itemviews.mSoftImgView.setImageDrawable(getIconDrawable(info.mPackageName));

		if (info.mLocation == 0 || info.mLocation == 2) {
			if (info.mIsInternal) {
				if (Machine.isSDCardExist()) {
					itemviews.mMoveImageView
							.setImageResource(R.drawable.appsmanagement_move_to_sdcard_icon);
				}
			} else {
				itemviews.mMoveImageView
						.setImageResource(R.drawable.appsmanagement_move_to_phone_icon);
			}
		} else {
			itemviews.mMoveImageView.setImageDrawable(null);
		}

		// 重置图标
		if (mViewType == MyAppsView.VIEW_TYPE_APPS_UNINSTALL) {
			if (info.mIsSelected) {
				itemviews.mOperatorButton.setBackgroundResource(R.drawable.apps_uninstall_selected);
			} else {
				itemviews.mOperatorButton
						.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
			}
		}
		itemviews.mOperatorButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 按钮点击
				if (mViewType == MyAppsView.VIEW_TYPE_APPS) {
					AppsManagementActivity.getApplicationManager().uninstallApp(info.mPackageName);
				} else if (mViewType == MyAppsView.VIEW_TYPE_APPS_UNINSTALL) {
					SelectApp(info, v);
				}
			}
		});

	}

	/**
	 * 钩选一个应用
	 * 
	 * @param info
	 * @param index
	 *            位置
	 */
	public void ClickedApp(AppInfo info, AppstListItemViews itemViews) {
		SelectApp(info, itemViews.mOperatorButton);
	}

	/**
	 * 钩选一个应用时
	 * 
	 * @param info
	 * @param btn
	 * @author zhaojunjie
	 */
	public void SelectApp(AppInfo info, View btn) {
		if (info.mIsSelected) { // 原来已被选中(取消选中)
			info.mIsSelected = false;
			btn.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
			if (isAllNotSelected()) {
				Message msg = new Message();
				msg.what = AppsUninstallContainer.SELECTED_STATE_NONE;
				msg.arg1 = 0;
				mContainerHandler.sendMessage(msg);
			} else {
				Message msg = new Message();
				msg.what = AppsUninstallContainer.SELECTED_STATE_PART;
				msg.arg1 = getSelectedCount();
				mContainerHandler.sendMessage(msg);
			}
		} else {
			info.mIsSelected = true;
			btn.setBackgroundResource(R.drawable.apps_uninstall_selected);
			if (isAllSelected()) {
				Message msg = new Message();
				msg.what = AppsUninstallContainer.SELECTED_STATE_ALL;
				msg.arg1 = getSelectedCount();
				mContainerHandler.sendMessage(msg);
			} else {
				Message msg = new Message();
				msg.what = AppsUninstallContainer.SELECTED_STATE_PART;
				msg.arg1 = getSelectedCount();
				mContainerHandler.sendMessage(msg);
			}
		}
	}

	/**
	 * 判断是否所有应用被选中
	 * 
	 * @return
	 * @author zhaojunjie
	 */
	private boolean isAllSelected() {
		boolean result = true;
		if (mMyAppsData == null) {
			return result;
		}

		for (AppInfo info : mMyAppsData) {
			if (info.mType != ITEMTYPE_GROUP) {
				if (!info.mIsSelected) {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 判断是否所有应用被取消选中
	 * 
	 * @return
	 * @author zhaojunjie
	 */
	private boolean isAllNotSelected() {
		boolean result = true;
		if (mMyAppsData == null) {
			return result;
		}

		for (AppInfo info : mMyAppsData) {
			if (info.mType != ITEMTYPE_GROUP) {
				if (info.mIsSelected) {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 取得已被选中的应用个数
	 * 
	 * @return
	 */
	private int getSelectedCount() {
		int count = 0;
		if (mMyAppsData == null) {
			return count;
		}

		for (AppInfo info : mMyAppsData) {
			if (info.mType != ITEMTYPE_GROUP) {
				if (info.mIsSelected) {
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * 改变所有应用选中状态
	 * 
	 * @param b
	 * @author zhaojunjie
	 */
	public void SetAllAppsSelectState(boolean b) {
		if (mMyAppsData == null) {
			return;
		}

		for (AppInfo info : mMyAppsData) {
			if (info.mType != ITEMTYPE_GROUP) {
				info.mIsSelected = b;
			}
		}

		// 返回消息
		if (b) {
			Message msg = new Message();
			msg.what = AppsUninstallContainer.SELECTED_STATE_ALL;
			msg.arg1 = getSelectedCount();
			mContainerHandler.sendMessage(msg);
		} else {
			Message msg = new Message();
			msg.what = AppsUninstallContainer.SELECTED_STATE_NONE;
			msg.arg1 = 0;
			mContainerHandler.sendMessage(msg);
		}
	}

	/**
	 * 取得被选中的应用包名
	 * 
	 * @return
	 */
	public ArrayList<String> getSelectApps() {
		ArrayList<String> appsPkgNames = new ArrayList<String>();

		if (mMyAppsData == null) {
			return appsPkgNames;
		}

		for (AppInfo info : mMyAppsData) {
			if (info.mType != ITEMTYPE_GROUP) {
				if (info.mIsSelected) {
					appsPkgNames.add(info.mPackageName);
				}
			}
		}
		return appsPkgNames;
	}

	private Drawable getIconDrawable(String packageName) {
		Drawable drawable = getDrawableFromAppItemInfo(packageName);
		if (drawable == null) {
			// drawable =
			// mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon);

			drawable = getBitmap(packageName);
			if (drawable == null || drawable.getIntrinsicWidth() <= 0
					|| drawable.getIntrinsicHeight() <= 0) {
				drawable = mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
			}
			// else {
			// Log.d("MyAppsListAdapter", packageName + " is exist");
			// }
		}
		return drawable;

	}

	private BitmapDrawable getDrawableFromAppItemInfo(String pkgName) {
		if (pkgName == null || appItemInfos == null || appItemInfos.size() == 0) {
			return null;
		}
		for (AppItemInfo appItemInfo : appItemInfos) {
			if (pkgName.equals(appItemInfo.getAppPackageName())) {
				return appItemInfo.mIcon;
			}
		}
		return null;
	}

	private BitmapDrawable getBitmap(final String packageName) {
		if (packageName == null || mBitmapHashMap == null) {
			return null;
		}
		BitmapDrawable bmp = null;
		// 先从内存里面取
		SoftReference<BitmapDrawable> image = mBitmapHashMap.get(packageName);
		if (image != null) {
			// 图片先从内存里面取
			bmp = image.get();
		}

		if (bmp == null) {
			// 如果内存取到的图片为空
			if (mLoadingImgRunableHashMap != null
					&& !mLoadingImgRunableHashMap.containsKey(packageName)) {
				mLoadingImgRunableHashMap.put(packageName, new Runnable() {
					@Override
					public void run() {
						try {
							BitmapDrawable drawable = loadIcon(packageName);
							if (drawable != null) {
								// 如果本地图片取得到
								if (mHandler != null) {
									Message message = mHandler.obtainMessage(2);
									Bundle bundle = new Bundle();
									bundle.putString("packageName", packageName);

									message.obj = drawable;
									message.setData(bundle);
									mHandler.sendMessage(message);
								}
							}
						} catch (OutOfMemoryError error) {
							error.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				ThreadPoolManager.getInstance("load_image").execute(
						mLoadingImgRunableHashMap.get(packageName));
			}
		}

		return bmp;
	}

	public void cleanImage() {
		if (mLoadingImgRunableHashMap != null && mLoadingImgRunableHashMap.size() > 0) {
			Iterator<String> keyStrIter = mLoadingImgRunableHashMap.keySet().iterator();
			while (keyStrIter.hasNext()) {
				String key = keyStrIter.next();
				Runnable runnable = mLoadingImgRunableHashMap.get(key);
				if (runnable != null) {
					ThreadPoolManager.getInstance("load_image").cancel(runnable);
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
					refer = null;
				}
			}
			mBitmapHashMap.clear();
		}
	}

	private BitmapDrawable loadIcon(String packname) throws NotFoundException {
		BitmapDrawable bitDrawable = null;
		try {
			PackageManager pkgmanager = mContext.getPackageManager();
			PackageInfo packinfo = pkgmanager.getPackageInfo(packname,
					PackageManager.GET_SIGNATURES);
			Drawable drawable = packinfo.applicationInfo.loadIcon(pkgmanager);
			if (drawable == null || drawable.getIntrinsicWidth() <= 0
					|| drawable.getIntrinsicHeight() <= 0) {
				drawable = mContext.getResources().getDrawable(android.R.drawable.sym_def_app_icon);
			}
			bitDrawable = AppDataEngine.getInstance(mContext).createBitmapDrawable(drawable);
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return bitDrawable;
	}

	@Override
	public int getPinnedHeaderState(int position) {
		if (getCount() <= 0) {
			return PINNED_HEADER_GONE;
		}
		int realPosition = getRealPosition(position);
		if (realPosition < 0) {
			return PINNED_HEADER_GONE;
		}
		// The header should get pushed up if the top item shown
		// is the last item in a section for a particular letter.

		int section = getSectionForPosition(realPosition);
		int nextSectionPosition = getPositionForSection(section + 1);
		if (nextSectionPosition != -1 && realPosition == nextSectionPosition - 1) {
			return PINNED_HEADER_PUSHED_UP;
		}
		return PINNED_HEADER_VISIBLE;
	}

	@Override
	public void configurePinnedHeader(View header, int position) {
		// 计算位置
		int realPosition = getRealPosition(position);
		int section = getSectionForPosition(realPosition);
		TextView headText = (TextView) header.findViewById(R.id.nametext);
		setTextShowInfo(headText, getSections(section));

	}

	private int getRealPosition(int pos) {
		return pos;
	}

	private int getSectionForPosition(int pos) {
		if (mIndexer == null) {
			return -1;
		}
		return mIndexer.getSectionForPosition(pos);
	}

	//
	private int getPositionForSection(int pos) {
		if (mIndexer == null) {
			return -1;
		}
		return mIndexer.getPositionForSection(pos);
	}

	public String getSections(int pos) {
		if (mIndexer == null || pos < 0 || pos >= mIndexer.getSections().length) {
			return " ";
		} else {
			return (String) mIndexer.getSections()[pos];
		}
	}

	@Override
	public boolean isEnabled(int position) {
		AppInfo info = (AppInfo) getItem(position);
		if (info != null) {
			// 如果为1不可选择
			return info.mType != ITEMTYPE_GROUP;
		}
		return super.isEnabled(position);
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 */
	public final class AppstListItemViews {

		private ImageView mSoftImgView = null;
		private TextView mSoftNameTextView = null;
		private TextView mVerTextView = null;
		private ImageView mMoveImageView = null;
		private Button mOperatorButton = null;
		// 组件类型
		public int mType;

		public void destory() {
			if (mSoftImgView != null) {
				// mSoftImgView.recycle();
				// mSoftImgView.clearIcon();
				mSoftImgView = null;
			}
		}

	}

	public void cleanup() {

		/*
		 * if (internalAppsData != null) { internalAppsData.clear(); } if
		 * (externalAppsData != null) { externalAppsData.clear(); }
		 */
		if (mMyAppsData != null) {
			mMyAppsData.clear();
		}
		cleanImage();
	}
}
