/*
 * 文 件 名:  DownloadControllViewAdapter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-9-5
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.downloadmanager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.appcenter.component.AppsSectionIndexer;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView.PinnedHeaderAdapter;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.download.IDownloadService;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.component.SimpleImageView;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-9-5]
 */
public class DownloadControllViewAdapter extends BaseAdapter implements PinnedHeaderAdapter {

	private LayoutInflater mInflater = null;

	//正在下载的任务列表
	private ArrayList<DownloadTask> mDownloadingList = new ArrayList<DownloadTask>();

	//已经下载完成的任务列表
	private ArrayList<DownloadTask> mDownloadedList = new ArrayList<DownloadTask>();

	//已经安装的下载任务列表
	private ArrayList<DownloadTask> mInstalledList = new ArrayList<DownloadTask>();

	private AppsSectionIndexer mIndexer;

	private AsyncImageManager mImgManager = null;

	private IDownloadService mDownloadController = null;

	private String mPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;

	private Context mContext = null;

	private static final int TYPE_GROUP = 0;

	private static final int TYPE_INFO = 1;

	private static final int TYPE_COUNT = 2;

	public DownloadControllViewAdapter(Context context, ArrayList<DownloadTask> downloadingList,
			ArrayList<DownloadTask> downloadedList, ArrayList<DownloadTask> installedList) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mImgManager = AsyncImageManager.getInstance();
		mDownloadingList = (ArrayList<DownloadTask>) downloadingList.clone();
		mDownloadedList = (ArrayList<DownloadTask>) downloadedList.clone();
		mInstalledList = (ArrayList<DownloadTask>) installedList.clone();

		//把分类组也做成downloadTask,分别加入三个列表的第0位
		DownloadTask dt1 = new DownloadTask("",
				mContext.getString(R.string.download_manager_group_downloading_item), "");
		DownloadTask dt2 = new DownloadTask("",
				mContext.getString(R.string.download_manager_group_downloaded_item), "");
		DownloadTask dt3 = new DownloadTask("",
				mContext.getString(R.string.download_manager_group_installed_item), "");

