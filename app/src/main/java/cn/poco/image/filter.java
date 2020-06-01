package cn.poco.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;

import cn.poco.interphoto2.R;


public class filter {
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

    public static Bitmap decodeBmpARGB(Context txt, int id, int width, int height) {
        if (width <= 0)
            width = 1;
        if (height <= 0)
            height = 1;
        Bitmap temp = BitmapFactory.decodeResource(txt.getResources(), id);
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        resizeBitmap(result, temp);
        temp.recycle();
        temp = null;
        return result;
    }
    //小资绿
    public static Bitmap polaroid_g(Bitmap destBmp) {
        if (destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        PocoNativeFilter.polaroidGreen(destBmp);

        return destBmp;

    }

    //小资黄
    public static Bitmap polaroid_y(Bitmap destBmp) {
        if (destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        PocoNativeFilter.polaroidYellow(destBmp);

        return destBmp;

    }


    //哑光绿
    public static Bitmap lightengreen2(Bitmap destBmp) {
        if ((null == destBmp) || (Bitmap.Config.ARGB_8888 != destBmp.getConfig()))
            return null;

        int width = destBmp.getWidth();
        int height = destBmp.getHeight();
        Bitmap mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mask);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        int[] linearGradientColors = new int[]{0x4093b5bf, 0x40fff3ef, 0x45fcf6f4, 0x456cc4e5};
        float[] linearGradientPositions = new float[]{0.0f, 0.11f, 0.57f, 1.0f};
        LinearGradient linearGradientShader = new LinearGradient(0, height / 2, width, height / 2,
                linearGradientColors,
                linearGradientPositions,
                TileMode.CLAMP);
        Paint shaderPaint = new Paint();
        shaderPaint.setShader(linearGradientShader);
        shaderPaint.setAntiAlias(true);
        Rect r = new Rect(0, 0, width, height);
        canvas.drawRect(r, shaderPaint);

        PocoNativeFilter.lightengreen2(destBmp, mask);

        mask.recycle();
        mask = null;

        return destBmp;
    }

    //美食特效
    public static Bitmap cate(Bitmap destBmp) {
        if (destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        PocoNativeFilter.cate(destBmp);
        return destBmp;
    }

    //一键增强
    public static Bitmap oneKeyRetinex(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.oneKeyRetinex(dest);

        return dest;

    }

//===================================================================
//其他

    /**
     * 调节亮度
     *
     * @src 源Bitmap
     * @value 亮度值，范围在[-100, 100]。
     * @返回一个处理后的新Bitmap
     */
    public static Bitmap changeBrightness(Bitmap dest, float value) {
        if (dest.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        value = value < -100 ? -100 : (value > 100 ? 100 : value);

        if (0.0f == value)
            return dest;


//		PocoNativeFilter.changeBrightness(dest, (int)(value * 127 / 100));
        PocoNativeFilter.newContrastAndBright(dest, 0, (int) (value * 0.5f));

        return dest;

    }

    /**
     * @param src
     * @param value 范围【-100，100】等价于以前的【-100,65】
     * @return
     */
    public static Bitmap changeContrast_p(Bitmap src, int value) {
        value = value < -100 ? -100 : (value > 100 ? 100 : value);
//		if(value > 0)
//			value = (int)(value * 0.65f);
//		PocoNativeFilter.changeContrast(src, value);

        if (value == 0)
            return src;

        if (value < 0)
            value /= 2;        //新版范围改为【-50， 100】

        PocoNativeFilter.newContrastAndBright(src, value, 0);

        return src;
    }

    /**
     * 调节锐度
     *
     * @src 源Bitmap
     * @value 亮度值，范围在[0, 100]。
     * @ 返回一个处理后的新Bitmap
     */
    public static Bitmap sharpen(Bitmap src, int percent) {
        if (src == null) {
            return null;
        }
        percent = percent < 0 ? 0 : (percent > 100 ? 100 : percent);
        percent = (int) (80.0 * percent / 100);

        Bitmap dest = src.copy(Bitmap.Config.ARGB_8888, true);

        if (0 == percent) {
            return dest;
        }
        PocoNativeFilter.sharpenImageFast(dest, src, percent);
        return dest;
    }

    /**
     * POCO相机饱和度调节 （poco相机1.6.0新增）
     *
     * @param destBmp
     * @param value   调节范围-100~100
     * @return
     */
    public static Bitmap changeSaturation(Bitmap destBmp, int value) {
        if (destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        if (0 == value)
            return destBmp;

        PocoNativeFilter.changeSaturation(destBmp, value);

        return destBmp;
    }

    /**
     * Poco相机白平衡调节接口
     *
     * @dest
     * @value -1.00~1.00
     * @
     */
    public static Bitmap whiteBalance_p(Bitmap dest, float value) {
        if (dest.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        if (0 == value)
            return dest;
        else {
            if (value < 0)
                value = 0.5f * value;
            else
                value = 0.8f * value;
            PocoNativeFilter.whiteBalance(dest, value);
            return dest;
        }
    }

    //装饰接口--增加了透明度参数(opacity : 0 ~ 100)
    public static Bitmap ornamentComposition(Bitmap dest, Bitmap mask, int comOp, int cutOp, int opacity) {
        if ((null == dest) || (dest.getConfig() != Bitmap.Config.ARGB_8888))
            return dest;
        if ((null == mask) || (Bitmap.Config.ARGB_8888 != mask.getConfig()))
            return dest;

        int width = dest.getWidth();
        int height = dest.getHeight();
        int sizeMax = (width > height ? width : height);
        int sizeMin = (width < height ? width : height);
        opacity = (opacity * 255) / 100;
        Rect rect1 = new Rect(0, 0, width, height);
        Rect rect2 = new Rect();
        Matrix matrix = new Matrix();
        Paint paint = new Paint();
        Canvas canvas = new Canvas();
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

//根据原图，素材相应裁掉上下左右。
        if ((cutOp == PocoCutOperator.left) || (cutOp == PocoCutOperator.right)
                || (cutOp == PocoCutOperator.top) || (cutOp == PocoCutOperator.bottom)
                || (cutOp == PocoCutOperator.leftRight) || (cutOp == PocoCutOperator.topBottom)) {
            Bitmap tempMask = Bitmap.createBitmap(sizeMax, sizeMax, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(tempMask);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            matrix.postScale((float) sizeMax / mask.getWidth(), (float) sizeMax / mask.getHeight());
            paint.setAntiAlias(true);
            canvas.drawBitmap(mask, matrix, paint);
            switch (cutOp) {
                case PocoCutOperator.left:
                    rect2 = new Rect(sizeMax - width, 0, sizeMax, sizeMax);
                    PocoNativeFilter.compositeImageRectChannel(dest, tempMask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
                case PocoCutOperator.right:
                    rect2 = new Rect(0, 0, width, height);
                    PocoNativeFilter.compositeImageRectChannel(dest, tempMask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
                case PocoCutOperator.top:
                    rect2 = new Rect(0, sizeMax - height, width, sizeMax);
                    PocoNativeFilter.compositeImageRectChannel(dest, tempMask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
                case PocoCutOperator.bottom:
                    rect2 = new Rect(0, 0, width, height);
                    PocoNativeFilter.compositeImageRectChannel(dest, tempMask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
                case PocoCutOperator.leftRight:
                    rect2 = new Rect((sizeMax - width) / 2, 0, (sizeMax - width) / 2 + width, height);
                    PocoNativeFilter.compositeImageRectChannel(dest, tempMask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
                case PocoCutOperator.topBottom:
                    rect2 = new Rect(0, (sizeMax - height) / 2, width, (sizeMax - height) / 2 + height);
                    PocoNativeFilter.compositeImageRectChannel(dest, tempMask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
            }
            tempMask.recycle();
            tempMask = null;
        } else if ((cutOp == PocoCutOperator.snapLeft) || (cutOp == PocoCutOperator.snapTop)
                || (cutOp == PocoCutOperator.snapRight) || (cutOp == PocoCutOperator.snapBottom)) {
//缩小素材与原图对齐。
            Bitmap snapMask = Bitmap.createBitmap(sizeMin, sizeMin, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(snapMask);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            //		matrix.reset();
            matrix.postScale((float) sizeMin / mask.getWidth(), (float) sizeMin / mask.getHeight());
            canvas.drawBitmap(mask, matrix, paint);
            switch (cutOp) {
                case PocoCutOperator.snapLeft:
                case PocoCutOperator.snapTop:
                    rect1 = new Rect(0, 0, sizeMin, sizeMin);
                    rect2 = new Rect(0, 0, sizeMin, sizeMin);
                    PocoNativeFilter.compositeImageRectChannel(dest, snapMask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
                case PocoCutOperator.snapRight:
                    rect1 = new Rect(width - sizeMin, 0, width, sizeMin);
                    rect2 = new Rect(0, 0, sizeMin, sizeMin);
                    PocoNativeFilter.compositeImageRectChannel(dest, snapMask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
                case PocoCutOperator.snapBottom:
                    rect1 = new Rect(0, height - sizeMin, sizeMin, height);
                    rect2 = new Rect(0, 0, sizeMin, sizeMin);
                    PocoNativeFilter.compositeImageRectChannel(dest, snapMask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
            }
            snapMask.recycle();
            snapMask = null;
        } else {
            switch (cutOp) {
                case PocoCutOperator.middle:
                    int mW = mask.getWidth();
                    int mH = mask.getHeight();
                    rect1 = new Rect((width - mW) / 2, (height - mH) / 2, (width - mW) / 2 + mW, (height - mH) / 2 + mH);
                    rect2 = new Rect(0, 0, mW, mH);
                    PocoNativeFilter.compositeImageRectChannel(dest, mask, rect1, rect2,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    break;
                case PocoCutOperator.stretching:
                    Bitmap tempMask2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    //				matrix.reset();
                    matrix.postScale((float) width / mask.getWidth(), (float) height / mask.getHeight());
                    canvas = new Canvas(tempMask2);
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
                    canvas.drawBitmap(mask, matrix, paint);
                    PocoNativeFilter.compositeImageRectChannel(dest, tempMask2, rect1, rect1,
                            PocoImageInfo.ChannelType.AllChannels, comOp, opacity);
                    tempMask2.recycle();
                    tempMask2 = null;
                    break;
            }
        }


        return dest;
    }

    public static Bitmap composite(Bitmap dest, Bitmap mask, int comOp, int opa) {
        if (dest.getConfig() != Bitmap.Config.ARGB_8888)
            return null;
        if (mask.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        Rect rect = new Rect(0, 0, dest.getWidth(), dest.getHeight());
        PocoNativeFilter.compositeImageRectChannel(dest, mask, rect, rect,
                PocoImageInfo.ChannelType.AllChannels, comOp, opa);
        return dest;
    }


    /**
     * (模拟毛玻璃)
     *
     * @param dest   透明    颜色
     *               |   |
     * @param colors - 0x ** ******
     * @return
     */
    public static Bitmap fakeGlass(Bitmap dest, int colors) {
        if (null == dest)
            return dest;

        if (dest.getWidth() > 640 || dest.getHeight() > 920) {

            float scale = 640f / dest.getWidth();

            Bitmap small = Bitmap.createBitmap(640, (int) (dest.getHeight() * scale), Bitmap.Config.ARGB_8888);
            resizeBitmap(small, dest);

            PocoNativeFilter.fakeGlass(small, 100, colors);

            resizeBitmap(dest, small);

            small.recycle();
            small = null;
        } else {
            int radius = (int) (dest.getWidth() / 5f);
            if (radius < 3)
                return dest;
            PocoNativeFilter.fakeGlass(dest, radius, colors);
        }

        return dest;
    }

    /*
     * 一键增强(可调)
	 * value 范围[0-100]
	 * */
    public static Bitmap oneKeyRetinexAdjust(Bitmap dest, int value) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        if (value == 0)
            return dest;

        PocoNativeFilter.oneKeyRetinexAdjust(dest, value);

        return dest;

    }

    //高光调整 [-10, 10] 默认0
    public static Bitmap AdjustHighLight(Bitmap destBmp, float value) {
        if (null == destBmp || destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        int val;
        if (value < 0)
            val = (int) (-14 * value + 0.5f);
        else
            val = (int) (-14 * value - 0.5f);

        if (val == 0)
            return destBmp;

        PocoNativeFilter.HighlightShadowAdjust(destBmp, 0, val, 50);

        return destBmp;
    }

    //阴影调整 [-10, 10] 默认0
    public static Bitmap AdjustShadow(Bitmap destBmp, float value) {
        if (null == destBmp || destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        int val;
        if (value < 0)
            val = (int) (10 * value - 0.5f);
        else
            val = (int) (10 * value + 0.5f);

        if (val == 0)
            return destBmp;

        PocoNativeFilter.HighlightShadowAdjust(destBmp, val, 0, 50);

        return destBmp;
    }

    //高光 阴影调整 范围均为[-10, 10] 默认0
    public static Bitmap AdjustShadowHighLight(Bitmap destBmp, float highlight, float shadow) {
        if (null == destBmp || destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        int hval;
        if (highlight < 0)
            hval = (int) (-14 * highlight + 0.5f);
        else
            hval = (int) (-14 * highlight - 0.5f);

        int sval;
        if (shadow < 0)
            sval = (int) (10 * shadow - 0.5f);
        else
            sval = (int) (10 * shadow + 0.5f);

        if (hval == 0 && sval == 0)
            return destBmp;

        PocoNativeFilter.HighlightShadowAdjust(destBmp, sval, hval, 50);

        return destBmp;
    }

    //色调调整 [-10, 10] 默认0
    public static Bitmap AdjustTone(Bitmap destBmp, float value) {
        if (null == destBmp || destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        int val;
        if (value < 0)
            val = (int) (-1.6 * value + 0.5f);
        else
            val = (int) (-2 * value - 0.5f);

        if (val == 0)
            return destBmp;

        PocoNativeFilter.ToneAdjust(destBmp, val);

        return destBmp;
    }

    //暗角调整[0, 10] 默认0
    public static Bitmap AdjustDarkenCorner(Bitmap destBmp, float value) {
        if (null == destBmp || destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        int val = (int) (10 * value + 0.5f);

        if (val == 0)
            return destBmp;

        PocoNativeFilter.DarkenCornerAdjsut(destBmp, 100, val, 12);

        return destBmp;
    }

    //颗粒调整 [0, 10] 默认0
    public static Bitmap AdjustGranule(Bitmap destBmp, float value) {
        if (null == destBmp || destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        int val = (int) (1.5 * value + 0.5f);

        if (val == 0)
            return destBmp;

        PocoNativeFilter.NoiseAdjust(destBmp, val);

        return destBmp;
    }

    //褪色调整 [0, 10] 默认0
    public static Bitmap AdjustFade(Bitmap destBmp, float value) {
        if (null == destBmp || destBmp.getConfig() != Bitmap.Config.ARGB_8888)
            return null;

        int val = (int) (5 * value + 0.5f);

        if (val == 0)
            return destBmp;

        PocoNativeFilter.FadeAdjust(destBmp, val);

        return destBmp;
    }


    //测试滤镜1
    public static Bitmap testFilter1(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter01(dest);

        return dest;
    }

    //测试滤镜2
    public static Bitmap testFilter2(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter02(dest);

        return dest;
    }

    //测试滤镜3
    public static Bitmap testFilter3(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter03(dest);

        return dest;
    }

    //测试滤镜4
    public static Bitmap testFilter4(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter04(dest);

        return dest;
    }

    //测试滤镜5
    public static Bitmap testFilter5(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter05(dest);

        return dest;
    }

    //测试滤镜6
    public static Bitmap testFilter6(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter06(dest);

        return dest;
    }

    //测试滤镜7
    public static Bitmap testFilter7(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter07(dest);

        return dest;
    }

    //测试滤镜8
    public static Bitmap testFilter8(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter08(dest);

        return dest;
    }

    //测试滤镜9
    public static Bitmap testFilter9(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter09(dest);

        return dest;
    }

    //测试滤镜10
    public static Bitmap testFilter10(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter10(dest);

        return dest;
    }

    //测试滤镜11
    public static Bitmap testFilter11(Bitmap dest, Context txt)
    {
        if(null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        int width = dest.getWidth();
        int height = dest.getHeight();

        Bitmap mask = decodeBmpARGB(txt, R.drawable.testfilter11, width, height);
        composite(dest, mask, PocoCompositeOperator.LightenCompositeOp, 255);

        if(null != mask)
        {
            mask.recycle();
            mask = null;
        }

        return dest;
    }

    //测试滤镜12
    public static Bitmap testFilter12(Bitmap dest, Context txt)
    {
        if(null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        int width = dest.getWidth();
        int height = dest.getHeight();

        Bitmap mask = decodeBmpARGB(txt, R.drawable.testfilter12, width, height);
        composite(dest, mask, PocoCompositeOperator.OverlayCompositeOp, 255);

        if(null != mask)
        {
            mask.recycle();
            mask = null;
        }

        return dest;
    }

    //测试滤镜13
    public static Bitmap testFilter13(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter13(dest);

        return dest;
    }

    //测试滤镜14
    public static Bitmap testFilter14(Bitmap dest, Context txt) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        int width = dest.getWidth();
        int height = dest.getHeight();

//		Bitmap mask = decodeBmpARGB(txt, R.drawable.testfilter14_1, width, height);
//		composite(dest, mask, PocoCompositeOperator.ScreenCompositeOp, 255);
//
//		if(null != mask)
//		{
//			mask.recycle();
//			mask = null;
//		}
//
//		Bitmap mask1 = decodeBmpARGB(txt, R.drawable.testfilter14_2, width, height);
//		composite(dest, mask1, PocoCompositeOperator.LightenCompositeOp, 255);
//
//		if(null != mask1)
//		{
//			mask1.recycle();
//			mask1 = null;
//		}
        Bitmap mask = decodeBmpARGB(txt, R.drawable.testfilter14_1, width, height);
        Bitmap mask1 = decodeBmpARGB(txt, R.drawable.testfilter14_2, width, height);

        PocoNativeFilter.TestFilter14(dest, mask, mask1);

        if (null != mask) {
            mask.recycle();
            mask = null;
        }
        if (null != mask1) {
            mask1.recycle();
            mask1 = null;
        }

        return dest;
    }

    //测试滤镜15
    public static Bitmap testFilter15(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter15(dest);

        return dest;
    }

    //测试滤镜16
    public static Bitmap testFilter16(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter16(dest);

        return dest;
    }

    //测试滤镜17
    public static Bitmap testFilter17(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter17(dest);

        return dest;
    }

    //测试滤镜18
    public static Bitmap testFilter18(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter18(dest);

        return dest;
    }

    //测试滤镜19
    public static Bitmap testFilter19(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter19(dest);

        return dest;
    }

    //测试滤镜20
    public static Bitmap testFilter20(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter20(dest);

        return dest;
    }

    //测试滤镜21
    public static Bitmap testFilter21(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter21(dest);

        return dest;
    }

    //测试滤镜22
    public static Bitmap testFilter22(Bitmap dest, Context txt)
    {
        if(null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        int width = dest.getWidth();
        int height = dest.getHeight();

        Bitmap mask = decodeBmpARGB(txt, R.drawable.testfilter22, width, height);

        PocoNativeFilter.TestFilter22(dest, mask);

        if(null != mask)
        {
            mask.recycle();
            mask = null;
        }

        return dest;
    }

    //测试滤镜23
    public static Bitmap testFilter23(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter23(dest);

        return dest;
    }

    //测试滤镜24
    public static Bitmap testFilter24(Bitmap dest, Context txt)
    {
        if(null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        int width = dest.getWidth();
        int height = dest.getHeight();

        Bitmap mask = decodeBmpARGB(txt, R.drawable.testfilter24, width, height);

        PocoNativeFilter.TestFilter24(dest, mask);

        if(null != mask)
        {
            mask.recycle();
            mask = null;
        }

        return dest;
    }

    //测试滤镜32
    public static Bitmap testFilter32(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilter32(dest);

        return dest;
    }

    //测试滤镜AY1
    public static Bitmap testFilterAY1(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterAY1(dest);

        return dest;
    }

    //测试滤镜AY2
    public static Bitmap testFilterAY2(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterAY2(dest);

        return dest;
    }

    //测试滤镜AY3
    public static Bitmap testFilterAY3(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterAY3(dest);

        return dest;
    }

    //测试滤镜AY4
    public static Bitmap testFilterAY4(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterAY4(dest);

        return dest;
    }

    //测试滤镜AY5
    public static Bitmap testFilterAY5(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterAY5(dest);

        return dest;
    }

    //测试滤镜AY6
    public static Bitmap testFilterAY6(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterAY6(dest);

        return dest;
    }

    //测试滤镜AY7
    public static Bitmap testFilterAY7(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterAY7(dest);

        return dest;
    }

    //测试滤镜AY8
    public static Bitmap testFilterAY8(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterAY8(dest);

        return dest;
    }

    //测试滤镜AY9
    public static Bitmap testFilterAY9(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterAY9(dest);

        return dest;
    }

//	//测试滤镜AY10
//	public static Bitmap testFilterAY10(Bitmap dest, Context txt)
//	{
//		if(null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
//			return null;
//
//		int width = dest.getWidth();
//		int height = dest.getHeight();
//
//		Bitmap mask = decodeBmpARGB(txt, R.drawable.xyk);
//
//		PocoNativeFilter.TestFilterAY10(dest, mask);
//
//		if(null != mask)
//		{
//			mask.recycle();
//			mask = null;
//		}
//
//		return dest;
//	}

    //测试滤镜Ph1
    public static Bitmap testFilterPh1(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterPh01(dest);

        return dest;
    }

    //测试滤镜Ph2
    public static Bitmap testFilterPh2(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterPh02(dest);

        return dest;
    }

    //测试滤镜Ph3
    public static Bitmap testFilterPh3(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterPh03(dest);

        return dest;
    }

    //测试滤镜Ph4
    public static Bitmap testFilterPh4(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterPh04(dest);

        return dest;
    }

    //测试滤镜Ph5
    public static Bitmap testFilterPh5(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterPh05(dest);

        return dest;
    }

    //王家卫色调
    public static Bitmap testFilterPh6(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.TestFilterPh06(dest);

        return dest;
    }

    //张悦滤镜1
    public static Bitmap interFilterZhangY1(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterZhangY01(dest);

        return dest;
    }

    //张悦滤镜2
    public static Bitmap interFilterZhangY2(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterZhangY02(dest);

        return dest;
    }

    //Z滤镜1
    public static Bitmap interFilterZ1(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterZ01(dest);

        return dest;
    }

    //Z滤镜2
    public static Bitmap interFilterZ2(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterZ02(dest);

        return dest;
    }

    //余硕滤镜
    public static Bitmap interFilterYuS(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterYuS(dest);

        return dest;
    }

    //余硕滤镜1
    public static Bitmap interFilterYuS1(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterYuS1(dest);

        return dest;
    }

    //余硕滤镜2
    public static Bitmap interFilterYuS2(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterYuS2(dest);

        return dest;
    }

    //余硕滤镜3
    public static Bitmap interFilterYuS3(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterYuS3(dest);

        return dest;
    }

    /**
     * 生成曲线
     *
     * @param controlPoints 控制点坐标（相对坐标）
     * @param count           控制点个数s
     * @param CurvePoints     曲线点数组 （点的个数为横坐标的长度）
     * @param screenW         screenH 坐标显示于屏幕的宽和高  宽和高等值
     */
    public static int CurvesCreate(float controlPoints[], int count, int[] CurvePoints, int screenW, int screenH) {
        if (controlPoints == null || controlPoints.length != 2 * count)
            return 0;

        if (CurvePoints.length / 2 != screenW || screenW != screenH)
            return 0;

        int ctrPoints[] = new int[2 * count];
        for (int i = 0; i < count; i++) {
            ctrPoints[2 * i] = (int) (controlPoints[2 * i] * screenW + 0.5);
            ctrPoints[2 * i + 1] = (int) (controlPoints[2 * i + 1] * screenH + 0.5);
        }

        int[] result = new int[screenW];
        PocoNativeFilter.CreateCurves(result, screenW, ctrPoints, count);

        for (int i = 0; i < screenW; i++) {
            CurvePoints[2 * i] = i;
            CurvePoints[2 * i + 1] = result[i];
        }

        return 1;
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
            return dest;

        int ctrPoints[] = new int[count * 2];
        for (int i = 0; i < 2 * count; i++) {
            ctrPoints[i] = (int) (controlPoints[i] * 255 + 0.5);
        }

        PocoNativeFilter.CurveAdjsut(dest, Colorchannel, ctrPoints, count);

        return dest;
    }

    /*
* 曲线调整
* dest      效果图
* src       原图
* controlPoints 所有通道的点的相对坐标，按(x, y)顺序
* count         每个通道的点的个数，长度为4（顺序为RGB, 红，绿，蓝） 至少8个点
* */
    public static Bitmap AdjustCurveAll(Bitmap dest, Bitmap src, float controlPoints[], int count[]) {
        if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return dest;

        if (4 != count.length || 16 > controlPoints.length)
            return dest;

        int countSum = 0;
        int flag = 0;
        for(int i=0; i<count.length; i++)
        {
            countSum += count[i];
            if(count[i] < 2)                //每个通道至少两个点
            {
                flag = 1;
                break;
            }
        }

        if(controlPoints.length != 2*countSum || 1 == flag)
            return dest;

        int ctrPoints[] = new int[controlPoints.length];
        for (int i = 0; i < controlPoints.length; i++) {
            ctrPoints[i] = (int) (controlPoints[i] * 255 + 0.5);
        }

        PocoNativeFilter.CurveAdjsutAll(dest, src, ctrPoints, count);

        return dest;
    }

    //林海音滤镜
    public static Bitmap interFilterLHY1(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterLHY1(dest);

        return dest;
    }

    //M30滤镜
    public static Bitmap interFilterM30(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterM30(dest);

        return dest;
    }

    //林海音滤镜2
    public static Bitmap interFilterLHY2(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterLHY2(dest);

        return dest;
    }

    //欧莱雅商业滤镜（CC轻唇膏）
    public static Bitmap interFilterOLY_CC(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterOLYCC(dest);

        return dest;
    }

    //M31滤镜
    public static Bitmap interFilterM31(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterM31(dest);

        return dest;
    }

    //黎晓亮滤镜1
    public static Bitmap interFilterLXL1(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterLXL1(dest);

        return dest;
    }

    //汤辉滤镜1
    public static Bitmap interFilterTH1(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterTH1(dest);

        return dest;
    }

    //汤辉滤镜2
    public static Bitmap interFilterTH2(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterTH2(dest);

        return dest;
    }

    //黎晓亮滤镜2
    public static Bitmap interFilterLXL2(Bitmap dest) {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        PocoNativeFilter.FilterLXL2(dest);

        return dest;
    }

    /* 光效效果
     * 调整参数： 默认值均为0
     * @param light: 亮度 [0-35]
     * @param saturation: 饱和度 [-100-60]
     * @param hue: 色相 [-50~50]
     */
    public static Bitmap lightEffectAdjsut(Bitmap mask, int light, int saturation, int hue)
    {
        if (mask == null || Bitmap.Config.ARGB_8888 != mask.getConfig())
            return mask;

        if(light == 0 && saturation == 0 && hue == 0)
            return mask;

        PocoNativeFilter.lightEffect(mask, light, saturation, hue);

        return mask;
    }

    /*
    * 肤色效果
    * val 范围 [-100 - 100] 默认0
    * */
    public static Bitmap skinAdjust(Bitmap dest, int val)
    {
        if (dest == null || Bitmap.Config.ARGB_8888 != dest.getConfig() || val == 0)
            return dest;

        int val1, val2, val3;
        if(val < 0)
        {
            val1 = (int)(-0.8*val);
            val2 = (int)(0.2*val);
            val3 = 0;
        } else{
            val1 = 0;
            val2 = (int)(-0.25*val);
            val3 = -val;
        }

        PocoNativeFilter.OptionColorAdjust(dest, val3, 0, val1, val2,
                0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0);

        return dest;
    }

/*
* 美人肤色效果
* val 范围 [-50 - 50] 默认0
* */
    public static Bitmap skinColorAdjust(Bitmap dest, int val)
    {
        if (null == dest || Bitmap.Config.ARGB_8888 != dest.getConfig())
            return null;

        if(val == 0)
            return dest;

        if(val < -50)
            val = -50;
        else if(val > 50)
            val = 50;

        PocoNativeFilter.skinColorAdjust(dest, val);

        return dest;
    }
}
