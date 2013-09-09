package com.strath.hub;

import android.util.Log;

/**
 * An abstraction of temperature data.
 * @author jbanford.
 */
public class TemperatureWrapper
{
	public final static String TAG = "TemperatureWrapper";
	private static final boolean Debug = true;

  private String timestamp;
  private int temperature1;
  private int temperature2;

  /**
   * TemperatureWrapper constuctor.
   *
   * @param t Sample timestamp
   * @param x Sample x value
   * @param y Sample y value
   * @param z Sample z value
   */
	public TemperatureWrapper(String t, int temp1, int temp2)
  {
  	if (Debug) Log.i(TAG, "TemperatureWrapper constructor called.");

    this.timestamp = t;
    this.temperature1 = temp1;
    this.temperature2 = temp2;
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
   * Get the value of temperature 1
   *
   * @return temperature1 The value of the temperature sensor 1
   */
  public int getTemp1()
  {
    return temperature1;
  }

  /**
   * Get the value of temperature 2
   *
   * @return temperature1 The value of the temperature sensor 2
   */
  public int getTemp2()
  {
    return temperature2;
  }
}
