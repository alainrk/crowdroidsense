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

                // Configure sensors and eventlistener
                mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
                for (Sensor sensor : sensorList) {
                    // TODO Create structures for sending, timeout etc.
                    if (Constants.isInMonitoredSensors(sensor.getType()))
                        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }

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
            // TODO Accorpare in modo generico con gli altri sensori (praticamente stub)
            if (action.equals(Constants.INTENT_RECEIVED_AMPLITUDE)){
                double amplitude = intent.getDoubleExtra(Constants.EXTRA_AMPLITUDE,-1);

                if (amplitude > 0) {
                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Constants.PREF_SENSOR_+Constants.TYPE_AMPLITUDE, Double.toString(amplitude));
                    editor.commit();

                    // Update view
                    Intent amplintent = new Intent(Constants.INTENT_UPDATE_AMPLITUDE);
                    amplintent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, "Amplitude value: "+ Double.toString(amplitude));
                    LocalBroadcastManager.getInstance(this).sendBroadcast(amplintent);
                }

                int index = Constants.getIndexAlarmForSensor(Constants.TYPE_AMPLITUDE);

                // Set the alarms for next sensing of amplitude
                alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                Intent intentAlarm = new Intent(getApplicationContext(), SensorsIntentService.class);
                intentAlarm.setAction(Constants.INTENT_START_AUDIOAMPLITUDE_SENSE);
                alarmIntent = PendingIntent.getService(getApplicationContext(), 0, intentAlarm, 0);

                // TODO Set timeout time from server indications
                int seconds = 10;
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

        // Random value
        float value = random.nextFloat();

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
        int seconds = random.nextInt(5) + 1;
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

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.PREF_SENSOR_ +event.sensor.getType(), Float.toString(event.values[0]));
        editor.commit();

        // Update view
        Intent senseintent = new Intent(Constants.INTENT_UPDATE_SENSORS);
        senseintent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, "Sensor " + event.sensor.getName() + " value: " + Float.toString(event.values[0]));
        LocalBroadcastManager.getInstance(this).sendBroadcast(senseintent);
    }

}