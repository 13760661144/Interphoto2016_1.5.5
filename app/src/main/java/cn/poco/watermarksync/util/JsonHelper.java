package cn.poco.watermarksync.util;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.resource.FontRes;
import cn.poco.watermarksync.manager.WatermarkSyncManager;
import cn.poco.watermarksync.model.Watermark;

/**
 * Created by Shine on 2017/3/3.
 */

public class JsonHelper {
    private static volatile JsonHelper sInstance = null;


    public static JsonHelper getInstacne() {
        JsonHelper localInstance = sInstance;
        if (localInstance == null) {
            synchronized (WatermarkSyncManager.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new JsonHelper();
                }
            }
        }
        return localInstance;
    }

    private JsonHelper() {

    }


    public String updateWholeWatermarkValueFromLocal(String content, Watermark watermark) {
        String newJsonString = null;
        try {
            String result = makeUpFileJson(content);
            JSONObject jsonObject = new JSONObject(result);
            JSONArray dataArray = jsonObject.getJSONArray(Constant.LOCAL_JSON_DATAKEY);
            if (dataArray != null && dataArray.length() > 0) {
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject curObject = (JSONObject) dataArray.get(i);
                    String localUrl = curObject.getString(Constant.KEY_URL);
                    int userIdfo = curObject.getInt(Constant.KEY_USER_ID);
                    String watermarkPath = watermark.getPath();
                    String watermarkUserId = watermark.getUserId();

                    if (localUrl.equals(watermarkPath) && userIdfo == Integer.parseInt(watermarkUserId)){
                        curObject.put(Constant.LOCAL_JSON_ID, watermark.getLocalId());
                        curObject.put(Constant.KEY_USER_ID, Integer.parseInt(watermark.getUserId()));
                        curObject.put(Constant.KEY_OBJECTID, watermark.getObjectId());
                        curObject.put(Constant.TITLE, watermark.getTitle());
                        curObject.put(Constant.KEY_URL, watermark.getPath());
                        curObject.put(Constant.EDITABLE, watermark.isEditable());
                        curObject.put(Constant.KEY_SAVETIME, watermark.getSaveTime());
                        curObject.put(Constant.KEY_SHOULD_DELETE, watermark.shouldDelete());
                        curObject.put(Constant.KEY_SHOULD_MODIFY, watermark.shouldModify());
                        // 更新字体信息
                        if (TextUtils.isEmpty(watermark.getResArray())) {
                            JSONArray jsonArray = new JSONArray();
                            curObject.put(Constant.KEY_RES_ARRAY, jsonArray);
                        } else {
                            curObject.put(Constant.KEY_RES_ARRAY, watermark.getResArray());
                        }
                    }
                }
            }
            newJsonString = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return newJsonString;
        }
    }

    public String updateWholeWatermarkValueFromServer(String content, Watermark watermark) {
        String newJsonString = null;
        try {
            String result = makeUpFileJson(content);
            JSONObject jsonObject = new JSONObject(result);
            JSONArray dataArray = jsonObject.getJSONArray(Constant.LOCAL_JSON_DATAKEY);
            if (dataArray != null && dataArray.length() > 0) {
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject curObject = (JSONObject) dataArray.get(i);
                    int objectId = curObject.getInt(Constant.KEY_OBJECTID);
                    int userIdfo = curObject.getInt(Constant.KEY_USER_ID);

                    int watermarkObjectId = watermark.getObjectId();
                    String watermarkUserId = watermark.getUserId();

                    if (objectId == watermarkObjectId && userIdfo == Integer.parseInt(watermarkUserId)){
                        curObject.put(Constant.LOCAL_JSON_ID, watermark.getLocalId());
                        curObject.put(Constant.KEY_USER_ID, Integer.parseInt(watermark.getUserId()));
                        curObject.put(Constant.KEY_OBJECTID, watermark.getObjectId());
                        curObject.put(Constant.TITLE, watermark.getTitle());
                        curObject.put(Constant.KEY_URL, watermark.getPath());
                        curObject.put(Constant.EDITABLE, watermark.isEditable());
                        curObject.put(Constant.KEY_SAVETIME, watermark.getSaveTime());
                        curObject.put(Constant.KEY_SHOULD_DELETE, watermark.shouldDelete());
                        curObject.put(Constant.KEY_SHOULD_MODIFY, watermark.shouldModify());
                        // 更新字体信息
                        if (TextUtils.isEmpty(watermark.getResArray())) {
                            JSONArray jsonArray = new JSONArray();
                            curObject.put(Constant.KEY_RES_ARRAY, jsonArray);
                        } else {
                            curObject.put(Constant.KEY_RES_ARRAY, watermark.getResArray());
                        }
                    }
                }
            }
            newJsonString = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return newJsonString;
        }
    }






    public String addResDataArrayItem(String content, Watermark watermark) {
        String result = null;
        try {
            String fileresult = makeUpFileJson(content);
            JSONObject jsonObject = new JSONObject(fileresult);
            JSONArray originJsonArray = jsonObject.getJSONArray(Constant.LOCAL_JSON_DATAKEY);

            JSONArray finalJsonArray = new JSONArray();

            JSONObject addItem = new JSONObject();
            addItem.put(Constant.LOCAL_JSON_ID, watermark.getLocalId());
            addItem.put(Constant.KEY_USER_ID, Integer.parseInt(watermark.getUserId()));
            addItem.put(Constant.KEY_OBJECTID, watermark.getObjectId());
            addItem.put(Constant.TITLE, watermark.getTitle());
            addItem.put(Constant.KEY_URL, watermark.getPath());
            addItem.put(Constant.EDITABLE, watermark.isEditable());
            addItem.put(Constant.KEY_SAVETIME, watermark.getSaveTime());
            addItem.put(Constant.KEY_SHOULD_DELETE, false);
            addItem.put(Constant.KEY_SHOULD_MODIFY, false);
            if (TextUtils.isEmpty(watermark.getResArray())) {
                JSONArray jsonArray = new JSONArray();
                addItem.put(Constant.KEY_RES_ARRAY, jsonArray);
            } else {
                addItem.put(Constant.KEY_RES_ARRAY, watermark.getResArray());
            }
            finalJsonArray.put(0, addItem);
            for (int i = 0; i < originJsonArray.length(); i++) {
                finalJsonArray.put(originJsonArray.get(i));
            }
            jsonObject.remove(Constant.LOCAL_JSON_DATAKEY);
            jsonObject.put(Constant.LOCAL_JSON_DATAKEY, finalJsonArray);
            result = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }



    // 从本地资源json文件中的data数组中移除特定的item;
    public String removeResDataArrayItemLocally(String jsonString, Watermark watermark) {
        String result = null;
        JSONObject jsonObject;
        try {
            String fileContent = makeUpFileJson(jsonString);
            jsonObject = new JSONObject(fileContent);
            JSONArray oldDataArray = jsonObject.getJSONArray(Constant.LOCAL_JSON_DATAKEY);
            JSONArray newDataArray = new JSONArray();
            for (int i = 0; i < oldDataArray.length(); i++) {
                JSONObject json = (JSONObject) oldDataArray.get(i);
                String url = json.getString(Constant.KEY_URL);
                int uId = json.getInt(Constant.KEY_USER_ID);

                String watermarkPath = watermark.getPath();
                String watermarkUserId = watermark.getUserId();


                if (!url.equals(watermarkPath) || Integer.parseInt(watermarkUserId) != uId) {
                    newDataArray.put(json);
                }
            }
            result = encodeLocalResjson(newDataArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String removeResDataArrayItemByServerId(String jsonString, int objectId, String userId) {
        String result = null;
        JSONObject jsonObject;
        try {
            String fileContent = makeUpFileJson(jsonString);
            jsonObject = new JSONObject(fileContent);
            JSONArray oldDataArray = jsonObject.getJSONArray(Constant.LOCAL_JSON_DATAKEY);
            JSONArray newDataArray = new JSONArray();
            for (int i = 0; i < oldDataArray.length(); i++) {
                JSONObject json = (JSONObject) oldDataArray.get(i);
                int curObjectId = json.getInt(Constant.KEY_OBJECTID);
                int uId = json.getInt(Constant.KEY_USER_ID);
                if (curObjectId != objectId || Integer.parseInt(userId) != uId) {
                    newDataArray.put(json);
                }
            }
            result = encodeLocalResjson(newDataArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getSpecificLocalZipPath(String jsonString, int objectId, String userId) {
        String targetZipPath = null;
        JSONObject jsonObject;
        try {
            String fileString = makeUpFileJson(jsonString);
            jsonObject = new JSONObject(fileString);
            JSONArray dataArray = jsonObject.getJSONArray(Constant.LOCAL_JSON_DATAKEY);
            if (dataArray != null && dataArray.length() > 0) {
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject currentJsonObject = (JSONObject) dataArray.get(i);
                    if ((currentJsonObject.getInt(Constant.KEY_OBJECTID)) == objectId && currentJsonObject.getInt(Constant.KEY_USER_ID) == Integer.parseInt(userId)) {
                        targetZipPath = currentJsonObject.getString("url");
                        return targetZipPath;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return targetZipPath;
    }

    public String makeUpSyncWaterJson(List<WatermarkSyncManager.SyncUploadInfo> infoList) {
        JSONArray subJson = new JSONArray();
        if (infoList.size() > 0) {
            for (WatermarkSyncManager.SyncUploadInfo item : infoList) {
                HashMap<String, Object> subElement = new HashMap<>();
                subElement.put("wm_id", item.mObjectId);
                subElement.put("save_time", item.mSaveTime);
                JSONObject jo = new JSONObject(subElement);
                subJson.put(jo);
            }
        } else {
            HashMap<String, Object> subElement = new HashMap<>();
            subElement.put("wm_id", null);
            subElement.put("save_time", null);
            JSONObject jo = new JSONObject(subElement);
            subJson.put(jo);
        }
        String result = subJson.toString();
        return result;
    }

    private String makeUpFileJson(String jsonString) throws JSONException{
        JSONObject jsonObject;
        if (TextUtils.isEmpty(jsonString)) {
            jsonObject = new JSONObject();
            jsonObject.put(Constant.KEY_VERSION, Constant.CUR_VERSION);
            JSONArray dataArray= new JSONArray();
            jsonObject.put(Constant.LOCAL_JSON_DATAKEY, dataArray);
            return jsonObject.toString();
        } else {
            return jsonString;
        }
    }


    private String encodeLocalResjson(String dataJson) {
        String result;
        JSONObject whole  = new JSONObject();
        try {
            whole.put("ver", Constant.CUR_VERSION);
            JSONArray data = new JSONArray(dataJson);
            whole.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        result = whole.toString();
        return result;
    }



public String formatFontInfoToJson(ArrayList<FontRes> fonList) {
        String result = null;
        JSONArray res_arr = new JSONArray();
        if(fonList != null && fonList.size() > 0)
        {
            int arrLen = fonList.size();
            JSONObject arrObj;
            FontRes fontRes;
            for(int i = 0; i < arrLen; i++)
            {
                fontRes = fonList.get(i);
                arrObj = new JSONObject();
                try {
                    arrObj.put("id", fontRes.m_id);
                    arrObj.put("size", fontRes.m_size);
                    arrObj.put("zip_url", fontRes.url_res);
                    res_arr.put(arrObj);
                    result = res_arr.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public ArrayList<FontRes> encodeToFontList(String fontResJson) {
        ArrayList<FontRes> fontResList = null;
        try {
            int fontId = 0;
            float fontSize = 0.0f;
            String fontUrlRes = null;
            if (!TextUtils.isEmpty(fontResJson)) {
                fontResList = new ArrayList<>();
                JSONArray jsonArray = new JSONArray(fontResJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    FontRes fontRes = new FontRes();
                    JSONObject curObject = (JSONObject) jsonArray.get(i);
                    if (curObject.has(Constant.KEY_RES_ID)) {
                        fontId = curObject.getInt(Constant.KEY_RES_ID);
                    }

                    if (curObject.has(Constant.KEY_RES_SIZE)) {
                        fontSize = (float)curObject.getDouble(Constant.KEY_RES_SIZE);
                    }

                    if (curObject.has(Constant.KEY_RES_ZIP_URL)) {
                        fontUrlRes = curObject.getString(Constant.KEY_RES_ZIP_URL);
                    }
                    fontRes.m_id = fontId;
                    fontRes.m_size = fontSize;
                    fontRes.url_res = fontUrlRes;
                    fontResList.add(fontRes);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fontResList;
    }





}
