package com.strath.hub;

import android.util.Log;

/**
 * 
 */
public class LocationWrapper
{
	public final static String TAG = "LocationWrapper";
  private static final boolean D = true;

  private int oid;
  private String timestamp;
  private String date;
  private String time;
  private String provider;
  private double accuracy;
  private double latitude;
  private double longitude;

  /**
   * 
   */
  public LocationWrapper(String timestamp,
  	                     String provider,
  	                     double latitude,
  	                     double longitude,
  	                     double accuracy)
  {
  	// Stub.
  }
}