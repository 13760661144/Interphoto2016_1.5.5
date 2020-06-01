package cn.poco.watermarksync.api;

import android.util.Log;

import org.json.JSONObject;

import cn.poco.pocointerfacelibs.AbsBaseInfo;
import cn.poco.watermarksync.model.Watermark;

/**
 * Created by Shine on 2017/3/1.
 */

public class UploadWatermarkCallbackApi extends AbsBaseInfo {
    public int mObjectId = Watermark.VALUE_NONE;
    public boolean mIsUploadSucceed;

    @Override
    public boolean DecodeMyData(Object object) throws Throwable {
        JSONObject jsonObject = (JSONObject) object;
        if (!(mCode == ApiConstant.WRONG_ARGUMENT || mCode == ApiConstant.OPERATION_FAILED)) {
            mIsUploadSucceed = true;
            if (jsonObject.has("ret_data")) {
                JSONObject retData = (JSONObject)jsonObject.get("ret_data");
                if (retData.has("object_id")) {
                    mObjectId = retData.getInt("object_id");
                };
            }
            return true;
        } else {
            mIsUploadSucceed = false;
            Log.i("ret_code", String.valueOf(mCode));
            return false;
        }
    }
}
