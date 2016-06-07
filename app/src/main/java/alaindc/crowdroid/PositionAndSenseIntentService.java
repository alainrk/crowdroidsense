package alaindc.crowdroid;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;

import java.util.Calendar;

import de.uzl.itm.ncoap.application.client.ClientCallback;
import de.uzl.itm.ncoap.application.client.CoapClient;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class PositionAndSenseIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_SENDDATA = "alaindc.crowdroid.action.ACTION_SENDDATA";
    public static final String ACTION_RECEIVEDDATA = "alaindc.crowdroid.action.ACTION_RECEIVEDDATA";

    public static final String EXTRA_RESPONSE = "alaindc.crowdroid.extras.EXTRA_RESPONSE";

    private static long POST = 2;
    private static CoapClient clientApplication;

    public PositionAndSenseIntentService() {
        super("PositionAndSenseIntentService");
        clientApplication = new CoapClient();
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
//    public static void startActionBaz(Context context, String param1, String param2) {
//        Intent intent = new Intent(context, PositionAndSenseIntentService.class);
//        intent.setAction(ACTION_BAZ);
//        intent.putExtra(EXTRA_PARAM1, param1);
//        intent.putExtra(EXTRA_PARAM2, param2);
//        context.startService(intent);
//    }

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

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSendData() {
        Long tsLong = System.currentTimeMillis();
        String timestamp = tsLong.toString();
        String date = getDate(tsLong);
        SendRequestTask sendreq = new SendRequestTask(clientApplication, this, "Body test client android: "+date);
        ClientCallback clientCallback = sendreq.doInBackground(POST);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionReceivedData(String response) {
        Intent intent = new Intent("receivedDataIntentActivity");
        intent.putExtra("receivedDataFromServerExtra", response);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private String getDate(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("yyyy/MM/dd HH:mm:ss", cal).toString();
        return date;
    }
}
