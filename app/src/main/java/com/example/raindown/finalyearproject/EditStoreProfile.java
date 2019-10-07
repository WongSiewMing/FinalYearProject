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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.StoreOB;

import static android.app.Activity.RESULT_OK;


public class EditStoreProfile extends Fragment {

    private static final int CAMERA_REQUEST = 10;
    private static final int IMAGE_GALLERY_REQUEST = 20;
    private static final int CROP_REQUEST = 30;
    private ImageView captureImage, buttonCamera;
    private Bitmap cameraImage;
    private String converted, targetStoreID, jsonURL= "", changeImage = "";
    private EditText editStoreName, editStoreDescription, editStoreLocation;
    private Spinner editStoreCategory;
    private TextView editOpenTime, editCloseTime, openText, closeText;
    private FloatingActionButton submitChange;
    private SimpleDateFormat sdf;
    private TimePickerDialog timePickerDialog;
    private StoreOB storeInfo;
    private JSONObject jsonObj;

    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command;
    FragmentManager fragmentManager;
    View view;
    private Uri uri;
    private Intent cameraIntent, photoPickerIntent, CropIntent;

    private static final String TAG = "testData";
    private int RequestCameraPermissionID = 1001;


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
