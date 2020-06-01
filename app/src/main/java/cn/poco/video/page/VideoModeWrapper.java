package cn.poco.video.page;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.resource.FilterRes;
import cn.poco.video.VideoConfig;
import cn.poco.video.VideoResMgr;
import cn.poco.video.helper.TimeFormatter;
import cn.poco.video.render.GLVideoView;
import cn.poco.video.render.IVideoPlayer;
import cn.poco.video.render.PlayRatio;
import cn.poco.video.render.adjust.AdjustItem;
import cn.poco.video.render.transition.TransitionItem;
import cn.poco.video.render.view.ProgressView;
import cn.poco.video.sequenceMosaics.VideoInfo;
import cn.poco.video.utils.VideoUtils;
import cn.poco.video.videoFeature.data.VideoSpeedInfo;
import cn.poco.video.videoMusic.MusicSaveInfo;
import cn.poco.video.videotext.TextSaveInfo;
import cn.poco.video.videotext.VideoTextPage;
import cn.poco.video.videotext.text.VideoTextView;
import cn.poco.video.view.ActionBar;

/**
 * Created by lgd on 2017/12/29.
 */

public class VideoModeWrapper {
    public ActionBar mActionBar;
    public GLVideoView mVideoView;
    public ProgressView mProgressView;

    public ArrayList<VideoInfo> mVideoInfos;
    public VideoInfo mCurrentBeautifiedVideo;
    public HashMap<String, VideoSpeedInfo> mSpeedVideoInfos = new HashMap<>(); //key是路径，存储视频

    public int mActionBarHeight;
    public int mVideoHeight;
    public boolean isModify;

    public int mPlayRatio = PlayRatio.RATIO_1_1;
    /**
     * 是否可以
     */
    public boolean isCanSave = true;
    /**
     * 是否拖动进度条
     */
    protected boolean isDragProgress;
    public boolean hasSave = false;  //保存过就设置true
    protected boolean mUiEnable;
    //水印页面
    public VideoTextView mTextView;
    public TextSaveInfo mTextSaveInfo = new TextSaveInfo();

    //音乐
    public MediaPlayer mMusicPlayer;
    public MusicSaveInfo mMusicSaveInfo = new MusicSaveInfo();

    protected ImageView mPlayBtn;
    private ProcessMode mCurrentMode = ProcessMode.Normal;
    protected TextView mProgressTip;

    public boolean mIsParticialFilterModify;


    public void refreshCurVideoDuration(VideoInfo videoInfo)
    {
        if(videoInfo != null){
            long realDuration = VideoUtils.getDurationFromVideo2(videoInfo.mClipPath);
            if(videoInfo.GetClipTime() != realDuration){
                if(realDuration > videoInfo.mDuration){
                    realDuration = videoInfo.mDuration;
                }
                if(videoInfo.GetClipTime() != realDuration){
                    long offset = realDuration - videoInfo.GetClipTime();
                    long start = videoInfo.GetStartTime();
                    long end = videoInfo.GetEndTime();
                    start = start - offset/2;
                    end = end + offset/2;
                    if(start < 0){
                        start = 0;
                        end = realDuration;
                    }else if(end > videoInfo.mDuration){
                        end = videoInfo.mDuration;
                        start = end - realDuration;
                        if(start < 0){
                            start = 0;
                        }
                    }
                    videoInfo.mSelectStartTime = start;
                    videoInfo.mSelectEndTime = end;
                }
            }
        }
    }

    public void refreshCurVideoDuration()
    {
        refreshCurVideoDuration(mCurrentBeautifiedVideo);
    }

    /**
     *
     */
    public long getUsaleTime()
    {
        long time = VideoConfig.DURATION_FREE_MODE - getVideosDuration();
        if(time < 0){
            time = 0;
        }
        return time;
    }

    public boolean isVideoSilence()
    {
//        return mMusicSaveInfo.mCurVolume == 0;
        return mVideoView.getVolume() == 0;
    }

