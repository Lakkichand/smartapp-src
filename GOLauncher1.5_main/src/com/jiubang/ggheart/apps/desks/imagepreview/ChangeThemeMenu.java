package com.jiubang.ggheart.apps.desks.imagepreview;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-10-29]
 */
public class ChangeThemeMenu implements ICleanable {
	private static final int DIV_LINE_HEIGHT = 3; // 分割线的高度（像素）

	private PopupWindow mPopupWindow;
	private ListView mListView;
	private Context mContext;
	private OnClickListener mItemClickListener;
	private ArrayList<String> mStrings;
	private LayoutInflater mInflater;

	public void setmItemClickListener(OnClickListener mItemClickListener) {
		this.mItemClickListener = mItemClickListener;
	}

	public ArrayList<String> getmStrings() {
		return mStrings;
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  licanhui
	 * @date  [2012-10-29]
	 */
	private class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			if (null != mStrings) {
				return mStrings.size();
			} else {
				return 0;
			}
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
			TextView textView = null;
			if (null == convertView) {
				textView = (TextView) mInflater.inflate(R.layout.change_icon_theme_menu_list_item,
						null);
				textView.setHeight((int) mContext.getResources().getDimension(
						R.dimen.changet_icon_theme_menu_item_height));
				textView.setTag(new Integer(position));
				convertView = textView;

				if (null != mItemClickListener) {
					// 外部convertView.gettag就可以得到点击了第几个
					convertView.setOnClickListener(mItemClickListener);
				}
			} else {
				textView = (TextView) convertView;
			}
			textView.setTag(new Integer(position));
			textView.setText(mStrings.get(position));
			//修改文字字体
			DeskSettingConstants.setTextViewTypeFace(textView);

			return convertView;
		}
	}

	/**
	 * 
	 */
	public ChangeThemeMenu(Context context, ArrayList<String> strings) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mListView = (ListView) mInflater.inflate(R.layout.change_icon_theme_menu_listview, null);

		mStrings = strings;

		MyAdapter adapter = new MyAdapter();
		mListView.setAdapter(adapter);

		// 分割线
		// Drawable drawable_devider =
		// mContext.getResources().getDrawable(R.drawable.skinlist_line);
		// mListView.setDivider(drawable_devider);

		mPopupWindow = new PopupWindow(mListView);
	}

	public void show(View view) {
		Drawable drawable = mContext.getResources().getDrawable(
				R.drawable.change_theme_menu_list_bg);
		mPopupWindow.setBackgroundDrawable(drawable);
		int location[] = new int[2];
		view.getLocationInWindow(location);

		int width = (int) mContext.getResources().getDimension(
				R.dimen.changet_icon_theme_menu_width);
		mPopupWindow.setWidth(width);

		int one_item_height = (int) mContext.getResources().getDimension(
				R.dimen.changet_icon_theme_menu_item_height)
				+ DIV_LINE_HEIGHT;;

		int height = 0;
		// 大于7个纪录就限高
		if (mStrings.size() > 7) {
			height = 7 * one_item_height;
		} else {
			height = mStrings.size() * one_item_height;
		}
		mPopupWindow.setHeight(height);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setAnimationStyle(R.style.QuickActionBelowAnimation);
		mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0],
				location[1] + view.getHeight());
	}

	@Override
	public void cleanup() {
		if (null != mPopupWindow && mPopupWindow.isShowing()) {
			mPopupWindow.dismiss();
		}
	}

	public boolean isShowing() {
		if (null != mPopupWindow && mPopupWindow.isShowing()) {
			return true;
		} else {
			return false;
		}
	}

	public void dismiss() {
		if (null != mPopupWindow) {
			mPopupWindow.dismiss();
		}
	}
}
