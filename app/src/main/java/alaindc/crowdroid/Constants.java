package alaindc.crowdroid;

import android.hardware.Sensor;

import com.google.android.gms.location.GeofenceStatusCodes;

import de.uzl.itm.ncoap.application.client.CoapClient;

/**
 * Created by alain on 07/06/16.
 */
public class Constants {

    public static final String SERVER_ADDR = "melot.cs.unibo.it";
//    public static final String SERVER_ADDR = "192.168.1.118";
    public static final int SERVER_PORT = 5683;
    public static long POST = 2;

    public static final String SERVER_SENSINGSEND_URI = "/sensing_send";
    public static final String SERVER_GETSUBSCRIPTION_URI = "/get_subscriptions";
    public static final String SERVER_UPDATESUBSCRIPTION_URI = "/update_subscriptions";
    public static final String SERVER_CALCTHROUGHPUT_URI = "/calc_throughput";

    public static final String PACKAGE_NAME_ACT = "com.google.android.gms.location.activityrecognition";

        public static final String GEOFENCES_ADDED_KEY = "GEOFENCES_ADDED_KEY";

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
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_RELATIVE_HUMIDITY,
            Sensor.TYPE_PRESSURE
    };

    public static final int TYPE_AMPLITUDE = 100;
    public static final int TYPE_WIFI = 101;
    public static final int TYPE_TEL = 102;

    public static final int MONITORED_SENSORS[] = {
            TYPE_AMPLITUDE,
            TYPE_WIFI,
            TYPE_TEL,
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
            case TYPE_WIFI:
                return "SENSOR_WIFI";
            case TYPE_TEL:
                    return "SENSOR_TELEPHONE";
            default:
                return "SENSOR_UNKNOWN";
        }
    }

    // https://developer.android.com/reference/android/hardware/SensorEvent.html
    // https://developer.android.com/reference/android/media/MediaRecorder.html#getMaxAmplitude()
    public static String getUnitsOfSensor (int type) {
        switch (type) {
            case Sensor.TYPE_LIGHT:
                return "lux";
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                return "°C";
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                return "%";
            case Sensor.TYPE_PRESSURE:
                return "hPa";
            case TYPE_AMPLITUDE:
                return "abs";
            case TYPE_WIFI:
                return "dB";
            case TYPE_TEL:
                return "dB";
            default:
                return "unknown";
        }
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
            case TYPE_WIFI:
                return 5;
            case TYPE_TEL:
                return 6;
        }
        return -1;
    }

    public static String INTENT_STUB_SENSOR_CHANGED = "INTENT_STUB_SENSOR_CHANGED";
    public static String INTENT_STUB_SENSOR_CHANGED_TYPE = "INTENT_STUB_SENSOR_CHANGED_TYPE";

    // Location updates intervals in sec
    public static int UPDATE_INTERVAL = 10000; // 10 sec
    public static int FASTEST_INTERVAL = 5000; // 5 sec
    public static int DISPLACEMENT = 10; // 10 meters

    public static String ACTION_SENDDATA = "ACTION_SENDDATA";
    public static String ACTION_RECEIVEDDATA = "ACTION_RECEIVEDDATA";
    public static String EXTRA_SENSE_RESPONSE = "EXTRA_SENSE_RESPONSE";

    public static String EXTRA_TYPE_OF_SENSOR_TO_SEND = "EXTRA_TYPE_OF_SENSOR_TO_SEND";

    public static String PREF_FILE = "CROWDROID_SHAREDPREFERENCES_FILE";

    public static String PREF_LATITUDE = "CROWDROID_PREF_LATITUDE";
    public static String PREF_LONGITUDE = "CROWDROID_PREF_LONGITUDE";
    public static String PREF_SENSOR_ = "CROWDROID_PREF_SENSOR_";

    public static String THROUGHPUT_TAKEN = "THROUGHPUT_TAKEN";
    public static String THROUGHPUT_VALUE = "THROUGHPUT_VALUE";

    public static String INTENT_RECEIVED_DATA = "INTENT_RECEIVED_DATA";
    public static String INTENT_RECEIVED_DATA_EXTRA_DATA = "INTENT_RECEIVED_DATA_EXTRA_DATA";
    public static String INTENT_RECEIVED_AMPLITUDE = "INTENT_RECEIVED_AMPLITUDE";
    public static String INTENT_UPDATE_POS = "INTENT_UPDATE_POS";
    public static String INTENT_UPDATE_SENSORS = "INTENT_UPDATE_SENSORS";
    public static String INTENT_START_SENSORS = "INTENT_START_SENSORS";

    public static String INTENT_START_AUDIOAMPLITUDE_SENSE = "INTENT_START_AUDIOAMPLITUDE_SENSE";
    public static String EXTRA_AMPLITUDE = "EXTRA_AMPLITUDE";

    public static String EXTRA_GEOFENCE_SENSORTYPE = "EXTRA_GEOFENCE_SENSORTYPE";
    public static String EXTRA_GEOFENCE_LATITUDE = "EXTRA_GEOFENCE_LATITUDE";
    public static String EXTRA_GEOFENCE_LONGITUDE = "EXTRA_GEOFENCE_LONGITUDE";
    public static String EXTRA_GEOFENCE_RADIUS = "EXTRA_GEOFENCE_RADIUS";
    public static String EXTRA_GEOFENCE_EXPIRE_MILLISEC = "EXTRA_GEOFENCE_EXPIRE_MILLISEC";

    public static String INTENT_UPDATE_GEOFENCEVIEW = "INTENT_UPDATE_GEOFENCEVIEW";
    public static String INTENT_GEOFENCEEXTRA_SENSOR = "INTENT_GEOFENCEEXTRA_SENSOR";
    public static String INTENT_GEOFENCEEXTRA_LATITUDE = "INTENT_GEOFENCEEXTRA_LATITUDE";
    public static String INTENT_GEOFENCEEXTRA_LONGITUDE = "INTENT_GEOFENCEEXTRA_LONGITUDE";
    public static String INTENT_GEOFENCEEXTRA_RADIUS = "INTENT_GEOFENCEEXTRA_RADIUS";

    public static String ACTION_GETSUBSCRIPTION = "ACTION_GETSUBSCRIPTION";
    public static String EXTRA_BODY_GETSUBSCRIPTION = "EXTRA_BODY_GETSUBSCRIPTION";
    public static String ACTION_UPDATESUBSCRIPTION = "ACTION_UPDATESUBSCRIPTION";
    public static String EXTRA_BODY_UPDATESUBSCRIPTION = "EXTRA_BODY_UPDATESUBSCRIPTION";
    public static String ACTION_RECEIVEDSUBSCRIPTION = "ACTION_RECEIVEDSUBSCRIPTION";
    public static String EXTRA_SUBSCRIPTION_RESPONSE = "EXTRA_SUBSCRIPTION_RESPONSE";
    public static String ACTION_CHECKTHROUGHPUT = "ACTION_CHECKTHROUGHPUT";
    public static String EXTRA_BODY_CHECKTHROUGHPUT = "EXTRA_BODY_CHECKTHROUGHPUT";
    public static String ACTION_RECEIVEDTHROUGHPUT = "ACTION_RECEIVEDTHROUGHPUT";
    public static String EXTRA_THROUGHPUT_RESPONSE = "EXTRA_THROUGHPUT_RESPONSE";

    public static String INTENTVIEW_RECEIVED_SUBSCRIPTION = "INTENTVIEW_RECEIVED_SUBSCRIPTION";
    public static String EXTRAVIEW_RECEIVED_SUBSCRIPTION = "EXTRAVIEW_RECEIVED_SUBSCRIPTION";

    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final float ESTIMATE_BYTE_EXCHANGE = 500;
    public static final String THROUGHPUT_STRING =
            "0000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000" +
            "0000000000000000000000000000000000000000" +
            "000000000000000000000000000000000000";
}
