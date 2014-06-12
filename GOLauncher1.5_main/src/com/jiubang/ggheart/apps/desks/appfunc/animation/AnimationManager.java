package com.jiubang.ggheart.apps.desks.appfunc.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;

import com.go.util.window.OrientationControl;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.XAnimator;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XMotion;
import com.jiubang.ggheart.apps.desks.appfunc.OrientationInvoker;

/**
 * 
 * <br>类描述: 动画管理者
 * <br>功能详细描述: 功能表内各种动画统一启动或取消的管理器
 * 
 * @author  yangguanxiang
 * @date  [2012-12-24]
 */
public class AnimationManager {
	private static AnimationManager sInstance;

	private Activity mActivity;

	// private LinkedList<AnimationTask> mQueue;
	private ConcurrentLinkedQueue<AnimationTask> mRunningTaskList;

	private AnimationManager(Activity activity) {
		mActivity = activity;
		// mQueue = new LinkedList<AnimationTask>();
		mRunningTaskList = new ConcurrentLinkedQueue<AnimationTask>();
	}

	public synchronized static AnimationManager getInstance(Activity activity) {
		if (sInstance == null) {
			sInstance = new AnimationManager(activity);
		}
		return sInstance;
	}

	/**
	 * <br>功能简述: 开始单个动画
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param info
	 * @param orientationInvoker
	 * @param params
	 */
	public void attachAnimation(AnimationInfo info, OrientationInvoker orientationInvoker,
			Object... params) {
		if (info != null) {
			AnimationTask task = new AnimationTask(info, orientationInvoker, params);
			task.execute();
		}
	}

	/**
	 * <br>功能简述: 开始批量动画
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param what
	 * @param list
	 * @param observer
	 * @param orientationInvoker
	 * @param params
	 */
	public void attachBatchAnimations(int what, ArrayList<AnimationInfo> list,
			BatchAnimationObserver observer, OrientationInvoker orientationInvoker,
			Object... params) {
		if (list != null && !list.isEmpty()) {
			AnimationTask task = new AnimationTask(what, list, observer, orientationInvoker, params);
			task.execute();
		}
	}

	/**
	 * <br>功能简述: 取消单个动画
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param parent
	 * @param motion
	 */
	public void cancelAnimation(XComponent parent, XMotion motion) {
		for (AnimationTask task : mRunningTaskList) {
			if (task.mAnimationInfo != null) {
				if (task.mAnimationInfo.mParent == parent && task.mAnimationInfo.mTarget == motion) {
					task.cancel();
					mRunningTaskList.remove(task);
					break;
				}
			}
		}
	}

	/**
	 * <br>功能简述: 取消全部动画
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	public void clearAllAnimations() {
		cancelAllRunningTasks();
	}

	private void cancelAllRunningTasks() {
		for (AnimationTask task : mRunningTaskList) {
			task.cancel();
		}
		mRunningTaskList.clear();
	}

	// public void animate() {
	// synchronized (mQueue) {
	// if (!mQueue.isEmpty()) {
	// AnimationTask task = mQueue.poll();
	// if (task != null) {
	// task.execute();
	// }
	// }
	// }
	// }

	/**
	 * 
	 * <br>类描述: 动画任务类
	 * <br>功能详细描述:
	 * 
	 * @author  yangguanxiang
	 * @date  [2012-12-24]
	 */
	private class AnimationTask {
		public ArrayList<AnimationInfo> mAnimationInfoList;
		public AnimationInfo mAnimationInfo;
		public MainAnimationListener mMainListener;
		public OrientationInvoker mOrientationInvoker;

		// public boolean mAnimating = false;

		public AnimationTask(int what, ArrayList<AnimationInfo> list,
				BatchAnimationObserver observer, OrientationInvoker orientationInvoker,
				Object[] params) {
			mAnimationInfoList = list;
			mOrientationInvoker = orientationInvoker;
			HashMap<XAnimator, IAnimateListener> map = new HashMap<XAnimator, IAnimateListener>();
			for (AnimationInfo info : list) {
				map.put(info.mTarget, info.mListener);
			}
			mMainListener = new MainAnimationListener(this, what, map, observer,
					mOrientationInvoker, params);
		}

		public AnimationTask(AnimationInfo info, OrientationInvoker orientationInvoker,
				Object[] params) {
			mAnimationInfo = info;
			mOrientationInvoker = orientationInvoker;
			HashMap<XAnimator, IAnimateListener> map = new HashMap<XAnimator, IAnimateListener>();
			map.put(info.mTarget, info.mListener);
			mMainListener = new MainAnimationListener(this, map, mOrientationInvoker, params);
		}

		public void execute() {
			mRunningTaskList.add(this);
			if (mOrientationInvoker == null) {
				OrientationControl.keepCurrentOrientation(mActivity);
			} else {
				mOrientationInvoker.keepCurrentOrientation();
			}
			if (mAnimationInfo != null) {
				mAnimationInfo.mTarget.setAnimateListener(mMainListener);
				exeAnimation(mAnimationInfo);
			} else {
				for (AnimationInfo info : mAnimationInfoList) {
					info.mTarget.setAnimateListener(mMainListener);
					exeAnimation(info);
				}
			}
		}

