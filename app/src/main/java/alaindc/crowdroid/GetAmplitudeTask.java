package alaindc.crowdroid;

/**
 * Created by alain on 08/06/16.
 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.SystemClock;
import java.io.IOException;
import java.io.StringReader;

public class GetAmplitudeTask {

    private MediaRecorder mRecorder = null;
    private AsyncTask<Void,Void,Void> asyncTask;
    private IntentService intentService;
    private boolean whileAsync = false;

    public GetAmplitudeTask (IntentService intentService) {
        this.intentService = intentService;
    }

    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            getAmplitude();
            try {
                mRecorder.start();
            } catch (RuntimeException e) {
                return;
            }

        }
    }

    public void stop() {
        if (mRecorder != null) {
            try {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
            } catch (RuntimeException e) {
                return;
            }
        }
    }

    public double getAmplitude() {
        if (mRecorder != null)
            return  mRecorder.getMaxAmplitude();
        else
            return 0;

    }

    public void getData(){
        asyncTask = new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                whileAsync = true;
                start();
                double amplitude = -1;
                int chance = 3;
                while(whileAsync) {
                    SystemClock.sleep(1000);
                    amplitude = getAmplitude();
                    chance --;
                    if (chance < 0 || amplitude > 0)
                        whileAsync = false;
                }

                stop();

                Intent sensorintent = new Intent(intentService.getApplicationContext(), SensorsIntentService.class);
                sensorintent.setAction(Constants.INTENT_RECEIVED_AMPLITUDE);
                sensorintent.putExtra(Constants.EXTRA_AMPLITUDE, amplitude);
                intentService.getApplicationContext().startService(sensorintent);

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stopTask(){
        this.whileAsync=false;
    }
}
