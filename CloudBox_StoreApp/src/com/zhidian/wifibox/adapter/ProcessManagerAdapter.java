package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.ProcessDataBean;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.Setting;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 进程管理适配器
 * 
 * @author xiedezhi
 * 
 */
public class ProcessManagerAdapter extends SectionedBaseAdapter {
	/**
	 * 用户程序
	 */
	private List<ProcessDataBean> mUserApp = new ArrayList<ProcessDataBean>();
	/**
	 * 系统程序
	 */
	private List<ProcessDataBean> mSysApp = new ArrayList<ProcessDataBean>();
	/**
	 * 保护点击事件
	 */
	private OnClickListener mLockClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ProcessDataBean bean = (ProcessDataBean) v.getTag();
			if (bean.mIsProtection) {
				bean.mIsProtection = false;
			} else {
				bean.mIsProtection = true;
			}
			Setting setting = new Setting(TAApplication.getApplication());
			String json = setting.getString(Setting.PROTECT_APP);
			JSONArray array = null;
			try {
				array = new JSONArray(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (array == null) {
				array = new JSONArray();
			}
			Set<String> protect = new HashSet<String>();
			for (int i = 0; i < array.length(); i++) {
				try {
					String packname = array.getString(i);
					protect.add(packname);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if (bean.mIsProtection) {
				protect.add(bean.mInfo.packageName);
			} else {
				protect.remove(bean.mInfo.packageName);
			}
			array = new JSONArray();
			for (String pkg : protect) {
				array.put(pkg);
			}
			setting.putString(Setting.PROTECT_APP, array.toString());
			notifyDataSetChanged();
		}
	};

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());

	@Override
	public Object getItem(int section, int position) {
		return null;
	}

	@Override
	public long getItemId(int section, int position) {
		return 0;
	}

	@Override
	public int getSectionCount() {
		return 2;
	}

	@Override
	public int getCountForSection(int section) {
		if (section == 0) {
			return mUserApp.size();
		} else if (section == 1) {
			return mSysApp.size();
		}
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * za.co.immedia.pinnedheaderlistview.SectionedBaseAdapter#getItemView(int,
	 * int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getItemView(int section, int position, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.process_item, null);
		}
		ProcessDataBean bean = null;
		if (section == 0) {
			bean = mUserApp.get(position);
		} else {
			bean = mSysApp.get(position);
		}
		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		icon.setTag(bean.mInfo.packageName);
		TextView name = (TextView) convertView.findViewById(R.id.name);
		TextView ram = (TextView) convertView.findViewById(R.id.ram);
		TextView cpu = (TextView) convertView.findViewById(R.id.cpu);
		Button lock = (Button) convertView.findViewById(R.id.lock);
		Bitmap bm = AsyncImageManager.getInstance().loadIcon(
				bean.mInfo.packageName, true, false,
				new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap != null && icon.getTag().equals(imgUrl)) {
							icon.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			icon.setImageBitmap(bm);
		} else {
			icon.setImageBitmap(DrawUtil.sDefaultIcon);
		}
		name.setText(bean.mName);
		ram.setText(FileUtil.convertFileSize(bean.mMemory));
		cpu.setText(bean.mCpuRate);
		if (bean.mIsProtection) {
			lock.setCompoundDrawablesWithIntrinsicBounds(
					null,
					TAApplication.getApplication().getResources()
							.getDrawable(R.drawable.icon_protect), null, null);
			lock.setText("已保护");
		} else {
			lock.setCompoundDrawablesWithIntrinsicBounds(
					null,
					TAApplication.getApplication().getResources()
							.getDrawable(R.drawable.icon_noprotect), null, null);
			lock.setText("未保护");
		}
		lock.setTag(bean);
		lock.setOnClickListener(mLockClickListener);
		return convertView;
	}

	@Override
	public View getSectionHeaderView(int section, View convertView,
			ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.process_title, null);
		}
		TextView title = (TextView) convertView.findViewById(R.id.text);
		title.setText("");
		title.setVisibility(View.GONE);
		return convertView;
	}

	/**
	 * 更新列表数据
	 */
	public void update(List<ProcessDataBean> userapp,
			List<ProcessDataBean> sysapp) {
		if (userapp == null) {
			userapp = new ArrayList<ProcessDataBean>();
		}
		if (sysapp == null) {
			sysapp = new ArrayList<ProcessDataBean>();
		}
		mUserApp.clear();
		mSysApp.clear();
		mUserApp.addAll(userapp);
		mSysApp.addAll(sysapp);
		notifyDataSetChanged();
	}

	/**
	 * 获取数据列表
	 */
	public List<ProcessDataBean> getDataList() {
		List<ProcessDataBean> list = new ArrayList<ProcessDataBean>();
		list.addAll(mUserApp);
		list.addAll(mSysApp);
		return list;
	}
}
