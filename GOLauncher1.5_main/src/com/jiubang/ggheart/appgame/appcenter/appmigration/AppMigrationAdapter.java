/*
 * 文 件 名:  AppMigrationAdapter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-19
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.appcenter.appmigration;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AppsSectionIndexer;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView.PinnedHeaderAdapter;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-11-19]
 */
public class AppMigrationAdapter extends BaseAdapter implements PinnedHeaderAdapter {

	private Context mContext = null;

	private ArrayList<AppMigrationBean> mInfoList = new ArrayList<AppMigrationBean>();

	private LayoutInflater mInflater = null;

	private Drawable mDefaultIcon;

	private AppsSectionIndexer mIndexer;
	
	private Bitmap mDefaultBitmap = null;

	private static final int TYPE_GROUP = 0;
	
	private static final int TYPE_INFO = 1;
	
	private static final int TYPE_COUNT = 2;
	
	public AppMigrationAdapter(Context context, ArrayList<AppMigrationBean> list) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		updateList(list);
		mDefaultIcon = mContext.getResources().getDrawable(R.drawable.default_icon);
		setDefaultIcon(mDefaultIcon);
	}

	/**
	 * <br>功能简述:更新列表数据源
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param list
	 */
	public void updateList(ArrayList<AppMigrationBean> list) {
		if (list != null && list.size() >= 0) {
			mInfoList = list;
			int count = 1;
			ArrayList<String> stringDivider = new ArrayList<String>();
			ArrayList<Integer> intDivider = new ArrayList<Integer>();
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getType() == AppMigrationBean.sTYPE_GROUP) {
					stringDivider.add(list.get(i).getName());
					if (i != 0) {
						intDivider.add(count);
						count = 1;
					}
				} else {
					count++;
					if (i == list.size() - 1) {
						intDivider.add(count);
					}
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

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return TYPE_COUNT;
	}
	
	@Override
	public int getItemViewType(int position) {
		if (mInfoList.get(position).getType() == AppMigrationBean.sTYPE_GROUP) {
			return TYPE_GROUP;
		} else {
			return TYPE_INFO;
		}
	}
	
	/** {@inheritDoc} */

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mInfoList.size();
	}

	/** {@inheritDoc} */

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mInfoList.get(position);
	}

	/** {@inheritDoc} */

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/** {@inheritDoc} */

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewholder = null;
		int type = getItemViewType(position);
		if (convertView == null) {
			viewholder = new ViewHolder();
			if (type == TYPE_INFO) {
				convertView = mInflater.inflate(R.layout.app_migration_item, null);
				viewholder.mImageSwitcher = (ImageSwitcher) convertView
						.findViewById(R.id.app_migration_switcher);
				viewholder.mAnotherIcon = (ImageView) convertView
						.findViewById(R.id.app_migration_another_icon);
				viewholder.mIcon = (ImageView) convertView.findViewById(R.id.app_migration_icon);
				viewholder.mName = (TextView) convertView.findViewById(R.id.app_migration_name);
				viewholder.mSize = (TextView) convertView.findViewById(R.id.app_migration_size);
				viewholder.mImageView = (ImageView) convertView
						.findViewById(R.id.app_migration_button);
				viewholder.mButtonText = (TextView) convertView
						.findViewById(R.id.app_migration_button_text);
				viewholder.mRightRelativeLayout = (RelativeLayout) convertView
						.findViewById(R.id.app_migration_right_relativelayout);
				viewholder.mLeftRelativeLayout = (RelativeLayout) convertView
						.findViewById(R.id.app_migration_left_relativelayout);
				convertView.setTag(viewholder);
			} else {
				convertView = mInflater.inflate(R.layout.recomm_appsmanagement_list_head, null);
				viewholder.mName = (TextView) convertView.findViewById(R.id.nametext);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.FILL_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				viewholder.mName.setPadding(
						mContext.getResources().getDimensionPixelSize(
								R.dimen.download_manager_text_padding) * 2,
						mContext.getResources().getDimensionPixelSize(
								R.dimen.download_manager_text_padding),
						0,
						mContext.getResources().getDimensionPixelSize(
								R.dimen.download_manager_text_padding));
				viewholder.mName.setLayoutParams(lp);
				convertView.setTag(viewholder);
			}
		} else {
			viewholder = (ViewHolder) convertView.getTag();
		}
		AppMigrationBean bean = mInfoList.get(position);
		if (bean != null) {
			if (bean.getType() == AppMigrationBean.sTYPE_GROUP) {
				viewholder.mName.setText(bean.getName());
			} else {
				viewholder.mName.setText(bean.getName());
				viewholder.mSize.setText(bean.getSize());
				setPackageIcon(mContext, viewholder.mImageSwitcher, bean.getPackageName(), position);
				viewholder.mRightRelativeLayout.setTag(bean.getPackageName());
				viewholder.mRightRelativeLayout.setOnClickListener(mOnClickListener);
				if (bean.getType() == AppMigrationBean.sTYPE_INTERNAL_STORAGE) {
					viewholder.mLeftRelativeLayout.setBackgroundDrawable(null);
					viewholder.mLeftRelativeLayout.setOnClickListener(null);
					viewholder.mButtonText.setText(mContext
							.getString(R.string.appgame_migration_to_sdcard));
					viewholder.mImageView
							.setImageResource(R.drawable.appgame_appmigration_to_sdcard);
				} else if (bean.getType() == AppMigrationBean.sTYPE_SD) {
					viewholder.mLeftRelativeLayout.setBackgroundDrawable(null);
					viewholder.mLeftRelativeLayout.setOnClickListener(null);
					viewholder.mButtonText.setText(mContext
							.getString(R.string.appgame_migration_to_phone));
					viewholder.mImageView
							.setImageResource(R.drawable.appgame_appmigration_to_phone);
				} else {
					viewholder.mLeftRelativeLayout.setTag(bean.getPackageName());
					viewholder.mLeftRelativeLayout.setOnClickListener(mOnClickListener);
					viewholder.mRightRelativeLayout.setVisibility(View.GONE);
				}
			}
		}
		return convertView;
	}

	/** {@inheritDoc} */

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

	/**
	 * PackageImageTask 复用
	 * @param imageSwitcher
	 * @param packageName
	 */
	private void setPackageIcon(Context context, final ImageSwitcher imageSwitcher, String packageName, int position) {
		imageSwitcher.setTag(packageName);
		imageSwitcher.getCurrentView().clearAnimation();
		imageSwitcher.getNextView().clearAnimation();
		Bitmap bm = AsyncImageManager.getInstance().loadImageIconForList(position, imageSwitcher.getContext(), packageName,
				true, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap bm, String url) {
						if (imageSwitcher != null && imageSwitcher.getTag().equals(url)) {
							Drawable drawable = ((ImageView) imageSwitcher
									.getCurrentView()).getDrawable();
							if (drawable instanceof BitmapDrawable) {
								Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
								if (bitmap == mDefaultBitmap) {
									imageSwitcher.setImageDrawable(new BitmapDrawable(bm));
								}
							}
						} else {
							bm = null;
						}
					}
				});
		ImageView imageView = (ImageView) imageSwitcher.getCurrentView();
		if (bm != null) {
			imageView.setImageBitmap(bm);
		} else {
			imageView.setImageBitmap(mDefaultBitmap);
		}
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

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String packageName = (String) v.getTag();
			if (packageName == null || packageName.equals("")) {
				return;
			}
			showAppDetails(packageName);
		}
	};

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-11-20]
	 */
	private class ViewHolder {
		ImageSwitcher mImageSwitcher;
		ImageView mIcon;
		ImageView mAnotherIcon;
		TextView mName;
		TextView mSize;
		ImageView mImageView;
		TextView mButtonText;
		RelativeLayout mRightRelativeLayout;
		RelativeLayout mLeftRelativeLayout;
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
