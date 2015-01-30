package com.zhidian.wifibox.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.AppUninstallBean;
import com.zhidian.wifibox.data.AppUninstallGroup;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 用户应用卸载适配器
 * 
 * @author xiedezhi
 * 
 */
public class UserAppUninstallAdapter extends BaseExpandableListAdapter {

	private List<AppUninstallGroup> mList = new ArrayList<AppUninstallGroup>();

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());

	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * 全选点击事件
	 */
	private OnClickListener mSelectAllListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppUninstallGroup bean = (AppUninstallGroup) v.getTag();
			boolean allselect = true;
			for (AppUninstallBean ab : bean.mList) {
				if (!ab.isSelect) {
					allselect = false;
					break;
				}
			}
			if (!allselect) {
				for (AppUninstallBean ab : bean.mList) {
					ab.isSelect = true;
				}
			} else {
				for (AppUninstallBean ab : bean.mList) {
					ab.isSelect = false;
				}
			}
			notifyDataSetChanged();
			TAApplication.sendHandler(null, IDiyFrameIds.APPUNINSTALL,
					IDiyMsgIds.UPDATE_UNINSTALL_BTN, 0, null, null);
		}
	};

	/**
	 * 单选点击事件
	 */
	private OnClickListener mSelectItemListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			AppUninstallBean bean = (AppUninstallBean) v.getTag();
			bean.isSelect = !bean.isSelect;
			notifyDataSetChanged();
			TAApplication.sendHandler(null, IDiyFrameIds.APPUNINSTALL,
					IDiyMsgIds.UPDATE_UNINSTALL_BTN, 0, null, null);
		}
	};

	/**
	 * 更新全选/取消全选
	 */
	private void updateSelectText(AppUninstallGroup bean, TextView v) {
		try {
			boolean allselect = true;
			for (AppUninstallBean ab : bean.mList) {
				if (!ab.isSelect) {
					allselect = false;
					break;
				}
			}
			if (!allselect) {
				v.setText("全选");
			} else {
				v.setText("取消全选");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getGroupCount() {
		return mList.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mList.get(groupPosition).mList.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.userappuninstalltitle,
					null);
		}
		AppUninstallGroup bean = mList.get(groupPosition);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		title.setText(bean.mTitle);
		TextView select = (TextView) convertView.findViewById(R.id.select);
		select.setTag(bean);
		select.setOnClickListener(mSelectAllListener);
		updateSelectText(bean, select);
		View gap = convertView.findViewById(R.id.gap);
		if (groupPosition == 0) {
			gap.setVisibility(View.VISIBLE);
		} else {
			gap.setVisibility(View.GONE);
		}
		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.userappuninstallitem, null);
		}
		AppUninstallBean bean = mList.get(groupPosition).mList
				.get(childPosition);
		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		icon.setTag(bean.packname);
		Bitmap bm = AsyncImageManager.getInstance().loadIcon(bean.packname,
				true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap != null && imgUrl.equals(icon.getTag())) {
							icon.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm == null) {
			icon.setImageBitmap(DrawUtil.sDefaultIcon);
		} else {
			icon.setImageBitmap(bm);
		}
		TextView title = (TextView) convertView.findViewById(R.id.title);
		title.setText(bean.appname);
		TextView info = (TextView) convertView.findViewById(R.id.info);
		AppUninstallGroup group = mList.get(groupPosition);
		if (group.type == 2) {
			Date curDate = new Date(bean.installTime);
			String nowTime = formatter.format(curDate);
			// 显示安装时间
			info.setText("安装时间:"
					+ nowTime
					+ "   占用:"
					+ Formatter.formatShortFileSize(
							TAApplication.getApplication(), bean.size));
		} else {
			// 显示上次打开时间
			info.setText("最近使用时间:"
					+ (bean.lastOpenTime <= 0 ? "未记录" : formatter
							.format(new Date(bean.lastOpenTime)))
					+ "   占用:"
					+ Formatter.formatShortFileSize(
							TAApplication.getApplication(), bean.size));
		}
		ImageView select = (ImageView) convertView.findViewById(R.id.select);
		if (bean.isSelect) {
			select.setImageResource(R.drawable.cleanmaster_select);
		} else {
			select.setImageResource(R.drawable.cleanmaster_noselect);
		}
		select.setTag(bean);
		select.setOnClickListener(mSelectItemListener);
		View gap3 = convertView.findViewById(R.id.gap3);
		if (isLastChild) {
			gap3.setVisibility(View.VISIBLE);
		} else {
			gap3.setVisibility(View.GONE);
		}
		View line = convertView.findViewById(R.id.line);
		if (isLastChild) {
			line.setVisibility(View.GONE);
		} else {
			line.setVisibility(View.VISIBLE);
		}
		View gap4 = convertView.findViewById(R.id.gap4);
		if (groupPosition == getGroupCount() - 1 && isLastChild) {
			gap4.setVisibility(View.VISIBLE);
		} else {
			gap4.setVisibility(View.GONE);
		}
		return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	/**
	 * 更新数据
	 */
	public void update(List<AppUninstallGroup> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

}
