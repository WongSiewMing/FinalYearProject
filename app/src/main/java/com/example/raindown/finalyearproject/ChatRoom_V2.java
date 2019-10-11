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
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
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
import Helper.PahoMqttClient;
import Helper.PrivateChat;
import Helper.PrivateChatOB;
import Helper.StoreBasicInfoOB;
import Helper.Student;
import Helper.StudentBasicInfoOB;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class ChatRoom_V2 extends Fragment {
    View view;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command;
    private JSONObject jsonObj;

    private Student myInfo;
    private String opponentID;
    private FloatingActionButton btnSendMessage;
    private EditText editMessage;
    private ImageView opponentPhoto, btnBack;
    private TextView opponentName, opponentStatus, messageStatus;
    private RecyclerView messageListrv;
    private FragmentManager fragmentManager;
    private ProgressBar messageProgressBar;
    private List<StudentBasicInfoOB> opponentBasicInfo = new ArrayList<>();
    private List<PrivateChatOB> arrayPrivateChat = new ArrayList<>();
    private ChatRoom_V2_Adapter adapter;
    private String currentChatID = "", newChatID = "", insertMessageUrl = "";
    private Date c;
    private SimpleDateFormat df;
    private String formattedDate, formattedTime;
    private JSONObject jsonObject;
    private String status = "ACTIVE";


    private static final String TAG = "Chat Room in";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat_room__v2, container, false);
        Bundle bundle = getArguments();
        myInfo = (Student) bundle.getSerializable("UserData");
        opponentID = bundle.getString("ClickedUserID");

        Log.d(TAG, "My Info = " + myInfo.getStudentID());
        Log.d(TAG, "Opponent ID = " + opponentID);


        btnSendMessage = view.findViewById(R.id.btn_send_message);
        editMessage = view.findViewById(R.id.edit_message);
        opponentPhoto = view.findViewById(R.id.Opponent_photo);
        btnBack = view.findViewById(R.id.back);
        opponentName = view.findViewById(R.id.Opponent_name);
        opponentStatus = view.findViewById(R.id.Opponent_status);
        messageListrv = view.findViewById(R.id.message_rv);
        messageProgressBar = view.findViewById(R.id.messageProgressBar);
        messageStatus = view.findViewById(R.id.message_status);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        opponentStatus.setText("Offline");
        opponentStatus.setTextColor(getResources().getColor(R.color.red));
        getOpponentBasicInfo();

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSubmitField()) {
                    c = Calendar.getInstance().getTime();
                    df = new SimpleDateFormat("dd/MM/yyyy");
                    formattedDate = df.format(c);
                    df = new SimpleDateFormat("HH:mm");
                    formattedTime = df.format(c);
                    Log.d(TAG, "Date = " + formattedDate);
                    Log.d(TAG, "Time = " + formattedTime);
                    getChatID();

                    try {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                    } catch (Exception e) {

                    }


                }
            }
        });

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {

            @Override
                public void connectComplete(boolean b, String s) {

                Log.d(TAG, "Connection established.");
                try {
                    String statusCommand = "{\"command\": \"303035303054\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"studentID\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentID()) + "\" ," +
                            "\"recipient\": " + "\"" + Conversion.asciiToHex(opponentID) + "\" ," +
                            "\"status\": " + "\"" + Conversion.asciiToHex(status) + "\" }";

                    pahoMqttClient.publishMessage(mqttAndroidClient, statusCommand, 1, "MY/TARUC/SSS/000000001/PUB");
                }catch (Exception e){

                }

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                try {
                    JSONObject myjsonObj = new JSONObject(mqttMessage.toString());
                    if (myjsonObj.getString("command").equals("303035303019")) {
                        if (Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(myInfo.getStudentID()) && Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(opponentID)) {
                            Log.d(TAG, "This is message that I send");
                            Log.d(TAG, "Array size =" + arrayPrivateChat.size());
                            arrayPrivateChat.add(new PrivateChatOB(Conversion.hexToAscii(myjsonObj.getString("privateID")),Conversion.hexToAscii(myjsonObj.getString("studentID")),
                                    Conversion.hexToAscii(myjsonObj.getString("studentName")), Conversion.hexToAscii(myjsonObj.getString("recipient")),
                                    Conversion.hexToAscii(myjsonObj.getString("message")), Conversion.hexToAscii(myjsonObj.getString("postDate")),
                                    Conversion.hexToAscii(myjsonObj.getString("postTime"))));
                            populateChatRecyclerView();
                        } else if (Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(opponentID) && Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(myInfo.getStudentID())) {
                            arrayPrivateChat.add(new PrivateChatOB(Conversion.hexToAscii(myjsonObj.getString("privateID")),Conversion.hexToAscii(myjsonObj.getString("studentID")),
                                    Conversion.hexToAscii(myjsonObj.getString("studentName")), Conversion.hexToAscii(myjsonObj.getString("recipient")),
                                    Conversion.hexToAscii(myjsonObj.getString("message")), Conversion.hexToAscii(myjsonObj.getString("postDate")),
                                    Conversion.hexToAscii(myjsonObj.getString("postTime"))));
                            populateChatRecyclerView();

                        }

                    } else if (myjsonObj.getString("command").equals("303035303055")) {
                        if (Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(myInfo.getStudentID()) && Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(opponentID)) {
                            Log.d(TAG, "Status received");
                            if (Conversion.hexToAscii(myjsonObj.getString("status")).equals("ACTIVE")) {
                                opponentStatus.setText("Online");
                                opponentStatus.setTextColor(getResources().getColor(R.color.brightgreeen));
                            }
                        }
                    }else if (myjsonObj.getString("command").equals("303035303056")){
                        if (Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(Navigation.student.getStudentID())){
                            for (int i = 0; i < arrayPrivateChat.size(); i++){
                                if (arrayPrivateChat.get(i).getPriChatID().equals(Conversion.hexToAscii(myjsonObj.getString("privateID")))){
                                    Log.d(TAG, "Delete msg");
                                    arrayPrivateChat.remove(i);
                                    populateChatRecyclerView();
                                }
                            }
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        return view;
    }

    private void getChatID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "generatePrivateID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    Log.d(TAG, "Hi commentID responded");
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject chatIDResponse = (JSONObject) response.get(i);
                                        currentChatID = chatIDResponse.getString("CurrentPrivateID");
                                    }
                                    Log.d(TAG, "Hi Current comment ID =" + currentChatID);

                                    if (currentChatID.equals("0")) {
                                        newChatID = "PRI1001";
                                    } else {
                                        String first = currentChatID.substring(0, 3);
                                        String last = currentChatID.substring(3);
                                        int number = Integer.parseInt(last) + 1;
                                        newChatID = first + Integer.toString(number);
                                    }
                                    Log.d(TAG, "New Chat ID =" + newChatID);


                                    insertPrivateData(newChatID);


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
                    "Error create activity:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void insertPrivateData(final String newChatID) {

        try {
            command = "{\"command\": \"303035303019\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"privateID\": " + "\"" + Conversion.asciiToHex(newChatID) + "\" ," +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentID()) + "\" ," +
                    "\"studentName\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentName()) + "\" ," +
                    "\"recipient\": " + "\"" + Conversion.asciiToHex(opponentID) + "\" ," +
                    "\"message\": " + "\"" + Conversion.asciiToHex(editMessage.getText().toString().trim()) + "\" ," +
                    "\"postDate\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                    "\"postTime\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\"}";
            messageStatus.setText("Sending text to server...");

            pahoMqttClient.publishChatMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB", messageStatus);

            String encodedMessage = "";

            jsonObject = new JSONObject(command);
            if (jsonObject.getString("command").equals("303035303019")) {
                String[] encodeMessage = {Conversion.hexToAscii(jsonObject.getString("message"))};
                for (String s : encodeMessage) {
                    encodedMessage += URLEncoder.encode(s, "UTF-8");
                }
            }


            //192.168.0.107/raindown/insertCommentData.php?commentID=cmt1002&activityID=1&studentID=2&commentText=3&commentTime=4&commentDate=5
            insertMessageUrl = Constant.serverFile + "insertPrivateData.php?privateID=" + newChatID + "&studentID=" + myInfo.getStudentID() + "&recipient=" +
                    opponentID + "&message=" + encodedMessage + "&postDate=" + formattedDate + "&postTime=" +
                    formattedTime;

            Log.d(TAG, "Add Comment URL = " + insertMessageUrl);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        insertMessageUrl,
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


                                    editMessage.setText("");

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
                        params.put("privateID", newChatID);
                        params.put("studentID", myInfo.getStudentID());
                        params.put("recipient", opponentID);
                        params.put("message", editMessage.getText().toString().trim());
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

        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded()) {
            }
        }

    }

    private boolean checkSubmitField() {
        String error = "";
        boolean indicator = true;
        if (editMessage.getText().toString().trim().equals("")) {
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


    private void getOpponentBasicInfo() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                //192.168.0.101/raindown/getUserBasicInfo.php?studentID=17WMR05969
                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getUserBasicInfo.php?studentID=" + opponentID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {

                                try {
                                    opponentBasicInfo.clear();
                                    JSONObject infoResponse = (JSONObject) response.get(0);
                                    opponentBasicInfo.add(new StudentBasicInfoOB(infoResponse.getString("studentID"),
                                            infoResponse.getString("photo"),
                                            infoResponse.getString("studentName"),
                                            infoResponse.getString("studentProgramme")));

                                    if (infoResponse.length() == 0) {
                                        Toast.makeText(getActivity().getApplication(), "Error retrieve user data...",
                                                Toast.LENGTH_LONG).show();

                                        getFragmentManager().popBackStack();
                                    } else {
                                        populateUserInfo();
                                        getPreviousPrivateChat();
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

    private void getPreviousPrivateChat() {
        command = "{\"command\": \"303035303037\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentID()) + "\" ," +
                "\"recipient\": " + "\"" + Conversion.asciiToHex(opponentID) + "\"}";

        try {
            RequestQueue queue = Volley.newRequestQueue(getActivity());

            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303037")) {
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getPreviousPrivateChat.php?studentID="
                        + Conversion.hexToAscii(jsonObj.getString("studentID")) + "&recipient=" + Conversion.hexToAscii(jsonObj.getString("recipient")),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d(TAG, "Chat responded!");
                                try {
                                    arrayPrivateChat.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject privateChatResponse = (JSONObject) response.get(i);
                                        arrayPrivateChat.add(new PrivateChatOB(
                                                privateChatResponse.getString("privateID"), privateChatResponse.getString("studentID"), privateChatResponse.getString("studentName"),
                                                privateChatResponse.getString("recipient"), privateChatResponse.getString("message"),
                                                privateChatResponse.getString("postDate"), privateChatResponse.getString("postTime")));
                                    }
                                    if (response.length() == 0) {
                                        messageProgressBar.setVisibility(View.GONE);
                                        messageListrv.setVisibility(View.VISIBLE);
                                    } else {
                                        populateChatRecyclerView();
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void populateChatRecyclerView() {
        adapter = new ChatRoom_V2_Adapter(getActivity(), arrayPrivateChat, opponentBasicInfo.get(0).getPhoto(), mqttAndroidClient);
        adapter.notifyDataSetChanged();
        messageListrv.setLayoutManager(new LinearLayoutManager(getActivity()));
        messageListrv.setAdapter(adapter);
        messageListrv.post(new Runnable() {
            @Override
            public void run() {
                messageListrv.getLayoutManager().scrollToPosition(adapter.getItemCount() - 1);
            }
        });
        messageProgressBar.setVisibility(View.GONE);
        messageListrv.setVisibility(View.VISIBLE);
    }

    private void populateUserInfo() {
        Picasso.with(getActivity()).load(opponentBasicInfo.get(0).getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(opponentPhoto);
        opponentName.setText(opponentBasicInfo.get(0).getStudentName());
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
        // Log.d(TAG,"Welcome back");
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
        Log.d(TAG, "You leaved 1");

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
