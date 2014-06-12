package com.jiubang.ggheart.appgame.base.component;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.recommend.AppKitsActivity;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;

/**
 * 
 * 应用中心，广告推荐位
 * 
 * @author xiedezhi
 * @date [2013-1-23]
 */
public class AppGameADBanner extends Gallery {

	/**
	 * gallery 默认的item间距
	 */
	private static final int DEFAULT_ITEM_SPACING = 8;

	/**
	 * 是否正在触摸gallery，如果是，不执行自动滚动
	 */
	private boolean mIsTouching = false;
	/**
	 * 自动切换时间间隔
	 */
	private int mScrollInterval = 4000;
	/**
	 * 每隔5秒自动切换banner图
	 */
	private Runnable mLoopRunnable = new Runnable() {

		@Override
		public void run() {
			if (AppGameADBanner.this != null
					&& AppGameADBanner.this.getParent() != null
					&& AppGameADBanner.this.getVisibility() == View.VISIBLE) {
				if (!mIsTouching) {
					onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null);
				}
				removeCallbacks(mLoopRunnable);
				postDelayed(mLoopRunnable, mScrollInterval);
			}
		}
	};

	public AppGameADBanner(Context context) {
		super(context);
		init();
	}

	public AppGameADBanner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AppGameADBanner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		setLongClickable(false);
		// 默认间距
		setSpacing(DrawUtils.dip2px(DEFAULT_ITEM_SPACING));
		// 广告开始轮播
		postDelayed(mLoopRunnable, mScrollInterval);
		// 背景
		setBackgroundResource(R.drawable.adbanner_bg);
		// 点击事件
		setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				postDelayed(new Runnable() {

					@Override
					public void run() {
						Object obj = view.getTag(R.id.appgame);
						if (obj != null && obj instanceof BoutiqueApp) {
							final BoutiqueApp app = (BoutiqueApp) obj;
							int acttype = app.acttype;
							switch (acttype) {
							case 1:// 打开专题应用列表
							{
								// 进入下一级tab栏
								TabController.skipToTheNextTab(app.rid,
										app.name, -1, true, -1, -1, null);
								break;
							}
							case 2:// 打开应用详情
							case 3:
							case 4: {
								// 统计tab点击
								int typeid = app.typeid;
								AppManagementStatisticsUtil.getInstance();
								AppManagementStatisticsUtil.saveTabClickData(
										getContext(), typeid, null);
								int startType = AppsDetail.START_TYPE_APPRECOMMENDED;
								AppsDetail.jumpToDetail(getContext(), app,
										startType, app.index, true);
								break;
							}
							case 5: {
								// 打开一键装机
								AppManagementStatisticsUtil.getInstance();
								AppManagementStatisticsUtil.saveTabClickData(
										getContext(), app.rid, null);
								// 启动一键装机
								Intent intent = new Intent(getContext(),
										AppKitsActivity.class);
								intent.putExtra(AppKitsActivity.ENTRANCE_KEY,
										AppKitsActivity.ENTRANCE_ID_CENTER);
								getContext().startActivity(intent);
							}
							default:
								break;
							}
						}
					}
				}, 350);
			}
		});
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		try {
			return super.onScroll(e1, e2, distanceX, distanceY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return super.onFling(e1, e2, velocityX * 0.15f, velocityY);
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		// 如果看见就postRunnable，不可见就removecallback
		if (visibility == View.VISIBLE) {
			removeCallbacks(mLoopRunnable);
			postDelayed(mLoopRunnable, mScrollInterval);
		} else {
			removeCallbacks(mLoopRunnable);
		}
		super.onVisibilityChanged(changedView, visibility);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsTouching = true;
			removeCallbacks(mLoopRunnable);
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			mIsTouching = false;
			postDelayed(mLoopRunnable, mScrollInterval);
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public void playSoundEffect(int soundConstant) {
		// do nothing
	}
	
	
	@Override
	protected void onDetachedFromWindow() {
		removeCallbacks(mLoopRunnable);
		super.onDetachedFromWindow();
	}
	
	@Override
	protected void onAttachedToWindow() {
		if (this.getVisibility() == View.VISIBLE) {
			removeCallbacks(mLoopRunnable);
			postDelayed(mLoopRunnable, mScrollInterval);
		}
		super.onAttachedToWindow();
	}
	
	/**
	 * 手动切换内容
	 */
	public void showNext() {
		if (mLoopRunnable != null) {
			mLoopRunnable.run();
		}
	}
}
