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

import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.eclipse.paho.android.service.MqttAndroidClient;
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

import Helper.*;

public class PasswordValidation extends AppCompatActivity {

    ProgressDialog pDialog = null;
    TextView instruction, back;
    EditText userID, email, resetCode;
    Button validate;
    List<Student> studentList = new ArrayList<>();
    List<ResetCode> resetCodeList = new ArrayList<>();
    String userEmail, currentResetPWID = "", resetPWID = "", command = "", jsonURL = "", encodedStudentID = "", encodedEmail = "", encodedResetCode = "", randomCode = "";
    JSONObject jsonObj;

    private static final String TAG = "testData";
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;

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

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(PasswordValidation.this , Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");

        pDialog = new ProgressDialog(this);
        instruction = findViewById(R.id.instruction);
        userID = findViewById(R.id.userID);
        email = findViewById(R.id.email);
        resetCode = findViewById(R.id.reset_code);
        back = findViewById(R.id.backToLogin);
        validate = findViewById(R.id.btnValidate);
        userEmail = email.getText().toString();
        randomCode = randomString(20);

        email.setVisibility(View.INVISIBLE);
        resetCode.setVisibility(View.INVISIBLE);
        validate.setText("NEXT");
        instruction.setText("1. Enter Your Student ID");

        back.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(PasswordValidation.this);

                builder.setMessage("Back to Login ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(PasswordValidation.this, Login.class);
                                startActivity(intent);
                                userID.setText("");
                                email.setText("");
                                resetCode.setText("");
                                finish();
                            }
                        })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                android.support.v7.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = validate.getText().toString();
                if (buttonText.equals("NEXT")) {
                    getUserID();
                } else if (buttonText.equals("SEND EMAIL")) {

                    if(isValidEmail(email.getText().toString().trim())){
                        try {
                            command = "{\"command\": \"303035303070\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                    "\"StudentID\": " + "\"" + Conversion.asciiToHex(userID.getText().toString().trim()) + "\" ," +
                                    "\"userEmail\": " + "\"" + Conversion.asciiToHex(email.getText().toString().trim()) + "\" ," +
                                    "\"resetCode\": " + "\"" + Conversion.asciiToHex(randomCode) + "\"}";

                            pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

                        } catch (MqttException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        getResetPWID();
                    }
                    else{
                        AlertDialog.Builder sent = new AlertDialog.Builder(PasswordValidation.this);
                        sent.setTitle("Validation failed");
                        sent.setMessage("Invalid email address ! \nPlease provide a valid email address.");
                        sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                email.setText("");
                            }
                        });
                        sent.show();
                    }

                }else if (buttonText.equals("Verify")) {
                    getResetCode();
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

    public void getResetCode() {
        try {

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(this.getApplication());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();

                final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getResetPWID.php?resetPWID=" + resetPWID ,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                System.out.println("We are here");
                                try {

                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject resetCodeResponse = (JSONObject) response.get(i);
                                        resetCodeList.add(new ResetCode(resetCodeResponse.getString("resetPasswordID"),
                                                resetCodeResponse.getString("StudentID"),
                                                resetCodeResponse.getString("userEmail"),
                                                resetCodeResponse.getString("resetCode")));

                                    }
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                    verifyCode();
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

    public void verifyCode() {
        String verificationCode = resetCodeList.get(0).getResetCode();

        if(resetCode.getText().toString().trim().equals(verificationCode)){
            Intent intent = new Intent(PasswordValidation.this, ChangePassword.class);
            intent.putExtra("PasswordValidation", studentList.get(0));
            studentList.clear();
            userID.setText("");
            email.setText("");
            resetCode.setText("");
            startActivity(intent);
            finish();
        }
        else{
            AlertDialog.Builder sent = new AlertDialog.Builder(PasswordValidation.this);
            sent.setTitle("Validation Failed");
            sent.setMessage("Invalid verification code.\nPlease try enter again.");
            sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            sent.show();
            resetCode.setText("");
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
                                    insertResetPWData();
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

    public void insertResetPWData() {
        encodedStudentID = "";
        encodedEmail = "";
        encodedResetCode = "";

        try {
            jsonObj = new JSONObject(command);
            if(jsonObj.getString("command").equals("303035303070")){

                String[] encodeStudentID = {Conversion.hexToAscii(jsonObj.getString("StudentID"))};
                for (String s : encodeStudentID) {
                    encodedStudentID += URLEncoder.encode(s, "UTF-8");
                }

                String[] encodeEmail = {Conversion.hexToAscii(jsonObj.getString("userEmail"))};
                for (String s : encodeEmail) {
                    encodedEmail += URLEncoder.encode(s, "UTF-8");
                }

                String[] encodeResetCode = {Conversion.hexToAscii(jsonObj.getString("resetCode"))};
                for (String s : encodeResetCode) {
                    encodedResetCode += URLEncoder.encode(s, "UTF-8");
                }

                jsonURL = Constant.serverFile + "insertResetPasswordRecord.php?resetPasswordID=" + resetPWID +
                        "&StudentID=" + encodedStudentID +
                        "&userEmail=" + encodedEmail +
                        "&resetCode=" + encodedResetCode;

                RequestQueue queue = Volley.newRequestQueue(PasswordValidation.this);
                try {
                    StringRequest postRequest = new StringRequest(
                            Request.Method.POST,
                            jsonURL,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(response);
                                        String success = jsonObject.getString("success");
                                        AlertDialog.Builder builder = new AlertDialog.Builder(PasswordValidation.this);
                                        if (success.equals("1")) {

                                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            break;
                                                    }
                                                }
                                            };

                                            builder.setTitle("Verification Code Sent");
                                            builder.setMessage("A verification code has been sent.\nPlease check your email inbox.")
                                                    .setPositiveButton("OK", dialogClickListener).show();
                                            email.setFocusable(false);
                                            instruction.setText("3. Enter Your Verification Code");
                                            resetCode.setVisibility(View.VISIBLE);
                                            validate.setText("Verify");
                                            sendEmail();

                                        } else {
                                            builder.setMessage("An Error Occurred ! Please Try Again.");
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
                            try {
                                params.put("resetPasswordID", resetPWID);
                                params.put("StudentID", Conversion.hexToAscii(jsonObj.getString("StudentID")));
                                params.put("userEmail", Conversion.hexToAscii(jsonObj.getString("userEmail")));
                                params.put("resetCode", Conversion.hexToAscii(jsonObj.getString("resetCode")));
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

        }
    }

    static boolean isValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }


    private void sendEmail() {
        final ProgressDialog dialog = new ProgressDialog(PasswordValidation.this);
        dialog.setTitle("Sending Email");
        dialog.setMessage("Please wait");
        dialog.show();
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EmailSender sender = new EmailSender("buyandsellnotreply@gmail.com", "buyandsell");
                    sender.sendMail("Buy + Sell Email Verification",
                            "Here's the verification code you need to continue with your password changing process : \n\n" + randomCode + "\n\nHave a nice day.\nBuy + Sell",
                            "BuyAndSell",
                            email.getText().toString());
                    dialog.dismiss();
                } catch (Exception e) {
                    Log.d("mylog", "Error: " + e.getMessage());
                }
            }
        });
        sender.start();
    }
}
