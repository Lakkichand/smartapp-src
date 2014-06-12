package com.zhidian.wifibox.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ta.TAApplication;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.TrashScanActivity;
import com.zhidian.wifibox.data.TransScanDataBean;
import com.zhidian.wifibox.util.FileUtil;

/**
 * 残留清理适配器
 * 
 * @author xiedezhi
 * 
 */
public class TrashScanAdapter extends BaseAdapter {

	private Context mContext;

	private List<TransScanDataBean> mList = new ArrayList<TransScanDataBean>();

	private LayoutInflater mInflater = LayoutInflater.from(TAApplication
			.getApplication());

	private Handler mHandler;
	/**
	 * 是否扫描完成
	 */
	private boolean mIsFinish;

	public TrashScanAdapter(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
	}

	/**
	 * 清理点击事件
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 清理
			TransScanDataBean bean = (TransScanDataBean) v.getTag();
			int index = mList.indexOf(bean);
			if (index >= 0) {
				mHandler.obtainMessage(TrashScanActivity.MSG_CLEAN, index, -1)
						.sendToTarget();
			}
		}
	};
	/**
	 * 列表点击事件
	 */
	private OnClickListener mItemClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// 弹框
			TransScanDataBean bean = (TransScanDataBean) view.getTag();
			if (bean.paths.size() > 0) {
				// 选项数组
				String[] array = new String[bean.paths.size()];
				for (int i = 0; i < array.length; i++) {
					array[i] = bean.paths.get(i);
				}
				// 包含多个选项的对话框
				AlertDialog dialog = new AlertDialog.Builder(mContext)
						.setTitle(bean.title).setItems(array, null)
						.setPositiveButton("确定", null).create();
				dialog.show();
			}
		}
	};

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
			convertView = mInflater.inflate(R.layout.transcan_item, null);
		}
		TransScanDataBean bean = mList.get(position);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView size = (TextView) convertView.findViewById(R.id.size);
		TextView tips = (TextView) convertView.findViewById(R.id.tips);
		LinearLayout clean = (LinearLayout) convertView.findViewById(R.id.aa);

		title.setText(bean.title + "(" + bean.paths.size() + "个)");
		size.setText(FileUtil.convertFileSize((long) (bean.size / 1024.0)));
		tips.setText(TextUtils.isEmpty(bean.suggestion) ? "" : bean.suggestion);

		if (mIsFinish) {
			clean.setClickable(true);
			clean.setTag(bean);
			clean.setOnClickListener(mClickListener);
		} else {
			clean.setClickable(false);
		}

		if (mIsFinish) {
			convertView.setTag(bean);
			convertView.setOnClickListener(mItemClickListener);
		} else {
			convertView.setOnClickListener(null);
		}
		return convertView;
	}

	/**
	 * 更新数据
	 */
	public void update(List<TransScanDataBean> list) {
		mList.clear();
		if (list == null || list.size() <= 0) {
			notifyDataSetChanged();
			return;
		}
		mList.addAll(list);
		notifyDataSetChanged();
	}

	/**
	 * 获取数据列表
	 */
	public List<TransScanDataBean> getData() {
		return mList;
	}

	public void finish() {
		mIsFinish = true;
		notifyDataSetChanged();
	}

}
