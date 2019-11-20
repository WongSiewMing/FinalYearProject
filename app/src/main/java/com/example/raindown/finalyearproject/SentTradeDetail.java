package com.example.raindown.finalyearproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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
import Helper.Trade;

public class SentTradeDetail extends Fragment {

    private View view;
    private Trade sentTrade = null;
    private Student myInfo = null;
    private ImageView sellerAvatar, offerStuff, requestStuff;
    private TextView sellerName, sellerFaculty, sellerProgramme, sellerYear, tradeSellerName, requestStuffName, requestStuffPrice, offerStuffName, offerStuffPrice;
    private Button btnStatus, btnCancel, btnChat;
    private FragmentManager fragmentManager;
    private String command, insertCommand, insertCommand2, jsonURL, tradeStatus, formattedDate, formattedTime, currentTradeHistoryID = "", tradeHistoryID = "";
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private JSONObject jsonObj;
    private Date date;
    private SimpleDateFormat dateFormat;

    private SurfaceView cameraPreview;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private final int RequestCameraPermissionID = 1001;
    private Dialog qrDialog;
    private int barcodeCounter = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        myInfo = (Student) bundle.getSerializable("StudentClickedTrade");
        sentTrade = (Trade) bundle.getSerializable("ClickedSentTrade");
        getActivity().setTitle("Sent Trade Detail");
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

        view = inflater.inflate(R.layout.sent_trade_detail, container, false);
        sellerAvatar = view.findViewById(R.id.sellerAvatar);
        sellerName = view.findViewById(R.id.sellerName_sent);
        sellerFaculty = view.findViewById(R.id.sellerFaculty_sent);
        sellerProgramme = view.findViewById(R.id.sellerProgramme_sent);
        sellerYear = view.findViewById(R.id.sellerYear_sent);
        offerStuff = view.findViewById(R.id.stuffImage_sent);
        offerStuffName = view.findViewById(R.id.stuffName_sent);
        offerStuffPrice = view.findViewById(R.id.stuffPrice_sent);
        tradeSellerName = view.findViewById(R.id.sentTradeSellerName);
        requestStuff = view.findViewById(R.id.requestStuffImage_sent);
        requestStuffName = view.findViewById(R.id.requestStuffName_sent);
        requestStuffPrice = view.findViewById(R.id.requestStuffPrice_sent);
        btnStatus = view.findViewById(R.id.button_status);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnChat = view.findViewById(R.id.chat_with_seller);

        Picasso.with(getActivity()).load(sentTrade.getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(sellerAvatar);
        Picasso.with(getActivity()).load(sentTrade.getUserStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(offerStuff);
        Picasso.with(getActivity()).load(sentTrade.getRequestStuffID().getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(requestStuff);

        sellerName.setText(sentTrade.getStudentID().getStudentName());
        sellerFaculty.setText(sentTrade.getStudentID().getStudentFaculty());
        sellerProgramme.setText(sentTrade.getStudentID().getStudentProgramme());
        sellerYear.setText("Year " + sentTrade.getStudentID().getYearOfStudy());

        offerStuffName.setText(sentTrade.getUserStuffID().getStuffName());
        offerStuffPrice.setText(String.format("RM %.2f", sentTrade.getUserStuffID().getStuffPrice()));

        requestStuffName.setText(sentTrade.getRequestStuffID().getStuffName());
        requestStuffPrice.setText(String.format("RM %.2f", sentTrade.getRequestStuffID().getStuffPrice()));

        tradeSellerName.setText("For " + sentTrade.getStudentID().getStudentName() + " :");

        qrDialog = new Dialog(getActivity());
        qrDialog.setContentView(R.layout.qrcode_scanner);
        cameraPreview = qrDialog.findViewById(R.id.cameraPreview);

        if (sentTrade.getTradeStatus().equals("Accepted")){
            btnStatus.setText("Scan QR Code");
            btnStatus.setBackgroundColor(getResources().getColor(R.color.brightgreeen));

            btnStatus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    barcodeDetector = new BarcodeDetector.Builder(getActivity())
                            .setBarcodeFormats(Barcode.QR_CODE)
                            .build();
                    cameraSource = new CameraSource
                            .Builder(getActivity(), barcodeDetector)
                            .setRequestedPreviewSize(640, 480)
                            .build();

                    cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder holder) {
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);
                                return;
                            }
                            try {
                                cameraSource.start(cameraPreview.getHolder());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder holder) {
                            cameraSource.stop();

                        }
                    });

                    barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                        @Override
                        public void release() {

                        }

