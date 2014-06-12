package com.jiubang.ggheart.plugin.mediamanagement;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.file.media.AudioFile;
import com.go.util.file.media.FileEngine;
import com.go.util.file.media.FileInfo;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.appfunc.setting.AppFuncAutoFitManager;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;

/**
 * 资源管理文件打开程序选择类
 * 
 * @author huangshaotao
 * 
 */
public class MediaManagementOpenChooser {
	public static final String APP_NONE = "APP_NONE";
	public static final String APP_GO_MUSIC_PLAYER = "APP_GO_MUSIC_PLAYER";
	public static final String APP_GO_PIC_VIEWER = "APP_GO_PIC_VIEWER";
	public static final int MSG_ID_REFRASH_LIST = 0x0001;

	private Context mContext = null;
	private View mRootView = null;
	private Dialog mDialog = null;
	private TextView mTitle = null;
	private ListView mList = null;
	private ImageView mCheckImg = null;
	private boolean mIsCheck;
	private MyAdapter mAdapter = null;
	private List<AppInfo> mApps = null;
	private Intent mIntent = null;
	private int mCurrentType = FileEngine.TYPE_IMAGE;
	private Object[] mObjects;
	private Handler mHandler = null;

	private int mDialogOriginalWidth;
	private int mDialogOriginalHeight;
	private int mItemHeight;
	private int mTopHeight;
	private int mBottomHeight;
	private static MediaManagementOpenChooser sInstance;

	private MediaManagementOpenChooser(Context context) {
		mContext = context;
		mApps = new ArrayList<AppInfo>();
		mAdapter = new MyAdapter();
		initDimenSize();
		initHandler();
		initViews();
		// 注册消息接收器，接受横竖屏切换事件
		GoLauncher.registMsgHandler(new MsgHanlder());
	}

