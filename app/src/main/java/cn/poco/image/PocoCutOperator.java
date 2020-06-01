package cn.poco.image;

/*
 * 如果是横向图（例如w:h = 4:3），使用left, right, leftright直接返回原图，应避免；
 * 如果是竖向图（例如w:h = 3:4），使用top, bottom, topbottom直接返回原图，应避免；
 * 
 */

public class PocoCutOperator 
{
	public static final int left=1;						//减左
	public static final int right=2;					//减右
	public static final int top=3;						//减上
	public static final int bottom=4;					//减下
	public static final int leftRight=5;				//减左减右
	public static final int topBottom=6;				//减上减下
	public static final int snapLeft=7;					//左对齐
	public static final int snapTop=8;					//上对齐
	public static final int snapRight=9;				//右对齐
	public static final int snapBottom=10;				//下对齐
	public static final int middle=11;					//居中
	public static final int stretching=12;				//按尺寸拉伸
//lefttop, tighttop, leftbottom, rightbottom属性不能给底层接口调用。	
	public static final int lefttop = 13;
	public static final int righttop = 14;
	public static final int leftbottom = 15;
	public static final int rightbottom = 16;
}
