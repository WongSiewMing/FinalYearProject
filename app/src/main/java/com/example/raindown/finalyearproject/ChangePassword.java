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
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;

public class ChangePassword extends AppCompatActivity {

    ProgressDialog pDialog = null;
    TextView instruction, backtoLogin, showChangePassword, showConfirmPassword;
    EditText newPassword, confirmPassword;
    Button btnConfirm, btnEdit;
    JSONObject jsonObj;
    String command = "", jsonURL = "",password ,confirmPW = "", encodedStudentName = "";
    private static final String TAG = "testData";

    public static Student student = null;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient mqttAndroidClient;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);

        Intent intent = getIntent();
        student = (Student) intent.getSerializableExtra("PasswordValidation");

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(ChangePassword.this , Constant.serverUrl, "MY/TARUC/SSS/000000001/PUB");

        pDialog = new ProgressDialog(this);
        instruction = findViewById(R.id.changePasswordInstruction);
        backtoLogin = findViewById(R.id.changePasswordBackToLogin);
        showChangePassword = findViewById(R.id.showChangePassword);
        showConfirmPassword = findViewById(R.id.showConfirmPassword);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnEdit = findViewById(R.id.btnChangePassword);

        showChangePassword.setVisibility(View.GONE);
        newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        confirmPassword.setVisibility(View.INVISIBLE);
        showConfirmPassword.setVisibility(View.GONE);
        confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        btnEdit.setVisibility(View.INVISIBLE);
        btnConfirm.setText("NEXT");
        instruction.setText("1. Enter Your New Password");

        backtoLogin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(ChangePassword.this);

                builder.setMessage("Back to Login ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(ChangePassword.this, Login.class);
                                startActivity(intent);
                                newPassword.setText("");
                                confirmPassword.setText("");
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

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(newPassword.getText().length() > 0){
                    showChangePassword.setVisibility(View.VISIBLE);
                }
                else {
                    showChangePassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        showChangePassword.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                if(showChangePassword.getText() == "SHOW"){
                    showChangePassword.setText("HIDE");
                    newPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    newPassword.setSelection(newPassword.length());
                }
                else {
                    showChangePassword.setText("SHOW");
                    newPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    newPassword.setSelection(newPassword.length());
                }
            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(confirmPassword.getText().length() > 0){
                    showConfirmPassword.setVisibility(View.VISIBLE);
                }
                else {
                    showConfirmPassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        showConfirmPassword.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                if(showConfirmPassword.getText() == "SHOW"){
                    showConfirmPassword.setText("HIDE");
                    confirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confirmPassword.setSelection(confirmPassword.length());
                }
                else {
                    showConfirmPassword.setText("SHOW");
                    confirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confirmPassword.setSelection(confirmPassword.length());
                }
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String buttonText = btnConfirm.getText().toString();
                if (buttonText.equals("NEXT")) {
                    verifyPassword();
                }
                else if(buttonText.equals("CHANGE PASSWORD")){
                    password = newPassword.getText().toString().trim();
                    confirmPW = confirmPassword.getText().toString().trim();

                    if(password.equals(confirmPW)){
                        try {
                        command = "{\"command\": \"303035303071\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                "\"studentID\": " + "\"" + Conversion.asciiToHex(student.getStudentID()) + "\" ," +
                                "\"photo\": " + "\"" + Conversion.asciiToHex(student.getPhoto()) + "\" ," +
                                "\"studentName\": " + "\"" + Conversion.asciiToHex(student.getStudentName()) + "\" ," +
                                "\"icNo\": " + "\"" + Conversion.asciiToHex(confirmPW) + "\" ," +
                                "\"studentProgramme\": " + "\"" + Conversion.asciiToHex(student.getStudentProgramme()) + "\" ," +
                                "\"studentFaculty\": " + "\"" + Conversion.asciiToHex(student.getStudentFaculty()) + "\" ," +
                                "\"yearOfStudy\": " + "\"" + student.getYearOfStudy() + "\"}";

                        pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                        } catch (MqttException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        updatePassword();
                    }
                    else{
                        AlertDialog.Builder sent = new AlertDialog.Builder(ChangePassword.this);
                        sent.setTitle("Mismatched Password");
                        sent.setMessage("Your password and confirmation password do not match.\nPlease try again.");
                        sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        sent.show();
                        confirmPassword.setText("");
                    }
                }
            }
        });


        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmPassword.setText("SHOW");
                newPassword.setFocusable(true);
                confirmPassword.setText("");
                confirmPassword.setVisibility(View.INVISIBLE);
                btnEdit.setVisibility(View.INVISIBLE);
                btnConfirm.setText("NEXT");
                instruction.setText("1. Enter Your New Password");
            }
        });
    }

    public void verifyPassword() {
        String password = newPassword.getText().toString().trim();

        if(student.getIcNo().equals(password)){
            AlertDialog.Builder sent = new AlertDialog.Builder(ChangePassword.this);
            sent.setTitle("Invalid Password");
            sent.setMessage("New password cannot be same with old password.\nPlease choose another password.");
            sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            sent.show();
            newPassword.setText("");
        }
        else{
            instruction.setText("2. Confirm Your New Password");
            newPassword.setFocusable(true);
            btnEdit.setVisibility(View.VISIBLE);
            btnConfirm.setText("CHANGE PASSWORD");
            confirmPassword.setVisibility(View.VISIBLE);
        }
    }

    public void updatePassword() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) ChangePassword.this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                encodedStudentName = "";

                try {

                    jsonObj = new JSONObject(command);
                    if (jsonObj.getString("command").equals("303035303071")) {
                        String[] encodeStudentName = {Conversion.hexToAscii(jsonObj.getString("studentName"))};
                        for (String s : encodeStudentName) {
                            encodedStudentName += URLEncoder.encode(s, "UTF-8");
                        }

                        jsonURL = Constant.serverFile + "updatePassword.php?studentID=" + Conversion.hexToAscii(jsonObj.getString("studentID"))
                                + "&photo=" + Conversion.hexToAscii(jsonObj.getString("photo"))
                                + "&studentName=" + encodedStudentName
                                + "&icNo=" + Conversion.hexToAscii(jsonObj.getString("icNo"))
                                + "&studentProgramme=" + Conversion.hexToAscii(jsonObj.getString("studentProgramme"))
                                + "&studentFaculty=" + Conversion.hexToAscii(jsonObj.getString("studentFaculty"))
                                + "&yearOfStudy=" + jsonObj.getString("yearOfStudy");

                        RequestQueue queue = Volley.newRequestQueue(ChangePassword.this);
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
                                                AlertDialog.Builder builder = new AlertDialog.Builder(ChangePassword.this);
                                                Log.d(TAG, success);
                                                if (success.equals("1")) {
                                                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            switch (which) {
                                                                case DialogInterface.BUTTON_POSITIVE:
                                                                    Intent intent = new Intent(ChangePassword.this, Login.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                            }
                                                        }
                                                    };

                                                    builder.setTitle("Password Updated")
                                                            .setMessage("Your password is successfully updated")
                                                            .setPositiveButton("Back to Login", dialogClickListener).show();
                                                } else {
                                                    builder.setTitle("Updated Failed");
                                                    builder.setMessage("Your password is failed to updated");
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
                                        params.put("studentID", Conversion.hexToAscii(jsonObj.getString("studentID")));
                                        params.put("photo", Conversion.hexToAscii(jsonObj.getString("photo")));
                                        params.put("studentName", Conversion.hexToAscii(jsonObj.getString("studentName")));
                                        params.put("icNo", Conversion.hexToAscii(jsonObj.getString("icNo")));
                                        params.put("studentProgramme", Conversion.hexToAscii(jsonObj.getString("studentProgramme")));
                                        params.put("studentFaculty", Conversion.hexToAscii(jsonObj.getString("studentFaculty")));
                                        params.put("yearOfStudy", jsonObj.getString("yearOfStudy"));
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

                    } catch(Exception e){
                        e.printStackTrace();
                    }

                } else{
                    Toast.makeText(ChangePassword.this.getApplication(), "Network is NOT available",
                            Toast.LENGTH_LONG).show();
                }
            } catch(Exception e){
                Toast.makeText(ChangePassword.this.getApplication(),
                        "Error reading record:" + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
}
