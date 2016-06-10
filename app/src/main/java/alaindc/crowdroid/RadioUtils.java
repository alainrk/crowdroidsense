package alaindc.crowdroid;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by narko on 10/06/16.
 */
public class RadioUtils {
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
