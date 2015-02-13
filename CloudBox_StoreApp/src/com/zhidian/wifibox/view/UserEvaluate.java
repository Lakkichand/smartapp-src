package com.zhidian.wifibox.view;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.CommentAdapter;
import com.zhidian.wifibox.controller.DoCommentsController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.pulldownlistview.PullDownRefreshView;
import com.zhidian.wifibox.pulldownlistview.PullDownRefreshView.onLoadMoreListener;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;
import com.zhidian.wifibox.view.CommentView.OnCallBackListener;
import com.zhidian.wifibox.view.dialog.LoadingDialog;

/**
 * 用户评价
 * 
 * @author zhaoyl
 * 
 */
public class UserEvaluate {

	private Context mContext;
	private long appId;
	private ListView listView; // 用户评论列表
	private CommentAdapter adapter;
	private int pageNow = 1;
	private CommentView commentView;
	private LinearLayout home_liear_pro; // 加载数据进度条
	private LinearLayout now_layout; // 当前页界面
	private BgPageView bgPageView;
	private LoadingDialog loadingDialog;
	private PullDownRefreshView refreshView;
	private boolean isLoading = false;
	public int DataSizePerPage = 18;

	public UserEvaluate(Context context, View view, long appId) {
		mContext = context;
		this.appId = appId;
		initView(view);
		initListView();
		bgPageView.showProgress();
		getCommentData();
	}

	/**************************
	 * 初始化UI
	 * 
	 * @param view
	 ************************/
	private void initView(View view) {
		home_liear_pro = (LinearLayout) view.findViewById(R.id.home_liear_pro);
		now_layout = (LinearLayout) view.findViewById(R.id.detail_home);

		// 下拉刷新上拉加载更多控件
		refreshView = (PullDownRefreshView) view
				.findViewById(R.id.detail_evaluate_refreshview);
		listView = (ListView) refreshView.getChildAt(1);
		bgPageView = new BgPageView(mContext, home_liear_pro, now_layout);

		refreshView.setOnLoadMoreListener(new onLoadMoreListener() {

			@Override
			public void onLoadMore() {// 底部加载更多
				refreshView.setOnLoadState(false, false);
				if (!isLoading) {
					Log.i("加载", "加载更多");
					pageNow++;
					getCommentData();
				}else {
					refreshView.removeListFootView();
					 //移除加载更多的footview
					Toast.makeText(mContext, "已经是最后一页啦",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	/*********************
	 * 获取应用评论数据
	 *********************/
	private void getCommentData() {

		String[] str = { pageNow + "", appId + "" };
		TAApplication.getApplication().doCommand(
				mContext.getString(R.string.commentcontroller),
				new TARequest(CDataDownloader.getCommentUrl(), str),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						PageDataBean bean = (PageDataBean) response.getData();
						if (bean == null) {
							bgPageView.showContent();
							return;
						}
						if (bean.mStatuscode == 0) {
							bgPageView.showContent();
							if (pageNow == 1) {

								if (commentView.getXianView() != null
										&& bean.commentList.size() > 0) {
									commentView.getXianView().setVisibility(
											View.VISIBLE);
								}
								adapter = new CommentAdapter(mContext,
										bean.commentList);
								listView.setAdapter(adapter);
							} else {
								if (bean.commentList != null) {
									adapter.addItems(bean.commentList);
								}

							}

							if (bean.mTotalPage > 1
									&& pageNow < bean.mTotalPage) {
								isLoading = false;
								refreshView.setOnLoadState(false, false);
								refreshView.initListFootView(adapter, listView); // 初始化加载更多的footview
								// onDataSuccess(null);
							} else {
								if (pageNow > 1) {
									isLoading = true;
								}
							}
						} else {
							bgPageView
									.showLoadException(new onCallBackOnClickListener() {

										@Override
										public void onClick() {
											bgPageView.showProgress();
											getCommentData();
										}
									});
						}
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {
						bgPageView
								.showLoadException(new onCallBackOnClickListener() {

									@Override
									public void onClick() {
										bgPageView.showProgress();
										getCommentData();
									}
								});

					}
				}, true, false);

	}

	/*********************
	 * 发表评论
	 *********************/
	private void doCommentsApp(String nickname, String content, String score) {
		showDialogMessage("卖命发表ing...");
		String[] str = { nickname, content, score, appId + "" };
		TAApplication.getApplication().doCommand(
				mContext.getString(R.string.docommentscontroller),
				new TARequest(DoCommentsController.SEND_COMMENT, str),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						int status = (Integer) response.getData();
						if (status == 0) {
							// 发表成功
							pageNow = 1;
							getCommentData();
							Toast.makeText(mContext, "酷！吐槽成功~~",
									Toast.LENGTH_SHORT).show();
						} else {
							// 失败
							Toast.makeText(mContext, "发表失败", Toast.LENGTH_SHORT)
									.show();
						}

						closeDialog();
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}

					@Override
					public void onFailure(TAResponse response) {
						closeDialog();
						Toast.makeText(mContext, "发表失败", Toast.LENGTH_SHORT)
								.show();

					}
				}, true, false);

	}

	/*********************
	 * 评论数据加载
	 *********************/
	private void initListView() {
		LayoutInflater inflater = LayoutInflater.from(mContext);
		commentView = (CommentView) inflater.inflate(
				R.layout.view_head_comment, null);
		listView.addHeaderView(commentView);
		commentView.onCallBackClick(new OnCallBackListener() {

			@Override
			public void onclick(String nickname, String content, String score) {
				doCommentsApp(nickname, content, score);
			}
		});

	}

	/***************************
	 * 对话框
	 ***************************/

	private void showDialogMessage(CharSequence message) {
		try {
			if (loadingDialog == null) {
				loadingDialog = new LoadingDialog(mContext);
			}
			loadingDialog.setMessage(message);
			loadingDialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void closeDialog() {
		try {
			loadingDialog.dismiss();
			loadingDialog = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
