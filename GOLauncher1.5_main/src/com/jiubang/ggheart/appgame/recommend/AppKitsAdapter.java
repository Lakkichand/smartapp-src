/*
 * 文 件 名:  AppkitAdapter.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-12-3
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.recommend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AppsSectionIndexer;
import com.jiubang.ggheart.appgame.appcenter.component.PinnedHeaderListView.PinnedHeaderAdapter;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-12-3]
 */
public class AppKitsAdapter extends BaseAdapter implements PinnedHeaderAdapter {

	private Context mContext = null;

	private LayoutInflater mInflater = null;

	private AsyncImageManager mImgMgr = null;

	private ArrayList<AppkitsBean> mList = new ArrayList<AppkitsBean>();

	String mPath = LauncherEnv.Path.APP_MANAGER_ICON_PATH;

	private HashMap<Long, Integer> mHashMap = new HashMap<Long, Integer>();

	private AppsSectionIndexer mIndexer;

	private IAppKitsActivityNotify mNotify = null;

	private static final int TYPE_GROUP = 0;

	private static final int TYPE_INFO = 1;

	private static final int TYPE_COUNT = 2;

	public AppKitsAdapter(Context context, IAppKitsActivityNotify notify,
			ArrayList<AppkitsBean> list, ArrayList<DownloadTask> taskList) {
		mImgMgr = AsyncImageManager.getInstance();
		mNotify = notify;
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		updateList(list, taskList);
	}

	public void updateList(ArrayList<AppkitsBean> list, ArrayList<DownloadTask> taskList) {
		if (list == null || list.size() <= 0) {
			return;
		}
		// 设置下载任务状态 
		if (taskList != null && taskList.size() > 0) {
			for (DownloadTask task : taskList) {
				for (int i = 0; i < list.size(); i++) {
					AppkitsBean bean = list.get(i);
					for (BoutiqueApp app : bean.mAppInfoList) {
						if (app == null || app.info == null || app.info.appid == null) {
							continue;
						}
						if (app.info.appid.equals(task.getId() + "")) {
							app.downloadState.state = task.getState();
							app.downloadState.alreadyDownloadPercent = task
									.getAlreadyDownloadPercent();
							break;
						}
					}
				}
			}
		}
		mHashMap.clear();
		mList.clear();
		mList = list;
		ArrayList<String> stringDivider = new ArrayList<String>();
		ArrayList<Integer> intDivider = new ArrayList<Integer>();
		int count = 0;
		for (AppkitsBean bean : mList) {
			if (bean.mTitle == null || bean.mTitle.equals("")) {
				for (BoutiqueApp app : bean.mAppInfoList) {
					if (app.info != null && app.info.appid != null) {
						mHashMap.put(Long.parseLong(app.info.appid), AppKitsActivity.STATE_UNSELECT);
					}
				}
				count++;
				intDivider.add(count);
				count = 0;
			} else {
				count++;
				stringDivider.add(bean.mTitle);
			}
		}
		// 分组节点
		String[] sections = stringDivider.toArray(new String[stringDivider.size()]);
		int[] counts = new int[intDivider.size()];
		for (int i = 0; i < intDivider.size(); i++) {
			counts[i] = intDivider.get(i).intValue();
		}
		mIndexer = new AppsSectionIndexer(sections, counts);
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		if (mList.get(position).mTitle != null && !mList.get(position).mTitle.equals("")) {
			return TYPE_GROUP;
		} else {
			return TYPE_INFO;
		}
	}

	/** {@inheritDoc} */

	@Override
	public int getCount() {
		return mList.size();
	}

	/** {@inheritDoc} */

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	/** {@inheritDoc} */

	@Override
	public long getItemId(int position) {
		return position;
	}

