package com.mfcoding.locationBP.services;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import com.mfcoding.locationBP.PlacesConstants;
import com.mfcoding.locationBP.content_providers.LocationContentProvider;
import com.mfcoding.locationBP.receivers.ConnectivityChangedReceiver;
import com.mfcoding.locationBP.receivers.LocationChangedReceiver;
import com.mfcoding.locationBP.receivers.PassiveLocationChangedReceiver;

public class LocationUpdateService extends IntentService {
	protected static String TAG = "LocationUpdateService";

	protected ContentResolver contentResolver;
	protected SharedPreferences prefs;
	protected Editor prefsEditor;
	protected ConnectivityManager cm;
	protected boolean lowBattery = false;
	protected boolean mobileData = false;
	protected int prefetchCount = 0;

	public LocationUpdateService() {
		super(TAG);
		setIntentRedeliveryMode(false);
	}

	/**
	 * Set the Intent Redelivery mode to true to ensure the Service starts
	 * "Sticky" Defaults to "true" on legacy devices.
	 */
	protected void setIntentRedeliveryMode(boolean enable) {
	}

	/**
	 * Returns battery status. True if less than 10% remaining.
	 * 
	 * @param battery
	 *          Battery Intent
	 * @return Battery is low
	 */
	protected boolean getIsLowBattery(Intent battery) {
		float pctLevel = (float) battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 1)
				/ battery.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
		return pctLevel < 0.15;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		contentResolver = getContentResolver();
		prefs = getSharedPreferences(PlacesConstants.SHARED_PREFERENCE_FILE,
				Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();
	}

