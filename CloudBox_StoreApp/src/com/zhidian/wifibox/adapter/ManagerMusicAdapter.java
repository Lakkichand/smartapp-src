package com.zhidian.wifibox.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.FileDetailsBean;
import com.zhidian.wifibox.file.audio.AudioHelper;
import com.zhidian.wifibox.file.audio.MusicData;
import com.zhidian.wifibox.file.audio.MusicPlayState;
import com.zhidian.wifibox.file.audio.MusicTimer;
import com.zhidian.wifibox.file.audio.ServiceManager;
import com.zhidian.wifibox.receiver.MusicCheckChangeReceiver;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.IntentUtils;
import com.zhidian.wifibox.util.ToastUtils;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog;
import com.zhidian.wifibox.view.dialog.DeleteHintDialog.GoonCallBackListener;
import com.zhidian.wifibox.view.dialog.FileDetailsPopupWindow;
import com.zhidian.wifibox.view.dialog.WaitingDialog;

/**
 * 
 * 音乐列表适配器
 * 
 * @author shihuajian
 *
 */
public class ManagerMusicAdapter extends BaseAdapter {
	
	private final static String TAG = ManagerMusicAdapter.class.getSimpleName();

	private List<MusicData> mDataList = new ArrayList<MusicData>();
	private List<MusicData> mDataListDel = new ArrayList<MusicData>();
	private Context mContext;
	/** 当前播放Id */
	private int mCurPlayMusicIndex;
	/** 当前播放状态 */
	private int mPlayState;
	/** 当前播放时间 */
	private int mCurPlayMusicTime;
	/** 当前打开的下拉的Id */
	private int mCurListOpenIndex;
	/** 是否有列表被打开 */
	private boolean mCurListIsOpen;
	private ServiceManager mServiceManager;
	private MusicTimer mMusicTimer;
	private AudioHelper mHelper;
	
	private Handler mHandlerUpdate = new Handler(Looper.getMainLooper());
	
	private FileDetailsPopupWindow pop;
	
	public ManagerMusicAdapter(Context context, List<MusicData> dataList,
			ServiceManager sm, MusicTimer musicTimer, AudioHelper helper) {
		this.mDataList = dataList;
		this.mDataListDel = new ArrayList<MusicData>();
		this.mContext = context;
		this.mCurPlayMusicIndex = -1;
		this.mCurListOpenIndex = -1;
		this.mCurListIsOpen = false;
		this.mCurPlayMusicTime = 0;
		this.mPlayState = MusicPlayState.MPS_PREPARE;
		this.mServiceManager = sm;
		this.mMusicTimer = musicTimer;
		pop = new FileDetailsPopupWindow((Activity)context);
		this.mHelper = helper;
	}
	
	public void setServiceManager(ServiceManager sm) {
		this.mServiceManager = sm;
	}
	
	/** 实现全选 */
	public void chooseAll(boolean isAll) {
		if (mDataList != null) {
			for (MusicData data : mDataList) {
				data.mIsDel = isAll;
			}
			
			if (mDataListDel != null) {
				if (isAll) {
					mDataListDel.clear();
					mDataListDel.addAll(mDataList);
				} else {
					mDataListDel.clear();
				}
			}
			notifyDataSetChanged();
			sendMusicBroadcast(false);
		}
	}

	/**
	 * 发送选择广播
	 */
	private void sendMusicBroadcast(boolean isRefresh) {
		Intent intent = new Intent(MusicCheckChangeReceiver.PATH_NAME);
		intent.putExtra(MusicCheckChangeReceiver.CHOOSE_COUNT_FLAG, mDataListDel.size());
		intent.putExtra(MusicCheckChangeReceiver.TOTAL_COUNT, getCount());
		intent.putExtra(MusicCheckChangeReceiver.IS_REFRESH, isRefresh);
		mContext.sendBroadcast(intent);
	}
	
