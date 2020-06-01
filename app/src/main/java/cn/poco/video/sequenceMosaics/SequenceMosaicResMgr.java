package cn.poco.video.sequenceMosaics;

import android.content.Context;

import java.util.ArrayList;

import cn.poco.beautify.SimpleListItem;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv100.SimpleBtnList100;
import cn.poco.video.render.transition.TransitionItem;

/**
 * Created by admin on 2017/10/18.
 */

public class SequenceMosaicResMgr
{
	public static ArrayList<SimpleBtnList100.Item> getTransitionItems(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int[] outRess = new int[]{R.drawable.video_feature_clip_icon, R.drawable.transition_overlap_out, R.drawable.transition_black_out, R.drawable.transition_white_out, R.drawable.transition_fuzzy_out};
		int[] overRess = new int[]{R.drawable.video_feature_clip_icon, R.drawable.transition_overlap_over, R.drawable.transition_black_over, R.drawable.transition_white_over, R.drawable.transition_fuzzy_over};
		int[] transitions = new int[]{TransitionItem.NONE, TransitionItem.ALPHA, TransitionItem.BLACK, TransitionItem.WHITE,  TransitionItem.BLUR};
		int[] tjIds = new int[]{R.integer.美化_剪裁_free, R.integer.美化_剪裁_3_2, R.integer.美化_剪裁_4_3, R.integer.美化_剪裁_16_9, R.integer.美化_剪裁_1_1, R.integer.美化_剪裁_2_3, R.integer.美化_剪裁_3_4, R.integer.美化_剪裁_9_16};
		String[] titles = new String[]{context.getResources().getString(R.string.transition_none),
				context.getResources().getString(R.string.transition_overlay),
				context.getResources().getString(R.string.transition_black),
				context.getResources().getString(R.string.transition_white),
				context.getResources().getString(R.string.transition_fuzzy)};
		int width = ShareData.m_screenWidth / overRess.length;
		SimpleListItem item;
		for(int i = 0; i < outRess.length; i++)
		{
			item = new SimpleListItem(context);
			item.m_uri = i;
			item.m_ex = transitions[i];
			item.m_tjID = tjIds[i];
			item.img_res_over = overRess[i];
			item.img_res_out = outRess[i];
			item.item_width = width;
			item.isVertical  = true;
			item.m_showTitle = true;
			item.def_title_size = 10;
			item.title_color_out = 0xff999999;
			item.title_color_over = 0xffffc433;
			item.m_title = titles[i];
			item.InitDatas();
			out.add(item);
		}

		return out;
	}

	public static ArrayList<SimpleBtnList100.Item> getTimeItems(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int width = ShareData.m_screenWidth / 2;

		SimpleListItem item = new SimpleListItem(context);
		item.m_uri = VideoEditTimeId.TYPE_FREE;
		item.img_res_out = R.drawable.video_eidt_current_unselected;
		item.img_res_over = R.drawable.video_edit_current_selected;
		item.item_width = width;
		item.InitDatas();
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = VideoEditTimeId.TYPE_10S;
		item.img_res_over = R.drawable.video_edit_10s_selected;
		item.img_res_out = R.drawable.video_edit_10s_unselected;
		item.item_width = width;
		item.InitDatas();
		out.add(item);

		return out;
	}

	public static ArrayList<SimpleBtnList100.Item> getEditItems(Context context)
	{
		ArrayList<SimpleBtnList100.Item> out = new ArrayList<SimpleBtnList100.Item>();
		int width = ShareData.m_screenWidth / 2;

		SimpleListItem item = new SimpleListItem(context);
		item.m_uri = 0;
		item.img_res_out = R.drawable.video_edit_cut_over;
		item.img_res_over = R.drawable.video_edit_cut_over;
		item.item_width = width;
		item.InitDatas();
		out.add(item);

		item = new SimpleListItem(context);
		item.m_uri = 1;
		item.img_res_over = R.drawable.beauty_adjust_btn_out;
		item.img_res_out = R.drawable.beauty_adjust_btn_out;
		item.item_width = width;
		item.InitDatas();
		out.add(item);

		return out;
	}
}
