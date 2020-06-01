package cn.poco.video.save.decoder;

import java.nio.ByteBuffer;

/**
 * Created by: fwc
 * Date: 2017/12/26
 */
public interface IDecoder extends Runnable {

	BufferInfo getBufferInfo() throws InterruptedException;

	void recycle(BufferInfo info);

	void release();

	class BufferInfo {

		/**
		 * 视频帧数据
		 */
		public ByteBuffer byteBuffer;

		/**
		 * 视频宽
		 */
		public int width;

		/**
		 * 视频高
		 */
		public int height;

		/**
		 * 时间戳，单位微秒
		 */
		public long timestamp;
	}
}
