package com.jiubang.ggheart.components;

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
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 多选对话框公用组件
 * @author wuziyi
 *
 */
public class MultiCheckViewGroup extends LinearLayout
		implements
			IndicatorListner,
			IIndicatorUpdateListner,
			OnItemClickListener {

	private static final int INITFINISH = 1;

	private MutilCheckGridView mGridView;
	private DesktopIndicator mIndicator;

	private ArrayList<Object> mList;
	private ArrayList<Boolean> mBooleanList;
	private LayoutInflater mInflater;
	private Context mContext;
	private OnMultiItemClickedListener mMultiItemClickedListener;
	private boolean mIsSingleCheckType;
	private int mOldPosition = -1;
	private MyAdapter mOldAdapter;

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
					break;
				case MutilCheckGridView.UPDATEINDICATOR :
					mIndicator.updateIndicator(msg.arg1, (Bundle) msg.obj);
					break;
				default :
					break;
			}
		}
	};

	public MultiCheckViewGroup(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.multi_check_viewgroup, this);
	}

	public MultiCheckViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mInflater.inflate(R.layout.multi_check_viewgroup, this);
		mGridView = (MutilCheckGridView) findViewById(R.id.gridview);
		mGridView.setmIndicatorUpdateListner(this);
		mGridView.setHanler(mHandler);
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
		boolean isClicked = mBooleanList.get(p);
		if (p > mBooleanList.size()) {
			return;
		}
		mBooleanList.set(p, !isClicked);
		if (mIsSingleCheckType) {
			if (!isClicked) {
				if (mOldPosition != -1) {
					mBooleanList.set(mOldPosition, false);
					mOldAdapter.notifyDataSetChanged();
				}
				mOldPosition = p;
				mOldAdapter = adapter;
			} else {
				mOldAdapter = null;
				mOldPosition = -1;
			}
		}
		adapter.notifyDataSetChanged();
		if (mMultiItemClickedListener != null) {
			mMultiItemClickedListener.onMultiItemClicked(p, !isClicked);
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
		}// end for
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

			AppItemInfo appInfo = null;
			Object info = getItem(position);
			Drawable icon = null;
			String title = null;
			try {
				if (info instanceof AppItemInfo) {
					appInfo = (AppItemInfo) info;
					if (appInfo != null) {
						title = appInfo.mTitle;
					}
				} else if (info instanceof FunAppItemInfo) {
					appInfo = ((FunAppItemInfo) info).getAppItemInfo();
					if (appInfo != null) {
						title = appInfo.mTitle;
					}
				} else if (info instanceof ShortCutInfo) {
					ShortCutInfo shortCutInfo = (ShortCutInfo) info;
					appInfo = shortCutInfo.getRelativeItemInfo();
					
					//add by licanhui 2012-12-4
					//判断是否自定义标题。是就用自定标题。否则用程序自带的标题
					if (shortCutInfo.mIsUserTitle) {
						if (shortCutInfo.mTitle != null && shortCutInfo.mTitle.toString() != null) {
							title = shortCutInfo.mTitle.toString();
						} else {
							title = appInfo.mTitle;
						}
					} else {
						//change by dengdazhong 2013-2-4
						//修复隐藏程序名后，打开文件夹，然后按添加勾选的图标没有显示程序名
						//if (appInfo != null && GOLauncherApp.getSettingControler().getDesktopSettingInfo().isShowTitle()) {
						if (appInfo != null) {
							title = appInfo.mTitle;
						}
					}
				}
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
			if (appInfo == null) {
				return null;
			}
			if (convertView == null) {
				try {
					convertView = mInflater.inflate(R.layout.folder_grid_item, parent, false);
				} catch (InflateException e) {
					e.printStackTrace();
				}
			}
			if (convertView == null) {
				return null;
			}
			TextView textView = (TextView) convertView.findViewById(R.id.name);
			ImageView img = (ImageView) convertView.findViewById(R.id.choice);
			if (getCheckStatus(mScreen, position)) {
				img.setVisibility(View.VISIBLE);
			} else {
				img.setVisibility(View.INVISIBLE);
			}
			//add by licanhui 2012-12-4
			//判断是否自定义图片。是就用自定图片。否则用程序默认自带图片，广告图标
			if (info instanceof ShortCutInfo && ((ShortCutInfo) info).mIsUserIcon) {
				icon = ((ShortCutInfo) info).mIcon;
				if (icon == null) {
					icon = appInfo.mIcon;
				}
			} else {
				icon = appInfo.mIcon;
			}
				
			if (icon != null) {
				//change by dengdazhong 2013-2-5
				//修复全新安装，打开主屏GO文件，按添加按钮，GO短信与一键GO锁屏图标比其他的小（这两个图标都没有安装）
				int iconSize = GOLauncherApp
						.getSettingControler()
						.getDesktopSettingInfo()
						.getIconRealSize(
								GOLauncherApp.getSettingControler().getDesktopSettingInfo()
										.getIconSizeStyle());
				icon.setBounds(0, 0, iconSize, iconSize);
				textView.setCompoundDrawables(null, icon, null, null);
			}
			if (title != null) {
				textView.setText(title);
			}
			convertView.setTag(info);
			return convertView;
		}
	}

	private boolean getCheckStatus(int screen, int position) {
		if (mBooleanList == null) {
			return false;
		} else {
			return mBooleanList.get(screen * mGridView.getCountPerPage() + position);
		}
	}

	public void setContentList(ArrayList<Object> list, ArrayList<Boolean> booleanList) {
		mList = list;
		mBooleanList = booleanList;
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

	public void setMultiItemClickedListener(OnMultiItemClickedListener MultiItemClickedListener) {
		this.mMultiItemClickedListener = MultiItemClickedListener;
	}

	public void recyle() {
		if (mGridView != null) {
			mGridView.recyle();
		}
//		mIndicator = null;
//		mGridView = null;
		mMultiItemClickedListener = null;
	}

	public void setIsSingleCheckType(boolean mIsSingleCheckType) {
		this.mIsSingleCheckType = mIsSingleCheckType;
	}
}
