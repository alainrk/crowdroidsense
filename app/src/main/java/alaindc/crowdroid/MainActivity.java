package alaindc.crowdroid;

/**
 * Created by alain on 06/06/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Button button;
    private Button buttonLoc;
    private TextView textView;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        this.button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Start sending messages to server
                Intent serviceIntent = new Intent(getApplicationContext(), SendIntentService.class);
                serviceIntent.setAction(Constants.ACTION_SENDDATA);
                serviceIntent.putExtra(Constants.EXTRA_TYPE_OF_SENSOR_TO_SEND, Constants.TYPE_AMPLITUDE); // TODO Here set to send all kind of sensor for start
                getApplicationContext().startService(serviceIntent);
            }
        });

        this.buttonLoc = (Button) findViewById(R.id.buttonLoc);
        buttonLoc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Start intent service for update position
                Intent posintent = new Intent(getApplicationContext(), PositionIntentService.class);
                getApplicationContext().startService(posintent);

                // Start intent service for update sensors
                Intent sensorintent = new Intent(getApplicationContext(), SensorsIntentService.class);
                sensorintent.setAction(Constants.INTENT_START_SENSORS);
                getApplicationContext().startService(sensorintent);

                // Start intent service for update amplitude sensing
                Intent amplintent = new Intent(getApplicationContext(), SensorsIntentService.class);
                amplintent.setAction(Constants.INTENT_START_AUDIOAMPLITUDE_SENSE);
                getApplicationContext().startService(amplintent);
            }
        });

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.INTENT_RECEIVED_DATA)) {
                    String response = intent.getStringExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA);
                    if (response != null)
                        textView.append(response+"\n");
                } else if (intent.getAction().equals(Constants.INTENT_UPDATE_POS)) {
                    setLocationAndMap();
                } else if (intent.getAction().equals(Constants.INTENT_UPDATE_AMPLITUDE)) {
                    String response = intent.getStringExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA);
                    if (response != null)
                        textView.append(response+"\n");
                } else if (intent.getAction().equals(Constants.INTENT_UPDATE_SENSORS)) {
                    String response = intent.getStringExtra(Constants.INTENT_RECEIVED_DATA_EXTRA_DATA);
                    if (response != null)
                        textView.append(response+"\n");
                }
            }
        };

        IntentFilter rcvDataIntFilter = new IntentFilter(Constants.INTENT_RECEIVED_DATA);
        IntentFilter updatePosIntFilter = new IntentFilter(Constants.INTENT_UPDATE_POS);
        IntentFilter updateSenseIntFilter = new IntentFilter(Constants.INTENT_UPDATE_SENSORS);
        IntentFilter updateAmplIntFilter = new IntentFilter(Constants.INTENT_UPDATE_AMPLITUDE);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, rcvDataIntFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, updatePosIntFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, updateSenseIntFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, updateAmplIntFilter);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    }

    private void setLocationAndMap() {

        if (mMap == null)
            return;

        if (ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        int cameraZoom = 16;
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(Constants.PREF_FILE,Context.MODE_PRIVATE);

        double latitude = Double.parseDouble(sharedPref.getString(Constants.PREF_LATITUDE,"-1"));
        double longitude = Double.parseDouble(sharedPref.getString(Constants.PREF_LONGITUDE,"-1"));

        if (latitude < 0 || longitude < 0)
            return;

        LatLng target = new LatLng(latitude, longitude);

        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.clear();
        mMap.addMarker(
                new MarkerOptions()
                        .position(target)
                        .snippet("")
                        .title("")
        );

//        CircleOptions circleOptions = new CircleOptions()
//                .center(target)
//                .strokeWidth(7)
//                .fillColor(Color.argb( 60,238, 32, 32))
//                .strokeColor(Color.argb( 255,238, 32, 32))
//                .radius(10); // In meters

        // Get back the mutable Circle
//        mMap.addCircle(circleOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(target, cameraZoom);
        mMap.animateCamera(cameraUpdate);
        mMap.setMyLocationEnabled(true);
    }

}
