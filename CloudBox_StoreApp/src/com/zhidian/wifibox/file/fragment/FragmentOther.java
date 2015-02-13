package com.zhidian.wifibox.file.fragment;

import java.io.File;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.ManagerOtherAdapter;
import com.zhidian.wifibox.controller.FileManagerController;
import com.zhidian.wifibox.file.other.OtherHelper;
import com.zhidian.wifibox.file.other.OtherItem;
import com.zhidian.wifibox.receiver.MusicCheckChangeReceiver;
import com.zhidian.wifibox.receiver.OtherCheckChangeReceiver;
import com.zhidian.wifibox.util.IntentUtils;
import com.zhidian.wifibox.util.ToastUtils;

public class FragmentOther extends Fragment implements OnClickListener, OnItemClickListener {

	private View mView;
	private Context mContext;
	private LinearLayout mBottom;
	private Button mDelAll;
	private ProgressBar mLoading;
	
	private ListView mOther;
	private ManagerOtherAdapter mOtherAdapter;
	
	/** 文件集 */
	private List<OtherItem> dataList;
	/** 文件集帮助类 */
	private OtherHelper helper;
	private OtherCheckChangeReceiver mOtherCheckChangeReceiver;
	
	private TAIResponseListener mRListener = new TAIResponseListener() {
		
		@Override
		public void onSuccess(TAResponse response) {
	    	mLoading.setVisibility(View.GONE);
	    	
			dataList = (List<OtherItem>) response.getData();
			mOtherAdapter.refreshAdapter(dataList);
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

    public FragmentOther() {
        super();
    }
    
    @Override
    public void onAttach(Activity activity) {
    	// TODO Auto-generated method stub
    	super.onAttach(activity);
    	mContext = activity;
    	mOtherAdapter = new ManagerOtherAdapter(mContext);
    	helper = OtherHelper.getInstance();
    	helper.init(activity);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	mView = inflater.inflate(R.layout.fragment_other, container, false);
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
		TARequest request = new TARequest(FileManagerController.GET_FILE_OTHER, helper);
		TAApplication.getApplication().doCommand(
				mContext.getString(R.string.filemanagercontroller), 
				request, 
				mRListener, 
				true, 
				false);
		
	}

	/** 初始化 */
    private void init() {
    	mOther = (ListView) mView.findViewById(R.id.other);
    	mBottom = (LinearLayout) mView.findViewById(R.id.bottom);
    	mDelAll = (Button) mView.findViewById(R.id.delectAll);
    	mLoading = (ProgressBar) mView.findViewById(R.id.loading);
    	
    	mDelAll.setOnClickListener(this);
    	mOther.setOnItemClickListener(this);
    	
    	mOtherAdapter = new ManagerOtherAdapter(mContext, dataList, helper);
    	mOther.setAdapter(mOtherAdapter);
    	
    	registerBroadcast();

	}

    /**
     * 设置列表空视图
     */
	private void setListEmptyView() {
		View emptyView = LayoutInflater.from(mContext).inflate(R.layout.file_manager_empty_prompt, mOther, false);  
    	ViewGroup parentView = (ViewGroup) mOther.getParent();  
    	parentView.addView(emptyView);
    	mOther.setEmptyView(emptyView);
	}
    
    @Override
    public void onResume() {
    	super.onResume();
    }

    /**
     * 注销广播
     */
	private void registerBroadcast() {
		mOtherCheckChangeReceiver = new OtherCheckChangeReceiver();
        IntentFilter intentFilter = new IntentFilter(OtherCheckChangeReceiver.PATH_NAME);
        mContext.registerReceiver(mOtherCheckChangeReceiver, intentFilter);
	}
    
    @Override
    public void onStop() {
    	super.onStop();
    }

    /**
     * 注销广播
     */
	private void unregisterBroadcast() {
		if (mOtherCheckChangeReceiver != null) {
    		mContext.unregisterReceiver(mOtherCheckChangeReceiver);
    	}
	}
    
    @Override
    public void onDestroy() {
    	unregisterBroadcast();
    	super.onDestroy();
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.delectAll:
			if (mOtherAdapter != null) {
				mOtherAdapter.chooseDel();
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
		if (mOtherAdapter != null) {
			mOtherAdapter.chooseAll(isAll);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.other:
			OtherItem item = (OtherItem) parent.getAdapter().getItem(position);
			if (item != null) {
				String path = item.getData();
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
	
	public void OnOtherCheckChange(Intent intent) {
		int count = intent.getIntExtra(OtherCheckChangeReceiver.CHOOSE_COUNT_FLAG, 0);
		mDelAll.setText(getString(R.string.manager_delete_all, count));
		if (count == 0) {
			mBottom.setVisibility(View.GONE);
		} else {
			mBottom.setVisibility(View.VISIBLE);
		}
	}

}
