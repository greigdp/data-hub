package com.strath.hub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Start {@link SyncService}...
 *
 * @author jbanford
 *
 * ...
 */
public class SyncAlarmReceiver extends BroadcastReceiver
{
	public static final String TAG = "SyncAlarmReceiver";
  private static final boolean Debug = true;

  @Override
  public void onReceive(Context context, Intent intent)
  {
  	if (Debug) Log.i(TAG, "onReceive called.");

  	// Stub.
  }
}