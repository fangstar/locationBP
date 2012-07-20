package com.mfcoding.locationBP.UI.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mfcoding.locationBP.R;

public class HelloFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.hello_frag, container, false);
		//return super.onCreateView(inflater, container, savedInstanceState);
		return v;
	}

}
