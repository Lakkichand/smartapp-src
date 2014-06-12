/*
 * 文 件 名:  ButtonUtils.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-13
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.utils;

import java.util.Locale;

import android.widget.Button;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 类描述:由于邓总决定的UI设计原因，使用按钮显示“下载”等字体的时候，英文或者某些显示不全，因此Button的文本大小等信息需要根据语言重新设置，
 * 并且多个container都会用到，故将方法提至一个类里，修改方便 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-13]
 */
public class ButtonUtils {
	public static void setButtonTextSize(Button button) {
		String language = Locale.getDefault().getLanguage();
		String title = button.getResources().getString(
				R.string.themestore_title_apps);
		if (language.equals("zh") && isZH(title)) {
			button.setTextSize(GOLauncherApp.getApplication().getResources()
					.getDimension(R.dimen.appgame_textSize_cn));
		} else {
			button.setTextSize(GOLauncherApp.getApplication().getResources()
					.getDimension(R.dimen.appgame_textSize_es));
		}
	}

	/**
	 * 功能简述:判断字符是否为中文
	 * 
	 * @param msg
	 *            需要判读的字符
	 * @return 传入的字符串都为中文返回true，否则返回false
	 */
	public static boolean isZH(String msg) {
		if (msg == null || msg.trim().equals("")) {
			return false;
		}
		return msg.matches("[\\u4E00-\\u9FA5]+");
	}
}