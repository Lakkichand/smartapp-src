package com.zhidian.wifibox.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.FrameLayout;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.DownloadManagerActivity;
import com.zhidian.wifibox.adapter.DownloadManagerAdapter;
import com.zhidian.wifibox.controller.DownloadManagerController;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;

/**
 * 下载管理列表，下载数据由DownloadManagerController提供
 * 
 * @author xiedezhi
 * 
 */
public class DownloadManagerContainer extends FrameLayout implements IContainer {
	private ExpandableListView mListView;
	private View mNoContent;
	private DownloadManagerAdapter mAdapter;
	/**
	 * controller数据回调接口
	 */
	private TAIResponseListener mRListener = new TAIResponseListener() {

		@Override
		public void onStart() {
		}

		@Override
		public void onSuccess(TAResponse response) {
			Object[] objs = (Object[]) response.getData();
			List<DownloadTask> downloadingList = (List<DownloadTask>) objs[0];
			List<DownloadTask> downloadedList = (List<DownloadTask>) objs[1];
			mAdapter.update(downloadingList, downloadedList);
			if (downloadingList.size() <= 0 && downloadedList.size() <= 0) {
				mListView.setVisibility(View.GONE);
				mNoContent.setVisibility(View.VISIBLE);
			} else {
				mListView.setVisibility(View.VISIBLE);
				mNoContent.setVisibility(View.GONE);
			}
		}

		@Override
		public void onRuning(TAResponse response) {
		}

		@Override
		public void onFailure(TAResponse response) {
		}

		@Override
		public void onFinish() {
		}
	};

	public DownloadManagerContainer(Context context) {
		super(context);
	}

	public DownloadManagerContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DownloadManagerContainer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		mListView = (ExpandableListView) findViewById(R.id.myExpandableListView);
		mNoContent = findViewById(R.id.no_content);
		mListView.setGroupIndicator(null);
		mAdapter = new DownloadManagerAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				return true;
			}
		});
		findViewById(R.id.jump).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((DownloadManagerActivity) (getContext())).finish();
				TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONFEATURE, null);
				postDelayed(new Runnable() {

					@Override
					public void run() {
						TAApplication.sendHandler(null, IDiyFrameIds.ACTIONBAR,
								IDiyMsgIds.JUMP_TITLE, 0, null, null);
					}
				}, 50);
			}
		});
	}

	@Override
	public void onAppAction(String packName) {
		// 向DownloadService获取下载列表
		TARequest request = new TARequest(
				DownloadManagerController.GET_DOWNLOAD_TASK, null);
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.downloadmanagercontroller),
				request, mRListener, true, false);
	}

	@Override
	public String getDataUrl() {
		return PageDataBean.DOWNLOADMANAGER_URL;
	}

	@Override
	public void updateContent(PageDataBean bean) {
		// 向DownloadService获取下载列表
		TARequest request = new TARequest(
				DownloadManagerController.GET_DOWNLOAD_TASK, null);
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.downloadmanagercontroller),
				request, mRListener, true, false);
		mListView.expandGroup(0);
		mListView.expandGroup(1);
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		// 向DownloadService获取下载列表
		TARequest request = new TARequest(
				DownloadManagerController.GET_DOWNLOAD_TASK, null);
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.downloadmanagercontroller),
				request, mRListener, true, false);
	}

	@Override
	public void onResume() {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void beginPage() {
		// do nothing
	}

	@Override
	public void endPage() {
		// do nothing
	}

}
