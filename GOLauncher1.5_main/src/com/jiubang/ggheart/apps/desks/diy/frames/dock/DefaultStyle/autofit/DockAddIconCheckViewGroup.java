package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.DesktopIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IIndicatorUpdateListner;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IndicatorListner;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicatorItem;
import com.jiubang.ggheart.components.MutilCheckGridView;
import com.jiubang.ggheart.components.MutilCheckViewAdapter;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-11-14]
 */
public class DockAddIconCheckViewGroup extends LinearLayout
		implements
			IndicatorListner,
			IIndicatorUpdateListner,
			OnItemClickListener {
	private Context mContext;
	private LayoutInflater mInflater;
	private static final int INITFINISH = 1;
	private OnAddIconAppCheckListner mAddIconAppCheckListner;
	
	private DesktopIndicator mIndicator;	//指示器
	private MutilCheckGridView mGridView;	//现实内容容器
	private ArrayList<Object> mList;	//现实内容数组


	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case INITFINISH :
					if (mList != null) {
						mGridView.initLayoutData(mList.size());
						setAdapter();
					}
					mIndicator.setCurrent(0);
					mIndicator.setTotal(mGridView.getScreenCount());
					if (mGridView.getScreenCount() == 1) {
						mGridView.getScreenScroller().setPadding(0);
					}
					break;
					
				case MutilCheckGridView.UPDATEINDICATOR :
					mIndicator.updateIndicator(msg.arg1, (Bundle) msg.obj);
					break;
				default :
					break;
			}
		}
	};

	public DockAddIconCheckViewGroup(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.multi_check_viewgroup, this);
	}

	public DockAddIconCheckViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mInflater.inflate(R.layout.dock_add_icon_multi_check_viewgroup, this);
		
		//GridView
		mGridView = (MutilCheckGridView) findViewById(R.id.gridview);
		mGridView.setmIndicatorUpdateListner(this);
		mGridView.setHanler(mHandler);
		
		//指示器
		mIndicator = (DesktopIndicator) findViewById(R.id.folder_indicator);
		mIndicator.setDefaultDotsIndicatorImage(R.drawable.setting_dotindicator_lightbar,
				R.drawable.setting_dotindicator_normalbar);
		mIndicator.setDotIndicatorLayoutMode(ScreenIndicator.LAYOUT_MODE_ADJUST_PICSIZE);
		mIndicator.setDotIndicatorDrawMode(ScreenIndicatorItem.DRAW_MODE_INDIVIDUAL);
		mIndicator.setIndicatorListner(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		int itemsCountPerScreen = mGridView.getCountPerPage();
		MyAdapter adapter = (MyAdapter) parent.getAdapter();
		int screenIndex = adapter.mScreen;
		int p = position + screenIndex * itemsCountPerScreen;
		if (mAddIconAppCheckListner != null) {
			ImageView iconImg = (ImageView) view.findViewById(R.id.icon);
			if (iconImg != null) {
				mAddIconAppCheckListner.onItemCheck(iconImg, p);
			}
		}
	}

	@Override
	public void clickIndicatorItem(int index) {
		mGridView.snapToScreen(index, false, -1);
	}

	@Override
	public void sliding(float percent) {
		if (0 <= percent && percent <= 100) {
			mGridView.getScreenScroller().setScrollPercent(percent);
		}
	}

	@Override
	public void updateIndicator(int num, int current) {
		if (num >= 0 && current >= 0 && current < num) {
			mIndicator.setTotal(num);
			mIndicator.setCurrent(current);
		}
	}

	private void setAdapter() {
		if (mList == null) {
			return;
		}
		if (mGridView == null) {
			return;
		}
		final int count = mList.size();
		mGridView.removeAllViews();
		int screenCount = mGridView.getScreenCount();
		int itemsCountPerScreen = mGridView.getCountPerPage();
		int culumns = mGridView.getCellCol();
		for (int i = 0; i < screenCount; i++) {
			GridView page = new GridView(mContext);
			ArrayList<Object> tempList = new ArrayList<Object>();
			for (int j = 0; j < itemsCountPerScreen && itemsCountPerScreen * i + j < count; j++) {
				Object obj = mList.get(itemsCountPerScreen * i + j);
				tempList.add(obj);
			}
			page.setAdapter(new MyAdapter(mContext, tempList, i));
			page.setNumColumns(culumns);
			page.setHorizontalSpacing(0);
			page.setVerticalSpacing(0);
			page.requestLayout();
			page.setSelector(android.R.color.transparent);
			page.setOnItemClickListener(this);
			mGridView.addView(page);
		}
	}

	/**
	 * 内部数据适配器
	 * @author yangguanxiang
	 *
	 */
	private class MyAdapter extends MutilCheckViewAdapter {

		public MyAdapter(Context context, ArrayList<Object> list, int screenIndex) {
			super(list, screenIndex);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				try {
					convertView = mInflater.inflate(R.layout.dock_add_icon_grid_item, parent, false);
				} catch (InflateException e) {
					e.printStackTrace();
				}
			}
			if (convertView == null) {
				return null;
			}
			
			Object info = getItem(position);
			Drawable icon = null;
			String title = null;
			if (info instanceof ShortCutInfo) {
				ShortCutInfo shortCutInfo = (ShortCutInfo) info;
				title = shortCutInfo.mTitle.toString();
				icon = shortCutInfo.mIcon;
			}
			
			else if (info instanceof AppItemInfo) {
				AppItemInfo appItemInfo = (AppItemInfo) info;
				title = appItemInfo.mTitle.toString();
				icon = appItemInfo.mIcon;
			}

			TextView textView = (TextView) convertView.findViewById(R.id.name);
			if (title != null) {
				textView.setText(title);
			}
			
			ImageView iconImg = (ImageView) convertView.findViewById(R.id.icon);
			if (icon != null) {
				iconImg.setImageDrawable(icon);
			}
			return convertView;
		}
	}

	public void setContentList(ArrayList<Object> list) {
		mList = list;
		Message message = mHandler.obtainMessage();
		message.what = INITFINISH;
		mHandler.sendMessage(message);
	}

	public void onConfigurationChanged() {
		if (mGridView != null) {
			mGridView.changeOrientation();
			mGridView.removeAllViews();
			if (mList != null) {
				mGridView.initLayoutData(mList.size());
				setAdapter();
			}
			mIndicator.setTotal(mGridView.getScreenCount());
			mIndicator.setCurrent(0);
		}
	}

	public void setOnAddIconAppCheckListner(OnAddIconAppCheckListner addIconAppCheckListner) {
		this.mAddIconAppCheckListner = addIconAppCheckListner;
	}

	public void recyle() {
		if (mGridView != null) {
			mGridView.recyle();
		}
		//		mIndicator = null;
		//		mGridView = null;
		mAddIconAppCheckListner = null;
	}
}
