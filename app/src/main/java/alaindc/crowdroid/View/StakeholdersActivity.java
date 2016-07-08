/*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.

* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*
* This file is part of Crowdroid(sense)/Servercoap project, thesis in Crowdsensing.
* Copyright (C) 2016 Alain Di Chiappari
*/

package alaindc.crowdroid.View;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.vision.text.Line;

import org.jboss.netty.buffer.LittleEndianHeapChannelBuffer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import alaindc.crowdroid.Constants;
import alaindc.crowdroid.PositionIntentService;
import alaindc.crowdroid.R;
import alaindc.crowdroid.RadioUtils;
import alaindc.crowdroid.SendIntentService;
import alaindc.crowdroid.SendRequestTask;
import alaindc.crowdroid.SensorsIntentService;
import de.uzl.itm.ncoap.application.client.ClientCallback;
import de.uzl.itm.ncoap.application.client.CoapClient;


public class StakeholdersActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    Button updateStakeButton;
    Button sendStakeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stakeholders);

        linearLayout = (LinearLayout) findViewById(R.id.subscribesLinearLayout);
        updateStakeButton = (Button) findViewById(R.id.updateStakeButton);
        sendStakeButton = (Button) findViewById(R.id.sendStakeButton);

        updateStakeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent subscrIntent = new Intent(getApplicationContext(), SendIntentService.class);
                subscrIntent.setAction(Constants.ACTION_GETSUBSCRIPTION);
                getApplicationContext().startService(subscrIntent);
            }
        });

        sendStakeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                try {
                    JSONArray jsonArr = new JSONArray();

                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("user", RadioUtils.getMyDeviceId(getApplicationContext()));
                    jsonArr.put(jsonObj);

                    for (int i = 0; i < linearLayout.getChildCount(); i++) {
                        View view = linearLayout.getChildAt(i);
                        if (view instanceof CheckBox) {
                            CheckBox c = (CheckBox) view;
                            if (c.isChecked()) {
                                jsonObj = new JSONObject();
                                jsonObj.put("id", c.getId());
                                jsonArr.put(jsonObj);
                            }
                        }
                    }

                    String body = jsonArr.toString();

                    Intent subscrIntent = new Intent(getApplicationContext(), SendIntentService.class);
                    subscrIntent.setAction(Constants.ACTION_UPDATESUBSCRIPTION);
                    subscrIntent.putExtra(Constants.EXTRA_BODY_UPDATESUBSCRIPTION, body);
                    getApplicationContext().startService(subscrIntent);

                } catch (JSONException e) {
                    return;
                }
            }
        });

        Intent subscrIntent = new Intent(getApplicationContext(), SendIntentService.class);
        subscrIntent.setAction(Constants.ACTION_GETSUBSCRIPTION);
        getApplicationContext().startService(subscrIntent);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.INTENTVIEW_RECEIVED_SUBSCRIPTION)) {
                    String response = intent.getStringExtra(Constants.EXTRAVIEW_RECEIVED_SUBSCRIPTION);

                    //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();

                    linearLayout.removeAllViews();

                    try {
                        JSONArray jsonArray = new JSONArray(response);

                        for (int i=0; i<jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            int id = jsonObject.getInt("id");
                            String name = jsonObject.getString("name");
                            boolean subscribed = jsonObject.getInt("subscribed") == 1;

                            CheckBox checkBox = new CheckBox(getApplicationContext());
                            checkBox.setTextColor(Color.BLACK);
                            checkBox.setText(name);
                            checkBox.setId(id);
                            checkBox.setChecked(subscribed);

                            linearLayout.addView(checkBox);
                            int idx = linearLayout.indexOfChild(checkBox);
                            checkBox.setTag(Integer.toString(idx));
                        }

                    } catch (JSONException e) {
                        return;
                    }

                } else {
                    Log.d("","");
                }
            }
        };

        IntentFilter rcvDataIntFilter = new IntentFilter(Constants.INTENTVIEW_RECEIVED_SUBSCRIPTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, rcvDataIntFilter);

    }

}
