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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;

public class CustomPhoneStateListener extends PhoneStateListener {
    Context mContext;
    private String LOG_TAG = "";

    public CustomPhoneStateListener(Context context) {
        mContext = context;
    }


    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        int strength = 0;
        String[] values = signalStrength.toString().split(" ");
        String wcdmaStrength = values[3];
        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        String type = RadioUtils.getNetClass(mContext);
        //if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && (type.equals("2G") || type.equals("3G") || type.equals("4G"))) {
        try {
            strength = signalStrength.getGsmSignalStrength();
            strength = (strength != 0) ? (2 * strength) - 113 : Integer.parseInt(wcdmaStrength); // GSM / UMTS(WCDMA)
        } catch (Exception e) {
            strength = 0;
            Log.d("Hack", "Big hack");
        }
        //}

        SharedPreferences sharedPref = mContext.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.PREF_SENSOR_+Constants.TYPE_TEL, Integer.toString(strength));
        editor.commit();

        // Update view
        Intent senseintent = new Intent(Constants.INTENT_UPDATE_SENSORS);
        senseintent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, "Sensor " + Constants.getNameOfSensor(Constants.TYPE_TEL) + " value: " + Integer.toString(strength));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(senseintent);
    }
}