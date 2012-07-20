package com.mfcoding.locationBP.UI.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mfcoding.locationBP.R;

public class LocationFragment extends Fragment {

	protected Handler handler = new Handler();
	protected Activity activity;
	TextView locationTextView;
	TextView latitudeTextView;
	TextView longitudeTextView;	

//	public LocationFragment() {
//		super();
//	}
//
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//		activity = getActivity();
//
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.location2, container, false);
//		locationTextView = (TextView) view.findViewById(R.id.location);
//		latitudeTextView = (TextView) view.findViewById(R.id.latitude);
//		longitudeTextView = (TextView) view.findViewById(R.id.latitude);
//		locationTextView.setText("HI fangstar");
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

}
