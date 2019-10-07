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
import org.json.*;
import java.util.*;
import Helper.Constant;

/*Author : Adelina Tang Chooi Li
Programme : RSD3
Year : 2017*/

public class FeedbackForm extends Fragment {

    View view;
    ImageView bugIcon, uiIcon, featureIcon;
    TextView bugText, uiText, featureText;
    EditText problemInput, improveInput;
    Button submit;
    String error = "", currentFeedbackID = "", feedbackID = "", url = "", feedbackType = "";
    ProgressDialog pDialog = null;

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
        getActivity().setTitle("Feedback Form");
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
        view = inflater.inflate(R.layout.feedbackform, container, false);
        pDialog = new ProgressDialog(getActivity());
        bugIcon = (ImageView) view.findViewById(R.id.bugIcon);
        bugText = (TextView) view.findViewById(R.id.bugText);
        uiIcon = (ImageView) view.findViewById(R.id.uiIcon);
        uiText = (TextView) view.findViewById(R.id.uiText);
        featureIcon = (ImageView) view.findViewById(R.id.featureIcon);
        featureText = (TextView) view.findViewById(R.id.featureText);
        problemInput = (EditText) view.findViewById(R.id.problemInput);
        improveInput = (EditText) view.findViewById(R.id.improveInput);
        submit = (Button) view.findViewById(R.id.submit);

        bugIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bugText.setVisibility(View.VISIBLE);
                uiText.setVisibility(View.INVISIBLE);
                featureText.setVisibility(View.INVISIBLE);
            }
        });

        uiIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uiText.setVisibility(View.VISIBLE);
                bugText.setVisibility(View.INVISIBLE);
                featureText.setVisibility(View.INVISIBLE);
            }
        });

        featureIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                featureText.setVisibility(View.VISIBLE);
                uiText.setVisibility(View.INVISIBLE);
                bugText.setVisibility(View.INVISIBLE);
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput() == true) {
                    getFeedbackID();
                }

            }
        });

        return view;
    }

    public void getFeedbackID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getFeedbackID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject ratingIDResponse = (JSONObject) response.get(i);
                                        currentFeedbackID = ratingIDResponse.getString("CurrentFeedbackID");
                                    }
                                    if (currentFeedbackID.equals("0")) {
                                        feedbackID = "FED0001";
                                    } else {
                                        feedbackID = String.format("FED%04d", (Integer.parseInt(currentFeedbackID.substring(3, 7)) + 1));
                                    }
                                    insertFeedbackData();
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

    public void insertFeedbackData() {

        if (bugText.getVisibility() == View.VISIBLE) {
            feedbackType = "Bug";
        }
        if (uiText.getVisibility() == View.VISIBLE) {
            feedbackType = "User Interface";
        }
        if (featureText.getVisibility() == View.VISIBLE) {
            feedbackType = "Feature";
        }

        url = Constant.serverFile + "insertFeedbackData.php?feedbackID=" + feedbackID + "&feedbackType=" + feedbackType
                + "&input=" + problemInput.getText().toString().trim() + "&suggestion=" + improveInput.getText().toString().trim();

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
                    params.put("feedbackID", feedbackID);
                    params.put("feedbackType", feedbackType);
                    params.put("input", problemInput.getText().toString().trim());
                    params.put("suggestion", improveInput.getText().toString().trim());
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

    public void clearData(){

        bugText.setVisibility(View.INVISIBLE);
        uiText.setVisibility(View.INVISIBLE);
        featureText.setVisibility(View.INVISIBLE);
        problemInput.setText("");
        improveInput.setText("");
    }


    public boolean validateInput() {

        error = "";
        boolean indicator = true;

        if(bugText.getVisibility() == View.INVISIBLE && uiText.getVisibility() == View.INVISIBLE
                && featureText.getVisibility() == View.INVISIBLE){
            error += "- Please select the type of feedback.\n";
            indicator = false;
        }

        if (problemInput.getText().toString().trim().equals("")) {
            error += "- Please state your feedback.\n";
            indicator = false;
        }
        if (improveInput.getText().toString().trim().equals("")) {
            error += "- Please state your suggestion.\n";
            indicator = false;
        }
        if (indicator == false) {
            AlertDialog.Builder sent = new AlertDialog.Builder(getActivity());
            sent.setTitle("Missing input");
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

}
