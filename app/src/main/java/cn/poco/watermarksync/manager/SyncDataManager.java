package cn.poco.watermarksync.manager;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.resource.MyLogoRes;
import cn.poco.resource.MyLogoResMgr;
import cn.poco.watermarksync.model.EditableWatermark;
import cn.poco.watermarksync.model.NotEditableWatermark;
import cn.poco.watermarksync.model.Watermark;
import cn.poco.watermarksync.util.FileUtil;
import cn.poco.watermarksync.util.JsonHelper;
import cn.poco.watermarksync.util.MemorySyncHelper;

import static cn.poco.resource.MyLogoResMgr.BASE_RES_ID;
import static cn.poco.watermarksync.util.FileUtil.readSpecificFileContent;

/**
 * Created by Shine on 2017/3/20.
 */

public class SyncDataManager {
    private static SyncDataManager sInstance = null;
    private Context mContext;

    public static SyncDataManager getInstance(Context context) {
        SyncDataManager localInstance = sInstance;
        if (localInstance == null) {
            synchronized (SyncDataManager.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new SyncDataManager(context);
                }
            }
        }
        return localInstance;
    }

    private SyncDataManager (Context context) {
        mContext = context;
    }

    // 添加水印
    public void addWatermark(Watermark watermark) {
        MyLogoRes myLogoRes = watermark.changeToMyLogoRes();
        int id = BASE_RES_ID;
        while(MyLogoResMgr.getInstance().HasItem(MyLogoResMgr.m_sdcardArr, myLogoRes.m_userId, id) != -1)
        {
            id++;
        }
        myLogoRes.m_id = id;

        // 如果已经存在objectId 和 userId相同的水印，先删除。（这种情况在app里面先在文件夹删除水印，然后同步会出现）
        if (MyLogoResMgr.m_sdcardArr != null) {
            int originIndex = MemorySyncHelper.getInstance(mContext).deleteItemByObjectId(MyLogoResMgr.m_sdcardArr, watermark.getObjectId(), watermark.getUserId());
            originIndex = originIndex == -1 ? 0 : originIndex;

            // 完美还原原来出现的位置，同时将新下载下来的zip包，更新到内存中
            boolean result = MemorySyncHelper.getInstance(mContext).addItem(MyLogoResMgr.m_sdcardArr, myLogoRes, originIndex);
            // 成功添加到内存后，才写进文件
            if (result) {
                watermark.setLocalId(myLogoRes.m_id);
                String fileContent = readSpecificFileContent(MyLogoResMgr.SDCARD_PATH);
                // 下载完成，写入资源记录文件
                String finalContent = JsonHelper.getInstacne().addResDataArrayItem(fileContent, watermark);
                FileUtil.writeDataToFile(MyLogoResMgr.SDCARD_PATH, finalContent);
            }
        }
    }

    //通过本地url以及水印名字更新水印数据
    public void updateWatermarkByLocally(Watermark watermark) {
        for (MyLogoRes item : MyLogoResMgr.m_sdcardArr) {
            if (MemorySyncHelper.getInstance(mContext).isDataEqualLocally(item, watermark)) {
                // 更新内存数据
                MemorySyncHelper.getInstance(mContext).updateMemoryData(item, watermark);
                // 更新资源文件
                FileUtil.modifyJsonFileContent(MyLogoResMgr.SDCARD_PATH, watermark, false);
                break;
            }
        }
    }

    // 通过obejctId更新水印数据
    public void updateWatermarkByObjectIds(Watermark watermark) {
        for (MyLogoRes item : MyLogoResMgr.m_sdcardArr) {
            if (MemorySyncHelper.getInstance(mContext).isDataEqualFromServer(item, watermark)) {
                // 更新内存数据
                MemorySyncHelper.getInstance(mContext).updateMemoryData(item, watermark);
                watermark.setLocalId(item.m_id);
                // 更新资源文件
                FileUtil.modifyJsonFileContent(MyLogoResMgr.SDCARD_PATH, watermark, true);
                break;
            }
        }
    }

    // 更新资源文件
    public void updateResFileRecordLocally(Watermark watermark) {
        String fileContent = readSpecificFileContent(MyLogoResMgr.SDCARD_PATH);
        String resultContent = JsonHelper.getInstacne().updateWholeWatermarkValueFromLocal(fileContent, watermark);
        FileUtil.writeDataToFile(MyLogoResMgr.SDCARD_PATH, resultContent);
    }


    // 通过objectId,删除水印
    public void deleteWatermarkByObjectId(Watermark watermark) {
        // 内存
        MemorySyncHelper.getInstance(mContext).deleteItemByObjectId(MyLogoResMgr.m_sdcardArr, watermark.getObjectId(), watermark.getUserId());
        deleteZipPackgeFromLocal(watermark.getObjectId(), watermark.getUserId());

        // 资源文件
        String fileContent = FileUtil.readSpecificFileContent(MyLogoResMgr.SDCARD_PATH);
        if (!TextUtils.isEmpty(fileContent)) {
            String content = JsonHelper.getInstacne().removeResDataArrayItemByServerId(fileContent, watermark.getObjectId(), watermark.getUserId());
            FileUtil.writeDataToFile(MyLogoResMgr.SDCARD_PATH, content);
        }
    }

    // 删除对应路径的包
    public void deleteZipPackgeFromLocal(int objectId, String userId) {
        File file = new File(MyLogoResMgr.SDCARD_PATH);
        if (file.exists()){
            String fileContent = readSpecificFileContent(MyLogoResMgr.SDCARD_PATH);
            String targetZipPath = JsonHelper.getInstacne().getSpecificLocalZipPath(fileContent, objectId, userId);
            FileUtil.deleteFile(targetZipPath);
        }
    }

    // 删除资源文件对应的json item
    public void deleteResFileRecordByLocally(Watermark watermark) {
        String fileContent = readSpecificFileContent(MyLogoResMgr.SDCARD_PATH);
        // 本地删除记录
        String resultContent = JsonHelper.getInstacne().removeResDataArrayItemLocally(fileContent, watermark);
        FileUtil.writeDataToFile(MyLogoResMgr.SDCARD_PATH, resultContent);
    }

    // 判断新下载的水印是否为新的水印
    public boolean isNewWatermark(Watermark watermark) {
        boolean result = false;
        for (MyLogoRes res : MyLogoResMgr.getInstance().ReadSDCardResArr()) {
            if (MemorySyncHelper.getInstance(mContext).isDataEqualFromServer(res, watermark)) {
                result = true;
                break;
            }
        }
        return result;
    }

    // 修正网络数据
    public boolean isAnyDataNeedToCorrect(String userId) {
        HashMap<Integer, List<Watermark>> result = categoryPendentHandleList(userId);
        List<Watermark> uploadList = result.get(0);
        List<Watermark> deleteList = result.get(1);
        List<Watermark> modifyList = result.get(2);
        int correctCount = uploadList.size() + deleteList.size() + modifyList.size();
        return correctCount > 0 ? true : false;
    }



    // 从资源文件中获取并封装成全部的水印
    public List<Watermark> getAllWatermarks(String userId) {
        final List<Watermark> watermarkList = new ArrayList<>();
        for (MyLogoRes item : MyLogoResMgr.getInstance().ReadSDCardResArr()) {
            if (String.valueOf(item.m_userId).equals(userId)) {
                Watermark watermark;
                if (item.m_editable) {
                    watermark = new EditableWatermark(item);
                } else {
                    watermark = new NotEditableWatermark(item);
                }
                watermarkList.add(watermark);
            }
        }
        return watermarkList;
    }

    public List<Watermark> getAllDeleteWatermark(String userId, Watermark.OperateType type) {
        List<Watermark> watermarkList = new ArrayList<>();
        for (MyLogoRes item : MyLogoResMgr.getInstance().ReadSDCardResArr()) {
            if (String.valueOf(item.m_userId).equals(userId) && item.mShouldDelete) {
                Watermark watermark;
                if (item.m_editable) {
                    watermark = new EditableWatermark(item);
                } else {
                    watermark = new NotEditableWatermark(item);
                }
                // 设置操作类型为同步
                watermark.setOperateType(type);
                watermarkList.add(watermark);
            }
        }
        return watermarkList;
    }

    public List<Watermark> getAllWatermarkExceptMarkAsDelete(String userId, Watermark.OperateType type) {
        List<Watermark> watermarkList = new ArrayList<>();
        for (MyLogoRes item : MyLogoResMgr.getInstance().ReadSDCardResArr()) {
            if (String.valueOf(item.m_userId).equals(userId) && !item.mShouldDelete) {
                Watermark watermark;
                if (item.m_editable) {
                    watermark = new EditableWatermark(item);
                } else {
                    watermark = new NotEditableWatermark(item);
                }
                // 设置操作类型为同步
                watermark.setOperateType(type);
                watermarkList.add(watermark);
            }
        }
        return watermarkList;
    }


    public HashMap<Integer, List<Watermark>> categoryPendentHandleList(String userId) {
        HashMap<Integer, List<Watermark>> result = new HashMap<>();
        List<Watermark> uploadList = new ArrayList<>();
        List<Watermark> deleteList = new ArrayList<>();
        List<Watermark> modifyList = new ArrayList<>();
        result.put(0, uploadList);
        result.put(1, deleteList);
        result.put(2, modifyList);

        List<Watermark> resList = this.getAllWatermarks(userId);
        for (Watermark watermark : resList) {
            // 首先只要是删除，必须先执行删除
            if (watermark.shouldDelete()) {
                watermark.setShouldDelete(true);
                watermark.setOperateType(Watermark.OperateType.DELETE);
                deleteList.add(watermark);
            } else if (watermark.getObjectId() == Watermark.VALUE_NONE) {
                // 将上传的情况优先于修改
                watermark.setOperateType(Watermark.OperateType.UPLOAD);
                uploadList.add(watermark);
            } else if (watermark.shouldModify()) {
                // 最后剩下的全部都是修改
                watermark.setOperateType(Watermark.OperateType.MODIFY);
                watermark.setShouldModify(true);
                modifyList.add(watermark);
            }
        }
        return result;
    }

}
