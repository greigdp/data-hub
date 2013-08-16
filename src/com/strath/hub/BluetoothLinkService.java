package com.strath.hub;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

  /** Notify the UI activity that a connection attempt failed. */
  private void connectionFailed()
  {
    Message msg = mHandler.obtainMessage(Hub.MESSAGE_TOAST);
    Bundle bundle = new Bundle();
    bundle.putString(Hub.TOAST, "Unable to connect to device");
    msg.setData(bundle);
    mHandler.sendMessage(msg);
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

    public void run()
    {
      if (Debug) Log.i(TAG, "BEGIN mConnectThread.\n" + 
                            "SocketType: " + mSocketType);

      // Cancel device discovery to prevent it from slowing the connection.
      mAdapter.cancelDiscovery();

      // Connect to the BluetoothSocket. The call to connect() is a blocking
      // call and will only return on a successful connection or an 
      // exception.
      try
      {
        if (Debug) Log.i(TAG, "Trying to connect.");
        mSocket.connect();
      }
      catch (IOException e)
      {
        try
        {
          mSocket.close();
        }
        catch (IOException e2)
        {
          Log.e(TAG, "Connection failure. Unable to close socket.\n" + e2);
        }
        if (Debug) Log.i(TAG, "Connection failed.");
        connectionFailed();
        return;
      }

      // Reset the ConnectThread
      synchronized (BluetoothLinkService.this)
      {
        mConnectThread = null;
      }

      // Start the connected thread
      connected(mSocket, mDevice, mSocketType);
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