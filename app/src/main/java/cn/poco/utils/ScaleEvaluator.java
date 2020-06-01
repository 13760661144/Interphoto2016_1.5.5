package cn.poco.utils;

import android.animation.TypeEvaluator;
import android.graphics.Point;

/**
 * Created by admin on 2017/3/17.
 */

public class ScaleEvaluator implements TypeEvaluator<Point>
{
	@Override
	public Point evaluate(float fraction, Point startValue, Point endValue)
	{
		int w = startValue.x + (int)((endValue.x - startValue.x) * fraction);
		int h = startValue.y + (int)((endValue.y - startValue.y) * fraction);
		return new Point(w, h);
	}
}
