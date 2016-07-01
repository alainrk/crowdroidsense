package alaindc.crowdroid;

/**
 * Created by alain on 06/06/16.
 */

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
import java.util.Random;

public class SensorsIntentService extends IntentService implements SensorEventListener {

    private SensorManager mSensorManager;
    private GetAmplitudeTask amplitudeTask;

    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;

    private Random random;

    public SensorsIntentService() {
        super("SensorsIntentService");
        random = new Random(System.currentTimeMillis());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(Constants.INTENT_START_SENSORS)) {

                // Init throughput taken
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(Constants.THROUGHPUT_TAKEN, false);
                editor.commit();

                // Configure sensors and eventlistener
                mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
                for (Sensor sensor : sensorList) {
                    if (Constants.isInMonitoredSensors(sensor.getType()))
                        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }

                // TODO STUB: Comment this in release
                for (int type: Constants.STUBBED_MONITORED_SENSORS) {
                    stub_onSensorChanged(type);
                }

            }

            if (action.equals(Constants.INTENT_START_AUDIOAMPLITUDE_SENSE)){
                // Configure amplitude and start TEST
                amplitudeTask = new GetAmplitudeTask(this);
                amplitudeTask.getData();
            }

            if (action.equals(Constants.INTENT_STUB_SENSOR_CHANGED + Sensor.TYPE_AMBIENT_TEMPERATURE)) {
                stub_onSensorChanged(intent.getIntExtra(Constants.INTENT_STUB_SENSOR_CHANGED_TYPE, -1));
            }

            if (action.equals(Constants.INTENT_STUB_SENSOR_CHANGED + Sensor.TYPE_PRESSURE)) {
                stub_onSensorChanged(intent.getIntExtra(Constants.INTENT_STUB_SENSOR_CHANGED_TYPE, -1));
            }

            if (action.equals(Constants.INTENT_STUB_SENSOR_CHANGED + Sensor.TYPE_RELATIVE_HUMIDITY)) {
                stub_onSensorChanged(intent.getIntExtra(Constants.INTENT_STUB_SENSOR_CHANGED_TYPE, -1));
            }

            if (action.equals(Constants.INTENT_RECEIVED_AMPLITUDE)){
                double amplitude = intent.getDoubleExtra(Constants.EXTRA_AMPLITUDE,-1);

                if (amplitude > 0) {
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Constants.PREF_SENSOR_+Constants.TYPE_AMPLITUDE, Double.toString(amplitude));
                    editor.commit();

                    // Update view
                    Intent senseintent = new Intent(Constants.INTENT_UPDATE_SENSORS);
                    senseintent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, "Sensor " + Constants.getNameOfSensor(Constants.TYPE_AMPLITUDE) + " value: " + Double.toString(amplitude));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(senseintent);
                }

                int index = Constants.getIndexAlarmForSensor(Constants.TYPE_AMPLITUDE);

                // Set the alarms for next sensing of amplitude
                alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Intent intentAlarm = new Intent(getApplicationContext(), SensorsIntentService.class);
                intentAlarm.setAction(Constants.INTENT_START_AUDIOAMPLITUDE_SENSE);
                alarmIntent = PendingIntent.getService(getApplicationContext(), 0, intentAlarm, 0);

                // TIMEOUT for another monitoring of audio
                int seconds = 30; // TODO: De-hardcode this
                alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() +
                                seconds * 1000, alarmIntent);

            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////// STUB IN SUBSTITUTION OF REAL SENSORS /////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public void stub_onSensorChanged(int typeSensor) {
        if (typeSensor < 0)
            return;

        float value, minf, maxf;
        switch (typeSensor) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                minf = -20;
                maxf = 42;
                break;
            case Sensor.TYPE_PRESSURE: // https://it.wikipedia.org/wiki/Pressione_atmosferica
                minf = 870;
                maxf = 1085;
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                minf = 30;
                maxf = 100;
                break;
            default:
                minf = 0;
                maxf = 0;
                break;
        }

        value = random.nextFloat() * (maxf - minf) + minf;

        int index = Constants.getIndexAlarmForSensor(typeSensor);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.PREF_SENSOR_ + typeSensor, Float.toString(value));
        editor.commit();

        // Update view
        Intent senseintent = new Intent(Constants.INTENT_UPDATE_SENSORS);
        senseintent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, "Sensor " + Constants.getNameOfSensor(typeSensor) + " value: " + Float.toString(value));
        LocalBroadcastManager.getInstance(this).sendBroadcast(senseintent);

        // Set the alarm random
        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(getApplicationContext(), SensorsIntentService.class);
        intentAlarm.setAction(Constants.INTENT_STUB_SENSOR_CHANGED + typeSensor);
        intentAlarm.putExtra(Constants.INTENT_STUB_SENSOR_CHANGED_TYPE, typeSensor);
        alarmIntent = PendingIntent.getService(getApplicationContext(), 0, intentAlarm, 0);

        // TODO Set timeout time from server indications
        int seconds = random.nextInt(50) + 10; // 10/60 sec
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        seconds * 1000, alarmIntent);
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        Log.d("SENSORS_INTENTSERVICE", sensor.toString());
    }

    public final void onSensorChanged(SensorEvent event) {
        Log.d("SENSORS_INTENTSERVICE", event.toString());

        // Hack Reduce updates
        if (random.nextInt(10) < 9)
            return;

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.PREF_SENSOR_ +event.sensor.getType(), Float.toString(event.values[0]));
        editor.commit();

        // Update view
        Intent senseintent = new Intent(Constants.INTENT_UPDATE_SENSORS);
        senseintent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, "Sensor " + Constants.getNameOfSensor(event.sensor.getType()) + " value: " + Float.toString(event.values[0]));
        LocalBroadcastManager.getInstance(this).sendBroadcast(senseintent);
    }

}