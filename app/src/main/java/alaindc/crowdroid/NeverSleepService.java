package alaindc.crowdroid;

/**
 * Created by alain on 08/06/16.
 */

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class NeverSleepService extends Service {

    private TelephonyManager telephonManager;

    public NeverSleepService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            telephonManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephonManager.listen(new CustomPhoneStateListener(this), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }
}
