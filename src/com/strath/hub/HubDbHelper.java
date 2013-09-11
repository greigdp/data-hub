package com.strath.hub;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

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
  private final static int BATCH_SIZE = 5;

  private HubDbOpenHelper dbHelper;

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
      values.put(HubDbOpenHelper.TIMESTAMP, accWrap.getTimestamp());
      values.put(HubDbOpenHelper.X_AXIS, accWrap.getX());
      values.put(HubDbOpenHelper.Y_AXIS, accWrap.getY());
      values.put(HubDbOpenHelper.Z_AXIS, accWrap.getZ());
      db.insertOrThrow(HubDbOpenHelper.ACC_TABLE_NAME,
                       HubDbOpenHelper.TIMESTAMP,
                       values);
      if (Debug) Log.i(TAG, "Acc record added.");
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
   * Add a temperature sample to the db.
   * @param tempWrap The TemperatureWrapper containing data to be added. 
   */
  public synchronized void addTempSample(TemperatureWrapper tempWrap)
  {
    if (Debug) Log.i(TAG, "Adding a record to the temperature table");

    SQLiteDatabase db = null;
    try
    {
      db = dbHelper.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.put(HubDbOpenHelper.TIMESTAMP, tempWrap.getTimestamp());
      values.put(HubDbOpenHelper.TEMP_1, tempWrap.getTemp1());
      values.put(HubDbOpenHelper.TEMP_2, tempWrap.getTemp2());
      db.insertOrThrow(HubDbOpenHelper.TEMP_TABLE_NAME,
                       HubDbOpenHelper.TIMESTAMP,
                       values);
      if (Debug) Log.i(TAG, "Temp record added: " + values);
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
   * 
   */
  public synchronized void addLocation(LocationWrapper location)
  {
    // Add interaction to the db
    if(Debug) Log.i(TAG, "Adding record to locations table");

    SQLiteDatabase db = null;
    try
    {
      db = dbHelper.getWritableDatabase();
      ContentValues values = new ContentValues();
      values.put(HubDbOpenHelper.TIMESTAMP, location.getTimestamp());
      values.put(HubDbOpenHelper.PROVIDER, location.getProvider());
      values.put(HubDbOpenHelper.LATITUDE, location.getLatitude());
      values.put(HubDbOpenHelper.LONGITUDE, location.getLongitude());
      values.put(HubDbOpenHelper.ACCURACY, location.getAccuracy());
      db.insertOrThrow(HubDbOpenHelper.LOC_TABLE_NAME,
                       HubDbOpenHelper.TIMESTAMP,
                       values);
      if (Debug) Log.i(TAG, "Location record added: " + values);     
    } 
    catch (SQLException e)
    {
      Log.i(TAG, "Could not insert record:\n" + e);
    } 
    finally
    {
      Log.i(TAG, "Closing database");
      if (db != null) db.close();
    }
  }

  /**
   * Return a JSONArray of all rows in the accelerometer table with an id
   * greater than {@code latestId}
   *
   * @param latestId
   * @return JSONArray of accelerometer samples
   */
  public ArrayList getLatestMovement(int latestId)
  {
    if (Debug) Log.i(TAG, "getLatestMovement called.");

    ArrayList accData = new ArrayList();
    SQLiteDatabase db = null;
    int number_of_rows = 0;
    int batch_size = BATCH_SIZE;
    int number_of_batches = 0;

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

      number_of_rows = c.getCount();
      number_of_batches = number_of_rows / BATCH_SIZE;

      for (int b = 0; b < number_of_batches; b = b + 1)
      {
        JSONArray batch = new JSONArray();
        
        Cursor bq = 
          db.query(HubDbOpenHelper.ACC_TABLE_NAME,
                   null,
                   HubDbOpenHelper.ID + " BETWEEN " + 
                     ((b * BATCH_SIZE) + latestId) + 
                     " AND " + (((b + 1) * BATCH_SIZE) + latestId - 1),
                   null,
                   null,
                   null,
                   HubDbOpenHelper.ID + " ASC");

        while(bq.moveToNext())
        {
          int id = bq.getInt(0);
          String t = bq.getString(1);
          int x = bq.getInt(2);
          int y = bq.getInt(3);
          int z = bq.getInt(4);

          JSONObject data = new JSONObject();

          try
          {
            data.put(HubDbOpenHelper.ID, id);
            data.put(HubDbOpenHelper.TIMESTAMP, t);
            data.put(HubDbOpenHelper.X_AXIS, x);
            data.put(HubDbOpenHelper.Y_AXIS, y);
            data.put(HubDbOpenHelper.Z_AXIS, z);
            batch.put(data);
          }
          catch (org.json.JSONException e)
          {
            Log.e(TAG, "Exception occured.\n" + e.getMessage());
          }
        }

        Log.i(TAG, "Batch:\n" + batch);
        accData.add(batch);
      }

      if (Debug) Log.i(TAG, "Final string:\n" + accData);
      return accData;
    }
    finally
    {
      if (db != null) db.close();
    }
  }

  /**
   * Return a JSONArray of all rows in the accelerometer table with an id
   * greater than {@code latestId}
   *
   * @param latestId
   * @return JSONArray of accelerometer samples
   */
  public ArrayList getLatestTemperature(int latestId)
  {
    if (Debug) Log.i(TAG, "getLatestTemperature called.");

    ArrayList tempData = new ArrayList();
    SQLiteDatabase db = null;
    int number_of_rows = 0;
    int batch_size = BATCH_SIZE;
    int number_of_batches = 0;

    try
    {
      db = dbHelper.getReadableDatabase();
      Cursor c = db.query(HubDbOpenHelper.TEMP_TABLE_NAME,
                          null,
                          HubDbOpenHelper.ID + " > " + latestId,
                          null,
                          null,
                          null,
                          HubDbOpenHelper.ID + " ASC");

      number_of_rows = c.getCount();
      number_of_batches = number_of_rows / BATCH_SIZE;

      for (int b = 0; b < number_of_batches; b = b + 1)
      {
        JSONArray batch = new JSONArray();
        
        Cursor bq = 
          db.query(HubDbOpenHelper.TEMP_TABLE_NAME,
                   null,
                   HubDbOpenHelper.ID + " BETWEEN " + 
                     ((b * BATCH_SIZE) + latestId) + 
                     " AND " + (((b + 1) * BATCH_SIZE) + latestId - 1),
                   null,
                   null,
                   null,
                   HubDbOpenHelper.ID + " ASC");

        while(bq.moveToNext())
        {
          int id = bq.getInt(0);
          String t = bq.getString(1);
          int t1 = bq.getInt(2);
          int t2 = bq.getInt(3);

          JSONObject data = new JSONObject();

          try
          {
            data.put(HubDbOpenHelper.ID, id);
            data.put(HubDbOpenHelper.TIMESTAMP, t);
            data.put(HubDbOpenHelper.TEMP_1, t1);
            data.put(HubDbOpenHelper.TEMP_2, t2);
            batch.put(data);
          }
          catch (org.json.JSONException e)
          {
            Log.e(TAG, "Exception occured.\n" + e.getMessage());
          }
        }

        Log.i(TAG, "Batch:\n" + batch);
        tempData.add(batch);
      }

      if (Debug) Log.i(TAG, "Final string:\n" + tempData);
      return tempData;
    }
    finally
    {
      if (db != null) db.close();
    }
  }

  /**
   * Return a JSONArray of all rows in the location table with an id
   * greater than {@code latestId}
   *
   * @param latestId
   * @return JSONArray of location samples
   */
  public ArrayList getLatestLocation(int latestId)
  {
    if (Debug) Log.i(TAG, "getLatestLocation called.");

    ArrayList locData = new ArrayList();
    SQLiteDatabase db = null;
    int number_of_rows = 0;
    int batch_size = BATCH_SIZE;
    int number_of_batches = 0;

    try
    {
      db = dbHelper.getReadableDatabase();
      Cursor c = db.query(HubDbOpenHelper.LOC_TABLE_NAME,
                          null,
                          HubDbOpenHelper.ID + " > " + latestId,
                          null,
                          null,
                          null,
                          HubDbOpenHelper.ID + " ASC");

      number_of_rows = c.getCount();
      number_of_batches = number_of_rows / BATCH_SIZE;

      for (int b = 0; b < number_of_batches; b = b + 1)
      {
        JSONArray batch = new JSONArray();
        
        Cursor bq = 
          db.query(HubDbOpenHelper.LOC_TABLE_NAME,
                   null,
                   HubDbOpenHelper.ID + " BETWEEN " + 
                     ((b * BATCH_SIZE) + latestId) + 
                     " AND " + (((b + 1) * BATCH_SIZE) + latestId - 1),
                   null,
                   null,
                   null,
                   HubDbOpenHelper.ID + " ASC");

        while(bq.moveToNext())
        {
          int id = bq.getInt(0);
          String t = bq.getString(1);
          String prov = bq.getString(2);
          float lat = bq.getFloat(3);
          float lon = bq.getFloat(4);
          float acc = bq.getFloat(5);

          JSONObject data = new JSONObject();

          try
          {
            data.put(HubDbOpenHelper.ID, id);
            data.put(HubDbOpenHelper.TIMESTAMP, t);
            data.put(HubDbOpenHelper.PROVIDER, prov);
            data.put(HubDbOpenHelper.LATITUDE, lat);
            data.put(HubDbOpenHelper.LONGITUDE, lon);
            data.put(HubDbOpenHelper.ACCURACY, acc);
            batch.put(data);
          }
          catch (org.json.JSONException e)
          {
            Log.e(TAG, "Exception occured.\n" + e.getMessage());
          }
        }

        Log.i(TAG, "Batch:\n" + batch);
        locData.add(batch);
      }

      if (Debug) Log.i(TAG, "Final string:\n" + locData);
      return locData;
    }
    finally
    {
      if (db != null) db.close();
    }
  }
}
