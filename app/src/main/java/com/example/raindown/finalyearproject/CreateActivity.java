package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
   Programme : RSD3
   Year : 2018*/

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.StoreBasicInfoOB;
import Helper.StuffBasicInfoOB;

import static android.app.Activity.RESULT_OK;


public class CreateActivity extends Fragment {
    private static final int CAMERA_REQUEST = 10;
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int CROP_REQUEST = 30;
    private FloatingActionButton createActivity;
    private ImageView captureImage, buttonCamera, selectedStuffImage;
    private Bitmap cameraImage;
    private String converted, formattedDate, formattedTime;
    private TextView choose_Attach_Stuff, noRecordFound, selectedStuffName, selectedStuffDescription, selectedStuffID, removeSelectedStuff;
    private EditText create_activity_text;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private Dialog chooseStuff;
    private ProgressBar progressBar;
    private List<StuffBasicInfoOB> stuffBasicInfoList;
    private RecyclerView stuffListrv;
    private String UserID, command, currentActivityID, newActivityID, addActivityUrl, setActivityImage = "";
    private RelativeLayout selectedStuffField;
    FragmentManager fragmentManager;
    private Date c;
    private SimpleDateFormat df;
    private ProgressDialog pDialog = null;
    private JSONObject jsonObj;
    private Uri uri;
    private Intent cameraIntent, photoPickerIntent, CropIntent;
    private int RequestCameraPermissionID = 1001;
    View view;

