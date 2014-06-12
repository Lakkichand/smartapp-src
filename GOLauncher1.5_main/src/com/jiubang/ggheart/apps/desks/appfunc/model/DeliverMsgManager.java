package com.jiubang.ggheart.apps.desks.appfunc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.graphics.Point;

/**
 * 投递消息管理器，直接指向一个消息处理器投递消息
 * 
 * @author wenjiaming
 * 
 */
public class DeliverMsgManager {
	private static DeliverMsgManager instance;

	private HashMap<Long, IMsgHandler> mHandlerMap = new HashMap<Long, IMsgHandler>();
	private ArrayList<DispenseMsgHandler> mDispenseHandlerList = new ArrayList<DeliverMsgManager.DispenseMsgHandler>();

	/*
	 * public IMsgHandler getMsgHandler(int key) { return mHandlerMap.get(key);
	 * }
	 */

	/**
	 * 注册一个监听器
	 * 
	 * @param listener
	 * @return
	 */
	public synchronized void registerMsgHandler(long handlerID, IMsgHandler handler) {
		if (!mHandlerMap.containsKey(handlerID)) {
			mHandlerMap.put(handlerID, handler);
		}
		// else
		// {
		// try
		// {
		// throw new IllegalArgumentException(
		// "The key had exist! Register fail!!!");
		// } catch (IllegalArgumentException e)
		// {
		// e.printStackTrace();
		// }
		// }
	}

	/**
	 * 注销一个监听器
	 * 
	 * @param listener
	 * @return 是否注销成功
	 */
	public synchronized void unregisterMsgHandler(long handlerID) {
		IMsgHandler handler = mHandlerMap.remove(handlerID);
		// if (handler == null)
		// {
		// try
		// {
		// throw new IllegalStateException(
		// "The handler is not exist! Unregister fail!!!");
		// } catch (IllegalStateException e)
		// {
		// e.printStackTrace();
		// }
		// }
	}

	public void onChange(long handlerID, int msgID, Object obj) {
		IMsgHandler handler = mHandlerMap.get(handlerID);
		if (handler != null) {
			handler.notify(msgID, obj);
		}
		// else
		// {
		// try
		// {
		// throw new IllegalStateException(
		// "The handler is not exist! notify fail!!!");
		// } catch (IllegalStateException e)
		// {
		// e.printStackTrace();
		// }
		// }
	}

	// ///////////////////////////////////////////////////////////////////////////////
	/**
	 * 注册一个群发的消息处理器
	 * 
	 * @param startId
	 *            消息ID的开始范围
	 * @param endId
	 *            消息ID的结束范围
	 * @param handler
	 *            消息处理器
	 */
	public synchronized void registerDispenseMsgHandler(int startId, int endId, IMsgHandler handler) {
		mDispenseHandlerList.add(new DispenseMsgHandler(new Point(startId, startId), handler));
	}

	/**
	 * 注册一个群发的消息处理器
	 * 
	 * @param point
	 *            封装了消息开始和结束范围的对象
	 * @param handler
	 *            消息处理器
	 */
	public synchronized void registerDispenseMsgHandler(Point point, IMsgHandler handler) {
		mDispenseHandlerList.add(new DispenseMsgHandler(point, handler));
	}

	/**
	 * 注册一个群发的消息处理器
	 * 
	 * @param handlerID
	 *            消息ID(开始和结束一样)
	 * @param handler
	 *            消息处理器
	 */
	public synchronized void registerDispenseMsgHandler(int handlerID, IMsgHandler handler) {
		mDispenseHandlerList.add(new DispenseMsgHandler(new Point(handlerID, handlerID), handler));
	}

	/**
	 * 反注册一个群发的消息处理器
	 * 
	 * @param point
	 *            封装了消息开始和结束范围的对象
	 */
	public synchronized void unRegisterDispenseMsgHandler(Point point) {
		Iterator<DispenseMsgHandler> iterator = mDispenseHandlerList.iterator();
		while (iterator.hasNext()) {
			DispenseMsgHandler handler = iterator.next();
			if (handler.mPoint.x == point.x && handler.mPoint.y == point.y) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * 反注册一个群发的消息处理器
	 * 
	 * @param startId
	 *            消息ID的开始范围
	 * @param endId
	 *            消息ID的结束范围
	 */
	public synchronized void unRegisterDispenseMsgHandler(int startId, int endId) {
		Iterator<DispenseMsgHandler> iterator = mDispenseHandlerList.iterator();
		while (iterator.hasNext()) {
			DispenseMsgHandler handler = iterator.next();
			if (handler.mPoint.x == startId && handler.mPoint.y == endId) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * 反注册一个群发的消息处理器,只删除对应的这个
	 * 
	 * @param startId
	 *            消息ID的开始范围
	 * @param handler
	 *            消息处理对象
	 */
	public synchronized void unRegisterDispenseMsgHandler(int handlerID, Object imHandler) {
		Iterator<DispenseMsgHandler> iterator = mDispenseHandlerList.iterator();
		while (iterator.hasNext()) {
			DispenseMsgHandler handler = iterator.next();
			if (handler.mPoint.x == handlerID && handler.mPoint.y == handlerID
					&& handler.mHandler == imHandler) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * 反注册一个群发的消息处理器
	 * 
	 * @param handlerID
	 *            消息ID(开始和结束一样)
	 */
	public synchronized void unRegisterDispenseMsgHandler(int handlerID) {
		Iterator<DispenseMsgHandler> iterator = mDispenseHandlerList.iterator();
		while (iterator.hasNext()) {
			DispenseMsgHandler handler = iterator.next();
			if (handler.mPoint.x == handlerID && handler.mPoint.y == handlerID) {
				iterator.remove();
				break;
			}
		}
	}

	/**
	 * 消息发送
	 * 
	 * @param msgId
	 *            消息ID
	 * @param obj
	 *            附带的内容
	 */
	public synchronized void sendMsg(int msgId, Object obj) {
		for (DispenseMsgHandler handler : mDispenseHandlerList) {
			if (handler.mPoint.x >= msgId && handler.mPoint.y <= msgId) {
				handler.mHandler.notify(msgId, obj);
			}
		}
	}

	public class DispenseMsgHandler {
		Point mPoint;
		IMsgHandler mHandler;

		public DispenseMsgHandler(Point point, IMsgHandler handler) {
			mPoint = point;
			mHandler = handler;
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////

	public static synchronized DeliverMsgManager getInstance() {
		if (instance == null) {
			instance = new DeliverMsgManager();
		}
		return instance;
	}

	public static synchronized void destroyInstance() {

		if (instance != null) {
			synchronized (instance) {
				instance.mHandlerMap.clear();
				instance.mDispenseHandlerList.clear();
				instance = null;
			}
		}
	}

}
