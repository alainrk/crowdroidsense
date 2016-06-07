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
                Intent serviceIntent = new Intent(getApplicationContext(), SendIntentService.class);
                serviceIntent.setAction(SendIntentService.ACTION_SENDDATA);
                getApplicationContext().startService(serviceIntent);

                // TODO Debug REMOVEME and take it in broadcastreceiver like log
                setLocationAndMap();
            }
        });

        this.buttonLoc = (Button) findViewById(R.id.buttonLoc);
        buttonLoc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent posintent = new Intent(getApplicationContext(), PositionIntentService.class);
                getApplicationContext().startService(posintent);
            }
        });


        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String response = intent.getStringExtra("receivedDataFromServerExtra");
                if (response != null)
                    textView.append(response+"\n");
            }
        };
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter("receivedDataIntentActivity"));

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
//                .radius(jsonSingle.getInt("r")); // In meters
//
//        // Get back the mutable Circle
//        mMap.addCircle(circleOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(target, cameraZoom);
        mMap.animateCamera(cameraUpdate);
        mMap.setMyLocationEnabled(true);
    }

}
