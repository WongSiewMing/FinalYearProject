package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;


public class ChatRoom_V2 extends Fragment {
    View view;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command;
    private JSONObject jsonObj;
    private Intent cameraIntent, photoPickerIntent, CropIntent;
    public static final int CAMERA_REQUEST = 10;
    public static final int IMAGE_GALLERY_REQUEST = 20;

    private Student myInfo;
    private String opponentID;
    private FloatingActionButton btnSendMessage;
    private ImageView btnSendImage, sendImagePreview;
    private EditText editMessage;
    private ImageView opponentPhoto, btnBack;
    private TextView opponentName, opponentStatus, messageStatus, sendImageCaption;
    private RecyclerView messageListrv;
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
    private int RequestCameraPermissionID = 1001;
    private Bitmap imagePreview;
    private String converted = "";

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

        btnSendMessage = view.findViewById(R.id.btn_send_message);
        btnSendImage = view.findViewById(R.id.btnSendImage);
        sendImagePreview = view.findViewById(R.id.send_image_preview);
        editMessage = view.findViewById(R.id.edit_message);
        opponentPhoto = view.findViewById(R.id.Opponent_photo);
        btnBack = view.findViewById(R.id.back);
        opponentName = view.findViewById(R.id.Opponent_name);
        opponentStatus = view.findViewById(R.id.Opponent_status);
        messageListrv = view.findViewById(R.id.message_rv);
        messageProgressBar = view.findViewById(R.id.messageProgressBar);
        messageStatus = view.findViewById(R.id.message_status);
        sendImageCaption = view.findViewById(R.id.send_image_caption);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatRoomList_V2 chatRoomList_v2 = new ChatRoomList_V2();
                Bundle bundle1 = new Bundle();
                bundle1.putSerializable("chatRoomList_v2", myInfo);
                chatRoomList_v2.setArguments(bundle1);
                getFragmentManager()
                .beginTransaction()
                        .replace(R.id.update_fragmentHolder, chatRoomList_v2)
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });
        opponentStatus.setText("Offline");
        opponentStatus.setTextColor(getResources().getColor(R.color.white));
        getOpponentBasicInfo();
        sendImagePreview.setVisibility(View.INVISIBLE);
        sendImageCaption.setVisibility(View.INVISIBLE);

        btnSendMessage.setEnabled(false);
        editMessage.addTextChangedListener(msgTextWatcher);
        sendImagePreview.setImageResource(R.drawable.ic_camera);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance().getTime();
                df = new SimpleDateFormat("dd/MM/yyyy");
                formattedDate = df.format(c);
                df = new SimpleDateFormat("HH:mm");
                formattedTime = df.format(c);
                getChatID();

                try {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                } catch (Exception e) {

                }


            }
        });

        btnSendImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Take Photo", "Choose From Gallery", "Cancel"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose Your Action");
                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int selection) {

                        if (options[selection] == "Take Photo") {
                            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);
                                Log.d(TAG, "Permission requested");
                                return;
                            } else {
                                Log.d(TAG, "Permission ady granted");
                            }
                            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);

                        } else if (options[selection] == "Choose From Gallery") {
                            photoPickerIntent = new Intent();
                            photoPickerIntent.setType("image/*");
                            photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Image From Gallary"), IMAGE_GALLERY_REQUEST);
                        } else if (options[selection] == "Cancel") {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();

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
                } catch (Exception e) {

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

                        if (Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(myInfo.getStudentID())
                                && Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(opponentID)
                        && !Conversion.hexToAscii(myjsonObj.getString("message")).equals("")) {
                            Log.d(TAG, "This is message that I send");
                            Log.d(TAG, "Array size =" + arrayPrivateChat.size());
                            arrayPrivateChat.add(new PrivateChatOB(
                                    Conversion.hexToAscii(myjsonObj.getString("privateID")),
                                    Conversion.hexToAscii(myjsonObj.getString("studentID")),
                                    Conversion.hexToAscii(myjsonObj.getString("studentName")),
                                    Conversion.hexToAscii(myjsonObj.getString("recipient")),
                                    Conversion.hexToAscii(myjsonObj.getString("message")),
                                    "empty",
                                    Conversion.hexToAscii(myjsonObj.getString("postDate")),
                                    Conversion.hexToAscii(myjsonObj.getString("postTime"))));
                            populateChatRecyclerView();
                        } else if (Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(opponentID)
                                && Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(myInfo.getStudentID())
                                && !Conversion.hexToAscii(myjsonObj.getString("message")).equals("")) {
                            arrayPrivateChat.add(new PrivateChatOB(Conversion.hexToAscii(myjsonObj.getString("privateID")),
                                    Conversion.hexToAscii(myjsonObj.getString("studentID")),
                                    Conversion.hexToAscii(myjsonObj.getString("studentName")),
                                    Conversion.hexToAscii(myjsonObj.getString("recipient")),
                                    Conversion.hexToAscii(myjsonObj.getString("message")),
                                    "empty",
                                    Conversion.hexToAscii(myjsonObj.getString("postDate")),
                                    Conversion.hexToAscii(myjsonObj.getString("postTime"))));
                            populateChatRecyclerView();

                        } else {

                            ChatRoom_V2 fg = new ChatRoom_V2();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("UserData", myInfo);//own data
                            bundle.putString("ClickedUserID", opponentID);
                            fg.setArguments(bundle);

                            getFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.update_fragmentHolder, fg)
                                    .commit();
                        }


                    } else if (myjsonObj.getString("command").equals("303035303055")) {
                        if (Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(myInfo.getStudentID()) && Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(opponentID)) {
                            Log.d(TAG, "Status received");
                            if (Conversion.hexToAscii(myjsonObj.getString("status")).equals("ACTIVE")) {
                                opponentStatus.setText("Online");
                                opponentStatus.setTextColor(getResources().getColor(R.color.brightgreeen));
                            }
                        }
                    } else if (myjsonObj.getString("command").equals("303035303056")) {
                        if (Conversion.hexToAscii(myjsonObj.getString("recipient")).equals(UpdateNavigation.student.getStudentID())) {
                            for (int i = 0; i < arrayPrivateChat.size(); i++) {
                                if (arrayPrivateChat.get(i).getPriChatID().equals(Conversion.hexToAscii(myjsonObj.getString("privateID")))) {
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
                                    Log.d(TAG, "commentID responded");
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject chatIDResponse = (JSONObject) response.get(i);
                                        currentChatID = chatIDResponse.getString("CurrentPrivateID");
                                    }
                                    Log.d(TAG, "Current comment ID =" + currentChatID);

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

            if (sendImagePreview.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_camera).getConstantState()) {
                insertMessageUrl = Constant.serverFile + "insertPrivateData.php?privateID=" + newChatID + "&studentID=" + myInfo.getStudentID() + "&recipient=" +
                        opponentID + "&message=" + encodedMessage + "&image=" + "empty" + "&postDate=" + formattedDate + "&postTime=" +
                        formattedTime;
            } else {
                insertMessageUrl = Constant.serverFile + "insertPrivateData.php?privateID=" + newChatID + "&studentID=" + myInfo.getStudentID() + "&recipient=" +
                        opponentID + "&message=" + "[Image]" + "&image=" + "" + "&postDate=" + formattedDate + "&postTime=" +
                        formattedTime;
            }

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
                                    editMessage.setFocusableInTouchMode(true);
                                    sendImagePreview.setVisibility(view.INVISIBLE);
                                    sendImageCaption.setVisibility(view.INVISIBLE);
                                    sendImagePreview.setImageResource(R.drawable.ic_camera);

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
                        params.put("image", converted);
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

    private void getOpponentBasicInfo() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

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
                                                privateChatResponse.getString("privateID"),
                                                privateChatResponse.getString("studentID"),
                                                privateChatResponse.getString("studentName"),
                                                privateChatResponse.getString("recipient"),
                                                privateChatResponse.getString("message"),
                                                privateChatResponse.getString("image"),
                                                privateChatResponse.getString("postDate"),
                                                privateChatResponse.getString("postTime")));
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        converted = "";
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                imagePreview = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                imagePreview.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                sendImagePreview.setVisibility(View.VISIBLE);
                sendImageCaption.setVisibility(View.VISIBLE);
                sendImagePreview.setImageBitmap(imagePreview);
                editMessage.setText("");
                editMessage.setFocusable(false);
                converted = bitmapToString(imagePreview);
                btnSendMessage.setEnabled(true);

            }

            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Bitmap bm = null;
                if (data != null) {
                    try {
                        bm = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), data.getData());
                        sendImagePreview.setImageBitmap(getResizedBitmap(bm, 500, 500));
                        sendImagePreview.setVisibility(View.VISIBLE);
                        sendImageCaption.setVisibility(View.VISIBLE);
                        converted = bitmapToString(getResizedBitmap(bm, 500, 500));
                        editMessage.setText("");
                        editMessage.setFocusable(false);
                        btnSendMessage.setEnabled(true);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public String bitmapToString(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] arr = baos.toByteArray();
        String result = Base64.encodeToString(arr, Base64.DEFAULT);
        return result;
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    private TextWatcher msgTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            String msg = editMessage.getText().toString().trim();

            btnSendMessage.setEnabled(!msg.isEmpty());
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

}
