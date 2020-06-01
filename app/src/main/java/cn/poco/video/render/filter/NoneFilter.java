package cn.poco.video.render.filter;

import android.content.Context;

import cn.poco.interphoto2.R;

/**
 * Created by: fwc
 * Date: 2017/12/15
 */
public class NoneFilter extends AbstractFilter {

	public NoneFilter(Context context) {
		super(context);

		createProgram(R.raw.vertex_shader, R.raw.fragment_origin_shader);
	}

	@Override
	public void setParams(Params params) {

	}

	@Override
	protected void onSetUniformData() {

	}
}
