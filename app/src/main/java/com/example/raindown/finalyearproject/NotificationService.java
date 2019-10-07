package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import org.json.*;
import java.util.*;
import Helper.*;

/*Author : Adelina Tang Chooi Li & Chin Wei Song
Programme : RSD3
Year : 2017*/

public class NotificationService extends Service implements MqttCallback {

    public static MqttAndroidClient client;
    String clientId = MqttClient.generateClientId();
    List<Subscribe> arrSubscribe = new ArrayList<>();
    String tpc = "";
    static String USERNAME = "xyywumtr";
    static String PASSWORD = "nzUZoyHS0ADF";

    public NotificationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

        client = new MqttAndroidClient(this.getApplicationContext(), Constant.serverUrl, clientId);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        tpc = pref.getString("topicstr", null);
        Log.d("topic", tpc);
       // getFollowTopic();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        Log.d("followMessageArrived", message.toString());
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.icon_logo)
                        .setContentTitle("Buy + Sell")
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentText("" + message);


        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }


    public void getFollowTopic() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(this.getApplication());

                JsonArrayRequest followChatObjectRequest = new JsonArrayRequest(Constant.serverFile + "followNotification.php?studentID=" + tpc,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    arrSubscribe.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject subscribeResponse = (JSONObject) response.get(i);
                                        arrSubscribe.add(new Subscribe(subscribeResponse.getString("subscribeID"), subscribeResponse.getString("subscribeeID"),
                                                subscribeResponse.getString("subscriberID"), subscribeResponse.getString("subscribeDate"),
                                                subscribeResponse.getString("unsubscribeDate"), subscribeResponse.getString("subscribeStatus")));
                                    }
                                    Log.d("arrSubscribe1", arrSubscribe.size() + "");
                                    try {
                                        MqttConnectOptions options = new MqttConnectOptions();
                                        //edit
                                        options.setUserName(USERNAME);

                                        options.setPassword(PASSWORD.toCharArray());

                                        IMqttToken token = client.connect(options);
                                        Log.d("arrSubscribe2", arrSubscribe.size() + "");
                                        token.setActionCallback(new IMqttActionListener() {
                                            @Override
                                            public void onSuccess(IMqttToken asyncActionToken) {

                                                client.setCallback(NotificationService.this);
                                                int qos = 1;

                                                try {
                                                    for (int i = 0; i < arrSubscribe.size(); i++) {
                                                        Log.d("arrSubscribe3", arrSubscribe.get(i).getSubscribeeID2() + "");
                                                        IMqttToken subToken = client.subscribe(arrSubscribe.get(i).getSubscribeeID2(), qos);
                                                        subToken.setActionCallback(new IMqttActionListener() {
                                                            @Override
                                                            public void onSuccess(IMqttToken asyncActionToken) {
                                                                Log.d("subscribeNotification", "Success");
                                                            }

                                                            @Override
                                                            public void onFailure(IMqttToken asyncActionToken,
                                                                                  Throwable exception) {
                                                                Log.d("subscribeNotification", "Failure");
                                                            }
                                                        });
                                                    }
                                                } catch (MqttException e) {
                                                    e.printStackTrace();
                                                } catch (NullPointerException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                                Log.d("error", "Error");
                                            }
                                        });
                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                            }
                        });
                queue.add(followChatObjectRequest);

            } else {
                Toast.makeText(this.getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this.getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


}
