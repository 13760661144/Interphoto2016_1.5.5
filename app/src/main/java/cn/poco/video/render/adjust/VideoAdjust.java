package cn.poco.video.render.adjust;

import android.content.Context;

import cn.poco.interphoto2.R;

/**
 * Created by: fwc
 * Date: 2017/12/15
 * 视频调整：
 *  亮度：-30 ~ 30，默认为0
 * 	对比度：0.9 ~ 1.2，默认1
 * 	饱和度：0.5 ~ 1.5，默认1
 * 	色温：-0.35 ~ 0.35，默认0
 * 	色调：-0.1 ~ 0.1，默认0
 * 	阴影补偿：0 ~ 1，默认0
 * 	高光减淡：0 ~ 1，默认1
 */
public class VideoAdjust extends AbstractAdjust {

	public VideoAdjust(Context context) {
		super(context);

		createProgram(R.raw.vertex_shader, R.raw.fragment_video_adjust);
	}

	@Override
	protected void checkParams(AbstractAdjust.Params params) {
		if (!(params instanceof Params)) {
			throw new IllegalArgumentException();
		}
	}

	public static class Params extends AbstractAdjust.Params {

		public Params() {
			super(7);
		}

		@Override
		protected void initValues(float[] values) {
			values[0] = 0;
			values[1] = 1;
			values[2] = 1;
			values[3] = 0;
			values[4] = 0;
			values[5] = 0;
			values[6] = 1;
		}

		@Override
		public boolean isDefault() {
			if (values[0] != 0) {
				return false;
			} else if (values[1] != 1) {
				return false;
			} else if (values[2] != 1) {
				return false;
			} else if (values[3] != 0) {
				return false;
			} else if (values[4] != 0) {
				return false;
			} else if (values[5] != 0) {
				return false;
			} else if (values[6] != 1) {
				return false;
			}

			return true;
		}

		@Override
		public void addItem(AdjustItem item) {
			switch (item.id) {
				case AdjustItem.BRIGHTNESS:
					values[0] = item.value;
					break;
				case AdjustItem.CONTRAST:
					values[1] = item.value;
					break;
				case AdjustItem.SATURATION:
					values[2] = item.value;
					break;
				case AdjustItem.WHITE_BALANCE:
					values[3] = item.value;
					break;
				case AdjustItem.COLOR_BALANCE:
					values[4] = item.value;
					break;
				case AdjustItem.SHADOW:
					values[5] = item.value;
					break;
				case AdjustItem.HIGHLIGHT:
					values[6] = item.value;
					break;
				default:
					throw new RuntimeException();
			}
		}
	}
}
