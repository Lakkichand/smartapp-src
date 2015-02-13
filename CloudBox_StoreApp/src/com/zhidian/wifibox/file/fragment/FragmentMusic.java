package com.zhidian.wifibox.file.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.ManagerMusicAdapter;
import com.zhidian.wifibox.controller.FileManagerController;
import com.zhidian.wifibox.file.audio.AudioHelper;
import com.zhidian.wifibox.file.audio.MusicData;
import com.zhidian.wifibox.file.audio.MusicPlayMode;
import com.zhidian.wifibox.file.audio.MusicPlayState;
import com.zhidian.wifibox.file.audio.MusicPlayer;
import com.zhidian.wifibox.file.audio.MusicTimer;
import com.zhidian.wifibox.file.audio.ServiceManager;
import com.zhidian.wifibox.listener.IOnServiceConnectComplete;
import com.zhidian.wifibox.receiver.MusicCheckChangeReceiver;
import com.zhidian.wifibox.util.IntentUtils;
import com.zhidian.wifibox.util.ToastUtils;

public class FragmentMusic extends Fragment implements OnClickListener,
		IOnServiceConnectComplete, OnItemClickListener {
	
	private final static String TAG = FragmentMusic.class.getSimpleName();
	private final static int REFRESH_PROGRESS_EVENT = 0x100;

	private View mView;
	private Context mContext;

	private ListView mMusic;
	private LinearLayout mBottom;
	private Button mDelAll;
	private ProgressBar mLoading;
	private ManagerMusicAdapter mMusAdapter;

	private Handler mHandler;
	private MusicTimer mMusicTimer;
	private ServiceManager mServiceManager;
	private MusicPlayStateBrocast mPlayStateBrocast;
	private MusicCheckChangeReceiver mMusicCheckChangeBrocast;
	private SDStateBrocast mSDStateBrocast;
	private AudioHelper helper;
	private List<MusicData> m_MusicFileList;
	private boolean mIsSdExist = false;
	private boolean mIsHaveData = false;
	private int mCurMusicTotalTime = 0;
	private int mCurPlayMode = MusicPlayMode.MPM_SINGLE_STOP_PLAY;
	
	private TAIResponseListener mRListener = new TAIResponseListener() {

		@Override
		public void onSuccess(TAResponse response) {
			mLoading.setVisibility(View.GONE);
			m_MusicFileList.clear();
			m_MusicFileList = (List<MusicData>) response.getData();
			mServiceManager.refreshMusicList(m_MusicFileList);
			mMusAdapter.refreshAdapter(m_MusicFileList);
			if (mIsHaveData) {
				if (m_MusicFileList.size() > 0) {
					mIsHaveData = true;
				}
			}
			setListEmptyView();
		}

		@Override
		public void onStart() {}

		@Override
		public void onRuning(TAResponse response) {}

		@Override
		public void onFinish() {}

		@Override
		public void onFailure(TAResponse response) {}
	};

    public FragmentMusic() {
        super();
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	mContext = activity;
    	helper = AudioHelper.getInstance();
    	helper.init(activity);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	mView = inflater.inflate(R.layout.fragment_music, container, false);
    	return mView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onActivityCreated(savedInstanceState);
    	
    	init();
    }
    
    /** 初始化 */
    private void init() {
    	mMusic = (ListView) mView.findViewById(R.id.music);
    	mBottom = (LinearLayout) mView.findViewById(R.id.bottom);
    	mDelAll = (Button) mView.findViewById(R.id.delectAll);
    	mLoading = (ProgressBar) mView.findViewById(R.id.loading);
    	
    	mDelAll.setOnClickListener(this);
    	mMusic.setOnItemClickListener(this);
    	
    	mHandler = new Handler() {
    		@Override
    		public void handleMessage(Message msg) {
    			switch (msg.what) {
				case REFRESH_PROGRESS_EVENT:
					mMusAdapter.setPlayInfo(mServiceManager.getCurPosition());
					break;

				default:
					break;
				}
    		}
    		
    	};
    	
    	mMusicTimer = new MusicTimer(mHandler, REFRESH_PROGRESS_EVENT);
    	
    	m_MusicFileList = new ArrayList<MusicData>();
    	mMusAdapter = new ManagerMusicAdapter(mContext, m_MusicFileList, mServiceManager, mMusicTimer, helper);
    	mMusic.setAdapter(mMusAdapter);

		registerBrocast();

	}

    /**
     * 设置列表空视图
     */
	private void setListEmptyView() {
		View emptyView = LayoutInflater.from(mContext).inflate(R.layout.file_manager_empty_prompt, mMusic, false);  
    	ViewGroup parentView = (ViewGroup) mMusic.getParent();  
    	parentView.addView(emptyView);
    	mMusic.setEmptyView(emptyView);
	}

    @Override
    public void onResume() {
    	super.onResume();
    }

    /**
     * 注册广播
     */
	private void registerBrocast() {
		mPlayStateBrocast = new MusicPlayStateBrocast();
		IntentFilter intentFilter1 = new IntentFilter(MusicPlayer.BROCAST_NAME);
		mContext.registerReceiver(mPlayStateBrocast, intentFilter1);

		mSDStateBrocast = new SDStateBrocast();
		IntentFilter intentFilter2 = new IntentFilter();
		intentFilter2.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter2.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter2.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter2.addAction(Intent.ACTION_MEDIA_EJECT);
		mContext.registerReceiver(mSDStateBrocast, intentFilter2);

		mMusicCheckChangeBrocast = new MusicCheckChangeReceiver();
		IntentFilter intentFilter3 = new IntentFilter(
				MusicCheckChangeReceiver.PATH_NAME);
		mContext.registerReceiver(mMusicCheckChangeBrocast, intentFilter3);
		
		mServiceManager = new ServiceManager(mContext);
		mServiceManager.setOnServiceConnectComplete(this);
		mServiceManager.connectService();
		mMusAdapter.setServiceManager(mServiceManager);
	}
    
    @Override
    public void onPause() {
    	stopPlay();
    	super.onPause();
    }

    /**
     * 停止播放音乐
     */
	public void stopPlay() {
		if (mMusAdapter != null && mServiceManager.isPlay()) {
    		mMusAdapter.setPlayState(-1, MusicPlayState.MPS_PREPARE, 0);
    		mMusAdapter.stop();
			mMusicTimer.stopTimer();
    		mMusAdapter.notifyDataSetChanged();
    	}
	}

	@Override
	public void onStart() {
		super.onStart();
	}
    
	@Override
	public void onStop() {
		super.onStop();
	}
	
    @Override
	public void onDestroy() {
		unregisterBrocast();
		super.onDestroy();
	}

	private void unregisterBrocast() {
		exit();
		if (mMusicTimer != null) {
			mMusicTimer.stopTimer();
		}
		if (mPlayStateBrocast != null) {
			mContext.unregisterReceiver(mPlayStateBrocast);
		}
		if (mSDStateBrocast != null) {
			mContext.unregisterReceiver(mSDStateBrocast);
		}
		if (mMusicCheckChangeBrocast != null) {
			mContext.unregisterReceiver(mMusicCheckChangeBrocast);
		}
		if (mServiceManager != null) {
			mServiceManager.disconnectService();
		}
	}
	
	public void showNoData() {
		Toast.makeText(mContext, "No Music Data...", Toast.LENGTH_SHORT).show();
	}
	
	public void rePlay() {
		if (mIsHaveData == false) {
			showNoData();
		} else {
			mServiceManager.rePlay();
		}
	}
	
	public void play(int position) {
		if (mIsHaveData == false) {
			showNoData();
		} else {
			mServiceManager.play(position);
		}
	}
	
	public void pause() {
		mServiceManager.pause();
	}
	
	public void stop() {
		mServiceManager.stop();
	}
	
	public void playPre() {
		if (mIsHaveData == false) {
			showNoData();
		} else {
			mServiceManager.playPre();
		}
	}
	
	public void playNext() {
		if (mIsHaveData == false) {
			showNoData();
		} else {
			mServiceManager.playNext();
		}
	}
	
	public void seekTo(int rate) {
		mServiceManager.seekTo(rate);
	}
	
	public void exit() {
		mServiceManager.exit();
	}
	
	public void modeChange() {
		mCurPlayMode++;
		if (mCurPlayMode > MusicPlayMode.MPM_SINGLE_STOP_PLAY) {
			mCurPlayMode = MusicPlayMode.MPM_SINGLE_LOOP_PLAY;
		}
		
		mServiceManager.setPlayMode(mCurPlayMode);
	}
	
	/** 获取数据 */
	public void getData() {
//		mLoading.setVisibility(View.VISIBLE);
		TARequest request = new TARequest(FileManagerController.GET_FILE_MUSIC, helper);
		TAApplication.getApplication().doCommand(
				mContext.getString(R.string.filemanagercontroller), 
				request, 
				mRListener, 
				true, 
				false);
	}

	@Override
	public void OnServiceConnectComplete() {
		String state = Environment.getExternalStorageState().toString();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			mIsSdExist = true;
		} else {
			Toast.makeText(mContext, "SD卡未安装，建议安装SD卡", Toast.LENGTH_SHORT).show();
			return;
		}

		int playState = mServiceManager.getPlayState();
		switch (playState) {
		case MusicPlayState.MPS_NOFILE:	// 无文件
			mLoading.setVisibility(View.GONE);
			m_MusicFileList.clear();
			mMusAdapter.refreshAdapter(m_MusicFileList);
			getData();
			break;
		case MusicPlayState.MPS_INVALID:
			break;
		case MusicPlayState.MPS_PREPARE:
//			mLoading.setVisibility(View.VISIBLE);
//			getData();
			break;
		case MusicPlayState.MPS_PLAYING:
			break;
		case MusicPlayState.MPS_PAUSE:
			mLoading.setVisibility(View.GONE);
			m_MusicFileList = mServiceManager.getFileList();
			mServiceManager.sendPlayStateBrocast();
			break;
		default:
			break;
		}

		if (m_MusicFileList.size() > 0) {
			mIsHaveData = true;
		}

		mMusAdapter.refreshAdapter(m_MusicFileList);

	}
    
    

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.delectAll:
			if (mMusAdapter != null) {
				mMusAdapter.chooseDel();
			}
			break;

		default:
			break;
		}
		
	}
	
	/**
	 * 实现全选/反选功能
	 */
	public void chooseAll(boolean isAll) {
		if (mMusAdapter != null) {
			mMusAdapter.chooseAll(isAll);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.music:
			MusicData item = (MusicData) parent.getAdapter().getItem(position);
			if (item != null) {
				String path = item.mMusicPath;
				File file = new File(path);
				Intent intent = IntentUtils.createFileOpenIntent(file);
				try {
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
					ToastUtils.showShortToast(mContext, mContext.getString(R.string.file_not_found));
				}
			}
			break;

		default:
			break;
		}
		
	}
	
	public void OnMusicCheckChange(Intent intent) {
		int count = intent.getIntExtra(MusicCheckChangeReceiver.CHOOSE_COUNT_FLAG, 0);
		mDelAll.setText(getString(R.string.manager_delete_all, count));
		if (count == 0) {
			mBottom.setVisibility(View.GONE);
		} else {
			mBottom.setVisibility(View.VISIBLE);
		}
	}
    
    class SDStateBrocast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				mIsSdExist = true;
			} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
				mIsSdExist = false;
			} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
				if (mIsSdExist) {
					m_MusicFileList.clear();
					TARequest request = new TARequest(FileManagerController.GET_FILE_MUSIC, helper);
					TAApplication.getApplication().doCommand(
							mContext.getString(R.string.filemanagercontroller), 
							request, 
							mRListener, 
							true, 
							false);
				}
			} else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
				m_MusicFileList.clear();
				mMusAdapter.refreshAdapter(m_MusicFileList);
				mIsHaveData = false;
			}

		}

	}
    
    class MusicPlayStateBrocast extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MusicPlayer.BROCAST_NAME)) {
				TransPlayStateEvent(intent);
			}
		}

		public void TransPlayStateEvent(Intent intent) {
			MusicData data = new MusicData();
			int playState = intent.getIntExtra(MusicPlayState.PLAY_STATE_NAME,
					-1);
			Bundle bundle = intent.getBundleExtra(MusicData.KEY_MUSIC_DATA);
			if (bundle != null) {
				data = bundle.getParcelable(MusicData.KEY_MUSIC_DATA);
			}
			int playIndex = intent.getIntExtra(MusicPlayState.PLAY_MUSIC_INDEX,
					-1);
			
			switch (playState) {
			case MusicPlayState.MPS_INVALID:
				mMusicTimer.stopTimer();
				Toast.makeText(mContext, "当前音乐文件无效", Toast.LENGTH_SHORT).show();
				break;
			case MusicPlayState.MPS_PREPARE:
				mMusicTimer.stopTimer();
				playIndex = -1;
				mCurMusicTotalTime = data.mMusicTime;
				if (mCurMusicTotalTime == 0) {
					mCurMusicTotalTime = mServiceManager.getDuration();
				}
				break;
			case MusicPlayState.MPS_PLAYING:
				mMusicTimer.startTimer();;
				if (mCurMusicTotalTime == 0) {
					mCurMusicTotalTime = mServiceManager.getDuration();
				}
				break;
			case MusicPlayState.MPS_PAUSE:
				mMusicTimer.stopTimer();
				if (mCurMusicTotalTime == 0) {
					mCurMusicTotalTime = mServiceManager.getDuration();
				}
				break;
			case MusicPlayState.MPS_STOP:
				mMusicTimer.stopTimer();
				mCurMusicTotalTime = 0;
				playIndex = -1;
				break;
			default:
				break;
			}
			
			mMusAdapter.setPlayState(playIndex, playState, mServiceManager.getCurPosition());
			
		}

	}

}
