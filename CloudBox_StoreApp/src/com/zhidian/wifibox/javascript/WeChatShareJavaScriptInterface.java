package com.zhidian.wifibox.javascript;

import com.ta.TAApplication;
import com.zhidian.wifibox.data.WeChatShareBean;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.view.dialog.ShareWeChatPopupWindow;
import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * 微信分享Javascript接口
 * 
 * @author zhaoyl
 * 
 */
public class WeChatShareJavaScriptInterface {

	private Activity activity;
	private View parent;

	public WeChatShareJavaScriptInterface(Activity activity, View parent) {
		this.activity = activity;
		this.parent = parent;
	}

	/**
	 * 
	 * @param title
	 *            标题
	 * @param desc
	 *            描述
	 * @param img_url
	 *            图片连接
	 * @param link
	 *            跳转地址
	 */
	@JavascriptInterface
	public void weChatShareOnAndroid(String title, String desc, String img_url,
			String link) {
		//先判断是否安装有微信
		boolean result = AppUtils.isInstallWx(TAApplication.getApplication(), AppUtils.WX_PACKAGE_NAME);
		if (result) {
			// 先弹出一个对话框

			WeChatShareBean bean = new WeChatShareBean();
			bean.title = title;
			bean.desc = desc;
			bean.link = link;
			bean.img_url = img_url;

			ShareWeChatPopupWindow popupWindow = new ShareWeChatPopupWindow(
					activity, bean);
			popupWindow.showAtLocation(parent, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, 0);
		}else {
			Toast.makeText(TAApplication.getApplication(), "未安装有微信", Toast.LENGTH_SHORT).show();
		}
		

	}
}
