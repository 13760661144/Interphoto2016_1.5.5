package cn.poco.album2.view;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2017/3/6
 */
public class ItemView extends LinearLayout {

	public View square;
	public TextView text;
	public ItemView(Context context) {
		super(context);

		init();
	}

	private void init() {
		setOrientation(HORIZONTAL);
		square = new View(getContext());
		LayoutParams params = new LayoutParams(ShareData.PxToDpi_xhdpi(6), ShareData.PxToDpi_xhdpi(6));
		params.gravity = Gravity.CENTER_VERTICAL;
		addView(square, params);

		text = new TextView(getContext());
		text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
		text.setTextColor(0xffaaaaaa);
		text.setIncludeFontPadding(false);
		params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.leftMargin = ShareData.PxToDpi_xhdpi(10);
		params.gravity = Gravity.CENTER_VERTICAL;
		addView(text, params);
	}
}
