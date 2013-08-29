package com.strath.hub;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Base64;
import android.util.Log;

/**
 * Synchronise locally stored data with a server.
 *
 * @author jbanford
 */
public class SyncService extends Service
{
	public static final String TAG = "SyncService";
  private static final boolean Debug = true;

  private static final int MAX_RETRIES = 3;
  private static final int CONNECTION_TIMEOUT = 10000;
  private static final int[] RETRY_DELAY = new int[] {5000, 10000, 20000};
  private static final String LATEST_ACC_PATH =
    "http://sederunt.org/movements/latest";
  private static final String ACC_PATH =
    "http://sederunt.org/movements/input";
  private static final String CHARSET = "UTF-8";

  private PowerManager mPowerManager;
  private PowerManager.WakeLock mWakeLock;
  private SyncThread mSyncThread;
  private boolean mHasError = false;

  @Override
  public void onCreate()
  {
  	if (Debug) Log.i(TAG, "onCreate");

  	super.onCreate();
    
    // Wake the device before syncing
  	mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    mWakeLock =
      mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
    mWakeLock.acquire();
  }

  @Override
  public IBinder onBind(Intent intent)
  {
  	// Do nothing.
  	// Keep the complier happy.

  	return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
  	if (Debug) Log.i(TAG, "onStartCommand called: " + intent);

    mSyncThread = new SyncThread();
    mSyncThread.start();

  	return 0;
  }

  @Override
  public void onDestroy()
  {
  	if (Debug) Log.i(TAG, "onDestroy called.");

  	super.onDestroy();

  	mWakeLock.release();
  }

  /**
   * Send new data to the server.
   *
   * @param url The URL of the server.
   * @param data The data to be sent.
   */
  private int updateServer(String url, String data)
  {
  	if (Debug) Log.i(TAG, "updateServer called.");

  	HttpParams mParams = new BasicHttpParams();
    HttpConnectionParams.setConnectionTimeout(mParams, CONNECTION_TIMEOUT);
    HttpConnectionParams.setSoTimeout(mParams, CONNECTION_TIMEOUT);
    HttpClient httpClient = new DefaultHttpClient(mParams);

    try
    {
      HttpPost httpPost = new HttpPost(url);
      httpPost.setHeader("Content-type", "application/json");
      httpPost.setHeader("Authorization", "Basic " + 
                         Base64.encodeToString("admin:admin".getBytes(),
                         Base64.NO_WRAP)); // Oh God. WAT?!

      StringEntity stringEntity = new StringEntity(data);
      stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                                      "application/json"));
      Log.i(TAG, "StringEntity:\n" + stringEntity);
      httpPost.setEntity(stringEntity);

      HttpResponse response = httpClient.execute(httpPost);
      HttpEntity entity = response.getEntity();
      StatusLine status = response.getStatusLine();
      int statusCode = status.getStatusCode();

      if (statusCode == HttpStatus.SC_OK)
      {
        if (Debug) Log.i(TAG, "Status code is " + statusCode + ". Syncing.");

        return 0;
      }
      else
      {
        if (Debug)
          Log.i(TAG, "Status code is " + statusCode + ". Did not sync.");

        mHasError = true;

        return -1;
      }
    }
    catch (Exception e)
    {
      Log.e(TAG, "Exception occured. \n" + e.getMessage());

      mHasError = true;

      return -1;
    }
  }

  /**
   * Perform the syncronisation with the server in a separate thread.
   *
   * @see CancelableThread
   */
  private class SyncThread extends CancelableThread
  {
    public SyncThread()
    {
    	super("SyncThread");
    }

    public void run()
    {
    	if (Debug) Log.i(SyncService.TAG, "Start SyncThread.");

    	int attempt = 0;
    	String latestAccId = "";

    	while (attempt < MAX_RETRIES + 1)
    	{
    		if (Debug) Log.i(SyncService.TAG, "Sync attempt " + (attempt + 1));

    		DefaultHttpClient httpClient =
    		  new DefaultHttpClient(new BasicHttpParams());
    		HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
    			                                        CONNECTION_TIMEOUT);

    		try
    		{
          HttpGet getLatestAcc = new HttpGet(LATEST_ACC_PATH);
          HttpResponse latestAcc = httpClient.execute(getLatestAcc);
          int statusCode = latestAcc.getStatusLine().getStatusCode();

          if(statusCode == HttpStatus.SC_OK)
          {
          	HttpEntity httpEntity = latestAcc.getEntity();
          	if(httpEntity != null)
          	{
          		latestAccId = EntityUtils.toString(httpEntity,
          			                                 SyncService.CHARSET);
          		latestAccId = latestAccId.replace("\n", "").replace("\r", "");
          		if (Debug) Log.i(SyncService.TAG,
          			               "Latest ID in db is " + latestAccId);
          	}
            else
            {
            	if (Debug) Log.i(SyncService.TAG, "Server did not respond");
            	mHasError = true;
            }
          }
          else
          {
          	if (Debug) Log.i(SyncService.TAG,
          		               "Server responded with status code " + 
          		               statusCode);
          	mHasError = true;
          }
    		}
    		catch (Exception e)
    		{
    			Log.e(SyncService.TAG, "Exception occured: " + e.getMessage());
    			mHasError = true;
    		}
    		if (!mHasError)
    		{
    			HubDbHelper db = new HubDbHelper(SyncService.this);

    			ArrayList dataList =
    			  db.getLatestMovement(Integer.parseInt(latestAccId));
          for (Object data : dataList)
          {
            Log.i(TAG, "Sending accelerometer data:\n" + data);
            updateServer(ACC_PATH, data.toString());  
          }
    		}
    		if (mHasError)
    		{
    			if (Debug) Log.i(SyncService.TAG,
    				               "Incrementing attempts to " + attempt +
    				               "and sleeping for " + RETRY_DELAY[attempt - 1]);
    			mHasError = false;
    			attempt++;

    			if (attempt >= MAX_RETRIES)
    				break;
          
          try
          {
          	Thread.sleep(RETRY_DELAY[attempt - 1]);
          }
          catch (InterruptedException e) {}
    		}
    		else
    		{
    			break;
    		}	
    	}
    	if (!mHasError)
        if (Debug) Log.i(SyncService.TAG, "Server successfully updated.");
      else
      	if (Debug) Log.i(SyncService.TAG, "Failed to update server.");
      stopSelf();
    }
  }
}
