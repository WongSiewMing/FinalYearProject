package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.AvailableTimeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Stuff;


public class MakeAppointment extends Fragment {
    private final static String TAG = "Make Appointment";
    private Stuff stuff;
    private View view;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private String command = "";
    private JSONObject jsonObj;
    private FragmentManager fragmentManager;
    private String connection = "Disconnect";
    private ImageView btnBack, stuffImage;
    private Button btnMakeAppointment;
    private ProgressBar progressBar;
    private ScrollView body;
    private TextView stuffStudentName, stuffName, stuffDescription, stuffPrice, stuffQuantity, stuffCategory, stuffCondition, stuffValidity;
    private TextView btnSelectTime, availableDayOfWeek, meetDate, startTime, endTime;

    private Dialog selectTimeDialog;
    private ProgressBar selectTimeProgressBar;
    private RecyclerView selectTimeRV;
    private List<AvailableTimeOB> arrayAvailableTime = new ArrayList<>();
    private TextView noAvailableTimeIndicator;
    private SelectAvailableTime_Adapter adapter;
    private String timeSet = "", currentAppointmentID = "", newAppointmentID = "", insertAppoinmentURL = "";
    private JSONObject myjsonObj;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_make_appointment, container, false);
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");
        Bundle bundle = getArguments();
        stuff = (Stuff) bundle.getSerializable("ClickedStuff");
        btnBack = view.findViewById(R.id.back);
        stuffImage = view.findViewById(R.id.stuffImage);
        btnMakeAppointment = view.findViewById(R.id.btnMakeAppointment);
        progressBar = view.findViewById(R.id.progressBar);
        body = view.findViewById(R.id.body);
        stuffStudentName = view.findViewById(R.id.studentName);
        stuffName = view.findViewById(R.id.stuffName);
        stuffDescription = view.findViewById(R.id.stuffDescription);
        stuffPrice = view.findViewById(R.id.stuffPrice);
        stuffQuantity = view.findViewById(R.id.stuffQuantity);
        stuffCategory = view.findViewById(R.id.stuffCategory);
        stuffCondition = view.findViewById(R.id.stuffCondition);
        stuffValidity = view.findViewById(R.id.stuffValidity);
        btnSelectTime = view.findViewById(R.id.btnSelectTime);
        availableDayOfWeek = view.findViewById(R.id.availableDate);
        meetDate = view.findViewById(R.id.date);
        startTime = view.findViewById(R.id.availableStartTime);
        endTime = view.findViewById(R.id.availableEndTime);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                connection = "Connected";
                Log.d(TAG,"Connection = " + connection);
                populateView();
            }

            @Override
            public void connectionLost(Throwable cause) {
                connection = "Disconnected";
                Log.d(TAG,"Connection = " + connection);


            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                try {
//                    command = "{\"command\": \"30303530305D\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
//                            "\"availableID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAvailableID()) + "\" ," +
//                            "\"studentID\": " + "\"" + Conversion.asciiToHex(Navigation.student.getStudentID()) + "\" ," +
//                            "\"targetUserID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getStudentID()) + "\" ," +
//                            "\"availableDate\": " + "\"" + Conversion.asciiToHex(holder.appointmentDate.getText().toString().trim()) + "\" ," +
//                            "\"availableDayOfWeek\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAvailableDate()) + "\" ," +
//                            "\"startTime\": " + "\"" + Conversion.asciiToHex(holder.startTime.getText().toString().trim()) + "\" ," +
//                            "\"endTime\": " + "\"" + Conversion.asciiToHex(holder.endTime.getText().toString().trim()) + "\" ," +
//                            "\"availableStatus\": " + "\"" + Conversion.asciiToHex(mData.get(position).getAvailableStatus()) + "\" }";

                    myjsonObj = new JSONObject(mqttMessage.toString());
                    if (myjsonObj.getString("command").equals("30303530305D")){
                        if (Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(UpdateNavigation.student.getStudentID())){
                            Log.d(TAG, "Message received!");
                            availableDayOfWeek.setText(Conversion.hexToAscii(myjsonObj.getString("availableDayOfWeek")));
                            meetDate.setText(Conversion.hexToAscii(myjsonObj.getString("availableDate")));
                            startTime.setText(Conversion.hexToAscii(myjsonObj.getString("startTime")));
                            endTime.setText(Conversion.hexToAscii(myjsonObj.getString("endTime")));
                            timeSet = "SET";
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
        if (connection.equals("Connected")){

        }else if (connection.equals("Disconnected")){
            Toast.makeText(getActivity().getApplicationContext(), "No wifi access", Toast.LENGTH_LONG).show();

        }
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        selectTimeDialog = new Dialog(getActivity());

        btnSelectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTimeDialog.setContentView(R.layout.select_available_time);
                selectTimeProgressBar = selectTimeDialog.findViewById(R.id.progressBar2);
                selectTimeRV = selectTimeDialog.findViewById(R.id.availableTimeRV2);
                noAvailableTimeIndicator = selectTimeDialog.findViewById(R.id.noTimeIndicator);
                selectTimeDialog.show();
                if (checkConnection()){
                    populateAvailableList();
                }
            }
        });

        btnMakeAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkConnection()){
                    Log.d(TAG, "Button Clicked");
                    if (timeSet.equals("SET")){
                        Log.d(TAG, "Time set.");
                        getAppointmentID();
                    }else {
                        AlertDialog.Builder sent = new AlertDialog.Builder(getActivity());
                        sent.setTitle("Available time not select.");
                        sent.setMessage("Available time is require");
                        sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        sent.show();
                    }


                }
            }
        });

        return view;
    }

    private void getAppointmentID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getAppointmentID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                   Log.d(TAG, "Responded");
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject appointmentIDResponse = (JSONObject) response.get(i);
                                        currentAppointmentID = appointmentIDResponse.getString("CurrentAppointmentID");
                                    }
                                    Log.d(TAG, " Current appointment ID =" + currentAppointmentID);

                                    if (currentAppointmentID.equals("0")) {
                                        newAppointmentID = "apt1001";
                                    } else {
                                        String first = currentAppointmentID.substring(0, 3);
                                        String last = currentAppointmentID.substring(3);
                                        int number = Integer.parseInt(last) + 1;
                                        newAppointmentID = first + Integer.toString(number);
                                    }

                                    Log.d(TAG, "New appointment ID =" + newAppointmentID);
                                    insertAppointmentInfo(newAppointmentID);



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

    private void insertAppointmentInfo(final String newAppointmentID) {
        try {
            command = "{\"command\": \"30303530305E\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"appointmentID\": " + "\"" + Conversion.asciiToHex(newAppointmentID) + "\" ," +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(UpdateNavigation.student.getStudentID().trim()) + "\" ," +
                    "\"availableID\": " + "\"" + myjsonObj.getString("availableID") + "\" ," +
                    "\"availableDate\": " + "\"" + myjsonObj.getString("availableDate") + "\" ," +
                    "\"stuffID\": " + "\"" + Conversion.asciiToHex(stuff.getStuffID()) + "\" ," +
                    "\"opponentID\": " + "\"" + Conversion.asciiToHex(stuff.getStudentID().getStudentID()) + "\" ," +
                    "\"appointmentStatus\": " + "\"" + Conversion.asciiToHex("PENDING") + "\" }";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            insertAppoinmentURL = Constant.serverFile + "insertAppointmentData.php?appointmentID=" + newAppointmentID
                    + "&studentID=" + UpdateNavigation.student.getStudentID()
                    + "&availableID=" + Conversion.hexToAscii(myjsonObj.getString("availableID"))
                    + "&stuffID=" + stuff.getStuffID()
                    + "&opponentID=" + stuff.getStudentID().getStudentID()
                    + "&appointmentDate=" + Conversion.hexToAscii(myjsonObj.getString("availableDate"));

            Log.d(TAG, "make appointment URL = " + insertAppoinmentURL);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        insertAppoinmentURL,
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
//                                                        getFragmentManager().popBackStack();
                                                        btnMakeAppointment.setText("APPOINTMENT MADE");
                                                        btnMakeAppointment.setClickable(false);
                                                        btnMakeAppointment.setFocusable(false);

                                                        break;
                                                }
                                            }
                                        };

                                        builder.setTitle("Appointment request is successfully added.")
                                                .setMessage("Appointment request is successfully send to user.")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Failed to send appointment request.");
                                        builder.setMessage("Your appointment request is not send to user.");
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
                                if (isAdded()) {
                                }
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        try {
                        params.put("appointmentID", newAppointmentID);
                        params.put("studentID", UpdateNavigation.student.getStudentID());
                        params.put("availableID", Conversion.hexToAscii(myjsonObj.getString("availableID")));
                        params.put("stuffID", stuff.getStuffID());
                        params.put("opponentID", stuff.getStudentID().getStudentID());
                        params.put("appointmentDate", Conversion.hexToAscii(myjsonObj.getString("availableDate")));
                        }catch (JSONException e){
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


        }catch (Exception e) {
            e.printStackTrace();
            if (isAdded()) {
            }
        }
    }

    private void populateAvailableList() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                //http://192.168.0.103/raindown/getAvailableTimeList.php?studentID=17wmr05969
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getAvailableTimeList.php?studentID=" + stuff.getStudentID().getStudentID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {

                                    arrayAvailableTime.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject availableTimeResponse = (JSONObject) response.get(i);
                                        arrayAvailableTime.add(new AvailableTimeOB(availableTimeResponse.getString("availableID"),
                                                availableTimeResponse.getString("studentID"),
                                                availableTimeResponse.getString("availableDate"),
                                                availableTimeResponse.getString("startTime"),
                                                availableTimeResponse.getString("endTime"),
                                                availableTimeResponse.getString("availableStatus")));

                                    }
                                    Log.d(TAG, "Respond length = " + response.length());
                                    Log.d(TAG, "List length = " + arrayAvailableTime.size());

                                    if (response.length() == 0) {
                                        selectTimeProgressBar.setVisibility(View.GONE);
                                        noAvailableTimeIndicator.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "Respond length =" + response.length());
                                    } else {
                                        Log.d(TAG,"Respond length not empty");
                                        populateRecycleView();
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
        adapter = new SelectAvailableTime_Adapter(getActivity(), arrayAvailableTime, mqttAndroidClient, selectTimeDialog);
        selectTimeRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        selectTimeRV.setAdapter(adapter);
        Log.d(TAG,"You here");
        selectTimeRV.setVisibility(View.VISIBLE);
        selectTimeProgressBar.setVisibility(View.GONE);
        noAvailableTimeIndicator.setVisibility(View.GONE);


    }

    private boolean checkConnection(){
        String error = "";
        boolean indicator = true;
        if (connection.equals("Disconnected")){
            indicator = false;
        }
        if (indicator == false){
            Toast.makeText(getActivity().getApplicationContext(),"No internet access", Toast.LENGTH_LONG).show();
        }
        return indicator;
    }

    private void populateView() {
        Picasso.with(getActivity()).load(stuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(stuffImage);
        stuffStudentName.setText(stuff.getStudentID().getStudentName());//seller name
        stuffName.setText(stuff.getStuffName());
        stuffDescription.setText(stuff.getStuffDescription());
        stuffPrice.setText(String.format("%.2f", stuff.getStuffPrice()));
        stuffQuantity.setText("" + stuff.getStuffQuantity());
        stuffCategory.setText(stuff.getStuffCategory());
        stuffCondition.setText(stuff.getStuffCondition());
        stuffValidity.setText(stuff.getValidStartDate() + " - " + stuff.getValidEndDate());


        progressBar.setVisibility(View.GONE);
        body.setVisibility(View.VISIBLE);
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
