package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
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
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.ActivityLikeOB;
import Helper.Constant;
import Helper.Conversion;
import Helper.PahoMqttClient;
import Helper.Student;
import Helper.Stuff;
import Helper.UserActivityOB;

public class ActivityRecyclerViewAdapter extends RecyclerView.Adapter<ActivityRecyclerViewAdapter.MyviewHolder> {

    private Context mContext;
    private List<UserActivityOB> mData;
    private Student s;
    private MqttAndroidClient mqttAndroidClient;
    private PahoMqttClient pahoMqttClient;

    private List<ActivityLikeOB> likeStatus;
    private List<Stuff> arrayMyStuff = new ArrayList<>();
    private String likeRespond = "";
    FragmentManager fragmentManager;
    private static final String TAG = "User Activity";
    private ProgressDialog pDialog = null;
    private String UserID, insertNewLikeActivityUrl = "";
    private String command;


    public ActivityRecyclerViewAdapter(Context mContext, List<UserActivityOB> mData, Student s, MqttAndroidClient mqttAndroidClient, PahoMqttClient pahoMqttClient) {
        this.mContext = mContext;
        this.mData = mData;
        this.s = s;
        this.mqttAndroidClient = mqttAndroidClient;
        this.pahoMqttClient = pahoMqttClient;
    }

    @Override
    public MyviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.user_activity_cardview, parent, false);

        return new MyviewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyviewHolder holder, final int position) {

        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);

