package com.jiubang.ggheart.apps.desks.diy.frames.dock.DefaultStyle.autofit;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gau.go.launcherex.R;
import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.core.message.IMsgType;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.drag.DragFrame;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenUtils;
import com.jiubang.ggheart.apps.desks.dock.DockUtil;
import com.jiubang.ggheart.data.info.ShortCutInfo;

/**
 * 
 * <br>类描述:Dock条添加图标层
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-11-13]
 */
public class DockAddIconFrame extends AbstractFrame implements OnAddIconClickListner {
	public RelativeLayout mContentView;	//总的布局
	public RelativeLayout mAllLayout;	//背景遮照层布局
	public LinearLayout mCoverLayout;	//遮照层
	public LinearLayout mContentLayout;	//内容层
	
	public boolean mIsHideStatusbar = false;	//通知栏是否隐藏
	public int mBlankIndexInRow;		//点击DOCK条哪个空白的位置
	public int mCurDockSize;			//DOCK条当前有图标的个数
	public int mAddMaxSize;				//DOCK目前能添加图标的个数
	public int mDockHeight;				//dock条高度
	public int mStatusbarHeight;		//通知栏高度
	
	public DockAddIconAppView mAppView;				//应用程序、go快捷方式、默认图标view
	public DockAddIconIndexVIew mAddIconIndexView;	//导航页view

	public boolean mAddIconFinish = true;		//添加图标是否完成
	public long mLastClickTime;					//最后一次点击时间
	public static final long CLICK_TIME = 400;	//每次点击间隔时间
	
	public static final int DOCK_TO_RED_BG_DE_TIME = 1000;	//dock红色背景持续时间
	public static final int MESSAGE_CLEAR_DOCK_RED_BG = 0;	//取消DOCK红色背景消息
	
