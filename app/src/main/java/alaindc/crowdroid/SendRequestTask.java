package alaindc.crowdroid;

/**
 * Created by alain on 06/06/16.
 */

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Random;

import de.uzl.itm.ncoap.application.client.ClientCallback;
import de.uzl.itm.ncoap.application.client.CoapClient;
import de.uzl.itm.ncoap.communication.blockwise.BlockSize;
import de.uzl.itm.ncoap.message.CoapMessage;
import de.uzl.itm.ncoap.message.CoapRequest;
import de.uzl.itm.ncoap.message.CoapResponse;
import de.uzl.itm.ncoap.message.MessageType;

public class SendRequestTask extends AsyncTask<Long, Void, SendRequestTask.SpitfirefoxCallback> {

    private CoapClient coapClient;
    private IntentService intentService;
    private String bodytext;
    private Random random;
    private String requestUri;

    private int mCurrentSensorSending;

    public SendRequestTask(CoapClient coapClient, IntentService intentService, String bodytext, int sensorType, String requestUri){
        this.coapClient = coapClient;
        this.bodytext = bodytext;
        this.intentService = intentService;
        this.random = new Random(System.currentTimeMillis());
        this.mCurrentSensorSending = sensorType;
        this.requestUri = requestUri;
    }

    public SendRequestTask(CoapClient coapClient, IntentService intentService, String bodytext, String requestUri){
        this.coapClient = coapClient;
        this.bodytext = bodytext;
        this.intentService = intentService;
        this.random = new Random(System.currentTimeMillis());
        this.requestUri = requestUri;
    }

    private BlockSize getBlock2Size() {
        return BlockSize.getBlockSize(-1);
    }

    private BlockSize getBlock1Size() {
        return BlockSize.getBlockSize(-1);
    }

    private byte[][] getOpaqueOptionValues(String hex) throws IllegalArgumentException{
        if(!"".equals(hex)) {
            String[] tmp = hex.split(";");
            byte[][] result = new byte[tmp.length][];
            for (int i = 0; i < tmp.length; i++) {
                result[i] = hexStringToByteArray(tmp[i]);
            }
            return result;
        } else {
            return new byte[0][];
        }
    }

