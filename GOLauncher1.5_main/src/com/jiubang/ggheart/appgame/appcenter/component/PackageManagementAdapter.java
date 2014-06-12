/**
 * 
 */
package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.bean.AppPackageInfoBean;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView.PinnedHeaderAdapter;

/**
 * @author liguoliang
 *
 */
public class PackageManagementAdapter extends BaseAdapter implements PinnedHeaderAdapter {
	private Context mContext;

	private LayoutInflater mInflater = null;

	/**
	 * 安装列表
	 */
	private List<AppPackageInfoBean> mInstallList;

	/**
	 * 已安装列表
	 */
	private List<AppPackageInfoBean> mInstalledList;

	private AppsSectionIndexer mIndexer;

	private PackageOnclickListener mListener;

	private PopupWindow mPopup;

	private RelativeLayout mPopupUpLayout;

	private Button mUpBtn;

	private RelativeLayout mPopupDownLayout;

	private Button mDownBtn;

	/**
	 * 弹出框的高度，暂时不知如何计算，写死先
	 */
	private static final int POPUP_HEIGHT = DrawUtils.dip2px(62.0f);

	/**
	 * 弹出框箭头的高度，暂时不知如何计算，写死先
	 */
	private static final int POPUP_ARROW_HEIGHT = DrawUtils.dip2px(8.0f);

	private static final int TYPE_GROUP = 0;

	private static final int TYPE_INFO = 1;

	private static final int TYPE_COUNT = 2;

	private boolean mIsDelete = false;
	private HashMap<AppPackageInfoBean, Boolean> mSelectedMap = null;;

	public void setOnClickLisener(PackageOnclickListener listener) {
		mListener = listener;
	}

	/**
	 * @author liguoliang
	 *
	 */
	public interface PackageOnclickListener {
		public static final int OP_INSTALL = 1;
		public static final int OP_DELETE = 2;
		public static final int OP_SELECT = 3;

		void onClick(int op, AppPackageInfoBean bean);
	}

	public PackageManagementAdapter(Context context) {
		this.mContext = context;
		mInflater = LayoutInflater.from(context);
	}

	public void updateData(List<AppPackageInfoBean> installList,
			List<AppPackageInfoBean> installedList) {
		if (installList != null) {
			mInstallList = (List<AppPackageInfoBean>) ((ArrayList<AppPackageInfoBean>) installList)
					.clone();
		}
		if (installedList != null) {
			mInstalledList = (List<AppPackageInfoBean>) ((ArrayList<AppPackageInfoBean>) installedList)
					.clone();
		}
		String[] sections = null;
		int[] counts = null;
		if (mInstallList == null || mInstallList.isEmpty()) {
			sections = new String[] { mContext.getString(R.string.appcenter_package_installed) };
			counts = new int[] { mInstalledList == null ? 1 : mInstalledList.size() + 1 };
		} else {
			sections = new String[] { mContext.getString(R.string.appcenter_package_uninstall),
					mContext.getString(R.string.appcenter_package_installed) };
			counts = new int[] { mInstallList.size() + 1,
					mInstalledList == null ? 1 : mInstalledList.size() + 1 };
		}
		mIndexer = new AppsSectionIndexer(sections, counts);
	}

