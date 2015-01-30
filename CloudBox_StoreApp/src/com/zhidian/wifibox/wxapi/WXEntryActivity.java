package com.zhidian.wifibox.wxapi;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhidian.wifibox.util.ShareToWeChatUtil;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

/**
 * 微信回调
 * @author zhaoyl
 *
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler{
	
	private IWXAPI api;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	api = WXAPIFactory.createWXAPI(this, ShareToWeChatUtil.APP_ID, true);
    	api.registerApp(ShareToWeChatUtil.APP_ID); 
        api.handleIntent(getIntent(), this);
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		setIntent(intent);
        api.handleIntent(intent, this);
	}
	
	@Override
	public void onReq(BaseReq arg0) {
		// TODO Auto-generated method stub
		
	}

	// 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
	@Override
	public void onResp(BaseResp resp) {
		String result = "";
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			result = "发送成功";
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			result = "取消";
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			result = "发送失败";
			break;
		default:
			result = "出现异常";
			break;
		}
		Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
		finish();
	}
}