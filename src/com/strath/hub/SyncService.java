package com.strath.hub;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Synchronise locally stored data with a server.
 *
 * @author jbanford
 */
public class SyncService extends Service
{
	public static final String TAG = "SyncService";
  private static final boolean Debug = true;

  @Override
  public void onCreate()
  {
  	if (Debug) Log.i(TAG, "onCreate");

  	super.onCreate();

  	// Stub.
  }

  @Override
  public IBinder onBind(Intent intent)
  {
  	// Do nothing.
  	// Keep the complier happy.

  	return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
  	if (Debug) Log.i(TAG, "onStartCommand called: " + intent);

  	return 0;
  }

  @Override
  public void onDestroy()
  {
  	if (Debug) Log.i(TAG, "onDestroy called.");

  	super.onDestroy();

  	// Stub.
  }  
}