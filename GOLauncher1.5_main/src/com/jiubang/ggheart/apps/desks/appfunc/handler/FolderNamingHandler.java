package com.jiubang.ggheart.apps.desks.appfunc.handler;

import java.util.ArrayList;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncExceptionHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.themescan.EditDialog;
import com.jiubang.ggheart.data.DatabaseException;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 文件夹命名控制器
 * 
 * @author tanshu
 * 
 */
public class FolderNamingHandler implements OnClickListener { // ,
																// OnDismissListener{
	private static FolderNamingHandler sInstance;
	private static Activity mActivity;
	/**
	 * 重命名控件
	 */
	private EditDialog mRenameDialog;
	/**
	 * 文件夹Info
	 */
	private FunFolderItemInfo mFolderInfo;
	/**
	 * 初始化时的字符串
	 */
	private String mText;

	/**
	 * 点击的按键: 0为未点击按键， 1为确定，2为取消
	 */
	// private int mButtonClicked;

	public static FolderNamingHandler getInstance(Activity activity) {
		if (mActivity == null) {
			mActivity = activity;
		}
		if (sInstance == null) {
			sInstance = new FolderNamingHandler();
		}
		// sInstance.mButtonClicked = 0;
		return sInstance;
	}

	/**
	 * 显示编辑框控件
	 * 
	 * @param text
	 * @param mIntent
	 *            如果是新建文件夹，值为null
	 */
	public void showEditDialog(String text, FunFolderItemInfo folderInfo) {
		if (mRenameDialog == null) {
			mRenameDialog = new EditDialog(mActivity, GOLauncherApp.getApplication().getResources()
					.getString(R.string.folder_naming));
			mRenameDialog.setPositiveButton(GOLauncherApp.getApplication().getResources()
					.getString(R.string.ok), this);
			mRenameDialog.setNegativeButton(GOLauncherApp.getApplication().getResources()
					.getString(R.string.cancle), this);
			mRenameDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {
					mRenameDialog.selfDestruct();
					mRenameDialog = null;
				}
			});
			// mRenameDialog.setOnDismissListener(this);
		}
		mText = text;
		mRenameDialog.setText(text);
		mRenameDialog.showWithInputMethod();
		mFolderInfo = folderInfo;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		if (which == DialogInterface.BUTTON_POSITIVE) {
			// mButtonClicked = 1;
			String title = null;
			if (mRenameDialog != null) {
				title = mRenameDialog.getText();
			}
			if (title == null || (title != null && title.trim().length() == 0)) {
				title = GOLauncherApp.getApplication().getResources()
						.getString(R.string.folder_name);
			}
			// if(mFolderId == -1){
			// //通知TabComponent创建文件夹
			// DeliverMsgManager.getInstance().onChange(AppFuncConstants.TABCOMPONENT,
			// AppFuncConstants.CREATEFOLDER, title);
			// }
			// else{
			// 通知后台更新文件夹名称
			if (mFolderInfo != null) {
				if (title.compareTo(mText) != 0) {
					String text = title;
					text = text.replaceAll("\\s+", " ");
					try {
						mFolderInfo.setTitle(text);
					} catch (DatabaseException e) {
						AppFuncExceptionHandler.handle(e);
						return;
					}
					// 通知桌面重命名
					ArrayList<String> nameList = new ArrayList<String>();
					// 第一个为新的名字
					nameList.add(text);
					// 第二个为以前的名字
					nameList.add(mText);
					GoLauncher
							.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
									IDiyMsgIds.SCREEN_FOLDER_RENAME, 0, mFolderInfo.getFolderId(),
									nameList);
					GoLauncher
							.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
									IDiyMsgIds.SCREEN_FOLDER_RENAME, 0, mFolderInfo.getFolderId(),
									nameList);
					// AppCore.getInstance(mActivity).getFunControler().
					// setFolderName(mFolderId, title);
				}
			}
			// }
		}
		// else if(which == DialogInterface.BUTTON_NEGATIVE){
		// mButtonClicked = 2;
		// if(mFolderId == -1){
		// //通知TabComponent让图标归位
		// DeliverMsgManager.getInstance().onChange(AppFuncConstants.TABCOMPONENT,
		// AppFuncConstants.REVERTICON, null);
		// }
		// }
	}

	// @Override
	// public void onDismiss(DialogInterface dialog) {
	// // TODO Auto-generated method stub
	// if(mButtonClicked == 0){
	// //只有当不是通过确定取消按钮触发的消失才通知归位
	// if(mFolderId == -1){
	// //通知TabComponent让图标归位
	// DeliverMsgManager.getInstance().onChange(AppFuncConstants.TABCOMPONENT,
	// AppFuncConstants.REVERTICON, null);
	// }
	// }
	// }
}
