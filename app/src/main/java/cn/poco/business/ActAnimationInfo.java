package cn.poco.business;

import java.util.ArrayList;


public class ActAnimationInfo
{
	public static class ActAnimationFrame
	{
		public Object img;
		public int time = 100;
		public boolean stop = false;

		public String url_img;
	}

	public String align = "center";
	public String place;
	public ArrayList<ActAnimationFrame> frames = new ArrayList<ActAnimationFrame>();
}
