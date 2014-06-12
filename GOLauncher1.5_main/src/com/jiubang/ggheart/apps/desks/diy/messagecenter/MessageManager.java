package com.jiubang.ggheart.apps.desks.diy.messagecenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.RemoteViews;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.ConvertUtils;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.go.util.log.LogConstants;
import com.go.util.log.LogUnit;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.utils.ApkInstallUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.INotificationId;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverFrame;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageBaseBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean.MessageHeadBean;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageWidgetBean;
import com.jiubang.ggheart.apps.desks.diy.themescan.BannerDetailActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeConstants;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeDetailActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageView;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreOperatorUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.billing.base.Consts;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.BroadCaster;
import com.jiubang.ggheart.data.info.FunTaskItemInfo;
import com.jiubang.ggheart.data.info.MessageInfo;
import com.jiubang.ggheart.data.model.MessageCenterDataModel;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.GuiThemeStatistics;
import com.jiubang.ggheart.data.tables.MessageCenterTable;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * 类描述: 消息中心管理Manaager
 * 功能详细描述:  
 * @date  [2012-9-28]
 */
public class MessageManager extends BroadCaster implements PraseListener, MessageDownLoadObserver {

	private static MessageManager selfInstance;
	private MessageHttp mMsgHttp;
	private MessageListBean mMsgListBean; // 所有消息列表
	private int mUnReadedCnt = 0;
	private MessageCenterDataModel mDataModel;
	private String mMsgId = null; //消息内容id
	private int mEntrance = -1;

	private static final String MARKET_PRE = "market://";
	private static final String MARKET_URL = "https://play.google.com/store/apps/details?id=";

	private static final int MAX_MSG_COUNT = 20;

	private Context mContext;
	/**
	 * 文件状态， 0 代表没有下载文件 ，1 代表已经下载但是没有安装该文件 ，2 代表已经安装了该文件
	 */
	public static final int FILE_NOT_EXIST = 0;
	public static final int FILE_NOT_INSTALL = 1;
	public static final int FILE_INSTALLED = 2;

	public static final int MSG_ZIP_DOWNLOAD_FINISHED = 0;
	public static final int MSG_ZIP_DOWNLOAD_FAILED = 1;
	private final static int MSG_REFRASH_ICONVIEW = 0X2;
	public static final int MSG_REUPDATE_STATISTICSDATA = 3;
	public static final int MSG_REUPDATE_THEME_STATISTICSDATA = 4;
	public static final int MSG_REUPDATE_ERRORSTATISTICSDATA = 5;

	private ArrayList<Bitmap> mEnterBmpList; // 进场动画图片
	private ArrayList<Bitmap> mActingBmpList; // 退场动画图片

	private final static int MSG_PREPARE_ICONVIEW = 0X34;

	private MessageHeadBean mMsgHeadBean; //主题推送的特殊头消息

	private MessageManager(Context context) {
		mDataModel = new MessageCenterDataModel(context);
		init();
		mMsgHttp = new MessageHttp(context);
		mMsgHttp.setPraseListener(this);
		mContext = context;
		if (ConstValue.DEBUG) {
			initProperties();
		}
	}

	public static synchronized MessageManager getMessageManager(Context context) {
		if (selfInstance == null) {
			selfInstance = new MessageManager(context);
		}
		return selfInstance;
	}

	private static void releaseSelf() {
		selfInstance = null;
	}
	/**
	 * 功能简述: 申请消息列表网络请求
	 */
	public void postUpdateRequest(final int auto) {
		if (!Machine.isNetworkOK(GOLauncherApp.getContext())) {
			// mListener.updateFinish(false);
			broadCast(MessageCenterActivity.GET_MSG_NO_NETWORK, -1, null, null);
		}
		new Thread() {

			@Override
			public void run() {
				super.run();
				mMsgHttp.postUpdateRequest(auto);
			}

		}.start();
	}
	
	/**
	 * 功能简述: 获取桌面后台链接
	 * @param id
	 */
	public void postGetUrlRequest() {
		if (!Machine.isNetworkOK(GOLauncherApp.getContext())) {
			broadCast(MessageCenterActivity.GET_MSG_NO_NETWORK, ConvertUtils.boolean2int(false),
					null, null);
		}

		new Thread() {

			@Override
			public void run() {
				super.run();
				mMsgHttp.postGetUrlRequest();
			}

		}.start();
	}
	
	/**
	 * 功能简述: 申请消息内容网络请求
	 * @param id
	 */
	public void postGetMsgRequest(final String id) {
		if (!Machine.isNetworkOK(GOLauncherApp.getContext())) {
			broadCast(MessageCenterActivity.GET_MSG_NO_NETWORK, ConvertUtils.boolean2int(false),
					null, null);
		}

		new Thread() {

			@Override
			public void run() {
				super.run();
				setCurrentMsgId(id);
				mMsgHttp.postGetMsgContentRequest(id);
			}

		}.start();
	}

	public void recycle() {
		mMsgId = null;
		if (mMsgListBean != null) {
			mMsgListBean.clearMsgs();
		}
		mMsgListBean = null;
		mMsgHttp = null;

		releaseSelf();
		clearAllObserver();
	}
	
	public Vector<MessageHeadBean> getMessageList() {
		if (mMsgListBean == null) {
			return null;
		}
		return mMsgListBean.getAllMessagHead();
	}

	public MessageHeadBean getMessageHeadBean(String id) {
		if (mMsgListBean == null) {
			return null;
		}
		return mMsgListBean.getMessageHead(id);
	}

	public void markAllReaded() {
		if (mMsgListBean == null) {
			return;
		}
		removeCoverFrameView();
		for (int i = 0; i < mMsgListBean.getAllMessagHead().size(); i++) {
			MessageHeadBean head = mMsgListBean.getAllMessagHead().get(i);
			if (!head.misReaded) {
				markAsReaded(head);
			}
		}
	}

	public void clearMsg() {
		if (mMsgListBean != null) {
			mMsgListBean.clearMsgs();
		}
		mDataModel.deleteAllMessages();
		mMsgId = null;
		mUnReadedCnt = 0;
	}

	public void setCurrentMsgId(String msgId) {
		mMsgId = msgId;
	}

	public String getMsgId() {
		return mMsgId;
	}

	public boolean isReaded(String id) {
		return mMsgListBean.getMsgReaded(id);
	}

	public int getViewType(String id) {
		return mMsgListBean.getMsgViewType(id);
	}

	/**
	 * 
	 * 消息列表解析完毕的回调方法执行
	 */
	@Override
	public void listParseFinish(boolean bool, MessageListBean msgs) {
		if (msgs != null) {
			if (mMsgListBean == null) {
				mMsgListBean = new MessageListBean();
				
			}
			
			if (!msgs.getAllMessagHead().isEmpty()) {
				for (int i = 0; i < msgs.getAllMessagHead().size(); i++) {
					savePushStatisticsData(msgs.getAllMessagHead().get(i).mId);
				}
				updateStatisticsData(msgs.getAllMessagHead(), MessageBaseBean.VIEWTYPE_NORMAL, 0,
						IPreferencesIds.SHAREDPREFERENCES_MSG_PUSH_TIMES, null);
				
				saveMsgList(msgs);
				addSaveMessagesToList();
				HttpUtil.sortList(mMsgListBean.getAllMessagHead());
				deleteFilterPkgsInstallMsg();
				filterMsgByWhitelist();
				deleteOutDateMsg();
				countUnreadCnt();
			}
			
		}
		if (msgs != null && !msgs.getAllMessagHead().isEmpty()) { //收到的消息msg数量不为0，需刷新
			broadCast(MessageCenterActivity.GET_MSG_LIST_FINISH, ConvertUtils.boolean2int(bool), 1,
					null);
		} else { //收到的消息数量为0，不需要刷新
			broadCast(MessageCenterActivity.GET_MSG_LIST_FINISH, ConvertUtils.boolean2int(bool), 0,
					null);
		}
	}

