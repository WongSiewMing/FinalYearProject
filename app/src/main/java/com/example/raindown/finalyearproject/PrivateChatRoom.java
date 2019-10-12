package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.Context;
import android.content.Intent;
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

/*Author : Adelina Tang Chooi Li
Programme : RSD3
Year : 2017*/

public class PrivateChatRoom extends Fragment {

    View view;
    public List<PrivateChat> arrayPrivateChatNew = new ArrayList<>();
    PrivateChat currentPrivateRoom;
    ArrayAdapter<PrivateChat> adapter;
    String message = "", formattedDate = "", time = "", jsonURL = "", currentPrivateID = "", privateID = "",
            jsonMsg = "", newStudentID = "", newRecipient = "", newMessage = "", newPostDate = "", newPostTime = "",
            topicName = "", encodedMessage = "", name = "";
    Student recipient = null, student = null;
    Calendar c = null;
    EditText sendMessage;
    ImageView send;
    SimpleDateFormat df, sdf;
    ListView list;
    ProgressDialog pDialog = null;
    public static MqttAndroidClient mqttAndroidClient;
    public static PahoMqttClient pahoMqttClient;

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
        recipient = (Student) bundle.getSerializable("Recipient");
        getActivity().setTitle(recipient.getStudentName());


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mqttAndroidClient.unregisterResources();

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
        /*try {
            pahoMqttClient.unSubscribe(mqttAndroidClient, topicName);
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (MqttException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        view = inflater.inflate(R.layout.privatechatroom, container, false);
        Bundle bundle = getArguments();
        student = (Student) bundle.getSerializable("Me"); //me
        pDialog = new ProgressDialog(getActivity());

        arrayPrivateChatNew.clear();
        send = (ImageView) view.findViewById(R.id.send);
        sendMessage = (EditText) view.findViewById(R.id.sendMessage);

        name = "Private" + StuffDetails.confirmedTopic;
        topicName = name + "/+";
        arrayPrivateChatNew = StuffDetails.arrayPrivateChat;
        setLayout();

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, topicName);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                message = sendMessage.getText().toString().trim();
                c = Calendar.getInstance();
                df = new SimpleDateFormat("dd/MM/yyyy");
                formattedDate = df.format(c.getTime());
                sdf = new SimpleDateFormat("HH:mm");
                time = sdf.format(new Date());

                jsonMsg = "{\"command\": \"303035303019\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"studentID\": " + "\"" + Conversion.asciiToHex(student.getStudentID()) + "\" ," +
                        "\"studentName\": " + "\"" + Conversion.asciiToHex(student.getStudentName()) + "\" ," +
                        "\"recipient\": " + "\"" + Conversion.asciiToHex(recipient.getStudentID()) + "\" ," +
                        "\"message\": " + "\"" + Conversion.asciiToHex(message) + "\" ," +
                        "\"postDate\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                        "\"postTime\": " + "\"" + Conversion.asciiToHex(time) + "\"}";

                sendMessage.setText("");

                try {
                    pahoMqttClient.publishMessage(mqttAndroidClient, jsonMsg, 1, (name + "/" + recipient.getStudentID()));
                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        generatePrivateID();

//        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
//
//            @Override
//            public void connectComplete(boolean b, String s) {
//
//            }
//
//            @Override
//            public void connectionLost(Throwable throwable) {
//
//            }
//
//            @Override
//            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
//
//                try {
//                    JSONObject jsonObj = new JSONObject(mqttMessage.toString());
//                    if(jsonObj.getString("command").equals("303035303019")){
//                        arrayPrivateChatNew.add(new PrivateChat(Conversion.hexToAscii(jsonObj.getString("studentID")),
//                                Conversion.hexToAscii(jsonObj.getString("studentName")), Conversion.hexToAscii(jsonObj.getString("recipient")),
//                                Conversion.hexToAscii(jsonObj.getString("message")), Conversion.hexToAscii(jsonObj.getString("postDate")),
//                                Conversion.hexToAscii(jsonObj.getString("postTime"))));
//                        newStudentID = Conversion.hexToAscii(jsonObj.getString("studentID"));
//                        newRecipient = Conversion.hexToAscii(jsonObj.getString("recipient"));
//                        newMessage = Conversion.hexToAscii(jsonObj.getString("message"));
//                        newPostDate = Conversion.hexToAscii(jsonObj.getString("postDate"));
//                        newPostTime = Conversion.hexToAscii(jsonObj.getString("postTime"));
//                        Log.d("messageArrived", mqttMessage.toString());
//                        generatePrivateID();
//                    }
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//
//            }
//        });


        return view;
    }


    public void setLayout() {

        adapter = new MyListAdapter();
        list = (ListView) view.findViewById(R.id.dynamicPrivateChatList);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        list.post(new Runnable() {
            @Override
            public void run() {
                list.setSelection(adapter.getCount() - 1);
            }
        });
    }


    public class MyListAdapter extends ArrayAdapter<PrivateChat> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templateprivatechatroom, arrayPrivateChatNew);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templateprivatechatroom, parent, false);
            }
            currentPrivateRoom = arrayPrivateChatNew.get(position);

            TextView studentName = (TextView) itemView.findViewById(R.id.studentName);
            studentName.setText(currentPrivateRoom.getStudentName());

            TextView date = (TextView) itemView.findViewById(R.id.date);
            date.setText(currentPrivateRoom.getPostDate());

            TextView time = (TextView) itemView.findViewById(R.id.time);
            time.setText(currentPrivateRoom.getPostTime());

            TextView message = (TextView) itemView.findViewById(R.id.message);
            message.setText(currentPrivateRoom.getMessage());

            return itemView;

        }

    }


    public void generatePrivateID() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "generatePrivateID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject privateIDResponse = (JSONObject) response.get(i);
                                        currentPrivateID = privateIDResponse.getString("CurrentPrivateID");
                                    }
                                    if (currentPrivateID.equals("0")) {
                                        privateID = "PRI0001";
                                    } else {
                                        privateID = String.format("PRI%04d", (Integer.parseInt(currentPrivateID.substring(3, 7)) + 1));
                                    }
                                    insertPrivateData();
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


    public void insertPrivateData() {

        encodedMessage = "";

        try {
            String[] encodeMessage = {newMessage};
            for (String s : encodeMessage) {
                encodedMessage += URLEncoder.encode(s, "UTF-8");
            }

            jsonURL = Constant.serverFile + "insertPrivateData.php?privateID=" + privateID + "&studentID=" + newStudentID + "&recipient=" +
                    newRecipient + "&message=" + encodedMessage + "&postDate=" + newPostDate + "&postTime=" +
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
                                    String success = jsonObject.getString("success");
                                    if (success.equals("1")) {
                                        Log.d("privateChatRecord", "Added");
                                    } else {
                                        Log.d("privateChatRecord", "Failed");
                                    }
                                    setLayout();
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
                        params.put("privateID", privateID);
                        params.put("studentID", newStudentID);
                        params.put("recipient", newRecipient);
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


}
