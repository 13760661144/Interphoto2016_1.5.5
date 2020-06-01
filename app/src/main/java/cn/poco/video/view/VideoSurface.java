package cn.poco.video.view;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by admin on 2018/1/8.
 */

public class VideoSurface extends SurfaceView {
    private static final String TAG = "VideoSurface";
    private int videoWidth, videoHeight;
    private GestureDetectorCompat gestureDetectorCompat;


    /*    public VideoSurface(Context context ,GestureDetectorCompat detectorCompat) {
            super(context);
            this.gestureDetectorCompat = detectorCompat;
            setClickable(true);

        }*/
    public VideoSurface(Context context) {
        super(context);
    }


    public VideoSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setVideoMeasuredDimension(int width, int height) {
        videoWidth = width;
        videoHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ;
        float videoRatio = videoWidth * 1.0f / videoHeight;
        if (videoRatio > 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            float ratio = width * 1f / height;
            if (videoRatio > ratio) {
                setMeasuredDimension(width, (int) (width / videoRatio));
            } else {
                setMeasuredDimension((int) (height * videoRatio), height);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

/*    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }*/
}
