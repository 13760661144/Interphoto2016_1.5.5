package cn.poco.video.videoFeature.data;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by lgd on 2018/1/19.
 * <p>
 * 速率页面的数据记录
 */

public class VideoSpeedInfo
{
    private HashMap<Float, String> reverseSpeeds = new HashMap<>(); //key是速度， String 是视屏相册的原始路径
    private HashMap<Float, String> originalSpeeds = new HashMap<>();//key是速度， String 是视屏相册的原始路径的倒序

    private HashSet<String> pathUsed = new HashSet<>();  //外键是上面两个,  防止被删除

    public String getVideoPath(boolean isReverse, float speed)
    {
        if (isReverse)
        {
            return reverseSpeeds.get(speed);
        } else
        {
            return originalSpeeds.get(speed);

        }
    }
//
    public void resetUsedPath(List<String> videoPaths)
    {
        pathUsed.clear();
        for (int i = 0; i < videoPaths.size(); i++)
        {
            pathUsed.add(videoPaths.get(i));
        }
    }

    public void putUsedPath(String path)
    {
        pathUsed.add(path);
    }

    public void putVideoPath(boolean isReverse, float speed, String videoPath)
    {
        if (isReverse)
        {
            reverseSpeeds.put(speed, videoPath);
        } else
        {
            if (speed == 1)
            {
                pathUsed.add(videoPath);
            }
            originalSpeeds.put(speed, videoPath);
        }
    }

    /**
     * 删除除了原视频的其他文件
     */
    public void deleteAllTempFile(String exceptPath)
    {
        HashMap<Float, String> temp = (HashMap<Float, String>) originalSpeeds.clone();
        Iterator iterator = temp.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            float key = (float) entry.getKey();
            String path = (String) entry.getValue();
            if (!path.equals(exceptPath))
            {
                new File(path).delete();
                originalSpeeds.remove(key);
            }
        }
        temp = (HashMap<Float, String>) reverseSpeeds.clone();
        iterator = temp.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            float key = (float) entry.getKey();
            String path = (String) entry.getValue();
            if (!path.equals(exceptPath))
            {
                new File(path).delete();
                reverseSpeeds.remove(key);
            }
        }
    }

    /**
     * 删除除了速度1和被使用文件的其他文件
     */
    public void deleteCacheFile()
    {
        HashMap<Float, String> temp = (HashMap<Float, String>) originalSpeeds.clone();
        Iterator iterator = temp.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            float key = (float) entry.getKey();
            String path = (String) entry.getValue();
            if (pathUsed.contains(path))
            {
                continue;
            }
            if (key == 1)
            {
                continue;
            }
            new File(path).delete();
            originalSpeeds.remove(key);
        }
        temp = (HashMap<Float, String>) reverseSpeeds.clone();
        iterator = temp.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry entry = (Map.Entry) iterator.next();
            float key = (float) entry.getKey();
            String path = (String) entry.getValue();
            if (pathUsed.contains(path))
            {
                continue;
            }
            if (key == 1)
            {
                continue;
            }
            new File(path).delete();
            reverseSpeeds.remove(key);
        }
    }

    /**
     * 删除除了原视频的其他文件
     */
    public void deleteAllTempFile(boolean curIsReverse, float curSpeed)
    {
        String path = getVideoPath(curIsReverse, curSpeed);
        deleteAllTempFile(path);
    }
}