	/** 删除选择的数据 */
	public void chooseDel() {
		if (mDataListDel != null) {
			String countTip = mContext.getString(R.string.delete_hint_music, mDataListDel.size() + "");
			final WaitingDialog waiting = new WaitingDialog(mContext);
			DeleteHintDialog dialog = new DeleteHintDialog(mContext, countTip);
			dialog.setGoonCallBackListener(new GoonCallBackListener() {
				
				@Override
				public void onClick() {
					waiting.show();
					// 关闭打开的Item
					setCurListIsOpen(-1);
					// 停止播放音乐
					setPlayState(-1, MusicPlayState.MPS_PREPARE, 0);
					stop();
					mMusicTimer.stopTimer();
					
					new Thread() {
						public void run() {
							final long startTime = System.currentTimeMillis();
							final List<MusicData> xData = new ArrayList<MusicData>();
							xData.addAll(mDataList);
							for (int i = 0; i < mDataListDel.size(); i++) {
								MusicData data = mDataListDel.get(i);
								if (FileUtil.DeleteFolder(data.mMusicPath)) {
									mHelper.delete(data.mId + "");
								}
							}
							if (xData.removeAll(mDataListDel)) {
								mDataListDel.clear();
							}
							FileUtil.scanSdCard(mContext);
							mHandlerUpdate.post(new Runnable() {
								
								@Override
								public void run() {
									refreshAdapter(xData);
									sendMusicBroadcast(true);
									long endTime = System.currentTimeMillis();
									if ((endTime - startTime) < 1000) {
										waiting.close();
									} else {
										waiting.dismiss();
									}
								}
							});
						};
					}.start();
					
				}
			});
			dialog.show();
		}
	}
	
	public void refreshAdapter(List<MusicData> FileList) {
		mDataList = new ArrayList<MusicData>();
		if (FileList != null) {
			mDataList.addAll(FileList);
		}
		mDataListDel.clear();
		sendMusicBroadcast(false);
		notifyDataSetChanged();
	}
	
	/** 设置播放状态 */
	public void setPlayState(int playIndex, int playState, int curPlayTime) {
		mCurPlayMusicIndex = playIndex;
		mPlayState = playState;
		mCurPlayMusicTime = curPlayTime;
		notifyDataSetChanged();
	}
	
	public int getCurPlayIndex() {
		return mCurPlayMusicIndex;
	}
	
	public int getCurPlayState() {
		return mPlayState;
	}

