package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.file.album.AlbumHelper;
import com.zhidian.wifibox.file.album.HeaderBean;
import com.zhidian.wifibox.file.album.ImageChildItem;
import com.zhidian.wifibox.receiver.AlbumCheckChangeReceiver;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.TimeTool;
import com.zhidian.wifibox.view.StickyImageView;
import com.zhidian.wifibox.view.StickyImageView.OnMeasureListener;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog.GoonCallBackListener;
import com.zhidian.wifibox.view.dialog.WaitingDialog;
import com.zhidian.wifibox.view.stickygrid.StickyGridHeadersSimpleAdapter;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 图片文件夹详细页面适配器
 * 
 * @author shihuajian
 *
 */

public class StickyGridAdapter extends BaseAdapter implements
		StickyGridHeadersSimpleAdapter {
	
	private final static String TAG = StickyGridAdapter.class.getSimpleName();

	private Context mContext;
	private List<ImageChildItem> mData = new ArrayList<ImageChildItem>();
	private List<ImageChildItem> mDataDel = new ArrayList<ImageChildItem>();
	private SparseArray<HeaderBean> mSectionMap = new SparseArray<HeaderBean>();
	private LayoutInflater mInflater;
	private GridView mGridView;
	private Point mPoint = new Point(0, 0);	// 用来封装ImageView的宽和高的对象 
	private Handler mHandlerUpdate = new Handler(Looper.getMainLooper());
	private AlbumHelper mHelper;

	public StickyGridAdapter(Context context, List<ImageChildItem> list,
			GridView mGridView, AlbumHelper helper) {
		this.mContext = context;
		this.mData = list;
		this.mHelper = helper;
		mInflater = LayoutInflater.from(context);
		this.mGridView = mGridView;
	}

	/**
	 * 刷新数据
	 * @param mCategoryData
	 * @param sectionMap
	 * @param isFirst
	 * @param isSelect
	 */
	public void refreshAdapter(List<ImageChildItem> mCategoryData, SparseArray<HeaderBean> sectionMap,
			boolean isFirst, boolean isSelect) {
		this.mData = mCategoryData;
		this.mSectionMap = sectionMap;
		if (isSelect) {
			mDataDel.clear();
			mDataDel.addAll(mData);
		}
		notifyDataSetChanged();
		sendPictureBroadcast();
	}

	/**
	 * 发送选择广播
	 */
	private void sendPictureBroadcast() {
		int totalCount = mData.size();
		int delCount = mDataDel.size();
		Intent intent = new Intent(AlbumCheckChangeReceiver.PATH_NAME2);
		intent.putExtra(AlbumCheckChangeReceiver.CHOOSE_COUNT_FLAG, delCount);
		intent.putExtra(AlbumCheckChangeReceiver.TOTAL_COUNT, totalCount);
		mContext.sendBroadcast(intent);
	}
	
	/** 实现全选/反选 */
	public void chooseAll(boolean isAll) {
		if (mData != null) {
			// 设置所有数据的选择状态
			for (ListIterator<ImageChildItem> it = mData.listIterator(); it.hasNext();) {
				ImageChildItem item = it.next();
				item.setIsSelected(isAll);
			}
			
			// 设置头部选择状态
			int headerCount = mSectionMap.size();
			for (int i = 0; i < headerCount; i++) {
				HeaderBean bean = mSectionMap.get(i);
				if (bean != null) {
					bean.setIsSelect(isAll);
				}
			}
			
			// 设置选择的数据
			mDataDel.clear();
			if (isAll) {
				mDataDel.addAll(mData);
			}
			
			notifyDataSetChanged();
			sendPictureBroadcast();
		}
	}
	
	/** 删除选择的数据 */
	public void chooseDel() {
		if (mData != null) {
			int delCount = mDataDel.size();
			String countTip = mContext.getString(R.string.delete_hint_picture, delCount + "");
			DeleteHintDialog dialog = new DeleteHintDialog(mContext, countTip);
			dialog.setGoonCallBackListener(new GoonCallBackListener() {
				
				@Override
				public void onClick() {
					delAll();
				}
			});
			dialog.show();
		}
	}
	
	/** 删除所有选择的数据 */
	private void delAll() {
		final WaitingDialog waiting = new WaitingDialog(mContext);
		waiting.show();
		// 遍历要删除的数据
		new Thread() {
			public void run() {
				final long startTime = System.currentTimeMillis();
				delData();
				mDataDel.clear();
				FileUtil.scanSdCard(mContext);
				sendPictureBroadcast();
				mHandlerUpdate.post(new Runnable() {
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
			}

			/**
			 * 删除数据
			 */
			private void delData() {
				for (ListIterator<ImageChildItem> it = mDataDel.listIterator(); it.hasNext();) {
					ImageChildItem item = it.next();
					String sPath = item.getImagePath();
					if (FileUtil.DeleteFolder(sPath)) {
						mHelper.delete(item.getImageId());
						mData.remove(item);
						
						updateDelHeaderData(item);
						
						Log.e(TAG, "删除“" + item.getImagePath() + "”成功");
					} else {
						Log.e(TAG, "删除“" + item.getImagePath() + "”失败");
					}
				}
			}
			
			/**
			 * 删除数据后更新头部数据
			 */
			private void updateDelHeaderData(ImageChildItem item) {
				int section = item.getSection();
				HeaderBean bean = mSectionMap.get(section);
				int sTotal = bean.getSectionTotal();
				sTotal = sTotal - 1;
				if (sTotal == 0) {
					mSectionMap.remove(section);
				} else {
					bean.setSectionTotal(sTotal);
					bean.setIsSelect(false);
				}
			};
		}.start();;

	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ChildViewHolder mViewHolder;
		if (convertView == null) {
			mViewHolder = new ChildViewHolder();
			convertView = mInflater.inflate(R.layout.gridview_item_image_children, parent, false);
			mViewHolder.mImageView = (StickyImageView) convertView.findViewById(R.id.thumb);
			mViewHolder.mIsSelect = (ImageButton) convertView.findViewById(R.id.isSelect);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ChildViewHolder) convertView.getTag();
		}
		
		// 用来监听ImageView的宽和高
		mViewHolder.mImageView.setOnMeasureListener(new OnMeasureListener() {  
             
           @Override  
           public void onMeasureSize(int width, int height) {  
               mPoint.set(width, height);  
           }  
		});
		
		// 单选
		boolean isSelect = mData.get(position).getIsSelected();
		if (isSelect) {
			mViewHolder.mIsSelect.setImageResource(R.drawable.checkbox_pic_press);;
		} else {
			mViewHolder.mIsSelect.setImageResource(R.drawable.checkbox_pic_normal);
		}
		mViewHolder.mIsSelect.setTag(position);
		mViewHolder.mIsSelect.setOnClickListener(singelSelectListener);

		String path = mData.get(position).getImagePath();
		mViewHolder.mImageView.setTag(path);
		String imgName = mData.get(position).getDisplayName();
		String imgUrl = mData.get(position).getImagePath();
		String filePath = mData.get(position).getFilePath();
		mViewHolder.mImageView.setTag(imgUrl);
		boolean isCache = true;
		Bitmap bm = AsyncImageManager.getInstance().loadAlbumImage(filePath, imgName,
				imgUrl, isCache, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (mViewHolder.mImageView.getTag().equals(imgUrl)) {
							mViewHolder.mImageView.setImageBitmap(imageBitmap);
						}

					}
				});
		if (bm != null) {
			mViewHolder.mImageView.setImageBitmap(bm);
		} else {
			// 默认
			mViewHolder.mImageView.setImageBitmap(DrawUtil.sDefaultPicture);
		}

		return convertView;
	}
	
	/**
	 * 单选监听
	 */
	private OnClickListener singelSelectListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			
			ImageChildItem item = mData.get(position);
			boolean isSelect = item.getIsSelected();
			item.setIsSelected(!isSelect);
			
			if (isSelect) {
				mDataDel.remove(item);
			} else {
				mDataDel.add(item);
			}
			
			int headerId = (int) getHeaderId(position);
			int selectCount = getHeaderCount(headerId);
			int selectTotal = getHeaderCountTotal(headerId);
			if (selectCount == selectTotal) {
				mSectionMap.get(headerId).setIsSelect(true);
			} else {
				mSectionMap.get(headerId).setIsSelect(false);
			}
			
			notifyDataSetChanged();
			sendPictureBroadcast();
		}
	};
	
	@Override
	public long getHeaderId(int position) {
		return mData.get(position).getSection();
	}
	
	/**
	 * 获取分类内部选择图片数量
	 * 
	 * @param headerId	头部ID
	 * @return 返回内部选择的数量
	 */
	public int getHeaderCount(int headerId) {
		int count = 0;
		for (ListIterator<ImageChildItem> it = mData.listIterator(); it.hasNext();) {
			ImageChildItem item = it.next();
			int section = item.getSection();
			boolean isSelect = item.getIsSelected();
			if (section == headerId && isSelect) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 获取分类内部图片总数量
	 * @param headerId	头部ID
	 * @return	返回总数量
	 */
	public int getHeaderCountTotal(int headerId) {
		return mSectionMap.get(headerId).getSectionTotal();
	}
	
	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		HeaderViewHolder mHeaderHolder;
		if (convertView == null) {
			mHeaderHolder = new HeaderViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_image_group, parent, false);
			mHeaderHolder.mTextView = (TextView) convertView.findViewById(R.id.time);
			mHeaderHolder.mSelect = (TextView) convertView.findViewById(R.id.select);
			mHeaderHolder.mTop = (View) convertView.findViewById(R.id.top);
			convertView.setTag(mHeaderHolder);
		} else {
			mHeaderHolder = (HeaderViewHolder) convertView.getTag();
		}
		
		int headerId = (int) getHeaderId(position);
		
		if (headerId == 0) {
			mHeaderHolder.mTop.setVisibility(View.GONE);
		} else {
			mHeaderHolder.mTop.setVisibility(View.VISIBLE);
		}

		String dateTaken = mData.get(position).getDateTaken();
		mHeaderHolder.mTextView.setText(TimeTool.timestampToString(dateTaken));
		
		boolean isSelect = mSectionMap.get(headerId).getIsSelect();
		if (isSelect) {
			mHeaderHolder.mSelect.setText(mContext.getString(R.string.manager_uncheck));
		} else {
			mHeaderHolder.mSelect.setText(mContext.getString(R.string.manager_check));
		}
		
		mHeaderHolder.mSelect.setTag(R.id.tag_object, headerId);
		mHeaderHolder.mSelect.setTag(R.id.tag_position, position);
		mHeaderHolder.mSelect.setOnClickListener(selectListener);

		return convertView;
	}
	
	/** 内部全选/反选按钮监听 */
	private OnClickListener selectListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag(R.id.tag_position);
			int headerId = (Integer) v.getTag(R.id.tag_object);
			boolean isSelect = mSectionMap.get(headerId).getIsSelect();
			List<ImageChildItem> list = new ArrayList<ImageChildItem>();
			for (ImageChildItem item : mData) {
				int secition = item.getSection();
				if (secition == headerId) {
					item.setIsSelected(!isSelect);
					list.add(item);
				}
			}
			if (isSelect) {
				mDataDel.removeAll(list);
			} else {
				mDataDel.removeAll(list);
				mDataDel.addAll(list);
			}
			
			mSectionMap.get(headerId).setIsSelect(!isSelect);
			
			notifyDataSetChanged();
			sendPictureBroadcast();
		}

	};

	class ChildViewHolder {
		public StickyImageView mImageView;
		public ImageButton mIsSelect;
	}

	class HeaderViewHolder {
		public TextView mTextView;
		public TextView mSelect;
		public View mTop;
	}

}
