package cn.poco.album2.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;

/**
 * Created by: fwc
 * Date: 2016/9/1
 */
public class PhotoView extends RelativeLayout {

	private ImageView mPhoto;
	private ImageView mSelect;
	private ImageView mEdit;

	private boolean mStartSelect;

	public PhotoView(Context context) {
		super(context);
		init();
	}

	public PhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mPhoto = new ImageView(getContext());
		mPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(mPhoto, params);

		mSelect = new ImageView(getContext());
		mSelect.setImageResource(R.drawable.album_ic_unselect);
		params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = ShareData.PxToDpi_xhdpi(15);
		params.topMargin = ShareData.PxToDpi_xhdpi(15);
		addView(mSelect, params);
		mSelect.setVisibility(GONE);

		mEdit = new ImageView(getContext());
		mEdit.setImageResource(R.drawable.album_ic_edit);
		params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.rightMargin = ShareData.PxToDpi_xhdpi(15);
		params.bottomMargin = ShareData.PxToDpi_xhdpi(15);
		addView(mEdit, params);
		mEdit.setVisibility(GONE);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}

	public ImageView getImageView() {
		return mPhoto;
	}

	public void startSelect() {
		if (!mStartSelect) {
			mStartSelect = true;
			mSelect.setImageResource(R.drawable.album_ic_unselect);
			mSelect.setVisibility(VISIBLE);
		}
	}

	public void cancelSelect() {
		if (mStartSelect) {
			mStartSelect = false;
			mSelect.setVisibility(GONE);
		}
	}

	public void setSelect(boolean select) {
		if (mStartSelect) {
			if (select) {
				mSelect.setImageResource(R.drawable.album_ic_selected);
			} else {
				mSelect.setImageResource(R.drawable.album_ic_unselect);
			}
		}
	}

	public void setEdit(boolean edit) {
		if (edit) {
			mEdit.setVisibility(VISIBLE);
		} else {
			mEdit.setVisibility(GONE);
		}
	}
}
