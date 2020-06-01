package cn.poco.album2.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by: fwc
 * Date: 2017/7/31
 */
public class PhotoPagerAdapter extends PagerAdapter {

	private List<View> mViews;

	public PhotoPagerAdapter(List<View> views) {
		mViews = views;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(mViews.get(position));
		return mViews.get(position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		return mViews.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}
