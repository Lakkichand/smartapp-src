package com.zhidian.wifibox.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import cn.trinea.android.common.util.StringUtils;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.ManagerPicDetailsAdapter;
import com.zhidian.wifibox.controller.FileManagerController;
import com.zhidian.wifibox.file.album.AlbumHelper;
import com.zhidian.wifibox.file.album.ImageItem;
import com.zhidian.wifibox.file.album.ImageItemGroup;
import com.zhidian.wifibox.file.fragment.FileManagerActivity;
import com.zhidian.wifibox.file.fragment.FragmentPicture;
import com.zhidian.wifibox.listener.IOnAlbumCheckChangeListener;
import com.zhidian.wifibox.receiver.AlbumCheckChangeReceiver;
import com.zhidian.wifibox.receiver.MusicCheckChangeReceiver;

/**
 * 
 * 图片文件夹详细
 * 
 * @author shihuajian
 *
 */
public class ManagerPicDetailActivity extends Activity implements OnClickListener, IOnAlbumCheckChangeListener {
	
	private static final String TAG = ManagerPicDetailActivity.class.getSimpleName();
	
	private Context mContext;
	/** 头部返回 */
	private ImageView mHeaderBack;
	/** 头部标题 */
	private TextView mHeaderTitle;
	private LinearLayout mBottom;
	private ToggleButton mChooseAll;
	private Button mDelAll;
	private ProgressBar mLoading;
	/** 多级列表 */
	private ExpandableListView mImageList;
	/** 多级列表适配器 */
	private ManagerPicDetailsAdapter mImageAdapter;

	/** 图片集选择发生改变的广播 */
	private AlbumCheckChangeReceiver mAlbumCheckChangeReceiver;
	/** 图片数据 */
	private List<ImageItem> dataList = new ArrayList<ImageItem>();
	/** 分类数据 */
	private List<ImageItemGroup> mCategoryData = new ArrayList<ImageItemGroup>();
	/** 图片所在文件夹名称 */
	private String fileName;
	/** 图片所在文件夹ID */
	private String bucketId;
	/** 初次进入界面是否全选 */
	private boolean isSelect = false;
	/** 是否初次进入界面 */
	private boolean isFirst = true;
	private AlbumHelper helper;
	
	/** 是否刷新过数据 */
	private boolean isRefresh = false;
	
	private TAIResponseListener mRListener = new TAIResponseListener() {
		
		@Override
		public void onSuccess(TAResponse response) {
	    	mLoading.setVisibility(View.GONE);
	    	
			Map<String, Object> map = (Map<String, Object>) response.getData();
			dataList = (List<ImageItem>) map.get(FileManagerController.FLAG_DATA_LIST);
			mCategoryData = (List<ImageItemGroup>) map.get(FileManagerController.FLAG_CATEGORY_DATA);
			if (mImageAdapter != null) {
				mImageAdapter.refreshAdapter(mCategoryData, isFirst, isSelect);
				showChildAll();
			}
			
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture_detail);
		
		mContext = ManagerPicDetailActivity.this;
		helper = AlbumHelper.getInstance();
		helper.init(ManagerPicDetailActivity.this);
		
		init();
	}

	/** 初始化 */
	@SuppressWarnings("unchecked")
	private void init() {
		mHeaderBack = (ImageView) findViewById(R.id.header_title_back);
		mHeaderTitle = (TextView) findViewById(R.id.header_title_text);
		mImageList = (ExpandableListView) findViewById(R.id.expandableImage);
    	mBottom = (LinearLayout) findViewById(R.id.bottom);
    	mChooseAll = (ToggleButton) findViewById(R.id.chooseAll);
    	mDelAll = (Button) findViewById(R.id.delectAll);
    	mLoading = (ProgressBar) findViewById(R.id.loading);
    	
    	mChooseAll.setOnClickListener(this);
    	mDelAll.setOnClickListener(this);
		mHeaderBack.setOnClickListener(this);
		mImageList.setOnGroupClickListener(new OnGroupClickListener() {

			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
					int groupPosition, long id) {
				return true;
			}

		});
		
		// 获取数据
		fileName = getIntent().getStringExtra(FragmentPicture.EXTRA_FILE_NAME);
		bucketId = getIntent().getStringExtra(FragmentPicture.EXTRA_BUCKET_ID);
		isSelect = getIntent().getBooleanExtra(FragmentPicture.EXTRA_FILE_IS_CHECK, false);
		Log.e(TAG, "IsSelected: " + isSelect);

