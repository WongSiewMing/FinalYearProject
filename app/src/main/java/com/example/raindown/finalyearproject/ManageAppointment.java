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


public class ManageAppointment extends Fragment {

    private final static String TAG = "Appointment";
    private View view;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private String command = "";
    private JSONObject jsonObj;
    private Button btnEditAvailableTime;
    private ProgressBar appointmentProgressBar, requestProgressBar;
    private TextView noAppointmentIndicator, noRequestIndicator;
    private RecyclerView appointmentRV, requestRV;
    private FragmentManager fragmentManager;
    private String connection = "", updateAppointmentInfoURL = "";
    private List<AppointmentOB> appointmentList;
    private AppointmentListAdapter appointmentAdapter;
    private JSONObject myjsonObj;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Manage appointment");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_manage__appointment, container, false);
        getActivity().setTitle("Manage Appointment");
        btnEditAvailableTime = view.findViewById(R.id.btnEditAvailableTime);
        appointmentProgressBar = view.findViewById(R.id.appoinmentProgressBar);
        requestProgressBar = view.findViewById(R.id.requestProgressBar);
        noAppointmentIndicator = view.findViewById(R.id.noAppointmentIndicator);
        noRequestIndicator = view.findViewById(R.id.noRequestIndicator);
        appointmentRV = view.findViewById(R.id.appointmentRV);
        requestRV = view.findViewById(R.id.requestRV);
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");
        appointmentList = new ArrayList<>();
        appointmentList.clear();

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                connection = "Connected";
                getAppointmentList();

            }

            @Override
            public void connectionLost(Throwable cause) {
                connection = "Disconnected";

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                myjsonObj = new JSONObject(mqttMessage.toString());
                if (myjsonObj.getString("command").equals("30303530305F")){
                    for (int i = 0; i < appointmentList.size(); i ++){
                        if (Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(Navigation.student.getStudentID())){
                            if (Conversion.hexToAscii(myjsonObj.getString("appointmentID")).equals(appointmentList.get(i).getAppointmentID())){
                                updateAppoinmentInfo(appointmentList.get(i));

                            }
                        }

                    }
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        btnEditAvailableTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditAvailableTime editAvailableTime = new EditAvailableTime();
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, editAvailableTime)
                        .addToBackStack(null)
                        .commit();


            }
        });

        return view;
    }

    private void updateAppoinmentInfo(final AppointmentOB appointmentOB) {
        String newAppointmentStatus= "";

        if (appointmentOB.getAppointmentStatus().equals("PENDING")){
            newAppointmentStatus = "REJECTED";
        }else {
            newAppointmentStatus = appointmentOB.getAppointmentStatus();
        }

        updateAppointmentInfoURL = Constant.serverFile + "updateAppointmentInfo.php?appointmentID=" + appointmentOB.getAppointmentID()
        + "&studentID=" + appointmentOB.getStudentID().getStudentID()
        + "&availableID=" + appointmentOB.getAvailableID().getAvailableID()
        + "&stuffID=" + appointmentOB.getStuffID().getStuffID()
        + "&opponentID=" + appointmentOB.getOpponentID()
        + "&appointmentStatus=" + newAppointmentStatus
        + "&appointmentDate=" + appointmentOB.getAppointmentDate()
        + "&opponentRecord=" + "DELETED";
        Log.d(TAG, "Update appointment URL = " + updateAppointmentInfoURL);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        try {
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST,
                    updateAppointmentInfoURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                String success = jsonObject.getString("success");
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                if (success.equals("1")) {

                                    for (int ai = 0; ai < appointmentList.size(); ai++){
                                        if (appointmentOB.getAppointmentID().equals(appointmentList.get(ai).getAppointmentID())) {
                                            appointmentList.remove(ai);
                                        }
                                        }

//                                    appointmentList.remove(position);
                                    if (appointmentList.size() == 0) {
                                        Log.d(TAG, "Array size = " + appointmentList.size());
                                        appointmentProgressBar.setVisibility(View.GONE);
                                        noAppointmentIndicator.setVisibility(View.VISIBLE);
                                    } else {
                                        Log.d(TAG, "array size = " + appointmentList.size());
                                        populateAppointmentRecycleView();
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


    private void getAppointmentList() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                //http://192.168.0.106/raindown/getAppointmentList.php?studentID=17wmr05969
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getAppointmentList.php?studentID=" + Navigation.student.getStudentID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {

                                    appointmentList.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject appointmentListResponse = (JSONObject) response.get(i);
                                        appointmentList.add(new AppointmentOB(appointmentListResponse.getString("appointmentID"),
                                                new Student(appointmentListResponse.getString("studentID"),
                                                        appointmentListResponse.getString("photo"),
                                                        appointmentListResponse.getString("studentName"),
                                                        appointmentListResponse.getString("studentProgramme")),
                                                new AvailableTimeOB(appointmentListResponse.getString("availableID"),
                                                        appointmentListResponse.getString("availableStudentID"),
                                                        appointmentListResponse.getString("availableDate"),
                                                        appointmentListResponse.getString("startTime"),
                                                        appointmentListResponse.getString("endTime"),
                                                        appointmentListResponse.getString("availableStatus")),
                                                new Stuff(appointmentListResponse.getString("stuffID"),
                                                        appointmentListResponse.getString("stuffName"),
                                                        appointmentListResponse.getString("stuffImage"),
                                                        appointmentListResponse.getString("stuffDescription"),
                                                        appointmentListResponse.getString("stuffStudentID")),
                                                appointmentListResponse.getString("opponentID"),
                                                appointmentListResponse.getString("appointmentStatus"),
                                                appointmentListResponse.getString("appointmentDate")));


                                    }
                                    Log.d(TAG, "Respond length = " + response.length());
                                    Log.d(TAG, "List length = " + appointmentList.size());

                                    if (response.length() == 0) {
                                        appointmentProgressBar.setVisibility(View.GONE);
                                        noAppointmentIndicator.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "Respond length =" + response.length());
                                    } else {
                                        Log.d(TAG,"Respond length not empty");
                                        populateAppointmentRecycleView();
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

    private void populateAppointmentRecycleView() {
        appointmentAdapter = new AppointmentListAdapter(getActivity(), appointmentList, mqttAndroidClient);
        appointmentAdapter.notifyDataSetChanged();
        appointmentRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        appointmentRV.setAdapter(appointmentAdapter);
        appointmentRV.setVisibility(View.VISIBLE);
        appointmentProgressBar.setVisibility(View.GONE);
        noAppointmentIndicator.setVisibility(View.GONE);

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
