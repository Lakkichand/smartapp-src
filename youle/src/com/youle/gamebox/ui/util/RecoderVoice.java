package com.youle.gamebox.ui.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RecoderVoice {
    /* 录制的音频文件 */
    private File mRecAudioFile;
    // private File mRecAudioPath;
    /* MediaRecorder对象 */
    private MediaRecorder mMediaRecorder;
    private MediaPlayer mediaPlayer;
    /* 零时文件的前缀 */
    private String strTempFile = "recaudio_";
    private Handler handler = new Handler();
    private Set<View> disImageList = new HashSet<View>();
    private IRecorderListener recorderListener;
    private  IPlayListener playListener ;
    public interface IRecorderListener {
        public void startRecorder(MediaRecorder recorder);

        public void stopedRecorder(MediaRecorder mr);
    }
    private File playingFile ;
    public interface IPlayListener{
        public void startPlay(File file);
        public void playing(File file);
        public void endPlay(File file) ;
    }

    public void setRecorderListener(IRecorderListener recorderListener) {
        this.recorderListener = recorderListener;
    }

    public void setPlayListener(IPlayListener playListener) {
        this.playListener = playListener;
    }

    public RecoderVoice() {
    }

    public void  start(Context mContext)  {
        try {
            if(recorderListener!=null) {
                recorderListener.startRecorder(mMediaRecorder);
            }
            File dir = new File(getCachDir(mContext));
            dir.mkdirs();
            /* 创建录音文件 */
            mRecAudioFile = new File(dir.getAbsolutePath() + File.separator
                    + UUID.randomUUID().toString() + ".amr");
            mRecAudioFile.createNewFile();
            if (mMediaRecorder != null) {
                mMediaRecorder.reset();
            }
			/* 实例化MediaRecorder对象 */
                mMediaRecorder = new MediaRecorder();
            mMediaRecorder.reset();
			/* 设置麦克风 */
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			/* 设置输出文件的格式 */
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			/* 设置音频文件的编码 */
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			/* 设置输出文件的路径 */
            mMediaRecorder.setOutputFile(mRecAudioFile.getAbsolutePath());
            mMediaRecorder.setMaxDuration(90000);
			/* 准备 */
            mMediaRecorder.prepare();
			/* 开始 */
            mMediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String stop() {

        if (mRecAudioFile != null && mMediaRecorder != null) {
            mMediaRecorder.reset();
            if(recorderListener!=null) {
                recorderListener.stopedRecorder(mMediaRecorder);
            }
            mMediaRecorder = null;
            return mRecAudioFile.getAbsolutePath();
        }
        return null;
    }

    /* 播放录音文件 */
    public void playMusic(File file) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.reset();
        }

        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(false);
            mediaPlayer.start();
            this.playingFile = file ;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        } else {
            return mediaPlayer.isPlaying();
        }
    }

    private Runnable playVoice = new Runnable() {
        @Override
        public void run() {
            if (playListener!=null){
                playListener.playing(playingFile);
            }
        }
    };

    private String getCachDir(Context mContext) {
        StringBuilder sb = new StringBuilder();
        String sdcardpath = Environment.getExternalStorageDirectory().getPath();
        if (sdcardpath != null) {
            return sb.append(sdcardpath).append(File.separator).append("zhidian")
                    .append(File.separator).append("voice").toString();
        } else {
            return sb.append(mContext.getCacheDir().getPath())
                    .append(File.separator).append("zhidian")
                    .append(File.separator).append("voice").toString();
        }
    }

    public void stopVoice() {
        if ( mediaPlayer != null) {// 没有停止
            mediaPlayer.pause();
            mediaPlayer.stop();
            mediaPlayer.reset();
            handler.removeCallbacks(playVoice);
            mediaPlayer = null ;
        }
    }


    private OnPreparedListener onPreparedListener = new OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            if(playListener!=null){
                playListener.startPlay(playingFile);
            }
            handler.post(playVoice);
        }
    };

    private OnCompletionListener onCompletionListener = new OnCompletionListener() {

        @Override
        public void onCompletion(MediaPlayer mp) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            handler.removeCallbacks(playVoice);
            if(playListener!=null){
                playListener.endPlay(playingFile);
            }
        }
    };

}

