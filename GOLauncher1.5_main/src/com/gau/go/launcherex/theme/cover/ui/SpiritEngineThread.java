package com.gau.go.launcherex.theme.cover.ui;

import java.util.ArrayList;
import java.util.List;
/**
 * 
 * 类描述:罩子层精灵运动引擎
 * 功能详细描述:
 * 
 * @author  guoyiqing
 * @date  [2012-10-17]
 */
public class SpiritEngineThread extends Thread implements ICleanable {

	private static final String SPIRIT_THREAD_NAME = "SpiritEngineThread";
	private List<IMovable> mSpirits = new ArrayList<IMovable>();
	private volatile boolean mIsRun;
	private int mUpdateInteval = 40;
	private static final byte[] MUTEX = new byte[0];

	public SpiritEngineThread() {
		mIsRun = true;
		setName(SPIRIT_THREAD_NAME);
	}

	public void setUpdateInterval(int updateInterval) {
		mUpdateInteval = updateInterval;
	}

	/**
	 * 功能简述:加入动态精灵
	 * @param movable
	 */
	public void loadMovable(IMovable movable) {
		synchronized (MUTEX) {
			if (mSpirits != null) {
				mSpirits.add(movable);
				mIsRun = true;
			}
		}
	}

	/**
	 * <br>功能简述:删掉动态精灵
	 * @param movable
	 */
	public void removeMovable(IMovable movable) {
		synchronized (MUTEX) {
			if (mSpirits != null) {
				mSpirits.remove(movable);
			}
		}
	}

	@Override
	public void run() {
		while (mIsRun) {
			synchronized (MUTEX) {
				for (IMovable movable : mSpirits) {
					movable.moving();
				}
			}
			try {
				Thread.sleep(mUpdateInteval);
			} catch (InterruptedException e) {
			}
		}
		super.run();
	}

	@Override
	public void cleanUp() {
		mIsRun = false;
		if (mSpirits != null) {
			synchronized (MUTEX) {
				mSpirits.clear();
				mSpirits = null;
			}
		}
	}

	public void onStop() {
		mIsRun = false;
	}

}
