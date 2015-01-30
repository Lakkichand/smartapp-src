package com.zhidian.wifibox.view.dialog;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zhidian.wifibox.R;
import com.zhidian.wifibox.data.FileDetailsBean;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.TimeTool;

/**
 * 文件详细弹出框
 * 
 * @author shihuajian
 *
 */
public class FileDetailsPopupWindow extends PopupWindow {

	private Context mContext;
	private TextView btnCancel;//取消按钮
	private TextView mFileName;
	private TextView mFileType;
	private TextView mFileSize;
	private TextView mFileDatetaken;
	private TextView mFilePath;
	private View mMenuView;
	private FileDetailsBean detailBean;

	public FileDetailsPopupWindow(Activity context) {
		super(context);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.popupwindow_details_hint, null);
		btnCancel = (TextView) mMenuView.findViewById(R.id.close);
		mFileName = (TextView) mMenuView.findViewById(R.id.file_name);
		mFileType = (TextView) mMenuView.findViewById(R.id.file_type);
		mFileSize = (TextView) mMenuView.findViewById(R.id.file_size);
		mFileDatetaken = (TextView) mMenuView.findViewById(R.id.file_datetaken);
		mFilePath = (TextView) mMenuView.findViewById(R.id.file_path);
		
		//取消按钮
		btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				//销毁弹出框
				dismiss();
			}
		});
		
		//设置SelectPicPopupWindow的View
		this.setContentView(mMenuView);
		//设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.FILL_PARENT);
		//设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		//设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.AnimBottom);
		//实例化一个ColorDrawable颜色为透明
		//ColorDrawable dw = new ColorDrawable(Color.argb(0, 0, 0, 0));//透明
		ColorDrawable dw = new ColorDrawable(0x7D010101);//半透明
		//设置SelectPicPopupWindow弹出窗体的背景
		this.setBackgroundDrawable(dw);
		//mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
		mMenuView.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				int height = mMenuView.findViewById(R.id.pop_layout).getTop();
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y < height){
						dismiss();
					}
				}				
				return true;
			}
		});

	}
	
	/** 设置数据 */
	public void setData(FileDetailsBean bean) {
		detailBean = bean;
		String fileName = detailBean.getFileName();
		String fileType = detailBean.getFileType();
		int fileSize = detailBean.getFileSize();
		int fileDatetaken = detailBean.getFileDatetaken();
		String filePath = detailBean.getFilePath();
		
		mFileName.setText(fileName);
		mFileType.setText(fileType);
		mFileSize.setText(FileUtil.bytes2kb(detailBean.getFileSize()));
		mFileDatetaken.setText(TimeTool.timestampToString(fileDatetaken + "", "yyyy-MM-dd HH:mm:ss"));
		mFilePath.setText(filePath);

	}
	
	/** 从底部弹出 */
	public void showAtLocation(View v) {
		showAtLocation(v.getRootView(), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);

	}

}
