package cn.poco.video.videotext;

import android.graphics.Bitmap;

import cn.poco.video.videotext.text.VideoTextView;

/**
 * Created by lgd on 2018/1/3.
 */

public class VideoTextCallbackAdapter implements VideoTextView.VideoTextCallback
{

    @Override
    public void OnViewTouch(boolean hasChoose)
    {

    }

    @Override
    public void OnEditBtn()
    {

    }

    @Override
    public void onDragEnd()
    {

    }

    @Override
    public Bitmap MakeShowImg(Object info, int frW, int frH)
    {
        return null;
    }

    @Override
    public Bitmap MakeOutputImg(Object info, int outW, int outH)
    {
        return null;
    }

    @Override
    public Bitmap MakeShowFrame(Object info, int frW, int frH)
    {
        return null;
    }

    @Override
    public Bitmap MakeOutputFrame(Object info, int outW, int outH)
    {
        return null;
    }

    @Override
    public Bitmap MakeShowBK(Object info, int frW, int frH)
    {
        return null;
    }

    @Override
    public Bitmap MakeOutputBK(Object info, int outW, int outH)
    {
        return null;
    }

    @Override
    public Bitmap MakeShowPendant(Object info, int frW, int frH)
    {
        return null;
    }

    @Override
    public Bitmap MakeOutputPendant(Object info, int outW, int outH)
    {
        return null;
    }

    @Override
    public void SelectPendant(int index)
    {

    }
}
