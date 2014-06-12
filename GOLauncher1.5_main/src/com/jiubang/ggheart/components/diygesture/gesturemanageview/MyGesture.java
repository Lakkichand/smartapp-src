package com.jiubang.ggheart.components.diygesture.gesturemanageview;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureStroke;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureInfo;
import com.jiubang.ggheart.components.diygesture.model.DiyGestureModelImpl;
import com.jiubang.ggheart.data.info.ShortCutInfo;
import com.jiubang.ggheart.data.info.SysAppInfo;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 */
public class MyGesture extends Activity implements OnClickListener {
	private DiyGestureModelImpl mBusiness;
	MyGestureListAdapter mMyGestureListAdapter;
	private ImageView mFingerBtn = null; // 手指按钮
	private ImageView mAddBtn = null; // “+”按钮
	private ListView mGestureListView = null; // 手势列表
	private LinearLayout mAddGestureTips = null; // 没有手势时的提示
	private ArrayList<DiyGestureInfo> mGestureList; // 列表数据源
	private int mDrawPosition = -1; // 当前画的位置
	private int mFirstVisiablePos = -1; // ListView显示的首位
	private int mLastVisiablePos = -1; // ListView显示的最后位置
	private int mLength = 0; // ListView显示的总个数
	private Gesture mNeedAutoDrawGesture = null; // 自动画预览图对应的手势

	private DiyGestureItemView mImageView = null; // 需自动画预览图的imageview
	private Path mDrawPath = null;
	private byte[] mLock = new byte[0]; // 锁

	private boolean mIsCanceled = false; // 是否需要停止自动画预览图线程的标注位
	private DrawBitmapTask mAsyncTask = null; // 动画线程
	private DrawBitmapTaskOneItem mAsyncTaskOneItem = null; // 单个手势动画线程
	private boolean mIsCanceledOneItem = false; // 是否需要停止自动画预览图线程的标注位
	private long mTotalSleepTime = 800; // 每个动画所执行的总时间

