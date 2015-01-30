package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.file.album.AlbumHelper;
import com.zhidian.wifibox.file.album.ImageItem;
import com.zhidian.wifibox.file.album.ImageItemGroup;
import com.zhidian.wifibox.receiver.AlbumCheckChangeReceiver;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.IntentUtils;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog.GoonCallBackListener;
import com.zhidian.wifibox.view.dialog.WaitingDialog;

/**
 * 查看图片详细适配器
 * 
 * @author shihuajian
 *
 */
public class ManagerPicDetailsAdapter extends BaseExpandableListAdapter {
	
	private final static String TAG = ManagerPicDetailsAdapter.class.getSimpleName();
	private Context mContext;
	/** 分类数据 */
	private List<ImageItemGroup> mCategoryData;
	private List<ImageItemGroup> mCategoryDataDel;
	private ManagerPicDetailsGridAdapter mGridAdapter;
	private AlbumHelper mHelper;
	
	/** 组ID */
	public final static String GROUP_POSITION = "group_position";
	/** 子ID */
	public final static String CHILDREN_POSITION = "children_position";
	
	/** 与GridView适配器通信的Handler */
	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			if (bundle != null) {
				int groupPosition = bundle.getInt(GROUP_POSITION, -1);
				int childrenPosition = bundle.getInt(CHILDREN_POSITION, -1);
				
				// 实现反选
				ImageItem mData = mCategoryData.get(groupPosition).getChildren().get(childrenPosition);
				boolean isChecked = mData.getIsSelected();
				mCategoryData.get(groupPosition).getChildren().get(childrenPosition).setIsSelected(!isChecked);
				
				if (mCategoryDataDel != null) {
					isChecked = mCategoryData.get(groupPosition).getChildren().get(childrenPosition).getIsSelected();
					// 如果为true，则把数据添加到mCategoryDataDel
					if (isChecked) {
						// 如果是第一条，则添加一个ImageItemGroup
						if (mCategoryDataDel != null && mCategoryDataDel.size() == 0) {
							addNewGroup(groupPosition, childrenPosition);
						} else {
							int isExist = -1;
							// 判断要添加的数据是否存在Group
							for (int i = 0; i < mCategoryDataDel.size(); i++) {
								String delGroup = mCategoryDataDel.get(i).getGroup();
								String sourceGroup = mCategoryData.get(groupPosition).getGroup();
								if (sourceGroup.equals(delGroup)) {
									isExist = i;
									break;
								}
							}
							// 是否存在该Group数据，存在则直接添加子项，否则添加一个新的Group
							if (isExist != -1) {
								ImageItem imgItem = mCategoryData.get(groupPosition).getChildren().get(childrenPosition);
								List<ImageItem> item = new ArrayList<ImageItem>();
								item.add(imgItem);
								mCategoryDataDel.get(isExist).getChildren().addAll(item);
							} else {
								addNewGroup(groupPosition, childrenPosition);
							}
							
							// 设置group的数据当前的选择状态
							for (int i = 0; i < mCategoryDataDel.size(); i++) {
								String sourceGroup = mCategoryData.get(groupPosition).getGroup();
								String delGroup = mCategoryDataDel.get(i).getGroup();
								if (sourceGroup.equals(delGroup)) {
									int sourceSize = mCategoryData.get(groupPosition).getChildren().size();
									int delSize = mCategoryDataDel.get(i).getChildren().size();
									if (delSize == sourceSize) {
										mCategoryDataDel.get(i).setIsSelected(true);
										mCategoryData.get(groupPosition).setIsSelected(true);
									} else {
										mCategoryDataDel.get(i).setIsSelected(false);
										mCategoryData.get(groupPosition).setIsSelected(false);
									}
									break;
								}
							}
						}
					} else {
						String sourceGroup = mCategoryData.get(groupPosition).getGroup();
						for (int i = 0; i < mCategoryDataDel.size(); i++) {
							String delGroup = mCategoryDataDel.get(i).getGroup();
							if (sourceGroup.equals(delGroup)) {
								int delSize = mCategoryDataDel.get(i).getChildren().size();
								if (delSize == 1) {
									mCategoryData.get(groupPosition).setIsSelected(false);
									mCategoryDataDel.remove(i);
								} else {
									mCategoryData.get(groupPosition).setIsSelected(false);
									String sourceImgId = mCategoryData.get(groupPosition).getChildren().get(childrenPosition).getImageId();
									for (int j = 0; j < mCategoryDataDel.get(i).getChildren().size(); j++) {
										String delImgId = mCategoryDataDel.get(i).getChildren().get(j).getImageId();
										if (sourceImgId.equals(delImgId)) {
											mCategoryDataDel.get(i).getChildren().remove(j);
											break;
										}
									}
								}
								break;
							}
						}
					}
				}
				notifyDataSetChanged();
				sendPictureBroadcast();
			}
		}

		/**
		 * 添加一个新的Group
		 * @param groupPosition			组位置
		 * @param childrenPosition		子位置
		 */
		private void addNewGroup(int groupPosition, int childrenPosition) {
			ImageItemGroup iGroup = mCategoryData.get(groupPosition);
			
			List<ImageItem> children = new ArrayList<ImageItem>();
			children.add(iGroup.getChildren().get(childrenPosition));
			
			ImageItemGroup copyIGroup = new ImageItemGroup();
			copyIGroup.setCount(iGroup.getCount());
			copyIGroup.setGroup(iGroup.getGroup());
			copyIGroup.setIsSelected(iGroup.getIsSelected());
			copyIGroup.setChildren(children);
			
			mCategoryDataDel.add(copyIGroup);
		};
		
	};
	
	private Handler mHandlerUpdate = new Handler(Looper.getMainLooper());
	
	public ManagerPicDetailsAdapter() {}
	
	public ManagerPicDetailsAdapter(Context cxt) {
		this.mContext = cxt;
		this.mCategoryData = new ArrayList<ImageItemGroup>();
		this.mCategoryDataDel = new ArrayList<ImageItemGroup>();
		this.mHelper = AlbumHelper.getInstance();
		this.mHelper.init(cxt);
	}
	
	/**
	 * 
	 * @param cxt 上下文菜单
	 * @param mCategoryData 文件夹数据
	 * @param isChoose 文件夹是否被全选
	 */
	public ManagerPicDetailsAdapter(Context cxt, List<ImageItemGroup> mCategoryData, boolean isChoose, AlbumHelper helper) {
		this.mContext = cxt;
		this.mHelper = helper;
		this.mCategoryData = mCategoryData;
		if (isChoose) {
			this.mCategoryDataDel = new ArrayList<ImageItemGroup>();
			copyCategoryDataToDel();
		} else {
			this.mCategoryDataDel = new ArrayList<ImageItemGroup>();
		}
	}
	
	/**
	 * 刷新数据
	 * @param FileList
	 * @param isFirst	是否第一次调用改方法
	 * @param isSelected 初次进入页面是否全选
	 */
	public void refreshAdapter(List<ImageItemGroup> FileList, boolean isFirst, boolean isSelected) {
		mCategoryData = FileList;
		if (isFirst && isSelected) {
			copyCategoryDataToDel();
		} else {
			mCategoryDataDel.clear();
		}
		sendPictureBroadcast();
		notifyDataSetChanged();
	}
	
	/** 实现全选 */
	public void chooseAll(boolean isAll) {
		if (mCategoryData != null) {
			for (int i = 0; i < mCategoryData.size(); i++) {
				ImageItemGroup data = mCategoryData.get(i);
				data.setIsSelected(isAll);
				for (ImageItem item : data.getChildren()) {
					item.setIsSelected(isAll);
				}
			}
			
			if (mCategoryDataDel != null) {
				if (isAll) {
					mCategoryDataDel.clear();
					copyCategoryDataToDel();
				} else {
					mCategoryDataDel.clear();
				}
			}
			notifyDataSetChanged();
			sendPictureBroadcast();
		}
	}
	
	/** 如果为true的话，把数据复制到mCategoryDataDel */
	private void copyCategoryDataToDel() {
		if (mCategoryData != null && mCategoryData.size() > 0) {
			for (int i = 0; i < mCategoryData.size(); i++) {
				List<ImageItem> children = new ArrayList<ImageItem>();
				children.addAll(mCategoryData.get(i).getChildren());
				int count = mCategoryData.get(i).getCount();
				String group = mCategoryData.get(i).getGroup();
				boolean isSelected = mCategoryData.get(i).getIsSelected();
				ImageItemGroup iGroup = new ImageItemGroup();
				iGroup.setChildren(children);
				iGroup.setCount(count);
				iGroup.setGroup(group);
				iGroup.setIsSelected(isSelected);
				this.mCategoryDataDel.add(iGroup);
			}
		}

	}

	/**
	 * 发送选择广播
	 */
	private void sendPictureBroadcast() {
		Intent intent = new Intent(AlbumCheckChangeReceiver.PATH_NAME2);
		intent.putExtra(AlbumCheckChangeReceiver.CHOOSE_COUNT_FLAG, getDelCount(mCategoryDataDel));
		intent.putExtra(AlbumCheckChangeReceiver.TOTAL_COUNT, getDelCount(mCategoryData));
		mContext.sendBroadcast(intent);
	}
	
	/** 删除选择的数据 */
	public void chooseDel() {
		if (mCategoryDataDel != null) {
			String countTip = mContext.getString(R.string.delete_hint_picture, getDelCount(mCategoryDataDel) + "");
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
				for (int i = 0; i < mCategoryDataDel.size(); i++) {
					ImageItemGroup data = mCategoryDataDel.get(i);
					for (int j = 0; j < data.getChildren().size(); j++) {
						ImageItem item = data.getChildren().get(j);
						if (FileUtil.DeleteFolder(item.getImagePath())) {
							mHelper.delete(item.getImageId());
							Log.e(TAG, "删除“" + item.getImagePath() + "”成功");

							String delGroup = data.getGroup();
							for (int k = 0; k < mCategoryData.size(); k++) {
								ImageItemGroup group = mCategoryData.get(k);
								String sourceGroup = group.getGroup();
								if (delGroup.equals(sourceGroup)) {
									if (group.getChildren().size() == 1) {
										mCategoryData.remove(k);
									} else {
										String delImgId = item.getImageId();
										for (int l = 0; l < group.getChildren().size(); l++) {
											String sourceImgId = group.getChildren()
													.get(l).getImageId();
											if (delImgId.equals(sourceImgId)) {
												mCategoryData.get(k).getChildren()
														.remove(l);
												break;
											}
										}
									}
									break;
								}
							}
						} else {
							Log.e(TAG, "删除“" + item.getImagePath() + "”失败");
						}
					}
				}
				mCategoryDataDel.clear();
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
			};
		}.start();;
		

	}
	
	/** 获取数据的总数 */
	private int getDelCount(List<ImageItemGroup> data) {
		int count = 0;
		for (int i = 0; i < data.size(); i++) {
			List<ImageItem> item = data.get(i).getChildren();
			count = count + item.size();
		}
		return count;

	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return mCategoryData.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		if (getGroupCount() > 0) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return mCategoryData.get(groupPosition);
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return mCategoryData.get(groupPosition).getChildren().get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		GroupHolder holder;
		if (convertView == null) {
			holder = new GroupHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_image_group, parent, false);
			holder.mTime = (TextView) convertView.findViewById(R.id.time);
			holder.mIsSelect = (TextView) convertView.findViewById(R.id.select);
			convertView.setTag(holder);
		}else {
			holder = (GroupHolder) convertView.getTag();
		}
		
		// 如果只有一条信息，则隐藏这个全选按钮
		if (getGroupCount() == 1) {
			holder.mIsSelect.setVisibility(View.GONE);
		} else {
			holder.mIsSelect.setVisibility(View.VISIBLE);
		}
		
		holder.mTime.setText(mCategoryData.get(groupPosition).getGroup());
		
		// 选择设置
		boolean isSelected = mCategoryData.get(groupPosition).getIsSelected();
		if (isSelected) {
			holder.mIsSelect.setText(R.string.manager_uncheck);
		} else {
			holder.mIsSelect.setText(R.string.manager_check);
		}
		holder.mIsSelect.setTag(R.id.tag_position, groupPosition);
		holder.mIsSelect.setTag(R.id.tag_object, mCategoryData.get(groupPosition));
		holder.mIsSelect.setOnClickListener(chooseListener);
		
		return convertView;
	}
	
	/** 内部全选按钮监听 */
	private OnClickListener chooseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag(R.id.tag_position);
			ImageItemGroup mData = (ImageItemGroup) v.getTag(R.id.tag_object);
			boolean isChecked = mData.getIsSelected();
			mCategoryData.get(position).setIsSelected(!isChecked);

			if (mCategoryDataDel != null) {
				isChecked = mCategoryData.get(position).getIsSelected();
				if (isChecked) {
					ImageItemGroup iGroup = mCategoryData.get(position);
					// 子类全选
					for (ImageItem items : iGroup.getChildren()) {
						items.setIsSelected(isChecked);
					}
					
					// 如果存在相等的，则清理掉
					String sourceGroup = iGroup.getGroup();
					for (int i = 0; i < mCategoryDataDel.size(); i++) {
						String delGroup = mCategoryDataDel.get(i).getGroup();
						if (sourceGroup.equals(delGroup)) {
							mCategoryDataDel.remove(i);
							break;
						}
					}
					
					List<ImageItem> children = new ArrayList<ImageItem>();
					children.addAll(iGroup.getChildren());
					
					ImageItemGroup copyIGroup = new ImageItemGroup();
					copyIGroup.setCount(iGroup.getCount());
					copyIGroup.setGroup(iGroup.getGroup());
					copyIGroup.setIsSelected(iGroup.getIsSelected());
					copyIGroup.setChildren(children);
					
					mCategoryDataDel.add(copyIGroup);
				} else {
					for (int i = 0; i < mCategoryDataDel.size(); i++) {
						if (mData.getGroup().equals(mCategoryDataDel.get(i).getGroup())) {
							ImageItemGroup item = mCategoryData.get(position);
							// 子类全反选
							for (ImageItem items : item.getChildren()) {
								items.setIsSelected(isChecked);
							}
							mCategoryDataDel.remove(i);
							break;
						}
					}
				}
			}
			notifyDataSetChanged();
			sendPictureBroadcast();
		}

	};

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ChildrenHolder holder;
		if (convertView == null) {
			holder = new ChildrenHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_image_children, parent, false);
			holder.mImage = (GridView) convertView.findViewById(R.id.imageChildren);
			convertView.setTag(holder);
		}else {
			holder = (ChildrenHolder) convertView.getTag();
		}
		
		mGridAdapter = new ManagerPicDetailsGridAdapter(mContext, mCategoryData.get(groupPosition).getChildren(), mHandler, groupPosition);
		holder.mImage.setAdapter(mGridAdapter);
		holder.mImage.setOnItemClickListener(gridItemListener);
		
		return convertView;
	}
	
	/** 设置Item点击监听 */
	private OnItemClickListener gridItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ImageItem item = (ImageItem) parent.getAdapter().getItem(position);
			String path = item.getImagePath();
			File file = new File(path);
			Intent intent = IntentUtils.createFileOpenIntent(file);
			mContext.startActivity(intent);
		}
	};

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

	class GroupHolder {
		public TextView mTime;
		public TextView mIsSelect;
	}

	class ChildrenHolder {
		public GridView mImage;
	}

}
