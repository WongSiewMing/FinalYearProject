package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
   Programme : RSD3
   Year : 2018*/

import android.Manifest;
import android.app.*;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.StoreOB;
import Helper.Stuff;

import static android.app.Activity.RESULT_OK;


public class EditStoreProfile extends Fragment {

    private static final int CAMERA_REQUEST = 10;
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int CROP_REQUEST = 30;
    private static final int RequestStuffCode = 40;
    private ImageView captureImage, buttonCamera;
    private Bitmap cameraImage;
    private String converted, targetStoreID, jsonURL= "", changeImage = "";
    private EditText editStoreName, editStoreDescription, editStoreLocation;
    private Spinner editStoreCategory;
    private TextView editOpenTime, editCloseTime, openText, closeText;
    private FloatingActionButton submitChange;
    private Button AddRow;
    private SimpleDateFormat sdf;
    private TimePickerDialog timePickerDialog;
    private StoreOB storeInfo;
    private ArrayList<Stuff> stuffList = new ArrayList<Stuff>();
    private JSONObject jsonObj;
    private String currentStoreStuffID, newStoreStuffID;
    private boolean retrievedStoreStuffID = false, storeStuffIDExist = false;
    private String addStuffUrl, checkStuffUrl;

    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command, userID = "";
    FragmentManager fragmentManager;
    View view;
    private Uri uri;
    private Intent cameraIntent, photoPickerIntent, CropIntent;

    private static final String TAG = "EditStoreProfile";
    private int RequestCameraPermissionID = 1001;

    private RecyclerView mRecycleView;
    private StoreStuffAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Edit Profile");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_store_profile, container, false);

        Bundle bundle = getArguments();
//        targetStoreID = bundle.getString("storeID");
//        Log.d(TAG, "Current Store ID = " + targetStoreID);
        storeInfo = (StoreOB) bundle.getSerializable("storeInfo");
        stuffList = (ArrayList<Stuff>) bundle.getSerializable("stuffList");
        userID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        Log.d(TAG, "Store name =" + storeInfo.getStoreName());

        captureImage = view.findViewById(R.id.captureImage2);
        buttonCamera = view.findViewById(R.id.buttonCamera2);
        editStoreName = view.findViewById(R.id.edit_storeName);
        editStoreDescription = view.findViewById(R.id.edit_storeDescription);
        editStoreLocation = view.findViewById(R.id.edit_storeLocation);
        editStoreCategory = view.findViewById(R.id.edit_StoreCategory);
        editOpenTime = view.findViewById(R.id.edit_StoreOpenTime);
        editCloseTime = view.findViewById(R.id.edit_StoreCloseTime);
        submitChange = view.findViewById(R.id.edit_StoreInformation);
        openText = view.findViewById(R.id.openText);
        closeText = view.findViewById(R.id.closeText);

