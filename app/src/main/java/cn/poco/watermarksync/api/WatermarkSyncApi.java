package cn.poco.watermarksync.api;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.poco.framework.MyFramework;
import cn.poco.framework.MyFramework2App;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.pocointerfacelibs.AbsBaseInfo;
import cn.poco.resource.FontResMgr;
import cn.poco.resource.MyLogoRes;
import cn.poco.resource.MyLogoResMgr;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.watermarksync.model.EditableWatermark;
import cn.poco.watermarksync.model.NotEditableWatermark;
import cn.poco.watermarksync.model.Watermark;
import cn.poco.watermarksync.util.FileUtil;
import cn.poco.watermarksync.util.MemorySyncHelper;

/**
 * Created by Shine on 2017/2/27.
 */

public class WatermarkSyncApi extends AbsBaseInfo{
    public boolean mIsSyncSucceed;
    public List<Watermark> mDeleteList = new ArrayList<>();
    public List<Watermark> mDownLoadList = new ArrayList<>();
    public List<Watermark> mUploadList = new ArrayList<>();




    @Override
    protected boolean DecodeMyData(Object object) throws Throwable {
        JSONObject jsonObject = (JSONObject) object;
        if (!(mCode == ApiConstant.WRONG_ARGUMENT || mCode == ApiConstant.OPERATION_FAILED)) {
            mIsSyncSucceed = true;
            JSONObject result = jsonObject.getJSONObject("ret_data");
            JSONArray deleteDataArray, downloadDataArray, uploadDataArray;

            boolean hasDeleteWatermark = result.has("delete");
            boolean hasDownloadWatermark = result.has("download");
            boolean hasUploadWatermark = result.has("upload");

            if (hasDeleteWatermark) {
                deleteDataArray = result.getJSONArray("delete");
                for (int i = 0; i <= deleteDataArray.length() - 1; i++) {
                    JSONObject currentJsonObject = deleteDataArray.getJSONObject(i);
                    String url = null;
                    if (currentJsonObject.has("wm_url")) {
                        url = currentJsonObject.getString("wm_url");
                    }

                    Watermark watermark = initWatermark(url, Watermark.VALUE_NONE);
                    watermark.setUserId(currentJsonObject.getString("user_id"));
                    Object objectId = currentJsonObject.get("wm_id");
                    if (objectId instanceof String) {
                        watermark.setObjectId(Integer.parseInt((String)objectId));
                    }

                    watermark.setTitle(currentJsonObject.getString("wm_title"));
                    watermark.setUrl(url);

                    // 字体信息
                    if (currentJsonObject.has("content")) {
                        String contentArray = currentJsonObject.getString("content");
                        if (!TextUtils.isEmpty(contentArray)) {
                            JSONArray jsonArray = new JSONArray(contentArray);
                            watermark.setResArray(FontResMgr.ReadResArr(jsonArray));
                        }
                    }

                    watermark.setSaveTime(currentJsonObject.getString("save_time"));
                    watermark.setUpdatTime(currentJsonObject.getString("update_time"));
                    watermark.setStatus(currentJsonObject.getInt("status"));
                    watermark.setOperateType(Watermark.OperateType.DELETE);
                    mDeleteList.add(watermark);
                }
            }

            if (hasDownloadWatermark) {
                downloadDataArray = result.getJSONArray("download");
                for (int i = 0; i <= downloadDataArray.length() - 1; i++) {
                    JSONObject currentJsonObject = downloadDataArray.getJSONObject(i);
                    String url = currentJsonObject.getString("wm_url");
                    Watermark watermark = initWatermark(url, Watermark.VALUE_NONE);

                    if (watermark != null) {
                        watermark.setUserId(currentJsonObject.getString("user_id"));
                        Object objectId = currentJsonObject.get("wm_id");
                        if (objectId instanceof String) {
                            watermark.setObjectId(Integer.parseInt((String)objectId));
                        }
                        watermark.setTitle(currentJsonObject.getString("wm_title"));
                        watermark.setUrl(url);
                        // 字体信息
                        if (currentJsonObject.has("content")) {
                            String contentArray = currentJsonObject.getString("content");
                            if (!TextUtils.isEmpty(contentArray)) {
                                JSONArray jsonArray = new JSONArray(contentArray);
                                watermark.setResArray(FontResMgr.ReadResArr(jsonArray));
                            }
                        }
                        watermark.setSaveTime(currentJsonObject.getString("save_time"));
                        watermark.setUpdatTime(currentJsonObject.getString("update_time"));
                        watermark.setStatus(currentJsonObject.getInt("status"));
                        watermark.setOperateType(Watermark.OperateType.DOWNLOAD);
                        mDownLoadList.add(watermark);
                    }
                }
            }

            if (hasUploadWatermark) {
                uploadDataArray = result.getJSONArray("upload");
                for (int i = 0; i <= uploadDataArray.length() - 1; i++) {
                    JSONObject currentJsonObject = uploadDataArray.getJSONObject(i);
                    int objectId = -1;
                    Object resultObject = null;
                    Object saveTimeObject = null;

                    String saveTime = null;

                    if (currentJsonObject.has("wm_id")) {
                        resultObject = currentJsonObject.get("wm_id");
                    }

                    if (currentJsonObject.has("save_time")) {
                        saveTimeObject = currentJsonObject.get("save_time");
                    }

                    if (resultObject == JSONObject.NULL && saveTimeObject == JSONObject.NULL) {
                        continue;
                    }


                    if (resultObject instanceof String) {
                        objectId = Integer.parseInt((String)resultObject);
                    }

                    if (saveTimeObject instanceof String) {
                        saveTime = currentJsonObject.getString("save_time");
                    }

                    Watermark watermark = initWatermark(null, objectId);

                    if (currentJsonObject.has("user_id")) {
                        watermark.setUserId(currentJsonObject.getString("user_id"));
                    }

                    if (currentJsonObject.has("wm_title")) {
                        watermark.setTitle(currentJsonObject.getString("wm_title"));
                    }

                    if (currentJsonObject.has("wm_url")) {
                        watermark.setUrl(currentJsonObject.getString("wm_url"));
                    }

                    if (currentJsonObject.has("update_time")) {
                        watermark.setUpdatTime(currentJsonObject.getString("update_time"));
                    }

                    if (currentJsonObject.has("status")) {
                        watermark.setStatus(currentJsonObject.getInt("status"));
                    }

                    watermark.setObjectId(objectId);
                    watermark.setSaveTime(saveTime);
                    watermark.setOperateType(Watermark.OperateType.UPLOAD);
                    mUploadList.add(watermark);
                }
            }
            return true;
        } else {
            mIsSyncSucceed = false;
            Log.i("ret_code", String.valueOf(mCode));
            return false;
        }
    }