                        @Override
                        public void receiveDetections(Detector.Detections<Barcode> detections) {
                            final SparseArray<Barcode> qrcodes = detections.getDetectedItems();
                            if (qrcodes.size() != 0) {
                                btnStatus.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        date = Calendar.getInstance().getTime();
                                        dateFormat = new SimpleDateFormat("d MMM yyyy");
                                        formattedDate = dateFormat.format(date);
                                        dateFormat = new SimpleDateFormat("h:mm a");
                                        formattedTime = dateFormat.format(date);

                                        if (sentTrade.getTradeID().equals(qrcodes.valueAt(0).displayValue)) {
                                            command = "{\"command\": \"303035303078\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                    "\"TradeID\": " + "\"" + Conversion.asciiToHex(sentTrade.getTradeID()) + "\"}";

                                            insertCommand = "{\"command\": \"30303530307F\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                    "\"StudentID\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentID()) + "\" ," +
                                                    "\"Date\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                                                    "\"Time\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\" ," +
                                                    "\"TradeID\": " + "\"" + Conversion.asciiToHex(sentTrade.getTradeID()) + "\"}";

                                            insertCommand2 = "{\"command\": \"30303530307F\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                    "\"StudentID\": " + "\"" + Conversion.asciiToHex(sentTrade.getSellerID().getStudentID()) + "\" ," +
                                                    "\"Date\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                                                    "\"Time\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\" ," +
                                                    "\"TradeID\": " + "\"" + Conversion.asciiToHex(sentTrade.getTradeID()) + "\"}";

                                            if (barcodeCounter == 1) {
                                                try {
                                                    pahoMqttClient = new PahoMqttClient();
                                                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");
                                                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, insertCommand, "MY/TARUC/SSS/000000001/PUB");
                                                    mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, insertCommand2, "MY/TARUC/SSS/000000001/PUB");
                                                    qrDialog.dismiss();
                                                    getTradeHistoryID();
                                                    barcodeCounter = 2;
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }


                                        } else if (!sentTrade.getTradeID().equals(qrcodes.valueAt(0).displayValue)) {
                                            qrDialog.dismiss();
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            break;
                                                    }
                                                }
                                            };
                                            builder.setTitle("Invalid QR Code")
                                                    .setMessage("Invalid QR code, please ensure you scan the correct QR code.")
                                                    .setPositiveButton("OK", dialogClickListener)
                                                    .show();
                                        }

                                    }
                                });

                            }

                        }
                    });
                    qrDialog.show();

                }
            });
        }

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ChatRoom_V2 frag = new ChatRoom_V2();
                Bundle bundle = new Bundle();
                bundle.putSerializable("UserData", myInfo);//own data
                bundle.putString("ClickedUserID", sentTrade.getSellerID().getStudentID());
                frag.setArguments(bundle);
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .addToBackStack(null)
                        .commit();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                                command = "{\"command\": \"303035303075\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                        "\"TradeID\": " + "\"" + Conversion.asciiToHex(sentTrade.getTradeID()) + "\"}";

                                pahoMqttClient = new PahoMqttClient();
                                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, command, "MY/TARUC/SSS/000000001/PUB");

                                insertCommand = "{\"command\": \"30303530307F\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                        "\"StudentID\": " + "\"" + Conversion.asciiToHex(myInfo.getStudentID()) + "\" ," +
                                        "\"Date\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                                        "\"Time\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\" ," +
                                        "\"TradeID\": " + "\"" + Conversion.asciiToHex(sentTrade.getTradeID()) + "\"}";

                                pahoMqttClient = new PahoMqttClient();
                                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, insertCommand, "MY/TARUC/SSS/000000001/PUB");

                                insertCommand2 = "{\"command\": \"30303530307F\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                        "\"StudentID\": " + "\"" + Conversion.asciiToHex(sentTrade.getSellerID().getStudentID()) + "\" ," +
                                        "\"Date\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                                        "\"Time\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\" ," +
                                        "\"TradeID\": " + "\"" + Conversion.asciiToHex(sentTrade.getTradeID()) + "\"}";

                                pahoMqttClient = new PahoMqttClient();
                                mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, insertCommand2, "MY/TARUC/SSS/000000001/PUB");

                                getTradeHistoryID();

                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                if (sentTrade.getTradeStatus().equals("Accepted")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setTitle("Error");
                    builder.setMessage("Cannot cancel an on going trading !");
                    builder.setNegativeButton("OK", dialogClickListener);
                    builder.show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Cancel Trade Request ?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }

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

                    if(jsonObj.getString("command").equals("303035303075")) {
                        tradeStatus = "Canceled";
                    } else if (jsonObj.getString("command").equals("303035303078")) {
                        tradeStatus = "Completed";
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
                                                                ManageSentTrade manageSentTrade = new ManageSentTrade();
                                                                Bundle bundle = new Bundle();
                                                                bundle.putSerializable("manageSentTrade", myInfo);
                                                                manageSentTrade.setArguments(bundle);
                                                                fragmentManager = getFragmentManager();
                                                                fragmentManager.beginTransaction()
                                                                        .replace(R.id.update_fragmentHolder, manageSentTrade)
                                                                        .addToBackStack(null)
                                                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                                                        .commit();

                                                                break;
                                                        }
                                                    }
                                                };


                                                if(jsonObj.getString("command").equals("303035303075")) {
                                                    builder.setTitle("Trade Request Cancel")
                                                            .setMessage("Trade request to " + sentTrade.getStudentID().getStudentName() + " had been canceled.")
                                                            .setPositiveButton("OK", dialogClickListener).show();
                                                } else if (jsonObj.getString("command").equals("303035303078")) {
                                                    builder.setTitle("Trade Complete")
                                                            .setMessage("Trade with " + sentTrade.getStudentID().getStudentName() + " is completed.")
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

    public void getTradeHistoryID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "generateTradeHistoryID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject tradeHistoryIDResponse = (JSONObject) response.get(i);
                                        currentTradeHistoryID = tradeHistoryIDResponse.getString("CurrentTradeHistoryID");
                                    }
                                    if (currentTradeHistoryID.equals("0")) {
                                        tradeHistoryID = "TH0001";
                                    } else {
                                        tradeHistoryID = String.format("TH%04d", (Integer.parseInt(currentTradeHistoryID.substring(3, 6)) + 1));
                                    }
                                    insertTradeHistory();

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

    public void getAnotherTradeHistoryID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "generateTradeHistoryID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject tradeHistoryIDResponse = (JSONObject) response.get(i);
                                        currentTradeHistoryID = tradeHistoryIDResponse.getString("CurrentTradeHistoryID");
                                    }
                                    if (currentTradeHistoryID.equals("0")) {
                                        tradeHistoryID = "TH0001";
                                    } else {
                                        tradeHistoryID = String.format("TH%04d", (Integer.parseInt(currentTradeHistoryID.substring(3, 6)) + 1));
                                    }
                                    insertAnotherTradeHistory();

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

    public void insertTradeHistory() {
        try {
            jsonObj = new JSONObject(insertCommand);
            if(jsonObj.getString("command").equals("30303530307F")){

                jsonURL = Constant.serverFile + "insertTradeHistory.php?TradeHistoryID=" + tradeHistoryID
                        + "&StudentID=" + Conversion.hexToAscii(jsonObj.getString("StudentID"))
                        + "&Date=" + Conversion.hexToAscii(jsonObj.getString("Date"))
                        + "&Time=" + Conversion.hexToAscii(jsonObj.getString("Time"))
                        + "&TradeID=" + Conversion.hexToAscii(jsonObj.getString("TradeID"))
                        + "&status=Show";

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
                                        if (success.equals("1")) {
                                            getAnotherTradeHistoryID();
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
                                params.put("TradeHistoryID", tradeHistoryID);
                                params.put("StudentID", Conversion.hexToAscii(jsonObj.getString("StudentID")));
                                params.put("Date", Conversion.hexToAscii(jsonObj.getString("Date")));
                                params.put("Time", Conversion.hexToAscii(jsonObj.getString("Time")));
                                params.put("TradeID", Conversion.hexToAscii(jsonObj.getString("TradeID")));
                                params.put("status", "Show");
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

    public void insertAnotherTradeHistory() {
        try {
            jsonObj = new JSONObject(insertCommand2);
            if(jsonObj.getString("command").equals("30303530307F")){

                jsonURL = Constant.serverFile + "insertTradeHistory.php?TradeHistoryID=" + tradeHistoryID
                        + "&StudentID=" + Conversion.hexToAscii(jsonObj.getString("StudentID"))
                        + "&Date=" + Conversion.hexToAscii(jsonObj.getString("Date"))
                        + "&Time=" + Conversion.hexToAscii(jsonObj.getString("Time"))
                        + "&TradeID=" + Conversion.hexToAscii(jsonObj.getString("TradeID"))
                        + "&status=Show";

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
                                        if (success.equals("1")) {
                                            updateTradeStatus();
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
                                params.put("TradeHistoryID", tradeHistoryID);
                                params.put("StudentID", Conversion.hexToAscii(jsonObj.getString("StudentID")));
                                params.put("Date", Conversion.hexToAscii(jsonObj.getString("Date")));
                                params.put("Time", Conversion.hexToAscii(jsonObj.getString("Time")));
                                params.put("TradeID", Conversion.hexToAscii(jsonObj.getString("TradeID")));
                                params.put("status", "Show");
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
