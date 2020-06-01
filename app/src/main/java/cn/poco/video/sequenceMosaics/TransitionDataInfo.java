package cn.poco.video.sequenceMosaics;

import cn.poco.interphoto2.R;
import cn.poco.video.render.transition.TransitionItem;

/**
 * Created by admin on 2017/10/18.
 */

public class TransitionDataInfo
{
	public int mID;
	public int mRes;
	public int mResSelected;
	public int mResDefault;

	public TransitionDataInfo(){
		mID = TransitionItem.NONE;
		mRes = R.drawable.video_feature_clip_icon;
		mResSelected = R.drawable.video_transition_none;
		mResDefault = R.drawable.video_transition_none_default;
	}

	public TransitionDataInfo(int id)
	{
		mID = id;
		switch(id)
		{
			case TransitionItem.NONE:
				mResSelected = R.drawable.video_transition_none;
				mResDefault = R.drawable.video_transition_none_default;
				break;
			case TransitionItem.ALPHA:
				mResSelected = R.drawable.video_transition_crossstack;
				mResDefault = R.drawable.video_transition_cross_default;
				break;
			case TransitionItem.BLACK:
				mResSelected = R.drawable.video_transition_black;
				mResDefault = R.drawable.video_transition_black_default;
				break;
			case TransitionItem.BLUR:
				mResSelected = R.drawable.video_transition_blurry;
				mResDefault = R.drawable.video_transition_blurry_default;
				break;
			case TransitionItem.WHITE:
				mResSelected = R.drawable.video_white_transition;
				mResDefault = R.drawable.video_transition_while_default;
				break;
		}
	}


}
