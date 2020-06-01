package cn.poco.watermarksync.api;

import android.util.Log;

import cn.poco.pocointerfacelibs.AbsBaseInfo;

/**
 * Created by Shine on 2017/3/6.
 */

public class DeleteWatermarkApi extends AbsBaseInfo{
    public boolean mIsDeleteSucceed;

    @Override
    protected boolean DecodeMyData(Object object) throws Throwable {
        if (!(mCode == ApiConstant.WRONG_ARGUMENT || mCode == ApiConstant.OPERATION_FAILED)) {
            mIsDeleteSucceed = true;
            return true;
        } else {
            Log.i("ret_code", String.valueOf(mCode));
            mIsDeleteSucceed = false;
            return false;
        }
    }
}
