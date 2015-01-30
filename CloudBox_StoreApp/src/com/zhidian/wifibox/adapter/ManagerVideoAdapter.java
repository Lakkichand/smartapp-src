package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.FileDetailsBean;
import com.zhidian.wifibox.file.audio.MusicData;
import com.zhidian.wifibox.file.video.VideoHelper;
import com.zhidian.wifibox.file.video.VideoItem;
import com.zhidian.wifibox.receiver.VideoCheckChangeReceiver;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.IntentUtils;
import com.zhidian.wifibox.util.ToastUtils;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog.GoonCallBackListener;
import com.zhidian.wifibox.view.dialog.FileDetailsPopupWindow;
import com.zhidian.wifibox.view.dialog.WaitingDialog;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 
 * 视频列表适配器
 * 
 * @author shihuajian
 *
 */
public class ManagerVideoAdapter extends BaseAdapter {
	
	private final static String TAG = ManagerVideoAdapter.class.getSimpleName();

	private Context mContext;
	private List<VideoItem> mDataList = new ArrayList<VideoItem>();
	private List<VideoItem> mDataListDel = new ArrayList<VideoItem>();
	/** 当前打开的下拉的Id */
	private int mCurListOpenIndex;
	/** 是否有列表被打开 */
	private boolean mCurListIsOpen;
	private VideoHelper mHelper;

	private Handler mHandler = new Handler(Looper.getMainLooper());
	
	private FileDetailsPopupWindow pop;
	
	public ManagerVideoAdapter(Context context) {
		this.mContext = context;
		this.mDataList = new ArrayList<VideoItem>();
		this.mDataListDel = new ArrayList<VideoItem>();
		this.mCurListOpenIndex = -1;
		this.mCurListIsOpen = false;
		this.pop = new FileDetailsPopupWindow((Activity)context);
		this.mHelper = VideoHelper.getInstance();
		this.mHelper.init(context);
	}
	
	public ManagerVideoAdapter(Context context, List<VideoItem> dataList, VideoHelper helper) {
		if (dataList != null) {
			mDataList.addAll(dataList);
		}
		this.mContext = context;
		this.mDataListDel = new ArrayList<VideoItem>();
		this.mCurListOpenIndex = -1;
		this.mCurListIsOpen = false;
		this.pop = new FileDetailsPopupWindow((Activity)context);
		this.mHelper = helper;
	}
	
	/** 刷新数据 */
	public void refreshAdapter(List<VideoItem> FileList) {
		mDataList = new ArrayList<VideoItem>();
		if (FileList != null) {
			mDataList.addAll(FileList);
		}
		mDataListDel.clear();
		sendVideoBroadcast(false);
		notifyDataSetChanged();
	}
	
	/** 实现全选 */
	public void chooseAll(boolean isAll) {
		if (mDataList != null) {
			for (VideoItem data : mDataList) {
				data.setIsSelected(isAll);
			}
			
			if (mDataListDel != null) {
				if (isAll) {
					mDataListDel.clear();
					mDataListDel.addAll(mDataList);
				} else {
					mDataListDel.clear();
				}
			}
			notifyDataSetChanged();
			sendVideoBroadcast(false);
		}
	}
	
	/**
	 * 发送选择广播
	 */
	private void sendVideoBroadcast(boolean isRefresh) {
		Intent intent = new Intent(VideoCheckChangeReceiver.PATH_NAME);
		intent.putExtra(VideoCheckChangeReceiver.CHOOSE_COUNT_FLAG, mDataListDel.size());
		intent.putExtra(VideoCheckChangeReceiver.TOTAL_COUNT, getCount());
		intent.putExtra(VideoCheckChangeReceiver.IS_REFRESH, isRefresh);
		mContext.sendBroadcast(intent);
	}
	
	/** 删除选择的数据 */
	public void chooseDel() {
		if (mDataListDel != null) {
			String countTip = mContext.getString(R.string.delete_hint_video, mDataListDel.size() + "");
			final WaitingDialog waiting = new WaitingDialog(mContext);
			DeleteHintDialog dialog = new DeleteHintDialog(mContext, countTip);
			dialog.setGoonCallBackListener(new GoonCallBackListener() {
				
				@Override
				public void onClick() {
					// 关闭打开的Item
					setCurListIsOpen(-1);
					waiting.show();
					new Thread() {
						@Override
						public void run() {
							final long startTime = System.currentTimeMillis();
							final List<VideoItem> xData = new ArrayList<VideoItem>();
							xData.addAll(mDataList);
							for (int i = 0; i < mDataListDel.size(); i++) {
								VideoItem data = mDataListDel.get(i);
								if (FileUtil.DeleteFolder(data.getData())) {
									mHelper.delete(data.getId());
								}
							}
							if (xData.removeAll(mDataListDel)) {
								mDataListDel.clear();
							}
							FileUtil.scanSdCard(mContext);
							mHandler.post(new Runnable() {
								
								@Override
								public void run() {
									refreshAdapter(xData);
									sendVideoBroadcast(true);
									long endTime = System.currentTimeMillis();
									if ((endTime - startTime) < 1000) {
										waiting.close();
									} else {
										waiting.dismiss();
									}
								}
							});
						}
					}.start();
					
					
				}
			});
			dialog.show();
		}
	}

