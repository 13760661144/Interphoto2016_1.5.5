package cn.poco.image;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class PocoNativeFilter
{
	static
	{
		System.loadLibrary("PocoImage");
	}

	//
	//==========================================================================================
	//
	//Nomal

	public static native int compositeImageChannel(Bitmap dest, Bitmap src, int channel, int comOp, int opacity);
	public static native int compositeImageRectChannel(Bitmap dest, Bitmap src, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh, int channel, int comOp, int opacity);
	public static  int compositeImageRectChannel(Bitmap dest, Bitmap src, Rect dr, Rect sr, int channel, int comOp, int opacity)
	{
		return compositeImageRectChannel(dest,src, dr.left,dr.top,dr.width(),dr.height(), sr.left,sr.top,sr.width(),sr.height(), channel,comOp,opacity);
	}

	//====================================调色工具接口==============================================
	public static native int composite(Bitmap dest, Bitmap src, int flage);
	public static native int LevelImageChannel(Bitmap dest, int channel, double black_point, double white_point, double gamma);
	public static native int gaussianBlurImageChannel(Bitmap dest, int channel, double sigma);
	public static native int sharpenImageFast(Bitmap dest, Bitmap src, int percent);

	public static native int changeSaturation(Bitmap src, int value);
	public static native int changeSaturationAndContrast(Bitmap dest, float s, float c);
	public static native int whiteBalance(Bitmap destBmp, float value);

	public static native int newContrastAndBright(Bitmap destBmp, int contrast, int bright);
	public static native int oneKeyRetinex(Bitmap dest);

	public static native int fakeGlass(Bitmap destBmp, int radius, int colors);
	public static native int oneKeyRetinexAdjust(Bitmap destBmp, int value);

	public static native int HighlightShadowAdjust(Bitmap dest, int shadows, int highlights, int smoothing);
	public static native int DarkenCornerAdjsut(Bitmap dest, int radius, int opa, int strengh);
	public static native int NoiseAdjust(Bitmap dest, int param);
	public static native int ToneAdjust(Bitmap dest, int value);
	public static native int FadeAdjust(Bitmap dest, int value);

	public static native int CreateCurves(int []result, int rcount, int[] controlPoints, int count);
	public static native int CurveAdjsut(Bitmap dest, int channel, int[] controlPointXs, int count);
	public static native int CurveAdjsutAll(Bitmap dest, Bitmap src, int []controlPointXs, int []count);

	//=============================大师滤镜工具功能接口===========================================
	public static native int ContrastAdjust(Bitmap dest, int value);
	public static native int BrightnessAdjust(Bitmap dest, int value);
	public static native int HueAndSaturationAdjust(Bitmap dest, int ProcessChannel, int Hvalue, int Svalue, int Bvalue);

	public static native int ColorLeverAdjust(Bitmap dest, int channel, int input_low, double gamma, int input_high, int output_low, int output_high);
	public static native int BlackWhiteAdjust(Bitmap dest, int red, int yellow, int green, int cyan, int blue, int magenta);
	public static native int SharpenAdjust(Bitmap dest, int value);
	public static native int OptionColorAdjust(Bitmap dest, int rc, int rm, int ry, int rk,
											   int yc, int ym, int yy, int yk,
											   int gc, int gm, int gy, int gk,
											   int cc, int cm, int cy, int ck,
											   int bc, int bm, int by, int bk,
											   int mc, int mm, int my, int mk,
											   int wc, int wm, int wy, int wk,
											   int nc, int nm, int ny, int nk,
											   int kc, int km, int ky, int kk, int abs);
	public static native int ColorBalanceAdjust(Bitmap dest, double cyan_red_l, double cyan_red_m, double cyan_red_h,
												double magenta_green_l, double magenta_green_m, double magenta_green_h,
												double yellow_blue_l, double yellow_blue_m, double yellow_blue_h, int preserve_luminosity);
	public static native int NegativeAdjust(Bitmap dest);
	public static native int MixChannelAdjust(Bitmap dest, int channel, int rRedPercent, int rGreenPercent, int rBluePercent, int rConstant
			, int gRedPercent, int gGreenPercent, int gBluePercent, int gConstant
			, int bRedPercent, int bGreenPercent, int bBluePercent, int bConstant);
	public static native int NoiseAdjustPs(Bitmap dest, int value);
	public static native int SurfaceBlurAdjust(Bitmap dest, int radius, int threshold);
	public static native int GaussianBlurAdjust(Bitmap dest, double sigmaw, double sigmah, int kernel_width, int kernel_height);
	public static native int SolidFillAdjust(Bitmap dest, int r, int g, int b, int channels, int comOp, int opacity);
	public static native int CompositeImageAdjust(Bitmap dest, Bitmap src, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh, int channel, int comOp, int opacity);
	public static native int CurveAdjsutPs(Bitmap dest, int channel, int[] controlPointXs, int count);
	public static native int ExposureAdjust(Bitmap dest, int value);
	public static native int HighlightShadowAdjustPs(Bitmap dest, int shadows, int highlights, int smoothing);
	public static native int DarkenCornerAdjsutPs(Bitmap dest, int radius, int opa, int strengh);
	public static native int CompositeMask(Bitmap dest, Bitmap[]mask, int []comOp, int []opa);
	public static native int CreateCurvesPs(int []result, int rcount, int[] controlPoints, int count);


	//======================================滤镜接口========================================
	public static native int TestFilter01(Bitmap dest);
	public static native int TestFilter02(Bitmap dest);
	public static native int TestFilter03(Bitmap dest);
	public static native int TestFilter04(Bitmap dest);
	public static native int TestFilter05(Bitmap dest);
	public static native int TestFilter06(Bitmap dest);
	public static native int TestFilter07(Bitmap dest);
	public static native int TestFilter08(Bitmap dest);
	public static native int TestFilter09(Bitmap dest);
	public static native int TestFilter10(Bitmap dest);
	public static native int TestFilter13(Bitmap dest);
	public static native int TestFilter14(Bitmap dest, Bitmap mask, Bitmap mask1);
	public static native int TestFilter15(Bitmap dest);
	public static native int TestFilter16(Bitmap dest);
	public static native int TestFilter17(Bitmap dest);
	public static native int TestFilter18(Bitmap dest);
	public static native int TestFilter19(Bitmap dest);
	public static native int TestFilter20(Bitmap dest);
	public static native int TestFilter21(Bitmap dest);
	public static native int TestFilter22(Bitmap dest, Bitmap mask);
	public static native int TestFilter23(Bitmap dest);
	public static native int TestFilter24(Bitmap dest, Bitmap mask);
	public static native int TestFilter32(Bitmap dest);

	public static native int TestFilterAY1(Bitmap dest);
	public static native int TestFilterAY2(Bitmap dest);
	public static native int TestFilterAY3(Bitmap dest);
	public static native int TestFilterAY4(Bitmap dest);
	public static native int TestFilterAY5(Bitmap dest);
	public static native int TestFilterAY6(Bitmap dest);
	public static native int TestFilterAY7(Bitmap dest);
	public static native int TestFilterAY8(Bitmap dest);
	public static native int TestFilterAY9(Bitmap dest);
	public static native int TestFilterAY10(Bitmap dest, Bitmap mask);

	public static native int TestFilterPh01(Bitmap dest);
	public static native int TestFilterPh02(Bitmap dest);
	public static native int TestFilterPh03(Bitmap dest);
	public static native int TestFilterPh04(Bitmap dest);
	public static native int TestFilterPh05(Bitmap dest);
	public static native int TestFilterPh06(Bitmap dest);

	public static native int polaroidGreen(Bitmap destBmp);
	public static native int polaroidYellow(Bitmap destBmp);
	public static native int cate(Bitmap dest);
	public static native int lightengreen2(Bitmap dest, Bitmap mask);

	public static native int FilterZhangY01(Bitmap dest);
	public static native int FilterZhangY02(Bitmap dest);
	public static native int FilterZ01(Bitmap dest);
	public static native int FilterZ02(Bitmap dest);
	public static native int FilterYuS(Bitmap dest);
	public static native int FilterYuS1(Bitmap dest);
	public static native int FilterYuS2(Bitmap dest);
	public static native int FilterYuS3(Bitmap dest);

	public static native int FilterLHY1(Bitmap dest);
	public static native int FilterM30(Bitmap dest);
	public static native int FilterLHY2(Bitmap dest);
	public static native int FilterOLYCC(Bitmap dest);
	public static native int FilterM31(Bitmap dest);

	public static native int FilterLXL1(Bitmap dest);
	public static native int FilterTH1(Bitmap dest);
	public static native int FilterTH2(Bitmap dest);
	public static native int FilterLXL2(Bitmap dest);

	//光效底层接口
	public static native int lightEffect(Bitmap dest, int light, int saturation, int hue);

	public static native int skinColorAdjust(Bitmap dest, int val);
}
