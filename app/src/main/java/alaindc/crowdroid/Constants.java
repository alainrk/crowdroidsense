package alaindc.crowdroid;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * Created by alain on 07/06/16.
 */
public class Constants {

    public static final String PREF_FILE = "GEOSPOTPREF";
    public static final String PREF_RESPONSE = "RESPONSE";
    public static final String PACKAGE_NAME_ACT = "com.google.android.gms.location.activityrecognition";
    public static final String BROADCAST_ACTION = PACKAGE_NAME_ACT + ".BROADCAST_ACTION";
    public static final String ACTIVITY_EXTRA = PACKAGE_NAME_ACT + ".ACTIVITY_EXTRA";
    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME_ACT + ".SHARED_PREFERENCES";

    public static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GEOFENCE NOT AVAILABLE";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "TOO MANY GEOFENCE";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "TOO MANY PENDING INTENT";
            default:
                return "UNKNOWN ERROR";
        }
    }

    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME_LOC = "com.google.android.gms.location.sample.locationaddress";
    public static final String RECEIVER = PACKAGE_NAME_LOC + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME_LOC + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME_LOC + ".LOCATION_DATA_EXTRA";



    final static int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 90;

    public static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";


    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km

}
