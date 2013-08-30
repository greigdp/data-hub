package com.strath.hub;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Integer;
import java.util.Arrays;
import java.util.List;
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
 * Set up and manage Bluetooth connections. Use separate threads to connect
 * to devices, and, when connected, to receive data.
 */
public class BluetoothLinkService
{
	private static final String TAG = "BluetoothLinkService";
	private static final boolean Debug = true;

  /** Default secure SPP UUID */
  private static final UUID SECURE_UUID = 
    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  private int mState;
  private ConnectThread mConnectThread;
  private ConnectedThread mConnectedThread;
  private Context mContext;
  private final BluetoothAdapter mAdapter;
  private final Handler mHandler;

  // States and corresponding values
  public static final int STATE_NONE = 0;
  public static final int STATE_LISTEN = 1;
  public static final int STATE_CONNECTING = 2;
  public static final int STATE_CONNECTED = 3;

  /** 
   * BluetoothLinkService Constructor.
   * @param context The current context of the main activity
   * @param handler A handler to send messages to the main activity
   * @see Hub
   */
	public BluetoothLinkService(Context context, Handler handler)
	{
    mContext = context;
    mHandler = handler;
    mAdapter = BluetoothAdapter.getDefaultAdapter();
    mState = STATE_NONE;
	}

  /** 
   * Set the current state of the link.
   * 
   * @param state An integer representing the current state.
   * See 'states and corresponding values' above.
   */
  private synchronized void setState(int state)
  {
    if (Debug) Log.i(TAG, "Change state from " + mState + " to " + state);
    mState = state;

    // Notify the UI activity of the state change
    mHandler.obtainMessage(Hub.MESSAGE_STATE_CHANGE,
                           state,
                           -1).sendToTarget();
  }

  /** 
   * Start {@code ConnectThread} to initiate a connection to a remote device.
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
   * Manage a Bluetooth link with a ConnectedThread
   *
   * @param socket The BluetoothSocket of the link
   * @param device The BluetoothDevice representing the connected device
   */
  public synchronized void connected(BluetoothSocket socket,
                                     BluetoothDevice device)
  {
    if (Debug) Log.i(TAG, "Connected.");
    
    // Cancel the connectThread that created the connection.
    if (mConnectThread != null)
    {
      mConnectThread.cancel();
      mConnectThread = null;
    }
    
    // Cancel any previous ConnectThread instances currently running.
    if (mConnectedThread != null) 
    {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }

    // Start a new ConnectedThread
    mConnectedThread = new ConnectedThread(socket);
    mConnectedThread.start();

    // Send the name of the connected device back to the UI activity.
    Message msg = mHandler.obtainMessage(Hub.MESSAGE_DEVICE_NAME);
    Bundle bundle = new Bundle();
    bundle.putString(Hub.DEVICE_NAME, device.getName());
    msg.setData(bundle);
    mHandler.sendMessage(msg);

    setState(STATE_CONNECTED);
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

    if (mConnectedThread != null)
    {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }
    
    setState(STATE_NONE);
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
   * Notify the UI activity that the connection was lost.
   */
  private void connectionLost()
  {
    Message msg = mHandler.obtainMessage(Hub.MESSAGE_TOAST);
    Bundle bundle = new Bundle();
    bundle.putString(Hub.TOAST, "Connection lost");
    msg.setData(bundle);
    mHandler.sendMessage(msg);
  }

  /**
   * Attempt to set up a link. The thread runs straight through; it either
   * succeeds or fails.
   */
  private class ConnectThread extends Thread
  {
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;

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
        Log.e(TAG, "Failed to create socket.\n", e);
      }
      mSocket = tmp;
    }

    public void run()
    {
      if (Debug) Log.i(TAG, "BEGIN mConnectThread.\n");

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
      connected(mSocket, mDevice);
    }

    public void cancel()
    {
      try
      {
        mSocket.close();
      }
      catch (IOException e)
      {
        Log.e(TAG, "Failed to close socket.\n", e);
      }
    }
  }

  /**
   * Manage a link. All incoming data is parsed here. (Outgoing
   * data would be sent from here too, if required.)
   */
  private class ConnectedThread extends Thread
  {
    private final BluetoothSocket mSocket;
    private final InputStream mInStream;  

    public ConnectedThread(BluetoothSocket socket)
    {
      if (Debug) Log.i(TAG, "Create ConnectedThread.");
      mSocket = socket;
      InputStream tmpIn = null;  

      // Get the input stream
      try
      {
        tmpIn = socket.getInputStream();
      }
      catch (IOException e)
      {
        Log.e(TAG, "Temporary input stream not created.\n" + e);
      }  

      mInStream = tmpIn;
    }

    public void run()
    {
      if (Debug) Log.i(TAG, "BEGIN mConnectedThread.");

      // Keep listening to the input stream while connected.
      while (true)
      {
        // Create a BufferedReader and read each array of bytes sent over the
        // link into it using an InputStreamReader.
        // The format of the data sent from the embedded system is 
        // timestamp, no. samples per second, x, y, z, t1, t2
        try
        {
          BufferedReader reader = new BufferedReader(
            new InputStreamReader(mInStream));
          String line;

          while ((line = reader.readLine()) != null)
          {
            List<String> data = Arrays.asList(line.split(","));
            if (data.size() == 7)
            {
              if (Debug) Log.i(TAG, "Received Data: " + line);  

              String timestamp = data.get(0);
              // Ignore the second element of data for now
              int xaxis = Integer.parseInt(data.get(2));
              int yaxis = Integer.parseInt(data.get(3));
              int zaxis = Integer.parseInt(data.get(4));

              // Temperature data...

              HubDbHelper db = new HubDbHelper(mContext);
              AccelerometerWrapper accWrap = 
                new AccelerometerWrapper(timestamp, xaxis, yaxis, zaxis);
              db.addAccSample(accWrap);
            }

            if (Debug) Log.i(TAG, "Send " + line + " to the UI activity.");
            mHandler.obtainMessage(Hub.MESSAGE_READ, line).sendToTarget();
          }
        }
        catch (IOException e)
        {
          Log.e(TAG, "Disconnected.\n", e);
          connectionLost();
          break;
        }
      }
    }

    public void cancel()
    {
      try
      {
        mSocket.close();
      }
      catch (IOException e)
      {
        Log.e(TAG, "Call to close socket failed.\n", e);
      }
    }
  }
}