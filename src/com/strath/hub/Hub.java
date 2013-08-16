package com.strath.hub;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.widget.TextView;

/** 
 * The main activity and entry-point of the app.
 * @author jbanford.
 */
public class Hub extends Activity
{
  public static final String TAG = "Hub";
	private static final boolean Debug = true;

  public static final int MESSAGE_STATE_CHANGE = 1;
  public static final int MESSAGE_READ  = 2;
  public static final int MESSAGE_WRITE = 3;
  public static final int MESSAGE_DEVICE_NAME = 4;
  public static final int MESSAGE_TOAST = 5;

  private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  public static final String DEVICE_NAME = "device name";
  public static final String TOAST = "toast";
  public static final String CONN_LOST = "Connection lost";
  public static final String CONN_FAIL = "Unable to connect to device";

  // Hard code the MAC of the slave. [Fix;me: this should be configurable.]
  public static final String MAC_ADDRESS = "00:12:06:12:82:84";

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothLinkService mLinkService = null;
	private String mConnectedDeviceName = null;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
  	if (Debug) Log.i(TAG, "onCreate called.");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Get the local Bluetooth adapter and check that Bluetooth is
    // supported.
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if (mBluetoothAdapter == null)
    {
    	Toast.makeText( this, 
    		             "Bluetooth is not supported.", 
    		             Toast.LENGTH_LONG ).show();
    	finish();
    	return;
    }
    else
    {
    	// If Bluetooth is not on, request that it be enabled.
    	// setupLink() will then be called during onActivityResult.
    	if (!mBluetoothAdapter.isEnabled())
    	{
    		Intent enableIntent = new Intent(BluetoothAdapter.
    			                               ACTION_REQUEST_ENABLE);

    	}
    	// Bluetooth is on so set up the link session.
    	else
    	{
        if (mLinkService == null) setupLink();
        connectDevice();
    	}
    }
  }

  @Override
  public void onStart()
  {
  	super.onStart();
  	if (Debug) Log.i(TAG, "onStart called.");
  }

  @Override
  public synchronized void onPause()
  {
  	super.onPause();
  	if (Debug) Log.i(TAG, "onPause called.");
  }

  @Override
  public void onStop()
  {
  	super.onStop();
  	if (Debug) Log.i(TAG, "onStop called.");
  }

  @Override
  public void onDestroy()
  {
  	super.onDestroy();
  	if (Debug) Log.i(TAG, "onDestroy called.");

  	// Stop BluetoothLinkService
  	if (mLinkService != null) mLinkService.stop();
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    if (Debug) Log.i(TAG, "onActivityResult called.\n" +
                          "resultCode: " + resultCode);
    switch (requestCode)
    {
      case REQUEST_CONNECT_DEVICE_SECURE:
      // DeviceListActivity returns with a device to connect to.
        if (resultCode == Activity.RESULT_OK)
          connectDevice();
        break;
      case REQUEST_ENABLE_BT:
      // The user turns on Bluetooth.
        if (resultCode == Activity.RESULT_OK)
          setupLink();
        else
        // There was a problem. Log it, alert the user, and quit the app.
        {
          Log.e(TAG, "Bluetooth not enabled.");
          Toast.makeText(this, 
                         R.string.bt_not_enabled,
                         Toast.LENGTH_SHORT).show();
          finish();
        }
    }
  }

  /**
   * Initialise a {@codeBluetoothLinkService} object and call
   * {@codeconnectDevice} to initiate a Bluetooth connection.
   *
   * @see #BluetoothLinkService(Activity, Handler)
   * @see #connectDevice()
   */
  private void setupLink()
  {
    if (Debug) Log.i(TAG, "setupLink called.");

    // Initialise the BluetoothLinkService
    mLinkService = new BluetoothLinkService(this, mHandler);
  }

  // Handler to receive information from the BluetoothLinkService
  private final Handler mHandler = new Handler()
  {
    @Override
    public void handleMessage(Message msg)
    {
    	switch (msg.what)
    	{
    		case MESSAGE_STATE_CHANGE:
      		if(Debug) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
      		switch (msg.arg1)
      		{
      			case BluetoothLinkService.STATE_CONNECTED:
      			  setStatus(getString(R.string.title_connected_to,
      				                    mConnectedDeviceName));
              break;
            case BluetoothLinkService.STATE_CONNECTING:
              setStatus(R.string.title_connecting);
              break;
            case BluetoothLinkService.STATE_LISTEN:
              // [Fix;me: what happens in the listening state?
              // Is it necessary?]
              break;
            case BluetoothLinkService.STATE_NONE:
              setStatus(R.string.title_not_connected);
              break;
      		}
      		break;
      	case MESSAGE_WRITE:
      	  // [Fix;me: is this case necessary?]
      	  break;
        case MESSAGE_READ:
          String readMessage = (String) msg.obj;
          if (Debug) Log.i(TAG, readMessage);
          TextView display_bt_data = (TextView) findViewById(R.id.bt_data);
          display_bt_data.setText(readMessage);
          break;
        case MESSAGE_DEVICE_NAME:
          mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
          Toast.makeText(getApplicationContext(),
          	             "Connected to " + mConnectedDeviceName,
          	             Toast.LENGTH_SHORT).show();
          break;
        case MESSAGE_TOAST:
          String toast = msg.getData().getString(TOAST);
          Toast.makeText(getApplicationContext(),
          	             toast,
          	             Toast.LENGTH_SHORT).show();
          if (toast.equals(CONN_LOST) || toast.equals(CONN_FAIL))
          {
          	if (Debug) Log.i(TAG, toast);
          	if (mLinkService != null)
          	{
          		connectDevice();
          	}
          	else
          	{
          		setupLink();
          		connectDevice();
          	}
          }
          break;
    	}
    }
  };

  /**
   * Create a {@codeBluetoothDevice} object representing the slave device,
   * and connect to the device.
   * 
   * @see BluetoothDevice
   * @see BluetoothLinkService#connect(BluetoothDevice)
   */
  private void connectDevice()
  {
  	if (Debug) Log.i(TAG, "connectDevice called");
    // Create a Bluetooth device representing the slave and connect to it.
    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MAC_ADDRESS);
    mLinkService.connect(device);
  }

  private final void setStatus(int resId)
  {
    final ActionBar actionBar = getActionBar();
    actionBar.setSubtitle(resId);
  }

  private final void setStatus(CharSequence subTitle)
  {
    final ActionBar actionBar = getActionBar();
    actionBar.setSubtitle(subTitle);
  }

}