	/**
	 * {@inheritDoc} Checks the battery and connectivity state before removing
	 * stale venues and initiating a server poll for new venues around the
	 * specified location within the given radius.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// Check if we're running in the foreground, if not, check if
		// we have permission to do background updates.
		boolean backgroundAllowed = cm.getBackgroundDataSetting();
		boolean inBackground = prefs.getBoolean(
				PlacesConstants.EXTRA_KEY_IN_BACKGROUND, true);

		if (!backgroundAllowed && inBackground)
			return;

		// Extract the location and radius around which to conduct our search.
		Location location = new Location(
				PlacesConstants.CONSTRUCTED_LOCATION_PROVIDER);
		int radius = PlacesConstants.DEFAULT_RADIUS;

		Bundle extras = intent.getExtras();
		if (intent.hasExtra(PlacesConstants.EXTRA_KEY_LOCATION)) {
			location = (Location) (extras.get(PlacesConstants.EXTRA_KEY_LOCATION));
			radius = extras.getInt(PlacesConstants.EXTRA_KEY_RADIUS,
					PlacesConstants.DEFAULT_RADIUS);
		}

		// Check if we're in a low battery situation.
		IntentFilter batIntentFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		Intent battery = registerReceiver(null, batIntentFilter);
		lowBattery = getIsLowBattery(battery);

		// Check if we're connected to a data network, and if so - if it's a mobile
		// network.
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null
				&& activeNetwork.isConnectedOrConnecting();
		mobileData = activeNetwork != null
				&& activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

		// If we're not connected, enable the connectivity receiver and disable the
		// location receiver.
		// There's no point trying to poll the server for updates if we're not
		// connected, and the
		// connectivity receiver will turn the location-based updates back on once
		// we have a connection.
		if (!isConnected) {
			PackageManager pm = getPackageManager();

			ComponentName connectivityReceiver = new ComponentName(this,
					ConnectivityChangedReceiver.class);
			ComponentName locationReceiver = new ComponentName(this,
					LocationChangedReceiver.class);
			ComponentName passiveLocationReceiver = new ComponentName(this,
					PassiveLocationChangedReceiver.class);

			pm.setComponentEnabledSetting(connectivityReceiver,
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);

			pm.setComponentEnabledSetting(locationReceiver,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);

			pm.setComponentEnabledSetting(passiveLocationReceiver,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		} else {
			// If we are connected check to see if this is a forced update (typically
			// triggered
			// when the location has changed).
			boolean doUpdate = intent.getBooleanExtra(
					PlacesConstants.EXTRA_KEY_FORCEREFRESH, false);

			// If it's not a forced update (for example from the Activity being
			// restarted) then
			// check to see if we've moved far enough, or there's been a long enough
			// delay since
			// the last update and if so, enforce a new update.
			if (!doUpdate) {
				// Retrieve the last update time and place.
				long lastTime = prefs.getLong(
						PlacesConstants.SP_KEY_LAST_LIST_UPDATE_TIME, Long.MIN_VALUE);
				long lastLat = prefs.getLong(
						PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LAT, Long.MIN_VALUE);
				long lastLng = prefs.getLong(
						PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LNG, Long.MIN_VALUE);
				Log.d(TAG, "!doUpdate - lastLat:"+lastLat+" lastLng:"+lastLng);
				Location lastLocation = new Location(
						PlacesConstants.CONSTRUCTED_LOCATION_PROVIDER);
				lastLocation.setLatitude(lastLat);
				lastLocation.setLongitude(lastLng);

				// If update time and distance bounds have been passed, do an update.
				if ((lastTime < System.currentTimeMillis() - PlacesConstants.MAX_TIME)
						|| (lastLocation.distanceTo(location) > PlacesConstants.MAX_DISTANCE))
					doUpdate = true;
			}

			if (doUpdate) {
				// Refresh the prefetch count for each new location.
				prefetchCount = 0;
				// Remove the old locations
				removeOldLocations(location, radius);
				
				// Hit the server for new venues for the current location.
				refreshPlaces(location, radius);
				Log.d(TAG, " doUpdate here");
        // Save the last update time and place to the Shared Preferences.
//        prefsEditor.putLong(PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LAT, (long) location.getLatitude());
//        prefsEditor.putLong(PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LNG, (long) location.getLongitude());
//        prefsEditor.putLong(PlacesConstants.SP_KEY_LAST_LIST_UPDATE_TIME, System.currentTimeMillis());      
//        prefsEditor.commit();				
			} else
				Log.d(TAG, "Data is fresh: Not refreshing");

			// Retry any queued checkins.
			// Intent checkinServiceIntent = new Intent(this,
			// PlaceCheckinService.class);
			// startService(checkinServiceIntent);
		}
		Log.d(TAG, "Location Service Complete");
	}
  
  /**
   * Polls the underlying service to return a list of places within the specified
   * radius of the specified Location. 
   * @param location Location
   * @param radius Radius
   */
  protected void refreshPlaces(Location location, int radius) {   
  	Log.d(TAG, "refreshPlaces");
    // Log to see if we'll be prefetching the details page of each new place.
    if (mobileData) {
      Log.d(TAG, "Not prefetching due to being on mobile");
    } else if (lowBattery) {
      Log.d(TAG, "Not prefetching due to low battery");
    }

    long currentTime = System.currentTimeMillis();
    
    // Remove places from the PlacesContentProviderlist that aren't from this updte.
    //String where = PlaceDetailsContentProvider.KEY_LAST_UPDATE_TIME + " < " + currentTime; 
    contentResolver.delete(LocationContentProvider.CONTENT_URI, null, null);
   
    // Add each new place to the Places Content Provider
    addPlace(location, currentTime);
    

    
    // Save the last update time and place to the Shared Preferences.
    prefsEditor.putLong(PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LAT, (long) location.getLatitude());
    prefsEditor.putLong(PlacesConstants.SP_KEY_LAST_LIST_UPDATE_LNG, (long) location.getLongitude());
    prefsEditor.putLong(PlacesConstants.SP_KEY_LAST_LIST_UPDATE_TIME, System.currentTimeMillis());      
    prefsEditor.commit();

  }
	
	
  
