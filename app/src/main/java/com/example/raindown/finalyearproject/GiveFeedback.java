package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.*;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import Helper.*;

/*Author : Adelina Tang Chooi Li
Programme : RSD3
Year : 2017*/

public class GiveFeedback extends Fragment {

    View view;
    Student student = null, seller = null;
    RatingBar rating;
    RadioGroup experience;
    RadioButton good, poor, excellent;
    EditText comment;
    Button submit;
    Float ratingValue;
    String currentRatingID = "", ratingID = "", jsonURL = "", experienceType = "",
            ratingDate = "", time = "", encodedComment = "", command = "", error = "";
    SimpleDateFormat sdf = null;
    Date d = null;
    ProgressDialog pDialog = null;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    JSONObject jsonObj;

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
        getActivity().setTitle("Give Feedbacks");
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

        view = inflater.inflate(R.layout.givefeedback, container, false);
        pDialog = new ProgressDialog(getActivity());
        Bundle bundle = getArguments();
        student = (Student) bundle.getSerializable("FeedbackMe");
        seller = (Student) bundle.getSerializable("GiveFeedback");

        rating = (RatingBar) view.findViewById(R.id.ratingBar);
        experience = (RadioGroup) view.findViewById(R.id.radiogroupExperience);
        excellent = (RadioButton) view.findViewById(R.id.radioExcellent);
        good = (RadioButton) view.findViewById(R.id.radioGood);
        poor = (RadioButton) view.findViewById(R.id.radioPoor);
        submit = (Button) view.findViewById(R.id.submit);
        comment = (EditText) view.findViewById(R.id.commentInput);

        rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ratingValue = rating;
                if (ratingValue >= 0.5 && ratingValue <= 1.0) {
                    poor.setChecked(true);
                } else if (ratingValue >= 1.5 && ratingValue <= 4.0) {
                    good.setChecked(true);
                } else if (ratingValue >= 4.5 && ratingValue <= 5.0) {
                    excellent.setChecked(true);
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:

                                    if (experience.getCheckedRadioButtonId() == R.id.radioExcellent) {
                                        experienceType = "303031";
                                    } else if (experience.getCheckedRadioButtonId() == R.id.radioGood) {
                                        experienceType = "303032";
                                    } else if (experience.getCheckedRadioButtonId() == R.id.radioPoor) {
                                        experienceType = "303033";
                                    }

                                    command = "{\"command\": \"30303530300F\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                            "\"ratingReceiver\": " + "\"" + Conversion.asciiToHex(seller.getStudentID()) + "\" ," +
                                            "\"ratingGiver\": " + "\"" + Conversion.asciiToHex(student.getStudentID()) + "\" ," +
                                            "\"ratingValue\": " + "\"" + Conversion.asciiToHex(String.valueOf(ratingValue)) + "\" ," +
                                            "\"experienceType\": " + "\"" + experienceType + "\" ," +
                                            "\"comments\": " + "\"" + Conversion.asciiToHex(comment.getText().toString().trim()) + "\"}";

                                    pahoMqttClient = new PahoMqttClient();
                                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");
                                    getRatingID();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Confirm to submit?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();


                }
            }
        });

        return view;
    }

    public boolean validateInput() {

        error = "";
        boolean indicator = true;

        if (ratingValue == null) {
            error += "- Please click the star to rate.\n";
            indicator = false;
        }
        if (!comment.getText().toString().trim().equals("")) {
            if (comment.getText().toString().trim().length() > 301) {
                error += "- No of characters for comment has exceeded 300.\n";
                indicator = false;
            }
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


    public void getRatingID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getRatingID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject ratingIDResponse = (JSONObject) response.get(i);
                                        currentRatingID = ratingIDResponse.getString("CurrentRatingID");
                                    }
                                    if (currentRatingID.equals("0")) {
                                        ratingID = "RAT0001";
                                    } else {
                                        ratingID = String.format("RAT%04d", (Integer.parseInt(currentRatingID.substring(3, 7)) + 1));
                                    }
                                    insertRatingData();
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
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }


    public void insertRatingData() {

        encodedComment = "";

        try {
            jsonObj = new JSONObject(command);
            if(jsonObj.getString("command").equals("30303530300F")){

                try {
                    if (jsonObj.getString("experienceType").equals("303031")) {
                        experienceType = "Excellent";
                    } else if (jsonObj.getString("experienceType").equals("303032")) {
                        experienceType = "Good";
                    } else if (jsonObj.getString("experienceType").equals("303033")) {
                        experienceType = "Poor";
                    }

                    d = Calendar.getInstance().getTime();
                    sdf = new SimpleDateFormat("dd/MM/yyyy");
                    ratingDate = sdf.format(d);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    time = sdf.format(new Date());

                    String[] encodeComment = {Conversion.hexToAscii(jsonObj.getString("comments"))};
                    for (String s : encodeComment) {
                        encodedComment += URLEncoder.encode(s, "UTF-8");
                    }

                    jsonURL = Constant.serverFile + "insertRatingData.php?ratingID=" + ratingID + "&ratingReceiver=" + Conversion.hexToAscii(jsonObj.getString("ratingReceiver"))
                            + "&ratingGiver=" + Conversion.hexToAscii(jsonObj.getString("ratingGiver")) + "&ratingValue=" + Conversion.hexToAscii(jsonObj.getString("ratingValue"))
                            + "&experience=" + experienceType + "&comments=" + encodedComment + "&ratingDate=" + ratingDate + "&ratingTime=" + time;

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
                                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        switch (which) {
                                                            case DialogInterface.BUTTON_POSITIVE:
                                                                clearData();
                                                                break;
                                                        }
                                                    }
                                                };
                                                builder.setTitle("Feedback sent!").setMessage("Feedback is successfully sent")
                                                        .setPositiveButton("Ok", dialogClickListener).show();
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
                                    params.put("ratingID", ratingID);
                                    params.put("ratingReceiver", Conversion.hexToAscii(jsonObj.getString("ratingReceiver")));
                                    params.put("ratingGiver", Conversion.hexToAscii(jsonObj.getString("ratingGiver")));
                                    params.put("ratingValue", Conversion.hexToAscii(jsonObj.getString("ratingValue")));
                                    params.put("experience", experienceType);
                                    params.put("comments", Conversion.hexToAscii(jsonObj.getString("comments")));
                                    params.put("ratingDate", ratingDate);
                                    params.put("ratingTime", time);
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

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    public void clearData() {
        comment.setText("");
        experience.clearCheck();
        rating.setRating(0F);
    }

}
