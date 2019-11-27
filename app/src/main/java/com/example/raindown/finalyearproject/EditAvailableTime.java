package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.AvailableTimeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class EditAvailableTime extends Fragment {
    private final static String TAG = "EditTime";
    private View view;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command, currenAvailableID, newAvailableID, insertAvailableTimeUrl, newStartTimeFormat, newEndTimeFormat;
    private JSONObject jsonObj;
    private JSONObject myjsonObj;
    private List<AvailableTimeOB> arrayAvailableTime = new ArrayList<>();

    private TextView noTimeIndicator, startTime, endTime;
    private ProgressBar progressBar;
    private RecyclerView availableTimeRV;
    private Spinner dayOfWeek;
    private AvailableTimeAdapter adapter;
    private Button btnAddTime;

    private Date c;
    private SimpleDateFormat df;
    private TimePickerDialog timePickerDialog;
    private ProgressDialog pDialog = null;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Manage Available Time");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_available_time, container, false);
        btnAddTime = view.findViewById(R.id.btnAddTime);
        noTimeIndicator = view.findViewById(R.id.noTimeIndicator);
        progressBar = view.findViewById(R.id.progressBar);
        availableTimeRV = view.findViewById(R.id.availableTimeRV);
        startTime = view.findViewById(R.id.startTime);
        endTime = view.findViewById(R.id.endTime);
        dayOfWeek = view.findViewById(R.id.dayOfWeek);
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");
        pDialog = new ProgressDialog(getActivity());

        populateAvailableList();

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                populateAvailableList();

            }

            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                myjsonObj = new JSONObject(mqttMessage.toString());

                if (myjsonObj.getString("command").equals("30303530305C")){
                    for (int i = 0; i< arrayAvailableTime.size(); i++){
                        if (Conversion.hexToAscii(myjsonObj.getString("availableID")).equals(arrayAvailableTime.get(i).getAvailableID())){
                            Log.d(TAG, "ID MATCHED!!" + arrayAvailableTime.get(i).getAvailableID());
                            arrayAvailableTime.remove(i);
                            populateRecycleView();
                        }
                    }
                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        btnAddTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSubmitField()){
                    Log.d(TAG, "OK");
                    getAvailableID();
                }
            }
        });

        ArrayAdapter<CharSequence> adaperDayOfWeek = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.dayOfWeek,
                android.R.layout.simple_spinner_dropdown_item);
        dayOfWeek.setAdapter(adaperDayOfWeek);

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c = Calendar.getInstance().getTime();
                df = new SimpleDateFormat("hh:mma");
                //startTime.setText(df.format(c));
                int mhour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int mminute = Calendar.getInstance().get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String start = hourOfDay + ":" + minute;
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                        Date d;
                        try {
                            d = dateFormat.parse(start);
                            Log.d(TAG,"Selected open time = " + df.format(d));
                            startTime.setText(df.format(d));
                        }catch (ParseException e){
                            e.printStackTrace();
                        }

                    }
                }, mhour,mminute, false);
                timePickerDialog.setTitle("Select start time");
                timePickerDialog.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c= Calendar.getInstance().getTime();
                df = new SimpleDateFormat("hh:mma");
                int mhour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int mminute = Calendar.getInstance().get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String end = hourOfDay + ":" + minute;
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                        Date d;
                        try {
                            d = dateFormat.parse(end);
                            Log.d(TAG,"Selected end time = " + df.format(d));
                            endTime.setText(df.format(d));

                        }catch (ParseException e){
                            e.printStackTrace();
                        }

                    }
                }, mhour,mminute, false);
                timePickerDialog.setTitle("Select end time");
                timePickerDialog.show();
            }
        });


        return view;
    }


    private void populateAvailableList() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getAvailableTimeList.php?studentID=" + UpdateNavigation.student.getStudentID(),
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
                                        progressBar.setVisibility(View.GONE);
                                        noTimeIndicator.setVisibility(View.VISIBLE);
                                        Log.d(TAG, "Respond length =" + response.length());
                                    } else {
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
        adapter = new AvailableTimeAdapter(getActivity(), arrayAvailableTime, mqttAndroidClient);
        adapter.notifyDataSetChanged();
        availableTimeRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        availableTimeRV.setAdapter(adapter);
        progressBar.setVisibility(View.GONE);
        availableTimeRV.setVisibility(View.VISIBLE);
        noTimeIndicator.setVisibility(View.GONE);
    }


    private void getAvailableID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getAvailableID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    Log.d(TAG, "commentID responded");
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject availableIDResponse = (JSONObject) response.get(i);
                                        currenAvailableID = availableIDResponse.getString("CurrentAvailableID");
                                    }
                                    Log.d(TAG, " Current comment ID =" + currenAvailableID);

                                    if (currenAvailableID.equals("0")) {
                                        newAvailableID = "avai1001";
                                    } else {
                                        String first = currenAvailableID.substring(0, 4);
                                        String last = currenAvailableID.substring(4);
                                        int number = Integer.parseInt(last) + 1;
                                        newAvailableID = first + Integer.toString(number);
                                    }

                                    Log.d(TAG, "New Comment ID = " + newAvailableID);

                                    insertAvailableTime(newAvailableID);


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

    private void insertAvailableTime(final String newAvailableID) {
        try {
            command = "{\"command\": \"303035303059\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"availableID\": " + "\"" + Conversion.asciiToHex(newAvailableID) + "\" ," +
                    "\"studentID\": " + "\"" + Conversion.asciiToHex(UpdateNavigation.student.getStudentID()) + "\" ," +
                    "\"availableDate\": " + "\"" + Conversion.asciiToHex(dayOfWeek.getSelectedItem().toString().trim()) + "\" ," +
                    "\"startTime\": " + "\"" + Conversion.asciiToHex(startTime.getText().toString().trim()) + "\" ," +
                    "\"endTime\": " + "\"" + Conversion.asciiToHex(endTime.getText().toString().trim()) + "\" }";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
            newStartTimeFormat = "";
            newEndTimeFormat = "";

            df = new SimpleDateFormat("HHmm");
            DateFormat dateFormat = new SimpleDateFormat("hh:mma");
            Date d;
            try {
                d= dateFormat.parse(startTime.getText().toString().trim());
                newStartTimeFormat = df.format(d);
                d= dateFormat.parse(endTime.getText().toString().trim());
                newEndTimeFormat = df.format(d);
            }catch (ParseException e){
                e.printStackTrace();
            }

            insertAvailableTimeUrl = Constant.serverFile + "insertAvailableTime.php?availableID=" + newAvailableID
                    + "&studentID=" + UpdateNavigation.student.getStudentID()
                    + "&availableDate=" + dayOfWeek.getSelectedItem().toString().trim()
                    + "&startTime=" + newStartTimeFormat
                    + "&endTime=" + newEndTimeFormat;

            Log.d(TAG, "Add Comment URL = " + insertAvailableTimeUrl);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        insertAvailableTimeUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (isAdded()) {
                                }
                                JSONObject jsonObject = null;
                                Log.d(TAG, "Respond is here");
                                try {
                                    jsonObject = new JSONObject(response);
                                    String success = jsonObject.getString("success");
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                    if (success.equals("1")) {
                                        arrayAvailableTime.add(new AvailableTimeOB(newAvailableID,
                                                UpdateNavigation.student.getStudentID(),
                                                dayOfWeek.getSelectedItem().toString().trim(),
                                                newStartTimeFormat,
                                                newEndTimeFormat,
                                                "ACTIVE"));
                                        populateRecycleView();

                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:

                                                        dayOfWeek.setSelection(0);
                                                        startTime.setText("click to select start time");
                                                        endTime.setText("click to select end time");

                                                        break;
                                                }
                                            }
                                        };

                                        builder.setTitle("Available time is successfully added")
                                                .setMessage("Available time is uploaded")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Failed to add available time");
                                        builder.setMessage("Available time is failed to upload");
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

//                                                commentMessage.getText().clear();
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
                        params.put("availableID", newAvailableID);
                        params.put("studentID", UpdateNavigation.student.getStudentID());
                        params.put("availableDate", dayOfWeek.getSelectedItem().toString().trim());
                        params.put("startTime", newStartTimeFormat);
                        params.put("endTime", newEndTimeFormat);
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

    private boolean checkSubmitField(){
        String error = "";
        boolean indicator = true;
        DateFormat dateFormat = new SimpleDateFormat("hh:mma");
        Date start = new Date();
        Date end= new Date();

        if (startTime.getText().toString().trim().equals("click to select start time") || endTime.getText().toString().trim().equals("click to select end time")){
            error += "- Time not selected.\n";
            indicator = false;
        }else {
            try {
                start = dateFormat.parse(startTime.getText().toString());
                end = dateFormat.parse(endTime.getText().toString());

            }catch (ParseException e){
                e.printStackTrace();
            }
            if (end.before(start)){
                error += "- End time can not choose before start time.\n";
                indicator = false;
            }
        }

        if (dayOfWeek.getSelectedItem().toString().equals("--Choose Day--")){
            error += "- Day of week is require.\n";
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
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();

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

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
