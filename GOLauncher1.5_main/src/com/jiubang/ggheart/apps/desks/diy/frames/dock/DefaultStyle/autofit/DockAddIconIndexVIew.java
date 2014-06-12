package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit;

import android.content.Context;
import android.content.res.Configuration;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.Preferences.dialogs.DialogBase;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;

/**
 * 
 * <br>类描述:Dock长按导航页View
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-11-13]
 */
public class DockAddIconIndexVIew extends LinearLayout implements View.OnClickListener {
	private Context mContext;
	private GridView mGridView;
	private IconAdapter mAdapter;
	private OnAddIconClickListner mListner;
	private LinearLayout mContentLayout;
	private Button mCancleButton;
	private int mType;	//打开类型
	
	public DockAddIconIndexVIew(Context context, int type) {
		super(context);
		mContext = context;
		mType = type;
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.dialog_dock_add_icon_index, this);
		mContentLayout = (LinearLayout) findViewById(R.id.contentLayout);
		mGridView = (GridView) findViewById(R.id.gridview);
		mCancleButton = (Button) findViewById(R.id.cancle_btn);
		mCancleButton.setOnClickListener(this);
		initLayoutWidth();
		initGridView();
	}
	
	/**
	 * <br>功能简述:设置view的宽度
	 * <br>功能详细描述:横屏未屏幕宽度，竖屏为自定义宽度
	 * <br>注意:
	 */
	public void initLayoutWidth() {
		//更换总的布局高宽
		if (mContentLayout != null) {
			if (GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
				DialogBase.setDialogWidth(mContentLayout, mContext);
			} else {
				mContentLayout.getLayoutParams().width = (int) getResources().getDimension(R.dimen.folder_edit_view_width);
			}
		}
	}

	/**
	 * <br>功能简述:初始化GridView
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initGridView() {
		mAdapter = new IconAdapter();
		mGridView.setAdapter(mAdapter);
		mGridView.setNumColumns(4);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mListner != null) {
					mListner.onIconsClick(DockAddIconFrame.TYPE_ADD_ICON_INDEX, null, position, null);
				}
			}
		});
	}
	
	/**
	 * 
	 * <br>类描述:adapter
	 * <br>功能详细描述:
	 * 
	 * @author  ruxueqin
	 * @date  [2012-10-18]
	 */
	private class IconAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		private int[] mImageId = new int[] { R.drawable.gesture_application,
				R.drawable.tab_add_shortcut_icon, R.drawable.screen_edit_go_shortcut,
				R.drawable.dock_add_icon_default };

		private int[] mTitle = { R.string.tab_add_app,
				R.string.add_app_icon, R.string.dialog_name_go_shortcut, R.string.default_icon };

		public IconAdapter() {
			super();
			mInflater = LayoutInflater.from(getContext());
		}

		@Override
		public int getCount() {
			return mTitle != null ? mTitle.length : 0;
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
				try {
					convertView = mInflater.inflate(R.layout.folder_grid_item, parent, false);
				} catch (InflateException e) {
					e.printStackTrace();
				}
			}

			if (convertView == null) {
				return null;
			}
			ImageView img = (ImageView) convertView.findViewById(R.id.choice);
			img.setVisibility(View.INVISIBLE);

			TextView textView = (TextView) convertView.findViewById(R.id.name);
			textView.setText(mTitle[position]);
			textView.setCompoundDrawablesWithIntrinsicBounds(null, mContext.getResources()
					.getDrawable(mImageId[position]), null, null);
//			textView.setTextSize(GoLauncher.getAppFontSize());
			return convertView;
		}
	}
	
	/**
	 * <br>功能简述:横竖屏切换事件
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onConfigurationChanged() {
		//更换总的布局高宽
		initLayoutWidth();
	}

	/**
	 * <br>功能简述:设置点击ITEM监听器
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param listener
	 */
	public void setOnAddIconClickListener(OnAddIconClickListner listener) {
		mListner = listener;
	}
	
	@Override
	public void onClick(View v) {
		if (v == mCancleButton) {
			if (mListner != null) {
				mListner.onBackBtnClick(mType);
			}
		}
	}
	
	/**
	 * <br>功能简述:注销时释放资源
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onDestroy() {
		mGridView = null;
		mAdapter = null;
		mListner = null;
		mCancleButton = null;
	}
}
