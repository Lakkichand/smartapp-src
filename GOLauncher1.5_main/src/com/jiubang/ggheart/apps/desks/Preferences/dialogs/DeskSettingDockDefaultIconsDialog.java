package com.jiubang.ggheart.apps.desks.Preferences.dialogs;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewResultType;
import com.jiubang.ggheart.data.DockItemControler;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.IItemType;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.launcher.AppIdentifier;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * 
 * <br>类描述:dock长按空白－〉默认图标 弹出框
 * <br>功能详细描述:
 * 
 * @author  ruxueqin
 * @date  [2012-10-18]
 */
public class DeskSettingDockDefaultIconsDialog extends Dialog {
	private Context mContext;
	private GridView mGridView;
	private Button mOKButton;
	private Button mCancelButton;
	private OnDefaultIconsListner mListener; // 包名可见
	private IconAdapter mAdapter;
	private int mCount; // 最多可选 几个

	public DeskSettingDockDefaultIconsDialog(Context context) {
		super(context, R.style.Dialog);
		mContext = context;
	}

	/**
	 * 
	 * <br>类描述:选择图标返回数据监听
	 * <br>功能详细描述:
	 * 
	 * @author  ruxueqin
	 * @date  [2012-10-18]
	 */
	public interface OnDefaultIconsListner {
		public abstract void onDefaultIconsClick(ArrayList<ShortCutInfo> infos);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.desk_setting_dock_default_icon_dialog, null);

		mGridView = (GridView) view.findViewById(R.id.gridview);

		mOKButton = (Button) view.findViewById(R.id.dialog_ok);
		mCancelButton = (Button) view.findViewById(R.id.dialog_cancel);

		mOKButton.setEnabled(false);
		mOKButton.setTextColor(0XFFB9B9B9);
		mOKButton.setOnClickListener(new PositiveClickListener());
		mCancelButton.setOnClickListener(new NegativeClickListener());
		setContentView(view);

