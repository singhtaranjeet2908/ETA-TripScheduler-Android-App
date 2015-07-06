package com.nyu.cs9033.eta.adaptors;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;

import com.nyu.cs9033.eta.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * This class represents a custom CursorAdapter that fits in between a Cursor (data source from SQLite query)
 * and the ListView (visual representation) and configures two aspects:
 * Which layout template to inflate for an item.
 * Which fields of the cursor to bind to views in the template.
 *
 * This is used to populate the ListViews that display the lists of current, future and past
 * scheduled trips respectively on TripHistoryActivity UI.
 *
 */
public class ListViewCursorAdapter extends CursorAdapter {


    private LayoutInflater layoutInflater;

    public ListViewCursorAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    // this method is used to inflate a new view and return it
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.custom_list_item, parent, false);
    }

    //this method is used to bind the required data from the cursor to the given view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtTripName = (TextView) view.findViewById(R.id.item_tripName);
        TextView txtTripDate = (TextView) view.findViewById(R.id.item_tripDate);

        String itemTripName = cursor.getString(cursor.getColumnIndexOrThrow("t_name"));
        long itemTripDate = cursor.getLong(cursor.getColumnIndexOrThrow("t_date"));

        final Date date = new Date(itemTripDate);

        DateFormat dateFormat = DateFormat.getDateInstance();

        txtTripName.setText(itemTripName);

        //format the trip date into a user friendly format and display it on the view/UI
        txtTripDate.setText(dateFormat.format(date.getTime()));

        }

    }

