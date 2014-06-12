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

package com.jiubang.ggheart.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.SysAppInfo;
import com.jiubang.ggheart.data.theme.ThemeManager;

/**
 * 
 *
 */
public class InstallShortcutReceiver extends BroadcastReceiver {
	// public static final String ACTION_INSTALL_SHORTCUT =
	// "com.android.launcher.action.INSTALL_SHORTCUT";
	static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

	@Override
	public void onReceive(Context context, Intent data) {
		if (!ICustomAction.ACTION_INSTALL_SHORTCUT.equals(data.getAction())) {
			return;
		}
		Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		if (null == intent) {
			return;
		}
		String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		if (intent.getAction() == null) {
			intent.setAction(Intent.ACTION_VIEW);
		}

		boolean duplicate = data
				.getBooleanExtra(EXTRA_SHORTCUT_DUPLICATE, true);
		boolean isExist = GoLauncher.sendMessage(this,
				IDiyFrameIds.SCREEN_FRAME,
				IDiyMsgIds.SCREEN_GET_SHORTCUT_ISEXIST, -1, intent, null);
		String addAction = intent.getAction();
		boolean isGoMarket = true;
		// 判断是否应用中心添加桌面快捷方式
		// 因为Go桌面本身会默认有应用中心快捷方式，不需要重复添加
		if (addAction == null || !addAction.equals(AppsManagementActivity.GOMARKET_ICON_ATICON)) {
			isGoMarket = false;
		}

		boolean install = false;
		if ((duplicate || !isExist) && !isGoMarket) {
			// 获取出intent中包含的应用
			final ShortCutInfo info = SysAppInfo.createFromShortcut(context,
					data);
			if (info != null) {
				// 过滤掉桌面主题
				if (info.mIconResource != null) {
					String pkg = info.mIconResource.packageName;
					if (pkg != null
							&& !pkg.startsWith(ThemeManager.MAIN_THEME_PACKAGE)) {
						// 不是桌面主题则将本应用发送给屏幕层进行添加工作
						install = GoLauncher.sendMessage(this,
								IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_ADD_SHORTCUT_COMPLETE, -1,
								info, null);
					}
				} else {
					// 将本应用发送给屏幕层进行添加工作
					install = GoLauncher.sendMessage(this,
							IDiyFrameIds.SCREEN_FRAME,
							IDiyMsgIds.SCREEN_ADD_SHORTCUT_COMPLETE, -1, info,
							null);
				}
			}
		}

		// toast 提示
		// try {
		// if (install) {
		// DeskToast.makeText(context,
		// context.getString(R.string.shortcut_installed, name),
		// Toast.LENGTH_SHORT).show();
		// }
		// else if (!duplicate) {
		// DeskToast.makeText(context,
		// context.getString(R.string.shortcut_duplicate, name),
		// Toast.LENGTH_SHORT).show();
		// }
		// } catch (Exception e) {
		// }
	}
}
