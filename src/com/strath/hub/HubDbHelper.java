package com.strath.hub;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

/**
 * Helper class for accessing the database.
 * @author jbanford
 *
 * @see HubDbOpenHelper
 */
public class HubDbHelper
{
	public final static String TAG = "HubDbHelper";
	private static final boolean Debug = true;

  private final static String DB_NAME = "hub_db.sqlite3";
  private final static String BASE_DIR = "Hub";

  private HubDbOpenHelper dbHelper;
  private int uid = 1; // [Fix;me: this hack adds a user id to the data.]

  /**
   * HubDB constructor
   *
   * @param context The context in which the HubDB object is created.
   */
	public HubDbHelper(Context context)
	{
		if (Debug) Log.i(TAG, "HubDbHelper constructor called.");

		String dbPath = HubDbHelper.getDbPath() + "/" + DB_NAME;
		dbHelper = new HubDbOpenHelper(context, dbPath);
	}

  /**
   * Return the absolute path to the hub database.
   *
   * @return path The path to the db as a string
   */
  private static String getDbPath()
  {
  	if (Debug) Log.i(TAG, "getDBPath() called.");

  	String path = Environment.getExternalStorageDirectory()
  	                         .getPath() + "/" + BASE_DIR;

    File dbDir = new File(path);
    if (!dbDir.isDirectory())
    {
    	try
    	{
    		if (Debug) Log.i(TAG, "Trying to create " + path);
    		dbDir.mkdirs();
    	}
    	catch (Exception e)
    	{
    		final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        Log.e(TAG, result.toString());
    	}
    }
    return path;
  }

  /**
   * Add an accelerometer sample to the db.
   * @param accWrap The AccelerometerWrapper containing data to be added. 
   */
	public synchronized void addAccSample(AccelerometerWrapper accWrap)
	{
		if (Debug) Log.i(TAG, "Adding a record to the accelerometer table");

		SQLiteDatabase db = null;
    try
    {
      db = dbHelper.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.put(HubDbOpenHelper.UID, uid);
      values.put(HubDbOpenHelper.TIMESTAMP, accWrap.getTimestamp());
      values.put(HubDbOpenHelper.X_AXIS, accWrap.getX());
      values.put(HubDbOpenHelper.Y_AXIS, accWrap.getY());
      values.put(HubDbOpenHelper.Z_AXIS, accWrap.getZ());
      db.insertOrThrow(HubDbOpenHelper.ACC_TABLE_NAME,
                       HubDbOpenHelper.TIMESTAMP,
                       values);
      if (Debug) Log.i(TAG, "Record added.");
    }
    catch(SQLException e)
    {
      Log.e(TAG, "Could not insert record:\n" + e);
    }
    finally
    {
      if (Debug) Log.i(TAG, "Closing database.");
      if (db != null) db.close();
    }
	}

  /**
   * Return a JSONArray of all row in the accelerometer table with an id
   * greater than {@code latestId}
   *
   * @param latestId
   * @return JSONArray of accelerometer samples
   */
  public JSONArray getLatestMovement(int latestId)
  {
    if (Debug) Log.i(TAG, "getLatestMovement called.");

    JSONArray acc_data = new JSONArray();
    SQLiteDatabase db = null;

    try
    {
      db = dbHelper.getReadableDatabase();
      Cursor c = db.query(HubDbOpenHelper.ACC_TABLE_NAME,
                          null,
                          HubDbOpenHelper.ID + " > " + latestId,
                          null,
                          null,
                          null,
                          HubDbOpenHelper.ID + " ASC");

      while (c.moveToNext())
      {
        int id = c.getInt(0);
        int uid = c.getInt(1);
        String t = c.getString(2);
        int x = c.getInt(3);
        int y = c.getInt(4);
        int z = c.getInt(5);
        JSONObject data = new JSONObject();

        try
        {
          data.put(HubDbOpenHelper.ID, id);
          data.put(HubDbOpenHelper.UID, uid);
          data.put(HubDbOpenHelper.TIMESTAMP, t);
          data.put(HubDbOpenHelper.X_AXIS, x);
          data.put(HubDbOpenHelper.Y_AXIS, y);
          data.put(HubDbOpenHelper.Z_AXIS, z);

          acc_data.put(acc);
        }
        catch (org.json.JSONException e)
        {
          Log.e(TAG, "Exception occured.\n" + e.getMessage());
        }
      }

      return acc_data;
    }
    finally
    {
      if (db != null) db.close();
    }
  }
}
