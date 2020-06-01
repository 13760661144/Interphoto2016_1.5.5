package cn.poco.video.page;

/**
 * Created by lgd on 2018/1/29.
 */

public enum ProcessMode {
    Normal("normal"),
    Fileter("filter"),
    Music("music"),
    Watermark("watermark"),
    Transition("transition"),
//    NONE("none"),
    Edit("edit"),
    CLIP("clip"),
    CANVASADJUST("canvasadjust"),
    FILTER("filter"),
    SPEEDRATE("speedrate"),
    SEGENTATION("segentation"),
    COPY("copy"),
    DELETE("delete");


    private String mode;

    ProcessMode(String mode) {
        this.mode = mode;
    }
}
