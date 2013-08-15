package com.strath.hub;

import android.app.Activity;
import android.os.Bundle;

/** 
 * The main activity and entry-point of the app.
 * @author jbanford.
 */
public class Hub extends Activity
{
  public static final String TAG = "Hub";
	private static final boolean Debug = true;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
  	if(Debug) Log.i(TAG, "onCreate called.");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }
}
