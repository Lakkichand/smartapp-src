package com.jiubang.ggheart.apps.desks.diy.themescan;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.SimpleAdapter;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.data.theme.parser.ThemeInfoParser;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 主题与widget的“获取更多”与“检查更新”弹出框
 * 
 * @author ruxueqin
 * 
 */

public class CheckUpdate implements ICleanable {
	private PopupWindow mPopupWindow = null;

	private OnDismissListener mDismissListener;

	private Activity mActivity;

	private View mPopupwindowParent;

	// GridView默认列数
	private static final int DEFAULT_NUMBER_COLUMNS = 3;

	// 链接入口，排序与相应morethemes.xml文件定义相同
	public static final int ENTRANCE_GOSTORE = 0;
	public static final int ENTRANCE_MARKET = 1;
	public static final int ENTRANCE_BROWSER = 2;
	private int[] mEntances = { ENTRANCE_GOSTORE, ENTRANCE_MARKET, ENTRANCE_BROWSER };

	/**
	 * 链接地址指定
	 */
	private String mMarketLink;
	private String mBrowserLink;

	public CheckUpdate(Activity activity) {
		mActivity = activity;
	}

	/**
	 * 设置弹出框的windowToken来源View
	 * 
	 * @param view
	 */
	public void setParentView(View view) {
		mPopupwindowParent = view;
		initMoreWindow();
	}

	public void popup() {
		mPopupWindow.showAtLocation(mPopupwindowParent, Gravity.BOTTOM | Gravity.CENTER, 0, 0);
	}

	private void initMoreWindow() {
		View view = mActivity.getLayoutInflater().inflate(R.layout.morethemes, null);

		initPopUpWindow(view);

		// animation
		mPopupWindow.setAnimationStyle(R.style.PopupAnimation);

		ArrayList<ThemeInfoBean> moreThemesInfoBeans = new ArrayList<ThemeInfoBean>();
		final String xmlFile = LauncherEnv.Path.GOTHEMES_PATH + "morethemes.xml";;

		// 解析本地文件生成themeinfobean列表
		final StringBuffer curVersionBuf = new StringBuffer();
		final StringBuffer recommendThemesBuf = new StringBuffer();
		new ThemeInfoParser().parseLauncherThemeXml(mActivity, xmlFile, curVersionBuf,
				recommendThemesBuf, moreThemesInfoBeans, ThemeConstants.LAUNCHER_FEATURED_THEME_ID);
		adjustEntrance(moreThemesInfoBeans);
		int size = moreThemesInfoBeans.size();
		String[] names = new String[size];
		String previewNames = null;
		int[] previewIamgesId = new int[size];
		// 获取图片
		for (int i = 0; i < names.length; i++) {
			names[i] = moreThemesInfoBeans.get(i).getThemeName();
			previewNames = moreThemesInfoBeans.get(i).getPreViewDrawableNames().get(0);
			previewIamgesId[i] = mActivity.getResources().getIdentifier(previewNames, "drawable",
					ThemeManager.DEFAULT_THEME_PACKAGE);
		}

		// 初始化popupwindow里面的MenuGrid，和Item响应
		initMenuGrid(view, names, previewIamgesId, size);
	}

	/**
	 * 初始化PopupWindow
	 * 
	 * @param popupWindow
	 * @param view
	 */
	private void initPopUpWindow(View view) {
		mPopupWindow = new PopupWindow(view, android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT); // 大小设置为全屏幕
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		mPopupWindow.setFocusable(true); // 如果不加这个，Grid不会响应ItemClick
		mPopupWindow.setOnDismissListener(mDismissListener);
	}

