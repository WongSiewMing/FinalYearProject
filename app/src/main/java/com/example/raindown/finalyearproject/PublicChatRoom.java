package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.Context;
import android.graphics.Color;
import android.net.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import org.json.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class PublicChatRoom extends Fragment {

    View view;
    public static List<Message> arrayPublicChat = new ArrayList<>();
    Room msg;
    ArrayAdapter<Message> adapter;
    String message = "", formattedDate = "", time = "", jsonMsg = "", currentMessageID = "", messageID = "", jsonURL = "";
    String newSender = "", newRoomID = "", newMessage = "", newPostDate = "", newPostTime = "", newStudentName = "", encodedMessage = "";
    String currentParticipateID = "", participateID = "";
    ProgressDialog pDialog = null;
    Student s = null;
    ImageView send;
    Button participateButton;
    ListView list;
    public static MqttAndroidClient mqttAndroidClient;
    public static PahoMqttClient pahoMqttClient;
    EditText sendMessage;
    Calendar c;
    SimpleDateFormat df, sdf;
    boolean indicator = false;
    JSONObject jsonObj;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        msg = (Room) bundle.getSerializable("PublicList");
        s = (Student) bundle.getSerializable("Me");
        getActivity().setTitle(msg.getSubject());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mqttAndroidClient != null) {
            mqttAndroidClient.unregisterResources();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.publicchatroom, container, false);
        send = (ImageView) view.findViewById(R.id.send);
        sendMessage = (EditText) view.findViewById(R.id.sendMessage);
        participateButton = (Button) view.findViewById(R.id.participateButton);
        pDialog = new ProgressDialog(getActivity());

        if(Integer.parseInt(PublicList.participateCount) > 0){
            arrayPublicChat = PublicList.arrayPreviousMsg;
            setLayout();
        }

        if (Integer.parseInt(PublicList.participateCount) > 0) { //joining
            participateButton.setText("x Click to leave group");
            participateButton.setBackgroundColor(Color.parseColor("#ad1111"));
            send.setEnabled(true);
            sendMessage.setEnabled(true);
            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "Public" + msg.getRoomID());

        } else {
            participateButton.setText("+ Click to join group"); // not yet join
            participateButton.setBackgroundColor(Color.parseColor("#70a354"));
            send.setEnabled(false);
            sendMessage.setEnabled(false);
        }

        participateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (participateButton.getText().toString().equals("+ Click to join group")) {
                    indicator = true;
                    participateButton.setText("x Click to leave group");
                    participateButton.setBackgroundColor(Color.parseColor("#ad1111"));
                    send.setEnabled(true);
                    sendMessage.setEnabled(true);
                    pahoMqttClient = new PahoMqttClient();
                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "Public" + msg.getRoomID());
                    generateParticipateID();
                    mqttAndroidClient.setCallback(new MqttCallbackExtended() {
                        @Override
                        public void connectComplete(boolean reconnect, String serverURI) {

                        }

                        @Override
                        public void connectionLost(Throwable cause) {

                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            try {

                                JSONObject jsonObj = new JSONObject(message.toString());
                                arrayPublicChat.add(new Message(Conversion.hexToAscii(jsonObj.getString("sender")),
                                        Conversion.hexToAscii(jsonObj.getString("roomID")), Conversion.hexToAscii(jsonObj.getString("message")),
                                        Conversion.hexToAscii(jsonObj.getString("postDate")), Conversion.hexToAscii(jsonObj.getString("postTime")),
                                        Conversion.hexToAscii(jsonObj.getString("studentName"))));
                                newSender = Conversion.hexToAscii(jsonObj.getString("sender"));
                                newRoomID = Conversion.hexToAscii(jsonObj.getString("roomID"));
                                newMessage = Conversion.hexToAscii(jsonObj.getString("message"));
                                newPostDate = Conversion.hexToAscii(jsonObj.getString("postDate"));
                                newPostTime = Conversion.hexToAscii(jsonObj.getString("postTime"));
                                newStudentName = Conversion.hexToAscii(jsonObj.getString("studentName"));
                                Log.d("messageArrived", message.toString());
                                generateMessageID();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {

                        }
                    });

                    getPreviousPublicChat(getActivity(), Constant.serverFile + "getPreviousPublicChat.php?roomID=" + msg.getRoomID());

                } else {
                    participateButton.setText("+ Click to join group");
                    participateButton.setBackgroundColor(Color.parseColor("#70a354"));
                    send.setEnabled(false);
                    sendMessage.setEnabled(false);

                    arrayPublicChat.clear();
                    setLayout();
                    try {
                        pahoMqttClient.unSubscribe(mqttAndroidClient, "Public" + msg.getRoomID());
                        pahoMqttClient.disconnect(mqttAndroidClient);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                    updateParticipant();
                }
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                message = sendMessage.getText().toString().trim();
                c = Calendar.getInstance();
                df = new SimpleDateFormat("dd/MM/yyyy");
                formattedDate = df.format(c.getTime());
                sdf = new SimpleDateFormat("HH:mm");
                time = sdf.format(new Date());

                jsonMsg = "{\"command\": \"30303530301E\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"sender\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                        "\"roomID\": " + "\"" + Conversion.asciiToHex(msg.getRoomID()) + "\" ," +
                        "\"message\": " + "\"" + Conversion.asciiToHex(message) + "\" ," +
                        "\"postDate\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                        "\"postTime\": " + "\"" + Conversion.asciiToHex(time) + "\" ," +
                        "\"studentName\": " + "\"" + Conversion.asciiToHex(s.getStudentName()) + "\"}";

                sendMessage.setText("");

                try {
                    pahoMqttClient.publishMessage(mqttAndroidClient, jsonMsg, 1, "Public" + msg.getRoomID());
                    if (mqttAndroidClient != null && indicator == true) {
                        goToCallback();
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });

        if (mqttAndroidClient != null) {

            mqttAndroidClient.setCallback(new MqttCallbackExtended() {

                @Override
                public void connectComplete(boolean b, String s) {

                }

                @Override
                public void connectionLost(Throwable throwable) {

                }

                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                    try {
                        JSONObject jsonObj = new JSONObject(mqttMessage.toString());
                        arrayPublicChat.add(new Message(Conversion.hexToAscii(jsonObj.getString("sender")),
                                Conversion.hexToAscii(jsonObj.getString("roomID")), Conversion.hexToAscii(jsonObj.getString("message")),
                                Conversion.hexToAscii(jsonObj.getString("postDate")), Conversion.hexToAscii(jsonObj.getString("postTime")),
                                Conversion.hexToAscii(jsonObj.getString("studentName"))));
                        newSender = Conversion.hexToAscii(jsonObj.getString("sender"));
                        newRoomID = Conversion.hexToAscii(jsonObj.getString("roomID"));
                        newMessage = Conversion.hexToAscii(jsonObj.getString("message"));
                        newPostDate = Conversion.hexToAscii(jsonObj.getString("postDate"));
                        newPostTime = Conversion.hexToAscii(jsonObj.getString("postTime"));
                        newStudentName = Conversion.hexToAscii(jsonObj.getString("studentName"));
                        Log.d("messageArrived", mqttMessage.toString());
                        generateMessageID();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                }
            });
        }

        return view;
    }

    public void goToCallback() {

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                try {

                    JSONObject jsonObj = new JSONObject(mqttMessage.toString());
                    arrayPublicChat.add(new Message(Conversion.hexToAscii(jsonObj.getString("sender")),
                            Conversion.hexToAscii(jsonObj.getString("roomID")), Conversion.hexToAscii(jsonObj.getString("message")),
                            Conversion.hexToAscii(jsonObj.getString("postDate")), Conversion.hexToAscii(jsonObj.getString("postTime")),
                            Conversion.hexToAscii(jsonObj.getString("studentName"))));
                    newSender = Conversion.hexToAscii(jsonObj.getString("sender"));
                    newRoomID = Conversion.hexToAscii(jsonObj.getString("roomID"));
                    newMessage = Conversion.hexToAscii(jsonObj.getString("message"));
                    newPostDate = Conversion.hexToAscii(jsonObj.getString("postDate"));
                    newPostTime = Conversion.hexToAscii(jsonObj.getString("postTime"));
                    newStudentName = Conversion.hexToAscii(jsonObj.getString("studentName"));
                    Log.d("messageArrived", mqttMessage.toString());
                    generateMessageID();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }


    public void setLayout() {

        adapter = new MyListAdapter();
        list = (ListView) view.findViewById(R.id.dynamicPublicChat);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        list.post(new Runnable() {
            @Override
            public void run() {
                list.setSelection(adapter.getCount() - 1);
            }
        });

    }


    public class MyListAdapter extends ArrayAdapter<Message> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatepublicchatroom, arrayPublicChat);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatepublicchatroom, parent, false);
            }

            Message currentMsg = arrayPublicChat.get(position);

            TextView studentName = (TextView) itemView.findViewById(R.id.studentName);
            studentName.setText(currentMsg.getStudentName());

            TextView date = (TextView) itemView.findViewById(R.id.date);
            date.setText(currentMsg.getPostDate());

            TextView time = (TextView) itemView.findViewById(R.id.time);
            time.setText(currentMsg.getPostTime());

            TextView message = (TextView) itemView.findViewById(R.id.message);
            message.setText(currentMsg.getMessage());

            return itemView;

        }
    }


    public void generateParticipateID() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "generateParticipateID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject privateIDResponse = (JSONObject) response.get(i);
                                        currentParticipateID = privateIDResponse.getString("CurrentParticipateID");
                                    }
                                    if (currentParticipateID.equals("0")) {
                                        participateID = "PAR0001";
                                    } else {
                                        participateID = String.format("PAR%04d", (Integer.parseInt(currentParticipateID.substring(3, 7)) + 1));
                                    }
                                    insertParticipateData();
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                } catch (Exception e) {

                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                                if (pDialog.isShowing())
                                    pDialog.dismiss();
                            }
                        });
                queue.add(jsonObjectRequest);

            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

    }


    public void insertParticipateData() {

        c = Calendar.getInstance();
        df = new SimpleDateFormat("dd/MM/yyyy");
        formattedDate = df.format(c.getTime());

        try {
            jsonURL = Constant.serverFile + "insertParticipateData.php?participateID=" + participateID
                    + "&participant=" + s.getStudentID() + "&roomID=" +
                    msg.getRoomID() + "&dateJoined=" + formattedDate;

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        jsonURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (isAdded()) {
                                }
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);

                                    String success = jsonObject.getString("success");
                                    if (success.equals("1")) {
                                        Log.d("participateAdded", "Added");
                                    } else {
                                        Log.d("participateAdded", "Failed");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (isAdded()) {
                                }

                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("participateID", participateID);
                        params.put("participant", s.getStudentID());
                        params.put("roomID", msg.getRoomID());
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/x-www-form-urlencoded");
                        return params;
                    }
                };
                queue.add(postRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded()) {
            }
        }
    }


    public void updateParticipant() {

        c = Calendar.getInstance();
        df = new SimpleDateFormat("dd/MM/yyyy");
        formattedDate = df.format(c.getTime());

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                try {

                    StringRequest postRequest = new StringRequest(
                            Request.Method.POST,
                            Constant.serverFile + "updateParticipant.php?participant=" + s.getStudentID()
                                    + "&roomID=" + msg.getRoomID()
                                    + "&dateLeft=" + formattedDate,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (isAdded()) {
                                    }
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(response);
                                        String success = jsonObject.getString("success");
                                        if (success.equals("1")) {
                                            Log.d("updateParticipant", "Updated");

                                        } else {
                                            Log.d("updateParticipant", "Failed to update");
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (isAdded()) {
                                    }

                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();

                            params.put("participant", s.getStudentID());
                            params.put("roomID", msg.getRoomID());
                            params.put("dateLeft", formattedDate);

                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("Content-Type", "application/x-www-form-urlencoded");
                            return params;
                        }
                    };
                    queue.add(postRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

    }


    public void generateMessageID() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "generateMessageID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject privateIDResponse = (JSONObject) response.get(i);
                                        currentMessageID = privateIDResponse.getString("CurrentMessageID");
                                    }
                                    if (currentMessageID.equals("0")) {
                                        messageID = "MSG0001";
                                    } else {
                                        messageID = String.format("MSG%04d", (Integer.parseInt(currentMessageID.substring(3, 7)) + 1));
                                    }
                                    insertMessageData();
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                } catch (Exception e) {

                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                                if (pDialog.isShowing())
                                    pDialog.dismiss();
                            }
                        });
                queue.add(jsonObjectRequest);

            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void insertMessageData() {

        encodedMessage = "";

        try {
            String[] encodeMessage = {newMessage};
            for (String s : encodeMessage) {
                encodedMessage += URLEncoder.encode(s, "UTF-8");
            }

            jsonURL = Constant.serverFile + "insertMessageData.php?messageID=" + messageID + "&sender=" + newSender + "&roomID=" +
                    newRoomID + "&message=" + encodedMessage + "&postDate=" + newPostDate + "&postTime=" +
                    newPostTime;



            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        jsonURL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (isAdded()) {
                                }
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);
                                    Log.d("setlayout", jsonObject.toString());
                                    setLayout();

                                    String success = jsonObject.getString("success");
                                    if (success.equals("1")) {
                                        Log.d("messageAdded", "Added");

                                    } else {
                                        Log.d("messageAdded", "Failed");
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (isAdded()) {
                                }

                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("messageID", messageID);
                        params.put("sender", newSender);
                        params.put("roomID", newRoomID);
                        params.put("message", newMessage);
                        params.put("postDate", newPostDate);
                        params.put("postTime", newPostTime);

                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("Content-Type", "application/x-www-form-urlencoded");
                        return params;
                    }
                };
                queue.add(postRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded()) {
            }
        }
    }


    public void getPreviousPublicChat(Context context, String url) {
        RequestQueue queue = Volley.newRequestQueue(context);
        if (!pDialog.isShowing())
            pDialog.setMessage("Sync with server...");
        pDialog.show();

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            arrayPublicChat.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject previousChatResponse = (JSONObject) response.get(i);
                                arrayPublicChat.add(new Message(previousChatResponse.getString("sender"), previousChatResponse.getString("roomID"),
                                        previousChatResponse.getString("message"), previousChatResponse.getString("postDate"),
                                        previousChatResponse.getString("postTime"), previousChatResponse.getString("studentName")));
                            }
                            setLayout();
                            if (pDialog.isShowing())
                                pDialog.dismiss();
                        } catch (Exception e) {

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                        if (pDialog.isShowing())
                            pDialog.dismiss();
                    }
                });
        queue.add(jsonObjectRequest);
    }

}


