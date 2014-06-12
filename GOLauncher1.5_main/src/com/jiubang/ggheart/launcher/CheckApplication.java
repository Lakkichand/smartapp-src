package com.jiubang.ggheart.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.download.ServiceCallbackDownload;
import com.jiubang.ggheart.components.DeskAlertDialog;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 */
public class CheckApplication {

	// 下面是进行下载时，不同的进入类型区分
	public static final int FROM_MENU = 0; // 从菜单进入
	public static final int FROM_SCREENEDIT = 1; // 从添加模块进入
	public static final int FROM_GO_FOLDER = 2; // 从桌面GO文件夹的应用进入
	public static final int FROM_SCREEN_FAVORITE_WIDGET = 3; // 从推荐widget进入
	public static final int FROM_MEDIA_DOWNLOAD_DIGLOG = 4; // 从下载资源管理插件提示弹框方式进入
	public static final int FROM_TOUCHHELPER_RECOMMAND = 5; // 从全屏插件推荐进入
	public static final int FROM_RECOMMEND_APP = 6; // 从推荐应用进入（包括15屏推荐）

	private static boolean smDeskAlertDlgCanShow = true;

	public static void showTip(final Context context, final String title, final String content,
			final String[] linkArray, final boolean isCnUser) {
		if (smDeskAlertDlgCanShow) {
			String positiveBtnText = context.getString(R.string.ok);
			String negativeBtnText = context.getString(R.string.cancle);

			final DeskAlertDialog deskAlertDialog = new DeskAlertDialog(context);
			deskAlertDialog.setTitle(title);
			deskAlertDialog.setMessage(content);
			deskAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveBtnText,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (null == linkArray) {
								return;
							}
							if (Statistics.is200ChannelUid(context)) {
								// 如果有電子市場
								if (AppUtils.isMarketExist(deskAlertDialog.getContext())) {
									AppUtils.gotoMarket(deskAlertDialog.getContext(), linkArray[0]);
								} else {
									AppUtils.gotoBrowser(deskAlertDialog.getContext(), linkArray[1]);
								}
							} else {
								AppsDetail.gotoDetailDirectly(context, 
										AppsDetail.START_TYPE_APPRECOMMENDED, 
										LauncherEnv.Plugin.NOTIFICATION_PACKAGE_NAME);
//								GoStoreOperatorUtil.gotoStoreDetailDirectly(context,
//										LauncherEnv.Plugin.NOTIFICATION_PACKAGE_NAME);
							}
						}
					});

			deskAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeBtnText,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// if (isCnUser)
							// {
							// AppUtils.gotoBrowser(deskAlertDialog.getContext(),
							// linkArray[1]);
							// }
						}
					});
			deskAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					smDeskAlertDlgCanShow = true;
					deskAlertDialog.selfDestruct();
				}
			});

			smDeskAlertDlgCanShow = false;
			deskAlertDialog.show();
		}
	}

	public static void showDownloadDirectlyTip(final Context context, final String title,
			final String content, final String[] linkArray, final boolean isCnUser,
			final long taskId) {
		showDownloadDirectlyTipWithGoogleReferalLink(context, title, content, linkArray, isCnUser,
				taskId, LauncherEnv.GOLAUNCHER_FORWIDGET_GOOGLE_REFERRAL_LINK);
	}

	public static void showDownloadDirectlyTipWithGoogleReferalLink(final Context context,
			final String title, final String content, final String[] linkArray,
			final boolean isCnUser, final long taskId, final String googleLink) {
		// 对google链接的特殊处理，若为null，则修改为空串，以便连接。
		final String emptyString = "";
		final String googleReferalLink = googleLink != null ? googleLink : emptyString;

		if (smDeskAlertDlgCanShow) {
			// 左边按钮文字
			String positiveBtnText = context.getString(R.string.ok);
			// 右边按钮文字
			String negativeBtnText = context.getString(R.string.cancle);

			final DeskAlertDialog deskAlertDialog = new DeskAlertDialog(context);
			String dlgTitle = context.getString(R.string.fav_title_first);
			deskAlertDialog.setTitle(dlgTitle + " " + title);
			deskAlertDialog.setMessage(content);
			deskAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveBtnText,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final Context context = deskAlertDialog.getContext();
							final String pkgName = linkArray[0];
							final String downUrl = linkArray[1];
							StatisticsData.updateAppClickData(context, pkgName);
							if (Statistics.is200ChannelUid(context)) {
								// 首先区分渠道号，200的渠道号，则全部跳转至Market，若有market，则跳market，若没有，则跳浏览器版market
								if (AppUtils.isMarketExist(context)) {
									AppUtils.gotoMarket(context, LauncherEnv.Market.APP_DETAIL
											+ pkgName + googleReferalLink);
								} else {
									AppUtils.gotoBrowser(context,
											LauncherEnv.Market.BROWSER_APP_DETAIL + pkgName
													+ googleReferalLink);
								}

							} else {
								// 非200的渠道号，则按国家区域规则进行划分
								if (isCnUser) {
									if (downUrl == null || downUrl.equals("")) {
										Toast.makeText(context, "Url error", Toast.LENGTH_SHORT)
												.show();
									}
									// 国内直接ftp下载
									ServiceCallbackDownload.ServiceCallbackRunnable runnable = new ServiceCallbackDownload.ServiceCallbackRunnable() {
										@Override
										public void run() {
											// TODO Auto-generated method stub
											downloadFileDirectly(context, title, downUrl, taskId,
													pkgName, null, 0, null);
										}
									};
									ServiceCallbackDownload.callbackDownload(context, runnable);
								} else {
									// 国外直接跳转至电子市场，没有电子市场，则使用浏览器下载
									// 如果有電子市場
									if (AppUtils.isMarketExist(context)) {
										AppUtils.gotoMarket(context, LauncherEnv.Market.APP_DETAIL
												+ pkgName + googleReferalLink);
									} else {
										if (downUrl == null || downUrl.equals("")) {
											Toast.makeText(context, "Url error", Toast.LENGTH_SHORT)
													.show();
										}
										AppUtils.gotoBrowser(context, downUrl);
									}
								}
							}

						}
					});

			deskAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeBtnText,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							/*
							 * if (isCnUser) { AppUtils.gotoBrowser(context,
							 * linkArray[1]); }
							 */
						}
					});
			deskAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					smDeskAlertDlgCanShow = true;
					deskAlertDialog.selfDestruct();
				}
			});

			smDeskAlertDlgCanShow = false;
			deskAlertDialog.show();
		}
	}

	/**
	 * 
	 * @param activity
	 * @param title
	 * @param content
	 * @param positiveButtonText
	 * @param negativeButtonText
	 * @param searchKey
	 * @param htmlAddress
	 * @param failTip
	 */
	public static void showDowlaodsDialog(final Activity activity, final String title,
			final String content, final String positiveButtonText, final String negativeButtonText,
			final String searchKey, final String htmlAddress, final String failTip) {
		final DeskAlertDialog mdialog = new DeskAlertDialog(activity);
		mdialog.setTitle(title);
		mdialog.setMessage(content);
		mdialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveButtonText,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String uriString = LauncherEnv.Market.BY_KEYWORD + searchKey;
						if (!AppUtils.gotoMarket(activity, uriString)) {
							AppUtils.gotoBrowser(activity, htmlAddress);
						}
					}
				});

		mdialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeButtonText,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AppUtils.gotoBrowser(activity, htmlAddress);
					}
				});
		mdialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mdialog.selfDestruct();
			}
		});
		mdialog.show();
	}

	/**
	 * 根据渠道号,200的渠道号，则全部跳转至Market，若有market，则跳market，若没有，则跳浏览器版market，带上google分析
	 * 非200的渠道号，则按传进来的类型进行区分，直接FTP下载或跳转GOStore
	 * 
	 * @param context
	 * @param content
	 * @param linkArray
	 * @param googleLink
	 * @param title
	 * @param taskId
	 * @param isCnUser
	 * @param where
	 *            在哪里进行点击下载
	 */
	public static void downloadAppFromMarketFTPGostore(final Context context, final String content,
			final String[] linkArray, final String googleLink, final String title,
			final long taskId, final boolean isCnUser, final int where) {
		final String emptyString = "";
		final String googleReferalLink = googleLink != null ? googleLink : emptyString;
		final String pkgName = linkArray[0];
		final String downUrl = linkArray[1];
		// 如果是推荐应用则不进行统计，因为已在外部进行了实时统计，避免重复
		if (where != FROM_RECOMMEND_APP) {
			StatisticsData.updateAppClickData(context, pkgName);
		}
		if (Statistics.is200ChannelUid(context)) {
			// 首先区分渠道号，200的渠道号，则全部跳转至Market，若有market，则跳market，若没有，则跳浏览器版market
			if (AppUtils.isMarketExist(context)) {
				AppUtils.gotoMarket(context, LauncherEnv.Market.APP_DETAIL + pkgName
						+ googleReferalLink);
			} else {
				AppUtils.gotoBrowser(context, LauncherEnv.Market.BROWSER_APP_DETAIL + pkgName
						+ googleReferalLink);
			}

		} else {
			// 非200的渠道号，则按国家区域规则进行划分
			switch (where) {
				case FROM_MENU :
				case FROM_SCREENEDIT :
					AppsDetail.gotoDetailDirectly(context, 
							AppsDetail.START_TYPE_APPRECOMMENDED, pkgName);
//					GoStoreOperatorUtil.gotoStoreDetailDirectly(context, pkgName);
					break;

				case FROM_GO_FOLDER :
				case FROM_SCREEN_FAVORITE_WIDGET : 
				case FROM_RECOMMEND_APP : 
				{
					// 左边按钮文字
					String positiveBtnText = context.getString(R.string.ok);
					// 右边按钮文字
					String negativeBtnText = context.getString(R.string.cancle);

					final DeskAlertDialog deskAlertDialog = new DeskAlertDialog(context);
					String dlgTitle = context.getString(R.string.fav_title_first);
					deskAlertDialog.setTitle(dlgTitle + " " + title);
					deskAlertDialog.setMessage(content);
					deskAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, positiveBtnText,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (downUrl == null || downUrl.equals("")) {
										Toast.makeText(context, "Url error", Toast.LENGTH_SHORT)
												.show();
									}
									// 国内直接ftp下载
									ServiceCallbackDownload.ServiceCallbackRunnable runnable = new ServiceCallbackDownload.ServiceCallbackRunnable() {
										@Override
										public void run() {
											// TODO Auto-generated method stub
											downloadFileDirectly(context, title, downUrl, taskId,
													pkgName, null, 0, null);
										}
									};
									ServiceCallbackDownload.callbackDownload(context, runnable);
								}
							});

					deskAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, negativeBtnText,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
					deskAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							smDeskAlertDlgCanShow = true;
							deskAlertDialog.selfDestruct();
						}
					});

					smDeskAlertDlgCanShow = false;
					deskAlertDialog.show();
				}
					break;
				case FROM_MEDIA_DOWNLOAD_DIGLOG : {
					if (downUrl == null || downUrl.equals("")) {
						Toast.makeText(context, "Url error", Toast.LENGTH_SHORT)
								.show();
					}
					// 国内直接ftp下载
					ServiceCallbackDownload.ServiceCallbackRunnable runnable = new ServiceCallbackDownload.ServiceCallbackRunnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							downloadFileDirectly(context, title, downUrl, taskId,
									pkgName, null, 0, null);
						}
					};
					ServiceCallbackDownload.callbackDownload(context, runnable);
				}
					break;
				case FROM_TOUCHHELPER_RECOMMAND :
					ServiceCallbackDownload.ServiceCallbackRunnable runnable = new ServiceCallbackDownload.ServiceCallbackRunnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							downloadFileDirectly(context, title, downUrl, taskId, pkgName, null, 0,
									null);
						}
					};
					ServiceCallbackDownload.callbackDownload(context, runnable);
					break;
				default :
					break;
			}
		}
	}

	/**
	 * 功能简述:根据渠道下载应用
	 * 功能详细描述:200渠道，有电子市场则跳转电子市场，没有则跳转web版电子市场；非200渠道，跳转至GO精品详情页
	 * @param context
	 * @param pkgName 需要下载的应用的包名
	 * @param googleLink 是否有谷歌分析链接，如果没有，写null
	 */
	public static void downloadAppFromMarketGostoreDetail(final Context context,
			final String pkgName, final String googleLink) {
		final String emptyString = "";
		final String googleReferalLink = googleLink != null ? googleLink : emptyString;
		if (Statistics.is200ChannelUid(context)) {
			// 首先区分渠道号，200的渠道号，则全部跳转至Market，若有market，则跳market，若没有，则跳浏览器版market
			if (AppUtils.isMarketExist(context)) {
				AppUtils.gotoMarket(context, LauncherEnv.Market.APP_DETAIL + pkgName
						+ googleReferalLink);
			} else {
				AppUtils.gotoBrowser(context, LauncherEnv.Market.BROWSER_APP_DETAIL + pkgName
						+ googleReferalLink);
			}
		} else {
			// 否则调转至GO精品详情
			AppsDetail.gotoDetailDirectly(context, AppsDetail.START_TYPE_APPRECOMMENDED, pkgName);
		}
	}
}
