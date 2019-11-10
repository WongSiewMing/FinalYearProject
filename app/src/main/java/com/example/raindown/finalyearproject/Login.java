package com.example.raindown.finalyearproject;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.json.*;

import java.util.*;

import Helper.*;

/*Author : Chin Wei Song
Programme : RSD3
Year : 2017*/

public class Login extends AppCompatActivity {

    ProgressDialog pDialog = null;
    EditText username, password;
    Button login;
    List<Student> studentList = new ArrayList<>();
    TextView showPassword, forgotPassword;
    String userName, userPassword;

    private static final String TAG = "testData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        pDialog = new ProgressDialog(this);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        login = (Button) findViewById(R.id.btnLogin);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStudentData();
            }
        });
        showPassword = (TextView) findViewById(R.id.showPassword);
        forgotPassword = (TextView) findViewById(R.id.forgot_password);

        showPassword.setVisibility(View.GONE);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(password.getText().length() > 0){
                    showPassword.setVisibility(View.VISIBLE);
                }
                else {
                    showPassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        showPassword.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                if(showPassword.getText() == "SHOW"){
                    showPassword.setText("HIDE");
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    password.setSelection(password.length());
                }
                else {
                    showPassword.setText("SHOW");
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    password.setSelection(password.length());
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                Intent intent = new Intent(Login.this, PasswordValidation.class);
                startActivity(intent);
                finish();
            }
        });

        if (PreferenceUnits.getID(this) != null) {
            getStudentData();
        }

    }

    public void getStudentData() {

        if (!username.getText().toString().matches("") || !password.getText().toString().matches("")){
            userName = username.getText().toString();
            userPassword = password.getText().toString();
        }
        else {
            userName = PreferenceUnits.getID(this);
            userPassword = PreferenceUnits.getPassword(this);
        }

        try {

            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();

            if (isConnected) {
                RequestQueue queue = Volley.newRequestQueue(this.getApplication());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                    pDialog.show();

                final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getStudentData.php?username=" + userName
                        + "&password=" + userPassword ,
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
                                    verifyStudent();
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


    public void verifyStudent() {
        if (studentList.size() == 0) {
            AlertDialog.Builder sent = new AlertDialog.Builder(Login.this);
            sent.setTitle("Login failed");
            sent.setMessage("Invalid username or password! Please try again");
            sent.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    username.setText("");
                    password.setText("");
                }
            });
            sent.show();
        } else {
            Intent intent = new Intent(Login.this, UpdateNavigation.class);
            intent.putExtra("Login", studentList.get(0));
            PreferenceUnits.saveID(userName, this);
            PreferenceUnits.savePassword(userPassword, this);
            studentList.clear();
            username.setText("");
            password.setText("");
            startActivity(intent);
            finish();
        }
    }
}
