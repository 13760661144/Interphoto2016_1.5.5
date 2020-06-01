package cn.poco.video.helper;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Shine on 2017/6/21.
 */

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration{
    private int mSpanCount;
    private int mSpacing;

    public GridSpacingItemDecoration(int spanCount, int space) {
        this.mSpanCount = spanCount;
        this.mSpacing = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int columnIndex = position % mSpanCount;

        outRect.left = columnIndex * mSpacing / mSpanCount;
        outRect.right = mSpacing - (columnIndex + 1) * mSpacing / mSpanCount;
        if (position >= mSpanCount) {
            outRect.top = mSpacing;
        }
    }
}
