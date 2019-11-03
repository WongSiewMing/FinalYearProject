package com.example.raindown.finalyearproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import Helper.Constant;
import Helper.Student;
import Helper.StudentBasicInfoOB;
import Helper.Stuff;

public class ChooseSellerStuff extends Fragment {

    View view;
    private Student myID;
    private String sellerID;
    private ImageView sellerPhoto;
    private TextView sellerName, sellerFaculty, sellerProgramme, sellerYear;
    private List<Student> sellerInfo = new ArrayList<>();

    private static final String TAG = "Seller";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        myID = (Student) bundle.getSerializable("UserData");
        sellerID = bundle.getString("ClickedUserID");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view =  inflater.inflate(R.layout.choose_seller_stuff, container, false);
        sellerPhoto = view.findViewById(R.id.sellerPhoto);
        sellerName = view.findViewById(R.id.sellerName);
        sellerFaculty = view.findViewById(R.id.sellerFaculty);
        sellerProgramme = view.findViewById(R.id.sellerProgramme);
        sellerYear = view.findViewById(R.id.sellerYear);

        Log.d(TAG, sellerID);
        getSellerInfo();
        return view;
    }

    private void getSellerInfo() {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {
                Log.d(TAG, "Hi");
                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getSellerData.php?username=" + sellerID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {

                                try {
                                    sellerInfo.clear();
                                    JSONObject infoResponse = (JSONObject) response.get(0);
                                    sellerInfo.add(new Student(infoResponse.getString("studentID"),
                                            infoResponse.getString("photo"),
                                            infoResponse.getString("studentName"),
                                            infoResponse.getString("icNo"),
                                            infoResponse.getString("studentProgramme"),
                                            infoResponse.getString("studentFaculty"),
                                            infoResponse.getInt("yearOfStudy")));


                                    if (infoResponse.length() == 0) {
                                        Toast.makeText(getActivity().getApplication(), "Error retrieve user data...",
                                                Toast.LENGTH_LONG).show();

                                        getFragmentManager().popBackStack();
                                    } else {
                                        populateSellerInfo();
                                        //getPreviousPrivateChat();
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

    private void populateSellerInfo(){

        Picasso.with(getActivity()).load(sellerInfo.get(0).getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(sellerPhoto);
        sellerName.setText(sellerInfo.get(0).getStudentName());
        sellerFaculty.setText(sellerInfo.get(0).getStudentFaculty());
        sellerProgramme.setText(sellerInfo.get(0).getStudentFaculty());
        sellerYear.setText(sellerInfo.get(0).getYearOfStudy());
    }

}