	private final float mTOUCH_TOLERANCE = 3; // 2个点间的距离阀值
	private final int mMESSAGEID_SET_IMAGET_BITMAP = 0; // 设置每个手势动画
	private final int mMESSAGEID_AUTO_DRAW_PREVIEW_ONE_ITEM = 1; // 点击单个手势动画

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_gesture);

		mBusiness = DiyGestureModelImpl.getInstance(this);
		DiyGestureModelImpl.addFlag(DiyGestureModelImpl.sFLAG_MANAGER);
		initView();
	}

	private void initView() {
		mGestureList = mBusiness.getAllDiyGestureInfoList();

		mMyGestureListAdapter = new MyGestureListAdapter(this, mGestureList);
		int gestureListSize = mMyGestureListAdapter.getCount();
		if (gestureListSize <= 0) {
			mAddGestureTips = (LinearLayout) findViewById(R.id.no_gesture_info);
			mAddGestureTips.setVisibility(View.VISIBLE);
		} else {
			initGestureList(mMyGestureListAdapter);
		}

		mFingerBtn = (ImageView) findViewById(R.id.my_gesture_finger);
		mFingerBtn.setOnClickListener(this);
		mAddBtn = (ImageView) findViewById(R.id.my_gesture_plus);
		mAddBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		// 输入手势按钮
			case R.id.my_gesture_finger :
				Intent intent = new Intent(MyGesture.this, DiyGestureRecogniser.class);
				startActivityForResult(intent, DiyGestureConstants.RECOGNISE_REQUEST_CODE);
				cancelItemMenu();
				break;

			// 添加手势按钮
			case R.id.my_gesture_plus :
				startActivity(new Intent(MyGesture.this, DiyGestureAddActivity.class));
				cancelItemMenu();
				break;

			default :
				break;
		}
	}

	/**
	 * 取消点击打开的item菜单
	 */
	public void cancelItemMenu() {
		if (mMyGestureListAdapter != null) {
			mMyGestureListAdapter.setGestureMenuPosition(-1);
			mMyGestureListAdapter.notifyDataSetChanged();
		}
	}

	private void initGestureList(final MyGestureListAdapter adapter) {
		mGestureListView = (ListView) findViewById(R.id.my_gesture_list);
		mGestureListView.setAdapter(adapter);
		mGestureListView.setOnScrollListener(new ListViewScrollListener());
		mGestureListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				cancelTask(); // 点击的时候就取消动画线程
				cancelTaskOneItem();
				mHandler.removeMessages(mMESSAGEID_AUTO_DRAW_PREVIEW_ONE_ITEM); // 取消点击单个手势动画的消息

				if (adapter.getGestuureMenuPosition() != position) {
					adapter.setGestureMenuPosition(position);
					Message msg = new Message();
					msg.what = mMESSAGEID_AUTO_DRAW_PREVIEW_ONE_ITEM;
					msg.obj = view;
					msg.arg1 = position;
					mHandler.sendMessageDelayed(msg, 150);
					checkSelectLastOne(position);
				} else {
					adapter.setGestureMenuPosition(-1);
				}
				adapter.notifyDataSetInvalidated();
			}
		});
		autoDrawPreview(); // 启动手势动画线程
	}

	/**
	 * 检查是否选择最后2项，是就往上移
	 * 
	 * @param position
	 */
	public void checkSelectLastOne(int position) {
		int selectPosition = 0;
		if (mLastVisiablePos - 1 == position) {
			selectPosition = position - mLength + 2;
		}
		if (mLastVisiablePos - 2 == position) {
			selectPosition = position - mLength + 3;
		}

		if (selectPosition > 0) {
			int moveHeight = (int) MyGesture.this.getResources().getDimension(
					R.dimen.gesture_select_last_move_height);
			mGestureListView.setSelectionFromTop(selectPosition, moveHeight);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mGestureListView != null) {
			MyGestureListAdapter adapter = (MyGestureListAdapter) mGestureListView.getAdapter();
			if (adapter.getCount() > 0 && mAddGestureTips != null) {
				mAddGestureTips.setVisibility(View.GONE);
			}
			adapter.notifyDataSetInvalidated();
		} else {
			initGestureList(new MyGestureListAdapter(this, mGestureList));
		}
	}

	@Override
	protected void onPause() {
		cancelTask();
		cancelTaskOneItem();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cancelTask();
		cancelTaskOneItem();
		DiyGestureModelImpl.removeFlag(DiyGestureModelImpl.sFLAG_MANAGER);
		DiyGestureModelImpl.checkClear();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case DiyGestureConstants.APP_REQUEST_CODE : {
				if (resultCode == RESULT_OK) {
					Intent intent = data.getParcelableExtra(DiyGestureConstants.APP_INTENT);
					String name = data.getStringExtra(DiyGestureConstants.APP_NAME);
					MyGestureListAdapter adapter = (MyGestureListAdapter) mGestureListView
							.getAdapter();
					int position = adapter.getGestuureMenuPosition();
					if (0 <= position && position < adapter.getCount()) {
						String typeName = getString(R.string.gesture_app);
						DiyGestureInfo info = (DiyGestureInfo) adapter.getItem(position);
						info.setType(DiyGestureConstants.TYPE_APP);
						info.setIntent(intent);
						info.setTypeName(typeName);
						info.setName(name);
						DiyGestureModelImpl business = DiyGestureModelImpl.getInstance(this);
						business.modifyGestureResetAction(info);
						adapter.notifyDataSetInvalidated();
					}
				}
			}
				break;

			case DiyGestureConstants.GOSHORTCUT_REQUEST_CODE : {
				if (resultCode == RESULT_OK) {
					Intent intent = data.getParcelableExtra(DiyGestureConstants.APP_INTENT);
					String name = data.getStringExtra(DiyGestureConstants.APP_NAME);
					MyGestureListAdapter adapter = (MyGestureListAdapter) mGestureListView
							.getAdapter();
					int position = adapter.getGestuureMenuPosition();
					if (0 <= position && position < adapter.getCount()) {
						String typeName = getString(R.string.gesture_goshortcut);
						DiyGestureInfo info = (DiyGestureInfo) adapter.getItem(position);
						info.setType(DiyGestureConstants.TYPE_GOSHORTCUT);
						info.setIntent(intent);
						info.setTypeName(typeName);
						info.setName(name);
						DiyGestureModelImpl business = DiyGestureModelImpl.getInstance(this);
						business.modifyGestureResetAction(info);
						adapter.notifyDataSetInvalidated();
					}
				}
			}
				break;

			case DiyGestureConstants.SHORTCUT_PAGE2_REQUEST_CODE : {
				// 处理快捷方式
				if (resultCode == Activity.RESULT_OK) {
					// 获取出intent中包含的应用
					final ShortCutInfo info = infoFromShortcutIntent(this, data);
					if (info != null) {
						data = info.mIntent;
						if (data != null) {
							String name = (String) info.mTitle;
							MyGestureListAdapter adapter = (MyGestureListAdapter) mGestureListView
									.getAdapter();
							int position = adapter.getGestuureMenuPosition();
							if (0 <= position && position < adapter.getCount()) {
								String typeName = getString(R.string.gesture_shortcut);
								DiyGestureInfo diyGestureInfo = (DiyGestureInfo) adapter
										.getItem(position);
								diyGestureInfo.setType(DiyGestureConstants.TYPE_SHORTCUT);
								diyGestureInfo.setIntent(data);
								diyGestureInfo.setTypeName(typeName);
								diyGestureInfo.setName(name);
								DiyGestureModelImpl business = DiyGestureModelImpl
										.getInstance(this);
								business.modifyGestureResetAction(diyGestureInfo);
								adapter.notifyDataSetInvalidated();
							}
						}
					}
				}
				break;
			}

			case DiyGestureConstants.SHORTCUT_PAGE1_REQUEST_CODE : {
				if (resultCode == RESULT_OK) {
					startActivityForResult(data, DiyGestureConstants.SHORTCUT_PAGE2_REQUEST_CODE);
				}
			}
				break;

			case DiyGestureConstants.RECOGNISE_REQUEST_CODE : {
				if (resultCode == RESULT_OK) {
					finish();
				}
			}
				break;

			default :
				break;
		}
	}

	private ShortCutInfo infoFromShortcutIntent(Context context, Intent data) {
		return SysAppInfo.createFromShortcut(context, data);
	}

	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 */
	class ListViewScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			cancelTask();
			cancelTaskOneItem();
			switch (scrollState) {
				case OnScrollListener.SCROLL_STATE_FLING :
					// Log.i("lch1:","正在滚动：SCROLL_STATE_FLING");
					break;

				// 停止滚动时，但拖动到最后或者最上时监听不了
				case OnScrollListener.SCROLL_STATE_IDLE :
					mFirstVisiablePos = mGestureListView.getFirstVisiblePosition();
					mLastVisiablePos = mGestureListView.getLastVisiblePosition();
					// autoDrawPreview();
					// Log.i("lch1",
					// "停止滚动........mFirstVisiablePos="+mFirstVisiablePos+",mLastVisiablePos="+mLastVisiablePos);
					break;

				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL :
					// Log.i("lch1:","开始滚动：SCROLL_STATE_TOUCH_SCROLL");
					break;

				default :
					break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
			mFirstVisiablePos = firstVisibleItem;
			mLastVisiablePos = firstVisibleItem + visibleItemCount;
			mLength = visibleItemCount;
			// Log.i("lch1",
			// "onScroll........mFirstVisiablePos="+mFirstVisiablePos+",mLastVisiablePos="+mLastVisiablePos
			// +",mLength = " +mLength);
		}

	}

	private void autoDrawPreview() {
		if (mFirstVisiablePos == -1) {
			return;
		}

		if (mFirstVisiablePos == 0 && mLastVisiablePos == -1) {
			mLastVisiablePos = 6;
		}

		if (mAsyncTask != null && mAsyncTask.getStatus() != DrawBitmapTask.Status.FINISHED) {
			mIsCanceled = true;
			mAsyncTask.cancel(true);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		mIsCanceled = false;
		mAsyncTask = new DrawBitmapTask();
		mAsyncTask.execute();
	}

	/**
	 * 
	 * <br>类描述：绘制手势动画类
	 * <br>功能详细描述:
	 * 
	 * @author  ruxueqin
	 * @date  [2013-3-1]
	 */
	class DrawBitmapTask extends AsyncTask<Void, Path, String> {

		@Override
		protected void onProgressUpdate(Path... values) {
			if (!mIsCanceled) {
				Path path = values[0];
				mImageView.updateGestureAnimation(path);
			}
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			while (!mIsCanceled) {
				// 判断是否已经加载完毕，
				if (mLength != 0) {
					break;
				} else {
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				// View view = mGestureListView.getChildAt(0);
				// if (view != null) {
				// break;
				// }
			}
			// mLength = mLastVisiablePos - mFirstVisiablePos;

			for (int i = 0; i < mLength; i++) {
				if (mIsCanceled) {
					break; // 取消线程终止循环
				}

				View view = mGestureListView.getChildAt(i); // 获取ListView当前显示的View
				if (view != null) {
					// ViewHolder holder = (ViewHolder) view.getTag();
					//
					// if (holder != null) {
					// mImageView = holder.iconImageView;

					mImageView = (DiyGestureItemView) view.findViewById(R.id.my_gesture_item_icon);
					mDrawPosition = mFirstVisiablePos + i;
					if (mDrawPosition > mGestureList.size() - 1) {
						mDrawPosition = mGestureList.size() - 1;
					}

					mNeedAutoDrawGesture = mGestureList.get(mDrawPosition).getmGesture();

					synchronized (mLock) {
						if (mIsCanceled) {
							return null;
						}
						mHandler.sendEmptyMessage(mMESSAGEID_SET_IMAGET_BITMAP);
						try {
							mLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					mDrawPath = new Path();
					ArrayList<GestureStroke> strokes = mNeedAutoDrawGesture.getStrokes();

					long sleepTime = getSleepTime(strokes);
					int strokesSize = strokes.size(); // 手势的笔画数
					for (int m = 0; m < strokesSize; m++) {
						if (mIsCanceled) {
							return null;
						}
						GestureStroke stroke = strokes.get(m);
						float[] points = stroke.points; // 笔画中路径坐标
						float mX = 0;
						float mY = 0;

						int pointsSize = points.length;

						for (int j = 0; j < pointsSize; j += 2) {
							if (mIsCanceled) {
								return null;
							}
							float x = points[j]; // X坐标
							float y = points[j + 1]; // Y坐标
							if (j == 0) {
								mDrawPath.moveTo(x, y);
								mX = x;
								mY = y;
							} else {
								float dx = Math.abs(x - mX);
								float dy = Math.abs(y - mY);
								// 判断2个点是否>一定值，如果设置过大会导致不圆滑
								if (dx >= mTOUCH_TOLERANCE || dy >= mTOUCH_TOLERANCE) {
									mDrawPath.lineTo(x, y);
									mX = x;
									mY = y;

									if (mIsCanceled) {
										return null;
									}
									synchronized (mLock) {
										publishProgress(mDrawPath);
										try {
											SystemClock.sleep(sleepTime);
											if (mIsCanceled) {
												return null;
											}
											mLock.wait();
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
						// }
					}
				}
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}

	}

	/**
	 * 取消手势动画线程
	 */
	private void cancelTask() {
		if (mAsyncTask != null && mAsyncTask.getStatus() != DrawBitmapTask.Status.FINISHED) {
			mIsCanceled = true;
			mAsyncTask.cancel(true);
			mAsyncTask = null;
			if (mDrawPosition >= mFirstVisiablePos && mDrawPosition <= mLastVisiablePos) {
				if (mGestureList != null) {
					mImageView.setGestureImageView(mNeedAutoDrawGesture);
				}
			}
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case mMESSAGEID_SET_IMAGET_BITMAP :
					if (!mIsCanceled) {
						mImageView.setGestureImageView(mNeedAutoDrawGesture);
					}
					synchronized (mLock) {
						mLock.notifyAll();
					}
					break;

				// 点击单个手势动画
				case mMESSAGEID_AUTO_DRAW_PREVIEW_ONE_ITEM :
					int position = msg.arg1;
					// View view = (View) msg.obj;

					// 必须要用这样的方法。如果用适配器刷新了。获取的传递过来的View会有问题
					View view = mGestureListView.getChildAt(position - mFirstVisiablePos);
					if (view != null) {
						mNeedAutoDrawGesture = mGestureList.get(position).getmGesture();
						mImageView = (DiyGestureItemView) view
								.findViewById(R.id.my_gesture_item_icon);
						mImageView.setGestureImageView(mNeedAutoDrawGesture);
						autoDrawPreviewOneItem();
					}

					break;

				default :
					break;
			}
		};
	};

	/**
	 * 点击单个选项手势动画
	 */
	private void autoDrawPreviewOneItem() {
		mIsCanceledOneItem = false;
		mAsyncTaskOneItem = new DrawBitmapTaskOneItem();
		mAsyncTaskOneItem.execute();
	}

	/**
	 * 取消单个手势动画
	 */
	private void cancelTaskOneItem() {
		if (mAsyncTaskOneItem != null
				&& mAsyncTaskOneItem.getStatus() != DrawBitmapTaskOneItem.Status.FINISHED) {
			mIsCanceledOneItem = true;
			mAsyncTaskOneItem.cancel(true);
			mAsyncTaskOneItem = null;

			if (mGestureList != null) {
				mImageView.setGestureImageView(mNeedAutoDrawGesture);
			}
		}
	}

	/**
	 * 单个手势动画
	 * 
	 * @author licanhui
	 * 
	 */

	class DrawBitmapTaskOneItem extends AsyncTask<Void, Path, String> {

		@Override
		protected void onProgressUpdate(Path... values) {
			if (!mIsCanceledOneItem) {
				Path path = values[0];
				mImageView.updateGestureAnimation(path);
			}
			synchronized (mLock) {
				mLock.notifyAll();
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			ArrayList<GestureStroke> strokes = mNeedAutoDrawGesture.getStrokes();
			mDrawPath = new Path();
			long sleepTime = getSleepTime(strokes);
			int strokesSize = strokes.size(); // 手势的笔画数
			for (int m = 0; m < strokesSize; m++) {
				if (mIsCanceledOneItem) {
					return null;
				}
				GestureStroke stroke = strokes.get(m);
				float[] points = stroke.points; // 笔画中路径坐标
				float mX = 0;
				float mY = 0;

				int pointsSize = points.length;

				for (int j = 0; j < pointsSize; j += 2) {
					if (mIsCanceledOneItem) {
						return null;
					}
					float x = points[j]; // X坐标
					float y = points[j + 1]; // Y坐标
					if (j == 0) {
						mDrawPath.moveTo(x, y);
						mX = x;
						mY = y;
					} else {
						float dx = Math.abs(x - mX);
						float dy = Math.abs(y - mY);
						// 判断2个点是否>一定值，如果设置过大会导致不圆滑
						if (dx >= mTOUCH_TOLERANCE || dy >= mTOUCH_TOLERANCE) {
							mDrawPath.lineTo(x, y);
							mX = x;
							mY = y;

							if (mIsCanceledOneItem) {
								return null;
							}
							synchronized (mLock) {
								publishProgress(mDrawPath);
								try {
									SystemClock.sleep(sleepTime);
									if (mIsCanceledOneItem) {
										return null;
									}
									mLock.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}

			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
		}
	}

	/**
	 * 获取每个手势动画运行时间
	 * 
	 * @param strokes
	 * @return
	 */
	private long getSleepTime(ArrayList<GestureStroke> strokes) {
		long sleepTime = 0;
		int totalSize = 0;
		int size = strokes.size();
		for (int i = 0; i < size; i++) {
			GestureStroke stroke = strokes.get(i);
			float[] points = stroke.points; // 每笔画中路径坐标
			totalSize = totalSize + points.length; // 坐标点总的数量
		}
		sleepTime = mTotalSleepTime / (totalSize / 2); // 每个点延迟的时间
		return sleepTime;
	}
}
