package alaindc.crowdroid;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.text.format.DateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import de.uzl.itm.ncoap.application.client.ClientCallback;
import de.uzl.itm.ncoap.application.client.CoapClient;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SendIntentService extends IntentService {

    // ALARMS!!!!
    // https://developer.android.com/training/scheduling/alarms.html#type

    // TODO Create an array of these to have multiple timers for multiple sensors sending
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private static long POST = 2;
    private static CoapClient clientApplication;

    public SendIntentService() {
        super("SendIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        clientApplication = new CoapClient();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            // DIFFERENTIATE like in sensorIntentService per each sensor!
            if (action.startsWith(Constants.ACTION_SENDDATA)) {
                handleActionSendData(intent.getIntExtra(Constants.EXTRA_TYPE_OF_SENSOR_TO_SEND, -1)); // Name in shared preference
            } else if (action.startsWith(Constants.ACTION_RECEIVEDDATA)) {
                final String response = intent.getStringExtra(Constants.EXTRA_RESPONSE);
                handleActionReceivedData(response);
            }
        }
    }

    // TODO REMOVEME Debug purpose
    private void handleActionSendData(int typeOfSensorToSend) {
        if (typeOfSensorToSend < 0)
            return;

        String body;
        String sensordata;

        String sharedPreftypeSensor = Constants.PREF_SENSOR_+typeOfSensorToSend;
        String nameOfSensor = Constants.getNameOfSensor(typeOfSensorToSend);

        Long tsLong = System.currentTimeMillis();
        String timestamp = tsLong.toString();
        //String date = getDate(tsLong);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE,Context.MODE_PRIVATE);
        String longitude = sharedPref.getString(Constants.PREF_LONGITUDE,"-1");
        String latitude = sharedPref.getString(Constants.PREF_LATITUDE,"-1");

        if (typeOfSensorToSend == Constants.TYPE_WIFI) {
            sensordata = TextUtils.join(",", RadioUtils.getWifiInfo(this));
        } else if (typeOfSensorToSend == Constants.TYPE_TEL){
            sensordata = TextUtils.join(",", RadioUtils.getTelInfo(this));
        } else {
            sensordata = sharedPref.getString(sharedPreftypeSensor, "-1");
        }

        try {
            // Here we convert Java Object to JSON
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("user", RadioUtils.getMyDeviceId(this));
            jsonObj.put("time", timestamp); // Set the first name/pair
            jsonObj.put("lat", latitude);
            jsonObj.put("long", longitude);
            jsonObj.put("sensor", typeOfSensorToSend);
            jsonObj.put("value", sensordata);

            JSONArray jsonArr = new JSONArray();
            jsonArr.put(jsonObj);
            body = jsonArr.toString();

        } catch (JSONException e) {
            return;
        }

        SendRequestTask sendreq = new SendRequestTask(clientApplication, this, body);
        ClientCallback clientCallback = sendreq.doInBackground(POST);
    }

    private void handleActionReceivedData(String response) {

        // Data got from server response
        int timeout; // sec
        int radius; // meters
        int sensor;
        double latitude, longitude;

        // Update view sending a broadcast intent
        Intent intent = new Intent(Constants.INTENT_RECEIVED_DATA);
        intent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, response);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        try {
            JSONArray jsonArray = new JSONArray(response);
            JSONObject jsonObject = jsonArray.getJSONObject(0);

            sensor = jsonObject.getInt("sensor");
            // For time homogeneity
            timeout = jsonObject.getInt("timeout");
            // For space homogeneity
            radius = jsonObject.getInt("radius");
            latitude = jsonObject.getDouble("lat");
            longitude = jsonObject.getDouble("long");

        } catch (JSONException e) {
            return;
        }

        // TODO Set geofence based on server response

        // Set timeout based on server response
        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(getApplicationContext(), SendIntentService.class);
        intentAlarm.setAction(Constants.ACTION_SENDDATA+sensor);
        intentAlarm.putExtra(Constants.EXTRA_TYPE_OF_SENSOR_TO_SEND, sensor);
        alarmIntent = PendingIntent.getService(getApplicationContext(), 0, intentAlarm, 0);

        int seconds = timeout;
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        seconds * 1000, alarmIntent);
    }

    private String getDate(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("yyyy/MM/dd HH:mm:ss", cal).toString();
        return date;
    }
}
