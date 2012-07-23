package com.mfcoding.locationBP.UI.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

public class PrevLocationFragment extends Fragment {
	static final String TAG = "PrevLocationFragment";

	/**
	 * Factory that return a new instance of the {@link PrevLocationFragment}
	 * 
	 * @param 
	 * @return A new LocationFragment
	 */
	public static PrevLocationFragment newInstance(String[] location) {
		PrevLocationFragment f = new PrevLocationFragment();

		// Supply id input as an argument.
		Bundle args = new Bundle();
		args.putString(PlacesConstants.ARGUMENTS_KEY_LATITUDE, location[0]);
		args.putString(PlacesConstants.ARGUMENTS_KEY_LONGITUDE, location[1]);
		f.setArguments(args);

		return f;
	}

	public static PrevLocationFragment newInstance(String latitude, String longitude) {
		PrevLocationFragment f = new PrevLocationFragment();

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
	TextView timeTextView;
	String latitude;
	String longitude;
	String[] location;
	protected SharedPreferences prefs;
	
	public PrevLocationFragment() {
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
		timeTextView = (TextView) view.findViewById(R.id.time);
		locationTextView.setText("Prev Location");
/*
		if (getArguments() != null) {
			latitude = getArguments().getString(
					PlacesConstants.ARGUMENTS_KEY_LATITUDE);
			longitude = getArguments().getString(
					PlacesConstants.ARGUMENTS_KEY_LONGITUDE);
			Log.d(TAG, String.format("getArgs - latitude:%s longitude:%s", latitude, longitude));
		}
*/
		
		prefs = getActivity().getSharedPreferences(PlacesConstants.SHARED_PREFERENCE_FILE,
				Context.MODE_PRIVATE);
		// Retrieve the last update time and place.
		long lastTime = prefs.getLong(
				PlacesConstants.SP_KEY_LAST_LIST_UPDATE_TIME, Long.MIN_VALUE);		
		long latitude = prefs.getLong(
				PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LAT, Long.MIN_VALUE);
		long longitude = prefs.getLong(
				PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LNG, Long.MIN_VALUE);
		Log.d(TAG, String.format("getArgs - time:%d latitude:%d longitude:%d", lastTime,latitude, longitude));
		

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

}