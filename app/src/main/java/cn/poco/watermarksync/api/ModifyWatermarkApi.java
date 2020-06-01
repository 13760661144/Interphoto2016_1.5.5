package cn.poco.watermarksync.api;

import android.util.Log;

import cn.poco.pocointerfacelibs.AbsBaseInfo;

/**
 * Created by Shine on 2017/3/6.
 */

public class ModifyWatermarkApi extends AbsBaseInfo{
    public boolean mIsModifySucceed = false;

    @Override
    protected boolean DecodeMyData(Object object) throws Throwable {
        boolean validCondition = !(mCode == ApiConstant.WRONG_ARGUMENT || mCode == ApiConstant.MODIFY_OPETATION_FAILED_SPECIFIC
                || mCode == ApiConstant.MODIFY_WATERMARK_PLEASE_UPDATE || mCode == ApiConstant.MODIFY_WATERMARK_NOT_EXIST);
        if (validCondition) {
            mIsModifySucceed = true;
        } else {
            Log.i("ret_code", String.valueOf(mCode));
            if (mCode == ApiConstant.MODIFY_WATERMARK_PLEASE_UPDATE || mCode == ApiConstant.MODIFY_WATERMARK_NOT_EXIST) {
                mIsModifySucceed = true;
            } else {
                mIsModifySucceed = false;
            }
        }
        return mIsModifySucceed;
    }
}
