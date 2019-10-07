package com.example.raindown.finalyearproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Helper.ActivityLikeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.StoreBasicInfoOB;
import Helper.Student;
import Helper.StudentBasicInfoOB;
import Helper.UserActivityOB;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/


public class UserActivity extends Fragment {
    private Button createActivity;

    View view;
    FragmentManager fragmentManager;
    private ProgressBar progressBar;
    private RecyclerView activityListrv;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command, UserID;
    private List<UserActivityOB> activityList;
    private Student s;


    private static final String TAG = "User Activity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("User Activity");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_activity, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("UserActivity");
        activityList = new ArrayList<>();
        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        Log.d(TAG, "User ID =" + UserID);
        createActivity = view.findViewById(R.id.create_activity);
        progressBar = view.findViewById(R.id.activityProgressBar);
        activityListrv = view.findViewById(R.id.activityListrv);

        createActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CreateActivity createActivity = new CreateActivity();
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, createActivity)
                        .addToBackStack(null)
                        .commit();
            }
        });

        populateActivityList();

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);

        return view;
    }

    private void populateActivityList() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {


                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getUserActivity.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    activityList.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject activityResponse = (JSONObject) response.get(i);
                                        activityList.add(new UserActivityOB(activityResponse.getString("activityID"), new StudentBasicInfoOB(activityResponse.getString("studentID"),
                                                activityResponse.getString("photo"), activityResponse.getString("studentName"), activityResponse.getString("studentProgramme")),
                                                activityResponse.getString("stuffID"), activityResponse.getString("activityCaption"), activityResponse.getString("activityThumbnail"),
                                                activityResponse.getString("uploadDate"), activityResponse.getString("uploadTime"), activityResponse.getString("activityStatus")));

                                    }

                                    Log.d(TAG, "Respond length = " + response.length());
                                    command = "{\"command\": \"303035303069\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                            "\"StudentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" }";

                                    pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                                    if (response.length() > 0) {
                                        populateRecycleView();
                                    }else{
                                        Toast.makeText(getActivity().getApplication(), "Seem like no one posted anything...", Toast.LENGTH_LONG).show();
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



    private void populateRecycleView() {
        ActivityRecyclerViewAdapter activityListAdapter = new ActivityRecyclerViewAdapter(getActivity(), activityList, s, mqttAndroidClient, pahoMqttClient);
        activityListrv.setLayoutManager(new LinearLayoutManager(getActivity()));
        activityListrv.setAdapter(activityListAdapter);
        progressBar.setVisibility(View.GONE);
        activityListrv.setVisibility(View.VISIBLE);

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
        getActivity().setTitle("User Activity");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "You leaved");
        try {
           // pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "You leaved");
        try {
           // pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
