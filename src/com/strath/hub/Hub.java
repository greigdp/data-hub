package com.strath.hub;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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
  public static final int MESSAGE_ERROR = 5;

  private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  public static final String DEVICE_NAME = "device name";
  public static final String ERROR = "toast";
  public static final String CONN_LOST = "Connection lost";
  public static final String CONN_FAIL = "Unable to connect to device";

  // Hard code the MAC of the slave. [Fix;me: this should be configurable.]
  public static final String MAC_ADDRESS = "00:12:05:31:93:59";

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
    	Toast.makeText(this,
                     "Bluetooth is not supported",
                     Toast.LENGTH_LONG).show();
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

    startGatheringData();
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

    stopGatheringData();
  }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(Hub.this,
                        PreferencesActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
          Log.e(TAG, "Bluetooth not enabled");
          Toast.makeText(this, 
                         R.string.bt_not_enabled,
                         Toast.LENGTH_SHORT).show();
          finish();
        }
    }
  }

  /**
   * Start the data gathering service.
   *
   * @see DataGatheringService
   */
  private void startGatheringData()
  {
    if (Debug) Log.i(TAG, "startGatheringData called.");

    Intent intent = new Intent(this, DataGatheringService.class);
    startService(intent);
  }

  /**
   * Stop the data gathering service.
   */
  private void stopGatheringData()
  {
    if (Debug) Log.i(TAG, "stopGatheringData called.");

    Intent intent = new Intent(this, DataGatheringService.class);
    stopService(intent);
  }

  /**
   * Initialise a {@code BluetoothLinkService} object
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

  /**
   * Create a {@code BluetoothDevice} object representing the slave device,
   * and connect to the device.
   * 
   * @see BluetoothDevice
   * @see BluetoothLinkService#connect(BluetoothDevice)
   */
  private void connectDevice()
  {
    String clientMacAddress = "";

  	if (Debug) Log.i(TAG, "connectDevice called");
     // Open a connection to the application's
      try {
          SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(this);
          clientMacAddress = myPrefs.getString("clientmac", null);
          if (clientMacAddress != null)
          {
              // Create a Bluetooth device representing the slave and connect to it.
              BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(formatMacAddress(clientMacAddress));
              mLinkService.connect(device);
          }
          else
          {
              // Null MAC address stored
              Log.w(TAG, "No MAC address found in settings - likely this is the first run of the app");
              TextView display_bt_data = (TextView) findViewById(R.id.bt_data);
              display_bt_data.setText("No bluetooth MAC set!");
              Intent intent = new Intent(Hub.this,
                      PreferencesActivity.class);
              startActivity(intent);
          }
      } catch (Exception NullPointerException)
      {
          Log.w(TAG, "NPE during Bluetooth Config - likely this is the first run of the app");
          TextView display_bt_data = (TextView) findViewById(R.id.bt_data);
          display_bt_data.setText("Exception in Bluetooth connection!");
      }



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
            case BluetoothLinkService.STATE_NONE:
              setStatus(R.string.title_not_connected);
              break;
          }
          break;
        case MESSAGE_READ:
          String readMessage = (String) msg.obj;
          if (Debug) Log.i(TAG, readMessage);
          TextView display_bt_data = (TextView) findViewById(R.id.bt_data);
          display_bt_data.setText(readMessage);
          break;
        case MESSAGE_DEVICE_NAME:
          mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
          if (Debug) Log.i(TAG, "Connected to " + mConnectedDeviceName);
          Toast.makeText(Hub.this,
                         mConnectedDeviceName,
                         Toast.LENGTH_LONG).show();
          break;
        case MESSAGE_ERROR:
          String message = msg.getData().getString(ERROR);
          if (message.equals(CONN_LOST) || message.equals(CONN_FAIL))
          {
            if (Debug) Log.i(TAG, message);
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


    /**
     * Accepts user-supplied MAC address, and format it to be colon-separated if needed
     * @param storedMAC The user-supplied MAC address of the datalogger
     * @return The colon-separated, correctly formatted MAC if valid, null
     *         otherwise! (will give a handledNPE)
     */
  private String formatMacAddress(String storedMAC)
  {

    String outputMAC = "";
    // First let's try to figure out what the user has given us!
    if (storedMAC.matches("^([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])$"))
      {
          // MAC is in colon separated form, no action required
          return storedMAC;
      }
      else if (storedMAC.matches("^([0-9a-fA-F]){12}$"))
        {
          outputMAC = storedMAC;
          // MAC is in non-separated form, must add colons
              for (int macposition = 2 ; macposition < storedMAC.length()+4 ;  macposition = macposition+3)
              {
                  outputMAC = new StringBuilder(outputMAC).insert(macposition, ":").toString();
                  // MAC should be in colon delimited form
              }
            return outputMAC;
        }
      // if all else has failed, return null, as MAC is invalid!
      return null;
  }
}
