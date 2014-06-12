package com.jiubang.ggheart.appgame.base.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.gau.go.launcherex.R;

/**
 * 启动loading页和加载loading提示的工具类
 * 
 * @author xiedezhi
 * @date [2012-10-25]
 */
public class LoadingTipUtil {

	/**
	 * 获取默认的启动loading页
	 */
	public static View getDefaultStartLoading(Context context) {
		if (context == null) {
			return null;
		}
		LayoutInflater inflater = LayoutInflater.from(context);
		View ret = inflater.inflate(R.layout.appgame_default_start_loading,
				null);
		ProgressBar progressBar = (ProgressBar) ret
				.findViewById(R.id.appgame_default_start_loading_progressbar);
		progressBar.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		Drawable drawable = context.getResources().getDrawable(
				R.drawable.go_progress_green);
		progressBar.setIndeterminateDrawable(drawable);
		return ret;
	}

}
