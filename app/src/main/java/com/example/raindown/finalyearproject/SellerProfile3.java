package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import Helper.Requests;
import Helper.Student;


public class SellerProfile3 extends Fragment {

    List<Requests> sellerDetailsList = new ArrayList<>();
    FragmentManager fragmentManager;
    Button follow,feedback;
    View view;
    Requests stuff = null;
    Student student = null;
    ProgressDialog pDialog = null;
    ImageView studentPhoto;
    TextView studentName, yearOfStudy, studentProgramme, studentFaculty, followers, following, itemSelling;
    String subscribeID = "", currentSubscribeID = "", jsonURL = "", subscribeDate = "", subscribeCount = "",
            overallRating = "", countFollowers = "", countFollowing = "";
    SimpleDateFormat sdf = null;
    Date d = null;
    RatingBar rating;
    ImageView infoIcon;
    TextView notice;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    JSONObject jsonObj;
    String countFollowersCommand = "", countFollowingCommand = "", ratingCommand = "",
            sellerStuffCommand = "", checkSubscribeCommand = "", command = "";

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
        student = (Student) bundle.getSerializable("Me");
        getActivity().setTitle(student.getStudentName());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.sellerprofile, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        stuff = (Requests) bundle.getSerializable("ViewProfile");

        countFollowersCommand = (String) bundle.getSerializable("Followers");
        countFollowingCommand = (String) bundle.getSerializable("Following");
        ratingCommand = (String) bundle.getSerializable("Rating");
        sellerStuffCommand = (String) bundle.getSerializable("SellerStuff");
        checkSubscribeCommand = (String) bundle.getSerializable("CheckSubscribe");

        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);

        studentPhoto = (ImageView) view.findViewById(R.id.studentPhoto);
        studentName = (TextView) view.findViewById(R.id.studentName);
        studentFaculty = (TextView) view.findViewById(R.id.faculty);
        studentProgramme = (TextView) view.findViewById(R.id.programme);
        yearOfStudy = (TextView) view.findViewById(R.id.year);
        followers = (TextView) view.findViewById(R.id.followers);
        following = (TextView) view.findViewById(R.id.following);
        follow = (Button) view.findViewById(R.id.follow);
        feedback = (Button) view.findViewById(R.id.feedback);
        rating = (RatingBar) view.findViewById(R.id.ratingBar);
        itemSelling = (TextView) view.findViewById(R.id.itemselling);

        Picasso.with(getActivity()).load(stuff.getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(studentPhoto);
        studentName.setText(stuff.getStudentID().getStudentName());
        studentProgramme.setText(stuff.getStudentID().getStudentProgramme());
        studentFaculty.setText(stuff.getStudentID().getStudentFaculty());
        yearOfStudy.setText("Year " + String.valueOf(stuff.getStudentID().getYearOfStudy()));
        registerClickCallBack();

        countFollowers();

        follow.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (follow.getText().equals("Follow")) {
                    follow.setText("Followed");
                    setSubscription("Subscribe");

                } else {
                    follow.setText("Follow");
                    setSubscription("Unsubscribe");
                }
            }
        });

        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GiveFeedback frag = new GiveFeedback();
                Bundle bundles = new Bundle();
                bundles.putSerializable("GiveFeedback", stuff.getStudentID());
                bundles.putSerializable("FeedbackMe", student);
                frag.setArguments(bundles);
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl);

        return view;
    }

    public void registerClickCallBack() {

        ListView list = (ListView) view.findViewById(R.id.sellerStuffList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {

                Requests clickedStuff = sellerDetailsList.get(position);
                RequestStuffDetails frag = new RequestStuffDetails();
                Bundle bundles = new Bundle();
                bundles.putSerializable("ClickedStuff", clickedStuff);
                bundles.putSerializable("StudentClickedStuff",student);
                frag.setArguments(bundles);
                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });
    }


    public void countFollowers(){

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(countFollowersCommand);

                if(jsonObj.getString("command").equals("303035303038")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "countFollowers.php?subscribeeID="
                            + Conversion.hexToAscii(jsonObj.getString("subscribeeID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject countFollowersResponse = (JSONObject) response.get(i);
                                            countFollowers = countFollowersResponse.getString("myfollowers");
                                        }
                                        followers.setText(countFollowers + " followers");
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();
                                        jsonObj = new JSONObject(countFollowingCommand);
                                        if(jsonObj.getString("command").equals("303035303039")){
                                            countFollowing(getActivity(), Constant.serverFile + "countFollowing.php?subscriberID=" + Conversion.hexToAscii(jsonObj.getString("subscriberID")));
                                        }
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


    public void countFollowing(Context context, String url){

        RequestQueue queue = Volley.newRequestQueue(context);
        if (!pDialog.isShowing())
            pDialog.setMessage("Sync with server...");
        pDialog.show();
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject countFollowingResponse = (JSONObject) response.get(i);
                                countFollowing = countFollowingResponse.getString("myfollowing");
                            }
                            following.setText(countFollowing + " following");

                            RequestQueue queue = Volley.newRequestQueue(getActivity());
                            if (!pDialog.isShowing())
                                pDialog.setMessage("Sync with server...");
                            pDialog.show();

                            jsonObj = new JSONObject(ratingCommand);
                            if(jsonObj.getString("command").equals("30303530300A")){
                                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getOverallRating.php?studentID="
                                        + Conversion.hexToAscii(jsonObj.getString("studentID")),
                                        new Response.Listener<JSONArray>() {
                                            @Override
                                            public void onResponse(JSONArray response) {
                                                try {

                                                    for (int i = 0; i < response.length(); i++) {
                                                        JSONObject overallRatingResponse = (JSONObject) response.get(i);
                                                        overallRating = overallRatingResponse.getString("OverallRating");
                                                    }
                                                    rating.setRating(Float.valueOf(overallRating));
                                                    getSellerDetails();
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


    public void getSellerDetails(){

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(sellerStuffCommand);
                if(jsonObj.getString("command").equals("30303530300B")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSellerDetails2.php?studentID="
                            + Conversion.hexToAscii(jsonObj.getString("studentID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    if(response.toString().equals("[]")){
                                        infoIcon.setVisibility(view.VISIBLE);
                                        notice.setVisibility(view.VISIBLE);
                                    }
                                    try {
                                        sellerDetailsList.clear();
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject sellerResponse = (JSONObject) response.get(i);
                                            sellerDetailsList.add(new Requests(sellerResponse.getString("requeststuffID"), new Student(sellerResponse.getString("studentID"),
                                                    sellerResponse.getString("clientID"), sellerResponse.getString("photo"), sellerResponse.getString("studentName"),
                                                    sellerResponse.getString("icNo"), sellerResponse.getString("studentProgramme"), sellerResponse.getString("studentFaculty"),
                                                    sellerResponse.getInt("yearOfStudy")), sellerResponse.getString("stuffName"), sellerResponse.getString("stuffImage"),
                                                    sellerResponse.getString("stuffDescription"), sellerResponse.getString("stuffCategory"), sellerResponse.getString("stuffCondition"),
                                                    sellerResponse.getDouble("stuffPrice"), sellerResponse.getInt("stuffQuantity"), sellerResponse.getString("validStartDate"),
                                                    sellerResponse.getString("validEndDate"), sellerResponse.getString("stuffStatus")) {
                                                //@Override
                                                protected Response parseNetworkResponse(NetworkResponse networkResponse) {
                                                    return null;
                                                }

                                                //@Override
                                                protected void deliverResponse(Object o) {

                                                }

                                                //@Override
                                                public int compareTo(@NonNull Object o) {
                                                    return 0;
                                                }
                                            });

                                        }
                                        itemSelling.setText("Stuffs Request : " + String.valueOf(sellerDetailsList.size()) + " items");
                                        populateListView();
                                        checkSubscribe();
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


    public void populateListView() {
        ArrayAdapter<Requests> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.sellerStuffList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<Requests> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatesellerstuff, sellerDetailsList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatesellerstuff, parent, false);
            }
            Requests currentSellerStuffs = sellerDetailsList.get(position);

            ImageView stuffImage = (ImageView) itemView.findViewById(R.id.stuffImage);
            Picasso.with(getActivity()).load(currentSellerStuffs.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffImage);

            TextView stuffName = (TextView) itemView.findViewById(R.id.stuffName);
            stuffName.setText(currentSellerStuffs.getStuffName());

            TextView stuffCategory = (TextView) itemView.findViewById(R.id.stuffCategory);
            stuffCategory.setText(currentSellerStuffs.getStuffCategory());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.stuffPrice);
            stuffPrice.setText(String.format("RM %.2f",currentSellerStuffs.getStuffPrice()));

            return itemView;

        }
    }


    public void checkSubscribe(){

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(checkSubscribeCommand);
                if(jsonObj.getString("command").equals("30303530300C")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "checkSubscribe.php?subscriberID="
                            + Conversion.hexToAscii(jsonObj.getString("subscriberID"))
                            + "&subscribeeID=" + Conversion.hexToAscii(jsonObj.getString("subscribeeID")),
                            new Response.Listener<JSONArray>() {
                                @Override
                                public void onResponse(JSONArray response) {
                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject subscribeCountResponse = (JSONObject) response.get(i);
                                            subscribeCount = subscribeCountResponse.getString("SubscribeCount");
                                        }
                                        if (subscribeCount.equals("1")) {
                                            follow.setText("Followed");
                                        }
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


    public void setSubscription(String status){

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                if(status.equals("Subscribe")){
                    command = "{\"command\": \"30303530300D\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"subscribeeID\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\" ," +
                            "\"subscriberID\": " + "\"" + Conversion.asciiToHex(student.getStudentID()) + "\"}";

                    pahoMqttClient.publishMessage(mqttAndroidClient,command,1,"MY/TARUC/SSS/000000001/PUB");
                    getSubscribeID(getActivity(), Constant.serverFile + "getSubscribeID.php");

                }else{
                    command = "{\"command\": \"30303530300E\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"subscribeeID\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\" ," +
                            "\"subscriberID\": " + "\"" + Conversion.asciiToHex(student.getStudentID()) + "\"}";

                    pahoMqttClient.publishMessage(mqttAndroidClient,command,1,"MY/TARUC/SSS/000000001/PUB");
                    removeSubscribe();

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


    public void getSubscribeID(Context context, String url){

        RequestQueue queue = Volley.newRequestQueue(context);
        if (!pDialog.isShowing())
            pDialog.setMessage("Sync with server...");
        pDialog.show();

        try {
            jsonObj = new JSONObject(command);
            if(jsonObj.getString("command").equals("30303530300D")){
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject subscribeIDResponse = (JSONObject) response.get(i);
                                        currentSubscribeID = subscribeIDResponse.getString("CurrentSubscribeID");
                                    }
                                    if (currentSubscribeID.equals("0")) {
                                        subscribeID = "SUB0001";
                                    } else {
                                        subscribeID = String.format("SUB%04d", (Integer.parseInt(currentSubscribeID.substring(3, 7)) + 1));
                                    }

                                    try {
                                        d = Calendar.getInstance().getTime();
                                        sdf = new SimpleDateFormat("dd/MM/yyyy");
                                        subscribeDate = sdf.format(d);

                                        jsonURL = Constant.serverFile + "insertSubscribeData.php?subscribeID=" + subscribeID + "&subscribeeID=" + Conversion.hexToAscii(jsonObj.getString("subscribeeID"))
                                                + "&subscriberID=" + Conversion.hexToAscii(jsonObj.getString("subscriberID")) + "&subscribeDate=" + subscribeDate + "&unsubscribeDate=" + ""
                                                + "&subscribeStatus=" + "Active";

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

                                                                    Toast.makeText(getActivity().getApplicationContext(), "Added to follow list.", Toast.LENGTH_LONG).show();
                                                                    countFollowers2(getActivity(), Constant.serverFile + "countFollowers.php?subscribeeID=" + stuff.getStudentID().getStudentID());
                                                                    //pahoMqttClient.subscribe(mqttAndroidClient,requests.getStudentID().getStudentID(),1);
                                                                    Intent intent2 = new Intent(getActivity(), NotificationService.class);
                                                                    getActivity().startService(intent2);
                                                                } else {
                                                                    Toast.makeText(getActivity().getApplicationContext(), "Error.", Toast.LENGTH_LONG).show();
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
                                                    try {
                                                        params.put("subscribeID", subscribeID);
                                                        params.put("subscribeeID", Conversion.hexToAscii(jsonObj.getString("subscribeeID")));
                                                        params.put("subscriberID", Conversion.hexToAscii(jsonObj.getString("subscriberID")));
                                                        params.put("subscribeDate", subscribeDate);
                                                        params.put("unsubscribeDate", "");
                                                        params.put("subscribeStatus", "Active");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
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
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public void countFollowers2(Context context, String url){

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject countFollowersResponse = (JSONObject) response.get(i);
                                countFollowers = countFollowersResponse.getString("myfollowers");
                            }
                            followers.setText(countFollowers + " followers");
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


    public void removeSubscribe(){

        try {
            jsonObj = new JSONObject(command);
            if(jsonObj.getString("command").equals("30303530300E")){
                String url = Constant.serverFile + "removeSubscribe.php?subscribeeID=" + Conversion.hexToAscii(jsonObj.getString("subscribeeID"))
                        + "&subscriberID=" + Conversion.hexToAscii(jsonObj.getString("subscriberID"));
                RequestQueue queue = Volley.newRequestQueue(getActivity());
                try {
                    StringRequest postRequest = new StringRequest(
                            Request.Method.POST,
                            url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if (isAdded()) {
                                    }
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(response);
                                        String success = jsonObject.getString("updated");
                                        if (success.equals("1")) {
                                            Toast.makeText(getActivity().getApplicationContext(), "Removed from follow list.", Toast.LENGTH_LONG).show();
                                            countFollowers2(getActivity(), Constant.serverFile + "countFollowers.php?subscribeeID=" + stuff.getStudentID().getStudentID());

                                            if(NotificationService.client != null){
                                                NotificationService.client.unsubscribe(stuff.getStudentID().getStudentID());
                                            }
                                            //pahoMqttClient.unSubscribe(mqttAndroidClient,requests.getStudentID().getStudentID());
                                        } else {
                                            Toast.makeText(getActivity().getApplicationContext(), "Error.", Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (MqttException e) {
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
                            try {
                                params.put("subscribeeID", Conversion.hexToAscii(jsonObj.getString("subscribeeID")));
                                params.put("subscriberID", Conversion.hexToAscii(jsonObj.getString("subscriberID")));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
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
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded()) {
            }

        }
    }

}
