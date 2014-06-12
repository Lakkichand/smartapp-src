package com.jiubang.ggheart.apps.appfunc.timer;

public class Clock {
	boolean m_running;

	long m_thisTime;
	long m_lastTime;

	/**
	 * 真实时间
	 */
	int m_systemTime;
	long m_systemOffset;
	int m_pauseTime;

	/**
	 * 帧数
	 */
	int m_frameCount;
	/**
	 * 一个帧的开始时间
	 */
	int m_frameStart;
	/**
	 * 一个帧的结束时间
	 */
	int m_frameEnd;

	long m_simTime;
	int m_simOffset;

	public void reset() {
		m_running = false;

		m_thisTime = System.currentTimeMillis();
		m_lastTime = m_thisTime;

		m_systemTime = 0;
		m_pauseTime = 0;
		m_systemOffset = m_thisTime;

		m_frameCount = 0;
		m_frameStart = 0;
		m_frameEnd = 0;

		m_simTime = 0;
		m_simOffset = 0;
	}

	public void run() {
		if (!m_running) {
			update();
			m_simOffset += (m_systemTime - m_pauseTime);
			// printf("Clock: started, %u ms elapsed since last stop\n",
			// m_systemTime - m_pauseTime);
		}
		m_running = true;
	}

	public void stop() {
		if (m_running) {
			update();
			m_pauseTime = m_systemTime;
			// printf("Clock: stopped, paused at %u ms\n", m_pauseTime);
		}
		m_running = false;
	}

	public void update() {
		// get windows' idea of current time
		long elapsed = 0;
		m_lastTime = m_thisTime;
		m_thisTime = System.currentTimeMillis();

		// convert to elapsed time
		// also handle roll over, which happens every 2^32 milliseconds
		if (m_thisTime < m_lastTime) {
			elapsed = m_lastTime - m_thisTime;
		} else {
			elapsed = m_thisTime - m_lastTime;
		}

		// system time is real time and never pauses
		m_systemTime += elapsed;
		// printf("<CLOCK> update, system=%u, elapsed=%u\n", m_systemTime,
		// elapsed);
	}

	public void beginFrame() {
		// Begin a new frame. This method is normally called
		// immediately AFTER rendering the previous frame, and
		// sets the context for all work which will be rendered
		// on the next frame.
		//
		// This method increments the frame count and samples real
		// time to determine the elapsed time since the last frame.
		//
		// <Render frame n>
		// <Begin frame n+1>
		// <Task1>
		// <Task2>
		// <Task3...>
		// <Render frame n+1>

		update(); // update system time
		if (m_running) {
			m_frameCount++;

			m_frameStart = m_frameEnd; // start of this frame = end of last
										// frame
			m_frameEnd = m_systemTime - m_simOffset; // end of this frame =
														// current time
			m_simTime = m_frameStart; // set simulation time to start of frame
		}
		// printf("Clock: begin frame %u (begin %u, end %u), sys=%u, sim=%u (%u)\n",
		// m_frameCount, m_frameStart, m_frameEnd, m_systemTime, m_simTime,
		// m_simOffset);
	}

	public void advanceTo(long newTime) {
		if (m_running && newTime >= m_simTime) {
			m_simTime = newTime;
			// printf("<CLOCK> advance to %u\n", m_simTime);
		}
	}

	public void advanceToEnd() {
		if (m_running) {
			m_simTime = m_frameEnd;
			// printf("<CLOCK> advance to %u (end)\n", m_simTime);
		}
	}

	/**
	 * 获取真实系统时间
	 * 
	 * @return
	 */
	public long getSystem() {
		update();
		return m_systemTime;
	}

	public long getTime() {
		return m_simTime;
	}

	public long getFrame() {
		return m_frameCount;
	}

	public long getFrameStart() {
		return m_frameStart;
	}

	public long getFrameEnd() {
		return m_frameEnd;
	}

	public boolean isRunning() {
		return m_running;
	}
}
