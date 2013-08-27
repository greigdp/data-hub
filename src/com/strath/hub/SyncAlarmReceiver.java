package com.strath.hub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Listen for a pending intent triggered by an {@link 
 * android.app.AlarmManager} in {@link DataGatheringService} and start 
 * {@link syncService}.
 *
 * @author jbanford
 *
 * @see com.strath.hub.DataGatheringService
 * @see com.strath.hub.SyncService
 */
public class SyncAlarmReceiver extends BroadcastReceiver
{
	public static final String TAG = "SyncAlarmReceiver";
  private static final boolean Debug = true;

  @Override
  public void onReceive(Context context, Intent intent)
  {
  	if (Debug) Log.i(TAG, "onReceive called.");

  	Intent syncIntent = new Intent(context, SyncService.class);
  	context.startService(syncIntent);
  }
}