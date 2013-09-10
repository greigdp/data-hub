package com.strath.hub;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 */
public class LocationReceiver implements LocationListener
{
	private final static String TAG = "LocationReceiver";
	private static final boolean Debug = true;

  private static final String EXTRAS_KEY = "networkLocationType";
  public static final String LOCATION_ACTION 
    = "com.strath.hub.LocationAction";
  public static final String LOCATION_DATA = "new_location_data";

	private Context mContext;
	Intent intent;

  /**
   * 
   */
	public LocationReceiver(Context context)
	{
		if (Debug) Log.i(TAG, "Create a LocationReceiver.");

		mContext = context;
	}

  /**
   * 
   */
	public void onLocationChanged(Location location)
  {
  	if (Debug) Log.i(TAG, "onLocationChanged called.");

  	String timestamp
  	  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    Bundle extras = location.getExtras();
    String provider
      = (extras == null || extras.getString(EXTRAS_KEY) == null)
        ? location.getProvider() : extras.getString(EXTRAS_KEY);
            
    if(Debug) Log.i(TAG, "Location provider at " 
                      + timestamp + " : " 
                      + provider + " Location: " 
                      + location.getLatitude() + ", " 
                      + location.getLongitude());

    HubDbHelper db = new HubDbHelper(mContext);

    db.addLocation(new LocationWrapper(
                                        timestamp, 
                                        provider, 
                                        location.getLatitude(),
                                        location.getLongitude(),
                                        location.getAccuracy())
                                      );
    intent = new Intent(LOCATION_ACTION);
    intent.putExtra(LOCATION_DATA, timestamp);
    mContext.sendBroadcast(intent);
  }

  // Keep the compiler happy.
  public void onStatusChanged(String provider, int status, Bundle extras) {}
  public void onProviderEnabled(String provider) {}
	public void onProviderDisabled(String provider) {}
}
