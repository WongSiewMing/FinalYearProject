package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;
import Helper.Trade;

public class TradeRequestDetail extends Fragment {

    private View view;
    private Trade tradeRequest = null;
    private Student myInfo = null;
    private ImageView requesterAvatar, offerStuff, requestStuff;
    private TextView requesterName, requesterFaculty, requesterProgramme, requesterYear, tradeRequesterName, requestStuffName, requestStuffPrice, offerStuffName, offerStuffPrice;
    private Button btnAccept, btnReject, btnChat, btnQR;
    private FragmentManager fragmentManager;
    private String tradeStatus, command, jsonURL;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private JSONObject jsonObj;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        myInfo = (Student) bundle.getSerializable("StudentClickedTrade");
        tradeRequest = (Trade) bundle.getSerializable("ClickedTradeRequest");
        getActivity().setTitle("Trade Request Detail");
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

        view = inflater.inflate(R.layout.trade_request_detail, container, false);
        requesterAvatar = view.findViewById(R.id.requesterAvatar);
        requesterName = view.findViewById(R.id.requester_name);
        requesterFaculty = view.findViewById(R.id.requester_faculty);
        requesterProgramme = view.findViewById(R.id.requester_programme);
        requesterYear = view.findViewById(R.id.requester_year);
        tradeRequesterName = view.findViewById(R.id.requesterOfferItem);
        offerStuff = view.findViewById(R.id.offer_stuffImage_detail);
        offerStuffName = view.findViewById(R.id.offer_stuffName_detail);
        offerStuffPrice = view.findViewById(R.id.offer_stuffPrice_detail);
        requestStuff = view.findViewById(R.id.requestStuffImage_detail);
        requestStuffName = view.findViewById(R.id.requestStuffName_detail);
        requestStuffPrice = view.findViewById(R.id.request_detail_price);
        btnAccept = view.findViewById(R.id.button_accept);
        btnReject = view.findViewById(R.id.button_reject);
        btnChat = view.findViewById(R.id.chat_with_requester);
        btnQR = view.findViewById(R.id.button_generate_qr_code);

        Picasso.with(getActivity()).load(tradeRequest.getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(requesterAvatar);
        Picasso.with(getActivity()).load(tradeRequest.getUserStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(offerStuff);
        Picasso.with(getActivity()).load(tradeRequest.getRequestStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(requestStuff);

        requesterName.setText(tradeRequest.getStudentID().getStudentName());
        requesterFaculty.setText(tradeRequest.getStudentID().getStudentFaculty());
        requesterProgramme.setText(tradeRequest.getStudentID().getStudentProgramme());
        requesterYear.setText("Year " + tradeRequest.getStudentID().getYearOfStudy());

        offerStuffName.setText(tradeRequest.getUserStuffID().getStuffName());
        offerStuffPrice.setText(String.format("RM %.2f", tradeRequest.getUserStuffID().getStuffPrice()));

        requestStuffName.setText(tradeRequest.getRequestStuffID().getStuffName());
        requestStuffPrice.setText(String.format("RM %.2f", tradeRequest.getRequestStuffID().getStuffPrice()));

        tradeRequesterName.setText("Item Offered By " + tradeRequest.getStudentID().getStudentName() + " :");

        if (tradeRequest.getTradeStatus().equals("Accepted")){
            btnQR.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.GONE);
            btnAccept.setVisibility(View.GONE);
        }

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    ChatRoom_V2 frag = new ChatRoom_V2();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("UserData", myInfo);//own data
                    bundle.putString("ClickedUserID", tradeRequest.getStudentID().getStudentID());
                    frag.setArguments(bundle);
                    fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .addToBackStack(null)
                            .commit();

            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:

                                command = "{\"command\": \"303035303073\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                        "\"TradeID\": " + "\"" + Conversion.asciiToHex(tradeRequest.getTradeID()) + "\"}";

                                pahoMqttClient = new PahoMqttClient();
                                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                updateTradeStatus();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Accept Trade Request ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:

                                command = "{\"command\": \"303035303074\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                        "\"TradeID\": " + "\"" + Conversion.asciiToHex(tradeRequest.getTradeID()) + "\"}";

                                pahoMqttClient = new PahoMqttClient();
                                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                updateTradeStatus();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Reject Trade Request ?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        return view;
    }

    public void updateTradeStatus() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                try {
                    jsonObj = new JSONObject(command);
                    if(jsonObj.getString("command").equals("303035303073")){
                        tradeStatus = "Accepted";
                    }else if (jsonObj.getString("command").equals("303035303074")) {
                        tradeStatus = "Rejected";
                    }

                    jsonURL = Constant.serverFile + "updateTradeStatus.php?TradeID=" + Conversion.hexToAscii(jsonObj.getString("TradeID")) + "&TradeStatus=" + tradeStatus;

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
                                                                if(tradeStatus.equals("Accepted")){
                                                                    btnQR.setVisibility(View.VISIBLE);
                                                                    btnReject.setVisibility(View.GONE);
                                                                    btnAccept.setVisibility(View.GONE);
                                                                } else {
                                                                    ManageTradeRequest manageTradeRequest = new ManageTradeRequest();
                                                                    Bundle bundle = new Bundle();
                                                                    bundle.putSerializable("manageTradeRequest", myInfo);
                                                                    manageTradeRequest.setArguments(bundle);
                                                                    fragmentManager = getFragmentManager();
                                                                    fragmentManager.beginTransaction()
                                                                            .replace(R.id.update_fragmentHolder, manageTradeRequest)
                                                                            .addToBackStack(null)
                                                                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                                                            .commit();
                                                                }

                                                                break;
                                                        }
                                                    }
                                                };

                                                if (tradeStatus.equals("Accepted")){
                                                    builder.setTitle("Trade Request accepted")
                                                            .setMessage("Trade request from " + tradeRequest.getStudentID().getStudentName() + " had been accepted.")
                                                            .setPositiveButton("OK", dialogClickListener).show();
                                                } else {
                                                    builder.setTitle("Trade Request Rejected")
                                                            .setMessage("Trade request from " + tradeRequest.getStudentID().getStudentName() + " had been rejected.")
                                                            .setPositiveButton("OK", dialogClickListener).show();
                                                }

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
                                    params.put("TradeID", Conversion.hexToAscii(jsonObj.getString("TradeID")));
                                    params.put("TradeStatus", tradeStatus);
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
}
