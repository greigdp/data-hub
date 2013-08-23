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

  private String timestamp;
  private int xaxis;
  private int yaxis;
  private int zaxis;

  /**
   * AccelerometerWrapper constuctor.
   *
   * @param t Sample timestamp
   * @param x Sample x value
   * @param y Sample y value
   * @param z Smaple z value
   */
	public AccelerometerWrapper(String t, int x, int y, int z)
  {
  	if (Debug) Log.i(TAG, "AccelerometerWrapper constructor called.");

    this.timestamp = t;
    this.xaxis = x;
    this.yaxis = y;
    this.zaxis = z;
  }

  /**
   * Get the sample timestamp
   *
   * @return timestamp The recorded-at timestamp of the sample
   */
  public String getTimestamp()
  {
    return timestamp;
  }

  /**
   * Get the sample x value
   *
   * @return xaxis The x value of the sample
   */
  public int getX()
  {
    return xaxis;
  }

  /**
   * Get the sample y value
   *
   * @return xaxis The y value of the sample
   */
  public int getY()
  {
    return yaxis;
  }

  /**
   * Get the sample z value
   *
   * @return xaxis The z value of the sample
   */
  public int getZ()
  {
    return zaxis;
  }
}
