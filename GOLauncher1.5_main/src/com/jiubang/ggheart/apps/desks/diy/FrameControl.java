package com.jiubang.ggheart.apps.desks.diy;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.jiubang.core.framework.AbstractFrame;
import com.jiubang.core.framework.FrameManager;
import com.jiubang.core.framework.ICleanable;
import com.jiubang.core.message.MessageManager;
import com.jiubang.ggheart.apps.desks.diy.frames.cover.CoverFrame;
import com.jiubang.ggheart.plugin.shell.ShellPluginFactory;

/**
 * 帧控制器，用于控制DIY桌面的帧的生命周期,与业务逻辑挂钩
 * 
 * @author yuankai
 * @version 1.0
 */
public class FrameControl extends FrameManager implements ICleanable {
	private List<AbstractFrame> mCachedFrames; // 缓存的帧
	private CoverFrame mCoverFrame; // 罩子层
	private Activity mActivity = null;

	/**
	 * 被缓存的帧ID,这些帧不会每次显示时被重新初始化
	 */
	private final static int[] CACHED_FRAME_IDS = { IDiyFrameIds.SCREEN_FRAME,
			IDiyFrameIds.APPFUNC_FRAME, IDiyFrameIds.DOCK_FRAME, IDiyFrameIds.SCREEN_PREVIEW_FRAME };

	/**
	 * 帧控制器构造方法
	 * 
	 * @param activity
	 *            活动
	 * @param frameManager
	 *            帧管理器
	 * @param theme
	 *            主题数据
	 */
	public FrameControl(Activity activity, ViewGroup layout, MessageManager manager) {
		super(layout, manager);
		mActivity = activity;
		mCachedFrames = new ArrayList<AbstractFrame>();
	}

	public void initCachedFrame() {
		if (ShellPluginFactory.isUseShellPlugin(mActivity)) {
			ShellPluginFactory.buildShellPlugin(mActivity);
			View overlayedViewGroup = ShellPluginFactory.getShellManager().getOverlayedViewGroup();
			View compatibleView = ShellPluginFactory.getShellManager().getCompatibleView();
			showFrame(IDiyFrameIds.SHELL_FRAME);
			mViewManager.addView(overlayedViewGroup);
			mViewManager.addView(compatibleView);
		} else {
			showFrame(IDiyFrameIds.SCREEN_FRAME);
			showFrame(IDiyFrameIds.DOCK_FRAME);
			addFrame(produceIfNotCached(IDiyFrameIds.APPFUNC_FRAME), View.INVISIBLE);
		}
	}

	/**
	 * 显示
	 * 
	 * @param frameId
	 *            帧ID
	 * @return 是否操作成功
	 */
	public synchronized boolean showFrame(int frameId) {
		if (showFilter(frameId)) {
			return false;
		}

		AbstractFrame frame = getFrame(frameId);
		if (frame != null) {
			if (frame.getVisibility() != View.VISIBLE) {
				setFrameVisiable(frameId, View.VISIBLE);
			}
			return true;
		} else {
			frame = produceIfNotCached(frameId);
			if (frame != null) {
				return addFrame(frame, View.VISIBLE);
			}
		}
		return false;
	}

	/**
	 * 如果对应ID的帧当前正在显示，则移除 如果对应ID的帧当前未显示，则显示
	 * 
	 * @param id
	 *            帧ID
	 */
	public void showOrHide(int id) {
		if (isExits(id)) {
			hideFrame(id);
		} else {
			showFrame(id);
			AbstractFrame topFrame = getTopFrame();
			if (topFrame != null && topFrame.getId() != id) {
				hideFrame(topFrame.getId());
				topFrame = getTopFrame();
			}
		}
	}