		if (!StringUtils.isEmpty(fileName)) {
			mHeaderTitle.setText(fileName);
		} else {
			mHeaderTitle.setText(R.string.fragment_picture);
		}
		
		mChooseAll.setChecked(isSelect);
		mDelAll.setText(getString(R.string.manager_delete_all, dataList.size()));
		if (isSelect) {
			mBottom.setVisibility(View.VISIBLE);
		} else {
			mBottom.setVisibility(View.GONE);
		}
		
		mImageAdapter = new ManagerPicDetailsAdapter(mContext, mCategoryData, isSelect, helper);
		View v = LayoutInflater.from(mContext).inflate(R.layout.file_bottom_padding, null);
		mImageList.addFooterView(v);
		mImageList.setAdapter(mImageAdapter);
		// 全部展开
		showChildAll();
		
		// 注册广播
		registerBroadcast();
		
		getData();
	}

	/** 全部展开 */
	private void showChildAll() {
		for (int i = 0; i < mCategoryData.size(); i++) {
			mImageList.expandGroup(i);
		}
	}

	/**
	 * 注册广播
	 */
	private void registerBroadcast() {
		mAlbumCheckChangeReceiver = new AlbumCheckChangeReceiver();
		IntentFilter intentFilter = new IntentFilter(AlbumCheckChangeReceiver.PATH_NAME2);
		mContext.registerReceiver(mAlbumCheckChangeReceiver, intentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 页面统计
		StatService.trackBeginPage(this, getString(R.string.picture_details));
		XGPushManager.onActivityStarted(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, getString(R.string.picture_details));
		XGPushManager.onActivityStoped(this);
	}
	
	@Override
	protected void onDestroy() {
		unregisterBroadcast();
		super.onDestroy();
	}

	private void unregisterBroadcast() {
		if (mAlbumCheckChangeReceiver != null) {
			mContext.unregisterReceiver(mAlbumCheckChangeReceiver);
		}
	}
	
	/** 获取数据 */
	private void getData() {
		mLoading.setVisibility(View.VISIBLE);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FileManagerController.FLAG_HELPER, helper);
		map.put(FileManagerController.FLAG_BUCKET_ID, bucketId);
		map.put(FileManagerController.FLAG_IS_SELECT, isSelect);
		TARequest request = new TARequest(FileManagerController.GET_FILE_PICTURE_DETAIL, map);
		TAApplication.getApplication().doCommand(
				mContext.getString(R.string.filemanagercontroller), 
				request, 
				mRListener, 
				true, 
				false);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_title_back:
			setResults();
			finish();
			break;
		case R.id.delectAll:
			if (mImageAdapter != null) {
				mImageAdapter.chooseDel();
				isRefresh = true;
			}
			break;
		case R.id.chooseAll:
			boolean isAll = mChooseAll.isChecked();
			if (mImageAdapter != null) {
				mImageAdapter.chooseAll(isAll);
			}
			break;

		default:
			break;
		}
		
	}
    
	@Override
	public void OnAlbumCheckChange(Intent intent) {
		int count = intent.getIntExtra(MusicCheckChangeReceiver.CHOOSE_COUNT_FLAG, 0);
		int countTotal = intent.getIntExtra(MusicCheckChangeReceiver.TOTAL_COUNT, 0);
		mDelAll.setText(getString(R.string.manager_delete_all, count));
		if (count == 0) {
			mBottom.setVisibility(View.GONE);
			mChooseAll.setChecked(false);
		} else {
			mBottom.setVisibility(View.VISIBLE);
			if (count == countTotal) {
				mChooseAll.setChecked(true);
			} else {
				mChooseAll.setChecked(false);
			}
		}
		
		if (isFirst && isSelect) {
			mChooseAll.setChecked(true);
			isFirst = false;
		}
		
	}
	
	@Override
	public void onBackPressed() {
		setResults();
		super.onBackPressed();
	}

	/** 设置回调 */
	private void setResults() {
		Intent data = new Intent();
		data.putExtra(FileManagerActivity.REFRESH_FLAG, isRefresh);
		setResult(FileManagerActivity.RESULT_CODE_PICTURE, data);
	}

}
