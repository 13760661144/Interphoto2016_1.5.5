package cn.poco.login.util;

import org.json.JSONObject;

import cn.poco.loginlibs.ILogin;
import cn.poco.pocointerfacelibs.PocoWebUtils;
import cn.poco.storagesystemlibs.UploadInfo;

/**
 * Created by admin on 2017/8/30.
 */

public class InterLoginUtils
{
	/**
	 *
	 * @param userId
	 * @param accessToken
	 * @param suffix	文件后缀
	 * @param iLogin
	 * @return
	 */
	public static UploadInfo getUploadHeadThumbToken(String userId, String accessToken, String suffix, ILogin iLogin)
	{
		UploadInfo out = null;

		try
		{
			JSONObject json = new JSONObject();
			json.put("user_id", userId);
			json.put("access_token", accessToken);
			json.put("file_ext", suffix);
			json.put("file_base_name_ext", suffix);
			json.put("file_base_name_count", 1);
			json.put("b_beauty_avatar", true);
			out = (UploadInfo)PocoWebUtils.Post(UploadInfo.class, iLogin.GetUploadHeadThumbTokenUrl(), false, json, null, null, iLogin);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

		return out;
	}
}