	public void clear() {
		mDataList.clear();
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (mDataList == null || mDataList.size() <= 0)
			return 0;
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext)
					.inflate(R.layout.list_item_manager_music, parent, false);
			holder.headerPadding = (LinearLayout) convertView.findViewById(R.id.headerPadding);
			holder.bottomPadding = (LinearLayout) convertView.findViewById(R.id.bottomPadding);
			holder.choose = (ImageButton) convertView.findViewById(R.id.choose);
			holder.play = (ImageView) convertView.findViewById(R.id.play);
			holder.songName = (TextView) convertView.findViewById(R.id.songName);
			holder.songTime = (TextView) convertView.findViewById(R.id.songTime);
			holder.curSongTime = (TextView) convertView.findViewById(R.id.curSongTime);
			holder.songSize = (TextView) convertView.findViewById(R.id.songSize);
			holder.handle = (ImageButton) convertView.findViewById(R.id.handle);
			holder.hide = (LinearLayout) convertView.findViewById(R.id.hide);
			holder.delete = (LinearLayout) convertView.findViewById(R.id.delete);
			holder.details = (LinearLayout) convertView.findViewById(R.id.details);
			holder.open = (LinearLayout) convertView.findViewById(R.id.open);
			convertView.setTag(holder);
		}else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		if (getCount() > 0) {
			// 设置头部间距
			if (position == 0) {
				holder.headerPadding.setVisibility(View.VISIBLE);
			} else {
				holder.headerPadding.setVisibility(View.GONE);
			}
			// 设置底部间距
			if (position == (getCount() - 1)) {
				holder.bottomPadding.setVisibility(View.VISIBLE);
			} else {
				holder.bottomPadding.setVisibility(View.GONE);
			}
			
			holder.songName.setText(mDataList.get(position).mMusicName);
			holder.songSize.setText(FileUtil.bytes2kb(mDataList.get(position).mMusicSize));
			
			// 设置播放时间
			if (position == mCurPlayMusicIndex) {
				holder.play.setImageResource(R.drawable.icon_file_mus_play);
				holder.curSongTime.setVisibility(View.VISIBLE);
				holder.curSongTime.setText(getCurPlayTime());
				holder.songTime.setText("/" + FileUtil.formatTime(mDataList.get(position).mMusicTime));
			} else {
				holder.play.setImageResource(R.drawable.icon_file_mus_pause);
				holder.curSongTime.setVisibility(View.GONE);
				holder.songTime.setText(FileUtil.formatTime(mDataList.get(position).mMusicTime));
			}
			
			boolean isChoose = mDataList.get(position).mIsDel;
			if (isChoose) {
				holder.choose.setImageResource(R.drawable.cleanmaster_select);
			} else {
				holder.choose.setImageResource(R.drawable.cleanmaster_noselect);
			}
			
			// 选择按钮监听
			holder.choose.setTag(R.id.tag_position, position);
			holder.choose.setTag(R.id.tag_object, mDataList.get(position));
			holder.choose.setOnClickListener(chooseListener);
			
			// 操作按钮点击监听事件
			holder.handle.setTag(position);
			holder.handle.setOnClickListener(handleListener);
			if (mCurListOpenIndex == position) {
				holder.hide.setVisibility(View.VISIBLE);
				holder.handle.setImageResource(R.drawable.arrow_rise);
			} else {
				holder.hide.setVisibility(View.GONE);
				holder.handle.setImageResource(R.drawable.arrow_drop);
			}
			
			// 音乐播放按钮
			holder.play.setTag(R.id.tag_position, position);
			holder.play.setOnClickListener(playListener);
			
			// 删除按钮点击监听事件
			holder.delete.setTag(position);
			holder.delete.setOnClickListener(delListener);
			
			// 详情按钮点击监听事件
			holder.details.setTag(position);
			holder.details.setOnClickListener(detailsListener);
			
			// 打开按钮点击监听事件
			holder.open.setTag(position);
			holder.open.setOnClickListener(openListener);
		}
		
		return convertView;
	}
	
	/** 选择按钮监听 */
	private OnClickListener chooseListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			setCurListIsOpen(-1);
			int position = (Integer) v.getTag(R.id.tag_position);
			MusicData  mData = (MusicData) v.getTag(R.id.tag_object);
			boolean isChecked = mData.mIsDel;
			mDataList.get(position).mIsDel = !isChecked;

			if (mDataListDel != null) {
				isChecked = mDataList.get(position).mIsDel;
				if (isChecked) {
					mDataListDel.add(mDataList.get(position));
				} else {
					for (int i = 0; i < mDataListDel.size(); i++) {
						if (mData.mId == mDataListDel.get(i).mId) {
							mDataListDel.remove(i);
							break;
						}
					}
				}
				sendMusicBroadcast(false);
			}
			notifyDataSetChanged();
		}
		
	};
	
	/** 操作监听 */
	private OnClickListener handleListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			if (mCurListIsOpen) {
				// 如果ID相等，则隐藏，反之显示另外一个
				if (mCurListOpenIndex == position) {
					setCurListIsOpen(-1);
				} else {
					setCurListIsOpen(position);
				}
			} else {
				setCurListIsOpen(position);
			}
		}
	};
	
	/**
	 * 设置当前打开的Item
	 * @param position -1:关闭, 其他整数则为打开
	 */
	private void setCurListIsOpen(int position) {
		if (position == -1) {
			mCurListIsOpen = false;
			mCurListOpenIndex = -1;
			mCurPlayMusicTime = mServiceManager.getCurPosition();
		} else {
			mCurListIsOpen = true;
			mCurListOpenIndex = position;
			mCurPlayMusicTime = mServiceManager.getCurPosition();
		}
		notifyDataSetChanged();

	}
	
	/** 音乐播放监听 */
	private OnClickListener playListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag(R.id.tag_position);
			if (mServiceManager == null) {
				mServiceManager = new ServiceManager(mContext);
				mServiceManager.connectService();
			}
			
			if (isPlay()) {
				if (mCurPlayMusicIndex == position) {
					setPlayState(-1, MusicPlayState.MPS_PREPARE, 0);
					stop();
					mMusicTimer.stopTimer();
				} else {
					setPlayState(position, MusicPlayState.MPS_PLAYING, mServiceManager.getCurPosition());
					play(position);
					mMusicTimer.startTimer();
				}
			} else {
				setPlayState(position, MusicPlayState.MPS_PLAYING, mServiceManager.getCurPosition());
				play(position);
				mMusicTimer.startTimer();
			}
		}
	};
	
	/** 选项删除监听 */
	private OnClickListener delListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String countTip = mContext.getString(R.string.delete_hint_music, "1");
			final int position = (Integer) v.getTag();
			final WaitingDialog waiting = new WaitingDialog(mContext);
			DeleteHintDialog dialog = new DeleteHintDialog(mContext, countTip);
			dialog.setGoonCallBackListener(new GoonCallBackListener() {
				
				@Override
				public void onClick() {
					// 如果相等，关闭打开的Item
					waiting.show();
					if (mCurListOpenIndex == position) {
						setCurListIsOpen(-1);
						// 停止播放音乐
						setPlayState(-1, MusicPlayState.MPS_PREPARE, 0);
						stop();
						mMusicTimer.stopTimer();
					}
					
					new Thread() {
						@Override
						public void run() {
							final long startTime = System.currentTimeMillis();
							final List<MusicData> xData = new ArrayList<MusicData>();
							xData.addAll(mDataList);
							MusicData item = xData.get(position);
							if (FileUtil.DeleteFolder(item.mMusicPath)) {
								mHelper.delete(item.mId + "");
								xData.remove(position);
								
								for (int i = 0; i < mDataListDel.size(); i++) {
									if (item.mId == mDataListDel.get(i).mId) {
										mDataListDel.remove(i);
										break;
									}
								}
								FileUtil.scanSdCard(mContext);
								mHandlerUpdate.post(new Runnable() {
									
									@Override
									public void run() {
										refreshAdapter(xData);
										sendMusicBroadcast(true);
										long endTime = System.currentTimeMillis();
										if ((endTime - startTime) < 1000) {
											waiting.close();
										} else {
											waiting.dismiss();
										}
									}
								});
							}
						};
					}.start();
					
				}
			});
			dialog.show();
			
		}
	};
	
	/** 详情按钮监听 */
	private OnClickListener detailsListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			MusicData item = mDataList.get(position);
			FileDetailsBean bean = new FileDetailsBean();
			bean.setFileName(item.mMusicDisplayName);
			bean.setFileType(item.mMusicType);
			bean.setFileSize(item.mMusicSize);
			bean.setFileDatetaken(item.mMusicDateModified);
			bean.setFilePath(item.mMusicPath);
			pop.setData(bean);
			pop.showAtLocation(v);
		}
	};
	
	/** 打开按钮监听 */
	private OnClickListener openListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			int position = (Integer) v.getTag();
			File file = new File(mDataList.get(position).mMusicPath);
			Intent intent = IntentUtils.createFileOpenIntent(file);
			try {
				mContext.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
				ToastUtils.showShortToast(mContext, mContext.getString(R.string.file_not_found));
			}
		}
	};
	
	public void showNoData() {
		Toast.makeText(mContext, "No Music Data...", Toast.LENGTH_SHORT).show();
	}
	
	public void rePlay() {
		if (mDataList == null && mDataList.size() == 0) {
			showNoData();
		} else {
			mServiceManager.rePlay();
		}
	}
	
	public void play(int position) {
		if (mDataList == null && mDataList.size() == 0) {
			showNoData();
		} else {
			mServiceManager.play(position);
		}
	}
	
	public boolean isPlay() {
		return mServiceManager.isPlay();
	}
	
	public void pause() {
		mServiceManager.pause();
	}
	
	public void stop() {
		mServiceManager.stop();
	}
	
	public void seekTo(int rate) {
		mServiceManager.seekTo(rate);
	}
	
	public void exit() {
		mServiceManager.exit();
	}
	
	public void setPlayInfo(int curTime) {
		mCurPlayMusicTime = curTime;
		
		notifyDataSetChanged();
	}
	
	/** 获取当前播放时间 */
	public String getCurPlayTime() {
		mCurPlayMusicTime /= 1000;
		int curminute = mCurPlayMusicTime / 60;
		int cursecond = mCurPlayMusicTime % 60;
		
		String curTimeString = String.format("%02d:%02d", curminute,cursecond);
		
		return curTimeString;

	}

	class ViewHolder {
		LinearLayout headerPadding;	// 头部边界
		LinearLayout bottomPadding;	// 底部边界
		ImageButton choose;	// 选择框
		ImageView play; // 播放按钮
		TextView songName; // 歌曲名
		TextView curSongTime; // 当前播放时间
		TextView songTime; // 播放总时间
		TextView songSize; // 文件大小
		ImageButton handle;	// 操作
		LinearLayout hide; // 隐藏部分
		LinearLayout delete; // 删除
		LinearLayout details;	// 详情
		LinearLayout open;	// 打开

	}

}
