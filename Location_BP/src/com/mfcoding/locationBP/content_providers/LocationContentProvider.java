/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mfcoding.locationBP.content_providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Content Provider and database for storing the list of
 * places nearby our current location
 */
public class LocationContentProvider extends ContentProvider {

  /** The underlying database */
  private SQLiteDatabase locationDB;

  private static final String TAG = "LocationContentProvider";
  private static final String DATABASE_NAME = "location.db";
  private static final int DATABASE_VERSION = 2;
  private static final String LOCATIONS_TABLE = "locations";

  // TODO Replace the columns names and database creation SQL with values for your own app.  
  // Column Names
  public static final String KEY_ID = "_id";
  //public static final String KEY_NAME = "name";
  //public static final String KEY_VICINITY = "vicinity";
  public static final String KEY_LOCATION_LAT = "latitude";
  public static final String KEY_LOCATION_LNG = "longitude";
  //public static final String KEY_TYPES = "types";
  //public static final String KEY_VIEWPORT = "viewport";
  //public static final String KEY_ICON = "icon";
  //public static final String KEY_REFERENCE = "reference";
  //public static final String KEY_DISTANCE = "distance";
  public static final String KEY_LAST_UPDATE_TIME = "lastupdatetime";
	
  // TODO Replace this URI with something unique to your own application.
  public static final Uri CONTENT_URI = Uri.parse("content://com.mfcoding.provider.locations/locations");
  
  //Create the constants used to differentiate between the different URI requests.
  private static final int LOCATIONS = 1;
  private static final int LOCATION_ID = 2;

  //Allocate the UriMatcher object, where a URI ending in 'location' will
  //correspond to a request for all location, and 'location' with a trailing '/[Unique ID]' will represent a single place details row.
  private static final UriMatcher uriMatcher;
  static {
   uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
   uriMatcher.addURI("com.mfcoding.provider.locations", "locations", LOCATIONS);
   uriMatcher.addURI("com.mfcoding.provider.locations", "locations/*", LOCATION_ID);
  }
  
  @Override
  public boolean onCreate() {
  	Log.d(TAG, "onCreate");
    Context context = getContext();
  
    PlacesDatabaseHelper dbHelper = new PlacesDatabaseHelper(context, DATABASE_NAME, 
                                                             null, DATABASE_VERSION);
    try {
      locationDB = dbHelper.getWritableDatabase();
    } catch (SQLiteException e) {
      locationDB = null;
      Log.d(TAG, "Database Opening exception");
    }
    ContentValues values = new ContentValues();
    //  values.put(PlacesContentProvider.KEY_ID, id);  
    //  values.put(PlacesContentProvider.KEY_NAME, name);
    double lat = -123.084095;
    double lng = 34.422006;
    //  double lat = location.getLatitude();
    //  double lng = location.getLongitude();
    values.put(LocationContentProvider.KEY_LOCATION_LAT, lat);
    values.put(LocationContentProvider.KEY_LOCATION_LNG, lng);
    //  values.put(LocationContentProvider.KEY_VICINITY, vicinity);
    //  values.put(LocationContentProvider.KEY_TYPES, types);
    //  values.put(LocationContentProvider.KEY_VIEWPORT, viewport);
    //  values.put(LocationContentProvider.KEY_ICON, icon);
    //  values.put(LocationContentProvider.KEY_REFERENCE, reference);
    values.put(LocationContentProvider.KEY_LAST_UPDATE_TIME, System.currentTimeMillis());    
    long rowID = locationDB.insert(LOCATIONS_TABLE, "nullhack", values);
    
    return (locationDB == null) ? false : true;
  }

  @Override
  public String getType(Uri uri) {
    switch (uriMatcher.match(uri)) {
      case LOCATIONS: return "vnd.android.cursor.dir/vnd.mfcoding.location";
      case LOCATION_ID: return "vnd.android.cursor.item/vnd.mfcoding.location";
      default: throw new IllegalArgumentException("Unsupported URI: " + uri);
    }
  }

  
  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
  	Log.d(TAG, "query");
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    qb.setTables(LOCATIONS_TABLE);

