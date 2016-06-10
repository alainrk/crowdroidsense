package alaindc.crowdroid;

import android.hardware.Sensor;

import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * Created by alain on 07/06/16.
 */
public class Constants {

    //public static final String SERVER_ADDR = "melot.cs.unibo.it";//"192.168.1.112";
    public static final String SERVER_ADDR = "192.168.1.118";
    public static final int SERVER_PORT = 5683;
    public static final String SERVER_LOCAL_URI = "/myresp";

    public static final String PACKAGE_NAME_ACT = "com.google.android.gms.location.activityrecognition";

    public static String getErrorString(int errorCode) {
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
        for (int i: MONITORED_SENSORS_REAL){
            if (i == type)
                return true;
        }
        return false;
    }

    public static final int MONITORED_SENSORS_REAL[] = {
            Sensor.TYPE_LIGHT
    };

    public static final int TYPE_AMPLITUDE = 100;

    public static final int MONITORED_SENSORS[] = {
            TYPE_AMPLITUDE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_AMBIENT_TEMPERATURE, // Stub
            Sensor.TYPE_RELATIVE_HUMIDITY, // Stub
            Sensor.TYPE_PRESSURE // Stub
    };

    public static final int STUBBED_MONITORED_SENSORS[] = {
            Sensor.TYPE_AMBIENT_TEMPERATURE, // Stub
            Sensor.TYPE_RELATIVE_HUMIDITY, // Stub
            Sensor.TYPE_PRESSURE // Stub
    };

    public static String getNameOfSensor (int type) {
        switch (type) {
            case Sensor.TYPE_LIGHT:
                return "SENSOR_LIGHT";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "SENSOR_AMBIENT_TEMPERATURE";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "SENSOR_RELATIVE_HUMIDITY";
            case Sensor.TYPE_PRESSURE:
                return "SENSOR_PRESSURE";
            case TYPE_AMPLITUDE:
                return "SENSOR_AMPLITUDE";

        }
        return "";
    }

    // To keep alarms after receiving values from server
    public static int getIndexAlarmForSensor (int type) {
        switch (type) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return 0;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return 1;
            case Sensor.TYPE_PRESSURE:
                return 2;
            case TYPE_AMPLITUDE:
                return 3;
            case Sensor.TYPE_LIGHT:
                return 4;

        }
        return -1;
    }

    public static String INTENT_STUB_SENSOR_CHANGED = "INTENT_STUB_SENSOR_CHANGED";
    public static String INTENT_STUB_SENSOR_CHANGED_TYPE = "INTENT_STUB_SENSOR_CHANGED_TYPE";

    // Location updates intervals in sec
    public static int UPDATE_INTERVAL = 10000; // 10 sec
    public static int FATEST_INTERVAL = 5000; // 5 sec
    public static int DISPLACEMENT = 10; // 10 meters

    public static String ACTION_SENDDATA = "ACTION_SENDDATA";
    public static String ACTION_RECEIVEDDATA = "ACTION_RECEIVEDDATA";
    public static String EXTRA_RESPONSE = "EXTRA_RESPONSE";

    public static String EXTRA_TYPE_OF_SENSOR_TO_SEND = "EXTRA_TYPE_OF_SENSOR_TO_SEND";

    public static String PREF_FILE = "CROWDROID_SHAREDPREFERENCES_FILE";

    public static String PREF_LATITUDE = "CROWDROID_PREF_LATITUDE";
    public static String PREF_LONGITUDE = "CROWDROID_PREF_LONGITUDE";
    public static String PREF_SENSOR_ = "CROWDROID_PREF_SENSOR_";

    public static String INTENT_RECEIVED_DATA = "INTENT_RECEIVED_DATA";
    public static String INTENT_RECEIVED_DATA_EXTRA_DATA = "INTENT_RECEIVED_DATA_EXTRA_DATA";
    public static String INTENT_RECEIVED_AMPLITUDE = "INTENT_RECEIVED_AMPLITUDE";
    public static String INTENT_UPDATE_POS = "INTENT_UPDATE_POS";
    public static String INTENT_UPDATE_SENSORS = "INTENT_UPDATE_SENSORS";
    public static String INTENT_UPDATE_AMPLITUDE = "INTENT_UPDATE_AMPLITUDE";
    public static String INTENT_START_SENSORS = "INTENT_START_SENSORS";

    public static String INTENT_START_AUDIOAMPLITUDE_SENSE = "INTENT_START_AUDIOAMPLITUDE_SENSE";
    public static String EXTRA_AMPLITUDE = "EXTRA_AMPLITUDE";

    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

}
