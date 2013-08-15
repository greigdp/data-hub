package com.strath.hub;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * Set up and manage Bluetooth connections. Use seperate threads to listen
 * for incomming connections, connect to devices, and to transmit and
 * recieve data.
 */
public class BluetoothLinkService
{
	private static final String TAG = "BluetoothLinkService";
	private static final boolean Debug = true;

  public static final int STATE_NONE = 0;
  public static final int STATE_LISTEN = 1;
  public static final int STATE_CONNECTING = 2;
  public static final int STATE_CONNECTED = 3;

	public BluetoothLinkService(Context context, Handler handler)
	{
    // Stub.
	}

  public synchronized void stop()
  {
  	if (Debug) Log.i(TAG, "Stop!");

  	// Stub.
  }
}