    private static final String TAG = "testData";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_activity, container, false);
        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        Log.d(TAG, "User ID =" + UserID);
        pDialog = new ProgressDialog(getActivity());

        createActivity = view.findViewById(R.id.create_activity_button);
        captureImage = view.findViewById(R.id.captureImage4);
        buttonCamera = view.findViewById(R.id.buttonCamera4);
        choose_Attach_Stuff = view.findViewById(R.id.choose_attach_stuff);
        create_activity_text = view.findViewById(R.id.create_activity_text);
        chooseStuff = new Dialog(getActivity());
        selectedStuffField = view.findViewById(R.id.selectedStuffField);
        selectedStuffName = view.findViewById(R.id.selectedStuffName);
        selectedStuffImage = view.findViewById(R.id.selectedStuffImage);
        selectedStuffDescription = view.findViewById(R.id.selectedStuffDescription);
        selectedStuffID = view.findViewById(R.id.selected_stuff_id);
        removeSelectedStuff = view.findViewById(R.id.remove_selected_stuff);
        stuffBasicInfoList = new ArrayList<>();
        stuffBasicInfoList.clear();

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Camera button clicked");
                final CharSequence[] options = {"Take photo", "Choose from gallery", "Cancel"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose your action");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selection) {
                        if (options[selection] == "Take photo") {
                            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);
                                Log.d(TAG, "Permission requested");
                                return;
                            } else {
                                Log.d(TAG, "Permission ady granted");
                            }
                            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            startActivityForResult(cameraIntent, CAMERA_REQUEST);
                        } else if (options[selection] == "Choose from gallery") {
                            photoPickerIntent = new Intent();
                            photoPickerIntent.setType("image/*");
                            photoPickerIntent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(photoPickerIntent, "Select Image From Gallary"), IMAGE_GALLERY_REQUEST);
                        } else if (options[selection] == "Cancel") {
                            dialog.dismiss();
                        }

                    }
                });
                builder.show();

            }
        });

        choose_Attach_Stuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseStuff.setContentView(R.layout.choose_attach_stuff);
                progressBar = chooseStuff.findViewById(R.id.progressBar);
                stuffListrv = chooseStuff.findViewById(R.id.stuffListrv);
                noRecordFound = chooseStuff.findViewById(R.id.noRecordFound);
                chooseStuff.show();

                try {
                    command = "{\"command\": \"303035303067\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"StudentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\"}";

                    pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                populateStuffBasicInfoList();
                progressBar.setVisibility(View.GONE);
                stuffListrv.setVisibility(View.VISIBLE);

            }
        });

        removeSelectedStuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedStuffName.setText("");
                selectedStuffDescription.setText("");
                selectedStuffImage.setImageDrawable(null);
                selectedStuffID.setText("");
                selectedStuffField.setVisibility(View.GONE);
            }
        });

        createActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSubmitField() == true) {
                    c = Calendar.getInstance().getTime();
                    df = new SimpleDateFormat("dd/MM/yyyy");
                    formattedDate = df.format(c);
                    Log.d(TAG, "Formatted Date = " + formattedDate);
                    df = new SimpleDateFormat("HHmm");
                    formattedTime = df.format(c);
                    Log.d(TAG, "Formatted Time = " + formattedTime);


                    getUserActivityID();

                }

            }
        });

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);

        return view;
    }

    private void getUserActivityID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getUserActivityID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    //CurrentActivityID
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject activityIDResponse = (JSONObject) response.get(i);
                                        currentActivityID = activityIDResponse.getString("CurrentActivityID");
                                    }
                                    Log.d(TAG, " Current activity ID =" + currentActivityID);

                                    if (currentActivityID.equals("0")) {
                                        newActivityID = "act1001";
                                    } else {
                                        String first = currentActivityID.substring(0, 3);
                                        String last = currentActivityID.substring(3);
                                        int number = Integer.parseInt(last) + 1;
                                        newActivityID = first + Integer.toString(number);
                                    }

                                    Log.d(TAG, "New Activity ID =" + newActivityID);
                                    insertUserActivityInfo(newActivityID);


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

    private void insertUserActivityInfo(final String newID) {

        try {

            command = "{\"command\": \"303035303066\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"StudentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" ," +
                    "\"stuffID\": " + "\"" + Conversion.asciiToHex(selectedStuffID.getText().toString().trim()) + "\" ," +
                    "\"ActivityCaption\": " + "\"" + Conversion.asciiToHex(create_activity_text.getText().toString().trim()) + "\" ," +
                    "\"UploadDate\": " + "\"" + Conversion.asciiToHex(formattedDate) + "\" ," +
                    "\"UploadTime\": " + "\"" + Conversion.asciiToHex(formattedTime) + "\" }";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            String encodedActivityCaption = "";

            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303066")) {
                String[] encodeActivityCaption = {Conversion.hexToAscii(jsonObj.getString("ActivityCaption"))};
                for (String s : encodeActivityCaption) {
                    encodedActivityCaption += URLEncoder.encode(s, "UTF-8");
                }
            }


            addActivityUrl = Constant.serverFile + "insertUserActivityData.php?activityID=" + newID
                    + "&studentID=" + UserID
                    + "&stuffID=" + selectedStuffID.getText().toString()
                    + "&activityCaption=" + encodedActivityCaption
                    + "&activityThumbnail=" + ""
                    + "&uploadDate=" + formattedDate
                    + "&uploadTime=" + formattedTime
                    + "&setActivityImage=" + setActivityImage;

            Log.d(TAG, "insert activity url = " + addActivityUrl);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        addActivityUrl,
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
                                                        getFragmentManager().popBackStack();
                                                        break;
                                                }
                                            }
                                        };

                                        builder.setTitle("Activity is successfully posted")
                                                .setMessage("Your activity is uploaded")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Failed to post activity");
                                        builder.setMessage("Your activity is failed to be posted");
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
                        params.put("activityID", newID);
                        params.put("studentID", UserID);
                        params.put("stuffID", selectedStuffID.getText().toString());
                        params.put("activityCaption", create_activity_text.getText().toString().trim());
                        if (setActivityImage.equals("yes")) {
                            params.put("activityThumbnail", converted);
                        } else {
                            params.put("activityThumbnail", "");
                        }
                        params.put("uploadDate", formattedDate);
                        params.put("uploadTime", formattedTime);
                        params.put("setActivityImage", setActivityImage);


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

    private void populateStuffBasicInfoList() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStudentStuff.php?studentID=" + UserID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    stuffBasicInfoList.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject stuffRespond = (JSONObject) response.get(i);
                                        stuffBasicInfoList.add(new StuffBasicInfoOB(stuffRespond.getString("stuffID"),
                                                stuffRespond.getString("studentID"),
                                                stuffRespond.getString("stuffName"),
                                                stuffRespond.getString("stuffImage"),
                                                stuffRespond.getString("stuffDescription")));
                                    }

                                    Log.d(TAG, "Respond length =" + response.length());
                                    if (response.length() == 0) {
                                        noRecordFound.setVisibility(View.VISIBLE);
                                    } else {
                                        noRecordFound.setVisibility(View.GONE);
                                        populateRecyclerView();
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


    private void populateRecyclerView() {
        StuffBasicInfoRecycleViewAdapter stuffBasicInfoRecycleViewAdapter = new StuffBasicInfoRecycleViewAdapter(getActivity(), stuffBasicInfoList, chooseStuff, selectedStuffName, selectedStuffDescription, selectedStuffImage, selectedStuffField, selectedStuffID);
        stuffListrv.setLayoutManager(new LinearLayoutManager(getActivity()));
        stuffListrv.setAdapter(stuffBasicInfoRecycleViewAdapter);
    }

    private boolean checkSubmitField() {
        String error = "";
        boolean indicator = true;
        if (create_activity_text.getText().toString().trim().equals("")) {
            error += "- Activity caption is required.\n";
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        converted = "";
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                cameraImage = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                cameraImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                captureImage.setImageBitmap(cameraImage);
                converted = bitmapToString(cameraImage);
                setActivityImage = "yes";
            }
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Bitmap bm = null;
                if (data != null){
                    try {
                        bm = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), data.getData());
                        captureImage.setImageBitmap(getResizedBitmap(bm, 500, 500));
                        converted = bitmapToString(getResizedBitmap(bm, 500,500));
                        setActivityImage = "yes";

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth){
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    public String bitmapToString(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] arr = baos.toByteArray();
        String result = Base64.encodeToString(arr, Base64.DEFAULT);
        return result;
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
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "You leaved");
        try {
            //pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "You leaved");
        try {
            //pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
