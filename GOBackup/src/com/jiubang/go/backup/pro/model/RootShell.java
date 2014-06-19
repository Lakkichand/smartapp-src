package com.jiubang.go.backup.pro.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import android.text.TextUtils;

import com.jiubang.go.backup.pro.util.Util;

/**
 * @author maiyongshen
 */
public class RootShell {
	private static final int MAX_WAIT_TIME = 30 * 1000;
	private static final String CMD_TOKEN = "^@#*end*#@^";
	private static final int STATE_ROOT_TIME_OUT = 100;
	private static final int STATE_ROOT_DENIED = 101;
	private static final int STATE_ROOTED = 1;
	private int mState;

	private Process mProcess;
	private BufferedReader mProcessInput = null;
	private BufferedWriter mProcessOutput = null;
	private boolean mClosed = false;
	private Command mCurrentCommand = null;
	private Boolean mIsRootValid = null;
	private static RootShell sInstance = null;
	private static boolean sUserDenied = false;
	private List<Command> mPendingCommands = new ArrayList<RootShell.Command>();
	
	private RootShell(String cmd) throws IOException, TimeoutException, RootDeniedException {
		mProcess = new ProcessBuilder(cmd).redirectErrorStream(true).start();
		mProcessInput = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
		mProcessOutput = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));
		
		mState = STATE_ROOT_TIME_OUT;
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					mProcessOutput.write("echo StartShell\n");
					mProcessOutput.flush();
					final String emptyLine = "";
					while (true) {
						String line = mProcessInput.readLine();
						if (line == null) {
							throw new EOFException();
						}
						if (emptyLine.equals(line)) {
							continue;
						}
						if (line.equals("StartShell")) {
							mState = STATE_ROOTED;
							break;
						}
					}
				} catch (IOException e) {
					mProcess.destroy();
					mState = STATE_ROOT_DENIED;
				}
			}
		});
		thread.start();
		try {
			thread.join(MAX_WAIT_TIME);
			if (mState == STATE_ROOT_TIME_OUT) {
				mProcess.destroy();
				throw new TimeoutException();
			} else if (mState == STATE_ROOT_DENIED) {
				mProcess.destroy();
				throw new RootDeniedException("User denied");
			} else if (mState == STATE_ROOTED) {
				new Thread(mProcessReader).start();
				new Thread(mProcessWriter).start();
			}
			
		} catch (InterruptedException e) {
			thread.interrupt();
			throw new TimeoutException();
		}
	}
	
	public static synchronized RootShell startShell() throws IOException, TimeoutException, RootDeniedException {
		if (sInstance == null) {
			int retries = 0;
			while (sInstance == null) {
				try {
					sInstance = new RootShell("su");
				} catch (IOException e) {
					if (retries++ >= 3) {
						throw e;
					}
				}
			}
		}
		return sInstance;
	}
	
	public static boolean isRootValid() {
		String result = null;
		if (sUserDenied) {
			return false;
		}
		if (sInstance != null && sInstance.mIsRootValid != null) {
			return sInstance.mIsRootValid;
		}
		try {
			result = new Command("id").execute(startShell());
		} catch (IOException e) {
//			LogUtil.d("get Root io exception");
			e.printStackTrace();
		} catch (TimeoutException e) {
//			LogUtil.d("get Root time out");
			e.printStackTrace();
		} catch (RootDeniedException e) {
			sUserDenied = true;
//			LogUtil.d("get Root user denied");
			e.printStackTrace();
		}
		if (!TextUtils.isEmpty(result)) {
			Set<String> usersId = new HashSet<String>(Arrays.asList(result.split(" ")));
			for (String id : usersId) {
				if (id.toLowerCase().contains("uid=0")) {
					if (sInstance != null) {
						sInstance.mIsRootValid = true;
					}
					return true;
				}
			}
		} else {
			if (sInstance != null) {
				sInstance.mIsRootValid = false;
			}
		}
        return false;
	}
	
	private Runnable mProcessReader = new Runnable() {
		@Override
		public void run() {
			StringBuilder sb = null;
//			LogUtil.d("ProcessReader thread id " + Thread.currentThread().getId());
			while (true) {
				try {
					synchronized (mPendingCommands) {
						if (mClosed) {
							break;
						}
					}
					if (sb == null) {
						sb = new StringBuilder();
					}
//					LogUtil.d("before readLine");
					String line = mProcessInput.readLine();
//					LogUtil.d("after readLine line = " + line);
					if (line == null) {
						break;
					}
					if (mCurrentCommand == null) {
//						LogUtil.d("ProcessReader command is null, continue");
						continue;
					}
					int tokenIndex = line.indexOf(CMD_TOKEN);
//					LogUtil.d("ProcessReader token index = " + tokenIndex);
					if (tokenIndex >0) {
						sb.append(line.substring(0, tokenIndex));
					}
					if (tokenIndex >= 0) {
						line = line.substring(tokenIndex);
						String fields[] = line.split(" ");
						if (fields != null && fields.length >= 2 && fields[1] != null) {
							int id = -1;
							try {
								id = Integer.parseInt(fields[1]);
							} catch (NumberFormatException e) {}
							
							int exitCode = -1;
							try {
								exitCode = Integer.parseInt(fields[2]);
							} catch (NumberFormatException e) {}
							
							if (id == mCurrentCommand.getId()) {
								String result = sb.toString();
								mCurrentCommand.setResult(exitCode, result);
								sb = null;
								mCurrentCommand = null;
								continue;
							}
						}
					}
					sb.append(line).append("\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
//			LogUtil.d("ProcessReader destory process");
			if (!Util.isCollectionEmpty(mPendingCommands)) {
				Command remainCmd = null;
				while ((remainCmd = mPendingCommands.remove(0)) != null) {
					remainCmd.terminate("Unexpected termination");
				}
			}
			try {
				mProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				mProcess.destroy();
			}
		}
	};
	
	private Runnable mProcessWriter = new Runnable() {
		@Override
		public void run() {
			int index = 0;
//			LogUtil.d("ProcessWriter thread id " + Thread.currentThread().getId());
			while (true) {
				synchronized (mPendingCommands) {
					try {
						while (!mClosed && mPendingCommands.size() < 1) {
//							LogUtil.d("ProcessWriter wait");
							mPendingCommands.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					if (mClosed) {
						mProcessOutput.write("\n exit 0\n");
						mProcessOutput.flush();
						mProcessOutput.close();
						return;
					} else if (mPendingCommands.size() > 0) {
						mCurrentCommand = mPendingCommands.remove(0);
						mCurrentCommand.setId(index);
//						LogUtil.d("ProcessWriter write command " + mCurrentCommand.getContent());
						mProcessOutput.write(mCurrentCommand.getContent() + "\necho " + CMD_TOKEN
								+ " " + mCurrentCommand.getId() + " $?\n");
						mProcessOutput.flush();
						index++;
					}
				} catch (IOException e) {
					exit();
					return;
				}
			}
		}
	};
	
	private void enqueueCommand(Command command) {
//		LogUtil.d("enqueueCommand thread id " + Thread.currentThread().getId());
		synchronized (mPendingCommands) {
			mPendingCommands.add(command);
//			LogUtil.d("enqueueCommand notify");
			mPendingCommands.notifyAll();
		}
	}
	
	public static void writeCommand(Command command) throws IOException, TimeoutException, RootDeniedException {
		startShell().enqueueCommand(command);
	}
	
	public void exit() {
		synchronized (mPendingCommands) {
			mClosed = true;
			sInstance = null;
			mIsRootValid = null;
			mPendingCommands.notifyAll();
		}
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static class RootDeniedException extends Exception {
		public RootDeniedException(String error) {
			super(error);
		}
	}
	
	/**
	 * @author maiyongshen
	 *
	 */
	public static class Command {
		public static final int EXIT_CODE_NORMAL = 1;
		public static final int EXIT_CODE_EXCEPTIOIN = -1;
		public static final int TIME_OUT = 30 * 1000;
		private int mExitCode;
		private int mId = -1;
		private String mResult;
		private String mContent;
		private boolean mFinished;
		
		public Command(String cmd) {
			mContent = cmd + "\n";
			mFinished = false;
			mResult = "";
		}
		
		public void setId(int id) {
			mId = id;
		}
		
		public int getId() {
			return mId;
		}
		
		public String getContent() {
			return mContent;
		}
		
		public int getExitCode() {
			return mExitCode;
		}
		
		public void setResult(int exitCode, String result) {
			synchronized (this) {
				mExitCode = exitCode;
				mResult = result;
				mFinished = true;
				notifyAll();
			}
		}
		
		public String execute(RootShell shell) {
			return execute(shell, TIME_OUT);
		}
		
		public String execute(RootShell shell, int timeOut) {
			try {
//				LogUtil.d("excute command thread id = " + Thread.currentThread().getId());
				RootShell.writeCommand(this);
				synchronized (this) {
					try {
						while (!mFinished) {
//							LogUtil.d("excute command wait");
							wait(timeOut);
							if (!mFinished) {
								mFinished = true;
								terminate("execute command time out!");
								shell.exit();
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				terminate("start root process failed!");
			} catch (TimeoutException e) {
				terminate("start root process time out!");
			} catch (RootDeniedException e) {
				terminate("user denied!");
			}
			return mResult;
		}
		
		public void terminate(String message) {
			// 打印日志
			setResult(-1, null);
		}
	}
	
}
