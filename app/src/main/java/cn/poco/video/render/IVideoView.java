package cn.poco.video.render;

import android.graphics.Bitmap;

/**
 * Created by: fwc
 * Date: 2017/12/13
 */
public interface IVideoView {

	Bitmap getFrame();

	int getSurfaceWidth();

	int getSurfaceHeight();

	int getSurfaceLeft();

	int getSurfaceTop();

	void enterFrameAdjust();

	void exitFrameAdjust();

	void translateFrame(float dx, float dy);

	void scaleFrame(float px, float py, float scale);

	void doubleScaleFrame(float px, float py);

	void rotateFrame(boolean right);

	void resetFrameAdjust();
}
