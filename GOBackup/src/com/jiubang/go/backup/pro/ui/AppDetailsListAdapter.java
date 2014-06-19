package com.jiubang.go.backup.pro.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
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
import com.jiubang.go.backup.pro.model.DrawableProvider.OnDrawableLoadedListener;

/**
 * @author maiyongshen
 *
 */
public abstract class AppDetailsListAdapter extends BaseAdapter {
	private Context mContext;
	private LayoutInflater mInflater;
	private OnAdapterItemUpdateListener mOnAdapterItemUpdateListener;
	private OnSelectChangeListener mOnItemSelectChangeListener;
	private Handler mHandler = new Handler();
	
	public AppDetailsListAdapter(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}
	
	public Context getContext() {
		return mContext;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.layout_record_entry_view, parent, false);
		}
		updateView(convertView, position);
		return convertView;
	}
	
	public void updateView(View convertView, int pos) {
		if (convertView == null || pos < 0 || pos >= getCount()) {
			return;
		}
		ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
		icon.setImageDrawable(getIcon(pos, new IconLoadListener(pos)));
		
		TextView title = (TextView) convertView.findViewById(R.id.entry_title);
		bindTitle(title, pos);
		
		TextView summary = (TextView) convertView.findViewById(R.id.entry_summary1);
		bindSummary(summary, pos);
		
		CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
		checkBox.setVisibility(isEnabled(pos) ? View.VISIBLE : View.GONE);
		checkBox.setOnCheckedChangeListener(null);
		checkBox.setChecked(isSelected(pos));
		checkBox.setTag(pos);
		checkBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
	}
	
	public void setOnItemUpdateListener(OnAdapterItemUpdateListener listener) {
		mOnAdapterItemUpdateListener = listener;
	}
	
	public void setOnItemSelectChangeListener(OnSelectChangeListener listener) {
		mOnItemSelectChangeListener = listener;
	}
	
	public void notifyListenerToUpdateView(int pos) {
		if (mOnAdapterItemUpdateListener != null) {
			mOnAdapterItemUpdateListener.onItemUpdate(this, pos);
		}
	}
	
	public void toggle(int pos) {
		setSelected(pos, !isSelected(pos));
	}
	
	public boolean hasItemSelected() {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (isSelected(i)) {
				return true;
			}
		}
		return false;
	}
	
	public int getSelectedItemCount() {
		int selectedCount = 0;
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (isSelected(i)) {
				selectedCount++;
			}
		}
		return selectedCount;
	}
	
	public void setSelected(int pos, boolean selected) {
		SelectableItem item = getSelectableItem(pos);
		if (item == null) {
			return;
		}
		if (item.isSelected() ^ selected) {
			item.setSelected(selected);
			if (mOnItemSelectChangeListener != null) {
				mOnItemSelectChangeListener.onSelectChange(pos, selected);
			}
		}
	}
	
	public boolean isSelected(int pos) {
		SelectableItem item = getSelectableItem(pos);
		if (item == null) {
			return false;
		}
		return item.isSelected();
	}
	
	public abstract SelectableItem getSelectableItem(int pos);
	public abstract Drawable getIcon(int pos, OnDrawableLoadedListener listener);
	public abstract void bindTitle(View view, int pos);
	public abstract void bindSummary(View view, int pos);
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static interface OnAdapterItemUpdateListener {
		public void onItemUpdate(BaseAdapter adapter, int pos);
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static interface OnSelectChangeListener {
		public void onSelectChange(int pos, boolean selected);
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static interface SelectableItem {
		public boolean isSelected();
		public void setSelected(boolean selected);
	}
	
	
	private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			Object tag = buttonView.getTag();
			if (tag instanceof Integer) {
				setSelected((Integer) tag, isChecked);
			}
		}
	};
	
	/**
	 * @author maiyongshen
	 *
	 */
	protected class IconLoadListener implements OnDrawableLoadedListener {
		private int mPosition;
		
		public IconLoadListener(int pos) {
			mPosition = pos;
		}

		@Override
		public void onDrawableLoaded(Drawable drawable) {
			if (drawable != null) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						notifyListenerToUpdateView(mPosition);
					}
				});
			}
		}
	}

}
