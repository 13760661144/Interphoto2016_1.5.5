package cn.poco.image;

import android.R.integer;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;

public class PocoPsAdjust {

	//此类选择处理通道
	public static class ColorChannelType {
		public static final int RGB = 0x0007;        //全图/RGB	7
		public static final int Red = 0x0001;        //红色		1
		public static final int Yellow = 0x0020;    //黄色		32
		public static final int Green = 0x0002;        //绿色		2
		public static final int Cyan = 0x0008;        //青色		8
		public static final int Blue = 0x0004;        //蓝色		4
		public static final int Magenta = 0x0010;    //洋红		16
		public static final int White = 0x0040;        //白色		64
		public static final int Neutral = 0x0080;    //中性色		128
		public static final int Black = 0x0100;        //黑色		256
	}

	public static void resizeBitmap(Bitmap destBmp, Bitmap srcBmp) {
		if (srcBmp == null)
			return;

		int width = destBmp.getWidth();
		int height = destBmp.getHeight();
		Canvas canvas = new Canvas(destBmp);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		Matrix matrix = new Matrix();
		matrix.postScale((float) width / srcBmp.getWidth(), (float) height / srcBmp.getHeight());
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		canvas.drawBitmap(srcBmp, matrix, paint);
	}

	/**
	 * 对比度调整，PS中新版，通过曲线调整得到。
	 * 范围[-50,100]		默认值0
	 */
	public static Bitmap AdjustContrast(Bitmap dest, int value) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (value == 0)
			return dest;

		PocoNativeFilter.ContrastAdjust(dest, value);

