package com.zhidian.wifibox.view;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.jiubang.core.message.IMessageHandler;
import com.ta.TAApplication;
import com.zhidian.wifibox.R;
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
	private Setting setting;
	private ImageView mUpdatePoint;

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
		TAApplication.registMsgHandler(this);
		setting = new Setting(getContext());
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
			// 通知TabManageView导航栏切换
			TAApplication.sendHandler(getContext(), IDiyFrameIds.TABMANAGEVIEW,
					IDiyMsgIds.SWITCH_NAVIGATION, -1, url, null);
			// 跳转完后更新下标和导航栏UI
			if (mCurrentView != null) {
				ImageView image = (ImageView) mCurrentView
						.findViewById(R.id.image);
				image.setImageResource((Integer) (mCurrentView
						.getTag(R.string.app)));
				TextView text = (TextView) mCurrentView.findViewById(R.id.text);
				text.setTextColor(0xFFb2b2b2);
				mCurrentView.setBackgroundDrawable(null);
			}
			{
				ImageView image = (ImageView) v.findViewById(R.id.image);
				image.setImageResource((Integer) (v.getTag(R.string.cancel)));
				TextView text = (TextView) v.findViewById(R.id.text);
				text.setTextColor(0xFFFFFFFF);
				v.setBackgroundColor(0xFF242424);
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
		topic.setTag(R.string.cancel, R.drawable.footer_grey_icon_01_pressd);
		// 应用
		View app = findViewById(R.id.app);
		app.setTag(TabController.NAVIGATIONAPP);
		app.setOnClickListener(mListener);
		app.setTag(R.string.app, R.drawable.footer_grey_icon_02);
		app.setTag(R.string.cancel, R.drawable.footer_grey_icon_02_pressd);
		// 推荐
		View feature = findViewById(R.id.feature);
		feature.setTag(TabController.NAVIGATIONFEATURE);
		feature.setOnClickListener(mListener);
		feature.setTag(R.string.app, R.drawable.footer_grey_icon_03);
		feature.setTag(R.string.cancel, R.drawable.footer_grey_icon_03_pressd);
		// 游戏
		View game = findViewById(R.id.game);
		game.setTag(TabController.NAVIGATIONGAME);
		game.setOnClickListener(mListener);
		game.setTag(R.string.app, R.drawable.footer_grey_icon_04);
		game.setTag(R.string.cancel, R.drawable.footer_grey_icon_04_pressd);
		// 管理
		View manage = findViewById(R.id.manage);
		manage.setTag(TabController.NAVIGATIONMANAGE);
		manage.setOnClickListener(mListener);
		manage.setTag(R.string.app, R.drawable.footer_grey_icon_05);
		manage.setTag(R.string.cancel, R.drawable.footer_grey_icon_05_pressd);
		mUpdatePoint = (ImageView) findViewById(R.id.index_top_point);
		// 更新个数
		int count = setting.getInt(Setting.UPDATE_COUNT);
		if (count > 0) {
			mUpdatePoint.setVisibility(View.VISIBLE);
		} else {
			mUpdatePoint.setVisibility(View.GONE);
		}
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
				}
			}
			break;
		case IDiyMsgIds.SHOW_UPDATE_COUNT: {
			int count = param;
			if (count > 0) {
				mUpdatePoint.setVisibility(View.VISIBLE);
			} else {
				mUpdatePoint.setVisibility(View.GONE);
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
		TAApplication.unRegistMsgHandler(this);
	}

}
