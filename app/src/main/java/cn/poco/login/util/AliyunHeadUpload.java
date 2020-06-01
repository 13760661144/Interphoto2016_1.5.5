package cn.poco.login.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import cn.poco.loginlibs.ILogin;
import cn.poco.storagesystemlibs.AbsAliyunBase;
import cn.poco.storagesystemlibs.UploadInfo;
import cn.poco.tianutils.NetCore2;

/**
 * Created by admin on 2017/8/30.
 */

public class AliyunHeadUpload extends AbsAliyunBase
{
	private UploadInfo mInfo;
	@Override
	protected OSSFederationToken GetFederationToken()
	{
		if(mInfo != null)
		{
			return new OSSFederationToken(mInfo.mAccessKeyId, mInfo.mAccessKeySecret, mInfo.mSecurityToken, mInfo.mExpire);
		}
		return null;
	}

	/**
	 * 上传头像缩略图
	 *
	 * @return 用户头像上传后所在的网络地址
	 */
	public String uploadHeadThumb(Context context, String userId, String accessToken, String path, ILogin iLogin)
	{
		String out = null;

		if(userId != null && path != null)
		{
			//获取token
			String suff = NetCore2.GetFileSuffix(path, false);
			if(!TextUtils.isEmpty(suff))
			{
				try
				{
					mInfo = InterLoginUtils.getUploadHeadThumbToken(userId, accessToken, suff, iLogin);
					if(mInfo != null && mInfo.mUrls != null && mInfo.mUrls.length > 0)
					{

						OSSClient oss = GetOSS(context);
						if(oss != null)
						{
							suff = "." + suff;
							String objectKey = mInfo.mKeys[0] + suff;
							PutObjectRequest put = new PutObjectRequest(mInfo.mBucketName, objectKey, path);
							try
							{
								PutObjectResult putResult = oss.putObject(put);
								out = mInfo.mUrls[0];
								Log.d("path", out);
								Log.d("PutObject", "UploadSuccess");
								Log.d("ETag", putResult.getETag());
								Log.d("RequestId", putResult.getRequestId());
							}
							catch(ClientException e)
							{
								// 本地异常如网络异常等
								e.printStackTrace();
							}
							catch(ServiceException e)
							{
								// 服务异常
								Log.e("RequestId", e.getRequestId());
								Log.e("ErrorCode", e.getErrorCode());
								Log.e("HostId", e.getHostId());
								Log.e("RawMessage", e.getRawMessage());
							}
						}
					}
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		}

		return out;
	}
}
