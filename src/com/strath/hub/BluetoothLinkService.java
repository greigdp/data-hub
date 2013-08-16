package com.strath.hub;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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

  /** Default RFCOMM/SPP UUID */
  private static final UUID SECURE_UUID = 
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  private int mState;
  private ConnectThread mConnectThread;
  private Context mContext;
  private final BluetoothAdapter mAdapter;
  private final Handler mHandler;

  // States and corresponding values
  public static final int STATE_NONE = 0;
  public static final int STATE_LISTEN = 1;
  public static final int STATE_CONNECTING = 2;
  public static final int STATE_CONNECTED = 3;

	public BluetoothLinkService(Context context, Handler handler)
	{
    mContext = context;
    mAdapter = BluetoothAdapter.getDefaultAdapter();
    mState = STATE_NONE;
    mHandler = handler;
	}

  /** 
   * Set the current state of the link.
   * 
   * @param state An integer representing the current state.
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

  	if (mConnectThread != null)
    {
      mConnectThread.cancel();
      mConnectThread = null;
    }
    setState(STATE_NONE);
  }

  private class ConnectThread extends Thread
  {
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private String mSocketType; // Is this required? All sockets are secure.

    public ConnectThread(BluetoothDevice device)
    {
      mDevice = device;
      BluetoothSocket tmp = null;

      try
      {
        tmp = device.createRfcommSocketToServiceRecord(SECURE_UUID);
      }
      catch (IOException e)
      {
        Log.e(TAG, "Failed to create " + mSocketType + " socket.\n", e);
      }
      mSocket = tmp;
    }

    public void cancel()
    {
      try
      {
        mSocket.close();
      }
      catch (IOException e)
      {
        Log.e(TAG, "Failed to close " + mSocketType + " socket.\n", e);
      }
    }
  }
}