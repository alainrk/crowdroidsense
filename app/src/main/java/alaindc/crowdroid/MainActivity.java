package alaindc.crowdroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.URI;

import de.uzl.itm.ncoap.application.client.CoapClient;
import de.uzl.itm.ncoap.message.CoapMessage;
import de.uzl.itm.ncoap.message.CoapResponse;
import de.uzl.itm.ncoap.message.options.UintOptionValue;

public class MainActivity extends AppCompatActivity {
    private static long POST = 2;
    private CoapClient clientApplication;
    private Button button;

    public CoapClient getClientApplication(){
        return this.clientApplication;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.clientApplication = new CoapClient();

        this.button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new SendRequestTask(MainActivity.this, "Body test client android: ").execute(POST);
            }
        });
    }

    public void processResponse(final CoapResponse coapResponse, final URI serviceURI, final long duration) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long block2Num = coapResponse.getBlock2Number();
                String text = "Response received";
                if (block2Num != UintOptionValue.UNDEFINED) {
                    text += " (" + block2Num + " blocks in " + duration + " ms)";
                } else {
                    text += " (after " + duration + " ms)";
                }

                Toast.makeText(MainActivity.this, coapResponse.getContent().toString(CoapMessage.CHARSET), Toast.LENGTH_LONG).show();
            }
        });
    }

}
