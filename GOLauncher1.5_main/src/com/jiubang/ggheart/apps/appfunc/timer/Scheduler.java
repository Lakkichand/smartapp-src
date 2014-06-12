package com.jiubang.ggheart.apps.appfunc.timer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Scheduler {
	public static final int TASK_TIME = 100;
	public static final int TASK_FRAME = 101;
	public static final int TASK_RENDER = 102;

	public static final int TASK_ACTIVE = 0;
	public static final int TASK_PAUSE = 1;
	public static final int TASK_DELETE = 2;

	private static final int RENDER_TASK_ID = 1;

	private static Scheduler instance;

	/**
	 * 渲染任务
	 */
	TaskInfo renderTask;
	/**
	 * 时间任务列表
	 */
	TaskInfo m_pTaskList;
	/**
	 * 帧任务列表
	 */
	TaskInfo m_pFrameList;
	long m_nextId;
	Clock m_clock;
	private ConcurrentLinkedQueue<Long> queue;

	private Scheduler() {
		m_pTaskList = null;
		m_pFrameList = null;
		// renderTask.pTask = null;
		renderTask = new TaskInfo();
		m_nextId = RENDER_TASK_ID + 1;
		m_clock = new Clock();
		queue = new ConcurrentLinkedQueue<Long>();
	}

	private void insertFrameTask(TaskInfo pTaskInfo) {
		//
		// inserted into list in time order
		//
		if (m_pFrameList == null) {
			pTaskInfo.pNext = null;
			m_pFrameList = pTaskInfo;
		} else if (m_pFrameList.time.next > pTaskInfo.time.next) {
			pTaskInfo.pNext = m_pFrameList;
			m_pFrameList = pTaskInfo;
		} else {
			TaskInfo pInfo = m_pFrameList;
			while (pInfo != null) {
				if (pInfo.pNext == null) {
					pTaskInfo.pNext = null;
					pInfo.pNext = pTaskInfo;
					break;
				} else if (pInfo.pNext.time.next > pTaskInfo.time.next) {
					pTaskInfo.pNext = pInfo.pNext;
					pInfo.pNext = pTaskInfo;
					break;
				}
				pInfo = pInfo.pNext;
			}
		}
	}

	private void insertTimeTask(TaskInfo pTaskInfo) {
		//
		// inserted into list in time order
		//
		if (m_pTaskList == null) {
			pTaskInfo.pNext = null;
			m_pTaskList = pTaskInfo;
		} else if (m_pTaskList.time.next > pTaskInfo.time.next) {
			pTaskInfo.pNext = m_pTaskList;
			m_pTaskList = pTaskInfo;
		} else {
			TaskInfo pInfo = m_pTaskList;
			while (pInfo != null) {
				if (pInfo.pNext == null) {
					pTaskInfo.pNext = null;
					pInfo.pNext = pTaskInfo;
					break;
				} else if (pInfo.pNext.time.next > pTaskInfo.time.next) {
					pTaskInfo.pNext = pInfo.pNext;
					pInfo.pNext = pTaskInfo;
					break;
				}
				pInfo = pInfo.pNext;
			}
		}
	}

	/**
	 * 
	 * @param type
	 *            任务类型(TASK_TIME，TASK_RENDER，TASK_FRAME)
	 * @param start
	 *            任务开始时间或者帧
	 * @param period
	 *            任务间隔触发时间或者帧数
	 * @param duration
	 *            任务持续时间或者帧数
	 * @param pTask
	 *            请求任务
	 * @param pUser
	 *            用户数据
	 * @return 系统生成任务ID
	 */
	public long schedule(int type, long start, int period, int duration, ITask pTask, Object pUser) {
		//
		// this schedules a task of the appropriate type (RENDER, FRAME, or
		// TIME). Time tasks and
		// frame tasks are kept in separate lists for ease of handling.
		// Render tasks are stored as a single special-case entry.
		//
		// time and frame tasks have a start time, a duration, and a period.
		// 时间和帧任务都有一个开始时间，持续时间，时间间隔。
		// the duration is relative to the start time, except for duration 0
		// which is a special case.
		// 如果持续时间为0，那么就是一个无限循环的任务
		// since the scheduler doesn't care about the duration itself, it
		// converts it into an end time
		// and stores that instead. the render task does ignores
		// start/duration/end.
		//
		// pUser is a user data pointer.
		//
		// a unique task id is generated and returned in pID. if you don't care
		// about an ID,
		// pass in NULL instead.
		//
		if (type == TASK_RENDER) {
			renderTask.pTask = pTask;
			renderTask.pUser = pUser;
			renderTask.id = RENDER_TASK_ID;
			return RENDER_TASK_ID;
		} else {
			//
			// Allocating memory like this has high overhead. It would be much
			// better to use a
			// fast allocator which preallocates and/or reuses TaskInfo
			// structures.
			//
			TaskInfo pTaskInfo = new TaskInfo();

			{
				pTaskInfo.pTask = pTask;
				pTaskInfo.pNext = m_pTaskList;
				pTaskInfo.status = TASK_ACTIVE;
				pTaskInfo.id = m_nextId++;
				// if (pID != 0)
				// pID = pTaskInfo.id;
				pTaskInfo.pUser = pUser;
				pTaskInfo.time.start = start;
				pTaskInfo.time.period = period;
				if (duration == 0) {
					pTaskInfo.time.duration = 0; // infinite
				} else {
					pTaskInfo.time.duration = start + duration - 1; // compute
				}
				// end time
				pTaskInfo.time.next = start;
				// printf("Scheduling %s task %u from %3u to %3u, every %2u %s\n",
				// type==TASK_TIME?"time ":"frame",
				// pTaskInfo.id,
				// pTaskInfo.time.start,
				// pTaskInfo.time.duration,
				// pTaskInfo.time.period,
				// type==TASK_TIME?"ms":"frames");

				if (type == TASK_TIME) {
					insertTimeTask(pTaskInfo);
				} else if (type == TASK_FRAME) {
					insertFrameTask(pTaskInfo);
				}
				return pTaskInfo.id;
			}
		}
	}

	public void terminate(long id) {
		//
		// Terminate a task. This is most useful with infinite-duration tasks,
		// but
		// it is also handy to delete finite tasks prematurely. It would be a
		// simple
		// matter to add a "suspend task" option which suspends a task instead
		// of
		// removing it.
		//
		// Terminate marks the task for deletion rather than just removing it,
		// since it may be called while ExecuteFrame is iterating through the
		// list.
		// Marked tasks are swept up at the end of each frame.
		//
		// Since all task ID's are unique, this method stops looking after it
		// finds
		// a matching task.
		//
		boolean found = false;

		if (id == RENDER_TASK_ID) {
			renderTask.pTask = null;
			found = true;
		}

		TaskInfo pTaskInfo = m_pTaskList;
		while (pTaskInfo != null && !found) {
			if (pTaskInfo.id == id) {
				pTaskInfo.status = TASK_DELETE;
				pTaskInfo.pTask.finish(pTaskInfo.id, m_clock.getTime(), pTaskInfo.pUser);
				found = true;
				break;
			}
			pTaskInfo = pTaskInfo.pNext;
		}
		pTaskInfo = m_pFrameList;
		while (pTaskInfo != null && !found) {
			if (pTaskInfo.id == id) {
				pTaskInfo.status = TASK_DELETE;
				pTaskInfo.pTask.finish(pTaskInfo.id, m_clock.getTime(), pTaskInfo.pUser);
				found = true;
				break;
			}
			pTaskInfo = pTaskInfo.pNext;
		}
	}

	public void terminateAll() {
		TaskInfo pTaskInfo = m_pTaskList;
		while (pTaskInfo != null) {
			terminate(pTaskInfo.id);
			pTaskInfo = pTaskInfo.pNext;
		}
		pTaskInfo = m_pFrameList;
		while (pTaskInfo != null) {
			terminate(pTaskInfo.id);
			pTaskInfo = pTaskInfo.pNext;
		}
	}

	/**
	 * 删除被停止的任务
	 */
	private void sweepGarbage() {
		//
		// Check both task list and frame list for tasks that were marked
		// for deletion by the Terminate() method. This implementation is
		// pretty brute-force; garbage collection could be run as an
		// idle processing task.
		//
		// printf("SWEEP BEGIN\n");
		TaskInfo pTaskInfo = m_pTaskList;
		TaskInfo pPrev = null;
		while (pTaskInfo != null) {
			if (pTaskInfo.status == TASK_DELETE) {
				TaskInfo pTemp = pTaskInfo;
				pTaskInfo = pTaskInfo.pNext;
				if (pTemp.equals(m_pTaskList)) {
					m_pTaskList = pTaskInfo;
				} else {
					pPrev.pNext = pTaskInfo;
					// printf("Sched: Deleted %d\n",pTemp->id);
					// delete pTemp;
				}
			} else {
				pPrev = pTaskInfo;
				pTaskInfo = pTaskInfo.pNext;
			}
		}

		pTaskInfo = m_pFrameList;
		pPrev = null;
		while (pTaskInfo != null) {
			if (pTaskInfo.status == TASK_DELETE) {
				TaskInfo pTemp = pTaskInfo;
				pTaskInfo = pTaskInfo.pNext;
				if (pTemp.equals(m_pFrameList)) {
					m_pFrameList = pTaskInfo;
				} else {
					pPrev.pNext = pTaskInfo;
					// printf("Sched: Deleted %d\n",pTemp->id);
					// delete pTemp;
				}
			} else {
				pPrev = pTaskInfo;
				pTaskInfo = pTaskInfo.pNext;
			}
		}
		// printf("SWEEP END\n");
	}

	public TaskInfo getNextFrameTask(TaskInfo ppTaskInfo) {
		// return the next frame task. If no frame task is scheduled to run this
		// frame,
		// return null. note that this method REMOVES the task from the list, so
		// the
		// caller is responsible for either adding it back in (rescheduling) or
		// deleting it.
		TaskInfo pNextTask = m_pFrameList;
		if (pNextTask != null && pNextTask.time.next <= m_clock.getFrame()) {
			m_pFrameList = pNextTask.pNext;
			// pNextTask.pNext = null;
			ppTaskInfo = pNextTask;
			return ppTaskInfo;
		} else {
			return null;
		}
	}

	public TaskInfo getNextTimeTask(TaskInfo ppTaskInfo) {
		// return the next time task. If no time task is scheduled to run this
		// frame,
		// return null. note that this method REMOVES the task from the list, so
		// the
		// caller is responsible for either adding it back in (rescheduling) or
		// deleting it.
		TaskInfo pNextTask = m_pTaskList;
		if (pNextTask != null && pNextTask.time.next <= m_clock.getFrameEnd()) {
			m_pTaskList = pNextTask.pNext;
			// pNextTask.pNext = null;
			ppTaskInfo = pNextTask;
			return ppTaskInfo;
		} else {
			return null;
		}
	}

	public int executeFrame() {
		// printf("EXEC BEGIN\n");
		//
		// Run one frame. This takes the time stamp marking the end of the frame
		// and then processes events for that frame retroactively. This method
		// has
		// the advantage of flexibility, especially if the frame rate
		// fluctuates.
		// However it is always a little behind, because it can't compute the
		// frame length until the end of the frame is reached. With a fixed
		// known
		// frame rate you could optimize things a bit and make the start/end
		// times
		// correspond exactly with real time.
		//
		m_clock.beginFrame();
		// long started = m_clock.GetSystem();

		//
		// Execute any time-based tasks
		//
		// (1) Pop the next task off the list. Since the list is always
		// sorted, the first item in the list is always the next task.
		// (2) Execute it and update times
		// (3) If it's expired, delete it
		// Otherwise, insert it into the list in its new position
		//
		TaskInfo pTaskInfo = null;
		TaskInfo save = m_pTaskList;
		pTaskInfo = getNextTimeTask(pTaskInfo);
		while (pTaskInfo != null) {
			m_clock.advanceTo(pTaskInfo.time.next);

			pTaskInfo.pTask.execute(pTaskInfo.id, m_clock.getTime(), pTaskInfo.pUser);
			pTaskInfo.time.last = pTaskInfo.time.next;
			pTaskInfo.time.next += pTaskInfo.time.period;
			pTaskInfo = getNextTimeTask(pTaskInfo);
		}
		m_pTaskList = save;
		pTaskInfo = m_pTaskList;
		while (pTaskInfo != null) {

			if (pTaskInfo.time.duration == 0 || pTaskInfo.time.duration >= pTaskInfo.time.next) {
				// re-insert into list with updated time
				// InsertTimeTask(pTaskInfo);
			} else {
				// task is expired, delete it
				// printf("Sched: Expired %d\n",pTaskInfo->id);
				// delete pTaskInfo;
				terminate(pTaskInfo.id);
			}
			pTaskInfo = pTaskInfo.pNext;
		}

		//
		// Advance simulation clock to end of frame
		//
		m_clock.advanceToEnd();

		//
		// Now execute all frame tasks in round-robin fashion.
		// Frame tasks always execute at the end of the frame just
		// before rendering. A priority scheme could be used to
		// control sequence. It would be more efficient to keep the
		// list sorted, the same as with time tasks (exe
		//
		save = m_pFrameList;
		pTaskInfo = m_pFrameList;
		pTaskInfo = getNextFrameTask(pTaskInfo);
		// TaskInfo * pPrev = NULL;
		while (pTaskInfo != null) {
			pTaskInfo.pTask.execute(pTaskInfo.id, m_clock.getFrame(), pTaskInfo.pUser);
			pTaskInfo.time.last = pTaskInfo.time.next;
			pTaskInfo.time.next += pTaskInfo.time.period;
			pTaskInfo = getNextFrameTask(pTaskInfo);
		}
		m_pFrameList = save;
		// 检测是否有到期的任务
		pTaskInfo = m_pFrameList;
		while (pTaskInfo != null) {
			if (pTaskInfo.time.duration == 0 || pTaskInfo.time.duration >= pTaskInfo.time.next) {
				// re-insert into list with updated time
				// InsertFrameTask(pTaskInfo);
			} else {
				// task is expired, delete it
				// printf("Sched: Expired %d\n",pTaskInfo->id);
				// delete pTaskInfo;
				terminate(pTaskInfo.id);
			}
			pTaskInfo = pTaskInfo.pNext;
		}
		m_pFrameList = save;
		//
		// render
		//
		if (renderTask.pTask != null) {
			renderTask.pTask.execute(renderTask.id, m_clock.getFrame(), renderTask.pUser);
		}

		//
		// here is where we could do idle processing or load balancing
		//
		// long elapsed = m_clock.GetSystem() - started;
		// long frameLength = m_clock.GetFrameEnd() - m_clock.GetFrameStart();
		// printf("Busy %u ms, idle %u ms\n", elapsed, frameLength - elapsed);

		//
		// If any tasks are terminated during execution, it is easier to leave
		// them in the list until we're finished iterating through it, then
		// sweep
		// them out later.
		//
		// printf("EXEC END\n");
		for (Long i : queue) {
			terminate(i);
		}
		sweepGarbage();
		return 0;
	}

	public boolean isRunning() {
		return m_clock.isRunning();
	}

	public void run() {
		m_clock.run();
	}

	public void stop() {
		m_clock.stop();
	}

	public Clock getClock() {
		return m_clock;
	}

	public static synchronized Scheduler getInstance() {
		if (instance == null) {
			instance = new Scheduler();
		}
		return instance;
	}

	public void terminateTask(long id) {
		queue.add(id);
	}
}
