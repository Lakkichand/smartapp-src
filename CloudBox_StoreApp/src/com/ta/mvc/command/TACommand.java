/*
 * Copyright (C) 2013  WhiteCat 白猫 (www.thinkandroid.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ta.mvc.command;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TAResponse;

public abstract class TACommand extends TABaseCommand {
	protected final static int command_start = 1;
	protected final static int command_runting = 2;
	protected final static int command_failure = 3;
	protected final static int command_success = 4;
	protected final static int command_finish = 5;
	private TAIResponseListener listener;
	private Handler handler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case command_start:
				listener.onStart();
				break;
			case command_runting:
				listener.onRuning(new TAResponse(null, msg.obj));
				break;
			case command_success:
				listener.onSuccess(new TAResponse(null, msg.obj));
				break;
			case command_failure:
				listener.onFailure(new TAResponse(null, msg.obj));
				break;
			case command_finish:
				listener.onFinish();
				break;
			default:
				break;
			}
		};

	};

	public final void execute() {
		onPreExecuteCommand();
		executeCommand();
		onPostExecuteCommand();
	}

	protected abstract void executeCommand();

	protected void onPreExecuteCommand() {
		sendStartMessage();
	}

	protected void onPostExecuteCommand() {

	}

	protected void sendMessage(int state) {
		listener = getResponseListener();
		if (listener != null) {
			handler.sendEmptyMessage(state);
		}
	}

	public void sendStartMessage() {
		sendMessage(command_start);
	}

	public void sendSuccessMessage(Object object) {
		listener = getResponseListener();
		if (listener != null) {
			Message.obtain(handler, command_success, object).sendToTarget();
		}
	}

	public void sendFailureMessage(Object object) {
		listener = getResponseListener();
		if (listener != null) {
			Message.obtain(handler, command_failure, object).sendToTarget();
		}
	}

	public void sendRuntingMessage(Object object) {
		listener = getResponseListener();
		if (listener != null) {
			Message.obtain(handler, command_runting, object).sendToTarget();
		}
	}

	public void sendFinishMessage() {
		sendMessage(command_finish);
	}
}
