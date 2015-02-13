package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;

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
import com.zhidian.wifibox.file.album.AlbumHelper;
import com.zhidian.wifibox.file.album.ImageBucket;
import com.zhidian.wifibox.file.album.ImageChildItem;
import com.zhidian.wifibox.receiver.AlbumCheckChangeReceiver;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog.GoonCallBackListener;
import com.zhidian.wifibox.view.dialog.WaitingDialog;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 
 * 图片管理目录适配器
 * 
 * @author shihuajian
 * 
 */
public class ManagerPictureAdapter extends BaseAdapter {
	
	private static final String TAG = ManagerPictureAdapter.class.getSimpleName();
	
	private Context mContext;
	private List<ImageBucket> mDataList = new ArrayList<ImageBucket>();
	/** 要删除的数据 */
	private List<ImageBucket> mDataListDel = new ArrayList<ImageBucket>();
	private AlbumHelper mHelper;
	
	private int mCount = 0;

	private Handler mHandler = new Handler(Looper.getMainLooper());

	public ManagerPictureAdapter(Context context) {
		this.mContext = context;
		this.mDataList = new ArrayList<ImageBucket>();
		this.mDataListDel = new ArrayList<ImageBucket>();
		this.mHelper = AlbumHelper.getInstance();
		this.mHelper.init(context);
	}

	public ManagerPictureAdapter(Context context, List<ImageBucket> dataList,
			AlbumHelper helper) {
		this.mContext = context;
		if (dataList != null) {
			this.mDataList.addAll(dataList);
		}
		this.mDataListDel = new ArrayList<ImageBucket>();
		this.mHelper = helper;
	}

	/** 刷新数据 */
	public void refreshAdapter(List<ImageBucket> FileList) {
		mDataList = new ArrayList<ImageBucket>();
		if (FileList != null) {
			mDataList.addAll(FileList);
		}
		mDataListDel.clear();
		sendPictureBroadcast(false);
		notifyDataSetChanged();
	}