    private static byte[] hexStringToByteArray(String hex) throws IllegalArgumentException {
        // add leading zero if necessary
        hex = hex.length() % 2 == 0 ? hex : "0" + hex;

        int length = hex.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int leftBits = Character.digit(hex.charAt(i), 16);
            int rightBits = Character.digit(hex.charAt(i+1), 16);
            if(leftBits == -1 || rightBits == -1) {
                throw new IllegalArgumentException("No HEX value: '" + hex.charAt(i) + hex.charAt(i+1) + "'");
            } else {
                data[i / 2] = (byte) ((leftBits << 4) + rightBits);
            }
        }
        return data;
    }

    @Override
    protected SpitfirefoxCallback doInBackground(Long... method){

        String serverName, acceptedFormats, payloadFormat, payload, ifMatch, etags;
        int portNumber;
        boolean confirmable, observe;
        BlockSize block1Size, block2Size;

        serverName = Constants.SERVER_ADDR;
        portNumber = Constants.SERVER_PORT;
        confirmable = true;
        observe = false;
        acceptedFormats = "";
        payloadFormat = "0";
        payload = bodytext;
        ifMatch = "";
        etags = "";
        block2Size = this.getBlock2Size();
        block1Size = this.getBlock1Size();

        try{
            if("".equals(serverName)) {
                return null;
            }

            //Create socket address from server name and port
            InetSocketAddress remoteEndpoint = new InetSocketAddress(InetAddress.getByName(serverName), portNumber);

            int messageType = (confirmable) ? MessageType.CON : MessageType.NON;

            //Create URI from server name, port and service path (and query)
            URI serviceURI = new URI("coap", null, serverName, remoteEndpoint.getPort(), requestUri, null, null);

            //Create initial CoAP request
            CoapRequest coapRequest = new CoapRequest(messageType, method[0].intValue(), serviceURI);

            //Set if-match option values (if any)
            try {
                coapRequest.setIfMatch(getOpaqueOptionValues(ifMatch));
            } catch (IllegalArgumentException ex) {
                return null;
            }

            //Set etag option values (if any)
            try {
                coapRequest.setEtags(getOpaqueOptionValues(etags));
            } catch (IllegalArgumentException ex) {
                return null;
            }

            //Set observe option (for GET only)
            if(observe && method[0] != 1) {
                return null;
            } else if (observe) {
                coapRequest.setObserve(0);
            }

            //Set accept option values in request (if any)
            if(!("".equals(acceptedFormats))){
                String[] array = acceptedFormats.split(";");
                long[] acceptOptionValues = new long[array.length];
                for(int i = 0; i < acceptOptionValues.length; i++) {
                    if(!("".equals(array[i]))) {
                        acceptOptionValues[i] = Long.valueOf(array[i]);
                    }
                }
                coapRequest.setAccept(acceptOptionValues);
            }

            //Set block options (if any)
            if(BlockSize.UNBOUND != block1Size) {
                coapRequest.setPreferredBlock1Size(block1Size);
            }

            if(BlockSize.UNBOUND != block2Size) {
                coapRequest.setPreferredBlock2Size(block2Size);
            }

            //Set payload and payload related options in request (if any)
            if(!("".equals(payload)) && "".equals(payloadFormat)){
                return null;
            } else if (!("".equals(payloadFormat))){
                coapRequest.setContent(payload.getBytes(CoapMessage.CHARSET), Long.valueOf(payloadFormat));
            }

            //Create callback and send request
            SpitfirefoxCallback clientCallback = new SpitfirefoxCallback(
                    serviceURI, coapRequest.isObservationRequest()
            );

            this.coapClient.sendCoapRequest(coapRequest, remoteEndpoint, clientCallback);

            return clientCallback;

        } catch (final Exception e) {
            return null;
        }
    }

    public class SpitfirefoxCallback extends ClientCallback {

        private URI serviceURI;
        private int retransmissionCounter; //TODO See how and where retransmit
        private long startTime;
        private boolean observationCancelled;

        private SpitfirefoxCallback(URI serviceURI, boolean observation) {
            this.serviceURI = serviceURI;
            this.retransmissionCounter = 0;
            this.startTime = System.currentTimeMillis();
            this.observationCancelled = !observation;
        }


        @Override
        public void processCoapResponse(CoapResponse coapResponse) {
            long duration = System.currentTimeMillis() - startTime;

            if (requestUri.equals(Constants.SERVER_SENSINGSEND_URI)) {
                String text = coapResponse.getContent().toString(CoapMessage.CHARSET);

                int ran = random.nextInt(500000); // HACK To avoid action intentservice override

                Intent serviceIntent = new Intent(intentService.getApplicationContext(), SendIntentService.class);
                serviceIntent.setAction(Constants.ACTION_RECEIVEDDATA+ran);
                serviceIntent.putExtra(Constants.EXTRA_SENSE_RESPONSE, text);
                intentService.getApplicationContext().startService(serviceIntent);
            }
            else if (requestUri.equals(Constants.SERVER_GETSUBSCRIPTION_URI)) {
                String text = coapResponse.getContent().toString(CoapMessage.CHARSET);

                Intent serviceIntent = new Intent(intentService.getApplicationContext(), SendIntentService.class);
                serviceIntent.setAction(Constants.ACTION_RECEIVEDSUBSCRIPTION);
                serviceIntent.putExtra(Constants.EXTRA_SUBSCRIPTION_RESPONSE, text);
                intentService.getApplicationContext().startService(serviceIntent);
            }
            else if (requestUri.equals(Constants.SERVER_CALCTHROUGHPUT_URI)) {
                Intent serviceIntent = new Intent(intentService.getApplicationContext(), SendIntentService.class);
                serviceIntent.setAction(Constants.ACTION_RECEIVEDTHROUGHPUT);
                serviceIntent.putExtra(Constants.EXTRA_THROUGHPUT_RESPONSE, duration);
                intentService.getApplicationContext().startService(serviceIntent);
            }
            else if (requestUri.equals(Constants.SERVER_UPDATESUBSCRIPTION_URI)) {
                return; // TODO Give a feedback to user
            }

        }

        @Override
        public void processEmptyAcknowledgement(){
            Log.d("","");
        }

        @Override
        public void processReset(){
            Log.d("","");
        }

        @Override
        public void processTransmissionTimeout(){
            if (requestUri.equals(Constants.SERVER_SENSINGSEND_URI)) {
                // If timeout, re-queue an intent for sending
                Intent resendIntent = new Intent(intentService.getApplicationContext(), SendIntentService.class);
                resendIntent.setAction(Constants.ACTION_SENDDATA + mCurrentSensorSending);
                resendIntent.putExtra(Constants.EXTRA_TYPE_OF_SENSOR_TO_SEND, mCurrentSensorSending); // TODO Here set to send all kind of sensor for start
                intentService.getApplicationContext().startService(resendIntent);
            }
        }

        @Override
        public void processResponseBlockReceived(final long receivedLength, final long expectedLength) {
            Log.d("","");
        }

        @Override
        public void processRetransmission(){
            Log.d("SENDREQUESTTASK","Retransmission num. "+Integer.toString(retransmissionCounter++));
        }

        @Override
        public void processMiscellaneousError(final String description) {
            Log.d("","");
        }

        public void cancelObservation(){
            this.observationCancelled = true;
        }

        @Override
        public boolean continueObservation() {
            return !this.observationCancelled;
        }
    }
}

