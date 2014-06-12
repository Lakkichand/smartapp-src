package com.jiubang.ggheart.bussiness;

import android.content.Context;

import com.jiubang.core.framework.ICleanable;
import com.jiubang.ggheart.data.AppDataEngine;

/**
 * 
 * <br>类描述: 业务逻辑处理器基类
 * <br>功能详细描述: 所有业务逻辑处理器都必须继承该类
 * 
 * @author  yangguanxiang
 * @date  [2012-12-27]
 */
public abstract class BaseBussiness implements ICleanable {

	protected Context mContext;
	protected AppDataEngine mAppDataEngine;

	public BaseBussiness(Context context) {
		mAppDataEngine = AppDataEngine.getInstance(context);
		mContext = context;
	}
	@Override
	public void cleanup() {
		mAppDataEngine = null;
	}
}
