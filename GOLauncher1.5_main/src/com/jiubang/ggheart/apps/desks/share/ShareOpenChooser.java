package com.jiubang.ggheart.apps.desks.share;
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
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.plugin.mediamanagement.AppInfo;
import com.jiubang.ggheart.plugin.mediamanagement.ChooserListViewIcon;
/***
 * 
 * <br>类描述: 自定义打开程序选择框
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-9-11]
 */
public class ShareOpenChooser {
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
	private Handler mHandler = null;

	private int mDialogOriginalWidth;
	private int mDialogOriginalHeight;
	private int mItemHeight;
	private int mTopHeight;
	private int mBottomHeight;
	private static ShareOpenChooser sInstance;

	public ShareOpenChooser(Context context) {
		mContext = context;
		mApps = new ArrayList<AppInfo>();
		mAdapter = new MyAdapter();
		initDimenSize();
		initHandler();
		initViews();
		// 注册消息接收器，接受横竖屏切换事件
		GoLauncher.registMsgHandler(new MsgHanlder());
	}

	public static ShareOpenChooser getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new ShareOpenChooser(context);
		} else if (sInstance.mContext != context) {
			sInstance = new ShareOpenChooser(context);
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
	public void openChooser(Intent intent) {
		mIsCheck = false;
		initViews();
		mIntent = intent;
		initPKDatasByIntent(mIntent);
		if (mApps == null || mApps.size() <= 0) {
			DeskToast.makeText(mContext, R.string.no_way_to_open_file, Toast.LENGTH_SHORT).show();
		} else if (mApps.size() == 1) {
			// 如果只有一个可以打开媒体的程序，直接打开
			open(mApps.get(0));
		} else {
			showDialog();
		}
	}

	public void openChooser(Intent intent, String title) {
		openChooser(intent);
		mTitle.setText(title);
	}
	
	/**
	 * 显示对话框
	 * 
	 */
	private void showDialog() {
		mDialog = new Dialog(mContext, R.style.media_open_chooser_dialog);
		mDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
			}
		});
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
		mDialog.addContentView(mRootView, params);
		mDialog.show();
	}

	/**
	 * 计算对话框的高度
	 */
	private int getDialogHeight(int maxHeight) {
		int height = mTopHeight + mBottomHeight + mItemHeight * mApps.size();
		return maxHeight > height ? height : maxHeight;
	}

	private void initPKDatasByIntent(Intent intent) {
		mTitle.setText(R.string.choose_share_way);
		// 初始化系统程序数据
		PackageManager pm = mContext.getPackageManager();
		List<ResolveInfo> infos = pm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		mApps.clear();
		boolean is200Channerl = GoStorePhoneStateUtil.is200ChannelUid(mContext);

		if (infos != null && !infos.isEmpty()) {
			for (ResolveInfo resolveInfo : infos) {
				AppInfo info = new AppInfo();
				info.pkName = resolveInfo.activityInfo.packageName;
				info.actName = resolveInfo.activityInfo.name;
				info.displayName = resolveInfo.activityInfo.loadLabel(pm).toString();
				Intent newIntent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				ComponentName c = new ComponentName(info.pkName, info.actName);
				newIntent.setComponent(c);
				info.icon = AppDataEngine.getInstance(mContext).getAppItemIconByIntent(newIntent);
				mApps.add(info);
			}
		}
		//排序
		mApps = sortmAppsList(mApps , is200Channerl);
		if (mHandler != null) {
			Message msg = mHandler.obtainMessage();
			msg.what = MSG_ID_REFRASH_LIST;
			msg.sendToTarget();
		}
	}

	/**
	 *排序规则:1.200 渠道的排序: Facebook、Instagram 、whatsapp 其余按名称排序
	 *		2.非200 渠道的排序: QQ空间、新浪微博 、微信 其余按名称排序 
	 * @param mApps2 
	 * @param flag 是否是200渠道
	 * @return
	 */
	private ArrayList<AppInfo> sortmAppsList(List<AppInfo> mApps2, boolean flag) {
		ArrayList<AppInfo> temp = new ArrayList<AppInfo>();
		if (flag) {
			for (int i = 0; i < mApps.size(); i++) {
				if (mApps.get(i).pkName.contains("com.whatsapp")) {
					temp.add(0, mApps.get(i));
				}
			}
			for (int i = 0; i < mApps.size(); i++) {
				if (mApps.get(i).pkName.contains("com.instagram.android")) {
					temp.add(0, mApps.get(i));
				}
			}
			for (int i = 0; i < mApps.size(); i++) {
				if (mApps.get(i).pkName.contains("com.facebook.katana")) {
					temp.add(0, mApps.get(i));
				}
			}
		} else {
			for (int i = 0; i < mApps.size(); i++) {
				if (mApps.get(i).pkName.contains("com.tencent.mm")) {
					temp.add(0, mApps.get(i));
				}
			}
			for (int i = 0; i < mApps.size(); i++) {
				if (mApps.get(i).pkName.contains("com.sina.weibo")) {
					temp.add(0, mApps.get(i));
				}
			}
			for (int i = 0; i < mApps.size(); i++) {
				if (mApps.get(i).pkName.contains("com.qzone")) {
					temp.add(0, mApps.get(i));
				}
			}
		}
		mApps2.removeAll(temp);
		for (int i = 0; i < mApps.size(); i++) {
			temp.add(mApps.get(i));
		}
		return temp;
	}
	
	private void initViews() {
		LayoutInflater factory = LayoutInflater.from(mContext);
		mRootView = factory.inflate(R.layout.appfunc_mediamanagement_activitychooser, null);
		mTitle = (TextView) mRootView
				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_title);
		mCheckImg = (ImageView) mRootView
				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_img);
		TextView defaultText = (TextView) mRootView.findViewById(R.id.set_as_default);
		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsCheck) {
					mCheckImg.setImageResource(R.drawable.apps_uninstall_not_selected);
					mIsCheck = false;
				} else {
					mCheckImg.setImageResource(R.drawable.apps_uninstall_selected);
					mIsCheck = true;
				}
			}
		};
		defaultText.setOnClickListener(listener);
		mCheckImg.setOnClickListener(listener);
		defaultText.setVisibility(View.GONE);
		mCheckImg.setVisibility(View.GONE);
		mList = (ListView) mRootView
				.findViewById(R.id.appfunc_mediamanagement_activity_chooser_list);
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
				AppInfo info = (AppInfo) view.getTag();
				StatisticsData.countMenuData(mContext, StatisticsData.SHARE_KEY + info.pkName);
				open(info);
				mDialog.dismiss();
			}
		});
	}

	private void open(AppInfo info) {
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
			DeskToast.makeText(mContext, R.string.no_way_to_open_file, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			DeskToast.makeText(mContext, R.string.no_way_to_open_file, Toast.LENGTH_SHORT).show();
		}

	}

	private void saveDefault(String uri) {
		//			FunAppSetting setting = GoSettingControler.getInstance(mContext).getFunAppSetting();
		//			setting.saveMediaOpenWay(mCurrentType, uri);
	}

	/***
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  maxiaojun
	 * @date  [2012-9-11]
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
	/***
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  maxiaojun
	 * @date  [2012-9-11]
	 */
	public class MsgHanlder implements IMessageHandler {
		@Override
		public int getId() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean handleMessage(Object arg0, int arg1, int arg2, int arg3, Object arg4,
				List arg5) {
			switch (arg2) {
				case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED : {
					if (mRootView != null) {
						FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
								LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
				default :
					break;
			}
			return false;
		}
	}
}
