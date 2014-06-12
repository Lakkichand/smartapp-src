package com.jiubang.ggheart.components.advert;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.gau.utils.net.operator.IHttpOperator;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.BasicResponse;
import com.gau.utils.net.response.IResponse;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述: 解析NET请求返回的数据转换成字符串
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-5]
 */
public class NetImageOperator implements IHttpOperator {
	public AdvertInfo mAdvertInfo;

	public NetImageOperator(AdvertInfo advertInfo) {
		mAdvertInfo = advertInfo;
	}

	@Override
	public IResponse operateHttpResponse(THttpRequest request, HttpResponse response)
			throws IllegalStateException, IOException {
		HttpEntity entity = response.getEntity();

		int state = AdvertConstants.DOWN_IMAGE_FAIL;

		InputStream is = entity.getContent();
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(is);
			if (bitmap != null) {
				String path = LauncherEnv.Path.ADVERT_PATH + mAdvertInfo.mId + ".png";	//图片SD卡路径
				boolean isSuccess = FileUtil.saveBitmapToSDFile(bitmap, path, CompressFormat.PNG);
				if (isSuccess) {
					mAdvertInfo.mIcon = path;
					state = AdvertConstants.DOWN_IMAGE_SUCCESS;
//					Log.i("lch", "path: " + path);
				} else {
//					Log.i("lch", "path: " + "失败");
				}
			}
		} catch (Exception e) {
		} catch (OutOfMemoryError e) {
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		BasicResponse ret = new BasicResponse(IResponse.RESPONSE_TYPE_STRING, state);
		return ret;
	}
}
