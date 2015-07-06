package com.nyu.cs9033.eta;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.nyu.cs9033.eta.models.Person;
import com.nyu.cs9033.eta.models.Trip;


import java.util.ArrayList;
import java.util.Date;


/**
 * This class represents the 'trips' SQLite database for the 'ETA' trip scheduler app
 * and implements various methods to support storage, modification and retrieval
 * of trip details (trip location details and trip friends) for the trips created using the app
 */
public class TripDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "TripDatabaseHelper";

    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_NAME = "trips";

    //global variables for 'trip' table
    private static final String TABLE_TRIP = "trip";
    private static final String COLUMN_TRIP_ID = "_id"; // convention
    private static final String COLUMN_TRIP_NAME = "t_name";
    private static final String COLUMN_TRIP_DATE = "t_date";
    private static final String COLUMN_TRIP_TIME = "t_time";
    private static final String COLUMN_TRIP_STATUS = "t_status";

    //global variables for 'person' table
    private static final String TABLE_PERSON = "person";
    private static final String COLUMN_PERSON_TRIPID = "trip_id";
    private static final String COLUMN_PERSON_NAME = "p_name";

    //global variables for 'locDetails' table
    private static final String TABLE_LOCATION = "locDetails";
    private static final String COLUMN_LOC_TRIPID = "loc_trip_id";
    private static final String COLUMN_LOC_NAME = "loc_name";
    private static final String COLUMN_LOC_ADDRESS = "address";
    private static final String COLUMN_LOC_LAT = "latitude";
    private static final String COLUMN_LOC_LONG = "longitude";
    private static final String COLUMN_LOC_PROVIDER = "provider";


    public TripDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /*This method is invoked whenever a TripDatabaseHelper object is created
    and is used to create the required database tables*/
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create trip table
        Log.d(TAG, "Entered DB onCreate");
        db.execSQL("CREATE TABLE " + TABLE_TRIP + "("
                + COLUMN_TRIP_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_TRIP_NAME + " text, "
                + COLUMN_TRIP_DATE + " integer, "
                + COLUMN_TRIP_TIME + " text, "
                + COLUMN_TRIP_STATUS + " text)");

        //create locDetails table
        db.execSQL("create table " + TABLE_LOCATION + "("
                + COLUMN_LOC_TRIPID + " integer references trip(_id), "
                + COLUMN_LOC_NAME + " text, "
                + COLUMN_LOC_ADDRESS + " text, "
                + COLUMN_LOC_LAT + " text, "
                + COLUMN_LOC_LONG + " text, "
                + COLUMN_LOC_PROVIDER + " text)");

        //create person table
        db.execSQL("create table " + TABLE_PERSON + "("
                + COLUMN_PERSON_TRIPID + " integer references trip(_id), "
                + COLUMN_PERSON_NAME + " text)");
        Log.d(TAG, "Exited DB onCreate");


    }

    /*This method is invoked whenever the database is upgraded
    and requires the old database version to be upgraded and new database version*/
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSON);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP);

        // create tables again
        onCreate(db);
    }

    //method to insert the trip details into the 'trip' table and get the trip ID of the inserted row
    public long insertTrip(Trip trip) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TRIP_ID, trip.getTripId());
        Log.d(TAG, "DBTripID:" + trip.getTripId());
        cv.put(COLUMN_TRIP_NAME, trip.getName());
        cv.put(COLUMN_TRIP_DATE, trip.getDate().getTime());
        cv.put(COLUMN_TRIP_TIME, trip.getTime());
        cv.put(COLUMN_TRIP_STATUS, trip.getStatus());
        // return id of new trip
        return getWritableDatabase().insert(TABLE_TRIP, null, cv);
    }

    /*method to insert the location details related to a particular trip into the 'locDetails' table
    based on the ID of the trip*/
    public void insertLocationDetails(long tripId, Trip trip) {

        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("insert into " + TABLE_LOCATION +
                "(" + COLUMN_LOC_TRIPID + ", "
                + COLUMN_LOC_NAME + ", "
                + COLUMN_LOC_ADDRESS + ", "
                + COLUMN_LOC_LAT + ", "
                + COLUMN_LOC_LONG + ", "
                + COLUMN_LOC_PROVIDER + ")"
                + " values(" + tripId + ", '"
                + trip.getLocationName() + "' , '"
                + trip.getLocationAddress() + "' , '"
                + trip.getLocationLatitude() + "' , '"
                + trip.getLocationLongitude() + "' , 'HW3API')");
    }

    /*method to insert a single person(friend) related to a particular trip into the 'person' table
    based on the ID of the trip*/
    public void insertPerson(long tripId, Person person) {

        SQLiteDatabase database = this.getWritableDatabase();
        database.execSQL("insert into " + TABLE_PERSON + "(" + COLUMN_PERSON_TRIPID + ", " + COLUMN_PERSON_NAME + ")"
                + " values(" + tripId + ", '" + person.getName() + "')");
    }


    //method to get the ArrayList of location details related to a single trip based on the ID of the trip stored in the database
    public ArrayList<String> getLocationDetailsFromDB(long tripId) {
        ArrayList<String> locDetails = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_LOCATION, null);
            if (cursor.getCount() != 0) {
                // loop through all query results
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    if (cursor.getLong(0) == tripId) {
                        locDetails.add(cursor.getString(1));
                        locDetails.add(cursor.getString(2));
                        locDetails.add(cursor.getString(3));
                        locDetails.add(cursor.getString(4));
                        locDetails.add(cursor.getString(5));
                    }

                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getLocationDetailsFromDB: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return locDetails;
    }


    //method to get the ArrayList of friends/persons related to a single trip based on the ID of the trip stored in the database
    public ArrayList<Person> getTripFriendsFromDB(long tripId) {
        ArrayList<Person> friendList = new ArrayList<Person>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_PERSON, null);
            if (cursor.getCount() != 0) {
                // loop through all query results
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    if (cursor.getLong(0) == tripId) {
                        Person person = new Person();
                        person.setName(cursor.getString(1));
                        friendList.add(person);
                    }

                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getTripFriendsFromDB: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return friendList;
    }

    //method to get the Trip Object from the trip details based on the trip ID of the trip stored in the database
    public Trip getTripObjectFromDB(long tripId) {
        Trip trip = new Trip();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from " + TABLE_TRIP, null);
            if (cursor.getCount() != 0) {

                // loop through all query results
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                    if ((cursor.getLong(0) == tripId)) {

                        trip.setTripId(cursor.getLong(0));
                        trip.setName(cursor.getString(1));
                        ArrayList<String> tripLocDetails = this.getLocationDetailsFromDB(cursor.getLong(0));
                        trip.setLocationDetails(tripLocDetails);
                        //trip.setLocationName(tripLocDetails.get(0));
                        //trip.setLocationAddress(tripLocDetails.get(1));
                        //trip.setLocationLatitude(tripLocDetails.get(2));
                        //trip.setLocationLongitude(tripLocDetails.get(3));
                        trip.setDate(new Date(cursor.getLong(2)));
                        trip.setTime(cursor.getString(3));
                        trip.setStatus(cursor.getString(4));
                        trip.setFriends(this.getTripFriendsFromDB(cursor.getLong(0)));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getTripObjectFromDB: ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return trip;
    }


    /**
     * method to update the status of a given trip in the 'trip' table based on the trip ID of the trip.
     * NOTE: parameter 'tripStatus' can only be "active" or "inactive".
     */
    public int updateTripStatus(Trip trip, String tripStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TRIP_STATUS, tripStatus);
        return db.update("trip", cv, COLUMN_TRIP_ID + "=?",
                new String[]{String.valueOf(trip.getTripId())});
    }

    /**
     * method to get the trip ID of the "current Active Trip" from the database
     */

    public long getActiveTripIDFromDB() {
        long activeTripID = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select _id from trip where t_status=?", new String[]{"active"});
        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            activeTripID = cursor.getLong(0);
            cursor.close();
            db.close();
        }
        return activeTripID;
    }
}
