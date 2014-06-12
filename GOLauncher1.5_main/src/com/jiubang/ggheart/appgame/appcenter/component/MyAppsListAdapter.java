package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.ConvertUtils;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.bean.AppInfo;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView.PinnedHeaderAdapter;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager;
import com.jiubang.ggheart.appgame.appcenter.contorler.MyAppsDataManager.SortedAppInfo;
import com.jiubang.ggheart.appgame.base.component.MoreRecommendedAppsActivity;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 */
public class MyAppsListAdapter extends BaseAdapter implements PinnedHeaderAdapter {

	private Context mContext = null;
	private ArrayList<AppInfo> mMyAppsData = null;
	private LayoutInflater mLayoutInflater = null;
	private Drawable mInternalDrawable = null;
	private Drawable mSDCardDrawable = null;
	private AppsSectionIndexer mIndexer;
	private Handler mContainerHandler = null;
	private Bitmap mDefaultBitmap = null;
	/**
	 * 保存是否弹出应用管理和打开的框
	 */
	// public HashMap<Integer, Boolean> mSaveIsOpen = new HashMap<Integer,
	// Boolean>();
	private int mViewType = MyAppsView.VIEW_TYPE_APPS;
	private Drawable mDefaultIcon;

	private static final int TYPE_GROUP = 0;

	private static final int TYPE_INFO = 1;

	private static final int TYPE_COUNT = 2;

	public MyAppsListAdapter(Context context, int state) {
		mContext = context;
		// mMyAppsData = uninstallData;
		mLayoutInflater = LayoutInflater.from(mContext);
		mInternalDrawable = mContext.getResources().getDrawable(
				R.drawable.appsmanagement_phone_icon);
		mSDCardDrawable = mContext.getResources().getDrawable(R.drawable.appsmanagement_sd_icon);

		mDefaultIcon = mContext.getResources().getDrawable(R.drawable.default_icon);
		setDefaultIcon(mDefaultIcon);
		// initHandler();
	}

	public void setData(SortedAppInfo sortedAppInfo) {
		if (sortedAppInfo != null) {
			mMyAppsData = sortedAppInfo.mSortedAppsData;
			if (sortedAppInfo.mSortType == 0) {

				int internalAppSize = sortedAppInfo.mInternalAppsData;
				int externalAppSize = sortedAppInfo.mExternalAppsData;

				Resources resource = mContext.getResources();
				String phoneMemory = resource.getString(R.string.phone_memory) + "("
						+ internalAppSize + ")";
				String sdMemory = resource.getString(R.string.sd_memory) + "(" + externalAppSize
						+ ")";

				setGroupTextInfo(0, phoneMemory);
				setGroupTextInfo(internalAppSize + 1, sdMemory);

				String[] sections = new String[] { phoneMemory, sdMemory };
				int[] counts = new int[] { internalAppSize + 1, externalAppSize + 1 };
				mIndexer = new AppsSectionIndexer(sections, counts);
			} else if (sortedAppInfo.mSortType == 2) {

				updateList(mMyAppsData);

			}
			this.notifyDataSetChanged();
		}
	}