    public int getTotalCount() {
        return mDownLoadList.size() + mUploadList.size() + mDeleteList.size();
    }

    private Watermark initWatermark(@Nullable String serverUrl, int objectId) {
        Watermark watermark = null;
        if (!TextUtils.isEmpty(serverUrl)) {
            final String originSuffix = FileUtil.getPathSuffix(serverUrl);
            final String editableSuffix = "zip";
            final String notEditableSuffix = "png";
            if (originSuffix.toUpperCase().equals(editableSuffix.toUpperCase())) {
                watermark = new EditableWatermark();
            } else if (originSuffix.toUpperCase().equals(notEditableSuffix.toUpperCase())) {
                watermark = new NotEditableWatermark();
            }
        } else {
            if (objectId != Watermark.VALUE_NONE) {
                String userId = SettingInfoMgr.GetSettingInfo(MyFramework2App.getInstance().getApplicationContext()).GetPoco2Id(true);
                MyLogoRes resItem = null;
                if (!TextUtils.isEmpty(userId)) {
                    resItem = MemorySyncHelper.getInstance(MyFramework2App.getInstance().getApplicationContext()).getItemByObjectIds(MyLogoResMgr.getInstance().GetMyLogoResArr(), objectId, userId);
                }

                if (resItem != null) {
                    if (resItem.m_editable) {
                        watermark = new EditableWatermark();
                    } else {
                        watermark = new NotEditableWatermark();
                    }
                }
            }
        }
        return watermark;
    }



}
