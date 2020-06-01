package cn.poco.video.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.StateListDrawable;

/**
 * Created by Simon Meng on 2018/1/8.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class UiUtil {

    public static StateListDrawable makeSelector(Context context, int normal, int selected) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int [] {android.R.attr.state_pressed}, context.getResources().getDrawable(selected));
        stateListDrawable.addState(new int [] {android.R.attr.state_selected}, context.getResources().getDrawable(selected));
        stateListDrawable.addState(new int [] {android.R.attr.state_focused}, context.getResources().getDrawable(selected));
        stateListDrawable.addState(new int[] {}, context.getResources().getDrawable(normal));
        return stateListDrawable;
    }

    public static ColorStateList makeColorSelector(Context context, int normal ,int selected) {
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_pressed},
                new int[] {android.R.attr.state_selected},
                new int[] {android.R.attr.state_focused},
                new int[] {}
        };

        int[] colors = new int[] {
                selected,
                selected,
                selected,
                normal
        };

        ColorStateList stateList = new ColorStateList(states, colors);
        return stateList;
    }




}
