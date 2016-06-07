package alaindc.crowdroid;

/**
 * Created by alain on 06/06/16.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        this.button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent serviceIntent = new Intent(getApplicationContext(), PositionAndSenseIntentService.class);
                serviceIntent.setAction(PositionAndSenseIntentService.ACTION_SENDDATA);
                getApplicationContext().startService(serviceIntent);
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

}
