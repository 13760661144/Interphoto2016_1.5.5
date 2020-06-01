package cn.poco.login.area;

import java.util.Comparator;

/**
 * Created by lgd on 2017/3/9.
 */
public class AlphabetComparator implements Comparator<SortModel>
{
	public int compare(SortModel o1, SortModel o2) {
		return o1.getEnName().compareTo(o2.getEnName());
	}
}
