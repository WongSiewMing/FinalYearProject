package com.example.raindown.finalyearproject;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Helper.AvailableTimeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;

public class UpdateAvailableTime extends Fragment {

    private View view;
    private AvailableTimeOB availableTimeOB = null;
    private Spinner dayOfWeek;
    private TextView startTime, endTime;
    private SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
    private DateFormat dateFormat = new SimpleDateFormat("HHmm");
    private DateFormat dateFormat2 = new SimpleDateFormat("hh:mm a");
    private Date startDate, endDate, date;
    private TimePickerDialog timePickerDialog;
    private Button btnUpdateTime;
    private String command, jsonURL, newStartTimeFormat, newEndTimeFormat;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private FragmentManager fragmentManager;
    private JSONObject jsonObj;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Edit Available Time");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.update_available_time, container, false);
        Bundle bundle = getArguments();
        availableTimeOB = (AvailableTimeOB) bundle.getSerializable("EditAvailableTime");
        dayOfWeek = view.findViewById(R.id.dayOfWeek_edit);
        startTime = view.findViewById(R.id.startTime_edit);
        endTime = view.findViewById(R.id.endTime_edit);
        btnUpdateTime = view.findViewById(R.id.btnEditTime);

        ArrayAdapter<CharSequence> adaperDayOfWeek = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.dayOfWeek,
                android.R.layout.simple_spinner_dropdown_item);
        dayOfWeek.setAdapter(adaperDayOfWeek);

        String day = availableTimeOB.getAvailableDate();
        int spinnerPosition = adaperDayOfWeek.getPosition(day);
        dayOfWeek.setSelection(spinnerPosition);

        try {

            date = dateFormat.parse(availableTimeOB.getStartTime());
            startTime.setText(df.format(date));
            startDate = dateFormat2.parse(startTime.getText().toString());

            date = dateFormat.parse(availableTimeOB.getEndTime());
            endTime.setText(df.format(date));
            endDate = dateFormat2.parse(endTime.getText().toString());

        } catch (ParseException e) {
            e.printStackTrace();
        }

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    startDate = dateFormat2.parse(startTime.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar c = Calendar.getInstance();
                c.setTime(startDate);
                df = new SimpleDateFormat("hh:mm a");
                int mhour = c.get(Calendar.HOUR_OF_DAY);
                int mminute = c.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String start = hourOfDay + ":" + minute;
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                        Date d;
                        try {
                            d = dateFormat.parse(start);

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

                try {
                    endDate = dateFormat2.parse(endTime.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar c = Calendar.getInstance();
                c.setTime(endDate);
                df = new SimpleDateFormat("hh:mm a");
                int mhour = c.get(Calendar.HOUR_OF_DAY);
                int mminute = c.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String start = hourOfDay + ":" + minute;
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                        Date d;
                        try {
                            d = dateFormat.parse(start);

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

        btnUpdateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSubmitField()){
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:

                                    try {
                                        date = dateFormat2.parse(startTime.getText().toString().trim());
                                        newStartTimeFormat = dateFormat.format(date);

                                        date = dateFormat2.parse(endTime.getText().toString().trim());
                                        newEndTimeFormat = dateFormat.format(date);

                                    }catch (ParseException e){
                                        e.printStackTrace();
                                    }

                                    command = "{\"command\": \"303035303084\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                            "\"availableID\": " + "\"" + Conversion.asciiToHex(availableTimeOB.getAvailableID()) + "\" ," +
                                            "\"availableDate\": " + "\"" + Conversion.asciiToHex(dayOfWeek.getSelectedItem().toString().trim()) + "\" ," +
                                            "\"startTime\": " + "\"" + Conversion.asciiToHex(newStartTimeFormat) + "\" ," +
                                            "\"endTime\": " + "\"" + Conversion.asciiToHex(newEndTimeFormat) + "\" }";

                                    pahoMqttClient = new PahoMqttClient();
                                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                    updateAvailableTime();

                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Update Available Time ?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private boolean checkSubmitField(){
        String error = "";
        boolean indicator = true;
        Date start = new Date();
        Date end= new Date();

        if (startTime.getText().toString().trim().equals("click to select start time") || endTime.getText().toString().trim().equals("click to select end time")){
            error += "- Time not selected.\n";
            indicator = false;
        }else {
            try {
                start = dateFormat2.parse(startTime.getText().toString());
                end = dateFormat2.parse(endTime.getText().toString());

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

    public void updateAvailableTime(){
        try {

            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                try {
                    jsonObj = new JSONObject(command);
                    if (jsonObj.getString("command").equals("303035303084")) {

                        jsonURL = Constant.serverFile + "editAvailableTime.php?availableID=" + Conversion.hexToAscii(jsonObj.getString("availableID"))
                                + "&availableDate=" + Conversion.hexToAscii(jsonObj.getString("availableDate"))
                                + "&startTime=" + Conversion.hexToAscii(jsonObj.getString("startTime"))
                                + "&endTime=" + Conversion.hexToAscii(jsonObj.getString("endTime"));

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
                                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                if (success.equals("1")) {
                                                    Toast.makeText(getActivity().getApplication(), "Available time updated successfully",
                                                            Toast.LENGTH_LONG).show();
                                                    EditAvailableTime editAvailableTime = new EditAvailableTime();
                                                    fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                                                    fragmentManager.beginTransaction()
                                                            .replace(R.id.update_fragmentHolder, editAvailableTime)
                                                            .addToBackStack(null)
                                                            .commit();
                                                } else {
                                                    builder.setTitle("Oops !");
                                                    builder.setMessage("Something Wrong. Please Try Again.");
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
                                        params.put("availableID", Conversion.hexToAscii(jsonObj.getString("availableID")));
                                        params.put("availableDate", Conversion.hexToAscii(jsonObj.getString("availableDate")));
                                        params.put("startTime", Conversion.hexToAscii(jsonObj.getString("startTime")));
                                        params.put("endTime", Conversion.hexToAscii(jsonObj.getString("endTime")));
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

        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