  /**
   * Adds the new place to the {@link PlacesContentProvider} using the values passed in.
   * TODO Update this method to accept and persist the place information your service provides.
   * @param currentLocation Current location
   * @param id Unique identifier
   * @param name Name
   * @param vicinity Vicinity
   * @param types Types
   * @param location Location
   * @param viewport Viewport
   * @param icon Icon
   * @param reference Reference
   * @param currentTime Current time
   * @return Successfully added
   */
  protected boolean addPlace(Location currentLocation, long currentTime) {
  	Log.d(TAG, "addPlace");
    // Contruct the Content Values
    ContentValues values = new ContentValues();
//    values.put(PlacesContentProvider.KEY_ID, id);  
//    values.put(PlacesContentProvider.KEY_NAME, name);
    double lat = currentLocation.getLatitude();
    double lng = currentLocation.getLongitude();
//    double lat = location.getLatitude();
//    double lng = location.getLongitude();
    values.put(LocationContentProvider.KEY_LOCATION_LAT, lat);
    values.put(LocationContentProvider.KEY_LOCATION_LNG, lng);
//    values.put(LocationContentProvider.KEY_VICINITY, vicinity);
//    values.put(LocationContentProvider.KEY_TYPES, types);
//    values.put(LocationContentProvider.KEY_VIEWPORT, viewport);
//    values.put(LocationContentProvider.KEY_ICON, icon);
//    values.put(LocationContentProvider.KEY_REFERENCE, reference);
    values.put(LocationContentProvider.KEY_LAST_UPDATE_TIME, currentTime);

    // Calculate the distance between the current location and the venue's location
//    float distance = 0;
//    if (currentLocation != null && location != null)
//      distance = currentLocation.distanceTo(location);
//    values.put(PlacesContentProvider.KEY_DISTANCE, distance);  
    
    // Update or add the new place to the PlacesContentProvider
    //String where = LocationContentProvider.KEY_ID + " = '" + id + "'";
    String where = "";
    boolean result = false;
    try {
      if (contentResolver.update(LocationContentProvider.CONTENT_URI, values, where, null) == 0) {
        if (contentResolver.insert(LocationContentProvider.CONTENT_URI, values) != null)
          result = true;
      }
      else 
        result = true;
    }
    catch (Exception ex) { 
      Log.e(TAG, "Adding " + " to DB" + " failed. (Exception)");
    }
    
    // If we haven't yet reached our prefetching limit, and we're either
    // on WiFi or don't have a WiFi-only prefetching restriction, and we
    // either don't have low batter or don't have a low battery prefetching 
    // restriction, then prefetch the details for this newly added place.
/*    if ((prefetchCount < PlacesConstants.PREFETCH_LIMIT) &&
        (!PlacesConstants.PREFETCH_ON_WIFI_ONLY || !mobileData) &&
        (!PlacesConstants.DISABLE_PREFETCH_ON_LOW_BATTERY || !lowBattery)) {
      prefetchCount++;
      
      // Start the PlaceDetailsUpdateService to prefetch the details for this place.
      // As we're prefetching, don't force the refresh if we already have data.
      Intent updateServiceIntent = new Intent(this, PlaceDetailsUpdateService.class);
      updateServiceIntent.putExtra(PlacesConstants.EXTRA_KEY_REFERENCE, reference);
      updateServiceIntent.putExtra(PlacesConstants.EXTRA_KEY_ID, id);
      updateServiceIntent.putExtra(PlacesConstants.EXTRA_KEY_FORCEREFRESH, false);
      startService(updateServiceIntent);
    }*/
    
    return result;
  }

  
  /**
   * Remove stale place detail records unless we've set the persistent cache flag to true.
   * This is typically the case where a place has actually been viewed rather than prefetched. 
   * @param location Location
   * @param radius Radius
   */
  protected void removeOldLocations(Location location, int radius) {
  	Log.d(TAG, "removeOldLocations");
    // Stale Detail Pages
//    long minTime = System.currentTimeMillis()-PlacesConstants.MAX_DETAILS_UPDATE_LATENCY;
//    String where = PlaceDetailsContentProvider.KEY_LAST_UPDATE_TIME + " < " + minTime + " AND " +
//                   PlaceDetailsContentProvider.KEY_FORCE_CACHE + " = 0";
//    contentResolver.delete(PlaceDetailsContentProvider.CONTENT_URI, where, null);
  }
}
