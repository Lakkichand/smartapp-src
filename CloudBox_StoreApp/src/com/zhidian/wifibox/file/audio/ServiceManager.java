package com.zhidian.wifibox.file.audio;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.zhidian.wifibox.file.audio.MusicConnect;
import com.zhidian.wifibox.listener.IOnServiceConnectComplete;

public class ServiceManager {

	private final static String TAG = "ServiceManager";
	
	private final static String SERVICE_NAME = "com.zhidian.wifibox.file.audio.service.musicservices";
	
	private Boolean mConnectComplete;
	
	private ServiceConnection mServiceConnection;
	
	private MusicConnect mMusicConnect;
	
	private IOnServiceConnectComplete mIOnServiceConnectComplete;
	
	private Context mContext;
	
	public ServiceManager(Context context) {
		mContext = context;
		defaultParam();
	}
	
	private void defaultParam() {
		mServiceConnection = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mMusicConnect = MusicConnect.Stub.asInterface(service);
				if (mMusicConnect != null) {
					if (mIOnServiceConnectComplete != null) {
						mIOnServiceConnectComplete.OnServiceConnectComplete();
					}
				}
			}
		};
		
		mConnectComplete = false;
		mMusicConnect = null;
	}
	
	public boolean connectService() {
		if (mConnectComplete == true) {
			return true;
		}
		
		Intent intent = new Intent(SERVICE_NAME);
		if (mContext != null) {
			mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
			mContext.startService(intent);
			mConnectComplete = true;
		}
		return false;
	}
	
	public boolean disconnectService() {
		if (mConnectComplete == false) {
			return true;
		}
		
		Intent intent = new Intent(SERVICE_NAME);
		if (mContext != null) {
			mContext.unbindService(mServiceConnection);
			mMusicConnect = null;
			mConnectComplete = false;
			return true;
		}
		
		return false;
	}
	
	public void exit() {
		if (mConnectComplete) {
			try {
				mMusicConnect.exit();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Intent intent = new Intent(SERVICE_NAME);
			mContext.unbindService(mServiceConnection);
			mContext.stopService(intent);
			mMusicConnect = null;
			mConnectComplete = false;
		}
	}
	
	public void reset() {
		if (mConnectComplete) {
			try {
				mMusicConnect.exit();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setOnServiceConnectComplete(IOnServiceConnectComplete IServiceConnect) {
		mIOnServiceConnectComplete = IServiceConnect;
	}
	
	public void refreshMusicList(List<MusicData> FileList) {
		if (mConnectComplete) {
			try {
				mMusicConnect.refreshMusicList(FileList);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public List<MusicData> getFileList() {
		List<MusicData> musicFileList = new ArrayList<MusicData>();
		try {
			mMusicConnect.getFileList(musicFileList);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return musicFileList;
	}
	
	public boolean isPlay() {
		if (mConnectComplete != null) {
			try {
				return mMusicConnect.isPlay();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean rePlay() {
		if (mConnectComplete != null) {
			try {
				return mMusicConnect.rePlay();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean play(int position) {
		if (mConnectComplete != null) {
			try {
				return mMusicConnect.play(position);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean pause() {
		if (mConnectComplete != null) {
			try {
				return mMusicConnect.pause();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean stop() {
		if (mConnectComplete != null) {
			try {
				return mMusicConnect.stop();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean playNext() {
		if (mConnectComplete != null) {
			try {
				return mMusicConnect.playNext();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean playPre() {
		if (mConnectComplete != null) {
			try {
				return mMusicConnect.playPre();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public boolean seekTo(int rate) {
		if (mConnectComplete != null) {
			try {
				return mMusicConnect.seekTo(rate);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false ;
	}
	
	public int getCurPosition()
	{
		if (mMusicConnect != null)
		{
			try {
				return mMusicConnect.getCurPosition();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
		return 0;
	}
	
	public int getDuration()
	{
		if (mMusicConnect != null)
		{
			try {
				return mMusicConnect.getDuration();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return 0;
	}
	
	public int getPlayState()
	{
		if (mMusicConnect != null)
		{
			try {
				return mMusicConnect.getPlayState();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		
		return MusicPlayState.MPS_NOFILE;
	}

	public void setPlayMode(int mode)
	{

		if (mMusicConnect != null)
		{
			try {
				mMusicConnect.setPlayMode(mode);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public int getPlayMode()
	{
		if (mMusicConnect != null)
		{
			try {
				return mMusicConnect.getPlayMode();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
		
		return MusicPlayMode.MPM_SINGLE_STOP_PLAY;
	}
	
	public void sendPlayStateBrocast()
	{
		if (mMusicConnect != null)
		{
			try {
				mMusicConnect.sendPlayStateBrocast();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
	
}
