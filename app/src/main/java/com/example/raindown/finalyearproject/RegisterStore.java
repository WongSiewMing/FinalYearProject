package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;
import Helper.Stuff;

import static android.app.Activity.RESULT_OK;


public class RegisterStore extends Fragment {

    private static final int CAMERA_REQUEST = 10;
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int CROP_REQUEST = 30;
    private static final int RequestCameraPermissionID = 1001;
    private static final int RequestStuffCode = 1002;
    private ImageView captureImage, buttonCamera;
    private String converted;
    private EditText registerStoreName, registerStoreDescription, registerStoreLocation;
    private Spinner registerStoreCategory;
    private TextView registerOpenTime, registerCloseTime, openText, closeText;
    private Button AddRow, submitRegister, btnEdit, btnRemove;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    FragmentManager fragmentManager;
    private String command = "", currentStoreID,newStoreID, addStoreUrl, addStuffUrl;
    private String currentStoreStuffID, newStoreStuffID;
    private boolean retrievedStoreStuffID = false, storeStuffIDExist = false;
    private SimpleDateFormat sdf;
    private TimePickerDialog timePickerDialog;
    private ProgressDialog pDialog = null;
    private JSONObject jsonObj;
    private Uri uri;
    private Intent cameraIntent, photoPickerIntent, CropIntent;
    private ArrayList<Stuff> stuffList = new ArrayList<>();

    private Bitmap cameraImage;
    private String userID = "";

    private RecyclerView mRecycleView;
    private StoreStuffAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    View view;

    private static final String TAG = "RegisterStore";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Register Store");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_register_store, container, false);
        userID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        Log.d(TAG, "User ID =" + userID);
        pDialog = new ProgressDialog(getActivity());


        captureImage = view.findViewById(R.id.captureImage3);
        buttonCamera = view.findViewById(R.id.buttonCamera3);
        registerStoreName = view.findViewById(R.id.register_storeName);
        registerStoreDescription = view.findViewById(R.id.register_storeDescription);
        registerStoreLocation = view.findViewById(R.id.register_storeLocation);
        registerStoreCategory = view.findViewById(R.id.register_StoreCategory);
        registerOpenTime = view.findViewById(R.id.register_StoreOpenTime);
        registerCloseTime = view.findViewById(R.id.register_StoreCloseTime);
        submitRegister = view.findViewById(R.id.btnRegisterStore);
        openText = view.findViewById(R.id.openText);
        closeText = view.findViewById(R.id.closeText);

