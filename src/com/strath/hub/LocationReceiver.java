package com.strath.hub;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class LocationReceiver implements LocationListener
{
	// Stub.

	public LocationReceiver(Context context)
	{
		// Stub.
	}

	public void onLocationChanged(Location location)
  {
  	// Stub.
  }

  // Keep the compiler happy.
  public void onStatusChanged(String provider, int status, Bundle extras) {}
  public void onProviderEnabled(String provider) {}
	public void onProviderDisabled(String provider) {}
}
