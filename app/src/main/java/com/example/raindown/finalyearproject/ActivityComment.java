package com.example.raindown.finalyearproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

import org.eclipse.paho.android.service.MqttAndroidClient;
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

import Helper.ActivityCommentOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.StudentBasicInfoOB;

import static android.content.Context.INPUT_METHOD_SERVICE;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

public class ActivityComment extends Fragment {

    View view;
    private String name, activityID, activityCaption, currentCommentID, newCommentID, addCommentUrl, formattedDate, formattedTime, UserID, UserName, UserPhoto, UserProgramme;
    private FloatingActionButton postCommentBtn;
    private EditText commentMessage;
    private TextView posterName, posterContent, noCommentIndicator;
    private ProgressBar progressBar;
    private Date c;
    private SimpleDateFormat df;
    private List<ActivityCommentOB> commmentList;
    private ActivityCommentAdapter adapter;
    private RecyclerView commentListrv;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command;
    private JSONObject josnObj;

    private static final String TAG = "User Comment";
    private ProgressDialog pDialog = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Comment Section");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_activity_comment, container, false);
        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        UserName = UserSharedPreferences.read(UserSharedPreferences.userName, null);
        UserPhoto = UserSharedPreferences.read(UserSharedPreferences.userPhoto, null);
        UserProgramme = UserSharedPreferences.read(UserSharedPreferences.userProgramme, null);
        commmentList = new ArrayList<>();
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();

        activityID = (String) bundle.getSerializable("activityID");
        name = (String) bundle.getSerializable("posterName");
        activityCaption = (String) bundle.getSerializable("activityCaption");

        Log.d(TAG, "Activity ID = " + activityID);
        Log.d(TAG, "Activity name = " + name);
        Log.d(TAG, "Activity caption = " + activityCaption);

        postCommentBtn = view.findViewById(R.id.postCommentButton);
        commentMessage = view.findViewById(R.id.CommentMessage);
        posterName = view.findViewById(R.id.posterName);
        posterContent = view.findViewById(R.id.posterContent);
        progressBar = view.findViewById(R.id.commentProgressbar);
        noCommentIndicator = view.findViewById(R.id.noCommentIndicator);

        posterName.setText(name);
        posterContent.setText(activityCaption);

        populateCommentList(activityID);

        postCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSubmitField() == true) {
                    c = Calendar.getInstance().getTime();
                    df = new SimpleDateFormat("dd/MM/yyyy");
                    formattedDate = df.format(c);
                    Log.d(TAG, "Formatted Date = " + formattedDate);
                    df = new SimpleDateFormat("HHmm");
                    formattedTime = df.format(c);
                    Log.d(TAG, "Formatted Time = " + formattedTime);
                    getCommentID();
                }
            }
        });

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);

        return view;
    }

    private void getCommentID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();
                //192.168.0.107/raindown/getCommentID.php

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getCommentID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    Log.d(TAG, "commentID responded");
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject commentIDResponse = (JSONObject) response.get(i);
                                        currentCommentID = commentIDResponse.getString("CurrentCommentID");
                                    }
                                    Log.d(TAG, " Current comment ID =" + currentCommentID);

                                    if (currentCommentID.equals("0")) {
                                        newCommentID = "cmt1001";
                                    } else {
                                        String first = currentCommentID.substring(0, 3);
                                        String last = currentCommentID.substring(3);
                                        int number = Integer.parseInt(last) + 1;
                                        newCommentID = first + Integer.toString(number);
                                    }

                                    Log.d(TAG, "New Comment ID = " + newCommentID);

                                    insertCommentText(newCommentID);


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
                    "Error create activity:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void insertCommentText(final String newID) {

        try {
            command = "{\"command\": \"30303530304E\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"commentID\": " + "\"" + Conversion.asciiToHex(newID) + "\" ," +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" ," +
                    "\"commentText\": " + "\"" + Conversion.asciiToHex(commentMessage.getText().toString().trim()) + "\" ," +
                    "\"commentTime\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\" ," +
                    "\"commentDate\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                    "\"activityID\": " + "\"" + Conversion.asciiToHex(activityID) + "\" }";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            String encodedCommentText = "";

            josnObj = new JSONObject(command);
            if (josnObj.getString("command").equals("30303530304E")){
                String[] encodeCommentText = {Conversion.hexToAscii(josnObj.getString("commentText"))};
                for (String s : encodeCommentText){
                    encodedCommentText += URLEncoder.encode(s, "UTF-8");
                }
            }


            //192.168.0.107/raindown/insertCommentData.php?commentID=cmt1002&activityID=1&studentID=2&commentText=3&commentTime=4&commentDate=5
            addCommentUrl = Constant.serverFile + "insertCommentData.php?commentID=" + newID
                    + "&activityID=" + activityID
                    + "&studentID=" + UserID
                    + "&commentText=" + encodedCommentText
                    + "&commentTime=" + formattedTime
                    + "&commentDate=" + formattedDate;

            Log.d(TAG, "Add Comment URL = " + addCommentUrl);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        addCommentUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (isAdded()) {
                                }
                                JSONObject jsonObject = null;
                                Log.d(TAG, "Activity Respond is here");
                                try {
                                    jsonObject = new JSONObject(response);
                                    String success = jsonObject.getString("success");
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                    if (success.equals("1")) {


                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        commentMessage.getText().clear();
                                                        populateCommentList(activityID);
                                                        break;
                                                }
                                            }
                                        };

                                        builder.setTitle("Comment is successfully posted")
                                                .setMessage("Your Comment is uploaded")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Failed to post Comment");
                                        builder.setMessage("Your Comment is failed to be posted");
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                commentMessage.getText().clear();
                                            }
                                        });
                                        builder.show();
                                    }
                                    try {
                                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                                    } catch (Exception e) {

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
                        params.put("commentID", newID);
                        params.put("activityID", activityID);
                        params.put("studentID", UserID);
                        params.put("commentText", commentMessage.getText().toString().trim());
                        params.put("commentTime", formattedTime);
                        params.put("commentDate", formattedDate);
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

    private void populateCommentList(String activityID) {
        try {
            Log.d(TAG,"HI thian xin");
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                //http://192.168.0.107/raindown/getActivityCommentList.php?activityID=act1003
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getActivityCommentList.php?activityID=" + activityID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {

                                    commmentList.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject commendResponse = (JSONObject) response.get(i);
                                        commmentList.add(new ActivityCommentOB(commendResponse.getString("commentID"),
                                                commendResponse.getString("activityID"),
                                                new StudentBasicInfoOB(commendResponse.getString("studentID"),
                                                        commendResponse.getString("photo"),
                                                        commendResponse.getString("studentName"),
                                                        commendResponse.getString("studentProgramme")),
                                                commendResponse.getString("commentText"),
                                                commendResponse.getString("commentTime"),
                                                commendResponse.getString("commentDate")));
                                    }
                                    Log.d(TAG, "Respond length = " + response.length());
                                    Log.d(TAG, "List length = " + commmentList.size());

                                    if (response.length() == 0) {
                                        progressBar.setVisibility(View.GONE);
                                        noCommentIndicator.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "Respond length =" + response.length());
                                    } else {
                                       // Log.d(TAG, "Student Name =" + commmentList.get(0).getStudentID().getStudentName());
                                        progressBar.setVisibility(View.GONE);
                                        noCommentIndicator.setVisibility(View.GONE);
                                        populateRecycleView();
                                    }


                                } catch (Exception e) {
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.d(TAG,"HI thian xin4");
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

    private void populateRecycleView() {
        commentListrv = view.findViewById(R.id.commentrv);
        adapter = new ActivityCommentAdapter(getActivity(), commmentList);
        commentListrv.setLayoutManager(new LinearLayoutManager(getActivity()));
        commentListrv.setAdapter(adapter);
        commentListrv.setVisibility(View.VISIBLE);

    }

    private boolean checkSubmitField() {
        String error = "";
        boolean indicator = true;
        if (commentMessage.getText().toString().trim().equals("")) {
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
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "You leaved");
        try {
//            pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
