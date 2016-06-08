package alaindc.crowdroid;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;

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
            if (Constants.ACTION_SENDDATA.equals(action)) {
                handleActionSendData(intent.getStringExtra(Constants.EXTRA_TYPE_OF_SENSOR_TO_SEND)); // Name in shared preference
            } else if (Constants.ACTION_RECEIVEDDATA.equals(action)) {
                final String response = intent.getStringExtra(Constants.EXTRA_RESPONSE);
                handleActionReceivedData(response);
            }
        }
    }

    // TODO REMOVEME Debug purpose
    private void handleActionSendData(String typeOfSensorToSend) {
        Long tsLong = System.currentTimeMillis();
        String timestamp = tsLong.toString();
        String date = getDate(tsLong);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE,Context.MODE_PRIVATE);
        String longitude = sharedPref.getString(Constants.PREF_LONGITUDE,"-1");
        String latitude = sharedPref.getString(Constants.PREF_LATITUDE,"-1");
        String sensordata = sharedPref.getString(typeOfSensorToSend, "-1");

        SendRequestTask sendreq = new SendRequestTask(clientApplication, this, date+", "+latitude+", "+longitude+" Sensor "+typeOfSensorToSend+": "+sensordata);
        ClientCallback clientCallback = sendreq.doInBackground(POST);
    }

    private void handleActionReceivedData(String response) {
        // Update view sending a broadcast intent
        Intent intent = new Intent(Constants.INTENT_RECEIVED_DATA);
        intent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, response);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        // Set the alarms
        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(getApplicationContext(), SendIntentService.class);
        intentAlarm.setAction(Constants.ACTION_SENDDATA);
        intentAlarm.putExtra(Constants.EXTRA_TYPE_OF_SENSOR_TO_SEND, ""); // TODO Here set wich sensor to send after time
        alarmIntent = PendingIntent.getService(getApplicationContext(), 0, intentAlarm, 0);

        // TODO Set here time
        int seconds = 999999;
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
