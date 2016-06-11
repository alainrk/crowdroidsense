package alaindc.crowdroid;

/**
 * Created by alain on 11/06/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

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
        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        String type = RadioUtils.getNetClass(mContext);
        if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE && (type.equals("2G") || type.equals("3G") || type.equals("4G"))) {
            strength = signalStrength.getGsmSignalStrength();
            strength = (2 * strength) - 113; // -> dBm
        }

        SharedPreferences sharedPref = mContext.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.PREF_SENSOR_+Constants.TYPE_TEL, Integer.toString(strength));
        editor.commit();
    }

//    @Override
//    // See href="http://docs.oracle.com/javase/tutorial/reflect/">http://docs.oracle.com/javase/tutorial/reflect/</a>
//    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
//        super.onSignalStrengthsChanged(signalStrength);
//
//        int rssi = signalStrength.getCdmaDbm();
//        int snr = signalStrength.getEvdoSnr();
//        int evdorssi = signalStrength.getEvdoDbm();
//        int cdmaecio = signalStrength.getCdmaEcio();
//        int evdoecio = signalStrength.getEvdoEcio();
//        int a = signalStrength.getCdmaDbm();
//        int b = signalStrength.getGsmBitErrorRate();
//        int c = signalStrength.describeContents();
//
//        int signStrength = signalStrength.getGsmSignalStrength();
//        signStrength = (2 * signStrength) - 113; // -> dBm
//
//        SharedPreferences sharedPref = mContext.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPref.edit();
//        editor.putString(Constants.PREF_SENSOR_+Constants.TYPE_TEL, Integer.toString(signStrength));
//        editor.commit();
//
//        Log.i(LOG_TAG, "onSignalStrengthsChanged: " + signalStrength);
//
//        if (signalStrength.isGsm()) {
//            Log.i(LOG_TAG, "onSignalStrengthsChanged: getGsmBitErrorRate "
//                    + signalStrength.getGsmBitErrorRate());
//            Log.i(LOG_TAG, "onSignalStrengthsChanged: getGsmSignalStrength "
//                    + signalStrength.getGsmSignalStrength());
//        } else if (signalStrength.getCdmaDbm() > 0) {
//            Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaDbm "
//                    + signalStrength.getCdmaDbm());
//            Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaEcio "
//                    + signalStrength.getCdmaEcio());
//        } else {
//            Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoDbm "
//                    + signalStrength.getEvdoDbm());
//            Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoEcio "
//                    + signalStrength.getEvdoEcio());
//            Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoSnr "
//                    + signalStrength.getEvdoSnr());
//        }
//
//        // Reflection code starts from here
//        try {
//            Method[] methods = android.telephony.SignalStrength.class.getMethods();
//            for (Method mthd : methods) {
//                if (mthd.getName().equals("getLteSignalStrength")
//                        || mthd.getName().equals("getLteRsrp")
//                        || mthd.getName().equals("getLteRsrq")
//                        || mthd.getName().equals("getLteRssnr")
//                        || mthd.getName().equals("getLteCqi")) {
////                    Object test = mthd.invoke(signalStrength);
////                    String method = mthd.getName();
//                    Log.i(LOG_TAG, "onSignalStrengthsChanged: " + mthd.getName() + " " + mthd.invoke(signalStrength));
//                }
//            }
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//        // Reflection code ends here
//    }
}