	@Override
	public int getCount() {
		int count = 0;
		if (mInstallList != null && !mInstallList.isEmpty()) {
			count += mInstallList.size();
			++count;	//加上头信息
		}
		if (mInstalledList != null && !mInstalledList.isEmpty()) {
			count += mInstalledList.size();
			++count;	//加上头信息
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewholder = null;
		if (convertView == null || (convertView != null && convertView.getTag() == null)) {
			viewholder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.apps_management_packagemanagement_item_layout,
					null);
			viewholder.mInfoLayout = (RelativeLayout) convertView
					.findViewById(R.id.apps_packagemanagement_package_info_layout);
			viewholder.mPackageIcon = (ImageView) convertView
					.findViewById(R.id.apps_packagemanagement_package_icon);
			viewholder.mPackageName = (TextView) convertView
					.findViewById(R.id.apps_packagemanagement_package_name);
			viewholder.mPackageSize = (TextView) convertView
					.findViewById(R.id.apps_packagemanagement_package_size);
			viewholder.mPackageVersion = (TextView) convertView
					.findViewById(R.id.apps_packagemanagement_package_version);
			viewholder.mOperatorButton = (Button) convertView
					.findViewById(R.id.apps_packagemanagement_operator);
			viewholder.mOperatorText = (TextView) convertView
					.findViewById(R.id.apps_packagemanagement_operator_text);
			viewholder.mInstalledNew = (TextView) convertView
					.findViewById(R.id.apps_packagemanagement_package_has_intalled_new);
			viewholder.mDeleteImageView = (ImageView) convertView
					.findViewById(R.id.apps_packagemanagement_delete_select);
			convertView.setTag(viewholder);
		} else {
			viewholder = (ViewHolder) convertView.getTag();
		}
		int installSize = 0;
		if (mInstallList != null && !mInstallList.isEmpty()) {
			installSize = mInstallList.size();
			if (position == 0) {
				// 首位设置为未安装包
				return createGroupView(mContext.getString(R.string.appcenter_package_uninstall));
			} else if (position > 0 && position < installSize + 1) {
				final AppPackageInfoBean bean = mInstallList.get(position - 1);
				if (bean.mIcon != null) {
					viewholder.mPackageIcon.setImageDrawable(bean.mIcon);
				} else {
					viewholder.mPackageIcon.setImageResource(android.R.drawable.sym_def_app_icon);
				}
				viewholder.mPackageName.setText(bean.mName);
				viewholder.mPackageSize.setText(bean.mSize);
				if (TextUtils.isEmpty(bean.mVersionName)) {
					viewholder.mPackageVersion.setVisibility(View.INVISIBLE);
				} else {
					viewholder.mPackageVersion.setText(mContext
							.getString(R.string.app_detail_version_tip) + bean.mVersionName);
				}

				setOperateState(bean.mState, viewholder);
				viewholder.mOperatorButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mListener != null) {
							mListener.onClick(PackageOnclickListener.OP_INSTALL, bean);
						}
					}
				});

				final RelativeLayout infoLayout = viewholder.mInfoLayout;
				viewholder.mInfoLayout.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						showPopup(infoLayout, PackageOnclickListener.OP_DELETE, bean);
					}
				});
				if (mIsDelete && mSelectedMap != null) {
					viewholder.mOperatorButton.setVisibility(View.GONE);
					viewholder.mOperatorText.setVisibility(View.GONE);
					viewholder.mDeleteImageView.setVisibility(View.VISIBLE);
					if (mSelectedMap.get(bean) != null && mSelectedMap.get(bean)) {
						viewholder.mDeleteImageView
								.setBackgroundResource(R.drawable.apps_uninstall_selected);
					} else {
						viewholder.mDeleteImageView
								.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
					}
					final ImageView imageView = viewholder.mDeleteImageView;
					viewholder.mDeleteImageView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (mSelectedMap == null || mSelectedMap.get(bean) == null) {
								return;
							}
							boolean isSelected = mSelectedMap.get(bean);
							if (!isSelected) {
								mSelectedMap.put(bean, true);
								imageView.setBackgroundResource(R.drawable.apps_uninstall_selected);
							} else {
								mSelectedMap.put(bean, false);
								imageView
										.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
							}
							if (mListener != null) {
								mListener.onClick(PackageOnclickListener.OP_SELECT, bean);
							}
						}
					});
				} else {
					viewholder.mOperatorButton.setVisibility(View.VISIBLE);
					viewholder.mOperatorText.setVisibility(View.VISIBLE);
					viewholder.mDeleteImageView.setVisibility(View.GONE);
					viewholder.mDeleteImageView.setOnClickListener(null);
				}
			}
		}
		if (mInstalledList != null && !mInstalledList.isEmpty()) {
			if (installSize == 0) {
				// 如果未安装包数为0
				if (position == 0) {
					return createGroupView(mContext.getString(R.string.appcenter_package_installed));
				} else {
					final AppPackageInfoBean bean = mInstalledList.get(position - 1);
					if (bean.mIcon != null) {
						viewholder.mPackageIcon.setImageDrawable(bean.mIcon);
					} else {
						viewholder.mPackageIcon
								.setImageResource(android.R.drawable.sym_def_app_icon);
					}
					viewholder.mPackageName.setText(bean.mName);
					viewholder.mPackageSize.setText(bean.mSize);
					if (TextUtils.isEmpty(bean.mVersionName)) {
						viewholder.mPackageVersion.setVisibility(View.INVISIBLE);
					} else {
						viewholder.mPackageVersion.setText(mContext
								.getString(R.string.app_detail_version_tip) + bean.mVersionName);
					}
					setOperateState(bean.mState, viewholder);
					viewholder.mOperatorButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (mListener != null && mSelectedMap != null) {
								mSelectedMap.remove(bean);
								mListener.onClick(PackageOnclickListener.OP_DELETE, bean);
							}
						}
					});

					final RelativeLayout infoLayout = viewholder.mInfoLayout;
					viewholder.mInfoLayout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							showPopup(infoLayout, PackageOnclickListener.OP_INSTALL, bean);
						}
					});
					if (mIsDelete && mSelectedMap != null) {
						viewholder.mOperatorButton.setVisibility(View.GONE);
						viewholder.mOperatorText.setVisibility(View.GONE);
						viewholder.mDeleteImageView.setVisibility(View.VISIBLE);
						if (mSelectedMap.get(bean) != null && mSelectedMap.get(bean)) {
							viewholder.mDeleteImageView
									.setBackgroundResource(R.drawable.apps_uninstall_selected);
						} else {
							viewholder.mDeleteImageView
									.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
						}
						final ImageView imageView = viewholder.mDeleteImageView;
						viewholder.mDeleteImageView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if (mSelectedMap == null) {
									return;
								}
								boolean isSelected = mSelectedMap.get(bean);
								if (!isSelected) {
									mSelectedMap.put(bean, true);
									imageView
											.setBackgroundResource(R.drawable.apps_uninstall_selected);
								} else {
									mSelectedMap.put(bean, false);
									imageView
											.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
								}
								if (mListener != null) {
									mListener.onClick(PackageOnclickListener.OP_SELECT, bean);
								}
							}
						});
					} else {
						viewholder.mOperatorButton.setVisibility(View.VISIBLE);
						viewholder.mOperatorText.setVisibility(View.VISIBLE);
						viewholder.mDeleteImageView.setVisibility(View.GONE);
						viewholder.mDeleteImageView.setOnClickListener(null);
					}
				}
			} else {
				// 如果未安装包数不为0
				if (position == installSize + 1) {
					return createGroupView(mContext.getString(R.string.appcenter_package_installed));
				} else if (position > installSize + 1) {
					final AppPackageInfoBean bean = mInstalledList.get(position - installSize - 2);
					if (bean.mIcon != null) {
						viewholder.mPackageIcon.setImageDrawable(bean.mIcon);
					} else {
						viewholder.mPackageIcon
								.setImageResource(android.R.drawable.sym_def_app_icon);
					}
					viewholder.mPackageName.setText(bean.mName);
					viewholder.mPackageSize.setText(bean.mSize);
					viewholder.mPackageVersion.setText(mContext
							.getString(R.string.app_detail_version_tip) + bean.mVersionName);
					setOperateState(bean.mState, viewholder);
					viewholder.mOperatorButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (mListener != null && mSelectedMap != null) {
								mSelectedMap.remove(bean);
								mListener.onClick(PackageOnclickListener.OP_DELETE, bean);
							}
						}
					});

					final RelativeLayout infoLayout = viewholder.mInfoLayout;
					viewholder.mInfoLayout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							showPopup(infoLayout, PackageOnclickListener.OP_INSTALL, bean);
						}
					});
					if (mIsDelete && mSelectedMap != null) {
						viewholder.mOperatorButton.setVisibility(View.GONE);
						viewholder.mOperatorText.setVisibility(View.GONE);
						viewholder.mDeleteImageView.setVisibility(View.VISIBLE);
						if (mSelectedMap.get(bean) != null && mSelectedMap.get(bean)) {
							viewholder.mDeleteImageView
									.setBackgroundResource(R.drawable.apps_uninstall_selected);
						} else {
							viewholder.mDeleteImageView
									.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
						}
						final ImageView imageView = viewholder.mDeleteImageView;
						viewholder.mDeleteImageView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if (mSelectedMap == null) {
									return;
								}
								boolean isSelected = mSelectedMap.get(bean);
								if (!isSelected) {
									mSelectedMap.put(bean, true);
									imageView
											.setBackgroundResource(R.drawable.apps_uninstall_selected);
								} else {
									mSelectedMap.put(bean, false);
									imageView
											.setBackgroundResource(R.drawable.apps_uninstall_not_selected);
								}
								if (mListener != null) {
									mListener.onClick(PackageOnclickListener.OP_SELECT, bean);
								}
							}
						});
					} else {
						viewholder.mOperatorButton.setVisibility(View.VISIBLE);
						viewholder.mOperatorText.setVisibility(View.VISIBLE);
						viewholder.mDeleteImageView.setVisibility(View.GONE);
						viewholder.mDeleteImageView.setOnClickListener(null);
					}
				}
			}

		}
		return convertView;
	}

	private View createGroupView(String text) {
		View view = mInflater.inflate(R.layout.recomm_appsmanagement_list_head, null);
		TextView tv = (TextView) view.findViewById(R.id.nametext);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		tv.setPadding(
				mContext.getResources().getDimensionPixelSize(R.dimen.appcenter_list_item_padding),
				mContext.getResources()
						.getDimensionPixelSize(R.dimen.download_manager_text_padding), 0, mContext
						.getResources()
						.getDimensionPixelSize(R.dimen.download_manager_text_padding));
		tv.setLayoutParams(lp);
		tv.setText(text);
		return view;
	}

	private void showPopup(View parent, int op, final AppPackageInfoBean bean) {
		if (mIsDelete) {
			return;
		}
		if (mPopup == null) {
			View view = mInflater.inflate(R.layout.apps_management_packagemanagement_popup_layout,
					null);
			mPopup = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			// 设置Popup
			mPopup.setFocusable(true);
			mPopup.setBackgroundDrawable(new ColorDrawable(0));

			mPopupUpLayout = (RelativeLayout) view
					.findViewById(R.id.app_packagemenagement_popup_up_layout);
			mUpBtn = (Button) view
					.findViewById(R.id.apps_packagemanagement_package_popup_up_operate);

			mPopupDownLayout = (RelativeLayout) view
					.findViewById(R.id.app_packagemenagement_popup_down_layout);
			mDownBtn = (Button) view
					.findViewById(R.id.apps_packagemanagement_package_popup_down_operate);
		}
		if (mPopup.isShowing()) {
			mPopup.dismiss();
		} else {
			//			mPopup.showAtLocation(parent, Gravity.TOP | Gravity.LEFT, 0, 0);
			//			mPopup.showAsDropDown(parent);
			//下面的方法是用屏幕上的绝对坐标显示，mPopupWindow  
			//我们往往不知道mPopupWindow要显示的精确位置，通常先计算页面上某个元素mView的位置，在进行偏移  

			// 设置动画效果
			mPopup.setAnimationStyle(R.style.PopupAnimation_apps_magane_up);

			//得到mView在屏幕中的坐标  
			int[] pos = new int[2];
			parent.getLocationOnScreen(pos);
			int screenHeight = getScreenHeight(mContext);
			int viewTopPos = pos[1];
			int offsetY = 0;
			if (viewTopPos < screenHeight / 2) {
				// 如果是在上半屏幕，则mPopup在组件下方
				offsetY = pos[1] + parent.getHeight() - POPUP_ARROW_HEIGHT;
				mPopupUpLayout.setVisibility(View.VISIBLE);
				mPopupDownLayout.setVisibility(View.GONE);
				mDownBtn.setOnClickListener(null);
				if (op == PackageOnclickListener.OP_INSTALL) {
					mUpBtn.setText(R.string.gostore_detail_install);
					mUpBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								mPopup.dismiss();
							} catch (Exception e) {
							}
							if (mListener != null) {
								mListener.onClick(PackageOnclickListener.OP_INSTALL, bean);
							}
						}
					});
				} else if (op == PackageOnclickListener.OP_DELETE) {
					mUpBtn.setText(R.string.delete);
					mUpBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								mPopup.dismiss();
							} catch (Exception e) {
							}
							if (mListener != null && mSelectedMap != null) {
								mSelectedMap.remove(bean);
								mListener.onClick(PackageOnclickListener.OP_DELETE, bean);
							}
						}
					});
				}
			} else {
				// 如果是在下半屏幕，则mPopup在组件上方

				// 设置动画效果
				mPopup.setAnimationStyle(R.style.PopupAnimation_apps_magane_down);

				offsetY = pos[1] - POPUP_HEIGHT + POPUP_ARROW_HEIGHT;
				mPopupUpLayout.setVisibility(View.GONE);
				mPopupDownLayout.setVisibility(View.VISIBLE);
				mUpBtn.setOnClickListener(null);
				if (op == PackageOnclickListener.OP_INSTALL) {
					mDownBtn.setText(R.string.gostore_detail_install);
					mDownBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								mPopup.dismiss();
							} catch (Exception e) {
							}
							if (mListener != null) {
								mListener.onClick(PackageOnclickListener.OP_INSTALL, bean);
							}
						}
					});
				} else if (op == PackageOnclickListener.OP_DELETE) {
					mDownBtn.setText(R.string.delete);
					mDownBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							try {
								mPopup.dismiss();
							} catch (Exception e) {
							}
							if (mListener != null && mSelectedMap != null) {
								mSelectedMap.remove(bean);
								mListener.onClick(PackageOnclickListener.OP_DELETE, bean);
							}
						}
					});
				}
			}
			mPopup.showAtLocation(parent, Gravity.TOP, 0, offsetY);
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

	private void setOperateState(int state, ViewHolder holder) {
		switch (state) {
			case AppPackageInfoBean.STATE_INSTALL :
				holder.mOperatorButton
						.setBackgroundResource(R.drawable.downloadmanager_install_selector);
				holder.mOperatorText.setText(R.string.gostore_detail_install);
				holder.mInstalledNew.setVisibility(View.GONE);
				break;
			case AppPackageInfoBean.STATE_UPDATE :
				// 更新
				holder.mOperatorButton.setBackgroundResource(R.drawable.appsgame_update_selector);
				holder.mOperatorText.setText(R.string.gostore_detail_update);
				holder.mInstalledNew.setVisibility(View.GONE);
				break;
			case AppPackageInfoBean.STATE_VERSION_LOWER :
				holder.mOperatorButton
						.setBackgroundResource(R.drawable.appgame_delete_apk_selector);
				holder.mOperatorText.setText(R.string.delete);
				holder.mInstalledNew.setVisibility(View.VISIBLE);
				break;
			case AppPackageInfoBean.STATE_INSTALLED :
				holder.mOperatorButton
						.setBackgroundResource(R.drawable.appgame_delete_apk_selector);
				holder.mOperatorText.setText(R.string.delete);
				holder.mInstalledNew.setVisibility(View.GONE);
				break;
			default :
				break;
		}
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

	public void setIsDelete(boolean isDelete) {
		if (isDelete != mIsDelete) {
			mIsDelete = isDelete;
			HashMap<AppPackageInfoBean, Boolean> map = mSelectedMap;
			Iterator<AppPackageInfoBean> it = map.keySet().iterator();
			while (it.hasNext()) {
				map.put((AppPackageInfoBean) it.next(), false);
			}
		}
	}

	public void setHashMap(HashMap<AppPackageInfoBean, Boolean> hashMap) {
		mSelectedMap = hashMap;
	}

	public HashMap<AppPackageInfoBean, Boolean> getHashMap() {
		return mSelectedMap;
	}

	public int getSelectCount() {
		if (mSelectedMap == null) {
			return 0;
		}
		int count = 0;
		for (boolean flag : mSelectedMap.values()) {
			if (flag) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 
	 * <br>类描述:viewholder类，用于加快getview速度
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-9-7]
	 */
	private class ViewHolder {
		RelativeLayout mInfoLayout;
		ImageView mPackageIcon;
		TextView mPackageName;
		TextView mPackageSize;
		TextView mPackageVersion;
		Button mOperatorButton;
		TextView mOperatorText;
		TextView mInstalledNew;
		ImageView mDeleteImageView;
	}
}
