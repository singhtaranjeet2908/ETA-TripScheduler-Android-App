package com.nyu.cs9033.eta.controllers;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class represents a custom IntentService for handling the asynchronous task of periodically
 * updating the Web Server with current user location obtained via the GPS on a separate handler thread
 * while there is a "current active trip" (set by the "Start Trip" button on ViewTripActivity).
 * This service achieves this task by sending an "UPDATE_LOCATION" JSON Request to the Web Server periodically.
 *
 */

public class LocationUpdateService extends IntentService {

    //time interval between any two consecutive GPS location updates sent to the Web Server
    private static final int POLL_INTERVAL = 1000 * 60; // 1 minute
    private static final String TAG = "LocationUpdateService";

    //URL of the Web Server
    private static final String SERVER_URL = "http://cs9033-homework.appspot.com";


    // flag for GPS status
    boolean isGPSEnabled = false;

    LocationManager locationManager;
    Location location;

    String latitude = "";
    String longitude = "";

    public LocationUpdateService() {
        super(TAG);
    }

    /**
     * method to trigger this IntentService (i.e. LocationUpdateService) after every 1 minute by using
     * AlarmManager system service to send an explicit intent to this IntentService after every 1 minute using a PendingIntent.
     * The AlarmManager system service will be set on and off based on the value of a boolean flag.
     */

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent intent = new Intent(context, LocationUpdateService.class);
        PendingIntent pi = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            //set the alarm
            alarmManager.setRepeating(AlarmManager.RTC,
                    System.currentTimeMillis(), POLL_INTERVAL, pi);

        } else {
            //cancel the alarm
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    /**
     * method to get the current location of the user's device using LocationManager system service and "GPS_PROVIDER"
     * as the location provider and then save this location in a SharedPreferences file on internal storage for subsequent retrieval (if required)
     */
    private Location getCurrentUserLocation() {

        try {
            //Acquire a reference to the system Location Manager
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);


            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!isGPSEnabled) {
                Log.d(TAG, ": GPS is not enabled");
            } else {
                if (location == null) {

                    if (locationManager != null) {
                        //get the last known user location via GPS without using a listener
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            //get the latitude and longitude from the location object
                            latitude = Double.toString(location.getLatitude());
                            longitude = Double.toString(location.getLongitude());

                            //save this last known user location in a SharedPreferences file on internal storage for subsequent retrieval (if required)
                            SharedPreferences.Editor editableObject = getSharedPreferences("currentUserLocation", MODE_PRIVATE).edit();
                            editableObject.putString("latitude", latitude);
                            editableObject.putString("longitude", longitude);
                            //commit for single FS write
                            editableObject.apply();

                        }
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: Getting User Location ", e);
            e.printStackTrace();
        }

        return location;
    }


    /**
     * This overriden method of IntentService handles the incoming intents in a background thread.
     * It performs the task of sending an "UPDATE_LOCATION" JSON Request to the Web Server and getting
     * the response back.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "Service Started !");


        //get the current user location (latitude and longitude) via GPS
        Location currentUserLocation = getCurrentUserLocation();

        if (currentUserLocation != null) {
            double userLatitude = currentUserLocation.getLatitude();
            double userLongitude = currentUserLocation.getLongitude();

            HttpURLConnection connection = null;
            try {

                //create URL and HttpURLConnection objects
                URL url = new URL(SERVER_URL);

                connection = (HttpURLConnection) url.openConnection();

                //set the HTTP Request method to POST
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                //create a JSON Object for the "UPDATE_LOCATION" JSON request
                Log.d(TAG, "Creating UPDATE_LOCATION Request...");
                JSONObject updateLocationRequest = new JSONObject();
                updateLocationRequest.put("command", "UPDATE_LOCATION");
                updateLocationRequest.put("latitude", userLatitude);
                updateLocationRequest.put("longitude", userLongitude);
                updateLocationRequest.put("datetime", System.currentTimeMillis());

                //convert JSON Object to String
                String updateRequest = updateLocationRequest.toString();

                Log.d("UPDATE_LOC_REQUEST", updateLocationRequest.toString());

                //send the "UPDATE_LOCATION" JSON request string to the Web Server and get the JSON response string back
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(updateRequest);
                out.close();

                Log.d(TAG, "Sending UPDATE_LOCATION Request...");

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    Log.d("SERVER_RESPONSE_ERROR: ", connection.getResponseMessage());

                }

                Log.d(TAG, "Retrieving SERVER RESPONSE...");
                InputStream in = connection.getInputStream();
                BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));
                String serverData;
                StringBuilder responseString = new StringBuilder();
                while ((serverData = buffReader.readLine()) != null) {
                    responseString.append(serverData);
                }
                in.close();

                //store the server JSON response string
                String serverResponse = responseString.toString();

                //Log the server JSON response string
                Log.d("SERVER_RESPONSE_SUCCESS", serverResponse);


            } catch (MalformedURLException e) {
                Log.e(TAG, "MalformedURLException: SERVER URL may be invalid", e);
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e(TAG, "JSONException: In creating JSON Object ", e);
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "IOException: Retrieving Response", e);
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } else {

            Log.d(TAG, "Unable to get user location from the GPS: Location is null");

        }

    }

}