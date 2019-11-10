package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;
import Helper.Stuff;

public class TradeConfirm extends Fragment {

    View view;
    private Student myID;
    private Stuff requestStuff = null;
    private Stuff offerStuff = null;
    private ImageView sellerPhoto, requestItem, offerItem;
    private TextView sellerName, sellerFaculty, sellerProgramme, sellerYear;
    private TextView tradeSellerName, requestItemName, requestItemPrice, offerItemName, offerItemPrice;
    private FragmentManager fragmentManager;
    private Button btnConfirm, btnCancel;
    private String command, formattedDate, formattedTime, currentTradeID = "", tradeID = "", jsonURL = "";
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private Date date;
    private SimpleDateFormat dateFormat;
    private JSONObject jsonObj;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        myID = (Student) bundle.getSerializable("MyInfo");
        requestStuff = (Stuff) bundle.getSerializable("RequestStuff");
        offerStuff = (Stuff) bundle.getSerializable("OfferStuff");
        getActivity().setTitle("Confirm Trade");
    }

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
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.trade_confirm, container, false);
        btnConfirm = view.findViewById(R.id.button_confirm);
        btnCancel = view.findViewById(R.id.button_cancel);
        sellerPhoto = view.findViewById(R.id.sellerPhoto_confirm);
        sellerName = view.findViewById(R.id.sellerName_confirm);
        sellerFaculty = view.findViewById(R.id.sellerFaculty_confirm);
        sellerProgramme = view.findViewById(R.id.sellerProgramme_confirm);
        sellerYear = view.findViewById(R.id.sellerYear_confirm);

        offerItem = view.findViewById(R.id.stuffImage_confirm);
        offerItemName = view.findViewById(R.id.stuffName_confirm);
        offerItemPrice = view.findViewById(R.id.stuffPrice_confirm);

        tradeSellerName = view.findViewById(R.id.tradeSellerName);
        requestItem = view.findViewById(R.id.requestStuffImage_confirm);
        requestItemName = view.findViewById(R.id.requestStuffName_confirm);
        requestItemPrice = view.findViewById(R.id.requestStuffPrice_confirm);

        Picasso.with(getActivity()).load(requestStuff.getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(sellerPhoto);
        Picasso.with(getActivity()).load(offerStuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(offerItem);
        Picasso.with(getActivity()).load(requestStuff.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(requestItem);
        sellerName.setText(requestStuff.getStudentID().getStudentName());
        sellerFaculty.setText(requestStuff.getStudentID().getStudentFaculty());
        sellerProgramme.setText(requestStuff.getStudentID().getStudentProgramme());
        sellerYear.setText("Year " + requestStuff.getStudentID().getYearOfStudy());

        offerItemName.setText(offerStuff.getStuffName());
        offerItemPrice.setText(String.format("RM %.2f", offerStuff.getStuffPrice()));

        tradeSellerName.setText("For " + requestStuff.getStudentID().getStudentName() + " :");
        requestItemName.setText(requestStuff.getStuffName());
        requestItemPrice.setText(String.format("RM %.2f", requestStuff.getStuffPrice()));

        btnCancel.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                builder.setMessage("Cancel trade ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Home home = new Home();
                                Bundle bundle1 = new Bundle();
                                bundle1.putSerializable("Home", myID);
                                home.setArguments(bundle1);
                                fragmentManager = getFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.update_fragmentHolder, home)
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                        .commit();
                            }
                        })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                date = Calendar.getInstance().getTime();
                                dateFormat = new SimpleDateFormat("d MMM yyyy");
                                formattedDate = dateFormat.format(date);
                                dateFormat = new SimpleDateFormat("h:mm a");
                                formattedTime = dateFormat.format(date);

                                try {
                                    command = "{\"command\": \"303035303072\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                            "\"User_StuffID\": " + "\"" + Conversion.asciiToHex(offerStuff.getStuffID()) + "\" ," +
                                            "\"Request_StuffID\": " + "\"" + Conversion.asciiToHex(requestStuff.getStuffID()) + "\" ," +
                                            "\"StudentID\": " + "\"" + Conversion.asciiToHex(myID.getStudentID()) + "\" ," +
                                            "\"SellerID\": " + "\"" + Conversion.asciiToHex(requestStuff.getStudentID().getStudentID()) + "\" ," +
                                            "\"TradeDate\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                                            "\"TradeTime\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\"}";

                                    pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                getTradeID();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                builder.setMessage("Confirm sent trade ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }
        });

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl);

        return view;
    }

    public void getTradeID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getTradeID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject tradeIDResponse = (JSONObject) response.get(i);
                                        currentTradeID = tradeIDResponse.getString("CurrentTradeID");
                                    }
                                    if (currentTradeID.equals("0")) {
                                        tradeID = "TRD0001";
                                    } else {
                                        tradeID = String.format("TRD%04d", (Integer.parseInt(currentTradeID.substring(3, 7)) + 1));
                                    }
                                    insertTradeData();

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

    public void insertTradeData() {
        try {
            jsonObj = new JSONObject(command);
            if(jsonObj.getString("command").equals("303035303072")){

                jsonURL = Constant.serverFile + "insertTradeData.php?TradeID=" + tradeID
                        + "&User_StuffID=" + Conversion.hexToAscii(jsonObj.getString("User_StuffID"))
                        + "&Request_StuffID=" + Conversion.hexToAscii(jsonObj.getString("Request_StuffID"))
                        + "&StudentID=" + Conversion.hexToAscii(jsonObj.getString("StudentID"))
                        + "&SellerID=" + Conversion.hexToAscii(jsonObj.getString("SellerID"))
                        + "&TradeStatus=Pending"
                        + "&TradeDate=" + Conversion.hexToAscii(jsonObj.getString("TradeDate"))
                        + "&TradeTime=" + Conversion.hexToAscii(jsonObj.getString("TradeTime"));

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
                                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                                        if (success.equals("1")) {
                                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            Home home = new Home();
                                                            Bundle bundle1 = new Bundle();
                                                            bundle1.putSerializable("Home", myID);
                                                            home.setArguments(bundle1);
                                                            fragmentManager = getFragmentManager();
                                                            fragmentManager.beginTransaction()
                                                                    .replace(R.id.update_fragmentHolder, home)
                                                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                                                    .commit();

                                                            break;
                                                    }
                                                }
                                            };

                                            builder.setTitle("Trade Request is successfully sent")
                                                    .setMessage("Trade request had been sent to " +requestStuff.getStudentID().getStudentName())
                                                    .setPositiveButton("OK", dialogClickListener).show();
                                        } else {
                                            builder.setTitle("Failed to sent Trade");
                                            builder.setMessage("Trade request is failed to sent to " +requestStuff.getStudentID().getStudentName());
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
                                params.put("TradeID", tradeID);
                                params.put("User_StuffID", Conversion.hexToAscii(jsonObj.getString("User_StuffID")));
                                params.put("Request_StuffID", Conversion.hexToAscii(jsonObj.getString("Request_StuffID")));
                                params.put("StudentID", Conversion.hexToAscii(jsonObj.getString("StudentID")));
                                params.put("SellerID", Conversion.hexToAscii(jsonObj.getString("SellerID")));
                                params.put("TradeStatus", "Pending");
                                params.put("TradeDate", Conversion.hexToAscii(jsonObj.getString("TradeDate")));
                                params.put("TradeTime", Conversion.hexToAscii(jsonObj.getString("TradeTime")));
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
