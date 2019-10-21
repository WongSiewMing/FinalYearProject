package com.example.raindown.finalyearproject;


import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.SyncStateContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.PrivateChat;
import Helper.PrivateChatOB;

import static com.example.raindown.finalyearproject.App.CHANNEL_1_ID;

public class ServiceCenter extends Service {

    private static final String TAG = "Service";
    private NotificationManagerCompat notificationManager;
    private Context context = this;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String UserID;
    private PrivateChatOB privateChat;
    private String notificationTitle = "", notificationMessage = "";


    public ServiceCenter() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(this, Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");
        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        notificationManager = NotificationManagerCompat.from(context);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {


            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d(TAG,"Message Arrived!");
                try {
                    JSONObject myjsonObj = new JSONObject(message.toString());
                    if (myjsonObj.getString("command").equals("303035303019")){
                        if (Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(UserID)){
                            privateChat = new PrivateChatOB(Conversion.hexToAscii(myjsonObj.getString("privateID")),Conversion.hexToAscii(myjsonObj.getString("studentID")),
                                    Conversion.hexToAscii(myjsonObj.getString("studentName")), Conversion.hexToAscii(myjsonObj.getString("recipient")),
                                    Conversion.hexToAscii(myjsonObj.getString("message")),
                                    Conversion.hexToAscii(myjsonObj.getString("image")),
                                    Conversion.hexToAscii(myjsonObj.getString("postDate")),
                                    Conversion.hexToAscii(myjsonObj.getString("postTime")));
                            notificationTitle = "New message from " + privateChat.getStudentName();
                            notificationMessage = privateChat.getMessage();

                            pushNotification(notificationTitle, notificationMessage);


                        }

                    }else if (myjsonObj.getString("command").equals("303035303054")){
                        if (Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(UserID)){
                            String recipient = Conversion.hexToAscii(myjsonObj.getString("studentID"));
                           // Log.d(TAG, "Recipient ID = " + recipient);
                            String statusCommand = "{\"command\": \"303035303055\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                    "\"studentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" ," +
                                    "\"recipient\": " + "\"" + Conversion.asciiToHex(recipient) + "\" ," +
                                    "\"status\": " + "\"" + Conversion.asciiToHex("ACTIVE") + "\" }";

                            pahoMqttClient.publishMessage(mqttAndroidClient, statusCommand, 1, "MY/TARUC/SSS/000000001/PUB");
                        }
                    }else if (myjsonObj.getString("command").equals("30303530305E")){
                        if (Conversion.hexToAscii(myjsonObj.getString("opponentID")).equals(UserID)){
                            notificationTitle = "Appointment request notification.";
                            notificationMessage = "New appointment request arrived.";

                            appointmentNotification(notificationTitle, notificationMessage);
                        }
                    }else if (myjsonObj.getString("command").equals("303035303062")){
                        Log.d(TAG,"Message in here");
                        if (Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(UserID)){
                            notificationTitle = "Appointment request notification.";
                            notificationMessage = "Your appointment request is " + Conversion.hexToAscii(myjsonObj.getString("appointmentStatus")).toLowerCase();

                            appointmentNotification(notificationTitle, notificationMessage);
                        }
                    }


                }catch (JSONException e){
                    e.printStackTrace();
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


        return START_STICKY;
    }

    private void appointmentNotification(String notificationTitle, String notificationMessage) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_privatechat)
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }

    private void pushNotification(String title, String message) {

//        if (!isAppOnForeground(context)){
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                    .setSmallIcon(R.drawable.ic_privatechat)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .build();

            notificationManager.notify(1, notification);
       // }
    }

    private boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        }catch (Exception e){

        }
    }
}