	/**
	 * 功能简述: 删除一些过时的Msg 
	 * 功能详细描述:	最多显示和保留20跳消息数据
	 * 注意:
	 */
	private void deleteOutDateMsg() {
		Vector<MessageHeadBean> heads = mMsgListBean.getAllMessagHead();
		if (heads.size() > MAX_MSG_COUNT) {
			int cnt = heads.size() - MAX_MSG_COUNT;
			for (int i = 0; i < cnt; i++) {
				MessageHeadBean head = heads.get(heads.size() - i - 1);
				mDataModel.deleteMessage(head.mId);
				heads.remove(head);
			}
		}
	}

	/**
	 * 功能描述：黑名单过滤掉已安装包名信息列表中的消息
	 */
	private void deleteFilterPkgsInstallMsg() {
		Vector<MessageHeadBean> heads = mMsgListBean.getAllMessagHead();
		Vector<MessageHeadBean> installPkgs = new Vector<MessageHeadBean>();
		if (heads != null && heads.size() > 0) {
			for (int i = 0; i < heads.size(); i++) {
				MessageHeadBean head = heads.get(i);
				String filterpkgsString = head.mFilterPkgs;
				if (filterpkgsString != null) {
					String[] filterpkgs = filterpkgsString.split(",");

					for (int j = 0; j < filterpkgs.length; j++) {
						String pkg = filterpkgs[j];
						//判断过滤的包名程序已安装
						if (AppUtils.isAppExist(mContext, pkg)) {
							installPkgs.add(head);
							mDataModel.deleteMessage(head.mId);
							heads.remove(head);
							i--;
							break;
						}

					}
				}
			}
			//上传错误统计日志
			if (installPkgs.size() > 0) {
				updateErrorStatisticsData(installPkgs, 1, 1, 0);
			}

		}
	}
	
	/**
	 * 根据白名单信息过滤消息（未安装白名单内指定的包名或者安装版本不在规定内需过滤）
	 */
	private void filterMsgByWhitelist() {
		Vector<MessageHeadBean> heads = mMsgListBean.getAllMessagHead();
		Vector<MessageHeadBean> showMsgHeads = new Vector<MessageHeadBean>();
		
		for (int i = 0; i < heads.size(); i++) {
			MessageHeadBean head = heads.get(i);
			String whitelist = head.mWhiteList;
			if (Consts.DEBUG) {
				Log.i(Consts.TAG, "filterMsgByWhitelist id =" + head.mId + ",whitelist = " + whitelist);
			}
			if (whitelist != null && !whitelist.equals("")) {
				String[] pkgInfos = whitelist.split(",");
				for (int j = 0; j < pkgInfos.length; j++) { //格式为包名#minversioncode|maxversioncode
					String pkgInfo = pkgInfos[j];
					int index = pkgInfo.indexOf("#");
					int index1 = pkgInfo.indexOf("|");
					String pkgName = ""; //包名
					String minVersionCode = ""; //最小versionCode
					String maxVersioncode = ""; //最大versionCode
					//获取包名等信息
					if (index < 0) {
						pkgName = pkgInfo;
					} else {
						pkgName = pkgInfo.substring(0, index);
						if (index1 < 0) {
							minVersionCode = pkgInfo.substring(index + 1);
						} else {
							minVersionCode = pkgInfo.substring(index + 1, index1); 
							maxVersioncode = pkgInfo.substring(index1 + 1);
						}
					}
					if (Consts.DEBUG) {
						Log.d(Consts.TAG, "名单信息：" + pkgInfo + ",包名：" + pkgName + ",min = " + minVersionCode + ",max = " + maxVersioncode);
					}
					if (AppUtils.isAppExist(mContext, pkgName)) {
						int min = -1;
						int max = -1;
						int versionCode = -1;
						try {
							if (!minVersionCode.equals("")) {
								min = Integer.parseInt(minVersionCode);
							}
							
						} catch (Exception e) {
							
						}
						try {
							if (!maxVersioncode.equals("")) {
								max = Integer.parseInt(maxVersioncode);
							}
							
						} catch (Exception e) {
							
						}
						try {
							versionCode = mContext.getPackageManager().getPackageInfo(pkgName, 0).versionCode;
						} catch (NameNotFoundException e) {
							
						}
						
						if (Consts.DEBUG) {
							Log.i(Consts.TAG, "包的versioncode :" + versionCode + ",min = " + min + ",max = " + max);
						}
						if (canShowMsg(versionCode, min, max)) {
							if (Consts.DEBUG) {
								Log.d(Consts.TAG, "安装了应用。。。。可显示消息");
							}
							showMsgHeads.add(head);
							break;
						}
					} 
				}
			} else { //未填写白名单表示都显示
				showMsgHeads.add(head);
			}
		}
		
		Vector<MessageHeadBean> deleteHeads = new Vector<MessageHeadBean>();
		//找到所有可显示的信息，清除不可显示的信息
		for (int i = 0; i < heads.size(); i++) {
			MessageHeadBean head = heads.get(i);
			if (!showMsgHeads.contains(head)) {
				deleteHeads.add(head);
				mDataModel.deleteMessage(head.mId);
				heads.remove(head);
				i--;
			}
		}
		
		//上传统计
		if (deleteHeads.size() > 0) {
			updateErrorStatisticsData(deleteHeads, 1, 2, 0);
		}
	}

	/**
	 * 判断版本号是否在区间内
	 * @param versionCode
	 * @param min
	 * @param max
	 * @return
	 */
	private boolean canShowMsg(int versionCode, int min, int max) {
		boolean canShow = false;
		if (min != -1) {
			if (max == -1) {
				if (versionCode >= min) {
					canShow = true;
				}
			} else {
				if (versionCode >= min && versionCode < max) {
					canShow = true;
				}
			}
		} else {
			if (max == -1) {
				canShow = true;
			} else {
				if (versionCode < max) {
					canShow = true;
				}
			}
		}
		return canShow;
	}	
	
	/**
	 * 消息内容解析完毕回调方法的执行
	 */
	//	@Override
	//	public void msgParseFinish(boolean bool, String msgUrl) {
	////		if (bool && msg != null) {
	////			// if(mMsgContent != null)
	////			// {
	////			// mMsgContent.recycle();
	////			// }
	////			mMsgContent = msg;
	////		}
	////		// if(mListener != null)
	////		{
	//			// mListener.getMsgFinish(bool);
	//			broadCast(MessageCenterActivity.GET_MSG_CONTENT_FINISH, ConvertUtils.boolean2int(bool),
	//					msgUrl, null);
	////		}
	//	}

	public void markAsReaded(String id) {
		if (mMsgListBean != null) {
			MessageHeadBean head = mMsgListBean.getMessageHead(id);
			markAsReaded(head);
		}
	}

	public void markAsReaded(MessageHeadBean msg) {
		if (msg == null || msg.misReaded) {
			return;
		}
		MessageInfo info = convertBean2Info(msg);
		info.misReaded = true;
		mDataModel.updateRecord(info);
		if (mUnReadedCnt > 0) {
			mUnReadedCnt--;
		}
		mMsgListBean.setReaded(msg.mId, true);
	}