	public void clear() {
		mDataList.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (mDataList == null || mDataList.size() <= 0)
			return 0;
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext)
					.inflate(R.layout.list_item_manager_video, parent, false);
			holder.headerPadding = (LinearLayout) convertView.findViewById(R.id.headerPadding);
			holder.bottomPadding = (LinearLayout) convertView.findViewById(R.id.bottomPadding);
			holder.choose = (ImageButton) convertView.findViewById(R.id.choose);
			holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
			holder.videoName = (TextView) convertView.findViewById(R.id.videoName);
			holder.videoTime = (TextView) convertView.findViewById(R.id.videoTime);
			holder.videoSize = (TextView) convertView.findViewById(R.id.videoSize);
			holder.handle = (ImageButton) convertView.findViewById(R.id.handle);
			holder.hide = (LinearLayout) convertView.findViewById(R.id.hide);
			holder.delete = (LinearLayout) convertView.findViewById(R.id.delete);
			holder.details = (LinearLayout) convertView.findViewById(R.id.details);
			holder.open = (LinearLayout) convertView.findViewById(R.id.open);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (getCount() > 0) {
			// 设置头部间距
			if (position == 0) {
				holder.headerPadding.setVisibility(View.VISIBLE);
			} else {
				holder.headerPadding.setVisibility(View.GONE);
			}
			// 设置底部间距
			if (position == (getCount() - 1)) {
				holder.bottomPadding.setVisibility(View.VISIBLE);
			} else {
				holder.bottomPadding.setVisibility(View.GONE);
			}
			
			holder.videoName.setText(mDataList.get(position).getTitle());
			holder.videoTime.setText(FileUtil.milliscond2Time(mDataList.get(position).getDuration()));
			holder.videoSize.setText(FileUtil.bytes2kb(mDataList.get(position).getSize()));
			
			String imgName = mDataList.get(position).getDisplayName();
			String imgUrl = mDataList.get(position).getData();
			String filePath = mDataList.get(position).getFilePath();
			holder.thumb.setTag(imgUrl);
			boolean isCache = true;
			Bitmap bm = AsyncImageManager.getInstance().loadVideoImage(filePath, imgName, imgUrl, isCache, false, 
					new AsyncImageLoadedCallBack() {
						
						@Override
						public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
							if (imageBitmap == null) {
								return;
							}
							if (holder.thumb.getTag().equals(imgUrl)) {
								holder.thumb.setImageBitmap(imageBitmap);
							}
							
						}
					});
			if (bm != null) {
				holder.thumb.setImageBitmap(bm);
			} else {
				// 默认
				holder.thumb.setImageBitmap(DrawUtil.sDefaultPicture);
			}
			
			// 设置是否选择
			boolean isChoose = mDataList.get(position).getIsSelected();
			if (isChoose) {
				holder.choose.setImageResource(R.drawable.cleanmaster_select);
			} else {
				holder.choose.setImageResource(R.drawable.cleanmaster_noselect);
			}
			
			holder.choose.setTag(R.id.tag_position, position);
			holder.choose.setTag(R.id.tag_object, mDataList.get(position));
			holder.choose.setOnClickListener(chooseListener);
			
			// 操作按钮点击监听事件
			holder.handle.setTag(position);
			holder.handle.setOnClickListener(handleListener);
			if (mCurListOpenIndex == position) {
				holder.hide.setVisibility(View.VISIBLE);
				holder.handle.setImageResource(R.drawable.arrow_rise);
			} else {
				holder.hide.setVisibility(View.GONE);
				holder.handle.setImageResource(R.drawable.arrow_drop);
			}
			
			// 删除按钮点击监听事件
			holder.delete.setTag(position);
			holder.delete.setOnClickListener(delListener);
			
			// 详情按钮点击监听事件
			holder.details.setTag(position);
			holder.details.setOnClickListener(detailsListener);
			