        ArrayAdapter<CharSequence> adapterStoreCategory = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.storeCategorySpinner,
                android.R.layout.simple_spinner_dropdown_item);
        registerStoreCategory.setAdapter(adapterStoreCategory);

        sdf = new SimpleDateFormat("HHmm");

        openText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c1 = Calendar.getInstance();
                int mHour = c1.get(Calendar.HOUR_OF_DAY);
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
                            registerOpenTime.setText(sdf.format(d));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, mHour, mminute, true);
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
                            registerCloseTime.setText(sdf.format(d));
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
                Log.d(TAG, "Camera button clicked");
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

        submitRegister = view.findViewById(R.id.btnRegisterStore);
        submitRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Hello");
                if (checkSubmitField() == true) {
                    Log.d(TAG, "Store Name = " + registerStoreName.getText().toString().trim());
                    Log.d(TAG, "Store Description = " + registerStoreDescription.getText().toString().trim());
                    Log.d(TAG, "Store Category = " + registerStoreCategory.getSelectedItem().toString());
                    Log.d(TAG, "Open Time = " + registerOpenTime.getText().toString());
                    Log.d(TAG, "Close Time = " + registerCloseTime.getText().toString());
                    Log.d(TAG, "Store Location = " + registerStoreLocation.getText().toString().trim());


                    getStoreID();

                }

            }
        });

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);

        return view;
    }

    public void removeItem(int position){
        stuffList.remove(position);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        mRecycleView = view.findViewById(R.id.stuffLL);
        btnEdit = view.findViewById(R.id.btnEdit);
        btnRemove = view.findViewById(R.id.btnRemove);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new StoreStuffAdapter(stuffList);
        mRecycleView.setLayoutManager(mLayoutManager);
        mRecycleView.setAdapter(mAdapter);

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
                storeStuffFragment.setTargetFragment(RegisterStore.this, RequestStuffCode);
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

            }
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Bitmap bm = null;
                if (data != null){
                    try {
                        bm = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), data.getData());
                        captureImage.setImageBitmap(getResizedBitmap(bm, 500, 500));
                        converted = bitmapToString(getResizedBitmap(bm, 500,500));

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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private boolean checkSubmitField() {
        String error = "";
        boolean indicator = true;
        int oTime = Integer.parseInt(registerOpenTime.getText().toString());
        int cTime = Integer.parseInt(registerCloseTime.getText().toString());

        if (captureImage.getDrawable() == null) {
            error += "- No image is attached.\n";
            indicator = false;
        }
        if (registerStoreName.getText().toString().trim().equals("")) {
            error += "- Store name is required.\n";
            indicator = false;
        }
        if (registerStoreDescription.getText().toString().trim().equals("")) {
            error += "- Store description is required.\n";
            indicator = false;
        }
        if (registerStoreLocation.getText().toString().trim().equals("")) {
            error += "- Store location is required.\n";
            indicator = false;
        }
        if (registerStoreCategory.getSelectedItem().toString().equals("--Category--")) {
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

    public void getStoreID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStoreID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject storeIDResponse = (JSONObject) response.get(i);
                                        currentStoreID = storeIDResponse.getString("CurrentStoreID");
                                    }
                                    Log.d(TAG, "Current Store ID =" + currentStoreID);

                                    if (currentStoreID.equals("0")) {
                                        newStoreID = "str1001";
                                        Log.d(TAG, "New Store ID =" + newStoreID);
                                        insertStoreInfo(newStoreID);
                                    } else {
                                        String first = currentStoreID.substring(0, 3);
                                        String last = currentStoreID.substring(3);
                                        int number = Integer.parseInt(last) + 1;
                                        newStoreID = first + Integer.toString(number);
                                        Log.d(TAG, "New Store ID =" + newStoreID);
                                        insertStoreInfo(newStoreID);
                                    }

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

    public void getStoreStuffID(final String stuffID){

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStoreStuffID.php",
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
                                        } while (checkExistStoreStuffID());
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
                                            } while (checkExistStoreStuffID());
                                            Log.d(TAG, "New Store Stuff ID =" + newStoreStuffID);
                                            retrievedStoreStuffID = true;
                                            insertStoreStuffList(stuffID, newStoreStuffID);
                                        }
                                    }

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

    public Boolean checkExistStoreStuffID() {
        try {
            Log.d(TAG, "Check store stuff ID =" + newStoreStuffID);

            command = "{\"command\": \"303035303089\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"StoreStuffID\": " + "\"" + Conversion.asciiToHex(newStoreStuffID.trim()) + "\"}";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            String encodedStoreStuffID = "";

            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303089")) {
                String[] encodeStoreStuffID = {Conversion.hexToAscii(jsonObj.getString("StoreStuffID"))};
                for (String s : encodeStoreStuffID) {
                    encodedStoreStuffID += URLEncoder.encode(s, "UTF-8");
                }

            }

            addStuffUrl = Constant.serverFile + "checkStoreStuffID.php?storeStuffID=" + encodedStoreStuffID;
            Log.d(TAG, "Check store stuff ID Url =" + addStuffUrl);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(addStuffUrl,
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
                                        Log.d(TAG, "Check store stuff ID = Not Exist");
                                        storeStuffIDExist = false;
                                    } else {
                                        Log.d(TAG, "Check store stuff ID = Exist");
                                        storeStuffIDExist = true;
                                    }

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

    public void insertStoreInfo(final String newID) {
        //192.168.0.107/raindown/insertStoreData.php?storeID=str1006&studentID=17WWW05969&storeName=1&storeImage=&storeDescription=2&storeCategory=3&openTime=0000&closeTime=1111&storeLocation=4

        try {

            command = "{\"command\": \"303035303068\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"StudentID\": " + "\"" + Conversion.asciiToHex(userID) + "\" ," +
                    "\"StoreName\": " + "\"" + Conversion.asciiToHex(registerStoreName.getText().toString().trim()) + "\" ," +
                    "\"StoreDescription\": " + "\"" + Conversion.asciiToHex(registerStoreDescription.getText().toString().trim()) + "\" ," +
                    "\"StoreCategory\": " + "\"" + Conversion.asciiToHex(registerStoreCategory.getSelectedItem().toString()) + "\" ," +
                    "\"OpenTime\": " + "\"" + Conversion.asciiToHex(registerOpenTime.getText().toString().trim()) + "\" ," +
                    "\"CloseTime\": " + "\"" + Conversion.asciiToHex(registerCloseTime.getText().toString().trim()) + "\" ," +
                    "\"StoreLocation\": " + "\"" + Conversion.asciiToHex(registerStoreLocation.getText().toString().trim()) + "\"}";

            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

            String encodedStoreName = "";
            String encodedStoreDescription = "";
            String encodedStoreLocation = "";

            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303068")){
                String[] encodeStoreName = {Conversion.hexToAscii(jsonObj.getString("StoreName"))};
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



            addStoreUrl = Constant.serverFile + "insertStoreData.php?storeID=" + newID
                    + "&studentID=" + userID
                    + "&storeName=" + encodedStoreName
                    + "&storeImage=" + ""
                    + "&storeDescription=" + encodedStoreDescription
                    + "&storeCategory=" + registerStoreCategory.getSelectedItem().toString()
                    + "&openTime=" + registerOpenTime.getText().toString()
                    + "&closeTime=" + registerCloseTime.getText().toString()
                    + "&storeLocation=" + encodedStoreLocation;
            Log.d(TAG, "Insert store profile Url =" + addStoreUrl);

            RequestQueue queue = Volley.newRequestQueue(getActivity());
            try {

                StringRequest postRequest = new StringRequest(
                        Request.Method.POST,
                        addStoreUrl,
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
                                        for (int i = 0; i < stuffList.size(); i++){
                                            getStoreStuffID(stuffList.get(i).getStuffID());
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

                                        builder.setTitle("Store is successfully created")
                                                .setMessage(registerStoreName.getText().toString().trim() + " is uploaded")
                                                .setPositiveButton("OK", dialogClickListener).show();
                                    } else {
                                        builder.setTitle("Failed to create store");
                                        builder.setMessage(registerStoreName.getText().toString().trim() + " is failed to be uploaded");
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
                        params.put("storeID", newID);
                        params.put("studentID", userID);
                        params.put("storeName", registerStoreName.getText().toString().trim());
                        params.put("storeImage", converted);
                        params.put("storeDescription", registerStoreDescription.getText().toString().trim());
                        params.put("storeCategory", registerStoreCategory.getSelectedItem().toString());
                        params.put("openTime", registerOpenTime.getText().toString());
                        params.put("closeTime", registerCloseTime.getText().toString());
                        params.put("storeLocation", registerStoreLocation.getText().toString().trim());
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

    public void insertStoreStuffList(final String newStuffID, final String newID){
        try {

            command = "{\"command\": \"303035303086\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                    "\"StoreStuffID\": " + "\"" + Conversion.asciiToHex(newID.trim()) + "\" ," +
                    "\"StoreID\": " + "\"" + Conversion.asciiToHex(newStoreID.trim()) + "\" ," +
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
                        params.put("storeID", newStoreID);
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
           // pahoMqttClient.unSubscribe(mqttAndroidClient, "MY/TARUC/SSS/000000001/PUB");
            pahoMqttClient.disconnect(mqttAndroidClient);
        } catch (Exception e) {

        }
    }

}
