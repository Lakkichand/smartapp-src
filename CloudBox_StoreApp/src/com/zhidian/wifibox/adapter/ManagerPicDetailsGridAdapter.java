package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.file.album.ImageItem;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 
 * 图片详细管理目录适配器
 * 
 * @author shihuajian
 *
 */
public class ManagerPicDetailsGridAdapter extends BaseAdapter {
	private static final String TAG = ManagerPicDetailsGridAdapter.class.getSimpleName();  
	private Context mContext;
	private List<ImageItem> mDataList;
	private int groupPosition = -1;
	private Handler mHandler;
	
	public ManagerPicDetailsGridAdapter(Context context) {
		this.mContext = context;
		this.mDataList = new ArrayList<ImageItem>();
		this.mHandler = new Handler();
	}
	
	public ManagerPicDetailsGridAdapter(Context context, List<ImageItem> dataList) {
		this.mContext = context;
		this.mDataList = dataList;
		this.mHandler = new Handler();
	}
	
	public ManagerPicDetailsGridAdapter(Context context, List<ImageItem> dataList, Handler handler, int groupPosition) {
		this.mContext = context;
		this.mDataList = dataList;
		this.mHandler = handler;
		this.groupPosition = groupPosition;
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
							.inflate(R.layout.gridview_item_image_children, parent, false);
			holder.thumb = (ImageView) convertView.findViewById(R.id.thumb);
			holder.isSelect = (ImageButton) convertView.findViewById(R.id.isSelect);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (getCount() > 0) {
			String imgName = mDataList.get(position).getDisplayName();
			String imgUrl = mDataList.get(position).getImagePath();
			String filePath = mDataList.get(position).getFilePath();
			holder.thumb.setTag(imgUrl);
			boolean isCache = true;
			Bitmap bm = AsyncImageManager.getInstance().loadAlbumImage(filePath, 
										imgName, imgUrl, isCache, 
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
			
			// 选择设置
			boolean isSelected = mDataList.get(position).getIsSelected();
			if (isSelected) {
				holder.isSelect.setImageResource(R.drawable.checkbox_pic_press);
			} else {
				holder.isSelect.setImageResource(R.drawable.checkbox_pic_normal);
			}
			holder.isSelect.setTag(R.id.tag_position, position);
			holder.isSelect.setOnClickListener(chooseListener);
		}
		
		return convertView;
	}
	
	/** 选择按钮监听 */
	private OnClickListener chooseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag(R.id.tag_position);
			
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putInt(ManagerPicDetailsAdapter.GROUP_POSITION, groupPosition);
			bundle.putInt(ManagerPicDetailsAdapter.CHILDREN_POSITION, position);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}

	};

	static class ViewHolder {
		ImageView thumb; // 缩略图
		ImageButton isSelect; // 是否删除

	}

}
