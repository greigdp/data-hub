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

  private PowerManager mPowerManager;
  private PowerManager.WakeLock mWakeLock;
  private SyncTemp mSyncTemp;
  private SyncAcc mSyncAcc;
  private SyncLoc mSyncLoc;

  private static final int CONNECTION_TIMEOUT = 10000;
  private static final String LATEST_ACC_PATH =
    "http://sederunt.org/movements/latest";
  private static final String LATEST_TEMP_PATH =
    "http://sederunt.org/temperatures/latest";
  private static final String ACC_PATH =
    "http://sederunt.org/movements/input";
  private static final String TEMP_PATH =
    "http://sederunt.org/temperatures/input";
  private static final String CHARSET = "UTF-8";

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

    mSyncTemp = new SyncTemp();
    mSyncAcc = new SyncAcc();
    mSyncLoc = new SyncLoc();

    mSyncTemp.start();
    mSyncAcc.start();
    mSyncLoc.start();

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

        return -1;
      }
    }
    catch (Exception e)
    {
      Log.e(TAG, "Exception occured. \n" + e.getMessage());

      return -1;
    }
  }
  
  /**
   * Perform the syncronisation of temperature data to the server in a
   * separate thread.
   */
  private class SyncTemp extends Thread
  {
    public SyncTemp()
    {
      // Empty constructor.
    }

    public void run()
    {
      if (Debug) Log.i(SyncService.TAG, "Start SyncTemp.");

      String latestTempId = "";
      DefaultHttpClient httpClient =
        new DefaultHttpClient(new BasicHttpParams());
      HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
                                                  CONNECTION_TIMEOUT);

      try
      {

        HttpGet getLatestTemp = new HttpGet(LATEST_TEMP_PATH);
        HttpResponse latestTemp = httpClient.execute(getLatestTemp);
        int statusCode = latestTemp.getStatusLine().getStatusCode();

        if(statusCode == HttpStatus.SC_OK)
        {
          HttpEntity httpEntity = latestTemp.getEntity();
          if(httpEntity != null)
          {
            latestTempId = EntityUtils.toString(httpEntity,
                                               SyncService.CHARSET);
            latestTempId = latestTempId.replace("\n", "").replace("\r", "");
            if (Debug) Log.i(SyncService.TAG,
                             "Latest ID on server is " + latestTempId);
          }
          else
          {
            if (Debug) Log.i(SyncService.TAG, "Server did not respond.");
          }
        }
        else
        {
          if (Debug) Log.i(SyncService.TAG,
                           "Server responded with status code " + 
                           statusCode);
        }
      }
      catch (Exception e)
      {
        Log.e(SyncService.TAG, "Exception occured: " + e.getMessage());
      }

      HubDbHelper tempDb = new HubDbHelper(SyncService.this);

      ArrayList tempDataList =
        tempDb.getLatestTemperature(Integer.parseInt(latestTempId));

      for (Object data : tempDataList)
      {
        Log.i(TAG, "Sending temperature data:\n" + data);
        updateServer(TEMP_PATH, data.toString());
      }
    }

    public void cancel()
    {
      // Empty method?
    }
  }

  /**
   * Perform the syncronisation of accelerometer data with the server in a
   * separate thread.
   */
  private class SyncAcc extends Thread
  {
    public SyncAcc()
    {
      // Empty constructor?
    }

    public void run()
    {
      if (Debug) Log.i(SyncService.TAG, "Start SyncAcc.");

      String latestAccId = "";
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
                             "Latest ID on server is " + latestAccId);
          }
          else
          {
            if (Debug) Log.i(SyncService.TAG, "Server did not respond.");
          }
        }

        else
        {
          if (Debug) Log.i(SyncService.TAG,
                           "Server responded with status code " + 
                           statusCode);
        }
      }
      catch (Exception e)
      {
        Log.e(SyncService.TAG, "Exception occured: " + e.getMessage());
      }

      HubDbHelper accDb = new HubDbHelper(SyncService.this);

      ArrayList accDataList =
        accDb.getLatestMovement(Integer.parseInt(latestAccId));

      for (Object data : accDataList)
      {
        Log.i(TAG, "Sending accelerometer data:\n" + data);
        updateServer(ACC_PATH, data.toString());
      }
    }

    public void cancel()
    {
      // Empty method?
    }
  }

  /**
   * Perform the syncronisation of location data with the server in a
   * separate thread.
   */
  private class SyncLoc extends Thread
  {
    public SyncLoc()
    {
      // Empty constructor?
    }

    public void run()
    {
      if (Debug) Log.i(SyncService.TAG, "Start SyncThread.");

      String latestLocId = "";
      DefaultHttpClient httpClient =
        new DefaultHttpClient(new BasicHttpParams());
      HttpConnectionParams.setConnectionTimeout(httpClient.getParams(),
                                                  CONNECTION_TIMEOUT);

      try
      {
        HttpGet getLatestLoc = new HttpGet(LATEST_ACC_PATH);
        HttpResponse latestLoc = httpClient.execute(getLatestLoc);
        int statusCode = latestLoc.getStatusLine().getStatusCode();

        if(statusCode == HttpStatus.SC_OK)
        {
          HttpEntity httpEntity = latestLoc.getEntity();
          if(httpEntity != null)
          {
            latestLocId = EntityUtils.toString(httpEntity,
                                               SyncService.CHARSET);
            latestLocId = latestLocId.replace("\n", "").replace("\r", "");
            if (Debug) Log.i(SyncService.TAG,
                             "Latest ID on server is " + latestLocId);
          }
          else
          {
            if (Debug) Log.i(SyncService.TAG, "Server did not respond.");
          }
        }

        else
        {
          if (Debug) Log.i(SyncService.TAG,
                           "Server responded with status code " + 
                           statusCode);
        }
      }
      catch (Exception e)
      {
        Log.e(SyncService.TAG, "Exception occured: " + e.getMessage());
      }

      HubDbHelper locDb = new HubDbHelper(SyncService.this);

      ArrayList locDataList =
        locDb.getLatestLocation(Integer.parseInt(latestLocId));

      for (Object data : locDataList)
      {
        Log.i(TAG, "Sending accelerometer data:\n" + data);
        updateServer(ACC_PATH, data.toString());
      }
    }

    public void cancel()
    {
      // Empty method?
    }
  }
}
