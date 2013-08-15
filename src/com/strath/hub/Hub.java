package com.strath.hub;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/** 
 * The main activity and entry-point of the app.
 * @author jbanford.
 */
public class Hub extends Activity
{
  public static final String TAG = "Hub";
	private static final boolean Debug = true;

	private BluetoothAdapter mBluetoothAdapter = null;
	private BluetoothLinkService mLinkService = null;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
  	if(Debug) Log.i(TAG, "onCreate called.");

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
  public syncronised void onPause()
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
  /**
   * Initalise a {@codeBluetoothLinkService} object and call
   * {@codeconnectDevice} to initiate a Bluetooth connection.
   *
   * @see #BluetoothLinkservice(Activity, Handler)
   * @see #connectDevice()
   */
  private void setupLink()
  {
    if(Debug) Log.i(TAG, "setupLink called.");

    // Stub method for now.
  }

  /**
   * Create a {@codeBluetoothDevice} object representing the slave device,
   * and connect to the device.
   * 
   * @see BluetoothDevice
   * @see BluetoothLinkservice#connect(BluetoothDevice)
   */
  private void connectDevice()
  {
  	if(Debug) Log.i(TAG, "connectDevice called");

  	// Stub method for now.
  }

}
