package cn.poco.utils;

import android.app.Activity;
import android.graphics.Paint;

import java.util.ArrayList;

import cn.poco.beautify.FastDynamicListV4;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.ShareData;
import cn.poco.tsv.FastHSV;
import cn.poco.tsv.FastItemList;

public class CommonUI
{
	public static FastHSV MakeFastDynamicList4(Activity ac, ArrayList<FastDynamicListV4.ItemInfo> infos, boolean hasTitle, boolean hasBG, boolean hasNewState, FastItemList.ControlCallback cb)
	{
		ShareData.InitData(ac);

		FastHSV out = new FastHSV(ac);

		FastDynamicListV4 svc = new FastDynamicListV4(ac);
		int numOffsetW = 2;
		int numOffsetH = 2;
		svc.def_item_width = ShareData.PxToDpi_xhdpi(140) + numOffsetW;
		svc.def_item_height = ShareData.PxToDpi_xhdpi(160) + numOffsetH;
		svc.def_img_color = 0x80FFFFFF;
//		svc.def_bk_over_color = 0xFF21cba6;
		svc.def_item_left = ShareData.PxToDpi_xhdpi(10);
		svc.def_item_right = svc.def_item_left;
		svc.def_bk_x = 0;
		svc.def_bk_y = numOffsetH;
		svc.def_bk_w = svc.def_item_width - numOffsetW;
		svc.def_bk_h = svc.def_item_height - numOffsetH;
		svc.def_img_x = ShareData.PxToDpi_xhdpi(0) + numOffsetW;
		svc.def_img_y = ShareData.PxToDpi_xhdpi(20) + numOffsetH;
		svc.def_img_w = ShareData.PxToDpi_xhdpi(140);
		svc.def_img_h = svc.def_img_w;
		svc.def_img_round_size = ShareData.PxToDpi_xhdpi(10);
		svc.def_move_size = ShareData.PxToDpi_hdpi(30);
		svc.def_head_w = ShareData.PxToDpi_xhdpi(60);
		svc.def_head_h = ShareData.PxToDpi_xhdpi(60);
		svc.def_head_x = (svc.def_img_w - svc.def_head_w) / 2;
		svc.def_head_y = 0;
		svc.def_show_title = hasTitle;
		svc.def_loading_anim = true;
		if(hasTitle)
		{
			svc.def_title_size = ShareData.PxToDpi_xhdpi(20);
			svc.def_title_color_out = 0xFFFFFFFF;
			svc.def_title_color_over = 0xFFFFFFFF;
			svc.def_title_bottom_margin = ShareData.PxToDpi_xhdpi(10);
			svc.def_author_size = ShareData.PxToDpi_xhdpi(15);
			svc.def_title_margin = ShareData.PxToDpi_xhdpi(15);
			
			svc.def_text_bg_w_out = svc.def_img_w;
			Paint pt = new Paint();
			pt.setTextSize(svc.def_title_size);
			svc.def_text_bg_h_out = (int)(pt.descent() - pt.ascent() + 0.5) + svc.def_title_bottom_margin;
			svc.def_text_bg_h_out1 = 2 * svc.def_text_bg_h_out;
			svc.def_text_bg_x_out = svc.def_img_x;
			svc.def_text_bg_y_out = svc.def_img_y + svc.def_img_h - svc.def_text_bg_h_out;
			
			svc.def_text_y_over = svc.def_head_y + svc.def_head_h + ShareData.PxToDpi_xhdpi(30);
			svc.def_title_over_res = R.drawable.framework_item_over_icon;
			svc.def_title_over_w = ShareData.PxToDpi_xhdpi(46);
			svc.def_title_over_h = ShareData.PxToDpi_xhdpi(20);
			svc.def_title_over_y = svc.def_item_height - ShareData.PxToDpi_xhdpi(40) - svc.def_title_over_h;	
			svc.def_title_over_x = (svc.def_img_w - svc.def_title_over_w) / 2;
		}
		if(hasBG)
		{
			svc.def_show_title_bg = true;
			svc.def_text_bg_w_over = svc.def_img_w;
			svc.def_text_bg_h_over = svc.def_img_h;
			svc.def_text_bg_x_over = svc.def_img_x;
			svc.def_text_bg_y_over = svc.def_img_y;
		}
		svc.def_state_w = ShareData.PxToDpi_xhdpi(56);
		svc.def_state_h = svc.def_state_w;
		svc.def_state_x = svc.def_img_x + (svc.def_img_w - svc.def_state_w) / 2;
		svc.def_state_y = svc.def_img_y + (svc.def_img_h - svc.def_state_h) / 2;
		svc.def_new_w = ShareData.PxToDpi_hdpi(80);
		svc.def_new_h = svc.def_new_w;
		svc.def_new_x = svc.def_img_x + svc.def_img_w - svc.def_new_w;
		svc.def_new_y = svc.def_img_y;
		if(hasNewState)
		{
			svc.def_wait_res = R.drawable.beauty_item_loading;
			svc.def_loading_res = R.drawable.beauty_item_loading;
			svc.def_ready_res = R.drawable.framework_item_new_logo;
		}
		svc.def_loading_res = R.drawable.beauty_item_loading;
		svc.def_download_item_res = R.drawable.beauty_item_loading;
		svc.def_download_item_name = "下载更多";
		svc.def_num_bk_res = R.drawable.framework_download_num_bk;
		svc.def_num_x = svc.def_item_width - ShareData.PxToDpi_hdpi(30);
		svc.def_num_y = 0;
		svc.def_num_text_size = ShareData.PxToDpi_hdpi(12);
		svc.def_lock_x = svc.def_item_width - ShareData.PxToDpi_hdpi(31);
		svc.def_lock_y = svc.def_img_y + ShareData.PxToDpi_xhdpi(5);
		svc.def_lock_res = R.drawable.photofactory_item_lock;
		//参数设置完成才能INIT
		svc.InitData(cb);
		svc.SetData(infos);

		out.SetShowCore(svc);

		return out;
	}
	
