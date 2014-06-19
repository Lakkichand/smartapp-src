package com.jiubang.go.backup.pro.image.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.BaseBackupEntry;
import com.jiubang.go.backup.pro.data.BaseEntry;
import com.jiubang.go.backup.pro.data.BaseRestoreEntry;
import com.jiubang.go.backup.pro.model.BackupDBHelper;
import com.jiubang.go.backup.pro.ui.RecordDetailListAdpater;

/**
 * @author jiangpeihe
 *
 */
public class CustomImageAlertDialog {
	private static Dialog sDialog = null;

	public synchronized static void showImageDialog(Context context, final BaseEntry entry,
			final ExpandableListView parent, final View v, final int groupPosition,
			final int childPosition, final RecordDetailListAdpater mAdapter,
			final ExpandableListView mBackupListView, BackupDBHelper backupDBHelper,
			Bitmap defaultBitmap) {
		if (sDialog != null && sDialog.isShowing()) {
			return;
		}
		final BaseAdapter imageDapter;
		final boolean[] selectTypes = initArray(entry);
		View entryView = null;
		View titleView = null;
		LayoutInflater factory = LayoutInflater.from(context);
		entryView = factory.inflate(R.layout.layout_dialog_image_view, null);
		titleView = factory.inflate(R.layout.alertdialogcustomtitle, null);
		TextView folderName = (TextView) titleView.findViewById(R.id.folderName);
		final CheckBox checkBox = (CheckBox) titleView.findViewById(R.id.checkbox);
		if (entry.isSelected()) {
			checkBox.setChecked(true);
		}
		ListView listView = (ListView) entryView.findViewById(R.id.listview);
		imageDapter = getImageAdapter(context, entry, checkBox, listView, backupDBHelper,
				defaultBitmap);
		listView.setAdapter(imageDapter);

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
				if (checkBox.isChecked()) {
					checkBox.setChecked(false);
				} else {
					checkBox.setChecked(true);
				}
			}
		});

		checkBox.setOnCheckedChangeListener(null);
		checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (checkBox.isChecked()) {
					entry.setSelected(true);
					imageDapter.notifyDataSetChanged();
				} else {
					if (entry.isSelected()) {
						entry.setSelected(false);
						imageDapter.notifyDataSetChanged();
					}
				}
			}
		});

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		folderName.setText(entry.getDescription());
		builder.setCustomTitle(titleView).setView(entryView)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						updateAdapterChildItemView(mAdapter, mBackupListView, parent, v,
								groupPosition, childPosition);
						dialog.dismiss();
					}
				}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						cancleOperation(entry, selectTypes);
						dialog.cancel();
					}
				});
		sDialog = builder.create();
		sDialog.setCanceledOnTouchOutside(false);
		sDialog.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				realseResourceByAdapter(entry, imageDapter);
				sDialog = null;
			}
		});
		sDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		sDialog.show();

	}

	protected static void realseResourceByAdapter(BaseEntry entry, BaseAdapter imageDapter) {
		Log.i("TEST", "realseResourceByAdapter" + "-----");
		if (entry instanceof GroupBackupEntry) {
			((ImageBackupAdapter) imageDapter).release();
		} else if (entry instanceof GroupRestoreEntry) {
			((ImageRestoreAdapter) imageDapter).release();
		}
	}

	protected static void updateAdapterChildItemView(RecordDetailListAdpater mAdapter,
			ExpandableListView mBackupListView, ExpandableListView parent, View v,
			int groupPosition, int childPosition) {
		mAdapter.updateAdapterChildItemView(v, groupPosition, childPosition);
		// 更新组视图状态
		long packedPos = ExpandableListView.getPackedPositionForGroup(groupPosition);
		int flatPos = mBackupListView.getFlatListPosition(packedPos);
		int firstVisiblePos = mBackupListView.getFirstVisiblePosition();
		int lastVisiblePos = mBackupListView.getLastVisiblePosition();
		if (flatPos >= firstVisiblePos && flatPos <= lastVisiblePos) {
			final ExpandableListAdapter adapter = parent.getExpandableListAdapter();
			View convertView = mBackupListView.getChildAt(flatPos - firstVisiblePos);
			((RecordDetailListAdpater) adapter).updateAdapterGroupItemView(convertView,
					groupPosition);
		}

	}

	private static boolean[] initArray(BaseEntry entry) {
		int i = 0;
		int imageCount = 0;
		boolean[] selectTypes = null;
		if (entry instanceof GroupBackupEntry) {
			imageCount = ((GroupBackupEntry) entry).getCount();
			selectTypes = new boolean[imageCount];
			for (BaseBackupEntry backupEntry : ((GroupBackupEntry) entry).getEntryList()) {
				selectTypes[i++] = backupEntry.isSelected();
			}
		} else if (entry instanceof GroupRestoreEntry) {
			imageCount = ((GroupRestoreEntry) entry).getCount();
			selectTypes = new boolean[imageCount];
			for (BaseRestoreEntry backupEntry : ((GroupRestoreEntry) entry).getEntryList()) {
				selectTypes[i++] = backupEntry.isSelected();
			}
		}
		return selectTypes;
	}

	protected static void cancleOperation(BaseEntry entry, boolean[] selectTypes) {
		int j = 0;
		if (entry instanceof GroupBackupEntry) {
			for (BaseBackupEntry backupEntry : ((GroupBackupEntry) entry).getEntryList()) {
				boolean orignalSelected = selectTypes[j++];
				if (orignalSelected != backupEntry.isSelected()) {
					backupEntry.setSelected(orignalSelected);
				}
			}
		} else if (entry instanceof GroupRestoreEntry) {
			for (BaseRestoreEntry restoreEntry : ((GroupRestoreEntry) entry).getEntryList()) {
				boolean orignalSelected = selectTypes[j++];
				if (orignalSelected != restoreEntry.isSelected()) {
					restoreEntry.setSelected(orignalSelected);
				}
			}
		}

	}

	private static BaseAdapter getImageAdapter(Context context, BaseEntry entry, CheckBox checkBox,
			ListView listView, BackupDBHelper backupDBHelper, Bitmap defaultBitmap) {
		if (entry instanceof GroupBackupEntry) {
			return new ImageBackupAdapter(context, entry, checkBox, listView, defaultBitmap);
		} else if (entry instanceof GroupRestoreEntry) {
			return new ImageRestoreAdapter(context, entry, checkBox, listView, backupDBHelper,
					defaultBitmap);
		}
		return null;
	}

}
