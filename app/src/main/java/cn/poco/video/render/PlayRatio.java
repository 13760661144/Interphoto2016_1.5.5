package cn.poco.video.render;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by: fwc
 * Date: 2017/10/18
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({PlayRatio.RATIO_9_16, PlayRatio.RATIO_16_9, PlayRatio.RATIO_235_1, PlayRatio.RATIO_1_1})
public @interface PlayRatio {
	int RATIO_9_16 = 1;
	int RATIO_16_9 = 2;
	int RATIO_235_1 = 3;
	int RATIO_1_1 = 4;
}
