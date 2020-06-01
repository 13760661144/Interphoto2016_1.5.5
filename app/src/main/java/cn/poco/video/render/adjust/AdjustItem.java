package cn.poco.video.render.adjust;

/**
 * Created by: fwc
 * Date: 2017/12/15
 */
public class AdjustItem {

	/**
	 * 暗角
	 */
	public static final int DARK_CORNER = 1;

	/**
	 * 白平衡（色温）
	 */
	public static final int WHITE_BALANCE = 2;

	/**
	 * 饱和度
	 */
	public static final int SATURATION = 3;

	/**
	 * 对比度
	 */
	public static final int CONTRAST = 4;

	/**
	 * 高光
	 */
	public static final int HIGHLIGHT = 5;

	/**
	 * 亮度
	 */
	public static final int BRIGHTNESS = 6;

	/**
	 * 锐化
	 */
	public static final int SHARPEN = 7;

	/**
	 * 色彩平衡（色调）
	 */
	public static final int COLOR_BALANCE = 8;

	/**
	 * 阴影
	 */
	public static final int SHADOW = 9;

	public int id;
	public float value;

	public AdjustItem(int id, float value) {
		this.id = id;
		this.value = value;
	}

	public static int getType(int id) {
		int type;
		switch (id) {
			case DARK_CORNER:
				type = AbstractAdjust.TYPE_DARK_CORNER;
				break;
			case SHARPEN:
				type = AbstractAdjust.TYPE_SHARPEN_ADJUST;
				break;
			default:
				type = AbstractAdjust.TYPE_VIDEO_ADJUST;
				break;
		}

		return type;
	}

	public static AbstractAdjust.Params getParams(int id) {

		AbstractAdjust.Params params;
		switch (id) {
			case DARK_CORNER:
				params = new DarkCornerAdjust.Params();
				break;
			case SHARPEN:
				params = new SharpenAdjust.Params();
				break;
			default:
				params = new VideoAdjust.Params();
				break;
		}

		return params;
	}
}
