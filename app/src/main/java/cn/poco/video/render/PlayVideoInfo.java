package cn.poco.video.render;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.opengl.Matrix;

import cn.poco.video.decode.DecodeUtils;
import cn.poco.video.decode.VideoInfo;
import cn.poco.video.render.curve.CurveEffect;

/**
 * Created by: fwc
 * Date: 2017/9/27
 */
public class PlayVideoInfo {

	private static final int MAX_SCALE = 2;

	public String path;
	public VideoInfo data;

	public float[] mvpMatrix = new float[16];
	public float[] texMatrix = new float[16];

	public float[] saveMatrix = new float[16];

	private float curScale = 1f;
	private int rotateAngle = 0;

	private float initScaleX;
	private float initScaleY;

	private RectF mRectF = new RectF();

	private boolean doAnimation = false;

	private AdjustInfo mAdjustInfo;

	public CurveEffect.Params curve;

	public boolean isMute = false;

	public void initMvpMatrix(float scaleX, float scaleY) {

		scaleX = checkScale(scaleX);
		scaleY = checkScale(scaleY);

		initScaleX = scaleX;
		initScaleY = scaleY;

		Matrix.setIdentityM(mvpMatrix, 0);
		Matrix.scaleM(mvpMatrix, 0, scaleX, scaleY, 1);

		Matrix.setIdentityM(texMatrix, 0);

		mAdjustInfo = new AdjustInfo();
		curve = new CurveEffect.Params();
	}

	private float checkScale(float scale) {
		return (int)(scale * 100) / 100f;
	}

	public void enterFrameAdjust() {
		mAdjustInfo.save(this);
	}

	public void resetFrameAdjust() {
		mAdjustInfo.reset(this);
	}

	public void translate(float dx, float dy, float left, float top) {

		if (doAnimation) {
			return;
		}

		final float scaleX = mvpMatrix[0];
		final float scaleY = mvpMatrix[5];

		dy = -dy;

		float tempDx = dx * scaleX;
		float tempDy = dy * scaleY;

		float bottom = -top;
		float right = -left;

		mapRect();
		if (mRectF.left + tempDx > left) {
			dx = (left - mRectF.left) / scaleX;
		}

		if (mRectF.right + tempDx < right) {
			dx = (right - mRectF.right) / scaleX;
		}

		if (mRectF.top + tempDy < top) {
			dy = (top - mRectF.top) / scaleY;
		}

		if (mRectF.bottom + tempDy > bottom) {
			dy = (bottom - mRectF.bottom) / scaleY;
		}

		if (dx != 0 || dy != 0) {
			Matrix.translateM(mvpMatrix, 0, dx, dy, 0);
		}
	}

	public void scale(int width, int height, float px, float py, float scale, float left, float top) {

		if (doAnimation) {
			return;
		}

		float right = -left;
		float bottom = -top;

		if (curScale * scale > MAX_SCALE) {
			scale = MAX_SCALE / curScale;
		} else if (curScale * scale < 1) {
			scale = 1f / curScale;
		}

		scaleMvp(width / 2f, height / 2f, px, py, scale);

		curScale *= scale;

		if (scale < 1) {
			checkBounds(left, top, right, bottom);
		}
	}

	public void doubleScale(int width, int height, float px, float py,
							float left, float top, Runnable refresh) {
		if (doAnimation) {
			return;
		}

		float fromScale;
		if (curScale == MAX_SCALE) {
			fromScale = MAX_SCALE;
			curScale = 1;
		} else {
			fromScale = curScale;
			curScale = 2;
		}

		scaleAnime(width / 2f, height / 2f, px, py, fromScale, curScale, left, top, refresh);
	}

