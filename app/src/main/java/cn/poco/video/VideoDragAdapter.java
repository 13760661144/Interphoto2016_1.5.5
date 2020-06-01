package cn.poco.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import cn.poco.draglistview.DragListItemInfo;
import cn.poco.draglistview.MyDragItemAdapter;
import cn.poco.draglistview.MyListItem;
import cn.poco.interphoto2.R;
import cn.poco.resource.MusicRes;
import cn.poco.resource.VideoTextRes;
import cn.poco.tianutils.ImageUtils;
import cn.poco.tianutils.ShareData;
import cn.poco.video.view.VideoDragItem;

/**
 * Created by lgd on 2017/6/8.
 */

public class VideoDragAdapter extends MyDragItemAdapter {

    public VideoDragAdapter(Context context, boolean dragOnLongPress) {
        super(context, dragOnLongPress);
    }

    @Override
    public void onBindViewHolder(DragHolder holder, int position) {
        DragListItemInfo info = mItemList.get(position);
        final VideoDragItem item = (VideoDragItem) holder.itemView;
        item.setMusicMode(false);
        item.SetSelectLogo(null);
        item.m_textColorOut = Color.WHITE;
        if(info.m_uri == DragListItemInfo.URI_LOCAL_MUSIC){
            item.m_textColorOut = Color.BLACK;
            item.m_textColorOver = Color.BLACK;
        }else if(info.m_uri != DragListItemInfo.URI_MUSIC_NONE){
            if (info.m_ex instanceof MusicRes) {
                item.setMusicMode(true);
            }
        }
        if (info.m_uri != DragListItemInfo.URI_VIDEO_TEXT_NONE) {
            if (info.m_ex instanceof VideoTextRes && info.m_selected && !info.m_isHideEditLogo) {
                item.SetSelectLogo(BitmapFactory.decodeResource(m_context.getResources(), R.drawable.video_text_entry));
            }
        }
        super.onBindViewHolder(holder, position);
        item.ShowLock(false);
        if(info.m_uri != DragListItemInfo.URI_LOCAL_MUSIC || info.m_uri != DragListItemInfo.URI_MUSIC_NONE) {
            if (info.m_ex instanceof MusicRes) {
                if(info.m_style == DragListItemInfo.Style.NEED_DOWNLOAD || info.m_isLock) {
                    item.ShowRecomment(true);
                }
            }
        }
        if (info.m_uri != DragListItemInfo.URI_VIDEO_TEXT_NONE) {
            if (info.m_ex instanceof VideoTextRes) {
                if(info.m_style == DragListItemInfo.Style.NEED_DOWNLOAD || info.m_isLock) {
                    item.ShowRecomment(true);
                }
            }
        }
    }

    @Override
    protected Bitmap MakeThumbBmp(DragListItemInfo res) {

        if (res.m_uri == DragListItemInfo.URI_VIDEO_TEXT_NONE || res.m_uri == DragListItemInfo.URI_MUSIC_NONE) {
            return makeVideoThumb(m_context);
        } else {
            return super.MakeThumbBmp(res);
        }
    }

    protected Bitmap makeVideoThumb(Context context) {
        int bmpSize = ShareData.PxToDpi_xhdpi(140);
        Bitmap out = Bitmap.createBitmap(bmpSize, bmpSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(out);
        canvas.drawColor(0xff181818);
        Paint pt = new Paint();
        pt.setAntiAlias(true);
        float imgSize = ShareData.PxToDpi_xhdpi(48);
        float imgX = (bmpSize - imgSize) / 2f;
        float imgY = (bmpSize - imgSize) / 2f;
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.video_text_none);
        Matrix matrix = new Matrix();
        matrix.setTranslate(imgX, imgY);
        canvas.drawBitmap(bmp, matrix, null);

        if (out != null) {
            Bitmap temp = out;
            out = ImageUtils.MakeRoundBmp(temp, m_thumbW, m_thumbH, m_defRoundSize);
            if (out != temp) {
                temp = null;
            }
        }

        return out;
    }

    @Override
    public MyListItem MakeItem(Context context) {
        MyListItem item = new VideoDragItem(context);
        item.m_thumbW = item.m_thumbH = ShareData.PxToDpi_xhdpi(140);
        item.m_headW = item.m_headH = ShareData.PxToDpi_xhdpi(60);
        item.m_textPadding = ShareData.PxToDpi_xhdpi(10);
        item.m_textSize = 11;
        item.m_authorSize = 9;
        item.m_thumbTopMargin = ShareData.PxToDpi_xhdpi(20);
        item.m_lockMargin = ShareData.PxToDpi_xhdpi(5);
        item.m_textColorOut = item.m_textColorOver = 0xffffffff;
        item.m_leftMargin = item.m_rigthMargin = ShareData.PxToDpi_xhdpi(10);
        item.m_tipBottomMargin = ShareData.PxToDpi_xhdpi(10);
        item.m_roundSize = 2;
        item.Init();
        return item;
    }
}
