package com.jiubang.ggheart.apps.desks.diy.frames.screeneditbox;

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
import com.go.util.graphics.DrawUtils;
import com.jiubang.core.framework.ICleanable;

/**
 * 
 * <br>类描述: Widget的主题选择菜单
 * <br>功能详细描述:
 * 
 */
public class ScreenEditSkinMenu implements ICleanable {
	private static final int PIC_PADDING_LEFT = 9; // popupwindow背景图左边宽度（像素）

	private static final float EDITSKINMENU = 0.575f; //分辨率小于800的屏幕使用此参数设置位置

	private PopupWindow mPopupWindow;

	private ListView mListView;

	private Context mContext;

	private OnClickListener mItemClickListener;

	private String[] mStrings;
	private LayoutInflater mInflater;

	/**
	 * @return the mStrings
	 */
	public String[] getmStrings() {
		return mStrings;
	}

	/**
	 * @param mStrings
	 *            the mStrings to set
	 */
	public void setmStrings(String[] mStrings) {
		this.mStrings = mStrings;
	}

	public void setParrentHeight(int height) {
	}

	/**
	 * @param mItemClickListener
	 *            the mItemClickListener to set
	 */
	public void setmItemClickListener(OnClickListener mItemClickListener) {
		this.mItemClickListener = mItemClickListener;
	}
/**
 * 
 * <br>类描述: 皮肤选取listview的adapter项
 * <br>功能详细描述:
 * 
 */
	private class MyAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			if (null != mStrings) {
				return mStrings.length;
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
				textView = (TextView) mInflater.inflate(
						R.layout.screen_edit_gowidget_skin_list_item, null);
				textView.setHeight((int) mContext.getResources().getDimension(
						R.dimen.screen_edit_skin_menu_item_height));
				textView.setTag(new Integer(position));
				convertView = textView;

				if (null != mItemClickListener) {
					// 外部convertView.gettag就可以得到点击了第几个
					convertView.setOnClickListener(mItemClickListener);
				}
			} else {
				textView = (TextView) convertView;
			}
			textView.setText(mStrings[position]);

			return convertView;
		}
	}

	public ScreenEditSkinMenu(Context context, String[] strings) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		// mListView = new ListView(context);
		mListView = (ListView) mInflater.inflate(R.layout.screen_edit_gowidget_skin_list, null);

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
		if (mStrings.length == 0) {
			return;
		}
		Drawable drawable = mContext.getResources().getDrawable(
				R.drawable.screenedit_widget_skinlist);
		mPopupWindow.setBackgroundDrawable(drawable);
		int location[] = new int[2];
		view.getLocationInWindow(location);
		int width = (int) mContext.getResources().getDimension(R.dimen.skin_menu_width)
				+ PIC_PADDING_LEFT * 2;
		mPopupWindow.setWidth(width);
		int height = 0;
		if (mStrings.length > 4) {
			// 大于4个纪录就限高
			height = (int) mContext.getResources().getDimension(R.dimen.skin_menu_max_height);
			mPopupWindow.setHeight(height);
		} else {
			// mPopupWindow.setHeight(android.view.WindowManager.LayoutParams.WRAP_CONTENT);
			int one_item_height = (int) mContext.getResources().getDimension(
					R.dimen.screen_edit_skin_menu_item_height) + (int) mContext.getResources().getDimension(
							R.dimen.screen_edit_widget_div_line_height); // 加上分割线的高度
			height = mStrings.length * one_item_height + (int) mContext.getResources().getDimension(
					R.dimen.screen_edit_widget_pic_padding_top); // 加上popupwindow背景图的高度
			mPopupWindow.setHeight(height);

		}
		view.getLeft();
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		// mPopupWindow.setAnimationStyle(R.style.QuickActionAboveAnimation);

		int marginRight = DrawUtils.dip2px(23);
		int marginBottom;
		// 总高-下面空余  add by xiangliang 低分辨率手机高度适配，根据TabView高度作调整
		if (DrawUtils.sHeightPixels < 800) {
			marginBottom = (int) (DrawUtils.sHeightPixels * EDITSKINMENU);
		} else {
			marginBottom = DrawUtils.sHeightPixels
					- (int) mContext.getResources().getDimension(
							R.dimen.screen_edit_box_container_gowidgets);
		}
		// float GOWIDGETSCALE = 0.425f;//widget模式下高度比例
		// int marginBottom = (int)
		// (DrawUtils.sHeightPixels-DrawUtils.sHeightPixels * GOWIDGETSCALE);
		mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY, /*
																* location[0] -
																* PIC_PADDING_LEFT
																*/
				DrawUtils.sWidthPixels - (marginRight + width), marginBottom - height);
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