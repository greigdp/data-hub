package com.strath.hub;

import android.util.Log;

/**
 * An abstraction of location data.
 */
public class LocationWrapper
{
	public final static String TAG = "LocationWrapper";
  private static final boolean Debug = true;

  private int oid;
  private String timestamp;
  private String provider;
  private double accuracy;
  private double latitude;
  private double longitude;

  /**
   * Constructs an object of type {@code LocationWrapper}.
   *
   * @param timestamp The timestamp of adding the interaction to the
   * interactions db.
   * @param provider The name of the location provider. Can be one of the
   * three values: cell, wifi, gps.
   * @param latitude The latitude of the location reading.
   * @param longitude The longitude of the location reading.
   * @param accuracy The accuracy of the location reading in metres.
   */
  public LocationWrapper(String timestamp,
  	                     String provider,
  	                     double latitude,
  	                     double longitude,
  	                     double accuracy)
  {
  	if(Debug) Log.i(TAG, "Create a LocationWrapper.");

  	this.timestamp = timestamp;
    this.provider = provider;
    this.latitude = latitude;
    this.longitude = longitude;
    this.accuracy = accuracy;
  }

  /**
   * @return The recorded-at timestamp of the location.
   */
  public String getTimestamp()
  {
    return timestamp;
  }

  /**
   * @return The name of the location provider. Can be one of the three values:
   * cell, wifi, gps.
   */
  public String getProvider()
  {
    return provider;
  }

  /**
   * @return The accuracy of the location reading (in metres).
   */
  public double getAccuracy()
  {
    return accuracy;
  }

  /**
   * @return The latitude of the location reading.
   */
  public double getLatitude()
  {
    return latitude;
  }

  /**
   * @return The longitude of the location reading.
   */
  public double getLongitude()
  {
    return longitude;
  }
}