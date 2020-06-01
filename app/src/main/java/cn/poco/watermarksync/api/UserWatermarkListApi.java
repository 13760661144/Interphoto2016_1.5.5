package cn.poco.watermarksync.api;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.poco.pocointerfacelibs.AbsBaseInfo;
import cn.poco.watermarksync.model.EditableWatermark;
import cn.poco.watermarksync.model.NotEditableWatermark;
import cn.poco.watermarksync.model.Watermark;
import cn.poco.watermarksync.util.FileUtil;

/**
 * Created by Shine on 2017/2/28.
 */

public class UserWatermarkListApi extends AbsBaseInfo{
    public List<Watermark> mWatermarkList = new ArrayList<>();
    public boolean mIsGetUserWatermarkSucceed;

    @Override
    protected boolean DecodeMyData(Object object) throws Throwable {
        JSONObject jsonObject = (JSONObject) object;
        if (!(mCode == ApiConstant.OPERATION_FAILED || mCode == ApiConstant.WRONG_ARGUMENT)) {
            mIsGetUserWatermarkSucceed = true;
            JSONArray result = jsonObject.getJSONArray("ret_data");
            for (int i = 0; i < result.length(); i++) {
                JSONObject currentObject = result.getJSONObject(i);
                String suffix = FileUtil.getPathSuffix(currentObject.getString("cover_img_url"));
                Watermark watermark = null;
                if (suffix.toUpperCase().equals("ZIP")) {
                    watermark = new EditableWatermark();
                } else if (suffix.toUpperCase().equals("IMG")){
                    watermark = new NotEditableWatermark();
                }

                try {
                    if (watermark != null) {
                        watermark.setObjectId(currentObject.getInt("object_id"));
                        watermark.setTitle(currentObject.getString("title"));
                        watermark.setSubTitle(currentObject.getString("sub_title"));
                        watermark.setSummary(currentObject.getString("summary"));
                        watermark.setTags(currentObject.getString("tags"));
                        watermark.setContent(currentObject.getString("content"));
                        watermark.setUrl(currentObject.getString("cover_img_url"));
                        watermark.setLocationId(currentObject.getString("location_id"));
                        if (currentObject.has("save_time")) {
                            watermark.setCustomData(String.valueOf(currentObject.get("save_time")));
                        };
                        mWatermarkList.add(watermark);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        } else {
            mIsGetUserWatermarkSucceed = false;
        }
        return mIsGetUserWatermarkSucceed;
    }
}
