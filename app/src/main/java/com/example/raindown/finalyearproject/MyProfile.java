package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Requests;
import Helper.Student;
import Helper.Stuff;

/*Author : Adelina Tang Chooi Li
Programme : RSD3
Year : 2017*/

public class MyProfile extends Fragment {

    public final static List<Stuff> arrayMyStuff = new ArrayList<>();
    public final static List<Requests> arrayMyRequest = new ArrayList<>();
    String countFollowers = "", countFollowing = "";
    TextView studentName, studentProgramme, yearOfStudy, studentFaculty, followers, following, itemSelling,request;
    FragmentManager fragmentManager;
    ImageView feedback, photo;
    View view;
    ProgressDialog pDialog = null;
    Student s = null;
    ImageView infoIcon;
    TextView notice;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    JSONObject jsonObj;
    String command = "", overallRatingCommand = "", ratingListCommand = "", requestCommand ="";
    int ss = 0;

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
        getActivity().setTitle("My Profile");
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

        view =  inflater.inflate(R.layout.myprofile, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("MyProfile");
        pDialog = new ProgressDialog(getActivity());
        infoIcon = (ImageView) view.findViewById(R.id.infoIcon);
        notice = (TextView) view.findViewById(R.id.notice);

        photo = (ImageView) view.findViewById(R.id.studentPhoto);
        studentName = (TextView) view.findViewById(R.id.studentName);
        studentFaculty = (TextView) view.findViewById(R.id.faculty);
        studentProgramme = (TextView) view.findViewById(R.id.programme);
        yearOfStudy = (TextView) view.findViewById(R.id.year);
        followers = (TextView) view.findViewById(R.id.followers);
        following = (TextView) view.findViewById(R.id.following);
        itemSelling = (TextView) view.findViewById(R.id.itemselling);
        request = (TextView) view.findViewById(R.id.itemrequest);
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ss = 1;
//                command = "{\"command\": \"303035303042\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
//                        "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";
//                try {
//                pahoMqttClient = new PahoMqttClient();
//                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, requestCommand, "MY/TARUC/SSS/000000001/PUB");
//                jsonObj = new JSONObject(requestCommand);
//
//                    if (jsonObj.getString("requestCommand").equals("303035303042")) {
//                        getRequestStuffs(getActivity(), Constant.serverFile + "getMyRequestStuffList.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")));
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                countFollowers();
            }

        });
        itemSelling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ss = 0;
                countFollowers();
            }

        });
        Picasso.with(getActivity()).load(s.getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(photo);
        studentName.setText(s.getStudentName());
        studentFaculty.setText(s.getStudentFaculty());
        studentProgramme.setText(s.getStudentProgramme());
        yearOfStudy.setText("Year " + s.getYearOfStudy());

        registerClickCallBack();

        feedback = (ImageView) view.findViewById(R.id.feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRatingList();
            }
        });

        countFollowers();

        return view;
    }

    private void getRequestStuffs(Context context, String url) {


        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.toString().equals("[]")){
                            infoIcon.setVisibility(view.VISIBLE);
                            notice.setVisibility(view.VISIBLE);
                        }
                        try {
                            arrayMyRequest.clear();
                            arrayMyStuff.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject myStuffResponse = (JSONObject) response.get(i);
                                arrayMyRequest.add(new Requests(myStuffResponse.getString("requeststuffID"), new Student(myStuffResponse.getString("studentID"),
                                        myStuffResponse.getString("clientID"),myStuffResponse.getString("photo"),myStuffResponse.getString("studentName"),
                                        myStuffResponse.getString("icNo"),myStuffResponse.getString("studentProgramme"),myStuffResponse.getString("studentFaculty"),
                                        myStuffResponse.getInt("yearOfStudy")), myStuffResponse.getString("stuffName"), myStuffResponse.getString("stuffImage"),
                                        myStuffResponse.getString("stuffDescription"), myStuffResponse.getString("stuffCategory"), myStuffResponse.getString("stuffCondition"),
                                        myStuffResponse.getDouble("stuffPrice"), myStuffResponse.getInt("stuffQuantity"), myStuffResponse.getString("validStartDate"),
                                        myStuffResponse.getString("validEndDate"), myStuffResponse.getString("stuffStatus")));
                            }
                            request.setText("Stuffs Request : " + String.valueOf(arrayMyRequest.size()) + " items");
                            requestListView();
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

    public void countFollowers(){

        try {
            command = "{\"command\": \"303035303038\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"subscribeeID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

            pahoMqttClient = new PahoMqttClient();
            mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                jsonObj = new JSONObject(command);
                if(jsonObj.getString("command").equals("303035303038")){
                    JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "countFollowers.php?subscribeeID=" + Conversion.hexToAscii(jsonObj.getString("subscribeeID")),
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
                                        command = "{\"command\": \"303035303039\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                "\"subscriberID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

                                        pahoMqttClient = new PahoMqttClient();
                                        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                        jsonObj = new JSONObject(command);
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

                            if (pDialog.isShowing())
                                pDialog.dismiss();
                            if(ss > 0){
                                command = "{\"command\": \"303035303042\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                        "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";
                            }else{
                                command = "{\"command\": \"30303530300B\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                        "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

                            }
                            jsonObj = new JSONObject(command);

                            if(jsonObj.getString("command").equals("30303530300B")){
                                pahoMqttClient = new PahoMqttClient();
                                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");
                                getRequestStuffs(getActivity(), Constant.serverFile + "getMyRequestStuffList.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")));
                                getMyStuffs(getActivity(), Constant.serverFile + "getMyStuffs.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")));

                            }else if (jsonObj.getString("command").equals("303035303042")){

                                    pahoMqttClient = new PahoMqttClient();
                                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");
                                    getRequestStuffs(getActivity(), Constant.serverFile + "getMyRequestStuffList.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID")));
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


    public void getMyStuffs(Context context, String url) {

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        if(response.toString().equals("[]")){
                            infoIcon.setVisibility(view.VISIBLE);
                            notice.setVisibility(view.VISIBLE);
                        }
                        try {
                            arrayMyStuff.clear();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject myStuffResponse = (JSONObject) response.get(i);
                                arrayMyStuff.add(new Stuff(myStuffResponse.getString("stuffID"), new Student(myStuffResponse.getString("studentID"),
                                        myStuffResponse.getString("clientID"),myStuffResponse.getString("photo"),myStuffResponse.getString("studentName"),
                                        myStuffResponse.getString("icNo"),myStuffResponse.getString("studentProgramme"),myStuffResponse.getString("studentFaculty"),
                                        myStuffResponse.getInt("yearOfStudy")), myStuffResponse.getString("stuffName"), myStuffResponse.getString("stuffImage"),
                                        myStuffResponse.getString("stuffDescription"), myStuffResponse.getString("stuffCategory"), myStuffResponse.getString("stuffCondition"),
                                        myStuffResponse.getDouble("stuffPrice"), myStuffResponse.getInt("stuffQuantity"), myStuffResponse.getString("validStartDate"),
                                        myStuffResponse.getString("validEndDate"), myStuffResponse.getString("stuffStatus")));
                            }
                            itemSelling.setText("Stuffs Selling : " + String.valueOf(arrayMyStuff.size()) + " items");
                            populateListView();
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


    public void populateListView() {

        ArrayAdapter<Stuff> adapter = new MyListAdapter();
        ListView list = (ListView) view.findViewById(R.id.myStuffList);
        list.setAdapter(adapter);
    }


    public class MyListAdapter extends ArrayAdapter<Stuff> {

        public MyListAdapter() {
            super(getActivity(), R.layout.templatesellerstuff, arrayMyStuff);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatesellerstuff, parent, false);
            }
            Stuff currentStuff = arrayMyStuff.get(position);

            ImageView stuffImage = (ImageView) itemView.findViewById(R.id.stuffImage);
            Picasso.with(getActivity()).load(currentStuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffImage);

            TextView stuffName = (TextView) itemView.findViewById(R.id.stuffName);
            stuffName.setText(currentStuff.getStuffName());

            TextView stuffCategory = (TextView) itemView.findViewById(R.id.stuffCategory);
            stuffCategory.setText(currentStuff.getStuffCategory());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.stuffPrice);
            stuffPrice.setText(String.format("RM %.2f",currentStuff.getStuffPrice()));

            return itemView;

        }
    }


    public void registerClickCallBack() {

        ListView list = (ListView) view.findViewById(R.id.myStuffList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {


                if(ss>0){
                    Requests clickedStuff = arrayMyRequest.get(position);
                    MaintainRequestStuff frag = new MaintainRequestStuff();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("MaintainStuff", clickedStuff);
                    frag.setArguments(bundle);

                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }else{
                    Stuff clickedStuff = arrayMyStuff.get(position);
                    MaintainStuff frag = new MaintainStuff();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("MaintainStuff", clickedStuff);
                    frag.setArguments(bundle);

                    fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }

            }
        });
    }

    public void requestListView() {

        ArrayAdapter<Requests> adapter = new ListAdapter();
        ListView list = (ListView) view.findViewById(R.id.myStuffList);
        list.setAdapter(adapter);
    }


    public class ListAdapter extends ArrayAdapter<Requests> {

        public ListAdapter() {
            super(getActivity(), R.layout.templatesellerstuff, arrayMyRequest);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.templatesellerstuff, parent, false);
            }
            Requests currentStuff = arrayMyRequest.get(position);

            ImageView stuffImage = (ImageView) itemView.findViewById(R.id.stuffImage);
            Picasso.with(getActivity()).load(currentStuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(stuffImage);

            TextView stuffName = (TextView) itemView.findViewById(R.id.stuffName);
            stuffName.setText(currentStuff.getStuffName());

            TextView stuffCategory = (TextView) itemView.findViewById(R.id.stuffCategory);
            stuffCategory.setText(currentStuff.getStuffCategory());

            TextView stuffPrice = (TextView) itemView.findViewById(R.id.stuffPrice);
            stuffPrice.setText(String.format("RM %.2f",currentStuff.getStuffPrice()));

            return itemView;

        }
    }


    public void getRatingList(){

        ratingListCommand = "{\"command\": \"303035303014\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\"}";

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, ratingListCommand, "MY/TARUC/SSS/000000001/PUB");

        ViewFeedback frag = new ViewFeedback();
        Bundle bundle1 = new Bundle();
        bundle1.putSerializable("MyProfileFeedback", s);
        bundle1.putSerializable("OverallRating", overallRatingCommand);
        bundle1.putSerializable("RatingList", ratingListCommand);
        frag.setArguments(bundle1);
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.update_fragmentHolder, frag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

    }


}
