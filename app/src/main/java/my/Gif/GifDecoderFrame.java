package my.Gif;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

public class GifDecoderFrame {
	public byte[] jpgBytes;
	public Bitmap image;
	public int delay;
	public GifDecoderFrame nextFrame = null;
	
	public GifDecoderFrame(Bitmap im, int del) {
		image = im;
		delay = del;
	}
	
	public GifDecoderFrame(byte[] bytes, int del) {
		jpgBytes = bytes;
		delay = del;
	}
	
	public Bitmap getImage()
	{
		if(image != null)
			return image;
		if(jpgBytes != null)
		{
			Options opts = new Options();
			opts.inPreferredConfig = Config.ARGB_8888;
			return BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.length, opts);
		}
		return null;
	}
}
