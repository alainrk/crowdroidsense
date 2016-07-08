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
