package com.jiubang.ggheart.plugin.mediamanagement.inf;

import java.util.List;

import com.jiubang.core.message.IMessageHandler;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-11-16]
 */
public interface IMediaMessageManager {

	/**
	 * 功能简述:消息发送接口 功能详细描述: 注意:
	 * 
	 * @param who
	 *            发送者
	 * @param type
	 *            消息类型
	 * @param handlerId
	 *            接收的ID
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objList
	 *            传递的对象队列
	 * @return 是否处理
	 */
	public boolean sendMessage(Object who, int type, int handlerId,
			int msgId, int param, Object object, List<?> objList);
	
	/**
	 * 功能简述:同步广播消息 功能详细描述: 注意:
	 * 
	 * @param who
	 *            发送者
	 * @param handlerId
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列 return 是否处理
	 */
	public  boolean sendMessage(Object who, int handlerId, int msgId,
			int param, Object object, List<?> objList);
	
	/**
	 * 功能简述: 同步广播消息 功能详细描述: 注意:
	 * 
	 * @param who
	 *            发送者
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列
	 */
	public  void sendBroadcastMessage(Object who, int msgId, int param,
			Object object, List<?> objList);
	
	/**
	 * 功能简述: 带先后次序的同步广播消息 功能详细描述: 注意:
	 * 
	 * @param who
	 *            发送者
	 * @param type
	 *            消息类型
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列
	 * @param orderedHandlers
	 *            要求被排在前面的接收者ID数组
	 */
	public  void sendBroadcastMessage(Object who, int type, int msgId,
			int param, Object object, List<?> objList,
			final int[] orderedHandlers);
	
	/**
	 * 异步发送消息
	 * 
	 * @param who
	 *            发送者
	 * @param handlerId
	 *            接收者的ID
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列
	 */
	public void postMessage(Object who, int handlerId, int msgId,
			int param, Object object, List<?> objList);
	
	/**
	 * 异步广播消息
	 * 
	 * @param who
	 *            发送者
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列
	 */
	public void postBroadcastMessage(Object who, int msgId, int param,
			Object object, List<?> objList);
	/**
	 * 异步广播消息
	 * 
	 * @param who
	 *            发送者
	 * @param msgId
	 *            消息ID
	 * @param param
	 *            辅助参数
	 * @param object
	 *            传递的对象
	 * @param objects
	 *            传递的对象队列
	 * @param orderedHandlers
	 * 			   处理广播的对象           
	 */
	public void postBroadcastMessage(Object who, int msgId, int param,
			Object object, List<?> objList, final int[] orderedHandlers);
	
	/**
	 * 注册消息接收者
	 * 
	 * @param handler
	 * @return
	 */
	public boolean registMsgHandler(final IMessageHandler handler);
	
	/**
	 * 反注册消息接收者 与{@link GoLauncher#registMsgHandler(IMessageHandler)} 配对使用
	 * 
	 * @param handler
	 * @param msgId
	 * @return
	 */
	public boolean unRegistMsgHandler(final IMessageHandler handler);
	
}
