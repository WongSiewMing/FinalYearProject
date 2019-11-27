package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Helper.AppointmentOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;
import Helper.Stuff;


public class RequestDetail extends Fragment {
    private final static String TAG = "RequestDetail";
    private View view;

    private ImageView stuffImage;
    private ProgressBar progressBar;
    private RelativeLayout fragmentBody;
    private TextView stuffOwnerName, stuffName, stuffDescription, stuffPrice, stuffQuantity, stuffCategory, stuffCondition, stuffValidity;
    private TextView dayOfWeek, requestDate, fromTime, toTime;
    private Button btnCancel, btnAppointmentStatus;
    private AppointmentOB appointmentOB;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;
    private String connection = "", command = "", updateRequestUrl = "";
    private Stuff stuffDetail;
    private JSONObject myjsonObj;
    private ImageView btnChat;
    private FragmentManager fragmentManager;

    //Scan QRcode
    private SurfaceView cameraPreview;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private final int RequestCameraPermissionID = 1001;
    private Dialog qrDialog;
    private int barcodeCounter = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Appointment Detail");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_request_detail, container, false);
        Bundle bundle = getArguments();
        appointmentOB = (AppointmentOB) bundle.getSerializable("requestOB");
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");

        stuffOwnerName = view.findViewById(R.id.studentName);
        stuffImage = view.findViewById(R.id.stuffImage);
        progressBar = view.findViewById(R.id.progressBar);
        fragmentBody = view.findViewById(R.id.body);
        stuffName = view.findViewById(R.id.stuffName);
        stuffDescription = view.findViewById(R.id.stuffDescription);
        stuffPrice = view.findViewById(R.id.stuffPrice);
        stuffQuantity = view.findViewById(R.id.stuffQuantity);
        stuffCategory = view.findViewById(R.id.stuffCategory);
        stuffCondition = view.findViewById(R.id.stuffCondition);
        stuffValidity = view.findViewById(R.id.stuffValidity);
        dayOfWeek = view.findViewById(R.id.dayOfWeek);
        requestDate = view.findViewById(R.id.appointmentDate);
        fromTime = view.findViewById(R.id.startTime);
        toTime = view.findViewById(R.id.endTime);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnAppointmentStatus = view.findViewById(R.id.btnStatus);
        btnChat = view.findViewById(R.id.btnChat);
        btnCancel.setVisibility(View.GONE);
        btnAppointmentStatus.setVisibility(View.GONE);
        fragmentBody.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        btnAppointmentStatus.setEnabled(false);

