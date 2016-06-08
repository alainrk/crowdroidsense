package alaindc.crowdroid;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Sensor;

import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * Created by alain on 07/06/16.
 */
public class Constants {

    public static final String PACKAGE_NAME_ACT = "com.google.android.gms.location.activityrecognition";

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

    public static boolean isInMonitoredSensors (int type) {
        for (int i: MONITORED_SENSORS){
            if (i == type)
                return true;
        }
        return false;
    }

    public static int MONITORED_SENSORS[] = {
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_RELATIVE_HUMIDITY,
            Sensor.TYPE_PRESSURE
    };

    // Location updates intervals in sec
    public static int UPDATE_INTERVAL = 10000; // 10 sec
    public static int FATEST_INTERVAL = 5000; // 5 sec
    public static int DISPLACEMENT = 10; // 10 meters

    public static String PREF_FILE = "CROWDROIDSHAREDPREFERENCESFILE";
    public static String PREF_LATITUDE = "CROWDROIDLATITUDE";
    public static String PREF_LONGITUDE = "CROWDROIDLONGITUDE";
    public static String PREF_AMPLITUDE = "CROWDROIDAMPLITUDE";

    public static String PREF_SENSOR = "CROWDROIDSENSOR_";

    public static String INTENT_RECEIVED_DATA = "INTENT_RECEIVED_DATA";
    public static String INTENT_RECEIVED_DATA_EXTRA_DATA = "INTENT_RECEIVED_DATA_EXTRA_DATA";
    public static String INTENT_RECEIVED_AMPLITUDE = "INTENT_RECEIVED_AMPLITUDE";
    public static String INTENT_UPDATE_POS = "INTENT_UPDATE_POS";
    public static String INTENT_UPDATE_SENSORS = "INTENT_UPDATE_SENSORS";
    public static String INTENT_UPDATE_AMPLITUDE = "INTENT_UPDATE_AMPLITUDE";
    public static String INTENT_START_SENSORS = "INTENT_START_SENSORS";

    public static String INTENT_START_AUDIOAMPLITUDE_SENSE = "INTENT_START_AUDIOAMPLITUDE_SENSE";

    public static String EXTRA_AMPLITUDE = "EXTRA_AMPLITUDE";

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
