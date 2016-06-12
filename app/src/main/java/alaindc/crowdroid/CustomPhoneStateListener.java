package alaindc.crowdroid;

/**
 * Created by alain on 11/06/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    }
}