package alaindc.crowdroid;

/**
 * Created by alain on 07/06/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SensAndSendBcastRcv extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, SendIntentService.class);
            context.startService(serviceIntent);
        }
    }
}