	public static MediaManagementOpenChooser getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new MediaManagementOpenChooser(context);
		}
		return sInstance;
	}

	private void initDimenSize() {
		mDialogOriginalHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_height);
		mDialogOriginalWidth = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_width);
		mItemHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_listitem_height);
		mTopHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_top_height);
		mBottomHeight = mContext.getResources().getDimensionPixelSize(
				R.dimen.media_open_dialog_bottom_height);

	}

	/**
	 * 打开activity选择器，用于选择打开图片和音乐文件的程序
	 * 
	 * @param contentType
	 * @param info
	 * @param mimeType
	 * @param type
	 *            FileEngine里的TYPE类型（TYPE_IMAGE, TYPE_VIDEO, TYPE_AUDIO)
	 * @param objs
	 *            额外参数
	 */
	public void openChooser(FileInfo info, String mimeType,
			int type, Object... objs) {
		if (info == null || mimeType == null) {
			return;
		}
		if (mCheckImg != null) { // 还原勾选框为默认状态
			mCheckImg.setImageResource(R.drawable.apps_uninstall_not_selected);
		}
		mIsCheck = false;
		// initViews();
		mObjects = objs;
		mCurrentType = type;
		mIntent = new Intent(Intent.ACTION_VIEW);
		mIntent.setDataAndType(Uri.parse("file://" + info.fullFilePath),
				mimeType);
		initPKDatasByIntent(mIntent, objs);
		if (mApps == null || mApps.size() <= 0) {
			DeskToast.makeText(mContext, R.string.no_way_to_open_file,
					Toast.LENGTH_SHORT).show();
		} else if (mApps.size() == 1) {
			// 如果只有一个可以打开媒体的程序，直接打开
			open(mApps.get(0));
		} else {
			// mDialog.show();
			showDialog();
		}
	}

	/**
	 * 显示对话框
	 * 
	 */
	private void showDialog() {
		// 计算对话框高度
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		if (AppFuncUtils.getInstance(mContext).isVertical()) {
			// 竖屏
			params.height = getDialogHeight(mDialogOriginalHeight);
			params.width = mDialogOriginalWidth;
		} else {
			// 横屏
			params.height = getDialogHeight(mDialogOriginalWidth);
			params.width = mDialogOriginalHeight;
		}
		if (mDialog == null) {
			mDialog = new Dialog(mContext, R.style.media_open_chooser_dialog);
			mDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					mObjects = null;
					mApps.clear();
				}
			});
			mDialog.addContentView(mRootView, params);
		} else {
			mDialog.setContentView(mRootView, params);
		}
		mDialog.show();
		XViewFrame.getInstance().setDisplayedDialog(mDialog);

	}

	/**
	 * 计算对话框的高度
	 */
	private int getDialogHeight(int maxHeight) {
		int height = mTopHeight + mBottomHeight + mItemHeight * mApps.size();
		return maxHeight > height ? height : maxHeight;
	}

	/**
	 * 初始化go程序数据
	 */
	private void initGoData() {
		mApps.clear();
		AppInfo info = new AppInfo();
		AppFuncAutoFitManager appFuncAutoFitManager = AppFuncAutoFitManager
				.getInstance(GOLauncherApp.getContext());

		switch (mCurrentType) {
		case FileEngine.TYPE_AUDIO: {
			mTitle.setText(R.string.appfunc_mediamanagement_chooser_music);
			if (!appFuncAutoFitManager.needHideMusicPlayer()
					&& MediaPluginFactory.isMediaPluginExist(mContext)) {
				info.pkName = APP_GO_MUSIC_PLAYER;
				info.displayName = mContext.getResources().getString(
						R.string.go_music_player);
				info.icon = mContext.getResources().getDrawable(
						R.drawable.go_music_player_icon);
				mApps.add(info);
			}
			break;
		}
		case FileEngine.TYPE_IMAGE: {
			mTitle.setText(R.string.appfunc_mediamanagement_chooser_pic);
			if (!appFuncAutoFitManager.needHideImageBrowser()
					&& MediaPluginFactory.isMediaPluginExist(mContext)) {
				info.pkName = APP_GO_PIC_VIEWER;
				info.displayName = mContext.getResources().getString(
						R.string.go_picture_browser);
				info.icon = mContext.getResources().getDrawable(
						R.drawable.go_picture_browser_icon);
				mApps.add(info);
			}
			break;
		}
		}
	}

	private void initPKDatasByIntent(Intent intent, Object... objs) {
		// 初始化go程序数据
		initGoData();
		// 初始化系统程序数据
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> infos = pm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);

		if (infos != null && !infos.isEmpty()) {
			for (ResolveInfo resolveInfo : infos) {
				AppInfo info = new AppInfo();
				info.pkName = resolveInfo.activityInfo.packageName;
				info.actName = resolveInfo.activityInfo.name;
				info.displayName = resolveInfo.activityInfo.loadLabel(pm)
						.toString();
				Intent newIntent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				ComponentName c = new ComponentName(info.pkName, info.actName);
				newIntent.setComponent(c);
				info.icon = AppDataEngine.getInstance(mContext)
						.getAppItemIconByIntent(newIntent);
				mApps.add(info);
			}
		}
		if (mHandler != null) {
			Message msg = mHandler.obtainMessage();
			msg.what = MSG_ID_REFRASH_LIST;
			msg.sendToTarget();
		}
	}

	private void initViews() {
		LayoutInflater factory = LayoutInflater.from(mContext);
		mRootView = factory.inflate(
				R.layout.appfunc_mediamanagement_activitychooser, null);
		mTitle = (TextView) mRootView
				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_title);
		mCheckImg = (ImageView) mRootView
				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_img);
		TextView defaultText = (TextView) mRootView
				.findViewById(R.id.set_as_default);
		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mIsCheck) {
					mCheckImg
							.setImageResource(R.drawable.apps_uninstall_not_selected);
					mIsCheck = false;
				} else {
					mCheckImg
							.setImageResource(R.drawable.apps_uninstall_selected);
					mIsCheck = true;
				}

			}

		};
		defaultText.setOnClickListener(listener);
		mCheckImg.setOnClickListener(listener);

		mList = (ListView) mRootView
				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_list);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int position, long id) {
				AppInfo info = (AppInfo) view.getTag();
				open(info);
				mDialog.dismiss();
				mObjects = null;
			}

		});
	}

	private void open(AppInfo info) {
		if (APP_GO_MUSIC_PLAYER.equals(info.pkName)) {
			if (mIsCheck) {
				saveDefault(APP_GO_MUSIC_PLAYER);
			}
			// 打开go音乐播放器
			openWithGoMusicPlayer();
		} else if (APP_GO_PIC_VIEWER.equals(info.pkName)) {
			if (mIsCheck) {
				saveDefault(APP_GO_PIC_VIEWER);
			}
			// 打开go图片浏览器
			openWithGoPictureBrowser();
		} else {
			try {
				ApplicationIcon.sIsStartApp = true;
				mIntent.setClassName(info.pkName, info.actName);
				if (mIsCheck) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setClassName(info.pkName, info.actName);
					saveDefault(intent.toURI());
				}
				mContext.startActivity(mIntent);
			} catch (ActivityNotFoundException ex) {
				DeskToast.makeText(mContext, R.string.no_way_to_open_file,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				DeskToast.makeText(mContext, R.string.no_way_to_open_file,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private void openWithGoMusicPlayer() {
		if (mObjects != null && mObjects.length > 0) {
			boolean isOpenBySearch = (Boolean) mObjects[1];
//			if (isOpenBySearch) {
//				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//						IFrameworkMsgId.HIDE_FRAME,
//						IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
//				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//						IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.APPFUNC_FRAME,
//						null, null);
//			}

			AudioFile mInfo = (AudioFile) mObjects[0];
			// DeliverMsgManager.getInstance().onChange(
			// AppFuncConstants.APP_FUNC_MAIN_VIEW,
			// AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
			// new Object[] {
			// MediamanagementTabBasicContent.CONTENT_TYPE_MUSIC_PLAY,
			// mContentType, (long) mInfo.dbId, mInfo.album, isOpenBySearch});
			// 使用go播放器打开
//			MediaManagerFactory.getMediaManager().openMusicPlayer(
//					new Object[] { (long) mInfo.dbId,
//							mInfo.album, isOpenBySearch, mInfo });
			DeliverMsgManager.getInstance().onChange(
					AppFuncConstants.APP_FUNC_MAIN_VIEW,
					AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
					new Object[] { AppFuncContentTypes.MUSIC_PLAYER, (long) mInfo.dbId,
					mInfo.album, isOpenBySearch, mInfo });
		}
	}

	private void openWithGoPictureBrowser() {
		// if (mObjects != null && mObjects.length > 0) {
		// FileInfo fileInfo = (FileInfo) mObjects[0];
		// ArrayList<FileInfo> itemInfos = (ArrayList<FileInfo>) mObjects[1];
		// // 使用go图片浏览器打开
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IFrameworkMsgId.SHOW_FRAME,
		// IDiyFrameIds.IMAGE_BROWSER_FRAME, null, null);
		// GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
		// IFrameworkMsgId.HIDE_FRAME,
		// IDiyFrameIds.IMAGE_BROWSER_FRAME, null, null);
		// ArrayList<Object> list = new ArrayList<Object>();
		// list.add(itemInfos);
		// list.add(mObjects[3]);
		// GoLauncher.sendMessage(this, IDiyFrameIds.IMAGE_BROWSER_FRAME,
		// IDiyMsgIds.IMAGE_BROWSER_RECEVIE_IMG_PATH,
		// itemInfos.indexOf(fileInfo),
		// mObjects[2], list);
		// }
		// 使用go图片浏览器打开
//		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
//				IFrameworkMsgId.SHOW_FRAME, IDiyFrameIds.IMAGE_BROWSER_FRAME,
//				null, null);
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
				AppFuncConstants.OPEN_IMAGE_BROWSER, mObjects);
	}

	private void saveDefault(String uri) {
		FunAppSetting setting = GoSettingControler.getInstance(mContext)
				.getFunAppSetting();
		setting.saveMediaOpenWay(mCurrentType, uri);
		// if (AppUtils.isMediaPluginExist(mContext)) {
		// IMediaManager mediaManager = MediaManagerFactory.getMediaManager();
		// if (mediaManager != null) {
		// if (mCurrentType == FileEngine.TYPE_AUDIO) {
		// mediaManager.setMusicDefaultOpenWay(uri);
		// } else if (mCurrentType == FileEngine.TYPE_IMAGE) {
		// mediaManager.setImageDefaultOpenWay(uri);
		// }
		// }
		// }
	}

	/**
	 * 
	 * 适配器
	 */
	private class MyAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			if (mApps == null) {
				return 0;
			}
			return mApps.size();
		}

		@Override
		public Object getItem(int i) {
			if (mApps == null) {
				return null;
			}
			if (i < 0 || i >= mApps.size()) {
				return null;
			}
			return mApps.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewgroup) {
			ChooserListViewIcon cv = null;
			if (view != null && view instanceof ChooserListViewIcon) {
				cv = (ChooserListViewIcon) view;
			} else {
				cv = new ChooserListViewIcon(mContext);
			}

			AppInfo info = mApps.get(i);
			cv.setTitle(info.displayName);
			cv.setIcon(info.icon);

			cv.setTag(info);
			return cv;
		}
	}

	private void initHandler() {
		mHandler = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MSG_ID_REFRASH_LIST) {
					if (mAdapter != null) {
						mAdapter.notifyDataSetChanged();
					}
				} else {
					super.handleMessage(msg);
				}
			}
		};

	}

	/**
	 * 
	 * 消息队列
	 */
	public class MsgHanlder implements IMessageHandler {

		@Override
		public int getId() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean handleMessage(Object arg0, int arg1, int arg2, int arg3,
				Object arg4, List arg5) {
			switch (arg2) {
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED: {
				if (mRootView != null) {
					// android.view.ViewGroup.LayoutParams layoutParams =
					// mRootView.getLayoutParams();
					FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					if (arg3 == Configuration.ORIENTATION_PORTRAIT) {
						layoutParams.height = getDialogHeight(mDialogOriginalHeight);
						layoutParams.width = mDialogOriginalWidth;
					} else if (arg3 == Configuration.ORIENTATION_LANDSCAPE) {
						layoutParams.height = getDialogHeight(mDialogOriginalWidth);
						layoutParams.width = mDialogOriginalHeight;

					}
					mRootView.setLayoutParams(layoutParams);
				}

			}
				break;
			default:
				break;
			}
			return false;
		}

	}
}
