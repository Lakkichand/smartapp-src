/*
 * 文 件 名:  LauncherSelector.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  rongjinsong
 * 修改时间:  2012-9-11
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.data.DBImport;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.log.LogConstants;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.launcher.ICustomAction;

/**
 * <br>
 * 类描述: <br>
 * 功能详细描述:
 * 
 * @author rongjinsong
 * @date [2012-9-11]
 */
public class LauncherSelectorActivity extends Activity {
	public static boolean sImportDB = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		showLauncherSelector();
	}

	private void showLauncherSelector() {
		Builder builder = new Builder(this);
		builder.setTitle(R.string.desk_migrate_tip_title);
		final LauncherChooseAdapter adapter = new LauncherChooseAdapter();
		builder.setAdapter(adapter, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (which >= 0 && which < adapter.getCount()) {
					String pkg = adapter.getItemPackage(which);
					Intent intent = new Intent(ICustomAction.ACTION_ADD_DB_READ_PERMISSION);
					intent.putExtra("pkg", pkg);
					sImportDB = true;
					sendBroadcast(intent);
				}
			}
		});
		Dialog dialog = builder.create();
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		dialog.show();

	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-14]
 */
	class LauncherChooseAdapter extends BaseAdapter {
		private ArrayList<Item> mItems;

		public LauncherChooseAdapter() {
			PackageManager packageManager = LauncherSelectorActivity.this.getPackageManager();
			Intent intent = new Intent("com.gau.go.launcherex.MAIN");
			List<ResolveInfo> infos = packageManager.queryIntentActivities(intent, 0);
			Iterator iterator = infos.iterator();
			while (iterator.hasNext()) {
				ResolveInfo info = (ResolveInfo) iterator.next();
				File file = new File("/data/data/" + info.activityInfo.packageName + "/shared_prefs/"
						+ IPreferencesIds.DB_PROVIDER_SUPPORT + ".xml");
				if (!file.exists()) {
					iterator.remove();
					continue;
				}
//				Context ctx = AppUtils.getAppContext(LauncherSelectorActivity.this, info.activityInfo.packageName);
//				SharedPreferences sharedPreferences = ctx.getSharedPreferences(
//						IPreferencesIds.DB_PROVIDER_SUPPORT, Context.MODE_WORLD_READABLE);
//				if (!sharedPreferences.getBoolean(IPreferencesIds.IMPORT_SUPPORT, false)) {
//					iterator.remove();
//					continue;
//				}
			}
			int sz = infos.size();
			for (int i = 0; i < sz; i++) {
				try {
					ResolveInfo info = infos.get(i);
					if (null == info) {
						continue;
					}
					String packageStr = info.activityInfo.packageName;
					if (packageStr.equals(LauncherSelectorActivity.this.getPackageName())) {
						continue;
					}
					Drawable icon = info.loadIcon(packageManager);
					if (icon instanceof BitmapDrawable) {
						int iconSz = (int) getResources().getDimension(
								android.R.dimen.app_icon_size);
						icon = resizeImage(((BitmapDrawable) icon).getBitmap(), iconSz, iconSz);
					}
					String title = info.loadLabel(packageManager).toString();
					if (null == mItems) {
						mItems = new ArrayList<Item>();
					}
					Item item = new Item();
					item.mIcon = icon;
					item.mTitle = title;
					item.mPackage = packageStr;
					mItems.add(item);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public String getItemPackage(int position) {
			if (null != mItems) {
				return mItems.get(position).mPackage;
			}
			return null;
		}

		public Uri getItemUri(int position) {
			if (null != mItems) {
				return mItems.get(position).mUri;
			}
			return null;
		}

		@Override
		public int getCount() {
			if (null != mItems) {
				return mItems.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			if (null != mItems) {
				return mItems.get(position);
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null == convertView) {
				LayoutInflater inflater = (LayoutInflater) LauncherSelectorActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.item, null);
			}

			if (null != mItems) {
				TextView textView = (TextView) convertView;
				textView.setText(mItems.get(position).mTitle);
				textView.setCompoundDrawablesWithIntrinsicBounds(mItems.get(position).mIcon, null,
						null, null);
			}
			return convertView;
		}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-14]
 */
		class Item {
			public String mTitle;
			public Drawable mIcon;
			public String mPackage;
			public Uri mUri;
		}
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 * @param w
	 * @param h
	 * @return
	 */
	private Drawable resizeImage(Bitmap bitmap, int w, int h) {
		// load the origial Bitmap
		Bitmap bitmapOrg = bitmap;

		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);

		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return new BitmapDrawable(getResources(), resizedBitmap);
	}
	
	@Override
	public void onBackPressed() {
		try {
			super.onBackPressed();
		} catch (Exception e) {
			Log.e(LogConstants.HEART_TAG, "onBackPressed err " + e.getMessage());
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
