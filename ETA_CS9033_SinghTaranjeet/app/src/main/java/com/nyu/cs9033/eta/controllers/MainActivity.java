package com.nyu.cs9033.eta.controllers;

import com.nyu.cs9033.eta.TripDatabaseHelper;
import com.nyu.cs9033.eta.R;
import com.nyu.cs9033.eta.models.Trip;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    //URL of the Web Server
    private static final String SERVER_URL = "http://cs9033-homework.appspot.com/";

    Button scheduleTripBtn;
    Button viewTripsBtn;
    Button activeTripStatusBtn;
    TextView txtTripStatus;
    private Trip activeTripObject = new Trip();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Entered onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO - fill in here
        txtTripStatus = (TextView) findViewById(R.id.lblTripStatus);
        scheduleTripBtn = (Button) findViewById(R.id.create_button);
        viewTripsBtn = (Button) findViewById(R.id.view_button);
        activeTripStatusBtn = (Button) findViewById(R.id.activeTripStatus);

        //get current active trip from SQLite database and display it on MainActivity View
        TripDatabaseHelper dbHelper = new TripDatabaseHelper(this);
        long activeTripId = dbHelper.getActiveTripIDFromDB();
        if (activeTripId != 0) {
            activeTripObject = dbHelper.getTripObjectFromDB(activeTripId);
            txtTripStatus.setText("CURRENT ACTIVE TRIP: " + activeTripObject.getName());
            txtTripStatus.setTextColor(getResources().getColor(R.color.active_trip_text_color));

            //start the LocationUpdateService to start periodic user location updates while there is a "current active trip"
            LocationUpdateService.setServiceAlarm(getApplicationContext(), true);
            Toast toast = Toast.makeText(getApplicationContext(),
                    "'" + activeTripObject.getName() + "'" + " is currently ACTIVE. Location Update Service is in progress for this trip ! ", Toast.LENGTH_LONG);
            toast.show();

        } else {
            activeTripObject = null;
            txtTripStatus.setText("NO ACTIVE TRIP !");
        }


        //set OnClick listener on 'Schedule Trip' button to display CreateTripActivity on click of the button
        scheduleTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreateTripActivity();
            }
        });

        //set OnClick listener on 'View Scheduled Trips' button to display TripHistoryActivity on click of the button
        viewTripsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTripHistoryActivity();
            }
        });
        Log.d(TAG, "Exited onCreate");

        //set OnClick listener on 'Active Trip Status' button to display the trip status for the "current active trip" on click of the button
        activeTripStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check for network connectivity before attempting to view the "current active trip" status to ensure network operations are not impacted due to internet unavailability
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo == null || !(networkInfo.isConnected())) {
                    //display an alert dialog with error message if there is no network connectivity
                    new AlertDialog.Builder(MainActivity.this).setTitle("NO INTERNET CONNECTIVITY !")
                            .setMessage("Please enable your WIFI or Mobile Data connection.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();

                                }
                            }).show();

                }


                /*
                 * call the AsyncTask to get the status of "current active trip" from the Web Server
                 * and display it in a user friendly format via a Toast.
                 */
                //call AsyncTask to perform network operation on separate thread
                else{
                    if(activeTripObject != null){
                        new getTripStatusAsyncTask(activeTripObject).execute(SERVER_URL);
                    }
                    else{
                        //display an alert dialog with error message if there is no "current active trip"
                        new AlertDialog.Builder(MainActivity.this).setTitle("NO ACTIVE TRIP !")
                                .setMessage("Please start a trip from the scheduled trips first to make it ACTIVE.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();

                                    }
                                }).show();
                    }
                }


            }


        });
    }

    @Override
    protected void onRestart(){
        super.onRestart();

        /**
         * get current active trip from SQLite database and display it on MainActivity View
         * when the activity is resumed/restarted after starting a trip (or marking it as "current active trip")
         * from ViewTripActivity
         */

        TripDatabaseHelper dbHelper = new TripDatabaseHelper(this);
        long activeTripId = dbHelper.getActiveTripIDFromDB();
        if (activeTripId != 0) {
            activeTripObject = dbHelper.getTripObjectFromDB(activeTripId);
            txtTripStatus.setText("CURRENT ACTIVE TRIP: " + activeTripObject.getName());
            txtTripStatus.setTextColor(getResources().getColor(R.color.active_trip_text_color));

        } else {
            activeTripObject = null;
            txtTripStatus.setText("NO ACTIVE TRIP !");
            txtTripStatus.setTextColor(getResources().getColor(R.color.inactive_trip_text_color));
        }
    }

    /**
     * This private inner class represents an AsyncTask which handles the task of sending the "TRIP_STATUS"
     * JSON request to the Web Server in a background thread to get the status/information of each person related to
     * the "current active trip" in response.
     * NOTE: The trip ID of the "current active trip" is obtained via a query to the app's SQLite database.
     *
     */

    private class getTripStatusAsyncTask extends AsyncTask<String, Void, String> {

        private Trip activeTripObj = new Trip();

        protected getTripStatusAsyncTask(Trip activeTripObj) {
            this.activeTripObj = activeTripObj;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                return getResponseString(urls[0]);
            } catch (IOException e) {
                Log.e(TAG, "IOException: Retrieving Response", e);
                e.printStackTrace();
                return "Unable to retrieve response string from the server. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "Entered onPostExecute");
            Log.d("SERVER_RESPONSE_SUCCESS", result);

            try {
                //create a JSON Object from the successful response string returned by the server
                JSONObject tripStatusResponse = new JSONObject(result);

                //get the status information of each person related to the "current active trip" from the server JSON response Object
                JSONArray peopleList = tripStatusResponse.getJSONArray("people");
                JSONArray distanceLeftList = tripStatusResponse.getJSONArray("distance_left");
                JSONArray timeLeftList = tripStatusResponse.getJSONArray("time_left");

                int timeInMin1 = timeLeftList.getInt(0)/60;
                int timeInMin2 = timeLeftList.getInt(1)/60;

                //format the trip status information to convert it into user friendly readable units
                String formattedTripStatus = String.format(Locale.US,
                        "%s is %5.2f miles away from the destination (reaching in %d min).\n%s is %5.2f miles away from the destination (reaching in %d min).",
                        peopleList.getString(0), distanceLeftList.getDouble(0), timeInMin1, peopleList.getString(1), distanceLeftList.getDouble(1), timeInMin2);

                //display the formatted Trip Status to the user in a Toast
                Toast toast = Toast.makeText(getApplicationContext(),
                        "ACTIVE TRIP: " + activeTripObject.getName() + ". "+ "\n" + formattedTripStatus +"", Toast.LENGTH_LONG);
                    toast.show();

                Log.d(TAG, "Exited onPostExecute");
            } catch (JSONException e) {
                Log.e(TAG, "JSONException: JSON Response Creation", e);
                e.printStackTrace();
            }
        }

        //method to send the "TRIP_STATUS" JSON request to the Web Server to get the JSON response string containing the information related to "current active trip"
        public String getResponseString(String inUrl) throws IOException {

            String tripStatusRequest = "";

            //create URL and HttpURLConnection objects
            URL url = new URL(inUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            //set the HTTP Request method to POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            //create a JSON Object for the "TRIP_STATUS" JSON request
            try {
                Log.d(TAG, "Creating JSON Request");
                JSONObject statusRequest = new JSONObject();
                statusRequest.put("command", "TRIP_STATUS");
                //activeTripObj has already been obtained via a query to the SQLite database when calling this AsyncTask from MainTripActivity
                statusRequest.put("trip_id", activeTripObj.getTripId());


                //convert JSON Object to String
                tripStatusRequest = statusRequest.toString();

                Log.d("TRIP_STATUS_REQUEST:", statusRequest.toString());


            }catch (JSONException e) {
                Log.e(TAG, "JSONException: JSON Request Creation", e);
                e.printStackTrace();
            }

            //send the "TRIP_STATUS" JSON request string to the Web Server and get the response string containing the information related to "current active trip"
            try {
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(tripStatusRequest);
                out.close();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    Log.d("SERVER_RESPONSE_ERROR: ", connection.getResponseMessage());
                    return null;
                }

                InputStream in = connection.getInputStream();
                BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));
                String serverData;
                StringBuilder responseString = new StringBuilder();
                while((serverData = buffReader.readLine())!= null) {
                    responseString.append(serverData);
                }
                in.close();

                //return JSON response string  containing the information related to "current active trip"
                return responseString.toString();
            } finally {
                connection.disconnect(); // happens even if return occurred
            }
        }

    }


    /**
     * This method should start the
     * Activity responsible for creating/scheduling
     * a new Trip.
     */
    public void startCreateTripActivity() {

        // TODO - fill in here
        //create an explicit intent to invoke CreateTripActivity
        Intent createTripIntent = new Intent(this, CreateTripActivity.class);
        startActivity(createTripIntent);
    }


    /**
     * This method should start the
     * Activity responsible for viewing
     * the list of all scheduled trips stored within
     * the database.
     */
    public void startTripHistoryActivity() {

        // TODO - fill in here
        TripDatabaseHelper dbHelper = new TripDatabaseHelper(this);

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("select * from trip", null);

        if (cursor.getCount() == 0) {
            cursor.close();
            db.close();

            //display an alert dialog with error message if there are no trips scheduled and saved in the database yet
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("NO TRIPS SCHEDULED YET !");
            builder.setMessage("Please schedule a trip using 'Schedule Trip' button.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create();
            builder.show();
        }
        else {

            //create an explicit intent to invoke TripHistoryActivity
            Intent listTripsIntent = new Intent(getBaseContext(), TripHistoryActivity.class);
            startActivity(listTripsIntent);
        }
    }


}