        ArrayAdapter<CharSequence> adapterStoreCategory = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.storeCategorySpinner,
                android.R.layout.simple_spinner_dropdown_item);
        editStoreCategory.setAdapter(adapterStoreCategory);

        //setOriginalData
        Picasso.with(getActivity()).load(storeInfo.getStoreImage()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(captureImage);
        editStoreName.setText(storeInfo.getStoreName());
        editStoreDescription.setText(storeInfo.getStoreDescription());
        if (storeInfo.getStoreCategory().toLowerCase().trim().equals("food")) {
            editStoreCategory.setSelection(1);
        } else if (storeInfo.getStoreCategory().toLowerCase().trim().equals("grocery")) {
            editStoreCategory.setSelection(2);
        } else if (storeInfo.getStoreCategory().toLowerCase().trim().equals("stationary")) {
            editStoreCategory.setSelection(3);
        } else if (storeInfo.getStoreCategory().toLowerCase().trim().equals("stall")) {
            editStoreCategory.setSelection(4);
        } else {
            editStoreCategory.setSelection(0);
        }
        editOpenTime.setText(storeInfo.getOpenTime());
        editCloseTime.setText(storeInfo.getCloseTime());
        editStoreLocation.setText(storeInfo.getStoreLocation());

        sdf = new SimpleDateFormat("HHmm");

        openText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c1 = Calendar.getInstance();
                int mhour = c1.get(Calendar.HOUR_OF_DAY);
                int mminute = c1.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Log.d(TAG, "HOUR OF DAY =" + hourOfDay);
                        Log.d(TAG, "minute =" + minute);
                        String openTime = hourOfDay + ":" + minute;
                        DateFormat df = new SimpleDateFormat("HH:mm");
                        Date d;
                        try {
                            d = df.parse(openTime);
                            Log.d(TAG, "Converted Time" + sdf.format(d));
                            editOpenTime.setText(sdf.format(d));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mhour, mminute, true);
                timePickerDialog.setTitle("Select open time");
                timePickerDialog.show();

            }
        });

        closeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c2 = Calendar.getInstance();
                int mhour = c2.get(Calendar.HOUR_OF_DAY);
                int mminute = c2.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String closeTime = hourOfDay + ":" + minute;
                        DateFormat df = new SimpleDateFormat("HH:mm");
                        Date d;
                        try {
                            d = df.parse(closeTime);
                            Log.d(TAG, "Converted Time" + sdf.format(d));
                            editCloseTime.setText(sdf.format(d));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mhour, mminute, true);
                timePickerDialog.setTitle("Select close time");
                timePickerDialog.show();
            }
        });


        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Take photo", "Choose from gallery", "Cancel"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choose your action");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selection) {
                        if (options[selection] == "Take photo") {
                            if (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA) !=PackageManager.PERMISSION_GRANTED){
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, RequestCameraPermissionID);
                                Log.d(TAG,"Permission requested");
                                return;
                            }else {
                                Log.d(TAG,"Permission ady granted");
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

        submitChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSubmitField() == true) {
                    updateStoreProfile();
                }
            }
        });

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mRecycleView = view.findViewById(R.id.stuffLL);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new StoreStuffAdapter(stuffList);
        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setLayoutManager(mLayoutManager);

        mAdapter.setOnItemClickListener(new StoreStuffAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                //Leave here empty
            }

            @Override
            public void onDeleteClick(int position) {
                removeItem(position);
            }

            @Override
            public void onEditClick(int position){
                Stuff clickedStuff = stuffList.get(position);
                MaintainStuff frag = new MaintainStuff();
                Bundle bundle = new Bundle();
                bundle.putSerializable("MaintainStuff", clickedStuff);
                frag.setArguments(bundle);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        });

        AddRow = view.findViewById(R.id.btnAddRow);

        AddRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StoreStuffFragment storeStuffFragment = new StoreStuffFragment();
                storeStuffFragment.setTargetFragment(EditStoreProfile.this, RequestStuffCode);
                Bundle bundle1 = new Bundle();
                bundle1.putString("selectStuff", userID);
                storeStuffFragment.setArguments(bundle1);

                fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, storeStuffFragment)
                        .addToBackStack(storeStuffFragment.getClass().getName())
                        .commit();

            }
        });
    }

    public void removeItem(int position){
        stuffList.remove(position);
        mAdapter.notifyDataSetChanged();
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
                changeImage = "change";
            }
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Bitmap bm = null;
                if (data != null){
                    try {
                        bm = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), data.getData());
                        captureImage.setImageBitmap(getResizedBitmap(bm, 500, 500));
                        converted = bitmapToString(getResizedBitmap(bm, 500,500));
                        changeImage = "change";

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            if (requestCode == RequestStuffCode){
                Stuff selectedStuff = (Stuff)data.getSerializableExtra("selectedStuff");
                Toast.makeText(getActivity().getApplication(), "Selected Stuff : " + selectedStuff.getStuffID(), Toast.LENGTH_LONG).show();
                stuffList.add(selectedStuff);
                Log.d(TAG, "Stuff ID fetched at Register=" + selectedStuff.getStuffID());
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


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private boolean checkSubmitField() {
        String error = "";
        boolean indicator = true;
        int oTime = Integer.parseInt(editOpenTime.getText().toString());
        int cTime = Integer.parseInt(editCloseTime.getText().toString());

        if (captureImage.getDrawable() == null) {
            error += "- No image is attached.\n";
            indicator = false;
        }
        if (editStoreName.getText().toString().trim().equals("")) {
            error += "- Store name is required.\n";
            indicator = false;
        }
        if (editStoreDescription.getText().toString().trim().equals("")) {
            error += "- Store description is required.\n";
            indicator = false;
        }
        if (editStoreLocation.getText().toString().trim().equals("")) {
            error += "- Store location is required.\n";
            indicator = false;
        }
        if (editStoreCategory.getSelectedItem().toString().equals("--Category--")) {
            error += "- Store category is required.\n";
            indicator = false;
        }
        if (oTime < 600 || cTime > 2000) {
            error += "- Time range must between 6am to 8pm\n";
            indicator = false;
        }
        if (cTime <= oTime) {
            error += "- Close time must after open time\n";
            indicator = false;
        }
        if (stuffList.size() == 0){
            error += "- At least one stuff should be selected\n";
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

    public void updateStoreProfile() {
        try {
            //http://192.168.0.101/raindown/updateStoreProfile.php?StoreID=str1005&StoreName=1&StoreImage=&StoreDescription=2&StoreCategory=stationary&OpenTime=0900&CloseTime=1700&StoreLocation=kk&ChangeImage=

            command = "{\"command\": \"303035303051\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"StoreID\": " + "\"" + Conversion.asciiToHex(storeInfo.getStoreID()) + "\" ," +
                            "\"StoreName\": " + "\"" + Conversion.asciiToHex(editStoreName.getText().toString().trim()) + "\" ," +
                            "\"StoreDescription\": " + "\"" + Conversion.asciiToHex(editStoreDescription.getText().toString().trim()) + "\" ," +
                            "\"StoreCategory\": " + "\"" + Conversion.asciiToHex(editStoreCategory.getSelectedItem().toString()) + "\" ," +
                            "\"OpenTime\": " + "\"" + Conversion.asciiToHex(editOpenTime.getText().toString()) + "\" ," +
                            "\"CloseTime\": " + "\"" + Conversion.asciiToHex(editCloseTime.getText().toString()) + "\" ," +
                            "\"StoreLocation\": " + "\"" + Conversion.asciiToHex(editStoreLocation.getText().toString().trim()) + "\" }";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            String encodedStoreName = "";
            String encodedStoreDescription = "";
            String encodedStoreLocation = "";

            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303051")){
                String[] encodeStoreName ={Conversion.hexToAscii(jsonObj.getString("StoreName"))};
                for (String s : encodeStoreName){
                    encodedStoreName += URLEncoder.encode(s, "UTF-8");
                }
                String[] encodeStoreDescription = {Conversion.hexToAscii(jsonObj.getString("StoreDescription"))};
                for (String s : encodeStoreDescription){
                    encodedStoreDescription += URLEncoder.encode(s, "UTF-8");
                }
                String[] encodeStoreLocation = {Conversion.hexToAscii(jsonObj.getString("StoreLocation"))};
                for (String s : encodeStoreLocation){
                    encodedStoreLocation += URLEncoder.encode(s, "UTF-8");
                }
            }

            Log.d(TAG, "Encoded Store name = " + encodedStoreName);



                jsonURL = Constant.serverFile + "updateStoreProfile.php?StoreID=" + storeInfo.getStoreID()
                        + "&StoreName=" + encodedStoreName
                        + "&StoreImage=" + ""
                        + "&StoreDescription=" + encodedStoreDescription
                        + "&StoreCategory=" + editStoreCategory.getSelectedItem().toString()
                        + "&OpenTime=" + editOpenTime.getText().toString()
                        + "&CloseTime=" + editCloseTime.getText().toString()
                        + "&StoreLocation=" + encodedStoreLocation
                        + "&ChangeImage=" + changeImage;

                Log.d(TAG,"Update Store URL = " + jsonURL);

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
                                        deleteStuff();

                                        for (int y = 0; y < stuffList.size(); y++){
                                            getStoreStuffID(stuffList.get(y).getStuffID());
                                        }
                                        stuffList.clear();

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

                                        builder.setTitle("Store is successfully updated")
                                                .setMessage(editStoreName.getText().toString().trim() + " is updated.")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Store is failed to update");
                                        builder.setMessage(storeInfo.getStoreName() + " is not updated.");
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
                        params.put("StoreID", storeInfo.getStoreID());
                        params.put("StoreName", editStoreName.getText().toString().trim());
                        if (changeImage.equals("change")) {
                            params.put("StoreImage", converted);
                        } else {
                            params.put("StoreImage", "");
                        }
                        params.put("StoreDescription", editStoreDescription.getText().toString().trim());
                        params.put("StoreCategory", editStoreCategory.getSelectedItem().toString());
                        params.put("OpenTime", editOpenTime.getText().toString());
                        params.put("CloseTime", editCloseTime.getText().toString());
                        params.put("StoreLocation", editStoreLocation.getText().toString().trim());
                        params.put("ChangeImage", changeImage);

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

    public void deleteStuff(){
        try {
            command = "{\"command\": \"303035303088\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"StoreID\": " + "\"" + Conversion.asciiToHex(storeInfo.getStoreID().trim()) + "\"}";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            String encodedStoreID = "";

            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303088")){
                String[] encodeStuffID ={Conversion.hexToAscii(jsonObj.getString("StoreID"))};
                for (String s : encodeStuffID){
                    encodedStoreID += URLEncoder.encode(s, "UTF-8");
                }
            }

            Log.d(TAG, "Delete Store ID = " + encodedStoreID);

            jsonURL = Constant.serverFile + "removeStoreStuffList.php?storeID=" + encodedStoreID;

            Log.d(TAG,"Delete Store Stuff URL = " + jsonURL);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                JsonArrayRequest deleteRequest = new JsonArrayRequest(jsonURL,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    Log.d(TAG, "Delete Stuff (onResponse)");
                                    JSONObject jsonObject = null;
                                    String success = "";
                                    try {
                                        Log.d(TAG, "Delete Stuff (onResponse) 2");
                                        for (int i = 0; i < response.length(); i++) {
                                            jsonObject = (JSONObject) response.get(i);
                                            success = jsonObject.getString("ID");
                                        }
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                        if (success.equals("0")) {
                                            Log.d(TAG, jsonObject.getString("message"));
                                        } else {
                                            builder.setTitle("Store is failed to update");
                                            builder.setMessage(jsonObject.getString("message"));
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
                                } catch (Exception e) {
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {

                            }
                        });
                queue.add(deleteRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }



        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded()) {
            }
        }
    }

    public void getStoreStuffID(final String stuffID){

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest getStoreStuffIDRequest = new JsonArrayRequest(Constant.serverFile + "getStoreStuffID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    JSONObject storeStuffIDResponse = null;
                                    for (int i = 0; i < response.length(); i++) {
                                        storeStuffIDResponse = (JSONObject) response.get(i);
                                        currentStoreStuffID = storeStuffIDResponse.getString("CurrentStoreStuffID");
                                    }
                                    Log.d(TAG, "Current Store Stuff ID =" + currentStoreStuffID);

                                    if (retrievedStoreStuffID == true){
                                        do {
                                            String first = newStoreStuffID.substring(0, 4);
                                            String last = newStoreStuffID.substring(4);
                                            int number = Integer.parseInt(last) + 1;
                                            newStoreStuffID = first + Integer.toString(number);
                                        } while (checkExistStoreStuffID(newStoreStuffID));
                                        Log.d(TAG, "New Store Stuff ID =" + newStoreStuffID);
                                        insertStoreStuffList(stuffID, newStoreStuffID);
                                    } else{
                                        if (currentStoreStuffID.equals("0")) {
                                            newStoreStuffID = "strs1001";
                                            Log.d(TAG, "New Store Stuff ID =" + newStoreStuffID);
                                            retrievedStoreStuffID = true;
                                            insertStoreStuffList(stuffID, newStoreStuffID);
                                        } else {
                                            do {
                                                String first = newStoreStuffID.substring(0, 4);
                                                String last = newStoreStuffID.substring(4);
                                                int number = Integer.parseInt(last) + 1;
                                                newStoreStuffID = first + Integer.toString(number);
                                            } while (checkExistStoreStuffID(newStoreStuffID));
                                            Log.d(TAG, "New Store Stuff ID =" + newStoreStuffID);
                                            retrievedStoreStuffID = true;
                                            insertStoreStuffList(stuffID, newStoreStuffID);
                                        }
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
                queue.add(getStoreStuffIDRequest);

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

    public Boolean checkExistStoreStuffID(String stuffID) {
        try {

            command = "{\"command\": \"303035303089\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"StoreStuffID\": " + "\"" + Conversion.asciiToHex(stuffID.trim()) + "\"}";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            String encodedStoreStuffID = "";

            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303089")) {
                String[] encodeStoreStuffID = {Conversion.hexToAscii(jsonObj.getString("StoreStuffID"))};
                for (String s : encodeStoreStuffID) {
                    encodedStoreStuffID += URLEncoder.encode(s, "UTF-8");
                }

            }

            checkStuffUrl = Constant.serverFile + "checkStoreStuffID.php?storeStuffID=" + encodedStoreStuffID;
            Log.d(TAG, "Check store stuff ID Url =" + checkStuffUrl);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                JsonArrayRequest checkStuffRequest = new JsonArrayRequest(checkStuffUrl,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    String success = "";
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject existResponse = (JSONObject) response.get(i);
                                        success = existResponse.getString("success");
                                    }
                                    if (success.equals("0")) {
                                        storeStuffIDExist = false;
                                    } else {
                                        storeStuffIDExist = true;
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
                queue.add(checkStuffRequest);

            } catch (Exception e) {
                e.printStackTrace();
                if (isAdded()) {
                }
            }

        }catch (Exception e){
            Toast.makeText(getActivity().getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        return storeStuffIDExist;
    }

    public void insertStoreStuffList(final String newStuffID, final String newID){
        try {

            command = "{\"command\": \"303035303086\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"StoreStuffID\": " + "\"" + Conversion.asciiToHex(newID.trim()) + "\" ," +
                    "\"StoreID\": " + "\"" + Conversion.asciiToHex(storeInfo.getStoreID().trim()) + "\" ," +
                    "\"StuffID\": " + "\"" + Conversion.asciiToHex(newStuffID.trim()) + "\"}";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            String encodedStoreStuffID = "";
            String encodedStoreID = "";
            String encodedStuffID = "";

            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303086")){
                String[] encodeStoreStuffID = {Conversion.hexToAscii(jsonObj.getString("StoreStuffID"))};
                for (String s : encodeStoreStuffID){
                    encodedStoreStuffID += URLEncoder.encode(s, "UTF-8");
                }
                String[] encodeStoreID = {Conversion.hexToAscii(jsonObj.getString("StoreID"))};
                for (String s : encodeStoreID){
                    encodedStoreID += URLEncoder.encode(s, "UTF-8");
                }
                String[] encodeStuffID = {Conversion.hexToAscii(jsonObj.getString("StuffID"))};
                for (String s : encodeStuffID){
                    encodedStuffID += URLEncoder.encode(s, "UTF-8");
                }

            }

            addStuffUrl = Constant.serverFile + "insertStoreStuffList.php?storeStuffID=" + encodedStoreStuffID
                    + "&storeID=" + encodedStoreID
                    + "&stuffID=" + encodedStuffID;
            Log.d(TAG, "Insert store stuff Url =" + addStuffUrl);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        addStuffUrl,
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
                                        Log.d(TAG, "StoreStuffID successful inserted =" + newID.trim());
                                    } else {
                                        builder.setTitle("Failed to create StoreStuffID");
                                        builder.setMessage(newID.trim() + " is failed to be uploaded");
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
                        params.put("storeStuffID", newID.trim());
                        params.put("storeID", storeInfo.getStoreID());
                        params.put("stuffID", newStuffID.trim());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "You leaved");
        try {
           // pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "You leaved");
        try {
          //  pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }
    }

}
