package com.jiubang.ggheart.plugin.mediamanagement;

import java.util.List;

import com.jiubang.core.message.IMessageHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.plugin.mediamanagement.inf.IMediaMessageManager;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-11-23]
 */
public class MediaMessageManager implements IMediaMessageManager {

	@Override
	public boolean sendMessage(Object who, int type, int handlerId, int msgId, int param,
			Object object, List<?> objList) {

		return GoLauncher.sendMessage(who, type, handlerId, msgId, param, object, objList);
	}

	@Override
	public boolean sendMessage(Object who, int handlerId, int msgId, int param, Object object,
			List<?> objList) {

		return GoLauncher.sendMessage(who, handlerId, msgId, param, object, objList);
	}

	@Override
	public void sendBroadcastMessage(Object who, int msgId, int param, Object object,
			List<?> objList) {

		GoLauncher.sendBroadcastMessage(who, msgId, param, object, objList);
	}

	@Override
	public void sendBroadcastMessage(Object who, int type, int msgId, int param, Object object,
			List<?> objList, int[] orderedHandlers) {

		GoLauncher.sendBroadcastMessage(who, type, msgId, param, object, objList, orderedHandlers);

	}

	@Override
	public void postMessage(Object who, int handlerId, int msgId, int param, Object object,
			List<?> objList) {

		GoLauncher.postMessage(who, handlerId, msgId, param, object, objList);
	}

	@Override
	public void postBroadcastMessage(Object who, int msgId, int param, Object object,
			List<?> objList) {
		GoLauncher.postBroadcastMessage(who, msgId, param, object, objList);
	}

	@Override
	public void postBroadcastMessage(Object who, int msgId, int param, Object object,
			List<?> objList, int[] orderedHandlers) {

		GoLauncher.postBroadcastMessage(who, msgId, param, object, objList, orderedHandlers);

	}

	@Override
	public boolean registMsgHandler(IMessageHandler handler) {

		return GoLauncher.registMsgHandler(handler);
	}

	@Override
	public boolean unRegistMsgHandler(IMessageHandler handler) {

		return GoLauncher.unRegistMsgHandler(handler);
	}
}
