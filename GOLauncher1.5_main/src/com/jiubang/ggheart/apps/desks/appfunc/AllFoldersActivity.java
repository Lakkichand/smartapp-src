package com.jiubang.ggheart.apps.desks.appfunc;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncFolderInfoToDesk;
import com.jiubang.ggheart.components.DeskActivity;
import com.jiubang.ggheart.components.DeskTextView;
import com.jiubang.ggheart.data.info.FunAppItemInfo;
import com.jiubang.ggheart.data.info.FunFolderItemInfo;
import com.jiubang.ggheart.data.info.ScreenStyleConfigInfo;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * todo
 *
 */
public class AllFoldersActivity extends DeskActivity
		implements
			OnItemClickListener,
			OnClickListener {
	private volatile ArrayList<FunFolderItemInfo> mList;
	private FunControler mController;
	private AppFuncThemeController mThemeController;
	private MyAdapter mAdapter;
	private LayoutInflater mInflater;
	private DeskTextView mTipsView;
	private ListView mListView;
	private int mTotal;
	private int mUsed;
	private ArrayList<Boolean> mBooleanList;
	private String mString;

	// 文件夹图标缓存
	private HashMap<Long, SoftReference<BitmapDrawable>> mIconCache = new HashMap<Long, SoftReference<BitmapDrawable>>();
	private int mIconSize;
	private Paint mPaint = new Paint();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appfunc_folderlist);
		setTitle(R.string.appfunc_folderlist);

		final Resources resources = getResources();
		mIconSize = resources.getDimensionPixelSize(R.dimen.inner_desk_folder_icon_size);
		mPaint.setFilterBitmap(true);
		mPaint.setAntiAlias(true);

		mInflater = LayoutInflater.from(this);
		mTipsView = (DeskTextView) findViewById(R.id.appfunc_folder_tips);
		mController = AppFuncFrame.getFunControler();

		if (mController == null || mController.isHandling()) {
			// 如果功能表还未初始化完成
			mTipsView.setText(R.string.appfunc_uninitialized);
		} else {
			Intent data = getIntent();
			if (null != data) {
				mTotal = data.getIntExtra(AppFuncConstants.FOLDERCOUNT, 1);
			} else {
				mTotal = 1;
			}

			mUsed = 0;
			mString = resources.getString(R.string.homescreen_available);
			refreshTips();
			mThemeController = AppFuncFrame.getThemeController();
			initList();
			
			mListView = (ListView) findViewById(R.id.folder_list);
			mAdapter = new MyAdapter(this);
			mListView.setAdapter(mAdapter);
			mListView.requestFocus();
			mListView.setOnItemClickListener(this);
		}

		Button ok = (Button) findViewById(R.id.ok);
		ok.setOnClickListener(this);
		Button cancle = (Button) findViewById(R.id.cancel);
		cancle.setOnClickListener(this);
	}

	/**
	 * 刷新提示语句
	 */
	private void refreshTips() {
		if (mTipsView != null) {
			mTipsView.setText(mString + " " + mUsed + "/" + mTotal);
		}
	}

	private void showWarning() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.dlg_promanageTitle));
		builder.setMessage(getString(R.string.homescreen_full_warning));
		builder.setPositiveButton(getString(R.string.ok), null);
		// builder.setNegativeButton(getString(R.string.cancel), null);
		builder.show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		clearCackedIcon();
		if (mList != null) {
			mList.clear();
			mList = null;
		}
		if (mTipsView != null && mTipsView instanceof DeskTextView) {
			mTipsView.selfDestruct();
			mTipsView = null;
		}
	}

	private void initList() {
		mList = mController.getFunFolders();
		if (mBooleanList == null) {
			mBooleanList = new ArrayList<Boolean>();
		}

		mBooleanList.clear();
		int size = mList.size();
		for (int i = 0; i < size; i++) {
			mBooleanList.add(false);
		}
	}

	private ArrayList<AppFuncFolderInfoToDesk> constructToDeskList() {
		ArrayList<AppFuncFolderInfoToDesk> list = null;
		if (mList != null && mBooleanList != null) {
			int size = mBooleanList.size();
			for (int i = 0; i < size; i++) {
				if (mBooleanList.get(i) && mList.get(i) != null) {
					AppFuncFolderInfoToDesk info = new AppFuncFolderInfoToDesk(mList.get(i));
					if (list == null) {
						list = new ArrayList<AppFuncFolderInfoToDesk>();
					}
					list.add(info);
				}
			}
		}
		return list;
	}
	/**
	 * 
	 * @author todo
	 *
	 */
	private class MyAdapter extends BaseAdapter {
		public MyAdapter(Context context) {
		}

		@Override
		public int getCount() {
			if (mList == null) {
				return 0;
			} else {
				return mList.size();
			}
		}

		@Override
		public Object getItem(int position) {
			if (mList == null) {
				return null;
			} else {
				return mList.get(position);
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			FunFolderItemInfo appItemInfo = null;
			if (mList != null) {
				appItemInfo = mList.get(position);
				if (convertView == null) {
					view = mInflater.inflate(R.layout.multi_choice_item, null);
				} else {
					view = convertView;
				}

				if (appItemInfo != null) {
					CheckBox lockCheckBox = (CheckBox) view.findViewById(R.id.checkbox);

					// 合成文件夹缩略图
					BitmapDrawable icon = getCacheIcon(appItemInfo);
					TextView title = (TextView) view.findViewById(R.id.label);
					title.setText(appItemInfo.getTitle());
					title.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
					DeskSettingConstants.setTextViewTypeFace(title);
					if (mBooleanList.get(position)) {
						lockCheckBox.setChecked(true);
					} else {
						lockCheckBox.setChecked(false);
					}
				}

			}
			return view;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 检查屏幕翻转设置，并应用
		OrientationControl.setOrientation(this);
	}

	private BitmapDrawable getCacheIcon(FunFolderItemInfo folderInfo) {
		final long folderId = folderInfo.getFolderId();
		SoftReference<BitmapDrawable> drawableRef = mIconCache.get(folderId);
		BitmapDrawable drawable = null;
		boolean exist = false;
		if (drawableRef != null) {
			drawable = drawableRef.get();
			if (drawable != null) {
				exist = true;
			}
		}

		if (!exist) {
			drawable = new BitmapDrawable(getResources(), createFolderIcon(folderInfo));
			mIconCache.remove(folderId);
			mIconCache.put(folderId, new SoftReference<BitmapDrawable>(drawable));
		}
		return drawable;
	}

	/**
	 * 创建文件夹缩略图
	 * 
	 * @param folderInfo
	 * @return
	 */
	private Bitmap createFolderIcon(FunFolderItemInfo folderInfo) {
		if (folderInfo == null) {
			return null;
		}
		// 取文件夹底图
		try {
			ScreenStyleConfigInfo screenStyleConfigInfo = GOLauncherApp.getSettingControler()
					.getScreenStyleSettingInfo();
			if (null == screenStyleConfigInfo) {
				return null;
			}
			String themePackage = screenStyleConfigInfo.getFolderStyle();
			Bitmap copy = ((BitmapDrawable) mThemeController.getDrawable(
					mThemeController.getThemeBean().mFoldericonBean.mFolderIconBottomPath,
					themePackage, false)).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
			int width = copy.getWidth();
			int height = copy.getHeight();
			Canvas canvas = new Canvas(copy);
			Matrix matrix = new Matrix();
			// 画缩略图
			ArrayList<FunAppItemInfo> appList = folderInfo.getFunAppItemInfosForShow();
			Iterator<FunAppItemInfo> iterator = appList.iterator();
			int count = 0;
			int curColunm = 0;
			int row = 0;
			while (iterator.hasNext() && count < AppFuncConstants.SHOW_ICON_SIZE) {
				FunAppItemInfo next = iterator.next();
				BitmapDrawable icon = next.getAppItemInfo().getIcon();
				if (icon != null && icon.getBitmap() != null
						&& icon.getBitmap().isRecycled() == false) {
					Bitmap bitmap = icon.getBitmap();
					float w = bitmap.getWidth() + 0.0f;
					float h = bitmap.getHeight() + 0.0f;
					float left = (width - mIconSize * 2 - width * 0.03f) / 2 + curColunm
							* (mIconSize + width * 0.03f);
					float top = height * 0.12f + row * (mIconSize + width * 0.03f);

					matrix.reset();
					matrix.postScale(mIconSize / w, mIconSize / h);
					matrix.postTranslate(left, top);
					canvas.drawBitmap(bitmap, matrix, mPaint);
					curColunm++;
					if (curColunm >= 2) {
						curColunm = 0;
						row++;
					}
				}
				count++;
			}
			// 画罩子
			BitmapDrawable top = (BitmapDrawable) mThemeController.getDrawable(
					mThemeController.getThemeBean().mFoldericonBean.mFolderIconTopClosedPath,
					themePackage, false);
			if (top != null && top.getBitmap() != null && top.getBitmap().isRecycled() == false) {
				canvas.drawBitmap(top.getBitmap(), 0, 0, mPaint);
			}

			return copy;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			System.gc();
		}
		return null;
	}

	/**
	 * 清空合成的图片资源
	 */
	public void clearCackedIcon() {
		mIconCache.entrySet();
		Iterator<Entry<Long, SoftReference<BitmapDrawable>>> iterator = mIconCache.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<Long, SoftReference<BitmapDrawable>> entry = iterator.next();
			SoftReference<BitmapDrawable> drawableRef = entry.getValue();
			BitmapDrawable bitmapDrawable = drawableRef.get();
			if (bitmapDrawable != null) {
				bitmapDrawable.setCallback(null);
				Bitmap bmp = bitmapDrawable.getBitmap();
				if (bmp != null && !bmp.isRecycled()) {
					bmp.recycle();
				}
			}
		}
		mIconCache.clear();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		CheckBox lockCheckBox = (CheckBox) view.findViewById(R.id.checkbox);
		if (lockCheckBox.isChecked()) {
			mBooleanList.remove(position);
			mBooleanList.add(position, Boolean.FALSE);
			lockCheckBox.setChecked(false);
			mUsed--;
		} else {
			if (mUsed == mTotal) {
				// 弹出提示框
				showWarning();
			} else {
				mBooleanList.remove(position);
				mBooleanList.add(position, Boolean.TRUE);
				lockCheckBox.setChecked(true);
				mUsed++;
			}
		}
		refreshTips();

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
			case R.id.ok :
				// 组装文件夹数据结构发给桌面
				if (mList != null) {
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList(AppFuncConstants.FOLDERINFOLIST,
							constructToDeskList());
					intent.putExtras(bundle);
					setResult(RESULT_OK, intent);
				} else {
					setResult(RESULT_OK);
				}
				finish();
				break;

			case R.id.cancel :
				setResult(RESULT_CANCELED);
				finish();
				break;

			default :
				break;
		}
	}
}