	/** {@inheritDoc} */

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Viewholder viewholder = null;
		GroupViewholder groupViewholder = null;
		int type = getItemViewType(position);
		if (convertView == null) {
			if (type == TYPE_INFO) {
				convertView = mInflater.inflate(R.layout.recommend_listview_check1, null);
				viewholder = new Viewholder();
				viewholder.mRelativeLayout1 = (RelativeLayout) convertView
						.findViewById(R.id.Recommend_relativeLayout1);
				viewholder.mRelativeLayout2 = (RelativeLayout) convertView
						.findViewById(R.id.Recommend_relativeLayout2);
				viewholder.mRelativeLayout3 = (RelativeLayout) convertView
						.findViewById(R.id.Recommend_relativeLayout3);
				viewholder.mRelativeLayout4 = (RelativeLayout) convertView
						.findViewById(R.id.Recommend_relativeLayout4);

				viewholder.mIcon1 = (ImageView) convertView.findViewById(R.id.Recommend_icon1);
				viewholder.mIcon2 = (ImageView) convertView.findViewById(R.id.Recommend_icon2);
				viewholder.mIcon3 = (ImageView) convertView.findViewById(R.id.Recommend_icon3);
				viewholder.mIcon4 = (ImageView) convertView.findViewById(R.id.Recommend_icon4);

				viewholder.mAppname1 = (TextView) convertView.findViewById(R.id.Recommend_appname1);
				viewholder.mAppname2 = (TextView) convertView.findViewById(R.id.Recommend_appname2);
				viewholder.mAppname3 = (TextView) convertView.findViewById(R.id.Recommend_appname3);
				viewholder.mAppname4 = (TextView) convertView.findViewById(R.id.Recommend_appname4);

				viewholder.mProgressTextView1 = (TextView) convertView
						.findViewById(R.id.Recommend_progressTextView1);
				viewholder.mProgressTextView2 = (TextView) convertView
						.findViewById(R.id.Recommend_progressTextView2);
				viewholder.mProgressTextView3 = (TextView) convertView
						.findViewById(R.id.Recommend_progressTextView3);
				viewholder.mProgressTextView4 = (TextView) convertView
						.findViewById(R.id.Recommend_progressTextView4);

				viewholder.mProgressBar1 = (ProgressBar) convertView
						.findViewById(R.id.Recommend_progressbar1);
				viewholder.mProgressBar2 = (ProgressBar) convertView
						.findViewById(R.id.Recommend_progressbar2);
				viewholder.mProgressBar3 = (ProgressBar) convertView
						.findViewById(R.id.Recommend_progressbar3);
				viewholder.mProgressBar4 = (ProgressBar) convertView
						.findViewById(R.id.Recommend_progressbar4);

				viewholder.mCheckbox1 = (ImageView) convertView.findViewById(R.id.Recommend_checkbox1);
				viewholder.mCheckbox2 = (ImageView) convertView.findViewById(R.id.Recommend_checkbox2);
				viewholder.mCheckbox3 = (ImageView) convertView.findViewById(R.id.Recommend_checkbox3);
				viewholder.mCheckbox4 = (ImageView) convertView.findViewById(R.id.Recommend_checkbox4);

				viewholder.mSize1 = (TextView) convertView.findViewById(R.id.Recommend_size1);
				viewholder.mSize2 = (TextView) convertView.findViewById(R.id.Recommend_size2);
				viewholder.mSize3 = (TextView) convertView.findViewById(R.id.Recommend_size3);
				viewholder.mSize4 = (TextView) convertView.findViewById(R.id.Recommend_size4);
				convertView.setTag(viewholder);
			} else {
				convertView = mInflater.inflate(R.layout.recomm_appsmanagement_list_head, null);
				groupViewholder = new GroupViewholder();
				groupViewholder.mGroupTextView = (TextView) convertView.findViewById(R.id.nametext);
				groupViewholder.mGroupTextView.setBackgroundResource(R.drawable.list_head_bg);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
				groupViewholder.mBottomLine = (ImageView) convertView.findViewById(R.id.divider);
				groupViewholder.mBottomLine.setBackgroundResource(R.drawable.listview_divider);
				groupViewholder.mGroupTextView.setPadding(
						mContext.getResources()
								.getDimensionPixelSize(R.dimen.download_manager_text_padding) * 2, mContext
								.getResources()
								.getDimensionPixelSize(R.dimen.download_manager_text_padding), 0, mContext
								.getResources()
								.getDimensionPixelSize(R.dimen.download_manager_text_padding));
				groupViewholder.mGroupTextView.setLayoutParams(lp);
				convertView.setTag(groupViewholder);
			}
		} else {
			if (type == TYPE_INFO) {
				viewholder = (Viewholder) convertView.getTag();
			} else {
				groupViewholder = (GroupViewholder) convertView.getTag();
			}
		}
		AppkitsBean bean = mList.get(position);
		if (bean != null) {
			// 分组头信息
			if (bean.mTitle != null && !bean.mTitle.equals("")) {
//				return createGroupView(bean.mTitle);
				groupViewholder.mGroupTextView.setText(bean.mTitle);
				groupViewholder.mBottomLine.setBackgroundResource(R.drawable.listview_divider);
			} else {
				for (int i = 0; i < 4; i++) {
					if (i >= bean.mAppInfoList.size()) {
						// 不可见
						viewholder.getRelativeLayout(i).setVisibility(View.GONE);
					} else {
						BoutiqueApp app = bean.mAppInfoList.get(i);
						// 使该区域可见
						viewholder.getRelativeLayout(i).setVisibility(View.VISIBLE);
						viewholder.getAppName(i).setText(app.info.name);
						viewholder.getSizeTextView(i).setText(app.info.size);
						setIcon(position, viewholder.getIcon(i), app.info.icon);
						// 判断该APP是否处于下载队列
						boolean flag = isDownloading(app.downloadState.state);
						// 处于下载队列中，区域块不可点击，颜色与背景不同
						if (flag) {
							viewholder.getCheckbox(i).setVisibility(View.GONE);
							viewholder.getRelativeLayout(i).setClickable(false);
							viewholder.getRelativeLayout(i).setOnTouchListener(null);
							viewholder.getSizeTextView(i).setVisibility(View.GONE);
							// 设置标志位为“下载”
							viewholder.getRelativeLayout(i).setTag(Long.parseLong(app.info.appid));
							mHashMap.put(Long.parseLong(app.info.appid),
									AppKitsActivity.STATE_DOWNLOADING);
							viewholder.getProgressBar(i).setVisibility(View.VISIBLE);
							viewholder.getProgressBar(i).setProgress(
									app.downloadState.alreadyDownloadPercent);
							switch (app.downloadState.state) {
								case DownloadTask.STATE_STOP :
									viewholder.getRelativeLayout(i).setBackgroundResource(
											R.color.center_background);
									viewholder.getAppName(i).setVisibility(View.GONE);
									viewholder.getProgressTextView(i).setVisibility(View.VISIBLE);
									viewholder.getProgressTextView(i).setText(
											mContext.getString(R.string.download_manager_pause));
									break;
								case DownloadTask.STATE_FAIL :
									viewholder.getRelativeLayout(i).setBackgroundResource(
											R.color.center_background);
									viewholder.getAppName(i).setVisibility(View.GONE);
									viewholder.getProgressTextView(i).setVisibility(View.VISIBLE);
									viewholder.getProgressTextView(i).setText(
											mContext.getString(R.string.download_manager_failed));
									break;
								case DownloadTask.STATE_WAIT :
									viewholder.getRelativeLayout(i).setBackgroundResource(
											R.color.center_background);
									viewholder.getAppName(i).setVisibility(View.GONE);
									viewholder.getProgressTextView(i).setVisibility(View.VISIBLE);
									viewholder.getProgressTextView(i).setText(
											mContext.getString(R.string.download_manager_wait));
									break;
								case DownloadTask.STATE_DOWNLOADING :
									viewholder.getRelativeLayout(i).setBackgroundResource(
											R.color.center_background);
									viewholder.getAppName(i).setVisibility(View.GONE);
									viewholder.getProgressTextView(i).setVisibility(View.VISIBLE);
									viewholder.getProgressTextView(i).setText(
											app.downloadState.alreadyDownloadPercent + "%");
									break;
								default :
									viewholder.getRelativeLayout(i).setBackgroundResource(
											R.color.center_background);
									viewholder.getAppName(i).setVisibility(View.GONE);
									viewholder.getProgressTextView(i).setVisibility(View.VISIBLE);
									viewholder.getProgressTextView(i).setText(
											mContext.getString(R.string.download_manager_wait));
									break;
							}
						} else {
							// 不处于下载队列中，区域块可点击，背景颜色因“被选”或者“可先”而变化
							viewholder.getRelativeLayout(i).setTag(Long.parseLong(app.info.appid));
							viewholder.getRelativeLayout(i).setClickable(true);
							viewholder.getAppName(i).setVisibility(View.VISIBLE);
							viewholder.getProgressTextView(i).setVisibility(View.GONE);
							viewholder.getSizeTextView(i).setVisibility(View.VISIBLE);
							viewholder.getProgressBar(i).setProgress(0);
							viewholder.getProgressBar(i).setVisibility(View.GONE);
							// 使状态还原
							if (mHashMap.get(Long.parseLong(app.info.appid)) == AppKitsActivity.STATE_SELECT) {
								viewholder.getCheckbox(i).setVisibility(View.VISIBLE);
								viewholder.getRelativeLayout(i).setBackgroundResource(
										R.drawable.yjzj_green_bg);
							} else if (mHashMap.get(Long.parseLong(app.info.appid)) == AppKitsActivity.STATE_UNSELECT) {
								viewholder.getCheckbox(i).setVisibility(View.GONE);
								viewholder.getRelativeLayout(i).setBackgroundResource(
										R.color.center_background);
							} else if (mHashMap.get(Long.parseLong(app.info.appid)) == AppKitsActivity.STATE_DOWNLOADING) {
								mHashMap.put(Long.parseLong(app.info.appid),
										AppKitsActivity.STATE_UNSELECT);
								viewholder.getCheckbox(i).setVisibility(View.GONE);
								viewholder.getRelativeLayout(i).setBackgroundResource(
										R.color.center_background);
							}
							final int nIndex = i;
							viewholder.getRelativeLayout(i).setOnTouchListener(
									new OnTouchListener() {
										boolean mIsMoveOut = true;
										@Override
										public boolean onTouch(View v, MotionEvent event) {
											Long id = (Long) v.getTag();
											if (event.getAction() == MotionEvent.ACTION_DOWN) {
												mIsMoveOut = false;
												v.setBackgroundResource(R.drawable.tab_press);
												return true;
											} else if (event.getAction() == MotionEvent.ACTION_OUTSIDE
													|| event.getAction() == MotionEvent.ACTION_CANCEL) {
												mIsMoveOut = true;
												if (mHashMap.get(id) == AppKitsActivity.STATE_SELECT) {
													v.setBackgroundResource(R.drawable.yjzj_green_bg);
												} else if (mHashMap.get(id) == AppKitsActivity.STATE_UNSELECT) {
													v.setBackgroundResource(R.color.center_background);
												}
												return true;
											} else if (event.getAction() == MotionEvent.ACTION_UP) {
												if (mIsMoveOut == true) {
													return true;
												}
												if (mHashMap.get(id) == AppKitsActivity.STATE_SELECT) {
													mHashMap.put(id, AppKitsActivity.STATE_UNSELECT);
												} else if (mHashMap.get(id) == AppKitsActivity.STATE_UNSELECT) {
													mHashMap.put(id, AppKitsActivity.STATE_SELECT);
												}
												switch (nIndex) {
													case 0 :
														if (mHashMap.get(id) == AppKitsActivity.STATE_SELECT) {
															v.setBackgroundResource(R.drawable.yjzj_green_bg);
															((ImageView) v
																	.findViewById(R.id.Recommend_checkbox1))
																	.setVisibility(View.VISIBLE);
														} else if (mHashMap.get(id) == AppKitsActivity.STATE_UNSELECT) {
															v.setBackgroundResource(R.color.center_background);
															((ImageView) v
																	.findViewById(R.id.Recommend_checkbox1))
																	.setVisibility(View.GONE);
														}
														break;
													case 1 :
														if (mHashMap.get(id) == AppKitsActivity.STATE_SELECT) {
															v.setBackgroundResource(R.drawable.yjzj_green_bg);
															((ImageView) v
																	.findViewById(R.id.Recommend_checkbox2))
																	.setVisibility(View.VISIBLE);
														} else if (mHashMap.get(id) == AppKitsActivity.STATE_UNSELECT) {
															v.setBackgroundResource(R.color.center_background);
															((ImageView) v
																	.findViewById(R.id.Recommend_checkbox2))
																	.setVisibility(View.GONE);
														}
														break;
													case 2 :
														if (mHashMap.get(id) == AppKitsActivity.STATE_SELECT) {
															v.setBackgroundResource(R.drawable.yjzj_green_bg);
															((ImageView) v
																	.findViewById(R.id.Recommend_checkbox3))
																	.setVisibility(View.VISIBLE);
														} else if (mHashMap.get(id) == AppKitsActivity.STATE_UNSELECT) {
															v.setBackgroundResource(R.color.center_background);
															((ImageView) v
																	.findViewById(R.id.Recommend_checkbox3))
																	.setVisibility(View.GONE);
														}
														break;
													case 3 :
														if (mHashMap.get(id) == AppKitsActivity.STATE_SELECT) {
															v.setBackgroundResource(R.drawable.yjzj_green_bg);
															((ImageView) v
																	.findViewById(R.id.Recommend_checkbox4))
																	.setVisibility(View.VISIBLE);
														} else if (mHashMap.get(id) == AppKitsActivity.STATE_UNSELECT) {
															v.setBackgroundResource(R.color.center_background);
															((ImageView) v
																	.findViewById(R.id.Recommend_checkbox4))
																	.setVisibility(View.GONE);
														}
														break;
												}
												// 通知外层VIEW 
												if (mNotify != null) {
													mNotify.notifyView();
												}
											}
											return true;
										}
									});
						}
					}
				}
			}
		}
		convertView.setId(position);
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
	 * 传入url，异步为ImageView设置图标
	 * 
	 * @param imgView
	 * @param url
	 * @param fileName
	 * @param viewGroup
	 */
	private void setIcon(final int position, final ImageView imgView, String url) {
		if (imgView == null || TextUtils.isEmpty(url)) {
			return;
		}
		if (mImgMgr == null) {
			mImgMgr = AsyncImageManager.getInstance();
		}
		imgView.setTag(url);
		String fileName = String.valueOf(url.hashCode());
		Bitmap bm = mImgMgr.loadImageForList(position, mPath, fileName, url, true, true, AppGameDrawUtils.getInstance().mMaskIconOperator,
				new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						Object object = imgView.getTag();
						if (object != null && imgUrl.equals(object)) {
							imgView.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			imgView.setImageBitmap(bm);
		} else {
			imgView.setImageResource(R.drawable.default_icon);
		}
	}

	public HashMap<Long, Integer> getSelectHashMap() {
		return mHashMap;
	}

	/**
	 * <br>功能简述:返回有序的ID列表
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public ArrayList<Long> getSelectIds() {
		ArrayList<Long> idsList = new ArrayList<Long>();
		// 取出已经勾选的APP的ID
		Iterator<Entry<Long, Integer>> iter = mHashMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Long, Integer> entry = iter.next();
			int state = entry.getValue();
			if (state == AppKitsActivity.STATE_SELECT) {
				idsList.add(entry.getKey());
			}
		}
		// 对取出ID的APP，按照在LIST的位置进行排序
		HashMap<Long, Integer> hashMap = new HashMap<Long, Integer>();
		int position = 0;
		for (AppkitsBean bean : mList) {
			if (bean.mTitle != null && !bean.mTitle.equals("")) {
				position++;
				continue;
			} else {
				for (BoutiqueApp app : bean.mAppInfoList) {
					position++;
					if (idsList.contains(Long.parseLong(app.info.appid))
							&& hashMap.get(Long.parseLong(app.info.appid)) == null) {
						hashMap.put(Long.parseLong(app.info.appid), position);
					}
				}
			}
		}
		ArrayList<Map.Entry<Long, Integer>> mapList = new ArrayList<Map.Entry<Long, Integer>>(
				hashMap.entrySet());
		Collections.sort(mapList, new Comparator<Map.Entry<Long, Integer>>() {
			@Override
			public int compare(Entry<Long, Integer> lhs, Entry<Long, Integer> rhs) {
				return lhs.getValue() - rhs.getValue();
			}
		});
		idsList.clear();
		for (Map.Entry<Long, Integer> entry : mapList) {
			idsList.add(entry.getKey());
		}
		return idsList;
	}
	/**
	 * 功能简述:判断是否处于下载服务中 功能详细描述: 注意:
	 * 
	 * @param state
	 * @return
	 */
	private boolean isDownloading(int state) {
		switch (state) {
			case DownloadTask.STATE_DOWNLOADING :
			case DownloadTask.STATE_START :
			case DownloadTask.STATE_STOP :
			case DownloadTask.STATE_WAIT :
				return true;
			default :
				return false;
		}
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-12-11]
	 */
	private class GroupViewholder {
		TextView mGroupTextView;
		ImageView mBottomLine;
	}
	
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  liuxinyang
	 * @date  [2012-9-10]
	 */
	private class Viewholder {
		RelativeLayout mRelativeLayout1;
		ImageView mIcon1;
		TextView mAppname1;
		TextView mProgressTextView1;
		ProgressBar mProgressBar1;
		ImageView mCheckbox1;
		TextView mSize1;

		RelativeLayout mRelativeLayout2;
		ImageView mIcon2;
		TextView mAppname2;
		TextView mProgressTextView2;
		ProgressBar mProgressBar2;
		ImageView mCheckbox2;
		TextView mSize2;

		RelativeLayout mRelativeLayout3;
		ImageView mIcon3;
		TextView mAppname3;
		TextView mProgressTextView3;
		ProgressBar mProgressBar3;
		ImageView mCheckbox3;
		TextView mSize3;

		RelativeLayout mRelativeLayout4;
		ImageView mIcon4;
		TextView mAppname4;
		TextView mProgressTextView4;
		ProgressBar mProgressBar4;
		ImageView mCheckbox4;
		TextView mSize4;

		public RelativeLayout getRelativeLayout(int index) {
			switch (index) {
				case 0 :
					return mRelativeLayout1;
				case 1 :
					return mRelativeLayout2;
				case 2 :
					return mRelativeLayout3;
				case 3 :
					return mRelativeLayout4;
				default :
					return null;
			}
		}

		public ImageView getIcon(int index) {
			switch (index) {
				case 0 :
					return mIcon1;
				case 1 :
					return mIcon2;
				case 2 :
					return mIcon3;
				case 3 :
					return mIcon4;
				default :
					return null;
			}
		}

		public TextView getAppName(int index) {
			switch (index) {
				case 0 :
					return mAppname1;
				case 1 :
					return mAppname2;
				case 2 :
					return mAppname3;
				case 3 :
					return mAppname4;
				default :
					return null;
			}
		}

		public TextView getProgressTextView(int index) {
			switch (index) {
				case 0 :
					return mProgressTextView1;
				case 1 :
					return mProgressTextView2;
				case 2 :
					return mProgressTextView3;
				case 3 :
					return mProgressTextView4;
				default :
					return null;
			}
		}

		private ImageView getCheckbox(int index) {
			switch (index) {
				case 0 :
					return mCheckbox1;
				case 1 :
					return mCheckbox2;
				case 2 :
					return mCheckbox3;
				case 3 :
					return mCheckbox4;
				default :
					return null;
			}
		}

		private TextView getSizeTextView(int index) {
			switch (index) {
				case 0 :
					return mSize1;
				case 1 :
					return mSize2;
				case 2 :
					return mSize3;
				case 3 :
					return mSize4;
				default :
					return null;
			}
		}

		private ProgressBar getProgressBar(int index) {
			switch (index) {
				case 0 :
					return mProgressBar1;
				case 1 :
					return mProgressBar2;
				case 2 :
					return mProgressBar3;
				case 3 :
					return mProgressBar4;
				default :
					return null;
			}
		}
	}
}
