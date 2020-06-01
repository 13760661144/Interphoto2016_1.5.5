package cn.poco.interphoto2.wxapi;

import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import cn.poco.interphoto2.BaseActivity;
import cn.poco.share.Constant;
import cn.poco.share.SendWXAPI;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler
{
	private IWXAPI api;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(this, Constant.weixinAppId);
		api.handleIntent(getIntent(), this);
		//微信登录才用
//		SendAuth.Resp resp = new SendAuth.Resp(getIntent().getExtras());
//		if(resp != null && resp.state != null && resp.state.equals(WeiXinBlog.WX_LOGIN) && resp.errCode == BaseResp.ErrCode.ERR_OK)
//		{
//			SharePage.mWeiXinGetCode = resp.code;
//		}
	}

	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req)
	{
//		switch(req.getType()) 
//		{
//		case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//			Intent intent =new Intent(WXEntryActivity.this, PocoCamera.class);
//			if(getIntent() != null)
//			{	
//				Bundle bundle = new Bundle();
//				bundle.putBundle("bundle", getIntent().getExtras());
//				bundle.putString("startBy", "wx");
//				intent.putExtras(bundle);
//				WXEntryActivity.this.startActivity(intent);
//			}
//			finish();
//			break;
//		case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
//			Toast.makeText(this, "分享到微信成功", Toast.LENGTH_LONG).show();
//			finish();
//			break;
//		default:
		finish();
//			break;
//		}		
	}

	@Override
	public void onResp(BaseResp resp)
	{
		SendWXAPI.dispatchResult(resp.errCode);
		finish();
	}

}
