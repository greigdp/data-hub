package com.strath.hub;

import android.content.Context;
import android.util.Log;

/**
 * Helper class for accessing the database.
 * @author jbanford
 *
 * @see HubDBHelper
 */
public class HubDB
{
	public final static String TAG = "HubDB";
	private static final boolean Debug = true;

  /**
   * HubDB constructor
   * @param context The context in which the HubDB object is created.
   */
	public HubDB(Context context)
	{
		if (Debug) Log.i(TAG, "HubDB constructor called.");

		// Stub.
	}

  /**
   * Add an accelerometer sample to the db.
   * @param accWrap The AccelerometerWrapper containing data to be added. 
   */
	public synchronized void addAccSample(AccelerometerWrapper accWrap)
	{
		if (Debug) Log.i(TAG, "Add a record to the accelerometer table");

		// Stub.
	}
}