	public void markAsClickClosed(MessageHeadBean msg) {
		if (msg == null || msg.mClickClosed) {
			return;
		}
		MessageInfo info = convertBean2Info(msg);
		info.mClickClosed = true;
		mDataModel.updateRecord(info);
		mMsgListBean.setClickClosed(msg.mId, true);
	}
	
	public void markAsRemoved(MessageHeadBean msg) {
		if (msg == null || msg.mIsRemoved) {
			return;
		}
		MessageInfo info = convertBean2Info(msg);
		info.mIsRemoved = true;
		mDataModel.updateRecord(info);
		mMsgListBean.setClickClosed(msg.mId, true);
	}

	private MessageInfo convertBean2Info(MessageHeadBean bean) {
		MessageInfo info = new MessageInfo();
		info.mId = bean.mId;
		info.misReaded = isReaded(bean.mId);
		info.mTimeStamp = bean.mMsgTimeStamp;
		info.mType = bean.mType;
		info.mTitle = bean.mTitle;
		info.mViewType = bean.mViewType;
		info.mUrl = bean.mUrl;
		info.mActtype = bean.mActType;
		info.mActValue = bean.mActValue;
		info.mEndTime = bean.mEndTime;
		info.mIconUrl = bean.mIcon;
		info.mIntro = bean.mSummery;
		info.mStartTime = bean.mStartTime;
		info.mZIcon1 = bean.mZicon1;
		info.mZIcon2 = bean.mZicon2;
		info.mZpos = bean.mZpos;
		info.mZtime = bean.mZtime;
		info.mIsClosed = bean.mIsColsed;
		info.mFilterPkgs = bean.mFilterPkgs;
		info.mClickClosed = bean.mClickClosed;
		info.mDynamic = bean.mDynamic;
		info.mIconpos = bean.mIconpos;
		info.mFullScreenIcon = bean.mFullScreenIcon;
		info.mIsRemoved = bean.mIsRemoved;
		info.mWhiteList = bean.mWhiteList;
		info.mIsNew = bean.mIsNew;
		return info;
	}
	/**
	 * 功能简述:  消息列表的初始化
	 * 功能详细描述: 从数据库中读取内容，转化成MessageListBean
	 * 注意:
	 */
	private void init() {
		Cursor cursor = mDataModel.queryMessages();
		if (cursor != null) {
			cursor.moveToPosition(-1);
			mMsgListBean = new MessageListBean();
			Vector<MessageHeadBean> msgs = mMsgListBean.getAllMessagHead();
			while (cursor.moveToNext()) {
				int readIndex = cursor.getColumnIndex(MessageCenterTable.READED);
				if (readIndex != -1) {
					boolean read = ConvertUtils.int2boolean(cursor.getInt(readIndex));
					if (!read) {
						mUnReadedCnt++;
					}

					MessageInfo info = new MessageInfo();
					info.parseFromCursor(cursor);
					MessageHeadBean bean = convertInfo2Bean(info);
					msgs.add(bean);
				}
			}
			cursor.close();
			cursor = null;
		}

	}

	private void countUnreadCnt() {
		Vector<MessageHeadBean> msgs = mMsgListBean.getAllMessagHead();
		if (msgs == null /* || mReadedList == null */) {
			return;
		}
		mUnReadedCnt = 0;
		for (int i = 0; i < msgs.size(); i++) {
			MessageHeadBean msg = msgs.get(i);
			if (!msg.misReaded) {
				mUnReadedCnt++;
			}
		}
	}

	public int getUnreadedCnt() {
		if (ConstValue.DEBUG) {
			Log.d(ConstValue.MSG_TAG, "count:" + mUnReadedCnt);
		}
		if (!Machine.isNetworkOK(GOLauncherApp.getContext())) {
			return 0;
		}
		return mUnReadedCnt;
	}

