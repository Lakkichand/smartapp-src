package com.zhidian.wifibox.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubang.core.message.IMessageHandler;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.Setting;

/**
 * 首层底部导航栏
 * 
 * @author xiedezhi
 * 
 */
public class NavigationBar extends FrameLayout implements IMessageHandler {
	/**
	 * 当前被选中的view
	 */
	private View mCurrentView;
	private TextView updateCount;
	private Setting setting;
	private boolean isShow;

	public NavigationBar(Context context) {
		super(context);
		init();
	}

	public NavigationBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public NavigationBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		// 注册消息组件
		MainActivity.registMsgHandler(this);
		setting = new Setting(getContext());
		isShow = setting.getBoolean(Setting.FIRST_UPDATE_CONTENT); 
	}

	/**
	 * 导航点击事件
	 */
	private OnClickListener mListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mCurrentView == v) {
				return;
			}
			String url = (String) v.getTag();
			if (url == TabController.NAVIGATIONMANAGE && (!isShow)) {
				updateCount.setVisibility(View.GONE);
			}
			// 通知TabManageView导航栏切换
			MainActivity.sendHandler(getContext(), IDiyFrameIds.TABMANAGEVIEW,
					IDiyMsgIds.SWITCH_NAVIGATION, -1, url, null);
			// 跳转完后更新下标和导航栏UI
			if (mCurrentView != null) {
				ImageView image = (ImageView) mCurrentView
						.findViewById(R.id.image);
				image.setImageResource((Integer) (mCurrentView
						.getTag(R.string.app)));
				TextView text = (TextView) mCurrentView.findViewById(R.id.text);
				text.setTextColor((Integer) (mCurrentView.getTag(R.id.app)));
				mCurrentView.setBackgroundDrawable(null);
			}
			{
				ImageView image = (ImageView) v.findViewById(R.id.image);
				image.setImageResource((Integer) (v.getTag(R.string.cancel)));
				TextView text = (TextView) v.findViewById(R.id.text);
				text.setTextColor((Integer) (v.getTag(R.id.back)));
				v.setBackgroundResource(R.drawable.footer_botton_bg);
			}
			mCurrentView = v;
		}
	};

	@Override
	protected void onFinishInflate() {
		// 专题
		View topic = findViewById(R.id.topic);
		// URL
		topic.setTag(TabController.NAVIGATIONTOPIC);
		// 下标
		topic.setOnClickListener(mListener);
		topic.setTag(R.string.app, R.drawable.footer_grey_icon_01);
		topic.setTag(R.string.cancel, R.drawable.footer_blue_icon_01);
		topic.setTag(R.id.app, 0xFFa1a1a1);
		topic.setTag(R.id.back, 0xFFFFFFFF);
		// 应用
		View app = findViewById(R.id.app);
		app.setTag(TabController.NAVIGATIONAPP);
		app.setOnClickListener(mListener);
		app.setTag(R.string.app, R.drawable.footer_grey_icon_02);
		app.setTag(R.string.cancel, R.drawable.footer_blue_icon_02);
		app.setTag(R.id.app, 0xFFa1a1a1);
		app.setTag(R.id.back, 0xFFFFFFFF);
		// 推荐
		View feature = findViewById(R.id.feature);
		feature.setTag(TabController.NAVIGATIONFEATURE);
		feature.setOnClickListener(mListener);
		feature.setTag(R.string.app, R.drawable.footer_grey_icon_03);
		feature.setTag(R.string.cancel, R.drawable.footer_blue_icon_03);
		feature.setTag(R.id.app, 0xFFa1a1a1);
		feature.setTag(R.id.back, 0xFFFFFFFF);
		// 游戏
		View game = findViewById(R.id.game);
		game.setTag(TabController.NAVIGATIONGAME);
		game.setOnClickListener(mListener);
		game.setTag(R.string.app, R.drawable.footer_grey_icon_04);
		game.setTag(R.string.cancel, R.drawable.footer_blue_icon_04);
		game.setTag(R.id.app, 0xFFa1a1a1);
		game.setTag(R.id.back, 0xFFFFFFFF);
		// 管理
		View manage = findViewById(R.id.manage);
		manage.setTag(TabController.NAVIGATIONMANAGE);
		manage.setOnClickListener(mListener);
		manage.setTag(R.string.app, R.drawable.footer_grey_icon_05);
		manage.setTag(R.string.cancel, R.drawable.footer_blue_icon_05);
		manage.setTag(R.id.app, 0xFFa1a1a1);
		manage.setTag(R.id.back, 0xFFFFFFFF);
		updateCount = (TextView) findViewById(R.id.update_count);
		if (!isShow) {
			updateCount.setVisibility(View.VISIBLE);
			setting.putBoolean(Setting.FIRST_UPDATE_CONTENT, true);
		} else {
			updateCount.setVisibility(View.GONE);
		}
		// 极速模式新品推荐
		View xFeature = findViewById(R.id.xnew);
		xFeature.setTag(TabController.XNAVIGATIONNEW);
		xFeature.setOnClickListener(mListener);
		xFeature.setTag(R.string.app, R.drawable.xnew);
		xFeature.setTag(R.string.cancel, R.drawable.xnew_light);
		xFeature.setTag(R.id.app, 0xFF9b9b9b);
		xFeature.setTag(R.id.back, 0xFF35ac1f);
		// 极速模式装机必备
		View xMust = findViewById(R.id.xmust);
		xMust.setTag(TabController.XNAVIGATIONMUST);
		xMust.setOnClickListener(mListener);
		xMust.setTag(R.string.app, R.drawable.xmust);
		xMust.setTag(R.string.cancel, R.drawable.xmust_light);
		xMust.setTag(R.id.app, 0xFF9b9b9b);
		xMust.setTag(R.id.back, 0xFF35ac1f);
		// 极速模式全部
		View xAll = findViewById(R.id.xall);
		xAll.setTag(TabController.XNAVIGATIONALL);
		xAll.setOnClickListener(mListener);
		xAll.setTag(R.string.app, R.drawable.xall);
		xAll.setTag(R.string.cancel, R.drawable.xall_light);
		xAll.setTag(R.id.app, 0xFF9b9b9b);
		xAll.setTag(R.id.back, 0xFF35ac1f);

		adjust();

	}

	/**
	 * 根据当前网络模式决定显示哪一个布局，但跳转还是由MainViewGroup发消息过来才跳
	 */
	public void adjust() {
		View c = findViewById(R.id.common_model_layout);
		View x = findViewById(R.id.x_model_layout);
		if (ModeManager.getInstance().isRapidly()) {
			c.setVisibility(View.GONE);
			x.setVisibility(View.VISIBLE);
		} else {
			c.setVisibility(View.VISIBLE);
			x.setVisibility(View.GONE);
		}
	}

	/**
	 * 网络模式发生改变
	 */
	public void onModeChange() {
		// 根据当前网络模式决定显示哪一个布局，但跳转还是由MainViewGroup发消息过来才跳
		adjust();
	}

	@Override
	public int getId() {
		return IDiyFrameIds.NAVIGATIONBAR;
	}

	@Override
	public boolean handleMessage(Object who, int type, final int msgId,
			final int param, final Object object, final List objects) {
		switch (msgId) {
		case IDiyMsgIds.NAV_SWITCH_NAVIGATION:
			if (object instanceof String) {
				String url = (String) object;
				if (url.equals(TabController.NAVIGATIONTOPIC)) {
					findViewById(R.id.topic).performClick();
				} else if (url.equals(TabController.NAVIGATIONAPP)) {
					findViewById(R.id.app).performClick();
				} else if (url.equals(TabController.NAVIGATIONFEATURE)) {
					findViewById(R.id.feature).performClick();
				} else if (url.equals(TabController.NAVIGATIONGAME)) {
					findViewById(R.id.game).performClick();
				} else if (url.equals(TabController.NAVIGATIONMANAGE)) {
					findViewById(R.id.manage).performClick();
				} else if (url.equals(TabController.XNAVIGATIONMUST)) {
					findViewById(R.id.xmust).performClick();
				} else if (url.equals(TabController.XNAVIGATIONNEW)) {
					findViewById(R.id.xnew).performClick();
				} else if (url.equals(TabController.XNAVIGATIONALL)) {
					findViewById(R.id.xall).performClick();
				}
			}
			break;
		case IDiyMsgIds.SHOW_UPDATE_COUNT: {
			int count = param;
			if (isShow) {
				if (count > 0) {
					updateCount.setVisibility(View.VISIBLE);
					updateCount.setText(count + "");
				} else {
					updateCount.setVisibility(View.GONE);
				}
			}
			
			break;
		}
		default:
			break;
		}
		return false;
	}

	/**
	 * activity onDestory
	 */
	public void onDestory() {
		MainActivity.unRegistMsgHandler(this);
	}

}
