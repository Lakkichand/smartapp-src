package com.zhidian.wifibox.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.FreezeAppAdapter;
import com.zhidian.wifibox.controller.FreezeAppController;
import com.zhidian.wifibox.data.AppUninstallBean;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.root.RootShell;
import com.zhidian.wifibox.util.AppFreezer;
import com.zhidian.wifibox.util.AppUninstaller;
import com.zhidian.wifibox.view.dialog.DeleteSysAppDialog;
import com.zhidian.wifibox.view.dialog.LoadingDialog;

/**
 * 回收站
 * 
 * @author xiedezhi
 * 
 */
public class FreezeAppActivity extends Activity {

	private ListView mListView;
	private FreezeAppAdapter mAdapter;
	private LinearLayout mNoContent;
	private LinearLayout mBtnFrame;
	private List<AppUninstallBean> mList = new ArrayList<AppUninstallBean>();
	private Button mUninstall;
	private Button mRestore;
	private View mLoadingFrame;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.freezeapp);

		findViewById(R.id.header_title_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
		TextView title = (TextView) findViewById(R.id.header_title_text);
		title.setText("回收站");
		TextView select_all = (TextView) findViewById(R.id.select_all);
		select_all.setVisibility(View.VISIBLE);
		select_all.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mList != null) {
					boolean allselect = true;
					for (AppUninstallBean bean : mList) {
						if (!bean.isSelect) {
							allselect = false;
							break;
						}
					}
					if (!allselect) {
						for (AppUninstallBean bean : mList) {
							bean.isSelect = true;
						}
					} else {
						for (AppUninstallBean bean : mList) {
							bean.isSelect = false;
						}
					}
					updateSelectText();
					mAdapter.notifyDataSetChanged();
				}
			}
		});
		mListView = (ListView) findViewById(R.id.listview);
		mAdapter = new FreezeAppAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setVisibility(View.GONE);
		mLoadingFrame = findViewById(R.id.loading_frame);
		mLoadingFrame.setVisibility(View.VISIBLE);
		mNoContent = (LinearLayout) findViewById(R.id.no_content);
		mNoContent.setVisibility(View.GONE);
		mBtnFrame = (LinearLayout) findViewById(R.id.btn_frame);
		mBtnFrame.setVisibility(View.GONE);
		mUninstall = (Button) findViewById(R.id.uninstall);
		mUninstall.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 彻底删除
				// 弹框
				DeleteSysAppDialog ddialog = new DeleteSysAppDialog(
						FreezeAppActivity.this, new OnClickListener() {

							@Override
							public void onClick(View v) {
								final Set<String> pkgs = new HashSet<String>();
								for (AppUninstallBean bean : mList) {
									if (bean.isSelect) {
										pkgs.add(bean.packname);
									}
								}
								if (pkgs.size() <= 0) {
									Toast.makeText(getApplicationContext(),
											"请选择需要删除的内容", Toast.LENGTH_SHORT)
											.show();
								} else {
									final LoadingDialog dialog = new LoadingDialog(
											FreezeAppActivity.this, "努力删除中...");
									dialog.setCancelable(false);
									dialog.show();
									new Thread("uninstallsystemapp") {
										@Override
										public void run() {
											if (!RootShell.isRootValid()) {
												runOnUiThread(new Runnable() {
													public void run() {
														Toast.makeText(
																getApplicationContext(),
																"操作失败，请检查是否有root权限",
																Toast.LENGTH_SHORT)
																.show();
														dialog.dismiss();
													}
												});
												return;
											}
											final Set<String> suc = new HashSet<String>();
											for (String pkg : pkgs) {
												boolean b = AppUninstaller
														.silentUninstallSystemApp(pkg);
												if (b) {
													suc.add(pkg);
												}
											}
											runOnUiThread(new Runnable() {
												public void run() {
													Toast.makeText(
															getApplicationContext(),
															"成功删除" + suc.size()
																	+ "个应用",
															Toast.LENGTH_SHORT)
															.show();
													dialog.dismiss();
													// 过滤
													List<AppUninstallBean> xlist = new ArrayList<AppUninstallBean>();
													for (AppUninstallBean bean : mList) {
														if (suc.contains(bean.packname)) {
															continue;
														}
														xlist.add(bean);
													}
													mList = xlist;
													mAdapter.update(mList);
													if (mList.size() <= 0) {
														mNoContent
																.setVisibility(View.VISIBLE);
														mListView
																.setVisibility(View.GONE);
														mBtnFrame
																.setVisibility(View.GONE);
													}
												}
											});
										}
									}.start();
								}

							}
						}, new OnClickListener() {

							@Override
							public void onClick(View v) {
							}
						});
				ddialog.setCancelable(true);
				ddialog.show();
			}
		});
		mRestore = (Button) findViewById(R.id.restore);
		mRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 还原
				final Set<AppUninstallBean> selectBeans = new HashSet<AppUninstallBean>();
				for (AppUninstallBean bean : mList) {
					if (bean.isSelect) {
						selectBeans.add(bean);
					}
				}
				if (selectBeans.size() <= 0) {
					Toast.makeText(getApplicationContext(), "请选择需要还原的内容",
							Toast.LENGTH_SHORT).show();
				} else {
					final LoadingDialog dialog = new LoadingDialog(
							FreezeAppActivity.this, "努力还原中...");
					dialog.setCancelable(false);
					dialog.show();
					new Thread("restoresystemapp") {
						@Override
						public void run() {
							if (!RootShell.isRootValid()) {
								runOnUiThread(new Runnable() {
									public void run() {
										Toast.makeText(getApplicationContext(),
												"操作失败，请检查是否有root权限",
												Toast.LENGTH_SHORT).show();
										dialog.dismiss();
									}
								});
								return;
							}
							final Set<AppUninstallBean> successBean = new HashSet<AppUninstallBean>();
							for (AppUninstallBean bean : selectBeans) {
								boolean b = AppFreezer.enablePackage(
										TAApplication.getApplication(),
										bean.packname);
								if (b) {
									bean.isSelect = false;
									successBean.add(bean);
								}
							}
							runOnUiThread(new Runnable() {
								public void run() {
									Toast.makeText(
											getApplicationContext(),
											"成功还原" + successBean.size() + "个应用",
											Toast.LENGTH_SHORT).show();
									dialog.dismiss();
									// 过滤
									List<AppUninstallBean> xlist = new ArrayList<AppUninstallBean>();
									for (AppUninstallBean bean : mList) {
										if (successBean.contains(bean)) {
											continue;
										}
										xlist.add(bean);
									}
									mList = xlist;
									mAdapter.update(mList);
									if (mList.size() <= 0) {
										mNoContent.setVisibility(View.VISIBLE);
										mListView.setVisibility(View.GONE);
										mBtnFrame.setVisibility(View.GONE);
									}
									// 通知AppUninstallActivity加入还原的应用
									TAApplication.sendHandler(null,
											IDiyFrameIds.APPUNINSTALL,
											IDiyMsgIds.UPDATE_RESTORE, 0,
											successBean, null);
								}
							});
						}
					}.start();

				}
			}
		});

		// 开始扫描
		TAApplication.getApplication().doCommand(
				TAApplication.getApplication().getString(
						R.string.freezeappcontroller),
				new TARequest(FreezeAppController.SCAN, null),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						mLoadingFrame.setVisibility(View.GONE);
						Object obj = response.getData();
						try {
							List<AppUninstallBean> list = (List<AppUninstallBean>) obj;
							if (list.size() <= 0) {
								mNoContent.setVisibility(View.VISIBLE);
								mListView.setVisibility(View.GONE);
								mBtnFrame.setVisibility(View.GONE);
							} else {
								mNoContent.setVisibility(View.GONE);
								mListView.setVisibility(View.VISIBLE);
								mBtnFrame.setVisibility(View.VISIBLE);
								mAdapter.update(list);
							}
							mList.clear();
							mList.addAll(list);
						} catch (Exception e) {
							e.printStackTrace();
							mNoContent.setVisibility(View.VISIBLE);
							mListView.setVisibility(View.GONE);
							mBtnFrame.setVisibility(View.GONE);
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
					}
				}, true, false);
	}

	/**
	 * 更新全选/取消全选
	 */
	public void updateSelectText() {
		try {
			if (mList != null) {
				boolean allselect = true;
				for (AppUninstallBean bean : mList) {
					if (!bean.isSelect) {
						allselect = false;
						break;
					}
				}
				TextView v = (TextView) findViewById(R.id.select_all);
				if (!allselect) {
					v.setText("全选");
				} else {
					v.setText("取消全选");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 页面统计
		StatService.trackBeginPage(this, "回收站");
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		if (click != null) {
			// TODO
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, "回收站");
		XGPushManager.onActivityStoped(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
