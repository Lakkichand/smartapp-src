package com.smartapp.autostartmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

public class MainAdapter extends BaseAdapter {

	private List<DataBean> mList = new ArrayList<DataBean>();

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());

	private Handler mHandler;

	private OnClickListener mDisableListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 禁用
			mHandler.obtainMessage(MainActivity.MSG_DISABLE, v.getTag())
					.sendToTarget();
		}
	};

	private OnClickListener mEnableListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 启用
			mHandler.obtainMessage(MainActivity.MSG_ENABLE, v.getTag())
					.sendToTarget();
		}
	};

	public MainAdapter(Handler handler) {
		mHandler = handler;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.process_item, null);
		}
		DataBean bean = mList.get(position);
		View gap = convertView.findViewById(R.id.gap);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		if (bean.mIsFirst) {
			if (position == 0) {
				gap.setVisibility(View.GONE);
			} else {
				gap.setVisibility(View.VISIBLE);
			}
			title.setVisibility(View.VISIBLE);
			if (!bean.mIsForbid) {
				title.setText(TAApplication.getApplication().getString(
						R.string.title1)
						+ "(" + getEnableCount() + ")");
			} else {
				title.setText(TAApplication.getApplication().getString(
						R.string.title2)
						+ "(" + getDisableCount() + ")");
			}
		} else {
			gap.setVisibility(View.GONE);
			title.setVisibility(View.GONE);
		}
		final ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		icon.setTag(bean.mInfo.packageName);
		Bitmap bm = AsyncImageManager.getInstance().loadIcon(
				bean.mInfo.packageName, true, new AsyncImageLoadedCallBack() {

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
		ImageView newicon = (ImageView) convertView.findViewById(R.id.newicon);
		if (bean.mIsNew) {
			newicon.setVisibility(View.VISIBLE);
		} else {
			newicon.setVisibility(View.GONE);
		}
		TextView name = (TextView) convertView.findViewById(R.id.name);
		name.setText(bean.mName);
		TextView tag = (TextView) convertView.findViewById(R.id.tag);
		if (bean.mIsSysApp) {
			tag.setVisibility(View.VISIBLE);
		} else {
			tag.setVisibility(View.GONE);
		}
		View appInfo = convertView.findViewById(R.id.app_info);
		if (bean.mIsForbid) {
			appInfo.setVisibility(View.GONE);
		} else {
			appInfo.setVisibility(View.VISIBLE);
		}
		TextView ram = (TextView) convertView.findViewById(R.id.ram);
		ram.setText(TAApplication.getApplication().getString(R.string.ram)
				+ FileUtil.convertFileSize(bean.mMemory));
		TextView cpu = (TextView) convertView.findViewById(R.id.cpu);
		cpu.setText(bean.mCpuRate);
		TextView permission = (TextView) convertView
				.findViewById(R.id.permission);
		if (bean.mIsForbid) {
			permission.getPaint().setStrikeThruText(true);
		} else {
			permission.getPaint().setStrikeThruText(false);
		}
		String action = "";
		if (bean.mBootReceiver.size() > 0) {
			action += TAApplication.getApplication().getString(
					R.string.bootstart)
					+ "  ";
		}
		if (bean.mBackgroundReceiver.size() > 0) {
			action += TAApplication.getApplication().getString(
					R.string.backgroundstart);
		}
		permission.setText(action);
		ImageView operaimg = (ImageView) convertView
				.findViewById(R.id.operator_img);
		TextView operatxt = (TextView) convertView
				.findViewById(R.id.operator_txt);
		convertView.findViewById(R.id.operator).setTag(bean);
		if (bean.mIsForbid) {
			operaimg.setImageResource(R.drawable.accept);
			operatxt.setText(R.string.accept);
			convertView.findViewById(R.id.operator).setOnClickListener(
					mEnableListener);
		} else {
			operaimg.setImageResource(R.drawable.forbid);
			operatxt.setText(R.string.forbid);
			convertView.findViewById(R.id.operator).setOnClickListener(
					mDisableListener);
		}
		return convertView;
	}

	/**
	 * 对已有的数据进行刷新
	 */
	public void updateSelf() {
		List<DataBean> list = new ArrayList<DataBean>();
		list.addAll(mList);
		Collections.sort(list);
		update(list);
	}

	/**
	 * 更新数据
	 */
	public void update(List<DataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		boolean hasAddFirst = false;
		// 排序
		for (DataBean bean : list) {
			if (!bean.mIsForbid && !bean.mIsSysApp && bean.mIsNew) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
		for (DataBean bean : list) {
			if (!bean.mIsForbid && !bean.mIsSysApp && !bean.mIsNew) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
		for (DataBean bean : list) {
			if (!bean.mIsForbid && bean.mIsSysApp && bean.mIsNew) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
		for (DataBean bean : list) {
			if (!bean.mIsForbid && bean.mIsSysApp && !bean.mIsNew) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
		hasAddFirst = false;
		for (DataBean bean : list) {
			if (bean.mIsForbid && !bean.mIsSysApp) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
		for (DataBean bean : list) {
			if (bean.mIsForbid && bean.mIsSysApp) {
				bean.mIsFirst = false;
				if (!hasAddFirst) {
					bean.mIsFirst = true;
				}
				mList.add(bean);
				hasAddFirst = true;
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * 更新CPU使用率
	 */
	public void updateCPU(Map<String, Float> retRate) {
		for (DataBean bean : mList) {
			if (retRate.containsKey(bean.mInfo.packageName)) {
				float rate = retRate.get(bean.mInfo.packageName);
				rate = ((int) (rate * 10.0f + 0.5f)) / 10.0f;
				bean.mCpuRate = "CPU:" + rate + "%";
			} else {
				bean.mCpuRate = "CPU:0%";
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * 自启动应用个数
	 */
	private int getEnableCount() {
		int ret = 0;
		for (DataBean bean : mList) {
			if (!bean.mIsForbid) {
				ret++;
			}
		}
		return ret;
	}

	/**
	 * 已禁用个数
	 */
	private int getDisableCount() {
		int ret = 0;
		for (DataBean bean : mList) {
			if (bean.mIsForbid) {
				ret++;
			}
		}
		return ret;
	}

}
