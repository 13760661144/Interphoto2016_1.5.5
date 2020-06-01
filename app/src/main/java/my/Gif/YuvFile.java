package my.Gif;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import cn.poco.utils.JniUtils;

public class YuvFile 
{
	private int CACHE_SIZE = 4;
	private int mWritedCount = 0;
	private int mPushedCount = 0;
	private String mSaveFile;
	private Exception mException;
	private Object mFileUsingSign = new Object();
	private ArrayList<YuvData> mYuvData = new ArrayList<YuvData>();
	
	public YuvFile(String file)
	{
		mSaveFile = file;
		mException = null;
		File f = new File(file);
		if(f.exists() == true)
		{
			f.delete();
		}
	}
	
	public YuvFile(Context context)
	{
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) == false) {
			File file = context.getDir("yuvcache", Context.MODE_PRIVATE);
			if(file != null)
			{
				mSaveFile = file.getAbsolutePath()+"/cache.yuv";
			}
		}
		else
		{
			File file = Environment.getExternalStorageDirectory();
			if(file != null)
			{
				mSaveFile = file.getAbsolutePath()+"/PocoCamera/appdata/cache";
				file = new File(mSaveFile);
				if(!file.exists())
				{
					file.mkdirs();
				}
				mSaveFile += "/cache.yuv";
			}
		}
		mException = null;
		File f = new File(mSaveFile);
		if(f.exists() == true)
		{
			f.delete();
		}
	}
	
	public void setFile(String file)
	{
		mSaveFile = file;
	}
	
	public boolean pushData(byte[] data, int width, int height, int rotation) throws Exception
	{
		if(data == null)
		{
			return false;
		}
		if(mException != null)
		{
			Exception e = mException;
			mException = null;
			throw e;
		}
		synchronized(mYuvData)
		{
			if(mYuvData.size() < CACHE_SIZE)
			{
//				PLog.out("push");
				mYuvData.add(new YuvData(data, width, height, rotation));
				if(mSaveThread == null)
				{
					mSaveThread = new Thread(mSaveProc);
					mSaveThread.start();
				}
				mPushedCount++;
				return true;
			}
			else
			{
//				PLog.out("wait");
			}
		}
		return false;
	}
	
	private Thread mSaveThread;
	private Runnable mSaveProc = new Runnable()
	{
		@Override
		public void run() {
			synchronized(mFileUsingSign)
			{
				while(getCount() > 0)
				{
					YuvData data = null;
					synchronized(mYuvData)
					{
						if(mYuvData.size() > 0)
						{
							data = mYuvData.get(0);
							mYuvData.remove(0);
						}
					}
					if(mSaveFile != null && data != null)
					{
						try
						{
							int count = data.data.length;
							int width = data.width;
							int height = data.height;
							int rotation = data.rotation;
							FileOutputStream fos = new FileOutputStream(mSaveFile, true);
							fos.write((width&0xff00)>>8);
							fos.write(width&0x00ff);
						
							fos.write((height&0xff00)>>8);
							fos.write(height&0x00ff);
							
							fos.write((rotation&0xff00)>>8);
							fos.write(rotation&0x00ff);
						
							fos.write((count&0xff000000)>>24);
							fos.write((count&0x00ff0000)>>16);
							fos.write((count&0x0000ff00)>>8);
							fos.write(count&0x000000ff);
						
							fos.write(data.data);
							fos.close();
							mWritedCount++;
						}
						catch(Exception e)
						{
							synchronized(mYuvData)
							{
								mYuvData.clear();
							}
							mException = e;
						}
					}
				}
			}
			mSaveThread = null;
		}
	};
	
	public void readData(OnReadYuvListener l)
	{
		try
		{
			synchronized(mFileUsingSign)
			{
				int width = 0;
				int height = 0;
				int count = 0;
				int rotation = 0;
				int len = 0;
				int oneByte;
				int read = 0;
				int readSize = 0;
				FileInputStream fis = new FileInputStream(mSaveFile);
				while(count < mWritedCount)
				{
					width = 0;
					oneByte = fis.read();
					if(oneByte == -1)
						break;
					width += oneByte<<8;
					oneByte = fis.read();
					width += oneByte;
					
					height = 0;
					oneByte = fis.read();
					height += oneByte<<8;
					oneByte = fis.read();
					height += oneByte;
					
					rotation = 0;
					oneByte = fis.read();
					rotation += oneByte<<8;
					oneByte = fis.read();
					rotation += oneByte;
					
					len = 0;
					oneByte = fis.read();
					len += oneByte<<24;
					oneByte = fis.read();
					len += oneByte<<16;
					oneByte = fis.read();
					len += oneByte<<8;
					oneByte = fis.read();
					len += oneByte;
					
					readSize = 0;
					byte[] data = new byte[len];
					while((read = fis.read(data, readSize, len-readSize)) != -1)
					{
						readSize += read;
						if(readSize >= len)
						{
							break;
						}
					}
					if(l != null)
					{
						int[] pixels = new int[width*height];
						JniUtils.yuv2rgb(width, height, pixels.length, data, pixels);
						/*if(cUtils.get_machine_mode().indexOf("kftt")!=-1)
						{	//亚马逊的要做垂直翻转
					   	   	pixels = cUtils.kftt_fix_gif_or(pixels,width,height);
						}*/
						switch(rotation%360)
						{
						case 180:
						case -180:
							JniUtils.reversePixels(pixels, height, width);
							break;
						}
						data = null;
						Bitmap bitmap = Bitmap.createBitmap(pixels, height, width, Config.ARGB_8888);
						pixels = null;
						l.onRead(bitmap, count);
					}
					if(read == -1)
						break;
					count++;
				}
				fis.close();
			}
		}
		catch(Exception e)
		{
		}
	}
	
	public void clear()
	{
		File file = new File(mSaveFile);
		if(file.exists())
		{
			file.delete();
		}
		mWritedCount = 0;
		mPushedCount = 0;
		mException = null;
		synchronized(mYuvData)
		{
			mYuvData.clear();
		}
	}
	
	public int getPushedCount()
	{
		return mPushedCount;
	}
	
	public int getWritedCount()
	{
		return mWritedCount;
	}
	
	private int getCount()
	{
		synchronized(mYuvData)
		{
			return mYuvData.size();
		}
	}
	
	private class YuvData
	{
		public YuvData(byte[] data, int width, int height, int rotation)
		{
			this.data = data;
			this.width = width;
			this.height = height;
			this.rotation = rotation;
		}
		byte[] data;
		int width;
		int height;
		int rotation = 0;
	}
	
	public interface OnReadYuvListener
	{
		void onRead(Bitmap bmp, int index);
	}
}