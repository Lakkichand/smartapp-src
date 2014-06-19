package com.jiubang.go.backup.pro.appwidget;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;

/**
 * @author jiangpeihe
 *
 */
public class BackupChooseDialog extends BaseAdapter {
	private Context mContext;
	private List<String> mList;
	private LayoutInflater mInflater;
	private boolean mSelectedBackupContent[];
	public BackupChooseDialog(Context context, List<String> list, boolean selectedBackupContent[]) {
		mContext = context;
		mList = list;
		mSelectedBackupContent = selectedBackupContent;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public String getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = mInflater.inflate(R.layout.layout_widget_choosebackupcontent, null);
		TextView titleView = (TextView) convertView.findViewById(R.id.entry_title);
		ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
		CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
		titleView.setText(getItem(position));
		if (position == 0) {
			imageView.setImageResource(R.drawable.icon_contacts);
		} else if (position == 1) {
			imageView.setImageResource(R.drawable.icon_sms);
		} else if (position == 2) {
			imageView.setImageResource(R.drawable.icon_call_log);
		} else if (position == 3) {
			imageView.setImageResource(R.drawable.icon_mms);
		}
		if (mSelectedBackupContent[position]) {
			checkBox.setChecked(true);
		}

		final int fPosition = position;
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mSelectedBackupContent[fPosition] = isChecked;
			}
		});
		return convertView;
	}
}
