package alaindc.crowdroid;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_SENDDATA = "alaindc.crowdroid.action.ACTION_SENDDATA";
    public static final String ACTION_RECEIVEDDATA = "alaindc.crowdroid.action.ACTION_RECEIVEDDATA";

    public static final String EXTRA_RESPONSE = "alaindc.crowdroid.extras.EXTRA_RESPONSE";

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
        Log.d("POSSENSEINTENTSERVICE: ","onHandleIntent!!!!!");

//        new SendRequestTask(clientApplication, "Body test client android: ").doInBackground(POST);

        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SENDDATA.equals(action)) {
                handleActionSendData();
            } else if (ACTION_RECEIVEDDATA.equals(action)) {
                final String response = intent.getStringExtra(EXTRA_RESPONSE);
                handleActionReceivedData(response);
            }
        }
    }

    // TODO REMOVEME Debug purpose
    private void handleActionSendData() {
        Long tsLong = System.currentTimeMillis();
        String timestamp = tsLong.toString();
        String date = getDate(tsLong);

        SendRequestTask sendreq = new SendRequestTask(clientApplication, this, "Body test client android: "+date);
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
        intentAlarm.setAction(SendIntentService.ACTION_SENDDATA);
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
