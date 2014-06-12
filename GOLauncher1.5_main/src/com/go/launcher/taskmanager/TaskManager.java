package com.go.launcher.taskmanager;

import android.content.Context;

public class TaskManager {
	public static synchronized ITaskManager getInstance(Context _ctx) {
		SingletonFactory factory = SingletonFactory.getFactory();
		TaskManagerImpl taskInstance = factory.getInstance(TaskManagerImpl.class);
		taskInstance.init(_ctx);
		return taskInstance;
	}
}
