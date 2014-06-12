package com.jiubang.ggheart.apps.appmanagement.component;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.gowidget.gostore.component.ThemeTitle;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.AppsBean.AppBean;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 忽略更新应用的页面
 * 
 * @author zhoujun
 * 
 */
public class AppsNoUpdateViewContainer extends LinearLayout {

	private LayoutInflater layoutInflater;
	private ThemeTitle mThemeTitle = null;
	private LinearLayout mLinearlayout;
	// 全部恢复/刷新
	private TextView mOperationButton;
	/**
	 * 刷新操作
	 */
	private static final int OPERATION_TYPE_REFRESH = 0;
	/**
	 * 全部恢复更新操作
	 */
	private static final int OPERATION_TYPE_REPROMPT_UPDATE = 1;
	private int mOperationType = 0;
	private TextView mUpdateText;

	private ListView mListView;
	private NoUpdateAdapter noUpdateApdater;
	private Context mContext;
	private Handler mHandler;
	private OnButtonClick mButtonClick;
	/**
	 * 无数据时，显示的提示信息
	 */
	private LinearLayout mNoDataLinear;
	private TextView mNoDataInfoText;

	public void setmButtonClick(OnButtonClick buttonClick) {
		this.mButtonClick = buttonClick;
	}

	interface OnButtonClick {
		public void click(String packageName, int position);
	}

