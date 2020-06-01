package cn.poco.resource.database;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by admin on 2017/4/20.
 */

public class ResourseDatabaseHelper extends SQLiteOpenHelper
{
	private final static String TAG = "ResourseDatabaseHelper";
	public final static String DB_NAME = "Resourse.db";//数据库名
	private final static int VERSION = 4;//版本号

	public ResourseDatabaseHelper(Context context)
	{
		this(context, DB_NAME, null, VERSION);
	}

	public ResourseDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
	{
		super(context, name, factory, version);
	}

	public ResourseDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler)
	{
		super(context, name, factory, version, errorHandler);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		try
		{
			//创建表
			onCreateThemeTable(db);
			onCreateFilterTable(db);
			onCreateTextTable(db);
			onCreateLightEffectTable(db);

			onCreateMusicTable(db);
			onCreateVideoTextTable(db);

//			Log.i(TAG, "创建数据库表成功");
		}
		catch(SQLException se)
		{
			se.printStackTrace();
//			Log.i(TAG, "创建数据库表失败");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		try
		{
			//创建表
			if(newVersion > oldVersion && newVersion == 3)
			{
				onCreateMusicTable(db);
				onCreateVideoTextTable(db);
				onUpdateFilterTable(db);
			}

			if(newVersion > oldVersion && newVersion == 4)
			{
				onDropThemeTable(db);
				onCreateThemeTable(db);
			}
		}
		catch(SQLException se)
		{
			se.printStackTrace();
		}

	}

	/**
	 * 创建滤镜表
	 * @param db
	 */
	private void onCreateFilterTable(SQLiteDatabase db)
	{
		String createStr = "create table if not exists " + TableNames.FILTER + "(" +
				"color_ID integer unique," +
				"color_name text," +
				"color_coverImage text," +
				"color_thumbnailImage text," +
				"color_alpha integer," +
				"color_coverColor text," +
				"color_locationType integer," +
				"color_location integer," +
				"color_statisticalID integer," +
				"color_masterType integer," +
				"color_authorImage text," +
				"color_introductionAuthor text," +
				"color_introductionAuthorTitle text," +
				"color_introductionDetail text," +
				"color_introductionURL text," +
				"color_unlockWeixinImage text," +
				"color_unlockWeixinTitle text," +
				"share_link text," +
				"detail text," +
				"is_hide integer," +
				"store_type integer," +		//存储方式（内置 1 、sd卡 2）
				/*****中间这些字段都是在version=3的时候添加的******/
				"table_pic text," +		//查表所需图片路径
				"blend_info text," +	//混合信息（json数组）
				"filter_type integer," +	//类型
				/***********/
				"PRIMARY KEY(color_ID)" +
				")";
		db.execSQL(createStr);
	}

	private void onUpdateFilterTable(SQLiteDatabase db)
	{
		String sqlStr = "alter table filter add column table_pic text";
		db.execSQL(sqlStr);
		sqlStr = "alter table filter add column blend_info text";
		db.execSQL(sqlStr);
		sqlStr = "alter table filter add column filter_type integer";
		db.execSQL(sqlStr);
	}

	private void onDropThemeTable(SQLiteDatabase db)
	{
		String sqlStr = "drop table " + TableNames.THEME;
		db.execSQL(sqlStr);
	}

	/**
	 * 创建主题表
	 * @param db
	 */
	private void onCreateThemeTable(SQLiteDatabase db)
	{
		String createStr = "create table if not exists " + TableNames.THEME + "(" +
				"id integer unique," +
				"name text," +
				"subtitle text," +
				"detail text," +
				"tj_id integer," +
				"icon_200 text," +
				"icon text," +
				"version text," +
				"share_title text," +
				"dashi_type text," +
				"dashi_name text," +
				"dashi_rank text," +
				"share_link text," +
				"dashi_icon text," +
				"title_color text," +
				"tj_link text," +
				"show_id integer," +
				"'order' integer," +
				"is_hide integer," +
				"is_business integer," +
				"text text," +
				"filter text," +
				"light_effect text," +
				"music text," +
				"watermark text," +
				"recoment integer," +
				"store_type integer," +		//存储方式（内置 1 、sd卡 2）
				"PRIMARY KEY(id)" +
				")";
		db.execSQL(createStr);
	}

	/**
	 * 创建文字表
	 * @param db
	 */
	private void onCreateTextTable(SQLiteDatabase db)
	{
		String createStr = "create table if not exists " + TableNames.TEXT + "(" +
				"id integer unique," +
				"editable integer," +
				"restype_id integer," +
				"restype text," +
				"'order' integer," +
				"title_color text," +
				"thumb_120 text," +
				"tracking_code integer," +
				"res_arr text," +
				"image_zip text," +
				"type text," +
				"previewTitle text," +
				"head_link text," +
				"head_img text," +
				"cover_pic text," +
				"pic text," +
				"align text," +
				"margin_x text," +
				"margin_y text," +
				"is_hide integer," +
				"store_type integer," +		//存储方式（内置 1 、sd卡 2）
				"PRIMARY KEY(id)" +
				")";
		db.execSQL(createStr);
	}

	/**
	 * 创建光效表
	 * @param db
	 */
	private void onCreateLightEffectTable(SQLiteDatabase db)
	{
		String createStr = "create table if not exists " + TableNames.LIGHT_EFFECT + "(" +
				"id integer unique," +
				"name text," +
				"class text," +
				"size integer," +
				"thumb_120 text," +
				"cover_pic text," +
				"type integer," +
				"tracking_code integer," +
				"location text," +
				"compose integer," +
				"color text," +
				"lockType text," +
				"lockIntroduce text," +
				"lockPage text," +
				"iamge_info text," +
				"scale float," +
				"min_scale float," +
				"max_scale float," +
				"shareContent text," +
				"shareThumb text," +
				"shareURL text," +
				"head_title text," +
				"head_link text," +
				"head_img text," +
				"is_hide integer," +
				"store_type integer," +		//存储方式（内置 1 、sd卡 2）
				"PRIMARY KEY(id)" +
				")";
		db.execSQL(createStr);
	}

	private void onCreateMusicTable(SQLiteDatabase db)
	{
		String createStr = "create table if not exists " + TableNames.MUSIC + "(" +
				"id integer unique," +
				"statisitcalID integer," +		//统计id
				"name text," +			//显示的name
				"type text," +			//音乐分类（轻快、动感。。）
				"author text," +
				"coverColor text," +
				"format text," +
				"fileName text," +			//音乐素材名称
				"duration integer," +
				"thumbnail text," +
				"res_path text," +
				"isLock integer," +
				"shareImg text," +
				"shareTitle text," +
				"shareLink text," +
				"is_hide integer," +
				"store_type integer," +		//存储方式（内置 1 、sd卡 2）
				"PRIMARY KEY(id)" +
				")";
		db.execSQL(createStr);
	}

	private void onCreateVideoTextTable(SQLiteDatabase db)
	{
		String createStr = "create table if not exists " + TableNames.VIDEO_TEXT + "(" +
				"id integer unique," +
				"statisitcalID integer," +		//统计id
				"name text," +			//显示的name
				"type text," +			//水印分类名字
				"author text," +
				"coverColor text," +
				"cover_pic text," +
				"thumbnail text," +
				"res_path text," +
				"isLock integer," +
				"shareImg text," +
				"res_arr text," +		//字体
				"type_id integer," +  //水印分类id
				"editType integer," + //可編輯、不可編輯
				"shareTitle text," +
				"shareIntroduce text," +
				"shareLink text," +
				"is_hide integer," +
				"store_type integer," +		//存储方式（内置 1 、sd卡 2）
				"PRIMARY KEY(id)" +
				")";
		db.execSQL(createStr);
	}
}
