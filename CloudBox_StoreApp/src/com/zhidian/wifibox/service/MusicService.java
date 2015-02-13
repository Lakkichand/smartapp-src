package com.zhidian.wifibox.service;

import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zhidian.wifibox.file.audio.MusicConnect;
import com.zhidian.wifibox.file.audio.MusicConnect.Stub;
import com.zhidian.wifibox.file.audio.MusicData;
import com.zhidian.wifibox.file.audio.MusicPlayer;

public class MusicService extends Service {
	private static final String TAG = "MusicService";
	
	private MusicPlayer m_mMusicPlayer;
	
	private SDStateBrocast mSDStateBrocast;
	
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		m_mMusicPlayer = new MusicPlayer(this);
		
		mSDStateBrocast = new SDStateBrocast();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addDataScheme("file");
		registerReceiver(mSDStateBrocast, intentFilter);
	}



	@Override
	public void onDestroy() {
		unregisterReceiver(mSDStateBrocast);
		super.onDestroy();
	}
	
	private MusicConnect.Stub mBinder = new Stub() {

		public void refreshMusicList(List<MusicData> musicFileList)
				throws RemoteException {
			m_mMusicPlayer.refreshMusicList(musicFileList);
			
		}

		public void getFileList(List<MusicData> musicFileList)
				throws RemoteException {
			List<MusicData> tmp = m_mMusicPlayer.getFileList();
			int count = tmp.size();
			for (int i = 0; i < count; i++) {
				musicFileList.add(tmp.get(i));
			}
			
		}

		@Override
		public boolean rePlay() throws RemoteException {
			return m_mMusicPlayer.replay();
		}

		@Override
		public boolean play(int position) throws RemoteException {
			Log.i(TAG, "play pos = " + position);
			return m_mMusicPlayer.play(position);
		}

		@Override
		public boolean pause() throws RemoteException {
			return m_mMusicPlayer.pause();
		}

		@Override
		public boolean stop() throws RemoteException {
			return m_mMusicPlayer.stop();
		}

		@Override
		public boolean playNext() throws RemoteException {
			return m_mMusicPlayer.playNext();
		}

		@Override
		public boolean playPre() throws RemoteException {
			return m_mMusicPlayer.playPre();
		}

		@Override
		public boolean isPlay() throws RemoteException {
			return m_mMusicPlayer.isPlay();
		}

		@Override
		public boolean seekTo(int rate) throws RemoteException {
			return m_mMusicPlayer.seekTo(rate);
		}

		@Override
		public int getCurPosition() throws RemoteException {
			return m_mMusicPlayer.getCurPosition();
		}

		@Override
		public int getDuration() throws RemoteException {
			return m_mMusicPlayer.getDuration();
		}

		@Override
		public int getPlayState() throws RemoteException {
			return m_mMusicPlayer.getPlayState();
		}

		@Override
		public int getPlayMode() throws RemoteException {
			return m_mMusicPlayer.getPlayMode();
		}

		@Override
		public void setPlayMode(int mode) throws RemoteException {
			m_mMusicPlayer.setPlayMode(mode);
			
		}

		@Override
		public void sendPlayStateBrocast() throws RemoteException {
			m_mMusicPlayer.sendPlayStateBrocast();
			
		}

		@Override
		public void exit() throws RemoteException {
			m_mMusicPlayer.exit();
			
		}
		
	};
	
	class SDStateBrocast extends BroadcastReceiver 
	{

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) 
			{
				
			} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) 
			{
				
			} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) 
			{
				
			} else if (Intent.ACTION_MEDIA_EJECT.equals(action)) 
			{
				m_mMusicPlayer.exit();
			}
			
		}
		
	}

}
