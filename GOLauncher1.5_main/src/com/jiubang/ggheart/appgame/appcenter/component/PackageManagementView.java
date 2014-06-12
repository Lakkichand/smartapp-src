/**
 * 
 */
package com.jiubang.ggheart.appgame.appcenter.component;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.SortUtils;
import com.go.util.file.FileUtil;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.bean.AppPackageInfoBean;
import com.jiubang.ggheart.appgame.appcenter.component.PackageManagementAdapter.PackageOnclickListener;
import com.jiubang.ggheart.appgame.appcenter.help.ApkScanThread;
import com.jiubang.ggheart.appgame.appcenter.help.ApkScanThread.IApkScanListener;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.ContainerBuiler;
import com.jiubang.ggheart.appgame.base.component.IContainer;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.component.TabTipsView;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * @author liguoliang
 *
 */
public class PackageManagementView extends RelativeLayout implements OnScrollListener, IContainer {
	private Context mContext;

	private PinnedHeaderListView mListView;

	private PackageManagementAdapter mAdapter;

	/**
	 * 未安装APK包列表
	 */
	private List<AppPackageInfoBean> mInstallPackageList;

	/**
	 * 已安装APK包列表
	 */
	private List<AppPackageInfoBean> mInstalledPackageList;

	private int mPrefSize = 0;

	/**
	 * 更新大小
	 */
	private static final int REFRESH_SIZE = 3;

	private Handler mHandler;

	private static final int MSG_REFRESH_UI = 1;

	private static final int MSG_SHOW_PROGRESS = 2;

	private static final int MSG_HIDE_PROGRESS = 3;

	private static final int MSG_DELETE_ITEM = 4;
	
	private static final int MSG_SHOW_PROGRESS_TIP = 5;
	
	private static final int MSG_HIDE_PROGRESS_TIP = 6;
	
	private static final int MSG_SHOW_NO_PACKAGE = 7;
	
	private static final int MSG_SELECT_ITEM = 8;
//	
//	private static final int MSG_SCAN_FINISHED = 9;
	
	private static final int MSG_DELETE_ALL = 10;
	
	private Object mLock = new Object();

	private View mFootView;

	private ApkScanThread mScanThread;
	
	private RelativeLayout mTipsLayout;
	private TabTipsView mTipsView;
	
	private NetworkTipsTool mNetworkTipsTool;
	
	private RelativeLayout mNormalTitle;
	private RelativeLayout mDeleteTitle;
	private RelativeLayout mDeleteButtonLayout;
	private ImageButton mTitleDeleteBtn;
	private ImageView mTitleSelectBtn;
	private Button mDeleteButton;
	private Button mDeleteCancelButton;
	private TextView mDeleteText;
	private boolean mIsDelete = false;
	private boolean mIsSelectAll = false;
	private boolean mIsScanFinished = false;
	
	private HashMap<AppPackageInfoBean, Boolean> mSelectedMap = null;

	public PackageManagementView(Context context) {
		super(context);
		init(context);
	}

	public PackageManagementView(Context context, AttributeSet attrs) {
		super(context);
		init(context);
	}

