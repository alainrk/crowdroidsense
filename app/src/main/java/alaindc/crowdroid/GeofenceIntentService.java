package alaindc.crowdroid;

/**
 * Created by alain on 11/06/16.
 */

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

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(Constants.PREF_FILE, MODE_PRIVATE);
        mGeofencesAdded = mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);

        int sensorType = intent.getIntExtra(Constants.EXTRA_GEOFENCE_SENSORTYPE, -1);
        double latitude = intent.getDoubleExtra(Constants.EXTRA_GEOFENCE_LATITUDE, 44);
        double longitude = intent.getDoubleExtra(Constants.EXTRA_GEOFENCE_LONGITUDE, 11);
        float radius = Float.parseFloat(intent.getStringExtra(Constants.EXTRA_GEOFENCE_RADIUS));

        addSensorGeofence(sensorType, radius, latitude, longitude);
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

    public void addSensorGeofence(int sensorType, float radius, double latitude, double longitude) {
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
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setLoiteringDelay(30)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_DWELL|Geofence.GEOFENCE_TRANSITION_EXIT|Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(geofenceList),
                    getGeofencePendingIntent(sensorType)
            ).setResultCallback(this); // Result processed in onResult().

            // Update view sending a broadcast intent
            Intent intent = new Intent(Constants.INTENT_UPDATE_GEOFENCEVIEW);
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