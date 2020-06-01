package my.Gif;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView.ScaleType;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class GifFrameData 
{
	protected ScaleType mScaleType = ScaleType.CENTER_INSIDE;
	protected int mMinTime = 50;
	protected int mIndex = 0;
	protected int mWidth = 165;
	protected int mHeight = 220;
	protected int mInterval = 150;
	protected boolean mUseCache = false;
	protected ArrayList<GifFrame> mFrames = new ArrayList<GifFrame>();
	protected Object mSyncSignal = new Object();
	protected boolean mReverse = false;
	protected boolean mCycle = false;
	protected int 	mDirection = 1;
	protected int	mQuality = 85;
	protected int	mRotation = 0;
	
	public GifFrameData() {

	}
	
	public GifFrameData(GifFrameData gifData) {
		mWidth = gifData.getWidth();
		mHeight = gifData.getHeight();
		mInterval = gifData.getInterval();
		mUseCache = gifData.mUseCache;
		mReverse = gifData.getReverse();
		mScaleType = gifData.getScaleType();
		mRotation = gifData.getRotation();
		int count = gifData.getFrameCount();
		synchronized(mSyncSignal)
		{
			for(int i = 0; i < count; i++)
			{
				mFrames.add(gifData.getFrame(i));
			}
		}
	}

	public GifFrameData(int width, int height, boolean cache) {
		mWidth = width;
		mHeight = height;
		mUseCache = cache;
	}
	
	public ScaleType getScaleType()
	{
		return mScaleType;
	}
	
	public void setScaleType(ScaleType type)
	{
		mScaleType = type;
	}

	public void addFrame(String pic, int time)
	{
		if(mInterval == 0)
			mInterval = time;
		if(pic == null)
			return;
		GifFrame frame = new GifFrame();
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pic, opts);
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = computeSimpleSize(opts.outWidth, opts.outHeight);
		Bitmap bitmap = BitmapFactory.decodeFile(pic, opts);
		makeGifFrame(frame, bitmap);
		frame.time = time;
		synchronized(mSyncSignal)
		{
			mFrames.add(frame);
		}
	}
	
	public void addFrame(byte[] jpgBytes, int time)
	{
		if(mInterval == 0)
			mInterval = time;
		if(jpgBytes == null)
			return;
		GifFrame frame = new GifFrame();
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.length, opts);
		opts.inJustDecodeBounds = false;
		opts.inSampleSize = computeSimpleSize(opts.outWidth, opts.outHeight);
		Bitmap bitmap = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.length, opts);
		makeGifFrame(frame, bitmap);
		frame.time = time;
		synchronized(mSyncSignal)
		{
			mFrames.add(frame);
		}
	}
	
	public void addFrame(Bitmap bitmap, int time)
	{
		if(mInterval == 0)
			mInterval = time;
		if(bitmap == null)
			return;
		GifFrame frame = new GifFrame();
		makeGifFrame(frame, bitmap);
		frame.time = time;
		synchronized(mSyncSignal)
		{
			mFrames.add(frame);
		}
	}
	
	public void addFrame(GifFrame frame)
	{
		synchronized(mSyncSignal)
		{
			mFrames.add(frame);
		}
	}
	
	protected int computeSimpleSize(int width, int height)
	{
		float r1 = (float) width / (float) height;
		float r2 = (float) mWidth / (float) mHeight;
		int sampleSize = 1;
		if(width > mWidth || height > mHeight)
		{
			if(mScaleType != ScaleType.CENTER_CROP)
			{
				if (r1 > r2) {
					sampleSize = width/mWidth;
				} else {
					sampleSize = height/mHeight;
				}
			}
			else
			{
				if (r1 > r2) {
					sampleSize = height/mHeight;
				} else {
					sampleSize = width/mWidth;
				}
			}
			if(sampleSize == 0)
				sampleSize = 1;
		}
		return sampleSize;
	}
	
	protected void makeGifFrame(GifFrame frame, Bitmap bitmap)
	{
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float r1 = (float) width / (float) height;
		float r2 = (float) mWidth / (float) mHeight;
		int w = mWidth, h = mHeight;
		if (width < mWidth && height < mHeight && mScaleType != ScaleType.CENTER_CROP) {
			w = width;
			h = height;
		} else {
			if(mScaleType == ScaleType.CENTER_CROP)
			{
				if (r1 < r2) {
					h = (int) ((float) w / r1);
				} else {
					w = (int) ((float) h * r1);
				}
			}
			else
			{
				if (r1 > r2) {
					h = (int) ((float) w / r1);
				} else {
					w = (int) ((float) h * r1);
				}
			}
		}

		Bitmap bmp = null;
		bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		canvas.drawBitmap(bitmap, null, new Rect(0, 0, w, h), null);

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(CompressFormat.JPEG, mQuality, stream);
		byte[] jpgBytes = stream.toByteArray();
		try {
			stream.close();
		} catch (IOException e) {
		}
		frame.bytes = jpgBytes;
		frame.width = w;
		frame.height = h;
		if (mUseCache == true) {
			frame.srcBytes = jpgBytes.clone();
		}
	}
	
	public void delFrame(int index)
	{
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			if(index < 0 || index >= count)
				return;
			mIndex = mIndex%(count-1);
			mFrames.remove(index);
		}
	}
	
	public void swapFrame(int from, int to)
	{
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			if(from >= 0 && from < count && to >= 0 && to < count)
			{
				GifFrame frame = mFrames.get(to);
				mFrames.set(to, mFrames.get(from));
				mFrames.set(from, frame);
			}
		}
	}
	
	public void insertFrame(int from, int to)
	{
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			if(from >= 0 && from < count && to >= 0 && to < count)
			{
				GifFrame frame = mFrames.get(from);
				mFrames.remove(from);
				if(to > from) to--; 
				mFrames.add(to, frame);
			}
		}
	}
	
	public void insertFrame(GifFrame gifFrame, int index)
	{
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			if(index >= 0 && index < count)
			{
				mFrames.add(index, gifFrame);
			}
		}
	}
	
	public void setQuality(int quality)
	{
		mQuality = quality;
	}
	
	public void setSize(int w, int h)
	{
		mWidth = w;
		mHeight = h;
	}
	
	public void clear()
	{
		synchronized(mSyncSignal)
		{
			mIndex = 0;
			mFrames.clear();
		}
	}
	
	public int getRotation()
	{
		return mRotation;
	}
	
	public void leftRotate()
	{
		mRotation -= 90;
		mRotation = mRotation%360;
		int temp = mWidth;
		mWidth = mHeight;
		mHeight = temp;
		GifFrame frame = null;
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			for(int i = 0; i < count; i++)
			{
				frame = mFrames.get(i);
				temp = frame.width;
				frame.width = frame.height;
				frame.height = temp;
			}
		}
	}
	
	public void rightRotate()
	{
		mRotation += 90;
		mRotation = mRotation%360;
		int temp = mWidth;
		mWidth = mHeight;
		mHeight = temp;
		GifFrame frame = null;
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			for(int i = 0; i < count; i++)
			{
				frame = mFrames.get(i);
				temp = frame.width;
				frame.width = frame.height;
				frame.height = temp;
			}
		}
	}
	
	public void setCycle(boolean cycle)
	{
		mCycle = cycle;
	}
	
	public boolean getCycle()
	{
		return mCycle;
	}
	
	public void useCache(boolean cache)
	{
		mUseCache = cache;
	}
	
	public int getDirection()
	{
		return mDirection;
	}
	
	public int getFrameCount()
	{
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			return count;
		}
	}
	
	public GifFrame getFrame(int index)
	{
		synchronized(mSyncSignal)
		{
			if(index >= mFrames.size())
				return null;
			return mFrames.get(index);
		}
	}
	
	public void setReverse(boolean reverse)
	{
		if(reverse != mReverse)
		{
			synchronized(mSyncSignal)
			{
				int count = mFrames.size();
				if(count == 0)
					return;
				ArrayList<GifFrame> frames = new ArrayList<GifFrame>();
				for(int i = count-1; i >= 0; i--)
				{
					frames.add(mFrames.get(i));
				}
				mIndex = count-mIndex-1;
				mFrames = frames;
			}
		}
		mReverse = reverse;
	}
	
	public boolean getReverse()
	{
		return mReverse;
	}
	
	public void setInterval(int interval)
	{
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			if(count == 0)
				return;
			if(interval < mMinTime)
				interval = mMinTime;
			for(int i = 0; i < count; i++)
			{
				GifFrame frame = mFrames.get(i);
				if(frame != null)
				{
					frame.time  = interval;
				}
			}
		}
		mInterval = interval;
	}
	
	public int getInterval()
	{
		return mInterval;
	}
	
	public int getMinInterval()
	{
		return mMinTime;
	}
	
	public void setMinInterval(int interval)
	{
		mMinTime = interval;
	}
	
	public int getWidth()
	{
		return mWidth;
	}
	
	public int getHeight()
	{
		return mHeight;
	}
	
	public int getFrameIndex()
	{
		return mIndex;
	}
	
	public void gotoFrame(int index)
	{
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			if(count == 0)
				return;
			mIndex = index%count;
		}
	}
	
	public GifFrame nextFrame()
	{
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			if(count == 0)
				return null;
			if(mCycle == false)
			{
				mIndex = (mIndex+1)%count;
			}
			else
			{
				if(mIndex == count-1)
					mDirection = -1;
				if(mIndex == 0)
					mDirection = 1;
				mIndex = (mIndex+mDirection)%count;
			}
			return mFrames.get(mIndex);
		}
	}
	
	public GifFrame lastFrame()
	{
		synchronized(mSyncSignal)
		{
			int count = mFrames.size();
			if(count == 0)
				return null;
			if(mCycle == false)
			{
				mIndex = (mIndex+count-1)%count;
			}
			else
			{
				if(mIndex == count-1)
					mDirection = -1;
				if(mIndex == 0)
					mDirection = 1;
				mIndex = (mIndex+mDirection)%count;
			}
			return mFrames.get(mIndex);
		}
	}
	
	public void saveGifData(String file) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		out.writeByte('G');
		out.writeByte('D');
		out.writeByte('T');
		out.writeInt(mInterval);
		out.writeInt(mWidth);
		out.writeInt(mHeight);
		out.writeBoolean(mUseCache);
		out.writeBoolean(mReverse);
		out.writeObject(mScaleType);
		out.writeInt(mRotation);
		int count = mFrames.size();
		GifFrame frame;
		for(int i = 0; i < count; i++)
		{
			out.writeShort(0x2b);
			frame = mFrames.get(i);
			out.writeInt(frame.width);
			out.writeInt(frame.height);
			out.writeInt(frame.time);
			if(frame.bytes != null)
			{
				out.writeShort(0x21);
				out.writeInt(frame.bytes.length);
				out.write(frame.bytes);
			}
			if(frame.srcBytes != null)
			{
				out.writeShort(0x21);
				out.writeInt(frame.srcBytes.length);
				out.write(frame.srcBytes);
			}
			out.flush();
		}
		out.writeShort(0x00);
		out.close();
		fos.close();
	}
	
	public void readGifData(String file)
	{
		short tag = 0;
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fis);
			if('G' != in.readByte())
			{
				in.close();
				fis.close();
				return;
			}
			if('D' != in.readByte())
			{
				in.close();
				fis.close();
				return;
			}
			if('T' != in.readByte())
			{
				in.close();
				fis.close();
				return;
			}
			mInterval = in.readInt();
			mWidth = in.readInt();
			mHeight = in.readInt();
			mUseCache = in.readBoolean();
			mReverse = in.readBoolean();
			mScaleType = (ScaleType)in.readObject();
			mRotation = in.readInt();
			synchronized(mSyncSignal)
			{
				mFrames.clear();
			}
			int len = 0;
			byte[] bytes = null;
			GifFrame frame;
			tag = in.readShort();
			while(tag == 0x2b)
			{
				frame = new GifFrame();
				frame.width = in.readInt();
				frame.height = in.readInt();
				frame.time = in.readInt();
				tag = in.readShort();
				if(tag == 0x21)
				{
					len = in.readInt();
					bytes = new byte[len];
					int read = 0;
					int offset = 0;
					while((read = in.read(bytes, offset, len)) != -1)
					{
						offset += read;
						len -= read;
						if(len == 0)
							break;
					}
					frame.bytes = bytes;
					tag = in.readShort();
				}
				else if(tag == 0x2b)
				{
					continue;
				}
				if(tag == 0x21)
				{
					len = in.readInt();
					bytes = new byte[len];
					int read = 0;
					int offset = 0;
					while((read = in.read(bytes, offset, len)) != -1)
					{
						offset += read;
						len -= read;
						if(len == 0)
							break;
					}
					frame.srcBytes = bytes;
					tag = in.readShort();
				}
				synchronized(mSyncSignal)
				{
					mFrames.add(frame);
				}
			}
			in.close();
			fis.close();
		} catch (Exception e) {
		}
	}
	
	public void drawFrame(View view, Canvas canvas, GifFrame frame)
	{
		if(frame == null)
			return;
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
		int pl = view.getPaddingLeft();
		int pt = view.getPaddingTop();
		int pr = view.getPaddingRight();
		int pb = view.getPaddingBottom();
		int sw = view.getWidth();
		int sh = view.getHeight();
		sw = sw - pr - pl;
		sh = sh - pt - pb;
		if(sw <= 0 || sh <= 0)
			return;
		//计算gif绘制区域
		float r1 = (float) sw / (float) sh;
		float r2 = (float) mWidth / (float) mHeight;
		int w = sw, h = sh, x = 0, y = 0;
		if(mWidth < sw && mHeight < sh)
		{
			w = mWidth;
			h = mHeight;
		}
		else
		{
			if (r1 > r2) {
				w = (int) (h * r2);
			} else {
				h = (int) (w / r2);
			}
		}
		x = (sw - w) / 2;
		y = (sh - h) / 2;
		//计算图片在绘制区的相对位置
		if(frame.width != mWidth || frame.height != mHeight)
		{
			w = w*frame.width/mWidth;
			h = w*frame.height/frame.width;
			x = (sw-w)/2;
			y = (sh-h)/2;
		}
		Bitmap bitmap = BitmapFactory.decodeByteArray(frame.bytes, 0, frame.bytes.length);
		canvas.drawBitmap(bitmap, null, new Rect(x, y, x+w, y+h), null);
	}
}
