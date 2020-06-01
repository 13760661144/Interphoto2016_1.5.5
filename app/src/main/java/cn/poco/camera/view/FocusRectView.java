package cn.poco.camera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.ImageView;

import cn.poco.interphoto2.R;

/**
 * Created by zwq on 2016/11/10 12:17.<br/><br/>
 * 对焦框
 */
public class FocusRectView extends ImageView {

    private static final String TAG = FocusRectView.class.getName();

    private Bitmap mFocusBmp;
    private Bitmap mFocusFinishBmp;
    private Bitmap mMeteringBmp;

    private Rect mMaxArea;
    private float mFocusX, mFocusY;
    private float mMeteringX, mMeteringY;
    private RectF[] mViewLocation;

    /** 0:idle, 1:start, 2:finish */
    private int mFocusState;

    /**
     * 0:idle, 1:prepare, 2:drawing
     */
    private int mDrawState;
    private boolean isClear = true;
    private float tx, ty;
    private Matrix mMatrix;

    public FocusRectView(Context context) {
        super(context);
        mViewLocation = new RectF[2];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mMaxArea = new Rect(0, 0, width, height);
    }

    private void initBmp() {
        if (mFocusBmp == null || !mFocusBmp.isRecycled()) {
            mFocusBmp = BitmapFactory.decodeResource(getResources(), R.drawable.camera_focusing);
        }
        if (mFocusFinishBmp == null || !mFocusFinishBmp.isRecycled()) {
            mFocusFinishBmp = BitmapFactory.decodeResource(getResources(), R.drawable.camera_focusing);
        }
        if (mMeteringBmp == null || !mMeteringBmp.isRecycled()) {
            mMeteringBmp = BitmapFactory.decodeResource(getResources(), R.drawable.camera_metering);
        }
    }

    public void setMaxArea(Rect area) {
        if (area != null) {
            mMaxArea = area;
        }
    }

    public void setFocusLocation(float x, float y) {
        setFocusAndMeteringLocation(x, y, mMeteringX, mMeteringY);
    }

    public void setMeteringLocation(float x, float y) {
        setFocusAndMeteringLocation(mFocusX, mFocusY, x, y);
    }

    public void setFocusAndMeteringLocation(float fx, float fy, float mx, float my) {
        if (mDrawState != 0) {
            return;
        }
        mFocusX = fx;
        mFocusY = fy;
        mMeteringX = mx;
        mMeteringY = my;
        mDrawState = 1;
        mFocusState = 1;
        isClear = false;
        postInvalidate();
    }

    public void setFocusFinish() {
        if (mFocusState == 1) {
            mDrawState = 1;
            mFocusState = 2;
            isClear = false;
            postInvalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isClear) {
            mDrawState = 0;
            return;
        }
        mDrawState = 2;
        if (mMaxArea != null) {
            initBmp();
            if (mMatrix == null) {
                mMatrix = new Matrix();
            }

            RectF rectF = mViewLocation[0];
            if (rectF == null) {
                rectF = new RectF();
                mViewLocation[0] = rectF;
            }
            if (mFocusState == 1) {
                if (mFocusX > 0 && mFocusY > 0 && mFocusBmp != null && !mFocusBmp.isRecycled()) {
                    mMatrix.reset();
                    tx = checkBound(mFocusX - mFocusBmp.getWidth() / 2, mMaxArea.left, mMaxArea.right - mFocusBmp.getWidth());
                    ty = checkBound(mFocusY - mFocusBmp.getHeight() / 2, mMaxArea.top, mMaxArea.bottom - mFocusBmp.getHeight());
                    mMatrix.postTranslate(tx, ty);
                    canvas.drawBitmap(mFocusBmp, mMatrix, null);

                    rectF.left = tx;
                    rectF.top = ty;
                    rectF.right = rectF.left + mFocusBmp.getWidth();
                    rectF.bottom = rectF.top + mFocusBmp.getHeight();
                }
            } else if (mFocusState == 2) {
                if (mFocusX > 0 && mFocusY > 0 && mFocusFinishBmp != null && !mFocusFinishBmp.isRecycled()) {
                    mMatrix.reset();
                    tx = checkBound(mFocusX - mFocusFinishBmp.getWidth() / 2, mMaxArea.left, mMaxArea.right - mFocusFinishBmp.getWidth());
                    ty = checkBound(mFocusY - mFocusFinishBmp.getHeight() / 2, mMaxArea.top, mMaxArea.bottom - mFocusFinishBmp.getHeight());
                    mMatrix.postTranslate(tx, ty);
                    canvas.drawBitmap(mFocusFinishBmp, mMatrix, null);

                    rectF.left = tx;
                    rectF.top = ty;
                    rectF.right = rectF.left + mFocusFinishBmp.getWidth();
                    rectF.bottom = rectF.top + mFocusFinishBmp.getHeight();
                }
                mFocusState = 0;
            }

            rectF = mViewLocation[1];
            if (rectF == null) {
                rectF = new RectF();
                mViewLocation[1] = rectF;
            }
            if (mMeteringX > 0 && mMeteringY > 0 && mMeteringBmp != null && !mMeteringBmp.isRecycled()) {
                mMatrix.reset();
                tx = checkBound(mMeteringX - mMeteringBmp.getWidth() / 2, mMaxArea.left, mMaxArea.right - mMeteringBmp.getWidth());
                ty = checkBound(mMeteringY - mMeteringBmp.getHeight() / 2, mMaxArea.top, mMaxArea.bottom - mMeteringBmp.getHeight());
                mMatrix.postTranslate(tx, ty);
                canvas.drawBitmap(mMeteringBmp, mMatrix, null);

                rectF.left = tx;
                rectF.top = ty;
                rectF.right = rectF.left + mMeteringBmp.getWidth();
                rectF.bottom = rectF.top + mMeteringBmp.getHeight();
            }
        }
        mDrawState = 0;
    }

    private float checkBound(float value, float min, float max) {
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
        return value;
    }

    public RectF[] getViewLocation() {
        return mViewLocation;
    }

    public float[] getFoucuAndMeterPosition() {
        float[] positions = new float[4];
        positions[0] = mFocusX;
        positions[1] = mFocusY;
        positions[2] = mMeteringX;
        positions[3] = mMeteringY;

        return positions;
    }

    public int getTouchAreaIndex(float x, float y) {
        int countPos = 0;
        if (mViewLocation != null) {
            countPos = mViewLocation.length - 1;
            for (int i = mViewLocation.length - 1; i >= 0; i--) {
                RectF rectF = mViewLocation[i];
                if (rectF != null && x > rectF.left && x < rectF.right && y > rectF.top && y < rectF.bottom) {
                    return i;
                } else {
                    countPos--;
                }
            }
        }
        return countPos;
    }

    public void clearAll() {
        if (!isClear) {
            isClear = true;
            postInvalidate();
        }
    }

    public void release() {
        if (mFocusBmp != null && !mFocusBmp.isRecycled()) {
            mFocusBmp.recycle();
            mFocusBmp = null;
        }
        if (mMeteringBmp != null && !mMeteringBmp.isRecycled()) {
            mMeteringBmp.recycle();
            mMeteringBmp = null;
        }
        mMatrix = null;
    }
}
