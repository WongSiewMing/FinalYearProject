package com.example.raindown.finalyearproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import Helper.Constant;
import Helper.Conversion;
import Helper.PreferenceUnits;
import Helper.Student;

public class PasswordValidation extends AppCompatActivity {

    ProgressDialog pDialog = null;
    TextView instruction, back;
    EditText userID, email, resetCode;
    Button validate;
    List<Student> studentList = new ArrayList<>();
    String userEmail, currentResetPWID = "", resetPWID = "", command = "", jsonURL = "", encodedEmail = "", encodedResetCode = "";
    JSONObject jsonObj;

    private static final String TAG = "testData";

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_validation);

        pDialog = new ProgressDialog(this);
        instruction = findViewById(R.id.instruction);
        userID = findViewById(R.id.userID);
        email = findViewById(R.id.email);
        resetCode = findViewById(R.id.reset_code);
        back = findViewById(R.id.backToLogin);
        validate = findViewById(R.id.btnValidate);
        userEmail = email.getText().toString();

        email.setVisibility(View.INVISIBLE);
        resetCode.setVisibility(View.INVISIBLE);
        validate.setText("NEXT");
        instruction.setText("1. Enter Your Student ID");

        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(PasswordValidation.this, Login.class);
                startActivity(intent);
                userID.setText("");
                email.setText("");
                resetCode.setText("");
                finish();
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = validate.getText().toString();
                if (buttonText.equals("NEXT")){
                    getUserID();
                } else if (buttonText.equals("SEND EMAIL")){
                    //Log.d(TAG, "Reset Code :" + randomString(20).toString());
                }

            }
        });
    }

    public void getUserID() {
        try {

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(this.getApplication());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStudentID.php?username=" + userID.getText().toString() ,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                System.out.println("We are here");
                                try {

                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject studentResponse = (JSONObject) response.get(i);
                                        studentList.add(new Student(studentResponse.getString("studentID"), studentResponse.getString("photo"),
                                                studentResponse.getString("studentName"), studentResponse.getString("icNo"),
                                                studentResponse.getString("studentProgramme"), studentResponse.getString("studentFaculty"),
                                                studentResponse.getInt("yearOfStudy")));

                                    }
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                    verifyUser();
                                } catch (Exception e) {
                                    System.out.println("Error catch");
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
                Toast.makeText(this.getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this.getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    public void verifyUser() {
        if (studentList.size() == 0) {
            AlertDialog.Builder sent = new AlertDialog.Builder(PasswordValidation.this);
            sent.setTitle("Validation failed");
            sent.setMessage("User ID Not Found ! Please try again !");
            sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    userID.setText("");
                }
            });
            sent.show();
        } else {
            userID.setFocusable(false);
            instruction.setText("2. Enter Your Email");
            email.setVisibility(View.VISIBLE);
            validate.setText("SEND EMAIL");
        }
    }

    public void getResetPWID() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) PasswordValidation.this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                RequestQueue queue = Volley.newRequestQueue(PasswordValidation.this);
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "generateResetPasswordID.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject resetPWIDResponse = (JSONObject) response.get(i);
                                        currentResetPWID = resetPWIDResponse.getString("CurrentResetPasswordID");
                                    }
                                    if (currentResetPWID.equals("0")) {
                                        resetPWID = "RPW0001";
                                    } else {
                                        resetPWID = String.format("RPW%04d", (Integer.parseInt(currentResetPWID.substring(3, 7)) + 1));
                                    }
                                   // insertResetPWData();
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
                Toast.makeText(PasswordValidation.this.getApplication(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(PasswordValidation.this.getApplication(),
                    "Error reading record:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

//    public void insertResetPWData() {
//        encodedEmail = "";
//        encodedResetCode = "";
//
//        try {
//            jsonObj = new JSONObject(command);
//            if(jsonObj.getString("command").equals("303035303100")){
//
//                String[] encodeEmail = {Conversion.hexToAscii(jsonObj.getString("userEmail"))};
//                for (String s : encodeEmail) {
//                    encodedEmail += URLEncoder.encode(s, "UTF-8");
//                }
//
//                String[] encodeResetCode = {Conversion.hexToAscii(jsonObj.getString("resetCode"))};
//                for (String s : encodeResetCode) {
//                    encodedResetCode += URLEncoder.encode(s, "UTF-8");
//                }
//
//                jsonURL = Constant.serverFile + "insertResetPasswordRecord.php?resetPasswordID=" + resetPWID + "&studentID="
//                        + Conversion.hexToAscii(jsonObj.getString("studentID"))
//                        + "&userEmail=" + encodedEmail + "&resetCode=" + encodedResetCode;
//
//
//                RequestQueue queue = Volley.newRequestQueue(PasswordValidation.this);
//                try {
//                    StringRequest postRequest = new StringRequest(
//                            Request.Method.POST,
//                            jsonURL,
//                            new Response.Listener<String>() {
//                                @Override
//                                public void onResponse(String response) {
//                                    if (isAdded()) {
//                                    }
//                                    JSONObject jsonObject = null;
//                                    try {
//                                        jsonObject = new JSONObject(response);
//                                        String success = jsonObject.getString("success");
//                                        AlertDialog.Builder builder = new AlertDialog.Builder(PasswordValidation.this);
//                                        if (success.equals("1")) {
//                                            pahoMqttClient.publishMessage(mqttAndroidClient, s.getStudentName() + " listed an item for sale", 1, s.getStudentID());
//                                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    switch (which) {
//                                                        case DialogInterface.BUTTON_POSITIVE:
//                                                            clearData();
//
//                                                            break;
//                                                    }
//                                                }
//                                            };
//
//                                            builder.setTitle("Stuff is successfully added")
//                                                    .setMessage(stuffName.getText().toString() + " is uploaded")
//                                                    .setPositiveButton("OK", dialogClickListener).show();
//                                        } else {
//                                            builder.setTitle("Failed to add stuff");
//                                            builder.setMessage(stuffName.getText().toString() + " is failed to be uploaded");
//                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                                @Override
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                }
//                                            });
//                                            builder.show();
//                                        }
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    } catch (UnsupportedEncodingException e) {
//                                        e.printStackTrace();
//                                    } catch (MqttException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            },
//                            new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    if (isAdded()) {
//                                    }
//
//                                }
//                            }) {
//                        @Override
//                        protected Map<String, String> getParams() {
//                            Map<String, String> params = new HashMap<>();
//                            try {
//                                params.put("stuffID", stuffID);
//                                params.put("studentID", Conversion.hexToAscii(jsonObj.getString("studentID")));
//                                params.put("stuffName", Conversion.hexToAscii(jsonObj.getString("stuffName")));
//                                params.put("stuffImage", converted);
//                                params.put("stuffDescription", Conversion.hexToAscii(jsonObj.getString("stuffDescription")));
//                                params.put("stuffCategory", category);
//                                params.put("stuffCondition", condition);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                            return params;
//                        }
//
//                        @Override
//                        public Map<String, String> getHeaders() throws AuthFailureError {
//                            Map<String, String> params = new HashMap<>();
//                            params.put("Content-Type", "application/x-www-form-urlencoded");
//                            return params;
//                        }
//                    };
//                    queue.add(postRequest);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (isAdded()) {
//            }
//        }
//    }

}
