package com.jiubang.ggheart.apps.systemwidget;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageActivity;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemeManageView;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * 
 * <br>类描述:桌面系统widget广播接收器
 * <br>功能详细描述:用于处理widget广播事件
 * 
 * @author  zhengxiangcan
 * @date  [2012-11-21]
 */
public class GoLauncherexWidgetProvider extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		final int length = appWidgetIds.length;
		// Perform this loop procedure for each App Widget that belongs to this provider  
		for (int i = 0; i < length; i++) {
			int appWidgetId = appWidgetIds[i];
			// Create an Intent to launch ThemeManageActivity  
			Intent intent = new Intent(context, ThemeManageActivity.class);
			intent.putExtra("entrance", ThemeManageView.LAUNCHER_THEME_VIEW_ID);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0);
			// Get the layout for the App Widget and attach an on-click listener to the button  
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.launcherex_widget);
			views.setOnClickPendingIntent(R.id.launcherex_widget, pendingIntent);
			// Tell the AppWidgetManager to perform an update on the current App Widget  
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
}
