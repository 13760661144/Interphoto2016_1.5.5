package cn.poco.watermarksync.api;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.poco.pocointerfacelibs.PocoWebUtils;
import cn.poco.resource.FontRes;
import cn.poco.watermarksync.manager.WatermarkSyncManager;
import cn.poco.watermarksync.model.WatermarkUpdateInfo;
import cn.poco.watermarksync.util.JsonHelper;

/**
 * Created by Shine on 2017/2/27.
 */

public class WatermarkSyncWebUtil {

    public static UploadWatermarkCallbackApi getUploadWatermarkCallback(WatermarkUpdateInfo updateInfo, IWatermarkSync iWater) {
        UploadWatermarkCallbackApi uploadWatermarkCallbackApi = null;

        JSONObject json = new JSONObject();
        if (updateInfo != null) {
            try {
                json.put("user_id", updateInfo.mUserId);
                json.put("access_token", updateInfo.mAccessToken);
                json.put("title", updateInfo.mTitle);
                json.put("cover_img_url", updateInfo.mCoverImgUrl);
                if (!TextUtils.isEmpty(updateInfo.mFontInfo)) {
                    json.put("content", updateInfo.mFontInfo);
                }
                json.put("volume", updateInfo.mFileVolume);
                json.put("save_time", Long.valueOf(updateInfo.mSaveTime));
                uploadWatermarkCallbackApi = (UploadWatermarkCallbackApi) PocoWebUtils.Post(UploadWatermarkCallbackApi.class, iWater.GetUpdateMyWebUrl(), false, json, null, null, iWater);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return uploadWatermarkCallbackApi;
    }



    public static WatermarkSyncApi getWatermarkSyncData(String userId, String accessToken, String data, IWatermarkSync iWater) {
        WatermarkSyncApi watermarkSyncApi = null;
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("access_token", accessToken);
            json.put("data", data);
            watermarkSyncApi = (WatermarkSyncApi)PocoWebUtils.Post(WatermarkSyncApi.class, iWater.getWatermarkSyncData(), false, json, null, null, iWater);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return watermarkSyncApi;
    }

    public static DeleteWatermarkApi deleteUserWatermark(String userId, String accessToken, int objectId, IWatermarkSync iWater) {
        DeleteWatermarkApi deleteWatermarkApi = null;
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("access_token", accessToken);
            json.put("object_id", objectId);
            deleteWatermarkApi = (DeleteWatermarkApi)PocoWebUtils.Post(DeleteWatermarkApi.class, iWater.deleteWatermark(), false, json, null, null, iWater);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return deleteWatermarkApi;
    }


    public static UserWatermarkListApi getUserWatermarkList(String userId, String accessToken, IWatermarkSync iWater) {
        UserWatermarkListApi userWatermarkList = null;
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", userId);
            json.put("access_token", accessToken);
            userWatermarkList = (UserWatermarkListApi) PocoWebUtils.Get(UserWatermarkListApi.class, iWater.getUserWatermarkList(), false, json, null, iWater);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return userWatermarkList;
    }

    public static ModifyWatermarkApi modifyWatermarkApi(String userId, String accessToken, int objectId, IWatermarkSync iwater, WatermarkSyncManager.ModifyWatermarkInfo info) {
        ModifyWatermarkApi modifyWatermarkApi = null;
        JSONObject json = new JSONObject();
        String mTitle = info.mWatermarkTitle;
        String mSubTitle = info.mWatermarkSubTitle;
        String mSummary = info.mWatermarkSummary;
        String tags = info.mWatermarkTags;
        String content = info.mWaterContent;
        String path = info.mWatermarkServerUrl;
        int locationId = info.mLocationId;
        String customData = info.mCustomData;
        String saveTime = info.mSaveTime;
        ArrayList<FontRes> fontInfo = info.mFontInfo;
        String font = JsonHelper.getInstacne().formatFontInfoToJson(fontInfo);
        long fileVolume = info.mVolume;

        try {
            json.put("user_id", Integer.parseInt(userId));
            json.put("access_token", accessToken);
            json.put("object_id", objectId);
            putValidKeyToJson("title", mTitle, json);
            putValidKeyToJson("sub_title", mSubTitle, json);
            putValidKeyToJson("summary", mSummary, json);
            putValidKeyToJson("tags", tags, json);
            putValidKeyToJson("content", content, json);
            putValidKeyToJson("cover_img_url", path, json);
            putValidKeyToJson("location_id", locationId, json);
            putValidKeyToJson("custom_data", customData, json);
            putValidKeyToJson("save_time", saveTime, json);
            putValidKeyToJson("content", font, json);
            putValidKeyToJson("volume", fileVolume, json);
            modifyWatermarkApi = (ModifyWatermarkApi) PocoWebUtils.Post(ModifyWatermarkApi.class, iwater.modifyWatermark(), false, json, null, null, iwater);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return modifyWatermarkApi;
    }

    private static void putValidKeyToJson(String key, Object value, JSONObject json) throws JSONException {
        if (!TextUtils.isEmpty(String.valueOf(value))) {
            if (value instanceof Integer) {
                if(!((Integer)value == 0)) {
                    json.put(key, value);
                };
            } else if (value instanceof Long) {
                if(!((Long)value == 0)) {
                    json.put(key, value);
                };
            } else if (value instanceof String){
                json.put(key, value);
            }
        }
    }




}
