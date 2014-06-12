package com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.widget.ImageView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageDownLoadObserver;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * 
 */
public class ImageWidgetBean extends MessageWidgetBean {

	public static final String TAG_SRC = "src";
	public static final String TAG_HEIGHT = "height";
	public static final String TAG_WIDTH = "width";
	private String mSrc; // 图片url地址 http格式url;
	private int mHeight;
	private int mWidth;
	private Bitmap mBitmap;
	private final static float MAX_IMAGE_WIDTH = GOLauncherApp.getContext().getResources()
			.getDimension(R.dimen.max_message_image_wight);

	public ImageWidgetBean() {
		mType = TYPE_IMG;
	}

	@Override
	public void prase(JSONObject obj) {
		// TODO Auto-generated method stub
		// super.prase(obj);
		if (obj != null) {
			getAttribute(TAG_SRC, obj);
			getAttribute(TAG_HEIGHT, obj);
			getAttribute(TAG_WIDTH, obj);
			getAttribute(TAG_ACTTYPE, obj);
			getAttribute(TAG_ACTVAULE, obj);
			downloadDrawable();

		}

	}

	private void getAttribute(String tag, JSONObject obj) {
		try {

			if (tag.equals(TAG_SRC)) {
				mSrc = obj.getString(TAG_SRC);
			} else if (tag.equals(TAG_HEIGHT)) {
				mHeight = obj.getInt(TAG_HEIGHT);

			} else if (tag.equals(TAG_WIDTH)) {
				mWidth = obj.getInt(TAG_WIDTH);
				if (mWidth > MAX_IMAGE_WIDTH) {
					mWidth = (int) MAX_IMAGE_WIDTH;
				}
			} else if (tag.equals(TAG_ACTTYPE)) {
				mActtype = obj.getInt(TAG_ACTTYPE);
			} else if (tag.equals(TAG_ACTVAULE)) {
				mActvaule = obj.getString(TAG_ACTVAULE);
			}
		} catch (JSONException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * 功能简述: 消息中心内容中的图片
	 * 功能详细描述: 
	 * 注意:
	 */
	private void downloadDrawable() {
		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				if (mSrc == null) {
					return;
				}
				HttpURLConnection conn = null;
				InputStream is = null;
				try {
					URL url_im = new URL(mSrc);
					conn = (HttpURLConnection) url_im.openConnection();
					conn.connect();
					//有可能网络传输InputStream出现问题 ，导致没有获取到网络图片，获取6次
					for (int i = 0; i < 6; i++) {
						is = conn.getInputStream();
						mBitmap = BitmapFactory.decodeStream(is);
						reSizeBitmap();
						if (mBitmap != null) {
								break;
						}
					}
				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (conn != null) {
						conn.disconnect();
					}
					if (mObserver != null) {
						mObserver.onDownLoadFinsish();
					}
				}

			}
		}.start();
	}

	@Override
	public void initView(View view, MessageDownLoadObserver observer) {
		// TODO Auto-generated method stub
		super.initView(view, observer);
		((ImageView) view).setImageBitmap(mBitmap);

	}

	@Override
	public void recycle() {
		// TODO Auto-generated method stub
		super.recycle();
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
		}
		mBitmap = null;
	}

	/**
	 * 功能简述: 调整消息中心后台传输的图片的大小
	 */
	private void reSizeBitmap() {
		try {
			int w = mBitmap.getWidth();
			int h = mBitmap.getHeight();
			if (w <= 0 || h <= 0 || w <= MAX_IMAGE_WIDTH) {
				return;
			}
			float scareFactor = MAX_IMAGE_WIDTH / w;
			Matrix matrix = new Matrix();
			matrix.postScale(scareFactor, scareFactor);
			mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, w, h, matrix, true);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
