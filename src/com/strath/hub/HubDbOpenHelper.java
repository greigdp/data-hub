package com.strath.hub;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class for opening and creating {@code hub_db.sqlite3}, to be used * in conjuction with {@link HubDbHelper}.
 *
 * @see HubDbHelper
 */
public class HubDbOpenHelper extends SQLiteOpenHelper
{
	private final static String TAG = "HubDbOpenHelper";
	private static final boolean Debug = true;

  public final static String ACC_TABLE_NAME = "accelerometer_data";
  public final static String TEMP_TABLE_NAME = "temperature_data";
  public final static String LOC_TABLE_NAME = "location_data";
  public final static String ID = "id";
  public final static String TIMESTAMP = "timestamp";
  public final static String X_AXIS = "xaxis";
  public final static String Y_AXIS = "yaxis";
  public final static String Z_AXIS = "zaxis";
  public final static String TEMP_1 = "temp1";
  public final static String TEMP_2 = "temp2";
  public final static String PROVIDER = "provider";
  public final static String LATITUDE = "latitude";
  public final static String LONGITUDE = "longitude";
  public final static String ACCURACY = "accuracy";

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
   * Create new tables in the hub_db.sqlite3 for accelerometer and
   * temperature data.
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
  	                    + TIMESTAMP + " text, "
  	                    + X_AXIS + " integer, "
  	                    + Y_AXIS + " integer, "
  	                    + Z_AXIS + " integer);";
    db.execSQL(createAccSQL);
    if (Debug) Log.i(TAG, "Create the accelerometer table.");

    String createTempSQL = "create table " 
                         + TEMP_TABLE_NAME
                         + " (" 
                         + ID + " integer primary key autoincrement, "
                         + TIMESTAMP + " text, "
                         + TEMP_1 + " integer, "
                         + TEMP_2 + " integer);";
    db.execSQL(createTempSQL);

    if(Debug) Log.i(TAG, "Create the location table.");
    String createLocSQL = "create table " 
                    + LOC_TABLE_NAME 
                    + " (" 
                    + ID + " integer primary key autoincrement, "
                    + TIMESTAMP + " datetime, "
                    + PROVIDER + " text, "
                    + LATITUDE + " real, "
                    + LONGITUDE + " real, "
                    + ACCURACY + " real);";
    db.execSQL(createLocSQL);
  }

  /** Stub to keep the compiler happy. */
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}