			// 打开按钮点击监听事件
			holder.open.setTag(position);
			holder.open.setOnClickListener(openListener);
		}
		return convertView;
	}
	
	/** 操作监听 */
	private OnClickListener handleListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			if (mCurListIsOpen) {
				// 如果ID相等，则隐藏，反之显示另外一个
				if (mCurListOpenIndex == position) {
					setCurListIsOpen(-1);
				} else {
					setCurListIsOpen(position);
				}
			} else {
				setCurListIsOpen(position);
			}
		}
	};
	
	/**
	 * 设置当前打开的Item
	 * @param position -1:关闭, 其他整数则为打开
	 */
	private void setCurListIsOpen(int position) {
		if (position == -1) {
			mCurListIsOpen = false;
			mCurListOpenIndex = -1;
		} else {
			mCurListIsOpen = true;
			mCurListOpenIndex = position;
		}
		notifyDataSetChanged();

	}
	
	/** 选择按钮监听 */
	private OnClickListener chooseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setCurListIsOpen(-1);
			int position = (Integer) v.getTag(R.id.tag_position);
			VideoItem mData = (VideoItem) v.getTag(R.id.tag_object);
			boolean isChecked = mData.getIsSelected();
			mDataList.get(position).setIsSelected(!isChecked);

			if (mDataListDel != null) {
				isChecked = mDataList.get(position).getIsSelected();
				if (isChecked) {
					mDataListDel.add(mDataList.get(position));
				} else {
					for (int i = 0; i < mDataListDel.size(); i++) {
						if (mData.getId() == mDataListDel.get(i).getId()) {
							mDataListDel.remove(i);
							break;
						}
					}
				}
			}
			notifyDataSetChanged();
			sendVideoBroadcast(false);
		}

	};
	
	/** 选项删除监听 */
	private OnClickListener delListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String countTip = mContext.getString(R.string.delete_hint_video, "1");
			final int position = (Integer) v.getTag();
			final WaitingDialog waiting = new WaitingDialog(mContext);
			DeleteHintDialog dialog = new DeleteHintDialog(mContext, countTip);
			dialog.setGoonCallBackListener(new GoonCallBackListener() {
				
				@Override
				public void onClick() {
					// 如果相等，关闭打开的Item
					waiting.show();
					if (mCurListOpenIndex == position) {
						setCurListIsOpen(-1);
					}
					new Thread() {
						public void run() {
							final long startTime = System.currentTimeMillis();
							final List<VideoItem> xData = new ArrayList<VideoItem>();
							xData.addAll(mDataList);
							VideoItem item = mDataList.get(position);
							if (FileUtil.DeleteFolder(item.getData())) {
								mHelper.delete(item.getId());
								xData.remove(position);
								
								for (int i = 0; i < mDataListDel.size(); i++) {
									if (item.getId() == mDataListDel.get(i).getId()) {
										mDataListDel.remove(i);
										break;
									}
								}
								FileUtil.scanSdCard(mContext);
								mHandler.post(new Runnable() {
									
									@Override
									public void run() {
										refreshAdapter(xData);
										sendVideoBroadcast(true);
										long endTime = System.currentTimeMillis();
										if ((endTime - startTime) < 1000) {
											waiting.close();
										} else {
											waiting.dismiss();
										}
									}
								});
							}
						};
					}.start();
				}
			});
			dialog.show();
		}
	};
	
	/** 详情按钮监听 */
	private OnClickListener detailsListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			VideoItem item = mDataList.get(position);
			FileDetailsBean bean = new FileDetailsBean();
			bean.setFileName(item.getDisplayName());
			bean.setFileType(item.getMimeType());
			bean.setFileSize(item.getSize());
			bean.setFileDatetaken(item.getDatetaken());
			bean.setFilePath(item.getData());
			pop.setData(bean);
			pop.showAtLocation(v);
		}
	};
	
	/** 打开按钮监听 */
	private OnClickListener openListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			String path = mDataList.get(position).getData();
			File file = new File(path);
			Intent intent = IntentUtils.createFileOpenIntent(file);
			try {
				mContext.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
				ToastUtils.showShortToast(mContext, mContext.getString(R.string.file_not_found));
			}
		}
	};

	static class ViewHolder {
		LinearLayout headerPadding;	// 头部边界
		LinearLayout bottomPadding;	// 底部边界
		ImageButton choose;	// 选择框
		ImageView thumb; // 视频缩略图
		TextView videoName; // 视频名
		TextView videoTime; // 播放总时间
		TextView videoSize; // 文件大小
		ImageButton handle;	// 操作
		LinearLayout hide; // 隐藏部分
		LinearLayout delete; // 删除
		LinearLayout details;	// 详情
		LinearLayout open;	// 打开

	}

}
