package com.strath.hub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

/**
 * Service which controls starting and stopping gathering data. It
 * currently controls the syncing of external data---received via
 * Bluetooth---to the server and the gathering of device context data
 * (such as location data).
 *
 * @author jbanford
 */
public class DataGatheringService extends Service
{
	public static final String TAG = "DataGatheringService";
	private static final boolean Debug = true;

	private final static long SYNC_PERIOD = 
	  AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5; // 3 minutes.
  private final static long GPS_UPDATE_PERIOD = 300000; // 5 minutes.
  private final static long NETWORK_UPDATE_PERIOD = 300000;

	private AlarmManager mAlarmManager;
	private Intent mSyncIntent;
	private PendingIntent mSyncPendingIntent;

  private LocationManager mLocationManager;
  private LocationReceiver mLocationReceiver;

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

    mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    startGathering();
    startSync();

  	return 0;
  }

  @Override
  public void onDestroy()
  {
  	if (Debug) Log.i(TAG, "onDestroy called.");

  	super.onDestroy();

    stopGathering();
  	stopSync();
  }

  /**
   * Start the synchronisation service.
   */
  private void startSync()
  {
  	if (Debug) Log.i(TAG, "startSync called. SYNC_PERIOD IS " + SYNC_PERIOD);

  	mSyncIntent = 
  	  new Intent(getApplicationContext(), SyncAlarmReceiver.class);
  	mSyncPendingIntent = 
  	  PendingIntent.getBroadcast(getApplicationContext(), 0, mSyncIntent, 0);
    mAlarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
    	                                SystemClock.elapsedRealtime(),
    	                                SYNC_PERIOD,
    	                                mSyncPendingIntent);
  }

  /**
   * Stop the synchronisation service
   */
  private void stopSync()
  {
  	if (Debug) Log.i(TAG, "stopSync called.");

  	if (mSyncPendingIntent != null)
      mAlarmManager.cancel(mSyncPendingIntent);
  }

  private void startGathering()
  {
    if (Debug) Log.i(TAG, "startGathering called.");

    mLocationReceiver = new LocationReceiver(getApplicationContext());
    mLocationManager
      = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                            NETWORK_UPDATE_PERIOD, 
                                            0, 
                                            mLocationReceiver);
    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                            GPS_UPDATE_PERIOD,
                                            0,
                                            mLocationReceiver);
  }

  private void stopGathering()
  {
    if (Debug) Log.i(TAG, "stopGathering called.");

    mLocationManager.removeUpdates(mLocationReceiver);
  }
}
