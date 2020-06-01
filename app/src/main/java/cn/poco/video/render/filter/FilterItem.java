package cn.poco.video.render.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import cn.poco.album2.utils.FileUtils;
import cn.poco.resource.FilterComposeInfo;
import cn.poco.resource.FilterRes;
import cn.poco.utils.FileUtil;
import cn.poco.utils.Utils;

/**
 * Created by: fwc
 * Date: 2017/12/15
 */
public class FilterItem {

	public static final int TYPE_NONE = 0;
	public static final int TYPE_LOOKUP_TABLE = 1;
	public static final int TYPE_BLEND = 2;
	public static final int TYPE_BOTH = 3;

	public int type;

	/**
	 * 不透明度，0.0 ~ 1.0
	 */
	public float resultAlpha;

	public String tableResPath;
	public boolean isBlackWhite = false;
	public Bitmap tableRes;

	public String[] blendResPaths;
	public Bitmap[] blendRes;

	public int[] blendComOps;
	public float[] blendAlphas;

	public FilterRes filterRes;
	public int filterUri;

	private FilterItem() {

	}

	public FilterItem(Context context, String tableResPath, float resultAlpha) {
		type = TYPE_LOOKUP_TABLE;

		this.tableResPath = tableResPath;
		this.resultAlpha = resultAlpha;

		createResource(context);
	}

	public FilterItem(Context context, String[] blendResPaths, int[] blendComOps, float[] blendAlphas, float resultAlpha) {
		type = TYPE_BLEND;

		this.blendResPaths = blendResPaths;
		this.blendComOps = blendComOps;
		this.blendAlphas = blendAlphas;
		this.resultAlpha = resultAlpha;

		createResource(context);
	}

	public FilterItem(Context context, String tableResPath, String[] blendResPaths, int[] blendComOps,
					  float[] blendAlphas, float resultAlpha) {
		type = TYPE_BOTH;

		this.tableResPath = tableResPath;
		this.blendResPaths = blendResPaths;
		this.blendComOps = blendComOps;
		this.blendAlphas = blendAlphas;
		this.resultAlpha = resultAlpha;

		createResource(context);
	}

	/**
	 * 创建资源
	 */
	private void createResource(Context context) {

		if ((type & TYPE_LOOKUP_TABLE) != 0) {
			tableRes = getBitmap(context, tableResPath);
		}

		if ((type & TYPE_BLEND) != 0) {
			blendRes = new Bitmap[blendResPaths.length];
			for (int i = 0; i < blendResPaths.length; i++) {
				blendRes[i] = getBitmap(context, blendResPaths[i]);
			}
		}
	}

	private static Bitmap getBitmap(Context context, String path) {

		// 本地路径
		if (FileUtil.isFileExists(path)) {
			return Utils.DecodeImage(context, path, 0, -1, -1, -1);
		}

		// assets文件夹
		InputStream inputStream = null;

		try {
			inputStream = context.getAssets().open(path);
			return BitmapFactory.decodeStream(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileUtils.close(inputStream);
		}
		return null;
	}

	/**
	 * 释放资源
	 */
	public void releaseResource() {
		if (tableRes != null && !tableRes.isRecycled()) {
			tableRes.recycle();
			tableRes = null;
		}

		if (blendRes != null) {
			for (Bitmap res : blendRes) {
				if (res != null && !res.isRecycled()) {
					res.recycle();
				}
			}
			Arrays.fill(blendRes, null);
			blendRes = null;
		}
	}

	/**
	 * 将FilterRes对象转换成FilterItem对象
	 *
	 * @param filterRes FilterRes对象
	 * @return FilterItem对象
	 */
	public static FilterItem wrap(Context context, FilterRes filterRes, int uri) {

		FilterItem filterItem = null;

		if (filterRes != null) {
			float alpha = filterRes.m_alpha / 100f;
			switch (filterRes.m_filterType) {
				case 0:
					// 查表
					filterItem = new FilterItem(context, filterRes.m_tablePic, alpha);
					break;
				case 1: {
					// 混合
					int size = filterRes.m_compose.length;
					String[] blendResPaths = new String[size];
					int[] blendComOps = new int[size];
					float[] blendAlphas = new float[size];
					FilterComposeInfo info;
					for (int i = 0; i < size; i++) {
						info = filterRes.m_compose[i];
						blendResPaths[i] = info.blend_pic;
						blendComOps[i] = info.blend_type;
						blendAlphas[i] = info.blend_alpha;
					}
					filterItem = new FilterItem(context, blendResPaths, blendComOps, blendAlphas, alpha);
					break;
				}
				case 2: {
					// 查表+混合
					int size = filterRes.m_compose.length;
					String[] blendResPaths = new String[size];
					int[] blendComOps = new int[size];
					float[] blendAlphas = new float[size];
					FilterComposeInfo info;
					for (int i = 0; i < size; i++) {
						info = filterRes.m_compose[i];
						blendResPaths[i] = info.blend_pic;
						blendComOps[i] = info.blend_type;
						blendAlphas[i] = info.blend_alpha;
					}
					filterItem = new FilterItem(context, filterRes.m_tablePic, blendResPaths, blendComOps, blendAlphas, alpha);
					break;
				}
			}
		}

		if (filterItem != null) {
			filterItem.filterRes = filterRes;
			filterItem.filterUri = uri;
		}

		if (filterItem != null && ("一刻".equals(filterRes.m_name) || "Super Stone".equals(filterRes.m_name) ||
				"Leslie".equals(filterRes.m_name) || "独白".equals(filterRes.m_name))) {
			filterItem.isBlackWhite = true;
		}
		return filterItem;
	}
}
