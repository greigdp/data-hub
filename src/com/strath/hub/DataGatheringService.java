package com.strath.hub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service which controls starting and stopping of gathering data. It
 * currently only controls the syncing of external data---received via
 * Bluetooth---to the server but the gathering of device context data
 * (such as location data) could also be controlled from here.
 *
 * @author jbanford
 */
public class DataGatheringService extends Service
{
	public static final String TAG = "DataGatheringService";
	private static final boolean Debug = true;

  @Override
  public void onCreate()
  {
  	if (Debug) Log.i(TAG, "onCreate called.");

  	super.onCreate();
  }

  @Override
  public IBinder onBind(Intent intent)
  {
  	// Do nothing.
  	// Keep the compiler happy.

  	return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
  	if (Debug) Log.i(TAG, "onStartCommand called : " + intent);

  	super.onStartCommand(intent, flags, startId);

    // Stub.

  	return 0;
  }
}
