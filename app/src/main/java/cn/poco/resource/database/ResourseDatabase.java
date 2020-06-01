package cn.poco.resource.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

import cn.poco.framework.MyFramework;
import cn.poco.framework.MyFramework2App;
import cn.poco.interphoto2.PocoCamera;

/**
 * Created by admin on 2017/4/21.
 */

public class ResourseDatabase
{
	private static ResourseDatabase m_resourseDatabase;
	private ResourseDatabaseHelper m_helper;
	private boolean m_isOpen = false;
	private SQLiteDatabase m_database;
	private ArrayList<Integer> m_openFlag = new ArrayList<>();

	private ResourseDatabase(Context context)
	{
		m_helper = new ResourseDatabaseHelper(context);
	}

	public synchronized static ResourseDatabase getInstance(Context context)
	{
		if(m_resourseDatabase == null)
		{
			m_resourseDatabase = new ResourseDatabase(context);
		}
		return m_resourseDatabase;
	}

	public synchronized SQLiteDatabase openDatabase()
	{
		if(!m_isOpen || m_database == null)
		{
			try
			{
				m_database = m_helper.getWritableDatabase();
				m_database.disableWriteAheadLogging();
				m_isOpen = true;
			}catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
		addToFlag();
		return m_database;
	}

	private synchronized void addToFlag()
	{
		m_openFlag.add(1);
	}

	private synchronized boolean needClose()
	{
		if(m_openFlag.size() == 0)
			return true;
		m_openFlag.remove(0);
		if(m_openFlag.size() == 0)
			return true;
		return false;
	}

	public void DeleteDatabase()
	{
		File database = MyFramework2App.getInstance().getApplicationContext().getDatabasePath("Resourse.db");
		if(database != null && database.exists())
		{
			database.delete();
		}
	}

	public synchronized void closeDatabase()
	{
		if(needClose())
		{
			m_isOpen = false;
			m_helper.close();
			m_database = null;
		}
	}
}
