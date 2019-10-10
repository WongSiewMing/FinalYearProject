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
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.AppointmentOB;
import Helper.AvailableTimeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;
import Helper.Stuff;


public class ManageRequest extends Fragment {
    private View view;
    private ProgressBar progressBar;
    private TextView noRequestIndicator;
    private RecyclerView requestRV;

    private final static String TAG = "Request";
    private PahoMqttClient pahoMqttClient = new PahoMqttClient();
    private MqttAndroidClient mqttAndroidClient;
    private String connection = "", command = "", updateRequestUrl = "";
    private List<AppointmentOB> requestList = new ArrayList<>();
    private RequestListAdapter adapter;
    private JSONObject myjsonObj;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Request");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_manage_request, container, false);
        progressBar = view.findViewById(R.id.requestProgressBar);
        noRequestIndicator = view.findViewById(R.id.noRequestIndicator);
        requestRV = view.findViewById(R.id.requestRV);
        progressBar.setVisibility(View.VISIBLE);
        noRequestIndicator.setVisibility(View.INVISIBLE);
        requestRV.setVisibility(View.INVISIBLE);

        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                connection = "Connected";
                getRequestList();
            }

            @Override
            public void connectionLost(Throwable cause) {
                connection = "Disconnected";

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.d(TAG, "Message arrived!");
                myjsonObj = new JSONObject(mqttMessage.toString());
                if (myjsonObj.getString("command").equals("303035303063")){
                    if (Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(UpdateNavigation.student.getStudentID())){
                        for (int i = 0; i < requestList.size(); i++) {
                            if (Conversion.hexToAscii(myjsonObj.getString("appointmentID")).equals(requestList.get(i).getAppointmentID())) {

                                updateRequestInfo(requestList.get(i));
                            }
                        }

                    }
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        return view;
    }

    private void updateRequestInfo(final AppointmentOB appointmentOB) {
        String newAppointmentStatus= "";

        if (appointmentOB.getAppointmentStatus().equals("PENDING")){
            newAppointmentStatus = "CANCELED";
        }else {
            newAppointmentStatus = appointmentOB.getAppointmentStatus();
        }

        updateRequestUrl = Constant.serverFile + "updateRequestInfo.php?appointmentID=" + appointmentOB.getAppointmentID()
                + "&availableID=" + appointmentOB.getAvailableID().getAvailableID()
                + "&stuffID=" + appointmentOB.getStuffID().getStuffID()
                + "&opponentID=" + appointmentOB.getOpponentID()
                + "&appointmentStatus=" + newAppointmentStatus
                + "&appointmentDate=" + appointmentOB.getAppointmentDate()
                + "&requesterRecord=" + "DELETED";
        Log.d(TAG, "Update appointment URL = " + updateRequestUrl);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        try {
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST,
                    updateRequestUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                String success = jsonObject.getString("success");
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                if (success.equals("1")) {

                                    for (int ai = 0; ai < requestList.size(); ai++){
                                        if (appointmentOB.getAppointmentID().equals(requestList.get(ai).getAppointmentID())) {
                                            requestList.remove(ai);
                                        }
                                    }

//                                    appointmentList.remove(position);
                                    if (requestList.size() == 0) {
                                        Log.d(TAG, "Array size = " + requestList.size());
                                        progressBar.setVisibility(View.GONE);
                                        noRequestIndicator.setVisibility(View.VISIBLE);
                                    } else {
                                        Log.d(TAG, "array size = " + requestList.size());
                                        populateRequestRecycleView();
                                    }


                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    break;
                                            }
                                        }
                                    };

                                    builder.setTitle("Appointment is successfully deleted")
                                            .setMessage("Appointment is deleted.")
                                            .setPositiveButton("OK", dialogClickListener).show();
                                } else {
                                    builder.setTitle("Appointment is failed to delete");
                                    builder.setMessage("Appointment is not delete.");
                                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    builder.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

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

    private void getRequestList() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                //http://192.168.0.106/raindown/getRequestList.php?studentID=17WMR05969
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getRequestList.php?studentID=" + UpdateNavigation.student.getStudentID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {

                                    requestList.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject requestListResponse = (JSONObject) response.get(i);
                                        requestList.add(new AppointmentOB(requestListResponse.getString("appointmentID"),
                                                new AvailableTimeOB(requestListResponse.getString("availableID"),
                                                        requestListResponse.getString("availableStudentID"),
                                                        requestListResponse.getString("availableDate"),
                                                        requestListResponse.getString("startTime"),
                                                        requestListResponse.getString("endTime"),
                                                        requestListResponse.getString("availableStatus")),
                                                new Stuff(requestListResponse.getString("stuffID"),
                                                        requestListResponse.getString("stuffName"),
                                                        requestListResponse.getString("stuffImage"),
                                                        requestListResponse.getString("stuffDescription"),
                                                        requestListResponse.getString("stuffStudentID")),
                                                requestListResponse.getString("opponentID"),
                                                requestListResponse.getString("appointmentStatus"),
                                                requestListResponse.getString("appointmentDate"),
                                                new Student(requestListResponse.getString("studentID"),
                                                        requestListResponse.getString("photo"),
                                                        requestListResponse.getString("studentName"),
                                                        requestListResponse.getString("studentProgramme"))));


                                    }
                                    Log.d(TAG, "Respond length = " + response.length());
                                    Log.d(TAG, "List length = " + requestList.size());

                                    if (response.length() == 0) {
                                        progressBar.setVisibility(View.GONE);
                                        noRequestIndicator.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "Respond length =" + response.length());
                                    } else {
                                        Log.d(TAG,"Respond length not empty");
                                        populateRequestRecycleView();
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

    private void populateRequestRecycleView() {
        adapter = new RequestListAdapter(getActivity(), requestList, mqttAndroidClient);
        adapter.notifyDataSetChanged();
        requestRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        requestRV.setAdapter(adapter);
        requestRV.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        noRequestIndicator.setVisibility(View.GONE);
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
