package alaindc.crowdroid;

/**
 * Created by alain on 11/06/16.
 */
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";

    public GeofenceTransitionsIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = Constants.getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            Intent serviceIntent[] = new Intent[triggeringGeofences.size()];
            int i = 0;
            for (Geofence g: triggeringGeofences){
                String type_sensor = g.getRequestId();

                serviceIntent[i] = new Intent(getApplicationContext(), SendIntentService.class);
                serviceIntent[i].setAction(Constants.ACTION_SENDDATA+"GEO"+type_sensor);
                serviceIntent[i].putExtra(Constants.EXTRA_TYPE_OF_SENSOR_TO_SEND, Integer.parseInt(type_sensor));
                getApplicationContext().startService(serviceIntent[i]);
                i++;
            }

        }
    }
}
