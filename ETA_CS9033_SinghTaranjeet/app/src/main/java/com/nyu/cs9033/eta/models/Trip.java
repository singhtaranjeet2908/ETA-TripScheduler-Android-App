package com.nyu.cs9033.eta.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;


public class Trip implements Parcelable {

    // Member fields should exist here, what else do you need for a trip?
	// Please add additional fields

    private long tripId;
	private String name;
    //private String destination;
    private String locationName;
    private String locationAddress;
    private String locationLatitude;
    private String locationLongitude;
    private Date date;
    private String time;
    private String status;
    //array list of 'Person' model objects where each person object represents a friend
    private ArrayList<Person> friends = new ArrayList<Person>();
    //array list of Strings to store the trip location details (location name, address, latitude, longitude) if returned from HW3API
    private ArrayList<String> locationDetails = new ArrayList<String>();


    //Getter methods for member fields

    public long getTripId(){
        return tripId;
    }

    public String getName() {
        return name;
    }

//    public String getDestination() {
//        return destination;
//    }

    public String getLocationName(){
        return locationName;
    }

    public String getLocationAddress(){
        return locationAddress;
    }

    public String getLocationLatitude(){
        return locationLatitude;
    }

    public String getLocationLongitude(){
        return locationLongitude;
    }


    public ArrayList<String> getLocationDetails(){
        return locationDetails;
    }


    public Date getDate() {
        return (Date) this.date.clone();
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public ArrayList<Person> getFriends(){
        return friends;
    }

    //method to get friend names into an arrayList of Strings from a given ArrayList of Person Objects
    public ArrayList<String> getFriendList(ArrayList<Person> friends) {
        ArrayList<String> friendList = new ArrayList<String>();
        for (Person friend : friends) {
            friendList.add(friend.getName());
        }
        return friendList;
    }




    //Setter methods for member fields

    public void setTripId(long tripId){
        this.tripId = tripId;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public void setDestination(String destination) {
//        this.destination = destination;
//    }
    public void setLocationName(String locationName){
        this.locationName = locationName;
    }

    public void setLocationAddress(String locationAddress){
        this.locationAddress = locationAddress;
    }

    public void setLocationLatitude(String locationLatitude){
        this.locationLatitude = locationLatitude;
    }

    public void setLocationLongitude(String locationLongitude){
        this.locationLongitude = locationLongitude;
    }

    public void setLocationDetails(ArrayList<String> locationDetails){
        this.locationName = locationDetails.get(0);
        this.locationAddress = locationDetails.get(1);
        this.locationLatitude = locationDetails.get(2);
        this.locationLongitude = locationDetails.get(3);
    }

    public void setDate(Date date) {
        this.date = (Date) date.clone();
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFriends (ArrayList<Person> friends){
        this.friends = friends;
    }

    /**
	 * Parcelable creator. Do not modify this function.
	 */
	public static final Parcelable.Creator<Trip> CREATOR = new Parcelable.Creator<Trip>() {
		public Trip createFromParcel(Parcel p) {
			return new Trip(p);
		}

		public Trip[] newArray(int size) {
			return new Trip[size];
		}
	};
	
	/**
	 * Create a Trip model object from a Parcel. This
	 * function is called via the Parcelable creator.
	 * 
	 * @param p The Parcel used to populate the
	 * Model fields.
	 */
	public Trip(Parcel p) {
		
		// TODO - fill in here
        this.tripId = p.readLong();
        this.name = p.readString();
        this.locationName = p.readString();
        this.locationAddress = p.readString();
        this.locationLatitude = p.readString();
        this.locationLongitude = p.readString();
        //this.destination = p.readString();
        p.readStringList(locationDetails);
        this.date = new Date(p.readLong());
        this.time = p.readString();
        this.status = p.readString();
        this.friends = new ArrayList<Person>();
        p.readTypedList(friends, Person.CREATOR);

	}


    //default constructor
    public Trip(){

    }


	/**
	 * Create a Trip model object from arguments
	 * 
	 * @param name  Add arbitrary number of arguments to
	 * instantiate Trip class based on member variables.
	 */
	public Trip(String name, ArrayList<String> locationDetails, Date date, String time, ArrayList<Person> friends, String status) {
		
		// TODO - fill in here, please note you must have more arguments here
        this.name = name;
        //this.destination = location;
        this.locationDetails = locationDetails;
        this.date = date;
        this.time = time;
        this.status = status;
        this.friends = friends;
	}

	/**
	 * Serialize Trip object by using writeToParcel. 
	 * This function is automatically called by the
	 * system when the object is serialized.
	 * 
	 * @param dest Parcel object that gets written on 
	 * serialization. Use functions to write out the
	 * object stored via your member variables. 
	 * 
	 * @param flags Additional flags about how the object 
	 * should be written. May be 0 or PARCELABLE_WRITE_RETURN_VALUE.
	 * In our case, you should be just passing 0.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		// TODO - fill in here
        dest.writeLong(tripId);
        dest.writeString(name);
        dest.writeString(locationName);
        dest.writeString(locationAddress);
        dest.writeString(locationLatitude);
        dest.writeString(locationLongitude);
        //dest.writeString(destination);
        dest.writeList(locationDetails);
        dest.writeLong(date.getTime());
        dest.writeString(time);
        dest.writeString(status);
        dest.writeTypedList(friends);
	}
	
	/**
	 * Feel free to add additional functions as necessary below.
	 */
	
	/**
	 * Do not implement
	 */
	@Override
	public int describeContents() {
		// Do not implement!
		return 0;
	}
}
