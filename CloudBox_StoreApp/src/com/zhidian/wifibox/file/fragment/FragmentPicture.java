package com.zhidian.wifibox.file.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.ManagerPicDetailActivity;
import com.zhidian.wifibox.adapter.ManagerPictureAdapter;
import com.zhidian.wifibox.controller.FileManagerController;
import com.zhidian.wifibox.file.album.AlbumHelper;
import com.zhidian.wifibox.file.album.ImageBucket;
import com.zhidian.wifibox.receiver.AlbumCheckChangeReceiver;
import com.zhidian.wifibox.receiver.MusicCheckChangeReceiver;

public class FragmentPicture extends Fragment implements OnClickListener {
	
	private View mView;
	private Context mContext;

	private GridView mAlbum;
	private LinearLayout mBottom;
	private Button mDelAll;
	private ProgressBar mLoading;
	private ManagerPictureAdapter mPicAdapter;
	
	/** 图片集列表 */
	private List<ImageBucket> dataList;
	/** 图片专辑帮助类 */
	private AlbumHelper helper;
	/** 图片集选择发生改变的广播 */
	private AlbumCheckChangeReceiver mAlbumCheckChangeReceiver;
	
	public static final String EXTRA_BUCKET_ID = "bucket_id";
	public static final String EXTRA_FILE_NAME = "file_name";
	public static final String EXTRA_FILE_IS_CHECK = "file_is_check";
	
	private TAIResponseListener mRListener = new TAIResponseListener() {
		
		@Override
		public void onSuccess(TAResponse response) {
	    	mLoading.setVisibility(View.GONE);
	    	dataList.clear();
			dataList = (List<ImageBucket>) response.getData();
			mPicAdapter.refreshAdapter(dataList);
	    	setListEmptyView();
		}
		
		@Override
		public void onStart() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onRuning(TAResponse response) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onFinish() {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onFailure(TAResponse response) {
			// TODO Auto-generated method stub
			
		}
	};

    public FragmentPicture() {
        super();
    }
    
    @Override
    public void onAttach(Activity activity) {
    	// TODO Auto-generated method stub
    	super.onAttach(activity);
    	mContext = activity;
    	helper = AlbumHelper.getInstance();
    	helper.init(activity);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	mView = inflater.inflate(R.layout.fragment_picture, container, false);
    	return mView;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	super.onActivityCreated(savedInstanceState);

    	init();
    	initData();
    }
    
    /** 初始化数据 */
    public void initData() {
//    	mLoading.setVisibility(View.VISIBLE);
		TARequest request = new TARequest(FileManagerController.GET_FILE_PICTURE, helper);
		TAApplication.getApplication().doCommand(
				mContext.getString(R.string.filemanagercontroller), 
				request, 
				mRListener, 
				true, 
				false);
		
	}

	/** 初始化 */
    private void init() {
    	mAlbum = (GridView) mView.findViewById(R.id.picture);
    	mBottom = (LinearLayout) mView.findViewById(R.id.bottom);
    	mDelAll = (Button) mView.findViewById(R.id.delectAll);
    	mLoading = (ProgressBar) mView.findViewById(R.id.loading);
    	
    	mDelAll.setOnClickListener(this);
    	
    	dataList = new ArrayList<ImageBucket>();
    	mPicAdapter = new ManagerPictureAdapter(mContext, dataList, helper);
    	mAlbum.setOnItemClickListener(skipListener);
		mAlbum.setAdapter(mPicAdapter);

		registerBroadcast();

	}

    /**
     * 设置列表空视图
     */
	private void setListEmptyView() {
		View emptyView = LayoutInflater.from(mContext).inflate(R.layout.file_manager_empty_prompt, mAlbum, false);
    	ViewGroup parentView = (ViewGroup) mAlbum.getParent();  
    	parentView.addView(emptyView);
    	mAlbum.setEmptyView(emptyView);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.delectAll:
			if (mPicAdapter != null) {
				mPicAdapter.chooseDel();
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
		if (mPicAdapter != null) {
			mPicAdapter.chooseAll(isAll);
		}

	}
    
	public void OnAlbumCheckChange(Intent intent) {
		int count = intent.getIntExtra(AlbumCheckChangeReceiver.CHOOSE_COUNT_FLAG, 0);
		mDelAll.setText(getString(R.string.manager_delete_all, count));
		if (count == 0) {
			mBottom.setVisibility(View.GONE);
		} else {
			mBottom.setVisibility(View.VISIBLE);
		}
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	/**
	 * 注册广播
	 */
	private void registerBroadcast() {
		mAlbumCheckChangeReceiver = new AlbumCheckChangeReceiver();
		IntentFilter intentFilter = new IntentFilter(AlbumCheckChangeReceiver.PATH_NAME);
		mContext.registerReceiver(mAlbumCheckChangeReceiver, intentFilter);
	}
    
	@Override
	public void onStop() {
		super.onStop();
	}
	
    @Override
    public void onDestroy() {
    	unregisterBroadcast();
    	super.onDestroy();
    }

    /**
     * 注销广播
     */
	private void unregisterBroadcast() {
		if (mAlbumCheckChangeReceiver != null) {
    		mContext.unregisterReceiver(mAlbumCheckChangeReceiver);
    	}
	}
    
    private OnItemClickListener skipListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			try {
				Intent intent = new Intent(mContext, ManagerPicDetailActivity.class);
				intent.putExtra(FragmentPicture.EXTRA_BUCKET_ID, dataList.get(position).id);
				intent.putExtra(FragmentPicture.EXTRA_FILE_NAME, dataList.get(position).bucketName);
				intent.putExtra(FragmentPicture.EXTRA_FILE_IS_CHECK, dataList.get(position).isSelected);
				((FileManagerActivity)mContext).startActivityForResult(intent, FileManagerActivity.REQUEST_CODE_PICTURE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
}