	public AppsNoUpdateViewContainer(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public AppsNoUpdateViewContainer(Context context, AttributeSet attr) {
		super(context, attr);
		mContext = context;
		init();
	}

	private void init() {
		this.setOrientation(LinearLayout.VERTICAL);
		setBackgroundColor(Color.parseColor("#faf9f9"));
		layoutInflater = LayoutInflater.from(mContext);
		initTitle();
		initView();
	}

	private void initTitle() {
		mThemeTitle = (ThemeTitle) layoutInflater.inflate(R.layout.themestore_toptitle, null);
		String title = mContext.getString(R.string.apps_no_prompt_update_title);
		mThemeTitle.setTitleText(title);
		mThemeTitle.setBackgroundResource(R.drawable.themestore_detail_topbar_bg);
		mThemeTitle.setBackViewOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Message message = mHandler
						.obtainMessage(AppsManagementActivity.NO_UPDATE_BUTTON_CLICK);
				message.arg1 = AppsManagementActivity.MESSAGE_REMOVE_NO_UPDATE_VIEW;
				mHandler.sendMessage(message);
				// finish();
			}
		});
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		this.addView(mThemeTitle, params);
	}

	private void initView() {
		mLinearlayout = (LinearLayout) layoutInflater.inflate(
				R.layout.apps_no_prompt_update_list_container, null);
		mOperationButton = (TextView) mLinearlayout.findViewById(R.id.no_operation_button);
		mOperationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mOperationType == OPERATION_TYPE_REFRESH) {
					refreshView();
				} else {
					buttonClick(null, -1);
				}
			}
		});
		mUpdateText = (TextView) mLinearlayout.findViewById(R.id.no_update_info);
		mListView = (ListView) mLinearlayout.findViewById(R.id.no_upate_list_view);
		noUpdateApdater = new NoUpdateAdapter(mContext);
		mListView.setAdapter(noUpdateApdater);

		mNoDataLinear = (LinearLayout) mLinearlayout.findViewById(R.id.no_upate_no_data_linear);
		mNoDataInfoText = (TextView) mLinearlayout.findViewById(R.id.no_data_text);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		this.addView(mLinearlayout, params);
	}

	/**
	 * 点击恢复更新操作
	 * 
	 * @param packageName
	 *            包名为null时，默认恢复所有
	 * @param position
	 */
	public void buttonClick(String packageName, int position) {
		if (mButtonClick != null) {
			mButtonClick.click(packageName, position);
			if (noUpdateApdater != null) {
				noUpdateApdater.notifyDataSetChanged();
				setUpdateText();
			}
		}
	}

	public void setmHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public void setmAppBeanList(ArrayList<AppBean> mAppBeanList) {
		// this.mAppBeanList = mAppBeanList;
		if (noUpdateApdater != null) {
			noUpdateApdater.refreshData(mAppBeanList);
			noUpdateApdater.notifyDataSetChanged();

			setUpdateText();
		}
	}

	public void refreshView() {
		if (noUpdateApdater != null) {
			noUpdateApdater.notifyDataSetChanged();
			setUpdateText();
		}
	}

	/**
	 * 设置忽略更新信息
	 */
	private void setUpdateText() {
		if (noUpdateApdater != null) {
			ArrayList<AppBean> appList = noUpdateApdater.getmNoUpdateInfos();
			int size = appList != null ? appList.size() : 0;
			String text = mContext.getString(R.string.apps_management_none_for_no_update);
			if (size <= 0) {
				// 改变颜色
				// operationButton.setEnabled(false);
				// operationButton.setTextColor(Color.parseColor("#acacac"));

				if (mNoDataLinear.getVisibility() == View.GONE) {
					mNoDataLinear.setVisibility(View.VISIBLE);
				}
				mNoDataInfoText.setVisibility(View.VISIBLE);
				if (!GoLauncher.isPortait()) {
					mNoDataInfoText.setVisibility(View.GONE);
				}
				mOperationButton.setText(R.string.refresh);
				mOperationType = OPERATION_TYPE_REFRESH;
			} else {
				text = size + mContext.getString(R.string.apps_management_has_no_update_item);
				mNoDataLinear.setVisibility(View.GONE);
				mOperationButton.setText(R.string.apps_management_reprompt_all_update);
				mOperationType = OPERATION_TYPE_REPROMPT_UPDATE;
			}
			mUpdateText.setText(text);
		}
	}

	public void clean() {
		if (mListView != null) {
			int count = mListView.getChildCount();
			if (count > 0) {
				AppsNoUpdateInfoListItem listItemView = null;
				for (int i = 0; i < count; i++) {
					listItemView = (AppsNoUpdateInfoListItem) mListView.getChildAt(i);
					listItemView.destory();
				}
			}
			mListView.setAdapter(null);
		}
		if (noUpdateApdater != null) {
			ArrayList<AppBean> appInfo = noUpdateApdater.getmNoUpdateInfos();
			if (appInfo != null) {
				appInfo = null;
			}
			noUpdateApdater = null;
		}

	}

	class NoUpdateAdapter extends BaseAdapter {
		private ArrayList<AppBean> mNoUpdateInfos;

		// private ArrayList<NoPromptUpdateInfo> mNoUpdateInfos;
		private Context mContext;
		private LayoutInflater mLayoutInflater = null;
		private ArrayList<AppItemInfo> mAppItemInfos;

		public NoUpdateAdapter(Context context) {
			mContext = context;
			mLayoutInflater = LayoutInflater.from(mContext);
			mAppItemInfos = GOLauncherApp.getAppDataEngine().getAllAppItemInfos();
		}

		// private void refreshData(ArrayList<NoPromptUpdateInfo> noUpdateInfos)
		// {
		// mNoUpdateInfos = noUpdateInfos;
		// }

		public void refreshData(ArrayList<AppBean> noUpdateInfos) {
			mNoUpdateInfos = noUpdateInfos;
		}

		@Override
		public int getCount() {
			return mNoUpdateInfos == null ? 0 : mNoUpdateInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return mNoUpdateInfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			AppsNoUpdateInfoListItem noUpdateInfoListItem = null;
			if (mNoUpdateInfos != null && position < mNoUpdateInfos.size()) {
				AppBean appBean = mNoUpdateInfos.get(position);
				if (convertView != null && convertView instanceof AppsNoUpdateInfoListItem) {
					noUpdateInfoListItem = (AppsNoUpdateInfoListItem) convertView;
					noUpdateInfoListItem.resetDefaultStatus();
				}
				if (noUpdateInfoListItem == null) {
					noUpdateInfoListItem = (AppsNoUpdateInfoListItem) mLayoutInflater.inflate(
							R.layout.appsmanagement_no_upate_list_item, null);
					noUpdateInfoListItem.setOnClickListener(onClickListen);
				}
				noUpdateInfoListItem.bindAppBean(mContext, position, appBean, mAppItemInfos);
			}

			return noUpdateInfoListItem;
		}

		public ArrayList<AppBean> getmNoUpdateInfos() {
			return mNoUpdateInfos;
		}

		private OnClickListener onClickListen = new OnClickListener() {

			@Override
			public void onClick(View v) {
				AppBean appBean = (AppBean) v.getTag();
				// sendMessage(appBean.mPkgName,v.getId());
				buttonClick(appBean.mPkgName, v.getId());
			}
		};
	}

}
