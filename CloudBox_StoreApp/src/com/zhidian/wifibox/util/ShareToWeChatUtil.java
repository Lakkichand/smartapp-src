package com.zhidian.wifibox.util;

import android.graphics.Bitmap;
import android.widget.Toast;

import com.ta.TAApplication;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhidian.wifibox.data.WeChatShareBean;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 分享到微信
 * 
 * @author zhaoyl
 * 
 */
public class ShareToWeChatUtil {

	public static final String APP_ID = "wx368d28e1d9f00cf3";// 正式
	private static final String APP_SECRET = "3e87c074ed777adc8c78852e7d66757d";
	private IWXAPI api;

	public ShareToWeChatUtil() {
		api = WXAPIFactory.createWXAPI(TAApplication.getApplication(), APP_ID,
				true);
		api.registerApp(APP_ID);

	}

	/**
	 * 微信分享
	 */
	public void sendReq(final WeChatShareBean bean) {
		String url = bean.link;// 收到分享的好友点击信息会跳转到这个地址去
		WXWebpageObject localWXWebpageObject = new WXWebpageObject();
		localWXWebpageObject.webpageUrl = url;
		final WXMediaMessage localWXMediaMessage = new WXMediaMessage(
				localWXWebpageObject);
		localWXMediaMessage.title = bean.title;// 不能太长，否则微信会提示出错。
		localWXMediaMessage.description = bean.desc;

		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.img_url.hashCode() + "",
				bean.img_url, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							localWXMediaMessage
									.setThumbImage(DrawUtil.sDefaultIcon);
						} else {
							localWXMediaMessage.setThumbImage(imageBitmap);
						}

						SendMessageToWX.Req localReq = new SendMessageToWX.Req();
						localReq.transaction = System.currentTimeMillis() + "";
						localReq.message = localWXMediaMessage;
						if (bean.type == 0) {// 分享到朋友圈
							localReq.scene = SendMessageToWX.Req.WXSceneTimeline;
						}

						api.sendReq(localReq);

					}
				});

		if (bm != null) {
			localWXMediaMessage.setThumbImage(bm);
			SendMessageToWX.Req localReq = new SendMessageToWX.Req();
			localReq.transaction = System.currentTimeMillis() + "";
			localReq.message = localWXMediaMessage;
			if (bean.type == 0) {// 分享到朋友圈
				localReq.scene = SendMessageToWX.Req.WXSceneTimeline;
			}

			api.sendReq(localReq);
		}

		

	}
	//
	// // 需要对图片进行处理，否则微信会在log中输出thumbData检查错误
	// private static byte[] getBitmapBytes(Bitmap bitmap, boolean paramBoolean)
	// {
	// Bitmap localBitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.RGB_565);
	// Canvas localCanvas = new Canvas(localBitmap);
	// int i;
	// int j;
	// if (bitmap.getHeight() > bitmap.getWidth()) {
	// i = bitmap.getWidth();
	// j = bitmap.getWidth();
	// } else {
	// i = bitmap.getHeight();
	// j = bitmap.getHeight();
	// }
	// while (true) {
	// localCanvas.drawBitmap(bitmap, new Rect(0, 0, i, j), new Rect(0, 0,
	// 80, 80), null);
	// if (paramBoolean)
	// bitmap.recycle();
	// ByteArrayOutputStream localByteArrayOutputStream = new
	// ByteArrayOutputStream();
	// localBitmap.compress(Bitmap.CompressFormat.JPEG, 100,
	// localByteArrayOutputStream);
	// localBitmap.recycle();
	// byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
	// try {
	// localByteArrayOutputStream.close();
	// return arrayOfByte;
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// i = bitmap.getHeight();
	// j = bitmap.getHeight();
	// }
	// }

}
