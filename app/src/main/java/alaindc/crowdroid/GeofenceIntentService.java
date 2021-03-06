/*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
* This file is part of Crowdroid(sense)/Servercoap project, thesis in Crowdsensing.
* Copyright (C) 2016 Alain Di Chiappari
*/

package alaindc.crowdroid;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class GeofenceIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    private Intent currentIntent;

    // For Geofencing
    protected static final String TAG = "NeverSleepService";
    protected GoogleApiClient mGoogleApiClient;
    private boolean mGeofencesAdded; // Tracks added geofences
    private SharedPreferences mSharedPreferences;

    public GeofenceIntentService() {
        super("PositionIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        buildGoogleApiClient();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        currentIntent = intent;

        Log.d("","");
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest(ArrayList<Geofence> geofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // If client already inside and geofence added right now
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(geofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    public void addSensorGeofence(int sensorType, float radius, double latitude, double longitude, long timeout) {
        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        ArrayList<Geofence> geofenceList = new ArrayList<>();
        geofenceList.add(new Geofence.Builder()
                // REQUEST ID (Could be sensor_type_id)
                .setRequestId(String.valueOf(sensorType))
                .setCircularRegion(
                        latitude,
                        longitude,
                        radius
                )
                .setExpirationDuration(timeout)
                .setLoiteringDelay(3000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL) // DEBUG
                .build());
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(geofenceList),
                    getGeofencePendingIntent(sensorType)
            ).setResultCallback(this); // Result processed in onResult().

            // Update view sending a broadcast intent
            Intent intent = new Intent(Constants.INTENT_UPDATE_GEOFENCEVIEW);
            intent.putExtra(Constants.INTENT_GEOFENCEEXTRA_SENSOR, sensorType);
            intent.putExtra(Constants.INTENT_GEOFENCEEXTRA_LATITUDE, latitude);
            intent.putExtra(Constants.INTENT_GEOFENCEEXTRA_LONGITUDE, longitude);
            intent.putExtra(Constants.INTENT_GEOFENCEEXTRA_RADIUS, radius);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } catch (SecurityException securityException) {
            logSecurityException(securityException);
        }
    }

    // Removing SHOULD be redundant, doc says:
    // If an existing geofence with the same request ID is already registered, the old geofence
    // is replaced by the new one, and the new PendingIntent is used to generate intents for alerts.
//    public void removeGeofence(int sensor_type) {
//        try {
//            // Remove geofence
//            LocationServices.GeofencingApi.removeGeofences(
//                    mGoogleApiClient,
//                    // This is the same pending intent that was used in addGeofences().
//                    getGeofencePendingIntent(sensor_type)
//            ).setResultCallback(this); // Result processed in onResult().
//        } catch (SecurityException securityException) {
//            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
//            logSecurityException(securityException);
//        }
//    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    // Runs when the result of calling addGeofences() and removeGeofences() becomes available.
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();

        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = Constants.getErrorString(status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    private PendingIntent getGeofencePendingIntent(int sensor_type) {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, sensor_type);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);
        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);

        int sensorType = currentIntent.getIntExtra(Constants.EXTRA_GEOFENCE_SENSORTYPE, -1);
        double latitude = currentIntent.getDoubleExtra(Constants.EXTRA_GEOFENCE_LATITUDE, 44);
        double longitude = currentIntent.getDoubleExtra(Constants.EXTRA_GEOFENCE_LONGITUDE, 11);
        float radius = Float.parseFloat(currentIntent.getStringExtra(Constants.EXTRA_GEOFENCE_RADIUS));
        long timeout = Long.parseLong(currentIntent.getStringExtra(Constants.EXTRA_GEOFENCE_EXPIRE_MILLISEC));

        addSensorGeofence(sensorType, radius, latitude, longitude, timeout);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "Connection suspended");
    }
}