	public synchronized void showMessage(MessageHeadBean bean) {
		if (bean == null) {
			return;
		}
		markAsReaded(bean);
		setCurrentMsgId(bean.mId);
		Intent intent = new Intent(GOLauncherApp.getContext(), MessageContentActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("msgid", bean.mId);
		bundle.putString("msgurl", bean.mUrl);
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GOLauncherApp.getContext().startActivity(intent);
	}

	public void showDialogMessage(String id) {
		if (id == null) {
			return;
		}
		MessageHeadBean bean = getMessageHeadBean(id);
		//上传展示数据
		saveShowStatisticsData(bean.mId);
		Vector<MessageHeadBean> statisticsMsgs = new Vector<MessageHeadBean>();
		statisticsMsgs.add(bean);
		updateStatisticsData(statisticsMsgs, MessageBaseBean.VIEWTYPE_DIALOG, 0,
				IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES, null);
		setCurrentMsgId(bean.mId);
		markAsReaded(bean);
		Intent intent = new Intent(GOLauncherApp.getContext(), MessageDialogContentActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("msgid", bean.mId);
		bundle.putString("msgurl", bean.mUrl);
		intent.putExtras(bundle);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			GOLauncherApp.getContext().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 功能简述:  消息中心通知栏的展示的准备
	 * @param bean
	 */
	public void sendNotify(MessageHeadBean bean) {
		if (bean == null) {
			return;
		}
		mMsgHeadBean = bean;
		mMsgHeadBean.initIconView(this);
		mHandler.sendEmptyMessage(MSG_PREPARE_ICONVIEW);
	}

	/**
	 * 功能简述:  消息中心通知栏的展示
	 * @param bean
	 */
	public void showMsgNotify(MessageHeadBean bean) {
		if (bean == null) {
			return;
		}
		Intent intent = null;

		Bundle bundle = new Bundle();
		if (bean.mType == MessageBaseBean.TYPE_HTML) {
			intent = new Intent(GOLauncherApp.getContext(), MessageContentActivity.class);
			bundle.putString("msgid", bean.mId);
			bundle.putString("msgurl", bean.mUrl);
			bundle.putInt("where", MessageBaseBean.VIEWTYPE_STATUS_BAR);
		} else {
			intent = new Intent();
			bundle.putString("msgId", bean.mId);
			intent.setClass(GOLauncherApp.getContext(), MsgNotifyActivity.class);
		}

		intent.putExtras(bundle);
		try {
			PendingIntent contentIntent = PendingIntent.getActivity(GOLauncherApp.getContext(), 0,
					intent, PendingIntent.FLAG_CANCEL_CURRENT);
			Notification notification = new Notification(R.drawable.msg_center_notification,
					bean.mTitle, System.currentTimeMillis());
			RemoteViews contentView = new RemoteViews(GOLauncherApp.getContext().getPackageName(),
					R.layout.msg_center_noitify_content);

			if (mMsgHeadBean.mBitmap != null && !mMsgHeadBean.mBitmap.isRecycled()) {
				if (mMsgHeadBean.mFullScreenIcon != null) {
//					Bitmap bmp = bitmpaScale(mMsgHeadBean.mBitmap);
					contentView.setViewVisibility(R.id.theme_view_image, View.GONE);
					contentView.setViewVisibility(R.id.theme_detail_content, View.GONE);
					contentView.setViewVisibility(R.id.theme_full_screen_icon, View.VISIBLE);
					contentView.setImageViewBitmap(R.id.theme_full_screen_icon, mMsgHeadBean.mBitmap);
				} else {
					contentView.setImageViewBitmap(R.id.theme_view_image, mMsgHeadBean.mBitmap);
					contentView.setTextViewText(R.id.theme_title, bean.mTitle);
					contentView.setTextViewText(R.id.theme_content, bean.mSummery);
				}
			} else {
				contentView.setImageViewResource(R.id.theme_view_image,
						R.drawable.msg_center_notification);
				contentView.setTextViewText(R.id.theme_title, bean.mTitle);
				contentView.setTextViewText(R.id.theme_content, bean.mSummery);
			}

			notification.contentIntent = contentIntent;
			notification.contentView = contentView;
			
			Bundle deleteBundle = new Bundle();
			deleteBundle.putString("msgId", bean.mId);
			deleteBundle.putBoolean("remove", true);
			Intent deleteIntent = new Intent();
			deleteIntent.setClass(GOLauncherApp.getContext(), MsgNotifyActivity.class);
			deleteIntent.putExtras(deleteBundle);
			notification.deleteIntent = PendingIntent.getActivity(GOLauncherApp.getContext(), 1,
					deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			// 点击后自动消失
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			NotificationManager nm = (NotificationManager) GOLauncherApp.getContext()
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.notify(INotificationId.MESSAGECENTER_NEW_MESSAGE, notification);

			saveShowStatisticsData(bean.mId);
			Vector<MessageHeadBean> statisticsMsgs = new Vector<MessageHeadBean>();
			statisticsMsgs.add(bean);
			updateStatisticsData(statisticsMsgs, MessageBaseBean.VIEWTYPE_STATUS_BAR, 0,
					IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES, null);
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "start notification error id = "
					+ INotificationId.MESSAGECENTER_NEW_MESSAGE);
		}
	}

	public void abortPost() {
		if (mMsgHttp != null) {
			mMsgHttp.abortPost();
		}
	}

	public ArrayList<MessageInfo> getNeedShowMessageInfo() {
		Cursor cursor = mDataModel.queryNeedShowdMessages();
		ArrayList<MessageInfo> msgInfos = null;
		if (cursor != null) {
			msgInfos = new ArrayList<MessageInfo>();
			cursor.moveToPosition(-1);
			while (cursor.moveToNext()) {
				MessageInfo info = new MessageInfo();
				info.parseFromCursor(cursor);
				msgInfos.add(info);
			}
			cursor.close();
			cursor = null;
		}

		return msgInfos;
	}
	/**
	 * 功能简述: 保存消息列表到数据库中
	 * @param listBean
	 */
	private void saveMsgList(MessageListBean listBean) {
		if (listBean == null || listBean.getAllMessagHead().size() == 0) {
			return;
		}
		Vector<MessageHeadBean> msgs = listBean.getAllMessagHead();
		for (int i = 0; i < msgs.size(); i++) {
			MessageHeadBean bean = msgs.get(i);
			MessageInfo info = convertBean2Info(bean);
			mDataModel.insertRecord(info);
		}
	}
	
	/**
	 * 功能简述: 从数据库中读取消息内容到list中
	 * 
	 */
	private void addSaveMessagesToList() {
		Cursor cursor = mDataModel.queryMessages();
		if (cursor != null) {
			mMsgListBean.clearMsgs();
			Vector<MessageHeadBean> msgs = mMsgListBean.getAllMessagHead();
			cursor.moveToPosition(-1);
			while (cursor.moveToNext()) {
				MessageInfo info = new MessageInfo();
				info.parseFromCursor(cursor);
				if (info.mId == null) {
					continue;
				}
				MessageHeadBean bean = convertInfo2Bean(info);
				if (mMsgListBean.getMessageHead(bean.mId) != null) {
					mMsgListBean.getMessageHead(bean.mId).misReaded = info.misReaded;
				} else {
					msgs.add(bean);
				}
			}
			cursor.close();
		}
		cursor = null;
	}
	/**
	 * 功能简述:  处理消息内容中的控件的点击事件
	 * 功能详细描述: 根据MessageWidgetBean中的动作类型，以及当前所处的activity环境
	 * 注意:
	 * @param bean
	 * @param activity
	 */
	public void handleWidgetClick(MessageWidgetBean bean, Activity activity) {
		if (ConstValue.DEBUG) {
			LogUnit.diyInfo(ConstValue.MSG_TAG, "handleWidgetClick，actiontype = " + bean.mActtype
					+ ", actionvalue = " + bean.mActvaule);
		}
		if (bean.mActtype == MessageWidgetBean.ACTTYPE_NON) {
			activity.finish();
		} else if (bean.mActtype == MessageWidgetBean.ACTTYPE_CANCLE) {
			activity.finish();
		} else if (bean.mActtype == MessageWidgetBean.ACTTYPE_LINK) {
			//使用浏览器去url
			//	AppUtils.gotoBrowser(activity, bean.mActvaule);
			gotoBrowser(bean.mActvaule);
		} else if (bean.mActtype == MessageWidgetBean.ACTTYPE_GOSTORE) {
			if (bean.mActvaule == null) {
				return;
			}
			if (bean.mActvaule.equals(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE)) {
				Intent it = new Intent(bean.mActvaule);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.START_ACTIVITY, -1, it, null);
			} else {
				AppsDetail.gotoDetailDirectly(mContext, 
						AppsDetail.START_TYPE_APPRECOMMENDED, bean.mActvaule);
//				GoStoreOperatorUtil.gotoStoreDetailDirectly(activity, bean.mActvaule);
			}
		} else if (bean.mActtype == MessageWidgetBean.ACTTYPE_OPENGO) {
			if (bean.mActvaule == null) {
				return;
			}

			Intent it = new Intent(bean.mActvaule);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.START_ACTIVITY,
					-1, it, null);
		} else if (bean.mActtype == MessageWidgetBean.ACTTYPE_OTHER) {
			if (bean.mActvaule == null) {
				return;
			}
			if (bean.mActvaule.startsWith(MARKET_PRE)) {
				if (AppUtils.isMarketExist(activity)) {
					AppUtils.gotoMarket(
							activity,
							LauncherEnv.Market.APP_DETAIL
									+ bean.mActvaule.substring(MARKET_PRE.length()));
				} else {
					//					AppUtils.gotoBrowser(activity,
					//							MARKET_URL + bean.mActvaule.substring(MARKET_PRE.length()));
					gotoBrowser(MARKET_URL + bean.mActvaule.substring(MARKET_PRE.length()));
				}
			}
		} else if (bean.mActtype == MessageWidgetBean.ACTTYPE_DOWNLOAD) {
			if (activity instanceof MessageContentActivity) {
				int state = getFileState(bean);
				switch (state) {
					case FILE_NOT_EXIST :
						((MessageContentActivity) activity).startDownLoad(bean.mActvaule);
						break;
					case FILE_NOT_INSTALL :
						String fileName = LauncherEnv.Path.MESSAGECENTER_PATH + mMsgId + ".apk";
						ApkInstallUtils.installApk(fileName);
						break;
					case FILE_INSTALLED :
						//do nothing
						break;
					default :
						break;
				}
			} else if (activity instanceof MessageDialogContentActivity) {
				int state = getFileState(bean);
				switch (state) {
					case FILE_NOT_EXIST :
						((MessageDialogContentActivity) activity).startDownLoad(bean.mActvaule);
						activity.finish();
						break;
					case FILE_NOT_INSTALL :
						String fileName = LauncherEnv.Path.MESSAGECENTER_PATH + mMsgId + ".apk";
						ApkInstallUtils.installApk(fileName);
						activity.finish();
						break;
					case FILE_INSTALLED :
						//do nothing
						activity.finish();
						break;
					default :
						break;
				}
			}
		} else if (bean.mActtype == MessageWidgetBean.ACTTYPE_NEWACTION) {
			action(bean.mActvaule);
			if (bean.mActvaule != null && bean.mActvaule.startsWith(ConstValue.PREFIX_GUI)) {
				GuiThemeStatistics.setCurrentEntry(GuiThemeStatistics.ENTRY_MESSAGE_CENTER,
						mContext);
			}
		}

		//上传点击
		uploadWidgetStatistics(bean.mValue);
	}

	/**
	 * 上传详情界面控件点击数
	 * @param value
	 */
	public void uploadWidgetStatistics(String value) {
		saveWidgetClickStaticsData(mMsgId, value);
		MessageHeadBean bean = getMessageHeadBean(mMsgId);
		Vector<MessageHeadBean> statisticsMsgs = new Vector<MessageHeadBean>();
		statisticsMsgs.add(bean);
		updateStatisticsData(statisticsMsgs, mEntrance, 0,
				IPreferencesIds.SHAREDPREFERENCES_MSG_BUTTON_CLICK_TIMES, value);
	}

	/**
	 * 设置点击入口（消息中心、通知栏、罩子层、对话框）
	 * @param entrance
	 */
	public void setEntrance(int entrance) {
		mEntrance = entrance;
	}

	private void gotoBrowser(String uriString) {
		// 跳转intent
		Uri uri = Uri.parse(uriString);
		Intent myIntent = new Intent(Intent.ACTION_VIEW, uri);

		// 1:已安装的浏览器列表
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> resolveList = pm.queryIntentActivities(myIntent, 0);
		boolean hasRun = false;

		if (resolveList != null && !resolveList.isEmpty()) {
			// 2:获取当前运行程序列表
			ArrayList<FunTaskItemInfo> curRunList = null;
			try {
				curRunList = AppCore.getInstance().getTaskMgrControler().getProgresses();
			} catch (Throwable e) {
			}
			int curRunSize = (curRunList != null) ? curRunList.size() : 0;

			// 两个列表循环遍历
			for (int i = curRunSize - 1; i > 0; i--) {
				FunTaskItemInfo funTaskItemInfo = curRunList.get(i);
				Intent funIntent = funTaskItemInfo.getAppItemInfo().mIntent;
				ComponentName funComponentName = funIntent.getComponent();
				for (ResolveInfo resolveInfo : resolveList) {
					if (resolveInfo.activityInfo.packageName != null
							&& resolveInfo.activityInfo.packageName.equals(funComponentName
									.getPackageName())) {
						// 找到正在运行的浏览器，直接拉起
						if (funIntent.getComponent() != null) {
							String pkgString = funIntent.getComponent().getPackageName();
							if (pkgString != null) {
								if (pkgString.equals("com.android.browser")
										|| pkgString.equals("com.dolphin.browser.cn")
										|| pkgString.equals("com.android.chrome")
										|| pkgString.equals("com.qihoo.browser")) {
									//上述浏览器后台拉起会跳转浏览器首页，而非保存的用户原来页面
									hasRun = true;
									funIntent.setAction(Intent.ACTION_VIEW);
									funIntent.setData(uri);
									mContext.startActivity(funIntent);
								}
							}
						}
					}
				}
			}
			//无正在运行的浏览器，直接取浏览器列表的第1个打开
			if (!hasRun) {
				ResolveInfo resolveInfo = resolveList.get(0);
				String pkgString = resolveInfo.activityInfo.packageName;
				String activityName = resolveInfo.activityInfo.name;
				myIntent.setClassName(pkgString, activityName);
				myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(myIntent);
			}
		}
	}

	/**
	 * 功能简述: 判断当前环境是否在消息中心里面
	 * 功能详细描述:
	 * 注意:
	 * @return
	 */
	public boolean isInMessageCenter() {
		if (getObserver() != null) {
			ArrayList<BroadCasterObserver> list = getObserver();
			for (BroadCasterObserver observer : list) {
				if (observer instanceof MessageCenterActivity
						|| observer instanceof MessageContentActivity) {
					return true;
				}
			}
		}
		return false;
	}

	public int getFileState(MessageWidgetBean bean) {
		int state = FILE_NOT_EXIST;
		if (bean.mActtype == MessageWidgetBean.ACTTYPE_DOWNLOAD) {
			String fileName = LauncherEnv.Path.MESSAGECENTER_PATH + mMsgId + ".apk";

			if (bean.mActvaule != null) {
				String[] urlContent = bean.mActvaule.split(MessageBaseBean.URL_SPLIT);
				if (urlContent.length < 2) {
					return state;
				}
				String[] nameContent = urlContent[1].split(MessageBaseBean.URL_SPLIT_NAME);
				String pkgName = nameContent[0];

				File newfile = new File(fileName);
				if (AppUtils.isAppExist(mContext, pkgName)) {
					if (newfile.exists()) {
						state = FILE_INSTALLED;
					}
				} else {
					if (newfile.exists()) {
						state = FILE_NOT_INSTALL;
					}
				}
			}
		}
		return state;
	}

	private void initProperties() {
		if (FileUtil.isSDCardAvaiable()
				&& !FileUtil.isFileExist(LauncherEnv.Path.MESSAGECENTER_PATH + "properties.txt")) {
			try {
				new Thread() {
					public void run() {
						String uid = GoStorePhoneStateUtil.getUid(mContext);
						String info = uid + "#" + ConstValue.HOSTURL_BASE;
						FileUtil.saveByteToSDFile(info.getBytes(),
								LauncherEnv.Path.MESSAGECENTER_PATH + "properties.txt");
					}
				}.start();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		LogUnit.sLogToFile = "messagecenter";
	}

	/**
	 * <br>功能简述:向CoverFrame中添加view
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param head
	 */
	public void addViewToCoverFrame(final MessageHeadBean head) {

		if (head == null) {
			return;
		}
		removeCoverFrameView();
		FrameLayout container = new FrameLayout(mContext);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

		CoverFrameView view = new CoverFrameView(mContext, head, mEnterBmpList, mActingBmpList);
		view.setMessageHead(head);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				handleMsgClick(head, MessageBaseBean.VIEWTYPE_DESK_TOP);
				//				updateStatisticsData(head, MessageBaseBean.VIEWTYPE_DESK_TOP);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.COVER_FRAME_REMOVE_VIEW, CoverFrame.COVER_VIEW_MESSAGECENTER,
						null, null);
				recyleZicons();
			}
		});
		container.addView(view, params);
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.COVER_FRAME_ADD_VIEW,
				CoverFrame.COVER_VIEW_MESSAGECENTER, container, null);
		saveShowStatisticsData(head.mId);
		Vector<MessageHeadBean> statisticsMsgs = new Vector<MessageHeadBean>();
		statisticsMsgs.add(head);
		updateStatisticsData(statisticsMsgs, MessageBaseBean.VIEWTYPE_DESK_TOP, 0,
				IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES, null);
	}
	/**
	 * <br>功能简述:准备罩子层消息所需要的资源
	 * <br>功能详细描述:下载zip包以及解压
	 * <br>注意:解压后的文件夹名为id+zip1/2
	 * @param head
	 */
	public void prepareZipRes(final MessageHeadBean head) {
		new Thread() {
			@Override
			public void run() {
				String zip1 = "zip1.zip";
				String zip2 = "zip2.zip";
				super.run();
				if (head.mZicon1 != null || head.mZicon2 != null) {
					File file = new File(LauncherEnv.Path.MESSAGECENTER_PATH + head.mId + zip1);
					if (!file.exists()) {
						if (!mMsgHttp.downloadZipRes(head.mZicon1,
								LauncherEnv.Path.MESSAGECENTER_PATH, head.mId + zip1)) {
						}
					}
					try {
						HttpUtil.unZipFolder(LauncherEnv.Path.MESSAGECENTER_PATH + head.mId + zip1,
								LauncherEnv.Path.MESSAGECENTER_PATH + head.mId + "zip1");
					} catch (Exception e) {
						e.printStackTrace();
					}
					file = new File(LauncherEnv.Path.MESSAGECENTER_PATH + head.mId + zip2);
					if (!file.exists()) {
						if (!mMsgHttp.downloadZipRes(head.mZicon2,
								LauncherEnv.Path.MESSAGECENTER_PATH, head.mId + zip2)) {
						}
					}
					try {
						HttpUtil.unZipFolder(LauncherEnv.Path.MESSAGECENTER_PATH + head.mId + zip2,
								LauncherEnv.Path.MESSAGECENTER_PATH + head.mId + "zip2");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				decodeBitmaps(head);
				Message msg = Message.obtain();
				msg.what = MSG_ZIP_DOWNLOAD_FINISHED;
				msg.obj = head;
				mHandler.sendMessage(msg);
			}
		}.start();

	}

	/**
	 * <br>功能简述:用于处理消息列表的点击事件
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bean
	 * @param fromWhere 相应那里的点击事件
	 */
	public void handleMsgClick(MessageHeadBean bean, int fromWhere) {
		if (bean == null) {
			return;
		}
		if (ConstValue.DEBUG) {
			LogUnit.diyInfo(ConstValue.MSG_TAG, "handleMsgClick，actiontype = " + bean.mActType
					+ ", actionvalue = " + bean.mActValue);
		}
		if (!bean.misReaded) {
			markAsReaded(bean.mId);
			if ((bean.mViewType & MessageBaseBean.VIEWTYPE_DESK_TOP) != 0) {
				removeCoverFrameView();
			}
		}
		if (bean.mType == MessageBaseBean.TYPE_HTML) {
			showMessage(bean);
		} else if (bean.mActType == MessageWidgetBean.ACTTYPE_NEWACTION) {
			action(bean.mActValue);
		} else if (bean.mActType == MessageWidgetBean.ACTTYPE_LINK) {
			//使用浏览器去url
			//			AppUtils.gotoBrowser(mContext, bean.mActValue);
			gotoBrowser(bean.mActValue);
		} else if (bean.mActType == MessageWidgetBean.ACTTYPE_GOSTORE) {
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(GoLauncher.getContext(), 
					AppRecommendedStatisticsUtil.ENTRY_TYPE_MESSAGE_CENTER);
			if (bean.mActValue == null) {
				return;
			}
			if (bean.mActValue.equals(ICustomAction.ACTION_FUNC_SPECIAL_APP_GOSTORE)) {
				Intent it = new Intent(bean.mActValue);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.START_ACTIVITY, -1, it, null);
			} else {
				AppsDetail.gotoDetailDirectly(mContext, 
						AppsDetail.START_TYPE_APPRECOMMENDED, bean.mActValue);
//				GoStoreOperatorUtil.gotoStoreDetailDirectly(mContext, bean.mActValue);
			}
		} else if (bean.mActType == MessageWidgetBean.ACTTYPE_OPENGO) {
			if (bean.mActValue == null) {
				return;
			}

			Intent it = new Intent(bean.mActValue);
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.START_ACTIVITY,
					-1, it, null);
		} else if (bean.mActType == MessageWidgetBean.ACTTYPE_OTHER) {
			if (bean.mActValue == null) {
				return;
			}
			if (bean.mActValue.startsWith(MARKET_PRE)) {
				if (AppUtils.isMarketExist(mContext)) {
					AppUtils.gotoMarket(
							mContext,
							LauncherEnv.Market.APP_DETAIL
									+ bean.mActValue.substring(MARKET_PRE.length()));
				} else {
					//					AppUtils.gotoBrowser(mContext,
					//							MARKET_URL + bean.mActValue.substring(MARKET_PRE.length()));
					gotoBrowser(MARKET_URL + bean.mActValue.substring(MARKET_PRE.length()));
				}
			}
		} else if (bean.mActType == MessageWidgetBean.ACTTYPE_DOWNLOAD) {
			if (bean.mActValue != null) {
				String[] urlContent = bean.mActValue.split(MessageBaseBean.URL_SPLIT);
				if (urlContent.length != 2) {
					return;
				}
				String[] nameContent = urlContent[1].split(MessageBaseBean.URL_SPLIT_NAME);
				if (nameContent.length != 2) {
					return;
				}
				String pkgName = nameContent[0];
				String appName = nameContent[1];	          //显示在通知栏上面的下载app name
				GoStoreOperatorUtil.downloadFileDirectly(mContext, appName, bean.mActValue,
						Long.valueOf(bean.mId), pkgName, null, 0, null);
			}

		}

		saveClickStatisticsData(bean.mId);
		Vector<MessageHeadBean> statisticsMsgs = new Vector<MessageHeadBean>();
		statisticsMsgs.add(bean);
		updateStatisticsData(statisticsMsgs, fromWhere, 0,
				IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES, null);
		mEntrance = fromWhere;

		if (bean.mActValue != null && bean.mActValue.startsWith(ConstValue.PREFIX_GUI)
				&& fromWhere == MessageBaseBean.VIEWTYPE_STATUS_BAR) {
			GuiThemeStatistics.setCurrentEntry(GuiThemeStatistics.ENTRY_MESSAGE_PUSH, mContext);
		}
	}

	/**
	 * <br>功能简述:1.3版本后的跳转响应
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param actValue
	 */
	public void action(String actValue) {
		if (actValue == null) {
			return;
		}

		if (actValue.startsWith(ConstValue.PREFIX_GOSTOREDETAIL)) {
			String id = getId(actValue, ConstValue.PREFIX_GOSTOREDETAIL);
//			GoStoreOperatorUtil.gotoStoreDetailDirectly(mContext, null, Integer.valueOf(id));
			AppsDetail.gotoDetailDirectly(mContext, 
					AppsDetail.START_TYPE_APPRECOMMENDED, Integer.valueOf(id));
		} else if (actValue.startsWith(ConstValue.PREFIX_GOSTORETYPE)) {
//			String id = getId(actValue, ConstValue.PREFIX_GOSTORETYPE);
//			Intent intent = new Intent();
//			Bundle bundle = new Bundle();
//			bundle.putString(GoStore.GOSTORE_SORT_ID, id);
//			intent.putExtras(bundle);
//			intent.setClass(mContext, GoStore.class);
//			intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
//			mContext.startActivity(intent);
			AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
					MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
		} else if (actValue.startsWith(ConstValue.PREFIX_GUI)) {
			String id = getId(actValue, ConstValue.PREFIX_GUI);
			if (id != null) {
				Intent mythemesIntent = new Intent();
				if (id.equals("1")) {
					mythemesIntent.putExtra("entrance", ThemeManageView.LAUNCHER_THEME_VIEW_ID);
				} else if (id.equals("2")) {
					mythemesIntent.putExtra("entrance", ThemeManageView.LOCKER_THEME_VIEW_ID);
				}
				mythemesIntent.setClass(mContext, ThemeManageActivity.class);
				mythemesIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(mythemesIntent);
			}
		} else if (actValue.startsWith(ConstValue.PREFIX_GUIDETAIL)) {

			Intent intent = new Intent(mContext, ThemeDetailActivity.class);
			intent.putExtra(ThemeConstants.DETAIL_MODEL_EXTRA_KEY,
					ThemeConstants.DETAIL_MODEL_FEATURED_EXTRA_VALUE);
			intent.putExtra(ThemeConstants.PACKAGE_NAME_EXTRA_KEY, "");
			intent.putExtra(ThemeConstants.DETAIL_ID_EXTRA_KEY,
					Integer.valueOf(getId(actValue, ConstValue.PREFIX_GUIDETAIL)));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(intent);
		} else if (actValue.startsWith(ConstValue.PREFIX_GUISPEC)) {
			String id = getId(actValue, ConstValue.PREFIX_GUISPEC);
			try {
				Intent intent = new Intent();
				intent.putExtra("ty", Integer.valueOf(id));
				intent.putExtra("entrance", true);
				intent.setClass(mContext, BannerDetailActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (actValue.startsWith(ConstValue.PREFIX_HTTP)) {
			gotoBrowser(actValue);
		} else if (actValue.startsWith(ConstValue.PREFIX_MARKET)) {
			String id = getId(actValue, ConstValue.PREFIX_MARKET);
			if (AppUtils.isMarketExist(mContext)) {
				AppUtils.gotoMarket(mContext, LauncherEnv.Market.APP_DETAIL + id);
			} else {
				gotoBrowser(LauncherEnv.Market.BROWSER_APP_DETAIL + id);
			}
		} else if (actValue.startsWith(ConstValue.PREFIX_APPCENTERTYPE)) { //应用中心分类
			String id = getId(actValue, ConstValue.PREFIX_APPCENTERTYPE);
			try {
				int typeId = Integer.parseInt(id);
				AppsManagementActivity.startAppCenter(GoLauncher.getContext(), typeId, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (actValue.startsWith(ConstValue.PREFIX_APPCENTERTOPIC)) { //应用中心专题
			String id = getId(actValue, ConstValue.PREFIX_APPCENTERTOPIC);
			try {
				int topicId = Integer.parseInt(id);
				AppsManagementActivity.startTopic(mContext, topicId, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (actValue.startsWith(ConstValue.PREFIX_APPCENTERDETAIL)) { //应用中心详情
			String id = getId(actValue, ConstValue.PREFIX_APPCENTERDETAIL);
			try {
				int detailId = Integer.parseInt(id);
				AppsDetail.gotoDetailDirectly(mContext, 
						AppsDetail.START_TYPE_APPRECOMMENDED, detailId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * <br>功能简述:获得actvalue中的参数id
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param actValue
	 * @param prex
	 * @return
	 */
	private String getId(String actValue, String prex) {
		String id = null;
		if (actValue != null && prex != null && actValue.length() > prex.length()) {
			id = actValue.substring(prex.length());
		}
		return id;
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_ZIP_DOWNLOAD_FINISHED :
					if (msg.obj != null && msg.obj instanceof MessageHeadBean) {
						addViewToCoverFrame((MessageHeadBean) msg.obj);
					}
					break;
				case MSG_PREPARE_ICONVIEW :
					mMsgHeadBean.downloadDrawable(mMsgHeadBean);
					break;
				case MSG_REFRASH_ICONVIEW :
					if (!isInMessageCenter()) {
						showMsgNotify(mMsgHeadBean);
					}
					break;
				case MSG_REUPDATE_STATISTICSDATA :
					if (msg.obj != null && msg.getData() != null) {
						Bundle data = msg.getData();
						long uuid = data.getLong("uuid");
						String staticticstype = data.getString("staticticstype");
						String clickItemName = data.getString("clickitemname");
						updateStatisticsData((Vector<MessageHeadBean>) msg.obj, msg.arg1, uuid,
								staticticstype, clickItemName);
					}
					break;
				case MSG_REUPDATE_THEME_STATISTICSDATA :
					if (msg.obj != null) {
						updateThemeNotifyStatisticsData(msg.arg1, (Long) msg.obj,
								ConvertUtils.int2boolean(msg.arg2));
					}
					break;
				case MSG_REUPDATE_ERRORSTATISTICSDATA :
					if (msg.obj != null && msg.getData() != null) {
						Bundle data = msg.getData();
						long uuid = data.getLong("uuid");
						int errorType = data.getInt("errortype");
						int errorReason = data.getInt("errorreason");
						updateErrorStatisticsData((Vector<MessageHeadBean>) msg.obj, errorType,
								errorReason, uuid);
					}
					break;
				default :
					break;
			}
		}

	};

	private void decodeBitmaps(MessageHeadBean bean) {
		File dir = new File(LauncherEnv.Path.MESSAGECENTER_PATH + bean.mId + "zip1");
		if (dir.exists() && dir.isDirectory()) {
			if (mEnterBmpList == null) {
				mEnterBmpList = new ArrayList<Bitmap>();
			}
			mEnterBmpList.clear();
			String[] files = dir.list();
			HttpUtil.sortFile(files);
			for (int i = 0; files != null && i < files.length; i++) {
				Bitmap orginalBmp = BitmapFactory.decodeFile(dir.getPath() + File.separator + files[i]);
				Bitmap bmp = bitmpaScale(orginalBmp);
				if (bmp != null) {
					mEnterBmpList.add(bmp);
				}
			}
		}
		dir = new File(LauncherEnv.Path.MESSAGECENTER_PATH + bean.mId + "zip2");
		if (dir.exists() && dir.isDirectory()) {
			if (mActingBmpList == null) {
				mActingBmpList = new ArrayList<Bitmap>();
			}
			mActingBmpList.clear();
			String[] files = dir.list();
			HttpUtil.sortFile(files);
			for (int i = 0; files != null && i < files.length; i++) {
				Bitmap orginalBmp = BitmapFactory.decodeFile(dir.getPath() + File.separator + files[i]);
				Bitmap bmp = bitmpaScale(orginalBmp);
				if (bmp != null) {
					mActingBmpList.add(bmp);
				}
			}
		}
	}

	private Bitmap bitmpaScale(Bitmap bmp) {
		if (bmp == null) {
			return null;
		}
		Bitmap scaleBitmap = null;
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		if (bmpWidth > 0 && bmpHeight > 0) {
			float scale = 1.0f;
			int width = GoLauncher.getScreenWidth();
			int height = GoLauncher.getScreenHeight();
			scale = width > height ? (height + 0.1f) / 480 : (width + 0.1f) / 480;
			int dstWidth = (int) (bmpWidth * scale);
			int dstHeight = (int) (bmpHeight * scale);
			try {
				scaleBitmap = Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, false);
			} catch (Exception e) {
				scaleBitmap = null;
				e.printStackTrace();
			}
		}
		//		if (bmp != null && !bmp.isRecycled()) {
		//			bmp.recycle();
		//			bmp = null;
		//		}
		return scaleBitmap;
	}

	private void recyleZicons() {
		if (mEnterBmpList != null && mEnterBmpList.size() != 0) {
			for (int i = 0; i < mEnterBmpList.size(); i++) {
				Bitmap bmp = mEnterBmpList.get(i);
				if (bmp != null && !bmp.isRecycled()) {
					bmp.recycle();
				}
			}
			mEnterBmpList.clear();
			mEnterBmpList = null;
		}
		if (mActingBmpList != null && mActingBmpList.size() != 0) {
			for (int i = 0; i < mActingBmpList.size(); i++) {
				Bitmap bmp = mActingBmpList.get(i);
				if (bmp != null && !bmp.isRecycled()) {
					bmp.recycle();
				}
			}
			mActingBmpList.clear();
			mActingBmpList = null;
		}
	}

	@Override
	public void onDownLoadFinsish() {
		mHandler.sendEmptyMessage(MSG_REFRASH_ICONVIEW);
	}

	/**
	 * <br>功能简述:移除罩子层视图
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void removeCoverFrameView() {
		GoLauncher
				.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.COVER_FRAME_REMOVE_VIEW,
						CoverFrame.COVER_VIEW_MESSAGECENTER, null, null);
	}

	public void clickCloseCoverFrameView(MessageHeadBean bean) {
		removeCoverFrameView();
		markAsClickClosed(bean);
		saveCoverFrameClickCloseStatisticsData(bean.mId);
		Vector<MessageHeadBean> statisticsMsgs = new Vector<MessageHeadBean>();
		statisticsMsgs.add(bean);
		updateStatisticsData(statisticsMsgs, MessageBaseBean.VIEWTYPE_DESK_TOP, 0,
				IPreferencesIds.SHAREDPREFERENCES_MSG_COVER_FRAME_CLOSE_BUTTON_CLICK_TIMES, null);
	}

	/**
	 * <br>功能简述:MessageInfo 转为MessageHeadBean
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param info
	 * @return
	 */
	private MessageHeadBean convertInfo2Bean(MessageInfo info) {
		MessageHeadBean bean = new MessageHeadBean();
		bean.mId = info.mId;
		bean.misReaded = info.misReaded;
		bean.mMsgTimeStamp = info.mTimeStamp;
		bean.mType = info.mType;
		bean.mTitle = info.mTitle;
		bean.mViewType = info.mViewType;
		bean.mUrl = info.mUrl;
		bean.mActType = info.mActtype;
		bean.mActValue = info.mActValue;
		bean.mEndTime = info.mEndTime;
		bean.mIcon = info.mIconUrl;
		bean.mSummery = info.mIntro;
		bean.mStartTime = info.mStartTime;
		bean.mZicon1 = info.mZIcon1;
		bean.mZicon2 = info.mZIcon2;
		bean.mZpos = info.mZpos;
		bean.mZtime = info.mZtime;
		bean.mIsColsed = info.mIsClosed;
		bean.mFilterPkgs = info.mFilterPkgs;
		bean.mClickClosed = info.mClickClosed;
		bean.mDynamic = info.mDynamic;
		bean.mIconpos = info.mIconpos;
		bean.mFullScreenIcon = info.mFullScreenIcon;
		bean.mIsRemoved = info.mIsRemoved;
		bean.mWhiteList = info.mWhiteList;
		return bean;
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param id
	 */
	public void saveShowStatisticsData(String id) {
		mMsgHttp.saveStaticItemData(id, IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES);
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param id
	 */
	public void saveClickStatisticsData(String id) {
		mMsgHttp.saveStaticItemData(id, IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES);
	}

	public void savePushStatisticsData(String id) {
		mMsgHttp.saveStaticItemData(id, IPreferencesIds.SHAREDPREFERENCES_MSG_PUSH_TIMES);
	}

	public void saveWidgetClickStaticsData(String msgId, String value) {
		mMsgHttp.saveStaticItemButtonClick(msgId, value);
	}

	public void saveCoverFrameClickCloseStatisticsData(String id) {
		mMsgHttp.saveStaticItemData(id,
				IPreferencesIds.SHAREDPREFERENCES_MSG_COVER_FRAME_CLOSE_BUTTON_CLICK_TIMES);
	}
	/**
	 * <br>功能简述:清楚一条消息的统计
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param id
	 */
	public void clearStaticData(String id) {
		PreferencesManager manager = new PreferencesManager(mContext);
		int count = manager.getInt(IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES + id, 0);
		manager.putInt(IPreferencesIds.SHAREDPREFERENCES_MSG_CLICK_TIMES + id, count + 1);
		count = manager.getInt(IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES + id, 0);
		manager.putInt(IPreferencesIds.SHAREDPREFERENCES_MSG_SHOW_TIMES + id, count + 1);
		manager.commit();
	}
	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:uuid 调用时为0，只有失败后自动再次上传时会是上次的值
	 * @param bean
	 * @param entrance
	 * @param uuid 传送失败后再次调用的id，第一次调用为0
	 */
	public void updateStatisticsData(final Vector<MessageHeadBean> beans, final int entrance,
			final long uuid, final String staticticsType, final String clickItemName) {

		new Thread() {
			@Override
			public void run() {
				super.run();
				if (beans != null && beans.size() > 0) {
					long id = uuid;
					if (uuid == 0) {
						id = System.currentTimeMillis();
					}
					if (!mMsgHttp.updateStatisticsData(beans, entrance, id, staticticsType,
							clickItemName) && uuid == 0) {
						Message msg = Message.obtain();
						msg.what = MSG_REUPDATE_STATISTICSDATA;
						msg.obj = beans;
						msg.arg1 = entrance;
						Bundle bundle = new Bundle();
						bundle.putLong("uuid", id);
						bundle.putString("staticticstype", staticticsType);
						bundle.putString("clickitemname", clickItemName);
						msg.setData(bundle);
						mHandler.sendMessageDelayed(msg, 6000);
					}
				}
			}
		}.start();
	}

	public void updateErrorStatisticsData(final Vector<MessageHeadBean> beans, final int errorType,
			final int errorReason, final long uuid) {
		new Thread() {
			@Override
			public void run() {
				super.run();
				if (beans != null && beans.size() > 0) {
					long id = uuid;
					if (uuid == 0) {
						id = System.currentTimeMillis();
					}
					if (!mMsgHttp.updateErrorStatisticsData(beans, errorType, errorReason, id)
							&& uuid == 0) {
						Message msg = Message.obtain();
						msg.what = MSG_REUPDATE_ERRORSTATISTICSDATA;
						msg.obj = beans;
						Bundle bundle = new Bundle();
						bundle.putLong("uuid", id);
						bundle.putInt("errortype", errorType);
						bundle.putInt("errorreason", errorReason);
						msg.setData(bundle);
						mHandler.sendMessageDelayed(msg, 6000);
					}
				}
			}
		}.start();
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:uuid 调用时为0，只有失败后自动再次上传时会是上次的值
	 * @param bean
	 * @param entrance
	 * @param uuid 传送失败后再次调用的id，第一次调用为0
	 * @param isShow 是否是显示统计数据
	 */
	public void updateThemeNotifyStatisticsData(final int type, final long uuid,
			final boolean isShow) {
		new Thread() {
			@Override
			public void run() {
				super.run();
				long id = uuid;
				if (uuid == 0) {
					id = System.currentTimeMillis();
				}
				if (!mMsgHttp.updateThemeNotifyStatisticsData(type, id, isShow) && uuid == 0) {
					Message msg = Message.obtain();
					msg.what = MSG_REUPDATE_THEME_STATISTICSDATA;
					msg.arg1 = type;
					msg.obj = id;
					msg.arg2 = ConvertUtils.boolean2int(isShow);
					mHandler.sendMessageDelayed(msg, 6000);
				}
			}
		}.start();
	}
}
