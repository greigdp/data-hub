package com.strath.hub;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
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

	private final static long SYNC_PERIOD = 
	  AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15; // Once a minute for testing.

	private AlarmManager mAlarmManager;
	private Intent mSyncIntent;
	private PendingIntent mSyncPendingIntent;
	private boolean mIsSynching;

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
    startSync();

  	return 0;
  }

  @Override
  public void onDestroy()
  {
  	if (Debug) Log.i(TAG, "onDestroy called.");

  	super.onDestroy();

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
    mIsSynching = true;
  }

  /**
   * Stop the synchronisation service
   */
  private void stopSync()
  {
  	if (Debug) Log.i(TAG, "stopSync called.");

  	if (mSyncPendingIntent != null)
      mAlarmManager.cancel(mSyncPendingIntent);
    mIsSynching = false;
  }
}
