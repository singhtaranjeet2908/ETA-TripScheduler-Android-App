package com.nyu.cs9033.eta.controllers;

import com.nyu.cs9033.eta.TripDatabaseHelper;
import com.nyu.cs9033.eta.models.Person;
import com.nyu.cs9033.eta.models.Trip;
import com.nyu.cs9033.eta.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
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
import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/*This class implements 'DatePickerDialog.OnDateSetListener' to listen for a callback
 when a trip date is selected from the Date Picker Dialog fragment created in this class
  */
public class CreateTripActivity extends Activity implements DatePickerDialog.OnDateSetListener {
	
	private static final String TAG = "CreateTripActivity";

    //URL of the Web Server
    private static final String SERVER_URL = "http://cs9033-homework.appspot.com/";

    //request code used for sending an implicit intent to the Phone's 'Contact Book' App
    private static final int REQUEST_CODE = 1;

    //request code used for sending an implicit intent to the 'HW3API' location finding App
    private static final int LOC_REQUEST_CODE = 2;

    //variable to store trip date
    private Date storeDate;

    //arrayList to store contact names of friends returned from the Phone's 'Contact Book' App
    ArrayList<String> selectedContactList = new ArrayList<String>();

    //arrayList to store trip location details returned from 'HW3API' location finding App
    ArrayList<String> tripLocationDetails = new ArrayList<String>();

    EditText nameTxt, locTxt,timeTxt;
    EditText areaTxt, itemTxt;
    TextView dateTxt;

    Spinner selectedContactSpinner;

    Button addFriendBtn;
    Button searchLocationBtn;
    Button saveTripBtn;
    Button cancelTripBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO - fill in here
        setContentView(R.layout.activity_create);

