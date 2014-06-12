/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.LiveFolders;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.gau.go.launcherex.R;
import com.jiubang.core.message.IMessageHandler;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.info.ScreenFolderInfo;
import com.jiubang.ggheart.data.info.ScreenLiveFolderInfo;

/**
 * live folder
 * 
 * @author luopeihuan
 * 
 */
public class LiveFolder extends FolderView {
	private AsyncTask<ScreenLiveFolderInfo, Void, Cursor> mLoadingTask;

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            xml属性
	 */
	public LiveFolder(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public static LiveFolder fromXml(Context context, ScreenFolderInfo folderInfo) {
		final int layout = R.layout.live_folder_list;
		// final int layout = isDisplayModeList(folderInfo) ?
		// R.layout.live_folder_list : R.layout.live_folder_grid;
		return (LiveFolder) LayoutInflater.from(context).inflate(layout, null);
	}

	private static boolean isDisplayModeList(ScreenFolderInfo folderInfo) {
		return ((ScreenLiveFolderInfo) folderInfo).mDisplayMode == LiveFolders.DISPLAY_MODE_LIST;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onItemClick(AdapterView parent, View v, int position, long id) {
		if (v == null || v.getTag() == null
				|| !(v.getTag() instanceof LiveFolderAdapter.ViewHolder)) {
			return;
		}
		LiveFolderAdapter.ViewHolder holder = (LiveFolderAdapter.ViewHolder) v.getTag();
		if (holder.useBaseIntent) {
			final Intent baseIntent = ((ScreenLiveFolderInfo) mInfo).mBaseIntent;
			if (baseIntent != null) {
				final Intent intent = new Intent(baseIntent);
				Uri uri = baseIntent.getData();
				uri = uri.buildUpon().appendPath(Long.toString(holder.id)).build();
				intent.setData(uri);
				// set bound
				Rect targetRect = new Rect();
				v.getGlobalVisibleRect(targetRect);
				try {
					// TODO APIlevel不允许
					// intent.setSourceBounds(targetRect);
				} catch (NoSuchMethodError e) {
				}

				// 启动程序
				if (mMessageHandler != null) {
					mMessageHandler.handleMessage(this, IMsgType.SYNC,
							IDiyMsgIds.SCREEN_FOLDER_EVENT, IScreenFolder.START_ACTIVITY, intent,
							null);
				}
			}
		} else if (holder.intent != null) {
			Rect targetRect = new Rect();
			v.getGlobalVisibleRect(targetRect);
			try {
				// TODO APIlevel不允许
				// holder.intent.setSourceBounds(targetRect);
			} catch (NoSuchMethodError e) {
			}
			// 启动程序
			if (mMessageHandler != null) {
				mMessageHandler.handleMessage(this, IMsgType.SYNC, IDiyMsgIds.SCREEN_FOLDER_EVENT,
						IScreenFolder.START_ACTIVITY, holder.intent, null);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		return false;
	}

	@Override
	public void bind(ScreenFolderInfo info) {
		super.bind(info);
		if (mLoadingTask != null && mLoadingTask.getStatus() == AsyncTask.Status.RUNNING) {
			mLoadingTask.cancel(true);
		}
		mLoadingTask = new FolderLoadingTask(this).execute((ScreenLiveFolderInfo) info);
	}

	@Override
	public void onOpen() {
		super.onOpen();
		requestFocus();
	}

	@Override
	void onClose() {
		super.onClose();
		if (mLoadingTask != null && mLoadingTask.getStatus() == AsyncTask.Status.RUNNING) {
			mLoadingTask.cancel(true);
		}

		// The adapter can be null if onClose() is called before
		// FolderLoadingTask
		// is done querying the provider
		final LiveFolderAdapter adapter = (LiveFolderAdapter) mContent.getAdapter();
		if (adapter != null) {
			adapter.cleanup();
		}
	}

	static class FolderLoadingTask extends AsyncTask<ScreenLiveFolderInfo, Void, Cursor> {
		private final WeakReference<LiveFolder> mFolder;
		private ScreenLiveFolderInfo mInfo;

		FolderLoadingTask(LiveFolder folder) {
			mFolder = new WeakReference<LiveFolder>(folder);
		}

		@Override
		protected Cursor doInBackground(ScreenLiveFolderInfo... params) {
			final LiveFolder folder = mFolder.get();
			if (folder != null) {
				mInfo = params[0];
				if (folder.mMessageHandler != null) {
					ArrayList<Cursor> cursors = new ArrayList<Cursor>();
					folder.mMessageHandler.handleMessage(this, IMsgType.SYNC,
							IDiyMsgIds.SCREEN_FOLDER_EVENT, IScreenFolder.QUERY_CURSOR, mInfo,
							cursors);

					if (cursors.size() > 0) {
						return cursors.get(0);
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Cursor cursor) {
			if (!isCancelled()) {
				if (cursor != null) {
					final LiveFolder folder = mFolder.get();
					if (folder != null) {
						final IMessageHandler msgHandler = folder.mMessageHandler;
						final Context context = folder.getContext();
						folder.setContentAdapter(new LiveFolderAdapter(msgHandler, context, mInfo,
								cursor));
					}
				}
			} else if (cursor != null) {
				cursor.close();
			}
		}
	}
}
