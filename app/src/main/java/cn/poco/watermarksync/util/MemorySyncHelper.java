package cn.poco.watermarksync.util;

import android.content.Context;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.poco.resource.MyLogoRes;
import cn.poco.resource.MyLogoResMgr;
import cn.poco.watermarksync.model.Watermark;

/**
 * Created by Shine on 2017/3/13.
 */

public class MemorySyncHelper {
    private static volatile MemorySyncHelper sInstance = null;
    private Context mContext;

    public static MemorySyncHelper getInstance(Context context) {
        MemorySyncHelper localInstance = sInstance;
        if (localInstance == null) {
            synchronized (MemorySyncHelper.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                     sInstance = localInstance = new MemorySyncHelper(context);
                }
            }
        }
        return localInstance;
    }

    private MemorySyncHelper(Context context) {
        mContext = context;
    }


    // 本地id不更新
    public void updateMemoryData(MyLogoRes myLogoRes, Watermark watermark) {
        myLogoRes.mUniqueObjectId = watermark.getObjectId();
        myLogoRes.m_userId = Integer.parseInt(watermark.getUserId());

        // 内存更新
        if (!TextUtils.isEmpty(watermark.getSaveTime())) {
            myLogoRes.mSaveTime = watermark.getSaveTime();
        }

        if (!TextUtils.isEmpty(watermark.getTitle())) {
            myLogoRes.m_name = watermark.getTitle();
        }

        if (!TextUtils.isEmpty(watermark.getPath())) {
            myLogoRes.m_res = watermark.getPath();
        }

        if (watermark.getResArray() != null) {
            myLogoRes.m_resArr = JsonHelper.getInstacne().encodeToFontList(watermark.getResArray());
        }

        myLogoRes.mShouldDelete = watermark.shouldDelete();
        myLogoRes.mShouldModify = watermark.shouldModify();
    }


    public int deleteItemByObjectId(List<MyLogoRes> logoResList, int objectId, String userId) {
        int originIndex = -1;
        if (logoResList == null) {
            return originIndex;
        }

        MyLogoRes deleteTarget = null;
        for (MyLogoRes item : logoResList) {
            if (item.mUniqueObjectId == objectId && String.valueOf(item.m_userId).equals(userId)) {
                deleteTarget = item;
                break;
            }
        }
        if (deleteTarget != null) {
            originIndex = logoResList.indexOf(deleteTarget);
            logoResList.remove(deleteTarget);
        }
        return originIndex;
    }

    public boolean addItem(List<MyLogoRes> logoResList, MyLogoRes res) {
        return addItem(logoResList, res, 0);
    }

    public boolean addItem(List<MyLogoRes> logoResList, MyLogoRes res, int index) {
        boolean result = false;
        if (logoResList == null) {
            return result;
        }

        if (MyLogoResMgr.getInstance().HasItem((ArrayList)logoResList, res.m_userId, res.m_id) == -1) {
            if (index == -1) {
                ((ArrayList) logoResList).add(res);
            } else {
                ((ArrayList) logoResList).add(index, res);
            }
            result = true;
        }
        return result;
    }

    public MyLogoRes getItemByObjectIds(List<MyLogoRes> logoResList, int objectId, String userId) {
        for (MyLogoRes item : logoResList) {
            if (item.mUniqueObjectId == objectId && String.valueOf(item.m_userId).equals(userId)) {
                return item;
            }
        }
        return null;
    }

    public boolean isDataEqualLocally(MyLogoRes myLogoRes, Watermark watermark) {
        boolean isEqual = false;
        if (myLogoRes.m_res.equals(watermark.getPath()) && myLogoRes.m_userId == Integer.parseInt(watermark.getUserId())) {
            isEqual = true;
        }
        return isEqual;
    }

    public boolean isDataEqualFromServer(MyLogoRes myLogoRes, Watermark watermark) {
        boolean isEqual = false;
        if (myLogoRes.mUniqueObjectId == watermark.getObjectId() && myLogoRes.m_userId == Integer.parseInt(watermark.getUserId())) {
            isEqual = true;
        }
        return isEqual;
    }






}
