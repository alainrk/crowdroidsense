package alaindc.crowdroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.util.List;

/**
 * Created by narko on 10/06/16.
 */
public class RadioUtils extends PhoneStateListener {

    private Context context;

    public RadioUtils (Context context){
        this.context = context;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
        int signStrength = signalStrength.getGsmSignalStrength();
        signStrength = (2 * signStrength) - 113; // -> dBm

        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.PREF_SENSOR_+Constants.TYPE_TEL, Integer.toString(signStrength));
        editor.commit();
    }


    public static String[] getWifiInfo(Context context){
        try {
            WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMan.getConnectionInfo();

            String bssid = wifiInfo.getBSSID();
            String ssid = wifiInfo.getSSID();
            String rssi = Integer.toString(wifiInfo.getRssi());

            return new String[]{bssid, ssid, rssi};
        } catch (Exception e) {
            return new String[]{"","",""};
        }

    }

    public static String[] getTelInfo(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        String netType = "";

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                netType = "2G";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                netType = "3G";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                netType = "4G";
                break;
            default:
                netType = "Unknown";
        }

        String operatorName = mTelephonyManager.getNetworkOperatorName();
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.PREF_FILE,Context.MODE_PRIVATE);
        String signalStrength = sharedPref.getString(Constants.PREF_SENSOR_+Constants.TYPE_TEL,"0");

        return new String[]{netType,signalStrength,operatorName};
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
