package com.example.raindown.finalyearproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Helper.ActivityLikeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.StoreBasicInfoOB;
import Helper.Student;
import Helper.StudentBasicInfoOB;
import Helper.Stuff;
import Helper.UserActivityOB;

/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/


public class UserActivity extends Fragment {
    private Button createActivity;
    private Switch btnSwitch;

    View view;
    FragmentManager fragmentManager;
    private ProgressBar progressBar;
    private RecyclerView activityListrv;
    private ProgressDialog pDialog = null;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;
    private String command, UserID;
    private ArrayList<UserActivityOB> activityList;
    private List<Stuff> arrayMyStuff = new ArrayList<>();
    private Student s;

    private RecyclerView mRecycleView;
    private ActivityRecyclerViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    private static final String TAG = "User Activity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("User Activity");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_activity, container, false);
        Bundle bundle = getArguments();
        s = (Student) bundle.getSerializable("UserActivity");
        activityList = new ArrayList<>();
        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        Log.d(TAG, "User ID =" + UserID);
        createActivity = view.findViewById(R.id.create_activity);
        progressBar = view.findViewById(R.id.activityProgressBar);
        activityListrv = view.findViewById(R.id.activityListrv);
        btnSwitch = view.findViewById(R.id.btnSwitch);
        btnSwitch.setChecked(false);
        populateActivityList();

        createActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CreateActivity createActivity = new CreateActivity();
                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, createActivity)
                        .addToBackStack(null)
                        .commit();
            }
        });

        btnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    populateFollowingActivityList();
                } else {
                    populateActivityList();
                }
            }
        });

        pahoMqttClient = new PahoMqttClient();
        mqttAndroidClient = pahoMqttClient.getMqttClient(getContext(), Constant.serverUrl);

        return view;
    }

    private void populateActivityList() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {


                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getUserActivity.php",
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    activityList.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject activityResponse = (JSONObject) response.get(i);
                                        activityList.add(new UserActivityOB(activityResponse.getString("activityID"), new StudentBasicInfoOB(activityResponse.getString("studentID"),
                                                activityResponse.getString("photo"), activityResponse.getString("studentName"), activityResponse.getString("studentProgramme")),
                                                activityResponse.getString("stuffID"), activityResponse.getString("activityCaption"), activityResponse.getString("activityThumbnail"),
                                                activityResponse.getString("uploadDate"), activityResponse.getString("uploadTime"), activityResponse.getString("activityStatus")));

                                    }

                                    Log.d(TAG, "Respond length = " + response.length());
                                    command = "{\"command\": \"303035303069\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                            "\"StudentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" }";

                                    pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                                    if (response.length() > 0) {
                                        populateRecycleView();
                                    }else{
                                        Toast.makeText(getActivity().getApplication(), "Seem like no one posted anything...", Toast.LENGTH_LONG).show();
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

    private void populateFollowingActivityList() {

        try {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {


                RequestQueue queue = Volley.newRequestQueue(getActivity());

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getUserActivityFollowingOnly.php?subscriberID=" + UserID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    activityList.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject activityResponse = (JSONObject) response.get(i);
                                        activityList.add(new UserActivityOB(activityResponse.getString("activityID"), new StudentBasicInfoOB(activityResponse.getString("studentID"),
                                                activityResponse.getString("photo"), activityResponse.getString("studentName"), activityResponse.getString("studentProgramme")),
                                                activityResponse.getString("stuffID"), activityResponse.getString("activityCaption"), activityResponse.getString("activityThumbnail"),
                                                activityResponse.getString("uploadDate"), activityResponse.getString("uploadTime"), activityResponse.getString("activityStatus")));

                                    }

                                    Log.d(TAG, "Respond length = " + response.length());
                                    command = "{\"command\": \"303035303090\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                                            "\"StudentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" }";

                                    pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                                    if (response.length() > 0) {
                                        populateRecycleView();
                                    }else{
                                        Toast.makeText(getActivity().getApplication(), "Seem like no one posted anything...", Toast.LENGTH_LONG).show();
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



    private void populateRecycleView() {
        mRecycleView = view.findViewById(R.id.activityListrv);
        mRecycleView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new ActivityRecyclerViewAdapter(activityList, view.getContext(), mqttAndroidClient, pahoMqttClient);
        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setLayoutManager(mLayoutManager);
        progressBar.setVisibility(View.GONE);
        activityListrv.setVisibility(View.VISIBLE);

        mAdapter.setOnItemClickListener(new ActivityRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onCommentClick(int position) {
                ActivityComment frag = new ActivityComment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("activityID", activityList.get(position).getActivityID());
                bundle.putSerializable("posterName", activityList.get(position).getStudentID().getStudentName());
                bundle.putSerializable("activityCaption", activityList.get(position).getActivityCaption());
                frag.setArguments(bundle);

                fragmentManager = ((AppCompatActivity) view.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onBookmarkClick(int position) {

            }

            @Override
            public void onShareClick(int position) {
                try {
                    Uri uriImage = Uri.parse(activityList.get(position).getActivityThumbnail());
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, activityList.get(position).getActivityCaption());
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uriImage);
                    sendIntent.setType("image/*");
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Intent shareIntent = Intent.createChooser(sendIntent, "Share Image via : ");
                    view.getContext().startActivity(shareIntent);
                } catch (Exception e){
                    Log.d(TAG, "Sharing Activity Failed : " + e.getMessage());
                }
            }

            @Override
            public void onViewStuffClick(int position) {
                getMySelectedStuff(activityList.get(position).getStuffID(), view, position);
            }

            @Override
            public void onViewPosterClick(int position) {
                Dialog dialog;
                ImageView basicInfoPhoto;
                TextView basicInfoName, basicInfoProgramme;
                //               Button btnviewProfile;
                dialog = new Dialog(view.getContext());
                dialog.setContentView(R.layout.userbasicinfo);
                basicInfoPhoto = dialog.findViewById(R.id.basicInfoPhoto);
                basicInfoName = dialog.findViewById(R.id.basicInfoName);
                basicInfoProgramme = dialog.findViewById(R.id.basicInfoProgramme);
                Picasso.with(view.getContext()).load(activityList.get(position).getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(basicInfoPhoto);
                basicInfoName.setText(activityList.get(position).getStudentID().getStudentName());
                basicInfoProgramme.setText(activityList.get(position).getStudentID().getStudentProgramme());
                //              btnviewProfile = dialog.findViewById(R.id.btnViewProfile);

                dialog.show();
            }
        });
    }

    private void getMySelectedStuff(String stuffID, final View v, final int position) {
        Log.d(TAG, "Stuff ID Clicked = " + stuffID);
        pDialog = new ProgressDialog(v.getContext());
        try {
            ConnectivityManager connMgr = (ConnectivityManager) v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {


                try {
                    command = "{\"command\": \"30303530304D\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"StudentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" ," +
                            "\"StuffID\": " + "\"" + Conversion.asciiToHex(stuffID) + "\" }";

                    pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");

                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                RequestQueue queue = Volley.newRequestQueue(v.getContext());
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();
                //http://192.168.0.107/raindown/getMyAttachedStuff.php?stuffID=STF0024
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getMyAttachedStuff.php?stuffID=" + stuffID,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    Log.d(TAG, "Stuff ID responded");
                                    arrayMyStuff.clear();
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject myStuffResponse = (JSONObject) response.get(i);
                                        arrayMyStuff.add(new Stuff(myStuffResponse.getString("stuffID"), new Student(myStuffResponse.getString("studentID"),
                                                myStuffResponse.getString("clientID"), myStuffResponse.getString("photo"), myStuffResponse.getString("studentName"),
                                                myStuffResponse.getString("icNo"), myStuffResponse.getString("studentProgramme"), myStuffResponse.getString("studentFaculty"),
                                                myStuffResponse.getInt("yearOfStudy")), myStuffResponse.getString("stuffName"), myStuffResponse.getString("stuffImage"),
                                                myStuffResponse.getString("stuffDescription"), myStuffResponse.getString("stuffCategory"), myStuffResponse.getString("stuffCondition"),
                                                myStuffResponse.getDouble("stuffPrice"), myStuffResponse.getInt("stuffQuantity"), myStuffResponse.getString("validStartDate"),
                                                myStuffResponse.getString("validEndDate"), myStuffResponse.getString("stuffStatus")));
                                    }
                                    Log.d(TAG, "Respond length =" + response.length());
                                    Log.d(TAG, "arrayMyStuff length =" + arrayMyStuff.size());
                                    if (response.length() == 0){
                                        Log.d(TAG, "Respond length is zero");
                                        AlertDialog.Builder noItem = new AlertDialog.Builder(v.getContext());
                                        noItem.setTitle("Alert");
                                        noItem.setMessage("Item no longer available");
                                        noItem.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                        noItem.show();

                                    }else {
                                        Log.d(TAG, "Respond length is not zero " + response.length());
                                        Stuff clickedStuff = arrayMyStuff.get(0);
                                        StuffDetails frag = new StuffDetails();
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("ClickedStuff", clickedStuff);
                                        bundle.putSerializable("StudentClickedStuff", s);
                                        frag.setArguments(bundle);

                                        fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                                        fragmentManager.beginTransaction()
                                                .replace(R.id.update_fragmentHolder, frag)
                                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                                .addToBackStack(null)
                                                .commit();

//                                        if (mData.get(position).getStudentID().getStudentID().equals(UserID)) {
//
//                                            Stuff clickedStuff = arrayMyStuff.get(0);
//                                            MaintainStuff frag = new MaintainStuff();
//                                            Bundle bundle = new Bundle();
//                                            bundle.putSerializable("MaintainStuff", clickedStuff);
//                                            frag.setArguments(bundle);
//
//                                            fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
//                                            fragmentManager.beginTransaction()
//                                                    .replace(R.id.fragmentHolder, frag)
//                                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                                                    .addToBackStack(null)
//                                                    .commit();
//                                        } else {
//                                            Log.d(TAG, "Retrieve other user item...");
//                                            Stuff clickedStuff = arrayMyStuff.get(0);
//                                            StuffDetails frag = new StuffDetails();
//                                            Bundle bundle = new Bundle();
//                                            bundle.putSerializable("ClickedStuff", clickedStuff);
//                                            bundle.putSerializable("StudentClickedStuff", s);
//                                            frag.setArguments(bundle);
//
//                                            fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
//                                            fragmentManager.beginTransaction()
//                                                    .replace(R.id.fragmentHolder, frag)
//                                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                                                    .addToBackStack(null)
//                                                    .commit();
//                                        }
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
                Toast.makeText(v.getContext().getApplicationContext(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(v.getContext().getApplicationContext(),
                    "Error create activity:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

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
    public void onResume() {
        super.onResume();
        getActivity().setTitle("User Activity");
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