        //QR Scanner
        qrDialog = new Dialog(getActivity());
        qrDialog.setContentView(R.layout.qrcode_scanner);
        cameraPreview = qrDialog.findViewById(R.id.cameraPreview);
        btnAppointmentStatus.setOnClickListener(new View.OnClickListener() {
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
                            btnAppointmentStatus.post(new Runnable() {
                                @Override
                                public void run() {
//                                    btnAppointmentStatus.setText(qrcodes.valueAt(0).displayValue);
//                                    qrDialog.dismiss();

                                    if (appointmentOB.getAppointmentID().equals(qrcodes.valueAt(0).displayValue)) {
                                        command = "{\"command\": \"303035303064\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                "\"appointmentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAppointmentID()) + "\" ," +
                                                "\"studentID\": " + "\"" + Conversion.asciiToHex(UpdateNavigation.student.getStudentID()) + "\" ," +
                                                "\"availableID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAvailableID().getAvailableID()) + "\" ," +
                                                "\"stuffID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getStuffID().getStuffID()) + "\" ," +
                                                "\"opponentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getOpponentID()) + "\" ," +
                                                "\"appointmentStatus\": " + "\"" + Conversion.asciiToHex("COMPLETED") + "\" ," +
                                                "\"appointmentDate\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAppointmentDate()) + "\" ," +
                                                "\"requesterRecord\": " + "\"" + Conversion.asciiToHex("ACTIVE") + "\" }";

                                        if (barcodeCounter == 1) {
                                            try {
                                                pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                                                qrDialog.dismiss();
                                                barcodeCounter = 2;
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }


                                    } else if (!appointmentOB.getAppointmentID().equals(qrcodes.valueAt(0).displayValue)) {
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
                                        builder.setTitle("ID not match!")
                                                .setMessage("Appointment ID not match.")
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

        getStuffDetail();

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                connection = "Connected";
                Log.d(TAG, "Connected");
                getStuffDetail();

            }

            @Override
            public void connectionLost(Throwable cause) {
                connection = "Disconnected";
                Log.d(TAG, "Disconnected");

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                myjsonObj = new JSONObject(mqttMessage.toString());
                int i = 0;
                if (myjsonObj.getString("command").equals("303035303064")) {
                    i = i + 1;

                    Log.d(TAG, "Message arrived " + i);
                    if (Conversion.hexToAscii(myjsonObj.getString("studentID")).equals(UpdateNavigation.student.getStudentID())) {
                        updateRequestDetail(myjsonObj);
                    }

                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connection.equals("Connected")) {
                    ChatRoom_V2 frag = new ChatRoom_V2();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("UserData", UpdateNavigation.student);//own data
                    bundle.putString("ClickedUserID", appointmentOB.getOpponentID());
                    frag.setArguments(bundle);
                    fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.update_fragmentHolder, frag)
                            .addToBackStack(null)
                            .commit();

                } else if (connection.equals("Disconnected")) {

                }
            }
        });


//        ChatRoom_V2 frag = new ChatRoom_V2();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("UserData", student);//own data
//        bundle.putString("ClickedUserID", mData.get(position).getRecipient2().getStudentID());
//        frag.setArguments(bundle);
//        fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.fragmentHolder, frag)
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                .addToBackStack(null)
//                .commit();

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    try {
                        cameraSource.start(cameraPreview.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void updateRequestDetail(final JSONObject myjsonObj) {
        try {
            updateRequestUrl = Constant.serverFile + "updateRequestInfo.php?appointmentID=" + Conversion.hexToAscii(myjsonObj.getString("appointmentID"))
                    + "&availableID=" + Conversion.hexToAscii(myjsonObj.getString("availableID"))
                    + "&stuffID=" + Conversion.hexToAscii(myjsonObj.getString("stuffID"))
                    + "&opponentID=" + Conversion.hexToAscii(myjsonObj.getString("opponentID"))
                    + "&appointmentStatus=" + Conversion.hexToAscii(myjsonObj.getString("appointmentStatus"))
                    + "&appointmentDate=" + Conversion.hexToAscii(myjsonObj.getString("appointmentDate"))
                    + "&requesterRecord=" + Conversion.hexToAscii(myjsonObj.getString("requesterRecord"));
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

                                        btnCancel.setVisibility(View.GONE);
                                        if (Conversion.hexToAscii(myjsonObj.getString("appointmentStatus")).equals("COMPLETED")) {
                                            btnAppointmentStatus.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                                            btnAppointmentStatus.setEnabled(false);
                                            command = "{\"command\": \"303035303065\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                    "\"studentID\": " + "\"" + Conversion.asciiToHex(UpdateNavigation.student.getStudentID()) + "\" ," +
                                                    "\"opponentID\": " + "\"" + myjsonObj.getString("opponentID") + "\" ," +
                                                    "\"appointmentStatus\": " + "\"" + Conversion.asciiToHex("COMPLETED") + "\" ," +
                                                    "\"appointmentID\": " + "\"" + myjsonObj.getString("appointmentID") + "\" }";
                                            try {
                                                pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        btnAppointmentStatus.setText(Conversion.hexToAscii(myjsonObj.getString("appointmentStatus")));
                                        btnAppointmentStatus.setVisibility(View.VISIBLE);

                                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        break;
                                                }
                                            }
                                        };

                                        builder.setTitle("Request is successfully updated")
                                                .setMessage("Request is updated.")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Request is failed to update");
                                        builder.setMessage("Request is not updated.");
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
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void getStuffDetail() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                command = "{\"command\": \"303035303061\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                        "\"studentID\": " + "\"" + Conversion.asciiToHex(UpdateNavigation.student.getStudentID()) + "\" ," +
                        "\"appointmentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAppointmentID()) + "\" ," +
                        "\"stuffID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getStuffID().getStuffID()) + "\" }";

                pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                //http://192.168.0.106/raindown/getStuffDetail.php?stuffID=STF0001
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStuffDetail.php?stuffID=" + appointmentOB.getStuffID().getStuffID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {


                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject stuffResponse = (JSONObject) response.get(i);
                                        stuffDetail = new Stuff(stuffResponse.getString("stuffID"),
                                                new Student(stuffResponse.getString("studentID"),
                                                        stuffResponse.getString("photo"),
                                                        stuffResponse.getString("studentName"),
                                                        stuffResponse.getString("icNo"),
                                                        stuffResponse.getString("studentProgramme"),
                                                        stuffResponse.getString("studentFaculty"),
                                                        stuffResponse.getInt("yearOfStudy")),
                                                stuffResponse.getString("stuffName"),
                                                stuffResponse.getString("stuffImage"),
                                                stuffResponse.getString("stuffDescription"),
                                                stuffResponse.getString("stuffCategory"),
                                                stuffResponse.getString("stuffCondition"),
                                                stuffResponse.getDouble("stuffPrice"),
                                                stuffResponse.getInt("stuffQuantity"),
                                                stuffResponse.getString("validStartDate"),
                                                stuffResponse.getString("validEndDate"),
                                                stuffResponse.getString("stuffStatus"));


                                    }
                                    Log.d(TAG, "Respond length = " + response.length());

                                    if (response.length() == 0) {
                                        progressBar.setVisibility(View.GONE);
                                        Log.d(TAG, "Respond length =" + response.length());
                                    } else {
                                        Log.d(TAG, "Respond length not empty");
                                        Log.d(TAG, "Stuff Name = " + stuffDetail.getStuffName());
                                        populateView(stuffDetail);
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
//                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
//                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
//            Toast.makeText(getActivity().getApplication(),
//                    "Error reading record:" + e.getMessage(),
//                    Toast.LENGTH_LONG).show();
        }
    }

    private void populateView(Stuff stuffDetail) {
        Picasso.with(getActivity()).load(stuffDetail.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(stuffImage);
        stuffOwnerName.setText(stuffDetail.getStudentID().getStudentName());
        stuffName.setText(stuffDetail.getStuffName());
        stuffDescription.setText(stuffDetail.getStuffDescription());
        stuffPrice.setText(String.format("%.2f", stuffDetail.getStuffPrice()));
        stuffQuantity.setText("" + stuffDetail.getStuffQuantity());
        stuffCategory.setText(stuffDetail.getStuffCategory());
        stuffCondition.setText(stuffDetail.getStuffCondition());
        stuffValidity.setText(stuffDetail.getValidStartDate() + " - " + stuffDetail.getValidEndDate());
        dayOfWeek.setText(appointmentOB.getAvailableID().getAvailableDate());
        requestDate.setText(appointmentOB.getAppointmentDate());
        fromTime.setText(appointmentOB.getAvailableID().getStartTime());
        toTime.setText(appointmentOB.getAvailableID().getEndTime());

        if (appointmentOB.getAppointmentStatus().equals("PENDING")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HHmm");
            Date c = Calendar.getInstance().getTime();
            String currentDate = sdf.format(c);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HHmm");
            Date cDate = new Date();
            Date tDate = new Date();
            try {
                cDate = dateFormat.parse(currentDate);
                tDate = dateFormat.parse(appointmentOB.getAppointmentDate() + " " + appointmentOB.getAvailableID().getEndTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (tDate.before(cDate)) {
                btnCancel.setVisibility(View.GONE);

                btnAppointmentStatus.setText("EXPIRED");
                btnAppointmentStatus.setVisibility(View.VISIBLE);
            } else {
                btnCancel.setVisibility(View.VISIBLE);
                btnAppointmentStatus.setVisibility(View.GONE);
            }

        } else if (appointmentOB.getAppointmentStatus().equals("ACCEPTED")) {
            btnCancel.setVisibility(View.GONE);
            btnAppointmentStatus.setText(appointmentOB.getAppointmentStatus());
            btnAppointmentStatus.setVisibility(View.VISIBLE);
            btnAppointmentStatus.setEnabled(true);

        } else {
            btnCancel.setVisibility(View.GONE);
            btnAppointmentStatus.setText(appointmentOB.getAppointmentStatus());
            btnAppointmentStatus.setVisibility(View.VISIBLE);
            btnAppointmentStatus.setEnabled(false);
        }

        progressBar.setVisibility(View.GONE);
        fragmentBody.setVisibility(View.VISIBLE);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connection.equals("Connected")) {
                    command = "{\"command\": \"303035303064\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"appointmentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAppointmentID()) + "\" ," +
                            "\"studentID\": " + "\"" + Conversion.asciiToHex(UpdateNavigation.student.getStudentID()) + "\" ," +
                            "\"availableID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAvailableID().getAvailableID()) + "\" ," +
                            "\"stuffID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getStuffID().getStuffID()) + "\" ," +
                            "\"opponentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getOpponentID()) + "\" ," +
                            "\"appointmentStatus\": " + "\"" + Conversion.asciiToHex("CANCELED") + "\" ," +
                            "\"appointmentDate\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAppointmentDate()) + "\" ," +
                            "\"requesterRecord\": " + "\"" + Conversion.asciiToHex("ACTIVE") + "\" }";

                    try {
                        pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (connection.equals("Disconnected")) {
                    Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
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


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
