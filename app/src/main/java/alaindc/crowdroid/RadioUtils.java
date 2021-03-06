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
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class RadioUtils {

    public static String[] getWifiInfo(Context context){
        try {
            WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMan.getConnectionInfo();

            String bssid = wifiInfo.getBSSID();
            String ssid = wifiInfo.getSSID();
            String signalStrength = String.valueOf(wifiInfo.getRssi());

            // Update view
            Intent senseintent = new Intent(Constants.INTENT_UPDATE_SENSORS);
            senseintent.putExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA, ssid + " " + signalStrength);
            LocalBroadcastManager.getInstance(context).sendBroadcast(senseintent);

            return new String[]{bssid, ssid, signalStrength};
        } catch (Exception e) {
            return new String[]{"","",""};
        }

    }

    public static boolean ifWifiConnected(Context context) {
        ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCheck = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiCheck.isConnected();
    }

    public static String[] getTelInfo(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);

        String netType = getNetClass(context);
        String operatorName = mTelephonyManager.getNetworkOperatorName();

        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_FILE,Context.MODE_PRIVATE);
        String signalStrength = sharedPref.getString(Constants.PREF_SENSOR_+Constants.TYPE_TEL,"0");
        String throughput = String.valueOf(sharedPref.getFloat(Constants.THROUGHPUT_VALUE, 0));

        return new String[]{netType,signalStrength,operatorName,throughput};
    }

    public static String getNetClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

    public static String getMyDeviceId(Context context){
        try {
            WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMan.getConnectionInfo();

            return Integer.toString(Math.abs(wifiInfo.getMacAddress().hashCode()));
        } catch (Exception e) {
            return "000000000000000000000";
        }

    }

}
