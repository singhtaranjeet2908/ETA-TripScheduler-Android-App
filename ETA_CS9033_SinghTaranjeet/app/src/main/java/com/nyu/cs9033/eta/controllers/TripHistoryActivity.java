package com.nyu.cs9033.eta.controllers;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.nyu.cs9033.eta.R;
import com.nyu.cs9033.eta.TripDatabaseHelper;
import com.nyu.cs9033.eta.adaptors.ListViewCursorAdapter;
import com.nyu.cs9033.eta.models.Trip;



/**
 * This class implements the methods to show a list of all trips stored within the database.
 * When clicking on a Trip within the list, you get to view the trip details within the ViewTripActivity.
 *
 * This class categorizes the trips based on trips that happened in the past, upcoming
 * trips in the future, and trips that are occurring today.
 */
public class TripHistoryActivity extends Activity {

    Button homebtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);

        homebtn = (Button) findViewById(R.id.buttonHome);

        //TripDatabaseHelper is a SQLiteOpenHelper class connecting to SQLite database
        final TripDatabaseHelper dbHelper = new TripDatabaseHelper(this);

        //Get access to the underlying write able database
        SQLiteDatabase db = dbHelper.getWritableDatabase();



        //Query for today's trips from the database and get a cursor back
        Cursor currentItemCursor = db.rawQuery("select * from trip where date(datetime(t_date/1000, 'unixepoch', 'localtime')) = date('now') order by t_date", null);

        //Query for future trips from the database and get a cursor back
        Cursor futureItemCursor = db.rawQuery("select * from trip where date(datetime(t_date/1000, 'unixepoch', 'localtime')) > date('now') order by t_date", null);

        //Query for past trips from the database and get a cursor back
        Cursor pastItemCursor = db.rawQuery("select * from trip where date(datetime(t_date/1000, 'unixepoch', 'localtime')) < date('now') order by t_date", null);



        //Find ListView to populate with today's trips
        ListView tripListView1 = (ListView) findViewById(R.id.listPresent);

        //Find ListView to populate with future trips
        ListView tripListView2 = (ListView) findViewById(R.id.listFuture);

        //Find ListView to populate with past trips
        ListView tripListView3 = (ListView) findViewById(R.id.listPast);

        // Setup cursor adapters using cursors from previous steps using a custom cursor adapter class 'ListViewCursorAdapter
        ListViewCursorAdapter cursorAdapter1 = new ListViewCursorAdapter(this, currentItemCursor, 0);
        ListViewCursorAdapter cursorAdapter2 = new ListViewCursorAdapter(this, futureItemCursor, 0);
        ListViewCursorAdapter cursorAdapter3 = new ListViewCursorAdapter(this, pastItemCursor, 0);

        //Attach cursor adapters to the respective ListViews
        tripListView1.setAdapter(cursorAdapter1);
        tripListView2.setAdapter(cursorAdapter2);
        tripListView3.setAdapter(cursorAdapter3);

        //call methods on all three ListViews respectively to adjust the height of the ListViews as per their children count
        setListViewHeight(tripListView1);
        setListViewHeight(tripListView2);
        setListViewHeight(tripListView3);

        //set OnClick listener on 'Home' button to go back to the MainActivity on click of the button
        homebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goHome();
            }
        });

        /*set an OnItemClickListener on the ListView of 'current trips' to get the ID of the trip
         clicked by the user. This trip ID is used to retrieve respective trip details
         from the database to create a Trip object which is sent to the ViewTripActivity
         via an explicit intent*/

        tripListView1.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Trip tripObject = dbHelper.getTripObjectFromDB(id);

                Intent viewTripIntent = new Intent(getBaseContext(), ViewTripActivity.class);
                viewTripIntent.putExtra("tripDetails", tripObject);
                startActivity(viewTripIntent);
            }
        });

        /*set an OnItemClickListener on the ListView of 'future trips' to get the ID of the trip
         clicked by the user. This trip ID is used to retrieve respective trip details
         from the database to create a Trip object which is sent to the ViewTripActivity
         via an explicit intent*/

        tripListView2.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Trip tripObject = dbHelper.getTripObjectFromDB(id);

                Intent viewTripIntent = new Intent(getBaseContext(), ViewTripActivity.class);
                viewTripIntent.putExtra("tripDetails", tripObject);
                startActivity(viewTripIntent);
            }
        });


        /*set an OnItemClickListener on the ListView of 'past trips' to get the ID of the trip
         clicked by the user. This trip ID is used to retrieve respective trip details
         from the database to create a Trip object which is sent to the ViewTripActivity
         via an explicit intent*/

       tripListView3.setOnItemClickListener(new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Trip tripObject = dbHelper.getTripObjectFromDB(id);

            Intent viewTripIntent = new Intent(getBaseContext(), ViewTripActivity.class);
            viewTripIntent.putExtra("tripDetails", tripObject);
            startActivity(viewTripIntent);
        }
    });

        dbHelper.close();

    }

    //method to set the height of a ListView based on the number of children items displayed in it
    public void setListViewHeight(ListView listView) {
        CursorAdapter cursorAdapter = (CursorAdapter) listView.getAdapter();
        if (cursorAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < cursorAdapter.getCount(); i++) {
            View listItem = cursorAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (cursorAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    /**
     * This method should take the user back to
     * the MainActivity on clicking 'Home' button
     */
    public void goHome() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trip_history, menu);
        return true;
    }

    @Override
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
