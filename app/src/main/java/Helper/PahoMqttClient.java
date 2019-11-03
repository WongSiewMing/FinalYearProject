package Helper;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import java.io.UnsupportedEncodingException;


/*Author : Adelina Tang Chooi & Chin Wei Song
Programme : RSD3
Year : 2017*/

/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

public class PahoMqttClient {

    private static final String TAG = "PahoMqttClient";
    private MqttAndroidClient mqttAndroidClient;
    private String clientID;
    private String topic = "";
    private String message = "";

    static String USERNAME = "xyywumtr";
    static String PASSWORD = "nzUZoyHS0ADF";

    public MqttAndroidClient getMqttClient(Context context, String brokerUrl) {

        clientID = MqttClient.generateClientId() + System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientID);


        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "Success");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failure " + exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return mqttAndroidClient;
    }


    public MqttAndroidClient getMqttClient(Context context, String brokerUrl, String topicToSub){
        this.topic = topicToSub;
        clientID = MqttClient.generateClientId() + System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientID);

        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "Success");
                    try {
                        subscribe(mqttAndroidClient,topic,1);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failure " + exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return mqttAndroidClient;


    }


    public MqttAndroidClient getMqttClient(Context context, String brokerUrl, String msg, String tpc){
        this.topic = tpc;
        this.message = msg;
        clientID = MqttClient.generateClientId() + System.currentTimeMillis();
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientID);

        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "Success");
                    try {
                        publishMessage(mqttAndroidClient,message,1,topic);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failure " + exception.toString());
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        return mqttAndroidClient;


    }


    public void disconnect(@NonNull MqttAndroidClient client) throws MqttException {

        IMqttToken mqttToken = client.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Successfully disconnected");
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.d(TAG, "Failed to disconnected " + throwable.toString());
            }
        });
    }

    @NonNull
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {

        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    @NonNull
    private MqttConnectOptions getMqttConnectionOption() {

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setConnectionTimeout(10000);
        mqttConnectOptions.setKeepAliveInterval(600000);
        //edit
//        mqttConnectOptions.setUserName(USERNAME);
//        mqttConnectOptions.setPassword(PASSWORD.toCharArray());

        return mqttConnectOptions;
    }


    public void publishMessage(@NonNull MqttAndroidClient client, 
	@NonNull String msg, int qos, @NonNull String topic)
            throws MqttException, UnsupportedEncodingException {

        byte[] encodedPayload = new byte[0];
        encodedPayload = msg.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setRetained(false);
        message.setQos(qos);
       // client.publish(topic, message);
        IMqttToken token = client.publish(topic, message);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "Message publish successfully");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

            }
        });
        Log.d("lp",message.toString());

    }

    //for chat module
    public void publishChatMessage(@NonNull MqttAndroidClient client,
                                   @NonNull String msg, int qos, @NonNull String topic, final TextView chatStatus)
            throws MqttException, UnsupportedEncodingException {

        byte[] encodedPayload = new byte[0];
        encodedPayload = msg.getBytes("UTF-8");
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setRetained(false);
        message.setQos(qos);
        // client.publish(topic, message);
        IMqttToken token = client.publish(topic, message);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.d(TAG, "Message publish successfully");
                CountDownTimer countDownTimer = new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        chatStatus.setText("Message send successfully.");
                    }

                    @Override
                    public void onFinish() {
                        chatStatus.setText("");
                    }
                }.start();

            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

            }
        });
        Log.d("lp",message.toString());

    }


    public void subscribe(@NonNull MqttAndroidClient client, @NonNull final String topic, int qos) throws MqttException {

        IMqttToken token = client.subscribe(topic, qos);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Subscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "Subscribe Failed " + topic);

            }
        });
    }


    public void unSubscribe(@NonNull MqttAndroidClient client, @NonNull final String topic) throws MqttException {

        IMqttToken token = client.unsubscribe(topic);

        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "UnSubscribe Successfully " + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "UnSubscribe Failed " + topic);
            }
        });
    }
}
