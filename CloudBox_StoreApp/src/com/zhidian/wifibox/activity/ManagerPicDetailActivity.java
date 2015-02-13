package com.zhidian.wifibox.activity;

import java.io.File;
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
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import cn.trinea.android.common.util.StringUtils;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.StickyGridAdapter;
import com.zhidian.wifibox.controller.FileManagerController;
import com.zhidian.wifibox.file.album.AlbumHelper;
import com.zhidian.wifibox.file.album.HeaderBean;
import com.zhidian.wifibox.file.album.ImageChildItem;
import com.zhidian.wifibox.file.fragment.FileManagerActivity;
import com.zhidian.wifibox.file.fragment.FragmentPicture;
import com.zhidian.wifibox.listener.IOnAlbumCheckChangeListener;
import com.zhidian.wifibox.receiver.AlbumCheckChangeReceiver;
import com.zhidian.wifibox.receiver.MusicCheckChangeReceiver;
import com.zhidian.wifibox.util.IntentUtils;
import com.zhidian.wifibox.view.stickygrid.StickyGridHeadersGridView;

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
	private GridView mImageGrid;
	/** 多级列表适配器 */
	private StickyGridAdapter mImageAdapter;

	/** 图片集选择发生改变的广播 */
	private AlbumCheckChangeReceiver mAlbumCheckChangeReceiver;
	/** 图片数据 */
	private List<ImageChildItem> dataList = new ArrayList<ImageChildItem>();
	/** 分类数据 */
	private SparseArray<HeaderBean> mHeaderData = new SparseArray<HeaderBean>();
	/** 图片所在文件夹名称 */
	private String fileName;
	/** 图片所在文件夹ID */
	private String bucketId;
	/** 初次进入界面是否全选 */
	private boolean isSelect = false;
	/** 是否初次进入界面 */
	private boolean isFirst = true;
	private AlbumHelper helper;
	
	/** 是否刷新主数据 */
	private boolean isRefresh = false;
	/** 内部删除刷新数据标记 */
	private boolean delRefresh = false;
	
	private TAIResponseListener mRListener = new TAIResponseListener() {
		
		@Override
		public void onSuccess(TAResponse response) {
	    	mLoading.setVisibility(View.GONE);
	    	
			Map<String, Object> map = (Map<String, Object>) response.getData();
			dataList = (List<ImageChildItem>) map.get(FileManagerController.FLAG_DATA_LIST);
			mHeaderData = (SparseArray<HeaderBean>) map.get(FileManagerController.FLAG_CATEGORY_DATA);
			if (mImageAdapter != null) {
				mImageAdapter.refreshAdapter(dataList, mHeaderData, isFirst, isSelect);
			}
			
		}
		
		@Override
		public void onStart() {}
		
		@Override
		public void onRuning(TAResponse response) {}
		
		@Override
		public void onFinish() {}
		
		@Override
		public void onFailure(TAResponse response) {
			mLoading.setVisibility(View.GONE);
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
	private void init() {
		mHeaderBack = (ImageView) findViewById(R.id.header_title_back);
		mHeaderTitle = (TextView) findViewById(R.id.header_title_text);
		mImageGrid = (GridView) findViewById(R.id.expandableImage);
    	mBottom = (LinearLayout) findViewById(R.id.bottom);
    	mChooseAll = (ToggleButton) findViewById(R.id.chooseAll);
    	mDelAll = (Button) findViewById(R.id.delectAll);
    	mLoading = (ProgressBar) findViewById(R.id.loading);
    	
    	mChooseAll.setOnClickListener(this);
    	mDelAll.setOnClickListener(this);
		mHeaderBack.setOnClickListener(this);
		
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
		
		mImageAdapter = new StickyGridAdapter(mContext, dataList, mImageGrid, helper);
		mImageGrid.setAdapter(mImageAdapter);
		((StickyGridHeadersGridView) mImageGrid).setAreHeadersSticky(false);
		mImageGrid.setOnItemClickListener(imgItemListener);
		
		// 注册广播
		registerBroadcast();
		
		getData();
	}
	
	/**
	 * 选项监听
	 */
	private OnItemClickListener imgItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ImageChildItem item = dataList.get(position);
			if (item != null) {
				String path = item.getImagePath();
				File file = new File(path);
				Intent intent = IntentUtils.createFileOpenIntent(file);
				startActivity(intent);
			} else {
				Toast.makeText(mContext, R.string.file_data_null, Toast.LENGTH_SHORT).show();
			}
			
		}
	};

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
				delRefresh = true;
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
			if (countTotal != 0) {
				mChooseAll.setChecked(false);
			}
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
		
		// 删除数据后刷新数据
		if (delRefresh) {
			getData();
			delRefresh = false;
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
