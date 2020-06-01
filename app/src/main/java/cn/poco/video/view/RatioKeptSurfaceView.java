package cn.poco.video.view;

import android.content.Context;
import android.view.SurfaceView;

/**
 * Created by Shine on 2017/6/2.
 */

public class RatioKeptSurfaceView extends SurfaceView{
    private int mVideoWidth, mVideoHeight;

    public RatioKeptSurfaceView(Context context) {
        super(context);
    }

    public void setVideoDimension(int videoWidth, int videoHeight) {
        mVideoWidth = videoWidth;
        mVideoHeight = videoHeight;
    }


        @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                width = widthSpecSize;
                height = heightSpecSize;

                if (mVideoWidth * height < mVideoHeight * width) {
                    width =  height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > mVideoHeight * width) {
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightMeasureSpec == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthMeasureSpec == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    width = widthSpecSize;
                }
            } else {
                width = mVideoWidth;
                height = mVideoHeight;

                if (widthMeasureSpec == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    height = width * mVideoHeight / mVideoWidth;
                }

                if (heightMeasureSpec == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    width = height * mVideoWidth / mVideoHeight;
                }
            }
        }
        setMeasuredDimension(width, height);
    }

}
