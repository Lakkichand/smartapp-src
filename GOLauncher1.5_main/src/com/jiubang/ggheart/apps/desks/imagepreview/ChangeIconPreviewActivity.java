package com.jiubang.ggheart.apps.desks.imagepreview;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.apps.desks.diy.CustomIconUtil;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.IRequestCodeIds;
import com.jiubang.ggheart.apps.desks.dock.DockStyleIconManager;
import com.jiubang.ggheart.apps.desks.dock.DockStylePkgInfo;
import com.jiubang.ggheart.apps.desks.dock.StyleBaseInfo;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewAdapter.FileImageNode;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewAdapter.IImageNode;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewAdapter.PackageImageNode;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewAdapter.ResourceImageNode;
import com.jiubang.ggheart.components.DeskActivity;
import com.jiubang.ggheart.components.DeskProgressDialog;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.components.IQuickActionId;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.CustomIconRes;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 更换图标
 * 
 * @author licanhui
 */
public class ChangeIconPreviewActivity extends DeskActivity implements OnClickListener {
	/**
	 * 标记是从哪种请求返回的更换图标操作
	 */
	public static int sFromWhatRequester;

	public static final int SCREEN_STYLE = 1; // 桌面普通图标
	public static final int DOCK_STYLE_FROM_EDIT = 2; // dock直接进入更换图标选择界面
	public static final int USER_FOLDER_STYLE = 3; // 桌面user文件夹的样式
	public static final int DOCK_FOLDER_STYLE = 4; // dock user文件夹的样式
	public static final int SCREEN_FOLDER_ITEM_STYLE = 5; // 桌面文件夹内的图标
	public static final int DOCK_FOLDER_ITEM_STYLE = 6; // dock文件夹内的图标
	public static final String DEFAULT_NAME = "defaultName";
	public static final String DEFAULT_ICON_BITMAP = "defaultIconBitmap";

	private RelativeLayout mLayout;
	private LayoutInflater mInflater;
	private TextView mThemeNameTextView; // 当前主题标题
	private LinearLayout mTitleLinearLayout;
	private ImageView mDefaultIcon; // 默认图标
	private TextView mDefaultName; // 默认名称
	private LinearLayout mChangeThemeBtn; // 点击主题
	private LinearLayout mCustomBtn; // 自定义按钮
	private LinearLayout mDownBtn; // 下载按钮
	private Button mResetBtn; // 恢复默认按钮

	private ImageGridView mImageGridView;
	private ImagePreviewAdapter mImageAdapter;
	private ImageGridParam mParam;
	private ImagePreviewData mData;

	private ProgressDialog mProgressDialog; // 加载所有主题文件夹图标的进度条
	private Handler mHandler;
	private static final int LOAD_ICON_FINISH = 1; // 完成加载主题的标志

	private ArrayList<String> mPackageList = null; // 所有文件夹包的队列
	private ArrayList<String> mResNameList = null; // 所有文件夹图标的名称队列
	private ChangeThemeMenu mChangeThemeMenu = null; // 更换主题菜单
	public int mSelection = -1; // 目前选择的位置

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayout = (RelativeLayout) mInflater.inflate(R.layout.change_theme_icon_select, null);
		mLayout.setBackgroundColor(0xB31f1f1f);

		setContentView(mLayout);

		mDefaultIcon = (ImageView) findViewById(R.id.defaultIcon);
		mDefaultName = (TextView) findViewById(R.id.defaultName);
		mThemeNameTextView = (TextView) findViewById(R.id.themeName);
		mChangeThemeBtn = (LinearLayout) findViewById(R.id.changeThemeBtn);
		mTitleLinearLayout = (LinearLayout) findViewById(R.id.titleLinearLayout);
		mCustomBtn = (LinearLayout) findViewById(R.id.customBtn);
		mDownBtn = (LinearLayout) findViewById(R.id.downBtn);
		mResetBtn = (Button) findViewById(R.id.resetBtn);
		mChangeThemeBtn.setOnClickListener(this);
		mCustomBtn.setOnClickListener(this);
		mDownBtn.setOnClickListener(this);
		mResetBtn.setOnClickListener(this);

