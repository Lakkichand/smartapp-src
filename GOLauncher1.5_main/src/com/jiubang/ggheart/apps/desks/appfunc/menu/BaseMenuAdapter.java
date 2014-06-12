package com.jiubang.ggheart.apps.desks.appfunc.menu;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.theme.ImageExplorer;

/**
 * 
 * <br>类描述: 功能表菜单适配器
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-10-29]
 */
public class BaseMenuAdapter extends BaseAdapter {
	protected Context mContext;
	protected ArrayList<BaseMenuItemInfo> mList;
	protected int mTextColor;
	private int mItemPaddingLeft;
	private int mItemPaddingTop;
	private int mItemPaddingRight;
	private int mItemPaddingBottom;
	private int mDrawablePadding;
	private int mTextSize = -1; // -1时使用预定义值
	private int mItemLayout = -1;
	private ImageExplorer mImageExplorer;
	private AppFuncThemeController mThemeController;

	public BaseMenuAdapter(Context context) {
		this.mContext = context;
		mImageExplorer = AppCore.getInstance().getImageExplorer();
		mThemeController = AppFuncFrame.getThemeController();
	}

	public BaseMenuAdapter(Context context, ArrayList<BaseMenuItemInfo> list) {
		this.mList = list;
		this.mContext = context;
		mImageExplorer = AppCore.getInstance().getImageExplorer();
		mThemeController = AppFuncFrame.getThemeController();
	}

	public void setItemList(ArrayList<BaseMenuItemInfo> itemList) {
		mList = itemList;
	}

	@Override
	public int getCount() {
		if (mList != null) {
			return mList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mList != null && position > -1 && position < mList.size()) {
			return mList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setTextColor(int color) {
		mTextColor = color;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			if (mItemLayout == -1) {
				mItemLayout = R.layout.app_func_all_app_menu;
			}
			convertView = View.inflate(mContext, mItemLayout, null);
		}
		convertView.setTag(mList.get(position));
		TextView textView = (TextView) convertView.findViewById(R.id.app_func_menu_text);
		textView.setPadding(mItemPaddingLeft, mItemPaddingTop, mItemPaddingRight,
				mItemPaddingBottom);
		textView.setTextColor(mTextColor);
		if (mTextSize > -1) {
			textView.setTextSize(mTextSize);
		}
		BaseMenuItemInfo info = mList.get(position);
		if (info.mText != null) {
			textView.setText(info.mText);
		} else if (info.mTextId != -1) {
			textView.setText(info.mTextId);
		}
		int pos = info.mDrawablePos;
		String packageName = mThemeController.getThemeBean().mSwitchMenuBean.mPackageName;
		if (pos != BaseMenuItemInfo.DRAWABLE_POS_NONE) {
			textView.setCompoundDrawablePadding(mDrawablePadding);
			Drawable drawable = null;
			if (info.mDrawable != null) {
				drawable = info.mDrawable;
			} else if (info.mDrawableId != -1) {
				drawable = mImageExplorer.getDrawable(packageName, info.mDrawableId);
			}
			if (drawable == null) {
				drawable = mImageExplorer.getDefaultDrawable(info.mDrawableId);
			}
			switch (pos) {
				case BaseMenuItemInfo.DRAWABLE_POS_LEFT :
					textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
					break;
				case BaseMenuItemInfo.DRAWABLE_POS_TOP :
					textView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
					break;
				case BaseMenuItemInfo.DRAWABLE_POS_RIGHT :
					textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
					break;
				case BaseMenuItemInfo.DRAWABLE_POS_BOTTOM :
					textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, drawable);
					break;
				default :
					break;
			}
		}
		return convertView;
	}

	public void setItemPadding(int left, int top, int right, int bottom) {
		mItemPaddingLeft = left;
		mItemPaddingTop = top;
		mItemPaddingRight = right;
		mItemPaddingBottom = bottom;
	}

	public void setItemDrawablePadding(int padding) {
		mDrawablePadding = padding;
	}

	public void setItemTextSize(int size) {
		mTextSize = size;
	}

	public void setItemLayout(int resId) {
		mItemLayout = resId;
	}
}
