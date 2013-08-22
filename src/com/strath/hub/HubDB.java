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

  /** ... */
	public HubDB(Context context)
	{
		if (Debug) Log.i(TAG, "HubDB constructor called.");

		// Stub.
	}

  /** ... */
	public synchronized void addAccSample(AccelerometerWrapper accWrap)
	{
		if (Debug) Log.i(TAG, "Add a record to the accelerometer table");

		// Stub.
	}
}