		initGridView();
	}

	private void initGridView() {
		Drawable[] drawables = new Drawable[DockUtil.ICON_COUNT_IN_A_ROW];
		String pkg = GoSettingControler.getInstance(mContext).getShortCutSettingInfo().mStyle;
		ImageExplorer imageExplorer = ImageExplorer.getInstance(mContext);
		int[] ids = new int[] { R.drawable.shortcut_0_0_phone, R.drawable.shortcut_0_1_contacts,
				R.drawable.shortcut_0_2_funclist, R.drawable.shortcut_0_3_sms,
				R.drawable.shortcut_0_4_browser };
		int drawableBound = mContext.getResources().getDimensionPixelSize(R.dimen.screen_icon_size);
		for (int i = 0; i < DockUtil.ICON_COUNT_IN_A_ROW; i++) {
			DeskThemeBean.SystemDefualtItem dockThemeItem = DockItemControler.getSystemDefualtItem(
					pkg, i);
			Drawable drawable = null;
			if (null != dockThemeItem && null != dockThemeItem.mIcon
					&& null != dockThemeItem.mIcon.mResName) {
				// 主题安装包
				drawable = imageExplorer.getDrawable(pkg, dockThemeItem.mIcon.mResName);
			} else {
				// 风格安装包
				drawable = DockItemControler.getStylePkgDrawable(mContext, pkg, i);
			}

			try {
				if (drawable == null) {
					drawable = mContext.getResources().getDrawable(ids[i]);
				}
				if (drawable != null) {
					//有些主题的图片制件不规范，大小不一
					int width = drawable.getIntrinsicWidth();
					float scale = drawableBound * 1.0f / width;
					drawable = scale == 1 ? drawable : BitmapUtility.zoomDrawable(drawable, scale,
							scale, mContext.getResources());
				}
			} catch (OutOfMemoryError e) {
			}
			drawables[i] = drawable;
		}

		mAdapter = new IconAdapter();
		mAdapter.setDrawables(drawables);

		mGridView.setAdapter(mAdapter);
		mGridView.setNumColumns(4);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mAdapter.mSelected[position]) {
					mAdapter.mSelected[position] = false;
					mAdapter.notifyDataSetInvalidated();
				} else {
					int count = 0;
					for (int i = 0; i < mAdapter.mSelected.length; i++) {
						if (mAdapter.mSelected[i]) {
							count++;
						}
					}
					if (count >= mCount) {
						Toast.makeText(mContext, R.string.dock_is_full, Toast.LENGTH_SHORT).show();
					} else {
						mAdapter.mSelected[position] = true;
						mAdapter.notifyDataSetInvalidated();
					}
				}
				int count = 0;
				for (int i = 0; i < mAdapter.mSelected.length; i++) {
					if (mAdapter.mSelected[i]) {
						count++;
					}
				}
				if (count == 0) {
					mOKButton.setEnabled(false);
					mOKButton.setTextColor(0XFFB9B9B9);
				} else {
					mOKButton.setEnabled(true);
					mOKButton.setTextColor(0XFF343434);
				}
			}
		});
	}

	/**
	 * <br>功能简述:设置最多可以选中几个
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param count
	 */
	public void setCount(int count) {
		mCount = count;
	}

	/**
	 * 功能简述:设置ok和cancel按钮的监听
	 * @param isPositive
	 * @param text
	 * @param listener
	 */
	public void setDefaultClickListener(OnDefaultIconsListner listener) {
		mListener = listener;
	}

	/**
	 * 
	 * 类描述:OK Button的回调
	 * 功能详细描述:
	 * 
	 * @author  guoyiqing
	 * @date  [2012-9-15]
	 */
	private class PositiveClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				ArrayList<ShortCutInfo> infos = new ArrayList<ShortCutInfo>();

				for (int i = 0; i < DockUtil.ICON_COUNT_IN_A_ROW; i++) {
					if (mAdapter.mSelected[i]) {
						infos.add(mAdapter.mInfos[i]);
					}
				}
				mListener.onDefaultIconsClick(infos);
			}
			dismiss();
		}

	}

	/**
	 * 
	 * 类描述:cancel Button的回调
	 * 功能详细描述:
	 * 
	 * @author  guoyiqing
	 * @date  [2012-9-15]
	 */
	private class NegativeClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			dismiss();
		}
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
		private Drawable[] mDrawables;
		private ShortCutInfo[] mInfos;
		protected boolean[] mSelected; // 勾选项
		private LayoutInflater mInflater;

		public IconAdapter() {
			super();
			mInflater = LayoutInflater.from(getContext());

			//info初始化
			mInfos = new ShortCutInfo[DockUtil.ICON_COUNT_IN_A_ROW];
			String[] name = new String[] {
					mContext.getResources().getString(R.string.customname_dial),
					mContext.getResources().getString(R.string.customname_contacts),
					mContext.getResources().getString(R.string.customname_Appdrawer),
					mContext.getResources().getString(R.string.customname_sms),
					mContext.getResources().getString(R.string.customname_browser) };
			String[] res = new String[] { "shortcut_0_0_phone", "shortcut_0_1_contacts",
					"shortcut_0_2_funclist", "shortcut_0_3_sms", "shortcut_0_4_browser" };
			Intent[] intent = new Intent[] { AppIdentifier.createSelfDialIntent(mContext),
					AppIdentifier.createSelfContactIntent(mContext),
					new Intent(ICustomAction.ACTION_SHOW_FUNCMENU),
					AppIdentifier.createSelfMessageIntent(),
					AppIdentifier.createSelfBrowseIntent(mContext.getPackageManager()) };

			long time = System.currentTimeMillis();
			for (int i = 0; i < name.length; i++) {
				ShortCutInfo shortCutInfo = new ShortCutInfo();
				shortCutInfo.mItemType = IItemType.ITEM_TYPE_SHORTCUT;

				shortCutInfo.mFeatureIconType = ImagePreviewResultType.TYPE_DEFAULT;
				shortCutInfo.mIntent = intent[i];

				shortCutInfo.mFeatureIconPath = res[i];
				shortCutInfo.mFeatureTitle = name[i];
				shortCutInfo.mInScreenId = time + i;
				shortCutInfo.mFeatureIconPackage = ThemeManager.DEFAULT_THEME_PACKAGE;
				mInfos[i] = shortCutInfo;
			}

			//选中初始化
			mSelected = new boolean[] { false, false, false, false, false };
		}

		public void setDrawables(Drawable[] drawables) {
			mDrawables = drawables;
		}

		@Override
		public int getCount() {
			return mDrawables != null ? mDrawables.length : 0;
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
			ShortCutInfo info = null;
			try {
				info = mInfos[position];
			} catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}

			if (info == null) {
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
			if (mSelected[position]) {
				img.setVisibility(View.VISIBLE);
			} else {
				img.setVisibility(View.INVISIBLE);
			}
			textView.setCompoundDrawablesWithIntrinsicBounds(null, mDrawables[position], null, null);
			textView.setText(info.mFeatureTitle);
			textView.setTextSize(GoLauncher.getAppFontSize());

			return convertView;
		}

	}
}
