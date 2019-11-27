package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.Message;
import Helper.PahoMqttClient;
import Helper.Room;
import Helper.Student;
import Helper.StudentBasicInfoOB;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class PublicChatRoom_V2 extends Fragment {
    private View view;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command, participateCount = "";
    private JSONObject jsonObj;
    private List<Message> arrayPreviousMsg = new ArrayList<>();

    private final static String TAG = "Public Chat Room";

    private Student myInfo;
    private Room myRoom;
    private FloatingActionButton btnSend;
    private EditText editMsg;
    private ImageView back, subjectPhoto;
    private TextView subjectName, messageStatus;
    private ProgressBar msgProgressBar;
    private RecyclerView publicMsgRV;
    private PublicChatRoom_V2_Adapter adapter;
    private Button btnParticipate;
    private Date c;
    private SimpleDateFormat df;
    private String formattedDate, formattedTime, currentParticipateID, participateID, jsonURL = "", userStatus = "", currentMessageID = "", messageID="", jsonMsg = "", insertMsgUrl="";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_public_chat_room__v2, container, false);
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");
        Bundle bundle = getArguments();
        myInfo = (Student) bundle.getSerializable("myInfo");
        myRoom = (Room) bundle.getSerializable("clickedRoom");
        Log.d(TAG, "ROOM name =" + myRoom.getSubject());
        Log.d(TAG, "Student name =" + myInfo.getStudentName());
        btnSend = view.findViewById(R.id.btnSendMessage);
        editMsg = view.findViewById(R.id.editMessage);
        back = view.findViewById(R.id.btnBack);
        subjectPhoto = view.findViewById(R.id.subjectPhoto);
        subjectName = view.findViewById(R.id.subjectName);
        messageStatus = view.findViewById(R.id.message_status);
        msgProgressBar = view.findViewById(R.id.messageProgressBar);
        publicMsgRV = view.findViewById(R.id.publicMessage_rv);
        btnParticipate = view.findViewById(R.id.btnParticipate);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        Picasso.with(getActivity()).load(myRoom.getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(subjectPhoto);
        subjectName.setText(myRoom.getSubject());

        btnParticipate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance().getTime();
                df = new SimpleDateFormat("dd/MM/yyyy");
                formattedDate = df.format(c);
                df = new SimpleDateFormat("HH:mm");
                formattedTime = df.format(c);
                Log.d(TAG, "Date = " + formattedDate);
                Log.d(TAG, "Time = " + formattedTime);
                if (btnParticipate.getText().toString().equals("+ Click to join group")) {
                    btnParticipate.setClickable(false);
                    btnParticipate.setFocusable(false);
                    generateParticipateID();
                    populatePublicChatRV();
                } else {
//                    btnParticipate.setText("+ Click to join group");
//                    btnParticipate.setBackgroundColor(getResources().getColor(R.color.brightgreeen));
//                    btnSend.setFocusable(false);
//                    btnSend.setClickable(false);
//                    publicMsgRV.setVisibility(View.GONE);
                    btnParticipate.setClickable(false);
                    btnParticipate.setFocusable(false);
                    updateParticipant();
                }

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Send btn clicked!");
                if (checkSubmitField()){
                    c = Calendar.getInstance().getTime();
                    df = new SimpleDateFormat("dd/MM/yyyy");
                    formattedDate = df.format(c);
                    df = new SimpleDateFormat("HH:mm");
                    formattedTime = df.format(c);
                    Log.d(TAG, "Date = " + formattedDate);
                    Log.d(TAG, "Time = " + formattedTime);
                    generateMessageID();
                    try {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                    } catch (Exception e) {

                    }
                }
            }
        });

