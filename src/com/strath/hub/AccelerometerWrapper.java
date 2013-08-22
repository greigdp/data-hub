package com.strath.hub;

import android.util.Log;

/**
 * An abstraction of accelerometer data.
 * @author jbanford.
 */

public class AccelerometerWrapper
{
	public final static String TAG = "AccelerometerWrapper";
	private static final boolean Debug = true;

  /**
   * AccelerometerWrapper constuctor.
   * @param t Sample timestamp
   * @param x Sample x value
   * @param y Sample y value
   * @param z Smaple z value
   */
	public AccelerometerWrapper(String t, int x, int y, int z)
  {
  	if (Debug) Log.i(TAG, "AccelerometerWrapper constructor called.");

    // Stub.
  }
}