	/**
	 * 初始化MenuGrid，和Item的响应
	 * 
	 * @param view
	 * @param names
	 *            图片名字
	 * @param previewIamgesId
	 *            图片ID
	 * @param numColumns
	 *            MenuGrid的列数，对小于3的进行特殊处理
	 */
	private void initMenuGrid(View view, String[] names, int[] previewIamgesId, int numColumns) {
		GridView menuGrid = (GridView) view.findViewById(R.id.gridview);
		// 默认为3行，小于三行则设置为当前numColumns的行数，否则按照3行来排列
		if (numColumns > 0 && numColumns < DEFAULT_NUMBER_COLUMNS) {
			menuGrid.setNumColumns(numColumns);
		}
		menuGrid.setAdapter(getMenuAdapter(names, previewIamgesId));
		menuGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switch (arg2) {
				// GOStore
					case 0 : {
//						Intent intent = new Intent();
//						intent.setClass(mActivity, GoStore.class);
//						mActivity.startActivity(intent);
						AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
								MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
					}
						break;
					// Market
					case 1 : {
						if (AppUtils.isMarketExist(mActivity)) {
							// 如果存在电子市场
							AppUtils.gotoMarket(mActivity, mMarketLink);
						} else {
							// 否则
							// String tipStr =
							// mActivity.getString(R.string.no_googlemarket_tip);
							// DeskToast.makeText(mActivity, tipStr,
							// Toast.LENGTH_SHORT).show();

							// 跳3G市场
							AppUtils.gotoBrowser(mActivity, mBrowserLink);
						}
					}
						break;
					// GOLauncherEX
					case 2 : {
						AppUtils.gotoBrowser(mActivity, mBrowserLink);
					}
						break;
					default :
						break;
				}
				if (mPopupWindow.isShowing()) {
					// 关闭菜单
					mPopupWindow.dismiss();
				}
			}
		});
	}

	/**
	 * “检查更新”入口按地区数据不同
	 */
	private void adjustEntrance(ArrayList<ThemeInfoBean> moreThemesInfoBeans) {
		if (null == moreThemesInfoBeans || moreThemesInfoBeans.isEmpty()) {
			return;
		}

		if (!Machine.isCnUser(mActivity)) {
			// 外国用户，不显示3G链接
			if (moreThemesInfoBeans.size() > 2) {
				moreThemesInfoBeans.remove(2);
			}
		}

		boolean useBrowserOption = false;
		if (mEntances != null) {
			for (int i = 0; i < mEntances.length; i++) {
				if (mEntances[i] == ENTRANCE_BROWSER) {
					useBrowserOption = true;
					break;
				}
			}
		}
		if (!useBrowserOption) {
			if (moreThemesInfoBeans.size() > 2) {
				moreThemesInfoBeans.remove(2);
			}
		}

		if (!AppUtils.isMarketExist(mActivity)) {
			// 没有安装电子市场，不显示电子市场链接
			if (moreThemesInfoBeans.size() > 1) {
				moreThemesInfoBeans.remove(1);
			}
		}
		// 对外部调制的入口定义调整
		// if (null != mEntances)
		// {
		int size = moreThemesInfoBeans.size();
		for (int i = size - 1; i >= 0; i--) {
			ThemeInfoBean bean = moreThemesInfoBeans.get(i);
			for (int j = 0; j < mEntances.length; j++) {
				if (mEntances[j] == i) {
					break;
				}
				if (j == mEntances.length - 1) {
					// 最后一次都没找到入口，就删除
					moreThemesInfoBeans.remove(bean);
				}
			}
		}
		// }
	}

	/**
	 * 构造菜单Adapter
	 * 
	 * @param menuNameArray
	 *            名称数组
	 * @param imageResourceArray
	 *            图片数组
	 * @return SimpleAdapter
	 */
	private SimpleAdapter getMenuAdapter(String[] nameArray, int[] imageArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < nameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageArray[i]);
			map.put("itemText", nameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(mActivity, data, R.layout.morethemes_item,
				new String[] { "itemImage", "itemText" }, new int[] { R.id.item_image,
						R.id.item_text });
		return simperAdapter;
	}

	public void setmMarketLink(String link) {
		mMarketLink = link;
	}

	public void setmBrowserLink(String link) {
		mBrowserLink = link;
	}

	public void setmDismissListener(OnDismissListener listener) {
		mDismissListener = listener;
		if (null != mPopupWindow) {
			mPopupWindow.setOnDismissListener(mDismissListener);
		}
	}

	public void setEntrances(int[] entrance) {
		mEntances = entrance;
	}

	@Override
	public void cleanup() {
		mDismissListener = null;
		mActivity = null;
		mPopupwindowParent = null;
		mMarketLink = null;
		mBrowserLink = null;
		mEntances = null;
		if (null != mPopupWindow) {
			mPopupWindow.dismiss();
			mPopupWindow = null;
		}
	}
}
