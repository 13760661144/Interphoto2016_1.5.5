package cn.poco.album2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by: fwc
 * Date: 2017/3/8
 */
public class MemoryInfo {

	public static long sdCardSize = -1;

	public static long sdAvailableSize = -1;

	public static long interPhotoSize = -1;

	private static List<OnInfoChangeListener> sOnInfoChangeListeners = new ArrayList<>();

	public static void addOnInfoChangeListener(OnInfoChangeListener listener) {
		if (listener != null) {
			sOnInfoChangeListeners.add(listener);
		}
	}

	public static void removeOnInfoChangeListener(OnInfoChangeListener listener) {
		if (listener != null) {
			sOnInfoChangeListeners.remove(listener);
		}
	}

	public static void notifyChange() {
		for (OnInfoChangeListener listener : sOnInfoChangeListeners) {
			if (listener != null) {
				listener.onChange();
			}
		}
	}

	public static void clear() {
		sdCardSize = -1;
		sdAvailableSize = -1;
		interPhotoSize = -1;

		sOnInfoChangeListeners.clear();
	}

	public interface OnInfoChangeListener {
		void onChange();
	}
}