		return dest;
	}

	/**
	 * 亮度调整，PS中新版，线性的，value值较大时有失真。
	 * 范围[-150,150]		默认值0
	 */
	public static Bitmap AdjustBrightness(Bitmap dest, int value) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (value == 0)
			return dest;

		value = (int) (value * 0.5 + 0.5);

		PocoNativeFilter.BrightnessAdjust(dest, value);

		return dest;
	}

	/**
	 * 曲线调整
	 *
	 * @param Colorchannel 通道(可选：RGB, 红, 绿, 蓝)
	 *                     controlPoints[] 控制点坐标数组按x,y排序相对坐标	默认[0,0,1,1]
	 *                     count 控制点个数   默认2
	 */
	public static Bitmap AdjustCurve(Bitmap dest, int Colorchannel, float controlPoints[], int count) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		int ctrPoints[] = new int[count * 2];
		for (int i = 0; i < 2 * count; i++) {
			ctrPoints[i] = (int) (controlPoints[i] * 255 + 0.5);
		}

		PocoNativeFilter.CurveAdjsutPs(dest, Colorchannel, ctrPoints, count);

		return dest;
	}

	/**
	 * 生成曲线
	 * @param controlPoints[] 控制点坐标（相对坐标）
	 * @param count 控制点个数s
	 * @param CurvePoints 曲线点数组 （点的个数为横坐标的长度）
	 * @param screenW screenH 坐标显示于屏幕的宽和高  宽和高等值
	 */
	public static int CurvesCreate(float controlPoints[], int count, int[] CurvePoints, int screenW, int screenH)
	{
		if(controlPoints == null || controlPoints.length != 2*count)
			return 0;

		if(CurvePoints.length /2 != screenW || screenW != screenH)
			return 0;

		int ctrPoints[] = new int[2*count];
		for(int i=0; i<count; i++)
		{
			ctrPoints[2*i] = (int)(controlPoints[2*i]*screenW+0.5);
			ctrPoints[2*i+1] = (int)(controlPoints[2*i+1]*screenH+0.5);
		}

		int[] result = new int[screenW];
		PocoNativeFilter.CreateCurvesPs(result, screenW, ctrPoints, count);

		for(int i=0; i<screenW; i++)
		{
			CurvePoints[2*i] = i;
			CurvePoints[2*i+1] = result[i];
		}

		return 1;
	}
	/**
	 * 曝光度
	 *
	 * @param value 曝光度  范围[-100, 100]  默认值0
	 */
	public static Bitmap AdjustExposure(Bitmap dest, int value) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (value == 0)
			return dest;

		PocoNativeFilter.ExposureAdjust(dest, value);

		return dest;
	}

	/**
	 * 色阶调整
	 *
	 * @param Colorchannel 通道(可选：RGB, 红, 绿, 蓝)
	 * @param input_low    输入色阶 黑点[0-253]		默认值0
	 * @param gamma        输入色阶 灰色gamma[9.99-0.01]	默认值1.0
	 * @param input_high   输入色阶[2-255]			默认值255
	 * @param output_low   输出色阶[0-255]			默认值0
	 * @param output_high  输出色阶[0-255]		默认值255
	 */
	public static Bitmap AdjustColorLevel(Bitmap dest, int Colorchannel, int input_low, double gamma, int input_high, int output_low, int output_high) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (input_low > input_high - 2)
			input_low = input_high - 2;

		if(input_low == 0 && input_high == 255 && output_low == 0 && output_high == 255 && gamma == 1.0)
			return dest;

		PocoNativeFilter.ColorLeverAdjust(dest, Colorchannel, input_low, gamma, input_high, output_low, output_high);

		return dest;
	}

	/**
	 * 色相/饱和度调整  PS界面
	 *
	 * @param Colorchannel 处理颜色通道
	 * @param Hvalue       色相参数 范围[-180, 180]	默认0
	 * @param Svalue       饱和度参数 范围[-100, 100]  默认0
	 * @param Bvalue       明度参数 范围[-100, 100]	 默认0
	 */
	public static Bitmap AdjustHueAndSaturation(Bitmap dest, int Colorchannel, int Hvalue, int Svalue, int Bvalue) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if ((Hvalue == 0) && (Svalue == 0) && (Bvalue == 0))
			return dest;

		Bvalue = (int) (0.5 * Bvalue + 0.5);

		PocoNativeFilter.HueAndSaturationAdjust(dest, Colorchannel, Hvalue, Svalue, Bvalue);

		return dest;
	}

	/**
	 * 黑白调整
	 *
	 * @param red     范围[-200, 300] 默认值40
	 * @param yellow  范围[-200, 300]	默认值60
	 * @param green   范围[-200, 300]	默认值40
	 * @param cyan    范围[-200, 300]	默认值60
	 * @param blue    范围[-200, 300]	默认值20
	 * @param magenta 范围[-200, 300]	默认值80
	 * @param isDone  是否处理   true处理  false不处理
	 */
	public static Bitmap AdjustBlackWhite(Bitmap dest, int red, int yellow, int green, int cyan, int blue, int magenta, boolean isDone) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (isDone == false)
			return dest;

		PocoNativeFilter.BlackWhiteAdjust(dest, red, yellow, green, cyan, blue, magenta);

		return dest;
	}

	/**
	 * 色彩平衡调整(青-红， 洋红-绿， 黄-蓝)	默认中间调
	 *
	 * @param cyan_red_l, magenta_green_l, yellow_blue_l 阴影 :范围[-100, 100]
	 * @param cyan_red_m, magenta_green_m, yellow_blue_m  中间调  :范围[-100, 100]
	 * @param cyan_red_h, magenta_green_h, yellow_blue_h  高光 ：范围[-100, 100]
	 */
	public static Bitmap AdjustColorBalance(Bitmap dest, int cyan_red_l, int cyan_red_m, int cyan_red_h,
											int magenta_green_l, int magenta_green_m, int magenta_green_h,
											int yellow_blue_l, int yellow_blue_m, int yellow_blue_h) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if(cyan_red_l == 0 && cyan_red_m == 0 && cyan_red_h == 0 &&
				magenta_green_l == 0 && magenta_green_m == 0 && magenta_green_h == 0 &&
				yellow_blue_l == 0 && yellow_blue_m == 0 && yellow_blue_h == 0)
			return dest;

		PocoNativeFilter.ColorBalanceAdjust(dest, (double) cyan_red_l, (double) cyan_red_m, (double) cyan_red_h,
				(double) magenta_green_l, (double) magenta_green_m, (double) magenta_green_h,
				(double) yellow_blue_l, (double) yellow_blue_m, (double) yellow_blue_h,
				0);

		return dest;
	}

	/**
	 * 通道混合器(有三个通道，每个通道含红绿蓝三个参数以及一个常数)
	 *
	 * @param rRedPercent, rGreenPercent, rBluePercent	红色通道参数	范围[-200, 200]
	 * @param rConstant    红色通道常数 		范围[-200, 200]
	 * @param gRedPercent, gGreenPercent, gBluePercent 	绿色通道参数 	范围[-200, 200]
	 * @param gConstant    绿色通道常数		范围[-200, 200]
	 * @param bRedPercent, bGreenPercent, bBluePercent	蓝色通道参数 	范围[-200, 200]
	 * @param bConstant    蓝色通道常数		范围[-200, 200]
	 * @param rRedPercent, gGreenPercent, bBluePercent默认为100， 其余为0
	 */
	public static Bitmap AdjustMixChannel(Bitmap dest
			, int rRedPercent, int rGreenPercent, int rBluePercent, int rConstant
			, int gRedPercent, int gGreenPercent, int gBluePercent, int gConstant
			, int bRedPercent, int bGreenPercent, int bBluePercent, int bConstant) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if( rRedPercent == 100 && rGreenPercent == 0 && rBluePercent == 0 && rConstant == 0 &&
		    gRedPercent == 0 && gGreenPercent == 100 && gBluePercent == 0 && gConstant == 0 &&
		    bRedPercent == 0 && bGreenPercent == 0 && bBluePercent == 100 && bConstant == 0)
			return  dest;

		PocoNativeFilter.MixChannelAdjust(dest, PocoImageInfo.ChannelType.AllChannels
				, rRedPercent, rGreenPercent, rBluePercent, rConstant
				, gRedPercent, gGreenPercent, gBluePercent, gConstant
				, bRedPercent, bGreenPercent, bBluePercent, bConstant);

		return dest;
	}

	/**
	 * 反相
	 *
	 * @param isDone 是否处理   true处理  false不处理
	 */
	public static Bitmap AdjustNegative(Bitmap dest, boolean isDone) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (isDone == false)
			return dest;

		PocoNativeFilter.NegativeAdjust(dest);

		return dest;
	}


	/**
	 * 高光阴影
	 *
	 * @param shadows    阴影  范围[0,200] 默认0
	 * @param highlights 高光  范围[0,200] 默认0
	 * @param smoothing  平滑  范围[0,100] 默认50
	 */
	public static Bitmap AdjustHighlightShadow(Bitmap dest, int shadows, int highlights, int smoothing) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (shadows == 0 && highlights == 0)
			return dest;

		PocoNativeFilter.HighlightShadowAdjustPs(dest, shadows, highlights, smoothing);

		return dest;
	}


	/**
	 * 可选颜色调整(每个颜色通道有青色，洋红，黄色，黑色处理参数， 默认值全为0)
	 *
	 * @param rc, rm, ry, rk  红色通道各参数	     范围均为[-100,100]
	 * @param yc, ym, yy, yk  黄色通道各参数
	 * @param gc, gm, gy, gk  绿色通道各参数
	 * @param cc, cm, cy, ck  青色通道各参数
	 * @param bc, bm, by, bk  蓝色通道各参数
	 * @param mc, mm, my, mk  洋红通道各参数
	 * @param wc, wm, wy, wk  白色通道各参数
	 * @param nc, nm, ny, nk  中性色通道各参数
	 * @param kc, km, ky, kk  黑色通道各参数
	 */
	public static Bitmap AdjustOptionColor(Bitmap dest, int rc, int rm, int ry, int rk,
										   int yc, int ym, int yy, int yk,
										   int gc, int gm, int gy, int gk,
										   int cc, int cm, int cy, int ck,
										   int bc, int bm, int by, int bk,
										   int mc, int mm, int my, int mk,
										   int wc, int wm, int wy, int wk,
										   int nc, int nm, int ny, int nk,
										   int kc, int km, int ky, int kk) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		PocoNativeFilter.OptionColorAdjust(dest, rc, rm, ry, rk,
				yc, ym, yy, yk,
				gc, gm, gy, gk,
				cc, cm, cy, ck,
				bc, bm, by, bk,
				mc, mm, my, mk,
				wc, wm, wy, wk,
				nc, nm, ny, nk,
				kc, km, ky, kk, 0);

		return dest;
	}

	/**
	 * 锐化调整
	 *
	 * @param dest  源Bitmap
	 * @param value 大小 范围在[0, 100]。
	 */
	public static Bitmap AdjustSharpen(Bitmap dest, int value) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (0 == value)
			return dest;

		value = (int) (20 + value * 0.8 + 0.5);

		PocoNativeFilter.SharpenAdjust(dest, value);

		return dest;

	}

	/**
	 * 添加杂色
	 *
	 * @param value 数量 范围[0,400]
	 */
	public static Bitmap AdjustNoise(Bitmap dest, int value) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (value == 0)
			return dest;

		value = (int) (value * 0.2 + 0.5);

		PocoNativeFilter.NoiseAdjustPs(dest, value);

		return dest;
	}


	/**
	 * 高斯模糊
	 *
	 * @param radius 范围[0-50] 默认1.0
	 */
	public static Bitmap AdjustGaussianBlur(Bitmap dest, double radius) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (radius == 0)
			return dest;

		radius = 0.4 * radius;

		int kernel_width = (int) (6 * radius + 1);

		PocoNativeFilter.GaussianBlurAdjust(dest, radius, radius, kernel_width, kernel_width);

		return dest;
	}

	/**
	 * 表面模糊
	 *
	 * @param radius    范围[0-50] 默认5
	 * @param threshold 阈值[0-255]	默认15
	 */
	public static Bitmap AdjustSurfaceBlur(Bitmap dest, int radius, int threshold) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (radius == 0 || threshold == 0)
			return dest;

		threshold = (int) (threshold * 0.4 + 0.5);

		PocoNativeFilter.SurfaceBlurAdjust(dest, radius, threshold);

		return dest;
	}


	/**
	 * 纯色填充
	 *
	 * @param r,        g, b 红绿蓝  范围[0,255]
	 * @param comOp     图层混合模式  可用同上
	 * @param fillParam 填充 范围[0, 100] 默认值0
	 */
	public static Bitmap AdjustSolidFill(Bitmap dest, int r, int g, int b, int comOp, int fillParam) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (fillParam == 0)
			return dest;

		int opacity = fillParam * 255 / 100;

		PocoNativeFilter.SolidFillAdjust(dest, r, g, b, PocoImageInfo.ChannelType.AllChannels, comOp, opacity);

		return dest;
	}


	/**
	 * 暗角
	 *
	 * @param radius  暗角范围  [0, 100] 默认 0
	 * @param opacity 不透明度  范围[0, 100] 默认100
	 */
	public static Bitmap AdjustDarkenCorner(Bitmap dest, int radius, int opacity) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (radius <= 0 || radius > 100 || opacity == 0)
			return dest;

		PocoNativeFilter.DarkenCornerAdjsutPs(dest, radius, opacity, 12);

		return dest;
	}


	/**
	 * 图层混合
	 *
	 * @param mask    滤镜模板
	 * @param comOp   图层混合模式
	 *                可用混合模式:	 NoCompositeOp				正常		默认
	 *                ColorBurnCompositeOp		颜色加深
	 *                ColorDodgeCompositeOp		颜色减淡
	 *                DarkenCompositeOp			变暗
	 *                HardLightCompositeOp		强光
	 *                OverlayCompositeOp			叠加
	 *                SoftLightCompositeOp		柔光
	 *                MultiplyCompositeOp		正片叠底
	 *                ScreenCompositeOp			滤色
	 *                DifferenceCompositeOp		差值
	 *                LightenCompositeOp			变亮
	 *                LinearDodgeCompositeOp		线性减淡
	 *                VividLightCompositeOp		亮光
	 *                LinearLightCompositeOp		线性光
	 *                PinLightCompositeOp		点光
	 *                ExclusionCompositeOp		排除
	 *                LinearBurnCompositeOp		线性加深
	 *                DivideCompositeOp			划分
	 *                HardMixCompositeOp			实色混合
	 * @param opacity 不透明度 范围[0, 100]	默认100
	 */
	public static Bitmap AdjustCompositeImage(Bitmap dest, Bitmap mask, int comOp, int opacity) {
		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
			return null;

		if (opacity == 0 || mask == null || Bitmap.Config.ARGB_8888 != mask.getConfig())
			return dest;

		int width = dest.getWidth();
		int height = dest.getHeight();

		if(mask.getWidth() != width || mask.getHeight() != height)
			return dest;

		int opa = opacity * 255 / 100;

		PocoNativeFilter.CompositeImageAdjust(dest, mask, 0, 0, width, height, 0, 0, width, height,
				PocoImageInfo.ChannelType.AllChannels, comOp, opa);

		return dest;
	}

	/*
	 * 素材混合 限定最多五张素材
	 * 素材，混合模式，不透明度按顺序填写
	 * */
	public static Bitmap CompositeMask(Bitmap destBmp, Bitmap []mask, int []comOp, int []opa)
	{
		if(null == destBmp || Bitmap.Config.ARGB_8888 != destBmp.getConfig())
			return null;

		int len = mask.length;

		if(len > 5 || len != comOp.length || len != opa.length)
			return destBmp;

		for(int i=0; i<len; i++)
		{
			if(destBmp.getWidth() != mask[i].getWidth() || destBmp.getHeight() != mask[i].getHeight())
			{
				return destBmp;
			}

			opa[i] = 255 * opa[i] / 100;
		}

		PocoNativeFilter.CompositeMask(destBmp, mask, comOp, opa);

		return destBmp;
	}

//	/**
//	 * @param dest 原图
//	 * @param ProcessSequence 操作顺序
//	 * @param ProcessChannels 操作通道
//	 * @param ProcessParam    操作对应的参数
//	 * */
//	public static Bitmap PocoAdjustDownload(Bitmap dest, int []ProcessSequence, int []ProcessChannels, int []ProcessParam) {
//		if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
//			return dest;
//
//		if(ProcessSequence  == null || ProcessSequence.length < 0 || ProcessParam == null || ProcessParam.length < 0)
//			return dest;
//
////        PocoNativeFilter.PocoYourSelfFilter(dest, ProcessSequence, ProcessChannels, ProcessParam);
//
//		return dest;
//	}

}