package com.strath.hub;

import android.app.Activity;
import android.os.Bundle;

/** 
 * The main activity and entry-point of the app.
 * @author jbanford.
 */
public class Hub extends Activity
{
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }
}