//        Picasso.with(mContext).load(mData.get(position).getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.posterPhoto);
        Picasso.with(mContext).load(mData.get(position).getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).transform(new CircleTransform()).into(holder.posterPhoto);


        holder.posterPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog;
                ImageView basicInfoPhoto;
                TextView basicInfoName, basicInfoProgramme;
                //               Button btnviewProfile;
                dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.userbasicinfo);
                basicInfoPhoto = dialog.findViewById(R.id.basicInfoPhoto);
                basicInfoName = dialog.findViewById(R.id.basicInfoName);
                basicInfoProgramme = dialog.findViewById(R.id.basicInfoProgramme);
                Picasso.with(mContext).load(mData.get(position).getStudentID().getPhoto()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(basicInfoPhoto);
                basicInfoName.setText(mData.get(position).getStudentID().getStudentName());
                basicInfoProgramme.setText(mData.get(position).getStudentID().getStudentProgramme());
                //              btnviewProfile = dialog.findViewById(R.id.btnViewProfile);

                dialog.show();

            }
        });
        holder.posterName.setText(mData.get(position).getStudentID().getStudentName());
        String date = mData.get(position).getUploadDate() + " on " + mData.get(position).getUploadTime();
        holder.posterDate.setText(date);
        holder.posterCaption.setText(mData.get(position).getActivityCaption());
        if (mData.get(position).getActivityThumbnail().equals("")) {
            holder.posterThumbnail.setVisibility(View.GONE);
        } else {
            Picasso.with(mContext).load(mData.get(position).getActivityThumbnail()).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(holder.posterThumbnail);
        }


        holder.posterThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "thumbnail String =" + holder.posterThumbnail);
            }
        });

        holder.likeCondition.setText("didnt like");
        checkLike(holder, mData.get(position).getActivityID(), UserID);
        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.likeCondition.getText().toString().equals("didnt like")) {
                    getLikeID(UserID, position, holder.likeCondition.getText().toString());
                    holder.likeButton.setImageResource(R.drawable.ic_liked);
                    holder.likeCondition.setText("Liked");
                } else if (holder.likeCondition.getText().equals("Liked")) {
                    getLikeID(UserID, position, holder.likeCondition.getText().toString());
                    holder.likeButton.setImageResource(R.drawable.ic_like);
                    holder.likeCondition.setText("didnt like");
                }
            }
        });

        if (mData.get(position).getStuffID().equals("")) {
            holder.btnViewStuff.setVisibility(View.GONE);
        }

        holder.btnViewStuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getMySelectedStuff(mData.get(position).getStuffID(), v, position);
            }
        });

        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityComment frag = new ActivityComment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("activityID", mData.get(position).getActivityID());
                bundle.putSerializable("posterName", mData.get(position).getStudentID().getStudentName());
                bundle.putSerializable("activityCaption", mData.get(position).getActivityCaption());
                frag.setArguments(bundle);

                fragmentManager = ((AppCompatActivity) v.getContext()).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.update_fragmentHolder, frag)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();


            }
        });

        holder.progressBar.setVisibility(View.GONE);
        holder.activityHolder.setVisibility(View.VISIBLE);


    }

    private void getMySelectedStuff(String stuffID, final View v, final int position) {
        Log.d(TAG, "Stuff ID Clicked = " + stuffID);
        pDialog = new ProgressDialog(mContext);
        try {
            ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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

                RequestQueue queue = Volley.newRequestQueue(mContext);
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
                                        AlertDialog.Builder noItem = new AlertDialog.Builder(mContext);
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
                Toast.makeText(mContext.getApplicationContext(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext.getApplicationContext(),
                    "Error create activity:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }


    }

    private void getLikeID(String UserID, final int position, String likeCondition) {
        likeStatus = new ArrayList<>();
        pDialog = new ProgressDialog(mContext);
        try {
            ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            Boolean isConnected = networkInfo != null && networkInfo.isConnectedOrConnecting();
            if (isConnected) {

                try {
                    command = "{\"command\": \"30303530304C\", \"reserve\": \"303030303030303030303030303030303030303030303030\", " +
                            "\"StudentID\": " + "\"" + Conversion.asciiToHex(UserID) + "\" ," +
                            "\"ActivityID\": " + "\"" + Conversion.asciiToHex(mData.get(position).getActivityID()) + "\" }";
                    pahoMqttClient.publishMessage(mqttAndroidClient, command, 1, "MY/TARUC/SSS/000000001/PUB");
                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }


                RequestQueue queue = Volley.newRequestQueue(mContext);
                if (!pDialog.isShowing())
                    pDialog.setMessage("Sync with server...");
                pDialog.show();
                //http://192.168.0.108/raindown/getLikeID.php?studentID=17Wmr05969&activityID=act1003

                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getLikeID.php?studentID=" + UserID + "&activityID=" +
                        mData.get(position).getActivityID(),
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                try {
                                    String status = "";
                                    String likeID = "";
                                    Log.d(TAG, "Respond mOU");
                                    for (int i = 0; i < response.length(); i++) {
                                        JSONObject likeResponse = (JSONObject) response.get(i);
                                        likeID = likeResponse.getString("likeID");
                                        status = likeResponse.getString("likeStatus");
                                    }
                                    Log.d(TAG, "Like ID =" + likeID);
                                    Log.d(TAG, "Like Status =" + status);

                                    if (response.length() == 0) {
                                        getLastLikeID(mData.get(position).getActivityID());
                                    } else if (response.length() > 0) {
                                        updateLikeID(status, likeID);
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
                Toast.makeText(mContext.getApplicationContext(), "Network is NOT available",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(mContext.getApplicationContext(),
                    "Error create activity:" + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void updateLikeID(String status, final String likeID) {
        Log.d(TAG, "like ID status =" + status);
        String ChangeLikeStatus = "";
        if (status.equals("ACTIVE")) {
            ChangeLikeStatus = "DEACTIVE";
        } else if (status.equals("DEACTIVE")) {
            ChangeLikeStatus = "ACTIVE";
        }
        String updateLikeActivityUrl = Constant.serverFile + "updateLikeActivity.php?likeID=" + likeID +
                "&status=" + ChangeLikeStatus;
        //http://192.168.0.108/raindown/updateLikeActivity.php?likeID=like1003&status=ACTIVE
        Log.d(TAG, "Update Like URL = " + updateLikeActivityUrl);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        try {
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST,
                    updateLikeActivityUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject = null;
                            Log.d(TAG, "insert like Respond is here");
                            try {
                                jsonObject = new JSONObject(response);
                                String message = jsonObject.getString("message");
                                Toast.makeText(mContext.getApplicationContext(), message,
                                        Toast.LENGTH_LONG).show();


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

    private void getLastLikeID(final String activityID) {
        RequestQueue queue = Volley.newRequestQueue(mContext);

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getLastLikeID.php",
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            String currentLikeID = "";
                            String newLikeID = "";
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject likeIDResponse = (JSONObject) response.get(i);
                                currentLikeID = likeIDResponse.getString("CurrentLikeID");
                            }
                            Log.d(TAG, "Current Like ID =" + currentLikeID);
                            if (currentLikeID.equals("0")) {
                                newLikeID = "like1001";
                            } else {
                                String first = currentLikeID.substring(0, 4);
                                String last = currentLikeID.substring(4);
                                int number = Integer.parseInt(last) + 1;
                                newLikeID = first + Integer.toString(number);
                            }

                            Log.d(TAG, "New Like ID = " + newLikeID);

                            insertNewLikeActivity(newLikeID, activityID);


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
    }

    private void insertNewLikeActivity(final String newID, final String activityID) {
        UserID = UserSharedPreferences.read(UserSharedPreferences.userID, null);
        //192.168.0.108/raindown/insertLikeActivity.php?likeID=like1003&studentID=17WMR05969&activityID=act1003
        insertNewLikeActivityUrl = Constant.serverFile + "insertLikeActivity.php?likeID=" + newID
                + "&studentID=" + UserID + "&activityID=" + activityID;

        Log.d(TAG, "Item to be insert are like id =" + newID + ", studentID =" + UserID + ", activity ID =" + activityID);

        RequestQueue queue = Volley.newRequestQueue(mContext);
        try {
            StringRequest postRequest = new StringRequest(
                    Request.Method.POST,
                    insertNewLikeActivityUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject = null;
                            Log.d(TAG, "insert like Respond is here");
                            try {
                                jsonObject = new JSONObject(response);
                                String success = jsonObject.getString("success");
                                if (success.equals("1")) {
                                    Toast.makeText(mContext.getApplicationContext(), "Activity Liked",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(mContext.getApplicationContext(), "Oops there is a problem occur...",
                                            Toast.LENGTH_LONG).show();
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
                    params.put("likeID", newID);
                    params.put("studentID", UserID);
                    params.put("activityID", activityID);

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


    private void checkLike(final MyviewHolder holder, String actID, String UserID) {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        //http://192.168.0.108/raindown/getActivityLikeList.php?studentID=17wmr05969&activityID=act1002

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Constant.serverFile + "getActivityLikeList.php?studentID=" + UserID + "&activityID=" + actID,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        if (response.length() >= 1) {
                            holder.likeButton.setImageResource(R.drawable.ic_liked);
                            holder.likeCondition.setText("Liked");

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d("Testing respond", "Not Responding");
                    }
                });
        queue.add(jsonObjectRequest);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyviewHolder extends RecyclerView.ViewHolder {

        CardView userActivity_cardview;
        ImageView posterPhoto, posterThumbnail, commentButton, bookmarkButton;
        TextView posterName, posterDate, posterCaption, likeAmount, commentAmount, likeCondition, btnViewStuff;
        ImageView likeButton;
        ProgressBar progressBar;
        RelativeLayout activityHolder;

        public MyviewHolder(View itemView) {
            super(itemView);
            userActivity_cardview = itemView.findViewById(R.id.userActivityCardView);
            posterPhoto = itemView.findViewById(R.id.posterPhoto);
            posterThumbnail = itemView.findViewById(R.id.posterThumbnail);
            likeButton = itemView.findViewById(R.id.likeButton);
            commentButton = itemView.findViewById(R.id.commentButton);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
            posterName = itemView.findViewById(R.id.posterName);
            posterDate = itemView.findViewById(R.id.posterDate);
            posterCaption = itemView.findViewById(R.id.posterCaption);
            likeAmount = itemView.findViewById(R.id.likeAmount);
            commentAmount = itemView.findViewById(R.id.commentAmount);
            likeCondition = itemView.findViewById(R.id.likeConditon);
            btnViewStuff = itemView.findViewById(R.id.btnviewStuff);
            progressBar = itemView.findViewById(R.id.userActProgressbar);
            activityHolder = itemView.findViewById(R.id.activityHolder);


        }
    }
}