    public void deleteVideo(int index)
    {
        VideoInfo info = mVideoInfos.remove(index);
        if(info.isHasClipped()){
            new File(info.mClipPath).delete();
        }
    }


    public boolean canAddVideo(VideoInfo videoInfo)
    {
        if(getVideosDuration() + videoInfo.GetClipTime() <= VideoConfig.DURATION_FREE_MODE){
            return true;
        }else{
            return false;
        }
    }

    public void changeVideoOrder(int fromPosition, int toPosition, int index, float progress)
    {
        if(fromPosition != toPosition && mVideoInfos != null && mVideoInfos.size() > fromPosition && mVideoInfos.size() > toPosition && index < mVideoInfos.size())
        {
            VideoInfo info = mVideoInfos.remove(fromPosition);
            mVideoInfos.add(toPosition, info);
            mVideoView.changeVideoOrder(fromPosition, toPosition, index, progress);
            long time = (long)(progress * mVideoInfos.get(index).GetClipTime()) + getBeforeTotalDuration(index);
            musicSeekTo(time);
        }
//        musicSeekTo((long) (progress * getVideosDuration()));
    }

    public long getVideoStartTime(int index) {
        long totalTime = 0;
        for (int i = 0; i < index; i++) {
            long duration = mVideoInfos.get(i).GetClipTime();
            totalTime += duration + TransitionItem.DEFAULT_TIME;
        }
        return totalTime;
    }



    public void pauseAll()
    {
        pauseAll(false);
    }

    public void pauseAll(boolean onlyStopMusic) {
        // 视频播放的时候显示时间标签，暂停的时候时间标签消失
//        long time = mVideoView.getCurrentPosition();
//        showProgressTip(time, mVideoView.getTotalDuration());
        pauseMusicAndText();
        if (!onlyStopMusic) {
            mVideoView.pause();
        }
    }


