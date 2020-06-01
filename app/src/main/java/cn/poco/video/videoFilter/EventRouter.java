package cn.poco.video.videoFilter;

import java.util.List;

/**
 * Created by Shine on 2017/6/8.
 */

public class EventRouter {
    public enum Event {
        OnBack(),
        OnResume(),
        OnPause();
    }

    public interface EventChain {
        void setNextChain(EventChain chain);
        boolean handleEvent(Event event);
    }


    private static volatile EventRouter sInstance = null;

    public static EventRouter getInstance() {
        EventRouter localInstance = sInstance;
        if (localInstance == null) {
            synchronized (EventRouter.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                     sInstance = localInstance = new EventRouter();
                }
            }
        }
        return localInstance;
    }

    private EventRouter() {

    }

    private List<EventChain> mChainList;
    public void initEventChain(List<EventChain> list) {
        mChainList = list;
        if (mChainList != null) {
            for (int i = 0; i < mChainList.size(); i++) {
                EventChain currentItem = mChainList.get(i);
                if (i < mChainList.size() - 1) {
                    currentItem.setNextChain(mChainList.get(i + 1));
                }
            }
        }
    }


    public void dispatchEvent(Event event) {
        EventChain topChain = mChainList.get(0);
        topChain.handleEvent(event);
    }

    public void clear() {
        mChainList.clear();
    }

}
