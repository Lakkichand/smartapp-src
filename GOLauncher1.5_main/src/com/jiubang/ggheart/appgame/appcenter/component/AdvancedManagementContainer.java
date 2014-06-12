/**
 * 
 */
package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.ContainerBuiler;
import com.jiubang.ggheart.appgame.base.component.IContainer;
import com.jiubang.ggheart.appgame.base.component.IMenuHandler;
import com.jiubang.ggheart.appgame.base.downloadmanager.AppsDownloadActivity;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * 高级管理主界面
 * @author liguoliang
 *
 */
public class AdvancedManagementContainer extends RelativeLayout implements IContainer {
	private Context mContext;
	private GridView mGridView;
	private AdvancedManagementAdapter mAdapter;
	private List<AdvanceManagementBean> mBeanList;
	public AdvancedManagementContainer(Context context) {
		super(context);
		init(context);
	}

	public AdvancedManagementContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public AdvancedManagementContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		initView();
		updateData();
	}

	private void initView() {
		mGridView = (GridView) this.findViewById(R.id.app_advanced_management_gridview);
		mAdapter = new AdvancedManagementAdapter();
		mGridView.setAdapter(mAdapter);
	}

	private void updateData() {
		if (mBeanList == null) {
			mBeanList = new ArrayList<AdvanceManagementBean>();
		} else {
			mBeanList.clear();
		}
		// 下载管理
		boolean channelNeedDownload = ChannelConfig.getInstance(mContext).isNeedDownloadService();
		if (channelNeedDownload) {
			AdvanceManagementBean beanDownload = new AdvanceManagementBean();
			beanDownload.mIcon = getResources().getDrawable(
					R.drawable.appcenter_icon_download_management);
			beanDownload.mName = getResources().getString(
					R.string.appcenter_advanced_management_download);
			beanDownload.mOperator = AdvanceManagementBean.OPERATOR_DOWNLOAD_MANAGEMENT;
			mBeanList.add(beanDownload);
		}		

		// 安装包管理
		boolean channedNeedPackage = ChannelConfig.getInstance(mContext).isNeedPackageManagement();
		if (channedNeedPackage) {
			AdvanceManagementBean beanPackage = new AdvanceManagementBean();
			beanPackage.mIcon = getResources().getDrawable(
					R.drawable.appcenter_icon_package_management);
			beanPackage.mName = getResources()
					.getString(R.string.appcenter_advanced_management_package);
			beanPackage.mOperator = AdvanceManagementBean.OPERATOR_PACKAGE_MANAGEMENT;
			mBeanList.add(beanPackage);
		}		

		// 应用搬家
		AdvanceManagementBean beanMakespace = new AdvanceManagementBean();
		beanMakespace.mIcon = getResources().getDrawable(
				R.drawable.appcenter_icon_app_move);
		beanMakespace.mName = getResources().getString(
				R.string.appcenter_advanced_management_makespace);
		beanMakespace.mOperator = AdvanceManagementBean.OPERATOR_APP_MOVE;
		mBeanList.add(beanMakespace);

		// 订单管理
		// 目前暂时不需要订单管理
//		AdvanceManagementBean beanOrders = new AdvanceManagementBean();
//		beanOrders.mIcon = getResources().getDrawable(
//				R.drawable.appcenter_icon_orders_management);
//		beanOrders.mName = getResources().getString(R.string.appcenter_advanced_management_orders);
//		beanOrders.mOperator = AdvanceManagementBean.OPERATOR_ORDERS_MANAGEMENT;
//		mBeanList.add(beanOrders);
		
		mAdapter.updateList(mBeanList);
	}

	@Override
	public void cleanup() { 
		// 切换时回收数据
		if (mBeanList != null) {
			mBeanList.clear();
			mBeanList = null;			
		}
		if (mAdapter != null) {
			mAdapter.updateList(null);
			mAdapter = null;
		}
	}

	@Override
	public void sdCardTurnOff() {
		// 不需要处理SD卡事件
	}

	@Override
	public void sdCardTurnOn() {
		// 不需要处理SD卡事件
	}

	@Override
	public void onActiveChange(boolean isActive) {
	}

	@Override
	public boolean onPrepareOptionsMenu(AppGameMenu menu) {
		int[] resId = new int[] { IMenuHandler.MENU_ITEM_SETTING, IMenuHandler.MENU_ITEM_FEEDBACK };
		menu.setResourceId(resId);
		menu.show(this);
		return true;
	}

	@Override
	public boolean onOptionItemSelected(int id) {
		return false;
	}

	@Override
	public void onResume() {

	}

	@Override
	public void onStop() {

	}

	@Override
	public void onAppAction(String packName, int appAction) {

	}

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {

	}

	@Override
	public void initEntrance(int access) {

	}

	@Override
	public int getTypeId() {
		return 0;
	}

	@Override
	public void onFinishAllUpdateContent() {

	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {

	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {

	}

	@Override
	public void onTrafficSavingModeChange() {

	}

	@Override
	public void setUpdateData(Object value, int state) {

	}

	private void operate(int operator) {
		switch (operator) {
			case AdvanceManagementBean.OPERATOR_DOWNLOAD_MANAGEMENT :
				StatisticsData.countStatData(mContext, StatisticsData.KEY_DOWNLOAD_MANAGER_CLICK);
				// 下载管理
				Intent i = new Intent(mContext, AppsDownloadActivity.class);
				mContext.startActivity(i);
				break;
			case AdvanceManagementBean.OPERATOR_PACKAGE_MANAGEMENT :
				StatisticsData.countStatData(mContext, StatisticsData.KEY_INSTALL_MANAGER_CLICK);
				// 安装包管理
//				Intent intent = new Intent(mContext, PackageManagementActivity.class);
//				mContext.startActivity(intent);
				AppsManagementActivity.sendMessage(this,
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.SHOW_PACKAGE_MANAGEMENT_VIEW, -1, null, null);
				break;
			case AdvanceManagementBean.OPERATOR_APP_MOVE :
				StatisticsData.countStatData(mContext, StatisticsData.KEY_APP_MOVE_CLICK);
				// 应用搬家
				AppsManagementActivity.sendMessage(null,
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.SHOW_APP_MIGRATION_VIEW, -1, null, null);
				break;
			case AdvanceManagementBean.OPERATOR_ORDERS_MANAGEMENT :
				// 订单管理
				break;
			default :
				break;
		}
	}

	/**
	 * 
	 * @author liguoliang
	 *
	 */
	private class AdvanceManagementBean {
		public Drawable mIcon;

		public String mName;

		public int mOperator;

		/**
		 * 操作：下载管理
		 */
		public static final int OPERATOR_DOWNLOAD_MANAGEMENT = 100;

		/**
		 * 操作：安装包管理
		 */
		public static final int OPERATOR_PACKAGE_MANAGEMENT = 101;

		/**
		 * 操作：应用搬家
		 */
		public static final int OPERATOR_APP_MOVE = 102;

		/**
		 * 操作：订单管理
		 */
		public static final int OPERATOR_ORDERS_MANAGEMENT = 103;
	}

	/**
	 * @author liguoliang
	 *
	 */
	private class AdvancedManagementAdapter extends BaseAdapter {
		private List<AdvanceManagementBean> mAppBeanList = null;
		private LayoutInflater mInflater = null;

		public AdvancedManagementAdapter() {
			mInflater = LayoutInflater.from(mContext);
		}

		public void updateList(List<AdvanceManagementBean> beanList) {
			if (beanList == null) {
				if (mAppBeanList != null) {
					mAppBeanList.clear();
					mAppBeanList = null;
				}
				notifyDataSetChanged();				
				return;
			}
			mAppBeanList = (ArrayList<AdvanceManagementBean>) ((ArrayList<AdvanceManagementBean>) beanList)
					.clone();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			if (mAppBeanList != null) {
				return mAppBeanList.size();
			}
			return 0;
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
			final AdvanceManagementBean bean = mAppBeanList.get(position);
			if (bean == null) {
				return null;
			}
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.apps_management_advancedmanagement_item_layout, null);
				holder = new ViewHolder();
				holder.mIcon = (ImageView) convertView.findViewById(R.id.imageView1);
				holder.mName = (TextView) convertView.findViewById(R.id.textView1);				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.mIcon.setImageDrawable(bean.mIcon);
			holder.mName.setText(bean.mName);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					operate(bean.mOperator);
				}
			});
			return convertView;
		}
	}

	/**
	 * @author liguoliang
	 *
	 */
	private class ViewHolder {
		public ImageView mIcon;
		public TextView mName;
	}

	@Override
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans, List<IContainer> containers) {
		// do nothing
		
	}

	@Override
	public void removeContainers() {
		// do nothing
		
	}

	@Override
	public List<IContainer> getSubContainers() {
		// do nothing
		return null;
	}

	@Override
	public void onMultiVisiableChange(boolean visiable) {
		// do nothing
		
	}

	@Override
	public void prevLoading() {
		//do nothing
	}

	@Override
	public void prevLoadFinish() {
		//do nothing
	}
	
	@Override
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}
}