	private void scaleAnime(final float centerX, final float centerY,
							final float px, final float py,
							float fromScale, float toScale,
							final float left, final float top,
							final Runnable refresh) {
		doAnimation = true;
		final float right = -left;
		final float bottom = -top;
		ValueAnimator animator = ValueAnimator.ofFloat(fromScale, toScale);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float scale = (float) animation.getAnimatedValue();

				Matrix.setIdentityM(mvpMatrix, 0);
				Matrix.scaleM(mvpMatrix, 0, initScaleX, initScaleY, 1);

				scaleMvp(centerX, centerY, px, py, scale);
				checkBounds(left, top, right, bottom);
				refresh.run();
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				doAnimation = false;
			}
		});
		animator.setDuration(300);
		animator.start();
	}

	private void scaleMvp(float centerX, float centerY, float px, float py, float scale) {

//		float tx = (px - centerX) / centerX + mvpMatrix[12];
//		float ty = (centerY - py) / centerY + mvpMatrix[13];
//
//		float scaleX = mvpMatrix[0];
//		float scaleY = mvpMatrix[5];
//
//		if (rotateAngle % 180 != 0) {
//			float temp = tx;
//			tx = ty;
//			ty = temp;
//
//			temp = scaleX;
//			scaleX = scaleY;
//			scaleY = temp;
//		}

//		Matrix.translateM(mvpMatrix, 0, tx / scaleX, ty / scaleY, 0);
		Matrix.scaleM(mvpMatrix, 0, scale, scale, 1);
//		Matrix.translateM(mvpMatrix, 0, -tx / scaleX, -ty / scaleY, 0);
	}

	/**
	 * 边界检测
	 */
	private void checkBounds(float left, float top, float right, float bottom) {
		float dx = 0;
		float dy = 0;

		final float scaleX = mvpMatrix[0];
		final float scaleY = mvpMatrix[5];

		mapRect();
		if (mRectF.left > left) {
			dx = (left - mRectF.left) / scaleX;
		}

		if (mRectF.right < right) {
			dx = (right - mRectF.right) / scaleX;
		}

		if (mRectF.top < top) {
			dy = (top - mRectF.top) / scaleY;
		}

		if (mRectF.bottom > bottom) {
			dy = (bottom - mRectF.bottom) / scaleY;
		}

		if (dx != 0 || dy != 0) {
			Matrix.translateM(mvpMatrix, 0, dx, dy, 0);
		}
	}

	private void mapRect() {
		mRectF.left = -mvpMatrix[0] + mvpMatrix[4] + mvpMatrix[12];
		mRectF.top = -mvpMatrix[1] + mvpMatrix[5] + mvpMatrix[13];
		mRectF.right = mvpMatrix[0] - mvpMatrix[4] + mvpMatrix[12];
		mRectF.bottom = mvpMatrix[1] - mvpMatrix[5] + mvpMatrix[13];
	}

	public void rotate(boolean right, float showRatio, Runnable refresh) {

		if (doAnimation) {
			return;
		}

		float fromDegree = (rotateAngle + 360) % 360;

		rotateAngle += right ? -90 : 90;
		rotateAngle = (rotateAngle + 360) % 360;
		float videoRatio = getVideoRatio();
		if (rotateAngle % 180 != 0) {
			videoRatio = 1 / videoRatio;
		}
		float fromScaleX = mvpMatrix[0];
		float fromScaleY = mvpMatrix[5];

		if (videoRatio >= showRatio) {
			if (showRatio > 1) {
				initScaleX = videoRatio / showRatio;
				initScaleY = 1 / showRatio;
			} else {
				initScaleX = videoRatio;
				initScaleY = 1;
			}
		} else {
			if (showRatio > 1) {
				initScaleX = 1;
				initScaleY = 1 / videoRatio;
			} else {
				initScaleX = showRatio;
				initScaleY = showRatio / videoRatio;
			}
		}

		curScale = 1f;

		float toDegree = (rotateAngle + 360) % 360;
		if (fromDegree == 0 && toDegree == 270) {
			fromDegree = 360;
		} else if (fromDegree == 270 && toDegree == 0) {
			fromDegree = -90;
		}
		rotateAnim(fromScaleX, initScaleX,
				fromScaleY, initScaleY,
				fromDegree, toDegree, refresh);
	}

	private void rotateAnim(final float fromScaleX, final float toScaleX,
							final float fromScaleY, final float toScaleY,
							final float fromDegree, final float toDegree,
							final Runnable refresh) {
		doAnimation = true;
		ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float ratio = (float)animation.getAnimatedValue();

				Matrix.setIdentityM(mvpMatrix, 0);
				float scaleX = (toScaleX - fromScaleX) * ratio + fromScaleX;
				float scaleY = (toScaleY - fromScaleY) * ratio + fromScaleY;
				Matrix.setIdentityM(mvpMatrix, 0);
				Matrix.scaleM(mvpMatrix, 0, scaleX, scaleY, 1);

				float degree = (toDegree - fromDegree) * ratio + fromDegree;
				Matrix.setIdentityM(texMatrix, 0);
				Matrix.translateM(texMatrix, 0, 0.5f, 0.5f, 0);
				Matrix.rotateM(texMatrix, 0, degree, 0, 0, 1);
				Matrix.translateM(texMatrix, 0, -0.5f, -0.5f, 0);

				refresh.run();
			}
		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				doAnimation = false;
			}
		});
		animator.setDuration(200);
		animator.start();
	}

	public PlayVideoInfo changeVideoPath(String videoPath) {
		PlayVideoInfo info = new PlayVideoInfo();
		info.path = videoPath;
		info.data = DecodeUtils.getVideoInfo(videoPath);
		info.initScaleX = initScaleX;
		info.initScaleY = initScaleY;
		System.arraycopy(mvpMatrix, 0, info.mvpMatrix, 0, 16);
		System.arraycopy(texMatrix, 0, info.texMatrix, 0, 16);
		info.curScale = curScale;
		info.rotateAngle = rotateAngle;
		info.isMute = isMute;
		info.curve = curve.Clone();
		info.mAdjustInfo = new AdjustInfo();

		return info;
	}

	public PlayVideoInfo Clone() {
		PlayVideoInfo info = new PlayVideoInfo();
		info.path = path;
		info.data = data.Clone();
		info.initScaleX = initScaleX;
		info.initScaleY = initScaleY;
		System.arraycopy(mvpMatrix, 0, info.mvpMatrix, 0, 16);
		System.arraycopy(texMatrix, 0, info.texMatrix, 0, 16);
		info.curScale = curScale;
		info.rotateAngle = rotateAngle;
		info.isMute = isMute;
		info.curve = curve.Clone();
		info.mAdjustInfo = new AdjustInfo();

		return info;
	}

	public float getVideoRatio() {
		int w = data.width;
		int h = data.height;
		if (data.rotation % 180 != 0) {
			w = data.height;
			h = data.width;
		}

		return w / (float)h;
	}

	public void calculateSaveMatrix(float left, float top) {

		float scaleX = 1;
		float scaleY = 1;
		if (left != 0) {
			scaleX = 1 / Math.abs(left);
		}
		if (top != 0) {
			scaleY = 1 / Math.abs(top);
		}

		float[] temp = new float[16];
		Matrix.setIdentityM(saveMatrix, 0);
		Matrix.setIdentityM(temp, 0);
		Matrix.scaleM(temp, 0, scaleX, scaleY, 1);
		Matrix.multiplyMM(saveMatrix, 0, temp, 0, mvpMatrix, 0);
	}

	private static class AdjustInfo {

		public float[] mvpMatrix = new float[16];
		public float[] texMatrix = new float[16];

		private float curScale = 1f;
		private int rotateAngle = 0;

		void save(PlayVideoInfo info) {
			this.curScale = info.curScale;
			this.rotateAngle = info.rotateAngle;

			System.arraycopy(info.mvpMatrix, 0, this.mvpMatrix, 0, 16);
			System.arraycopy(info.texMatrix, 0, this.texMatrix, 0, 16);
		}

		void reset(PlayVideoInfo info) {
			info.curScale = this.curScale;
			info.rotateAngle = this.rotateAngle;

			System.arraycopy(this.mvpMatrix, 0, info.mvpMatrix, 0, 16);
			System.arraycopy(this.texMatrix, 0, info.texMatrix, 0, 16);
		}
	}
}