		ArrayList<String> stringDivider = new ArrayList<String>();
		ArrayList<Integer> intDivider = new ArrayList<Integer>();
		if (mDownloadingList.size() > 0) {
			mDownloadingList.add(0, dt1);
			stringDivider.add(mContext.getString(R.string.download_manager_group_downloading_item)
					+ "(" + downloadingList.size() + ")");
			intDivider.add(mDownloadingList.size());
		}
		if (mDownloadedList.size() > 0) {
			mDownloadedList.add(0, dt2);
			stringDivider.add(mContext.getString(R.string.download_manager_group_downloaded_item)
					+ "(" + downloadedList.size() + ")");
			intDivider.add(mDownloadedList.size());
		}
		if (mInstalledList.size() > 0) {
			mInstalledList.add(0, dt3);
			stringDivider.add(mContext.getString(R.string.download_manager_group_installed_item)
					+ "(" + installedList.size() + ")");
			intDivider.add(mInstalledList.size());
		}
		String[] sections = stringDivider.toArray(new String[stringDivider.size()]);
		int[] counts = new int[intDivider.size()];
		for (int i = 0; i < intDivider.size(); i++) {
			counts[i] = intDivider.get(i).intValue();
		}
		mIndexer = new AppsSectionIndexer(sections, counts);
	}

	public void setDownloadController(IDownloadService downloadController) {
		mDownloadController = downloadController;
	}

	public void updateList(ArrayList<DownloadTask> downloadingList,
			ArrayList<DownloadTask> downloadedList, ArrayList<DownloadTask> installedList) {
		//正在下载列表清空
		mDownloadingList.clear();
		//已经下载列表清空
		mDownloadedList.clear();
		//下载且安装列表清空
		mInstalledList.clear();

		mDownloadingList = (ArrayList<DownloadTask>) downloadingList.clone();
		mDownloadedList = (ArrayList<DownloadTask>) downloadedList.clone();
		mInstalledList = (ArrayList<DownloadTask>) installedList.clone();
		//把分类组也做成downloadTask
		DownloadTask dt1 = new DownloadTask("",
				mContext.getString(R.string.download_manager_group_downloading_item), "");
		DownloadTask dt2 = new DownloadTask("",
				mContext.getString(R.string.download_manager_group_downloaded_item), "");
		DownloadTask dt3 = new DownloadTask("",
				mContext.getString(R.string.download_manager_group_installed_item), "");

		ArrayList<String> stringDivider = new ArrayList<String>();
		ArrayList<Integer> intDivider = new ArrayList<Integer>();
		if (mDownloadingList.size() > 0) {
			mDownloadingList.add(0, dt1);
			stringDivider.add(mContext.getString(R.string.download_manager_group_downloading_item)
					+ "(" + downloadingList.size() + ")");
			intDivider.add(mDownloadingList.size());
		}
		if (mDownloadedList.size() > 0) {
			mDownloadedList.add(0, dt2);
			stringDivider.add(mContext.getString(R.string.download_manager_group_downloaded_item)
					+ "(" + downloadedList.size() + ")");
			intDivider.add(mDownloadedList.size());
		}
		if (mInstalledList.size() > 0) {
			mInstalledList.add(0, dt3);
			stringDivider.add(mContext.getString(R.string.download_manager_group_installed_item)
					+ "(" + installedList.size() + ")");
			intDivider.add(mInstalledList.size());
		}
		String[] sections = stringDivider.toArray(new String[stringDivider.size()]);
		int[] counts = new int[intDivider.size()];
		for (int i = 0; i < intDivider.size(); i++) {
			counts[i] = intDivider.get(i).intValue();
		}
		mIndexer = new AppsSectionIndexer(sections, counts);
		notifyDataSetChanged();
	}

	@Override
	public boolean isEnabled(int position) {
		if (getItemViewType(position) == TYPE_GROUP) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		if (position >= 0 && position < mDownloadingList.size() && mDownloadingList.size() != 0) {
			if (position == 0) {
				return TYPE_GROUP;
			} else {
				return TYPE_INFO;
			}
		} else if (mDownloadedList != null
				&& (position - mDownloadingList.size()) < mDownloadedList.size()
				&& (position - mDownloadingList.size()) >= 0 && mDownloadedList.size() != 0) {
			int pos = position - mDownloadingList.size();
			if (pos == 0) {
				return TYPE_GROUP;
			} else {
				return TYPE_INFO;
			}
		} else if (mInstalledList != null
				&& (position - mDownloadingList.size() - mDownloadedList.size()) < mInstalledList
						.size()
				&& (position - mDownloadingList.size() - mDownloadedList.size()) >= 0
				&& mInstalledList.size() != 0) {
			int pos = position - mDownloadingList.size() - mDownloadedList.size();
			if (pos == 0) {
				return TYPE_GROUP;
			} else {
				return TYPE_INFO;
			}
		} else {
			return TYPE_GROUP;
		}
	}

	/** {@inheritDoc} */

	@Override
	public int getCount() {
		int count = 0;
		if (mDownloadingList != null) {
			count += mDownloadingList.size();
		}
		if (mDownloadedList != null) {
			count += mDownloadedList.size();
		}
		if (mInstalledList != null) {
			count += mInstalledList.size();
		}
		return count;
	}

	/** {@inheritDoc} */

	@Override
	public Object getItem(int position) {
		if (position < mDownloadingList.size() && mDownloadingList.size() != 0) {
			return mDownloadingList.get(position);
		} else if (position >= mDownloadingList.size()
				&& (position < mDownloadingList.size() + mDownloadedList.size())
				&& mDownloadedList.size() != 0) {
			int pos = position - mDownloadingList.size();
			return mDownloadedList.get(pos);
		} else if ((position >= mDownloadingList.size() + mDownloadedList.size())
				&& (position < mDownloadingList.size() + mDownloadedList.size()
						+ mInstalledList.size()) && mInstalledList.size() != 0) {
			int pos = position - mDownloadingList.size() - mDownloadedList.size();
			return mInstalledList.get(pos);
		}
		return null;
	}

	/** {@inheritDoc} */

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public void notifyDataSetChanged() {
		// TODO Auto-generated method stub
		super.notifyDataSetChanged();
	}

	/** {@inheritDoc} */

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewholder = null;
		int type = getItemViewType(position);
		if (convertView == null) {
			viewholder = new ViewHolder();
			if (type == TYPE_INFO) {
				convertView = mInflater.inflate(R.layout.layout_apps_download_item, null);
				viewholder.mDownloadIcon = (SimpleImageView) convertView
						.findViewById(R.id.download_icon);
				viewholder.mDownloadName = (TextView) convertView.findViewById(R.id.download_name);
				viewholder.mDownloadSize = (TextView) convertView.findViewById(R.id.download_size);
				viewholder.mDownloadPercent = (TextView) convertView
						.findViewById(R.id.download_percent);
				viewholder.mDownloadProgressbar = (ProgressBar) convertView
						.findViewById(R.id.download_progressbar);
				viewholder.mDownloadFinish = (TextView) convertView
						.findViewById(R.id.download_finish);
				viewholder.mDownloadButton = (Button) convertView
						.findViewById(R.id.app_download_manager_button);
				viewholder.mDownloadText = (TextView) convertView
						.findViewById(R.id.app_download_manager_text);
				viewholder.mDownloadLayout = (RelativeLayout) convertView
						.findViewById(R.id.app_download_manager_download_relativelayout);
				convertView.setTag(viewholder);
			} else {
				convertView = mInflater.inflate(R.layout.recomm_appsmanagement_list_head, null);
				viewholder.mDownloadName = (TextView) convertView.findViewById(R.id.nametext);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.FILL_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				int padding = mContext.getResources().getDimensionPixelSize(
						R.dimen.download_manager_text_padding);
				viewholder.mDownloadName.setPadding(padding * 3, padding, 0, padding);
				viewholder.mDownloadName.setLayoutParams(lp);
				convertView.setTag(viewholder);
			}
		} else {
			viewholder = (ViewHolder) convertView.getTag();
		}
		final ViewHolder finalViewholder = viewholder;
		// "正在下载"分类
		if (position >= 0 && position < mDownloadingList.size() && mDownloadingList.size() != 0) {
			DownloadTask task = mDownloadingList.get(position);
			if (position == 0) {
				//分组头
				int count = mDownloadingList.size() - 1;
				viewholder.mDownloadName.setText(task.getDownloadName() + "(" + count + ")");
			} else {
				viewholder.setDownloadingStyle();
				//设置图标
				if (task.getIconType() == DownloadTask.ICON_TYPE_URL) {
					if (!TextUtils.isEmpty(task.getIconInfo())) {
						// 给出url地址
						String imgName = String.valueOf(task.getIconInfo().hashCode());
						setIcon(position, viewholder.mDownloadIcon, task.getIconInfo(), mPath,
								imgName, true);
					}
				} else if (task.getIconType() == DownloadTask.ICON_TYPE_ID) {
					// 给出图标ID
					viewholder.mDownloadIcon.clearIcon();
					viewholder.mDownloadIcon.setImgId(task.getIconInfo());
				} else if (task.getIconType() == DownloadTask.ICON_TYPE_LOCAL) {
					// 本地图标
					viewholder.mDownloadIcon.setTag(task.getDownloadApkPkgName());
					Bitmap bmp = mImgManager.loadImageIcon(mContext, task.getDownloadApkPkgName(),
							true, new AsyncImageLoadedCallBack() {
								@Override
								public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
									if (finalViewholder.mDownloadIcon != null
											&& finalViewholder.mDownloadIcon.getTag()
													.equals(imgUrl)) {
										finalViewholder.mDownloadIcon.setImageBitmap(imageBitmap);
									} else {
										imageBitmap = null;
									}
								}
							});
					if (bmp != null) {
						viewholder.mDownloadIcon.setImageBitmap(bmp);
					}
				} else {
					// 默认图标
					viewholder.mDownloadIcon.setImageResource(R.drawable.default_icon);
				}
				// 设置软件名字
				viewholder.mDownloadName.setText(task.getDownloadName());
				// 设置软件大小
				viewholder.mDownloadSize.setText(getAlreadyDownloadSize(task
						.getAlreadyDownloadSize())
						+ "/"
						+ getAlreadyDownloadSize(task.getTotalSize()));
				// 设置进度条progressBar的进度
				viewholder.mDownloadProgressbar.setProgress(task.getAlreadyDownloadPercent());
				// 设置按钮和进度或者任务状态
				setButtonState(task, viewholder.mDownloadPercent, viewholder.mDownloadButton,
						viewholder.mDownloadText);
				// 按钮点击事件处理
//				final long taskId = task.getId();
//				final int pos = position;
				// R.id.app_download_manager_button作为KEY，存储id
				viewholder.mDownloadButton.setTag(R.id.app_download_manager_button, task.getId());
				viewholder.mDownloadLayout.setTag(R.id.app_download_manager_button, task.getId());
				// R.id.app_download_manager_download_relativelayout作为KEY，存储一个holder
				viewholder.mDownloadButton.setTag(R.id.app_download_manager_download_relativelayout, viewholder);
				viewholder.mDownloadLayout.setTag(R.id.app_download_manager_download_relativelayout, viewholder);
				// R.id.app_download_manager_text作为KEY，存储position
				viewholder.mDownloadButton.setTag(R.id.app_download_manager_text, position);
				viewholder.mDownloadLayout.setTag(R.id.app_download_manager_text, position);
				// 添加点击响应方法
				viewholder.mDownloadButton.setOnClickListener(mControllerListener);
				viewholder.mDownloadLayout.setOnClickListener(mControllerListener);
			}
		} else if (mDownloadedList != null
				&& (position - mDownloadingList.size()) < mDownloadedList.size()
				&& (position - mDownloadingList.size()) >= 0 && mDownloadedList.size() != 0) {
			// "已下载但未安装“分类
			final int pos = position - mDownloadingList.size();
			DownloadTask task = mDownloadedList.get(pos);
			if (pos == 0) {
				// "已经下载项"的分类头
				int count = mDownloadedList.size() - 1;
				viewholder.mDownloadName.setText(task.getDownloadName() + "(" + count + ")");
			} else {
				viewholder.setDownloadedStyle();
				//设置图标
				if (task.getIconType() == DownloadTask.ICON_TYPE_URL) {
					if (!TextUtils.isEmpty(task.getIconInfo())) {
						// 给出url地址
						String imgName = String.valueOf(task.getIconInfo().hashCode());
						setIcon(position, viewholder.mDownloadIcon, task.getIconInfo(), mPath,
								imgName, true);
					}
				} else if (task.getIconType() == DownloadTask.ICON_TYPE_ID) {
					// 给出图标ID
					viewholder.mDownloadIcon.clearIcon();
					viewholder.mDownloadIcon.setImgId(task.getIconInfo());
				} else if (task.getIconType() == DownloadTask.ICON_TYPE_LOCAL) {
					// 本地图标
					viewholder.mDownloadIcon.setTag(task.getDownloadApkPkgName());
					Bitmap bmp = mImgManager.loadImageIcon(mContext, task.getDownloadApkPkgName(),
							true, new AsyncImageLoadedCallBack() {
								@Override
								public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
									if (finalViewholder.mDownloadIcon != null
											&& finalViewholder.mDownloadIcon.getTag()
													.equals(imgUrl)) {
										finalViewholder.mDownloadIcon.setImageBitmap(imageBitmap);
									} else {
										imageBitmap = null;
									}
								}
							});
					if (bmp != null) {
						viewholder.mDownloadIcon.setImageBitmap(bmp);
					}
				} else {
					// 默认图标
					viewholder.mDownloadIcon.setImageResource(R.drawable.default_icon);
				}
				// 设置软件名字
				viewholder.mDownloadName.setText(task.getDownloadName());
				viewholder.mDownloadFinish.setText(getAlreadyDownloadSize(task.getTotalSize()));
				viewholder.mDownloadButton
						.setBackgroundResource(R.drawable.appgame_install_selector);
				viewholder.mDownloadText.setText(R.string.appgame_install);
//				final String savePath = task.getSaveFilePath();
				viewholder.mDownloadButton.setTag(R.id.app_download_manager_button, task.getSaveFilePath());
				viewholder.mDownloadLayout.setTag(R.id.app_download_manager_button, task.getSaveFilePath());
				viewholder.mDownloadButton.setOnClickListener(mInstallLOnClickListener);
				viewholder.mDownloadLayout.setOnClickListener(mInstallLOnClickListener);
			}
		} else if (mInstalledList != null
				&& (position - mDownloadingList.size() - mDownloadedList.size()) < mInstalledList
						.size()
				&& (position - mDownloadingList.size() - mDownloadedList.size()) >= 0
				&& mInstalledList.size() != 0) {
			// "已下载且安装"分类
			final int pos = position - mDownloadingList.size() - mDownloadedList.size();
			DownloadTask task = mInstalledList.get(pos);
			if (pos == 0) {
				// 分组头
				int count = mInstalledList.size() - 1;
				viewholder.mDownloadName.setText(task.getDownloadName() + "(" + count + ")");
			} else {
				viewholder.setDownloadedStyle();
				//设置图标
				if (task.getIconType() == DownloadTask.ICON_TYPE_URL) {
					if (!TextUtils.isEmpty(task.getIconInfo())) {
						// 给出url地址
						String imgName = String.valueOf(task.getIconInfo().hashCode());
						setIcon(position, viewholder.mDownloadIcon, task.getIconInfo(), mPath,
								imgName, true);
					}
				} else if (task.getIconType() == DownloadTask.ICON_TYPE_ID) {
					// 给出图标ID
					viewholder.mDownloadIcon.setImgId(task.getIconInfo());
				} else if (task.getIconType() == DownloadTask.ICON_TYPE_LOCAL) {
					// 本地图标
					viewholder.mDownloadIcon.setTag(task.getDownloadApkPkgName());
					Bitmap bmp = mImgManager.loadImageIcon(mContext, task.getDownloadApkPkgName(),
							true, new AsyncImageLoadedCallBack() {
								@Override
								public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
									if (finalViewholder.mDownloadIcon != null
											&& finalViewholder.mDownloadIcon.getTag()
													.equals(imgUrl)) {
										finalViewholder.mDownloadIcon.setImageBitmap(imageBitmap);
									} else {
										imageBitmap = null;
									}
								}
							});
					if (bmp != null) {
						viewholder.mDownloadIcon.setImageBitmap(bmp);
					}
				} else {
					// 默认图标
					viewholder.mDownloadIcon.setImageResource(R.drawable.default_icon);
				}
				// 设置软件名字
				viewholder.mDownloadName.setText(task.getDownloadName());
				viewholder.mDownloadFinish.setText(getAlreadyDownloadSize(task.getTotalSize()));
				// 设置按钮
				viewholder.mDownloadButton
						.setBackgroundResource(R.drawable.appgame_delete_apk_selector);
				viewholder.mDownloadText.setText(R.string.delete);
				final String savePath = task.getSaveFilePath();
				final long taskid = task.getId();
				// R.id.app_download_manager_button作为KEY，存储id
				viewholder.mDownloadButton.setTag(R.id.app_download_manager_button, task.getId());
				viewholder.mDownloadLayout.setTag(R.id.app_download_manager_button, task.getId());
				// R.id.app_download_manager_download_relativelayout作为KEY，存储一个holder
				viewholder.mDownloadButton.setTag(R.id.app_download_manager_download_relativelayout, task.getSaveFilePath());
				viewholder.mDownloadLayout.setTag(R.id.app_download_manager_download_relativelayout, task.getSaveFilePath());
				// R.id.app_download_manager_text作为KEY，存储position
				viewholder.mDownloadButton.setTag(R.id.app_download_manager_text, pos);
				viewholder.mDownloadLayout.setTag(R.id.app_download_manager_text, pos);
				//
				viewholder.mDownloadButton.setOnClickListener(mDeleteOnClickListener);
				viewholder.mDownloadLayout.setOnClickListener(mDeleteOnClickListener);
			}
		}
		return convertView;
	}

	private OnClickListener mControllerListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object obj1 = v.getTag(R.id.app_download_manager_button);
			Object obj2 = v.getTag(R.id.app_download_manager_download_relativelayout);
			Object obj3 = v.getTag(R.id.app_download_manager_text);
			if (obj1 == null || !(obj1 instanceof Long) || obj2 == null
					|| !(obj2 instanceof ViewHolder) || obj3 == null || !(obj3 instanceof Integer)) {
				return;
			}
			long taskId = (Long) obj1;
			ViewHolder finalViewholder = (ViewHolder) obj2;
			int pos = (Integer) obj3;
			try {
				if (mDownloadController != null) {
					DownloadTask dt = mDownloadController.getDownloadTaskById(taskId);
					PreferencesManager sp = new PreferencesManager(mContext, IPreferencesIds.DOWNLOAD_MANAGER_TASK_STATE,
							Context.MODE_PRIVATE);
					if (dt != null) {
						if (dt.getState() == DownloadTask.STATE_DOWNLOADING) {
							sp.putString(String.valueOf(taskId), String.valueOf(DownloadTask.TASK_STATE_NORMAL));
							mDownloadController.stopDownloadById(taskId);
						} else if (dt.getState() == DownloadTask.STATE_STOP) {
							sp.remove(String.valueOf(taskId));
							mDownloadController.restartDownloadById(taskId);
						} else if (dt.getState() == DownloadTask.STATE_FAIL) {
							sp.remove(String.valueOf(taskId));
							mDownloadController.restartDownloadById(taskId);
						} else if (dt.getState() == DownloadTask.STATE_WAIT) {
							sp.putString(String.valueOf(taskId), String.valueOf(DownloadTask.TASK_STATE_NORMAL));
							mDownloadController.stopDownloadById(taskId);
						}
						sp.commit();
						dt = mDownloadController.getDownloadTaskById(taskId);
						// AIDL服务，更新下载服务的下载任务之后
						//必须将更新之后的下载任务保留一份在当前进程，做到数据一致
						if (pos >= 0 && pos < mDownloadingList.size()) {
							mDownloadingList.set(pos, dt);
						}
						setButtonState(dt, finalViewholder.mDownloadPercent,
								finalViewholder.mDownloadButton, finalViewholder.mDownloadText);
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};
	
	private OnClickListener mInstallLOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object obj = v.getTag(R.id.app_download_manager_button);
			if (obj == null || !(obj instanceof String)) {
				return;
			}
			String savePath = (String) obj;
			File file = new File(savePath);
			// 判断文件后缀，不是apk，则不能安装
			String[] token = file.getName().split("\\.");
			String pf = token[token.length - 1];
			if (!file.exists()) {
				return;
			}
			if (!pf.equals("apk")) {
				Toast.makeText(
						mContext,
						mContext.getString(R.string.download_manager_cannot_be_installed),
						500).show();
				return;
			}
			ApkInstallUtils.installApk(file);
		}
	};
	
	private OnClickListener mDeleteOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Object obj1 = v.getTag(R.id.app_download_manager_button);
			Object obj2 = v.getTag(R.id.app_download_manager_download_relativelayout);
			Object obj3 = v.getTag(R.id.app_download_manager_text);
			if (obj1 == null || !(obj1 instanceof Long) || obj2 == null
					|| !(obj2 instanceof String) || obj3 == null || !(obj3 instanceof Integer)) {
				return;
			}
			long taskid = (Long) obj1;
			String savePath = (String) obj2;
			int pos = (Integer) obj3;
			try {
				if (mDownloadController != null) {
					mDownloadController.removeDownloadCompleteItem(taskid);
					mInstalledList.remove(pos);
					if (mInstalledList.size() == 1) {
						mInstalledList.clear();
					}
					FileUtil.deleteFile(savePath);
					notifyDataSetChanged();
					Toast.makeText(mContext, R.string.download_manager_delete_apk, 1000).show();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};
	
	private View createGroupView(DownloadTask task, ArrayList<DownloadTask> list) {
		int count = list.size() - 1;
		View view = mInflater.inflate(R.layout.recomm_appsmanagement_list_head, null);
		TextView tv = (TextView) view.findViewById(R.id.nametext);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		int padding = mContext.getResources().getDimensionPixelSize(
				R.dimen.download_manager_text_padding);
		tv.setPadding(padding * 3, padding, 0, padding);
		tv.setLayoutParams(lp);
		tv.setText(task.getDownloadName() + "(" + count + ")");
		return view;
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
	 * 
	 * <br>类描述:viewholder类，用于加快getview速度
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-9-7]
	 */
	private class ViewHolder {
		SimpleImageView mDownloadIcon;
		TextView mDownloadName;
		TextView mDownloadSize;
		TextView mDownloadPercent;
		ProgressBar mDownloadProgressbar;
		TextView mDownloadFinish;
		RelativeLayout mDownloadLayout;
		Button mDownloadButton;
		TextView mDownloadText;

		public void setGroupStyle() {
			mDownloadIcon.setVisibility(View.GONE);
			mDownloadName.setVisibility(View.VISIBLE);
			mDownloadSize.setVisibility(View.GONE);
			mDownloadPercent.setVisibility(View.GONE);
			mDownloadProgressbar.setVisibility(View.GONE);
			mDownloadFinish.setVisibility(View.GONE);
			mDownloadButton.setVisibility(View.GONE);
			mDownloadButton.setClickable(false);
			mDownloadLayout.setClickable(false);
		}

		public void setDownloadingStyle() {
			mDownloadIcon.setVisibility(View.VISIBLE);
			mDownloadIcon.setImageBitmap(null);
			mDownloadName.setVisibility(View.VISIBLE);
			mDownloadSize.setVisibility(View.VISIBLE);
			mDownloadPercent.setVisibility(View.VISIBLE);
			mDownloadProgressbar.setVisibility(View.VISIBLE);
			mDownloadFinish.setVisibility(View.GONE);
			mDownloadButton.setVisibility(View.VISIBLE);
			mDownloadText.setVisibility(View.VISIBLE);
			mDownloadButton.setClickable(true);
			mDownloadLayout.setClickable(true);
		}

		public void setDownloadedStyle() {
			mDownloadIcon.setVisibility(View.VISIBLE);
			mDownloadIcon.setImageBitmap(null);
			mDownloadName.setVisibility(View.VISIBLE);
			mDownloadSize.setVisibility(View.GONE);
			mDownloadPercent.setVisibility(View.GONE);
			mDownloadProgressbar.setVisibility(View.GONE);
			mDownloadFinish.setVisibility(View.VISIBLE);
			mDownloadButton.setVisibility(View.VISIBLE);
			mDownloadText.setVisibility(View.VISIBLE);
			mDownloadButton.setClickable(true);
			mDownloadLayout.setClickable(true);
		}
	}

	/**
	 * <br>功能简述:异步为imageview设置图片
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param imageView
	 * @param imgUrl
	 * @param imgPath
	 * @param imgName
	 * @param setDefaultIcon
	 */
	private void setIcon(final int position, final ImageView imageView, String imgUrl,
			String imgPath, String imgName, boolean setDefaultIcon) {
		imageView.setTag(imgUrl);
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, true,
				AppGameDrawUtils.getInstance().mMaskIconOperator, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl)) {
							imageView.setImageBitmap(imageBitmap);
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		if (bm != null) {
			imageView.setImageBitmap(bm);
		} else {
			if (setDefaultIcon) {
				imageView.setImageResource(R.drawable.default_icon);
			} else {
				imageView.setImageDrawable(null);
			}
		}
	}

	/**
	 * 将字节数转换成M,并返回例如：2.25M
	 * 
	 * @param size
	 * @return
	 */
	private final double mOneM = 1048576.0;
	private String getAlreadyDownloadSize(Long size) {
		double d = size / mOneM;
		DecimalFormat df = new DecimalFormat("######0.00");
		String str = df.format(d) + "M";
		return str;
	}

	/**
	 * 
	 */
	private void setButtonState(DownloadTask task, TextView textState, Button button,
			TextView buttonText) {
		switch (task.getState()) {
			case DownloadTask.STATE_WAIT :
				textState.setText(R.string.download_manager_wait);
				button.setBackgroundResource(R.drawable.app_detail_download_pause_selector);
				buttonText.setText(R.string.download_manager_pause);
				break;
			case DownloadTask.STATE_DOWNLOADING :
				textState.setText(task.getAlreadyDownloadPercent() + "%");
				button.setBackgroundResource(R.drawable.app_detail_download_pause_selector);
				buttonText.setText(R.string.download_manager_pause);
				break;
			case DownloadTask.STATE_FAIL :
				textState.setText(R.string.download_manager_failed);
				button.setBackgroundResource(R.drawable.app_detail_download_resume_selector);
				buttonText.setText(R.string.download_manager_continue);
				break;
			case DownloadTask.STATE_STOP :
				textState.setText(R.string.download_manager_pause);
				button.setBackgroundResource(R.drawable.app_detail_download_resume_selector);
				buttonText.setText(R.string.download_manager_continue);
				break;
			default :
				textState.setText(R.string.download_manager_wait);
				button.setBackgroundResource(R.drawable.app_detail_download_pause_selector);
				buttonText.setText(R.string.download_manager_pause);
				break;
		}
	}
}
