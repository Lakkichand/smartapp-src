package com.zhidian.wifibox.file.fragment;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.file.fragment.ui.IndicatorFragmentActivity;
import com.zhidian.wifibox.listener.IOnAlbumCheckChangeListener;
import com.zhidian.wifibox.listener.IOnMusicCheckChangeListener;
import com.zhidian.wifibox.listener.IOnOtherCheckChangeListener;
import com.zhidian.wifibox.listener.IOnVideoCheckChangeListener;
import com.zhidian.wifibox.receiver.AlbumCheckChangeReceiver;
import com.zhidian.wifibox.receiver.MusicCheckChangeReceiver;
import com.zhidian.wifibox.receiver.OtherCheckChangeReceiver;
import com.zhidian.wifibox.receiver.VideoCheckChangeReceiver;

/**
 * 
 * 文件管理
 * 
 * @author shihuajian
 *
 */

public class FileManagerActivity extends IndicatorFragmentActivity implements
		IOnMusicCheckChangeListener, IOnVideoCheckChangeListener,
		IOnOtherCheckChangeListener, IOnAlbumCheckChangeListener {
	
	private final static String TAG = FileManagerActivity.class.getSimpleName();
	
	public static final int FRAGMENT_PICTURE = 0;
    public static final int FRAGMENT_MUSIC = 1;
    public static final int FRAGMENT_VIDEO = 2;
    public static final int FRAGMENT_OTHER = 3;
    
    /** 图片详情请求码 */
    public static final int REQUEST_CODE_PICTURE = 20;
    /** 图片详情回调码 */
    public static final int RESULT_CODE_PICTURE = 20;
    
    /** 是否需要刷新数据标记 */
    public static final String REFRESH_FLAG = "refreshFlag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
	protected void onResume() {
		super.onResume();
		// 页面统计
		StatService.trackBeginPage(this, getString(R.string.manager_file));
		XGPushManager.onActivityStarted(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, getString(R.string.manager_file));
		XGPushManager.onActivityStoped(this);
	}

	@Override
	protected int supplyTabs(List<TabInfo> tabs) {
		tabs.add(new TabInfo(FRAGMENT_PICTURE, getString(R.string.fragment_picture),
                FragmentPicture.class));
        tabs.add(new TabInfo(FRAGMENT_MUSIC, getString(R.string.fragment_music),
                FragmentMusic.class));
        tabs.add(new TabInfo(FRAGMENT_VIDEO, getString(R.string.fragment_video),
                FragmentVideo.class));
        tabs.add(new TabInfo(FRAGMENT_OTHER, getString(R.string.fragment_other),
        		FragmentOther.class));

        return FRAGMENT_PICTURE;
	}

	@Override
	public void setChooseListener() {
		switch (mCurrentTab) {
		case FRAGMENT_PICTURE:	// 图片
			FragmentPicture picture = (FragmentPicture) myAdapter.getItem(FRAGMENT_PICTURE);
			mFlagPicture = !mFlagPicture;
			headerChoose.setChecked(mFlagPicture);
			picture.chooseAll(mFlagPicture);
			break;
		case FRAGMENT_MUSIC:	// 音乐
			FragmentMusic music = (FragmentMusic) myAdapter.getItem(FRAGMENT_MUSIC);
			mFlagMusic = !mFlagMusic;
			headerChoose.setChecked(mFlagMusic);
			music.chooseAll(mFlagMusic);
			break;
		case FRAGMENT_VIDEO:	// 视频
			FragmentVideo video = (FragmentVideo) myAdapter.getItem(FRAGMENT_VIDEO);
			mFlagVideo = !mFlagVideo;
			headerChoose.setChecked(mFlagVideo);
			video.chooseAll(mFlagVideo);
			break;
		case FRAGMENT_OTHER:	// 其他
			FragmentOther other = (FragmentOther) myAdapter.getItem(FRAGMENT_OTHER);
			mFlagOther = !mFlagOther;
			headerChoose.setChecked(mFlagOther);
			other.chooseAll(mFlagOther);
			break;

		default:
			break;
		}
	}
	
	@Override
	public void stopMusic() {
		// 如果当前页面不等于音乐页面，则停止播放音乐
		if (mCurrentTab != FRAGMENT_MUSIC) {
			FragmentMusic fm = (FragmentMusic) myAdapter.getItem(FRAGMENT_MUSIC);
			fm.stopPlay();
		}
		
	}

	// 音乐选择监听
	@Override
	public void OnMusicCheckChange(Intent intent) {
		int chooseCount = intent.getIntExtra(MusicCheckChangeReceiver.CHOOSE_COUNT_FLAG, 0);
		int totalCount = intent.getIntExtra(MusicCheckChangeReceiver.TOTAL_COUNT, 0);
		boolean isRefresh = intent.getBooleanExtra(MusicCheckChangeReceiver.IS_REFRESH, false);
		
		if (chooseCount == totalCount) {
			if (totalCount != 0) {
				mFlagMusic = true;
				headerChoose.setChecked(mFlagMusic);
			}
		} else {
			mFlagMusic = false;
			headerChoose.setChecked(mFlagMusic);
		}
		
		FragmentMusic music = (FragmentMusic) myAdapter.getItem(FRAGMENT_MUSIC);
		music.OnMusicCheckChange(intent);
		if (isRefresh) {
			music.getData();
		}
	}

	// 其他文件选择监听
	@Override
	public void OnOtherCheckChange(Intent intent) {
		int chooseCount = intent.getIntExtra(OtherCheckChangeReceiver.CHOOSE_COUNT_FLAG, 0);
		int totalCount = intent.getIntExtra(OtherCheckChangeReceiver.TOTAL_COUNT, 0);
		boolean isRefresh = intent.getBooleanExtra(OtherCheckChangeReceiver.IS_REFRESH, false);
		
		if (chooseCount == totalCount) {
			if (totalCount != 0) {
				mFlagOther = true;
				headerChoose.setChecked(mFlagOther);
			}
		} else {
			mFlagOther = false;
			headerChoose.setChecked(mFlagOther);
		}
		
		FragmentOther other = (FragmentOther) myAdapter.getItem(FRAGMENT_OTHER);
		other.OnOtherCheckChange(intent);
		if (isRefresh) {
			other.initData();
		}
		
	}

	// 视频选择监听
	@Override
	public void OnVideoCheckChange(Intent intent) {
		int chooseCount = intent.getIntExtra(VideoCheckChangeReceiver.CHOOSE_COUNT_FLAG, 0);
		int totalCount = intent.getIntExtra(VideoCheckChangeReceiver.TOTAL_COUNT, 0);
		boolean isRefresh = intent.getBooleanExtra(VideoCheckChangeReceiver.IS_REFRESH, false);
		
		if (chooseCount == totalCount) {
			if (totalCount != 0) {
				mFlagVideo = true;
				headerChoose.setChecked(mFlagVideo);
			}
		} else {
			mFlagVideo = false;
			headerChoose.setChecked(mFlagVideo);
		}
		
		FragmentVideo video = (FragmentVideo) myAdapter.getItem(FRAGMENT_VIDEO);
		video.OnVideoCheckChange(intent);
		if (isRefresh) {
			video.initData();
		}
		
	}

	// 图片集选择监听
	@Override
	public void OnAlbumCheckChange(Intent intent) {
		int chooseCount = intent.getIntExtra(AlbumCheckChangeReceiver.CHOOSE_COUNT_FLAG, 0);
		int totalCount = intent.getIntExtra(AlbumCheckChangeReceiver.TOTAL_COUNT, 0);
		boolean isRefresh = intent.getBooleanExtra(AlbumCheckChangeReceiver.IS_REFRESH, false);
		
		if (chooseCount == totalCount) {
			if (totalCount != 0) {
				mFlagPicture = true;
				headerChoose.setChecked(mFlagPicture);
			}
		} else {
			if (totalCount != 0) {
				mFlagPicture = false;
				headerChoose.setChecked(mFlagPicture);
			}
		}
		
		FragmentPicture picture = (FragmentPicture) myAdapter.getItem(FRAGMENT_PICTURE);
		picture.OnAlbumCheckChange(intent);
		if (isRefresh) {
			picture.initData();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 根据回调是否要刷新图片数据
		if (REQUEST_CODE_PICTURE == requestCode && RESULT_CODE_PICTURE == resultCode) {
			boolean isRefresh = data.getBooleanExtra(REFRESH_FLAG, false);
			if (isRefresh) {
				FragmentPicture picture = (FragmentPicture) myAdapter.getItem(FRAGMENT_PICTURE);
				picture.initData();
			}
		}
	}

}