	/** 实现全选 */
	public void chooseAll(boolean isAll) {
		if (mDataList != null) {
			for (ImageBucket data : mDataList) {
				data.isSelected = isAll;
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
			sendPictureBroadcast(false);
		}
	}

	/**
	 * 发送选择广播
	 */
	private void sendPictureBroadcast(boolean isRefresh) {
		Intent intent = new Intent(AlbumCheckChangeReceiver.PATH_NAME);
		intent.putExtra(AlbumCheckChangeReceiver.CHOOSE_COUNT_FLAG,
				mDataListDel.size());
		intent.putExtra(AlbumCheckChangeReceiver.TOTAL_COUNT, getCount());
		intent.putExtra(AlbumCheckChangeReceiver.IS_REFRESH, isRefresh);
		mContext.sendBroadcast(intent);
	}

	/** 删除选择的数据 */
	public void chooseDel() {
		if (mDataListDel != null) {
			String countTip = mContext.getString(R.string.delete_hint_album,
					mDataListDel.size() + "");
			final WaitingDialog waiting = new WaitingDialog(mContext);
			final DeleteHintDialog dialog = new DeleteHintDialog(mContext, countTip);
			dialog.setGoonCallBackListener(new GoonCallBackListener() {

				@Override
				public void onClick() {
					waiting.show();
					new Thread() {
						public void run() {
							final long startTime = System.currentTimeMillis();
							for (int i = 0; i < mDataListDel.size(); i++) {
								ImageBucket data = mDataListDel.get(i);
								for (int j = 0; j < data.imageList.size(); j++) {
									ImageChildItem item = data.imageList.get(j);
									if (FileUtil.DeleteFolder(item
											.getImagePath())) {
										mHelper.delete(item.getImageId());
									}
								}
							}
							if (mDataList.removeAll(mDataListDel)) {
								mDataListDel.clear();
							}
							FileUtil.scanSdCard(mContext);
							sendPictureBroadcast(true);
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									notifyDataSetChanged();
									long endTime = System.currentTimeMillis();
									if ((endTime - startTime) < 1000) {
										waiting.close();
									} else {
										waiting.dismiss();
									}
								}
							});
						};
					}.start();
				}
			});
			dialog.show();

		}
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
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.gridview_item_manager_picture, parent, false);
			holder.headerPadding = (LinearLayout) convertView.findViewById(R.id.headerPadding);
			holder.bottomPadding = (LinearLayout) convertView.findViewById(R.id.bottomPadding);
			holder.topPadding = (View) convertView.findViewById(R.id.topPadding);
			holder.content = (LinearLayout) convertView.findViewById(R.id.content);
			holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
			holder.isDel = (ImageButton) convertView.findViewById(R.id.isDel);
			holder.fileName = (TextView) convertView.findViewById(R.id.fileName);
			holder.picUnit = (TextView) convertView.findViewById(R.id.picture_unit);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (position == 0) {
			mCount++;
		} else {
			mCount = 0;
		}
		
		if (mCount <= 1 || (mCount == 1 && parent.getChildCount() == 0) || 
				position == parent.getChildCount()) {
			if (getCount() > 0) {
				// 设置头部间距
				if (position == 0 || position == 1) {
					holder.headerPadding.setVisibility(View.VISIBLE);
					holder.topPadding.setVisibility(View.VISIBLE);
				} else {
					holder.headerPadding.setVisibility(View.GONE);
					holder.topPadding.setVisibility(View.GONE);
				}
				// 设置底部间距
				if (position == (getCount() - 1)) {
					holder.bottomPadding.setVisibility(View.VISIBLE);
				} else {
					holder.bottomPadding.setVisibility(View.GONE);
				}
	
				ImageBucket item = mDataList.get(position);
				// 图片总数
				holder.picUnit.setText(mContext.getString(R.string.picture_unit, ""
						+ item.count));
				holder.fileName.setText(item.bucketName);
	
				// 是否选择
				boolean isChoose = mDataList.get(position).isSelected;
				if (isChoose) {
					holder.isDel.setImageResource(R.drawable.checkbox_pic_press);
				} else {
					holder.isDel.setImageResource(R.drawable.checkbox_pic_normal);
				}
	
				// 选择监听
				holder.isDel.setTag(R.id.tag_position, position);
				holder.isDel.setTag(R.id.tag_object, mDataList.get(position));
				holder.isDel.setOnClickListener(chooseListener);
	
				// 图片加载
				if (item.imageList != null && item.imageList.size() > 0) {
					String imgName = item.imageList.get(0).getDisplayName();
					String imgUrl = item.imageList.get(0).getImagePath();
					String filePath = item.imageList.get(0).getFilePath();
					holder.thumb.setTag(imgUrl);
					boolean isCache = true;
					Bitmap bm = AsyncImageManager.getInstance().loadAlbumImage(
							filePath, imgName, imgUrl, isCache,
							new AsyncImageLoadedCallBack() {
	
								@Override
								public void imageLoaded(Bitmap imageBitmap,
										String imgUrl) {
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
				}
			}
		}
		return convertView;
	}

	/**
	 * 选择监听
	 */
	private OnClickListener chooseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag(R.id.tag_position);
			ImageBucket mData = (ImageBucket) v.getTag(R.id.tag_object);
			boolean isChecked = mData.isSelected;
			mDataList.get(position).isSelected = !isChecked;

			if (mDataListDel != null) {
				isChecked = mDataList.get(position).isSelected;
				if (isChecked) {
					mDataListDel.add(mDataList.get(position));
				} else {
					for (int i = 0; i < mDataListDel.size(); i++) {
						if (mData.id.equals(mDataListDel.get(i).id)) {
							mDataListDel.remove(i);
							break;
						}
					}
				}
			}
			notifyDataSetChanged();
			sendPictureBroadcast(false);
		}
	};

	class ViewHolder {
		LinearLayout headerPadding; // 头部边界
		LinearLayout bottomPadding; // 底部边界
		View topPadding;
		LinearLayout content;
		ImageView thumb; // 缩略图
		ImageButton isDel; // 是否删除
		TextView fileName; // 文件夹名称
		TextView picUnit; // 图片张数

	}

}