	public int mCurType;	//当前打开view的类型
	public static final int TYPE_ADD_ICON_INDEX = 0;		//导航页类型
	public static final int TYPE_ADD_ICON_APP = 1;			//应用程序类型
	public static final int TYPE_ADD_ICON_SHORTCUT = 2;		//快捷方式类型
	public static final int TYPE_ADD_ICON_GOSHORTCUT = 3;	//go快捷方式类型
	public static final int TYPE_ADD_ICON_DEFAULT = 4;		//默认图标类型
	
	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//取消DOCK红色背景
			if (msg.what == MESSAGE_CLEAR_DOCK_RED_BG) {
				GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_ADD_ICON_RED_BG, -1,
						null, null);
			}
		}
	};
	
	public DockAddIconFrame(Activity arg0, IFrameManager arg1, int arg2) {
		super(arg0, arg1, arg2);
		initContentView();
	}

	@Override
	public View getContentView() {
		return mContentView;
	}

	/**
	 * <br>功能简述:初始化view
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void initContentView() {
		mDockHeight = DockUtil.getBgHeight();
		mStatusbarHeight = StatusBarHandler.getStatusbarHeight();
		LayoutInflater inflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContentView = (RelativeLayout) inflater.inflate(R.layout.dialog_dock_add_icon_frame, null);
		mAllLayout = (RelativeLayout) mContentView.findViewById(R.id.all_layout);
		mCoverLayout = (LinearLayout) mContentView.findViewById(R.id.cover_layout);
		mContentLayout = (LinearLayout) mContentView.findViewById(R.id.content_layout);
		
		setCoverView();
		addIndexView();
		startAnimation();
	}

	/**
	 * <br>功能简述:设置现实动画
	 * <br>功能详细描述:view慢慢显示后，背景再慢慢显示
	 * <br>注意:
	 */
	public void startAnimation() {
		//设置view动画
		AlphaAnimation viewAnimationAlpha = new AlphaAnimation(0.6f, 1.0f);
		viewAnimationAlpha.setDuration(100);
		viewAnimationAlpha.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
			
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				//黑色遮照层的动画
				mCoverLayout.setVisibility(View.VISIBLE);
				AlphaAnimation coverViewAnimationAlpha = new AlphaAnimation(0.2f, 1.0f);
				coverViewAnimationAlpha.setDuration(800);
				mCoverLayout.startAnimation(coverViewAnimationAlpha);
			}
		});
		mContentLayout.startAnimation(viewAnimationAlpha);
	}
	
	
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object, List objects) {
		boolean ret = false;
		switch (msgId) {
			//dock添加图标初始化
			case IDiyMsgIds.DOCK_ADD_ICON_INIT :
				mBlankIndexInRow = param;
				mCurDockSize = (Integer) object;
				mAddMaxSize = DockUtil.ICON_COUNT_IN_A_ROW - mCurDockSize;
				break;

			//上一次的添加图标完成
			case IDiyMsgIds.DOCK_ADD_ICON_ADD_FINISH:
				//添加完后修改添加位置等信息
				mBlankIndexInRow = mBlankIndexInRow + 1;
				mCurDockSize = mCurDockSize + 1;
				mAddMaxSize = mAddMaxSize - 1;
				
				//标志上一次添加完成
				mAddIconFinish = true;
				break;
				
			//横竖屏切换
			case IFrameworkMsgId.SYSTEM_CONFIGURATION_CHANGED :
				onConfigurationChanged();
				break;
				
			//监听HOME键
			case IDiyMsgIds.BACK_TO_MAIN_SCREEN:	 //go桌面作为默认桌面
			case IDiyMsgIds.SYSTEM_ON_NEW_INTENT :	 //点击HOME键选择go桌面
				onDestory();	
				break;
				
			default :
				break;
		}
		return ret;

	}

	@Override
	public void onIconsClick(int type, View view, int position, Object infos) {
		if (type == TYPE_ADD_ICON_INDEX) {
			selectIconOfIndex(position);	//点击导航也
		} else {
			selectIconOfItem(type, view, position, infos);	//点击应用程序、go快捷方式、默认图标
		}
	}

	/**
	 * <br>功能简述:点击导航页的item
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param position 点击位置
	 */
	public void selectIconOfIndex(int position) {
		//判断可以是否还可以继续添加。不能就DOCK条做红色提示
		if (mAddMaxSize <= 0) {
			setDockRedBg();
			return;
		}
		
		switch (position) {
			//应用程序
			case 0 :
				removeAppView();
				mCurType = TYPE_ADD_ICON_APP;
				mAppView = new DockAddIconAppView(mActivity, TYPE_ADD_ICON_APP);
				mAppView.setTitle(R.string.open_App);
				mAppView.setOnAddIconClickListener(this);
				mContentLayout.addView(mAppView);
				break;
			
			//快捷方式
			case 1 :
				mCurType = TYPE_ADD_ICON_SHORTCUT;
				GoLauncher.sendMessage(IMsgType.SYNC, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.DOCK_ENTER_SHORTCUT_SELECT, 1, null, null);
				break;
			
			//Go快捷方式	
			case 2 :
				removeAppView();
				mCurType = TYPE_ADD_ICON_GOSHORTCUT;
				mAppView = new DockAddIconAppView(mActivity, TYPE_ADD_ICON_GOSHORTCUT);
				mAppView.setTitle(R.string.dialog_name_go_shortcut);
				mAppView.setOnAddIconClickListener(this);
				mContentLayout.addView(mAppView);
				break;
				
			//5个默认图标	
			case 3 :
				removeAppView();
				mCurType = TYPE_ADD_ICON_DEFAULT;
				mAppView = new DockAddIconAppView(mActivity, TYPE_ADD_ICON_DEFAULT);
				mAppView.setTitle(R.string.default_icon);
				mAppView.setOnAddIconClickListener(this);
				mContentLayout.addView(mAppView);
				break;

			default :
				break;
		}
	}
	
	/**
	 * <br>功能简述:清除AppViewd资源
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void removeAppView() {
		mContentLayout.removeAllViews();
		if (mAppView != null) {
			mAppView.onDestroy();
			mAppView = null;
		}
	}

	/**
	 * <br>功能简述:点击应用程序、go快捷方式、默认图标的item
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param type 类型
	 * @param view 试图
	 * @param position 选择位置
	 * @param infos 对象
	 */
	public void selectIconOfItem(int type, View view, int position, Object infos) {
		//处理快速点击的BUG
		long curTime = System.currentTimeMillis();
		if (curTime - mLastClickTime < CLICK_TIME) {
			return;
		}
		mLastClickTime = curTime;

		//判断上一次添加是否已经完成
		if (!mAddIconFinish) {
			return;
		}
		
		//判断可以是否还可以继续添加。不能就DOCK条做红色提示
		if (mAddMaxSize <= 0) {
			setDockRedBg();
			return;
		}
		
		mAddIconFinish = false;
		
		int screenWidth = GoLauncher.getScreenWidth();	//屏幕宽度
		int screenHeight = GoLauncher.getScreenHeight();	//屏幕高度

		//判断通知栏是否隐藏，没有隐藏就减去通知栏高度
		if (!mIsHideStatusbar) {
			screenHeight =  screenHeight - mStatusbarHeight;
		}
		
		//设置终点坐标
		ArrayList<Integer> list = getEndPoint(view, screenWidth, screenHeight);
		
		Point dockPoint = getDockPoint(view, screenWidth, screenHeight);
		ArrayList<Object> indexArray = new ArrayList<Object>();
		indexArray.add(view);
		if (infos instanceof ShortCutInfo) {
			//需要重新创建一个对象。否则重新点击添加时DOCK持有的是同一个对象。删除的时候会同时删除掉
			ShortCutInfo oldShortCutInfo = (ShortCutInfo) infos;
			ShortCutInfo newShortCutInfo = new ShortCutInfo();
			newShortCutInfo.mItemType = oldShortCutInfo.mItemType;
			newShortCutInfo.mTitle = oldShortCutInfo.mTitle;
			newShortCutInfo.mFeatureIconType = oldShortCutInfo.mFeatureIconType;
			newShortCutInfo.mIntent = oldShortCutInfo.mIntent;
			newShortCutInfo.mIcon = oldShortCutInfo.mIcon;
			newShortCutInfo.mFeatureIconPath = oldShortCutInfo.mFeatureIconPath;
			newShortCutInfo.mInScreenId = System.currentTimeMillis() + 1;
			newShortCutInfo.mFeatureIconPackage = oldShortCutInfo.mFeatureIconPackage;
			view.setTag(newShortCutInfo);
		} else {
			//应用程序
			view.setTag(infos);
		}
		
		// 准备拖拽层
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IFrameworkMsgId.SHOW_FRAME,
				IDiyFrameIds.DRAG_FRAME, null, null);
		//图标飞
		GoLauncher.sendMessage(this, IDiyFrameIds.DRAG_FRAME, IDiyMsgIds.START_TO_AUTO_FLY,
				DragFrame.TYPE_DOCK_ADD_ICON, view, list);

		//通知dock层挤压动画
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_CHECK_POSITION, -1,
				dockPoint, indexArray);
	}
	
	/**
	 * <br>功能简述:设置dock条为红色背景
	 * <br>功能详细描述:当dock条添加图标大于5个时背景变红色
	 * <br>注意:
	 */
	public void setDockRedBg() {
		ScreenUtils.showToast(R.string.dock_is_full, mActivity);
		mHandler.removeMessages(MESSAGE_CLEAR_DOCK_RED_BG);
		GoLauncher.sendMessage(this, IDiyFrameIds.DOCK_FRAME, IDiyMsgIds.DOCK_ADD_ICON_RED_BG, 1, null, null);
		mHandler.sendEmptyMessageDelayed(MESSAGE_CLEAR_DOCK_RED_BG, DOCK_TO_RED_BG_DE_TIME);
	}
	
	/**
	 * <br>功能简述:设置遮照层
	 * <br>功能详细描述:横竖屏切换dock都需要高亮，其他地方半黑
	 * <br>注意:
	 */
	public void setCoverView() {
		android.widget.RelativeLayout.LayoutParams params = new android.widget.RelativeLayout.LayoutParams(-1, -1);
		if (GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			params.setMargins(0, 0, 0, mDockHeight);
		} else {
			params.setMargins(0, 0, mDockHeight, 0);
		}
		if (mAllLayout != null) {
			mAllLayout.setLayoutParams(params);
		}
	}
	
	/**
	 * <br>功能简述:获取VIEW的终点坐标
	 * <br>功能详细描述:
	 * <br>注意:	
	 * @param view	view
	 * @param screenWidth 屏幕宽度
	 * @param screenHeight 屏幕高度
	 * @return
	 */
	public ArrayList<Integer> getEndPoint(View view, int screenWidth, int screenHeight) {
		int endPointX = 0;	//结束X坐标
		int endPointY = 0;	//结束Y坐标
		int viewWidth = view.getWidth();
		int viewHeight = view.getHeight();
		
		//view 飞动的XY终点坐标
		//view的左上角飞到终点坐标的位置。所以如果需要view的中心点飞到终点坐标。需要减去一半view的宽度和高度一半
		if (GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			int addOneWidth = screenWidth / (mCurDockSize + 1);	//计算添加一个VIEW后每个VIEW所占的宽度
			endPointX = (addOneWidth * mBlankIndexInRow + (addOneWidth / 2)) - viewWidth / 2;
			endPointY = screenHeight - mDockHeight / 2 - viewHeight / 2;	
		} else {
			int addOneWidth = screenHeight / (mCurDockSize + 1);	
			endPointX = screenWidth - mDockHeight / 2 -  viewWidth / 2;
			endPointY = screenHeight - (addOneWidth * mBlankIndexInRow + (addOneWidth / 2)) - viewHeight / 2;
		}

		//设置终点坐标
		ArrayList<Integer> pointList = new ArrayList<Integer>();
		pointList.add(endPointX);	
		pointList.add(endPointY);
		
		return pointList;
	}
	
	/**
	 * <br>功能简述:获取移动到DOCK的坐标，未动画挤压做准备
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param view
	 * @param screenWidth 屏幕层宽度
	 * @param screenHeight 屏幕层高度
	 * @return
	 */
	public Point getDockPoint(View view, int screenWidth, int screenHeight) {
		int dockPointX = 0; //dock的X坐标
		int dockPointY = 0; //dock的Y坐标
		if (GoLauncher.getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			//判断当前DOCK只有0个图标
			if (mCurDockSize == 0) {
				dockPointX = screenWidth;
			} else {
				dockPointX = screenWidth / mCurDockSize * mBlankIndexInRow;
			}
			
			dockPointY =  screenHeight - mDockHeight;
			
			//判断添加哪个区域
			if (mBlankIndexInRow == 0) {
				dockPointX = dockPointX + 1; //最左边距+1不然不在DOCK区域内
			} else if (mBlankIndexInRow == mCurDockSize) {
				dockPointX = dockPointX - 1; //最右边距-1，不然不在DOCK区域内
			}
		} else {
			int dockHeight = screenHeight;
			int oneViewWidth = 0;
			
			//判断当前DOCK只有0个图标
			if (mCurDockSize == 0) {
				oneViewWidth = dockHeight;
			} else {
				oneViewWidth = dockHeight / mCurDockSize; //添加前VIEW后每个VIEW所占的宽度
			}
			
			dockPointX = screenWidth - mDockHeight;
			dockPointY = dockHeight -  mBlankIndexInRow * oneViewWidth;
			
			//判断添加哪个区域
			if (mBlankIndexInRow == 0) {
				dockPointY = dockPointY - 1; //最左边距+1不然不在DOCK区域内
			} else if (mBlankIndexInRow == mCurDockSize) {
				dockPointY = dockPointY + 1; //最右边距-1，不然不在DOCK区域内
			}
		}
		
		Point point = new Point(dockPointX, dockPointY);
		return point;
	}

	/**
	 * <br>功能简述:横竖屏切换
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onConfigurationChanged() {
		setCoverView();
		if (mAppView != null) {
			mAppView.onConfigurationChanged();
		}
		
		if (mAddIconIndexView != null) {
			mAddIconIndexView.onConfigurationChanged();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//点击手机返回按钮
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			onClickBack(mCurType);
		}
		return true;
	}
	
	//点击返回按钮和导航页面取消按钮
	@Override
	public void onBackBtnClick(int type) {
		onClickBack(type);
	}
	
	/**
	 * <br>功能简述:如果在导航页按返回就注销添加页面，否则返回导航页
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param type
	 */
	public void onClickBack(int type) {
		//如果在导航页按返回就注销添加页面，否则返回导航页
		if (type == TYPE_ADD_ICON_INDEX) {
			onDestory();
		} else {
			addIndexView();
		}
	}
	
	/**
	 * <br>功能简述:添加导航View
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void addIndexView() {
		mContentLayout.removeAllViews();
		mCurType = TYPE_ADD_ICON_INDEX;
		if (mAddIconIndexView == null) {
			mAddIconIndexView = new DockAddIconIndexVIew(mActivity, TYPE_ADD_ICON_INDEX);
			mAddIconIndexView.setOnAddIconClickListener(this);
		}
		mContentLayout.addView(mAddIconIndexView);
	}

	/**
	 * <br>功能简述:注销时释放资源
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void onDestory() {
		if (mAppView != null) {
			mAppView.onDestroy();
		}
		
		if (mAddIconIndexView != null) {
			mAddIconIndexView.onDestroy();
		}
		
		//发消息注销添加层
		GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
				IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.DOCK_ADD_ICON_FRAME, null, null);
	}
}