	/**
	 * 从其他层直接回到屏幕层 如果当前最外层是dock层，则dock层不被移除，移除dock层之下与screen之上的其他层
	 * 如果当前最外层不是dock层，则移除只到达到dock层所在层
	 */
	public void backToScreen() {
		AbstractFrame topFrame = getTopFrame();
		if (topFrame != null) {
			if (topFrame.getId() == IDiyFrameIds.DOCK_FRAME) {
				// 如果当前最外层是dock层，则去除dock层与screen中的其他层
				int topIndex = 0;
				AbstractFrame nextVisiableFrame = null;
				while (((topIndex = getZIndex(topFrame)) > 0)
						&& (nextVisiableFrame = getNextVisiableFrame(topIndex)) != null) {
					if (nextVisiableFrame.getId() != IDiyFrameIds.SCREEN_FRAME) {
						hideFrame(nextVisiableFrame.getId());
					} else {
						break;
					}
				}
			} else {
				while ((topFrame = getTopFrame()) != null
						&& topFrame.getId() != IDiyFrameIds.DOCK_FRAME
						&& topFrame.getId() != IDiyFrameIds.SCREEN_FRAME) {
					hideFrame(topFrame.getId());
				}
			}
		}
		topFrame = null;
	}

	/**
	 * 隐藏
	 * 
	 * @param frameId
	 *            帧ID
	 */
	public void hide(int frameId) {
		setFrameVisiable(frameId, View.INVISIBLE);
	}

	/**
	 * 不参与排版
	 * 
	 * @param frameId
	 *            帧ID
	 */
	public void gone(int frameId) {
		setFrameVisiable(frameId, View.GONE);
	}

	/**
	 * 判断frame是否顶层
	 * 
	 * @param frameid
	 * @return
	 */
	public boolean isForeground(int frameid) {
		AbstractFrame topFrame = getTopFrame();
		if (topFrame != null && topFrame.getId() == frameid) {
			return true;
		}
		return false;
	}

	/**
	 * 是否桌面
	 * 
	 * @param id
	 * @return
	 */
	public boolean isScreen(int frameId) {
		return frameId == IDiyFrameIds.SCREEN_FRAME || frameId == IDiyFrameIds.DOCK_FRAME;
	}

	/**
	 * 是否桌面在最上层
	 * 
	 * @return
	 */
	public boolean isScreenOnTop() {
		final AbstractFrame topFrame = getTopFrame();
		return topFrame != null && isScreen(topFrame.getId());
	}

	/**
	 * 移除最顶层帧
	 * 
	 * @return 是否成功
	 */
	public boolean removeTopFrame() {
		final AbstractFrame topFrame = getTopFrame();
		if (topFrame != null) {
			return removeFrame(topFrame);
		}
		return false;
	}

