package com.mfcoding.locationBP.UI.fragments;

import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfcoding.locationBP.PlacesConstants;
import com.mfcoding.locationBP.R;

public class LocationFragment extends Fragment {
	static final String TAG = "LocationFragment";

	/**
	 * Factory that return a new instance of the {@link LocationFragment}
	 * 
	 * @param 
	 * @return A new LocationFragment
	 */
	public static LocationFragment newInstance(String[] location) {
		LocationFragment f = new LocationFragment();

		// Supply id input as an argument.
		Bundle args = new Bundle();
		args.putString(PlacesConstants.ARGUMENTS_KEY_LATITUDE, location[0]);
		args.putString(PlacesConstants.ARGUMENTS_KEY_LONGITUDE, location[1]);
		f.setArguments(args);

		return f;
	}

	public static LocationFragment newInstance(String latitude, String longitude) {
		LocationFragment f = new LocationFragment();

		// Supply id input as an argument.
		Bundle args = new Bundle();
		args.putString(PlacesConstants.ARGUMENTS_KEY_LATITUDE, latitude);
		args.putString(PlacesConstants.ARGUMENTS_KEY_LONGITUDE, longitude);
		f.setArguments(args);

		return f;
	}

	protected Handler handler = new Handler();
	protected Activity activity;
	TextView locationTextView;
	TextView latitudeTextView;
	TextView longitudeTextView;
	String latitude;
	String longitude;
	String[] location;
	protected SharedPreferences prefs;
	
	public LocationFragment() {
		super();
	}

	void setLocation(String[] location) {
		this.location = location;
		Log.d(TAG,
				String.format("location: lat:%s long:%s", location[0], location[1]));
		// if (location != null)
		// getLoaderManager().restartLoader(0, null, this);

	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		activity = getActivity();
		// Populate the UI by initiating the loader to retrieve the
		// details of the venue from the underlying Place Content Provider.
		// if (latitude != null)
		// getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.location2, container, false);
		locationTextView = (TextView) view.findViewById(R.id.location);
		latitudeTextView = (TextView) view.findViewById(R.id.latitude);
		longitudeTextView = (TextView) view.findViewById(R.id.latitude);
		locationTextView.setText("HI fangstar");

		if (getArguments() != null) {
			latitude = getArguments().getString(
					PlacesConstants.ARGUMENTS_KEY_LATITUDE);
			longitude = getArguments().getString(
					PlacesConstants.ARGUMENTS_KEY_LONGITUDE);
			Log.d(TAG, String.format("getArgs != null - latitude:%s longitude:%s", latitude, longitude));
		}
		
		prefs = getActivity().getSharedPreferences(PlacesConstants.SHARED_PREFERENCE_FILE,
				Context.MODE_PRIVATE);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void updateUI(Location location) {
//		long lastTime = prefs.getLong(
//				PlacesConstants.SP_KEY_LAST_LIST_UPDATE_TIME, Long.MIN_VALUE);		
//		long latitude = prefs.getLong(
//				PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LAT, Long.MIN_VALUE);
//		long longitude = prefs.getLong(
//				PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LNG, Long.MIN_VALUE);
//		Log.d(TAG, String.format("getSharedPreferences - time:%d latitude:%d longitude:%d", lastTime,latitude, longitude));
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");
		
		latitudeTextView.setText(String.valueOf(latitude));
		longitudeTextView.setText(String.valueOf(longitude));		
		Log.d(TAG, "updateUI(loc)");
	}
	
}