		setDefaultIconAndName(); // 设置默认图标和名字
		initHandler();
		initmParam(); // 初始化排版参数
		initGridView(); // 初始化排版参数

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.BELOW, R.id.title);
		mLayout.addView(mImageGridView, params);

		setGridViewData();

		// if(sFromWhatRequester == USER_FOLDER_STYLE ||sFromWhatRequester ==
		// DOCK_FOLDER_STYLE){
		// ImageView mDownImageView =
		// (ImageView)findViewById(R.id.downImageView);
		// mDownImageView.setVisibility(View.INVISIBLE);
		// mChangeThemeBtn.setEnabled(false);
		// }
		
		// Home键跳转标识
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.CHANGE_GOTOMAINSCREEN_FOR_HOMECLICK, 0, null, null);
	}

	/**
	 * 设置默认图标和名字
	 */
	public void setDefaultIconAndName() {
		Intent intent = getIntent();
		if (null != intent) {
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				// 设置默认图标
				Bitmap bmp = bundle.getParcelable(DEFAULT_ICON_BITMAP);
				// 某些系统由于图片格式的不一样。Bitmap被序列化传递过来后获取的getConfig为NULL，无法正常显示图片
				// 可以采取把Bitmap转化成字节数组进行传递
				if (bmp != null && bmp.getConfig() != null) {
					mDefaultIcon.setImageBitmap(bmp);
				} else {
					// 设置系统机器人图片
					BitmapDrawable sysBitmapDrawable = AppDataEngine.getInstance(this)
							.getSysBitmapDrawable();
					mDefaultIcon.setImageDrawable(sysBitmapDrawable);
				}

				// 设置默认名称
				String defaultNameString = bundle.getString(DEFAULT_NAME);
				if (defaultNameString != null) {
					mDefaultName.setText(defaultNameString);
				}
			}
		}
	}

	/**
	 * 初始化排版参数
	 */
	private void initmParam() {
		mParam = new ImageGridParam();
		Resources resources = this.getResources();
		mParam.mWidth = resources.getDimensionPixelSize(R.dimen.imagepreview_grid_width);
		mParam.mHeight = resources.getDimensionPixelSize(R.dimen.imagepreview_grid_height);
		mParam.mLeftPadding = resources.getDimensionPixelSize(R.dimen.imagepreview_grid_l_padding);
		mParam.mTopPadding = resources.getDimensionPixelSize(R.dimen.imagepreview_grid_t_padding);
		mParam.mRightPadding = resources.getDimensionPixelSize(R.dimen.imagepreview_grid_r_padding);
		mParam.mBottomPadding = resources
				.getDimensionPixelSize(R.dimen.imagepreview_grid_b_padding);
	}

	/**
	 * 初始化普通图标的GridView
	 */
	private void initGridView() {
		// 宫格
		mImageGridView = new ImageGridView(this, mParam);
		mImageGridView.setSelector(R.drawable.change_icon_tab_press);
		// 监听
		mImageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mImageAdapter.getmDrawablesSize() <= position) {
					return;
				}

				int nodeIndex = mImageAdapter.getNodeIdFromPosition(position);
				IImageNode node = (IImageNode) parent.getAdapter().getItem(nodeIndex);
				if (null != node) {
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					if (node instanceof ImagePreviewAdapter.FileImageNode) {
						bundle.putInt(ImagePreviewResultType.TYPE_STRING,
								ImagePreviewResultType.TYPE_IMAGE_FILE);
						bundle.putString(ImagePreviewResultType.IMAGE_PATH_STRING,
								((FileImageNode) node).getFilePath());
					} else if (node instanceof ImagePreviewAdapter.ResourceImageNode) {
						// 现在版本升级：原来GOLauncher内的（非主题包内的）图片也当TYPE_PACKAGE_RESOURCE类型来保存
						bundle.putInt(ImagePreviewResultType.TYPE_STRING,
								ImagePreviewResultType.TYPE_PACKAGE_RESOURCE);
						bundle.putString(ImagePreviewResultType.IMAGE_PACKAGE_NAME,
								ThemeManager.DEFAULT_THEME_PACKAGE);
						bundle.putString(ImagePreviewResultType.IMAGE_PATH_STRING,
								((ResourceImageNode) node).getResourceName());
					} else if (node instanceof ImagePreviewAdapter.DrawableImageNode) {
						bundle.putInt(ImagePreviewResultType.TYPE_STRING,
								ImagePreviewResultType.TYPE_DEFAULT);
					} else if (node instanceof ImagePreviewAdapter.PackageImageNode) {
						if (mData != null) {
							int folderIconPosition = mData.getFolderIconPosition();
							if (folderIconPosition == -1 || position < folderIconPosition) {
								bundle.putInt(ImagePreviewResultType.TYPE_STRING,
										ImagePreviewResultType.TYPE_PACKAGE_RESOURCE);
							} else {
								bundle.putInt(ImagePreviewResultType.TYPE_STRING,
										ImagePreviewResultType.TYPE_APP_ICON);
							}
						}
						bundle.putString(ImagePreviewResultType.IMAGE_PACKAGE_NAME,
								((PackageImageNode) node).getPackageName());
						bundle.putString(ImagePreviewResultType.IMAGE_PATH_STRING,
								((PackageImageNode) node).getPackageResName());
					}
					intent.putExtras(bundle);
					setResult(RESULT_OK, intent);
				}
				finish();
			}
		});
	}

	/**
	 * 初始化图标数据
	 */
	public void setGridViewData() {
		showProgressDialog(); // 显示等待框
		new Thread() {
			@Override
			public void run() {
				if (sFromWhatRequester == USER_FOLDER_STYLE
						|| sFromWhatRequester == DOCK_FOLDER_STYLE) {
					// 初始化文件夹图标数据
					mData = new ImagePreviewData(ChangeIconPreviewActivity.this, true);
				} else {
					// 初始化普通图标数据
					mData = new ImagePreviewData(ChangeIconPreviewActivity.this, false);
				}
				mHandler.sendEmptyMessage(LOAD_ICON_FINISH);
			}
		}.start();
	}

	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
					case LOAD_ICON_FINISH :
						initChangeThemeMenu();
						dismissProgressDialog(); // 取消加载框

						// 无数据提示
						if (mImageAdapter.getCount() <= 0) {
							Toast t = DeskToast.makeText(ChangeIconPreviewActivity.this,
									R.string.imagepreviewtip, Toast.LENGTH_SHORT);
							t.show();
						}
						break;
				}
			}
		};
	}

	/**
	 * 初始化更换主题列表
	 */
	private void initChangeThemeMenu() {
		ArrayList<String> datas = new ArrayList<String>();
		int selection = 0;
		try {
			// 如果是文件夹就在第一项加"文件夹图标"选项
			if (sFromWhatRequester == USER_FOLDER_STYLE || sFromWhatRequester == DOCK_FOLDER_STYLE) {
				datas.add(0, getString(R.string.change_icon_folder));
			}

			String curName = GOLauncherApp.getThemeManager().getCurThemePackage();
			String themeName = null;
			String themePkgName = null;
			for (int i = 0; i < mData.getThemeInfoBeans().size(); i++) {
				themeName = mData.getThemeInfoBeans().get(i).getThemeName();
				themePkgName = mData.getThemeInfoBeans().get(i).getPackageName();
				if (curName.equals(themePkgName)) {
					datas.add(themeName + "(" + getString(R.string.current) + ")");
					if (sFromWhatRequester == USER_FOLDER_STYLE
							|| sFromWhatRequester == DOCK_FOLDER_STYLE) {
						selection = 0; // 设置默认选择项
					} else {
						selection = i;
					}
				} else {
					datas.add(themeName);
				}
			}
		} catch (Throwable e) {
			// 发生异常，不赋值
		}

		if (sFromWhatRequester == DOCK_STYLE_FROM_EDIT) {
			// 如果是从dock请求更换图标，要判断当前是否有安装dock风格包
			DockStyleIconManager manager = GOLauncherApp.getDockStyleIconManager();
			ArrayList<StyleBaseInfo> mList = manager.getAllStyleBaseInfos();
			for (StyleBaseInfo info : mList) {
				datas.add(info.mStyleName);
			}

			mList.clear();
			mList = null;
		}

		mChangeThemeMenu = new ChangeThemeMenu(this, datas);
		mChangeThemeMenu.setmItemClickListener(this);
		selectTheme(selection, false);
	}

	/**
	 * 选择主题
	 * 
	 * @param position
	 * @param configurationChanged
	 */
	public void selectTheme(int position, boolean configurationChanged) {
		if (mChangeThemeMenu != null) {
			mChangeThemeMenu.dismiss();
		}

		if (!configurationChanged) {
			if (position == mSelection || mData == null) {
				return;
			}
		}

		try {
			if (sFromWhatRequester == USER_FOLDER_STYLE || sFromWhatRequester == DOCK_FOLDER_STYLE) {
				// 如果选择文件夹第一个选项
				if (position == 0) {
					changeThemeOfAllFolder(); // 显示所有文件夹图标
				} else {
					changeTheme(mData.getThemeInfoBeans().get(position - 1).getPackageName()); // 数据需要-1
				}
			} else {
				if (position < mData.getThemeInfoBeans().size()) {
					changeTheme(mData.getThemeInfoBeans().get(position).getPackageName());
				}
				// 选择了dock风格包
				else if (position >= mData.getThemeInfoBeans().size()) {
					int count = position - (mData.getThemeInfoBeans().size());
					DockStyleIconManager manager = GOLauncherApp.getDockStyleIconManager();
					ArrayList<StyleBaseInfo> mList = manager.getAllStyleBaseInfos();
					DockStylePkgInfo info = manager.getDockStylePkgInfo(mList.get(count).mPkgName);
					if (null != info) {
						changeDockStyle(info.mPkgName, info.mImageResList);
					}
				}
			}
			mThemeNameTextView.setText(mChangeThemeMenu.getmStrings().get(position)); // 设置标题名称
			mSelection = position;
		} catch (IndexOutOfBoundsException e) {
			Toast.makeText(this, "Error:IndexOutOfBound!", Toast.LENGTH_LONG).show();
		} catch (Throwable e) {
			// 发生异常，不处理
		}
	}

	/**
	 * 显示所有文件夹图标
	 */
	public void changeThemeOfAllFolder() {
		if (null != mImageAdapter) {
			mImageAdapter.free();
			mImageAdapter = null;
		}
		mImageAdapter = new ImagePreviewAdapter(this, mParam);
		mImageGridView.setAdapter(mImageAdapter);
		mImageAdapter.freePictures();
		mPackageList = mData.getmFolderPackageList();
		mResNameList = mData.getmFolderResNameList();
		mData.resetFolderIconPosition();
		mImageAdapter.initPackageResourceArrayInAllPacksges(mPackageList, mResNameList);
		mImageAdapter.start();
	}

	/**
	 * 换主题，响应为可选图标集替换
	 */
	private void changeTheme(String themePkg) {
		if (null == mData) {
			return;
		}

		if (null != mImageAdapter) {
			mImageAdapter.free();
			mImageAdapter = null;
		}
		mImageAdapter = new ImagePreviewAdapter(this, mParam);
		mImageGridView.setAdapter(mImageAdapter);

		mData.initData(themePkg); // 初始化图标信息数据

		changeTheme(themePkg, mData.getmDrawable(), mData.getmFloder(), mData.getmStringsarray(),
				mData.getmResNameList());
	}

	private void changeDockStyle(String themePkg, ArrayList<String> mArrayList) {
		if (null == mData) {
			return;
		}

		if (null != mImageAdapter) {
			mImageAdapter.free();
			mImageAdapter = null;
		}
		mImageAdapter = new ImagePreviewAdapter(this, mParam);
		mImageGridView.setAdapter(mImageAdapter);

		changeTheme(themePkg, mData.getmDrawable(), null, null, mArrayList);
	}

	/**
	 * 可支持的图片来源类型
	 * 
	 * @param packageStr
	 * @param drawable
	 * @param folder
	 * @param stringsarray
	 * @param resNames
	 */
	private void changeTheme(String packageStr, Drawable drawable, String folder,
			String[] stringsarray, ArrayList<String> resNames) {
		if (null == mImageAdapter) {
			mImageAdapter = new ImagePreviewAdapter(this, mParam);
		}

		mImageAdapter.freePictures();
		mImageAdapter.initDrawable(drawable);
		mImageAdapter.initFolder(folder);
		mImageAdapter.initResourceStringArray(stringsarray);
		if (ThemeManager.DEFAULT_THEME_PACKAGE_3.equals(packageStr)
				|| ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER.equals(packageStr)) {

			// 解决ADT-7663 UI3.0主题换图标界面，图标显示异常，文件夹图标未放在第一位
			// 如果加载包含文件夹图标时，则将resNames中的文件夹图标先放入Adapter的list中
			// modify by zhengxiangcan 2012.09.03
			if (sFromWhatRequester == USER_FOLDER_STYLE || sFromWhatRequester == DOCK_FOLDER_STYLE) {
				ArrayList<String> tempList = new ArrayList<String>();
				tempList.add(resNames.get(0));
				mImageAdapter.initPackageResourceArray(packageStr, tempList);
				resNames.remove(0);
			}

			//如果是UI3.0图标库，会先加入默认主题图标库
			mImageAdapter.initPackageResourceArray(ThemeManager.DEFAULT_THEME_PACKAGE,
					CustomIconRes.getDefaultResList());
		}
		mImageAdapter.initPackageResourceArray(packageStr, resNames);
		mImageAdapter.start();
	}

	@Override
	public void onClick(View v) {
		// 点击了菜单的一项
		if (v instanceof TextView && v.getTag() != null && v.getTag() instanceof Integer) {
			int position = (Integer) v.getTag();
			selectTheme(position, false);
		} else {
			switch (v.getId()) {

			// 自定义按钮
				case R.id.customBtn :
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					try {
						startActivityForResult(intent, IRequestCodeIds.REQUEST_CHANGE_ICON);
					} catch (Exception e) {
						e.printStackTrace();
						DeskToast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT)
								.show();
					}
					break;

				// 下载按钮
				case R.id.downBtn :
//					Intent intentDown = new Intent();
//					intentDown.setClass(this, GoStore.class);
//					intentDown.putExtra("sort", SortsBean.SORT_THEME + "");
//					startActivity(intentDown);
					AppsManagementActivity.startAppCenter(GoLauncher.getContext(),
							MainViewGroup.ACCESS_FOR_APPCENTER_THEME, false);
					break;

				// 恢复默认图标
				case R.id.resetBtn :
					// 桌面
					if (sFromWhatRequester == SCREEN_STYLE
							|| sFromWhatRequester == USER_FOLDER_STYLE) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_RESET_DEFAULT, -1, null, null);
						finish();
					}
					// DOCK条
					else if (sFromWhatRequester == DOCK_STYLE_FROM_EDIT
							|| sFromWhatRequester == DOCK_FOLDER_STYLE) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.DOCK_RESET_DEFAULT, -1, null, null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SHELL_FRAME,
								IDiyMsgIds.DOCK_RESET_DEFAULT, -1, null, null);
						finish();
					}
					// 文件夹内部图标
					else if (sFromWhatRequester == SCREEN_FOLDER_ITEM_STYLE
							|| sFromWhatRequester == DOCK_FOLDER_ITEM_STYLE) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
								IDiyMsgIds.RESET_DEFAULT_ICON, -1, null, null);
						finish();
					}
					break;

				// 更换主题
				case R.id.changeThemeBtn :
					mChangeThemeMenu.show(mTitleLinearLayout);
					break;
				default :
					break;
			}
		}
	}

	/**
	 * 显示进度条
	 * 
	 * @param index
	 */
	private void showProgressDialog() {
		if (null == mProgressDialog) {
			mProgressDialog = DeskProgressDialog.show(this, null,
					this.getString(R.string.initialization), true);
			mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					return true;
				}
			});
		}
	}

	/**
	 * 关闭进度条
	 */
	private void dismissProgressDialog() {
		if (mProgressDialog != null) {
			try {
				mProgressDialog.dismiss();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mProgressDialog = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (null != mImageAdapter) {
			mImageAdapter.cancel();
			mImageAdapter.free();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		int frameid = sFromWhatRequester;
		switch (requestCode) {
		// 打开裁剪Intent
			case IRequestCodeIds.REQUEST_CHANGE_ICON :
				if (resultCode == Activity.RESULT_OK && null != data) {
					Intent intent = null;
					if (frameid == DOCK_STYLE_FROM_EDIT || frameid == DOCK_FOLDER_STYLE) {
						intent = CustomIconUtil.getCropImageIntent(this, data,
								CustomIconUtil.DOCK_ICON);
					} else if (frameid == SCREEN_STYLE || frameid == USER_FOLDER_STYLE
							|| frameid == SCREEN_FOLDER_ITEM_STYLE
							|| frameid == DOCK_FOLDER_ITEM_STYLE) {
						intent = CustomIconUtil.getCropImageIntent(this, data,
								CustomIconUtil.SCREEN_ICON);
					}
					if (intent != null) {
						startActivityForResult(intent, IRequestCodeIds.REQUEST_CHANGE_CROP_ICON);
					} else {
						finish();
					}

				}
				break;

			// 裁剪完成后发消息通知修改图标
			case IRequestCodeIds.REQUEST_CHANGE_CROP_ICON :
				if (resultCode == Activity.RESULT_OK) {
					String cropFilePath = CustomIconUtil.getCropFilePath(data.getAction());
					Bundle bundle = data.getExtras();
					bundle.putString(ImagePreviewResultType.IMAGE_PATH_STRING, cropFilePath);
					bundle.putInt(ImagePreviewResultType.TYPE_STRING,
							ImagePreviewResultType.TYPE_IMAGE_FILE);

					if (frameid == DOCK_STYLE_FROM_EDIT || frameid == DOCK_FOLDER_STYLE) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME,
								IDiyMsgIds.DOCK_CHANGE_STYLE_APP, -1, bundle, null);
					} else if (frameid == SCREEN_STYLE || frameid == USER_FOLDER_STYLE) {
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.QUICKACTION_EVENT, IQuickActionId.CHANGE_ICON, bundle,
								null);
					} else if (frameid == ChangeIconPreviewActivity.SCREEN_FOLDER_ITEM_STYLE
							|| frameid == ChangeIconPreviewActivity.DOCK_FOLDER_ITEM_STYLE) {
						GoLauncher.sendMessage(this, IDiyFrameIds.DESK_USER_FOLDER_FRAME,
								IDiyMsgIds.QUICKACTION_EVENT, IQuickActionId.CHANGE_ICON, bundle,
								null);
					}
				}
				finish();
				break;
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// 横竖屏切换的时候必须重新选择。否则mImageGridView不会刷新，因为mImageGridView改变了每行显示的个数
		selectTheme(mSelection, true);
	}
	
	public static void setFromWhatRequester(int fromWhatRequester) {
		ChangeIconPreviewActivity.sFromWhatRequester = fromWhatRequester;
	}
}
