package com.example.raindown.finalyearproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import Helper.Student;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Chew on 7/24/2018.
 */

public class RequestStuff extends Fragment {

    public static final int CAMERA_REQUEST = 10;
    public static final int IMAGE_GALLERY_REQUEST = 20;
    private static final String TAG = "requeststuff" ;
    View view;
    ImageView captureImage, buttonCamera;
    Button requestStuff;
    TextView validEndDate, validStartDate;
    EditText stuffName, stuffDescription, stuffQuantity, stuffPrice;
    Spinner conditionSpinner, categorySpinner;
    DatePickerDialog datePickerDialog;
    Student s = null;
    SimpleDateFormat sdf = null;
    String error = "", converted = "", jsonURL = "", CurrentRequestID = "", requestID = "",
            encodedImage = "", command = "", category = "", condition = "";
    FragmentManager fragmentManager;
    Bitmap cameraImage;
    ProgressDialog pDialog = null;
    String encodedStuffName = "", encodedStuffDescription = "";
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    JSONObject jsonObj;

    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a;
        if (context instanceof Activity) {
            a = (Activity) context;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Request Stuff");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.requeststuff, container, false);
        pDialog = new ProgressDialog(getActivity());

        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("BuyStuff");

        categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapterCategoryName = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.categorySpinner,
                android.R.layout.simple_spinner_item);
        adapterCategoryName.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapterCategoryName);

        conditionSpinner = (Spinner) view.findViewById(R.id.conditionSpinner);
        ArrayAdapter<CharSequence> adapterCondition = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.conditionSpinner,
                android.R.layout.simple_spinner_item);
        adapterCondition.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conditionSpinner.setAdapter(adapterCondition);

        captureImage = (ImageView) view.findViewById(R.id.captureImage);
        buttonCamera = (ImageView) view.findViewById(R.id.buttonCamera);
        stuffName = (EditText) view.findViewById(R.id.stuffName);
        stuffDescription = (EditText) view.findViewById(R.id.stuffDescription);
        stuffPrice = (EditText) view.findViewById(R.id.stuffPrice);
        stuffQuantity = (EditText) view.findViewById(R.id.stuffQuantity);
        requestStuff = (Button) view.findViewById(R.id.requeststuff);
        validEndDate = (TextView) view.findViewById(R.id.validEndDate);
        validStartDate = (TextView) view.findViewById(R.id.validStartDate);

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
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_REQUEST);

                        } else if (options[selection] == "Choose from gallery") {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            String pictureDirectoryPath = pictureDirectory.getPath();
                            Uri data = Uri.parse(pictureDirectoryPath);
                            photoPickerIntent.setDataAndType(data, "image/*");
                            startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);


                        } else if (options[selection] == "Cancel") {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();

            }
        });

        Date d = Calendar.getInstance().getTime();
        sdf = new SimpleDateFormat("dd/MM/yyyy");
        validStartDate.setText(sdf.format(d));

        validEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                String enddate = dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year;
                                DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                                Date d;
                                try {
                                    d = df.parse(enddate);
                                    validEndDate.setText(sdf.format(d));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        requestStuff.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (validateInput() == true) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    try {

                                        if (categorySpinner.getSelectedItem().toString().equals("Books")) {
                                            category = "303031";
                                        } else if (categorySpinner.getSelectedItem().toString().equals("Electronics")) {
                                            category = "303032";
                                        } else if (categorySpinner.getSelectedItem().toString().equals("Furnitures")) {
                                            category = "303033";
                                        } else if (categorySpinner.getSelectedItem().toString().equals("Miscellaneous")) {
                                            category = "303034";
                                        }

                                        if (conditionSpinner.getSelectedItem().toString().equals("New")) {
                                            condition = "303031";
                                        } else if (conditionSpinner.getSelectedItem().toString().equals("Used")) {
                                            condition = "303032";
                                        }

                                        command = "{\"command\": \"303035303040\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                                "\"studentID\": " + "\"" + Conversion.asciiToHex(s.getStudentID()) + "\" ," +
                                                "\"stuffName\": " + "\"" + Conversion.asciiToHex(stuffName.getText().toString().trim()) + "\" ," +
                                                "\"stuffDescription\": " + "\"" + Conversion.asciiToHex(stuffDescription.getText().toString().trim()) + "\" ," +
                                                "\"stuffCategory\": " + "\"" + category + "\" ," +
                                                "\"stuffCondition\": " + "\"" + condition + "\" ," +
                                                "\"stuffPrice\": " + "\"" + Conversion.asciiToHex(stuffPrice.getText().toString().trim()) + "\" ," +
                                                "\"stuffQuantity\": " + "\"" + Conversion.asciiToHex(stuffQuantity.getText().toString().trim()) + "\" ," +
                                                "\"validStartDate\": " + "\"" + Conversion.asciiToHex(validStartDate.getText().toString().trim()) + "\" ," +
                                                "\"validEndDate\": " + "\"" + Conversion.asciiToHex(validEndDate.getText().toString().trim()) + "\"}";

                                        pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

                                    } catch (MqttException e) {
                                        e.printStackTrace();
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    getStuffID();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Confirm to upload?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
            }
        });

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getActivity(), Constant.serverUrl);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        converted = "";
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                cameraImage = (Bitmap) data.getExtras().get("data");
                captureImage.setImageBitmap(cameraImage);
                converted = bitmapToString(cameraImage);
            }

            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Uri imageUri = data.getData();
                InputStream inputStream;

                try {
                    inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                    cameraImage = BitmapFactory.decodeStream(inputStream);
                    captureImage.setImageBitmap(cameraImage);
                    converted = bitmapToString(cameraImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Unable to open image", Toast.LENGTH_LONG).show();
                }


            }
        }

    }


    public String bitmapToString(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] arr = baos.toByteArray();
        String result = Base64.encodeToString(arr, Base64.DEFAULT);
        return result;
    }


    public boolean validateInput() {

        error = "";
        boolean indicator = true;

        if (captureImage.getDrawable() == null) {
            error += "- No image is attached.\n";
            indicator = false;
        }
        if (stuffName.getText().toString().trim().equals("")) {
            error += "- Item name is missing.\n";
            indicator = false;
        }
        if (stuffName.getText().toString().trim().length() > 31) {
            error += "- No of characters for name has exceeded 30.\n";
            indicator = false;
        }
        if (stuffDescription.getText().toString().trim().equals("")) {
            error += "- Item description is missing.\n";
            indicator = false;
        }
        if (stuffDescription.getText().toString().trim().length() > 301) {
            error += "- No of characters for description has exceeded 300.\n";
            indicator = false;
        }
        if (stuffPrice.getText().toString().trim().equals("")) {
            error += "- Price is missing.\n";
            indicator = false;
        } else {
            if (stuffPrice.getText().toString().trim().matches("^[0-9]\\d*(\\.(\\d{1}|\\d{2}))?$")) {

                double price = 0;
                price = Double.parseDouble(stuffPrice.getText().toString().trim());
                if (price <= 0) {
                    error += "- Invalid price.\n";
                    indicator = false;
                }

            } else {
                error += "- Invalid price.\n";
                indicator = false;
            }
        }

        if (stuffQuantity.getText().toString().trim().equals("")) {
            error += "- Quantity is missing.\n";
            indicator = false;
        } else {
            if (!stuffQuantity.getText().toString().trim().matches("^[1-9]\\d*$")) {
                error += "- Invalid quantity.\n";
                indicator = false;

            }
        }

        if (categorySpinner.getSelectedItem().equals("--Category--")) {
            error += "- Category type is not selected.\n";
            indicator = false;
        }
        if (conditionSpinner.getSelectedItem().equals("--Condition--")) {
            error += "- Condition is not selected.\n";
            indicator = false;
        }
        if (validEndDate.getText().toString().equals("")) {
            error += "- End date is not selected.\n";
            indicator = false;
        }

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = df.parse(validStartDate.getText().toString());
            endDate = df.parse(validEndDate.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (endDate.before(startDate)) {
            error += "- Past date is not allowed.\n";
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


    public void getStuffID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(getActivity());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getRequestStuffID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    Log.d(TAG, "onResponse: request");
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject stuffIDResponse = (JSONObject) response.get(i);
                                        CurrentRequestID = stuffIDResponse.getString("CurrentRequestID");
                                    }
                                    Log.d(TAG,"curre"+ CurrentRequestID);
                                    if (CurrentRequestID.equals("0")) {
                                        requestID = "STR0001";
                                    } else {
                                        requestID = String.format("STR%04d", (Integer.parseInt(CurrentRequestID.substring(3, 7)) + 1));
                                    }
                                    insertStuffData();
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


    public void insertStuffData() {
        encodedStuffName = "";
        encodedStuffDescription = "";
        encodedImage = "";

        try {
            jsonObj = new JSONObject(command);
            if (jsonObj.getString("command").equals("303035303040")) {
                String[] encodeStuffName = {Conversion.hexToAscii(jsonObj.getString("stuffName"))};
                for (String s : encodeStuffName) {
                    encodedStuffName += URLEncoder.encode(s, "UTF-8");
                }
                String[] encodeStuffDescription = {Conversion.hexToAscii(jsonObj.getString("stuffDescription"))};
                for (String s : encodeStuffDescription) {
                    encodedStuffDescription += URLEncoder.encode(s, "UTF-8");
                }


                if (jsonObj.getString("stuffCategory").equals("303031")) {
                    category = "Books";
                } else if (jsonObj.getString("stuffCategory").equals("303032")) {
                    category = "Electronics";
                } else if (jsonObj.getString("stuffCategory").equals("303033")) {
                    category = "Furnitures";
                } else if (jsonObj.getString("stuffCategory").equals("303034")) {
                    category = "Miscellaneous";
                }

                if (jsonObj.getString("stuffCondition").equals("303031")) {
                    condition = "New";
                } else if (jsonObj.getString("stuffCondition").equals("303032")) {
                    condition = "Used";
                }

                jsonURL = Constant.serverFile + "insertRequestStuffData.php?requeststuffID=" + requestID + "&studentID="
                        + Conversion.hexToAscii(jsonObj.getString("studentID"))
                        + "&stuffName=" + encodedStuffName + "&stuffImage=" + "" + "&stuffDescription=" + encodedStuffDescription
                        + "&stuffCategory=" + category + "&stuffCondition=" + condition
                        + "&stuffPrice=" + Conversion.hexToAscii(jsonObj.getString("stuffPrice"))
                        + "&stuffQuantity=" + Conversion.hexToAscii(jsonObj.getString("stuffQuantity"))
                        + "&validStartDate=" + Conversion.hexToAscii(jsonObj.getString("validStartDate"))
                        + "&validEndDate=" + Conversion.hexToAscii(jsonObj.getString("validEndDate")) + "&stuffStatus=Active";

//                jsonURL = Constant.serverFile + "insertRequestStuffData.php?requeststuffID=" + "1" + "&studentID="
//                        + "2"
//                        + "&stuffName=" + "3" + "&stuffImage=" + "" + "&stuffDescription=" + "4"
//                        + "&stuffCategory=" + "5" + "&stuffCondition=" + "6"
//                        + "&stuffPrice=" + "7"
//                        + "&stuffQuantity=" + "8"
//                        + "&validStartDate=" + "9"
//                        + "&validEndDate=" + "10" + "&stuffStatus=Active";

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
                                            pahoMqttClient.publishMessage(mqttAndroidClient, s.getStudentName() + " listed an item for request", 1, s.getStudentID());
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

                                            builder.setTitle("Stuff is successfully added")
                                                    .setMessage(stuffName.getText().toString() + " is uploaded")
                                                    .setPositiveButton("OK", dialogClickListener).show();
                                        } else {
                                            builder.setTitle("Failed to add requests");
                                            builder.setMessage(stuffName.getText().toString() + " is failed to be uploaded");
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            });
                                            builder.show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    } catch (MqttException e) {
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
//                            try {
////                                params.put("stuffID", requestID);
////                                params.put("studentID", Conversion.hexToAscii(jsonObj.getString("studentID")));
////                                params.put("stuffName", Conversion.hexToAscii(jsonObj.getString("stuffName")));
                                params.put("stuffImage", converted);
////                                params.put("stuffDescription", Conversion.hexToAscii(jsonObj.getString("stuffDescription")));
////                                params.put("stuffCategory", category);
////                                params.put("stuffCondition", condition);
////                                params.put("stuffPrice", Conversion.hexToAscii(jsonObj.getString("stuffPrice")));
////                                params.put("stuffQuantity", Conversion.hexToAscii(jsonObj.getString("stuffQuantity")));
////                                params.put("validStartDate", Conversion.hexToAscii(jsonObj.getString("validStartDate")));
////                                params.put("validEndDate", Conversion.hexToAscii(jsonObj.getString("validEndDate")));
////                                params.put("stuffStatus", "Active");
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }

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

    public void clearData() {
        captureImage.setImageResource(0);
        stuffName.setText("");
        stuffDescription.setText("");
        stuffQuantity.setText("");
        stuffPrice.setText("");
        categorySpinner.setSelection(0);
        conditionSpinner.setSelection(0);
        validEndDate.setText("");
        Date d = Calendar.getInstance().getTime();
        sdf = new SimpleDateFormat("dd/MM/yyyy");
        validStartDate.setText(sdf.format(d));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