	public PackageManagementView(Context context, AttributeSet attrs, int defStyle) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		this.mContext = context;
		mSelectedMap = new HashMap<AppPackageInfoBean, Boolean>();
		initHandler();
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_REFRESH_UI :
						changeDeleteTitle();
						mAdapter.updateData(mInstallPackageList, mInstalledPackageList);
						mAdapter.setHashMap(mSelectedMap);
						mListView.setAdapter(mAdapter);
						break;
					case MSG_SHOW_PROGRESS :
						showProgress();
						break;
					case MSG_HIDE_PROGRESS :
						hideProgress();
						break;
					case MSG_DELETE_ITEM :
						Toast.makeText(mContext, R.string.download_manager_delete_apk, 1000).show();
						break;
					case MSG_SHOW_PROGRESS_TIP:
						showProgressTip();
						mListView.setVisibility(View.GONE);
						break;
					case MSG_HIDE_PROGRESS_TIP:
						hideProgressTip();
						mListView.setVisibility(View.VISIBLE);
						break;
					case MSG_SHOW_NO_PACKAGE:
						showNoPackage();
						if (mTitleDeleteBtn != null) {
							mTitleDeleteBtn.setVisibility(GONE);
						}
						break;
					case MSG_SELECT_ITEM:
						changeDeleteTitle();
						break;
//					case MSG_SCAN_FINISHED:
//						if (mTitleDeleteBtn != null) {
//							mTitleDeleteBtn.setVisibility(VISIBLE);
//						}
//						break;
					case MSG_DELETE_ALL:
						mIsDelete = false;
						mNormalTitle.setVisibility(VISIBLE);
						mDeleteTitle.setVisibility(GONE);
						mDeleteButtonLayout.setVisibility(GONE);
						mAdapter.setIsDelete(mIsDelete);
						break;
					default :
						break;
				}
			}
		};
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initView();
		initData();
	}

	private void initView() {
		initTitle();
		initListHeaderView();
		initTip();
	}

	private void initTitle() {
		mNormalTitle = (RelativeLayout) this.findViewById(R.id.normal_title);
		mDeleteTitle = (RelativeLayout) this.findViewById(R.id.delete_title);
		mDeleteText = (TextView) this.findViewById(R.id.appcenter_packagemanagement_dlt_title);
		mDeleteButtonLayout = (RelativeLayout) this
				.findViewById(R.id.appcenter_packagemanagement_delete_buttom_layout);
		mDeleteButton = (Button) this
				.findViewById(R.id.appcenter_packagemanagement_delete_btn);
		mDeleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapter != null && mAdapter.getSelectCount() == 0) {
					Toast.makeText(getContext(),
							getContext().getString(R.string.download_manager_no_selected_item), 1000)
							.show();
				} else {
					View view = createDialog();
					mDialog = null;
					mDialog = new Dialog(getContext(), R.style.AppGameSettingDialog);
					mDialog.setContentView(view);
					mDialog.show();
				}
			}
		});
		mDeleteCancelButton = (Button) this
				.findViewById(R.id.appcenter_packagemanagement_delete_cancel_btn);
		mDeleteCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mIsDelete = false;
				mNormalTitle.setVisibility(VISIBLE);
				mDeleteTitle.setVisibility(GONE);
				mDeleteButtonLayout.setVisibility(GONE);
				mAdapter.setIsDelete(mIsDelete);
				Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
			}
		});
		this.findViewById(R.id.appcenter_packagemanagement_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						cleanup();
						AppsManagementActivity.sendMessage(this,
								IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
								IDiyMsgIds.REMOVE_PACKAGE_MANAGEMENT_VIEW, -1, null, null);
					}
				});
