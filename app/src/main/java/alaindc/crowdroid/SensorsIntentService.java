package alaindc.crowdroid;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

public class SensorsIntentService extends IntentService implements SensorEventListener {

    private SensorManager mSensorManager;
    private GetAmplitudeTask amplitudeTask;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    public SensorsIntentService() {
        super("SensorsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(Constants.INTENT_START_SENSORS)) {

                // Configure sensors and eventlistener
                mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
                for (Sensor sensor : sensorList) {
                    // TODO Create structures for sending, timeout etc.
                    if (Constants.isInMonitoredSensors(sensor.getType()))
                        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }

            if (action.equals(Constants.INTENT_START_AUDIOAMPLITUDE_SENSE)){
                // Configure amplitude and start TEST
                amplitudeTask = new GetAmplitudeTask(this);
                amplitudeTask.getData();
            }

            if (action.equals(Constants.INTENT_RECEIVED_AMPLITUDE)){
                double amplitude = intent.getDoubleExtra(Constants.EXTRA_AMPLITUDE,-1);

                if (amplitude > 0) {
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Constants.PREF_AMPLITUDE, Double.toString(amplitude));
                    editor.commit();

                    // Update view
                    Intent amplintent = new Intent(Constants.INTENT_UPDATE_AMPLITUDE);
                    amplintent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, "Amplitude value: "+ Double.toString(amplitude));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(amplintent);
                }

                // Set the alarms for next sensing of amplitude
                alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Intent intentAlarm = new Intent(getApplicationContext(), SensorsIntentService.class);
                intentAlarm.setAction(Constants.INTENT_START_AUDIOAMPLITUDE_SENSE);
                alarmIntent = PendingIntent.getService(getApplicationContext(), 0, intentAlarm, 0);

                // TODO Set here time
                int seconds = 10;
                alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() +
                                seconds * 1000, alarmIntent);

            }
        }
    }

    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        Log.d("SENSORS_INTENTSERVICE", sensor.toString());
    }

    public final void onSensorChanged(SensorEvent event) {
        //float millibars_of_pressure = event.values[0];
        Log.d("SENSORS_INTENTSERVICE", event.toString());

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.PREF_SENSOR_ +event.sensor.getType(), Float.toString(event.values[0]));
        editor.commit();

        // Update view
        Intent senseintent = new Intent(Constants.INTENT_UPDATE_SENSORS);
        senseintent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, "Sensor "+event.sensor.getName()+" value: "+ Float.toString(event.values[0]));
        LocalBroadcastManager.getInstance(this).sendBroadcast(senseintent);
    }

}