    private void musicSeekTo(long time)
    {
        if (mMusicPlayer != null && mMusicSaveInfo.mMusicPath != null)
        {
//				long duration = (int) VideoUtils.getDurationFromVideo2(mMusicSaveInfo.mMusicPath);
            long currentPosition;
            if (isEditMode() && mCurrentBeautifiedVideo != null)
            {
                long startDuration = getBeforeTotalDuration(mVideoInfos.indexOf(mCurrentBeautifiedVideo));
                currentPosition = (startDuration + +time);
            } else
            {
                currentPosition = time;
            }
            long duration = mMusicPlayer.getDuration();
            long startTime = (currentPosition + mMusicSaveInfo.mMusicStartTime) % duration;
            try
            {
                mMusicPlayer.seekTo((int) startTime);
            } catch (IllegalStateException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param time 视频的开始时间
     */
    public void seekTo(long time)
    {
       seekTo(time,false);
    }

    public void seekTo(long time,boolean isForce)
    {
        if(canPlayMusicAndText())
        {
            musicSeekTo(time);
        }
        hidePlayBtn();
        if(isForce && mCurrentMode == ProcessMode.CLIP)
        {
            //裁剪定时器seekto
            mVideoView.forceSeekTo(0,time);
        }else{
            mVideoView.seekTo(time);
        }
    }

    public void seekTo(int index, long videoTime, long musicTime) {
        if(canPlayMusicAndText())
        {
            musicSeekTo(musicTime);
        }
        //拖动时要隐藏，放手后不播放才显示
        hidePlayBtn();
        mVideoView.seekTo(index, videoTime);
    }



    //改变视频时长
    public void changeVideoTime(boolean change, long lastDuration, long curDuration){
        if(change){
            if(mTextView != null && mTextView.getVideText()!= null) {
                int type = mTextSaveInfo.mDisplayType;
                if (type == VideoTextPage.TYPE_ALL)
                {
                    mTextSaveInfo.mStartTime = 0;
                    if(getVideosDuration() > mTextView.GetTextTotalTimeNoStayTime()){
                        mTextSaveInfo.mStayTime = (int) (getVideosDuration() - mTextView.GetTextTotalTimeNoStayTime());
                    }else{
                        mTextSaveInfo.mStayTime = 0;
                    }
                } else if (type == VideoTextPage.TYPE_END)
                {
                    mTextSaveInfo.mStayTime = 0;
                    if(getVideosDuration() > mTextView.GetTextTotalTimeNoStayTime()){
                        mTextSaveInfo.mStartTime = (int) (getVideosDuration() - mTextView.GetTextTotalTimeNoStayTime());
                    }else{
                        mTextSaveInfo.mStartTime = 0;
                    }
                } else
                {
                    mTextSaveInfo.mStartTime = 0;
                    mTextSaveInfo.mStayTime = 0;
                }
                mTextView.setStartTime((int) mTextSaveInfo.mStartTime);
                mTextView.setStayTime((int) mTextSaveInfo.mStayTime);
                int tongJi = 0;
                if(mTextSaveInfo.mTextRes  != null){
                    tongJi  = mTextSaveInfo.mTextRes.m_tjId;
                }
                mVideoView.setVideoText(tongJi,mTextView.getVideText(), mTextView.m_origin,((int)mTextSaveInfo.mStartTime), (int) mTextSaveInfo.mStayTime);
            }
            if(!TextUtils.isEmpty(mMusicSaveInfo.mMusicPath)){
                if(mMusicSaveInfo.mMusicStartTime + curDuration > mMusicPlayer.getDuration()){
                    mMusicSaveInfo.mMusicStartTime = (int) (mMusicPlayer.getDuration() - curDuration);
                }
                if(mMusicSaveInfo.mMusicStartTime < 0){
                    mMusicSaveInfo.mMusicStartTime = 0;
                }
                int tongJi = 0;
                if(mMusicSaveInfo.mMusicRes != null){
                    tongJi = mMusicSaveInfo.mMusicRes.m_tjId;
                }
                mVideoView.setMusicPath(tongJi,mMusicSaveInfo.mMusicPath, mMusicSaveInfo.mMusicStartTime,mMusicSaveInfo.mMaxVolume);
            }
        }
    }

    /**
     * 当前播放器的总时长
     * @return
     */
    public long getCurDuration()
    {
        long duration = 0;
        if(mVideoView != null){
            duration = mVideoView.getTotalDuration();
        }
        if(duration == 0)
        {
            if(mCurrentBeautifiedVideo != null){
                duration = mCurrentBeautifiedVideo.GetClipTime();
            }
        }
        return duration;
    }

    public long getBeforeTotalDuration(int index)
    {
        int totalDuration = 0;
        if(index >=  mVideoInfos.size()){
            index = mVideoInfos.size() - 1;
        }
        if(index < 0){
            index = 0;
        }
        for (int i = 0; i < index; i++)
        {
            totalDuration += mVideoInfos.get(i).GetClipTime();
        }
        totalDuration -= mVideoView.getBlendTime(index);
        return totalDuration;
    }

    public long getVideosDuration()
    {
        int duration = 0;
        if (mVideoInfos != null)
        {
            for (VideoInfo info : mVideoInfos)
            {
                duration += info.GetClipTime();
            }
        }
        duration -= mVideoView.getBlendTime(mVideoInfos.size() -1 );
        return duration;
    }

    private void startMusicAndText(boolean isRestart)
    {
        if(canPlayMusicAndText())
        {
            if(mTextView != null){
                mTextView.SetSelPendant( - 1);
            }
            if (mMusicPlayer != null && mMusicSaveInfo.mMusicPath != null)
            {
                if (isRestart)
                {
                    musicSeekTo(0);
                }
                if (!mMusicPlayer.isPlaying())
                {
                    mMusicPlayer.start();
                }
            }
        }
    }

    /**
     * 编辑视频后调整音乐和文字
     */
    public void adjustMusicAndText()
    {
//        if(mTextView != null && mTextView.getVideText()!= null) {
//            int type = mTextSaveInfo.mDisplayType;
//            if (type == VideoTextPage.TYPE_ALL)
//            {
//                mTextSaveInfo.mStartTime = 0;
//                if(getVideosDuration() > mTextView.GetTextTotalTimeNoStayTime()){
//                    mTextSaveInfo.mStayTime = (int) (getVideosDuration() - mTextView.GetTextTotalTimeNoStayTime());
//                }else{
//                    mTextSaveInfo.mStayTime = 0;
//                }
//            } else if (type == VideoTextPage.TYPE_END)
//            {
//                mTextSaveInfo.mStayTime = 0;
//                if(getVideosDuration() > mTextView.GetTextTotalTimeNoStayTime()){
//                    mTextSaveInfo.mStartTime = (int) (getVideosDuration() - mTextView.GetTextTotalTimeNoStayTime());
//                }else{
//                    mTextSaveInfo.mStartTime = 0;
//                }
//            } else
//            {
//                mTextSaveInfo.mStartTime = 0;
//                mTextSaveInfo.mStayTime = 0;
//            }
//            mTextView.setStartTime((int) mTextSaveInfo.mStartTime);
//            mTextView.setStayTime((int) mTextSaveInfo.mStayTime);
//            mVideoView.setVideoText(mTextSaveInfo.mUri,mTextView.getVideText(), mTextView.m_origin,((int) mTextSaveInfo.mStartTime), (int) mTextSaveInfo.mStayTime);
//        }
//        if(!TextUtils.isEmpty(mMusicSaveInfo.mMusicPath)){
//            if(mMusicSaveInfo.mMusicStartTime + getVideosDuration() > mMusicPlayer.getDuration()){
//                mMusicSaveInfo.mMusicStartTime = (int) (mMusicPlayer.getDuration() - getVideosDuration());
//            }
//            if(mMusicSaveInfo.mMusicStartTime < 0){
//                mMusicSaveInfo.mMusicStartTime = 0;
//            }
//            mVideoView.setMusicPath(mMusicSaveInfo.mMusicUri,mMusicSaveInfo.mMusicPath, mMusicSaveInfo.mMusicStartTime,mMusicSaveInfo.mMaxVolume);
//        }


        long curPosition = mVideoView.getCurrentPosition();
        if(mMusicPlayer != null && mMusicSaveInfo.mMusicPath != null){
            long seekTo = (curPosition+mMusicSaveInfo.mMusicStartTime) % mMusicPlayer.getDuration();
            mMusicPlayer.seekTo((int) seekTo);
            if(mVideoView.isPlaying()){
                if(!mMusicPlayer.isPlaying())
                {
                    mMusicPlayer.start();
                }
            }
        }
        if(mTextView != null  ){
            mTextView.setCurTime((int) curPosition);
        }
    }

    public void onPauseAll()
    {
        showPlayBtn();
        pauseMusicAndText();
        mVideoView.onPause();
    }

    public void resumeAll(boolean isRestart)
    {
//        hideProgressTip();
        startMusicAndText(isRestart);
        hidePlayBtn();
		if (isRestart) {
            mVideoView.restart();
		} else {
            if(!mVideoView.isPlaying())
            {
                mVideoView.start();
            }
		}

    }

    public void onResumeAll()
    {
//        hideProgressTip();
//        hidePlayBtn();
        mVideoView.onResume();
    }

    public boolean isEditMode()
    {
        boolean isEdit = false;
        switch (mCurrentMode){
            case Edit:
            case CLIP:
            case CANVASADJUST:
            case FILTER:
            case SPEEDRATE:
            case SEGENTATION:
            case COPY:
            case DELETE:
                isEdit = true;
                break;
        }
        return isEdit;
    }

    public boolean canPlayMusicAndText()
    {
        boolean can = true;
        switch (mCurrentMode)
        {
            case CANVASADJUST:
            case CLIP:
            case Transition:
                can = false;
                break;
        }
        return can;
    }

    /**
     * 进入这些模式显示旧的进度条
     * @return
     */
    public boolean canShowProgressView()
    {
        if(mCurrentMode == ProcessMode.Watermark || mCurrentMode == ProcessMode.Fileter || mCurrentMode == ProcessMode.Music){
            return true;
        }else{
            return false;
        }
    }

    public void setCurrentMode(ProcessMode currentMode)
    {
        if(mCurrentMode != currentMode)
        {
//            if ((mCurrentMode == ProcessMode.Transition || mCurrentMode == ProcessMode.Edit )&& currentMode == ProcessMode.Normal)
            if ((mCurrentMode == ProcessMode.Edit )&& currentMode == ProcessMode.Normal)
            {
                adjustMusicAndText();
            }
            this.mCurrentMode = currentMode;
            if (canPlayMusicAndText())
            {
                if (isPlaying())
                {
                    startMusicAndText(false);
                }
            } else
            {
                pauseMusicAndText();
            }
            if (canShowProgressView())
            {
                showProgressView();
            } else
            {
                hideProgressView();
            }
        }
    }

    public ProcessMode getCurrentMode()
    {
        return mCurrentMode;
    }

    protected void pauseMusicAndText()
    {
        if(mMusicPlayer != null && mMusicSaveInfo.mMusicPath != null){
            if(mMusicPlayer.isPlaying()) {
                mMusicPlayer.pause();
            }
        }
        //文字根据时间，不用暂停
//        if(mTextView != null){
//            mTextView.setCurTime(0);
//        }
    }

    public boolean isPlaying()
    {
        return mVideoView.isPlaying();
    }

    public boolean isDragProgress()
    {
        return isDragProgress;
    }

    public void setIsDraggingProgress(boolean isDragging) {
        this.isDragProgress = isDragging;
        if(isDragging){
            pauseMusicAndText();
        }
    }

    public void showProgressView()
    {
        if(mProgressView != null){
            mProgressView.setVisibility(View.VISIBLE);
            mProgressView.setEnabled(true);
        }
    }

    public void hideProgressView()
    {
        if(mProgressView != null){
            mProgressView.setEnabled(false);
            mProgressView.setVisibility(View.GONE);
        }
    }

    public void showPlayBtn()
    {
        if (!isDragProgress && (mCurrentMode != ProcessMode.Watermark || mTextView.getVideText() == null) && mCurrentMode != ProcessMode.CANVASADJUST) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
    }
    public void hidePlayBtn()
    {
//        if (mPlayBtn != null && !isDragProgress()) {
        //拖动时要隐藏
        if (mPlayBtn != null) {
            mPlayBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void showProgressTip()
    {
        showProgressTip(mVideoView.getCurrentPosition(), mVideoView.getTotalDuration());
    }

    private void showProgressTip(long curTime, long duration) {
        showProgressTip(curTime, duration, false);
    }

    public void showProgressTip(long curTime, long duration, boolean userDashSperator) {
        mProgressTip.setText(TimeFormatter.formatTimeSpecifyToMills(curTime, duration, userDashSperator));
        mProgressTip.setVisibility(View.VISIBLE);
    }


    public void hideProgressTip()
    {
        if(mProgressTip != null && !isDragProgress()){
            mProgressTip.setVisibility(View.INVISIBLE);
        }
    }


    public boolean showCopyFeature() {
        boolean countCondition = this.mVideoInfos.size() < VideoConfig.MAX_NUM;
        boolean timeCondition = this.getVideosDuration() + mCurrentBeautifiedVideo.GetClipTime() <= VideoConfig.MAX_TIME;
        boolean show = countCondition && timeCondition;
        return show;
    }

    public boolean showDeleteFeature() {
        boolean countCondition = this.mVideoInfos.size() > VideoConfig.MIN_NUM;
        return countCondition;
    }

    public boolean showSplitFeature() {
        boolean countCondition = this.mVideoInfos.size() < VideoConfig.MAX_NUM;
        boolean timeCondition = mCurrentBeautifiedVideo.GetClipTime() > VideoConfig.SPLIT_MIN_TIME;
        return countCondition && timeCondition;
    }


    public void changeFilter(FilterRes filterRes) {
        mVideoView.changeFilter(filterRes);
    }

    public void changeFilter(int index, FilterRes filterRes) {
        mVideoView.changeFilter(index, filterRes);
    }


    public void changeFilterAlpha(float value) {
        mVideoView.changeFilterAlpha(value / 100);
    }

    public void addAdjust(int index, AdjustItem adjustItem) {
        mVideoView.addAdjust(0, adjustItem);
    }

    public void setVideoFilterUri(int uri) {
        for (VideoInfo item : mVideoInfos) {
            item.mFilterUri = uri;
        }
    }

    public void initResource(Context context)  {
        VideoResMgr.GetFilterRess(context);
    }

    public void onClose()
    {
        //清除速率的缓存文件
//        Iterator iterator = mSpeedVideoInfos.entrySet().iterator();
//        while (iterator.hasNext()){
//            Map.Entry entry = (Map.Entry) iterator.next();
//            String key = (String) entry.getKey();
//            VideoSpeedInfo speedInfo = (VideoSpeedInfo) entry.getValue();
//            speedInfo.deleteAllTempFile(key);
//        }
//        for (int i = 0; i < mVideoInfos.size(); i++)
//        {
//              deleteVideo(i);
//        }
        mVideoView.removeOnPlayListener(mOnPlayListener);
        mVideoView.removeOnProgressListener(mOnProgressListener);
//        mVideoView.removeOnDragSeekBarListener(mOnSeekBarChangeListener);
        mVideoView.release();
        if(mMusicPlayer != null){
            mMusicPlayer.pause();
            mMusicPlayer.release();
            mMusicPlayer = null;
        }
    }


    protected GLVideoView.OnProgressListener mOnProgressListener = new IVideoPlayer.OnProgressListener()
    {
        @Override
        public void onChanged(float progress, boolean isSeekTo)
        {
            //如使用mVideoView.getCurrentPosition()与progress不一致使水印闪动

            if(canPlayMusicAndText())
            {
                long currentPosition;
                if (isEditMode() && mCurrentBeautifiedVideo != null)
                {
                    long startDuration = getBeforeTotalDuration(mVideoInfos.indexOf(mCurrentBeautifiedVideo));
                    currentPosition = (long) (startDuration + mCurrentBeautifiedVideo.GetClipTime() * progress);
                }else{
                    currentPosition = (long) (getVideosDuration() * progress);
                }
                // 添加拖拽进度条按钮的统计
                if (mMusicPlayer != null && mMusicSaveInfo.mMusicPath != null)
                {
                    //最后1.5秒渐隐
                    if (mMusicPlayer.isPlaying())
                    {
                        int lastTime = (int) (getVideosDuration() - currentPosition);
                        if (lastTime <= 1500 && lastTime >= 0)
                        {
                            float volume = lastTime * mMusicSaveInfo.mMaxVolume / 1500;
                            if (volume != mMusicSaveInfo.mCurVolume)
                            {
                                mMusicSaveInfo.mCurVolume = volume;
                                mMusicPlayer.setVolume(mMusicSaveInfo.mCurVolume, mMusicSaveInfo.mCurVolume);
                            }
                        } else
                        {
                            if (mMusicSaveInfo.mCurVolume != mMusicSaveInfo.mMaxVolume)
                            {
                                mMusicSaveInfo.mCurVolume = mMusicSaveInfo.mMaxVolume;
                                mMusicPlayer.setVolume(mMusicSaveInfo.mCurVolume, mMusicSaveInfo.mCurVolume);
                            }
                        }
                    }
                    //视频没有结束回调，这里1暂停
                    if(progress == 1){
                        mMusicPlayer.pause();
                    }

                    //视频自己循环播放调用resume  resume那里音乐是start，没有seekTo,导致音乐继续播放，这里重设
                    if(progress == 0){
                         musicSeekTo(0);
                    }
                }
                if (mTextView != null)
                {
                    mTextView.setCurTime((int) currentPosition);
                }
            }
            if (mCurrentMode != ProcessMode.CLIP && mCurrentMode != ProcessMode.CANVASADJUST) {
                showProgressTip(mVideoView.getCurrentPosition(), mVideoView.getTotalDuration());
            } else {
                hideProgressTip();
            }
            if(canShowProgressView()){
                mProgressView.setProgress(progress);
            }
        }
    };

    protected GLVideoView.OnPlayListener mOnPlayListener = new IVideoPlayer.OnPlayListener()
    {

        @Override
        public void onStart()
        {
            mUiEnable = true;
            startMusicAndText(true);
            hidePlayBtn();
//            hideProgressTip();
        }

        @Override
        public void onResume()
        {
            startMusicAndText(false);
            hidePlayBtn();
//            hideProgressTip();
        }

        @Override
        public void onPause()
        {
//                long time = mVideoView.getCurrentPosition();
//                showProgressTip(time, mVideoView.getTotalDuration());
                showPlayBtn();
                pauseMusicAndText();
        }
    };

//	protected IVideoPlayer.OnDragSeekBarListener mOnSeekBarChangeListener = new IVideoPlayer.OnDragSeekBarListener() {
//
//        @Override
//        public void onDragChanged(float progress)
//        {
//            long time = (long)(mVideoView.getTotalDuration() * progress);
//            showProgressTip(time,mVideoView.getTotalDuration());
//            seekTo(time);
//        }
//
//        @Override
//        public void onDragStart(float progress)
//        {
//            setIsDraggingProgress(true);
//            pauseAll();
//            long time = (long)(mVideoView.getTotalDuration() * progress);
//            showProgressTip(time,mVideoView.getTotalDuration());
////            MyBeautyStat.onClickByRes(R.string.视频美化页_拖动视频进度条);
//        }
//
//        @Override
//        public void onDragStop(float progress)
//        {
//            setIsDraggingProgress(false);
//            if (mVideoView == null) {
//                return;
//            }
//            long time = (long)(mVideoView.getTotalDuration() * progress);
//            showProgressTip(time,mVideoView.getTotalDuration());
//            seekTo(time);
//            resumeAll(false);
//        }
//	};

    protected ProgressView.OnSeekBarChangeListener mOnSeekBarChangeListener = new ProgressView.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(ProgressView view, float progress) {
            long time = (long)(mVideoView.getTotalDuration() * progress);
            showProgressTip(time,mVideoView.getTotalDuration());
            seekTo(time);
        }

        @Override
        public void onStartTrackingTouch(ProgressView view) {

            // FIXME: 2018/2/1 水印还在播放
//            if(mTextView != null){
//                mTextView.SetSelPndant( -);
//            }
//
            setIsDraggingProgress(true);
            long time = (long)(mVideoView.getTotalDuration() * view.getProgress());
            showProgressTip(time,mVideoView.getTotalDuration());
            pauseAll();
        }

        @Override
        public void onStopTrackingTouch(ProgressView view) {
            setIsDraggingProgress(false);
            long time = (long)(mVideoView.getTotalDuration() * view.getProgress());
            showProgressTip(time,mVideoView.getTotalDuration());
            seekTo(time);
            resumeAll(false);
        }
    };

}