//		this.findViewById(R.id.appcenter_packagemanagement_dlt_back).setOnClickListener(
//				new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						cleanup();
//						AppsManagementActivity.sendMessage(this,
//								IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
//								IDiyMsgIds.REMOVE_PACKAGE_MANAGEMENT_VIEW, -1, null, null);
//					}
//				});
		mTitleDeleteBtn = (ImageButton) this.findViewById(R.id.appcenter_packagemanagement_title_delete_btn);
		mTitleDeleteBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapter != null) {
					if (mAdapter.getHashMap() == null || mAdapter.getHashMap().isEmpty()) {
						return;
					}
					if (!mIsDelete) {
						mIsDelete = true;
						mNormalTitle.setVisibility(GONE);
						mDeleteTitle.setVisibility(VISIBLE);
						mDeleteButtonLayout.setVisibility(VISIBLE);
						mAdapter.setIsDelete(mIsDelete);
						Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
					}
				}
			}
		});
		mTitleSelectBtn = (ImageView) this.findViewById(R.id.appcenter_packagemanagement_title_select_btn);
		mTitleSelectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsDelete) {
					boolean isSelectAll = true;
					if (mAdapter != null) {
						HashMap<AppPackageInfoBean, Boolean> map = mAdapter.getHashMap();
						for (Boolean flag : map.values()) {
							if (!flag) {
								isSelectAll = false;
								break;
							}
						}
						if (isSelectAll) {
							Iterator<AppPackageInfoBean> it = map.keySet().iterator();
							while (it.hasNext()) {
								map.put((AppPackageInfoBean) it.next(), false);
							}
							mIsSelectAll = false;
							mTitleSelectBtn.setImageResource(R.drawable.apps_uninstall_not_selected);
						} else {
							Iterator<AppPackageInfoBean> it = map.keySet().iterator();
							while (it.hasNext()) {
								map.put((AppPackageInfoBean) it.next(), true);
							}
							mIsSelectAll = true;
							mTitleSelectBtn.setImageResource(R.drawable.apps_uninstall_selected);
						}
						mAdapter.setHashMap(map);
						Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
					}
				}
			}
		});
		
		this.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && mIsDelete) {
					mIsDelete = false;
					mNormalTitle.setVisibility(VISIBLE);
					mDeleteTitle.setVisibility(GONE);
					mDeleteButtonLayout.setVisibility(GONE);
					mAdapter.setIsDelete(mIsDelete);
					Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
					return true;
				}
				return false;
			}
		});
	}
	
	private Dialog mDialog = null;
	private View createDialog() {
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.appgame_downloadmanager_delete_dialog, null);
		RelativeLayout relativeLayout = (RelativeLayout) view
				.findViewById(R.id.appgame_download_manager_delete_select);
		TextView textview = (TextView) view.findViewById(R.id.appgame_download_manager_tip);

		String leftTip = getContext().getString(R.string.download_manager_dialog_tips_left);
		String rightTip = getContext().getString(R.string.appcenter_package);
		int selectedCount = mAdapter.getSelectCount();
		if (selectedCount > 1) {
			rightTip = getContext().getString(R.string.appcenter_packages);
		}
		Spanned str = Html.fromHtml("<font color=#202020>" + leftTip + "</font>"
				+ "<font color=#FF0000>" + selectedCount + "</font>"
				+ "<font color=#202020>" + rightTip + "</font>");
		textview.setText(str);
		relativeLayout.setOnClickListener(null);
		relativeLayout.setVisibility(GONE);
		Button button = (Button) view.findViewById(R.id.appgame_download_delete_dialog_ok);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAdapter != null && mAdapter.getHashMap() != null) {
					HashMap<AppPackageInfoBean, Boolean> map = mAdapter.getHashMap();
					AppPackageInfoBean bean = null;
					Iterator it = map.entrySet().iterator();
					while (it.hasNext()) {
						Entry entry = (Entry) it.next();
						if ((Boolean) entry.getValue()) {
							bean = (AppPackageInfoBean) entry.getKey();
							it.remove();
							FileUtil.deleteFile(bean.mFilePath);
							synchronized (mLock) {
								if (bean.mState == AppPackageInfoBean.STATE_INSTALL
										|| bean.mState == AppPackageInfoBean.STATE_UPDATE) {
									mInstallPackageList.remove(bean);
								} else if (bean.mState == AppPackageInfoBean.STATE_INSTALLED
										|| bean.mState == AppPackageInfoBean.STATE_VERSION_LOWER) {
									mInstalledPackageList.remove(bean);
								}
							}
							if (mIsSelectAll && mIsScanFinished) {
								mIsSelectAll = false;
								Message.obtain(mHandler, MSG_DELETE_ALL).sendToTarget();
								Message.obtain(mHandler, MSG_SHOW_NO_PACKAGE).sendToTarget();
							}
							Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
						}
					}
					Message.obtain(mHandler, MSG_DELETE_ITEM).sendToTarget();
					StatisticsData.countStatData(mContext, StatisticsData.KEY_DELETE_ALL);
				}
				mDialog.dismiss();
			}
		});
		Button cancelBtn = (Button) view.findViewById(R.id.appgame_download_delete_dialog_cancel);
		cancelBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
		return view;
	}
	
	public void changeDeleteTitle() {
		if (mAdapter == null || (mAdapter != null && mAdapter.getSelectCount() == 0)) {
			mDeleteButton.setBackgroundResource(R.drawable.yjzi_btn_disable);
			mDeleteButton.setTextColor(getContext().getResources().getColor(
					R.color.appgame_download_btn_black));
			mDeleteButton.setOnClickListener(null);
			mDeleteText.setText(R.string.select_group_applications);
			// 可勾选项数为0，所以全选按钮为没有勾选的状态
			mTitleSelectBtn.setImageResource(R.drawable.apps_uninstall_not_selected);
		} else {
			mDeleteButton.setBackgroundResource(R.drawable.yzjz_white_button);
			mDeleteButton.setTextColor(getContext().getResources().getColor(
					R.color.downloadmanager_text_red));
			mDeleteButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mAdapter != null && mAdapter.getSelectCount() == 0) {
						Toast.makeText(getContext(),
								getContext().getString(R.string.download_manager_no_selected_item),
								1000).show();
					} else {
						View view = createDialog();
						mDialog = null;
						mDialog = new Dialog(getContext(), R.style.AppGameSettingDialog);
						mDialog.setContentView(view);
						mDialog.show();
					}

				}
			});
			int count = mAdapter.getSelectCount();
			String result = String.format(getContext().getString(R.string.selected_apps_result),
					count);
			mDeleteText.setText(result);
			if (mAdapter.getHashMap().size() == count && count != 0) {
				mTitleSelectBtn.setImageResource(R.drawable.apps_uninstall_selected);
			} else {
				mTitleSelectBtn.setImageResource(R.drawable.apps_uninstall_not_selected);
			}
		}
	}
	
	private void initTip() {
		mTipsLayout = (RelativeLayout) this.findViewById(R.id.appcenter_packagemanagement_tip);
		
		// 提示语进度条
		mTipsView = new TabTipsView(mContext);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mTipsLayout.addView(mTipsView, params);
		
		// 无SD卡提示
		mNetworkTipsTool = new NetworkTipsTool(mTipsLayout);
	}

	private void initListHeaderView() {
		mListView = (PinnedHeaderListView) this
				.findViewById(R.id.appcenter_packagemanagement_listView);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		View view = inflater.inflate(R.layout.recomm_appsmanagement_list_head, mListView, false);
		TextView tv = (TextView) view.findViewById(R.id.nametext);
		//对显示的文字做margin的设置
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		//		lp.setMargins(
		//				getContext().getResources().getDimensionPixelSize(
		//						R.dimen.download_manager_text_margin), 0, 0, 0);		
		tv.setPadding(
				getContext().getResources().getDimensionPixelSize(
						R.dimen.appcenter_list_item_padding),
				getContext().getResources().getDimensionPixelSize(
						R.dimen.download_manager_text_padding), 0, getContext().getResources()
						.getDimensionPixelSize(R.dimen.download_manager_text_padding));
		tv.setLayoutParams(lp);
//		int paddingLeft = getContext().getResources().getDimensionPixelSize(
//				R.dimen.appcenter_list_item_padding);
//		mListView.setPaddingLeft(paddingLeft);
		mListView.setPinnedHeaderView(view);
		mListView.setOnScrollListener(this);

		mFootView = inflater.inflate(R.layout.apps_mgr_listview_foot_more, null);
		mFootView.setVisibility(View.GONE);
		mListView.addFooterView(mFootView);
	}

	private void initData() {
		mAdapter = new PackageManagementAdapter(mContext);
		mAdapter.setOnClickLisener(new PackageOnclickListener() {

			@Override
			public void onClick(int op, AppPackageInfoBean bean) {
				operate(op, bean);
			}
		});
		mListView.setAdapter(mAdapter);
		startScan();
	}

	private void startScan() {
		if (!FileUtil.isSDCardAvaiable()) {
			showNoSDCard();
			mListView.setVisibility(View.GONE);
			return ;
		}
		if (mScanThread != null) {
			return;
		}
		
		// 先清空旧数据
		if (mInstalledPackageList != null) {
			mInstalledPackageList.clear();
		}
		if (mInstallPackageList != null) {
			mInstallPackageList.clear();
		}
		
		if (mSelectedMap != null) {
			mSelectedMap.clear();
		}
		
		mScanThread = new ApkScanThread(mContext);
		IApkScanListener listener = new IApkScanListener() {

			@Override
			public void onStart() {
				Message.obtain(mHandler, MSG_SHOW_PROGRESS_TIP).sendToTarget();
//				Message.obtain(mHandler, MSG_SCAN_BEGIN).sendToTarget();
			}

			@Override
			public void onProgress(AppPackageInfoBean infoBean, File file) {
				
				int state = infoBean.mState;
				synchronized (mLock) {
					if (mIsSelectAll) {
						mSelectedMap.put(infoBean, true);
					} else {
						mSelectedMap.put(infoBean, false);
					}
					if (state == AppPackageInfoBean.STATE_INSTALLED
							|| state == AppPackageInfoBean.STATE_VERSION_LOWER) {
						// 添加到已安装列表中
						if (mInstalledPackageList == null) {
							mInstalledPackageList = new ArrayList<AppPackageInfoBean>();
						}
						mInstalledPackageList.add(infoBean);
					} else if (state == AppPackageInfoBean.STATE_UPDATE
							|| state == AppPackageInfoBean.STATE_INSTALL) {
						// 添加到未安装列表中
						if (mInstallPackageList == null) {
							mInstallPackageList = new ArrayList<AppPackageInfoBean>();
						}
						mInstallPackageList.add(infoBean);
					}
				}				
				int size = getSize();
				if (getSize() == 1) {
					Message.obtain(mHandler, MSG_HIDE_PROGRESS_TIP).sendToTarget();
					Message.obtain(mHandler, MSG_SHOW_PROGRESS).sendToTarget();
					Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
				} else if (size < 10) {
					// 头10个依次加载防止首页出现跳跃效果
					Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
					mPrefSize = size;
				} else {
					// 更新大于3时刷新
					if (size - mPrefSize > REFRESH_SIZE) {
						Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
						mPrefSize = size;
					}
				}
			}

			@Override
			public void onFinish() {
				if (getSize() == 0) {
					// 如果数据为0
					Message.obtain(mHandler, MSG_HIDE_PROGRESS_TIP).sendToTarget();
					Message.obtain(mHandler, MSG_HIDE_PROGRESS).sendToTarget();
					Message.obtain(mHandler, MSG_SHOW_NO_PACKAGE).sendToTarget();
					mScanThread = null;
					return ;
				}
				synchronized (mLock) {
					// 完成后做排序
					if (mInstallPackageList != null) {
						Collections.sort(mInstallPackageList, mComparator);
					} 
					if (mInstalledPackageList != null) {
						Collections.sort(mInstalledPackageList, mComparator);
					}
				}					
				Message.obtain(mHandler, MSG_HIDE_PROGRESS).sendToTarget();
				Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();		
//				Message.obtain(mHandler, MSG_SCAN_FINISHED).sendToTarget();
				mIsScanFinished = true;
				mScanThread = null;
			}
		};
		mScanThread.setListener(listener);
		mScanThread.start();
	}

	private void operate(int op, AppPackageInfoBean bean) {
		if (TextUtils.isEmpty(bean.mFilePath)) {
			return ;
		}
		if (op == PackageOnclickListener.OP_INSTALL) {
			ApkInstallUtils.installApk(bean.mFilePath);
		} else if (op == PackageOnclickListener.OP_DELETE) {			
			FileUtil.deleteFile(bean.mFilePath);
			synchronized (mLock) {
				if (bean.mState == AppPackageInfoBean.STATE_INSTALL || bean.mState == AppPackageInfoBean.STATE_UPDATE) {
					mInstallPackageList.remove(bean);
				} else if (bean.mState == AppPackageInfoBean.STATE_INSTALLED || bean.mState == AppPackageInfoBean.STATE_VERSION_LOWER) {
					mInstalledPackageList.remove(bean);
				}
			}					
			mSelectedMap = mAdapter.getHashMap();
			Message.obtain(mHandler, MSG_DELETE_ITEM).sendToTarget();
			Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
		} else if (op == PackageOnclickListener.OP_SELECT) {
			if (mIsSelectAll) {
				mIsSelectAll = false;
				mSelectedMap = mAdapter.getHashMap();
				Message.obtain(mHandler, MSG_SELECT_ITEM).sendToTarget();
			} else {
				mSelectedMap = mAdapter.getHashMap();
				Message.obtain(mHandler, MSG_SELECT_ITEM).sendToTarget();
			}
		}
	}

	private Comparator<AppPackageInfoBean> mComparator = new Comparator<AppPackageInfoBean>() {

		@Override
		public int compare(AppPackageInfoBean object1, AppPackageInfoBean object2) {
			int result = 0;
			String str1 = object1.mName;
			String str2 = object2.mName;
			str1 = SortUtils.changeChineseToSpell(getContext(), str1);
			str2 = SortUtils.changeChineseToSpell(getContext(), str2);
			Collator collator = null;
			if (Build.VERSION.SDK_INT < 16) {
				collator = Collator.getInstance(Locale.CHINESE);
			} else {
				collator = Collator.getInstance(Locale.ENGLISH);
			}

			if (collator == null) {
				collator = Collator.getInstance(Locale.getDefault());
			}
			result = collator.compare(str1.toUpperCase(), str2.toUpperCase());
			return result;
		}
	};

	private int getSize() {
		int size = 0;
		if (mInstalledPackageList != null) {
			size += mInstalledPackageList.size();
		}
		if (mInstallPackageList != null) {
			size += mInstallPackageList.size();
		}
		return size;
	}

	private void showProgress() {	
		if (mFootView != null) {
			mFootView.setVisibility(View.VISIBLE);
			mFootView.findViewById(R.id.apps_mgr_listview_foot_loading).setVisibility(View.VISIBLE);
			((TextView) mFootView.findViewById(R.id.themestore_btmProgress_text))
					.setText(R.string.appcenter_package_scan);			
		}
	}	

	private void hideProgress() {
		if (mFootView != null) {
			mFootView.findViewById(R.id.apps_mgr_listview_foot_loading).setVisibility(View.GONE);
			mFootView.findViewById(R.id.apps_mgr_listview_foot_retry).setVisibility(View.GONE);
			mFootView.findViewById(R.id.apps_mgr_listview_foot_end).setVisibility(View.GONE);
			mFootView.setVisibility(View.GONE);
		}
	}
	
	private void showProgressTip() {
		if (mTipsLayout != null && mTipsView != null) {
			mTipsLayout.setVisibility(View.VISIBLE);			
			mTipsView.showProgress(null);			
		}
	}
	
	private void hideProgressTip() {
		if (mTipsLayout != null && mTipsView != null) {
			mTipsView.removeProgress();
			mTipsLayout.setVisibility(View.GONE);			
		}
	}
	
	private void showNoSDCard() {
		if (mTipsLayout != null && mNetworkTipsTool != null) {
			mTipsLayout.setVisibility(View.VISIBLE);			
			mNetworkTipsTool.showRetryErrorTip(null, false);		
		}
	}
	
	private void hideNoSDcard() {
		if (mTipsLayout != null && mNetworkTipsTool != null) {
			mTipsLayout.setVisibility(View.GONE);			
			mNetworkTipsTool.dismissTip();		
		}
	}
	
	private void showNoPackage() {
		if (mTipsLayout != null) {
			TextView tv = new TextView(mContext);
			tv.setTextColor(0xff404040);
			tv.setTextSize(DrawUtils.dip2px(12.0f));
			tv.setText(R.string.appcenter_package_no_package_tip);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);			
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			mTipsLayout.addView(tv, params);
			mListView.setVisibility(View.GONE);
			mTipsLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			cleanup();
			AppsManagementActivity.sendMessage(this, IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
					IDiyMsgIds.REMOVE_PACKAGE_MANAGEMENT_VIEW, -1, null, null);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
		}
	}

	@Override
	public void cleanup() {
		if (mScanThread != null) {
			mScanThread.stopScan();
			mScanThread = null;
		}
	}

	@Override
	public void sdCardTurnOff() {
		if (mScanThread != null) {
			mScanThread.stopScan();
			mScanThread = null;
		}
		mListView.setVisibility(View.GONE);
		if (mTitleDeleteBtn != null) {
			mTitleDeleteBtn.setVisibility(GONE);
		}
		showNoSDCard();
	}

	@Override
	public void sdCardTurnOn() {
		hideNoSDcard();
		mListView.setVisibility(View.VISIBLE);
		startScan();
	}

	@Override
	public void onActiveChange(boolean isActive) {

	}

	@Override
	public boolean onPrepareOptionsMenu(AppGameMenu menu) {
		return false;
	}

	@Override
	public boolean onOptionItemSelected(int id) {
		return false;
	}

	@Override
	public void onResume() {

	}

	@Override
	public void onStop() {

	}

	@Override
	public void onAppAction(String packName, int appAction) {
		if (TextUtils.isEmpty(packName)) {
			return;
		}
		if (mInstallPackageList != null
				&& (appAction == MainViewGroup.FLAG_INSTALL || appAction == MainViewGroup.FLAG_UPDATE)) {
			AppPackageInfoBean temp = null;
			synchronized (mLock) {
				// 如果有新应用安装
				for (AppPackageInfoBean bean : mInstallPackageList) {
					if (packName.equals(bean.mPackageName)) {
						temp = bean;
						break;
					}
				}
				if (temp != null) {
					// 从未安装列表中移除，并添加到已安装列表中
					mInstallPackageList.remove(temp);
					temp.mState = AppPackageInfoBean.STATE_INSTALLED;
					if (mInstalledPackageList == null) {
						mInstalledPackageList = new ArrayList<AppPackageInfoBean>();
					}
					mInstalledPackageList.add(temp);
					// 将已安装列表重新排序，未安装列表不需要重新排序
					Collections.sort(mInstalledPackageList, mComparator);
					Message.obtain(mHandler, MSG_REFRESH_UI).sendToTarget();
				}
			}
		}
	}
	
	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {

	}

	@Override
	public void initEntrance(int access) {

	}

	@Override
	public int getTypeId() {
		return 0;
	}

	@Override
	public void onFinishAllUpdateContent() {

	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {

	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {

	}

	@Override
	public void onTrafficSavingModeChange() {

	}

	@Override
	public void setUpdateData(Object value, int state) {

	}

	@Override
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans, List<IContainer> containers) {
		// do nothing
		
	}

	@Override
	public void removeContainers() {
		// do nothing
		
	}

	@Override
	public List<IContainer> getSubContainers() {
		// do nothing
		return null;
	}

	@Override
	public void onMultiVisiableChange(boolean visiable) {
		// do nothing
		
	}
	
	@Override
	public void prevLoading() {
		//do nothing
	}

	@Override
	public void prevLoadFinish() {
		//do nothing
	}
	
	@Override
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}
}
