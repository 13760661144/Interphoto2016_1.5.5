package cn.poco.camera;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import cn.poco.beautify.BeautifyResMgr;
import cn.poco.draglistview.DragListItemInfo;

/**
 * Created by: fwc
 * Date: 2017/10/9
 * 获取滤镜列表，有可能会卡住线程
 */
public class GetFilterTask extends AsyncTask<Void, Void, ArrayList<DragListItemInfo>> {

	private WeakReference<OnGetFilterListener> mWeakReference;

	public GetFilterTask(OnGetFilterListener listener) {
		mWeakReference = new WeakReference<>(listener);
	}

	@Override
	protected ArrayList<DragListItemInfo> doInBackground(Void... params) {
		OnGetFilterListener listener = mWeakReference.get();
		if (listener != null) {
			return BeautifyResMgr.GetFilterRess(listener.getPageContext(), true);
		}

		return null;
	}

	@Override
	protected void onPostExecute(ArrayList<DragListItemInfo> dragListItemInfos) {
		OnGetFilterListener listener = mWeakReference.get();
		if (listener != null && dragListItemInfos != null) {
			listener.setFilterList(dragListItemInfos);
		}
	}

	public interface OnGetFilterListener {
		Context getPageContext();
		void setFilterList(ArrayList<DragListItemInfo> dragListItemInfos);
	}
}
