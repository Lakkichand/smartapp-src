package com.jiubang.ggheart.apps.desks.diy.frames.animation;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.IFrameManager;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;

public class AnimationFrame extends AbstractFrame {
	TransitionView mLayout;
	TransitionAnimation mAnimation;
	BinaryView mDesktopAndDock;
	BinaryView mAppDrawer;
	BinaryView mAppDrawerSearch;
	BinaryView mImageBrowser;

	public AnimationFrame(Activity activity, IFrameManager frameManager, int id) {
		super(activity, frameManager, id);
		mLayout = new TransitionView(mActivity);
		mAnimation = new TransitionAnimation();
		mLayout.setTransitionAnimation(mAnimation);
	}

	@Override
	public View getContentView() {
		return mLayout;
	}

	@Override
	public boolean isOpaque() {
		// 不透明
		return true;
	}

	@Override
	public void onRemove() {
		// 隐藏此层
		// mLayout.clearAnimation(); // 需要取消动画吗？
		super.onRemove();
	}

	@Override
	protected void onPause() {
		super.onPause();
		leave();
	}

	private void leave() {
		// 隐藏此层
		mFrameManager.removeFrame(getId());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean handleMessage(Object who, int type, int msgId, int param, Object object,
			List objects) {
		super.handleMessage(who, type, msgId, param, object, objects);
		boolean ret = true;
		switch (msgId) {
			case IDiyMsgIds.SET_APP_DRAWER_TRANSITION_LISTENER : {
				mAnimation.setAnimationListener((AnimationListener) object);
			}
				break;
			case IDiyMsgIds.START_APP_DRAWER_ENTER_TRANSITION : {
				mDesktopAndDock = getDesktopAndDockLayout();
				mAppDrawer = getAppDrawerLayout();
				mLayout.setViews(mDesktopAndDock, mAppDrawer);
				mLayout.setReverseDrawOrderEnabled(false); // 让功能表画在上面
				mLayout.setTransition((Transition) object);
				mLayout.startTransition(param);
				// 如果直接设功能表层的可见性会在其回调方法中再次引起进出动画
				// setViewVisibility(mAppDrawer.getFirstView(), View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME,
				// View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME,
				// View.INVISIBLE);
			}
				break;
			case IDiyMsgIds.START_APP_DRAWER_LEAVE_TRANSITION : {
				mAppDrawer = getAppDrawerLayout();
				mDesktopAndDock = getDesktopAndDockLayout();
				mLayout.setViews(mAppDrawer, mDesktopAndDock);
				mLayout.setReverseDrawOrderEnabled(true); // 让功能表画在上面
				mLayout.setTransition((Transition) object);
				mLayout.startTransition(param);
				// 如果直接设功能表层的可见性会在其回调方法中再次引起进出动画
				// setViewVisibility(mAppDrawer.getFirstView(), View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME,
				// View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME,
				// View.INVISIBLE);
			}
				break;
			case IDiyMsgIds.FINISH_APP_DRAWER_ENTER_TRANSITION : {
				mLayout.setVisibility(View.INVISIBLE);
				mLayout.post(new Runnable() {
					@Override
					public void run() {
						// setViewVisibility(mAppDrawer.getFirstView(),
						// View.VISIBLE);
						mAnimation.setAnimationListener(null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.ANIMATION_FRAME, null,
								null);
						if (mDesktopAndDock != null) {
							mDesktopAndDock.setViews(null, null);
						}
						if (mAppDrawer != null) {
							mAppDrawer.setViews(null, null);
						}
					}
				});

			}
				break;
			/*
			 * case IDiyMsgIds.CHECK_ANIMATION_FINISH: { if
			 * (mAnimation!=null&&!mAnimation.hasEnded()) { ret = true; }else {
			 * ret = false; } } break;
			 */
			case IDiyMsgIds.FINISH_APP_DRAWER_LEAVE_TRANSITION : {
				mLayout.setVisibility(View.INVISIBLE);
				mLayout.post(new Runnable() {
					@Override
					public void run() {
						// mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME,
						// View.VISIBLE);
						// mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME,
						// View.VISIBLE);

						mAnimation.setAnimationListener(null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.ANIMATION_FRAME, null,
								null);
						if (mDesktopAndDock != null) {
							mDesktopAndDock.setViews(null, null);
						}
						if (mAppDrawer != null) {
							mAppDrawer.setViews(null, null);
						}
					}
				});
			}
				break;
			case IDiyMsgIds.SET_APP_DRAWER_TRANSITION_DESKTOP_ANIMATION : {
				if (object != null) {
					Animation animation = (Animation) object;
					animation.setFillAfter(true);
					mDesktopAndDock.startAnimation(animation);
				}
			}
				break;
			case IDiyMsgIds.SET_APP_DRAWER_TRANSITION_DRAWER_ANIMATION : {
				if (object != null) {
					Animation animation = (Animation) object;
					if (mAppDrawer != null) {
						mAppDrawer.startAnimation(animation);
					}
					animation.setFillAfter(true);
					mAnimation.setDuration(animation.getDuration());
				}
			}
				break;
			case IDiyMsgIds.START_APP_DRAWER_SEARCH_ENTER_TRANSITION : {
				mAppDrawerSearch = getAppDrawerSearchLayout();
				mAppDrawer = getAppDrawerLayout();
				mLayout.setViews(mAppDrawer, mAppDrawerSearch);
				mLayout.setReverseDrawOrderEnabled(false); // 让功能表画在上面
				mLayout.setTransition((Transition) object);
				mLayout.startTransition(param);
				// 如果直接设功能表层的可见性会在其回调方法中再次引起进出动画
				// setViewVisibility(mAppDrawer.getFirstView(), View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME,
				// View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME,
				// View.INVISIBLE);
			}
				break;
			case IDiyMsgIds.START_APP_DRAWER_SEARCH_LEAVE_TRANSITION : {
				mAppDrawer = getAppDrawerLayout();
				mAppDrawerSearch = getAppDrawerSearchLayout();
				mLayout.setViews(mAppDrawerSearch, mAppDrawer);
				mLayout.setReverseDrawOrderEnabled(true); // 让功能表画在上面
				mLayout.setTransition((Transition) object);
				mLayout.startTransition(param);
				// 如果直接设功能表层的可见性会在其回调方法中再次引起进出动画
				// setViewVisibility(mAppDrawer.getFirstView(), View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME,
				// View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME,
				// View.INVISIBLE);
			}
				break;
			case IDiyMsgIds.SET_APP_DRAWER_SEARCH_TRANSITION_SEARCH_ANIMATION : {
				if (object != null) {
					Animation animation = (Animation) object;
					if (mAppDrawerSearch != null) {
						mAppDrawerSearch.startAnimation(animation);
					}
					animation.setFillAfter(true);
					mAnimation.setDuration(animation.getDuration());
				}
			}
				break;
			case IDiyMsgIds.FINISH_APP_DRAWER_SEARCH_ENTER_TRANSITION : {
				mLayout.setVisibility(View.INVISIBLE);
				mLayout.post(new Runnable() {
					@Override
					public void run() {
						// setViewVisibility(mAppDrawer.getFirstView(),
						// View.VISIBLE);
						mAnimation.setAnimationListener(null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.ANIMATION_FRAME, null,
								null);
						if (mAppDrawer != null) {
							mAppDrawer.setViews(null, null);
						}
						if (mAppDrawerSearch != null) {
							mAppDrawerSearch.setViews(null, null);
						}
					}
				});

			}
				break;
			case IDiyMsgIds.FINISH_APP_DRAWER_SEARCH_LEAVE_TRANSITION : {
				mLayout.setVisibility(View.INVISIBLE);
				mLayout.post(new Runnable() {
					@Override
					public void run() {
						// mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME,
						// View.VISIBLE);
						// mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME,
						// View.VISIBLE);

						mAnimation.setAnimationListener(null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.ANIMATION_FRAME, null,
								null);
						if (mAppDrawerSearch != null) {
							mAppDrawerSearch.setViews(null, null);
						}
						if (mAppDrawer != null) {
							mAppDrawer.setViews(null, null);
						}
					}
				});
			}
				break;
			case IDiyMsgIds.START_IMAGE_BROWSER_ENTER_TRANSITION : {
				mImageBrowser = getImageBrowserLayout();
				mAppDrawer = getAppDrawerLayout();
				mLayout.setViews(mAppDrawer, mImageBrowser);
				mLayout.setReverseDrawOrderEnabled(false); // 让功能表画在上面
				mLayout.setTransition((Transition) object);
				mLayout.startTransition(param);
				// 如果直接设功能表层的可见性会在其回调方法中再次引起进出动画
				// setViewVisibility(mAppDrawer.getFirstView(), View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME,
				// View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME,
				// View.INVISIBLE);
			}
				break;
			case IDiyMsgIds.START_IMAGE_BROWSER_LEAVE_TRANSITION : {
				mAppDrawer = getAppDrawerLayout();
				mImageBrowser = getImageBrowserLayout();
				mLayout.setViews(mImageBrowser, mAppDrawer);
				mLayout.setReverseDrawOrderEnabled(true); // 让功能表画在上面
				mLayout.setTransition((Transition) object);
				mLayout.startTransition(param);
				// 如果直接设功能表层的可见性会在其回调方法中再次引起进出动画
				// setViewVisibility(mAppDrawer.getFirstView(), View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME,
				// View.INVISIBLE);
				// mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME,
				// View.INVISIBLE);
			}
				break;
			case IDiyMsgIds.SET_IMAGE_BROWSER_TRANSITION_BROWSER_ANIMATION : {
				if (object != null) {
					Animation animation = (Animation) object;
					if (mImageBrowser != null) {
						mImageBrowser.startAnimation(animation);
					}
					animation.setFillAfter(true);
					mAnimation.setDuration(animation.getDuration());
				}
			}
				break;
			case IDiyMsgIds.FINISH_IMAGE_BROWSER_ENTER_TRANSITION : {
				mLayout.setVisibility(View.INVISIBLE);
				mLayout.post(new Runnable() {
					@Override
					public void run() {
						// setViewVisibility(mAppDrawer.getFirstView(),
						// View.VISIBLE);
						mAnimation.setAnimationListener(null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.ANIMATION_FRAME, null,
								null);
						if (mAppDrawer != null) {
							mAppDrawer.setViews(null, null);
						}
						if (mImageBrowser != null) {
							mImageBrowser.setViews(null, null);
						}
					}
				});

			}
				break;
			case IDiyMsgIds.FINISH_IMAGE_BROWSER_LEAVE_TRANSITION : {
				mLayout.setVisibility(View.INVISIBLE);
				mLayout.post(new Runnable() {
					@Override
					public void run() {
						// mFrameManager.setFrameVisiable(IDiyFrameIds.SCREEN_FRAME,
						// View.VISIBLE);
						// mFrameManager.setFrameVisiable(IDiyFrameIds.DOCK_FRAME,
						// View.VISIBLE);

						mAnimation.setAnimationListener(null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.ANIMATION_FRAME, null,
								null);
						if (mImageBrowser != null) {
							mImageBrowser.setViews(null, null);
						}
						if (mAppDrawer != null) {
							mAppDrawer.setViews(null, null);
						}
					}
				});
			}
				break;
			case IDiyMsgIds.BACK_TO_MAIN_SCREEN : {
				// 隐藏此层
				leave();
				break;
			}
			default :
				ret = false;
		}
		return ret;
	}

	private View getDesktop() {
		return mFrameManager.getFrame(IDiyFrameIds.SCREEN_FRAME).getContentView();
	}

	private View getDock() {
		return mFrameManager.getFrame(IDiyFrameIds.DOCK_FRAME).getContentView();
	}

	private View getAppDrawer() {
		return mFrameManager.getFrame(IDiyFrameIds.APPFUNC_FRAME).getContentView();
	}

	private View getAppDrawerSearch() {
		return mFrameManager.getFrame(IDiyFrameIds.APPFUNC_SEARCH_FRAME).getContentView();
	}

	private View getImageBrowser() {
		return mFrameManager.getFrame(IDiyFrameIds.IMAGE_BROWSER_FRAME).getContentView();
	}

	private BinaryView getDesktopAndDockLayout() {
		BinaryView view = new BinaryView(mActivity);
		View dock = getDock();
		AbstractFrame dockFrame = mFrameManager.getFrame(IDiyFrameIds.DOCK_FRAME);
		if (dockFrame != null && dockFrame.getVisibility() == View.INVISIBLE) {
			// dock是不可见的
			dock = null;
		}
		view.setViews(getDesktop(), dock);
		view.layout(0, 0, mLayout.getWidth(), mLayout.getHeight());
		return view;
	}

	private BinaryView getAppDrawerLayout() {
		BinaryView view = new BinaryView(mActivity);
		view.setViews(getAppDrawer(), null);
		view.layout(0, 0, mLayout.getWidth(), mLayout.getHeight());
		return view;
	}

	private BinaryView getAppDrawerSearchLayout() {
		BinaryView view = new BinaryView(mActivity);
		view.setViews(getAppDrawerSearch(), null);
		view.layout(0, 0, mLayout.getWidth(), mLayout.getHeight());
		return view;
	}

	private BinaryView getImageBrowserLayout() {
		BinaryView view = new BinaryView(mActivity);
		view.setViews(getImageBrowser(), null);
		view.layout(0, 0, mLayout.getWidth(), mLayout.getHeight());
		return view;
	}

	// private void setViewVisibility(View view, int visibility) {
	// if(view != null){
	// view.setVisibility(visibility);
	// }
	// }
	//	@Override
	//	public boolean onKeyUp(int arg0, KeyEvent arg1) {
	//		// 动画层，不响应按键，以免动画未完成，就退出动画层。 add by dingzijian 2012-7-31
	//		Log.i("dzj", "key");
	//		return true;
	//	}
}