package com.nyu.cs9033.eta.controllers;

import com.nyu.cs9033.eta.TripDatabaseHelper;
import com.nyu.cs9033.eta.models.Person;
import com.nyu.cs9033.eta.models.Trip;
import com.nyu.cs9033.eta.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewTripActivity extends Activity {

    private static final String TAG = "ViewTripActivity";

    TextView viewName, viewDest, viewDate, viewTime;

    Button schedTrips;
    Button startTrip;
    Button stopTrip;

    private Trip receivedTrip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO - fill in here
        setContentView(R.layout.activity_view);


        schedTrips = (Button) findViewById(R.id.buttonScheduledTrips);
        startTrip = (Button) findViewById(R.id.buttonStartTrip);
        stopTrip = (Button) findViewById(R.id.buttonStopTrip);

        ArrayList<String> viewFriendList = new ArrayList<String>();


        try {
            Intent intent = getIntent();
            Log.d(TAG, "Entered onCreate");
            if (intent != null) {
                Log.d(TAG + ":Create", "Intent is received from TripHistoryActivity");

                //get the trip object from the intent using getTrip() method
                receivedTrip = getTrip(intent);

                //get the list of Person objects from the Trip object and store the friends into an Arraylist
                ArrayList<Person> tripFriends = receivedTrip.getFriends();
                for (Person person : tripFriends) {
                    String friendName = person.getName();
                    viewFriendList.add(friendName);
                }

                //sort the list of friends alphabetically using Java Collections class -'sort' method
                Collections.sort(viewFriendList);


                //display the trip details including list of trip friends on the ViewTrip UI using viewTrip() method
                viewTrip(receivedTrip, viewFriendList);

            }
        } catch (Exception e) {
            Log.e(TAG, "Exception:View:onCreate", e);
        }

        //set OnClick listener on 'Scheduled Trips' button to go back to TripHistoryActivity on click of the button
        schedTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewScheduledTrips();
            }
        });

        /**
         *  set OnClick listener on 'Start Trip' button to mark the currently viewed trip as the "current active trip"
         *  and set that trip status as "active" in the database on click of the button.
         *  The click event on this button also starts the LocationUpdateService which periodically updates the Web Server
         *  with user's current location (obtained via GPS) while there is a "current active trip"
         */

        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check for network connectivity before attempting to start a new trip to ensure network operations are not impacted due to internet unavailability
                if (!isNetworkAvailable()) {
                    //display an alert dialog with error message if there is no network connectivity
                    new AlertDialog.Builder(ViewTripActivity.this).setTitle("NO INTERNET CONNECTIVITY !")
                            .setMessage("Please enable your WIFI or Mobile Data connection.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    finish();
                                }
                            }).show();
                }

                //check for GPS status before starting the network operation via LocationUpdateService
                if (ViewTripActivity.this.isGPSEnabled()) {


                    //check if the currently viewed trip is already a "current active trip"
                    TripDatabaseHelper dbHelper = new TripDatabaseHelper(ViewTripActivity.this);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();

                    Cursor cursor = db.rawQuery("select t_status from trip where _id=?", new String[]{String.valueOf(receivedTrip.getTripId())});
                    Cursor cursor1 = db.rawQuery("select * from trip where t_status=?", new String[]{"active"});
                    if (cursor.moveToFirst() && cursor.getString(0).equals("active")) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "This trip is currently ACTIVE !", Toast.LENGTH_LONG);
                        toast.show();
                        cursor.close();
                    }
                    //check if there is already another "current active trip" in the app
                    else if (cursor1.moveToFirst() && cursor1.getCount() != 0) {
                        //display an alert dialog with error message that another trip is already "active"
                        new AlertDialog.Builder(ViewTripActivity.this).setTitle("ANOTHER ACTIVE TRIP !")
                                .setMessage("Please stop an already ACTIVE trip: " + cursor1.getString(1) + ", using STOP TRIP button.")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).show();
                        cursor1.close();
                    } else {
                        /**
                         * mark the currently viewed trip as the "current active trip" by updating
                         * the trip status as "active" in the database when trip is started
                         */
                        dbHelper.updateTripStatus(receivedTrip, "active");

                        //start the LocationUpdateService to start periodic user location updates for the "current active trip"
                        LocationUpdateService.setServiceAlarm(getApplicationContext(), true);
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "This trip is now ACTIVE. Location Update Service has been started for this trip ! ", Toast.LENGTH_LONG);
                        toast.show();

                    }
                } else {
                    //display an alert dialog with error message if GPS is disabled
                    new AlertDialog.Builder(ViewTripActivity.this).setTitle("GPS IS NOT ENABLED !")
                            .setMessage("Please enable your GPS from Settings menu before starting this trip.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                }

            }
        });

        /**
         *  set OnClick listener on 'Stop Trip' button to mark the currently viewed trip as "inactive"
         *  (if it is the "current active trip")and set that trip status as "inactive" in the database on click of the button.
         *  The click event on this button also stops the LocationUpdateService which periodically updates the Web Server
         *  with user's current location (obtained via GPS) when the trip is no longer "active"
         */

        stopTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TripDatabaseHelper dbHelper = new TripDatabaseHelper(ViewTripActivity.this);
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor cursor = db.rawQuery("select t_status from trip where _id=?", new String[]{String.valueOf(receivedTrip.getTripId())});

                //check if the trip being stopped is not the "current active trip"
                if (cursor.moveToFirst() && cursor.getString(0).equals("inactive")) {
                    //display an alert dialog with error message that the trip is not the "current active trip"
                    new AlertDialog.Builder(ViewTripActivity.this).setTitle("INACTIVE TRIP !")
                            .setMessage("Please start the trip using START TRIP button, before trying to stop it.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                    cursor.close();
                }

                //else currently viewed trip being stopped is the "current active trip"
                else{
                    /**
                     * mark the currently viewed trip as the "inactive" by updating
                     * the trip status as "inactive" in the database when trip is stopped
                     */
                    dbHelper.updateTripStatus(receivedTrip, "inactive");

                    //stop the LocationUpdateService to stop periodic user location updates for the stopped trip
                    LocationUpdateService.setServiceAlarm(getApplicationContext(), false);
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "This trip is now INACTIVE. Location Update Service has been stopped for this trip ! ", Toast.LENGTH_LONG);
                    toast.show();

                }

            }

        });
    }

    /**
     * method to check if the device GPS is enabled or not
     */
    public boolean isGPSEnabled(){

        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        return(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }

    /**
     * method to check if the Internet connectivity is available or not
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return ((networkInfo != null && networkInfo.isConnected()));
    }

    /**
     * Get a Trip object from the 'extra' that
     * was passed to ViewTripActivity via an
     * explicit Intent.
     *
     * @param i The Intent that contains
     *          parceable trip object as an extra.
     * @return The Trip that was passed to TripViewer,
     *          or null if there is none.
     */
    public Trip getTrip(Intent i) {

        // TODO - fill in here
        Log.d(TAG, "Entered getTrip");
        Trip receivedTripObject = i.getParcelableExtra("tripDetails");

        if (receivedTripObject != null) {

            Log.d(TAG, "Exited getTrip success");
            return receivedTripObject;
        } else {
            Log.d(TAG, "Exited getTrip: null returned");
            return null;
        }
    }


    //method to set the height of the 'ListView of Friends' based on the number of children items displayed in it
    public void setListViewHeight(ListView listView) {
        ArrayAdapter arrayAdapter = (ArrayAdapter) listView.getAdapter();
        if (arrayAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            View listItem = arrayAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (arrayAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    /**
     * Populate the View using a Trip model.
     *
     * @param trip       The Trip model used to
     *                   populate the View.
     * @param friendList The ArrayList of friends
     *                   used to populate the View.
     */

    public void viewTrip(Trip trip, List<String> friendList) {

        // TODO - fill in here
        viewName = (TextView) findViewById(R.id.txtTripName);
        viewName.setText(trip.getName());

        viewDest = (TextView) findViewById(R.id.txtTripDest);
        viewDest.setText(trip.getLocationName());

        viewDate = (TextView) findViewById(R.id.txtTripDate);
        //format the date into a user friendly format using an instance of DateFormat class
        DateFormat dateFormat = DateFormat.getDateInstance();
        viewDate.setText(dateFormat.format(trip.getDate().getTime()));

        viewTime = (TextView) findViewById(R.id.txtTripTime);
        viewTime.setText(trip.getTime());

        //populate the ListView of friends on the UI using an ArrayAdapter and ArrayList of friends(data source)
        if(friendList != null) {
            ListView listView = (ListView) findViewById(R.id.friendsJoiningId);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, friendList);
            listView.setAdapter(arrayAdapter);
            setListViewHeight(listView);
        }
    }


    /**
     * This method should take the user back to
     * the TripHistoryActivity on clicking
     * 'Scheduled Trips' button
     */
    public void viewScheduledTrips() {
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


