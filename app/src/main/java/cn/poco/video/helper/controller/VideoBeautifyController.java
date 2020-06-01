package cn.poco.video.helper.controller;

/**
 * Created by Shine on 2017/6/15.
 */

//public class VideoBeautifyController {
//    private VideoEntry mVideoEntry;
//    private GLPlayView mVideoView;
//
//
//    public VideoBeautifyController(VideoEntry video, GLPlayView videoView) {
//        this.mVideoEntry = video;
//        this.mVideoView = videoView;
//    }
//
//    public void updateVideoEntryFilterRes(FilterRes filterRes) {
//        this.mVideoEntry.mVideoFilter.mFilterRes = filterRes;
//    }
//
//    /**
//     * 当视频的播放路径与原路径不一致的时候,更新视频播放的内容
//     * @return 是否改变了视频的播放内容
//     */
//    public boolean updateVideoView() {
//        boolean isUpdateVideo = false;
//        if (!mVideoEntry.mMediaPath.equals(mVideoEntry.mOriginPath)) {
//            isUpdateVideo = true;
//            this.mVideoView.reset();
//            List<PlayVideoInfo> videoInfos = new ArrayList<>();
//            videoInfos.add(DecodeUtils.getPlayVideoInfo(mVideoEntry.mMediaPath));
////            this.mVideoView.setVideoInfos(videoInfos);
//            this.mVideoView.prepare();
//        }
//        return isUpdateVideo;
//    }
//
//    /**
//     * 播放原始路径的视频,如果当前的播放路径和原始视频路径一致，则不用改变
//     */
//    public void displayOriginVideo() {
//        if (mVideoEntry.mVideoMemory != null) {
//            String origin = mVideoEntry.mOriginPath;
//            String current = mVideoEntry.mMediaPath;
//            if (!origin.equals(current)) {
//                this.mVideoView.reset();
//                List<PlayVideoInfo> videoInfos = new ArrayList<>();
//                videoInfos.add(DecodeUtils.getPlayVideoInfo(origin));
////                this.mVideoView.setVideoInfos(videoInfos);
//                this.mVideoView.prepare();
//            }
//        }
//    }
//
//
//    public void clear() {
//        mVideoView = null;
//        mVideoEntry = null;
//    }
//}