	/**
	 * 不显示
	 * 
	 * @param frameId
	 *            帧ID
	 * @return 是否操作成功
	 */
	public boolean hideFrame(int frameId) {
		if (hideFilter(frameId)) {
			hide(frameId);
			return true;
		} else {
			boolean ret = false;
			try {
				ret = removeFrame(frameId);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			return ret;
		}
	}

	/**
	 * 
	 * @param frameId
	 * @return 返回true需要保留frmae在栈中，否则删除
	 */
	private boolean hideFilter(int frameId) {
		boolean ret = false;
		switch (frameId) {
			case IDiyFrameIds.SCREEN_FRAME :
			case IDiyFrameIds.DOCK_FRAME :
				ret = true;
				break;

			case IDiyFrameIds.SCREEN_EDIT_BOX_FRAME : {
				setFrameVisiable(IDiyFrameIds.DOCK_FRAME, View.VISIBLE);
				// 暂时设回false
				ret = false;
				break;
			}

			case IDiyFrameIds.SCREEN_PREVIEW_FRAME : {
				// 添加屏幕预览层时，将Dock和screen层显示
				setFrameVisiable(IDiyFrameIds.DOCK_FRAME, View.VISIBLE);
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.PREVIEW_NOTIFY_DESKTOP, 1, null, null);
				ret = true;
			}
				break;

			case IDiyFrameIds.APPFUNC_FRAME : {
				// 展现罩子层的view
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.COVER_FRAME_SHOW_ALL, -1, null, null);
				// 展现中间层
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_SHOW_MIDDLE_VIEW, -1, null, null);
				ret = true;
			}
				break;
			default :
				ret = isCacheId(frameId);
				break;
		}
		return ret;
	}

	/**
	 * 获取被缓存的帧对象
	 * 
	 * @param frameId
	 *            帧ID
	 * @return 帧对象
	 */
	public AbstractFrame getCachedFrame(int frameId) {
		int size = mCachedFrames.size();
		for (int i = 0; i < size; i++) {
			AbstractFrame frame = mCachedFrames.get(i);
			if (frame.getId() == frameId) {
				return frame;
			}
		}

		return null;
	}

	private synchronized AbstractFrame produceIfNotCached(int frameId) {
		AbstractFrame frame = null;
		if (isCacheId(frameId)) {
			frame = getCachedFrame(frameId);
			if (frame == null) {
				frame = FrameFactory.produce(mActivity, this, frameId);
				if (frame != null) {
					mCachedFrames.add(frame);
				}
			}
		} else {
			frame = FrameFactory.produce(mActivity, this, frameId);
		}
		return frame;
	}

	/**
	 * 处理特殊层的添加操作
	 * 
	 * @param id
	 *            帧ID
	 * @return 是否处理 true过滤使之不显示
	 */
	private boolean showFilter(int frameId) {
		boolean ret = false;
		switch (frameId) {
			case IDiyFrameIds.SCREEN_PREVIEW_FRAME :
				break;
			case IDiyFrameIds.APPFUNC_FRAME :
				// 隐藏罩子层的view
				GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
						IDiyMsgIds.COVER_FRAME_HIDE_ALL, -1, null, null);
				// 隐藏主题中间层
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
						IDiyMsgIds.SCREEN_HIDE_MIDDLE_VIEW, -1, null, null);
				break;
			default :
				break;
		}

		return ret;
	}

	/**
	 * 帧是否存在
	 * 
	 * @param id
	 *            帧id
	 * @return 是否在数组中并且可见
	 */
	public boolean isExits(int id) {
		final AbstractFrame frame = getFrame(id);
		return frame != null && frame.getVisibility() == View.VISIBLE;
	}

	private synchronized boolean isCacheId(int id) {
		int size = CACHED_FRAME_IDS.length;
		for (int i = 0; i < size; i++) {
			if (CACHED_FRAME_IDS[i] == id) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void cleanup() {
		mCachedFrames.clear();
		mFrames.clear();
		mCleanManager.cleanup();
		removeCoverFrame();
	}

	@Override
	public float getLastMotionX() {
		final ViewGroup root = getRootView();
		if (root != null && root instanceof DiyFrameLayout) {
			return ((DiyFrameLayout) root).getLastMotionX();
		}
		return 0f;
	}

	@Override
	public float getLastMotionY() {
		final ViewGroup root = getRootView();
		if (root != null && root instanceof DiyFrameLayout) {
			return ((DiyFrameLayout) root).getLastMotionY();
		}
		return 0f;
	}

	/**
	 * 获取罩子层
	 * @return
	 */
	public CoverFrame getCoverFrame() {
		if (mCoverFrame == null) {
			mCoverFrame = (CoverFrame) FrameFactory.produce(mActivity, this,
					IDiyFrameIds.COVER_FRAME);
		}
		return mCoverFrame;
	} // end getCoverFrame

	/**
	 * 移除罩子层
	 */
	public void removeCoverFrame() {
		if (mCoverFrame != null) {
			mMsgMan.unRegistMsgHandler(mCoverFrame);
			mKeyManager.unRegistKey(mCoverFrame);
			mCoverFrame.onVisiable(View.INVISIBLE);
			mCoverFrame.onRemove();
			mCoverFrame.cleanup();
			mCoverFrame = null;
		}
	} // end removeCoverFrame
}
