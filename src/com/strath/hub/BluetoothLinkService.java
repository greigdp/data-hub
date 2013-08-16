package com.strath.hub;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * Set up and manage Bluetooth connections. Use separate threads to listen
 * for incoming connections, connect to devices, and to transmit and
 * receive data.
 */
public class BluetoothLinkService
{
	private static final String TAG = "BluetoothLinkService";
	private static final boolean Debug = true;

  private int mState;
  private ConnectThread mConnectThread;

  // States and corresponding values
  public static final int STATE_NONE = 0;
  public static final int STATE_LISTEN = 1;
  public static final int STATE_CONNECTING = 2;
  public static final int STATE_CONNECTED = 3;

	public BluetoothLinkService(Context context, Handler handler)
	{
    // Stub.
	}

  /** 
   * Set the current state of the link.
   * 
   * @param state AN integer representing the current state.
   * See 'states and corresponding values' above.
   */
  private synchronized void setState(int state)
  {
    mState = state;
  }

  /** 
   * Start {@codeConnectThread} to initiate a connection to a remote device.
   *
   * @param device The BluetoothDevice representing the slave. 
   */
  public synchronized void connect(BluetoothDevice device)
  {
    if (Debug) Log.i(TAG, "Trying to connect to " + device);

    // Close any open connections, including those currently connecting,
    // and then connect.
    if (mState == STATE_CONNECTING)
    {
      if (mConnectThread != null)
      {
        mConnectThread.cancel();
        mConnectThread = null;
      }
    }

    if (mConnectThread != null)
    {
      mConnectThread.cancel();
      mConnectThread = null;
    }

    mConnectThread = new ConnectThread(device);
    mConnectThread.start();
    setState(STATE_CONNECTING);    
  }  
  /**
   * Stop all threads.
   */
  public synchronized void stop()
  {
  	if (Debug) Log.i(TAG, "Stop!");

  	// Stub.
  }

  private class ConnectThread extends Thread
  {
    // Stub.

    public ConnectThread(BluetoothDevice device)
    {
      // Stub.
    }

    public void cancel()
    {
      // Stub.
    }
  }
}