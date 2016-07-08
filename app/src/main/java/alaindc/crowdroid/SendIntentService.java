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

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.MediaStore;
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
                final String response = intent.getStringExtra(Constants.EXTRA_SENSE_RESPONSE);
                handleActionReceivedData(response);
            } else if (action.startsWith(Constants.ACTION_GETSUBSCRIPTION)) {
                handleActionGetSubscriptions();
            } else if (action.startsWith(Constants.ACTION_UPDATESUBSCRIPTION)) {
                handleActionUpdateSubscriptions(intent.getStringExtra(Constants.EXTRA_BODY_UPDATESUBSCRIPTION));
            } else if (action.startsWith(Constants.ACTION_RECEIVEDSUBSCRIPTION)) {
                handleActionReceivedSubscriptions(intent.getStringExtra(Constants.EXTRA_SUBSCRIPTION_RESPONSE));
            } else if (action.startsWith(Constants.ACTION_RECEIVEDTHROUGHPUT)) {
                handleActionReceivedThroughput(intent.getLongExtra(Constants.EXTRA_THROUGHPUT_RESPONSE, -1));
            }
        }
    }

    // mstime <= 0 called by handleActionSendData if connected with wifi
    private void handleActionReceivedThroughput(long mstime) {
        float throughput = (mstime > 0) ? 1000*Constants.ESTIMATE_BYTE_EXCHANGE/(mstime) : 0;

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putFloat(Constants.THROUGHPUT_VALUE, throughput);
        editor.putBoolean(Constants.THROUGHPUT_TAKEN, true);
        editor.commit();

        handleActionSendData(Constants.TYPE_TEL);
    }

    private void handleActionGetSubscriptions() {
        String body;
        try {
            // Here we convert Java Object to JSON
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("user", RadioUtils.getMyDeviceId(this));
            JSONArray jsonArr = new JSONArray();
            jsonArr.put(jsonObj);
            body = jsonArr.toString();

        } catch (JSONException e) {
            return;
        }

        SendRequestTask sendreq = new SendRequestTask(clientApplication, this, body, Constants.SERVER_GETSUBSCRIPTION_URI);
        ClientCallback clientCallback = sendreq.doInBackground(Constants.POST);
    }

    private void handleActionUpdateSubscriptions(String body) {
        SendRequestTask sendreq = new SendRequestTask(clientApplication, this, body, Constants.SERVER_UPDATESUBSCRIPTION_URI);
        ClientCallback clientCallback = sendreq.doInBackground(Constants.POST);
    }

    private void handleActionReceivedSubscriptions(String response) {
        // Update view sending a broadcast intent
        Intent intent = new Intent(Constants.INTENTVIEW_RECEIVED_SUBSCRIPTION);
        intent.putExtra(Constants.EXTRAVIEW_RECEIVED_SUBSCRIPTION, response);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    // TODO REMOVEME Debug purpose
    private void handleActionSendData(int typeOfSensorToSend) {
        if (typeOfSensorToSend < 0)
            return;

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE,Context.MODE_PRIVATE);

        // Getting throughput
        if (typeOfSensorToSend == Constants.TYPE_TEL && !sharedPref.getBoolean(Constants.THROUGHPUT_TAKEN, false)){
            if (RadioUtils.ifWifiConnected(getApplicationContext())) {
                handleActionReceivedThroughput(-1);
                return;
            }
            SendRequestTask sendreq = new SendRequestTask(clientApplication, this, Constants.THROUGHPUT_STRING, Constants.SERVER_CALCTHROUGHPUT_URI);
            ClientCallback clientCallback = sendreq.doInBackground(Constants.POST);
            return;
        }

        String body;
        String sensordata;

        String sharedPreftypeSensor = Constants.PREF_SENSOR_+typeOfSensorToSend;
        String nameOfSensor = Constants.getNameOfSensor(typeOfSensorToSend);
        String units = Constants.getUnitsOfSensor(typeOfSensorToSend);

        Long tsLong = System.currentTimeMillis();
        String timestamp = tsLong.toString();
        //String date = getDate(tsLong);

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
            jsonObj.put("units", units);

            JSONArray jsonArr = new JSONArray();
            jsonArr.put(jsonObj);
            body = jsonArr.toString();

        } catch (JSONException e) {
            return;
        }

        SendRequestTask sendreq = new SendRequestTask(clientApplication, this, body, typeOfSensorToSend, Constants.SERVER_SENSINGSEND_URI);
        ClientCallback clientCallback = sendreq.doInBackground(Constants.POST);
    }

    private void handleActionReceivedData(String response) {

        // Data got from server response
        int timeout; // sec
        double radius; // meters
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
            radius = jsonObject.getDouble("radius");
            latitude = jsonObject.getDouble("lat");
            longitude = jsonObject.getDouble("long");

        } catch (JSONException e) {
            return;
        }

        if (sensor == Constants.TYPE_TEL) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constants.THROUGHPUT_TAKEN, false);
            editor.commit();
        }

        Intent geofenceIntent = new Intent(getApplicationContext(), GeofenceIntentService.class);
        geofenceIntent.putExtra(Constants.EXTRA_GEOFENCE_SENSORTYPE, sensor);
        geofenceIntent.putExtra(Constants.EXTRA_GEOFENCE_LATITUDE, latitude);
        geofenceIntent.putExtra(Constants.EXTRA_GEOFENCE_LONGITUDE, longitude);
        geofenceIntent.putExtra(Constants.EXTRA_GEOFENCE_RADIUS, String.valueOf(radius));
        geofenceIntent.putExtra(Constants.EXTRA_GEOFENCE_EXPIRE_MILLISEC, String.valueOf(timeout*1000));
        getApplicationContext().startService(geofenceIntent);

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