    // If this is a row query, limit the result set to the passed in row. 
    switch (uriMatcher.match(uri)) {
      case LOCATION_ID: qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                     break;
      default      : break;
    }

    // If no sort order is specified sort by date / time
    String orderBy = sort;
/*    if (TextUtils.isEmpty(sort)) {
      orderBy = KEY_DISTANCE + " ASC";
    } else {
      orderBy = sort;
    }*/

    // Apply the query to the underlying database.
    Cursor c = qb.query(locationDB, 
                        projection, 
                        selection, selectionArgs, 
                        null, null, orderBy);

    // Register the contexts ContentResolver to be notified if
    // the cursor result set changes. 
    c.setNotificationUri(getContext().getContentResolver(), uri);
    
    // Return a cursor to the query result.
    return c;
  }

  @Override
  public Uri insert(Uri _uri, ContentValues _initialValues) {
  	Log.d(TAG, "insert");
    // Insert the new row, will return the row number if successful.
    long rowID = locationDB.insert(LOCATIONS_TABLE, "nullhack", _initialValues);
          
    // Return a URI to the newly inserted row on success.
    if (rowID > 0) {
      Uri uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
      getContext().getContentResolver().notifyChange(uri, null);
      return uri;
    }
    throw new SQLException("Failed to insert row into " + _uri);
  }

  @Override
  public int delete(Uri uri, String where, String[] whereArgs) {
  	Log.d(TAG, "delete");
    int count;
    
    switch (uriMatcher.match(uri)) {
      case LOCATIONS:
        count = locationDB.delete(LOCATIONS_TABLE, where, whereArgs);
        break;

      case LOCATION_ID:
        String segment = uri.getPathSegments().get(1);
        count = locationDB.delete(LOCATIONS_TABLE, KEY_ID + "="
                                    + segment
                                    + (!TextUtils.isEmpty(where) ? " AND (" 
                                    + where + ')' : ""), whereArgs);
        break;

      default: throw new IllegalArgumentException("Unsupported URI: " + uri);
    }

    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }

  @Override
  public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
  	Log.d(TAG, "update");
    int count;
    switch (uriMatcher.match(uri)) {
      case LOCATIONS: count = locationDB.update(LOCATIONS_TABLE, values, where, whereArgs);
                   break;

      case LOCATION_ID: String segment = uri.getPathSegments().get(1);
                     count = locationDB.update(LOCATIONS_TABLE, values, KEY_ID 
                             + "=" + segment 
                             + (!TextUtils.isEmpty(where) ? " AND (" 
                             + where + ')' : ""), whereArgs);
                     break;

      default: throw new IllegalArgumentException("Unknown URI " + uri);
    }

    getContext().getContentResolver().notifyChange(uri, null);
    return count;
  }
 
  
  // Helper class for opening, creating, and managing database version control
  private static class PlacesDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_CREATE =
      "create table " + LOCATIONS_TABLE + " (" 
      + KEY_ID + " INTEGER primary key autoincrement, "
//      + KEY_NAME + " TEXT, "
//      + KEY_VICINITY + " TEXT, "
      + KEY_LOCATION_LAT + " FLOAT, "
      + KEY_LOCATION_LNG + " FLOAT, "
//      + KEY_TYPES + " TEXT, "
//      + KEY_VIEWPORT + " TEXT, "
//      + KEY_ICON + " TEXT, "
//      + KEY_REFERENCE + " TEXT, "
//      + KEY_DISTANCE + " FLOAT, "
      + KEY_LAST_UPDATE_TIME + " LONG); ";
        
    public PlacesDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
      super(context, name, factory, version);
    	Log.d(TAG, "PlacesDatabaseHelper");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(DATABASE_CREATE);  
      Log.d(TAG, "onCreate(DB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                  + newVersion + ", which will destroy all old data");
              
      db.execSQL("DROP TABLE IF EXISTS " + LOCATIONS_TABLE);
      onCreate(db);
    }
  }
}