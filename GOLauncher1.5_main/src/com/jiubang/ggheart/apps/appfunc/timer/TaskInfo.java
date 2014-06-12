package com.jiubang.ggheart.apps.appfunc.timer;

public class TaskInfo {
	public ITask pTask;
	public TaskInfo pNext;
	public int status; // 0 = ok, 1 = pause, 2 = delete
	public long id;
	public Object pUser;
	public Time time;

	public TaskInfo() {
		time = new Time();
	}

	class Time {
		/**
		 * 开始时间
		 */
		public long start;
		/**
		 * 时间间隔
		 */
		public int period;
		/**
		 * 持续时间
		 */
		public long duration;

		public long last;
		/**
		 * 下一帧
		 */
		public long next;
	}
}