	public void updateList(ArrayList<AppInfo> list) {
		if (list != null && list.size() >= 0) {
			int count = 1;
			ArrayList<String> stringDivider = new ArrayList<String>();
			ArrayList<Integer> intDivider = new ArrayList<Integer>();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).mType == MyAppsDataManager.ITEMTYPE_GROUP) {
					stringDivider.add(list.get(i).getTitle());
					if (i != 0) {
						intDivider.add(count);
						count = 1;
					}
				} else {
					count++;
				}
				if (i == list.size() - 1) {
					intDivider.add(count);
				}
			}
			String[] sections = stringDivider.toArray(new String[stringDivider.size()]);
			int[] counts = new int[intDivider.size()];
			for (int i = 0; i < intDivider.size(); i++) {
				counts[i] = intDivider.get(i).intValue();
			}
			mIndexer = new AppsSectionIndexer(sections, counts);
		}
	}

	public MyAppsListAdapter(Context context, int state, int viewtype) {
		this(context, state);
		mViewType = viewtype;
	}

	private void setGroupTextInfo(int position, String title) {
		if (mMyAppsData != null && position < mMyAppsData.size()) {
			AppInfo groupAppInfo = mMyAppsData.get(position);
			if (groupAppInfo != null && groupAppInfo.mType == MyAppsDataManager.ITEMTYPE_GROUP) {
				groupAppInfo.mTitle = title;
			}
		}
	}

	/**
	 * 传入ContainerHandler
	 * 
	 * @param h
	 */
	public void setContainerHandler(Handler h) {
		mContainerHandler = h;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		if (mMyAppsData.get(position).mType == MyAppsDataManager.ITEMTYPE_GROUP) {
			return TYPE_GROUP;
		} else {
			return TYPE_INFO;
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
		int type = getItemViewType(position);
		AppstListItemViews itemviews = null;
		if (info != null) {
			if (convertView == null) {
				if (type == TYPE_GROUP) {
					itemviews = new AppstListItemViews();
					convertView = mLayoutInflater.inflate(R.layout.recomm_appsmanagement_list_head,
							parent, false);
					itemviews.mSoftNameTextView = (TextView) convertView
							.findViewById(R.id.nametext);
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.FILL_PARENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					int padding = mContext.getResources().getDimensionPixelSize(
							R.dimen.download_manager_text_padding);
					itemviews.mSoftNameTextView.setPadding(2 * padding, padding, 0, padding);
					itemviews.mSoftNameTextView.setLayoutParams(lp);
					convertView.setTag(itemviews);
				} else {
					itemviews = new AppstListItemViews();
					convertView = inflateItemRes(itemviews, parent);
				}
			} else {
				itemviews = (AppstListItemViews) convertView.getTag();
			}
			if (info.mType == MyAppsDataManager.ITEMTYPE_GROUP) {
				if (itemviews != null) {
					itemviews.mSoftNameTextView.setText(info.mTitle);
				}
			} else {
				if (itemviews != null) {
					itemviews.setAppValue(info, position);
					if (itemviews.mLeftLayout != null) {
						itemviews.mLeftLayout.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								showPopup(v, info.mPackageName);
							}
						});
					}
				}
			}
		}
		//		if (convertView == null) {
		//			itemviews = new AppstListItemViews();
		//			if (info.mType == MyAppsDataManager.ITEMTYPE_GROUP) {
		//				view = inflateGroupRes(itemviews, parent, info);
		//			} else {
		//				view = inflateItemRes(itemviews, parent);
		//				itemviews.setAppValue(info, position);
		//			}
		//		} else {
		//			itemviews = (AppstListItemViews) convertView.getTag();
		//			if (itemviews.mType == info.mType) {
		//				view = convertView;
		//				if (itemviews.mType == MyAppsDataManager.ITEMTYPE_GROUP) {
		//					// 显示group
		//					itemviews.mSoftNameTextView.setText(info.mTitle);
		//				} else {
		//					// 显示应用
		//					itemviews.setAppValue(info, position);
		//				}
		//			} else {
		//				itemviews = new AppstListItemViews();
		//				if (info.mType == MyAppsDataManager.ITEMTYPE_GROUP) {
		//					view = inflateGroupRes(itemviews, parent, info);
		//				} else {
		//					view = inflateItemRes(itemviews, parent);
		//					itemviews.setAppValue(info, position);
		//				}
		//			}
		//		}
		//		if (itemviews != null && itemviews.mLeftLayout != null) {
		//			itemviews.mLeftLayout.setOnClickListener(new OnClickListener() {
		//
		//				@Override
		//				public void onClick(View v) {
		//					showPopup(v, info.mPackageName);
		//				}
		//			});
		//		}
		return convertView;
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
			itemviews.mOperatorButton = (Button) view.findViewById(R.id.operatorbutton);
		} else {
			view = mLayoutInflater.inflate(R.layout.recomm_appsmanagement_soft_list_item, parent,
					false);
			itemviews.mOperatorButton = (Button) view.findViewById(R.id.operatorbutton);
			itemviews.mRightLayout = (LinearLayout) view.findViewById(R.id.content_right_layout);
		}

		// view绑定数据
		itemviews.mLeftLayout = (RelativeLayout) view.findViewById(R.id.contentRelativeLayout);
		itemviews.mImageSwitcher = (ImageSwitcher) view.findViewById(R.id.softImageSwitcher);
		itemviews.mSoftImgView = (ImageView) view.findViewById(R.id.softImageView);
		itemviews.mAnotherSoftImgView = (ImageView) view.findViewById(R.id.anotherSoftImageView);
		itemviews.mSoftNameTextView = (TextView) view.findViewById(R.id.softNameTextView);
		itemviews.mVerTextView = (TextView) view.findViewById(R.id.verTextView);
		itemviews.mMoveImageView = (ImageView) view.findViewById(R.id.moveImageView);
		// itemviews.mOperatorButton = (Button) view
		// .findViewById(R.id.operatorbutton);
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

		View view = mLayoutInflater
				.inflate(R.layout.recomm_appsmanagement_list_head, parent, false);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		// view绑定数据
		itemviews.mSoftNameTextView = (TextView) view.findViewById(R.id.nametext);
		itemviews.mSoftNameTextView.setPadding(
				mContext.getResources()
						.getDimensionPixelSize(R.dimen.download_manager_text_padding) * 2, mContext
						.getResources()
						.getDimensionPixelSize(R.dimen.download_manager_text_padding), 0, mContext
						.getResources()
						.getDimensionPixelSize(R.dimen.download_manager_text_padding));
		itemviews.mSoftNameTextView.setText(info.mTitle);
		itemviews.mSoftNameTextView.setLayoutParams(lp);
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
	 * 钩选一个应用
	 * 
	 * @param info
	 * @param index
	 *            位置
	 */
	public void clickedApp(AppInfo info, AppstListItemViews itemViews) {
		selectApp(info, itemViews.mOperatorButton);
	}

	/**
	 * 钩选一个应用时
	 * 
	 * @param info
	 * @param btn
	 * @author zhaojunjie
	 */
	public void selectApp(AppInfo info, View btn) {
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
			if (info.mType != MyAppsDataManager.ITEMTYPE_GROUP) {
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
			if (info.mType != MyAppsDataManager.ITEMTYPE_GROUP) {
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
			if (info.mType != MyAppsDataManager.ITEMTYPE_GROUP) {
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
	public void setAllAppsSelectState(boolean b) {
		if (mMyAppsData == null) {
			return;
		}

		for (AppInfo info : mMyAppsData) {
			if (info.mType != MyAppsDataManager.ITEMTYPE_GROUP) {
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
			if (info.mType != MyAppsDataManager.ITEMTYPE_GROUP) {
				if (info.mIsSelected) {
					appsPkgNames.add(info.mPackageName);
				}
			}
		}
		return appsPkgNames;
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
		headText.setText(getSections(section));

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
			return info.mType != MyAppsDataManager.ITEMTYPE_GROUP;
		}
		return super.isEnabled(position);
	}

	public void cleanup() {

		/*
		 * if (internalAppsData != null) { internalAppsData.clear(); } if
		 * (externalAppsData != null) { externalAppsData.clear(); }
		 */
		if (mMyAppsData != null) {
			mMyAppsData = null;
		}
	}

	/**
	 * 
	 * <br>
	 * 类描述: <br>
	 * 功能详细描述:
	 */
	public final class AppstListItemViews {

		private ImageView mSoftImgView = null;
		private ImageView mAnotherSoftImgView = null;
		private ImageSwitcher mImageSwitcher = null;
		private TextView mSoftNameTextView = null;
		private TextView mVerTextView = null;
		private ImageView mMoveImageView = null;
		// private Button mButton = null;
		private Button mOperatorButton = null;
		private RelativeLayout mLeftLayout = null;
		private LinearLayout mRightLayout = null;
		// 组件类型
		public int mType;
		private AppInfo mAppInfo;

		private AsyncImageLoadedCallBack mCallBack = new AsyncImageLoadedCallBack() {
			@Override
			public void imageLoaded(Bitmap bm, String url) {
				if (mImageSwitcher != null && mImageSwitcher.getTag().equals(url)) {
					Drawable drawable = ((ImageView) mImageSwitcher.getCurrentView()).getDrawable();
					if (drawable instanceof BitmapDrawable) {
						Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
						if (bitmap == mDefaultBitmap) {
							mImageSwitcher.setImageDrawable(new BitmapDrawable(bm));
						}
					}
				} else {
					bm = null;
				}
			}
		};

		// private OnClickListener mClickListener = new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// // 按钮点击
		// if (mViewType == MyAppsView.VIEW_TYPE_APPS) {
		// StatisticsData.countStatData(mContext,
		// StatisticsData.KEY_NO_BATCH_UNINSTALL);
		// AppsManagementActivity.sendHandler(this,
		// IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
		// IDiyMsgIds.APPS_MANAGEMENT_UNINSTALL_APP, 0,
		// mAppInfo.mPackageName, null);
		// } else if (mViewType == MyAppsView.VIEW_TYPE_APPS_UNINSTALL) {
		// selectApp(mAppInfo, v);
		// }
		// }
		// };

		private void setAppValue(final AppInfo info, final int position) {
			mAppInfo = info;
			mVerTextView.setText(ConvertUtils.convertSizeToString(info.mAppSize,
					ConvertUtils.FORM_DECIMAL_WITH_TWO));
			mSoftNameTextView.setText(info.mTitle.trim());
			if (mImageSwitcher != null) {
				mImageSwitcher.setTag(info.mPackageName);
				mImageSwitcher.getCurrentView().clearAnimation();
				mImageSwitcher.getNextView().clearAnimation();
				Bitmap bm = AsyncImageManager.getInstance().loadImageIconForList(position,
						mImageSwitcher.getContext(), info.mPackageName, true, mCallBack);
				ImageView imageView = (ImageView) mImageSwitcher.getCurrentView();
				if (bm != null) {
					imageView.setImageBitmap(bm);
				} else {
					imageView.setImageBitmap(mDefaultBitmap);
				}
			}

			if (info.mLocation == AppInfo.INSTALL_LOCATION_AUTO
					|| info.mLocation == AppInfo.INSTALL_LOCATION_PREFER_EXTERNAL) {
				if (info.mIsInternal) {
					mMoveImageView.setImageResource(R.drawable.appsmanagement_move_to_sdcard_icon);
				} else {
					mMoveImageView.setImageResource(R.drawable.appsmanagement_move_to_phone_icon);
				}
			} else {
				mMoveImageView.setImageDrawable(null);
			}

			// 重置图标
			if (mViewType == MyAppsView.VIEW_TYPE_APPS_UNINSTALL) {
				if (info.mIsSelected) {
					mOperatorButton.setBackgroundResource(R.drawable.apps_uninstall_selected);
				} else {
					mOperatorButton.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
				}
			}

			mOperatorButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					// 按钮点击
					if (mViewType == MyAppsView.VIEW_TYPE_APPS) {
						StatisticsData.countStatData(mContext,
								StatisticsData.KEY_NO_BATCH_UNINSTALL);
						AppsManagementActivity.sendHandler(this,
								IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
								IDiyMsgIds.APPS_MANAGEMENT_UNINSTALL_APP, 0, mAppInfo.mPackageName,
								null);
						// removeUpdateCommentState(position);
					} else if (mViewType == MyAppsView.VIEW_TYPE_APPS_UNINSTALL) {
						selectApp(mAppInfo, v);
					}

				}
			});
			
			if (mRightLayout != null) {
				mRightLayout.setOnClickListener(mRightLayoutListener);
			}
		}

		private OnClickListener mRightLayoutListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 按钮点击
				if (mViewType == MyAppsView.VIEW_TYPE_APPS) {
					StatisticsData.countStatData(mContext,
							StatisticsData.KEY_NO_BATCH_UNINSTALL);
					AppsManagementActivity.sendHandler(this,
							IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
							IDiyMsgIds.APPS_MANAGEMENT_UNINSTALL_APP, 0, mAppInfo.mPackageName,
							null);
					// removeUpdateCommentState(position);
				}
			}
		}; 
		
		public void destory() {
			mImageSwitcher = null;
			mSoftImgView = null;
			mAnotherSoftImgView = null;
			mSoftNameTextView = null;
			mVerTextView = null;
			mMoveImageView = null;
			if (mOperatorButton != null) {
				mOperatorButton.setOnClickListener(null);
				mOperatorButton = null;
			}
			// mClickListener = null;
			mCallBack = null;
			// if (mSaveIsOpen != null) {
			// mSaveIsOpen.clear();
			// mSaveIsOpen = null;
			// }
		}
	}

	private RelativeLayout mPopupUpLayout = null;
	private RelativeLayout mPopupDownLayout = null;
	private TextView mUpManage = null;
	private TextView mDownManage = null;
	private TextView mUpOpenApp = null;
	private TextView mDownOpenApp = null;
	private TextView mUpSimilarApp = null;
	private TextView mDownSimilarApp = null;

	/**
	 * 弹出框的高度，暂时不知如何计算，写死先
	 */
	private static final int POPUP_HEIGHT = DrawUtils.dip2px(62.0f);

	/**
	 * 弹出框箭头的高度，暂时不知如何计算，写死先
	 */
	private static final int POPUP_ARROW_HEIGHT = DrawUtils.dip2px(8.0f);

	private PopupWindow mPopupWindow;

	private void showPopup(View parent, final String pkgName) {
		if (mPopupWindow == null) {
			View view = mLayoutInflater.inflate(
					R.layout.recomm_apps_management_my_apps_popup_layout, null);
			mPopupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT,
					LayoutParams.WRAP_CONTENT);
			// 设置Popup
			mPopupWindow.setFocusable(true);
			mPopupWindow.setBackgroundDrawable(new ColorDrawable(0));
			mPopupUpLayout = (RelativeLayout) view.findViewById(R.id.popwindow_up);
			mUpManage = (TextView) view.findViewById(R.id.manange_app_up);
			mDownManage = (TextView) view.findViewById(R.id.manange_app_down);
			mPopupDownLayout = (RelativeLayout) view.findViewById(R.id.popwindow_down);
			mUpOpenApp = (TextView) view.findViewById(R.id.open_app_up);
			mDownOpenApp = (TextView) view.findViewById(R.id.open_app_down);
			mUpSimilarApp = (TextView) view.findViewById(R.id.similar_apps_up);
			mDownSimilarApp = (TextView) view.findViewById(R.id.similar_apps_down);

		}
		if (mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		} else {
			// mPopup.showAtLocation(parent, Gravity.TOP | Gravity.LEFT, 0, 0);
			// mPopup.showAsDropDown(parent);
			// 下面的方法是用屏幕上的绝对坐标显示，mPopupWindow
			// 我们往往不知道mPopupWindow要显示的精确位置，通常先计算页面上某个元素mView的位置，在进行偏移
			// 得到mView在屏幕中的坐标
			int[] pos = new int[2];
			parent.getLocationOnScreen(pos);
			int screenHeight = getScreenHeight(mContext);
			int viewTopPos = pos[1];
			int offsetY = 0;
			if (viewTopPos < screenHeight / 2) {
				mPopupWindow.setAnimationStyle(R.style.PopupAnimation_apps_magane_up);
				// 如果是在上半屏幕，则mPopup在组件下方
				offsetY = pos[1] + parent.getHeight() - POPUP_ARROW_HEIGHT;
				mPopupUpLayout.setVisibility(View.VISIBLE);
				mPopupDownLayout.setVisibility(View.GONE);
				mDownManage.setOnClickListener(null);
				mDownOpenApp.setOnClickListener(null);
				mDownSimilarApp.setOnClickListener(null);
				mUpManage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							mPopupWindow.dismiss();
						} catch (Exception e) {
						}

						showAppDetails(pkgName);
					}
				});

				mUpOpenApp.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							mPopupWindow.dismiss();
						} catch (Exception e) {
						}
						startApp(mContext, pkgName);
					}
				});
				
				mUpSimilarApp.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO 跳转到更多同类推荐应用
						try {
							mPopupWindow.dismiss();
						} catch (Exception e) {
						}
						gotoSimilarApps(pkgName);
					}
				});
			} else {
				// 如果是在下半屏幕，则mPopup在组件上方
				mPopupWindow.setAnimationStyle(R.style.PopupAnimation_apps_magane_down);

				offsetY = pos[1] - POPUP_HEIGHT + POPUP_ARROW_HEIGHT;
				mPopupUpLayout.setVisibility(View.GONE);
				mPopupDownLayout.setVisibility(View.VISIBLE);
				mUpManage.setOnClickListener(null);
				mUpOpenApp.setOnClickListener(null);
				mUpSimilarApp.setOnClickListener(null);
				mDownManage.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							mPopupWindow.dismiss();
						} catch (Exception e) {
						}

						showAppDetails(pkgName);
					}
				});

				mDownOpenApp.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						try {
							mPopupWindow.dismiss();
						} catch (Exception e) {
						}
						startApp(mContext, pkgName);
					}
				});
				
				mDownSimilarApp.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO 跳转到更多同类推荐应用
						try {
							mPopupWindow.dismiss();
						} catch (Exception e) {
						}
						gotoSimilarApps(pkgName);
					}
				});
			}
			mPopupWindow.showAtLocation(parent, Gravity.TOP, 0, offsetY);
		}
	}

	public static int getScreenHeight(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wMgr.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		return height;
	}

	private void showAppDetails(String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return;
		}
		final String scheme = "package";
		/**
		 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.1及之前版本)
		 */
		final String appPkgName21 = "com.android.settings.ApplicationPkgName";
		/**
		 * 调用系统InstalledAppDetails界面所需的Extra名称(用于Android 2.2)
		 */
		final String appPkgName22 = "pkg";
		/**
		 * InstalledAppDetails所在包名
		 */
		final String appDetailsPackageName = "com.android.settings";
		/**
		 * InstalledAppDetails类名
		 */
		final String appDetailsClassName = "com.android.settings.InstalledAppDetails";

		Intent intent = new Intent();
		final int apiLevel = Build.VERSION.SDK_INT;
		if (apiLevel >= 9) {
			// 2.3（ApiLevel 9）以上，使用SDK提供的接口
			intent.setAction(ICustomAction.ACTION_SETTINGS);
			Uri uri = Uri.fromParts(scheme, packageName, null);
			intent.setData(uri);
		} else {
			// 2.3以下，使用非公开的接口（查看InstalledAppDetails源码）
			// 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。
			final String appPkgName = apiLevel == 8 ? appPkgName22 : appPkgName21;
			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName(appDetailsPackageName, appDetailsClassName);
			intent.putExtra(appPkgName, packageName);
		}
		try {
			mContext.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据包名启动程序
	 * 
	 * @param context
	 * @param pkgName
	 * @return
	 */
	public static boolean startApp(Context context, String pkgName) {
		if (context == null) {
			throw new IllegalArgumentException("context can not be null");
		}
		if (pkgName == null) {
			return false;
		}
		PackageManager pm = context.getPackageManager();
		if (pm == null) {
			return false;
		}
		// 需要做这样的判断，否则用户删除该包之后，下面的startActivity会产生异常，导致程序挂掉
		Intent intent = pm.getLaunchIntentForPackage(pkgName);
		if (intent == null) {
			return false;
		}
		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * <br>功能简述:跳转到更多相关推荐应用Activity
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param pkgName
	 */
	private void gotoSimilarApps(String pkgName) {
		if (pkgName == null || "".equals(pkgName)) {
			return;
		}
		Intent in = new Intent(mContext, MoreRecommendedAppsActivity.class);
		in.putExtra(MoreRecommendedAppsActivity.sPACKAGE_NAME, pkgName);
		mContext.startActivity(in);
	}

	/**
	 * 设置列表展现的默认图标
	 */
	private void setDefaultIcon(Drawable drawable) {
		if (drawable != null && drawable instanceof BitmapDrawable) {
			mDefaultBitmap = ((BitmapDrawable) drawable).getBitmap();
		}
	}
}
