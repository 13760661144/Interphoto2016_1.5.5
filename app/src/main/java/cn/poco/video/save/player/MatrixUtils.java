package cn.poco.video.save.player;

import android.opengl.Matrix;

/**
 * Created by: fwc
 * Date: 2017/9/27
 */
public class MatrixUtils {

	private static float[] sTempMatrix = new float[32];

	/**
	 * 针对纹理矩阵做的旋转
	 * @param rm 矩阵
	 * @param rmOffset 矩阵偏移
	 * @param degree 旋转角度
	 */
	public static void rotateM(float[] rm, int rmOffset, int degree) {
		degree = (degree + 360) % 360;

		if (degree == 0) {
			return;
		}

		Matrix.setIdentityM(sTempMatrix, 0);
		switch (degree) {
			case 90:
				sTempMatrix[0] = 0;
				sTempMatrix[1] = 1;
				sTempMatrix[4] = -1;
				sTempMatrix[5] = 0;
				sTempMatrix[12] = 1;
				sTempMatrix[13] = 0;
				break;
			case 180:
				sTempMatrix[0] = -1;
				sTempMatrix[1] = 0;
				sTempMatrix[4] = 0;
				sTempMatrix[5] = -1;
				sTempMatrix[12] = 1;
				sTempMatrix[13] = 1;
				break;
			case 270:
				sTempMatrix[0] = 0;
				sTempMatrix[1] = -1;
				sTempMatrix[4] = 1;
				sTempMatrix[5] = 0;
				sTempMatrix[12] = 0;
				sTempMatrix[13] = 1;
				break;
		}

		Matrix.multiplyMM(sTempMatrix, 16, rm, rmOffset, sTempMatrix, 0);
		System.arraycopy(sTempMatrix, 16, rm, rmOffset, 16);
	}
}
