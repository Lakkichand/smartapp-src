package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureInfo;

/**
 * 手势查询相似列表适配器
 * 
 * @author licanhui
 * 
 */
public class DiyGestureResultAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<DiyGestureInfo> mListdate;

	public DiyGestureResultAdapter(Context context, List<DiyGestureInfo> listdate) {
		this.mListdate = listdate;
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mListdate.size();
	}

	@Override
	public DiyGestureInfo getItem(int position) {
		return mListdate.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.gesture_recognise_search_result_item, null);

			holder = new ViewHolder();
			holder.mIconImageView = (DiyGestureItemView) convertView
					.findViewById(R.id.my_gesture_item_icon);
			holder.mTypeName = (TextView) convertView.findViewById(R.id.my_gesture_item_type_name);
			holder.mName = (TextView) convertView.findViewById(R.id.my_gesture_item_name);
			DeskSettingConstants.setTextViewTypeFace(holder.mTypeName);
			DeskSettingConstants.setTextViewTypeFace(holder.mName);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.mIconImageView.setGestureImageView(mListdate.get(position).getmGesture()); // 设置预览图片
		holder.mTypeName.setText(mListdate.get(position).getTypeName());
		holder.mName.setText(mListdate.get(position).getName());
		return convertView;
	}
/**
 * 
 * @author 
 *
 */
	class ViewHolder {
		DiyGestureItemView mIconImageView;
		TextView mTypeName;
		TextView mName;
	}
}