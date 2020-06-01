package cn.poco.video.render.transition;

/**
 * Created by: fwc
 * Date: 2017/12/13
 */
public class TransitionItem {

	public static final int DEFAULT_TIME = 1000;

	/**
	 * 滤镜id
	 */
	public static final int NONE = 0;
	public static final int ALPHA = 1;
	public static final int BLACK = 2;
	public static final int WHITE = 3;
	public static final int BLUR = 4;

	/**
	 * 滤镜类型
	 */
	public static final int TYPE_NONE = 0x10;
	public static final int TYPE_LINEAR = 0x11;
	public static final int TYPE_BLEND = 0x12;

	public static boolean isBlendTransition(int id) {
		boolean result = false;

		if (id == TransitionItem.ALPHA || id == TransitionItem.BLUR) {
			result = true;
		}

		return result;
	}

	public static void setValue(AbstractTransition transition, int id) {
		switch (id) {
			case BLACK:
				transition.setValue(LinearTransition.MASK_BLACK);
				break;
			case WHITE:
				transition.setValue(LinearTransition.MASK_WHITE);
				break;
		}
	}

	public static int getType(int id) {
		int type = TYPE_NONE;
		switch (id) {
			case NONE:
				type = TYPE_NONE;
				break;
			case BLACK:
			case WHITE:
				type = TYPE_LINEAR;
				break;
			case ALPHA:
			case BLUR:
				type = TYPE_BLEND;
				break;
		}

		return type;
	}
}
