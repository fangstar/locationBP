package com.mfcoding.locationBP.UI.fragments;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfcoding.locationBP.PlacesConstants;
import com.mfcoding.locationBP.R;
import com.mfcoding.locationBP.content_providers.LocationsContentProvider;

public class LocationFragment extends Fragment implements
    LoaderCallbacks<Cursor> {
  static final String TAG = "LocationFragment";

  /**
   * Factory that return a new instance of the {@link LocationFragment}
   * 
   * @param
   * @return A new LocationFragment
   */
  // public static LocationFragment newInstance(String[] location) {
  // LocationFragment f = new LocationFragment();
  //
  // // Supply id input as an argument.
  // Bundle args = new Bundle();
  // args.putString(PlacesConstants.ARGUMENTS_KEY_LATITUDE, location[0]);
  // args.putString(PlacesConstants.ARGUMENTS_KEY_LONGITUDE, location[1]);
  // f.setArguments(args);
  //
  // return f;
  // }
  //
  // public static LocationFragment newInstance(String latitude, String
  // longitude) {
  // LocationFragment f = new LocationFragment();
  //
  // // Supply id input as an argument.
  // Bundle args = new Bundle();
  // args.putString(PlacesConstants.ARGUMENTS_KEY_LATITUDE, latitude);
  // args.putString(PlacesConstants.ARGUMENTS_KEY_LONGITUDE, longitude);
  // f.setArguments(args);
  //
  // return f;
  // }

  protected Handler handler = new Handler();
  protected Activity activity;
  ///TextView locationTextView;
  TextView latitudeTextView;
  TextView longitudeTextView;
  String latitude;
  String longitude;
  //String[] location;
  protected SharedPreferences prefs;
  protected Cursor cursor = null;
  protected SimpleCursorAdapter adapter;

  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    activity = getActivity();

    // Create a new SimpleCursorAdapter that displays the name of each nearby
    // venue and the current distance to it.
    adapter = new SimpleCursorAdapter(activity,
        android.R.layout.two_line_list_item, cursor, new String[] {
            LocationsContentProvider.KEY_LOCATION_LAT,
            LocationsContentProvider.KEY_LOCATION_LNG }, new int[] {
            android.R.id.text1, android.R.id.text2 }, 0);

    // Populate the UI by initiating the loader to retrieve the
    // details of the venue from the underlying Place Content Provider.
    // if (latitude != null)
    getLoaderManager().initLoader(0, null, this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.location2, container, false);
    latitudeTextView = (TextView) view.findViewById(R.id.latitude);
    longitudeTextView = (TextView) view.findViewById(R.id.longitude);

    if (getArguments() != null) {
      latitude = getArguments().getString(
          PlacesConstants.ARGUMENTS_KEY_LATITUDE);
      longitude = getArguments().getString(
          PlacesConstants.ARGUMENTS_KEY_LONGITUDE);
      Log.d(TAG, String.format("getArgs != null - latitude:%s longitude:%s",
          latitude, longitude));
    }

    prefs = getActivity().getSharedPreferences(
        PlacesConstants.SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  public void updateUI(Location location) {
    // long lastTime = prefs.getLong(
    // PlacesConstants.SP_KEY_LAST_LIST_UPDATE_TIME, Long.MIN_VALUE);
    // long latitude = prefs.getLong(
    // PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LAT, Long.MIN_VALUE);
    // long longitude = prefs.getLong(
    // PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LNG, Long.MIN_VALUE);
    // Log.d(TAG,
    // String.format("getSharedPreferences - time:%d latitude:%d longitude:%d",
    // lastTime,latitude, longitude));
    /*
     * double latitude = location.getLatitude(); double longitude =
     * location.getLongitude();
     * 
     * SimpleDateFormat sdf = new
     * SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
     * 
     * latitudeTextView.setText(String.valueOf(latitude));
     * longitudeTextView.setText(String.valueOf(longitude));
     */Log.d(TAG, "updateUI(loc)");
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String[] projection = new String[] {
        LocationsContentProvider.KEY_LOCATION_LAT,
        LocationsContentProvider.KEY_LOCATION_LNG };

    return new CursorLoader(activity, LocationsContentProvider.CONTENT_URI,
        projection, null, null, null);

  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    Log.d(TAG, "onLoadFinished data.count:" + data.getCount());

    // latitudeTextView.setText(data.getString(0));
    // longitudeTextView.setText(data.getString(1));
    for (int i = 0; i < data.getColumnCount(); i++) {
      Log.d(TAG,
          "onLoadFinished - data[" + i + "].column=" + data.getColumnName(i));
    }
    // iterate through cursor
    // data.moveToFirst();
    // int i = 0;
    // while (data.isAfterLast() == false) {
    // Log.d(TAG, "onLoadFinished - data["+(i++)+"].column=" +
    // data.getString(1));
    // data.moveToNext();
    // }
    // cur.close();
    if (data.getCount() > 0) {
      int last = data.getCount();
      data.moveToLast();
      latitudeTextView.setText(data.getString(0));
      longitudeTextView.setText(data.getString(1));
    }

  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    Log.d(TAG, "onLoaderReset");
    latitudeTextView.setText("");
    longitudeTextView.setText("");
  }

}
