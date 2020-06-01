package cn.poco.business;

public class ActInputInfo {
	public static final int INPUTTYPE_TEXT = 1;
	public static final int INPUTTYPE_NUMBER = 2;
	public static final int INPUTTYPE_SINGLESEL = 3;
	public static final int INPUTTYPE_MULTISEL = 4;
	public static final int INPUTTYPE_PASSWORD = 5;
	public static final int INPUTTYPE_COMBOBOX = 6;
	
	public int inputType;
	public int maxLength;
	public int minLength;
	public boolean important;
	public String name;
	public String describle;
	public String type;
	public String defaultValue; 
	public String options;
}
