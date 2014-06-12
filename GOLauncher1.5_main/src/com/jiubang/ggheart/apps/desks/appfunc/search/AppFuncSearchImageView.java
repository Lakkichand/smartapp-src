package com.jiubang.ggheart.apps.desks.appfunc.search;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.file.media.AudioFile;
import com.go.util.file.media.FileInfo;
import com.go.util.file.media.ImageFile;
import com.go.util.file.media.MediaBroadCaster.MediaBroadCasterObserver;
import com.go.util.file.media.ThumbnailManager;
import com.go.util.graphics.DrawUtils;
import com.go.util.graphics.ImageUtil;

/**
 * 
 * @author YeJijiong
 * @version 创建时间：2012-10-23 下午2:29:55
 * 功能表搜索多媒体结果图标类
 */
public class AppFuncSearchImageView extends View implements MediaBroadCasterObserver {

	private Context mContext;
	private FileInfo mInfo = null;
	/**
	 * 图片宽度（需要用48dp去转px，以适应不同屏幕）
	 */
	private int mImgWidth = 0;
	/**
	 * 媒体类型（音乐，图片，视频）
	 */
	private String mMediaType = null;
	/**
	 * 当前绘制的图标
	 */
	private Bitmap mContentImage = null;
	/**
	 * 绘制图标的矩形
	 */
	private Rect mContentImageRegion = new Rect();
	private Paint mContentPaint = null;
	
	public AppFuncSearchImageView(Context context) {
		super(context);
		mContext = context;
		mContentPaint = new Paint();
		mContentPaint.setFilterBitmap(true);
		mImgWidth = DrawUtils.dip2px(48);
	}
	
	
	public AppFuncSearchImageView(Context context, AttributeSet set) {
		super(context, set);
		mContext = context;
		mContentPaint = new Paint();
		mContentPaint.setFilterBitmap(true);
		mImgWidth = DrawUtils.dip2px(48);
	}
	
	/**
	 * 
	 * 加载缩略图
	 * @param type
	 * @param id
	 * @param filePath
	 * @param imgWidth
	 */
	private void loadThumbnail(String type, int id, String filePath, int imgWidth) {
		Bitmap bitmap = ThumbnailManager.getInstance(mContext).getThumbnail(this, type, id,
				filePath, imgWidth);
		if (bitmap != null) {
			mContentImage = bitmap;
			setBackground(true);
		} else {
			BitmapDrawable bd = null;
			// 设置默认图片
			if (mMediaType == ThumbnailManager.TYPE_ALBUM) { // 使用默认音乐图标
				bd = (BitmapDrawable) getResources().getDrawable(R.drawable.app_func_search_result_audio_icon);
				if (bd != null) {
					mContentImage = bd.getBitmap();
				}
			}
			else if (mMediaType == ThumbnailManager.TYPE_IMAGE) { // 使用默认图片图标
				bd = (BitmapDrawable) getResources().getDrawable(R.drawable.app_func_search_result_image_icon);
				if (bd != null) {
					mContentImage = bd.getBitmap();
				}
			} else { // 使用默认视频图片
				bd = (BitmapDrawable) getResources().getDrawable(R.drawable.app_func_search_result_video_icon);
				if (bd != null) {
					mContentImage = bd.getBitmap();
				}
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// 绘制图片内容
		canvas.save();
		canvas.clipRect(mContentImageRegion);
		if (mContentImage != null) {
			ImageUtil.drawFitImage(canvas, mContentImage, mContentImageRegion.left,
					mContentImageRegion.top, mContentImageRegion.right, mContentImageRegion.bottom,
					mContentPaint);
		}
		canvas.restore();
	}
	
	/**
	 * 取消加载缩略图
	 * @param type
	 * @param id
	 */
	protected void cancelLoadThumbnail(String type, int id) {
		ThumbnailManager manager = ThumbnailManager.getInstance(mContext);
		manager.cancelLoadThumbnail(this, type, id);
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		int picId = mInfo.thumbnailId;
		if (mMediaType == ThumbnailManager.TYPE_ALBUM) {
			picId = ((AudioFile) mInfo).albumId;
		}
		if (param == picId) {
			if (msgId == ThumbnailManager.MSG_ID_LOAD_IMAGE_COMPLETED && object != null) {
				mContentImage = (Bitmap) object;
				setBackground(true);
				invalidate();
			}
		}
	}
	
	/**
	 * 是否显示背景框
	 */
	private void setBackground(boolean ishow) {
		if (ishow) {
			setBackgroundResource(R.drawable.app_func_search_result_imageview_bg);
			setPadding(5, 5, 5, 5);
			mContentImageRegion.set(getPaddingLeft(), getPaddingTop(), mImgWidth
					- getPaddingRight(), mImgWidth - getPaddingBottom());
		} else {
			setBackgroundDrawable(null);
			setPadding(0, 0, 0, 0);
			mContentImageRegion.set(getPaddingLeft(), getPaddingTop(),
					mImgWidth - getPaddingRight(), mImgWidth
							- getPaddingBottom());
		}
	}
	
	/**
	 * 绑定FileInfo
	 * @param info
	 */
	public void setFileItemInfo(FileInfo info) {
		if (info == null) {
			return;
		}
		setBackground(false);

		// 当重新绑定新的FileInfo时取消上一次的缩略图加载
		if (mInfo != null && mMediaType != null && !mMediaType.equals("")) {
			cancelLoadThumbnail(mMediaType, mInfo.thumbnailId);
		}

		mInfo = info;
		if (mInfo instanceof AudioFile) { // 音乐
			mMediaType = ThumbnailManager.TYPE_ALBUM;
			loadThumbnail(mMediaType, ((AudioFile) mInfo).albumId,
					mInfo.thumbnailPath, mImgWidth);
		} else if (mInfo instanceof ImageFile) { // 图片
			mMediaType = ThumbnailManager.TYPE_IMAGE;
			loadThumbnail(mMediaType, mInfo.thumbnailId, mInfo.thumbnailPath,
					mImgWidth);
		} else { // 视频
			mMediaType = ThumbnailManager.TYPE_VIDEO;
			loadThumbnail(mMediaType, mInfo.thumbnailId, mInfo.thumbnailPath,
					mImgWidth);
		}
	}
	
	/**
	 * 
	 * 设置App图标图片，这里会去掉背景的框
	 * @param drawable
	 */
	public void setDrawable(Drawable drawable) {
		if (drawable != null) {
			setBackground(false);
			if (drawable instanceof BitmapDrawable) {
				mContentImage = ((BitmapDrawable) drawable).getBitmap();
				invalidate();
			}
		}
	}
}
