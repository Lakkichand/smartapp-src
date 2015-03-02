package com.zhidian.checkapk.adapter;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhidian.bean.InstallBean;
import com.zhidian.checkapk.R;


/**
 * 记录Adapter
 * @author zhaoyl
 *
 */
public class RecordAdapter extends AbsListAdapter<InstallBean>{

	public RecordAdapter(Context context, List<InstallBean> dataList) {
		super(context, dataList);
		// TODO Auto-generated constructor stub
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_record, null);
			holder.tvId = (TextView) convertView
					.findViewById(R.id.rank);
			holder.tvBoxNum = (TextView) convertView
					.findViewById(R.id.boxnum);
			holder.tvCode = (TextView) convertView
					.findViewById(R.id.code);
			holder.tvVersionCode = (TextView) convertView
					.findViewById(R.id.versionCode);
			holder.tvStatus = (TextView) convertView
					.findViewById(R.id.status);
			holder.tvInstallTime = (TextView) convertView
					.findViewById(R.id.installtime);
			holder.tvMsg = (TextView) convertView
					.findViewById(R.id.msg);
			holder.tvUnloadStatus = (TextView) convertView
					.findViewById(R.id.unload_status);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		InstallBean b = mDataList.get(position);
		String boxBum = b.boxNum;
		String code = b.code;
		String versionCode = b.versionCode;
		String status = b.status;
		String installTime = b.installTime;
		String msg = b.msg;
		String unloadStatus = b.unloadStatus;
		int rank = position + 1;
		holder.tvId.setText(rank + "");
		holder.tvBoxNum.setText(boxBum);
		holder.tvCode.setText(code);
		holder.tvVersionCode.setText(versionCode);
		holder.tvStatus.setText(status);
		holder.tvInstallTime.setText(installTime);
		holder.tvMsg.setText(msg);
		holder.tvUnloadStatus.setText(unloadStatus);
		return convertView;
	}
	
	static class ViewHolder{
		TextView tvId; //ID
		TextView tvBoxNum; //盒子编号
		TextView tvCode; //code
		TextView tvVersionCode; //版本号
		TextView tvStatus; //安装状态
		TextView tvInstallTime; //安装时间
		TextView tvMsg; //消息
		TextView tvUnloadStatus; //上传状态
	}

}
