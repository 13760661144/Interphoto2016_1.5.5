package cn.poco.image;

public class PocoImageInfo 
{
	public static class ChannelType
	{
		public static int UndefinedChannel = 0x0000;
		public static int RedChannel = 0x0001;
		public static int GrayChannel = 0x0001;
		public static int CyanChannel = 0x0001;
		public static int GreenChannel = 0x0002;
		public static int MagentaChannel = 0x0002;
		public static int BlueChannel = 0x0004;
		public static int YellowChannel = 0x0004;
		public static int  AlphaChannel = 0x0008;
		public static int  OpacityChannel = 0x0008;
		public static int  MatteChannel = 0x0008;  /* deprecated */
		public static int  BlackChannel = 0x0020;
		public static int  IndexChannel = 0x0020;
		public static int  AllChannels = 0xff;
		public static int  DefaultChannels = (AllChannels &~ OpacityChannel);
	
	}
		

}