//        getParticipate();

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Log.d(TAG, "Connection establish");
                getParticipate();

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                try {
                    JSONObject myjsonObj = new JSONObject(mqttMessage.toString());
                    if (myjsonObj.getString("command").equals("30303530301E")){
                        if (Conversion.hexToAscii(myjsonObj.getString("roomID")).equals(myRoom.getRoomID())){
                            Log.d(TAG, "Message Arrived!");
                            Log.d(TAG, "Room ID = " + Conversion.hexToAscii(myjsonObj.getString("roomID")));
                            arrayPreviousMsg.add(new Message(Conversion.hexToAscii(myjsonObj.getString("messageID")),
                                    Conversion.hexToAscii(myjsonObj.getString("sender")),
                                    Conversion.hexToAscii(myjsonObj.getString("roomID")),
                                    Conversion.hexToAscii(myjsonObj.getString("message")),
                                    Conversion.hexToAscii(myjsonObj.getString("postDate")),
                                    Conversion.hexToAscii(myjsonObj.getString("postTime")),
                                    new StudentBasicInfoOB(Conversion.hexToAscii(myjsonObj.getString("studentID")),
                                            Conversion.hexToAscii(myjsonObj.getString("photo")),
                                            Conversion.hexToAscii(myjsonObj.getString("studentName")),
                                            Conversion.hexToAscii(myjsonObj.getString("studentProgramme")))));
                            checkParticipateStatus();
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


        return view;
    }

    private void generateMessageID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());

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
                                    insertMessageData(messageID);

                                } catch (Exception e) {

                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

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

    private void insertMessageData(final String messageID) {
        try {
            jsonMsg = "{\"command\": \"30303530301E\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"messageID\": " + "\"" + Conversion.asciiToHex(messageID) + "\" ," +
                    "\"sender\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentID()) + "\" ," +
                    "\"roomID\": " + "\"" + Conversion.asciiToHex(myRoom.getRoomID()) + "\" ," +
                    "\"message\": " + "\"" + Conversion.asciiToHex(editMsg.getText().toString().trim()) + "\" ," +
                    "\"postDate\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                    "\"postTime\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\" ," +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentID()) + "\" ," +
                    "\"photo\": " + "\"" + Conversion.asciiToHex(myInfo.getPhoto()) + "\" ," +
                    "\"studentName\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentName()) + "\" ," +
                    "\"studentProgramme\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentProgramme()) + "\"}";
            editMsg.setText("");
            messageStatus.setText("Sending text to server...");
            pahoMqttClient.publishChatMessage(mqttAndroidClient, jsonMsg, 1, "MY/TARUC/SSS/000000001/PUB", messageStatus);

            String encodedMessage = "";

            jsonObj = new JSONObject(jsonMsg);
            if (jsonObj.getString("command").equals("30303530301E")){
                String[] encodeMessage = {Conversion.hexToAscii(jsonObj.getString("message"))};
                for (String s : encodeMessage){
                    encodedMessage += URLEncoder.encode(s, "UTF-8");
                }
            }
            insertMsgUrl = Constant.serverFile + "insertMessageData.php?messageID=" + messageID + "&sender=" + myInfo.getStudentID() + "&roomID=" +
                    myRoom.getRoomID() + "&message=" + encodedMessage + "&postDate=" + formattedDate + "&postTime=" +
                    formattedTime;

            Log.d(TAG, "Insert Message Url = "+ insertMsgUrl);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        insertMsgUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (isAdded()) {
                                }
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);
//                                    Log.d("setlayout", jsonObject.toString());
//                                    setLayout();

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
                        params.put("sender", myInfo.getStudentID());
                        params.put("roomID", myRoom.getRoomID());
                        params.put("message", editMsg.getText().toString().trim());
                        params.put("postDate", formattedDate);
                        params.put("postTime", formattedTime);

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

        }catch (Exception e){
            e.printStackTrace();
            if (isAdded()){

            }
        }
    }

    private void updateParticipant() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                try {

                    StringRequest postRequest = new StringRequest(
                            Request.Method.POST,
                            Constant.serverFile + "updateParticipant.php?participant=" + myInfo.getStudentID()
                                    + "&roomID=" + myRoom.getRoomID()
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
                                            Toast.makeText(getActivity().getApplication(), "UnSubscribe successfully",
                                                    Toast.LENGTH_LONG).show();
                                            btnParticipate.setText("+ Click to join group");
                                            btnParticipate.setBackgroundColor(getResources().getColor(R.color.brightgreeen));
                                            btnSend.setFocusable(false);
                                            btnSend.setClickable(false);
                                            publicMsgRV.setVisibility(View.GONE);
                                            userStatus = "UnSubScribe";

                                        } else {
                                            Log.d("updateParticipant", "Failed to update");
                                            Toast.makeText(getActivity().getApplication(), "UnSubscribe Failed",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                        btnParticipate.setClickable(true);
                                        btnParticipate.setFocusable(true);
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

                            params.put("participant", myInfo.getStudentID());
                            params.put("roomID", myRoom.getRoomID());
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
            e.printStackTrace();
            if (isAdded()) {
            }
        }


    }

    private void generateParticipateID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());


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


                                    if (!participateID.equals("")) {
                                        Log.d(TAG, "Current Participate ID = " + participateID);
                                        insertParticipateData();
                                    } else {
                                        Log.d(TAG, "Empty");
                                        Toast.makeText(getActivity(), "Error...", Toast.LENGTH_SHORT).show();
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

    private void insertParticipateData() {
        try {
            jsonURL = Constant.serverFile + "insertParticipateData.php?participateID=" + participateID
                    + "&participant=" + myInfo.getStudentID() + "&roomID=" +
                    myRoom.getRoomID() + "&dateJoined=" + formattedDate;

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
                                        btnParticipate.setText("x Click to leave group");
                                        btnParticipate.setBackgroundColor(getResources().getColor(R.color.red));
                                        btnSend.setFocusable(true);
                                        btnSend.setClickable(true);
                                        userStatus = "Subscribe";
                                        Toast.makeText(getActivity().getApplication(), "Subscribe successfully",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Log.d("participateAdded", "Failed");
                                        Toast.makeText(getActivity().getApplication(), "Subscribe failed",
                                                Toast.LENGTH_LONG).show();
                                    }
                                    btnParticipate.setClickable(true);
                                    btnParticipate.setFocusable(true);
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
                        params.put("participant", myInfo.getStudentID());
                        params.put("roomID", myRoom.getRoomID());
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
            ;
            if (isAdded()) {

            }
        }

    }

    private void getParticipate() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                command = "{\"command\": \"30303530301B\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"participant\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentID()) + "\" ," +
                        "\"roomID\": " + "\"" + Conversion.asciiToHex(myRoom.getRoomID()) + "\"}";