	public static FastHSV MakeFastDynamicList5(Activity ac, ArrayList<FastDynamicListV4.ItemInfo> infos, boolean hasTitle, FastItemList.ControlCallback cb)
	{
		ShareData.InitData(ac);

		FastHSV out = new FastHSV(ac);

		FastDynamicListV4 svc = new FastDynamicListV4(ac);
		int numOffsetW = 0;
		int numOffsetH = 0;
		svc.def_item_width = ShareData.PxToDpi_xhdpi(160) + numOffsetW;
		svc.def_item_height = ShareData.PxToDpi_xhdpi(160) + numOffsetH;
		svc.def_img_color = 0x80FFFFFF;
		svc.def_item_left = ShareData.PxToDpi_xhdpi(6);
		svc.def_item_right = svc.def_item_left;
		svc.def_bk_x = 0;
		svc.def_bk_y = numOffsetH;
		svc.def_bk_w = svc.def_item_width - numOffsetW;
		svc.def_bk_h = svc.def_item_height - numOffsetH;
		svc.def_img_x = 0;
		svc.def_img_y = 0 + numOffsetH;
		svc.def_img_w = ShareData.PxToDpi_xhdpi(160);
		svc.def_img_h = svc.def_img_w;
		svc.def_img_round_size = ShareData.PxToDpi_xhdpi(10);
		svc.def_move_size = ShareData.PxToDpi_hdpi(30);
		svc.def_show_title = hasTitle;
		if(hasTitle)
		{
			Paint pt = new Paint();
			pt.setTextSize(svc.def_title_size);
			int textHeight = (int)(pt.descent() - pt.ascent());
			svc.def_title_size = ShareData.PxToDpi_xhdpi(20);
			svc.def_title_color_out = 0x00FFFFFF;
			svc.def_title_color_over = 0xFFFFFFFF;
			svc.def_title_bottom_margin = (svc.def_img_h - textHeight) / 2;
			
			svc.def_text_bg_w_over = svc.def_img_w;
			svc.def_text_bg_h_over = svc.def_img_h;
			svc.def_text_bg_x_over = svc.def_img_x;
			svc.def_text_bg_y_over = svc.def_img_y;
			
			svc.def_text_y_over = (svc.def_img_h - textHeight) / 2;
		} 
		svc.def_state_w = ShareData.PxToDpi_xhdpi(56);
		svc.def_state_h = svc.def_state_w;
		svc.def_state_x = svc.def_img_x + (svc.def_img_w - svc.def_state_w) / 2;
		svc.def_state_y = svc.def_img_y + (svc.def_img_h - svc.def_state_h) / 2;
		svc.def_new_w = ShareData.PxToDpi_hdpi(80);
		svc.def_new_h = svc.def_new_w;
		svc.def_new_x = svc.def_img_x + svc.def_img_w - svc.def_new_w;
		svc.def_new_y = svc.def_img_y;
		svc.def_wait_res = R.drawable.beauty_item_loading;
		svc.def_loading_res = R.drawable.beauty_item_loading;
		svc.def_ready_res = R.drawable.framework_item_new_logo;
		svc.def_download_item_res = R.drawable.beauty_item_loading;
		svc.def_download_item_name = "下载更多";
		svc.def_num_bk_res = R.drawable.framework_download_num_bk;
		svc.def_num_x = svc.def_item_width - ShareData.PxToDpi_hdpi(30);
		svc.def_num_y = 0;
		svc.def_num_text_size = ShareData.PxToDpi_hdpi(12);
		svc.def_lock_x = svc.def_item_width - ShareData.PxToDpi_hdpi(26);
		svc.def_lock_y = 0;
		svc.def_lock_res = R.drawable.photofactory_item_lock;
		//参数设置完成才能INIT
		svc.InitData(cb);
		svc.SetData(infos);

		out.SetShowCore(svc);

		return out;
	}
}
