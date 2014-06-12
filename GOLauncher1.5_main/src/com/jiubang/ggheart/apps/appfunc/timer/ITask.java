package com.jiubang.ggheart.apps.appfunc.timer;

public interface ITask {
	public void execute(long id, long time, Object userName);

	public void finish(long id, long time, Object userName);
}
