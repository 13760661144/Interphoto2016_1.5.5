package cn.poco.camera;

/**
 * Created by zwq on 2016/04/29 11:36.<br/><br/>
 */
public class TeachInfo {

    private static final String TAG = TeachInfo.class.getName();

    private String id;
    private Object previewPic;
    private Object maskPic;
    private int tongjiId;
    private boolean fistItemId;
    private boolean endItemId;
    private int currentItemId;
    private int theme ;//与主题数组下标对应


    public TeachInfo(String id, Object preview, Object mask, int tongjiId) {
        this.id = id;
        this.previewPic = preview;
        this.maskPic = mask;
        this.tongjiId = tongjiId;
    }

    public TeachInfo(String id, Object previewPic, Object maskPic, int tongjiId, boolean fistItemId, boolean endItemId,int theme) {
        this.id = id;
        this.previewPic = previewPic;
        this.maskPic = maskPic;
        this.tongjiId = tongjiId;
        this.fistItemId = fistItemId;
        this.endItemId = endItemId;
        this.theme = theme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getMaskPic() {
        return maskPic;
    }

    public void setMaskPic(Object maskPic) {
        this.maskPic = maskPic;
    }

    public Object getPreviewPic() {
        return previewPic;
    }

    public void setPreviewPic(Object previewPic) {
        this.previewPic = previewPic;
    }

    public int getTongjiId() {
        return tongjiId;
    }

    public void setTongjiId(int tongjiId) {
        this.tongjiId = tongjiId;
    }

    public boolean isFistItemId() {
        return fistItemId;
    }

    public void setFistItemId(boolean fistItemId) {
        this.fistItemId = fistItemId;
    }

    public boolean isEndItemId() {
        return endItemId;
    }

    public void setEndItemId(boolean endItemId) {
        this.endItemId = endItemId;
    }

    public int getCurrentItemId() {
        return currentItemId;
    }

    public void setCurrentItemId(int currentItemId) {
        this.currentItemId = currentItemId;
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }
}