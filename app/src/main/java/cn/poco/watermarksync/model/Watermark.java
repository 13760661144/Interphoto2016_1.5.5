package cn.poco.watermarksync.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;

import cn.poco.resource.FontRes;
import cn.poco.resource.MyLogoRes;
import cn.poco.watermarksync.util.JsonHelper;

/**
 * Created by Shine on 2017/2/27.
 */

public abstract class Watermark implements Serializable{
    protected String mSuffix;
    public static final int VALUE_NONE = -1;
    public static final String USER_NONE = "-1";

    private int mObjectId = VALUE_NONE;
    private int mLocalId = VALUE_NONE;
    // 用户id
    private String mUserId = USER_NONE;

    private String mTitle;
    private String tags;

    // optional
    private String mSubTitle;
    private long mVolume;
    private String mContent;
    private String mLocationId;
    private String mCustomData;

    // 服务器地址
    private String mUrl;
    // 本地路径
    private String mPath;

    private String mSummary;
    private String mSaveTime;
    private String mUpdatTime;
    private int mStatus;

    protected boolean mIsEditable = true;
    private boolean mShouldDelete;
    private boolean mShouldModify;

    // 识别id
    private Object mCarrayData;
    private int mAcid;
    private OperateType mOperateType;

    private String mResJson;


    public enum OperateType {
        UPLOAD,
        DOWNLOAD,
        DELETE,
        MODIFY,
        MODIFY_UPLOAD,
        SYNC;
    }

    public Watermark(MyLogoRes res) {
        init(res);
    }

    protected void init(MyLogoRes res) {
        this.setUserId(String.valueOf(res.m_userId));
        this.setLocalId(res.m_id);
        this.setObjectId(res.mUniqueObjectId);
        this.setSaveTime(res.mSaveTime);
        this.setTitle(res.m_name);
        if (res.m_res instanceof String) {
            this.setPath((String)res.m_res);
        }
        this.setEditable(res.m_editable);
        this.setShouldModify(res.mShouldModify);
        this.setShouldDelete(res.mShouldDelete);
        this.setResArray(res.m_resArr);
    }



    public Watermark() {

    }


    public MyLogoRes changeToMyLogoRes() {
        MyLogoRes myLogoRes = new MyLogoRes();
        if (this.getLocalId() != VALUE_NONE) {
            myLogoRes.m_id = this.getLocalId();
        }

        if (!TextUtils.isEmpty(this.getUserId())) {
            myLogoRes.m_userId = Integer.parseInt(this.getUserId());
        }

        myLogoRes.mUniqueObjectId = this.getObjectId();
        myLogoRes.m_name = this.getTitle();
        myLogoRes.m_editable = this.isEditable();
        myLogoRes.m_res = this.getPath();
        myLogoRes.mSaveTime = this.getSaveTime();
        myLogoRes.mShouldDelete = this.shouldDelete();
        myLogoRes.mShouldModify = this.shouldModify();

        myLogoRes.m_resArr = JsonHelper.getInstacne().encodeToFontList(this.getResArray());
        return myLogoRes;
    }


    public abstract String generateNewPathAfterDownload();






    public int getObjectId() {
        return mObjectId;
    }

    public void setObjectId(int mObjectId) {
        this.mObjectId = mObjectId;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public int getLocalId() {
        return mLocalId;
    }

    public void setLocalId(int mLocalId) {
        this.mLocalId = mLocalId;
    }

    public String getCustomData() {
        return mCustomData;
    }

    public void setCustomData(String mCustomData) {
        this.mCustomData = mCustomData;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String mSummary) {
        this.mSummary = mSummary;
    }

    public String getLocationId() {
        return mLocationId;
    }

    public void setLocationId(String mLocationId) {
        this.mLocationId = mLocationId;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public void setPath(String path) {
        this.mPath = path;
    }

    public String getPath() {
        return mPath;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String mSubTitle) {
        this.mSubTitle = mSubTitle;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int mStatus) {
        this.mStatus = mStatus;
    }

    public String getUpdatTime() {
        return mUpdatTime;
    }

    public void setUpdatTime(String mUpdatTime) {
        this.mUpdatTime = mUpdatTime;
    }

    public String getSaveTime() {
        return mSaveTime;
    }

    public void setSaveTime(String mSaveTime) {
        this.mSaveTime = mSaveTime;
    }


    public Object getCarrayData() {
        return mCarrayData;
    }

    public void setCarrayData(Object mCarrayData) {
        this.mCarrayData = mCarrayData;
    }

    public int getAcid() {
        return mAcid;
    }

    public void setAcid(int acid) {
        this.mAcid = acid;
    }

    public OperateType getOperateType() {
        return mOperateType;
    }

    public void setOperateType(OperateType mOperateType) {
        this.mOperateType = mOperateType;
    }

    public void setEditable(boolean isEdit) {
        this.mIsEditable = isEdit;
    }

    public boolean isEditable() {
        return mIsEditable;
    }

    public boolean shouldDelete() {
        return mShouldDelete;
    }

    public void setShouldDelete(boolean mShouldDelete) {
        this.mShouldDelete = mShouldDelete;
    }

    public boolean shouldModify() {
        return mShouldModify;
    }

    public void setShouldModify(boolean mShouldModify) {
        this.mShouldModify = mShouldModify;
    }

    public void setVolume(long volume) {
        this.mVolume = volume;
    }

    public long getVolume() {
        return mVolume;
    }

    public void setResArray(ArrayList<FontRes> array) {
        this.mResJson = JsonHelper.getInstacne().formatFontInfoToJson(array);
    }

    public String getResArray() {
        return mResJson;
    }


}