//                pahoMqttClient = new PahoMqttClient();
//                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");
                try {

                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getParticipation.php?participant=" + myInfo.getStudentID() + "&roomID=" + myRoom.getRoomID(),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject participatingResponse = (JSONObject) response.get(i);
                                            participateCount = participatingResponse.getString("Participating");
                                        }


                                        command = "{\"command\": \"30303530301C\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                "\"roomID\": " + "\"" + Conversion.asciiToHex(myRoom.getRoomID()) + "\"}";

//                                    pahoMqttClient = new PahoMqttClient();
//                                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                        jsonObj = new JSONObject(command);

                                        getPreviousPublicChat();

                                    } catch (Exception e) {
                                    }

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {

                                }
                            });
                    queue.add(jsonObjectRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error create activity:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void getPreviousPublicChat() {
        try {

            RequestQueue queue = Volley.newRequestQueue(getActivity());

            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getPreviousPublicChatV2.php?roomID=" + myRoom.getRoomID(),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                arrayPreviousMsg.clear();
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject previousChatResponse = (JSONObject) response.get(i);
                                    arrayPreviousMsg.add(new Message(previousChatResponse.getString("messageID"),
                                            previousChatResponse.getString("sender"),
                                            previousChatResponse.getString("roomID"),
                                            previousChatResponse.getString("message"),
                                            previousChatResponse.getString("postDate"),
                                            previousChatResponse.getString("postTime"),
                                            new StudentBasicInfoOB(previousChatResponse.getString("studentID"),
                                                    previousChatResponse.getString("photo"),
                                                    previousChatResponse.getString("studentName"),
                                                    previousChatResponse.getString("studentProgramme"))));

                                }
                                Log.d(TAG, "Response length = " + response.length());
                                if (response.length() == 0) {
                                    checkParticipateStatus();
                                    msgProgressBar.setVisibility(View.GONE);
                                    publicMsgRV.setVisibility(View.VISIBLE);

                                } else {
                                    checkParticipateStatus();
                                    //populatePublicChatRV();
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
            queue.add(jsonObjectRequest);
        } catch (Exception e) {

        }

    }

    private void checkParticipateStatus() {
        Log.d(TAG, "Participate Status = " + participateCount);
        if (Integer.parseInt(participateCount) > 0 || userStatus.equals("Subscribe")) {
            populatePublicChatRV();
        }

        if (Integer.parseInt(participateCount) > 0 || userStatus.equals("Subscribe")) { //joining
            btnParticipate.setText("x Click to leave group");
            btnParticipate.setBackgroundColor(getResources().getColor(R.color.red));
            btnSend.setClickable(true);
            btnSend.setFocusable(true);
            // editMsg.setEnabled(true);

        } else {
            btnParticipate.setText("+ Click to join group"); // not yet join
            btnParticipate.setBackgroundColor(getResources().getColor(R.color.brightgreeen));
            btnSend.setClickable(false);
            btnSend.setFocusable(false);
        }


    }

    private void populatePublicChatRV() {
        adapter = new PublicChatRoom_V2_Adapter(getActivity(), arrayPreviousMsg, mqttAndroidClient);
        adapter.notifyDataSetChanged();
        publicMsgRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        publicMsgRV.setAdapter(adapter);
        publicMsgRV.post(new Runnable() {
            @Override
            public void run() {
                publicMsgRV.getLayoutManager().scrollToPosition(adapter.getItemCount() - 1);
            }
        });
        msgProgressBar.setVisibility(View.GONE);
        publicMsgRV.setVisibility(View.VISIBLE);
    }

    private boolean checkSubmitField(){
        String error = "";
        boolean indicator = true;
        if (editMsg.getText().toString().trim().equals("")) {
            error += "- Message is require.\n";
            indicator = false;
        }
        if (indicator == false) {
            AlertDialog.Builder sent = new AlertDialog.Builder(getActivity());
            sent.setTitle("Invalid input");
            sent.setMessage(error);
            sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            sent.show();
        }
        return indicator;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "You leaved");
        try {
            pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "You leaved");
        try {
            pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
