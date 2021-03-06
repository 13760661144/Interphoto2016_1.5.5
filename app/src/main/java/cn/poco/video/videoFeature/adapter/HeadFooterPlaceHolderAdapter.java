package cn.poco.video.videoFeature.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by Shine on 2017/7/25.
 */
public abstract class HeadFooterPlaceHolderAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    protected List<T> mDataList;
    private Context mContext;
    protected int mHeaderW;
    protected int mFooterW;
    protected int mHeight;

    public HeadFooterPlaceHolderAdapter(Context context, List<T> dataList) {
        mContext = context;
        mDataList = dataList;
        initHeaderAndFooter();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(mContext);
        RecyclerView.LayoutParams params;
        HeadFooterPlaceHolderAdapter.ViewHolder viewHolder;
        if (viewType == 0) {
            params = new RecyclerView.LayoutParams(mHeaderW, mHeight);
            imageView.setLayoutParams(params);
            viewHolder = new HeadFooterPlaceHolderAdapter.ViewHolder(imageView);
        } else if (viewType == 1) {
            params = new RecyclerView.LayoutParams(mFooterW, mHeight);
            imageView.setLayoutParams(params);
            viewHolder = new HeadFooterPlaceHolderAdapter.ViewHolder(imageView);
        } else {
            viewHolder = onCreateViewHolder(viewType);
        }
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else if (position == mDataList.size() - 1){
            return 1;
        } else {
            return 2;
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected abstract void initHeaderAndFooter();
    protected abstract HeadFooterPlaceHolderAdapter.ViewHolder onCreateViewHolder(int type);

}