		private void exeAnimation(AnimationInfo info) {
			if (info.mParent != null && info.mTarget != null) {
				if (info.mType == AnimationInfo.TYPE_SIMPLE) {
					info.mParent.attachAnimator(info.mTarget);
					info.mTarget.reStart();
				} else if (info.mType == AnimationInfo.TYPE_CHANGE_POSITION) {
					info.mParent.setMotionFilter(info.mTarget);
				}
			}
		}

		public void cancel() {
			if (mRunningTaskList.contains(this)) {
				if (mAnimationInfo != null) {
					cancelAnimation(mAnimationInfo);
					mMainListener.cancel(mAnimationInfo.mTarget);
				} else {
					for (AnimationInfo info : mAnimationInfoList) {
						cancelAnimation(info);
						mMainListener.cancel(info.mTarget);
					}
				}
				
			}
		}
		
		private void cancelAnimation(AnimationInfo info) {
			if (info.mParent != null && info.mTarget != null) {
				info.mParent.detachAnimator(info.mTarget);
			}
		}
	}

	/**
	 * 
	 * <br>类描述: 动画信息类
	 * <br>功能详细描述:
	 * 
	 * @author  yangguanxiang
	 * @date  [2012-12-24]
	 */
	public static class AnimationInfo {
		public static final int TYPE_SIMPLE = 0;
		public static final int TYPE_CHANGE_POSITION = 1;
		public int mType = TYPE_SIMPLE;
		public XComponent mParent;
		public XMotion mTarget;
		public IAnimateListener mListener;

		public AnimationInfo(int type, XComponent parent, XMotion target, IAnimateListener listener) {
			mType = type;
			mParent = parent;
			mTarget = target;
			mListener = listener;
		}
	}

	/**
	 * 
	 * <br>类描述: 主动画监听器
	 * <br>功能详细描述:
	 * 
	 * @author  yangguanxiang
	 * @date  [2012-12-24]
	 */
	private class MainAnimationListener implements IAnimateListener {
		private AnimationTask mTask;
		private int mWhat;
		private HashMap<XAnimator, IAnimateListener> mMap;
		private BatchAnimationObserver mObserver;
		private OrientationInvoker mOrientationInvoker;
		private Object[] mParams;

		public MainAnimationListener(AnimationTask task, HashMap<XAnimator, IAnimateListener> map,
				OrientationInvoker orientationInvoker, Object[] params) {
			mTask = task;
			mMap = map;
			mOrientationInvoker = orientationInvoker;
			mParams = params;
		}

		public MainAnimationListener(AnimationTask task, int what,
				HashMap<XAnimator, IAnimateListener> map, BatchAnimationObserver observer,
				OrientationInvoker orientationInvoker, Object[] params) {
			this.mTask = task;
			this.mWhat = what;
			this.mMap = map;
			this.mObserver = observer;
			this.mOrientationInvoker = orientationInvoker;
			this.mParams = params;
		}

		public void cancel(XAnimator animatior) {
			synchronized (mMap) {
				if (mMap.containsKey(animatior)) {
					mMap.remove(animatior);
				}
			}
		}

		@Override
		public void onStart(XAnimator animator) {
			if (mMap != null && mMap.containsKey(animator)) {
				IAnimateListener listener = mMap.get(animator);
				if (listener != null) {
					listener.onStart(animator);
				}
			}
			if (mObserver != null) {
				mObserver.onStart(mWhat, mParams);
			}
		}

		@Override
		public void onProgress(XAnimator animator, int progress) {
			if (mMap != null && mMap.containsKey(animator)) {
				IAnimateListener listener = mMap.get(animator);
				if (listener != null) {
					listener.onProgress(animator, progress);
				}
			}
		}

		@Override
		public void onFinish(XAnimator animator) {
			if (mMap != null && mMap.containsKey(animator)) {
				IAnimateListener listener = mMap.get(animator);
				if (listener != null) {
					listener.onFinish(animator);
				}
			}
			if (mMap != null) {
				synchronized (mMap) {
					if (!mMap.isEmpty()) {
						mMap.remove(animator);
					}
					if (mMap.isEmpty()) {
						if (mObserver != null) {
							mObserver.onFinish(mWhat, mParams);
						}
						mRunningTaskList.remove(mTask);
						mTask = null;
						if (mOrientationInvoker == null) {
							OrientationControl.setOrientation(mActivity);
						} else {
							mOrientationInvoker.resetOrientation();
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * <br>类描述: 批量动画状态观察者
	 * <br>功能详细描述:
	 * 
	 * @author  yangguanxiang
	 * @date  [2012-12-24]
	 */
	public static interface BatchAnimationObserver {
		public void onStart(int what, Object[] params);

		public void onFinish(int what, Object[] params);
	}

	public synchronized static void destroy() {
		if (sInstance != null) {
			sInstance.clearAllAnimations();
			sInstance.mActivity = null;
			sInstance = null;
		}
	}
}
