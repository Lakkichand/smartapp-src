package com.jiubang.ggheart.apps.desks.imagepreview;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.window.WindowControl;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewAdapter.FileImageNode;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewAdapter.IImageNode;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewAdapter.PackageImageNode;
import com.jiubang.ggheart.apps.desks.imagepreview.ImagePreviewAdapter.ResourceImageNode;
import com.jiubang.ggheart.components.DeskActivity;
import com.jiubang.ggheart.components.DeskToast;
import com.jiubang.ggheart.data.theme.ThemeManager;

/**
 * 说明：图片预览宫格Activity
 * 
 * 进入Intent参数说明： "bitmap" : bitmap 图片 （小于100k） "idarray" : int[] 图片ID数组 "folder"
 * : String 扫描文件夹 "package" : String 主题名 "resnamearray" : ArrayList<String>
 * 
 * "gridparam" : ImageGridParam 宫格参数 "nostatusbar" : boolean 隐藏状态栏
 * 
 * 出去Intent参数说明 "type" : int 0: 代表资源ID 1：代表文件路径 2. 默认图标 "imageid" : int
 * 选择的图片对应ID "imagepackage" : String 选择的图片包名 "imagepath" : String 选择的图片路径
 * 
 * @author masanbing & jiangxuwen
 * 
 */
public class ImagePreviewActivity extends DeskActivity {
	protected ImageGridView mImageGridView;
	protected ImagePreviewAdapter mImageAdapter;
	protected ImageGridParam mParam;

	public static final String PACKAGE_ARRAY = "packages";
	public static final String NAME_ARRAY = "resNames";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.select_icon);

		Intent intent = getIntent();
		if (null != intent) {
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				// 状态栏
				boolean hideStatusBar = bundle.getBoolean("nostatusbar");
				ArrayList<String> packageStrings = bundle.getStringArrayList(PACKAGE_ARRAY);
				ArrayList<String> resNameList = bundle.getStringArrayList(NAME_ARRAY);
				if (hideStatusBar) {
					WindowControl.setIsFullScreen(this, true);
				} else {
					WindowControl.setIsFullScreen(this, false);
				}
				// 初始化配置
				initmParam();
				// 适配器
				mImageAdapter = new ImagePreviewAdapter(this, mParam);
				mImageAdapter.initPackageResourceArrayInAllPacksges(packageStrings, resNameList);

				mImageAdapter.start();
				mImageAdapter.notifyDataSetInvalidated();

				// 宫格
				mImageGridView = new ImageGridView(this, mParam);
				mImageGridView.setAdapter(mImageAdapter);
				// 监听
				mImageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						if (mImageAdapter.getmDrawablesSize() <= position) {
							return;
						}
						int nodeIndex = mImageAdapter.getNodeIdFromPosition(position);
						IImageNode node = (IImageNode) parent.getAdapter().getItem(nodeIndex);
						// IImageNode node =
						// (IImageNode)parent.getAdapter().getItem(position);
						if (null != node) {
							Intent intent = new Intent();
							Bundle bundle = new Bundle();
							if (node instanceof ImagePreviewAdapter.FileImageNode) {
								bundle.putInt(ImagePreviewResultType.TYPE_STRING,
										ImagePreviewResultType.TYPE_IMAGE_FILE);
								bundle.putString(ImagePreviewResultType.IMAGE_PATH_STRING,
										((FileImageNode) node).getFilePath());
							} else if (node instanceof ImagePreviewAdapter.ResourceImageNode) {
								// 原来版本保存ID
								// bundle.putInt(ImagePreviewResultType.TYPE_STRING,
								// ImagePreviewResultType.TYPE_RESOURCE_ID);
								// bundle.putInt(ImagePreviewResultType.IMAGE_ID_STRING,
								// ((ResourceImageNode)node).getResourceId());

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
								bundle.putInt(ImagePreviewResultType.TYPE_STRING,
										ImagePreviewResultType.TYPE_PACKAGE_RESOURCE);
								bundle.putString(ImagePreviewResultType.IMAGE_PACKAGE_NAME,
										((PackageImageNode) node).getPackageName());
								bundle.putString(ImagePreviewResultType.IMAGE_PATH_STRING,
										((PackageImageNode) node).getPackageResName());
								Log.v("System.out.print",
										"Package=" + ((PackageImageNode) node).getPackageName()
												+ "  resName = "
												+ ((PackageImageNode) node).getPackageResName());
							}
							intent.putExtras(bundle);
							setResult(RESULT_OK, intent);
						}
						finish();
					}
				});

				// 添加背景模糊
				mImageGridView.setBackgroundColor(0xB31f1f1f);
				// 设置显示
				setContentView(mImageGridView);
				// 无数据提示
				if (mImageAdapter.getCount() <= 0) {
					Toast t = DeskToast
							.makeText(this, R.string.imagepreviewtip, Toast.LENGTH_SHORT);
					t.show();
				}
			}
		}

		if (null == mImageGridView) {
			// TODO LOG 解析Intent出错
			finish();
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

	public void setAdapter(ImagePreviewAdapter adapter) {
		if (null != mImageGridView) {
			mImageAdapter = adapter;
			mImageGridView.setAdapter(adapter);
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

	// @Override
	// public void onConfigurationChanged(Configuration newConfig)
	// {
	// super.onConfigurationChanged(newConfig);
	//
	// // mImageGridView.setAdapter(mImageAdapter);
	// // mImageGridView.
	// }
}
