package com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author rongjinsong
 * 
 */
public class MessageContentBean extends MessageBaseBean {

	public static String sTAG_MSGWIDGETS = "msgwidgets"; // 消息控件数组
	private ArrayList<MessageWidgetBean> mWidgets; // 一条消息包含的元素
	private ArrayList<MessageWidgetBean> mButtons; // 一条消息所附带的按钮

	public MessageContentBean() {
		mWidgets = new ArrayList<MessageWidgetBean>();
		mButtons = new ArrayList<MessageWidgetBean>();
	}

	public void praseWidget(JSONArray msg) {
		mWidgets.clear();
		mButtons.clear();
		int buttonIndex = 0;
		for (int i = 0; i < msg.length(); i++) {
			JSONObject obj = (JSONObject) msg.opt(i);
			MessageWidgetBean bean = null;
			if (obj != null) {
				try {
					String type = obj.getString(MessageWidgetBean.TAG_TYPE);
					if (type.equals(MessageWidgetBean.TYPE_TEXT)) {
						bean = new TextWidgetBean();
						// bean.prase(obj);
					} else if (type.equals(MessageWidgetBean.TYPE_BTN)) {
						bean = new ButtonWidgetBean();
						bean.prase(obj);
						mButtons.add(buttonIndex, bean);
						buttonIndex++;
						continue;
					} else if (type.equals(MessageWidgetBean.TYPE_HERF)) {
						bean = new HrefWidgetBean();
					} else if (type.equals(MessageWidgetBean.TYPE_IMG)) {
						bean = new ImageWidgetBean();
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (bean != null) {
				bean.prase(obj);
				mWidgets.add(bean);
			}
		}
	}
	/**
	 * 功能简述:  构建底部的按钮
	 * 功能详细描述: 根据MessageContent的内容初始化底部的按钮
	 * 注意:
	 * @param view
	 * @param btnListener
	 * @param context
	 */
//	public void initButtonView(View view, OnClickListener btnListener, Context context) {
//		int btnWidgetIndex = 0;
//
//		if (mButtons != null && !mButtons.isEmpty()) {
//			int btnCnt = mButtons.size();
//			LinearLayout linearlayout = null;
//			Button button = null;
//			for (int i = 0; i < mButtons.size(); i++) {
//				MessageWidgetBean bean = mButtons.get(i);
//				if (btnWidgetIndex == 0) {
//					linearlayout = (LinearLayout) view.findViewById(R.id.left_layout);
//					button = (Button) view.findViewById(R.id.left_btn);
//
//				} else if (btnCnt == 3 && btnWidgetIndex == 1) {
//					linearlayout = (LinearLayout) view.findViewById(R.id.middle_layout);
//					button = (Button) view.findViewById(R.id.middle_btn);
//				} else {
//					linearlayout = (LinearLayout) view.findViewById(R.id.right_layout);
//					button = (Button) view.findViewById(R.id.right_btn);
//				}
//				bean.initView(button, null);
//
//				if (bean.mActtype == MessageWidgetBean.ACTTYPE_DOWNLOAD) {
//					//再判断是否存在文件
//					String fileName = LauncherEnv.Path.MESSAGECENTER_PATH + mId + ".apk";
//					File newfile = new File(fileName);
//
//					//bean.mActvaule其实就是消息下载的url链接,
//					if (bean.mActvaule != null) {
//						String[] urlContent = bean.mActvaule.split(MessageBaseBean.URL_SPLIT);
//						String realUrl = urlContent[0];
//						String[] nameContent = urlContent[1].split(MessageBaseBean.URL_SPLIT_NAME);
//						String pkgName = nameContent[0];
//						String appName = nameContent[1];
//
//						if (AppUtils.isAppExist(context, pkgName)) {
//							if (newfile.exists()) {
//								button.setText(context.getString(R.string.message_file_install));
//							}
//						} else {
//							if (newfile.exists()) {
//								button.setText(context.getString(R.string.message_file_not_install));
//							}
//						}
//					}
//				}
//				linearlayout.setVisibility(View.VISIBLE);
//				button.setVisibility(View.VISIBLE);
//				button.setOnClickListener(btnListener);
//				button.setTag(bean);
//				btnWidgetIndex++;
//			}
//
//		}
//	}

	public ArrayList<MessageWidgetBean> getWidgets() {
		return mWidgets;
	}

	public ArrayList<MessageWidgetBean> getButtonWidgets() {
		return mButtons;
	}

	public void recycle() {
		if (mWidgets != null) {
			for (int i = 0; i < mWidgets.size(); i++) {
				MessageWidgetBean bean = mWidgets.get(i);
				bean.recycle();

			}
			mWidgets.clear();
			mWidgets = null;
		}
	}
}
