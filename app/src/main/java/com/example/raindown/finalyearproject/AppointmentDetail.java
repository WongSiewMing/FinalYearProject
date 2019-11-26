package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.google.zxing.WriterException;
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
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


public class AppointmentDetail extends Fragment {

    private final static String TAG = "Appointment Detail";
    private View view;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command="", connection= "", updateAppointmentDetailUrl="";
    private JSONObject myjsonObj;

    private AppointmentOB appointmentOB;
    private ImageView btnBack, stuffImage, requesterPhoto;
    private ProgressBar progressBar;
    private RelativeLayout fragmentBody;
    private TextView stuffName, stuffDescription, stuffPrice, stuffQuantity, stuffCategory, stuffCondition, stuffValidity, requesterName;
    private TextView dayOfWeek, requestDate, fromTime, toTime;
    private Button btnAccept, btnReject, btnAppointmentStatus;
    private Stuff stuffDetail;
    private ImageView btnChat;
    private FragmentManager fragmentManager;

    //testing
    private QRGEncoder qrgEncoder;
    private Dialog qrDialog;
    private ImageView qrImage;
    private Bitmap bitmap;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_appointment_detail, container, false);
        Bundle bundle = getArguments();
        appointmentOB = (AppointmentOB) bundle.getSerializable("appointmentOB");
        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");
        btnBack = view.findViewById(R.id.btnBack);
        stuffImage = view.findViewById(R.id.stuffImage);
        requesterPhoto = view.findViewById(R.id.requesterPhoto);
        progressBar = view.findViewById(R.id.progressBar);
        fragmentBody = view.findViewById(R.id.body);
        stuffName = view.findViewById(R.id.stuffName);
        stuffDescription = view.findViewById(R.id.stuffDescription);
        stuffPrice = view.findViewById(R.id.stuffPrice);
        stuffQuantity = view.findViewById(R.id.stuffQuantity);
        stuffCategory = view.findViewById(R.id.stuffCategory);
        stuffCondition = view.findViewById(R.id.stuffCondition);
        stuffValidity = view.findViewById(R.id.stuffValidity);
        requesterName = view.findViewById(R.id.requesterName);
        dayOfWeek = view.findViewById(R.id.dayOfWeek);
        requestDate = view.findViewById(R.id.appointmentDate);
        fromTime = view.findViewById(R.id.startTime);
        toTime = view.findViewById(R.id.endTime);
        btnAccept = view.findViewById(R.id.btnAccept);
        btnReject = view.findViewById(R.id.btnReject);
        btnAppointmentStatus = view.findViewById(R.id.btnStatus);
        btnChat = view.findViewById(R.id.btnChat);
        btnAccept.setVisibility(View.GONE);
        btnReject.setVisibility(View.GONE);
        btnAppointmentStatus.setVisibility(View.GONE);
        fragmentBody.setVisibility(View.GONE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });
        progressBar.setVisibility(View.VISIBLE);
        btnAppointmentStatus.setClickable(false);
        btnAppointmentStatus.setFocusable(false);
        btnAppointmentStatus.setEnabled(false);

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                connection = "Connected";
                Log.d(TAG, "Connected");
                progressBar.setVisibility(View.VISIBLE);
                getStuffDetail();
            }

            @Override
            public void connectionLost(Throwable cause) {
                connection = "Disconnected";
                Log.d(TAG, "Disconnected");

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
               // updateAppointmentDetail();
                Log.d(TAG, "Message arrived!!!");

                myjsonObj = new JSONObject(mqttMessage.toString());
                if (myjsonObj.getString("command").equals("303035303062")){
                    if (Conversion.hexToAscii(myjsonObj.getString("opponentID")).equals(UpdateNavigation.student.getStudentID())){
                        updateAppointmentDetail(myjsonObj);
                    }

                }else if (myjsonObj.getString("command").equals("303035303065")){
                    Log.d(TAG, "opponentID =" + Conversion.hexToAscii(myjsonObj.getString("opponentID")));
                    Log.d(TAG, "appointment ID =" + Conversion.hexToAscii(myjsonObj.getString("appointmentID")));

                    if (Conversion.hexToAscii(myjsonObj.getString("opponentID")).equals(UpdateNavigation.student.getStudentID()) && Conversion.hexToAscii(myjsonObj.getString("appointmentID")).equals(appointmentOB.getAppointmentID())){
                        btnAppointmentStatus.setText(Conversion.hexToAscii(myjsonObj.getString("appointmentStatus")));
                        btnAppointmentStatus.setBackgroundColor(getResources().getColor(R.color.lightgreen));
                        btnAppointmentStatus.setEnabled(false);
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
                        builder.setTitle("Trading complete")
                                .setMessage("Your trading process is completed")
                                .setPositiveButton("OK", dialogClickListener)
                                .show();

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
                    bundle.putString("ClickedUserID", appointmentOB.getStudentID().getStudentID());
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

        return view;
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
                                        Log.d(TAG,"Respond length not empty");
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
                Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void populateView(Stuff stuffDetail) {
        Picasso.with(getActivity()).load(stuffDetail.getStuffImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(stuffImage);
        stuffName.setText(stuffDetail.getStuffName());
        stuffDescription.setText(stuffDetail.getStuffDescription());
        stuffPrice.setText(String.format("%.2f", stuffDetail.getStuffPrice()));
        stuffQuantity.setText("" + stuffDetail.getStuffQuantity());
        stuffCategory.setText(stuffDetail.getStuffCategory());
        stuffCondition.setText(stuffDetail.getStuffCondition());
        stuffValidity.setText(stuffDetail.getValidStartDate() + " - " + stuffDetail.getValidEndDate());
        Picasso.with(getActivity()).load(appointmentOB.getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(requesterPhoto);
        requesterName.setText(appointmentOB.getStudentID().getStudentName());
        dayOfWeek.setText(appointmentOB.getAvailableID().getAvailableDate());
        requestDate.setText(appointmentOB.getAppointmentDate());
        fromTime.setText(appointmentOB.getAvailableID().getStartTime());
        toTime.setText(appointmentOB.getAvailableID().getEndTime());

        if (appointmentOB.getAppointmentStatus().equals("PENDING")){
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HHmm");
            Date c = Calendar.getInstance().getTime();
            String currentDate = sdf.format(c);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HHmm");
            Date cDate = new Date();
            Date tDate = new Date();
            try {
                cDate = dateFormat.parse(currentDate);
                tDate = dateFormat.parse(appointmentOB.getAppointmentDate() + " " + appointmentOB.getAvailableID().getEndTime());
            }catch (ParseException e){
                e.printStackTrace();
            }

            if (tDate.before(cDate)){
                btnAccept.setVisibility(View.GONE);
                btnReject.setVisibility(View.GONE);
                btnAppointmentStatus.setText("EXPIRED");
                btnAppointmentStatus.setVisibility(View.VISIBLE);
            }else {
                btnAccept.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
                btnAppointmentStatus.setVisibility(View.GONE);
            }

        }else if (appointmentOB.getAppointmentStatus().equals("ACCEPTED")){
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnAppointmentStatus.setText(appointmentOB.getAppointmentStatus());
            btnAppointmentStatus.setBackgroundColor(getResources().getColor(R.color.lightgreen));
            btnAppointmentStatus.setVisibility(View.VISIBLE);
            btnAppointmentStatus.setClickable(true);
            btnAppointmentStatus.setFocusable(true);
            btnAppointmentStatus.setEnabled(true);

        }else {
            btnAccept.setVisibility(View.GONE);
            btnReject.setVisibility(View.GONE);
            btnAppointmentStatus.setText(appointmentOB.getAppointmentStatus());
            btnAppointmentStatus.setVisibility(View.VISIBLE);
            btnAppointmentStatus.setEnabled(false);
        }



        progressBar.setVisibility(View.GONE);
        fragmentBody.setVisibility(View.VISIBLE);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connection.equals("Connected")){
                    command = "{\"command\": \"303035303062\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"appointmentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAppointmentID()) + "\" ," +
                            "\"studentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getStudentID().getStudentID()) + "\" ," +
                            "\"availableID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAvailableID().getAvailableID()) + "\" ," +
                            "\"stuffID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getStuffID().getStuffID()) + "\" ," +
                            "\"opponentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getOpponentID()) + "\" ," +
                            "\"appointmentStatus\": " + "\"" + Conversion.asciiToHex("ACCEPTED") + "\" ," +
                            "\"appointmentDate\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAppointmentDate()) + "\" ," +
                            "\"opponentRecord\": " + "\"" + Conversion.asciiToHex("ACTIVE") + "\" }";

                    try {
                        pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    
                }else if (connection.equals("Disconnected")){
                    Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connection.equals("Connected")){
                    command = "{\"command\": \"303035303062\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"appointmentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAppointmentID()) + "\" ," +
                            "\"studentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getStudentID().getStudentID()) + "\" ," +
                            "\"availableID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAvailableID().getAvailableID()) + "\" ," +
                            "\"stuffID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getStuffID().getStuffID()) + "\" ," +
                            "\"opponentID\": " + "\"" + Conversion.asciiToHex(appointmentOB.getOpponentID()) + "\" ," +
                            "\"appointmentStatus\": " + "\"" + Conversion.asciiToHex("REJECTED") + "\" ," +
                            "\"appointmentDate\": " + "\"" + Conversion.asciiToHex(appointmentOB.getAppointmentDate()) + "\" ," +
                            "\"opponentRecord\": " + "\"" + Conversion.asciiToHex("ACTIVE") + "\" }";

                    try {
                        pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else if (connection.equals("Disconnected")){
                    Toast.makeText(getActivity().getApplication(), "Network is NOT available",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        btnAppointmentStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Button clicked!");
                final CharSequence[] options = {"Generate", "Cancel"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Generate trade QR code?");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (options[which] == "Generate"){
                            generateQRcode();

                        }else if (options[which] == "Cancel"){
                            dialog.dismiss();
                        }

                    }
                });
                builder.show();
            }
        });

    }

    private void generateQRcode() {
        qrDialog = new Dialog(getActivity());
        qrDialog.setContentView(R.layout.qrcode_image);
        qrImage = qrDialog.findViewById(R.id.QRcodeImage);

        String inputValue = appointmentOB.getAppointmentID();
        if(inputValue.length() > 0){
            WindowManager manager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            int width = point.x;
            int height = point.y;
            int smallerDimension = width < height ? width : height;
            smallerDimension = smallerDimension * 3 / 4;

            qrgEncoder = new QRGEncoder(
                    inputValue, null,
                    QRGContents.Type.TEXT,
                    smallerDimension);

            try {
                bitmap = qrgEncoder.encodeAsBitmap();
                qrImage.setImageBitmap(bitmap);
            }catch (WriterException e){
                e.printStackTrace();
            }
            qrDialog.show();

        }else {
            Log.d(TAG, "Something missing");
        }
    }

    private void updateAppointmentDetail(final JSONObject myjsonObj) {
        try {

            updateAppointmentDetailUrl = Constant.serverFile + "updateAppointmentInfo.php?appointmentID=" + Conversion.hexToAscii(myjsonObj.getString("appointmentID"))
                    + "&studentID=" + Conversion.hexToAscii(myjsonObj.getString("studentID"))
                    + "&availableID=" + Conversion.hexToAscii(myjsonObj.getString("availableID"))
                    + "&stuffID=" + Conversion.hexToAscii(myjsonObj.getString("stuffID"))
                    + "&opponentID=" + Conversion.hexToAscii(myjsonObj.getString("opponentID"))
                    + "&appointmentStatus=" + Conversion.hexToAscii(myjsonObj.getString("appointmentStatus"))
                    + "&appointmentDate=" + Conversion.hexToAscii(myjsonObj.getString("appointmentDate"))
                    + "&opponentRecord=" + Conversion.hexToAscii(myjsonObj.getString("opponentRecord"));
            Log.d(TAG, "Update appointment URL = " + updateAppointmentDetailUrl);
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        updateAppointmentDetailUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response);
                                    String success = jsonObject.getString("success");
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    if (success.equals("1")) {

                                        btnAccept.setVisibility(View.GONE);
                                        btnReject.setVisibility(View.GONE);
                                        btnAppointmentStatus.setText(Conversion.hexToAscii(myjsonObj.getString("appointmentStatus")));
                                        btnAppointmentStatus.setVisibility(View.VISIBLE);
                                        if (Conversion.hexToAscii(myjsonObj.getString("appointmentStatus")).equals("ACCEPTED")){
                                            btnAppointmentStatus.setClickable(true);
                                            btnAppointmentStatus.setFocusable(true);
                                            btnAppointmentStatus.setEnabled(true);
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

                                        builder.setTitle("Appointment is successfully responded")
                                                .setMessage("Appointment is responded.")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Appointment is failed to respond");
                                        builder.setMessage("Appointment is not responded.");
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

        }catch (JSONException e){
            e.printStackTrace();
        }



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
        Log.d(TAG, "You leaved 1");

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
