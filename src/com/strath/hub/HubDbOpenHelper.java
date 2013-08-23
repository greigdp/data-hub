package com.strath.hub;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class for opening and creating hub_db.sqlite3, to be used in
 * conjuction with {@link HubDbHelper}.
 *
 * @see HubDbHelper
 */
public class HubDbOpenHelper extends SQLiteOpenHelper
{
	private final static String TAG = "HubDbOpenHelper";
	private static final boolean Debug = true;

  public final static String ACC_TABLE_NAME = "accelerometer_data";
  public final static String ID = "id";
  public final static String UID = "user_id";
  public final static String TIMESTAMP = "timestamp";
  public final static String X_AXIS = "xaxis";
  public final static String Y_AXIS = "yaxis";
  public final static String Z_AXIS = "zaxis";

  /**
   * Construct a PatientDbOpenHelper object.
   *
   * @param context The application context of the PatientDbHelper object
   * @param path The absolute path to hub_db.sqlite3
   */
  public HubDbOpenHelper(Context context, String path)
  {
  	super(context, path, null, 1);
  	if (Debug) Log.i(TAG, "Create a PatientDbOpenHelper.");
  }

  /**
   * Create a new table in the hub_db.sqlite3 for accelerometer data.
   *
   * @param db SQLiteDatabase object representing hub_db.sqlite3
   */
  @Override
  public void onCreate(SQLiteDatabase db)
  {
  	if (Debug) Log.i(TAG, "onCreate called.");

  	String createAccSQL = "create table "
  	                    + ACC_TABLE_NAME
  	                    + " ("
  	                    + ID + " integer primary key autoincrement, "
  	                    + UID + " integer, "
  	                    + TIMESTAMP + " text, "
  	                    + X_AXIS + " integer, "
  	                    + Y_AXIS + " integer, "
  	                    + Z_AXIS + " integer);";
    db.execSQL(createAccSQL);
  }

  /** Stub to keep the compiler happy. */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}