        //Check for network connectivity before attempting to create a new trip to ensure network operations are not impacted due to internet unavailability
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo == null || !(networkInfo.isConnected())){
            //display an alert dialog with error message if there is no network connectivity
            new AlertDialog.Builder(CreateTripActivity.this).setTitle("NO INTERNET CONNECTIVITY !")
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


        nameTxt = (EditText) findViewById(R.id.tripNameId);
        locTxt = (EditText) findViewById(R.id.tripLocationId);


        locTxt.setInputType(InputType.TYPE_NULL);
        locTxt.setKeyListener(null);
        locTxt.setClickable(false);
        locTxt.setCursorVisible(false);
        locTxt.setFocusable(false);
        locTxt.setFocusableInTouchMode(false);

        dateTxt = (TextView) findViewById(R.id.tripDateId);
        timeTxt = (EditText) findViewById(R.id.tripTimeId);

        areaTxt = (EditText) findViewById(R.id.edtSearchArea);
        itemTxt = (EditText) findViewById(R.id.edtSearchItem);


        searchLocationBtn = (Button) findViewById(R.id.searchLocationButton);
        selectedContactSpinner = (Spinner)findViewById(R.id.spinnerFriendsId);
        addFriendBtn = (Button) findViewById(R.id.addFriendButton);

        saveTripBtn = (Button)findViewById(R.id.saveTripButton);
        cancelTripBtn = (Button)findViewById(R.id.cancelTripButton);


        /*set listener on 'Search Area' editText view to enable 'Search Destination' button
        only when some text is entered in both 'Search Area' & 'Search Item' text fields*/
        areaTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if((!areaTxt.getText().toString().trim().isEmpty())&& (!itemTxt.getText().toString().trim().isEmpty())){
                    searchLocationBtn.setEnabled(true);
                    searchLocationBtn.setTextColor(getResources().getColor(R.color.button_text_color));
                    searchLocationBtn.setBackgroundColor(getResources().getColor(R.color.button_color));
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if((areaTxt.getText().toString().trim().isEmpty())|| (itemTxt.getText().toString().trim().isEmpty())){
                    searchLocationBtn.setEnabled(false);
                    searchLocationBtn.setTextColor(getResources().getColor(R.color.button_text_color_disabled));
                    //searchLocationBtn.setBackgroundColor(0);
                    searchLocationBtn.setBackgroundColor(getResources().getColor(R.color.button_background_color_disabled));
                }

            }
        });

        /*set listener on 'Search Item' editText view to enable 'Search Destination' button
        only when some text is entered in both 'Search Area' & 'Search Item' text fields*/
        itemTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
               if((!itemTxt.getText().toString().trim().isEmpty())&& (!areaTxt.getText().toString().trim().isEmpty())){
                   searchLocationBtn.setEnabled(true);
                   searchLocationBtn.setTextColor(getResources().getColor(R.color.button_text_color));
                   searchLocationBtn.setBackgroundColor(getResources().getColor(R.color.button_color));
               }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if((itemTxt.getText().toString().trim().isEmpty())|| (areaTxt.getText().toString().trim().isEmpty())){
                    searchLocationBtn.setEnabled(false);
                    searchLocationBtn.setTextColor(getResources().getColor(R.color.button_text_color_disabled));
                    //searchLocationBtn.setBackgroundColor(0);
                    searchLocationBtn.setBackgroundColor(getResources().getColor(R.color.button_background_color_disabled));
                }

            }
        });

        /*set listener on 'Trip Name' editText view to enable 'Save Trip' button
        only when some text is entered in all the mandatory fields on the CreateTripActivity layout*/

        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if((!nameTxt.getText().toString().trim().isEmpty())
                        && (!locTxt.getText().toString().trim().isEmpty())
                        && (!dateTxt.getText().toString().trim().equals("Select Date"))
                        && (!timeTxt.getText().toString().trim().isEmpty())
                        && (!selectedContactSpinner.getSelectedItem().toString().trim().isEmpty())){
                    saveTripBtn.setEnabled(true);
                    saveTripBtn.setTextColor(getResources().getColor(R.color.button_text_color));
                    saveTripBtn.setBackgroundColor(getResources().getColor(R.color.button_color));
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if((nameTxt.getText().toString().trim().isEmpty())
                        || (locTxt.getText().toString().trim().isEmpty())
                        || (dateTxt.getText().toString().trim().equals("Select Date"))
                        || (timeTxt.getText().toString().trim().isEmpty())
                        || (selectedContactSpinner.getSelectedItem().toString().trim().isEmpty())){
                    saveTripBtn.setEnabled(false);
                    saveTripBtn.setTextColor(getResources().getColor(R.color.button_text_color_disabled));
                    saveTripBtn.setBackgroundColor(getResources().getColor(R.color.button_background_color_disabled));
                }

            }
        });

        /*set listener on 'Trip Destination' editText view to enable 'Save Trip' button
        only when some text is entered in all the mandatory fields on the CreateTripActivity layout*/
        locTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if((!locTxt.getText().toString().trim().isEmpty())
                        && (!nameTxt.getText().toString().trim().isEmpty())
                        && (!dateTxt.getText().toString().trim().equals("Select Date"))
                        && (!timeTxt.getText().toString().trim().isEmpty())
                        && (!selectedContactSpinner.getSelectedItem().toString().trim().isEmpty())){
                    saveTripBtn.setEnabled(true);
                    saveTripBtn.setTextColor(getResources().getColor(R.color.button_text_color));
                    saveTripBtn.setBackgroundColor(getResources().getColor(R.color.button_color));
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if((locTxt.getText().toString().trim().isEmpty())
                        || (nameTxt.getText().toString().trim().isEmpty())
                        || (dateTxt.getText().toString().trim().equals("Select Date"))
                        || (timeTxt.getText().toString().trim().isEmpty())
                        || (selectedContactSpinner.getSelectedItem().toString().trim().isEmpty())){
                    saveTripBtn.setEnabled(false);
                    saveTripBtn.setTextColor(getResources().getColor(R.color.button_text_color_disabled));
                    saveTripBtn.setBackgroundColor(getResources().getColor(R.color.button_background_color_disabled));
                }


            }
        });

        /*set listener on 'Trip Date' Text view to enable 'Save Trip' button
        only when some text is entered in all the mandatory fields on the CreateTripActivity layout*/
        dateTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

           @Override
           public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if((!dateTxt.getText().toString().trim().equals("Select Date"))
                        && (!nameTxt.getText().toString().trim().isEmpty())
                        && (!locTxt.getText().toString().trim().isEmpty())
                        && (!timeTxt.getText().toString().trim().isEmpty())
                        && (!selectedContactSpinner.getSelectedItem().toString().trim().isEmpty())){
                    saveTripBtn.setEnabled(true);
                    saveTripBtn.setTextColor(getResources().getColor(R.color.button_text_color));
                    saveTripBtn.setBackgroundColor(getResources().getColor(R.color.button_color));
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if((dateTxt.getText().toString().trim().equals("Select Date"))
                        || (nameTxt.getText().toString().trim().isEmpty())
                        || (locTxt.getText().toString().trim().isEmpty())
                        || (timeTxt.getText().toString().trim().isEmpty())
                        || (selectedContactSpinner.getSelectedItem().toString().trim().isEmpty())){
                    saveTripBtn.setEnabled(false);
                    saveTripBtn.setTextColor(getResources().getColor(R.color.button_text_color_disabled));
                    saveTripBtn.setBackgroundColor(getResources().getColor(R.color.button_background_color_disabled));
                }

            }
        });


        /*set listener on 'Friends' spinner to enable 'Save Trip' button
        only when some text is entered in all the mandatory fields on the CreateTripActivity layout*/
        selectedContactSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if((!selectedContactSpinner.getSelectedItem().toString().trim().isEmpty())
                        && (!nameTxt.getText().toString().trim().isEmpty())
                        && (!locTxt.getText().toString().trim().isEmpty())
                        && (!dateTxt.getText().toString().trim().equals("Select Date"))
                        && (!timeTxt.getText().toString().trim().isEmpty())){
                    saveTripBtn.setEnabled(true);
                    saveTripBtn.setTextColor(getResources().getColor(R.color.button_text_color));
                    saveTripBtn.setBackgroundColor(getResources().getColor(R.color.button_color));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if((dateTxt.getText().toString().trim().equals("Select Date"))
                        || (locTxt.getText().toString().trim().isEmpty())
                        || (nameTxt.getText().toString().trim().isEmpty())
                        || (timeTxt.getText().toString().trim().isEmpty())
                        || (selectedContactSpinner.getSelectedItem().toString().trim().isEmpty())){
                    saveTripBtn.setEnabled(false);
                    saveTripBtn.setTextColor(getResources().getColor(R.color.button_text_color_disabled));
                    saveTripBtn.setBackgroundColor(getResources().getColor(R.color.button_background_color_disabled));
                }

            }
        });

        /*set listener on 'Search Destination' button to send an implicit intent to 'HW3API'
         to search for a destination location (as per the search criteria entered by the user)
         from the 'HW3API' location finding App*/
        searchLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchLocation();
            }
        });

        /*set listener on 'Trip Date' textView to show a Date Picker Dialog
         fragment on click of the textView*/
        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        /*set listener on 'Add Friend' button to send an implicit intent
        to pick a contact name from the Phone's 'Contact Book' App on click of the button*/
        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickContact();
            }
        });


         /*set listener on 'Save Trip' button to save the newly entered trip details into the SQLite
         database on click of the button*/
        saveTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                 //get the newly created trip model object using createTrip() method
                 Trip newTrip = createTrip();

                /*
                 * call the AsyncTask to get a trip ID for the newly created trip object from the Web Server
                 * and save the newly created trip object (along with returned trip ID) into the SQLite
                 * database using the saveTrip() method (called by the asyncTask)
                 */
                 //call AsyncTask to perform network operation on separate thread
                 new getTripIdAsyncTask(newTrip).execute(SERVER_URL);

            }
        });


        /*set listener on 'Cancel Trip' button to finish/destroy the CreateTripActivity anytime
         on click of the button by the user*/
        cancelTripBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelTrip();
            }
        });

	}

    //method to display a Date Picker Dialog Fragment to select a date on click of 'Trip Date' TextView
    public void showDatePickerDialog() {
        Log.d(TAG, "Entered showDialog");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = DatePickerDialogFragment.newInstance(CreateTripActivity.this);
        newFragment.show(ft, "Date Fragment");
        Log.d(TAG, "Exited showDialog");
    }

    /**
     * This inner class represents a reusable Date Picker hosted in a Dialog Fragment for 'ETA' app.
     * Code Reference : http://www.codinguser.com/2012/06/time-and-date-inputs-in-android/
     *
     */

    public static class DatePickerDialogFragment extends DialogFragment {

        protected DatePickerDialog.OnDateSetListener mDateSetListener;

        //Empty default constructor to prevent our app from crashing when the device is rotated.
        public DatePickerDialogFragment() {
            // nothing to see here, move along
        }

        //static method to create an instance of the DatePickerDialogFragment in the outer class
        public static DialogFragment newInstance(DatePickerDialog.OnDateSetListener callback){
            Log.d(TAG, "Entered newInstance");
            DatePickerDialogFragment dFragment = new DatePickerDialogFragment();
            dFragment.mDateSetListener = callback;
            Log.d(TAG, "Exited newInstance");
            return dFragment;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar cal = Calendar.getInstance();

            return new DatePickerDialog(getActivity(),mDateSetListener, cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        }
    }


    //method to set the date selected from the Date Picker Dialog into the 'Trip Date' TextView
    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);


        final Calendar now = new GregorianCalendar();


        //check if the user selects a past trip date and show an error
        if ((cal.get(Calendar.YEAR) < now.get(Calendar.YEAR))
            || ((cal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
                &&(cal.get(Calendar.MONTH) < now.get(Calendar.MONTH))
            || ((cal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
                &&(cal.get(Calendar.MONTH) == now.get(Calendar.MONTH))
                &&(cal.get(Calendar.DAY_OF_MONTH) < now.get(Calendar.DAY_OF_MONTH))))){
            dateTxt.setError("cannot select a past date");
            dateTxt.setText("Select Date");
        }

        else {
            //set the valid trip date selected by the user into the 'Trip Date' textView
            DateFormat dateFormat = DateFormat.getDateInstance();
            dateTxt.setText(dateFormat.format(cal.getTime()));
            dateTxt.setError(null);
            storeDate = cal.getTime();

        }
    }


    /**
     * This private inner class represents an AsyncTask which handles the task of sending the "CREATE_TRIP"
     * JSON request to the Web Server in a background thread to get a trip ID for the newly created trip in response
     * and then saves the newly created trip object into the SQLite database using the saveTrip() method of CreateTripActivity.
     *
     */

    private class getTripIdAsyncTask extends AsyncTask<String, Void, String> {

        private Trip tripObject = new Trip();


        protected getTripIdAsyncTask(Trip tripObject) {
            this.tripObject = tripObject;
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
                JSONObject createTripResponse = new JSONObject(result);

                Log.d("Trip ID from Server:", "" + createTripResponse.getLong("trip_id"));

                //get the trip ID from the server response and set it as the trip ID of the newly created trip Object
                tripObject.setTripId(createTripResponse.getLong("trip_id"));

                //call saveTrip() method to save the the newly created trip object into the SQLite database
                if (CreateTripActivity.this.saveTrip(tripObject)) {


                    TripDatabaseHelper dbHelper = new TripDatabaseHelper(CreateTripActivity.this);

                    //get newly saved trip object from the SQLite database
                    Trip dbTrip = dbHelper.getTripObjectFromDB(tripObject.getTripId());

                    //display an alert dialog for successful trip creation along with TRIP ID returned from the server
                    new AlertDialog.Builder(CreateTripActivity.this).setTitle("TRIP " + "'" + tripObject.getName() + "'" + " SAVED SUCCESSFULLY !")
                            .setMessage("Assigned TRIP ID: " + "" + dbTrip.getTripId())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast toast = Toast.makeText(getApplicationContext(),
                                            "Trip " + "'" + tripObject.getName() + "'" + " saved successfully ! ", Toast.LENGTH_LONG);
                                    toast.show();
                                    dialog.cancel();
                                    //destroy/finish the 'Create Trip Activity' after the new trip object is saved successfully into the database
                                    finish();
                                }
                            }).show();
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Failed: Error in saving trip !", Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
                Log.d(TAG, "Exited onPostExecute");
            } catch (JSONException e) {
                Log.e(TAG, "JSONException: JSON Response Creation", e);
                e.printStackTrace();
            }
        }

        //method to send the "CREATE_TRIP" JSON request to the Web Server to get the JSON response string containing the trip ID
        public String getResponseString(String inUrl) throws IOException {

            String createRequest = "";

            //create URL and HttpURLConnection objects
            URL url = new URL(inUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            //set the HTTP Request method to POST
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            //create a JSON Object for the "CREATE_TRIP" JSON request
            try {
                Log.d(TAG, "Creating JSON Request");
                JSONObject createTripRequest = new JSONObject();
                createTripRequest.put("command", "CREATE_TRIP");
                createTripRequest.put("location", new JSONArray(tripObject.getLocationDetails()));
                createTripRequest.put("datetime", tripObject.getDate().getTime());
                createTripRequest.put("people", new JSONArray(tripObject.getFriendList(tripObject.getFriends())));

                //convert JSON Object to String
                createRequest = createTripRequest.toString();

                Log.d("CREATE_TRIP_REQUEST:", createTripRequest.toString());


            }catch (JSONException e) {
                Log.e(TAG, "JSONException: JSON Request Creation", e);
                e.printStackTrace();
            }

           //send the "CREATE_TRIP" JSON request string to the Web Server and get the response string containing the trip ID
            try {
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(createRequest);
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

                //return JSON response string containing the trip ID
                return responseString.toString();
            } finally {
                connection.disconnect(); // happens even if return occurred
            }
        }

    }


    //method to add a friend by sending an implicit intent to pick a contact name from the Phone's 'Contact Book' App
    public void pickContact(){
        Log.d(TAG,"Entered pickContact()");
        Uri uri = Uri.parse("content://contacts");
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, uri);
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(pickContactIntent, REQUEST_CODE);
        Log.d(TAG, "Exited pickContact()");
    }

    /*method to populate the trip location by sending an implicit intent to search for a location
    (as per the search criteria entered by the user) from the 'HW3API' location finding App*/
    public void searchLocation(){
        Log.d(TAG,"Entered searchLocation()");
        Uri uri = Uri.parse("location://com.example.nyu.hw3api");
        Intent searchLocationIntent = new Intent(Intent.ACTION_VIEW, uri);
        searchLocationIntent.putExtra("searchVal", areaTxt.getText() + "::" + itemTxt.getText());
        startActivityForResult(searchLocationIntent, LOC_REQUEST_CODE);
        Log.d(TAG, "Exited searchLocation()");
    }

    /*method to get the result intents back from Phone's 'Contact Book' App as well as 'HW3API' location finding App
    and display the returned data into appropriate views on 'CreateTripActivity' UI*/

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //result returned from the Phone's 'Contacts' app
        if(data != null){

            if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){

                    Log.d(TAG, "intent not null");
                    Uri contactUri = data.getData(); //gets Uri from Intent Object containing the result
                    if (contactUri != null ){
                        Log.d(TAG, "returned URI not null");
                        Cursor cursor = null;
                        String[] queryFields = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
                        try {
                            cursor = getContentResolver().query(contactUri, queryFields, null, null, null);
                            if (cursor.getCount() == 0) {
                                cursor.close();
                                return;
                            }
                            Log.d(TAG, "cursor is not null");
                            cursor.moveToFirst();
                            String name = cursor.getString(0);
                            if (name != null) {
                                //check to verify if the user enters a duplicate friend and show an error via a Toast widget
                                if (selectedContactList.contains(name)){
                                        Toast toast = Toast.makeText(getApplicationContext(), "Duplicate Friend: " + name + " !", Toast.LENGTH_LONG);
                                        toast.show();
                                }
                                else {
                                    selectedContactList.add(name);
                                    Log.d(TAG, "name is " + name);
                                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, selectedContactList);
                                    selectedContactSpinner = (Spinner) findViewById(R.id.spinnerFriendsId);
                                    selectedContactSpinner.setAdapter(arrayAdapter);

                                    /*show a success message error via a Toast widget when a
                                    selected contact from Phone's 'Contacts' app is added
                                    successfully into the 'Friends' spinner*/

                                    Toast toast = Toast.makeText(getApplicationContext(), "Friend: " + "'" + name + "' added successfully!", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        }catch(Exception e){
                            Log.e(TAG, "Exception:addFriend", e);
                        }
                        finally {
                            if(cursor != null){
                                cursor.close();
                            }
                        }
                    }
                }
            //result returned from the 'HW3API' app
            if(requestCode == LOC_REQUEST_CODE){
                //store the trip location details returned from HW3API into a class-level arrayList
                tripLocationDetails = data.getStringArrayListExtra("retVal");
                if(!(tripLocationDetails.isEmpty())){

                    locTxt.setText(tripLocationDetails.get(0));

                    areaTxt.setText("");
                    itemTxt.setText("");
                    searchLocationBtn.setEnabled(false);
                    dateTxt.requestFocus();
                }


            }

        }
        //else intent returned from either of the "Phone Contacts App" or "HW3API" is null, so display a user notification via Toast
        else{
            Toast toast = Toast.makeText(getApplicationContext(), "NO VALUE SELECTED !", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

	/**
	 * This method should be used to
	 * instantiate a Trip model object.
	 * 
	 * @return The Trip as represented
	 * by the View.
	 */
	public Trip createTrip() {
	
		// TODO - fill in here
        Log.d(TAG, "Creating a Trip from User Input");
        nameTxt = (EditText) findViewById(R.id.tripNameId);
        String tripName = nameTxt.getText().toString();

        /*get the trip location details returned from the HW3API app
         and store them into an ArrayList*/
        ArrayList<String> locationDetailsList = new ArrayList<String>();
        if(tripLocationDetails != null){
            for (String locDet : tripLocationDetails){
                locationDetailsList.add(locDet);
                Log.d("Values locdetailslist:", locDet);
            }
        }


        dateTxt = (TextView) findViewById(R.id.tripDateId);
        Date tripDate = storeDate;

        timeTxt = (EditText) findViewById(R.id.tripTimeId);
        String tripTime = timeTxt.getText().toString();

        /*get the list of friends/contacts entered into the 'Friends' spinner
         and create a Person object out of each and store all Person objects
         into an ArrayList*/
        ArrayList<Person> friendsList= new ArrayList<Person>();
        if(selectedContactList != null){
            for (String name: selectedContactList){
                Person newFriend = new Person();
                newFriend.setName(name);
                friendsList.add(newFriend);
            }
        }



        //instantiate a trip object from the trip details entered on the CreateTripActivity View/UI
        Trip tripObject = new Trip(tripName, locationDetailsList, tripDate, tripTime, friendsList, "inactive");

        //set the trip location details variables for the newly created trip using the arrayList of location details
        tripObject.setLocationDetails(locationDetailsList);

        Log.d(TAG, "Exited createTrip");

        return tripObject;
	}


	/**
	 * This method will store a trip into
     * the SQLite database by inserting the trip details
     * and the corresponding trip friends and trip location details
     * into the respective database tables
	 * 
	 * @return whether the Trip was successfully 
	 * saved into the database
	 */
	public boolean saveTrip(Trip trip) {

        // TODO - fill in here
        if (trip != null){

            Log.d(TAG, "Entered saveTrip");
            TripDatabaseHelper tripDatabaseHelper = new TripDatabaseHelper(CreateTripActivity.this);

            //save the trip details (other than friends) of the newly created trip into the 'trip' table of 'trips' database
            long tripID = tripDatabaseHelper.insertTrip(trip);

            Log.d("Trip ID saved is:", "" + tripID );

            //save the trip location details related to the newly created trip into the 'locDetails' table of 'trips' database
            tripDatabaseHelper.insertLocationDetails(tripID, trip);

            Log.d("Trip Loc Name Saved:", "" + trip.getLocationName());

            //save the friends related to the newly created trip into the 'person' table of 'trips' database
            ArrayList<Person> personList = trip.getFriends();
            for (Person newPerson : personList){
                tripDatabaseHelper.insertPerson(tripID, newPerson);
                Log.d(TAG, "Inserted Person Object is: " + newPerson.getName());
            }

            tripDatabaseHelper.close();

            Log.d(TAG, "Exited saveTrip");
            return true;
        }
        return false;
	}

	/**
	 * This method should be used when a
	 * user wants to cancel the creation of
	 * a Trip.
	 * 
	 * Note: You most likely want to call this
	 * if your activity dies during the process
	 * of a trip creation or if a cancel/back
	 * button event occurs. Should return to
	 * the previous activity without a result
	 * using finish() and setResult().
	 */
	public void cancelTrip() {
	
		// TODO - fill in here
        setResult(Activity.RESULT_CANCELED, getIntent());
        Toast toast = Toast.makeText(getApplicationContext(),
                "Trip creation cancelled !", Toast.LENGTH_LONG);
        toast.show();
     	finish();
    }
}
