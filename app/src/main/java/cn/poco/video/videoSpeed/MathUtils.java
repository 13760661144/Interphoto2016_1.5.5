package cn.poco.video.videoSpeed;

/**
 * Created by lgd on 2018/2/3.
 */

public class MathUtils
{
    //二分法查询最近值
    public static int binarySearchApproximate(int array[], int key, int left, int right)
    {
        if (key <= array[left])
            return left;
        else if (key >= array[right])
            return right;
        else
        {
            int mid;
            while (left <= right)  //循环结束left>right
            {
                mid = (left + right) / 2;
                if (key > array[mid])
                    left = mid + 1;
                else
                    right = mid - 1;
            }
            if (array[left] - key < key - array[right])//当最接近的数有2个时，输出值较小的一个
                return left;
            else
                return right;
        }
    }

    public static int binarySearch(int[] arr, int key, int fromIndex, int endIndex)
    {
        int low = fromIndex;
        int high = endIndex - 1;

        while (low <= high)
        {
            int mid = (low + high) / 2;
            int midVal = arr[mid];

            if (key > midVal)
            {
                low = mid + 1;
            } else if (key < midVal)
            {
                high = mid - 1;
            } else
            {
                return mid;//找到，返回该值索引
            }
        }
        return -1;//找不到，反回-1

    }
}
